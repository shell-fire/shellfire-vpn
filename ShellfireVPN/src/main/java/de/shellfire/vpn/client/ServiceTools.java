package de.shellfire.vpn.client;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.xnap.commons.i18n.I18n;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.gui.LoginForm;
import de.shellfire.vpn.gui.ProgressDialog;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.webservice.ShellfireService;

public class ServiceTools {
	private static I18n i18n = VpnI18N.getI18n();
	private static String nl = "\r\n";
	private static ProgressDialog loginProgressDialog;

	public static void resetService() {
		//service = null;
	}
	
	public void ensureServiceEnvironment(LoginForm form) throws RemoteException {
		System.out.println("checking if service is running");
		if (!serviceIsRunning()) {
			if (!Util.isWindows()) {
				System.out.println("Service not running, trying request on port 60313 to have launchd start it!");
				tryStartViaPortRequestInSeparateThread();
			}
			
			if (!serviceIsRunning()) {
				System.out.println("service not running - installEelevated()");
				JOptionPane.showMessageDialog(null, i18n.tr("Der Shellfire VPN Service wird jetzt installiert. Gib dazu bitte im nachfolgenden Fenster dein Admin-Passwort ein."));
	            
				form.initDialog.dispose();
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
				System.out.println("serivce has been started");
				form.afterServiceEnvironmentEnsured();
			}
			
			
		} else {
			System.out.println("serivce is running - good to go, no action required");
			form.afterServiceEnvironmentEnsured();
		}

	}

	
	class WaitForServiceTask extends SwingWorker<Void, Object> {
		/*
		 * Main task. Executed in background thread.
		 */

		private LoginForm loginForm;

		public WaitForServiceTask(LoginForm form) {
			this.loginForm = form;
		}

		/*
		 * Executed in event dispatch thread
		 */
		public void done() {
			
			loginProgressDialog.setVisible(false);
			try {
        this.loginForm.afterServiceEnvironmentEnsured();
      } catch (RemoteException e) {
        Util.handleException(e);
      }

		}
		
		@Override
		protected Void doInBackground() throws Exception {
			while (!serviceIsRunning()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					Util.handleException(e);
				}
			}
			
			return null;
		}
	}	
	
	public static void installElevated() {
		String instdir = LoginForm.getInstDir();

		String command = "";
		if (Util.isWindows()) {
			if (Util.isVistaOrLater()) {
				// restart elevated
				command += "\"" + instdir + "elevate.exe\" -wait jre8\\bin\\javaw -jar ShellfireVPN2.exe installservice";

				System.out.println("Command: " + command);
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
				//install();
			}

		} else if (!Util.isWindows()) {
			try {
				
				String installerPath = com.apple.eio.FileManager.getPathToApplicationBundle() + "/Contents/Java/ShellfireVPN2.app";
				System.out.println("Opening installer using /usr/bin/open(): " +  installerPath);

				List<String> cmds = new LinkedList<String>();
				cmds.add("/usr/bin/open");
				cmds.add(installerPath);
				//cmds.add("-jar");
				//cmds.add("ShellfireVPN2.jar");
				//cmds.add("installservice");
				//System.out.println("sudo launch of Updater with installservice, cmds: " + Util.listToString(cmds));
				Process p = new ProcessBuilder(cmds).directory(new File(com.apple.eio.FileManager.getPathToApplicationBundle() + "/Contents/Java/")).start();

				Util.digestProcess(p);

			} catch (UnsupportedEncodingException e) {
				Util.handleException(e);
			} catch (IOException e) {
				Util.handleException(e);
			}

		}

		else {

		}
	}

	private static void startWithoutService() {
		//OpenVpnProcessHost.main(new String[0]);

	}

	private static void uninstallElevated() {
		String instdir = LoginForm.getInstDir();

		String command = "";
		if (Util.isVistaOrLater())
			command += "\"" + instdir + "elevate.exe\" -wait ShellfireVPN2.exe uninstallservice";
		else
			command += "\"" + instdir + "ShellfireVPN2.exe\" uninstallservice";

		Process p;
		try {
			p = Runtime.getRuntime().exec(command, null, new File(instdir));
			Util.digestProcess(p);
		} catch (IOException e) {
			Util.handleException(e);
		}
	}

	// test if port 1099 on localhost is open, if not return false
	private static boolean serviceIsRunning() {
		System.out.println("serviceIsRunning()");
		Socket s = null;
		/*
		try {
			s = new Socket("localhost", OpenVpnProcessHost.SHELLFIRE_REGISTRY_PORT);
			
		} catch (UnknownHostException e) {
			System.out.println("Service not running");
			return false;
		} catch (IOException e) {
			System.out.println("Service not running");
			return false;
		}
*/
		if (s != null) {
			try {
				s.close();
			} catch (IOException e) {
			}
		}

		System.out.println("Service running");
		return true;
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
			System.out.println("result: true");
			return true;
			
		}
					
		System.out.println("result: false");
		
		return false;
	}
	
	class PortRequest extends Thread {
		public Boolean result = null;
		
		public void run() {
			Socket s = null;
			try {
				s = new Socket("localhost", 60313);
				System.out.println("socket opened - yeah!");
			} catch (UnknownHostException e) {
				this.result = false;
				return;
			} catch (IOException e) {
				this.result = false;
				return;
			}

			if (s != null) {
				try {
					System.out.println("closing socket");
					s.close();
				} catch (IOException e) {
				}
			}

			this.result = true;
		}
	}
/*
	public static WrappedService initService(String instDir) {
		if (service == null) {
			System.out.println("initService()");
			if (Util.isWindows())
				ShellfireService.createConfigDirIfNotExists();
			

			System.out.println("instDIr: " + instDir);

			String sep = "";
			if (Util.isWindows()) {
				sep = "\\";
			} else
				sep = "/";

			String startConfig = instDir + sep + "start.conf";
			String stopConfig = instDir + sep + "stop.conf";
			
			if (!Util.isWindows()) {
				startConfig = "/etc/shellfirevpn.conf";
			}
			
			System.out.println("startConfig=" + startConfig);
			System.out.println("stopConfig=" + stopConfig);

			writeConfigFiles(instDir, startConfig, stopConfig);

			System.setProperty("wrapper.config", startConfig);
			WrappedService w = new WrappedService();
			w.init(); // read in configuration

			service = w;
		}

		System.out.println("initService() - return");
		return service;
	}*/
  /*
	public static void install() {
		String instDir = LoginForm.getInstDir();
		install(instDir);
	}

	public static void uninstall() {
		String instDir = LoginForm.getInstDir();
		uninstall(instDir);
	}
	
	public static void uninstall(String path) {
		if (Util.isWindows()) {
			WrappedService w = initService(path);
			w.stop();
			w.uninstall(); // stop the service
		} else {
			// we dont need no big yajsw. lets do it without!
			// load plist-template
			String plistPath = "/Library/LaunchDaemons/ShellfireVPNService.plist";
			
			// launchctl unload
			String[] params = new String[] { "/bin/launchctl", "unload", plistPath };
			Process p2;
			try {
				p2 = new ProcessBuilder(params).start();			
				p2.waitFor();
				
				System.out.println("sleeping for 3 seconds to let the task stop");
				Thread.sleep(3000);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			
			// deletion of plist file
			params = new String[] {"/bin/rm", plistPath};
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
	
	}*/

	private static void writeConfigFiles(String instDir, String startConfigFile, String stopConfigFile) {
		String libPath;
		String binPath;

		if (Util.isWindows()) {
			libPath = ".\\\\jre8\\\\bin\\\\";
			binPath = instDir.replace("\\", "\\\\") + "jre8\\\\bin\\\\java";
		} else {
			libPath = "/System/Library/Java/Extensions";
			libPath = System.getProperty("java.home") + "/lib";
			
			binPath = System.getProperty("java.home") + "/bin/java";
			try
			{
				binPath = new File(binPath).getCanonicalPath();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			protectKext(instDir);
		}

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

		if (Util.isWindows()) {
			start += "wrapper.launchd.dir=" + ShellfireService.CONFIG_DIR.replace("\\", "\\\\") + nl
					+ "wrapper.stop.conf=" + stopConfigFile.replace("\\", "\\\\") + nl;
			
			
			String stop = "wrapper.stopper=true" + nl 
					+ "wrapper.app.parameter.1=stop" + nl 
					+ "include=start.conf";

			System.out.println("Stop Config: ");
			
			Util.stringToFile(stop, stopConfigFile, true);			
			
			
		} else {
			start += nl + "wrapper.launchd.dir=/Library/LaunchDaemons/" + nl
						+ "wrapper.logfile=/var/log/ShellfireVPN/ShellfireVPN.log" + nl
						+ "wrapper.java.additional.1=-Dapple.awt.UIElement=true" + nl;
			start = start.replace(File.separatorChar + File.separator, File.separator);
			start = start.replace(" ", "\\ ");
			
		}
		System.out.println("Start Config:");
		System.out.println(start);
		Util.stringToFile(start, startConfigFile, true);


	}

	static void protectKext(String instDir) {
		String[] params;
		Process p2;

		params = new String[] { "/usr/sbin/chown", "-R", "root:wheel", instDir + "/openvpn/tun.kext" };
		System.out.println("setting permissions " + params[2] + " on " + params[3]);
		try {
			p2 = new ProcessBuilder(params).start();
			Util.digestProcess(p2);
			p2.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		params = new String[] { "/bin/chmod", "-R", "755", instDir + "/openvpn/tun.kext/" };
		System.out.println("setting permissions " + params[2] + " on " + params[3]);
		try {
			p2 = new ProcessBuilder(params).start();
			Util.digestProcess(p2);
			p2.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		params = new String[] { "/bin/mkdir", "/var/log/ShellfireVPN" };
		System.out.println("mkdir " + params[1]);
		try {
			p2 = new ProcessBuilder(params).start();
			Util.digestProcess(p2);
			p2.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
/*
	public static void install(String path) {
		System.out.println("install("+path+", ");
		
		if (Util.isWindows()) {
			path = LoginForm.getInstDir();
			WrappedService w = initService(path);

			System.out.println("installing service");
			if (w.install()) {
				System.out.println("service installed");
			} else {
				System.out.println("could not install service");
			}

			if (w.start()) {
				System.out.println("service started");
			} else {
				System.out.println("could not start service");
			}
	
		} else {
			// we dont need no big yajsw. lets do it without!
			// load plist-template
			try {
				// prepare and write plist
				String plistTemplate = Util.fileToString(path + "ShellfireVPNService-template.plist");
				String javaPath = System.getProperty("java.home") + "/bin/java";
				try
				{
					javaPath = new File(javaPath).getCanonicalPath();
				}
				catch (IOException e)
				{
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
					
					System.out.println("sleeping for 3 seconds to let the task start-up");
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
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} catch (IOException e) {
				Util.handleException(e);
			}
		}
				
	}
	*/
}
