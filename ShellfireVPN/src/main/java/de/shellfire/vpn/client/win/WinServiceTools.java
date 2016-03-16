package de.shellfire.vpn.client.win;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.client.ServiceTools;
import de.shellfire.vpn.gui.LoginForm;
import de.shellfire.vpn.gui.ProgressDialog;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.webservice.WebService;

public class WinServiceTools extends ServiceTools {
  private static Logger log = Util.getLogger(WinServiceTools.class.getCanonicalName());
  private static I18n i18n = VpnI18N.getI18n();

  @Override
  public void ensureServiceEnvironment(LoginForm form) throws RemoteException {
    log.debug("checking if service is running");
    if (!serviceIsRunning()) {
      log.debug("service not running - installEelevated()");
      JOptionPane.showMessageDialog(null,
          i18n.tr("Der Shellfire VPN Service wird jetzt installiert. Gib dazu bitte im nachfolgenden Fenster dein Admin-Passwort ein."));

      LoginForm.initDialog.dispose();
      installElevated();

      loginProgressDialog = new ProgressDialog(form, false, i18n.tr("Installiere Service.."));
      loginProgressDialog.setOption(2, i18n.tr("abbrechen"));
      loginProgressDialog.setOptionCallback(new Runnable() {

        @Override
        public void run() {
          JOptionPane.showMessageDialog(null, i18n.tr("Service wurde nicht korrekt installiert - Shellfire VPN wird jetzt beendet."));
          System.exit(0);
        }
      });

      loginProgressDialog.setVisible(true);

      WaitForServiceTask task = new WaitForServiceTask(form);
      task.execute();
    } else {
      log.debug("serivce has been started");
      form.afterServiceEnvironmentEnsured();
    }
  }

  public void install(String path) {
    log.debug("install(" + path + ", ");

    path = LoginForm.getInstDir();
    log.debug("installing service");
    // TODO: procrun implementation
    // SEE: https://joerglenhard.wordpress.com/2012/05/29/build-windows-service-from-java-application-with-procrun/
    log.debug("startingservice");
    // TODO: procrun implementation

  }
  
  protected void writeConfigFiles(String instDir, String startConfigFile, String stopConfigFile) {
    String libPath;
    String binPath;

    // TODO: procrun implementation
    libPath = ".\\\\jre8\\\\bin\\\\";
    binPath = instDir.replace("\\", "\\\\") + "jre8\\\\bin\\\\java";
    

    String start = "" + "wrapper.working.dir=" + instDir.replace("\\", "\\\\") + nl 
        + "wrapper.java.app.jar=ShellfireVPN2Service.dat" + nl
        + "wrapper.console.title=ShellfireVPN2Service" + nl 
        + "wrapper.ntservice.name=ShellfireVPN2Service" + nl
        + "wrapper.ntservice.displayname=ShellfireVPN2Service" + nl
        + "wrapper.ntservice.description=The ShellfireVPN2Service to handle VPN connections" + nl 
        + "wrapper.java.library.path.1=" + libPath + nl
        + "wrapper.java.command=" + binPath + nl 
        + "wrapper.java.classpath.1=." + nl 
        + "wrapper.console.loglevel=DEBUG" + nl
        + "wrapper.debug=true" + nl
        + "wrapper.ntservice.starttype=AUTOMATIC" + nl;

      start += "wrapper.launchd.dir=" + WebService.CONFIG_DIR.replace("\\", "\\\\") + nl
          + "wrapper.stop.conf=" + stopConfigFile.replace("\\", "\\\\") + nl;
      
      
      String stop = "wrapper.stopper=true" + nl 
          + "wrapper.app.parameter.1=stop" + nl 
          + "include=start.conf";

      log.debug("Stop Config: ");
      
      Util.stringToFile(stop, stopConfigFile, true);      
    
    log.debug("Start Config:");
    log.debug(start);
    Util.stringToFile(start, startConfigFile, true);
  }
  public void uninstall(String path) {
    //TODO: procrun implementation


  }

  public void initService(String instDir) {
    if (!init) {
      log.debug("initService()");
      WebService.createConfigDirIfNotExists();
      

      log.debug("instDIr: " + instDir);

      String sep = "";
      sep = "\\";

      String startConfig = instDir + sep + "start.conf";
      String stopConfig = instDir + sep + "stop.conf";
      
      log.debug("startConfig=" + startConfig);
      log.debug("stopConfig=" + stopConfig);

      writeConfigFiles(instDir, startConfig, stopConfig);

      System.setProperty("wrapper.config", startConfig);

      init = true;
    }

    log.debug("initService() - return");
  }
  

  public void installElevated() {
    String instdir = LoginForm.getInstDir();

    String command = "";
      if (Util.isVistaOrLater()) {
        // restart elevated
        command += "\"" + instdir + "elevate.exe\" -wait jre8\\bin\\javaw -jar ShellfireVPN2.exe installservice";

        log.debug("Command: " + command);
        Process p;
        try {
          p = Runtime.getRuntime().exec(command, null, new File(instdir));
          Util.digestProcess(p);
        } catch (IOException e) {
          Util.handleException(e);
        }

        try {
          Thread.sleep(5000);
        } catch (InterruptedException e) {
          Util.handleException(e);
          e.printStackTrace();
        }
      } else {
        // xp or before: just do it
        install();
      }

    
  }
  
  
}
