package de.shellfire.vpn.gui;

import de.shellfire.vpn.Util;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;

import de.shellfire.vpn.client.ServiceTools;
import de.shellfire.vpn.client.ServiceToolsFX;
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
import java.util.HashMap;
import java.util.Map;
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
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;

import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class LoginForms extends Application {

    private static final Logger log = Util.getLogger(LoginForms.class.getCanonicalName());
    public static Stage stage;
    public static String[] default_args;
    public static ProgressDialogController initDialog;
    public RegisterFormController registerController;
    public LicenseAcceptanceController licenceAcceptanceController;
    public static VpnSelectDialogController vpnSelectController;
    public static ShellfireVPNMainFormFxmlController shellFireMainController;
    private boolean minimize;
    public static LoginController instance;
    public final static boolean isJavaFX = true;
    private static final I18n I18N = VpnI18N.getI18n();
    //private AnchorPane page;
    private boolean licenseAccepted;
    // Variables to control draggin of window
    private static double xOffset = 0;
    private static double yOffset = 0;
    public static Stage initDialogStage = null;
    //public HashMap<Object,Stage> controllersAndStage = new HashMap<>();

    public static Stage getStage() {
        return stage;
    }

    public static void setStage(Stage stage) {
        LoginForms.stage = stage;
    }

    public static void main(String[] args) {
        System.setProperty("java.library.path", "./lib");
        default_args = args;
        //initializations(args);
        launch(args);
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
            this.stage = primaryStage;
            this.stage.initStyle(StageStyle.UNDECORATED);
            setLookAndFeel();
            this.loadLoginController();
            //initConnectionTest();
            this.afterDialogDisplay();

        } catch (Exception ex) {
            log.error("could not start with first stage load \n");
            ex.printStackTrace(System.out);
        }

        try {
            initializations(default_args);

            stage.sizeToScene();
            stage.getProperties().put("hostServices", this.getHostServices());

            afterDialogDisplay();
            //this.stage.show();
        } catch (Exception ex) {
            log.error("could not latter message after login in start \n" + ex.getMessage());
        }

    }

    /**
     * Initializing the dialog controller & stage
     */
    public static void setLookAndFeel() {
        log.debug("setLookAndFeel: first test");
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(LoginForms.class.getResource("/fxml/ProgressDialog.fxml"));
            AnchorPane page = (AnchorPane) loader.load();
            initDialog = loader.getController();
            initDialogStage = new Stage();
            initDialogStage.initOwner(getStage());
            Scene scene = new Scene(page);
            initDialogStage.setScene(scene);
            initDialog.setDialogText("Init ...");
            initDialog.getProgressBar().setProgress(ProgressBar.INDETERMINATE_PROGRESS);
            initDialog.addInfo("");
            initDialog.addBottomText("");
            
            initDialogStage.initStyle(StageStyle.UNDECORATED);
            initDialogStage.setTitle("Init");
            initDialogStage.initModality(Modality.WINDOW_MODAL);

            initDialogStage.show();
            //initDialog = loader.getController();            
            // unbind any previous progress bar
            initDialog.getProgressBar().progressProperty().unbind();

            initDialog.setVisible(true);
            
            log.debug("setLookAndFeel: last test");
        } catch (Exception e) {
            log.error("There is an exception caused by the setLookAndFeel method, " + e.toString());
        }
    }

    public static void loadInitializeProgressDialog() {
        try {
            initDialog = (ProgressDialogController) replaceSceneWithDialogStage("ProgressDialog.fxml");
            //initDialog.setApp(this);

        } catch (Exception ex) {
            log.error("could not load progressDialog fxml \n" + ex.getMessage());
        }
    }

    // Overloading of Method loadProgressDialog
    public void loadProgressDialog() {
        log.debug("LoginForms: sAttempt has been made to load progressdialog");
        try {
            this.initDialog = (ProgressDialogController) replaceSceneContent("ProgressDialog.fxml");
            this.initDialog.setApp(this);

        } catch (Exception ex) {
            log.error("could not load progressDialog fxml \n" + ex.getMessage());
        }
    }
    
    public void loadProgressDialogAndInitStage(){
        
    }
    public void loadLoginController() {
        System.out.println("In the getLogin controller");
        try {
            this.instance = (LoginController) replaceSceneContent("login.fxml");
            this.instance.setApp(this);
            this.stage.setTitle("Shellfire VPN 2 Login");
            log.debug("LoginForms: Login controller loaded");

        } catch (Exception ex) {
            log.error("could not load loginController fxml\n" + ex.getMessage());
        }

    }

    public void loadRegisterFormController() {
        log.debug("In the RegisterForm controller");
        try {
            this.registerController = (RegisterFormController) replaceSceneContent("RegisterFormFxml"
                    + ".fxml");
            this.registerController.setApp(this);

        } catch (Exception ex) {
            log.error("could not load RegisterForm fxml\n" + ex.getMessage());
        }

    }

    public void loadVPNSelect() {
        log.debug("In the VPN controller");
        try {
            this.vpnSelectController = (VpnSelectDialogController) replaceSceneContent("VpnSelectDialogFxml.fxml");
            this.vpnSelectController.setApp(this);
        } catch (Exception ex) {
            log.error("could not load vpnSelect fxml\n" + ex.getMessage());
        }

    }

    public void loadShellFireMainController() {
        log.debug("In the ShellFire Main controller");
        try {
            this.shellFireMainController = (ShellfireVPNMainFormFxmlController) replaceSceneContent("ShellfireVPNMainFormFxml.fxml");
            this.shellFireMainController.displayMessage("Object created");
            this.shellFireMainController.setApp(this);

        } catch (Exception ex) {
            log.error("could not load main form fxml\n" + ex.getMessage());
        }
        log.debug("ShellfireMainFrom initializtion method completed");
    }

    public void loadLicenceAcceptanceScreenController() {
        log.debug("In the licence Acceptance Screen controller");
        try {
            this.licenceAcceptanceController = (LicenseAcceptanceController) replaceSceneContent("LicenseAcceptScreen.fxml");
            //Platform.runLater(() -> progressDialog.setVisible(true));
            this.licenceAcceptanceController.setApp(this);
            //this.stage.setTitle("Shellfire VPN 2 Login");

        } catch (Exception ex) {
            log.error("could not load RegisterForm fxml\n" + ex.getMessage());
        }

    }

    public static Initializable replaceSceneContent(String fxml) throws Exception {
        FXMLLoader loader = new FXMLLoader(LoginForms.class.getClassLoader().getResource(fxml));
        loader.setLocation(LoginForms.class.getResource("/fxml/" + fxml));
        System.out.println("Loacation of loader is " + loader.getLocation());
        AnchorPane page = null;
        try {
            System.out.println("ReplaceSceneContent trying to load anchor pane for " + fxml);
            page = (AnchorPane) loader.load();
            System.out.println("Location of Controller is " + loader.getController());

        } catch (Exception ex) {
            log.error(" Loading fxml has error for replaceSceneContent for " + fxml + " with error " + ex.getMessage());
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
            log.debug("Scene of " + fxml + " has been newly created");
        } else {
            log.debug("Scene of " + fxml + " is that of anchorpane");
            stage.setScene(page.getScene());
        }
        stage.centerOnScreen();
        stage.sizeToScene();
        return (Initializable) loader.getController();
    }

    public static Initializable replaceSceneWithDialogStage(String fxml) throws Exception {
        FXMLLoader loader = new FXMLLoader(LoginForms.class.getClassLoader().getResource(fxml));
        loader.setLocation(LoginForms.class.getResource("/fxml/" + fxml));
        System.out.println("replaceSCenContentWithSameRoot Loacation of loader is " + loader.getLocation());
        AnchorPane page = null;
        try {
            System.out.println("ReplaceSceneContent trying to load anchor pane for " + fxml);
            page = (AnchorPane) loader.load();
            System.out.println("Location of Controller is " + loader.getController());
        } catch (Exception ex) {
            log.error(" Loading fxml has error for replaceSceneContentWithSameRoot " + ex.getMessage());
        }
        initDialogStage = new Stage();
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
                initDialogStage.setX(event.getScreenX() - xOffset);
                initDialogStage.setY(event.getScreenY() - yOffset);
            }
        });
        if (page.getScene() == null) {
            Scene scene = new Scene(page);
            initDialogStage.setScene(scene);
            log.debug("Scene of " + fxml + " has been newly created");
        } else {
            log.debug("Scene of " + fxml + " is that of anchorpane");
            initDialogStage.setScene(page.getScene());
        }
        initDialogStage.centerOnScreen();
        initDialogStage.sizeToScene();
        return (Initializable) loader.getController();
    }

    public void afterDialogDisplay() {
        if (default_args.length > 0) {

            String cmd = default_args[0];

            if (cmd.equals("uninstallservice")) {
                ServiceTools.getInstanceForOS().uninstall();
                //initDialog.dispose();
                //initDialog.getScene().setRoot(null);
                this.stage.hide();
                return;
            } else if (cmd.equals("installservice")) {
                //initDialog.dispose();
                //initDialog.getScene().setRoot(null);
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

                log.debug("Retrieved installation path from args parameter: " + path);
                //initDialog.dispose();
                //this.stage.hide();
                new Updater().performUpdate(path, user);

                return;
            }
        }
        // hidding stage
        log.debug("Hiding stage");
        this.stage.hide();
        log.debug("after dialog box , before login controller");

        // test Internet connection 
        boolean internetAvailable = Util.internetIsAvailable();
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
                    log.debug("Opening updater using Desktop.open(): " + installerPath);
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
            } else {
                log.debug("LoginForms: No update available");
            }
        } else {
            log.debug("No internet available, skipping update check");
        }
        log.debug("giving control to login");

        instance.setApp(this);
        log.debug("Preparing to display login menu");
        //this.stage.show();
        this.initConnectionTest();
    }

    public LoginController getLoginInstance() {
        if (this.instance == null) {
            loadLoginController();
        }
        return this.instance;
    }

    public LoginController getLoginInstance(boolean minimize) {
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

    private static void initConnectionTest() {
        // before doing anything else, we should test for an internet connection. without internet, we cant do anything!
        log.debug("In initConnection Test method");
        boolean internetAvailable = Util.internetIsAvailable();

        if (internetAvailable) {
            log.debug("Before the service Environment Ensure");
            initDialog.setDialogText(I18N.tr("Initializing ShellfireVPNService..."));
            ServiceToolsFX.getInstanceForOS().ensureServiceEnvironmentFX(instance);
            log.debug("After the service Environment Ensure");
        } else {
            log.debug("Connection not available");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(I18N.tr("No internet"));
            alert.setContentText(I18N.tr("No internet connection available - ShellfireVPN is being closed."));
            alert.showAndWait();
            Platform.exit();
        }
    }
}
