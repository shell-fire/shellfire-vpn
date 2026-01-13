package de.shellfire.vpn.service.win;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;

import de.shellfire.vpn.Util;

/**
 * IPV6-(Un)Bind Manager – jetzt versions-übergreifend:
 *
 * <ul>
 *   <li>Windows ≤ 10 → <code>wmic.exe nic get NetConnectionID</code></li>
 *   <li>Windows 11 / Systeme ohne <code>wmic.exe</code> → PowerShell-Fallback
 *       (<code>Get-CimInstance …</code>)</li>
 * </ul>
 *
 * Keine weiteren Änderungen an der nvspbind-Logik;
 * das bisherige „<code>split(" ")</code>“ bleibt, weil nvspbind selbst
 * die Adaptertokens wieder zusammensetzt.
 */
public class IPV6Manager {

    /*------------------------------------------------------------------*/
    /*                      Konstanten / Felder                         */
    /*------------------------------------------------------------------*/

    private static final String LIB_PREFIX        = "lib/";
    private static final String LIB_SUFFIX_X86    = "_x86.dll";
    private static final String LIB_SUFFIX_AMD64  = "_amd64.dll";

    /** Kopfzeilen, die aus <code>wmic</code> oder PowerShell gefiltert werden. */
    private static final List<String> HEADER_TOKENS =
            Arrays.asList("NetConnectionID", "Name");

    /** Kommando-Template für nvspbind. */
    private static final String IPV6_MANAGE = "%s /%s \"%s\" ms_tcpip6";

    private static final String SUCCESS = "finished (0)";

    private static Logger             log                 =
            Util.getLogger(IPV6Manager.class.getCanonicalName());
    private static String             nvspBindLocation;
    private static LinkedList<String> disabledAdapterList;

    Pattern p = Pattern.compile(".*\\{(.*)\\}");

    /*------------------------------------------------------------------*/
    /*                           Bibliotheken                           */
    /*------------------------------------------------------------------*/

    static void loadLibSpecial(String libName, boolean doLoad) {
        log.debug("loadLibSpecial {} - start", libName);
        String jvmArch = System.getProperty("sun.arch.data.model");

        String x86lib   = LIB_PREFIX + libName + LIB_SUFFIX_X86;
        String amd64lib = LIB_PREFIX + libName + LIB_SUFFIX_AMD64;
        String lib;

        if ("32".equals(jvmArch)) {
            lib = x86lib;
        } else if ("64".equals(jvmArch)) {
            lib = amd64lib;
        } else {
            lib = x86lib;
            log.warn("Could not determine architecture of JVM – loading 32-bit version");
        }

        Path libPath     = FileSystems.getDefault().getPath(lib);
        Path libPathDest = FileSystems.getDefault().getPath(libName + ".dll");
        try {
            log.info("Copying {} to {}", libPath, libPathDest);
            Files.copy(libPath, libPathDest, REPLACE_EXISTING);
        } catch (IOException e) {
            Util.handleException(e);
        }
        if (doLoad) {
            log.debug("Now loading library {}", libName);
            System.loadLibrary(libName);
        }

        log.debug("loadLibSpecial {} - finished", libName);
    }

    /*------------------------------------------------------------------*/
    /*           Öffentliche Ein-/Ausschalt-Methoden (IPv6)             */
    /*------------------------------------------------------------------*/

    public void enableIPV6OnPreviouslyDisabledDevices() {
        log.debug("enableIPV6OnPreviouslyDisabledDevices() - start");

        if (!Util.isVistaOrLater()) {
            log.warn("Not performing IPV6 fix on Windows XP");
            return;
        }

        String nvspbind = getNvspBindLocation();
        if (nvspbind == null) {
            log.warn("nvspbind not found – did not enable IPv6 on any devices");
        } else {
            if (disabledAdapterList == null) {
                log.warn("No adapters have been disabled yet. Doing nothing.");
            } else {
                for (String adapter : disabledAdapterList) {
                    String[] enableCommand =
                            String.format(IPV6_MANAGE, nvspBindLocation, "e", adapter).split(" ");
                    String result = Util.runCommandAndReturnOutput(enableCommand);
                    if (result.contains(SUCCESS)) {
                        log.debug("Successfully enabled IPv6 on {}", adapter);
                    }
                }
            }
        }

        log.debug("enableIPV6OnPreviouslyDisabledDevices() - finish");
    }

    public void enableIPV6OnAllDevices() {
        log.debug("enableIPV6() - start");

        List<String> adapterList = getAdapterList();

        String nvspbind = getNvspBindLocation();
        if (nvspbind != null) {
            for (String adapter : adapterList) {
                String[] enableCommand =
                        String.format(IPV6_MANAGE, nvspBindLocation, "e", adapter).split(" ");
                String result = Util.runCommandAndReturnOutput(enableCommand);
                if (result.contains(SUCCESS)) {
                    log.debug("Successfully enabled IPv6 on {}", adapter);
                }
            }
        } else {
            log.warn("nvspbind not found – did not enable IPv6 on any devices");
        }

        log.debug("enableIPV6() - finish");
    }

    public void disableIPV6OnAllDevices() {
        log.debug("disableIPV6() - start");

        if (!Util.isVistaOrLater()) {
            log.warn("Not performing IPV6 fix on Windows XP");
            return;
        }

        String nvspbind = getNvspBindLocation();
        if (nvspbind != null) {
            List<String> adapterList = getAdapterList();
            disabledAdapterList      = new LinkedList<>();
            for (String adapter : adapterList) {
                String[] disableCommand =
                        String.format(IPV6_MANAGE, nvspBindLocation, "d", adapter).split(" ");
                String result = Util.runCommandAndReturnOutput(disableCommand);
                if (result.contains(SUCCESS)) {
                    log.debug("Successfully disabled IPv6 on {} – adding to list", adapter);
                    disabledAdapterList.add(adapter);
                }
            }
        } else {
            log.warn("nvspbind not found – did not disable IPv6 on any devices");
        }

        log.debug("disableIPV6() - finish");
    }

    /*------------------------------------------------------------------*/
    /*                    Interne Helfer-Funktionen                     */
    /*------------------------------------------------------------------*/

    /**
     * Liefert die Liste aller Netzadapter (Anzeigename), unabhängig davon,
     * ob <code>wmic.exe</code> auf dem System vorhanden ist.
     */
    private List<String> getAdapterList() {
        log.debug("getAdapterList() - start");

        String[] cmd   = buildAdapterListCommand();
        String   output = Util.runCommandAndReturnOutput(cmd);

        List<String> result = new LinkedList<>();
        for (String line : output.split("\\R")) {      // \R = jedes Zeilenende
            line = line.trim();
            if (line.isEmpty() || HEADER_TOKENS.contains(line)) {
                continue;                             // Header & Leerzeilen ausfiltern
            }
            result.add(line);
        }

        log.debug("getAdapterList() - finished, returning {}", result);
        return result;
    }

    /**
     * Baut den Plattform-abhängigen Befehl:
     * <ol>
     *   <li><code>wmic nic get NetConnectionID</code> – falls <code>wmic.exe</code> existiert</li>
     *   <li>PowerShell / pwsh-Fallback mit <code>Get-CimInstance</code></li>
     * </ol>
     */
    private static String[] buildAdapterListCommand() {
        Path wmicExe = Paths.get(System.getenv("SystemRoot"),
                                 "System32", "wbem", "wmic.exe");
        if (Files.isRegularFile(wmicExe)) {
            return new String[] {
                    wmicExe.toString(), "nic", "get", "NetConnectionID"
            };
        }

        // Fallback: PowerShell (5.1) oder pwsh (7.x)
        String ps = findPowerShellExecutable();
        return new String[] {
                ps,
                "-NoProfile",
                "-ExecutionPolicy", "Bypass",
                "-Command",
                "Get-CimInstance -ClassName Win32_NetworkAdapter | " +
                "Where-Object { $_.NetConnectionID } | " +
                "Select-Object -ExpandProperty NetConnectionID"
        };
    }

    /**
     * Liefert den ersten existierenden PowerShell-Interpreter oder
     * schlicht „powershell“, falls PATH reicht.
     */
    private static String findPowerShellExecutable() {
        List<Path> candidates = Arrays.asList(
                Paths.get(System.getenv().getOrDefault("ProgramFiles",
                                "C:\\Program Files"),
                          "PowerShell", "7", "pwsh.exe"),
                Paths.get(System.getenv("SystemRoot"),
                          "System32", "WindowsPowerShell", "v1.0", "powershell.exe"),
                Paths.get(System.getenv("SystemRoot"),
                          "SysWOW64", "WindowsPowerShell", "v1.0", "powershell.exe")
        );

        for (Path p : candidates) {
            if (Files.isRegularFile(p)) {
                return p.toString();
            }
        }
        return "powershell";     // Fallback: rely on PATH
    }

    /**
     * Sucht nvspbind.exe in mehreren typischen Installations­pfaden.
     */
    private String getNvspBindLocation() {
        if (nvspBindLocation == null) {
            Map<String, String> envs        = System.getenv();
            String programFiles             = envs.get("ProgramFiles");
            String programFiles86           = envs.get("ProgramFiles(x86)");
            List<String> possibleLocations  = Arrays.asList(
                    "nvspbind\\nvspbind.exe",
                    "..\\nvspbind\\nvspbind.exe",
                    programFiles     + "\\ShellfireVPN\\nvspbind\\nvspbind.exe",
                    programFiles86   + "\\ShellfireVPN\\nvspbind\\nvspbind.exe",
                    programFiles     + "\\nvspbind\\nvspbind.exe",
                    programFiles86   + "\\nvspbind\\nvspbind.exe",
                    programFiles     + "\\ShellfireVPN\\bin\\nvspbind.exe",
                    programFiles86   + "\\ShellfireVPN\\bin\\nvspbind.exe"
            );

            for (String location : possibleLocations) {
                if (location != null && new File(location).exists()) {
                    nvspBindLocation = location;
                    return location;
                }
            }
            log.error("Did not find nvspbind. Looked in these places unsuccessfully: {}",
                      possibleLocations);
        }
        return nvspBindLocation;
    }
}
