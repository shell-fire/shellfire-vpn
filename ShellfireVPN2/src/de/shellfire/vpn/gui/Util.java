package de.shellfire.vpn.gui;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.Security;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.JOptionPane;

import org.xnap.commons.i18n.I18n;

import de.shellfire.vpn.rmi.Console;
import de.shellfire.vpn.rmi.IVpnRegistry;
import de.shellfire.vpn.rmi.LogStreamReader;
import de.shellfire.vpn.rmi.OpenVpnProcessHost;
import de.shellfire.vpn.rmi.WinRegistry;

public class Util {

  private static String LOG_FORMAT = "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$-6s %2$s %5$s%6$s%n";
  private static I18n i18n = VpnI18N.getI18n();
  private static IVpnRegistry registry;
  public static String GOOGLE_DE = null;
  private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
  private static String isoToday = sdf.format(new Date());
  private static Logger log = Util.getLogger(Util.class);
  
  static {
    Security.setProperty("networkaddress.cache.ttl", "1");
    Security.setProperty("networkaddress.cache.negative.ttl", "1");
    System.setProperty("java.util.logging.SimpleFormatter.format", LOG_FORMAT);
  }

  public static void openUrl(String url) {
    try {
      URI website = new URI(url);
      Util.openUrl(website);
    } catch (Exception e) {
    }
  }

  public static void openUrl(URI url) {
    Desktop desktop = Desktop.getDesktop();
    try {
      desktop.browse(url);
    } catch (IOException e) {
    }
  }

  public static void openUrl(URL url) {
    String address = url.toString();
    Util.openUrl(address);

  }

  public static void handleException(Exception ex) {
    Util.getLogger(Util.class).log(Level.SEVERE, ex.getMessage(), ex);
    ex.printStackTrace();
    Throwable t = ex.getCause();
    String msg = "";
    if (t != null) {
      t.printStackTrace();
      msg = t.getClass().getSimpleName() + ": " + t.getLocalizedMessage();
      // msg += "\n\n"+Util.getStackTrace(t);
    }

    else {
      msg = ex.getLocalizedMessage();
      // msg += "\n\n"+Util.getStackTrace(ex);
    }

    JOptionPane.showMessageDialog(null, i18n.tr("Vorgang konnte nicht ausgeführt werden, da ein Fehler aufgetreten ist:") + "\n" + msg,
        i18n.tr("Fehler"), JOptionPane.ERROR_MESSAGE);

  }

  public static String getStackTrace(Throwable t) {

    Throwable cur = t;
    String stacktrace = "--- Exception Details ---\r\n";
    while (cur != null) {
      StringWriter sw = new StringWriter();
      t.printStackTrace(new PrintWriter(sw));
      stacktrace += t.getMessage() + "\r\n" + sw.toString();

      cur = t.getCause();
      if (cur != null) {
        stacktrace += "\r\n-- Caused by -- \r\n";
      }
    }

    return stacktrace;
  }

  public static String runCommandAndReturnOutput(String command) {
    System.out.println("Running command: " + command);
    StringBuffer result = new StringBuffer();
    try {
      Runtime rt = Runtime.getRuntime();
      Process proc;

      proc = rt.exec(command);

      BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
      BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
      String s = null;

      while ((s = stdInput.readLine()) != null) {
        result.append(s);
        result.append("\n");
      }
      while ((s = stdError.readLine()) != null) {
      }

    } catch (IOException e) {

      return Util.getStackTrace(e);
    }

    System.out.println("Received result:" + result);
    return result.toString();
  }

  public static String getArchitecture() {
    if (isWindows()) {
      String cmd = "reg query \"HKLM\\System\\CurrentControlSet\\Control\\Session Manager\\Environment\" /v PROCESSOR_ARCHITECTURE";
      String result = runCommandAndReturnOutput(cmd);

      String[] lines = result.split("\n");
      String second = lines[lines.length - 1];
      String[] parts = second.split(" ");
      String arch = parts[parts.length - 1].trim();

      return arch;

    } else {
      return "(unknown-mac)";
    }
  }

  public static String getCmdExe() {
    String sysDir = System.getenv("SystemRoot") + "\\system32\\cmd.exe";

    return sysDir;
  }

  public static float getOsVersion() {
    String osVersion = System.getProperty("os.version");
    System.out.println("os.verson=" + osVersion);

    if (isWindows())
      return Float.parseFloat(osVersion);
    else
      return 0;
  }

  public static boolean isVistaOrLater() {
    if (!isWindows())
      return false;

    float version = getOsVersion();
    System.out.println(version);
    return isWindows() && version >= 6.0F;
  }

  public static boolean isWin8OrWin10() {
    if (!isWindows())
      return false;

    float version = getOsVersion();
    System.out.println(version);
    return isWindows() && version >= 6.20F;

  }

  public static String getTaskKillCommand() {
    if (isVistaOrLater())
      return "taskkill";
    else
      return "tskill";
  }

  public static boolean is7OrLater() {
    float version = getOsVersion();

    return isWindows() && version >= 6.1F;
  }

  public static void digestProcess(Process p) {
    LogStreamReader isr = new LogStreamReader(p.getInputStream(), false);
    Thread thread = new Thread(isr);
    thread.start();

    LogStreamReader esr = new LogStreamReader(p.getErrorStream(), true);
    Thread thread2 = new Thread(esr);
    thread2.start();

  }

  public static void stringToFile(String content, String fileName) {
    stringToFile(content, fileName, true);
  }

  public static void stringToFile(String content, String fileName, boolean overwrite) {
    try {
      File file = new File(fileName);
      if (file.exists() && !overwrite)
        return;

      FileWriter fw = new FileWriter(file);
      fw.write(content);
      fw.close();
    } catch (IOException e) {
      handleException(e);
    }

  }

  public static boolean isWindows() {
    String os = System.getProperty("os.name");
    return os.toLowerCase().contains("windows");
  }

  public static String getConfigDir() {
    String result;
    if (isWindows())
      result = System.getenv("APPDATA") + "\\ShellfireVpn";
    else {
      result = System.getProperty("user.home") + File.separator + "/Library/Application Support/Shellfire VPN";

      if (!result.startsWith("/var/root")) {
        File f = new File(result);
        if (!f.exists()) {
          f.mkdirs();
        }
      }

    }

    return result;
  }

  public static List<String> getPossibleExeLocations(String programFiles, String programFiles86) {
    if (isWindows()) {
      return Arrays.asList("openvpn\\openvpn.exe", "..\\openvpn\\openvpn.exe", programFiles + "\\ShellfireVPN\\openvpn\\openvpn.exe",
          programFiles86 + "\\ShellfireVPN\\openvpn\\openvpn.exe", programFiles + "\\OpenVPN\\openvpn.exe",
          programFiles86 + "\\OpenVPN\\openvpn.exe", programFiles + "\\ShellfireVPN\\bin\\openvpn.exe",
          programFiles86 + "\\ShellfireVPN\\bin\\openvpn.exe");
    } else {
      return Arrays.asList("openvpn/openvpn", com.apple.eio.FileManager.getPathToApplicationBundle() + "/Contents/Java/openvpn/openvpn");
    }

  }

  public static String getSeparator() {
    if (isWindows())
      return "\\";
    else
      return "/";
  }

  public static boolean isMacOs() {
    String os = System.getProperty("os.name");
    System.out.println("os.name = " + os);
    return os.toLowerCase().contains("mac os");
  }

  public static String listToString(List<String> cmds) {
    String result = "";
    for (String cmd : cmds) {
      result += cmd + " ";
    }

    return result;
  }

  public static String fileToString(String filename) throws FileNotFoundException, IOException {
    BufferedReader reader = new BufferedReader(new FileReader(filename));
    String line = null;
    StringBuilder stringBuilder = new StringBuilder();
    String ls = System.getProperty("line.separator");

    while ((line = reader.readLine()) != null) {
      stringBuilder.append(line);
      stringBuilder.append(ls);
    }

    return stringBuilder.toString();
  }

  public interface ExceptionThrowingReturningRunnable<T> {
    public T run() throws Exception;
  }

  public static <T> T runWithAutoRetry(ExceptionThrowingReturningRunnable<T> runnable, int maxTries, int delayMs) {
    return runWithAutoRetry(runnable, maxTries, delayMs, null);
  }

  public static <T> T runWithAutoRetry(ExceptionThrowingReturningRunnable<T> runnable, int maxTries, int delayMs, Class clazzToIgnore) {
    int numTries = 0;
    Exception e = null;
    T result;

    while (numTries++ < maxTries) {
      if (numTries > 1)
        Console.getInstance().append("runWithAutoRetry(try " + numTries + " / " + maxTries);

      try {
        result = runnable.run();
        e = null;
        return result;
      } catch (Exception e2) {
        e = e2;
        try {
          Thread.sleep(delayMs);
          delayMs *= 1.6;
        } catch (InterruptedException e1) {
        }
      }
    }

    if (e != null && clazzToIgnore != null && !clazzToIgnore.isInstance(e)) {
      Util.handleException(e);
    }

    return null;
  }

  private static class ReachableWithTimeout extends Thread {

    private boolean finished = false;
    private boolean result = false;
    private String site;

    public ReachableWithTimeout(String site) {
      this.site = site;
    }

    public void run() {
      result = isReachable(site);
      finished = true;
    }

  }

  public static boolean isReachableWithTimeoutAutoRetry(String site) {
    boolean result = false;

    int numTries = 0;
    while (!result && numTries++ < 5) {
      result = isReachableWithTimeout(site);
      if (!result) {
        Console.getInstance().append("not reachable " + numTries + " / 5");
        try {
          Thread.sleep(5000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }

    return result;
  }

  public static boolean isReachableWithTimeout(String site) {

    ReachableWithTimeout reach = new ReachableWithTimeout(site);
    reach.start();

    int timeout = 10;

    int timePassed = 0;

    while (reach.finished == false && timePassed < timeout * 1000) {

      try {
        Thread.sleep(50);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      timePassed += 50;
    }

    if (reach.finished) {
      Console.getInstance().append("ReachableWithTimeout has finished - returning result " + reach.result);
      return reach.result;
    } else {
      Console.getInstance().append("ReachableWithTimeout has NOT finished - returning false");
      reach.stop();
      return false;
    }
  }

  public static boolean isReachable(String site) {
    Socket socket = null;
    boolean reachable = false;
    try {
      socket = new Socket(site, 80);
      reachable = true;
    } catch (IOException e) {
      reachable = false;
    } finally {
      if (socket != null) {
        try {
          socket.close();
        } catch (IOException e) {
        }
      }
    }

    if (site.equals("www.google.de") && reachable) {
      try {
        Util.GOOGLE_DE = InetAddress.getByName(site).getHostAddress();
      } catch (UnknownHostException e) {

      }
    }

    return reachable;
  }

  public static boolean internetIsAvailable() {
    Boolean networkIsAvailable = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<Boolean>() {
      public Boolean run() throws Exception {
        List<String> siteList = new ArrayList<String>();
        siteList.add("www.google.de");
        siteList.add("www.google.com");
        siteList.add("www.shellfire.de");
        siteList.add("www.yahoo.de");
        siteList.add("www.bing.com");

        for (String site : siteList) {
          if (Util.isReachableWithTimeout(site)) {
            VpnConsole.getInstance().append("site " + site + " is reachable");
            return true;
          } else {
            VpnConsole.getInstance().append("site " + site + " NOT reachable - sleeping 3 seconds");
            Thread.sleep(3000);
          }
        }
        VpnConsole.getInstance().append("no known site reachable - returning false");

        return false;

      }
    }, 5, 50);

    boolean result = (networkIsAvailable == null) ? false : networkIsAvailable;

    return result;
  }

  public static void main(String args[]) {
    String arch = Util.getArchitecture();
    System.out.println(arch);
  }

  /**
   * This should only be instantiated from the process host with root privileges
   * 
   * @return
   */
  public static IVpnRegistry getRegistry() {
    if (registry == null) {
      if (Util.isWindows()) {
        try {
          registry = new WinRegistry();
        } catch (Exception e) {
          e.printStackTrace();
          Util.handleException(e);
        }

      } else {
        registry = new MacRegistry();
      }
    }

    return registry;
  }

  public static Logger getLogger(Class class1) {
    Logger logger = Logger.getLogger(class1.getCanonicalName());
    FileHandler fh;
    try {
      String type = OpenVpnProcessHost.IS_SERVICE ? "Service" : "Client";
      
      String logdir = Util.getConfigDir() + File.separator + "log" + File.separator;
      File f = new File(logdir);
      if (!f.exists()) {
        f.mkdirs();
      }
      
      fh = new FileHandler(logdir + type + "_" + Util.isoToday + "_.log");
      logger.addHandler(fh);
      SimpleFormatter formatter = new SimpleFormatter();
      fh.setFormatter(formatter);
    } catch (SecurityException e) {
      Util.handleException(e);
    } catch (IOException e) {
      Util.handleException(e);
    }

    logger.log(Level.FINE, "Logging to: {0}", getConfigDir());

    return logger;
  }

}
