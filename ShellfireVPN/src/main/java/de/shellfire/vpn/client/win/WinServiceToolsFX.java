/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.client.win;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.client.ServiceToolsFX;
import de.shellfire.vpn.gui.LoginForms;
import de.shellfire.vpn.gui.controller.LoginController;
import de.shellfire.vpn.gui.controller.ProgressDialogController;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.service.win.WindowsVpnController;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;

/**
 *
 * @author TList
 */
public class WinServiceToolsFX extends ServiceToolsFX {

	private static Logger log = Util.getLogger(WinServiceToolsFX.class.getCanonicalName());
	private static I18n i18n = VpnI18N.getI18n();

	@Override
	public void ensureServiceEnvironmentFX(LoginController form) {
		log.debug("checking if service is running");

		if (!serviceIsRunning()) {
			log.debug("service not running - installElevated()");
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setContentText(
					i18n.tr("Shellfire VPN service is now being installed. Please enter your admin password in the next window."));
			alert.showAndWait();
			log.debug("loginProgressDialog.hide();");
			if (loginProgressDialog != null) {
				loginProgressDialog.hide();	
			}
			
			log.debug("calling installElevated()");
			installElevated();
			log.debug("returned from calling installElevated()");
			
			log.debug("showing progressDialog");
			try {
				loginProgressDialog = ProgressDialogController.getInstance(i18n.tr("Installing Service..."), null, LoginForms.getStage(), true);
				loginProgressDialog.setButtonText(i18n.tr("Cancel"));
				loginProgressDialog.setOptionCallback(new Task() {

					@Override
					protected Object call() throws Exception {
						Alert alert = new Alert(Alert.AlertType.INFORMATION);
						alert.setContentText(i18n.tr("Service has not been installed correctly - Shellfire VPN is now exited"));
						System.exit(0);
						return null;
					}
				});

				loginProgressDialog.getDialogStage().show();
			} catch (IOException ex) {
				log.error(ex.getMessage());
			}
			
			log.debug("Starting WaitForServiceTask");
			WaitForServiceTask task = new WaitForServiceTask(form);
			Thread taskrun = new Thread(task);
			taskrun.start();
		} else {
			log.debug("serivce has been started");
			form.afterShellfireServiceEnvironmentEnsured();
		}
	}

	@Override
	public void uninstall(String path) {
		log.debug("uninstall()");
		log.debug("Uninstall all WireGuard services");
		uninstallWireGuardTunnelServices();
		try {
			log.debug("Uninstalling Shellfire VPN Service");
			String jarFile = Util.getPathJar();
			String instDir = new File(jarFile).getParent() + File.separator;

			String template = Util.fileToString(instDir + "UninstallServiceTemplate.txt");
			String procRunPath = instDir + getProcrunExe();

			template = template.replace("$$PROCRUNPATH$$", procRunPath);

			template = template.replace("$$INSTALLDIR$$", instDir);

			
			String uninstallBat = instDir + "UninstallService.bat";
			Util.stringToFile(template, uninstallBat);

			String command = String.format("%s /C \"%s\"", Util.getCmdExe(), uninstallBat);
			log.debug("Running command {}", command);
			Process p = Runtime.getRuntime().exec(command, null, new File(instDir));
			Util.digestProcess(p);
			p.waitFor();
			log.debug("service uninstalled (or not?); - exiting");

		} catch (IOException e) {
			Util.handleException(e);
		} catch (InterruptedException e) {
			Util.handleException(e);
		}
	}

	private void uninstallWireGuardTunnelServices() {
		List<String> serviceNameList = getWireGuardTunnelServiceList();
		
		for (String serviceName : serviceNameList) {
			WindowsVpnController.stopWireGuardService(serviceName);
			deleteService(serviceName);
		}
	}


	private void deleteService(String serviceName) {
		log.debug("deleteService({}) - start", serviceName);
		
		String[] cmdStopService = {
				WindowsVpnController.PATH_SC_EXE,
				"delete",
				serviceName
		};
		
		Util.runCommandAndReturnOutput(cmdStopService);
		
		log.debug("deleteService({}) - start", serviceName);
	}

	private List<String> getWireGuardTunnelServiceList() {
		log.debug("getWireGuardTunnelServiceList() - start");
		
		log.debug("Setting startup type to manual");
		String[] cmdGetAllServices = {
				WindowsVpnController.PATH_SC_EXE,
				"query",
				"state=",
				"all"
		};
		String resultGetAllServices = Util.runCommandAndReturnOutput(cmdGetAllServices);

		List<String> result = new LinkedList<String>();
		
		String[] lines = resultGetAllServices.split("\n");
		for (String line : lines) {
			if (line != null && line.contains("SERVICE_NAME: WireGuardTunnel$wg-sf")) {
				String service = line.trim();
				service = service.split(" ")[1];
				result.add(service);
			}
		}
		
		log.debug("getWireGuardTunnelServiceList() - return: {}", result);
		return result;
	}

	/**
	 * Assumes elevation
	 */
	@Override
	public void install(String path) {
		log.debug("install()");

		try {
			String jarFile = Util.getPathJar();
			String instDir = new File(jarFile).getParent() + File.separator;

			String template = Util.fileToString(instDir + "InstallServiceTemplate.txt");
			String procRunPath = instDir + getProcrunExe();

			template = template.replace("$$TEMP$$", Util.getTempDir());
			template = template.replace("$$LOGFILE$$", Util.getTempDir() + File.separator + "ProcRunLog.log");
			template = template.replace("$$JVM_DLL$$", Util.getJvmDll());
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

	@Override
	public void installElevated() {
		log.debug("installElevated() - start");

		if (Util.isVistaOrLater()) {
			// restart elevated

			String pathJavaw = Util.getJavaHome() + "\\bin\\javaw.exe";
			String jarFile = Util.getPathJar();
			log.debug(jarFile);
			File instDir = new File(jarFile).getParentFile();
			String arg = "installservice";

			// Check for execution from dev environment, will fail anyway
			if (jarFile == null) {
				log.warn("Path to Jar not found - elevated Relaunch only supported from deployed jar!");
			} else {
				String elevateVbs = System.getProperty("java.io.tmpdir") + "/elevate.vbs";

				String exec = pathJavaw;
				String cmds = "--add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/sun.nio.ch=ALL-UNNAMED --module-path=.\\lib\\javafx\\lib --add-modules=javafx.swing,javafx.graphics,javafx.fxml,javafx.media,javafx.web --add-reads=javafx.graphics=ALL-UNNAMED --add-opens=javafx.controls/com.sun.javafx.charts=ALL-UNNAMED --add-opens=javafx.graphics/com.sun.javafx.iio=ALL-UNNAMED --add-opens=javafx.graphics/com.sun.javafx.iio.common=ALL-UNNAMED --add-opens=javafx.graphics/com.sun.javafx.css=ALL-UNNAMED --add-opens=javafx.base/com.sun.javafx.runtime=ALL-UNNAMED --add-exports=java.base/jdk.internal.util=chronicle.bytes --add-exports=java.base/jdk.internal.ref=ALL-UNNAMED --add-exports=java.base/sun.nio.ch=ALL-UNNAMED --add-exports=jdk.unsupported/sun.misc=ALL-UNNAMED --add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.io=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED -jar \"\"" + jarFile + "\"\" " + arg;

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

	private boolean hasFinished(Process p) {
		try {
			p.exitValue();
			return true;
		} catch (IllegalThreadStateException e) {
			return false;
		}
	}

	public void writeElevationVbsFile(String elevationVbsFile, String exe, String cmds) {
		log.debug("creating elevationVbsFile at {}", elevationVbsFile);
		File file = new File(elevationVbsFile);
		file.delete();
		// file.deleteOnExit();
		try {
			try (FileWriter fw = new FileWriter(file, true)) {
				fw.write(String.format("Set objShell = CreateObject(\"Shell.Application\")\r\n" + "exec = \"%s\"\r\n" + "cmds = \"%s\"\r\n"
						+ "objShell.ShellExecute exec, cmds, \"\", \"runas\"", exe, cmds));

			}
		} catch (IOException e) {
			log.error("Erorr occured during elevate.bat creation", e);
		}

	}

}
