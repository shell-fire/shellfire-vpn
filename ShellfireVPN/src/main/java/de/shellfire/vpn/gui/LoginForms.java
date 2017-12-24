package de.shellfire.vpn.gui;

import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.client.ServiceTools;
import de.shellfire.vpn.gui.controller.LicenseAcceptanceController;
import de.shellfire.vpn.gui.controller.LoginController;
import de.shellfire.vpn.gui.controller.ProgressDialogController;
import de.shellfire.vpn.gui.controller.RegisterFormController;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.proxy.ProxyConfig;
import de.shellfire.vpn.updater.Updater;
import java.io.File;
import java.io.IOException;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class LoginForms extends Application {
    
    private static Logger log = Util.getLogger(LoginForms.class.getCanonicalName());
    public static Stage stage;    
    public static String[] default_args;
    public static ProgressDialogController initDialog;
    public RegisterFormController registerController;
    public LicenseAcceptanceController licenceAcceptanceController;
    private boolean minimize;    
    public static LoginController instance;    
    private static I18n i18n = VpnI18N.getI18n();
    private static AnchorPane page;
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
            this.stage.show();
            
        } catch (Exception ex) {
            log.debug("could not start with first stage load " + ex);
        }
        
        try {
            afterDialogDisplay();
        } catch (Exception ex) {
            log.debug("could not latter message after login in start \n" + ex.getMessage());
        }
    }
    
    public static void main(String[] args) {
        //System.setProperty("java.library.path", "C:\\Users\\Tcheutchoua\\Documents\\NetBeansProjects\\ShellFire\\shellfire-vpn\\ShellfireVPN\\lib");
        
        System.setProperty("java.library.path", "./lib");
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
        
        ProxyConfig.perform();
        setLookAndFeel();
    }
    
    private static void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            Util.setDefaultSize(Util.getFontSize());
            
        } catch (Exception ex) {
        }
        
    }
    
    public void loadProgressDialog(String message) {
        try {
            this.initDialog = (ProgressDialogController) replaceSceneContent("ProgressDialog.fxml");
            //Platform.runLater(() -> progressDialog.setVisible(true));
            this.initDialog.setDialogText(message);
            this.initDialog.setApp(this);
            
        } catch (Exception ex) {
            log.debug("could not load progressDialog fxml \n" + ex.getMessage());
        }
    }

    // Overloading of Method loadProgressDialog
    public void loadProgressDialog() {
        try {
            this.initDialog = (ProgressDialogController) replaceSceneContent("ProgressDialog.fxml");
            //Platform.runLater(() -> progressDialog.setVisible(true));
            this.initDialog.setApp(this);
            
        } catch (Exception ex) {
            log.debug("could not load progressDialog fxml \n" + ex.getMessage());
        }
    }
    
    public void loadLoginController() {
        System.out.println("In the getLogin controller");
        try {
            this.instance = (LoginController) replaceSceneContent("login.fxml");
            //Platform.runLater(() -> progressDialog.setVisible(true));
            this.instance.setApp(this);
            this.stage.setTitle("Shellfire VPN 2 Login");
            
        } catch (Exception ex) {
            log.debug("could not load loginController fxml\n" + ex.getMessage());
        }
        
    }
    
    public void loadRegisterFormController() {
        log.debug("In the RegisterForm controller");
        try {
            this.registerController = (RegisterFormController) replaceSceneContent("RegisterFormFxml.fxml");
            //Platform.runLater(() -> progressDialog.setVisible(true));
            this.registerController.setApp(this);
            //this.stage.setTitle("Shellfire VPN 2 Login");

        } catch (Exception ex) {
            log.debug("could not load RegisterForm fxml\n" + ex.getMessage());
        }
        
    }
    
    public void getLicenceAcceptanceScreenController() {
        log.debug("In the licence Acceptance Screen controller");
        try {
            this.licenceAcceptanceController = (LicenseAcceptanceController) replaceSceneContent("LicenseAcceptScreen.fxml");
            //Platform.runLater(() -> progressDialog.setVisible(true));
            this.licenceAcceptanceController.setApp(this);
            //this.stage.setTitle("Shellfire VPN 2 Login");

        } catch (Exception ex) {
            log.debug("could not load RegisterForm fxml\n" + ex.getMessage());
        }
        
    }
    
    public Initializable replaceSceneContent(String fxml) throws Exception {
        FXMLLoader loader = new FXMLLoader(LoginForms.class.getResource(fxml));
        // InputStream in = LoginForms.class.getResourceAsStream(fxml);
        //loader.setBuilderFactory(new JavaFXBuilderFactory());
        loader.setLocation(LoginForms.class.getResource("/fxml/" + fxml));
        System.out.println("Loacation of loader is " + loader.getLocation());
        
        try {
            System.out.println("trying to load anchor pane");
            page = (AnchorPane) loader.load();
            System.out.println("Location of Controller is " + loader.getController());
            
        } catch (Exception ex) {
            log.debug(" Loading fxml has error " + ex.getMessage());
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
        Scene scene = new Scene(page);
        stage.setScene(scene);
        stage.sizeToScene();
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
        //this.stage.hide();
        log.debug("after dialog box , before login controller");
        loadLoginController();

        // test Internet connection 
        boolean internetAvailable = Util.internetIsAvailable();
        log.debug("after getting the login controller ");
        if (internetAvailable) {
            Updater updater = new Updater();
            if (updater.newVersionAvailable()) {
                
                int answer = JOptionPane
                        .showConfirmDialog(
                                null,
                                i18n.tr("Es ist eine neue Version von Shellfire VPN verfügbar. Ein Update ist zwingend erforderlich. Möchtest du jetzt updaten?"),
                                i18n.tr("Neue Version"),
                                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                
                if (answer == JOptionPane.YES_OPTION) {
                    JOptionPane.showMessageDialog(
                            null,
                            i18n.tr("Du hast dich entschieden, zu updaten. Shellfire VPN wird jetzt mit Admin-Privilegien neugestartet um das Update durchzuführen."),
                            i18n.tr("Update wird durchgeführt"), JOptionPane.INFORMATION_MESSAGE);
                    
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
                    System.exit(0);
                    
                } else {
                    JOptionPane.showMessageDialog(
                            null,
                            i18n.tr("Du hast dich entschieden, nicht zu updaten. Shellfire VPN wird jetzt beendet."),
                            i18n.tr("Update abgelehnt"), JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                }
                
                return;
            }
        } else {
            log.debug("No internet available, skipping update check");
        }
        log.debug("giving control to login");
        instance.setApp(this);
        System.out.println("Preparing to display login menu");
        
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
        //this.loginProgressDialog = new ProgressDialog(this, false, i18n.tr("Einloggen..."));
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
}
