package de.shellfire.vpn.service.win;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;

import de.shellfire.vpn.Util;

public class IPV6Manager {

	private static final String LIB_PREFIX = "lib/";
	private static final String LIB_SUFFIX_X86 = "_x86.dll";
	private static final String LIB_SUFFIX_AMD64 = "_amd64.dll";
	private final static String[] GET_ADAPTER_LIST = new String[] { Util.getWmicExe(), "nic", "get", "NetConnectionID" };

	Pattern p = Pattern.compile(".*\\{(.*)\\}");

	private static Logger log = Util.getLogger(IPV6Manager.class.getCanonicalName());
	private final static String IPV6_MANAGE = "%s /%s \"%s\" ms_tcpip6";
	private static String nvspBindLocation;
	private static LinkedList<String> disabledAdapterList;

	private final static String SUCCESS = "finished (0)";

	static void loadLibSpecial(String libName, boolean doLoad) {
		log.debug("loadLibSpecial {} - start", libName);
		String jvmArch = System.getProperty("sun.arch.data.model");

		String x86lib = LIB_PREFIX + libName + LIB_SUFFIX_X86;
		String amd64lib = LIB_PREFIX + libName + LIB_SUFFIX_AMD64;
		String lib = null;

		if (jvmArch.equals("32")) {
			lib = x86lib;
		} else if (jvmArch.equals("64")) {
			lib = amd64lib;
		} else {
			lib = x86lib;
			log.warn("Could not determin architecture of jvm - trying to load 32 bit version");
		}

		Path libPath = FileSystems.getDefault().getPath(lib);
		Path libPathDest = FileSystems.getDefault().getPath(libName + ".dll");
		try {
			log.info("copying {} to {}", libPath, libPathDest);
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

	public void enableIPV6OnPreviouslyDisabledDevices() {
		log.debug("enableIPV6OnPreviouslyDisabledDevices() - start");

		if (!Util.isVistaOrLater()) {
			log.warn("Not performing IPV6 fix on Windows XP");
			return;
		}

		String nvspbind = getNvspBindLocation();
		if (nvspbind == null) {
			log.warn("nvspbind not found - did not enable ipv6 on any devices");
		} else {
			if (disabledAdapterList == null) {
				log.warn("no adapters have been disabled yet. doing nothing.");
			} else {
				for (String adapter : disabledAdapterList) {
					String[] enableCommand = String.format(IPV6_MANAGE, nvspBindLocation, "e", adapter).split(" ");
					String result = Util.runCommandAndReturnOutput(enableCommand);
					if (result.contains(SUCCESS)) {
						log.debug("succesfully enabled ipv6 on {}", adapter);
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
				String[] enableCommand = String.format(IPV6_MANAGE, nvspBindLocation, "e", adapter).split(" ");
				String result = Util.runCommandAndReturnOutput(enableCommand);
				if (result.contains(SUCCESS)) {
					log.debug("succesfully enabled ipv6 on {}", adapter);
				}
			}
		} else {
			log.warn("nvspbind not found - did not enable ipv6 on any devices");
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
			disabledAdapterList = new LinkedList<String>();
			for (String adapter : adapterList) {
				String[] enableCommand = String.format(IPV6_MANAGE, nvspBindLocation, "d", adapter).split(" ");
				String result = Util.runCommandAndReturnOutput(enableCommand);
				if (result.contains(SUCCESS)) {
					log.debug("succesfully disabled ipv6 on {} - adding to list", adapter);
					disabledAdapterList.add(adapter);
				}

			}
		} else {
			log.warn("nvspbind not found - did not disable ipv6 on any devices");
		}

		log.debug("disableIPV6() - finish");
	}

	private List<String> getAdapterList() {
		log.debug("getAdapterList() - start");

		String output = Util.runCommandAndReturnOutput(GET_ADAPTER_LIST);

		List<String> result = new LinkedList<String>();
		String[] lines = output.split("\\n");
		for (String line : lines) {
			line = line.trim();

			if (line.length() > 0) {
				result.add(line);
			}
		}

		log.debug("getAdapterList() - finished, returning {}", result);
		return result;
	}

	private String getNvspBindLocation() {
		if (nvspBindLocation == null) {
			Map<String, String> envs = System.getenv();
			String programFiles = envs.get("ProgramFiles");
			String programFiles86 = envs.get("ProgramFiles(x86)");
			List<String> possibleLocations = Arrays.asList("nvspbind\\nvspbind.exe", "..\\nvspbind\\nvspbind.exe",
					programFiles + "\\ShellfireVPN\\nvspbind\\nvspbind.exe", programFiles86 + "\\ShellfireVPN\\nvspbind\\nvspbind.exe",
					programFiles + "\\nvspbind\\nvspbind.exe", programFiles86 + "\\nvspbind\\nvspbind.exe",
					programFiles + "\\ShellfireVPN\\bin\\nvspbind.exe", programFiles86 + "\\ShellfireVPN\\bin\\nvspbind.exe");

			for (String location : possibleLocations) {
				if (new File(location).exists()) {
					nvspBindLocation = location;
					return location;
				}
			}
			log.error("Did not find nvspbind. Looked in these places unsuccesfully: {}", possibleLocations);
		}

		return nvspBindLocation;
	}

}
