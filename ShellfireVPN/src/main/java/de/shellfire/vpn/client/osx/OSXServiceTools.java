package de.shellfire.vpn.client.osx;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.client.ServiceTools;
import de.shellfire.vpn.gui.LoginForm;
import de.shellfire.vpn.gui.LoginForms;
import de.shellfire.vpn.gui.ProgressDialog;
import de.shellfire.vpn.i18n.VpnI18N;

public class OSXServiceTools extends ServiceTools {
	private static Logger log = Util.getLogger(OSXServiceTools.class.getCanonicalName());
	private static I18n i18n = VpnI18N.getI18n();

	@Override
	public void ensureServiceEnvironment(LoginForm form) {
		log.debug("checking if service is running");
		if (!serviceIsRunning()) {
			log.debug("Service not running, trying request on port 60313 to have launchd start it!");
			tryStartViaPortRequestInSeparateThread();

			if (!serviceIsRunning()) {
				log.debug("service not running - installElevated()");
				JOptionPane.showMessageDialog(null, i18n.tr(
						"Shellfire VPN service is now being installed. Please enter your admin password in the next window."));

				form.initDialog.dispose();
				installElevated();

				loginProgressDialog = new ProgressDialog(form, false, i18n.tr("Installing Service..."));
				loginProgressDialog.setOption(2, i18n.tr("cancel"));
				loginProgressDialog.setOptionCallback(new Runnable() {

					@Override
					public void run() {
						JOptionPane.showMessageDialog(null,
								i18n.tr("Service has not been installed correctly - Shellfire VPN is now exited"));
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

		} else {
			log.debug("serivce is running - good to go, no action required");
			form.afterShellfireServiceEnvironmentEnsured();
		}
	}

	protected boolean serviceIsInstalled() {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean tryStartViaPortRequestInSeparateThread() {

		PortRequest pr = new PortRequest();
		pr.start();

		int sleepTime = 0;
		while (pr.result == null && sleepTime < 3000) {
			try {
				sleepTime += 50;
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (pr.result == true) {
			log.debug("result: true");
			return true;

		}

		log.debug("result: false");

		return false;
	}

    @Override
    public void ensureServiceEnvironmentFX(LoginForms form) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

	class PortRequest extends Thread {
		public Boolean result = null;

		public void run() {
			Socket s = null;
			try {
				s = new Socket("localhost", 60313);
				log.debug("socket opened - yeah!");
			} catch (UnknownHostException e) {
				this.result = false;
				return;
			} catch (IOException e) {
				this.result = false;
				return;
			}

			if (s != null) {
				try {
					log.debug("closing socket");
					s.close();
				} catch (IOException e) {
				}
			}

			this.result = true;
		}
	}

	public void install(String path) {
		log.debug("install(" + path + ", ");

		// we dont need no big yajsw. lets do it without!
		// load plist-template
		try {
			// prepare and write plist
			String plistTemplate = Util.fileToString(path + "ShellfireVPNService-template.plist");
			String javaPath = System.getProperty("java.home") + "/bin/java";
			try {
				javaPath = new File(javaPath).getCanonicalPath();
			} catch (IOException e) {
				e.printStackTrace();
			}

			String serviceJar = path + "ShellfireVPN2Service.dat";

			String pListContent = plistTemplate.replace("%javaPath%", javaPath);
			pListContent = pListContent.replace("%serviceJar%", serviceJar);
			pListContent = pListContent.replace("%workingDirectory%", path);
			String plistPath = "/Library/LaunchDaemons/ShellfireVPNService.plist";
			Util.stringToFile(pListContent, plistPath);

			// launchctl load
			String[] params = new String[] { "/bin/launchctl", "load", plistPath };
			Process p2;
			try {
				p2 = new ProcessBuilder(params).start();
				p2.waitFor();

				log.debug("sleeping for 3 seconds to let the task start-up");
				Thread.sleep(3000);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			protectKext(path);

			// launchctl start
			params = new String[] { "/bin/launchctl", "start", "ShellfireVPNService" };
			try {
				p2 = new ProcessBuilder(params).start();
				p2.waitFor();

			} catch (IOException e) {
				log.error("IOException during chmod 755 openvpn/tun.kext", e);

			} catch (InterruptedException e) {
				log.error("InterruptedException during chmod 755 openvpn/tun.kext", e);
			}
		} catch (IOException e) {
			Util.handleException(e);
		}
	}

	public void protectKext(String instDir) {
		String[] params;
		Process p2;

		params = new String[] { "/usr/sbin/chown", "-R", "root:wheel", instDir + "/openvpn/tun.kext" };
		log.debug("setting permissions " + params[2] + " on " + params[3]);
		try {
			p2 = new ProcessBuilder(params).start();
			Util.digestProcess(p2);
			p2.waitFor();
		} catch (IOException e) {
			log.error("IOException during chmod 755 openvpn/tun.kext", e);

		} catch (InterruptedException e) {
			log.error("InterruptedException during chmod 755 openvpn/tun.kext", e);
		}

		params = new String[] { "/bin/chmod", "-R", "755", instDir + "/openvpn/tun.kext/" };
		log.debug("setting permissions " + params[2] + " on " + params[3]);
		try {
			p2 = new ProcessBuilder(params).start();
			Util.digestProcess(p2);
			p2.waitFor();
		} catch (IOException e) {
			log.error("IOException during chmod 755 openvpn/tun.kext", e);

		} catch (InterruptedException e) {
			log.error("InterruptedException during chmod 755 openvpn/tun.kext", e);
		}

	}

	public void uninstall(String path) {
		// we dont need no big yajsw. lets do it without!
		// load plist-template
		String plistPath = "/Library/LaunchDaemons/ShellfireVPNService.plist";

		// launchctl unload
		String[] params = new String[] { "/bin/launchctl", "unload", plistPath };
		Process p2;
		try {
			p2 = new ProcessBuilder(params).start();
			p2.waitFor();

			log.debug("sleeping for 3 seconds to let the task stop");
			Thread.sleep(3000);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// deletion of plist file
		params = new String[] { "/bin/rm", plistPath };
		Process p;
		try {
			p = new ProcessBuilder(params).start();
			p.waitFor();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void installElevated() {
		String instdir = LoginForm.getInstDir();
		String command = "";
		try {
			String installerPath = com.apple.eio.FileManager.getPathToApplicationBundle()
					+ "/Contents/Java/ShellfireVPN2.app";
			log.debug("Opening installer using /usr/bin/open(): " + installerPath);

			List<String> cmds = new LinkedList<String>();
			cmds.add("/usr/bin/open");
			cmds.add(installerPath);
			Process p = new ProcessBuilder(cmds)
					.directory(new File(com.apple.eio.FileManager.getPathToApplicationBundle() + "/Contents/Java/"))
					.start();

			Util.digestProcess(p);

		} catch (UnsupportedEncodingException e) {
			Util.handleException(e);
		} catch (IOException e) {
			Util.handleException(e);
		}

	}

}
