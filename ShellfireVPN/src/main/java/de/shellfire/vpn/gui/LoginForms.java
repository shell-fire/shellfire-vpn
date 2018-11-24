package de.shellfire.vpn.gui;

import de.shellfire.vpn.Util;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;


import de.shellfire.vpn.client.ServiceTools;
import de.shellfire.vpn.gui.controller.LicenseAcceptanceController;
import de.shellfire.vpn.gui.controller.LoginController;
import de.shellfire.vpn.gui.controller.ProgressDialogController;
import de.shellfire.vpn.gui.controller.RegisterFormController;
import de.shellfire.vpn.gui.controller.ShellfireVPNMainFormFxmlController;
import de.shellfire.vpn.gui.controller.VpnSelectDialogController;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.proxy.ProxyConfig;
import de.shellfire.vpn.updater.Updater;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class LoginForms extends Application {

    private static final Logger LOG = Util.getLogger(LoginForms.class.getCanonicalName());
    public static Stage stage;
    public static String[] default_args;
    public static ProgressDialogController initDialog;
    public RegisterFormController registerController;
    public LicenseAcceptanceController licenceAcceptanceController;
    public static VpnSelectDialogController vpnSelectController;
    public static ShellfireVPNMainFormFxmlController shellFireMainController;
    private boolean minimize;
    public static  LoginController instance;
    private static final I18n I18N = VpnI18N.getI18n();
    //private AnchorPane page;
    private boolean licenseAccepted;
    // Variables to control draggin of window
    private double xOffset = 0;
    private double yOffset = 0;

    public static Stage getStage() {
        return stage;
    }

    public static void setStage(Stage stage) {
        LoginForms.stage = stage;
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            this.stage = primaryStage;
            // remove the standard menu buttons on display
            this.stage.initStyle(StageStyle.UNDECORATED);
            initializations(default_args);
            loadProgressDialog();
            stage.sizeToScene();
            stage.getProperties().put("hostServices", this.getHostServices());
            this.stage.show();

        } catch (Exception ex) {
            LOG.debug("could not start with first stage load " + ex);
        }

        try {
            afterDialogDisplay();
        } catch (Exception ex) {
            LOG.debug("could not latter message after login in start \n" + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        ///TODO: login automatically chexbox makes login to bypass login form

        System.setProperty("java.library.path", "./lib");
        LOG.debug("In the main method");
        default_args = args;
        initializations(args);
        launch(args);
    }

    public static void initializations(String args[]) {
        final boolean minimize;
        if (args.length > 0) {
            String cmd = args[0];

            minimize = cmd.equals("minimize");
        } else {
            minimize = false;
        }
        ProxyConfig proxy = new ProxyConfig();
        proxy.perform();
        //setLookAndFeel();
    }


    public void loadProgressDialog(String message) {
        try {
            this.initDialog = (ProgressDialogController) replaceSceneContent("ProgressDialog.fxml");
            //Platform.runLater(() -> progressDialog.setVisible(true));
            this.initDialog.setDialogText(message);
            this.initDialog.setApp(this);

        } catch (Exception ex) {
            LOG.debug("could not load progressDialog fxml \n" + ex.getMessage());
        }
    }

    // Overloading of Method loadProgressDialog
    public void loadProgressDialog() {
        try {
            this.initDialog = (ProgressDialogController) replaceSceneContent("ProgressDialog.fxml");
            //Platform.runLater(() -> progressDialog.setVisible(true));
            this.initDialog.setApp(this);

        } catch (Exception ex) {
            LOG.debug("could not load progressDialog fxml \n" + ex.getMessage());
        }
    }

    public void loadLoginController() {
        System.out.println("In the getLogin controller");
        try {
            this.instance = (LoginController) replaceSceneContent("login.fxml");
            this.instance.setApp(this);
            this.stage.setTitle("Shellfire VPN 2 Login");
            LOG.debug("LoginForms: Login controller loaded");

        } catch (Exception ex) {
            LOG.debug("could not load loginController fxml\n" + ex.getMessage());
        }

    }

    public void loadRegisterFormController() {
        LOG.debug("In the RegisterForm controller");
        try {
            this.registerController = (RegisterFormController) replaceSceneContent("RegisterFormFxml"
                    + ".fxml");
            //Platform.runLater(() -> progressDialog.setVisible(true));
            this.registerController.setApp(this);
            //this.stage.setTitle("Shellfire VPN 2 Login");

        } catch (Exception ex) {
            LOG.debug("could not load RegisterForm fxml\n" + ex.getMessage());
        }

    }

    public void loadVPNSelect() {
        LOG.debug("In the VPN controller");
        try {
            this.vpnSelectController = (VpnSelectDialogController) replaceSceneContent("VpnSelectDialogFxml.fxml");
            //this.vpnSelectController.setApp(this);

        } catch (Exception ex) {
            LOG.debug("could not load vpnSelect fxml\n" + ex.getMessage());
        }

    }

    public void loadShellFireMainController() {
        LOG.debug("In the ShellFire Main controller");
        try {
            this.shellFireMainController = (ShellfireVPNMainFormFxmlController) replaceSceneContent("ShellfireVPNMainFormFxml.fxml");
            this.shellFireMainController.displayMessage("Object created");
            this.shellFireMainController.setApp(this);

        } catch (Exception ex) {
            LOG.debug("could not load main form fxml\n" + ex.getMessage());
        }
        LOG.debug("ShellfireMainFrom initializtion method completed");
    }

    public void loadLicenceAcceptanceScreenController() {
        LOG.debug("In the licence Acceptance Screen controller");
        try {
            this.licenceAcceptanceController = (LicenseAcceptanceController) replaceSceneContent("LicenseAcceptScreen.fxml");
            //Platform.runLater(() -> progressDialog.setVisible(true));
            this.licenceAcceptanceController.setApp(this);
            //this.stage.setTitle("Shellfire VPN 2 Login");

        } catch (Exception ex) {
            LOG.debug("could not load RegisterForm fxml\n" + ex.getMessage());
        }

    }

    public Initializable replaceSceneContent(String fxml) throws Exception {
        FXMLLoader loader = new FXMLLoader(LoginForms.class.getClassLoader().getResource(fxml));
        loader.setLocation(LoginForms.class.getResource("/fxml/" + fxml));
        System.out.println("Loacation of loader is " + loader.getLocation());
        AnchorPane page = null;
        try {
            System.out.println("ReplaceSceneContent trying to load anchor pane for " + fxml);
            page = (AnchorPane) loader.load();
            System.out.println("Location of Controller is " + loader.getController());

        } catch (Exception ex) {
            LOG.debug(" Loading fxml has error for replaceSceneContent " + ex.getMessage());
        }
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
        if (page.getScene() == null) {
            Scene scene = new Scene(page);
            stage.setScene(scene);
            LOG.debug("Scene of " + fxml + " has been newly created");
        } else {
            LOG.debug("Scene of " + fxml + " is that of anchorpane");
            stage.setScene(page.getScene());
        }
        stage.centerOnScreen();
        stage.sizeToScene();
        return (Initializable) loader.getController();
    }

    public Initializable replaceSceneContentWithSameRoot(String fxml) throws Exception {
        FXMLLoader loader = new FXMLLoader(LoginForms.class.getClassLoader().getResource(fxml));
        // InputStream in = LoginForms.class.getResourceAsStream(fxml);
        //loader.setBuilderFactory(new JavaFXBuilderFactory());
        loader.setLocation(LoginForms.class.getResource("/fxml/" + fxml));
        System.out.println("replaceSCenContentWithSameRoot Loacation of loader is " + loader.getLocation());
        AnchorPane page = null;
        try {
            System.out.println("trying to load anchor pane for " + fxml);
            //Parent root = (Parent) loader.load();
            loader.setController(shellFireMainController);
            loader.setRoot(loader);
            System.out.println("Location of Controller is " + loader.getController());

        } catch (Exception ex) {
            LOG.debug(" Loading fxml has error for replaceSceneContentWithSameRoot " + ex.getMessage());
        }
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
        return (Initializable) loader.getController();
    }

    public void afterDialogDisplay() {
        if (default_args.length > 0) {

            String cmd = default_args[0];

            if (cmd.equals("uninstallservice")) {
                ServiceTools.getInstanceForOS().uninstall();
                //initDialog.dispose();
                this.stage.hide();
                return;
            } else if (cmd.equals("installservice")) {
                //initDialog.dispose();
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

                LOG.debug("Retrieved installation path from args parameter: " + path);

                if (cmd.equals("installservice")) {
                    ServiceTools.getInstanceForOS().install(path);
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

                LOG.debug("Retrieved installation path from args parameter: " + path);
                //initDialog.dispose();
                //this.stage.hide();
                new Updater().performUpdate(path, user);

                return;
            }
        }
        // hidding stage
        LOG.debug("Hiding stage");
        //this.stage.hide();
        LOG.debug("after dialog box , before login controller");
        //this.loadLoginController();

        // test Internet connection 
        boolean internetAvailable = Util.internetIsAvailable();
        LOG.debug("after getting the login controller ");
        if (internetAvailable) {
            Updater updater = new Updater();
            if (updater.newVersionAvailable()) {

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setHeaderText(I18N.tr("New Version"));
                alert.setContentText(I18N.tr("A new version of Shellfire VPN is available. An update is mandatory. Would you like to update now?"));

                alert.showAndWait();
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    Alert ialert = new Alert(Alert.AlertType.INFORMATION);
                    ialert.setHeaderText(I18N.tr("Update is being performed"));
                    ialert.setContentText(I18N.tr("You decided, to update. Shellfire VPN is now being restarted with super user privileges to perform the update."));
                    String installerPath = com.apple.eio.FileManager.getPathToApplicationBundle() + "/Contents/Java/ShellfireVPN2-Updater.app";
                    LOG.debug("Opening updater using Desktop.open(): " + installerPath);
                    List<String> cmds = new LinkedList<String>();
                    cmds.add("/usr/bin/open");
                    cmds.add(installerPath);
                    Process p;
                    try {
                        p = new ProcessBuilder(cmds).directory(new File(com.apple.eio.FileManager.getPathToApplicationBundle() + "/Contents/Java/")).start();
                        Util.digestProcess(p);
                    } catch (IOException e) {
                        Util.handleException(e);
                    }
                    Platform.exit();
                    System.exit(0);
                } else {
                    Alert falert = new Alert(Alert.AlertType.ERROR);
                    falert.setHeaderText(I18N.tr("Update rejected"));
                    falert.setContentText(I18N.tr("You decided not to update - Shellfire VPN is now exiting."));
                    falert.showAndWait();
                    Platform.exit();
                    System.exit(0);
                }
                return;
            } else {LOG.debug("LoginForms: No update available");}
        } else {
            LOG.debug("No internet available, skipping update check");
        }
        LOG.debug("giving control to login");
        
        //instance.setApp(this);
        LOG.debug("Preparing to display login menu");
        this.loadLoginController();
        this.stage.show();
    }

    public LoginController getLoginInstance() {
        if (this.instance == null) {
            loadLoginController();
        }
        return this.instance;
    }

    public LoginController getLoginInstance(boolean minimize) {
        // if (instance == null)
        // gotoLogin();
        return this.instance;
    }

    private void showLoginProgress() {
        //this.loginProgressDialog = new ProgressDialog(this, false, i18n.tr("Logging in..."));
        this.initDialog.setVisible(true);
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

    public static ProgressDialogController getInitDialog() {
        return initDialog;
    }

    private void initConnectionTest() {
        // before doing anything else, we should test for an internet connection. without internet, we cant do anything!

        boolean internetAvailable = Util.internetIsAvailable();

        if (internetAvailable) {
            initDialog.setDialogText(I18N.tr("Initializing ShellfireVPNService..."));
            ServiceTools.getInstanceForOS().ensureServiceEnvironmentFX(this);
        } else {

            Alert alert = new Alert(Alert.AlertType.ERROR);
            //alert.setTitle("Error");
            alert.setHeaderText(I18N.tr("No internet"));
            alert.setContentText(I18N.tr("No internet connection available - ShellfireVPN is being closed."));
            alert.showAndWait();
            Platform.exit();
        }
    }
}
