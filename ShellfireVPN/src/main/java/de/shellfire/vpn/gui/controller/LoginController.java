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
import de.shellfire.vpn.gui.ServerImageBackgroundManager;
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
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

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
	private Button fButtonOpenRegistrationForm;
	@FXML
	private PasswordField fPassword;
	@FXML
	private Label loginLabel;

	private static final long serialVersionUID = 1L;
	WebService service = null;
	private boolean minimize;
	public static LoginForms application;
	private static I18n i18n = VpnI18N.getI18n();
	private static Logger log = Util.getLogger(LoginController.class.getCanonicalName());
	private String username;
	private String password;
	ProgressDialogController loginProgressDialog;
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
						loginProgressDialog.hide();
						this.application.getStage().show();
					} else if (!service.isLoggedIn()) {
						log.error("LoginController: Not logged in!");
						log.debug("LoginController: Login result is " + loginResult.getMessage());
						Alert alert = new Alert(AlertType.ERROR);
						alert.setHeaderText(i18n.tr("Error"));
						alert.setContentText(i18n.tr("Login error: wrong username/password"));
						alert.showAndWait();
						loginProgressDialog.hide();
						this.application.getStage().show();

					} else {
						loginProgressDialog.setDialogText(i18n.tr("Loading..."));
						MainFormLoaderTask loaderTask = new MainFormLoaderTask(loginResult);
						loaderTask.setOnSucceeded((WorkerStateEvent wEvent2) -> {
							application.loadShellFireMainController();
							
							application.shellfireVpnMainController.setShellfireService(service);
							boolean vis = true;
							if (minimize && service.getVpn().getAccountType() != ServerType.Free) {
								vis = false;
							}

							application.shellfireVpnMainController.initializeComponents();

							try {
								application.shellfireVpnMainController.setServiceAndInitialize(service);
							} catch (IOException e) {
								log.error("Error during setServiceAndInitialize", e);
							}
							
							application.shellfireVpnMainController.prepareSubviewControllers();
							
							application.shellfireVpnMainController.initConnection();
		
							
							application.shellfireVpnMainController.setApp(application);
							application.shellfireVpnMainController.afterLogin();
							
							
							application.shellfireVpnMainController.setUserName(this.username);

						});
						
						log.debug("before loaderTaskThread");
						
						Thread loaderTaskThread = new Thread(loaderTask);
						log.debug("before loaderTaskThread.setDaemon(true)");
						loaderTaskThread.setDaemon(true);
						log.debug("before loaderTaskThread.start()");
						loaderTaskThread.start();
						log.debug("after loaderTaskThread.start()");
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
	}

	public void initComponents() {
		this.fLabelUsername.setText(i18n.tr("Email / Username:"));

		this.fLabelPassword.setText(i18n.tr("Password:"));

		this.fButtonOpenRegistrationForm.setText(i18n.tr("No user credentials?"));

		this.fButtonLostUserCredential.setText(i18n.tr("User credentials lost?"));

		this.fButtonLogin.setText(i18n.tr("Login"));
		this.loginLabel.setText(i18n.tr("Login"));

		this.fStoreLoginData.setText(i18n.tr("Save login data"));

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

	}

	public void setIconImageIdle() {
		Util.mySetIconImage(this.application, new String[] {
				"/icons/sfvpn2-idle-256x256.png",
				"/icons/sfvpn2-idle-128x128.png",
				"/icons/sfvpn2-idle-64x64.png",
				"/icons/sfvpn2-idle-40x40.png",
				"/icons/sfvpn2-idle-32x32.png",
				"/icons/sfvpn2-idle-24x24.png",
				"/icons/sfvpn2-idle-16x16.png",
		});
	}

	public void setApp(LoginForms applic) {
		log.debug("LoginController: Application set up appropriately");
		this.application = applic;
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

	class MainFormLoaderTask extends Task<Void> {

		private Response<LoginResponse> loginResult;

		public MainFormLoaderTask(Response<LoginResponse> loginResult) {
			this.loginResult = loginResult;
		}

		@Override
		protected Void call() throws Exception {
			log.debug("Starting LoaderTask");
			log.debug("LoginController: handlefLogginButton - service is loggedIn " + loginResult.getMessage());
			if (fStoreLoginData.isSelected()) {
				storeCredentialsInVpnProperties(username, password);
				log.debug("LoginController: Login Data stored, username is " + username + " and passwd has a lenght > 3 characters? " + ((password != null && password.length() > 3) ? "yes" : "no"));
			} else {
				removeCredentialsFromRegistry();
			}
			
			ServerImageBackgroundManager.init();
			
			return null;
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
		String user = props.getProperty(Util.REG_USER, null);
		String pass = props.getProperty(Util.REG_PASS, null);

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

	private boolean autoLoginIfActive() {
		VpnProperties props = VpnProperties.getInstance();
		boolean doAutoLogin = props.getBoolean(Util.REG_AUTOlOGIN, false);

		if (doAutoLogin) {
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
		props.remove(Util.REG_USER);
		props.remove(Util.REG_PASS);
		props.remove(Util.REG_AUTOlOGIN);
	}

	private void askForNewAccountAndAutoStartIfFirstStart() {
		if (firstStart()) {

			askForAutoStart();
			askForNewAccount();
		}

		setFirstStart(false);
	}

	private boolean firstStart() {
		VpnProperties props = VpnProperties.getInstance();
		boolean firstStart = props.getBoolean(Util.REG_FIRST_START, true);
		String autoLogin = props.getProperty(Util.REG_AUTOlOGIN, null);

		return firstStart && autoLogin == null;
	}

	private void askForAutoStart() {
		Alert alert = new Alert(AlertType.CONFIRMATION, i18n.tr("Start Shellfire VPN on boot and connect automatically?"), ButtonType.YES,
				ButtonType.NO);
		alert.setTitle(i18n.tr("Startup"));

		Optional<ButtonType> result = alert.showAndWait();

		if ((result.isPresent()) && (result.get() == ButtonType.YES)) {
			Client.addVpnToAutoStart();
			fStoreLoginData.setSelected(true);
			
			VpnProperties props = VpnProperties.getInstance();
			props.setBoolean(Util.REG_AUTOCONNECT, true);
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
		props.setBoolean(Util.REG_FIRST_START, b);
	}

	private void storeCredentialsInVpnProperties(String user, String password) {
		VpnProperties props = VpnProperties.getInstance();
		props.setProperty(Util.REG_USER, CryptFactory.encrypt(user));
		props.setProperty(Util.REG_PASS, CryptFactory.encrypt(password));
		props.setBoolean(Util.REG_AUTOlOGIN, true);

	}

	private void setAutoConnectInRegistry(boolean autoConnect) {
		VpnProperties props = VpnProperties.getInstance();
		props.setBoolean(Util.REG_AUTOCONNECT, autoConnect);

	}

	public static void restart() {
		if (Util.isWindows()) {

			if (LoginForms.controllerInstance != null) {

				if (LoginForms.shellfireVpnMainController != null) {

					Controller c = LoginForms.shellfireVpnMainController.getController();
					if (c != null) {
						c.disconnect(Reason.GuiRestarting);

					}
					LoginForms.shellfireVpnMainController = null;
				}

				LoginForms.controllerInstance = null;

				List<String> restart = new ArrayList<String>();
				restart.add("ShellfireVPN2.exe");
				Process p;
				try {
					p = new ProcessBuilder(restart).directory(new File(Util.getInstDir())).start();
					Util.digestProcess(p);

					System.exit(0);
				} catch (IOException e) {
					Util.handleException(e);
				}

			}
		} 
	}

}