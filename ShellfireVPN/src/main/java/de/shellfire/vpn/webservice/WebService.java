package de.shellfire.vpn.webservice;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.Util.ExceptionThrowingReturningRunnable;
import de.shellfire.vpn.exception.VpnException;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.messaging.UserType;
import de.shellfire.vpn.proxy.ProxyConfig;
import de.shellfire.vpn.types.Protocol;
import de.shellfire.vpn.types.Server;
import de.shellfire.vpn.webservice.model.LoginResponse;
import de.shellfire.vpn.webservice.model.TrayMessage;
import de.shellfire.vpn.webservice.model.VpnAttributeList;
import de.shellfire.vpn.webservice.model.WsFile;
import de.shellfire.vpn.webservice.model.WsGeoPosition;
import de.shellfire.vpn.webservice.model.WsServer;
import de.shellfire.vpn.webservice.model.WsVpn;

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

  WebServiceBroker shellfire = new WebServiceBroker();

  private WebService() {
    // precache on load
    getVpnComparisonTable();
    getUrlHelp();
    getUrlPasswordLost();
    getUrlPremiumInfo();
    getUrlSuccesfulConnect();

  }

  /**
   * performs a log in with the specified account data. if no vpn exists yet for this account, it will be created automatically.
   * 
   * @param user
   *          username
   * @param pass
   *          password
   * @return the number of vpns in this account. 0 if login failed.
   */
  public Response<LoginResponse> login(final String user, final String pass) {

    log.debug("starting login request");
    Response<LoginResponse> result = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<Response<LoginResponse>>() {
      public Response<LoginResponse> run() throws Exception {
        return shellfire.login(user, pass);
      }
    }, 10, 50);

    log.debug("LoginResult received");

    if (shellfire.isLoggedIn()) {
      try {
        log.debug("Load vpn details - start");
        this.loadVpnDetails();
        log.debug("Load vpn details - finished");
      } catch (VpnException e) {
        result.setMessage(i18n.tr("VPN-Daten konnten nicht geladen werden."));
      }
    }

    return result;
  }

  private void loadVpnDetails() throws VpnException {

    List<WsVpn> wsVpns = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<List<WsVpn>>() {
      public List<WsVpn> run() throws Exception {

        return shellfire.getAllVpnDetails();
      }
    }, 10, 50);

    this.vpns = new LinkedList<Vpn>();

    for (WsVpn curWsVpn : wsVpns) {
      Vpn vpn = new Vpn(curWsVpn);
      this.vpns.add(vpn);
    }

    if (this.vpns.size() == 1) {
      this.selectVpn(this.vpns.getFirst());
    } else {
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
    if (this.servers == null || this.servers.getNumberOfServers() == 0) {
      List<WsServer> list = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<List<WsServer>>() {
        public List<WsServer> run() throws Exception {

          return shellfire.getServerList();
        }
      }, 10, 50);
      servers = new ServerList(list);
    }

    return this.servers;
  }

  public Vpn getVpn() {
    if (this.selectedVpn == null && shellfire.isLoggedIn()) {
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
    Boolean result = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<Boolean>() {
      public Boolean run() throws Exception {

        return shellfire.setServerTo(getVpnId(), server.getServerId());
      }
    }, 10, 50);

    if (result == true) {
      this.getVpn().setServer(server);
      return true;
    } else {
      return false;
    }
  }

  public boolean setProtocolTo(final Protocol protocol) {
    Boolean res = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<Boolean>() {
      public Boolean run() throws Exception {

        return shellfire.setProtocolTo(getVpnId(), protocol.toString());
      }
    }, 10, 50);

    if (res == true) {
      return true;
    } else {
      return false;
    }
  }

  public String getParametersForOpenVpn() {

    String params = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<String>() {
      public String run() throws Exception {

        return shellfire.getParametersForOpenVpn(getVpnId());
      }
    }, 10, 50);

    String proxyCommand = ProxyConfig.getOpenVpnConfigCommand();
    if (proxyCommand != null) {
      params += " " + proxyCommand;
    }

    if (!Util.isWindows()) {
      params = params.replace("--service ShellfireVPN2ExitEvent 0 ", "");

      params = params.replace("\\", "/");
      params = params.replace("verb 3", "verb 3");
    }

    return params;

  }

  public void downloadAndStoreCertificates() {
    List<WsFile> files;
    files = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<List<WsFile>>() {
      public List<WsFile> run() throws Exception {

        return shellfire.getCertificatesForOpenVpn(getVpnId());
      }
    }, 10, 50);

    createConfigDirIfNotExists();
    for (WsFile wsFile : files) {
      this.storeFile(wsFile);
    }
  }

  public static void createConfigDirIfNotExists() {
    File configDir = new File(CONFIG_DIR);
    configDir.mkdirs();

  }

  public static String macOsAppDirectory() {
    final String appDir = "asup";
    String result = "";
    try {
      result = com.apple.eio.FileManager.findFolder(com.apple.eio.FileManager.kUserDomain, com.apple.eio.FileManager.OSTypeToInt(appDir));
    } catch (Exception e) {
      result = "error";
    }
    return result;
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
    String ip = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<String>() {
      public String run() throws Exception {
        return shellfire.getLocalIpAddress();
      }
    }, 10, 50);

    if (ip == null)
      ip = i18n.tr("unbekannt");

    return ip;
  }

  public WsGeoPosition getOwnPosition() {
    if (this.ownPosition == null) {

      this.ownPosition = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<WsGeoPosition>() {
        public WsGeoPosition run() throws Exception {
          return shellfire.getLocalLocation();
        }
      }, 10, 50);

    }

    return this.ownPosition;
  }

  public Response<LoginResponse> registerNewFreeAccount(final String text, final String password, boolean subscribeNewsletter) {

    final int subscribe = subscribeNewsletter ? 1 : 0;
    Response<LoginResponse> result = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<Response<LoginResponse>>() {
      public Response<LoginResponse> run() throws Exception {
        return shellfire.register(text, password, subscribe);
      }
    }, 10, 50);

    return result;
  }

  public boolean accountActive() {
    Boolean accountActive = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<Boolean>() {
      public Boolean run() throws Exception {
        return shellfire.getIsActive();
      }
    }, 10, 50);

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
    if (this.vpnAttributeList == null) {
      vpnAttributeList = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<VpnAttributeList>() {
        public VpnAttributeList run() throws Exception {
          return shellfire.getComparisonTableData();
        }
      }, 10, 50);
    }

    return this.vpnAttributeList;
  }

  public List<TrayMessage> getTrayMessages() {
    if (this.trayMessages == null) {

      List<TrayMessage> messageList = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<List<TrayMessage>>() {
        public List<TrayMessage> run() throws Exception {
          return shellfire.getTrayMessages();
        }
      }, 10, 50);

      trayMessages = messageList;
    }

    return this.trayMessages;
  }

  /*
   * public WsUpgradeResult upgradeVpnToPremiumWithSerial(final String productKey) { WsUpgradeResult result = Util.runWithAutoRetry(new
   * ExceptionThrowingReturningRunnable<WsUpgradeResult>() { public WsUpgradeResult run() throws Exception { return
   * shellfire.upgradeVpnToPremiumWithCobiCode(selectedVpn.getVpnId(), productKey); } }, 10, 50);
   * 
   * return result; }
   */

  public int getLatestVersion() throws RemoteException {
    Integer latestVersion = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<Integer>() {
      public Integer run() throws Exception {
        return shellfire.getLatestVersion();
      }
    }, 10, 50);

    if (latestVersion == null)
      return 0;
    else
      return latestVersion;
  }

  public String getLatestInstaller() throws RemoteException {
    String latestZipInstaller;

    latestZipInstaller = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<String>() {
      public String run() throws Exception {
        return shellfire.getLatestInstaller();
      }
    }, 10, 50);

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
    if (urlSuccesfulConnect == null) {
      urlSuccesfulConnect = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<String>() {
        public String run() throws Exception {
          return shellfire.getUrlSuccesfulConnect();
        }
      }, 10, 50);

    }

    return urlSuccesfulConnect;
  }

  public String getUrlHelp() {
    if (urlHelp == null) {
      urlHelp = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<String>() {
        public String run() throws Exception {
          return shellfire.getUrlHelp();
        }
      }, 10, 50);
    }

    return urlHelp;
  }

  public String getUrlPremiumInfo() {
    if (urlPremiumInfo == null) {
      urlPremiumInfo = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<String>() {
        public String run() throws Exception {
          return shellfire.getUrlPremiumInfo();
        }
      }, 10, 50);
    }

    return urlPremiumInfo;
  }

  public String getUrlPasswordLost() {
    if (urlPasswordLost == null) {
      urlPasswordLost = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<String>() {
        public String run() throws Exception {
          return shellfire.getUrlPasswordLost();
        }
      }, 10, 50);
    }

    return urlPasswordLost;

  }

  public boolean sendLogToShellfire() {
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

    final String finalService = serviceLogString;
    final String finalClient = clientLogString;

    Boolean result = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<Boolean>() {
      public Boolean run() throws Exception {
        boolean result = shellfire.sendLogToShellfire(finalService, finalClient);
        ;

        return result;
      }
    }, 10, 50);

    return result;
  }
}
