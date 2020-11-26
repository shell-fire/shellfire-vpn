package de.shellfire.vpn.gui.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;

import de.shellfire.vpn.Storage;
import de.shellfire.vpn.Util;
import de.shellfire.vpn.VpnProperties;
import de.shellfire.vpn.client.Client;
import de.shellfire.vpn.client.Controller;
import de.shellfire.vpn.gui.CanContinueAfterBackEndAvailableFX;
import de.shellfire.vpn.gui.LoginForms;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.service.CryptFactory;
import de.shellfire.vpn.types.Reason;
import de.shellfire.vpn.types.ServerType;
import de.shellfire.vpn.webservice.EndpointManager;
import de.shellfire.vpn.webservice.Response;
import de.shellfire.vpn.webservice.WebService;
import de.shellfire.vpn.webservice.model.LoginResponse;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventDispatcher;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

public class LoginController extends AnchorPane implements Initializable, CanContinueAfterBackEndAvailableFX {

	@FXML
	private Button fButtonLogin;
	@FXML
	private Button fButtonLostUserCredential;
	@FXML
	private Label fLabelUsername;
	@FXML
	private Label fLabelPassword;
	@FXML
	private TextField fUsername;
	@FXML
	private CheckBox fStoreLoginData;
	@FXML
	private CheckBox fAutoLogin;
	@FXML
	private CheckBox fAutoStart;
	@FXML
	private CheckBox fAutoconnect;
	@FXML
	private Button fButtonOpenRegistrationForm;
	@FXML
	private PasswordField fPassword;
	@FXML
	private Pane headerPanel;
	@FXML
	private ImageView headerImageView;
	@FXML
	private Pane exitLogoPane;
	@FXML
	private ImageView exitImageView;
	@FXML
	private Label loginLabel;

	private static final long serialVersionUID = 1L;
	public static final String REG_PASS = "pass";
	public static final String REG_USER = "user";
	public static final String REG_AUTOlogIN = "autologin";
	public static final String REG_AUTOCONNECT = "autoConnect";
	public static final String REG_INSTDIR = "instdir";
	public static final String REG_SHOWSTATUSURL = "show_status_url_on_connect";
	private static final String REG_FIRST_START = "firststart";
	WebService service = null;
	private boolean minimize;
	public static LoginForms application;
	private static I18n i18n = VpnI18N.getI18n();
	private static Logger log = Util.getLogger(LoginController.class.getCanonicalName());
	private String username;
	private String password;
	private ProgressDialogController loginProgressDialog;
	private static boolean passwordBogus;
	public static ProgressDialogController initProgressDialog;
	public static ShellfireVPNMainFormFxmlController mainForm;

	public LoginController() {
	}

	// Event Listener on Button[#fButtonLogin].onAction
	@FXML
	public void handlefButtonLogin(ActionEvent event) {

		this.fButtonLogin.setDisable(true);
		log.debug("Login attempt with valid user input");

		try {
			Platform.runLater(() -> {
				try {
					//loginProgressDialog = ProgressDialogController.getInstance(i18n.tr("Logging in..."), null, LoginForms.getStage(), true);
					loginProgressDialog = ProgressDialogController.getInstance(i18n.tr("Logging in..."), null, null, true);
				} catch (IOException e) {
					log.error("Error in ProgressDialogController.getInstancet()", e);
				}
				loginProgressDialog.show();
			});
			LoginTask loginTask = new LoginTask();
			Thread loginTaskThread = new Thread(loginTask);
			loginTaskThread.start();

			loginTask.setOnSucceeded((WorkerStateEvent wEvent) -> {
				log.info("Login task completed successfully");

				Response<LoginResponse> loginResult = null;
				try {
					loginResult = loginTask.getValue();

					if (loginResult == null) {
						log.error("LoginController: Login result is null");
						Alert alert = new Alert(AlertType.ERROR);
						alert.setHeaderText(i18n.tr("Error"));
						alert.setContentText(i18n.tr("Login error: unknown error"));
						alert.showAndWait();
						this.application.getStage().show();
					} else if (!service.isLoggedIn()) {
						log.error("LoginController: Not logged in!");
						log.debug("LoginController: Login result is " + loginResult.getMessage());
						Alert alert = new Alert(AlertType.ERROR);
						alert.setHeaderText(i18n.tr("Error"));
						alert.setContentText(i18n.tr("Login error: wrong username/password"));
						alert.showAndWait();
						this.application.getStage().show();

					} else {
						loginProgressDialog.setDialogText(i18n.tr("Loading..."));
						MainFormLoaderTask loaderTask = new MainFormLoaderTask(loginResult);
						
						// TODO: in loaderTask.call(), separate background processing / gui loading etc. from 
						// needed gui manipulating, which needs to be in the onSucceed method...
						loaderTask.call();
					}

				} catch (Exception e) {
					log.error("Error while checking User registration", e);
				}

			});
			this.fButtonLogin.setDisable(false);
		} catch (Exception ex) {
			log.error("could not load progressDialog fxml in login window \n" + ex.getMessage());
		}

	}

	// Event Listener on Button[#fButtonLostUserCredential].onAction
	@FXML
	public void handlefButtonLostUserCredential(ActionEvent event) {
		Util.openUrl(service.getUrlPasswordLost());
	}

	// Event Listener on CheckBox[#fStoreLoginData].onAction
	@FXML
	public void handlefStoreLoginData(ActionEvent event) {
		if (!this.fStoreLoginData.isSelected()) {
			this.fAutoLogin.setSelected(false);
		}
	}

	// Event Listener on CheckBox[#fAutoLogin].onAction
	@FXML
	public void handlefAutoLogin(ActionEvent event) {
		if (this.fAutoLogin.isSelected()) {
			this.fStoreLoginData.setSelected(true);
		}
	}

	// Event Listener on CheckBox[#fAutoStart].onAction
	@FXML
	public void handlefAutoStart(ActionEvent event) {
		// empty
	}

	// Event Listener on CheckBox[#fAutoconnect].onAction
	@FXML
	public void handlefAutoconnect(ActionEvent event) {
		// empty
	}

	// Event Listener on Button[#fButtonOpenRegistrationForm].onAction
	@FXML
	public void handlefButtonOpenRegistrationForm(ActionEvent event) {

		requestRegistration();
	}

	public void requestRegistration() {
		this.application.loadRegisterFormController();
		this.application.getStage().show();
	}

	// Event Listener on ImageView[#exitImageView].onContextMenuRequested
	@FXML
	private void handleEXitButtonClicked(MouseEvent event) {
		Platform.exit();
		System.exit(0);
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		initComponents();
		this.service = WebService.getInstance();
		this.fButtonLostUserCredential.setOnAction((ActionEvent event) -> {
			Util.openUrl(service.getUrlPasswordLost());
		});
		this.restoreCredentialsFromRegistry();
		this.restoreAutoConnectFromRegistry();
		this.restoreAutoStartFromRegistry();
	}

	public void initComponents() {
		this.headerImageView.setImage(Util.getImageIconFX("/icons/sf_en.png"));
		this.exitImageView.setImage(Util.getImageIconFX("/icons/exit.png"));
		this.fLabelUsername.setText(i18n.tr("Email / Username:"));

		this.fLabelPassword.setText(i18n.tr("Password:"));

		this.fAutoLogin.setText(i18n.tr("Login automatically"));

		this.fButtonOpenRegistrationForm.setText(i18n.tr("No user credentials?"));

		this.fButtonLostUserCredential.setText(i18n.tr("User credentials lost?"));

		this.fButtonLogin.setText(i18n.tr("Login"));
		this.loginLabel.setText(i18n.tr("Login"));
		this.fAutoStart.setText(i18n.tr("Start on boot"));
		this.fAutoconnect.setText(i18n.tr("Connect automatically"));

		this.fStoreLoginData.setText(i18n.tr("Save login data"));

		this.headerImageView.setImage(ShellfireVPNMainFormFxmlController.getLogo());

		this.headerPanel.setStyle("-fx-background-color: rgb(18,172,229);");

		this.exitImageView.setImage(Util.getImageIconFX("/icons/exit.png"));

		this.fButtonLogin.managedProperty().bind(this.fButtonLogin.visibleProperty());

		// Listeners for changes in password field
		fPassword.focusedProperty()
				.addListener((ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) -> {
					// password field in focus
					if (newPropertyValue) {
						if (this.passwordBogus) {
							fPassword.setText("");
						}
					} else {
						// password field out of focus
						this.password = this.fPassword.getText();
						passwordBogus = false;
					}
				});

		// Listeners for changes in username field
		fUsername.focusedProperty()
				.addListener((ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) -> {
					// password field in focus
					this.username = this.fUsername.getText();
				});

		fButtonLogin.setOnMouseClicked(e -> {
			fUsername.requestFocus();
		});
		mySetIconImage("/icons/sfvpn2-idle-big.png");
	}

	public void setApp(LoginForms applic) {
		log.debug("LoginController: Application set up appropriately");
		this.application = applic;
	}

	public void mySetIconImage(String imagePath) {
		log.debug("mySetIconImage: the icon Image  path is " + imagePath);
		Platform.runLater(() -> {
			this.application.getStage().getIcons().clear();
			this.application.getStage().getIcons().add(new Image(imagePath));
		});
	}

	public boolean isMinimize() {
		return minimize;
	}

	public void setMinimize(boolean minimize) {
		this.minimize = minimize;
	}

	@FXML
	private void handleUsernameChanged(InputMethodEvent event) {
		this.username = fUsername.getText();
	}

	@FXML
	private void handleUsernameContextRequested(ContextMenuEvent event) {
	}

	@FXML
	private void handlePasswordFieldChanged(InputMethodEvent event) {
		// this.
	}

	@FXML
	private void handleExitImageMouseExited(MouseEvent event) {
		this.application.getStage().getScene().setCursor(Cursor.DEFAULT);

	}

	@FXML
	private void handleExitImageMouseEntered(MouseEvent event) {
		LoginForms.getStage().getScene().setCursor(Cursor.HAND);
	}

	@Override
	public void continueAfterBackEndAvailabledFX() {
		log.debug("continueAfterBackEndAvailabledFX: being enabled");
		Storage.register(service);
		this.restoreCredentialsFromRegistry();
		this.restoreAutoConnectFromRegistry();
		this.restoreAutoStartFromRegistry();
		LoginController.application.setLicenseAccepted(false);

		if (!LoginForms.getStage().isShowing()) {
			log.debug("Initial progress dialog is hidden");
			LoginForms.getStage().show();
		}

		if (!this.autoLoginIfActive()) {
			LoginForms.getStage().show();
			askForNewAccountAndAutoStartIfFirstStart();
		}
	}

	@Override
	public ProgressDialogController getProgressDialogFX() {
		return LoginForms.initDialog;
	}

	@FXML
	private void handlefButtonLoginClicked(MouseEvent event) {
		Platform.runLater(() -> {
			this.fButtonLogin.setDisable(true);
			fUsername.requestFocus();
		});
		this.fButtonLogin.managedProperty().set(false);
	}

	@FXML
	private void handlePasswordFieldKeyPressed(KeyEvent event) {
		if (event.getCode().equals(KeyCode.ENTER)) {

			this.password = this.fPassword.getText();
			this.passwordBogus = false;
			handlefButtonLogin(null);
		}

	}

	void setAutoLogin(boolean autologin) {
		this.fAutoLogin.setSelected(autologin);
	}

	class LoginTask extends Task<Response<LoginResponse>> {

		@Override
		protected Response<LoginResponse> call() throws Exception {
			Response<LoginResponse> loginResult = null;
			log.debug("Starting login background task");
			String user = getUser();
			String password = getPassword();
			log.debug("service.login() - start()");
			loginResult = service.login(user, password);
			log.debug("service.login() - finished()");
			fButtonLogin.setDisable(false);
			return loginResult;
		}

	}

	class MainFormLoaderTask extends Task<Boolean> {

		private Response<LoginResponse> loginResult;

		public MainFormLoaderTask(Response<LoginResponse> loginResult) {
			this.loginResult = loginResult;
		}

		@Override
		protected Boolean call() throws Exception {
			log.debug("Starting LoaderTask");
			log.debug("LoginController: handlefLogginButton - service is loggedIn " + loginResult.getMessage());
			if (fStoreLoginData.isSelected()) {
				storeCredentialsInVpnProperties(username, password);
				log.debug("LoginController: Login Data stored, username is " + username + " and passwd is " + password);
			} else {
				removeCredentialsFromRegistry();
			}
			if (fAutoStart.isSelected()) {
				Client.addVpnToAutoStart();
				log.debug("LoginController: Autostart Data stored");
			} else {
				Client.removeVpnFromAutoStart();
			}
			if (fAutoconnect.isSelected()) {
				setAutoConnectInRegistry(true);
				log.debug("LoginController: Autoconnect Data stored");
			} else {
				setAutoConnectInRegistry(false);
			}

			// prepare the other necessary controllers
			int rememberedVpnSelection = application.rememberedVpnSelection();
			
			boolean selectionRequired = service.vpnSelectionRequired();
			log.debug("LoginController: loginTask - selectedRequired is " + selectionRequired);
			application.getStage().hide();
			if (selectionRequired && rememberedVpnSelection == 0) {
				log.debug("Selection is required and no vpn remembered (yet) - showing dialog");
				application.loadVPNSelect();
				application.vpnSelectController.setService(service);
				application.vpnSelectController.setAutoConnect(fAutoconnect.isSelected());
				application.vpnSelectController.setApp(application);
				application.getStage().show();

			} else {
				if (selectionRequired && rememberedVpnSelection != 0) {
					log.debug("selection required and vpn already remembered - selecting remembered vpn automatically");
					if (!service.selectVpn(rememberedVpnSelection)) {
						log.debug("not possible to select remembered vpn");
						application.vpnSelectController.setApp(application);
						application.getStage().show();
					}
				}
				if (!application.getStage().isShowing()) {
					log.debug("handlefButtonLogin: vpnController not visible - not needed, continue with main controller");
					application.loadShellFireMainController();
					application.shellFireMainController.setShellfireService(service);
					boolean vis = true;
					if (minimize && service.getVpn().getAccountType() != ServerType.Free) {
						vis = false;
					}

					application.shellFireMainController.initializeComponents();
					application.shellFireMainController.setServiceAndInitialize(service);
					application.shellFireMainController.prepareSubviewControllers();
					application.shellFireMainController.setApp(application);
					application.shellFireMainController.afterLogin(fAutoconnect.isSelected());
				} else {
					log.debug("handlefButtonLogin: vpnController is visible");
				}
			}

			return true;
		}

	}

	public void hideLoginProgress() {
		this.setDisable(true);
	}

	public String getUser() {
		return this.fUsername.getText();
	}

	public String getPassword() {
		return this.fPassword.getText();
	}

	public boolean validate() {

		if ((fUsername.getText().trim().length() > 0) && (fPassword.getText().trim().length() > 0)) {
			return true;
		}
		return false;
	}

	public void afterShellfireServiceEnvironmentEnsured() {
		log.debug("Ensured that ShellfireVPNService is running. Trying to connect to the Shellfire webservice backend...");

		EndpointManager.getInstance().ensureShellfireBackendAvailableFx(this);
	}

	private void restoreCredentialsFromRegistry() {
		VpnProperties props = VpnProperties.getInstance();
		String user = props.getProperty(REG_USER, null);
		String pass = props.getProperty(REG_PASS, null);

		if (user != null && pass != null) {
			user = CryptFactory.decrypt(user);
			pass = CryptFactory.decrypt(pass);

			if (user != null && pass != null) { // decryption worked
				this.setUsername(user);
				this.setPassword(pass);
				this.fStoreLoginData.setSelected(true);
			} else {
				this.removeCredentialsFromRegistry();
			}

		}
	}

	private void restoreAutoStartFromRegistry() {
		boolean autoStart = Client.vpnAutoStartEnabled();
		this.fAutoStart.setSelected(autoStart);
	}

	private void restoreAutoConnectFromRegistry() {
		VpnProperties props = VpnProperties.getInstance();
		boolean autoConnect = props.getBoolean(REG_AUTOCONNECT, false);
		this.fAutoconnect.setSelected(autoConnect);

	}

	private boolean autoLoginIfActive() {
		VpnProperties props = VpnProperties.getInstance();
		boolean doAutoLogin = props.getBoolean(REG_AUTOlogIN, false);

		if (doAutoLogin) {
			this.fAutoLogin.setSelected(true);
			this.application.getStage().hide();
			handlefButtonLogin(null);
		}

		return doAutoLogin;

	}

	protected void setUsername(String username) {
		this.username = username;
		this.fUsername.setText(username);
	}

	protected void setPassword(String password) {
		this.password = password;
		this.fPassword.setText(this.password);
	}

	void setPasswordBogus() {
		this.fPassword.setText("boguspass");
		this.passwordBogus = true;
	}

	private void removeCredentialsFromRegistry() {
		VpnProperties props = VpnProperties.getInstance();
		props.remove(REG_USER);
		props.remove(REG_PASS);
		props.remove(REG_AUTOlogIN);
	}

	private void askForNewAccountAndAutoStartIfFirstStart() {
		if (firstStart()) {
			if (!Util.isWindows()) {
				askForLicense();

				if (!this.application.getLicenseAccepted()) {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setContentText(i18n.tr("Licence not accepted - Shellfire VPN is now exiting."));
					alert.showAndWait();
					Platform.exit();
					System.exit(0);
				}
			}
			askForAutoStart();
			askForNewAccount();
		}

		setFirstStart(false);
	}

	private boolean firstStart() {
		VpnProperties props = VpnProperties.getInstance();
		boolean firstStart = props.getBoolean(LoginController.REG_FIRST_START, true);
		String autoLogin = props.getProperty(LoginController.REG_AUTOlogIN, null);

		return firstStart && autoLogin == null;
	}

	public void askForLicense() {
		this.application.loadLicenceAcceptanceScreenController();
	}

	private void askForAutoStart() {
		Alert alert = new Alert(AlertType.CONFIRMATION, i18n.tr("Start Shellfire VPN on boot and connect automatically?"), ButtonType.YES,
				ButtonType.NO);
		alert.setTitle(i18n.tr("Startup"));

		Optional<ButtonType> result = alert.showAndWait();

		if ((result.isPresent()) && (result.get() == ButtonType.YES)) {

			Client.addVpnToAutoStart();
			fAutoStart.setSelected(true);
			setAutoLogin(true);
			fAutoconnect.setSelected(true);
			fStoreLoginData.setSelected(true);
		}
	}

	private void askForNewAccount() {
		Alert alert = new Alert(AlertType.CONFIRMATION,
				i18n.tr("This is the first time you start Shellfire VPN. Create a new Shellfire VPN account?"), ButtonType.YES,
				ButtonType.NO);
		alert.setTitle(i18n.tr("Welcome: First Start"));
		Optional<ButtonType> result = alert.showAndWait();

		if ((result.isPresent()) && (result.get() == ButtonType.YES)) {
			requestRegistration();
		}
	}

	private void setFirstStart(boolean b) {
		VpnProperties props = VpnProperties.getInstance();
		props.setBoolean(LoginController.REG_FIRST_START, b);
	}

	private void storeCredentialsInVpnProperties(String user, String password) {
		VpnProperties props = VpnProperties.getInstance();
		props.setProperty(REG_USER, CryptFactory.encrypt(user));
		props.setProperty(REG_PASS, CryptFactory.encrypt(password));
		props.setBoolean(REG_AUTOlogIN, fAutoLogin.isSelected());

	}

	private void setAutoConnectInRegistry(boolean autoConnect) {
		VpnProperties props = VpnProperties.getInstance();
		props.setBoolean(REG_AUTOCONNECT, autoConnect);

	}

	public static void restart() {
		if (Util.isWindows()) {

			if (LoginForms.instance != null) {

				if (LoginForms.shellFireMainController != null) {

					Controller c = LoginForms.shellFireMainController.getController();
					if (c != null) {
						c.disconnect(Reason.GuiRestarting);

					}
					LoginForms.shellFireMainController = null;
				}

				// TODO - investigage if commenting causes memory leaks
				// LoginForms.instance.close();
				LoginForms.instance = null;

				List<String> restart = new ArrayList<String>();
				restart.add("ShellfireVPN2.exe");
				Process p;
				try {
					p = new ProcessBuilder(restart).directory(new File(getInstDir())).start();
					Util.digestProcess(p);

					System.exit(0);
				} catch (IOException e) {
					Util.handleException(e);
				}

			}
		} else {
			List<String> restart = new ArrayList<String>();
			restart.add("/usr/bin/open");
			restart.add("-n");
			restart.add(com.apple.eio.FileManager.getPathToApplicationBundle());
			Process p;
			try {
				p = new ProcessBuilder(restart).directory(new File(com.apple.eio.FileManager.getPathToApplicationBundle())).start();
				Util.digestProcess(p);

				Platform.exit();
				System.exit(0);
			} catch (IOException e) {
				Util.handleException(e);
			}
		}
	}

	public static String getInstDir() {
		VpnProperties props = VpnProperties.getInstance();
		String instDir = props.getProperty(REG_INSTDIR, null);

		if (instDir == null) {
			instDir = new File("").getAbsolutePath();
		}

		return instDir;
	}

}