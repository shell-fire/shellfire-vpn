package de.shellfire.vpn.webservice;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.exception.VpnException;

import de.shellfire.vpn.gui.helper.ExceptionThrowingReturningRunnableImpl;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.messaging.UserType;
import de.shellfire.vpn.proxy.ProxyConfig;
import de.shellfire.vpn.types.Server;
import de.shellfire.vpn.types.VpnProtocol;
import de.shellfire.vpn.webservice.model.LoginResponse;
import de.shellfire.vpn.webservice.model.TrayMessage;
import de.shellfire.vpn.webservice.model.VpnAttributeList;
import de.shellfire.vpn.webservice.model.WsFile;
import de.shellfire.vpn.webservice.model.WsGeoPosition;
import de.shellfire.vpn.webservice.model.WsHelpItem;
import de.shellfire.vpn.webservice.model.WsServer;
import de.shellfire.vpn.webservice.model.WsVpn;
import javafx.scene.image.Image;

/**
 * 
 * @author bettmenn
 */
public class WebService {

	public static final String CONFIG_DIR = Util.getConfigDir();
	private static Logger log = Util.getLogger(WebService.class.getCanonicalName());

	private String user;
	private String pass;
	private ServerList servers;
	private Vpn selectedVpn;
	private int allowedServer = 0;
	private WsGeoPosition ownPosition;
	private LinkedList<Vpn> vpns = new LinkedList<Vpn>();
	private VpnAttributeList vpnAttributeList;
	private List<TrayMessage> trayMessages;
	private String urlPasswordLost;
	private String urlPremiumInfo;
	private String urlHelp;
	private String urlSuccesfulConnect;
	private static I18n i18n = VpnI18N.getI18n();
	private static WebService instance;
	WebServiceBroker shellfire = WebServiceBroker.getInstance();
	private boolean initialized;
	private String cryptoMinerConfig;
	private List<String> cryptoCurrencyVpn;
	private ExceptionThrowingReturningRunnableImpl<Boolean> runnableSendLogToShellfire;
	private List<WsHelpItem> helpItemList;

	private WebService() {

	}

	private void init() {
		if (!initialized) {
			initialized = true;
			updateWebServiceEndPointList();

			log.debug("Not yet initialized - intiializing - finished");
		}
	}

	/**
	 * performs a log in with the specified account data. if no vpn exists yet for this account, it will be created automatically.
	 * 
	 * @param user
	 *            username
	 * @param pass
	 *            password
	 * @return the number of vpns in this account. 0 if login failed.
	 */
	public Response<LoginResponse> login(final String user, final String pass) {
		log.debug("starting login request");
		init();
		Response<LoginResponse> result = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnableImpl<Response<LoginResponse>>() {
			public Response<LoginResponse> run() throws Exception {
				return shellfire.login(user, pass);
			}
		}, 3, 100);

		log.debug("LoginResult received");

		if (shellfire.isLoggedIn()) {
			try {
				log.debug("Load vpn details - start");
				this.loadVpnDetails();
				log.debug("Load vpn details - finished");
			} catch (VpnException e) {
				result.setMessage(i18n.tr("VPN data could not be loaded."));
			}
		}

		return result;
	}

	private void loadVpnDetails() throws VpnException {
		init();
		List<WsVpn> wsVpns = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnableImpl<List<WsVpn>>() {
			public List<WsVpn> run() throws Exception {

				return shellfire.getAllVpnDetails();
			}
		}, 3, 100);

		this.vpns = new LinkedList<Vpn>();

		for (WsVpn curWsVpn : wsVpns) {
			Vpn vpn = new Vpn(curWsVpn);
			this.vpns.add(vpn);
		}

		if (this.vpns.size() == 1) {
			this.selectVpn(this.vpns.getFirst());
		} else {
		}
		
		if (this.selectedVpn != null) {
			log.debug("reloading already selectedVpn");
			this.selectVpn(this.getVpnById(this.selectedVpn.getVpnId()));
			
		}
	}

	public void selectVpn(Vpn vpn) {
		if (this.vpns.contains(vpn)) {
			this.selectedVpn = this.vpns.get(this.vpns.indexOf(vpn));
			this.selectedVpn.loadServerObject(this.getServerList());
		}
	}

	public boolean selectVpn(int vpnId) {
		Vpn vpn = this.getVpnById(vpnId);
		if (vpn != null) {
			this.selectVpn(vpn);
			return true;
		} else {
			return false;
		}
	}

	public boolean vpnSelectionRequired() {
		if (this.selectedVpn == null && this.vpns.size() > 1)
			return true;
		else
			return false;
	}

	public ServerList getServerList() {
		init();
		if (this.servers == null || this.servers.getNumberOfServers() == 0) {
			List<WsServer> list = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnableImpl<List<WsServer>>() {
				public List<WsServer> run() throws Exception {

					return shellfire.getServerList();
				}
			}, 3, 100);
			servers = new ServerList(list);
		}

		return this.servers;
	}
	

	public List<WsHelpItem> getHelpDetails() {
		
		init();
		if (this.helpItemList == null || this.helpItemList.size() == 0) {
			List<WsHelpItem> list = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnableImpl<List<WsHelpItem>>() {
				public List<WsHelpItem> run() throws Exception {

					return shellfire.getHelpDetails();
				}
			}, 3, 100);
			this.helpItemList = list;
		}

		return this.helpItemList;
	}

	public Vpn getVpn() {
		if (this.selectedVpn == null && WebServiceBroker.isLoggedIn()) {
			try {
				this.loadVpnDetails();
			} catch (VpnException ex) {
				Util.handleException(ex);
			}
		}

		return selectedVpn;
	}

	private int getVpnId() {
		if (this.selectedVpn != null)
			return this.selectedVpn.getVpnId();
		else
			return 0;
	}

	public boolean setServerTo(final Server server) {
		init();

		Boolean result = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnableImpl<Boolean>() {
			public Boolean run() throws Exception {

				return shellfire.setServerTo(getVpnId(), server.getServerId());
			}
		}, 3, 100);

		if (result == null || result == false) {
			return false;
		} else {
			this.getVpn().setServer(server);
			return true;
		}
	}

	public boolean setProtocolTo(final VpnProtocol protocol) {
		init();

		Boolean res = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnableImpl<Boolean>() {
			public Boolean run() throws Exception {

				return shellfire.setProtocolTo(getVpnId(), protocol.toString());
			}
		}, 3, 100);

		if (res == true) {
			return true;
		} else {
			return false;
		}
	}

	public boolean setWireGuardPublicKeyUser(final String pubKey) {
		init();

		Boolean res = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnableImpl<Boolean>() {
			public Boolean run() throws Exception {
				Boolean result = shellfire.setWireGuardPublicKeyUser(getVpnId(), pubKey);
				loadVpnDetails();
				return result;
			}
		}, 3, 100);
		if (res != null && res == true) {
			return true;
		} else {
			return false;
		}
	}

	public String getParametersForOpenVpn() {
		init();

		String params = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnableImpl<String>() {
			public String run() throws Exception {

				return shellfire.getParametersForOpenVpn(getVpnId());
			}
		}, 3, 100);

		String proxyCommand = ProxyConfig.getOpenVpnConfigCommand();
		if (proxyCommand != null) {
			params += " " + proxyCommand;
		}

		params = params + " --register-dns";
		
		return params;

	}

	public void downloadAndStoreCertificates() {
		init();

		List<WsFile> files;
		files = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnableImpl<List<WsFile>>() {
			public List<WsFile> run() throws Exception {

				return shellfire.getCertificatesForOpenVpn(getVpnId());
			}
		}, 3, 100);

		createConfigDirIfNotExists();
		if (files != null) {
			for (WsFile wsFile : files) {
				this.storeFile(wsFile);
			}
		}
	}

	public static void createConfigDirIfNotExists() {
		File configDir = new File(CONFIG_DIR);
		configDir.mkdirs();

	}

	private void storeFile(WsFile wsFile) {
		this.storeFile(wsFile.getName(), wsFile.getContent());
	}

	private void storeFile(String name, String content) {
		String filePath = WebService.CONFIG_DIR + Util.getSeparator() + name;

		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(filePath));
			out.write(content);
			out.close();
		} catch (IOException ex) {
			Util.handleException(ex);
		}

	}

	public String getLocalIpAddress() {
		init();

		String ip = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnableImpl<String>() {
			public String run() throws Exception {
				return shellfire.getLocalIpAddress();
			}
		}, 3, 100);

		if (ip == null)
			ip = i18n.tr("unknown");

		return ip;
	}

	public WsGeoPosition getOwnPosition() {
		init();

		if (this.ownPosition == null) {

			this.ownPosition = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnableImpl<WsGeoPosition>() {
				public WsGeoPosition run() throws Exception {
					return shellfire.getLocalLocation();
				}
			}, 3, 100);

		}

		return this.ownPosition;
	}

	public Response<LoginResponse> registerNewFreeAccount(final String text, final String password, boolean subscribeNewsletter) {
		return registerNewFreeAccount(text, password, subscribeNewsletter, false);
	}
			
	
	public Response<LoginResponse> registerNewFreeAccount(final String text, final String password, boolean subscribeNewsletter, boolean isResend) {
		init();

		final int subscribe = subscribeNewsletter ? 1 : 0;
		final int resend = isResend ? 1 : 0;
		Response<LoginResponse> result = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnableImpl<Response<LoginResponse>>() {
			public Response<LoginResponse> run() throws Exception {
				return shellfire.register(text, password, subscribe, resend);
			}
		}, 3, 100);

		return result;
	}

	public boolean accountActive() {
		init();

		Boolean accountActive = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnableImpl<Boolean>() {
			public Boolean run() throws Exception {
				return shellfire.getIsActive();
			}
		}, 3, 100);

		return accountActive;
	}

	public boolean isLoggedIn() {
		return WebServiceBroker.isLoggedIn();
	}

	public LinkedList<Vpn> getAllVpn() {
		return this.vpns;
	}

	private Vpn getVpnById(int rememberedVpnSelection) {
		if (this.vpns != null) {
			for (Vpn curVpn : this.vpns) {
				if (curVpn.getVpnId() == rememberedVpnSelection)
					return curVpn;
			}
		}

		return null;
	}

	public boolean certificatesDownloaded() {
		int vpnId = this.getVpnId();
		String[] filesRequired = new String[] { WebService.CONFIG_DIR, WebService.CONFIG_DIR + "\\sf" + vpnId + ".crt",
				WebService.CONFIG_DIR + "\\sf" + vpnId + ".key", WebService.CONFIG_DIR + "\\ca.crt" };

		for (String file : filesRequired) {
			File f = new File(file);
			if (!f.isFile())
				return false;
		}

		return true;
	}

	public VpnAttributeList getVpnComparisonTable() {
		init();

		if (this.vpnAttributeList == null) {
			vpnAttributeList = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnableImpl<VpnAttributeList>() {
				public VpnAttributeList run() throws Exception {
					return shellfire.getComparisonTableData();
				}
			}, 3, 100);
		}

		return this.vpnAttributeList;
	}

	public List<TrayMessage> getTrayMessages() {
		init();

		if (this.trayMessages == null) {

			List<TrayMessage> messageList = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnableImpl<List<TrayMessage>>() {
				public List<TrayMessage> run() throws Exception {
					return shellfire.getTrayMessages();
				}
			}, 3, 100);

			trayMessages = messageList;
		}

		return this.trayMessages;
	}

	/*
	 * public WsUpgradeResult upgradeVpnToPremiumWithSerial(final String productKey) { WsUpgradeResult result = Util.runWithAutoRetry(new
	 * ExceptionThrowingReturningRunnableImpl<WsUpgradeResult>() { public WsUpgradeResult run() throws Exception { return
	 * shellfire.upgradeVpnToPremiumWithCobiCode(selectedVpn.getVpnId(), productKey); } }, 3, 100);
	 * 
	 * return result; }
	 */

	public int getLatestVersion() {
		init();

		Integer latestVersion = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnableImpl<Integer>() {
			public Integer run() throws Exception {
				return shellfire.getLatestVersion();
			}
		}, 3, 100);

		if (latestVersion == null)
			return 0;
		else
			return latestVersion;
	}

	public String getLatestInstaller() {
		init();

		String latestZipInstaller;

		latestZipInstaller = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnableImpl<String>() {
			public String run() throws Exception {
				return shellfire.getLatestInstaller();
			}
		}, 3, 100);

		if (latestZipInstaller == null)
			latestZipInstaller = "";

		return latestZipInstaller;
	}

	public static WebService getInstance() {
		if (instance == null)
			instance = new WebService();

		return instance;
	}

	public String getUrlSuccesfulConnect() {
		init();

		if (urlSuccesfulConnect == null) {
			urlSuccesfulConnect = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnableImpl<String>() {
				public String run() throws Exception {
					return shellfire.getUrlSuccesfulConnect();
				}
			}, 3, 100);

		}

		return urlSuccesfulConnect;
	}

	private void updateWebServiceEndPointList() {
		init();

		List<String> endPointList = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnableImpl<List<String>>() {
			public List<String> run() throws Exception {
				return shellfire.getWebServiceEndPointList();
			}
		}, 3, 100);

		EndpointManager.getInstance().setEndPointList(endPointList);

	}

	public String getUrlHelp() {
		init();

		if (urlHelp == null) {
			urlHelp = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnableImpl<String>() {
				public String run() throws Exception {
					return shellfire.getUrlHelp();
				}
			}, 3, 100);
		}

		return urlHelp;
	}
	

	public String getServerBackgroundImageFilename(int serverId) {
		init();

		String filename = null;
		filename = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnableImpl<String>() {
			public String run() throws Exception {
				return shellfire.getServerBackgroundImageFilename(serverId);
			}
		}, 3, 100);

		return filename;
	}
	
	public Image getServerBackgroundImage(int serverId) {
		init();

		Image image = null;
		
		String url = "https://www.shellfire.de/webservice/serverImage.php?iServerId=" + serverId;

		image = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnableImpl<Image>() {
			public Image run() throws Exception {
				Image image = new Image(url);
				if (image == null || image.getException() != null) {
					if (image.getException() != null) {
						log.error("error during loading of image from website", image.getException());
					}
					throw new Exception("could not load image");
				}
					
				
				return image;
			}
		}, 3, 2000);

		return image;
	}


	public String getUrlPremiumInfo() {
		init();

		if (urlPremiumInfo == null) {
			urlPremiumInfo = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnableImpl<String>() {
				public String run() throws Exception {
					return shellfire.getUrlPremiumInfo();
				}
			}, 3, 100);
		}

		return urlPremiumInfo;
	}

	public String getUrlPasswordLost() {
		init();

		if (urlPasswordLost == null) {
			urlPasswordLost = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnableImpl<String>() {
				public String run() throws Exception {
					return shellfire.getUrlPasswordLost();
				}
			}, 3, 100);
		}

		return urlPasswordLost;
	}

	public String getCryptoMinerConfig() {
		init();

		if (cryptoMinerConfig == null) {
			cryptoMinerConfig = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnableImpl<String>() {
				public String run() throws Exception {
					return shellfire.getCryptoMinerConfig();
				}
			}, 3, 100);
		}

		return cryptoMinerConfig;
	}

	public Boolean sendLogToShellfire() {
		init();

		String serviceLog = Util.getLogFilePath(UserType.Service);
		String serviceLogString = "";
		try {
			serviceLogString = Util.fileToString(serviceLog);
		} catch (IOException e) {
			log.error("Could not read serviceLog", e);
		}

		String clientLog = Util.getLogFilePath(UserType.Client);
		String clientLogString = "";
		try {
			clientLogString = Util.fileToString(clientLog);
		} catch (IOException e) {
			log.error("Could not read clientLog", e);
		}
		
		String wireguardLogString = Util.getWireGuardLog();

		String installLog;
		String installLogString = "";
		try {
			installLog = Util.getLogFilePathInstaller();
			installLogString = Util.fileToString(installLog);
		} catch (IOException e) {
			log.error("Could not read installLog", e);
		}

		final String finalService = serviceLogString;
		final String finalClient = clientLogString;
		final String finalWireguard = wireguardLogString;
		final String finalInstall = installLogString;

		
		runnableSendLogToShellfire = new ExceptionThrowingReturningRunnableImpl<Boolean>() {
			public Boolean run() throws Exception {
				boolean result = shellfire.sendLogToShellfire(finalService, finalClient, finalWireguard, finalInstall);

				return result;
			}
		};
		
		Boolean result = Util.runWithAutoRetry(runnableSendLogToShellfire, 3, 100);

		return result;
	}

	public void cancelSendLogToShellfire() {
		if (runnableSendLogToShellfire != null) {
			runnableSendLogToShellfire.cancel();
		}
	}


}
