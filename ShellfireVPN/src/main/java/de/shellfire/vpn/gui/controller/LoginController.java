package de.shellfire.vpn.gui.controller;

import de.shellfire.vpn.Storage;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;

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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.event.ActionEvent;

import javafx.scene.control.Label;

import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.control.PasswordField;

import javafx.scene.control.CheckBox;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class LoginController extends AnchorPane implements Initializable, CanContinueAfterBackEndAvailableFX {

    @FXML
    private Button fButtonLogin;
    @FXML
    private Button fButtonLostUserCredential;
    @FXML
    private Label label;
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

    private static final long serialVersionUID = 1L;
    public static final String REG_PASS = "pass";
    public static final String REG_USER = "user";
    public static final String REG_AUTOLOGIN = "autologin";
    public static final String REG_AUTOCONNECT = "autoConnect";
    public static final String REG_INSTDIR = "instdir";
    public static final String REG_SHOWSTATUSURL = "show_status_url_on_connect";
    private static final String REG_FIRST_START = "firststart";
    WebService service;
    private boolean minimize;
    private static LoginForms application;
    private static I18n i18n = VpnI18N.getI18n();
    private static Logger log = Util.getLogger(LoginForms.class.getCanonicalName());
    private String username;
    private String password;
    private static boolean passwordBogus;
    public static ProgressDialogController initProgressDialog;
    public static ShellfireVPNMainFormFxmlController mainForm;

    public LoginController() {
    }

    // Event Listener on Button[#fButtonLogin].onAction
    @FXML
    public void handlefButtonLogin(ActionEvent event) {
        //fButtonLogin.setDisable(true);
        log.debug("Login attempt made");
        if (validate()) {
            log.debug("Login attempt with valid user input");
            try {
                LoginTAsk task = new LoginTAsk();
                task.run();
                task.setOnSucceeded((WorkerStateEvent wEvent) -> {
                    log.info("Login task completed successfully");
                    Response<LoginResponse> loginResult = null;
                    try {
                        loginResult = task.getValue();
                    } catch (Exception e) {
                        log.debug("Error while checking User registration " + e.getMessage());
                    }
                    if (loginResult != null) {
                        if (service.isLoggedIn()) {

                            if (fStoreLoginData.isSelected()) {
                                storeCredentialsInRegistry(username, password);
                            } else {
                                removeCredentialsFromRegistry();
                            }
                            if (fAutoStart.isSelected()) {
                                Client.addVpnToAutoStart();
                            } else {
                                Client.removeVpnFromAutoStart();
                            }
                            if (fAutoconnect.isSelected()) {
                                setAutoConnectInRegistry(true);
                            } else {
                                setAutoConnectInRegistry(false);
                            }

                            // We initialise the vpn selection form but we do not display it yet.
                            this.application.loadVPNSelect();
                            this.application.vpnSelectController.setService(this.service);
                            this.application.vpnSelectController.setAutoConnect(fAutoconnect.isSelected());
                            int rememberedVpnSelection = this.application.vpnSelectController.rememberedVpnSelection();
                            /* FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/VpnSelectDialog.fxml"));
                        log.debug("Resource is found in: " +loader.getLocation());
                        //ShellfireVPNMainFormFxmlController mainFormController = new ShellfireVPNMainFormFxmlController();
                        //loader.setController(mainFormController);
                       
                        Parent mainFormParent;
                            try {
                                mainFormParent = (Parent)loader.load();
                                Scene mainFormScene = new Scene(mainFormParent);
                                 VpnSelectDialogController vpnController = loader.<VpnSelectDialogController>getController();
                        vpnController.setService(this.service);
                         vpnController.setAutoConnect(fAutoconnect.isSelected());
                        vpnController.echoMessage("VPN Controller instace successfully created");
                        Stage mainFormStage = (Stage)((Node)event.getSource()).getScene().getWindow();

                        mainFormStage.setScene(mainFormScene);
                        mainFormStage.show();
                            } catch (IOException ex) {
                                log.debug("LoginController Message: " + ex.getMessage());
                            }
                        
                           int rememberedVpnSelection = 0;*/
                            boolean selectionRequired = service.vpnSelectionRequired();

                            if (selectionRequired && rememberedVpnSelection == 0) {

                                setVisible(false);
                                //dia.setVisible(true);
                                // display the vpn selection window if there are no prefered stored vpns.
                                this.application.vpnSelectController.setApp(application);
                                log.debug("Condition for electionRequired && rememberedVpnSelection == 0");
                                this.application.getStage().show();

                            } else {
                                //try {
                                if (selectionRequired
                                        && rememberedVpnSelection != 0) {
                                    if (!service.selectVpn(rememberedVpnSelection)) {
                                        // remembered vpn id is invalid
                                        //dispose();
                                        //dia.setVisible(true);
                                        this.application.vpnSelectController.setApp(application);
                                        log.debug("condition for !service.selectVpn(rememberedVpnSelection");
                                        this.application.getStage().show();
                                    }
                                }

                                if (!this.application.vpnSelectController.isVisible()) {
                                    //setVisible(false);
                                    //dispose();
                                    //mainForm = new ShellfireVPNMainFormFxmlController(service);
                                    //TODO, uncomment load comment below
                                    //this.application.loadShellFireMainController();
                                    this.application.shellFireMainController.setSerciceAndInitialize(service);
                                    boolean vis = true;
                                    if (minimize
                                            && service.getVpn().getAccountType() != ServerType.Free) {
                                        vis = false;
                                    }

                                    mainForm.setVisible(vis);
                                    mainForm.afterLogin(fAutoconnect.isSelected());
                                }
                            }
                            /*catch (VpnException ex) {
                                    Util.handleException(ex);
                                }*/

                            //}

                        } else {
                            Alert alert = new Alert(AlertType.ERROR);
                            alert.setHeaderText(i18n.tr("Error"));
                            alert.setContentText(i18n.tr("Login error:") + loginResult.getMessage());
                            alert.showAndWait();
                        }
                    }
                });
            } catch (Exception ex) {
                log.debug("could not load progressDialog fxml in login window \n" + ex.getMessage());
            }

        } else {
            fUsername.requestFocus();
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
        // TODO Autogenerated
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

    /*   fPassword.focusedProperty().addListener(new ChangeListener<Boolean>()
=======
import javafx.scene.effect.BlendMode;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class LoginController extends AnchorPane implements Initializable {
	@FXML
	private Button fButtonLogin;
	@FXML
	private Button fButtonLostUserCredential;
	@FXML
	private Label label;
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
        
        WebService service;
	private boolean minimize;
	private LoginForms application;
	private static I18n i18n = VpnI18N.getI18n();
	private static Logger log = Util.getLogger(LoginForms.class.getCanonicalName());
        private String username ; 
        private String password; 
        private static boolean passwordBogus;

    public LoginController() {
    }
        
        
	// Event Listener on Button[#fButtonLogin].onAction
	@FXML
	public void handlefButtonLogin(ActionEvent event) {
		fButtonLogin.setDisable(true);
		try {
			ProgressDialogController pgressDialog = (ProgressDialogController) application
					.replaceSceneContent("ProgressDiagogV.fxml");
			pgressDialog.setDialogText(i18n.tr("Einloggen..."));
			this.application.getStage();

		} catch (Exception ex) {
			log.debug("could not load progressDialog fxml in login window \n" + ex.getMessage());
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
		// TODO Autogenerated
	}

	// Event Listener on CheckBox[#fAutoLogin].onAction
	@FXML
	public void handlefAutoLogin(ActionEvent event) {
		if (this.fAutoLogin.isSelected())
                    this.fStoreLoginData.setSelected(true);
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
            
            this.application.getRegisterFormController();
            this.application.getStage().show();
		//fButtonOpenRegistrationForm.setVisible(false);
                // TODO Autogenerated
                //RegisterForm regForm = new RegisterForm();
	}
        
        
     /*   fPassword.focusedProperty().addListener(new ChangeListener<Boolean>()
>>>>>>> 32656c998715dfdf2cb3c2b13af96c74a646dc3b
{
    @Override
    public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
    {
        if (newPropertyValue)
        {
            System.out.println("Textfield on focus");
        }
        else
        {
            System.out.println("Textfield out focus");
        }
    }
});*/
    // Event Listener on ImageView[#exitImageView].onContextMenuRequested
    @FXML
    private void handleEXitButtonClicked(MouseEvent event) {
        Platform.exit();
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        initComponents();
        this.service = WebService.getInstance();
        fButtonLostUserCredential.setOnAction((ActionEvent event) -> {
            Util.openUrl(service.getUrlPasswordLost());
        });
    }

    public void initComponents() {
        this.fLabelUsername.setText(i18n.tr("Email / Username:"));
        this.fLabelUsername.setFont(Font.font("Arial", Util.getFontSize()));

        this.fLabelPassword.setText(i18n.tr("Password:"));
        this.fLabelPassword.setFont(Font.font("Arial", Util.getFontSize()));

        this.fAutoLogin.setText(i18n.tr("Login automatically"));

        this.fButtonOpenRegistrationForm.setText(i18n.tr("No user credentials?"));

        this.fButtonLostUserCredential.setText(i18n.tr("User credentials lost?"));

        this.fButtonLogin.setText(i18n.tr("Login"));

        this.fAutoStart.setText(i18n.tr("Start on boot"));

        this.fAutoconnect.setText(i18n.tr("Connect  automatically"));

        this.fStoreLoginData.setText(i18n.tr("Save login data"));

        this.headerImageView.setImage(ShellfireVPNMainFormFxmlController.getLogo());

        this.headerPanel.setStyle("-fx-background-color: rgb(18,172,229);");

        this.exitImageView.setImage(Util.getImageIconFX("src/main/resources/icons/exit.png"));

        // Listeners for changes in password field
        fPassword.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) {
                // password field in focus
                if (newPropertyValue) {
                    if (passwordBogus) {
                        fPassword.setText("");
                    }
                } else {
                    // password field out of focus
                    password = fPassword.getText();
                    passwordBogus = false;
                }
            }
        });
    }

    public void setApp(LoginForms applic) {
        this.application = applic;
    }

    public boolean isMinimize() {
        return minimize;
    }

    public void setMinimize(boolean minimize) {
        this.minimize = minimize;
    }

    public void showLoginProgress() {
        // TODO implement loginprogrss
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
        //this.
    }

    @FXML
    private void handlePasswordFieldPressed(KeyEvent event) {
        if (event.getCode().equals(KeyCode.ENTER)) {

            this.password = fPassword.getText();
            this.passwordBogus = false;
            // perform login action when inputs are correct
            handlefButtonLogin(null);
        }

    }

    @FXML
    private void handleExitImageMouseExited(MouseEvent event) {
        //this.exitImageView.setBlendMode(BlendMode.LIGHTEN);
        this.application.getStage().getScene().setCursor(Cursor.DEFAULT);

    }

    @FXML
    private void handleExitImageMouseEntered(MouseEvent event) {
        //this.exitImageView.setBlendMode(BlendMode.OVERLAY);
        this.application.getStage().getScene().setCursor(Cursor.HAND);
    }

    @Override
    public void continueAfterBackEndAvailabledFX() {
        this.service = WebService.getInstance();
        Storage.register(service);
        this.restoreCredentialsFromRegistry();
        this.restoreAutoConnectFromRegistry();
        this.restoreAutoStartFromRegistry();
        this.application.setLicenseAccepted(false);

        if (null != initProgressDialog) {
            //initProgressDialog.h();
            // TODO check if logic intention was properly converted from swing counterpart.
            this.application.loadLoginController();
        }
        try {
            //Connection.initRmi();
        } catch (Exception e) {
            Util.handleException(e);
        }

        if (!this.autoLoginIfActive()) {
            this.setVisible(true);
            askForNewAccountAndAutoStartIfFirstStart();
        }
    }

    @Override
    public ProgressDialogController getDialogFX() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    class LoginTAsk extends Task<Response<LoginResponse>> {

        Response<LoginResponse> loginResult = null;

        @Override
        protected Response<LoginResponse> call() throws Exception {
            log.debug("Starting login background task");
            String user = getUser();
            String password = getPassword();
            log.debug("service.login() - start()");
            loginResult = service.login(user, password);
            log.debug("service.login() - finished()");

            return loginResult;
        }

        private void setAutoConnectInRegistry(boolean autoConnect) {
            VpnProperties props = VpnProperties.getInstance();
            props.setBoolean(REG_AUTOCONNECT, autoConnect);
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
            // TODO implement any further validation required.
            return true;
        }
        return false;
    }

    public void afterShellfireServiceEnvironmentEnsured() {
        log.debug("Ensured that ShellfireVPNService is running. Trying to connect to the Shellfire webservice backend...");

        EndpointManager.getInstance().ensureShellfireBackendAvailableFx(this);
    }

    public void continueAfterBackEndAvailabled() {
        this.service = WebService.getInstance();
        Storage.register(service);
        this.restoreCredentialsFromRegistry();
        this.restoreAutoConnectFromRegistry();
        this.restoreAutoStartFromRegistry();
        this.application.setLicenseAccepted(false);

        /* if (initProgressDialog != null) {
      initProgressDialog.dispose();
      instance.setEnabled(true);
    }*/
        // TODO ensure that the login menu is currently displayed.
        try {
            //Connection.initRmi();
        } catch (Exception e) {
            Util.handleException(e);
        }

        if (!this.autoLoginIfActive()) {
            this.setVisible(true);
            askForNewAccountAndAutoStartIfFirstStart();
        }
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
        boolean doAutoLogin = props.getBoolean(REG_AUTOLOGIN, false);

        if (doAutoLogin) {
            this.fAutoLogin.setSelected(true);
            this.setVisible(false);
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
        this.setPasswordBogus();
    }

    void setPasswordBogus() {
        this.fPassword.setText("boguspass");
        this.passwordBogus = true;
    }

    private void removeCredentialsFromRegistry() {
        VpnProperties props = VpnProperties.getInstance();
        props.remove(REG_USER);
        props.remove(REG_PASS);
        props.remove(REG_AUTOLOGIN);
    }

    private void askForNewAccountAndAutoStartIfFirstStart() {
        if (firstStart()) {
            if (!Util.isWindows()) {
                askForLicense();

                if (!this.application.getLicenseAccepted()) {
                    Alert alert = new Alert(AlertType.ERROR);
                    //alert.setTitle("Error");
                    //alert.setHeaderText("Printer error");
                    alert.setContentText(i18n.tr("Licence not accepted - Shellfire VPN is now exiting."));
                    alert.showAndWait();
                    Platform.exit();
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
        String autoLogin = props.getProperty(LoginController.REG_AUTOLOGIN, null);

        return firstStart && autoLogin == null;
    }

    public void askForLicense() {
        this.application.loadLicenceAcceptanceScreenController();
    }

    private void askForAutoStart() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(i18n.tr("Startup"));
//String s = i18n.tr("Start Shellfire VPN on boot and connect automatically?");
        alert.setContentText(i18n.tr("Start Shellfire VPN on boot and connect automatically?"));

        Optional<ButtonType> result = alert.showAndWait();

        if ((result.isPresent()) && (result.get() == ButtonType.OK)) {

            Client.addVpnToAutoStart();
            fAutoStart.setSelected(true);
            fAutoLogin.setSelected(true);
            fAutoconnect.setSelected(true);
            fStoreLoginData.setSelected(true);
        }
    }

    private void askForNewAccount() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(i18n.tr("Welcome: First Start"));
        alert.setContentText(i18n.tr("This is the first time you start ShellfireV PN. Create a new Shellfire VPN account?"));

        Optional<ButtonType> result = alert.showAndWait();

        if ((result.isPresent()) && (result.get() == ButtonType.OK)) {
            requestRegistration();
        }
    }

    private void setFirstStart(boolean b) {
        VpnProperties props = VpnProperties.getInstance();
        props.setBoolean(LoginController.REG_FIRST_START, b);
    }

    private void storeCredentialsInRegistry(String user, String password) {
        VpnProperties props = VpnProperties.getInstance();
        props.setProperty(REG_USER, CryptFactory.encrypt(user));
        props.setProperty(REG_PASS, CryptFactory.encrypt(password));
        props.setBoolean(REG_AUTOLOGIN, fAutoLogin.isSelected());

    }

    private void setAutoConnectInRegistry(boolean autoConnect) {
        VpnProperties props = VpnProperties.getInstance();
        props.setBoolean(REG_AUTOCONNECT, autoConnect);

    }

    public void restart() {
        if (Util.isWindows()) {

            if (LoginForms.instance != null) {

                if (LoginForms.shellFireMainController != null) {
                    
                    Controller c = this.application.shellFireMainController.getController();
                    if (c != null) {
                        c.disconnect(Reason.GuiRestarting);

                    }

                    //this.application.shellFireMainController.dispose();
                    this.application.shellFireMainController = null;
                }
                
                //TODO - investigage if commenting causes memory leaks
                //LoginForms.instance.close();
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
            } catch (IOException e) {
                Util.handleException(e);
            }

        }

    }

    public static String getInstDir() {
        VpnProperties props = VpnProperties.getInstance();
        String instDir = props.getProperty(REG_INSTDIR, null);

        if (instDir == null) {
            if (Util.isWindows()) {
                instDir = new File("").getAbsolutePath();
            } else {
                instDir = WebService.macOsAppDirectory() + "/ShellfireVPN";
            }
        }

        return instDir;
    }
}

//	class LoginTAsk extends Task<Response<LoginResponse>>{
//		
//		public void done(){
//			Response<LoginResponse> loginResult = null;
//			try {
//				loginResult = get();
//			} catch (Exception ignore) {
//				ignore.printStackTrace();
//			}
//			hideLoginProgress();
//			String user = getUser();
//			String password = getPassword();
//		}
//		@Override
//		protected Response<LoginResponse> call() throws Exception {
//			// TODO Auto-generated method stub
//			return null;
//		}
//		
//	}
//	
//	public void hideLoginProgress(){
//		this.setDisable(true);
//	}
//	
//	public String getUser(){
//		return this.fUsername.getText() ;
//	}
//	
//	public String getPassword(){
//		return this.fPassword.getText();
//	}
//}
