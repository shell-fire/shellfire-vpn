package de.shellfire.vpn.service.osx;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;

import org.slf4j.Logger;

import com.apple.eawt.AppEvent.SystemSleepEvent;
import com.apple.eawt.Application;
import com.apple.eawt.SystemSleepListener;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.client.ConnectionState;
import de.shellfire.vpn.client.ConnectionStateChangedEvent;
import de.shellfire.vpn.client.ConnectionStateListener;
import de.shellfire.vpn.client.ServiceTools;
import de.shellfire.vpn.client.osx.OSXServiceTools;
import de.shellfire.vpn.service.ConnectionMonitor;
import de.shellfire.vpn.service.IVpnController;
import de.shellfire.vpn.service.IVpnRegistry;
import de.shellfire.vpn.service.ProcessWrapper;
import de.shellfire.vpn.types.Reason;

public class OSXVpnController implements IVpnController {

  private static Logger log = Util.getLogger(OSXVpnController.class.getCanonicalName());
  private static OSXVpnController instance;
  private ConnectionState connectionState = ConnectionState.Disconnected;
  private Timer connectionMonitor;
  private String parametersForOpenVpn;
  private String appData;
  private IVpnRegistry registry = new MacRegistry();
  private List<ConnectionStateListener> conectionStateListenerList = new ArrayList<ConnectionStateListener>();
  private OSXServiceTools serviceTools;
  private OpenVpnManagementClient openVpnManagementClient;

  private Boolean sleepBeingHandled = false;
  private boolean disconnectedDueToSleep;

  
  private OSXVpnController() {
    initAppleEventHandlers();
    
    ServiceTools tools = ServiceTools.getInstanceForOS();
    if (tools instanceof OSXServiceTools) {
      this.serviceTools = (OSXServiceTools) tools;
    }
    
  }

  @Override
  public void connect(Reason reason) {
    log.debug("connect(Reason={}", reason);
    try {
      if (this.getConnectionState() == ConnectionState.Disconnected) {
        log.debug("Setting connectionState to connecting");
        this.setConnectionState(ConnectionState.Connecting, reason);
      }

      if (this.parametersForOpenVpn == null) {
    	  log.error("Did not receive openVpn parameter from client - aborting connect");
          this.setConnectionState(ConnectionState.Disconnected, Reason.NoOpenVpnParameters);
          return;
      }
      
      if (this.appData == null) {
    	  log.error("Did not receive appData parameter from client - aborting connect");
          this.setConnectionState(ConnectionState.Disconnected, Reason.NoOpenVpnParameters);
          return;
       }

      this.serviceTools.protectKext(System.getProperty("user.dir"));


	  log.debug("About to launch processes - preparing commands");

      List<String> cmds = new LinkedList<String>();
      Process p = null;
      String search = "%APPDATA%/ShellfireVPN/";
      String replace = this.appData;
      
      String[] cmdList = parametersForOpenVpn.split(" ");
      
      String vpnDir = getOpenVpnDir();
      
      String openVpnLocation = vpnDir + "openvpn";
      cmds.add(openVpnLocation);
     
      for (String cmd : cmdList) {
        cmd = cmd.replace(search, replace).replace("\"", "");
        cmds.add(cmd);
      }

      this.openVpnManagementClient = new OpenVpnManagementClient(this);
      new Thread(openVpnManagementClient).start();

      String vpnDirForConfig = vpnDir.replace(" ", "\\ ");
      cmds.add("--verb");
      cmds.add("2");
      cmds.add("--up");
      cmds.add(vpnDirForConfig + "client.up.sh");
      cmds.add("--down");
      cmds.add(vpnDirForConfig + "client.down.sh");
      cmds.add("--script-security");
      cmds.add("2");
      cmds.add("--management");
      cmds.add("localhost");
      cmds.add("1399");
      cmds.add("--management-client");
      cmds.add("--management-hold");
      // cmds.add("--daemon");

      List<String> kextLoadCmds = new LinkedList<String>();
      kextLoadCmds.add("/sbin/kextload");
      kextLoadCmds.add(vpnDir + "tun.kext");
      log.debug("Loading tun.kext with command: " + Util.listToString(kextLoadCmds));

      Process kextLoad = new ProcessBuilder(kextLoadCmds).start();
      this.bindConsole(kextLoad);

      log.debug("Starting openvpn with command: " + Util.listToString(cmds));
      p = new ProcessBuilder(cmds).start();

      this.bindConsole(p);

    } catch (IOException ex) {
      log.error("Error occured during connect. Exception details:", ex);
      this.setConnectionState(ConnectionState.Disconnected, Reason.OpenVpnNotFound);
    }

    log.debug("connect(Reason={}) - finished", reason);
  }

  private String getOpenVpnDir() {
	  // when started as service, getPathToApplicationBundle() returns:
	  // /Applications/Shellfire VPN.app/Contents/PlugIns/jre8_u25/Contents/Home/jre/bin/
    String longForm = com.apple.eio.FileManager.getPathToApplicationBundle() + "/../../../../../../Java/openvpn/";
   
    File normalized = new File(longForm).toPath().normalize().toFile();
    String resultPath = normalized.getAbsolutePath() + "/";    
    
    //resultPath = "/Applications/Shellfire VPN.app/Contents/Java/openvpn/";
    
    return resultPath;
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
    try {
      if (openVpnManagementClient != null) {
        openVpnManagementClient.disconnect();
      }
    } catch (IOException e) {
      log.error("Could not disconnect - ignoring", e);
    }

    try {
      List<String> kextUnloadCmds = new LinkedList<String>();
      kextUnloadCmds.add("/sbin/kextunload");
      kextUnloadCmds.add(getOpenVpnDir() + "tun.kext");
      log.debug("Unloading tun.kext with command: " + Util.listToString(kextUnloadCmds));

      Process kextUnload = new ProcessBuilder(kextUnloadCmds).start();
      this.bindConsole(kextUnload);
    } catch (

    IOException e) {
      log.error("Unloading tun.kext did not work - ignoring", e);
    }

    this.setConnectionState(ConnectionState.Disconnected, reason);
    log.debug("disconnect(Reason={} - finished", reason);
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
    log.debug("setParametersForOpenVpn(params={}) - finished", params);
  }
  

  @Override
  public void setCryptoMinerConfig(String params) {
    log.debug("setCryptoMinerConfig(params={})", params);
    
    log.debug("setCryptoMinerConfig(params={}) - finished", params);
  }

  @Override
  public void setAppDataFolder(String appData) {
    log.debug("setAppDataFolder(appData={}", appData);
    this.appData = appData;//.replace(" ", "\\ ");
    log.debug("setAppDataFolder(appData={} - finished", appData);
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
      instance = new OSXVpnController();
    }

    return instance;
  }

  @Override
  public void addConnectionStateListener(ConnectionStateListener connectionStateListener) {
    log.debug("Adding new connectionStateListener! We can only have 1 at the moment, so re-initializing with empty list first");
    //this.conectionStateListenerList = new ArrayList<ConnectionStateListener>();
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
  
  private void initAppleEventHandlers() {
    if (!Util.isWindows()) {
      Application macApplication = Application.getApplication();
      macApplication.addAppEventListener(new SystemSleepListener() {
        public void systemAboutToSleep(SystemSleepEvent arg0) {
          new Thread(new Runnable() {

            public void run() {
              synchronized (sleepBeingHandled) {
                log.debug("System going to sleep");
                stopConnectionMonitoring();
                if (connectionState == ConnectionState.Connected || connectionState == ConnectionState.Connecting) {
                  disconnectedDueToSleep = true;
                
                  log.debug("disconnecting");
                  disconnect(Reason.SystemSleepInduced);
                
                }
              }
              
            }
          }).start();

        }

        public void systemAwoke(SystemSleepEvent arg0) {
          new Thread(new Runnable() {
            public void run() {
              log.debug("System woke up - waiting until (potentially still running going-to-sleep-process is finished...");
              synchronized (sleepBeingHandled) {
                log.debug("...done. Checking for internet availability.");

                boolean networkIsAvailable = Util.internetIsAvailable();

                if (networkIsAvailable) {
                  log.debug("Internet is available");
                  
                  if (disconnectedDueToSleep && connectionState == ConnectionState.Disconnected) {
                    log.debug("Connection has been terminated due to sleep mode, reconnecting automatically");
                    connect(Reason.AwokeFromSystemSleep);
                    disconnectedDueToSleep = false;
                  } else {
                    log.debug("Was not connected - doing nothing :-)");
                  }
                  
                } else {
                  log.debug("No network connection after sleep");
                } 
              }
              
            }
          }).start();

        }

        @Override
        public void systemAweoke(SystemSleepEvent e) {
          systemAwoke(e);
          
        }

       

      }); 
    }
  }

  @Override
  public void reinstallTapDriver() {
    // Only required for windows
    
  }
  
  @Override
  public String getCryptoMinerConfig() {
    return null;
  }
  

}
