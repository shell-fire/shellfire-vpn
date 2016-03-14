/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.rmi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

import de.shellfire.vpn.ConnectionState;
import de.shellfire.vpn.Reason;
import de.shellfire.vpn.gui.IConsole;
import de.shellfire.vpn.gui.Util;

/**
 *
 * @author bettmenn
 */
public class ProcessWrapper extends Thread {

    private final IConsole console;
    private final InputStream inputStream;
    private OpenVpnProcessHost processHost;
    private boolean tapDriverReinstalledTried = false;
    public static final List<String> succesfulConnectList = Arrays.asList("Initialization Sequence Completed");
    public static final List<String> failedPassPhraseList = Arrays.asList(
            "TLS Error: Need PEM pass phrase for private key",
            "EVP_DecryptFinal:bad decrypt",
            "PKCS12_parse:mac verify failure",
            "Received AUTH_FAILED control message",
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
    
    
    public ProcessWrapper(InputStream inputStream, IConsole console, OpenVpnProcessHost processHost) {
        this.inputStream = inputStream;
        this.console = console;
        this.processHost = processHost;
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
                this.console.append("proc: " + line);
                
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

        try {
          switch (this.processHost.getConnectionState()) {
              case Disconnected:
                  break;
              case Connecting:
                  this.checkForStateChangeWhileConnecting(line);
                  break;
              case Connected:
                  this.checkForStateChangeWhileConnected(line);
                  break;

          }
        } catch (RemoteException e) {
          e.printStackTrace();
        }
    }

    private void checkSuccesfulConnect(String line) throws RemoteException {
        if (this.lineContainsAnElementOfList(line, ProcessWrapper.succesfulConnectList)) {
            System.out.println("succesful connect detected from line " + line);
            this.setStatus(ConnectionState.Connected, Reason.SuccesfulConnectDetected);
        }
    }

    private void checkFailedPassphrase(String line) throws RemoteException {
        if (this.lineContainsAnElementOfList(line, ProcessWrapper.failedPassPhraseList)) {
            System.out.println("password failed detected from line " + line);
            this.setStatus(ConnectionState.Disconnected, Reason.PasswordWrong);
        }
    }

    private void checkGatewayFailed(String line) throws RemoteException {
        if (this.lineContainsAnElementOfList(line, ProcessWrapper.gatewayFailed)) {
            System.out.println("could not redirect gateway  " + line);
            this.setStatus(ConnectionState.Disconnected, Reason.GatewayRedirectFailed);
        }
    }

    private void checkAllTapInUse(String line) throws RemoteException {
        if (this.lineContainsAnElementOfList(line, ProcessWrapper.allTapInUseList)) {
            System.out.println("all tap drivers in use detected from line " + line);
            this.setStatus(ConnectionState.Disconnected, Reason.AllTapInUse);
        }
    }

    private void checkCertificateInvalid(String line) throws RemoteException {
        if (this.lineContainsAnElementOfList(line, ProcessWrapper.certificateInvalidList)) {
            System.out.println("certificate invalid detected from line " + line);
            this.setStatus(ConnectionState.Disconnected, Reason.CertificateFailed);
        }
    }

    private void checkForStateChangeWhileConnecting(String line) throws RemoteException {
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

    private void checkTapDriverNotFound(String line) throws RemoteException {
      if (this.lineContainsAnElementOfList(line, ProcessWrapper.tapDriverNotFound)) {
        console.append("checkTapDriverNotFound() - TapDriverNotFound");
        // try automatic fix once
        if (!tapDriverReinstalledTried ) {
          console.append("checkTapDriverNotFound() - TapDriverNotFound - trying TapFixer.reinstallTapDriver()");
          tapDriverReinstalledTried = true;
          TapFixer.reinstallTapDriver();
          this.setStatus(ConnectionState.Disconnected, Reason.TapDriverNotFoundPleaseRetry);
        } else {
           console.append("Automatic fix tried before, apparently didnt work. giving up! :-(");
           this.setStatus(ConnectionState.Disconnected, Reason.TapDriverNotFound);
        }
        
        System.out.println("no tap driver found from line " + line);
        
    }
      
    }

    private void checkNotEnoughPrivileges(String line) throws RemoteException {
        if (this.lineContainsAnElementOfList(line, ProcessWrapper.notEnoughPrivilegesList)) {
            System.out.println("not enough privileges detected from line " + line);
            this.setStatus(ConnectionState.Disconnected, Reason.NotEnoughPrivileges);
        }

    }
    
    private void checkGeneralError(String line) throws RemoteException {
      if (this.lineContainsAnElementOfList(line, ProcessWrapper.generalError)) {
          System.out.println("general error detected from line " + line);
          this.setStatus(ConnectionState.Disconnected, Reason.UnknownOpenVPNError);
      }
  }
    
    private void checkTapDriverTooOld(String line) throws RemoteException {
      if (this.lineContainsAnElementOfList(line, ProcessWrapper.tapDriverTooOld)) {
          System.out.println("tap driver too old detected from line " + line);
          this.setStatus(ConnectionState.Disconnected, Reason.TapDriverTooOld);
      }
  }

    private void checkForStateChangeWhileConnected(String line) throws RemoteException {
        this.checkReconnecting(line);
        this.checkDisconnected(line);
    }

    private void checkReconnecting(String line) throws RemoteException {
        if (this.lineContainsAnElementOfList(line, ProcessWrapper.processRestartingList)) {
            System.out.println("reconnecting detected from line " + line);
            TapFixer.restartAllTapDevices();
            this.setStatus(ConnectionState.Connecting, Reason.ProcessRestartDetected);
        }
    }

    private void checkDisconnected(String line) throws RemoteException {
        if (this.lineContainsAnElementOfList(line, ProcessWrapper.processDisconnectedList)) {
            System.out.println("disconnect detected from line " + line);
            this.setStatus(ConnectionState.Connecting, Reason.DisconnectDetected);
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

    private void setStatus(ConnectionState connectionState, Reason reason) throws RemoteException {
        if (connectionState == ConnectionState.Disconnected)
            processHost.disconnect(reason);
        else
            this.processHost.setConnectionState(connectionState, reason);
    }
}