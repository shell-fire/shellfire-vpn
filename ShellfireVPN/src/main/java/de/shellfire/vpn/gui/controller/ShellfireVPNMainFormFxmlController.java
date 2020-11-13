package de.shellfire.vpn.gui.controller;

import de.shellfire.vpn.Storage;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;
import javafx.util.Pair;
import de.shellfire.vpn.Util;
import de.shellfire.vpn.VpnProperties;
import de.shellfire.vpn.client.Client;
import de.shellfire.vpn.client.ConnectionState;
import de.shellfire.vpn.client.ConnectionStateChangedEvent;
import de.shellfire.vpn.client.ConnectionStateListener;
import de.shellfire.vpn.client.Controller;
import de.shellfire.vpn.exception.VpnException;
import de.shellfire.vpn.gui.FxUIManager;
import de.shellfire.vpn.gui.LoginForms;
import de.shellfire.vpn.gui.VpnTrayMessage;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.proxy.ProxyConfig;
import de.shellfire.vpn.types.Reason;
import de.shellfire.vpn.types.Server;
import de.shellfire.vpn.types.ServerType;
import de.shellfire.vpn.types.VpnProtocol;
import de.shellfire.vpn.webservice.Vpn;
import de.shellfire.vpn.webservice.WebService;
import de.shellfire.vpn.webservice.model.TrayMessage;
import java.awt.AWTEvent;
import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jdesktop.application.Action;
import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.LocaleChangeEvent;
import org.xnap.commons.i18n.LocaleChangeListener;

public class ShellfireVPNMainFormFxmlController extends AnchorPane implements Initializable, LocaleChangeListener, ConnectionStateListener {

    HashMap<SidePane, Pair<Pane, Object>> leftPaneHashMap = new HashMap<>();
    private static LoginForms application;
    private static final Logger log = Util.getLogger(ShellfireVPNMainFormFxmlController.class.getCanonicalName());
    private static final I18n i18n = VpnI18N.getI18n();
    private Controller controller;
    private WebService shellfireService;
    private MenuItem popupConnectItem;
    private MenuItem abortItem;
    private MenuItem disconnectItem;
    private PopupMenu popup;
    private TrayIcon trayIcon;
    private StringBuffer typedStrings = new StringBuffer();
    private ProgressDialogController connectProgressDialog;
    private ServerListSubviewController serverListSubviewController;
    private java.awt.Image iconConnecting;
    private Date connectedSince;
    private Image iconEcncryptionActive;
    private Image iconEcncryptionInactive;
    private Image iconConnectedSmall;
    private Image iconIdleSmall;
    private Image buttonConnect;
    private Image buttonDisconnect; 
    private java.awt.Image iconConnected;
    private java.awt.Image iconDisconnectedAwt;
    private java.awt.Image iconIdleAwt;
    private ScheduledExecutorService currentConnectedSinceTimerFX = Executors.newSingleThreadScheduledExecutor();
    private Preferences preferences; 
    private boolean connectionStatus ; 
    
    private final LogViewerFxmlController logViewer;
    static SidePane currentSidePane = SidePane.CONNECTION;
    
    @FXML
    private Pane leftMenuPane;
    @FXML
    private Pane leftConnectionPane;
    @FXML
    private ImageView connectoinBackgroundImageView;
    @FXML
    private Label connectionHeaderLabel;
    @FXML
    private Label connectionFooter;
    @FXML
    private Pane serverListPane;
    @FXML
    private ImageView serverListBackgroundImage;
    @FXML
    private Label serverListHeaderLabel;
    @FXML
    private ImageView shellfireImageView;
    @FXML
    private AnchorPane menuBarAnchorPane;
    @FXML
    private ImageView helpImageView;
    @FXML
    private ImageView settingsImageView;
    @FXML
    private ImageView hideImageView;
    @FXML
    private ImageView minimizeImageView;
    @FXML
    private ImageView exitImageView;
    @FXML
    private AnchorPane contentAnchorPane;
    @FXML
    private Pane contentHeaderPane;
    @FXML
    private ImageView globeConnectionImageView;
    @FXML
    private Label connectionStatusLabel;
    @FXML
    private Label connectionStatusValue;
    @FXML
    private Label connectedSinceLabel;
    @FXML
    private Label onlineIpLabel;
    @FXML
    private Label connectedSinceValue;
    @FXML
    private Label onlineIpValue;
    @FXML
    private Label vpnIdLabel;
    @FXML
    private Label vpnIdValue;
    @FXML
    private Label vpnTypeValue;
    @FXML
    private Pane contentDetailsPane;
    @FXML
    private Label vpnType;
    @FXML
    private Label serverListFooterLabel;
   
    // Access to embedded controller and variables in subviews
    @FXML
    private Parent connectionSubview;
    
    @FXML
    private ConnectionSubviewController connectionSubviewController;
    @FXML
    private Label validUntilLabel;
    @FXML
    private Label validUntilValue;
    
    public ShellfireVPNMainFormFxmlController() throws IOException {
        this.logViewer = LogViewerFxmlController.getInstance();
        log.debug("No argumenent controller of shellfire has been called");
    }

    public ConnectionSubviewController getConnectionSubviewController() {
        return connectionSubviewController;
    }
       
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        // setting the scaling factor to adjust sizes 
        double scaleFactor = Util.getScalingFactor();
        log.debug("ScalingFactor: " + scaleFactor);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double width = screenSize.getWidth();

        String size = "736";
        if (width > 3000) {
            size = "1472";
        }

        String langKey = VpnI18N.getLanguage().getKey();
        log.debug("langKey: " + langKey);

        mySetIconImage("/icons/sfvpn2-idle-big.png");
        
        // initializing images of the form
        this.connectoinBackgroundImageView.setImage(Util.getImageIconFX("/buttons/button-connect-idle.png"));
        this.serverListBackgroundImage.setImage(Util.getImageIconFX("/buttons/button-serverlist-idle.png"));
        this.globeConnectionImageView.setImage(Util.getImageIconFX("/icons/small-globe-disconnected.png"));
        this.shellfireImageView.setImage(getLogo());
        this.iconEcncryptionActive = Util.getImageIconFX("/icons/status-encrypted-width" + size + ".gif");
        this.iconEcncryptionInactive = Util.getImageIconFX("/icons/status-unencrypted-width" + size + ".gif");
        this.buttonConnect = Util.getImageIconFX("/buttons/button-connect-" + langKey + ".gif");
        this.buttonDisconnect = Util.getImageIconFX("/buttons/button-disconnect-" + langKey + ".gif");
        // initializing text of the form 
        this.connectionStatusLabel.setText(i18n.tr("Connection status"));
        this.connectedSinceLabel.setText(i18n.tr("Connected since:"));
        this.connectedSinceValue.setText(i18n.tr("(not connected)"));
        this.onlineIpLabel.setText(i18n.tr("Online IP:"));
        this.vpnIdLabel.setText(i18n.tr("VPN Id:"));
        this.validUntilLabel.setText(i18n.tr("Valid Until:"));
        this.vpnType.setText(i18n.tr("VPN type:"));
        this.connectionHeaderLabel.setText(i18n.tr("Connection"));
        this.connectionFooter.setText(i18n.tr("Connect to Shellfire VPN now"));
        this.connectionFooter.setWrapText(true);
        this.serverListHeaderLabel.setText(i18n.tr("Server list"));
        this.connectionStatusValue.setText(i18n.tr("Not connected"));
        this.serverListFooterLabel.setText(i18n.tr("Show list of all VPN servers"));
        this.serverListFooterLabel.setWrapText(true);
        this.connectoinBackgroundImageView.setImage(Util.getImageIconFX("/buttons/button-connect-active.png"));
        currentSidePane = SidePane.CONNECTION;

        this.iconConnected = Util.getImageIcon("/icons/sfvpn2-connected-big.png").getImage();
        this.iconConnectedSmall = Util.getImageIconFX("/icons/small-globe-connected.png");
        this.iconConnecting = Util.getImageIcon("/icons/sfvpn2-connecting-big.png").getImage();

        this.iconDisconnectedAwt = Util.getImageIcon("/icons/sfvpn2-disconnected-big.png").getImage();
       this.iconIdleSmall = Util.getImageIconFX("/icons/small-globe-disconnected.png");
        this.iconIdleAwt = Util.getImageIcon("/icons/sfvpn2-idle-big.png").getImage();
    }

    public void initializeComponents() {
        //Notice that you manipulate the javaObjects out of the initialize if not it will raise an InvocationTargetException
        this.updateLoginDetail();

        //Bind visibility of buttons to their manage properties so that they are easilty rendered visible or invisible
        this.validUntilLabel.managedProperty().bind(this.validUntilLabel.visibleProperty());
        this.validUntilValue.managedProperty().bind(this.validUntilValue.visibleProperty());

        this.connectionSubviewController.initPremium(isFreeAccount());
        this.connectionSubviewController.setApp(this.application);
        this.connectionSubviewController.updateComponents(false);
        this.updateOnlineHost();
    }

    public void setShellfireService(WebService shellfireService) {
        this.shellfireService = shellfireService;
        log.debug("ShellfireVPNMainFormFxmlController:" + "service initialized");
    }

    /**
     * Initialized the service and other variables. Supposed to be an
     * overloading of constructor
     *
     * @param WebService service
     */
    public void setSerciceAndInitialize(WebService service) {
        log.debug("Shellfire service has a size of " + service.getAllVpn().size());
        if (!service.isLoggedIn()) {
            try {
                throw new VpnException("ShellfireVPN Main Form required a logged in service. This should not happen!");
            } catch (VpnException ex) {
                log.debug("Logged in service is required to run the application ");
            }
        }

        log.debug("ShellfireVPNMainForm starting up");
        if (Util.isWindows()) {
            log.debug("Running on Windows " + Util.getOsVersion());

            if (Util.isVistaOrLater()) {
                log.debug("Running on Vista Or Later Version");
            } else {
                log.debug("Running on XP");
            }

        } else {
            log.debug("Running on Mac OS X " + Util.getOsVersion());
        }

        log.debug("System Architecture: " + Util.getArchitecture());

        this.shellfireService = service;
        this.initController();
        this.initTray();

        Storage.register(this);

        this.initShortCuts();
        this.initConnection();
        this.application.getStage().show();
    }

    private final static HashMap<String, Image> mainIconMap = new HashMap<String, Image>() {
        {
            put("de", Util.getImageIconFX("/icons/sf.png"));
            put("en", Util.getImageIconFX("/icons/sf_en.png"));
            put("fr", Util.getImageIconFX("/icons/sf_fr.png"));
        }
    };

    public static Image getLogo() {
        Image imagelogo = ShellfireVPNMainFormFxmlController.mainIconMap.get(VpnI18N.getLanguage().getKey());
        return imagelogo;
    }

    public void setApp(LoginForms applic) {
        this.application = applic;
    }

    public void afterLogin(boolean autoConnect) {
        Vpn vpn = this.shellfireService.getVpn();
        log.debug("Starting the connection after login");
        if (ProxyConfig.isProxyEnabled()) {
            this.setSelectedProtocol(VpnProtocol.TCP);
            this.serverListSubviewController.getUDPRadioButton().setDisable(true);
        } else {
            VpnProtocol selectedProtocol = vpn.getProtocol();
            this.setSelectedProtocol(selectedProtocol);
        }

        if (autoConnect) {
          Platform.runLater(()->{
            this.connectFromButton();
          });
            
        }
    }

    @FXML
    private void handleConnectionPaneMouseExited(MouseEvent event) {
        this.application.getStage().getScene().setCursor(Cursor.DEFAULT);
           if(!currentSidePane.equals(SidePane.CONNECTION)){
           this.connectoinBackgroundImageView.setImage(Util.getImageIconFX("/buttons/button-connect-idle.png"));
        }
    }

    @FXML
    private void handleConnectionPaneMouseEntered(MouseEvent event) {
        this.application.getStage().getScene().setCursor(Cursor.HAND);
        if(!currentSidePane.equals(SidePane.CONNECTION)){
            this.connectoinBackgroundImageView.setImage(Util.getImageIconFX("/buttons/button-connect-hover.png"));
        }
    }

    @FXML
    private void handleConnectionPaneContext(ContextMenuEvent event) {
    }

    @FXML
    private void handleConnectionPaneClicked(MouseEvent event) {
        try {
            Pair<Pane, Object> pair = FxUIManager.SwitchSubview("connection_subview.fxml");
            contentDetailsPane.getChildren().setAll(pair.getKey());
            this.connectionSubviewController = (ConnectionSubviewController) pair.getValue();
            this.connectionSubviewController.initPremium(isFreeAccount());
            this.connectionSubviewController.setApp(this.application);
            log.debug("handleConnectionPanelClicked: VPN connection status is " + connectionStatus);
            this.connectionSubviewController.updateComponents(connectionStatus);
            currentSidePane = SidePane.CONNECTION;
            updateSidePanes(currentSidePane);
        } catch (IOException ex) {
            log.debug("ShellfireVPNMainFormFxmlController:  handleConnectionPaneClicked has error " + ex.getMessage());
        }
    }

    @FXML
    private void handleServerListPaneMouseExited(MouseEvent event) {
        this.application.getStage().getScene().setCursor(Cursor.DEFAULT);
                if(!currentSidePane.equals(SidePane.SERVERLIST)){
               this.serverListBackgroundImage.setImage(Util.getImageIconFX("/buttons/button-serverlist-idle.png"));
        }
    }

    @FXML
    private void handleServerListPaneMouseEntered(MouseEvent event) {
        this.application.getStage().getScene().setCursor(Cursor.HAND);
        if(!currentSidePane.equals(SidePane.SERVERLIST)){
               this.serverListBackgroundImage.setImage(Util.getImageIconFX("/buttons/button-serverlist-hover.png"));
        }
    }

    @FXML
    private void handleServerListPaneContext(ContextMenuEvent event) {
    }

    @FXML
    private void handleServerListPaneClicked(MouseEvent event) {
        contentDetailsPane.getChildren().setAll(leftPaneHashMap.get(SidePane.SERVERLIST).getKey());
        this.serverListSubviewController.updateComponents(connectionStatus);
        currentSidePane = SidePane.SERVERLIST;
        updateSidePanes(currentSidePane);
    }

    private void updateSidePanes(SidePane pane ){
        if(pane.equals(SidePane.CONNECTION)){
            this.serverListBackgroundImage.setImage(Util.getImageIconFX("/buttons/button-serverlist-idle.png"));
            this.connectoinBackgroundImageView.setImage(Util.getImageIconFX("/buttons/button-connect-active.png"));
        } else if (pane.equals(SidePane.SERVERLIST)){
            this.serverListBackgroundImage.setImage(Util.getImageIconFX("/buttons/button-serverlist-active.png"));
            this.connectoinBackgroundImageView.setImage(Util.getImageIconFX("/buttons/button-connect-idle.png"));            
        }
    }
    @FXML
    private void handleHelpImageViewMouseExited(MouseEvent event) {
        this.application.getStage().getScene().setCursor(Cursor.DEFAULT);
    }

    @FXML
    private void handleHelpImageViewMouseEntered(MouseEvent event) {
        this.application.getStage().getScene().setCursor(Cursor.HAND);
    }

    @FXML
    private void handleHelpImageViewContext(ContextMenuEvent event) {
    }

    @FXML
    private void handleHelpImageViewClicked(MouseEvent event) {
        this.openHelp();
    }

    @FXML
    private void handleSettingsImageViewMouseExited(MouseEvent event) {
        this.application.getStage().getScene().setCursor(Cursor.DEFAULT);
    }

    @FXML
    private void handleSettingsImageViewMouseEntered(MouseEvent event) {
        this.application.getStage().getScene().setCursor(Cursor.HAND);
    }

    @FXML
    private void handleSettingsImageViewContext(ContextMenuEvent event) {
    }

    @FXML
    private void handleSettingsImageViewClicked(MouseEvent event) {
        showSettingsDialog();
    }

    @FXML
    private void handleHideImageViewExited(MouseEvent event) {
        this.application.getStage().getScene().setCursor(Cursor.DEFAULT);
    }

    @FXML
    private void handleHideImageViewEntered(MouseEvent event) {
        this.application.getStage().getScene().setCursor(Cursor.HAND);
    }

    @FXML
    private void handleHideImageViewContext(ContextMenuEvent event) {
    }

    @FXML
    private void handleHideImageViewClicked(MouseEvent event) {
      hide(this.application.getStage());
    }
    
    private void hide(final Stage stage) {
      Platform.runLater(new Runnable() {
          @Override
          public void run() {
              if (SystemTray.isSupported()) {
                  stage.hide();
              } else {
                  System.exit(0);
              }
          }
      });
  }

    @FXML
    private void handleMinimizeImageViewExited(MouseEvent event) {
        this.application.getStage().getScene().setCursor(Cursor.DEFAULT);
    }

    @FXML
    private void handleMinimizeImageViewEntered(MouseEvent event) {
        this.application.getStage().getScene().setCursor(Cursor.HAND);
    }

    @FXML
    private void handleMinimizeImageViewContext(ContextMenuEvent event) {
    }

    @FXML
    private void handleMinimizeImageViewClicked(MouseEvent event) {
        ((Stage) ((ImageView) event.getSource()).getScene().getWindow()).setIconified(true);
    }

    @FXML
    private void handleExitImageViewMouseExited(MouseEvent event) {
        this.application.getStage().getScene().setCursor(Cursor.DEFAULT);
    }

    @FXML
    private void handleExitImageViewMouseEntered(MouseEvent event) {
        this.application.getStage().getScene().setCursor(Cursor.HAND);
    }

    @FXML
    private void handleExitImageViewContext(ContextMenuEvent event) {
    }

    @FXML
    private void handleExitImageViewClicked(MouseEvent event) {
        exitHandler();        
    }

    private void initController() {
        if (this.controller == null) {
            this.controller = Controller.getInstanceFX(this, this.shellfireService);
            this.controller.registerConnectionStateListener(this);
        }
    }

    @Override
    public void localeChanged(LocaleChangeEvent lce) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void connectionStateChanged(ConnectionStateChangedEvent e) {
        initController();

        ConnectionState state = e.getConnectionState();
        log.debug("connectionStateChanged " + state + ", reason=" + e.getReason());
        switch (state) {
            case Disconnected:
                connectionStatus = false;
                this.setStateDisconnected();
                break;
            case Connecting:
                this.setStateConnecting();
                break;
            case Connected:
                connectionStatus = true;
                this.setStateConnected();
                break;
        }

    }

    public void connectFromButton() {
        log.debug("connectFromButton()");
        this.setWaitCursor();
        
        Task<ConnectionState> task = new Task<ConnectionState>() {
            @Override
            protected ConnectionState call() throws Exception {
                ConnectionState state = controller.getCurrentConnectionState();
                log.debug("retrieved current connection state: " + state);
                return state;
            }
        };
        task.setOnSucceeded(ignoreEvent -> {
            ConnectionState state = task.getValue();
            if (state == null) {
                state = ConnectionState.Disconnected;
            }

            switch (state) {
                case Disconnected:
                    if (isFreeAccount()) {

                        Server server = this.serverListSubviewController.getSelectedServer();
                        if (server.getServerType() == ServerType.Premium || server.getServerType() == ServerType.PremiumPlus) {
                              setNormalCursor();
                              Alert alert = new Alert(Alert.AlertType.CONFIRMATION, i18n.tr("This server is only available for Shellfire VPN Premium customers\n\nShow more information about Shellfire VPN Premium?"), ButtonType.YES, ButtonType.NO);
                              alert.setHeaderText(i18n.tr("Premium server selected"));
                              Optional<ButtonType> result = alert.showAndWait();

                              if ((result.isPresent()) && (result.get() == ButtonType.YES)) {
                                Util.openUrl(shellfireService.getUrlPremiumInfo());
                              }

                              return;
                      }

                        controller.connect(serverListSubviewController.getSelectedServer(), serverListSubviewController.getSelectedProtocol(), Reason.ConnectButtonPressed);
                    } else if (isPremiumAccount()) {
                        log.debug("ServerList Subview controller  has the object " + serverListSubviewController.toString());
                        Server server = this.serverListSubviewController.getSelectedServer();

                        if (server.getServerType() == ServerType.PremiumPlus) {
                            setNormalCursor();

                            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, i18n.tr("This server is only available for Shellfire VPN PremiumPlus customers\n\nShow more information about Shellfire VPN PremiumPlus?"), ButtonType.YES, ButtonType.NO);
                            alert.setHeaderText(i18n.tr("PremiumPlus server selected"));
                            Optional<ButtonType> result = alert.showAndWait();
                            if ((result.isPresent()) && (result.get() == ButtonType.YES)) {
                              Util.openUrl(shellfireService.getUrlPremiumInfo());
                            }
                            return;
                                                                                                                                                                                               
                        }

                        controller.connect(serverListSubviewController.getSelectedServer(), serverListSubviewController.getSelectedProtocol(), Reason.ConnectButtonPressed);
                    } else {
                        controller.connect(serverListSubviewController.getSelectedServer(), serverListSubviewController.getSelectedProtocol(), Reason.ConnectButtonPressed);
                    }

                    break;
                case Connecting:

                // not possible to click
                case Connected:
                    controller.disconnect(Reason.DisconnectButtonPressed);
                    break;
            }
        });
        
        log.debug("showConnectProgress: Thread Has started");
        Platform.setImplicitExit(false);
        Platform.runLater(()->{
            try {

                connectProgressDialog = ProgressDialogController.getInstance("Connecting ...", task, this.application.getStage(), true);
                connectProgressDialog.getRightButton().setOnAction(new EventHandler<javafx.event.ActionEvent>() {

                    @Override
                    public void handle(javafx.event.ActionEvent event) {
                        controller.disconnect(Reason.DisconnectButtonPressed);
                        task.cancel(true);
                        log.debug("showConnectProgress: Cancel button clicked");
                    }
                });
            } catch (IOException ex) {
                log.debug("connectFromButton. Error is " + ex.getMessage());
                ex.printStackTrace(System.out);
            }
        });
        
        task.run();
    }

    private boolean isFreeAccount() {
        return this.shellfireService.getVpn().getAccountType() == ServerType.Free;
    }

    private void setStateDisconnected() {
        log.debug("setStateDisconnected() - start");
        enableSystemProxyIfProxyConfig();
        this.hideConnectProgress();
        
        this.connectionSubviewController.connectButtonDisable(false);
        Platform.runLater(()->{
            mySetIconImage("/icons/sfvpn2-disconnected-big.png");
            this.connectionStatusValue.setText(i18n.tr("Not connected"));
        });
        this.serverListSubviewController.setConnetImage1Disable(false);
        this.globeConnectionImageView.setImage(this.iconIdleSmall);
        this.connectionSubviewController.updateComponents(false);
        this.serverListSubviewController.getConnectImage1().setImage(this.buttonConnect);
        log.debug("ShellfireMainForm: In setStateDisconnected method ");
        popup.remove(disconnectItem);
        popup.remove(abortItem);
        popupConnectItem.setLabel(i18n.tr("Connect"));
        popupConnectItem.setEnabled(true);
        popup.add(popupConnectItem);

        boolean showMessage = false;
        String message = "";
        if (this.controller != null) {
            switch (this.controller.getReasonForStateChange()) {
                case PasswordWrong:
                    showMessage = true;
                    message = i18n.tr("Invalid password");
                    break;
                case NotEnoughPrivileges:
                    showMessage = true;
                    message = i18n.tr("Process is being executed without administrator rights.");
                    break;
                case CertificateFailed:
                    showMessage = true;
                    message = i18n.tr("Unknown certificate error");
                    break;
                case AllTapInUse:
                    showMessage = true;
                    message = i18n.tr("All Tap devices in use. Please close openvpn.exe using the task manager or reboot your PC.");
                    break;
                case DisconnectDetected:
                    showMessage = true;
                    message = i18n.tr("Connection interrupted.");
                    break;
                case OpenVpnNotFound:
                    showMessage = true;
                    message = i18n.tr("No OpenVPN installation found. Please reinstall Shellfire VPN.");
                    break;
                case NoOpenVpnParameters:
                    showMessage = true;
                    message = i18n.tr("OpenVPN startup parameters could not be downloaded - Please check your internet connection.");
                    break;
                case TapDriverTooOld:
                    showMessage = true;
                    message = i18n.tr("The installed Tap driver is out of date. Please reinstall Shellfire VPN.");
                    break;
                case TapDriverNotFound:
                    showMessage = true;
                    message = i18n.tr("No Tap driver installed. Please reinstall Shellfire VPN.");
                    break;
                case TapDriverNotFoundPleaseRetry:
                    Platform.runLater(()->{
                      connectFromButton();
                    });  

                    break;
                case GatewayRedirectFailed:
                    showMessage = true;
                    message = i18n
                            .tr("The gateway coul not be switched. Please set a gateway in the TCP/IP settings of the current network adapter.");
                    break;
                case UnknownOpenVPNError:
                    showMessage = true;
                    message = i18n
                            .tr("An unknown error has occured while establishing the VPN connection. Please reboot and/or reinstall Shellfire VPN.");
                    break;

                default:
                    break;
            }

            log.debug("setStateDisconnected() - end");
        }
        final boolean text = showMessage;
        if (text) {
            final String finalMessage = message;
            Platform.runLater(()-> {
                Alert alert = new Alert(Alert.AlertType.ERROR, finalMessage, ButtonType.OK);
                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                alert.showAndWait();
                if (this.trayIcon != null) {
                    this.trayIcon.setImage(this.iconDisconnectedAwt);
                }
            });
        } else if (this.trayIcon != null) {
            this.trayIcon.setImage(this.iconIdleAwt);
        }
        this.stopConnectedSinceTimer();
        serverListSubviewController.getServerListTableView().disableProperty().set(false);
        this.setNormalCursor();
        this.updateOnlineHost();
        if (!ProxyConfig.isProxyEnabled()) {
			this.serverListSubviewController.getUDPRadioButton().setDisable(false);
	}
	this.serverListSubviewController.getTCPRadioButton().setDisable(false);
        Task<Reason> disconnectTask = new Task<Reason>() {

            @Override
            protected Reason call() throws Exception {
                Reason reasonForChange = controller.getReasonForStateChange();
		        return reasonForChange;
            }

            @Override
            protected void succeeded() {
                try {
                    Reason reasonForChange = get();
                    if (reasonForChange == Reason.DisconnectButtonPressed || reasonForChange == Reason.DisconnectDetected) {
                            showTrayMessageWithoutCallback(i18n.tr("Disconnected"),
                                            i18n.tr("Shellfire VPN connection terminated. Your internet connection is no longer secured!"));
                    }
                    } catch (Exception e) {
                        log.debug("Error while deconnecting: " + e.getMessage());
                            Util.handleException(e);
                    }
            }
            
            
        };
        new Thread(disconnectTask).start();
    }

    private void setStateConnecting() {
        log.debug("SetStateConnecting: Connnection in proessess");
        try {
            this.showConnectProgress();
        } catch (IOException ex) {
            log.debug("setStateConnecting: cannot start showConnectProgress with error " + ex.getMessage());
        }
        this.connectionSubviewController.connectButtonDisable(true);
        this.serverListSubviewController.setConnetImage1Disable(true); 

        //TODO_subview
        
        //this.connectionStatusValue.setText(i18n.tr("Connection is being processed..."));
        Platform.runLater(()->{
            this.connectionStatusValue.setText(i18n.tr("Connection is being processed..."));
            this.globeConnectionImageView.setImage(Util.getImageIconFX("/icons/small-globe-connecting.png"));
            mySetIconImage("/icons/sfvpn2-connecting-big.png");
        });

        if (this.trayIcon != null) {
            this.trayIcon.setImage(this.iconConnecting);
        }
        this.setWaitCursor();
        //TODO find FX equivalant variable
        //this.jConnectionStateIcon.setIcon(new ImageIcon(this.iconConnectingSmall));

        popupConnectItem.setLabel(i18n.tr("Connecting..."));
        popupConnectItem.setEnabled(false);
        popup.add(abortItem);
        serverListSubviewController.getServerListTableView().disableProperty().set(true);
        serverListSubviewController.getUDPRadioButton().setDisable(true);
        serverListSubviewController.getTCPRadioButton().setDisable(true);
    }

    /**
     * Displays the progress of execution as a task is being ran
     *
     * @return no return value
     */
    private void showConnectProgress() throws IOException {
            // create an instance of progress dialog
        //TODO using tasks methods to update application thread
        Platform.runLater(()->{
                log.debug("showConnectProgress: Entrance of method");
                if(ProgressDialogController.getDialogStage() != null){
                    ProgressDialogController.getDialogStage().show();
                }   
        });
    }

    public void mySetIconImage(String imagePath) {
        log.debug("mySetIconImage: the icon Image  path is " + imagePath);
        Platform.runLater(()->{
            this.application.getStage().getIcons().clear();
            this.application.getStage().getIcons().add(new Image(imagePath));
        });
    }

    private void initTray() {
        if (SystemTray.isSupported()) {

            SystemTray tray = SystemTray.getSystemTray();
            Image image2 = new Image("/icons/sfvpn2-idle-big.png");
            BufferedImage image = SwingFXUtils.fromFXImage(image2, null);
            ActionListener exitListener = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    exitHandler();
                }
            };

            popup = new PopupMenu();
            MenuItem defaultItem = new MenuItem(i18n.tr("Exit"));
            defaultItem.addActionListener(exitListener);

            ActionListener nagListener = new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                          Util.openUrl(shellfireService.getUrlPremiumInfo());
                        }
                    });
                }
            };

            MenuItem nagItem = new MenuItem(i18n.tr("Shellfire VPN Premium Infos"));
            nagItem.addActionListener(nagListener);

            ActionListener helpListener = (ActionEvent e) -> openHelp();

            MenuItem helpItem = new MenuItem(i18n.tr("Help"));
            helpItem.addActionListener(helpListener);

            ActionListener popupConnectListener = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                  Platform.runLater(()->{
                    connectFromButton();
                  });  
                  
                }
            };

            popupConnectItem = new MenuItem(i18n.tr("Connect"));
            popupConnectItem.addActionListener(popupConnectListener);

            ActionListener statusListener = (ActionEvent e) -> {
                Util.openUrl(shellfireService.getUrlSuccesfulConnect());
            };

            MenuItem statusItem = new MenuItem(i18n.tr("Show VPN state in your browser"));
            statusItem.addActionListener(statusListener);

            ActionListener openListener = (ActionEvent e) -> {
                mouseClickedFX();
            };

            MenuItem openItem = new MenuItem(i18n.tr("Shellfire VPN to front"));
            openItem.addActionListener(openListener);

            abortItem = new MenuItem(i18n.tr("Abort"));
            ActionListener abortListener = (ActionEvent e) -> {
                connectProgressDialog.getRightButton().fire();
            };

            abortItem.addActionListener(abortListener);

            disconnectItem = new MenuItem(i18n.tr("Disconnect"));
            ActionListener disconnectListener = (ActionEvent e) -> {
                controller.disconnect(Reason.DisconnectButtonPressed);
            };

            disconnectItem.addActionListener(disconnectListener);
            popup = new PopupMenu();
            popup.add(openItem);
            popup.add(statusItem);
            popup.add(helpItem);
            popup.add(nagItem);
            popup.add(defaultItem);
            popup.addSeparator();
            popup.add(popupConnectItem);

            ActionListener actionListener = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                  log.debug("trayIcon actionPerformed");
                    setVisible(true);
                    toFront();
                }
            };

            MouseListener mouseListener = new MouseListener() {

                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                  log.debug("trayIcon mouseClicked");
                  mouseClickedFX();
                }

                @Override
                public void mousePressed(java.awt.event.MouseEvent e) {
                }

                @Override
                public void mouseReleased(java.awt.event.MouseEvent e) {
                }

                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                }

            };

            trayIcon = new TrayIcon(image, "Shellfire VPN", popup);
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(actionListener);
            trayIcon.addMouseListener(mouseListener);

            startNagScreenTimer();
            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                System.err.println("TrayIcon could not be added.");
            }
        }
    }

    public void mouseClickedFX() {
        Platform.runLater(()-> {
            if(!this.application.getStage().isShowing()) {
                this.application.getStage().show();
        } else {
                this.application.getStage().toFront();
            }
        
        
        ((Stage)  this.application.getStage()).setIconified(false);
        setVisible(true);
        toFront();

       });
        
    }

    private void initShortCuts() {
        EventQueue ev = Toolkit.getDefaultToolkit().getSystemEventQueue();
        
        ev.push(new EventQueue() {

            protected void dispatchEvent(AWTEvent event) {
                if (event instanceof KeyEvent) {
                    log.debug("Event noticed from AWT");
                    final KeyEvent oKeyEvent = (KeyEvent) event;
                    if (oKeyEvent.getID() == KeyEvent.KEY_PRESSED) {
                        final int iKeyCode = oKeyEvent.getKeyCode();
                        appendKey((char) iKeyCode);
                    }
                }

                super.dispatchEvent(event);
            }
        });
        
   }

    private void appendKey(char c) {
        log.debug("Charter " + c + " pressed");
        this.typedStrings.append(c);
        if (typedStrings.toString().toLowerCase().endsWith("showconsole")) {
            this.initConsole();
        }
    }

    private void initConnection() {
        new Thread() {
            public void run() {
                controller.getCurrentConnectionState();
            }
        }.start();
    }

    public Controller getController() {
        return this.controller;
    }

    private void setNormalCursor() {
        this.application.getStage().getScene().setCursor(Cursor.DEFAULT);
    }

    private void setWaitCursor() {
        this.application.getStage().getScene().setCursor(Cursor.WAIT);
    }

    private void setStateConnected() {
        this.hideConnectProgress();
        Platform.runLater(() -> {
            this.connectionStatusValue.setText(i18n.tr("Connected"));
            mySetIconImage("/icons/sfvpn2-connected-big.png");       
        });

        this.connectionSubviewController.updateComponents(true);
        this.serverListSubviewController.getConnectImage1().setImage(this.buttonDisconnect);
        this.connectionSubviewController.connectButtonDisable(false);
        this.serverListSubviewController.setConnetImage1Disable(false);
        serverListSubviewController.getServerListTableView().disableProperty().set(true);
        if (this.trayIcon != null) {
            this.trayIcon.setImage(this.iconConnected);
        }

        this.setNormalCursor();
        this.globeConnectionImageView.setImage(this.iconConnectedSmall);

        this.startConnectedSinceTimer();

        this.updateOnlineHost();
        serverListSubviewController.getUDPRadioButton().disableProperty().set(true);
        serverListSubviewController.getTCPRadioButton().disableProperty().set(true);
        showTrayMessageWithoutCallback(i18n.tr("Connection successful"),
				i18n.tr("You are now connected to Shellfire VPN. Your internet connection is encrypted."));
        showStatusUrlIfEnabled();
	    disableSystemProxyIfProxyConfig();
	    popup.remove(popupConnectItem);
	    popup.remove(abortItem);
	    popup.add(disconnectItem);
    }
    
    private void showStatusUrlIfEnabled() {
		if (showStatusUrl())
			Util.openUrl(shellfireService.getUrlSuccesfulConnect());

	}

	private boolean showStatusUrl() {
	  VpnProperties props = VpnProperties.getInstance();
	    return props.getBoolean(LoginController.REG_SHOWSTATUSURL, false);
	}
    	

        
    private void hideConnectProgress() {
        Platform.runLater(()->{
            if (ProgressDialogController.getDialogStage() != null){       
                    ProgressDialogController.getDialogStage().hide();
                } 
        });
    }

    private void startConnectedSinceTimer() {
        int delay = 1000; // milliseconds
        connectedSince = new Date();

        currentConnectedSinceTimerFX.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                updateConnectedSince();
            }
        }, 0, delay, TimeUnit.MILLISECONDS);
        
    }
    private void disableSystemProxyIfProxyConfig() {
        if (ProxyConfig.isProxyEnabled()) {
          Client.disableSystemProxy();
        }
    }
        
    private void enableSystemProxyIfProxyConfig() {
        if (ProxyConfig.isProxyEnabled()) {
            Client.enableSystemProxy();
        }

    }

    private void stopConnectedSinceTimer() {
        if (this.currentConnectedSinceTimerFX != null) {
            //terminate all existing timer tasks
            this.currentConnectedSinceTimerFX.shutdown();
            this.currentConnectedSinceTimerFX = Executors.newSingleThreadScheduledExecutor();
        }
    }

    private void updateOnlineHost() {
        Task<String> hostWorker = new Task<String>() {
            @Override
            protected String call() throws Exception {
                String host = shellfireService.getLocalIpAddress();
                log.debug("ShellfireMainFormController: Ip address in task" + host);
                return host;
            }
        };
        hostWorker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                String host = hostWorker.getValue();
                onlineIpValue.setText(host);
                log.debug("ShellfireMainFormController: Ip address is " + host);
            }

        });
        Thread worker = new Thread(hostWorker);
          worker.run();

    }

    public void setSelectedProtocol(VpnProtocol protocol) {
        if (protocol == null) {
            protocol = VpnProtocol.UDP;
        }

        switch (protocol) {
            case UDP:
                this.serverListSubviewController.getUDPRadioButton().setSelected(true);
                break;
            case TCP:
                this.serverListSubviewController.getTCPRadioButton().setSelected(true);
                break;
        }

    }

    private void updateLoginDetail() {
        Vpn vpn = this.shellfireService.getVpn();
        log.debug("ShellfireMainFormController: vpn id and server are %s and %s " + vpn.getServerId(), vpn.getServer());
        this.vpnIdValue.setText("sf" + vpn.getVpnId());
        this.vpnTypeValue.setText(vpn.getAccountType().toString());

        if (vpn.getAccountType() == ServerType.Free) {
            this.validUntilValue.setVisible(false);
            this.validUntilLabel.setVisible(false);
        } else {

            this.validUntilValue.setDisable(false);
            this.validUntilLabel.setDisable(false);

            SimpleDateFormat df = new SimpleDateFormat(i18n.tr("d/MM/yyyy"), VpnI18N.getLanguage().getLocale());
            String date = df.format(vpn.getPremiumUntil());

            this.validUntilValue.setText(date);
        }
    }


    public void updateConnectedSince() {
        Date now = new Date();
        long diffInSeconds = (now.getTime() - connectedSince.getTime()) / 1000;

        long diff[] = new long[]{0, 0, 0, 0};
        diff[3] = (diffInSeconds >= 60 ? diffInSeconds % 60 : diffInSeconds);
        diff[2] = (diffInSeconds = (diffInSeconds / 60)) >= 60 ? diffInSeconds % 60 : diffInSeconds;
        diff[1] = (diffInSeconds = (diffInSeconds / 60));
        String since = String.format("%dh %dm %ds", diff[1], diff[2], diff[3]);

        SimpleDateFormat df = new SimpleDateFormat("E, H:m", VpnI18N.getLanguage().getLocale());
        String start = df.format(connectedSince);
        String text = start + " " + "(" + since + ")";
        Platform.runLater(()->{this.connectedSinceValue.setText(text);});
    }

    private void showSettingsDialog() {
        try {
            Pair<Pane, Object> pair = FxUIManager.SwitchSubview("menuShellfireSettings.fxml");
            Stage dialogStage = new Stage(StageStyle.UTILITY);
            Scene scene = new Scene(pair.getKey());
            dialogStage.setTitle(i18n.tr("Shellfire VPN settings"));
            dialogStage.setScene(scene);
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setAlwaysOnTop(true);
            dialogStage.setResizable(false);
            dialogStage.show();
        } catch (IOException ex) {
            log.debug("ShellfireVPNMainFormFxmlController:  showSettingsDialog has error " + ex);
        }
    }

    @Action
    public void openHelp() {
        Util.openUrl(shellfireService.getUrlHelp());
    }

    private void startNagScreenTimer() {        
        currentConnectedSinceTimerFX.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                showTrayIconNagScreen();
            }
        }, 0, 1, TimeUnit.HOURS);
 
    }

    private void showTrayIconNagScreen() {
        LinkedList<VpnTrayMessage> messages = new LinkedList<VpnTrayMessage>();
        if (controller.getCurrentConnectionState() == ConnectionState.Connected) {

            if (this.shellfireService.getVpn().getAccountType() == ServerType.Free) {
                List<TrayMessage> trayMessages = this.shellfireService.getTrayMessages();

                trayMessages.forEach(msg -> {
                    messages.add(new VpnTrayMessage(msg.getHeader(), msg.getText()));
                });
            }
        } else {
            messages.add(new VpnTrayMessage(i18n.tr("Not connected"), i18n.tr("You are not connected to Shellfire VPN.")));
        }

        if (messages.size() > 0) {
            Random generator = new Random((new Date()).getTime());
            int num = generator.nextInt(messages.size());
            VpnTrayMessage msgToShow = messages.get(num);
            showTrayMessageWithoutCallback(msgToShow.getCaption(), msgToShow.getText());
        }

    }


    private boolean isPremiumAccount() {
        return this.shellfireService.getVpn().getAccountType() == ServerType.Premium;
    }

    public void setSelectedServer(Server server) {
        log.debug("setSelectedServer(" + server + ")");
        int num = this.shellfireService.getServerList().getServerNumberByServer(server);
        this.serverListSubviewController.setSelectedServer(num);

    }

    /**
     *  Prepare controllers so that they load controllers so that controller objects can be accessed. 
     */
    public void prepareSubviewControllers(){

        // load the serverList pane
        try {
            Pair<Pane, Object> pair = FxUIManager.SwitchSubview("serverList_subview.fxml");
            this.serverListSubviewController = (ServerListSubviewController) pair.getValue();
            this.serverListSubviewController.setShellfireService((this.shellfireService));
            this.serverListSubviewController.initComponents();
            this.serverListSubviewController.initPremium(isFreeAccount());
            this.serverListSubviewController.setApp(this.application);
            this.serverListSubviewController.setMainFormController(this);
            this.serverListSubviewController.afterInitialization();
            leftPaneHashMap.put(SidePane.SERVERLIST, pair);
            log.debug("Serverlist controller defined");
        } catch (IOException ex) {
            log.debug("ShellfireVPNMainFormFxmlController:  prepareControllers has error " + ex.getMessage());
        }
    }
    
    private void exitHandler() {
        boolean connected;

        connected = this.controller.getCurrentConnectionState() != ConnectionState.Disconnected;
        if (connected) {
            askForDisconnectedAndQuit();
        } else {
            enableSystemProxyIfProxyConfig();
            Platform.exit();
            System.exit(0);
        }
    }
    private void askForDisconnectedAndQuit() {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, i18n.tr("Disconnect and close Shellfire VPN?"), ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(i18n.tr("Currently Connected"));
        Optional<ButtonType> result = alert.showAndWait();

        if ((result.isPresent()) && (result.get() == ButtonType.YES)) {
            this.controller.disconnect(Reason.ApplicationExit);
            enableSystemProxyIfProxyConfig();
            Platform.exit();
            System.exit(0);
        }
    }
    
 	private void showTrayMessageWithoutCallback(String header, String content) {
 	  log.debug("showTrayMessageWithoutCallback(String header="+header+", String content)");
			trayIcon.displayMessage(header, content, MessageType.INFO);
	}

    private void initConsole() {
        log.debug("showing logviewer...");
            try {
	        log.debug("setting logViewer to visible");
	        logViewer.getInstanceStage().show();
                log.debug("Logviewer has been shown");
            } catch (Exception e) {
                log.error("Error occured while displaying logviewer", e);
            }
    }

    @FXML
    private void handleWindowKeyPressed(javafx.scene.input.KeyEvent event) {
        if(event.getCode().isLetterKey()) {
            appendKey(event.getCode().getName().charAt(0));
        }
    }

}
    enum SidePane 
    { 
        CONNECTION, SERVERLIST;
    } 