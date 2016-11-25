package de.shellfire.vpn.service.win;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;

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
import de.shellfire.vpn.types.Reason;

public class WindowsVpnController implements IVpnController {

  private static Logger log = Util.getLogger(WindowsVpnController.class.getCanonicalName());
  private static WindowsVpnController instance;
  private ConnectionState connectionState = ConnectionState.Disconnected;
  private Reason reasonForStateChange;
  private Timer connectionMonitor;
  private String parametersForOpenVpn;
  private String appData;
  private IVpnRegistry registry = new WinRegistry();
  private List<ConnectionStateListener> conectionStateListenerList = new ArrayList<ConnectionStateListener>();
  private IPV6Manager ipv6manager = new IPV6Manager();
  
  
  private String getOpenVpnLocation() {
    log.debug("getOpenVpnStartString() - start");
    
    Map<String, String> envs = System.getenv();
    String programFiles = envs.get("ProgramFiles");
    String programFiles86 = envs.get("ProgramFiles(x86)");

    List<String> possibleOpenVpnExeLocations = Util.getPossibleExeLocations(programFiles, programFiles86);

    for (String possibleLocaion : possibleOpenVpnExeLocations) {
      File f = new File(possibleLocaion);
      if (f.exists()) {
        log.debug("getOpenVpnStartString() - returning " + possibleLocaion);
        return possibleLocaion;
      }
        
    }
    log.debug("getOpenVpnStartString() - returning null: OPENVPN NOT FOUND!");
    return null;
  }
  
  @Override
  public void connect(Reason reason) {
    log.debug("connect(Reason={}", reason);
    try {
      if (this.getConnectionState() == ConnectionState.Disconnected) {
        log.debug("Setting connectionState to connecting");
        this.setConnectionState(ConnectionState.Connecting, reason);
      }

      fixTapDevices();
      ipv6manager.disableIPV6OnAllDevices();

      log.debug("getting openVpnLocation");
      String openVpnLocation = this.getOpenVpnLocation();
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

      log.debug("Bindin process to console");
      this.bindConsole(p);

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
    Kernel32 kernel32 = Kernel32.INSTANCE;
    HANDLE result = kernel32.CreateEvent(null, true, false, "ShellfireVPN2ExitEvent"); // request deletion
    kernel32.SetEvent(result);
    try {
      Thread.sleep(250);
    } catch (InterruptedException e) {
      log.error("", e);
    }
    kernel32.PulseEvent(result);
    
    this.setConnectionState(ConnectionState.Disconnected, reason);
    ipv6manager.enableIPV6OnPreviouslyDisabledDevices();
    fixTapDevices();
    log.debug("disconnect(Reason={} - finished", reason);
  }
  
  private void fixTapDevices() {
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
    log.debug("setParametersForOpenVpn(params={}) - finished", params);
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
    boolean result = registry.autoProxyConfigEnabled();;
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
  
  
}
