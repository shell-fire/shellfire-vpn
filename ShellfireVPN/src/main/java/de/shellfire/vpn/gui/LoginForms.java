package de.shellfire.vpn.gui;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.VpnProperties;
import de.shellfire.vpn.client.ServiceToolsFX;
import de.shellfire.vpn.gui.controller.LicenseAcceptanceController;
import de.shellfire.vpn.gui.controller.LoginController;
import de.shellfire.vpn.gui.controller.ProgressDialogController;
import de.shellfire.vpn.gui.controller.RegisterFormController;
import de.shellfire.vpn.gui.controller.ShellfireVPNMainFormFxmlController;
import de.shellfire.vpn.gui.controller.VpnSelectDialogController;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.proxy.ProxyConfig;
import de.shellfire.vpn.updater.UpdaterFX;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class LoginForms extends Application {

	private static FXMLLoader loader = new FXMLLoader();
	// private static FXMLLoader loader = new FXMLLoader(LoginForms.class.getResource("/fxml/login.fxml"));
	private static final Logger log = Util.getLogger(LoginForms.class.getCanonicalName());
	public static Stage stage;
	public static String[] default_args;
	public static ProgressDialogController initDialog;
	public RegisterFormController registerController;
	public LicenseAcceptanceController licenceAcceptanceController;
	public static VpnSelectDialogController vpnSelectController;
	public static ShellfireVPNMainFormFxmlController shellfireVpnMainController = null;
	public static LoginController instance;
	private static final I18n i18n = VpnI18N.getI18n();
	private boolean licenseAccepted;
	// Variables to control draggin of window
	private static double xOffset = 0;
	private static double yOffset = 0;
	public static final String REG_INSTDIR = "instdir";
	public static final String REG_REMEMBERSELECTION = "SelectedVpnId";

	public static Stage getStage() {
		return stage;
	}

	public static void setStage(Stage stage) {
		LoginForms.stage = stage;
	}

	public static void main(String[] args) {
		System.setProperty("java.library.path", "./lib");
		default_args = args;

		preventDuplicateStart();
		
		launch(args);
	}

	private static void preventDuplicateStart() {
		String userHome = System.getProperty("user.home");
		File file = new File(userHome, "my.lock");
		try {
			FileChannel fc = FileChannel.open(file.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
			FileLock lock = fc.tryLock();
			if (lock == null) {

				Platform.runLater(() -> {
					Alert alert = new Alert(Alert.AlertType.ERROR, i18n.tr("Shellfire VPN is already running."), ButtonType.OK);
					alert.setHeaderText(i18n.tr("Already running"));
					alert.showAndWait();
				});

				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
				}
				System.exit(0);
			}

		} catch (IOException e) {
			throw new Error(e);
		}
	}

	public static void initializations(String args[]) {
		System.setProperty("java.library.path", "./lib");
		final boolean minimize;
		if (args.length > 0) {
			String cmd = args[0];

			minimize = cmd.equals("minimize");
		} else {
			minimize = false;
		}
		ProxyConfig.perform();
		
		
	}

	@Override
	public void start(Stage primaryStage) {

		try {
			LoginForms.stage = primaryStage;
			LoginForms.stage.initStyle(StageStyle.UNDECORATED);
			initDialog = ProgressDialogController.getInstance("Init ...", null, stage, false);

			this.loadLoginController();
		} catch (Exception ex) {
			log.error("could not start with first stage load \n");
			ex.printStackTrace(System.out);
		}
		Platform.setImplicitExit(false);
		try {
			initializations(default_args);
			stage.sizeToScene();
			stage.getProperties().put("hostServices", this.getHostServices());
			afterDialogDisplay();
		} catch (Exception ex) {
			log.error("could not latter message after login in start \n" + ex.getMessage());
		}
		
		/*Platform.runLater(() -> {
			loadShellFireMainController(true);
		});
*/

	}

	public void loadLoginController() {
		try {
			this.instance = (LoginController) replaceSceneContent("login.fxml");
			this.instance.setApp(this);
			this.stage.setTitle("Shellfire VPN");
			log.debug("LoginForms: Login controller loaded");

		} catch (Exception ex) {
			log.error("could not load loginController fxml\n" + ex.getMessage());
		}

	}

	public void loadRegisterFormController() {
		log.debug("In the RegisterForm controller");
		try {
			this.registerController = (RegisterFormController) replaceSceneContent("RegisterFormFxml.fxml");
			this.registerController.setApp(this);
		} catch (Exception ex) {
			log.error("could not load RegisterForm fxml\n" + ex.getMessage());
		}

	}

	public void loadVPNSelect() {
		log.debug("loadVPNSelect() - start");
		try {
			this.vpnSelectController = (VpnSelectDialogController) replaceSceneContent("VpnSelectDialogFxml.fxml");
			this.vpnSelectController.setApp(this);
		} catch (Exception ex) {
			log.error("could not load vpnSelect fxml\n" + ex.getMessage());
		}

	}

	public int rememberedVpnSelection() {
		VpnProperties props = VpnProperties.getInstance();
		int remembered = props.getInt(REG_REMEMBERSELECTION, 0);

		return remembered;
	}
	
	public void loadShellFireMainController() {
		loadShellFireMainController(false);
	}
		
	public void loadShellFireMainController(boolean loadOnly) {
		log.debug("loadShellFireMainController - start()");
		try {
			if (shellfireVpnMainController == null) {
				this.shellfireVpnMainController = (ShellfireVPNMainFormFxmlController) replaceSceneContent("ShellfireVPNMainFormFxml.fxml");	
			}
			
			if (!loadOnly) {
				this.shellfireVpnMainController.setApp(this);
			}

		} catch (Exception ex) {
			log.error("could not load main form fxml\n" + ex.getMessage());
			ex.printStackTrace(System.out);
		}
		
		log.debug("loadShellFireMainController - end()");
	}

	public void loadLicenceAcceptanceScreenController() {
		log.debug("In the licence Acceptance Screen controller");
		try {
			this.licenceAcceptanceController = (LicenseAcceptanceController) replaceSceneContent("LicenseAcceptScreen.fxml");
			this.licenceAcceptanceController.setApp(this);
		} catch (Exception ex) {
			log.error("could not load RegisterForm fxml\n" + ex.getMessage());
		}

	}

	public static Initializable replaceSceneContent(String fxml) throws Exception {
		log.debug("replaceSceneContent fxml=" + fxml + " - start");
		loader = new FXMLLoader(LoginForms.class.getClassLoader().getResource("/fxml/" + fxml));
		loader.setLocation(LoginForms.class.getResource("/fxml/" + fxml));
		
		log.debug("Location of loader is " + loader.getLocation());
		AnchorPane page = null;
		try {
			log.debug("ReplaceSceneContent trying to load anchor pane for " + fxml);
			page = (AnchorPane) loader.load();
			log.debug("AnchorPane loaded");
		} catch (Exception ex) {
			log.error("Loading fxml has error for replaceSceneContent for " + fxml, ex);
		}
		log.debug("replaceSceneContent() - start setOnMouse...");
		page.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				xOffset = event.getSceneX();
				yOffset = event.getSceneY();
			}
		});
		page.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				stage.setX(event.getScreenX() - xOffset);
				stage.setY(event.getScreenY() - yOffset);
			}
		});
		log.debug("replaceSceneContent() - finished setOnMouse...");
		if (page.getScene() == null) {
			Scene scene = new Scene(page);
			log.debug("replaceSceneContent() - stage.setScene() - start...");
			stage.setScene(scene);
			log.debug("Scene of " + fxml + " has been newly created");
		} else {
			log.debug("Scene of " + fxml + " is that of anchorpane");
			stage.setScene(page.getScene());
		}
		log.debug("replaceSceneContent() - stage.centerOnScreen() - start...");
		stage.centerOnScreen();
		log.debug("replaceSceneContent() - stage.sizeToScene() - start...");
		stage.sizeToScene();
		
		log.debug("replaceSceneContent fxml=" + fxml + " - returning");
		return (Initializable) loader.getController();
	}

	public void afterDialogDisplay() {
		if (default_args.length > 0) {
			handleCommandLine();
			return;
		}
		log.debug("Hiding stage");
		this.stage.hide();
		
		// test Internet connection
		boolean internetAvailable = Util.internetIsAvailable();
		if (internetAvailable) {
			UpdaterFX updater = new UpdaterFX();
			if (updater.newVersionAvailable()) {
				enforceMandatoryUpdateOrExit();
				return;
			} else {
				log.debug("LoginForms: No update available");
			}
		} else {
			log.debug("No internet available, skipping update check");
		}
		log.debug("giving control to login");

		instance.setApp(this);
		log.debug("Preparing to display login menu");
		LoginForms.initConnectionTest();
	}

	private void enforceMandatoryUpdateOrExit() {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
				i18n.tr("A new version of Shellfire VPN is available. An update is mandatory. Would you like to update now?"),
				ButtonType.YES, ButtonType.NO);
		alert.setHeaderText(i18n.tr("New Version"));

		alert.showAndWait();
		Optional<ButtonType> result = alert.showAndWait();
		if ((result.isPresent()) && (result.get() == ButtonType.YES)) {
			Alert ialert = new Alert(Alert.AlertType.INFORMATION);
			ialert.setHeaderText(i18n.tr("Update is being performed"));
			ialert.setContentText(i18n.tr(
					"You decided, to update. Shellfire VPN is now being restarted with super user privileges to perform the update."));
			String installerPath = com.apple.eio.FileManager.getPathToApplicationBundle()
					+ "/Contents/Java/ShellfireVPN2-Updater.app";
			log.debug("Opening updater using Desktop.open(): " + installerPath);
			List<String> cmds = new LinkedList<String>();
			cmds.add("/usr/bin/open");
			cmds.add(installerPath);
			Process p;
			try {
				p = new ProcessBuilder(cmds)
						.directory(new File(com.apple.eio.FileManager.getPathToApplicationBundle() + "/Contents/Java/")).start();
				Util.digestProcess(p);
			} catch (IOException e) {
				Util.handleException(e);
			}
			Platform.exit();
			System.exit(0);
		} else {
			Alert falert = new Alert(Alert.AlertType.ERROR);
			falert.setHeaderText(i18n.tr("Update rejected"));
			falert.setContentText(i18n.tr("You decided not to update - Shellfire VPN is now exiting."));
			falert.showAndWait();
			Platform.exit();
			System.exit(0);
		}
	}

	private void handleCommandLine() {
		String cmd = default_args[0];

		if (cmd.equals("uninstallservice")) {
			ServiceToolsFX.getInstanceForOS().uninstall();
			this.stage.hide();
			return;
		} else if (cmd.equals("installservice")) {
			this.stage.hide();
			String path = "";

			if (default_args.length > 1) {
				for (int i = 1; i < default_args.length; i++) {
					path += default_args[i];

					if (i + 1 < default_args.length) {
						path += " ";
					}
				}
			}

			log.debug("Retrieved installation path from args parameter: " + path);

			if (cmd.equals("installservice")) {
				ServiceToolsFX.getInstanceForOS().install(path);
			}

			System.exit(0);
			return;
		} else if (cmd.equals("doupdate")) {
			String path = "";
			String user = "";
			if (default_args.length > 2) {
				user = default_args[1];

				for (int i = 2; i < default_args.length; i++) {
					path += default_args[i];

					if (i + 1 < default_args.length) {
						path += " ";
					}
				}
			}

			log.debug("Retrieved installation path from args parameter: " + path);
			new UpdaterFX().performUpdate(path, user);
			return;
		}
	}

	public void licenseAccepted() {
		this.licenseAccepted = true;
	}

	public void licenseNotAccepted() {
		this.licenseAccepted = false;
	}

	public boolean getLicenseAccepted() {
		return this.licenseAccepted;
	}

	public void setLicenseAccepted(boolean newLicence) {
		this.licenseAccepted = newLicence;
	}

	private static void initConnectionTest() {
		// before doing anything else, we should test for an internet connection. without internet, we cant do anything!
		log.debug("In initConnection Test method");
		boolean internetAvailable = Util.internetIsAvailable();

		if (internetAvailable) {
			log.debug("Before the service Environment Ensure");
			initDialog.setDialogText(i18n.tr("Initializing ShellfireVPNService..."));
			ServiceToolsFX.getInstanceForOS().ensureServiceEnvironmentFX(instance);
			log.debug("After the service Environment Ensure");
		} else {
			log.debug("Connection not available");
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setHeaderText(i18n.tr("No internet"));
			alert.setContentText(i18n.tr("No internet connection available - ShellfireVPN is being closed."));
			alert.showAndWait();
			Platform.exit();
			System.exit(0);
		}
	}
}