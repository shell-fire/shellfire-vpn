package de.shellfire.vpn.gui;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.VpnProperties;
import de.shellfire.vpn.client.ServiceToolsFX;
import de.shellfire.vpn.gui.controller.LoginController;
import de.shellfire.vpn.gui.controller.ProgressDialogController;
import de.shellfire.vpn.gui.controller.RegisterFormController;
import de.shellfire.vpn.gui.controller.ShellfireVPNMainFormFxmlController;
import de.shellfire.vpn.gui.controller.VpnSelectDialogController;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.proxy.ProxyConfig;
import de.shellfire.vpn.updater.UpdaterFX;
import de.shellfire.vpn.webservice.WebService;
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
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
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
	public static VpnSelectDialogController vpnSelectController;
	public static ShellfireVPNMainFormFxmlController shellfireVpnMainController = null;
	public static LoginController instance;
	private static final I18n i18n = VpnI18N.getI18n();
	private boolean licenseAccepted;
	private boolean startMinimized = false;
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
		if (args.length == 0 || !args[0].equals("installservice")) {
			log.debug("preventing duplicate start");
			preventDuplicateStart();
		} else {
			log.debug("not preventing duplicate start, because called with parameter installservice");
		}
		
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
		log.debug("LoginForms.start() - start");
		try {
			LoginForms.stage = primaryStage;
			LoginForms.stage.initStyle(StageStyle.DECORATED);
			initDialog = ProgressDialogController.getInstance("Init ...", null, stage, false);

			this.loadLoginController();
		} catch (Exception ex) {
			log.error("could not start with first stage load \n");
			ex.printStackTrace(System.out);
		}
		Platform.setImplicitExit(true);
		try {
			log.debug("LoginForms.start() - calling initializations");
			initializations(default_args);
			log.debug("LoginForms.start() - sizeToScene()");
			stage.sizeToScene();
			log.debug("LoginForms.start() - put(hostServices)");
			stage.getProperties().put("hostServices", this.getHostServices());
			log.debug("LoginForms.start() - afterDialogDisplay");
			afterDialogDisplay();
		} catch (Exception ex) {
			log.error("could not load message after login in start \n" + ex.getMessage());
		}
		
		log.debug("LoginForms.start() - return");
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

	public void loadVPNSelect(WebService service, boolean fAutoconnect) {
		log.debug("loadVPNSelect() - start");
		try {
			this.vpnSelectController = (VpnSelectDialogController) replaceSceneContent("VpnSelectDialogFxml.fxml");
			this.vpnSelectController.setApp(this);
			this.vpnSelectController.setService(service);
			this.vpnSelectController.setAutoConnect(fAutoconnect);
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
			// TODO: find a way to differentiate between loading the fxml and its controller and showing/hiding it...
			if (shellfireVpnMainController == null) {
				this.shellfireVpnMainController = (ShellfireVPNMainFormFxmlController) replaceSceneContent("ShellfireVPNMainFormFxml.fxml");
			}
			
			if (!loadOnly) {
				this.shellfireVpnMainController.setApp(this);
			}
			
			if (this.startMinimized) {
				log.debug("this.startMinimized is true, calling shellfireVpnMainController.minimizeToTray();");
				this.shellfireVpnMainController.minimizeToTray();
			} else {
				log.debug("this.startMinimized is false, NOT calling shellfireVpnMainController.minimizeToTray();");
			}

		} catch (Exception ex) {
			log.error("could not load main form fxml\n" + ex.getMessage());
			ex.printStackTrace(System.out);
		}
		
		log.debug("loadShellFireMainController - end()");
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
			page.setBackground(Background.EMPTY);
			log.debug("AnchorPane loaded");
		} catch (Exception ex) {
			log.error("Loading fxml has error for replaceSceneContent for " + fxml, ex);
		}
		if (page.getScene() == null) {
			Scene scene = new Scene(page);
			log.debug("replaceSceneContent() - stage.setScene() - start...");
			scene.setFill(Color.TRANSPARENT);

			stage.setScene(scene);

			log.debug("Scene of " + fxml + " has been newly created");
		} else {
			log.debug("Scene of " + fxml + " is that of anchorpane");
			page.getScene().setFill(Color.TRANSPARENT);

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
		log.debug("LoginForms.afterDialogDisplay() - start");
		if (default_args.length > 0) {
			log.debug("LoginForms.afterDialogDisplay() - has command line, call handleCommandLine()");
			handleCommandLine();
			log.debug("LoginForms.afterDialogDisplay() - returned from handleCommanLine, returning;");
			if (!startMinimized) {
				return;
			} else {
				log.debug("afterDialogDisplay - we need to start minimized, not returning");
			}
		}
		log.debug("Hiding stage");
		this.stage.hide();

		log.debug("giving control to login");

		instance.setApp(this);
		log.debug("Preparing to display login menu");
		LoginForms.initConnectionTest();
		
		log.debug("LoginForms.afterDialogDisplay() - return");
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
		} else if (cmd.equals("minimize")) {
			log.debug("handleCommandLine detected that mainForm should be started minimized");
			this.startMinimized  = true;
		} else {
			log.debug("handleCommandLine called not not supported command: {}", cmd);
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