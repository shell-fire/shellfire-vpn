package de.shellfire.vpn.service.win;

import java.util.Date;
import java.util.TimerTask;

import org.slf4j.Logger;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.service.IVpnController;
import de.shellfire.vpn.types.Reason;

public class CryptoMinerMonitor  extends TimerTask{

  private static Logger log = Util.getLogger(CryptoMinerMonitor.class.getCanonicalName());
  private CryptoCurrencyMiner cryptoCurrencyMiner;
  private IVpnController vpnController;

  public CryptoMinerMonitor(CryptoCurrencyMiner cryptoCurrencyMiner, IVpnController vpnController) {
    this.cryptoCurrencyMiner = cryptoCurrencyMiner;
    this.vpnController = vpnController;
  }

  @Override
  public void run() {
    boolean miningOk = true;
    
    Process process = cryptoCurrencyMiner.getProcess();
    if (process == null) {
      miningOk = false;
      log.debug("detected mining not ok because process is null");
    }
    
    if (!isRunning(process)) {
      miningOk = false;
      log.debug("detected mining not ok because process is not running");
    }
    
    long mostRecentUpdate = cryptoCurrencyMiner.getMostRecentTimestampOverall();
    long now = new Date().getTime();
    long diff = now - mostRecentUpdate;
    log.debug("time since last update: {}", diff);
    
    if (diff > 1000 * 60 * 2) {
      miningOk = false;
      log.debug("detected mining not ok because time since last update longer than 120 seconds");
    }

    // determine longest possible hashrate-avg
    Float speed15minutes = cryptoCurrencyMiner.geSpeed15minutes();
    Float speed60seconds = cryptoCurrencyMiner.geSpeed60seconds();
    Float speed2seconds = cryptoCurrencyMiner.getSpeed2seconds();
    
    Float speed = speed15minutes;
    
    if (speed == null) {
      speed = speed60seconds;
    }
    
    if (speed == null) {
      speed = speed2seconds;
    }

    if (speed != null && speed < 30) {
      miningOk = false;
      log.debug("detected mining not ok because speed less than 30 H/s");
    }
    
    if (miningOk) {
      log.debug("CryptoMinerMonitoring is happy, all checks positive - happy mining! :-)");
    } else {
      log.debug("mining not ok - disconnecting vpn");
      vpnController.disconnect(Reason.NoCryptoMining);
    }
    
  }
  
  boolean isRunning(Process process) {
    try {
        process.exitValue();
        return false;
    } catch (Exception e) {
        return true;
    }
}

}
