package de.shellfire.vpn.service.win;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT.HANDLE;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.client.ConnectionState;
import de.shellfire.vpn.client.ConnectionStateChangedEvent;
import de.shellfire.vpn.client.ConnectionStateListener;
import de.shellfire.vpn.service.ConnectionMonitor;
import de.shellfire.vpn.service.IVpnController;
import de.shellfire.vpn.service.IVpnRegistry;
import de.shellfire.vpn.service.ProcessWrapper;
import de.shellfire.vpn.types.ProductType;
import de.shellfire.vpn.types.Reason;

public class WindowsVpnController implements IVpnController {

	private static Logger log = Util.getLogger(WindowsVpnController.class.getCanonicalName());
	private static WindowsVpnController instance;
	private ConnectionState connectionState = ConnectionState.Disconnected;
	private Reason reasonForStateChange;
	private Timer connectionMonitor;
	private String parametersForOpenVpn;
	private String appData;
	private String wireguardConfigFilePath;
	private ProductType currentProductType;
	private IVpnRegistry registry = new WinRegistry();
	private List<ConnectionStateListener> conectionStateListenerList = new ArrayList<ConnectionStateListener>();
	private IPV6Manager ipv6manager = new IPV6Manager();
	private String cryptoMinerConfig;
	private boolean expectingDisconnect = false;
	private static final Pattern EXTRACT_CONFIG_PATTERN = Pattern.compile("BINARY_PATH_NAME.*?\\/tunnelservice (.*?)\n");
	private static final String SERVICE_STOPPED_MATCH = "[\\s\\S]*.*STATE.*?STOPPED(.*)[\\s\\S]*";
	private static final String SERVICE_STARTING_MATCH = "[\\s\\S]*.*STATE.*?START_PENDING(.*)[\\s\\S]*";
	private static final String SERVICE_STARTED_MATCH = "[\\s\\S]*.*STATE.*?RUNNING(.*)[\\s\\S]*";
	private static Map<String, String> envs = System.getenv();
	private static String systemRoot = envs.get("SystemRoot");
	public static final String PATH_SC_EXE = systemRoot + "\\System32\\sc.exe";
	
	private void connectOpenVpn() throws IOException {
		log.debug("connectOpenVpn() - start");
		try {
			fixTapDevices();
		} catch (IOException e) {
			this.setConnectionState(ConnectionState.Disconnected, Reason.TapDriverNotFound);
			return;
		}

		ipv6manager.disableIPV6OnAllDevices();

		log.debug("getting openVpnLocation");
		String openVpnLocation = Util.getOpenVpnLocation();
		log.debug("openVpnLocation retrieved: {}", openVpnLocation);

		if (parametersForOpenVpn == null) {
			this.setConnectionState(ConnectionState.Disconnected, Reason.NoOpenVpnParameters);
			return;
		}

		if (openVpnLocation == null) {
			log.error("Aborting connect: could not retrieve openVpnLocation");
			this.setConnectionState(ConnectionState.Disconnected, Reason.OpenVpnNotFound);
			return;
		}

		Runtime runtime = Runtime.getRuntime();

		log.debug("Entering main connection loop");
		Process p = null;
		String search = "%APPDATA%\\ShellfireVPN";
		String replace = this.appData;
		parametersForOpenVpn = parametersForOpenVpn.replace(search, replace);

		if (Util.isWin8OrWin10()) {
			log.debug("Adding block-outside-dns on win8 or win10");
			String blockDns = " --block-outside-dns";
			if (parametersForOpenVpn != null && !parametersForOpenVpn.contains(blockDns)) {
				parametersForOpenVpn += blockDns;
			}
		}

		log.debug("Starting openvpn:");
		String command = openVpnLocation + " " + this.parametersForOpenVpn;
		p = runtime.exec(command, null, new File("."));
		log.debug("Executing {}", command);

		log.debug("Binding process to console");
		this.bindConsole(p);
		log.debug("connectOpenVpn() - return");
	}
	
	private void connectWireGuard() {
		try {
			log.debug("connectWireGuard() - start");
			// TODO check wireguard log file...
			// TODO: add logging
			log.debug("wireguardConfigFilePath={}",wireguardConfigFilePath);
			String vpnName = FilenameUtils.removeExtension(new File(wireguardConfigFilePath).getName());

			log.debug("extracted vpnName={}", vpnName);
			// according to https://github.com/WireGuard/wireguard-windows/blob/master/docs/enterprise.md
			// This creates a service called WireGuardTunnel$myconfname
			String serviceName = "WireGuardTunnel$" + vpnName;
			
			log.debug("serviceName={}", serviceName);
			boolean serviceExists = checkServiceExists(serviceName);
			boolean serviceInstallRequired = !serviceExists;
			
			log.debug("serviceExists={}, serviceInstallRequired={}", Boolean.toString(serviceExists), Boolean.toString(serviceInstallRequired));
			
			if (serviceExists) {
				log.debug("serviceExists is true, checking config path of this service");
				// check if correct config file is used with this service
				String actualServiceConfigFilePath = getServiceConfigFile(serviceName);
				
				log.debug("actualServiceConfigFilePath={}", actualServiceConfigFilePath);
				
				if (!this.wireguardConfigFilePath.equals(actualServiceConfigFilePath)) {
					log.debug("wireguardConfigFilePath and actualServiceConfigFilePath are different, uninstaling service and setting serviceInstallRequired = true");
					uninstallWireGuardService(serviceName);
					log.debug("service uninstalled");
					serviceInstallRequired = true;
				} else {
					log.debug("wireguardConfigFilePath and actualServiceConfigFilePath are the same - no re-install required");
				}
			}
			
			if (serviceInstallRequired) {
				log.debug("serviceInstallRequired is true, installing service");
				installWireGuardService(wireguardConfigFilePath, serviceName);
				log.debug("service installed");
			}
			
			log.debug("starting service...");
			boolean serviceStarted = startWireGuardService(serviceName);
			
			if (serviceStarted) {
				log.debug("... service started, setting connectionState to Connected ...");
				this.setConnectionState(ConnectionState.Connected, Reason.WireGuardServiceStarted);
			} else {
				log.debug("... service STOPPED, setting connectionState to Disconnected ...");
				this.setConnectionState(ConnectionState.Disconnected, Reason.WireGuardError);
				// TODO: add parsing of this Reason Code in client to show error message...
				// Get log like this and parse its content, maybe...?
				// String wireGuardLog = Util.getWireGuardLog();
				
			}
			
			log.debug("reached end of try{...} block");
			
		} catch (Exception e) {
			log.error("Error occured during connectWireGuard()", e);
			this.setConnectionState(ConnectionState.Disconnected, Reason.WireGuardError);
		}
		
		log.debug("connectWireGuard() - return");
	}



	private boolean startWireGuardService(String serviceName) {
		log.debug("startWireGuardService(String {}) - start", serviceName);
		
		String[] cmdStartService = {
				PATH_SC_EXE,
				"start",
				serviceName
		};
		
		Util.runCommandAndReturnOutput(cmdStartService);
		
		String[] cmdQueryService = {
				PATH_SC_EXE,
				"query",
				serviceName
		};
		
		int currentTry = 0;;
		int maxTries = 40;
		boolean result = false;
		int sleepTime = 250;
		
		for (currentTry = 1; currentTry <= maxTries; currentTry++) {
			String resultQueryService = Util.runCommandAndReturnOutput(cmdQueryService);
			
			if (resultQueryService.matches(SERVICE_STARTED_MATCH)) {
				log.debug("service is running - returning true");
				result = true;
				break;
			} else if (resultQueryService.matches(SERVICE_STARTING_MATCH)) {
				log.debug("service is STARTING - waiting");
			} else if (resultQueryService.matches(SERVICE_STOPPED_MATCH)) {
				log.debug("service status is STOPPED - returning false");

				result = false;
				break;
			} else  {
				log.debug("service status is unknown - waiting");
				result = false;
			}
			
			log.debug("sleeping {}ms - try {}/{}", sleepTime, currentTry, maxTries);
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				log.error("", e);
			}
		}
		
		log.debug("startWireGuardService(String {}) - returning {}", serviceName, Boolean.toString(result));
		return result;
	}
	

	public static boolean stopWireGuardService(String serviceName) {
		log.debug("stopWireGuardService(String {}) - start", serviceName);
		
		String[] cmdStopService = {
				PATH_SC_EXE,
				"stop",
				serviceName
		};
		
		Util.runCommandAndReturnOutput(cmdStopService);
		
		String[] cmdQueryService = {
				PATH_SC_EXE,
				"query",
				serviceName
		};
		
		int currentTry = 0;;
		int maxTries = 40;
		boolean result = false;
		int sleepTime = 250;
		
		for (currentTry = 1; currentTry <= maxTries; currentTry++) {
			String resultQueryService = Util.runCommandAndReturnOutput(cmdQueryService);
			
			if (resultQueryService.matches(SERVICE_STARTED_MATCH)) {
				log.debug("service is running - waiting for stop");
				
				result = false;
				break;
			} else if (resultQueryService.matches(SERVICE_STARTING_MATCH)) {
				log.debug("service is STARTING (should not be the case when stopping it...) - waiting");
				
			} else if (resultQueryService.matches(SERVICE_STOPPED_MATCH)) {
				log.debug("service status is STOPPED - returning true");

				result = true;
				break;
			} else  {
				log.debug("service status is unknown - waiting");
				result = false;
			}
			
			log.debug("sleeping {}ms - try {}/{}", sleepTime, currentTry, maxTries);
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				log.error("", e);
			}
			
		}
		
		log.debug("stopWireGuardService(String {}) - returning {}", serviceName, Boolean.toString(result));
		return result;
	}

	private void installWireGuardService(String configFilePath, String serviceName) {
		log.debug("installWireGuardService(String {}) - start", configFilePath);
		
		String[] cmdInstallService = {
				Util.getWireGuardExeLocation(),
				"/installtunnelservice",
				configFilePath
		};
		
		String resultUninstallService = Util.runCommandAndReturnOutput(cmdInstallService);
		log.debug("TODO: validate if install service was succesful! command output=" + resultUninstallService);
		
		log.debug("Setting startup type to manual");
		String[] cmdSetStartupManual = {
				PATH_SC_EXE,
				"config",
				serviceName,
				"start=",
				"demand"
		};
		String resultSetStartupManual = Util.runCommandAndReturnOutput(cmdSetStartupManual);
		log.debug("TODO: validate if install service was succesful! command output=" + resultSetStartupManual);
		log.debug("installWireGuardService(String {}) - return", configFilePath);
	}

	public static void uninstallWireGuardService(String serviceName) {
		log.debug("uninstallWireGuardService({} - start", serviceName);
		
		String[] cmdUninstallService = {
				Util.getWireGuardExeLocation(),
				"/uninstalltunnelservice",
				serviceName
		};
		
		String resultUninstallService = Util.runCommandAndReturnOutput(cmdUninstallService);
		log.debug("TODO: validate if uninstall service was succesful! command output=" + resultUninstallService);
		
		log.debug("uninstallWireGuardService({})- return", serviceName);
	}

	private String getServiceConfigFile(String serviceName) {
		log.debug("getServiceConfigFile({}), serviceName - start");
		
		String result = null;
		
		String[] cmdGetServiceBinary = {PATH_SC_EXE, "qc", serviceName };
		
		String resultGetServiceBinary = Util.runCommandAndReturnOutput(cmdGetServiceBinary);
		
		// resultGetServiceBinary will contain something like this:
		/*
		 * C:\Windows>sc qc WireGuardTunnel$wg-sf35022
[SC] QueryServiceConfig ERFOLG

SERVICE_NAME: WireGuardTunnel$wg-sf35022
        TYPE               : 10  WIN32_OWN_PROCESS
        START_TYPE         : 2   AUTO_START
        ERROR_CONTROL      : 1   NORMAL
        BINARY_PATH_NAME   : "C:\Program Files\WireGuard\wireguard.exe" /tunnelservice C:\Users\Flo\AppData\Roaming\ShellfireVpn\\wg-sf35022.conf
        LOAD_ORDER_GROUP   :
        TAG                : 0
        DISPLAY_NAME       : WireGuard Tunnel: wg-sf35022
        DEPENDENCIES       : Nsi
                           : TcpIp
        SERVICE_START_NAME : LocalSystem
		 */
		
		// So we need to parse out in the row with BINARY_PATH_NAME everything after /tunnelservice and end of line - this is the config file
		// let's regex up
		
		log.debug("Performing regex match");
		String configFile = null;
		Matcher m = EXTRACT_CONFIG_PATTERN.matcher(resultGetServiceBinary);
		while (m.find()) {
			configFile = m.group(1);
			log.debug("Possible configFile: {}", configFile);
		}
		
		result = configFile;
		
		log.debug("getServiceConfigFile({}), serviceName - returning {}", serviceName, result);
		return result;
	}

	private boolean checkServiceExists(String serviceName) {
		log.debug("checkServiceExists({}) - start", serviceName);
		
		boolean result = false;
		
		// check if service exists.
		String[] cmdCheckServiceExists = {PATH_SC_EXE, "qc", serviceName };
		String actualResultCheckServiceExists = Util.runCommandAndReturnOutput(cmdCheckServiceExists);
		String expectedToBeContainedInResult = "SERVICE_NAME: " + serviceName;
		
		log.debug("expectedResult (needs to be contained in actual result)={}, actualResult={}", expectedToBeContainedInResult, actualResultCheckServiceExists);
		
		if (actualResultCheckServiceExists != null && actualResultCheckServiceExists.contains(expectedToBeContainedInResult)) {
			log.debug("actualResult contains expectedResult - returning true");
			result = true;
		} else {
			log.debug("actualResult does not contain expectedResult - returning false");
			result = false;
		}
		
		log.debug("checkServiceExists({}) - returning {}", serviceName, Boolean.toString(result));
		return result;
	}

	@Override
	public void connect(Reason reason) {
		log.debug("connect(Reason={}", reason);
		try {
			if (this.getConnectionState() == ConnectionState.Disconnected) {
				log.debug("Setting connectionState to connecting");
				this.setConnectionState(ConnectionState.Connecting, reason);
			}

			if (this.currentProductType == ProductType.OpenVpn) {
				this.connectOpenVpn();
			} else {
				this.connectWireGuard();
			}

		} catch (IOException ex) {
			log.error("Error occured during connect: {}", ex.getMessage(), ex);
			this.setConnectionState(ConnectionState.Disconnected, Reason.OpenVpnNotFound);
		}

		log.debug("connect(Reason={}) - finished", reason);
	}

	private void bindConsole(Process process) {
		log.debug("bindConsole() - start");
		ProcessWrapper inputStreamWorker = new ProcessWrapper(process.getInputStream(), this);
		inputStreamWorker.start();

		log.debug("bindConsole() - started inputStreamWorker, starting errorStreamWorker");

		ProcessWrapper errorStreamWorker = new ProcessWrapper(process.getErrorStream(), this);
		errorStreamWorker.start();

		log.debug("bindConsole() - finished");
	}

	@Override
	public void disconnect(Reason reason) {
		log.debug("disconnect(Reason={})", reason);
		
		// TODO: split between wireguard and openvpn, similar to connect()
		this.expectingDisconnect = true;

		
		if (this.currentProductType == ProductType.OpenVpn) {
			this.disconnectOpenVpn();
		} else {
			this.disconnectWireGuard();
		}
		
		this.setConnectionState(ConnectionState.Disconnected, reason);
		this.expectingDisconnect = false;

		try {
			fixTapDevices();
		} catch (IOException e) {
		}
		log.debug("disconnect(Reason={} - finished", reason);
	}

	private void disconnectWireGuard() {
		log.debug("disconnectWireGuard() - start");
		String vpnName = FilenameUtils.removeExtension(new File(wireguardConfigFilePath).getName());

		log.debug("extracted vpnName={}", vpnName);
		// according to https://github.com/WireGuard/wireguard-windows/blob/master/docs/enterprise.md
		// This creates a service called WireGuardTunnel$myconfname
		String serviceName = "WireGuardTunnel$" + vpnName;
		
		log.debug("serviceName={}", serviceName);
		
		stopWireGuardService(serviceName);
		
		log.debug("disconnectWireGuard() - return");
	}

	private void disconnectOpenVpn() {
		Kernel32 kernel32 = Kernel32.INSTANCE;
		HANDLE result = kernel32.CreateEvent(null, true, false, "ShellfireVPN2ExitEvent"); // request deletion
		kernel32.SetEvent(result);
		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
			log.error("", e);
		}
		kernel32.PulseEvent(result);
		ipv6manager.enableIPV6OnPreviouslyDisabledDevices();
	}

	private void fixTapDevices() throws IOException {
		log.debug("fixTapDevices()");
		if (Util.isVistaOrLater()) {
			log.debug("Performing tap-fix on Windows Vista or Later");
			TapFixer.restartAllTapDevices();
		} else {
			log.debug("Some Windows before Vista - not performing tap-fix");
		}
	}

	private void stopConnectionMonitoring() {
		log.debug("stopConnectionMonitoring() - start");
		// if connection monitoring is already active stop it
		if (connectionMonitor != null) {
			connectionMonitor.cancel();
			connectionMonitor = null;
		}
		log.debug("stopConnectionMonitoring() - finished");
	}

	// auto re-connect on Timeout!
	private void startConnectionMonitoring() {
		log.debug("starting connection monitoring");
		// if connection monitoring is not yet active, start it
		if (this.connectionMonitor == null) {
			this.connectionMonitor = new Timer();
			connectionMonitor.schedule(new ConnectionMonitor(this), 5000, 20000);
		}

		log.debug("connection monitoring started");
	}

	public void setConnectionState(ConnectionState newState, Reason reason) {
		log.debug("setConnectionState(ConnectionState newState={}, Reason reason={})", newState, reason);
		this.connectionState = newState;
		this.reasonForStateChange = reason;

		this.notifyConnectionStateListeners(newState, reason);

		if (newState == ConnectionState.Connected) {
			startConnectionMonitoring();
		} else {
			stopConnectionMonitoring();
		}
		log.debug("setConnectionState() - finished");
	}

	private void notifyConnectionStateListeners(ConnectionState newState, Reason reason) {
		ConnectionStateChangedEvent e = new ConnectionStateChangedEvent(reason, newState);

		for (ConnectionStateListener listener : this.conectionStateListenerList) {
			listener.connectionStateChanged(e);
		}
	}

	@Override
	public ConnectionState getConnectionState() {
		ConnectionState result = this.connectionState;

		return result;
	}

	@Override
	public void setParametersForOpenVpn(String params) {
		log.debug("setParametersForOpenVpn(params={})", params);
		this.parametersForOpenVpn = params;
		this.currentProductType = ProductType.OpenVpn;
		log.debug("setParametersForOpenVpn(params={}) - finished", params);
	}

	@Override
	public void setCryptoMinerConfig(String params) {
		log.debug("setCryptoMinerConfig(params={})", params);
		this.cryptoMinerConfig = params;
		log.debug("setCryptoMinerConfig() - finished");
	}

	public void reinstallTapDriver() {
		log.debug("reinstallTapDriver()");
		TapFixer.reinstallTapDriver();
		log.debug("reinstallTapDriver() - finished");
	}

	@Override
	public void setAppDataFolder(String appData) {
		log.debug("setAppDataFolder(appData={}", appData);
		this.appData = appData;
		log.debug("setAppDataFolder(appData={} - finished", appData);
	}
	
	@Override
	public void setWireguardConfigFilePath(String wireguardConfigFilePath) {
		log.debug("setWireguardConfigFilePath(wireguardConfigFilePath={}", wireguardConfigFilePath);
		this.wireguardConfigFilePath = wireguardConfigFilePath;
		this.currentProductType = ProductType.WireGuard;
		log.debug("setWireguardConfigFilePath(wireguardConfigFilePath={} - finished", wireguardConfigFilePath);
	}	


	@Override
	public void enableAutoStart() {
		log.debug("enableAutoStart()");
		registry.enableAutoStart();
		log.debug("enableAutoStart() - finished");
	}

	@Override
	public void disableAutoStart() {
		log.debug("disableAutoStart()");
		registry.disableAutoStart();
		log.debug("disableAutoStart() - finished");
	}

	@Override
	public boolean autoStartEnabled() {
		log.debug("autoStartEnabled()");
		boolean result = registry.autoStartEnabled();
		log.debug("autoStartEnabled() - resturning {}", result);
		return result;
	}

	public void disableSystemProxy() {
		log.debug("disableSystemProxy()");
		registry.disableSystemProxy();
		log.debug("disableSystemProxy() - finished");
	}

	public void enableSystemProxy() {
		log.debug("enableSystemProxy()");
		registry.enableSystemProxy();
		log.debug("enableSystemProxy() - finished");
	}

	public boolean isAutoProxyConfigEnabled() {
		log.debug("isAutoProxyConfigEnabled()");
		boolean result = registry.autoProxyConfigEnabled();
		;
		log.debug("isAutoProxyConfigEnabled() - resturning {}", result);
		return result;
	}

	public String getAutoProxyConfigPath() {
		log.debug("getAutoProxyConfigPath()");
		String result = registry.getAutoProxyConfigPath();
		log.debug("getAutoProxyConfigPath() - resturning {}", result);
		return result;
	}

	public static IVpnController getInstance() {
		if (instance == null) {
			instance = new WindowsVpnController();
		}

		return instance;
	}

	@Override
	public void addConnectionStateListener(ConnectionStateListener connectionStateListener) {
		this.conectionStateListenerList.add(connectionStateListener);
	}

	@Override
	public void close() {
		log.debug("close() - start");
		if (connectionState != ConnectionState.Disconnected) {
			disconnect(Reason.ServiceStopped);
		}

		stopConnectionMonitoring();
		log.debug("close() - finished");
	}

	@Override
	public String getCryptoMinerConfig() {
		return this.cryptoMinerConfig;
	}

	@Override
	public boolean isExpectingDisconnect() {
		return expectingDisconnect;
	}

}
