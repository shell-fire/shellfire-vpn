package de.shellfire.vpn;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.Security;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.ImageIcon;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.LoggerFactory;
import org.xnap.commons.i18n.I18n;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import de.shellfire.vpn.gui.LoginForms;
import de.shellfire.vpn.gui.controller.ShellfireVPNMainFormFxmlController;
import de.shellfire.vpn.gui.helper.ExceptionThrowingReturningRunnableImpl;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.messaging.UserType;
import de.shellfire.vpn.service.CryptFactory;
import de.shellfire.vpn.service.IVpnRegistry;
import de.shellfire.vpn.service.win.WinRegistry;
import javafx.scene.control.Alert;

public class Util {
	private static final String SHELLFIRE_VPN = "shellfire-vpn" + File.separator;
	private static IVpnRegistry registry;
	public static String GOOGLE_DE = null;
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	static String isoToday = sdf.format(new Date());
	static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

	private static String Arch;
	private static boolean firstGetLoggerCall = true;
	private static Object semaphore = new Object();
	public static UserType userType = null;
	private static Properties properties;
	private static String configDir;

	private static Map<String, String> envs = System.getenv();
	private static String programFiles = envs.get("ProgramFiles");
	private static String programFiles86 = envs.get("ProgramFiles(x86)");
	private static String programW6432 = envs.get("ProgramW6432");

	
	public static final String POWERSHELL_EXE = "%SystemRoot%\\system32\\WindowsPowerShell\\v1.0\\powershell.exe";
	
	private static Logger log = Util.getLogger(Util.class.getCanonicalName());
	private static HashMap<String, javafx.scene.image.Image> imageIconCacheMap = new HashMap<String, javafx.scene.image.Image>();
	static {
		semaphore = new Object();
		Security.setProperty("networkaddress.cache.ttl", "1");
		Security.setProperty("networkaddress.cache.negative.ttl", "1");
	}

	public static void openUrl(String url) {
		try {
			URI website = new URI(url);
			Util.openUrl(website);
		} catch (Exception e) {
		}
	}

	public static void openUrl(URI url) {
		Browser.browse(url.toString());
	}

	public static void openUrl(URL url) {
		String address = url.toString();
		Util.openUrl(address);

	}

	public static void handleException(Exception ex) {
		log.error(ex.getMessage(), ex);

		ex.printStackTrace();
		Throwable t = ex.getCause();
		String msg = "";
		if (t != null) {
			msg = t.getClass().getSimpleName() + ": " + t.getLocalizedMessage();
		}

		else {
			msg = ex.getLocalizedMessage();
		}

		// JOptionPane.showMessageDialog(null, i18n.tr("Action could not be completed, an error occured:") + "\n" + msg,
		// i18n.tr("Error"), JOptionPane.ERROR_MESSAGE);
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle(i18n.tr("Error"));
		alert.setContentText(i18n.tr("Action could not be completed, an error occured:") + "\n" + msg);
		alert.showAndWait();
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

	public static String runCommandAndReturnOutput(List<String> command) {
		return runCommandAndReturnOutput(null, command);
	}

	public static String runCommandAndReturnOutput(String writeToProc, List<String> command) {
		String[] array = new String[command.size()];
		command.toArray(array);
		return runCommandAndReturnOutput(writeToProc, array);
	}
	
	public static String runCommandAndReturnOutput(String... command) {
		return runCommandAndReturnOutput(null, command);
	}
	
	public static String runCommandAndReturnOutput(String writeToProc, String... command) {
		log.debug("Running command: {} and then writing to stdIn {}", Arrays.asList(command), writeToProc);
		final StringBuffer result = new StringBuffer();
		try {
			ProcessBuilder pb = new ProcessBuilder(command);
			pb.redirectErrorStream(true);
			Process proc = pb.start();

			final BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

			if (writeToProc != null) {
				final BufferedWriter stdOutput = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));
				stdOutput.write(writeToProc);
				stdOutput.flush();
				stdOutput.close();
			}
			
			String newLine = "";
			try {
				while ((newLine = stdInput.readLine()) != null) {
					result.append(new String(newLine));
					result.append("\n");
				}
			} catch (IOException e) {
				Util.handleException(e);
			}
			
			proc.waitFor();

		} catch (IOException e) {
			Util.handleException(e);
		} catch (InterruptedException e) {
			Util.handleException(e);
		}

		log.debug("Received result:" + result);
		return result.toString();
	}

	public static String getArchitecture() {
		if (Util.Arch == null) {
			if (isWindows()) {
				String[] cmds = new String[] { "reg", "query", "HKLM\\System\\CurrentControlSet\\Control\\Session Manager\\Environment",
						"/v", "PROCESSOR_ARCHITECTURE" };
				String result = runCommandAndReturnOutput(cmds);

				String[] lines = result.split("\n");
				String second = lines[lines.length - 1];
				String[] parts = second.split(" ");
				String arch = parts[parts.length - 1].trim();

				Util.Arch = arch;

			} else {
				Util.Arch = "(unknown-mac)";
			}
		}

		return Util.Arch;
	}

	public static String getCmdExe() {
		String sysDir = System.getenv("SystemRoot") + "\\system32\\cmd.exe";

		return sysDir;
	}

	public static String getWmicExe() {
		String wmic = System.getenv("SystemRoot") + "\\system32\\wbem\\wmic.exe";

		return wmic;
	}

	public static String getCscriptExe() {
		String cscript = System.getenv("SystemRoot") + "\\system32\\cscript.exe";

		return cscript;
	}

	public static float getOsVersion() {
		String osVersion = System.getProperty("os.version");
		log.debug("os.verson=" + osVersion);

		if (isWindows())
			return Float.parseFloat(osVersion);
		else
			return 0;
	}

	public static boolean isVistaOrLater() {
		if (!isWindows())
			return false;

		float version = getOsVersion();
		log.debug("{}", version);
		return isWindows() && version >= 6.0F;
	}

	public static boolean isWin8OrWin10() {
		if (!isWindows())
			return false;

		float version = getOsVersion();
		log.debug("{}", version);
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

			if (!file.exists()) {
				file.createNewFile();
			}

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
		if (Util.configDir == null) {
			String result;
			if (isWindows())
				result = System.getenv("APPDATA") + "\\ShellfireVpn\\";
			else {
				result = System.getProperty("user.home") + "/Library/Application Support/Shellfire VPN/";

				if (!result.startsWith("/var/root")) {
					File f = new File(result);
					if (!f.exists()) {
						f.mkdirs();
					}
				}

			}

			log.debug("Config dir set to: {}", result);
			Util.configDir = result;
		}

		return Util.configDir;
	}
	
	public static String getDownloadDir() {
		String configDir = getConfigDir();
		String downloadDir = configDir + "download\\";
		
		File downloadDirFile = new File(downloadDir);
		if (!downloadDirFile.exists()) {
			downloadDirFile.mkdirs();
		}
		
		return downloadDir;
	}

	public static String getOpenVpnLocation() {
		log.debug("getOpenVpnLocation() - start");

		List<String> possibleOpenVpnExeLocations = Util.getPossibleOpenVpnExeLocations(programFiles, programFiles86, programW6432);

		for (String possibleLocation : possibleOpenVpnExeLocations) {
			File f = new File(possibleLocation);
			if (f.exists()) {
				log.debug("getOpenVpnLocation() - returning " + possibleLocation);
				return possibleLocation;
			}

		}
		log.debug("getOpenVpnLocation() - returning null: OPENVPN NOT FOUND!");
		return null;
	}
	

	public static String getWireGuardExeLocation() {
		log.debug("getWireGuardExeLocation() - start");

		List<String> possibleWireGuardVpnExeLocations = Util.getPossibleWireGuardExeLocations(programFiles, programFiles86, programW6432);

		for (String possibleLocation : possibleWireGuardVpnExeLocations) {
			File f = new File(possibleLocation);
			if (f.exists()) {
				log.debug("getWireGuardExeLocation() - returning " + possibleLocation);
				return possibleLocation;
			}

		}
		log.debug("getWireGuardExeLocation() - returning null: WIREGUARD NOT FOUND!");
		return null;
	}	

	public static String getWGExeLocation() throws Exception {
		log.debug("getWGExeLocation() - start");

		List<String> possibleWireGuardVpnExeLocations = Util.getPossibleWGExeLocations(programFiles, programFiles86, programW6432);

		for (String possibleLocation : possibleWireGuardVpnExeLocations) {
			File f = new File(possibleLocation);
			if (f.exists()) {
				log.debug("getWireGuardExeLocation() - returning " + possibleLocation);
				return possibleLocation;
			} else {
				log.debug("getWireGuardExeLocation() does not exist: " + possibleLocation);
			}

		}
		log.debug("getWGExeLocation() - returning null: WIREGUARD NOT FOUND!");
		throw new Exception("WireGuard Not found");
	}	
	
	public static String getWireGuardLog() {
		log.debug("getWireGuardLog() - start");
		
		String wireGuardLogPath = Util.getLogFilePathWireguard();
		
		String[] cmdDumpLog = {
				Util.getWireGuardExeLocation(),
				"/dumplog",
				wireGuardLogPath
		};
		
		Util.runCommandAndReturnOutput(cmdDumpLog);

		String result = null;
		
		File wireGuardLogFile = new File(wireGuardLogPath);
		if (wireGuardLogFile.exists()) {
			try {
				result = Util.fileToString(wireGuardLogPath);
			} catch (IOException e) {
				log.error("Could not read wireguard.log file from " + wireGuardLogPath, e);
			}
		}
		
		log.debug("getWireGuardLog() - return");
		
		return result;
		
	}

	public static List<String> getPossibleOpenVpnExeLocations(String programFiles, String programFiles86, String programW6432) {
		return Arrays.asList(
				"openvpn\\openvpn.exe", 
				"..\\openvpn\\openvpn.exe", 
				
				programFiles + "\\ShellfireVPN\\openvpn\\openvpn.exe", 
				programFiles86 + "\\ShellfireVPN\\openvpn\\openvpn.exe", 
				programW6432 + "\\ShellfireVPN\\openvpn\\openvpn.exe", 
				
				programFiles + "\\OpenVPN\\openvpn.exe"
				,programFiles86 + "\\OpenVPN\\openvpn.exe",
				programW6432 + "\\OpenVPN\\openvpn.exe", 
				
				programFiles + "\\ShellfireVPN\\bin\\openvpn.exe", 
				programFiles86 + "\\ShellfireVPN\\bin\\openvpn.exe", 
				programW6432 + "\\ShellfireVPN\\bin\\openvpn.exe"
			);
	}
	public static List<String> getPossibleWireGuardExeLocations(String programFiles, String programFiles86, String programW6432) {
		return Arrays.asList(
				"wireguard\\wireguard.exe", 
				"..\\wireguard\\wireguard.exe", 

				programFiles + "\\ShellfireVPN\\wireguard\\wireguard.exe",
				programFiles86 + "\\ShellfireVPN\\wireguard\\wireguard.exe",
				programW6432 + "\\ShellfireVPN\\wireguard\\wireguard.exe",
				
				programFiles + "\\WireGuard\\wireguard.exe", 
				programFiles86 + "\\WireGuard\\wireguard.exe", 
				programW6432 + "\\WireGuard\\wireguard.exe"
			);
	}
	public static List<String> getPossibleWGExeLocations(String programFiles, String programFiles86, String programW6432) {
		return Arrays.asList(
				"wireguard\\wg.exe", 
				"..\\wireguard\\wg.exe", 

				programFiles + "\\ShellfireVPN\\wireguard\\wg.exe",
				programFiles86 + "\\ShellfireVPN\\wireguard\\wg.exe",
				programW6432 + "\\ShellfireVPN\\wireguard\\wg.exe",
				
				programFiles + "\\WireGuard\\wg.exe", 
				programFiles86 + "\\WireGuard\\wg.exe", 
				programW6432 + "\\WireGuard\\wg.exe"
			);		
	}

	public static String getSeparator() {
		if (isWindows())
			return "\\";
		else
			return "/";
	}

	public static boolean isMacOs() {
		String os = System.getProperty("os.name");
		log.debug("os.name = " + os);
		return os.toLowerCase().contains("mac os");
	}

	public static String listToString(List<String> cmds) {
		String result = "";
		for (String cmd : cmds) {
			result += cmd + " § ";
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

		public boolean isCancellled();
		public void cancel();
	}

	public static <T> T runWithAutoRetry(ExceptionThrowingReturningRunnableImpl<T> runnable, int maxTries, int delayMs) {
		return runWithAutoRetry(runnable, maxTries, delayMs, null);
	}

	public static <T> T runWithAutoRetry(ExceptionThrowingReturningRunnableImpl<T> runnable, int maxTries, int delayMs, Class clazzToIgnore) {
		int numTries = 0;
		Exception e = null;
		T result;

		while (numTries++ < maxTries && !runnable.isCancellled()) {
			if (numTries > 1)
				log.debug("runWithAutoRetry(try " + numTries + " / " + maxTries);

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
			super("ReachableWithTimeout");
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
				log.debug("not reachable " + numTries + " / 5");
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
			log.debug("ReachableWithTimeout has finished - returning result " + reach.result);
			return reach.result;
		} else {
			log.debug("ReachableWithTimeout has NOT finished - returning false");
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
		log.debug("Testing if connection exists");
		Boolean networkIsAvailable = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnableImpl<Boolean>() {
			public Boolean run() throws Exception {
				List<String> siteList = new ArrayList<String>();
				siteList.add("www.google.de");
				siteList.add("www.google.com");
				siteList.add("www.shellfire.de");
				siteList.add("www.yahoo.de");
				siteList.add("www.bing.com");

				for (String site : siteList) {
					if (Util.isReachableWithTimeout(site)) {
						log.debug("site " + site + " is reachable");
						return true;
					} else {
						log.debug("site " + site + " NOT reachable - sleeping 3 seconds");
						Thread.sleep(3000);
					}
				}
				log.debug("no known site reachable - returning false");

				return false;

			}
		}, 5, 50);

		boolean result = (networkIsAvailable == null) ? false : networkIsAvailable;

		return result;
	}

	/**
	 * This should only be instantiated from the process host with root privileges
	 * 
	 * @return
	 */
	public static IVpnRegistry getRegistry() {
		if (registry == null) {
			try {
				registry = new WinRegistry();
			} catch (Exception e) {
				e.printStackTrace();
				Util.handleException(e);
			}
		}

		return registry;
	}

	public static Logger getLogger(String className) {

		synchronized (semaphore) {
			if (firstGetLoggerCall) {
				cleanUpLog();
				firstGetLoggerCall = false;
			}
		}

		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		PatternLayoutEncoder ple = new PatternLayoutEncoder();

		ple.setPattern("%date %level [%thread] %logger{10} [%file:%line] %msg%n");
		ple.setContext(lc);
		ple.start();
		FileAppender<ILoggingEvent> fileAppender = new FileAppender<ILoggingEvent>();

		String file = getLogFilePath();
		fileAppender.setFile(file);
		fileAppender.setEncoder(ple);
		fileAppender.setContext(lc);
		fileAppender.start();

		Logger logger = (Logger) LoggerFactory.getLogger(className);
		logger.addAppender(fileAppender);
		logger.setLevel(Level.DEBUG);
		logger.setAdditive(true);

		return logger;
	}

	public static synchronized void cleanUpLog() {
		String logPath = getLogFilePath();
		File logFile = new File(logPath);
		logFile.delete();
		// log.debug("cleanUpLog() - finished deleting {} - logFile.exists(): {}", logPath, logFile.exists());
	}

	public static UserType getUserType() {
		if (userType == null) {
			userType = UserType.Client;
			String userTypeFromCommandLine = System.getProperty("de.shellfire.vpn.runtype");
			if (userTypeFromCommandLine != null && userTypeFromCommandLine.length() > 0) {
				userType = UserType.valueOf(userTypeFromCommandLine);
			}

		}

		return userType;
	}

	public static String getLogFilePath() {
		return getLogFilePath(Util.getUserType());
	}

	public static String getLogFilePath(UserType userType) {
		String result = getTempDir() + userType.name() + ".log";
		return result;
	}
	
	private static String getLogFilePathWireguard() {
		String result = getTempDir() + "wireguard.log";
		return result;
	}

	public static String getLogFilePathInstaller() {
		String jarFile = Util.getPathJar();
		if (jarFile == null) {
			return "";
		}
		File instDir = new File(jarFile).getParentFile();

		String result = instDir + "\\install.log";
		return result;
	}

	public static String encodeBase64(String string) {
		return new String(Base64.encodeBase64(string.getBytes()));
	}

	public static String getTempDir() {
		String result = "";
		if (Util.isWindows()) {
			// Some Windows versions have user specific temp files, so always use C:\Temp
			result = System.getenv("SystemDrive") + "\\Temp\\" + SHELLFIRE_VPN;
		} else {
			result = "/tmp/" + SHELLFIRE_VPN;
		}

		return result;
	}

	public static String getJavaHome() {
		return System.getProperty("java.home");
	}

	public static String getPathJar() throws IllegalStateException {
		Class<?> context = LoginForms.class;
		String rawName = context.getName();
		log.debug(rawName);
		String classFileName;
		/* rawName is something like package.name.ContainingClass$ClassName. We need to turn this into ContainingClass$ClassName.class. */
		{
			int idx = rawName.lastIndexOf('.');
			classFileName = (idx == -1 ? rawName : rawName.substring(idx + 1)) + ".class";
		}

		String uri = context.getResource(classFileName).toString();
		log.debug(uri);
		if (uri.startsWith("file:") || !uri.startsWith("jar:file:")) {
			return null;
		}

		int idx = uri.indexOf('!');

		try {
			String fileName = URLDecoder.decode(uri.substring("jar:file:".length(), idx), Charset.defaultCharset().name());
			return new File(fileName).getAbsolutePath();
		} catch (UnsupportedEncodingException e) {
			throw new InternalError("default charset doesn't exist. Your VM is borked.");
		}
	}

	public static void sleep(int i) {
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {
		}
	}

	public static String getJvmDll() {
		if (jvmDll == null) {
			String javaHome = Util.getJavaHome();

			String template = javaHome + "\\bin\\%s\\jvm.dll";
			String clientDllPath = String.format(template, "client");

			if (new File(clientDllPath).exists()) {
				jvmDll = clientDllPath;
			}

			if (jvmDll == null) {
				String serverDllPath = String.format(template, "server");
				if (new File(serverDllPath).exists()) {
					jvmDll = serverDllPath;
				}
			}

		}

		return jvmDll;
	}

	public static ImageIcon getImageIcon(String resourceName) {
		return getImageIcon(resourceName, 1);
	}

	public static ImageIcon getImageIcon(String resourceName, double d) {
		ImageIcon imageIcon = new javax.swing.ImageIcon(ShellfireVPNMainFormFxmlController.class.getResource(resourceName));
		return imageIcon;
	}

	public static javafx.scene.image.Image getImageIconFX(String resourceName) {
		javafx.scene.image.Image imageIcon = imageIconCacheMap.get(resourceName);
		if (imageIcon != null) {
			return imageIcon;
		}
		
		imageIcon = getImageIconFX(resourceName, 1);
		imageIconCacheMap.put(resourceName, imageIcon);
		
		return imageIcon;
	}

	public static javafx.scene.image.Image getImageIconFX(String resourceName, double d) {
		InputStream stream = LoginForms.class.getResourceAsStream(resourceName);
		javafx.scene.image.Image image = new javafx.scene.image.Image(stream);
		return image;
	}


	public static void chmod(String filePath, String permissions) {
		String[] params = new String[] { "/bin/chmod", "-R", permissions, filePath };
		log.debug("setting permissions " + params[2] + " on " + params[3]);
		try {
			Process p2 = new ProcessBuilder(params).start();
			Util.digestProcess(p2);
			p2.waitFor();
		} catch (IOException e) {
			log.error("IOException during " + Util.listToString(Arrays.asList(params)), e);

		} catch (InterruptedException e) {
			log.error("InterruptedException during " + Util.listToString(Arrays.asList(params)), e);
		}
	}

	public static void makeFilePublicReadWritable(String filePath) {
		Util.chmod(filePath, "777");

	}

	public static void makeFilePublicReadable(String filePath) {
		Util.chmod(filePath, "755");

	}

	public static String fileMd5Sum(String filePath) {
		try {
			FileInputStream fis = new FileInputStream(new File(filePath));
			String md5 = DigestUtils.md5Hex(fis);
			fis.close();
			return md5;
		} catch (IOException e) {
			log.error("Error occured while trying to compute md5 sum of file", e);
		}

		return null;
	}
	public static String getWireGuardFileNameConfig(String vpnName) {
		String filePath = Util.getConfigDir() + "\\wg-" + vpnName + ".conf";
		
		return filePath;
	}
	
	public static String getWireGuardPublicKeyUser(String vpnName) throws Exception {
		VpnProperties props = VpnProperties.getInstance();
		String pubKey = props.getProperty("wg-pubkey-"+vpnName, null);
		
		if (pubKey == null) {
			Util.generateWireGuardKeyPair(vpnName);
			
			pubKey = props.getProperty("wg-pubkey-"+vpnName, null);
			
			if (pubKey == null) {
				throw new Exception("Could not retrieve keypair for "+vpnName+", pubKey still null after Util.generateWireGuardKeyPair()");
			}
		}

		return pubKey;
	}
	
	public static String getWireGuardPrivateKeyUser(String vpnName) throws Exception {
		VpnProperties props = VpnProperties.getInstance();
		String privKey = props.getProperty("wg-privkey-"+vpnName, null);
		
		if (privKey == null) {
			Util.generateWireGuardKeyPair(vpnName);
			
			privKey = props.getProperty("wg-privkey-"+vpnName, null);
			
			if (privKey == null) {
				throw new Exception("Could not retrieve keypair for "+vpnName+", privKey still null after Util.generateWireGuardKeyPair()");
			}
		}

		privKey = CryptFactory.decrypt(privKey);
		return privKey;
	}

	private static void generateWireGuardKeyPair(String vpnName) throws Exception {
		log.debug("generateWireGuardKeyPair vpnName={} - start", vpnName);
		String wgPath = Util.getWGExeLocation();
		
		String[] cmdPrivKey = {wgPath, "genkey"};
		String privKey = Util.runCommandAndReturnOutput(cmdPrivKey).trim();		
		
		VpnProperties props = VpnProperties.getInstance();
		props.setProperty("wg-privkey-"+vpnName, CryptFactory.encrypt(privKey));
		
		String[] cmdPubKey = {wgPath, "pubkey"};
		String pubKey = Util.runCommandAndReturnOutput(privKey+"\n", cmdPubKey).trim();
		props.setProperty("wg-pubkey-"+vpnName, pubKey);
		
		log.debug("generateWireGuardKeyPair - done - stored keypair in VpnProperties privKey={}/pubKey={}", privKey, pubKey);
	}

	// do not mix this order around, must remain in the end of class so that log file can be deleted on startup
	private static I18n i18n = VpnI18N.getI18n();
	private static String jvmDll;

	static class Browser {
	    public static void browse(String url) {

	        if(Desktop.isDesktopSupported()){
	            Desktop desktop = Desktop.getDesktop();
	            try {
	                desktop.browse(new URI(url));
	            } catch (IOException | URISyntaxException e) {
	                Util.handleException(e);
	            }
	        }else{
	            Runtime runtime = Runtime.getRuntime();
	            try {
	            	runtime.exec("rundll32 url.dll,FileProtocolHandler " + url);
	            } catch (IOException e) {
	            	Util.handleException(e);
	            }
	        }
	    }
	}
	
}
