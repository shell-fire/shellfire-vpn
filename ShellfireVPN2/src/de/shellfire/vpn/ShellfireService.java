/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.xml.rpc.ServiceException;

import org.apache.axis.AxisFault;
import org.xnap.commons.i18n.I18n;

import de.shellfire.vpn.gui.Util;
import de.shellfire.vpn.gui.VpnException;
import de.shellfire.vpn.gui.VpnI18N;
import de.shellfire.vpn.gui.Util.ExceptionThrowingReturningRunnable;
import de.shellfire.vpn.proxy.ProxyConfig;
import de.shellfire.www.webservice.sf_soap_php.ShellfireWebServicePort;
import de.shellfire.www.webservice.sf_soap_php.ShellfireWebServiceServiceLocator;
import de.shellfire.www.webservice.sf_soap_php.TrayMessage;
import de.shellfire.www.webservice.sf_soap_php.VpnAttributeList;
import de.shellfire.www.webservice.sf_soap_php.WsFile;
import de.shellfire.www.webservice.sf_soap_php.WsGeoPosition;
import de.shellfire.www.webservice.sf_soap_php.WsLoginResult;
import de.shellfire.www.webservice.sf_soap_php.WsRegistrationResult;
import de.shellfire.www.webservice.sf_soap_php.WsServer;
import de.shellfire.www.webservice.sf_soap_php.WsUpgradeResult;
import de.shellfire.www.webservice.sf_soap_php.WsVpn;

/**
 * 
 * @author bettmenn
 */
public class ShellfireService {

  public static final String CONFIG_DIR = Util.getConfigDir();


  ShellfireWebServicePort shellfire = null;
  private String user;
  private String pass;
  private boolean loggedIn = false;
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
  private static ShellfireService instance;

  private ShellfireService() {
    ShellfireWebServiceServiceLocator locator = new ShellfireWebServiceServiceLocator();
    try {
      this.shellfire = locator.getShellfireWebServicePort();
      // precache on load
      getVpnComparisonTable();
      getUrlHelp();
      getUrlPasswordLost();
      getUrlPremiumInfo();
      getUrlSuccesfulConnect();
    } catch (ServiceException ex) {
      Util.handleException(ex);
    }
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
  public WsLoginResult login(final String user, final String pass) {
	  
      final String langKey = getLangKey();
      System.out.println("starting login request");
      System.out.println("shellfire.login("+langKey+", "+user+", "+pass+");");
      WsLoginResult result = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<WsLoginResult>() {
  		public WsLoginResult run() throws Exception {
  			
  			return shellfire.login(langKey, user, pass);
  		}
      }, 10, 50);
      
      
      
      System.out.println("WsLoginResult received");
      this.loggedIn = result.isLoggedIn();
      if (this.loggedIn) {
        this.user = user;
        this.pass = pass;

        try {
        	System.out.println("Load vpn details - start");
          this.loadVpnDetails();
          System.out.println("Load vpn details - finished");
        } catch (VpnException e) {
          result.setErrorMessage(i18n.tr("VPN-Daten konnten nicht geladen werden."));
          result.setLoggedIn(false);
        }

        return result;
      } else {
        return result;
      }


  }

  private void loadVpnDetails() throws VpnException {
 
      WsVpn[] wsVpns = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<WsVpn[]>() {
  		public WsVpn[] run() throws Exception {
  			
  			return shellfire.getAllVpnDetails(user, pass);
  		}
      }, 10, 50);
      
      this.vpns = new LinkedList<Vpn>();

      for (WsVpn curWsVpn : wsVpns) {
        Vpn vpn = new Vpn(curWsVpn);
        this.vpns.add(vpn);
      }

      // if we have 0 vpns loaded here, we need to request the creation of a free one
      if (this.vpns.size() == 0) {
        WsVpn wsVpn = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<WsVpn>() {
      		public WsVpn run() throws Exception {
      			
      			return shellfire.createNewFreeVpn(user, pass);
      		}
            }, 10, 50);
        
        
        if (wsVpn == null)
          throw new VpnException(i18n.tr("Fehler beim laden der VPN-Daten"));

        Vpn vpn = new Vpn(wsVpn);
        this.vpns.add(vpn);
        this.selectVpn(vpn);
      } else if (this.vpns.size() == 1) {
        this.selectVpn(this.vpns.getFirst());
      } else {
        // if we have several vpns loaded here, we need to let the user choose one!
        // this is done from the LoginForm class

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
        WsServer[] list = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<WsServer[]>() {
      		public WsServer[] run() throws Exception {
      			
      			return shellfire.getServerList();
      		}
            }, 10, 50);
        servers = new ServerList(list);
    }

    return this.servers;
  }  


  public Vpn getVpn() {
    if (this.selectedVpn == null && this.loggedIn) {
      try {
        this.loadVpnDetails();
      } catch (VpnException ex) {
        Util.handleException(ex);
      }
    }

    return selectedVpn;
  }

  public boolean maySwitchToServer(final Server server) {
    // shortcut so webservice isnt used twice
    if (this.allowedServer == server.getServerId()) {
      return true;
    }
	
	  Integer maySwitchInt = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<Integer>() {
		public Integer run() throws Exception {
			
			return shellfire.maySwitchToServer(user, pass, getVpnId(), server.getServerId());
		}
	    }, 10, 50);
	  
	  boolean maySwitch = (maySwitchInt == 1);
	
	  if (maySwitch) {
	    this.allowedServer = server.getServerId();
	  }
	
	  return maySwitch;
  }

  private int getVpnId() {
    if (this.selectedVpn != null)
      return this.selectedVpn.getVpnId();
    else
      return 0;
  }

  public boolean setServerTo(final Server server) {
      if (this.maySwitchToServer(server)) {
        Integer res = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<Integer>() {
    		public Integer run() throws Exception {
    			
    			return shellfire.setServerTo(user, pass, getVpnId(), server.getServerId());
    		}
          }, 10, 50);

        if (res == 1) {
        	this.getVpn().setServer(server);
          return true;
        } else {
          return false;
        }
      }

    return false;
  }

  public boolean setProtocolTo(final Protocol protocol) {
      Integer res = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<Integer>() {
		public Integer run() throws Exception {
			
			return shellfire.setProtocolTo(user, pass, getVpnId(), protocol.toString());
		}
      }, 10, 50);
      
      
      if (res == 1) {
        return true;
      } else {
        return false;
      }
  }

  public String getParametersForOpenVpn() {
 
      String params = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<String>() {
		public String run() throws Exception {
			
			return shellfire.getParametersForOpenVpn(user, pass, getVpnId());
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
    	  //params = params.replace("\"", "");
      }
    	  
      return params;

  }

  public void downloadAndStoreCertificates() {
    WsFile[] files;
	files = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<WsFile[]>() {
		public WsFile[] run() throws Exception {
			
			return shellfire.getCertificatesForOpenVpn(user, pass, getVpnId());
		}
	}, 10, 50);


      createConfigDirIfNotExists();
      for (int i = 0; i < files.length; i++) {
        WsFile wsFile = files[i];
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
    String filePath = ShellfireService.CONFIG_DIR + Util.getSeparator() + name;

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
          }, 10, 50, AxisFault.class);

    	
    }

    return this.ownPosition;
  }

  public WsRegistrationResult registerNewFreeAccount(final String text, final String password, boolean subscribeNewsletter, boolean isResend) {

      final int subscribe = subscribeNewsletter ? 1 : 0;
      final int resend = isResend ? 1 : 0;
      final String langKey = getLangKey();
      WsRegistrationResult result = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<WsRegistrationResult>() {
      		public WsRegistrationResult run() throws Exception {
      		  if (Util.isMacOs())
      		    return shellfire.registerNewFreeAccountMac(langKey, text, password, subscribe, resend);
      		  else
      		    return shellfire.registerNewFreeAccount(langKey, text, password, subscribe, resend);
      		}
          }, 10, 50);
      
      return result;
   }

  public boolean accountActive(final String token) {
	  Integer accountActive = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<Integer>() {
      		public Integer run() throws Exception {
      			return shellfire.accountActive(token);
      		}
          }, 10, 50);
	  
	  return accountActive == 1;
  }

  public boolean isLoggedIn() {
    return this.loggedIn;
  }

  public LinkedList<Vpn> getAllVpn() {
    return this.vpns;
  }

  public boolean setProductTypeToOpenVpn() {
    try {
      final Vpn selectedVpn = this.selectedVpn;
      
      Integer res = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<Integer>() {
      		public Integer run() throws Exception {
      			return shellfire.setProductTypeToOpenVpn(user, pass, selectedVpn.getVpnId());
      		}
          }, 10, 50);
      
      if (res == 1) {
        // invalidate cache of loaded vpns
        this.loadVpnDetails();
        this.selectVpn(selectedVpn);
        return true;
      } else {
        return false;
      }

    } catch (VpnException ex) {
      Util.handleException(ex);
    }

    return false;
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
    String[] filesRequired = new String[] { ShellfireService.CONFIG_DIR, ShellfireService.CONFIG_DIR+"\\sf" + vpnId + ".crt", ShellfireService.CONFIG_DIR+"\\sf" + vpnId + ".key", ShellfireService.CONFIG_DIR+"\\ca.crt" };

    for (String file : filesRequired) {
      File f = new File(file);
      if (!f.isFile())
        return false;
    }

    return true;
  }

  private String getLangKey() {
    String key = VpnI18N.getLanguage().getKey();

    return key;
  }

  public VpnAttributeList getVpnComparisonTable() {
    if (this.vpnAttributeList == null) {
        final String key = getLangKey();
        vpnAttributeList = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<VpnAttributeList>() {
      		public VpnAttributeList run() throws Exception {
      			return shellfire.getComparisonTableData(key);
      		}
          }, 10, 50);
    }

    return this.vpnAttributeList;
  }

  public List<TrayMessage> getTrayMessages() {
    if (this.trayMessages == null) {
        final String key = VpnI18N.getLanguage().getKey();
        
        TrayMessage[] messageArray = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<TrayMessage[]>() {
      		public TrayMessage[] run() throws Exception {
      			return shellfire.getTrayMessages(key);
      		}
          }, 10, 50);
        
         trayMessages = Arrays.asList(messageArray);
    }

    return this.trayMessages;
  }

  public WsUpgradeResult upgradeVpnToPremiumWithSerial(final String productKey) {
    WsUpgradeResult result = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<WsUpgradeResult>() {
  		public WsUpgradeResult run() throws Exception {
  			return shellfire.upgradeVpnToPremiumWithCobiCode(user, pass, selectedVpn.getVpnId(), productKey);
  		}
    }, 10, 50);
      
    return result;
  }

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
  
  public int getLatestVersionMac() throws RemoteException {
	    Integer latestVersion = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<Integer>() {
	  		public Integer run() throws Exception {
	  			return shellfire.getLatestVersionMac();
	  		}
	    }, 10, 50);
	    
	    if (latestVersion == null)
	    	return 0;
	    else
	    	return latestVersion;
	  }

  public String getLatestInstaller() throws RemoteException {
    String latestZipInstaller;
    
    if (Util.isWindows()) {
    	latestZipInstaller = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<String>() {
	  		public String run() throws Exception {
	  			return shellfire.getLatestInstaller();
	  		}
	    }, 10, 50);
    } else {
    	latestZipInstaller = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<String>() {
	  		public String run() throws Exception {
	  			return shellfire.getLatestInstallerMac();
	  		}
	    }, 10, 50);
    }
    
   if (latestZipInstaller == null)
	   latestZipInstaller = "";

    return latestZipInstaller;
  }

  public static ShellfireService getInstance() {
    if (instance == null)
      instance = new ShellfireService();

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
}
