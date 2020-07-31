/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.updater;

import de.shellfire.vpn.LogStreamReader;
import de.shellfire.vpn.Util;
import de.shellfire.vpn.client.ServiceToolsFX;
import de.shellfire.vpn.gui.CanContinueAfterBackEndAvailableFX;
import de.shellfire.vpn.gui.LoginForms;
import de.shellfire.vpn.gui.controller.ProgressDialogController;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.proxy.ProxyConfig;
import de.shellfire.vpn.webservice.EndpointManager;
import de.shellfire.vpn.webservice.WebService;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ProgressBar;
import org.hyperic.sigar.win32.FileVersion;
import org.hyperic.sigar.win32.Win32;
import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;

/**
 *
 * @author TList
 */
public class UpdaterFX implements CanContinueAfterBackEndAvailableFX {
  private static Logger log = Util.getLogger(UpdaterFX.class.getCanonicalName());
  private static final String MAIN_EXE = "ShellfireVPN2.dat";
  private static final String UPDATER_EXE = "ShellfireVPN2.exe";
  private static I18n i18n = VpnI18N.getI18n();
  
  private static WebService service = WebService.getInstance();
  private static int contentLength;


  private String[] args;

  public UpdaterFX(String[] args) {
    this.args = args;
  }

  public UpdaterFX() {
  }

  private static WebService getService() {
    if (service == null) {
      service = WebService.getInstance();
    }
    return service;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    System.setProperty("java.library.path", "./lib");
    ProxyConfig.perform();

    UpdaterFX updaterFX = new UpdaterFX(args);
    updaterFX.run();
  }

  private void run() {
    try {
      String cmd = "";

      if (args.length > 0) {
        cmd = args[0];

        if (cmd.equals("uninstallservice")) {
            ServiceToolsFX.getInstanceForOS().uninstall();
          return;
        } else if (cmd.equals("installservice")) {

          String path = "";
          if (args.length > 1) {
            for (int i = 1; i < args.length; i++) {
              path += args[i];

              if (i + 1 < args.length) {
                path += " ";
              }
            }

          }

          log.debug("Retrieved installation path from args parameter: " + path);
          ServiceToolsFX.getInstanceForOS().install(path);
          return;
        }
      }
      com.sun.javafx.application.PlatformImpl.startup(()->{});
      // Everything else required the backend, so make sure we can access it.
        Platform.runLater(()-> {EndpointManager.getInstance().ensureShellfireBackendAvailableFx(this);});

    } catch (Throwable e) {
      e.printStackTrace();
      e = e.getCause();
      if (e != null)
        e.printStackTrace();
    }

  }

  private static ProgressDialogController updateProgressDialog;

  private void executeJava(String exec) throws IOException {
    Process p = null;
    if (Util.isWindows()) {
      p = Runtime.getRuntime().exec(exec);
    } else {
      List<String> cmds = new LinkedList<String>();
      cmds.add("java");
      cmds.add("-jar");
      cmds.add(exec);

      log.debug("cmds: " + Util.listToString(cmds));
      p = new ProcessBuilder(cmds).start();
    }

    LogStreamReader isr = new LogStreamReader(p.getInputStream(), false);
    Thread thread = new Thread(isr, "InputStreamReader");
    thread.start();

    LogStreamReader esr = new LogStreamReader(p.getErrorStream(), true);
    Thread thread2 = new Thread(esr, "ErrorStreamReader");
    thread2.start();
  }

  private void silentRelaunchElevated() {

    String exec = "";
    if (Util.isVistaOrLater()) {
      exec = "elevate.exe ";
    }

    exec += UpdaterFX.UPDATER_EXE + " doupdate";
    try {
      executeJava(exec);

    } catch (IOException e) {

      this.displayError(i18n.tr("Update could not be processed. Launcher is being shut down.") + ("\r\n") + e.getMessage());

      System.exit(0);
    }
  }

  private void launchApp(String param) {
    String exec = UpdaterFX.MAIN_EXE;

    if (param != null && param.length() > 0) {
      exec += " " + param;
    }
    try {
      log.debug("exec: " + exec);
      executeJava(exec);
    } catch (IOException e) {

      this.displayError(i18n.tr("Main application could not be started. The launcher will now shut down.") + ("\r\n") + e.getMessage());

      System.exit(0);
    }

  }

  private void displayError(String msg) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("");
    alert.setContentText(msg);
    alert.showAndWait();
  }

  private void displayInfo(String msg) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Hinweis");
    alert.setContentText(msg);
    alert.showAndWait();
  }

    @Override
    public ProgressDialogController getDialogFX() {
        return null;
    } 
 
 class MyWorker extends Task<String> {
    private String filename;
    private String installPath;
    private String user;  
    
    public MyWorker(String filename, String path, String user) {
      this.filename = filename;
      this.installPath = path;
      this.user = user;
    }

    @Override
    protected String call() throws Exception {
      try {
        UpdaterFX.downloadAndRunExeFileFromUrl(filename, installPath, user);

        return "";
      } catch (IOException e1) {
        e1.printStackTrace();
        displayError(i18n.tr("I/O error during download of the newest version. Aborting"));
        System.exit(0);

      }
      return "";   
    }

    @Override
    protected void succeeded() {
        if(updateProgressDialog.getDialogStage() != null)
        updateProgressDialog.getDialogStage().hide(); //To change body of generated methods, choose Tools | Templates.
    }
    
    
  }

  public void performUpdate(final String path, String user) {
    try {
        final String fileName = getService().getLatestInstaller();
        final MyWorker w1 = new MyWorker(fileName, path, user);
        updateProgressDialog = ProgressDialogController.getInstance(i18n.tr("Downloading update..."), null, LoginForms.getStage(), false);
        updateProgressDialog.getProgressBar().setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        updateProgressDialog.setOption(2, i18n.tr("cancel"));
        updateProgressDialog.setOptionCallback(new Task() {

        @Override
        protected Object call() throws Exception {
            w1.cancel(true);
            displayError(i18n.tr("Update aborted, shutting down application."));
            System.exit(0);
            return null;
        }
        
        }); 
        Thread t = new Thread(w1);
      t.start();
      
      //work on stage here
      updateProgressDialog.getDialogStage().show();
      try {
        w1.get();
      } catch (CancellationException e) {
        log.error("Error while downloading, download cancelled?", e);
      }

    } catch (InterruptedException e) {
      log.error("Error while downloading", e);
      this.displayError(i18n.tr("Error while downloading new version. Aborting."));
      System.exit(0);
    } catch (ExecutionException e) {
      log.error("Error while downloading", e);
      this.displayError(i18n.tr("Error while downloading new version. Aborting."));
      System.exit(0);
    } catch (IOException ex) {
        log.error("Unable to access file ", ex);
    }

  }

  private static void extractZipFileToCurrentFolder(ZipFile zipFile) throws FileNotFoundException, IOException {
    Enumeration<? extends ZipEntry> entries;
    entries = zipFile.entries();

    while (entries.hasMoreElements()) {
      ZipEntry entry = (ZipEntry) entries.nextElement();

      if (entry.isDirectory()) {
        (new File(entry.getName())).mkdir();
        continue;
      }

      copyInputStream(zipFile.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(entry.getName())));
    }

  }

  public static final void copyInputStream(InputStream in, OutputStream out) throws IOException {
    byte[] buffer = new byte[1024];
    int len;

    while ((len = in.read(buffer)) >= 0)
      out.write(buffer, 0, len);

    in.close();
    out.close();
  }

  private boolean askIfUpdateShouldBePerformed() {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, i18n.tr("Update verfügbar. Ohne Durchführung des Updates ist der weitere Betrieb von Shellfire VPN leider nicht möglich.\n\nJetzt Updaten?"), ButtonType.YES, ButtonType.NO);
    alert.setTitle("Update available");
    Optional<ButtonType> result = alert.showAndWait();
    return ((result.isPresent()) && (result.get() == ButtonType.YES));
  }

  public boolean newVersionAvailable() {
    long installedVersion = this.getInstalledVersion();
    if (installedVersion == 0)
      return false;

    long latstAvailableVersion = this.getLatestAvailableVersionOnline();

    boolean updateAvailable = (latstAvailableVersion > installedVersion);

    return updateAvailable;
  }

  private long getLatestAvailableVersionOnline() {
    long version = 0;

    version = getService().getLatestVersion();

    return version;
  }

  private static long getInstalledVersionWindows() {
    FileVersion info = Win32.getFileVersion(UPDATER_EXE);

    if (info == null) {
      return 0;
    }
    long version = info.getFileMajor() * 1000 + info.getFileMinor();
    return version;

  }

  public static long getInstalledVersion() {
    if (Util.isWindows()) {
      return getInstalledVersionWindows();
    } else {
      try {
        String file = com.apple.eio.FileManager.getPathToApplicationBundle() + "/Contents/Java/VERSION";
        if (!(new File(file).exists())) {
          file = "VERSION";
        }

        return Long.valueOf(Util.fileToString((file)).trim());
      } catch (Exception e) {
        Util.handleException(e);
      }
      return 0;
    }

  }

  private static void downloadAndRunExeFileFromUrl(String url, String installPath, String user) throws IOException {
    log.debug(url);

    URL u = new URL(url);
    String host = u.getHost();
    int port = 80;
    String file = u.getPath();

    Properties props = System.getProperties();

    // modify request in case a proxy is used
    boolean useProxy = props.getProperty("http.proxySet") == "true";
    if (useProxy) {
      host = props.getProperty("http.proxyHost");
      port = Integer.valueOf(props.getProperty("http.proxyPort"));
      file = u.getProtocol() + "://" + u.getHost() + file;
    }

    Socket s = new Socket(host, port);
    PrintWriter out = new PrintWriter(s.getOutputStream(), true);
    InputStream is = s.getInputStream();

    String httpget = "GET " + file + "?tracked=true HTTP/1.0";
    out.println(httpget);
    log.debug(httpget);
    String httphost = "HOST: " + host;
    out.println(httphost);
    log.debug(httphost);
    out.println();

    FileOutputStream fos = null;
    String ext = "";
    if (Util.isWindows()) {
      ext = "exe";
    } else {
      ext = "dmg";
    }

    File f = File.createTempFile("ShellfireVPN_installer", "." + ext);

    fos = new FileOutputStream(f);

    int count = 0;
    byte buf[] = new byte[1024];
    int len;
    boolean afterHeader = false;
    String header = "";

    int contentBytesRead = 0;

    while ((len = is.read(buf)) > 0) {
      if (afterHeader) {
        fos.write(buf, 0, len);

        if (contentLength > 0) {
          contentBytesRead += len;
          float percentage = (float) contentBytesRead / (float) contentLength * 100F;
          updateProgressDialog.updateProgress(percentage);
        }

      } else {
        String str = new String(buf);
        // end of heaader is included, remove it and write what comes after into the file
        if (str.contains("\r\n\r\n")) {
          int offset = 0;
          byte cr = "\r".getBytes()[0];
          byte lf = "\n".getBytes()[0];
          for (int i = 0; i < buf.length; i++) {
            if (i >= 3) {
              if (buf[i - 3] == cr && buf[i - 2] == lf && buf[i - 1] == cr && buf[i] == lf) {
                // offset stores the position of the end of the header
                offset = i + 1;
                break;
              }
            }

          }
          fos.write(buf, offset, buf.length - offset);
          afterHeader = true;
          header += str.substring(0, offset - 1);

          log.debug("------ HEADER ------");
          log.debug(header);
          log.debug("------ HEADER ------");

          String[] lines = header.split("\\r\\n");
          for (String line : lines) {
            if (line.startsWith("Content-Length:"))
              contentLength = Integer.parseInt(line.substring(16));
          }

          log.debug("Length: " + contentLength);

        } else {
          header += str;
        }
      }

      count += 1024;
    }

    is.close();
    fos.close();

    // launch install process
    Process p;
    if (Util.isWindows()) {
      String start = "";

      start += "\"" + f.getAbsolutePath() + "\"";
      log.debug(start);
      p = Runtime.getRuntime().exec(start);
    } else {
      updateProgressDialog.setIndeterminate(true);
      updateProgressDialog.setDialogText(i18n.tr("Uninstalling Shellfire VPN Service"));
      ServiceToolsFX.getInstanceForOS().uninstall(installPath + "/Contents/Java/");
      updateProgressDialog.setDialogText(i18n.tr("Install new version"));

      List<String> unzip = new ArrayList<String>();
      unzip.add("/usr/bin/unzip");
      unzip.add("-o"); // overwrite without prompt
      unzip.add("-q"); // be quiet
      unzip.add(f.getAbsolutePath());
      unzip.add("-d");
      unzip.add(installPath);
      log.debug(Util.listToString(unzip));
      p = new ProcessBuilder(unzip).start();
    }

    LogStreamReader isr = new LogStreamReader(p.getInputStream(), false);
    Thread thread = new Thread(isr, "InputStreamReader");
    thread.start();

    LogStreamReader esr = new LogStreamReader(p.getErrorStream(), true);
    Thread thread2 = new Thread(esr, "ErrorStreamReader");
    thread2.start();

    if (!Util.isWindows()) {
      try {
        p.waitFor();

        List<String> chown = new ArrayList<String>();
        chown.add("/usr/sbin/chown");
        chown.add("-R");
        chown.add(user + ":staff");
        chown.add(installPath);
        log.debug(Util.listToString(chown));
        p = new ProcessBuilder(chown).start();
        Util.digestProcess(p);
        p.waitFor();

      } catch (InterruptedException e) {
        log.error("Error occured during update");
      }
      updateProgressDialog.setDialogText(i18n.tr("Installing Shellfire VPN Service"));
      ServiceToolsFX.getInstanceForOS().install(installPath + "/Contents/Java/");

      List<String> restart = new ArrayList<String>();
      restart.add("/usr/bin/open");
      restart.add(installPath);
      log.debug(Util.listToString(restart));
      new ProcessBuilder(restart).start();
    }

    // shutdown to ensure proper installation is possible
    System.exit(0);

  }

  @Override
  public void continueAfterBackEndAvailabledFX() {
    try {
      String cmd = "";
      if (args.length > 0) {
        cmd = args[0];
        if (cmd.equals("doupdate") && newVersionAvailable()) {
          // assumes we have been restarted with elevated privileges
          performUpdate(cmd, System.getProperty("user.name"));
        }
      }

      if (newVersionAvailable()) {
        if (askIfUpdateShouldBePerformed()) {
          if (Util.isVistaOrLater()) {
            silentRelaunchElevated();
          } else {
            performUpdate(cmd, System.getProperty("user.name"));
          }

        } else {
          displayError(i18n.tr("You chose not to update. The application will now shut down."));
          System.exit(0);
        }
      } else {
        launchApp(cmd);
      }

    } catch (Throwable e) {
      e.printStackTrace();
      e = e.getCause();
      if (e != null)
        e.printStackTrace();
    }
  }

}

