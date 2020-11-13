/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.client;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.client.win.WinServiceToolsFX;
import de.shellfire.vpn.gui.controller.LoginController;
import de.shellfire.vpn.gui.controller.ProgressDialogController;
import de.shellfire.vpn.service.Service;
import java.io.IOException;
import javafx.concurrent.Task;
import org.slf4j.Logger;

/**
 *
 * @author Tcheutchoua Steve
 */
public abstract class ServiceToolsFX {

	private static Logger log = Util.getLogger(ServiceToolsFX.class.getCanonicalName());
	protected static String nl = "\r\n";
	protected static ProgressDialogController loginProgressDialog;
	protected static boolean init;
	private static ServiceToolsFX instance;

	public abstract void ensureServiceEnvironmentFX(LoginController form);

	public abstract void uninstall(String path);

	public abstract void install(String path);

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

		log.debug("serviceIsRunning - finished - returnung result {}", result);
		return result;
	}

	public void install() {
		String instDir = LoginController.getInstDir();
		install(instDir);
	}

	public void uninstall() {
		String instDir = LoginController.getInstDir();
		uninstall(instDir);
	}

	public class WaitForServiceTask extends Task<Object> {

		private LoginController loginForm;

		public WaitForServiceTask(LoginController loginForm) {
			this.loginForm = loginForm;
		}

		/**
		 * Executes when the thread has changed to succeeded state
		 */
		@Override
		protected void succeeded() {
			ProgressDialogController.getDialogStage().hide();
			log.debug("ServiceToolsFX is has succeeded");
			this.loginForm.afterShellfireServiceEnvironmentEnsured();
		}

		@Override
		protected Object call() throws Exception {
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

	public static ServiceToolsFX getInstanceForOS() {
		if (instance == null) {
			if (Util.isWindows()) {
				instance = new WinServiceToolsFX();
			} else {
				instance = new WinServiceToolsFX();
				;
				log.error("Please run javafx on windows");
			}
		}

		return instance;
	}
}
