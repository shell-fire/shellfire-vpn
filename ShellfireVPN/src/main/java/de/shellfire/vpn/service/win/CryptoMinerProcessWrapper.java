package de.shellfire.vpn.service.win;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;

import de.shellfire.vpn.Util;


public class CryptoMinerProcessWrapper extends Thread {

  private final InputStream inputStream;
  private CryptoCurrencyMiner cryptoCurrencyMiner;
  private static Logger log = Util.getLogger(CryptoMinerProcessWrapper.class.getCanonicalName());
  
  public CryptoMinerProcessWrapper(InputStream inputStream, CryptoCurrencyMiner cryptoCurrencyMiner) {
    this.inputStream = inputStream;
    this.cryptoCurrencyMiner = cryptoCurrencyMiner;
  }

  public void run() {
      try {
          BufferedReader reader = new BufferedReader(new InputStreamReader(this.inputStream));
          String line;

          while ((line = reader.readLine()) != null) {
            this.parse(line);
          }

      } catch (IOException ex) {
        Util.handleException(ex);
      }

  }
  
  /**
Sample output
[2018-01-07 15:31:00] speed 2.5s/60s/15m 211.6 190.2 201.1 H/s max: 218.3 H/s
[2018-01-07 15:31:21] accepted (122/0) diff 5220 (16 ms)
[2018-01-07 15:31:24] new job from fr04.supportxmr.com:5555 diff 5130
[2018-01-07 15:32:00] speed 2.5s/60s/15m 197.8 192.4 200.8 H/s max: 218.3 H/s
[2018-01-07 15:32:07] accepted (123/0) diff 5130 (47 ms)
[2018-01-07 15:32:15] accepted (124/0) diff 5130 (40 ms)
[2018-01-07 15:32:18] accepted (125/0) diff 5130 (13 ms)
[2018-01-07 15:32:24] new job from fr04.supportxmr.com:5555 diff 5190
[2018-01-07 15:32:26] accepted (126/0) diff 5190 (13 ms)
[2018-01-07 15:32:33] accepted (127/0) diff 5190 (13 ms)
[2018-01-07 15:33:00] speed 2.5s/60s/15m 194.4 191.8 199.9 H/s max: 218.3 H/s
   */

  private void parse(String line) {
    log.debug(line);
    
    if (line == null || line.length() < 22) {
      log.debug("unparseable line - returning");
      return;
    }
    
    // parse output from miner, analyze hashrate
    String[] pieces = line.split(" ");
    
    if (pieces == null || pieces.length < 4) {
      return;
    }
    
    String content = pieces[2];
    
    if (content.equals("speed")) {
      this.parseSpeed(pieces);
    } else if (content.equals("new")) {
      this.parseNewJob(pieces);
    } else if (content.equals("accepted")) {
      this.parseAccepted(pieces);
    }
    
  }

  // [2018-01-07 15:31:24] new job from fr04.supportxmr.com:5555 diff 5130
  private void parseNewJob(String[] pieces) {
    String from = pieces[5];
    int difficulty = Integer.parseInt(pieces[7]);
    
    this.cryptoCurrencyMiner.updateMostRecentTimestampNewJob();
    log.debug("New job received, from: {}, difficulty: {}", from, difficulty);
  }

  // [2018-01-07 15:32:18] accepted (125/0) diff 5130 (13 ms)
  private void parseAccepted(String[] pieces) {
    this.cryptoCurrencyMiner.updateMostRecentTimestampAccepted();
  }

  // [2018-01-07 15:33:00] speed 2.5s/60s/15m 194.4 191.8 199.9 H/s max: 218.3 H/s
  private void parseSpeed(String[] pieces) {
    
    Float speed2seconds = Float.MIN_VALUE;
    if (!pieces[4].equals("n/a"))
      speed2seconds = Float.parseFloat(pieces[4]);
    
    Float speed60seconds = null;
    if (!pieces[5].equals("n/a"))
      speed60seconds = Float.parseFloat(pieces[5]);
    
    Float speed15minutes = null;
    if (!pieces[6].equals("n/a"))
      speed15minutes = Float.parseFloat(pieces[6]);
    
    log.debug("new speed report 2.5s={}, 60s={}, 15m={}", speed2seconds, speed60seconds, speed15minutes);
    this.cryptoCurrencyMiner.updateSpeed(speed2seconds, speed60seconds, speed15minutes);
  }
}
