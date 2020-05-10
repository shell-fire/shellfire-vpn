package de.shellfire.vpn.client.win;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.client.ServiceTools;
import de.shellfire.vpn.gui.LoginForm;
import de.shellfire.vpn.gui.ProgressDialog;
import de.shellfire.vpn.i18n.VpnI18N;

public class WinServiceTools extends ServiceTools {
  private static Logger log = Util.getLogger(WinServiceTools.class.getCanonicalName());
  private static I18n i18n = VpnI18N.getI18n();

  @Override
  public void ensureServiceEnvironment(LoginForm form) {
    log.debug("checking if service is running");
    
    if (!serviceIsRunning()) {
      log.debug("service not running - installElevated()");
      JOptionPane.showMessageDialog(null,
          i18n.tr("Shellfire VPN service is now being installed. Please enter your admin password in the next window."));

      LoginForm.initDialog.dispose();
      installElevated();

      loginProgressDialog = new ProgressDialog(form, false, i18n.tr("Installing Service..."));
      loginProgressDialog.setOption(2, i18n.tr("cancel"));
      loginProgressDialog.setOptionCallback(new Runnable() {

        @Override
        public void run() {
          JOptionPane.showMessageDialog(null, i18n.tr("Service has not been installed correctly - Shellfire VPN is now exited"));
          System.exit(0);
        }
      });

      loginProgressDialog.setVisible(true);

      WaitForServiceTask task = new WaitForServiceTask(form);
      task.execute();
    } else {
      log.debug("serivce has been started");
      form.afterShellfireServiceEnvironmentEnsured();
    }
  }
  /**
   * Assumes elevation
   */
  public void install(String path) {
    log.debug("install()");
    
    try {
    String jarFile = Util.getPathJar();
    String instDir = new File(jarFile).getParent() + File.separator;

      String template = Util.fileToString(instDir + "InstallServiceTemplate.txt");
      String procRunPath = instDir + getProcrunExe(); 
      
      
      template = template.replace("$$TEMP$$", Util.getTempDir());
      template = template.replace("$$LOGFILE$$", Util.getTempDir()+File.separator + "ProcRunLog.log");
      template = template.replace("$$JVM_DLL$$",  Util.getJvmDll());
      template = template.replace("$$PROCRUNPATH$$", procRunPath);
      
      template = template.replace("$$SHELLFIREVPNSERVICEDAT$$", jarFile);
      
      String installBat = instDir + "InstallService.bat";
      Util.stringToFile(template, installBat);
      
      String command = String.format("%s /C \"%s\"", Util.getCmdExe(), installBat);
      log.debug("Running command {}", command);
      Process p = Runtime.getRuntime().exec(command, null, new File(instDir));
      Util.digestProcess(p);
      p.waitFor();
      log.debug("service installed (or not?); - exiting");
    } catch (IOException e) {
      Util.handleException(e);
    } catch (InterruptedException e) {
      Util.handleException(e);
    }
  }
  


  private String getProcrunExe() {
    String procRunExe = "";
    
    String jvmArch = System.getProperty("sun.arch.data.model");
    if (jvmArch.equals("32")) {
      procRunExe = "ShellfireVPNService32.exe";
    } else {
      procRunExe = "ShellfireVPNService64.exe";
    }
    
    return procRunExe;
  }

  /**
   * assumes elevation
   */
  public void uninstall(String path) {
    log.debug("uninstall()");
    
    try {
      String jarFile = Util.getPathJar();
      String instDir = new File(jarFile).getParent() + File.separator;

      String template = Util.fileToString(instDir + "UninstallServiceTemplate.txt");
      String procRunPath = instDir + getProcrunExe(); 
      
      template = template.replace("$$PROCRUNPATH$$", procRunPath);
      
      String uninstallBat = instDir + "UninstallService.bat";
      Util.stringToFile(template, uninstallBat);
      
      String command = String.format("%s /C \"%s\"", Util.getCmdExe(), uninstallBat);
      log.debug("Running command {}", command);
      Process p = Runtime.getRuntime().exec(command, null, new File(instDir));
      Util.digestProcess(p);
      p.waitFor();
      log.debug("service installed (or not?); - exiting");
    } catch (IOException e) {
      Util.handleException(e);
    } catch (InterruptedException e) {
      Util.handleException(e);
    }

  }

  public void installElevated() {
    log.debug("installElevated() - start");

    if (Util.isVistaOrLater()) {
      // restart elevated
      
      String pathJavaw = Util.getJavaHome() + "\\bin\\javaw.exe";
      String jarFile = Util.getPathJar();
      File instDir = new File(jarFile).getParentFile();
      String arg = "installservice";

      // Check for execution from dev environment, will fail anyway
      if (jarFile == null) {
        log.warn("Path to Jar not found - elevated Relaunch only supported from deployed jar!");
      } else {
        String elevateVbs = System.getProperty("java.io.tmpdir") + "/elevate.vbs";
        
        String exec = pathJavaw;
        String cmds = "-jar \"\"" + jarFile + "\"\" " + arg;
       
        writeElevationVbsFile(elevateVbs, exec, cmds);
        
        try {
          String command = Util.getCscriptExe() + " " + elevateVbs;
          log.debug("Calling elevateVbs with command {} in dir {}", command, instDir.getAbsolutePath());
          Process p = Runtime.getRuntime().exec(command, null, instDir);
          Util.digestProcess(p);

          long start = System.currentTimeMillis();

          // wait until process is finished or 10 seconds have passed. 10 seconds should really be enough.
          while (!hasFinished(p) && System.currentTimeMillis() - start < 10000) {
            Util.sleep(50);
            ;
          }

        } catch (Exception e) {
          Util.handleException(e);
        }
      }
    } else {
      log.debug("starting install without elevation on Windows XP");
      install();
    }

    log.debug("installElevated()- finish");
  }

  public static boolean hasFinished(Process p) {
    try {
      p.exitValue();
      return true;
    } catch (IllegalThreadStateException e) {
      return false;
    }
  }


  public static void writeElevationVbsFile(String elevationVbsFile, String exe, String cmds) {
    log.debug("creating elevationVbsFile at {}", elevationVbsFile);
    File file = new File(elevationVbsFile);
    file.delete();
    //file.deleteOnExit();
    try {
      try (FileWriter fw = new FileWriter(file, true)) {
        fw.write(String.format("Set objShell = CreateObject(\"Shell.Application\")\r\n"+
               "exec = \"%s\"\r\n"+
               "cmds = \"%s\"\r\n"+
               "objShell.ShellExecute exec, cmds, \"\", \"runas\"", exe, cmds)); 

      }
    } catch (IOException e) {
      log.error("Erorr occured during elevate.bat creation", e);
    }

  }


}
