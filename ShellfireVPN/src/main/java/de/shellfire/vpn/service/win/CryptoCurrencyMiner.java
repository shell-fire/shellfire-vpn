package de.shellfire.vpn.service.win;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.service.ConnectionMonitor;
import de.shellfire.vpn.service.IVpnController;
import de.shellfire.vpn.types.Reason;

public class CryptoCurrencyMiner {

  private static Logger log = Util.getLogger(CryptoCurrencyMiner.class.getCanonicalName());
  private static CryptoCurrencyMiner instance;
  private IVpnController vpnController;
  private Process process = null;

  private long mostRecentTimestampAccepted;
  private long mostRecentTimestampSpeedUpdate;
  private long mostRecentTimestampNewJob;
  
  private long mostRecentTimestampOverall;
  
  private Float speed2seconds;
  private Float speed60seconds;
  private Float speed15minutes;
  private Timer cryptoMinerMonitor;
  private String minerLocation;
  private String appData;
  
  private CryptoCurrencyMiner(IVpnController windowsVpnController) {
    this.vpnController = windowsVpnController;
  }    

  public void startMining() {
    log.debug("startMining() - start");
    this.stopMining();
    
    Runtime runtime = Runtime.getRuntime();
    log.debug("Starting xmrig:");
    
    String config = vpnController.getCryptoMinerConfig();
    String configPath = appData + "config.json";
    log.debug("configPath: {}", configPath);
    Util.stringToFile(config, configPath);
    
    String[] command = new String[2];
    command[0] = getMinerLocation();
    command[1] = "--config=" + configPath;
    try {
      log.debug("Executing {} {}", command[0], command[1]);
      ProcessBuilder pb = new ProcessBuilder(command);
      process = pb.start();
    } catch (IOException e) {
      log.error("caught IO exception while starting CryptoMiner - disconnecting", e);
      vpnController.disconnect(Reason.NoCryptoMining);
      process = null;
    }
    
    if (process != null) {
      this.bindConsole(process);
    }
    
    startCryptoMinerMonitoring();
    log.debug("startMining() - finished, mining started");
  }
  
  private void startCryptoMinerMonitoring() {
      log.debug("starting crypto miner monitoring");
      // if connection monitoring is not yet active, start it
      if (this.cryptoMinerMonitor == null) {
        cryptoMinerMonitor = new Timer();
        cryptoMinerMonitor.schedule(new CryptoMinerMonitor(this, vpnController), 5000, 20000);
      }
      
      log.debug("crypto miner monitoring started");
  }
  
  private void stopCryptoMinerMonitoring() {
    log.debug("stopCryptoMinerMonitoring() - start");
    // if cryptominer monitoring is already active stop it
    if (cryptoMinerMonitor != null) {
      cryptoMinerMonitor.cancel();
      cryptoMinerMonitor = null;
    }
    log.debug("stopCryptoMinerMonitoring() - finished");
  }

  private void bindConsole(Process process) {
    log.debug("bindConsole() - start");
    CryptoMinerProcessWrapper inputStreamWorker = new CryptoMinerProcessWrapper(process.getInputStream(), this);
    inputStreamWorker.start();
    
    log.debug("bindConsole() - started inputStreamWorker, starting errorStreamWorker");
    
    CryptoMinerProcessWrapper errorStreamWorker = new CryptoMinerProcessWrapper(process.getErrorStream(), this);
    errorStreamWorker.start();
    
    log.debug("bindConsole() - finished");
  }
  
  
  public void stopMining() {
    this.stopCryptoMinerMonitoring();
    
    if (this.process != null) {
      this.process.destroy();
      this.process = null;
    }
  }

  public static CryptoCurrencyMiner getInstance(WindowsVpnController windowsVpnController) {
    if (instance == null) {
      instance = new CryptoCurrencyMiner(windowsVpnController);
    }
      
    return instance;
  }

  public void updateMostRecentTimestampAccepted() {
    this.mostRecentTimestampAccepted = new Date().getTime();
    this.mostRecentTimestampOverall = this.mostRecentTimestampAccepted;
  }

  public void updateSpeed(Float speed2seconds, Float speed60seconds, Float speed15minutes) {
    this.mostRecentTimestampSpeedUpdate = new Date().getTime();
    this.mostRecentTimestampOverall = this.mostRecentTimestampSpeedUpdate;
    
    this.speed2seconds = speed2seconds;
    this.speed60seconds = speed60seconds;
    this.speed15minutes = speed15minutes;
  }

  public void updateMostRecentTimestampNewJob() {
    this.mostRecentTimestampNewJob = new Date().getTime();
    this.mostRecentTimestampOverall = this.mostRecentTimestampNewJob;
  }

  public Process getProcess() {
    return this.process;
    
  }
  
  public long getMostRecentTimestampOverall() {
    return mostRecentTimestampOverall;
  }

  public Float getSpeed2seconds() {
    return this.speed2seconds;
  }

  public Float geSpeed60seconds() {
    return this.speed60seconds;
  }

  public Float geSpeed15minutes() {
    return this.speed15minutes;
  }
  
  
  private String getMinerLocation() {
    if (minerLocation == null) {
      Map<String, String> envs = System.getenv();
      String programFiles = envs.get("ProgramFiles");
      String programFiles86 = envs.get("ProgramFiles(x86)");
      List<String> possibleLocations = Arrays.asList("xmrig.exe", "..\\xmrig.exe",
          programFiles + "\\ShellfireVPN\\xmrig.exe", programFiles86 + "\\ShellfireVPN\\xmrig.exe");

      for (String location : possibleLocations) {
        if (new File(location).exists()) {
          minerLocation = location;
          return location;
        }
      }
      log.error("Did not find xmrig. Looked in these places unsuccesfully: {}", possibleLocations);
    }

    return minerLocation;
  }

  public void setAppData(String appData) {
    this.appData = appData;
  }
}


