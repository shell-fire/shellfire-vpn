package de.shellfire.vpn.client;

import java.io.IOException;
import java.rmi.RemoteException;

import javax.swing.SwingWorker;

import org.slf4j.Logger;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.client.osx.OSXServiceTools;
import de.shellfire.vpn.client.win.WinServiceTools;
import de.shellfire.vpn.gui.LoginForm;
import de.shellfire.vpn.gui.ProgressDialog;
import de.shellfire.vpn.service.Service;

public abstract class ServiceTools {
  private static Logger log = Util.getLogger(ServiceTools.class.getCanonicalName());
	protected static String nl = "\r\n";
	protected static ProgressDialog loginProgressDialog;
  protected static boolean init;
  private static ServiceTools instance;

	
	public abstract void ensureServiceEnvironment(LoginForm form) throws RemoteException;

	
	public class WaitForServiceTask extends SwingWorker<Void, Object> {
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
	
	public abstract void installElevated();

	public static void startWithoutService() {
		Service.main(new String[0]);
	}


	protected static boolean serviceIsRunning() {
		log.debug("serviceIsRunning() - start");
		boolean result = false;		
		
		try {
      Client client = Client.getInstance();
      
      if (client.ping()) {
        result = true;
      }
      
    } catch (IOException e) {
      log.error("error occured during serviceIsRunning() - returning false", e);
    }

		log.debug("serviceIsRunning - finsihed - returnung result {}", result);
		return result;
	}

  public abstract void uninstall(String path);
  public abstract void install(String path);

	public void install() {
		String instDir = LoginForm.getInstDir();
		install(instDir);
	}
	public void uninstall() {
		String instDir = LoginForm.getInstDir();
		uninstall(instDir);
	}

  public static ServiceTools getInstanceForOS() {
    if (instance == null) {
      if (Util.isWindows()) {
        instance = new WinServiceTools();
      } else {
        instance = new OSXServiceTools();
      }
    }
      
    return instance;
  }
	
}
