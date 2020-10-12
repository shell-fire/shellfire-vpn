/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.client.ConnectionState;
import de.shellfire.vpn.service.win.TapFixer;
import de.shellfire.vpn.types.Reason;

/**
 *
 * @author bettmenn
 */
public class ProcessWrapper extends Thread {

    private static Logger log = Util.getLogger(ProcessWrapper.class.getCanonicalName());
  
    private final InputStream inputStream;
    private boolean tapDriverReinstalledTried = false;
    private IVpnController vpnController;
    public static final List<String> succesfulConnectList = Arrays.asList("Initialization Sequence Completed");
    public static final List<String> failedPassPhraseList = Arrays.asList(
            "TLS Error: Need PEM pass phrase for private key",
            "EVP_DecryptFinal:bad decrypt",
            "PKCS12_parse:mac verify failure",
            "Received AUTH_FAILED control message",
            "AUTH: Received control message: AUTH_FAILED",
            "Auth username is empty");
    public static final List<String> certificateInvalidList = Arrays.asList(
            "error=certificate has expired",
            "error=certificate is not yet valid",
            "Cannot load certificate file");
    public static final List<String> processRestartingList = Arrays.asList(
            "process restarting", "Connection reset, restarting");
    public static final List<String> processDisconnectedList = Arrays.asList(
            "SIGTERM[hard,] received, process exiting");
    public static final List<String> allTapInUseList = Arrays.asList(
            "All TAP-Win32 adapters on this system are currently in use");
    public static final List<String> notEnoughPrivilegesList = Arrays.asList(
            "Windows route add command failed: returned error code 1",
            "FlushIpNetTable failed on interface");
    public static final List<String> tapDriverTooOld = Arrays.asList(
        "This version of OpenVPN requires a TAP-Win32 driver that is at least version");
    public static final List<String> generalError = Arrays.asList("ERROR:", "due to fatal error");
    public static final List<String> tapDriverNotFound = Arrays.asList(
    "There are no TAP-Win32 adapters on this system",
    "There are no TAP-Windows adapters on this system");
    public static final List<String> gatewayFailed = Arrays.asList("NOTE: unable to redirect default gateway");
    
    
    public ProcessWrapper(InputStream inputStream, IVpnController vpnController) {
        this.inputStream = inputStream;
        this.vpnController = vpnController;
    }

    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(this.inputStream));
            String line;

            while ((line = reader.readLine()) != null) {
                this.append(line);
            }

        } catch (IOException ex) {
          Util.handleException(ex);
        }

    }

    void append(String line) {
    	if (line != null) {
    		if (line.length() > 0) {
                log.debug(line);
                
                // on Mac OS X, parsing is done through the OpenVpnManagementClient
                if (Util.isWindows()) {
                	this.parse(line);
                }
                
    		}
    	}
    }

    /**
     * Parses the last log line retrieved for changes in status
     * @param line 
     */
    private void parse(String line) {
         switch (this.vpnController.getConnectionState()) {
            case Disconnected:
                break;
            case Connecting:
                this.checkForStateChangeWhileConnecting(line);
                break;
            case Connected:
                this.checkForStateChangeWhileConnected(line);
                break;
        }
    }

    private void checkSuccesfulConnect(String line)  {
        if (this.lineContainsAnElementOfList(line, ProcessWrapper.succesfulConnectList)) {
            log.debug("succesful connect detected from line {}", line);
            this.setStatus(ConnectionState.Connected, Reason.SuccesfulConnectDetected);
        }
    }

    private void checkFailedPassphrase(String line)  {
        if (this.lineContainsAnElementOfList(line, ProcessWrapper.failedPassPhraseList)) {
            log.debug("password failed detected from line {}", line);
            this.setStatus(ConnectionState.Disconnected, Reason.PasswordWrong);
        }
    }

    private void checkGatewayFailed(String line)  {
        if (this.lineContainsAnElementOfList(line, ProcessWrapper.gatewayFailed)) {
            log.debug("could not redirect gateway  " + line);
            this.setStatus(ConnectionState.Disconnected, Reason.GatewayRedirectFailed);
        }
    }

    private void checkAllTapInUse(String line)  {
        if (this.lineContainsAnElementOfList(line, ProcessWrapper.allTapInUseList)) {
            log.debug("all tap drivers in use detected from line {}", line);
            this.setStatus(ConnectionState.Disconnected, Reason.AllTapInUse);
        }
    }

    private void checkCertificateInvalid(String line)  {
        if (this.lineContainsAnElementOfList(line, ProcessWrapper.certificateInvalidList)) {
            log.debug("certificate invalid detected from line {}", line);
            this.setStatus(ConnectionState.Disconnected, Reason.CertificateFailed);
        }
    }

    private void checkForStateChangeWhileConnecting(String line)  {
        this.checkSuccesfulConnect(line);
        this.checkFailedPassphrase(line);
        this.checkCertificateInvalid(line);
        this.checkAllTapInUse(line);
        this.checkNotEnoughPrivileges(line);
        this.checkTapDriverTooOld(line);
        this.checkTapDriverNotFound(line);
        this.checkGeneralError(line);
        this.checkGatewayFailed(line);
        

    }

    private void checkTapDriverNotFound(String line)  {
      if (this.lineContainsAnElementOfList(line, ProcessWrapper.tapDriverNotFound)) {
        log.debug("checkTapDriverNotFound() - TapDriverNotFound");
        // try automatic fix once
        if (!tapDriverReinstalledTried ) {
          log.debug("checkTapDriverNotFound() - TapDriverNotFound - trying TapFixer.reinstallTapDriver()");
          tapDriverReinstalledTried = true;
          TapFixer.reinstallTapDriver();
          this.setStatus(ConnectionState.Disconnected, Reason.TapDriverNotFoundPleaseRetry);
        } else {
          log.debug("Automatic fix tried before, apparently didnt work. giving up! :-(");
           this.setStatus(ConnectionState.Disconnected, Reason.TapDriverNotFound);
        }
    }
      
    }

    private void checkNotEnoughPrivileges(String line)  {
        if (this.lineContainsAnElementOfList(line, ProcessWrapper.notEnoughPrivilegesList)) {
            log.debug("not enough privileges detected from line {}", line);
            this.setStatus(ConnectionState.Disconnected, Reason.NotEnoughPrivileges);
        }

    }
    
    private void checkGeneralError(String line)  {
      if (this.lineContainsAnElementOfList(line, ProcessWrapper.generalError)) {
          log.debug("general error detected from line {}", line);
          this.setStatus(ConnectionState.Disconnected, Reason.UnknownOpenVPNError);
      }
  }
    
    private void checkTapDriverTooOld(String line)  {
      if (this.lineContainsAnElementOfList(line, ProcessWrapper.tapDriverTooOld)) {
          log.debug("tap driver too old detected from line {}", line);
          this.setStatus(ConnectionState.Disconnected, Reason.TapDriverTooOld);
      }
  }

    private void checkForStateChangeWhileConnected(String line)  {
        this.checkReconnecting(line);
        this.checkDisconnected(line);
    }

    private void checkReconnecting(String line)  {
        if (this.lineContainsAnElementOfList(line, ProcessWrapper.processRestartingList)) {
            log.debug("reconnecting detected from line {}", line);
            TapFixer.restartAllTapDevices();
            this.setStatus(ConnectionState.Connecting, Reason.ProcessRestartDetected);
        }
    }

    private void checkDisconnected(String line)  {
        if (this.lineContainsAnElementOfList(line, ProcessWrapper.processDisconnectedList)) {
            log.debug("disconnect detected from line {}", line);
            
            if (!this.vpnController.isExpectingDisconnect()) {
              this.setStatus(ConnectionState.Connecting, Reason.DisconnectDetected);  
            }
            
        }
    }
    
    

    private boolean lineContainsAnElementOfList(String line, List<String> list) {
        for (String check : list) {
            if (line.toLowerCase().contains(check.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private void setStatus(ConnectionState connectionState, Reason reason)  {
        if (connectionState == ConnectionState.Disconnected)
          this.vpnController.disconnect(reason);
        else
            this.vpnController.setConnectionState(connectionState, reason);
    }
}
