package de.shellfire.vpn.messaging;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.regex.Pattern;

import org.slf4j.Logger;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.client.ConnectionState;
import de.shellfire.vpn.client.ConnectionStateListener;
import de.shellfire.vpn.service.IVpnRegistry;
import de.shellfire.vpn.service.win.IPV6Manager;
import de.shellfire.vpn.service.win.WinRegistry;
import de.shellfire.vpn.service.win.WindowsVpnController;
import de.shellfire.vpn.types.ProductType;
import de.shellfire.vpn.types.Reason;

public class TestService {
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
	private static final String SERVICE_STARTED_MATCH = ".*STATE.*?RUNNING \r\n.*";
	private static final String SERVICE_STARTING_MATCH = ".*STATE.*?START_PENDING \r\n.*";
	private static final String SERVICE_STOPPED_MATCH = "[\\s\\S]*.*STATE.*?STOPPED(.*)[\\s\\S]*";
	
	
	private static Map<String, String> envs = System.getenv();
	private static String systemRoot = envs.get("SystemRoot");
	private static final String PATH_SC_EXE = systemRoot + "\\System32\\sc.exe";
	
	
	
	public static void main(String[] args) {
		String[] cmdQueryService = {
				PATH_SC_EXE,
				"query",
				"WireGuardTunnel$wg-sf35022"
		};
		
		String resultQueryService = Util.runCommandAndReturnOutput(cmdQueryService);
		
		if (resultQueryService.matches(SERVICE_STARTED_MATCH)) {
			System.out.println("Matches started");
		} else if (resultQueryService.matches(SERVICE_STARTING_MATCH)) {
			System.out.println("Matches starting");			
		} else if (resultQueryService.matches(SERVICE_STOPPED_MATCH)) {
			System.out.println("Matches stopped");
		} else {
			System.out.println("Matches nothing known");
		}
		
		
	}
}
