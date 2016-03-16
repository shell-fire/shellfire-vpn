package de.shellfire.vpn.updater;

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
import java.util.Properties;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

import org.hyperic.sigar.win32.FileVersion;
import org.hyperic.sigar.win32.Win32;
import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;

import de.shellfire.vpn.LogStreamReader;
import de.shellfire.vpn.Util;
import de.shellfire.vpn.client.ServiceTools;
import de.shellfire.vpn.gui.ProgressDialog;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.proxy.ProxyConfig;
import de.shellfire.vpn.webservice.WebService;

public class Updater {
  private static Logger log = Util.getLogger(Updater.class.getCanonicalName());
  private static final String MAIN_EXE = "ShellfireVPN2.dat";
  private static final String UPDATER_EXE = "ShellfireVPN2.exe";
  private static I18n i18n = VpnI18N.getI18n();

  private static WebService service = WebService.getInstance();
  private static int contentLength;

  static {
    setLookAndFeel();
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
    try {
      ProxyConfig.perform();

      Updater updater = new Updater();
      String cmd = "";

      if (args.length > 0) {
        cmd = args[0];

        if (cmd.equals("uninstallservice")) {
          ServiceTools.getInstanceForOS().uninstall();
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
          ServiceTools.getInstanceForOS().install(path);
          return;
        } else if (cmd.equals("doupdate") && updater.newVersionAvailable()) {
          // assumes we have been restarted with elevated privileges

          updater.performUpdate(cmd, System.getProperty("user.name"));
        }
      }

      if (updater.newVersionAvailable()) {
        if (updater.askIfUpdateShouldBePerformed()) {
          if (Util.isVistaOrLater()) {
            updater.silentRelaunchElevated();
          } else {
            updater.performUpdate(cmd, System.getProperty("user.name"));
          }

        } else {
          updater.displayError(i18n.tr("Du hast dich entschieden, das Update nicht durchzuführen. Die Anwendung wird sich beenden."));
          System.exit(0);
        }
      } else {
        updater.launchApp(cmd);
      }

    } catch (Throwable e) {
      e.printStackTrace();
      e = e.getCause();
      if (e != null)
        e.printStackTrace();
    }
  }

  private static ProgressDialog updateProgressDialog;

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

    exec += Updater.UPDATER_EXE + " doupdate";
    try {
      executeJava(exec);

    } catch (IOException e) {

      this.displayError(i18n.tr("Update konnte nicht durchgeführt werden. Launcher wird beendet.") + ("\r\n") + e.getMessage());

      System.exit(0);
    }
  }

  private void launchApp(String param) {
    String exec = Updater.MAIN_EXE;

    if (param != null && param.length() > 0) {
      exec += " " + param;
    }
    try {
      log.debug("exec: " + exec);
      executeJava(exec);
    } catch (IOException e) {

      this.displayError(i18n.tr("Hauptanwendung konnte nicht gestartet werden. Launcher wird beendet.") + ("\r\n") + e.getMessage());

      System.exit(0);
    }

  }

  private void displayError(String msg) {
    JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
  }

  private void displayInfo(String msg) {
    JOptionPane.showMessageDialog(null, msg, "Hinweis", JOptionPane.INFORMATION_MESSAGE);
  }

  class MyWorker extends SwingWorker<String, Object> {

    private String filename;
    private String installPath;
    private String user;

    public MyWorker(String filename, String path, String user) {
      this.filename = filename;
      this.installPath = path;
      this.user = user;
    }

    protected String doInBackground() {

      try {
        Updater.downloadAndRunExeFileFromUrl(filename, installPath, user);

        return "";
      } catch (IOException e1) {
        e1.printStackTrace();
        displayError(i18n.tr("I/O Fehler während des Downloades der neuen Version. Abbruch."));
        System.exit(0);

      }
      return "";
    }

    protected void done() {
      updateProgressDialog.dispose();
    }
  }

  public void performUpdate(final String path, String user) {
    try {
      final String fileName = getService().getLatestInstaller();

      updateProgressDialog = new ProgressDialog(null, false, i18n.tr("Downloade neue Version..."));

      updateProgressDialog.setOption(2, i18n.tr("abbrechen"));
      final MyWorker w1 = new MyWorker(fileName, path, user);
      updateProgressDialog.setOptionCallback(new Runnable() {
        @Override
        public void run() {
          w1.cancel(true);
          displayError(i18n.tr("Update abgebrochen. Anwendung wird beendet."));
          System.exit(0);
        }
      });

      w1.execute();

      updateProgressDialog.setVisible(true);
      try {
        w1.get();
      } catch (CancellationException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

    } catch (IOException e) {
      this.displayError(i18n.tr("I/O Fehler während des Downloades der neuen Version. Abbruch."));
      System.exit(0);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ExecutionException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
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
    int result = JOptionPane.showConfirmDialog(null,
        i18n.tr(
            "Update verfügbar. Ohne Durchführung des Updates ist der weitere Betrieb von Shellfire VPN leider nicht möglich.\n\nJetzt Updaten?"),
        i18n.tr("Update verfügbar"), JOptionPane.YES_NO_OPTION);

    return (result == JOptionPane.YES_OPTION);
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

    try {
      version = getService().getLatestVersion();

    } catch (IOException e) {
      this.displayError(i18n.tr("I/O Fehler beim Abruf der Online Versionsinformation. Anwendung wird beendet."));
      System.exit(0);
    }

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

  private static void setLookAndFeel() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception ex) {
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
      updateProgressDialog.setText(i18n.tr("Shellfire VPN Service deinstallieren..."));

      ServiceTools.getInstanceForOS().uninstall(installPath + "/Contents/Java/");

      updateProgressDialog.setText(i18n.tr("Neue Version installieren "));

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
      updateProgressDialog.setText(i18n.tr("Shellfire VPN Service installieren..."));
      ServiceTools.getInstanceForOS().install(installPath + "/Contents/Java/");

      List<String> restart = new ArrayList<String>();
      restart.add("/usr/bin/open");
      restart.add(installPath);
      log.debug(Util.listToString(restart));
      new ProcessBuilder(restart).start();
    }

    // shutdown to ensure proper installation is possible
    System.exit(0);

  }

}
