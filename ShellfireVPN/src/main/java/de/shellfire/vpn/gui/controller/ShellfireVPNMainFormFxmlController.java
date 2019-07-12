package de.shellfire.vpn.gui.controller;

import de.shellfire.vpn.Storage;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;
import javafx.util.Pair;
import de.shellfire.vpn.Util;
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
import javafx.concurrent.Worker;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
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
import javax.swing.JOptionPane;
import org.jdesktop.application.Action;
import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.LocaleChangeEvent;
import org.xnap.commons.i18n.LocaleChangeListener;

public class ShellfireVPNMainFormFxmlController extends AnchorPane implements Initializable, LocaleChangeListener, ConnectionStateListener {

    private static final I18n I18N = VpnI18N.getI18n();

    private static LoginForms application;
    private static final Logger log = Util.getLogger(ShellfireVPNMainFormFxmlController.class.getCanonicalName());
    private static final I18n i18n = VpnI18N.getI18n();
    private Controller controller;
    private WebService shellfireService;
    private MenuItem popupConnectItem;
    private PopupMenu popup;
    private TrayIcon trayIcon;
    private StringBuffer typedStrings = new StringBuffer();
    private ProgressDialogController connectProgressDialog;
    private MapEncryptionSubviewController mapEncryptionSubviewController;
    private ServerListSubviewController serverListSubviewController;
    private TvStreasSubviewController tvStreasSubviewController;
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
    private PremiumScreenController nagScreen;
    private ScheduledExecutorService currentConnectedSinceTimerFX = Executors.newSingleThreadScheduledExecutor();
    private Preferences preferences; 
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
    private Pane streamsPane;
    @FXML
    private ImageView streamsBackgroundImageView;
    @FXML
    private Label streamsHeaderLabel;
    @FXML
    private Label streamsFooterLabel;
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
    
    String baseImageUrl = "src/main/resources";
    Stage popUpStage = null; 
    static SidePane currentSidePane = SidePane.CONNECTION;
    
    public ShellfireVPNMainFormFxmlController() {
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
        this.connectoinBackgroundImageView.setImage(Util.getImageIconFX(baseImageUrl + "/buttons/button-connect-idle.png"));
        this.serverListBackgroundImage.setImage(Util.getImageIconFX(baseImageUrl + "/buttons/button-serverlist-idle.png"));
        this.streamsBackgroundImageView.setImage(Util.getImageIconFX(baseImageUrl + "/buttons/button-usa-idle.png"));
        this.globeConnectionImageView.setImage(Util.getImageIconFX(baseImageUrl + "/icons/small-globe-disconnected.png"));

        this.iconEcncryptionActive = Util.getImageIconFX(baseImageUrl + "/icons/status-encrypted-width" + size + ".gif");
        this.iconEcncryptionInactive = Util.getImageIconFX(baseImageUrl + "/icons/status-unencrypted-width" + size + ".gif");
        this.buttonConnect = Util.getImageIconFX(baseImageUrl + "/buttons/button-connect-" + langKey + ".gif");
        this.buttonDisconnect = Util.getImageIconFX(baseImageUrl + "/buttons/button-disconnect-" + langKey + ".gif");
        // initializing text of the form 
        this.connectionStatusLabel.setText(i18n.tr("Connection status"));
        this.connectedSinceLabel.setText(i18n.tr("Connected since:"));
        this.onlineIpLabel.setText(i18n.tr("Online IP"));
        this.vpnIdLabel.setText(i18n.tr("VPN Id:"));
        this.validUntilLabel.setText(i18n.tr("Valid Until:"));

        this.connectionHeaderLabel.setText(i18n.tr("Connection"));
        this.connectionFooter.setText(i18n.tr("Connect to Shellfire VPN now"));
        this.serverListHeaderLabel.setText(i18n.tr("Server list"));
        this.connectionStatusValue.setText(i18n.tr("Not connected"));
        this.serverListFooterLabel.setText(i18n.tr("Show list of all VPN servers"));
        this.streamsHeaderLabel.setText(i18n.tr("USA streams"));
        this.streamsFooterLabel.setText(i18n.tr("List of american TV streams"));
        this.connectoinBackgroundImageView.setImage(Util.getImageIconFX(baseImageUrl + "/buttons/button-connect-active.png"));
        currentSidePane = SidePane.CONNECTION;
            
//        log.debug(connectionSubviewController.toString());
        //      log.debug(connectionSubviewController.displayCreationMessage("Object refreence properly created"));
        this.iconConnected = Util.getImageIcon("/icons/sfvpn2-connected-big.png").getImage();
        this.iconConnectedSmall = Util.getImageIconFX(baseImageUrl + "/icons/small-globe-connected.png");
        this.iconConnecting = Util.getImageIcon("/icons/sfvpn2-connecting-big.png").getImage();

        this.iconDisconnectedAwt = Util.getImageIcon("/icons/sfvpn2-disconnected-big.png").getImage();
       this.iconIdleSmall = Util.getImageIconFX(baseImageUrl + "/icons/small-globe-disconnected.png");
        this.iconIdleAwt = Util.getImageIcon("/icons/sfvpn2-idle-big.png").getImage();
        
        //this.serverList = this.shellfireService.getServerList();
        //this.updateLoginDetail();
        // this.initTray();
        //Storage.register(this);
        //this.initConnection();
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
        log.debug("In setShellfireService method");
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
        //this.application.getStage().show();
        /*
        // continue here, cursor
        //CustomLayout.register();
        //this.setFont(TitiliumFont.getFont());
        //this.loadIcons();
        //this.setLookAndFeel();
        //initComponents();
         */
        this.initTray();

        //TODO
        //this.initLayeredPaneSize();
        //this.initContent();
        Storage.register(this);

        this.initShortCuts();
        //TODO_subview uncomment initPrimium and add corresponding logic
        //connectionSubviewController.initPremium(isFreeAccount());
        this.initConnection();

        //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //pack();
        //this.setLocationRelativeTo(null);
        //setVisible(true);
        this.application.getStage().show();
    }

    private final static HashMap<String, Image> mainIconMap = new HashMap<String, Image>() {
        {
            put("de", Util.getImageIconFX("src/main/resources/icons/sf.png"));
            put("en", Util.getImageIconFX("src/main/resources/icons/sf_en.png"));
            put("fr", Util.getImageIconFX("src/main/resources/icons/sf_fr.png"));
        }
    };

    public static Image getLogo() {
        Image imagelogo = ShellfireVPNMainFormFxmlController.mainIconMap.get(VpnI18N.getLanguage().getKey());
        System.out.println("The image key is found at " + VpnI18N.getLanguage().getKey());

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

        Server server = vpn.getServer();

        if (autoConnect) {
            this.connectFromButton(false);
        }

    }

    @FXML
    private void handleConnectionPaneMouseExited(MouseEvent event) {
        this.application.getStage().getScene().setCursor(Cursor.DEFAULT);
                if(!currentSidePane.equals(SidePane.CONNECTION)){
               this.connectoinBackgroundImageView.setImage(Util.getImageIconFX(baseImageUrl + "/buttons/button-connect-idle.png"));
        }
    }

    @FXML
    private void handleConnectionPaneMouseEntered(MouseEvent event) {
      this.application.getStage().getScene().setCursor(Cursor.HAND);
              if(!currentSidePane.equals(SidePane.CONNECTION)){
               this.connectoinBackgroundImageView.setImage(Util.getImageIconFX(baseImageUrl + "/buttons/button-connect-hover.png"));
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
            boolean connectionStatus = getController().getCurrentConnectionState() == ConnectionState.Connected ;
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
               this.serverListBackgroundImage.setImage(Util.getImageIconFX(baseImageUrl + "/buttons/button-serverlist-idle.png"));
        }
    }

    @FXML
    private void handleServerListPaneMouseEntered(MouseEvent event) {
        this.application.getStage().getScene().setCursor(Cursor.HAND);
        if(!currentSidePane.equals(SidePane.SERVERLIST)){
               this.serverListBackgroundImage.setImage(Util.getImageIconFX(baseImageUrl + "/buttons/button-serverlist-hover.png"));
        }
    }

    @FXML
    private void handleServerListPaneContext(ContextMenuEvent event) {
    }

       @FXML
    private void handleServerListPaneClicked(MouseEvent event) {

        try {
            Pair<Pane, Object> pair = FxUIManager.SwitchSubview("serverList_subview.fxml");

            this.serverListSubviewController = (ServerListSubviewController) pair.getValue();
            this.serverListSubviewController.setShellfireService((this.shellfireService));
            this.serverListSubviewController.initComponents();
            this.serverListSubviewController.initPremium(isFreeAccount());
            this.serverListSubviewController.setApp(this.application);
            this.serverListSubviewController.setMainFormController(this);
            this.serverListSubviewController.afterInitialization();
            boolean connectionStatus = getController().getCurrentConnectionState() == ConnectionState.Connected ;
            this.serverListSubviewController.updateComponents(connectionStatus);
            contentDetailsPane.getChildren().clear();
            contentDetailsPane.getChildren().setAll(pair.getKey());
            currentSidePane = SidePane.SERVERLIST;
            updateSidePanes(currentSidePane);
        } catch (IOException ex) {
            log.debug("ShellfireVPNMainFormFxmlController:  handleServerListPaneClicked has error " + ex.getMessage());
        }
       
    }
 
    @FXML
    private void handleStreamsPaneMouseExited(MouseEvent event) {
        this.application.getStage().getScene().setCursor(Cursor.DEFAULT);
        if(!currentSidePane.equals(SidePane.TVSTREAM)){
               this.streamsBackgroundImageView.setImage(Util.getImageIconFX(baseImageUrl + "/buttons/button-usa-idle.png"));
        }
    }

    @FXML
    private void handleStreamsPaneMouseEntered(MouseEvent event) {
        this.application.getStage().getScene().setCursor(Cursor.HAND);
        if(!currentSidePane.equals(SidePane.TVSTREAM)){
               this.streamsBackgroundImageView.setImage(Util.getImageIconFX(baseImageUrl + "/buttons/button-usa-hover.png"));
        }
    }

    @FXML
    private void handleStreamsPaneContext(ContextMenuEvent event) {
    }

    @FXML
    private void handleStreamsPaneClicked(MouseEvent event) {
        try {
            Pair<Pane, Object> pair = FxUIManager.SwitchSubview("tvStreams_subview.fxml");
            contentDetailsPane.getChildren().clear();
            contentDetailsPane.getChildren().setAll(pair.getKey());
            //this.tvStreasSubviewController = (TvStreasSubviewController) pair.getValue();
            //this.tvStreasSubviewController.initializeContents();
            currentSidePane = SidePane.TVSTREAM;
            updateSidePanes(currentSidePane);
        } catch (IOException ex) {
            log.debug("ShellfireVPNMainFormFxmlController:  handleStreamsPaneClicked has error " + ex.getMessage());
        }
    }

    private void updateSidePanes(SidePane pane ){
        if(pane.equals(SidePane.CONNECTION)){
            this.streamsBackgroundImageView.setImage(Util.getImageIconFX(baseImageUrl + "/buttons/button-usa-idle.png"));
            this.serverListBackgroundImage.setImage(Util.getImageIconFX(baseImageUrl + "/buttons/button-serverlist-idle.png"));
            this.connectoinBackgroundImageView.setImage(Util.getImageIconFX(baseImageUrl + "/buttons/button-connect-active.png"));
        } else if (pane.equals(SidePane.SERVERLIST)){
            this.streamsBackgroundImageView.setImage(Util.getImageIconFX(baseImageUrl + "/buttons/button-usa-idle.png"));
            this.serverListBackgroundImage.setImage(Util.getImageIconFX(baseImageUrl + "/buttons/button-serverlist-active.png"));
            this.connectoinBackgroundImageView.setImage(Util.getImageIconFX(baseImageUrl + "/buttons/button-connect-idle.png"));            
        }else if(pane.equals(SidePane.TVSTREAM)){
            this.streamsBackgroundImageView.setImage(Util.getImageIconFX(baseImageUrl + "/buttons/button-usa-active.png"));
            this.serverListBackgroundImage.setImage(Util.getImageIconFX(baseImageUrl + "/buttons/button-serverlist-idle.png"));
            this.connectoinBackgroundImageView.setImage(Util.getImageIconFX(baseImageUrl + "/buttons/button-connect-idle.png"));            
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
        //this.application.getStage().
        this.application.getStage().toBack();
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
                Platform.runLater(()->{this.setStateDisconnected();});
                break;
            case Connecting:
                Platform.runLater(() ->{ this.setStateConnecting();});
                break;
            case Connected:
                this.setStateConnected();
                break;
        }

    }

    public void connectFromButton(final boolean failIfPremiumServerForFreeUser) {
        log.debug("connectFromButton(" + failIfPremiumServerForFreeUser + ")");
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
                            if (failIfPremiumServerForFreeUser) {
                                setNormalCursor();
                                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                                alert.setHeaderText(i18n.tr("Premium server selected"));
                                alert.setContentText(i18n.tr("Dieser Server steht nur für Shellfire VPN Premium Kunden zur Verfügung\n\nWeitere Informationen zu Shellfire VPN Premium anzeigen?"));
                                Optional<ButtonType> result = alert.showAndWait();

                                if ((result.isPresent()) && (result.get() == ButtonType.OK)) {

                                    //showNagScreenWithoutTimer();
                                }

                                return;
                            } else {
                                server = this.serverListSubviewController.getRandomFreeServer();
                                setSelectedServer(server);
                            }

                        }

                        delayedConnect(server, this.serverListSubviewController.getSelectedProtocol(), Reason.ConnectButtonPressed);
                    } else if (isPremiumAccount()) {
                        log.debug("ServerList Subview controller  has the object " + serverListSubviewController.toString());
                        Server server = this.serverListSubviewController.getSelectedServer();

                        if (server.getServerType() == ServerType.PremiumPlus) {
                            if (failIfPremiumServerForFreeUser) {
                                setNormalCursor();

                                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                                alert.setHeaderText(i18n.tr("PremiumPlus server selected"));
                                alert.setContentText(i18n.tr("Dieser Server steht nur für Shellfire VPN PremiumPlus Kunden zur Verfügung\n\nWeitere Informationen zu Shellfire VPN PremiumPlus anzeigen?"));
                                Optional<ButtonType> result = alert.showAndWait();

                                if ((result.isPresent()) && (result.get() == ButtonType.OK)) {

                                    showNagScreenWithoutTimer();
                                }

                                return;
                            } else {
                                server = this.serverListSubviewController.getRandomPremiumServer();
                                setSelectedServer(server);
                            }

                        }

                        controller.connect(this.serverListSubviewController.getSelectedServer(), this.serverListSubviewController.getSelectedProtocol(), Reason.ConnectButtonPressed);
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
                // Load the fxml file and create a new stage for the popup dialog.
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(LoginForms.class.getResource("/fxml/ProgressDialog.fxml"));
                AnchorPane page = (AnchorPane) loader.load();
                connectProgressDialog = (ProgressDialogController)loader.getController();
                connectProgressDialog.setDialogText("Connecting ...");
                connectProgressDialog.getProgressBar().setProgress(ProgressBar.INDETERMINATE_PROGRESS);
                connectProgressDialog.addInfo("");
                connectProgressDialog.addBottomText("");
                
                connectProgressDialog.setOptionCallback(task);
                connectProgressDialog.getLeftButton().setOnAction(new EventHandler<javafx.event.ActionEvent>() {

                    @Override
                    public void handle(javafx.event.ActionEvent event) {
                        controller.disconnect(Reason.DisconnectButtonPressed);
                        task.cancel(true);
                        log.debug("showConnectProgress: Cancel button clicked");
                    }
                });
                popUpStage = new Stage();
                popUpStage.initStyle(StageStyle.UNDECORATED);
                popUpStage.setTitle("Connecting");
                popUpStage.initModality(Modality.WINDOW_MODAL);
                popUpStage.initOwner(this.application.getStage());
                Scene scene = new Scene(page);
                popUpStage.setScene(scene);
                connectProgressDialog = loader.getController();            
                // unbind any previous progress bar
                connectProgressDialog.getProgressBar().progressProperty().unbind();
            
                connectProgressDialog.setVisible(true);
            } catch (IOException ex) {
                log.debug("connectFromButton. Error is " + ex.getMessage());
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
        this.serverListSubviewController.setConnetImage1Disable(false);
        this.connectionStatusValue.setText(i18n.tr("Not connected"));
        mySetIconImage("/icons/sfvpn2-disconnected-big.png");
        Platform.runLater(()->{this.globeConnectionImageView.setImage(this.iconIdleSmall);});
        this.connectionSubviewController.getStatusConnectionImageView().setImage(this.iconEcncryptionInactive);
        this.connectionSubviewController.getConnectImageView().setImage(this.buttonConnect);
        this.serverListSubviewController.getConnectImage1().setImage(this.buttonConnect);
        log.debug("ShellfireMainForm: In setStateDisconnected method ");

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
                    connectFromButton(true);
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
        if (showMessage) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(message);
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            //alert.getDialogPane().getChildren().stream().filter(node -> node instanceof Label).forEach(node -> ((Label)node).setMinWidth(Region.USE_PREF_SIZE));
            alert.showAndWait();
            if (this.trayIcon != null) {
                this.trayIcon.setImage(this.iconDisconnectedAwt);
            }
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
        
        if (null != mapEncryptionSubviewController) { // test if the controller has been initialized before doing any work.
            if (!this.mapEncryptionSubviewController.getShowOwnPosition().isSelected()) {
                this.mapEncryptionSubviewController.getShowOwnPosition().setDisable(true);
            }
        }
        this.connectionStatusValue.setText(i18n.tr("Connection is being processed..."));
        Platform.runLater(()->{this.globeConnectionImageView.setImage(Util.getImageIconFX(baseImageUrl + "/icons/small-globe-connecting.png"));});
        mySetIconImage("/icons/sfvpn2-connecting-big.png");

        if (this.trayIcon != null) {
            this.trayIcon.setImage(this.iconConnecting);
        }
        this.setWaitCursor();
        //TODO find FX equivalant variable
        //this.jConnectionStateIcon.setIcon(new ImageIcon(this.iconConnectingSmall));

        popupConnectItem.setLabel(i18n.tr("Connecting..."));
        popupConnectItem.setEnabled(false);
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
        log.debug("showConnectProgress: Entrance of method");
        if(popUpStage != null){
            popUpStage.show();
        } 
    }

    public void mySetIconImage(String imagePath) {
        log.debug("mySetIconImage: the icon Image  path is " + imagePath);
        Platform.runLater(()->{
            this.application.getStage().getIcons().clear();
            this.application.getStage().getIcons().add(new Image(imagePath));});
    }

    private void initTray() {
        if (!Util.isWindows()) {
            this.hideImageView.setVisible(false);

        }

        if (SystemTray.isSupported()) {

            SystemTray tray = SystemTray.getSystemTray();
            Image image2 = new Image("/icons/sfvpn2-idle-big.png");
            BufferedImage image = SwingFXUtils.fromFXImage(image2, null);
            ActionListener exitListener = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    // TODO
                    //exitHandler();
                }
            };

            popup = new PopupMenu();
            MenuItem defaultItem = new MenuItem(i18n.tr("Exit"));
            defaultItem.addActionListener(exitListener);

            ActionListener nagListener = new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    //TODO
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            showNagScreenWithoutTimer();
                        }
                    });

                }
            };

            MenuItem nagItem = new MenuItem(i18n.tr("Shellfire VPN premium infos"));
            nagItem.addActionListener(nagListener);

            ActionListener helpListener = (ActionEvent e) -> {
                openHelp();
            };

            MenuItem helpItem = new MenuItem(i18n.tr("Help"));
            helpItem.addActionListener(helpListener);

            ActionListener popupConnectListener = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    // TODO
                    //connectFromButton(false);
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
                Platform.runLater(() -> {
                    mouseClickedFX();
                });

                if (!Util.isWindows()) {
                    com.apple.eawt.Application app = com.apple.eawt.Application.getApplication();
                    app.requestForeground(true);
                }
            };

            MenuItem openItem = new MenuItem(i18n.tr("Shellfire VPN to front"));
            openItem.addActionListener(openListener);
            popup = new PopupMenu();
            popup.add(openItem);
            popup.add(popupConnectItem);
            popup.add(statusItem);
            popup.add(helpItem);
            popup.add(nagItem);
            popup.add(defaultItem);

            ActionListener actionListener = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    setVisible(true);
                    toFront();
                    //setState(Frame.NORMAL);
                }
            };

            MouseListener mouseListener = new MouseListener() {

                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        Platform.runLater(() -> {
                            mouseClickedFX();
                        });
                        //TODO
                        //setState(Frame.NORMAL);

                        if (!Util.isWindows()) {
                            com.apple.eawt.Application app = com.apple.eawt.Application.getApplication();
                            app.requestForeground(true);
                        }
                    };

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

            //TODO
            startNagScreenTimer();
            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                System.err.println("TrayIcon could not be added.");
            }

            //TODO
            //pack();
        }
    }

    public void mouseClickedFX() {

        

        if(this.application.getStage().isIconified())
            this.application.getStage().setIconified(false);
        else {
            this.application.getStage().toFront();
        }
        if (!Util.isWindows()) {
            com.apple.eawt.Application app = com.apple.eawt.Application.getApplication();
            app.requestForeground(true);
        }

    }

    private void initShortCuts() {
        EventQueue ev = Toolkit.getDefaultToolkit().getSystemEventQueue();

        ev.push(new EventQueue() {

            protected void dispatchEvent(AWTEvent event) {
                if (event instanceof KeyEvent) {

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
        this.typedStrings.append(c);
        if (typedStrings.toString().toLowerCase().endsWith("showconsole")) {
            //TODO
            //this.initConsole();
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
        Platform.runLater(() -> {this.hideConnectProgress();});
        
        Platform.runLater(()->{this.connectionStatusValue.setText(i18n.tr("Connected"));;});

        //TODO check if image not already loaddd
        mySetIconImage("/icons/sfvpn2-connected-big.png");
        this.connectionSubviewController.getStatusConnectionImageView().setImage(this.iconEcncryptionActive);
        this.connectionSubviewController.getConnectImageView().setImage(this.buttonDisconnect);
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
    }
    
    private void showStatusUrlIfEnabled() {
		if (showStatusUrl())
			Util.openUrl(shellfireService.getUrlSuccesfulConnect());

	}

	private boolean showStatusUrl() {
            Preferences prefs = this.getPreferences();

            boolean showStatus = prefs.getBoolean(LoginController.REG_SHOWSTATUSURL, false);
            return showStatus;
	}
    	
        private Preferences getPreferences() {
            if (preferences == null) {
                    preferences = Preferences.userNodeForPackage(this.getClass());
            }
            return preferences;
	}
        
    private void hideConnectProgress() {
        if (this.popUpStage != null) {
            this.popUpStage.close();
        }
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
            //this.validUntilValue.setManaged(false);

            this.validUntilLabel.setVisible(false);
            //this.validUntilLabel.setManaged(false);
        } else {

            this.validUntilValue.setDisable(false);
            this.validUntilLabel.setDisable(false);

            SimpleDateFormat df = new SimpleDateFormat(i18n.tr("d/MM/yyyy"), VpnI18N.getLanguage().getLocale());
            String date = df.format(vpn.getPremiumUntil());

            this.validUntilValue.setText(date);
        }
    }

    public void displayMessage(String message) {
        log.debug("ShellFireMainController: " + message);
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
        Parent root;
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
            log.debug("ShellfireVPNMainFormFxmlController:  showSettingsDialog has error " + ex.getMessage());
        }
    }

    @Action
    public void openHelp() {
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance().getContext()
                .getResourceMap(ShellfireVPNMainFormFxmlController.class);

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
            ActionListener premiumInfoClicked = (ActionEvent e) -> {
                showNagScreenWithoutTimer();
            };

            if (this.shellfireService.getVpn().getAccountType() == ServerType.Free) {
                List<TrayMessage> trayMessages = this.shellfireService.getTrayMessages();

                trayMessages.forEach((msg) -> {
                    messages.add(new VpnTrayMessage(msg.getHeader(), msg.getText(), msg.getButtontext(), premiumInfoClicked));
                });
            }
        } else {
            messages.add(new VpnTrayMessage(i18n.tr("Not connected"), i18n.tr("You are not connected to Shellfire VPN.")));
        }

        if (messages.size() > 0) {
            Random generator = new Random((new Date()).getTime());
            int num = generator.nextInt(messages.size());
            VpnTrayMessage msgToShow = messages.get(num);
            msgToShow.run();
        }

    }

    private void showNagScreenWithoutTimer() {
        log.debug("ShellfireVPNMainFormFxmlController:  showNagScreenWithoutTimer method entered");
        WebService service = WebService.getInstance();
        Util.openUrl(service.getUrlPremiumInfo());
        setNormalCursor();
    }

    private boolean isPremiumAccount() {
        return this.shellfireService.getVpn().getAccountType() == ServerType.Premium;
    }

    public void setSelectedServer(Server server) {
        log.debug("setSelectedServer(" + server + ")");
        int num = this.shellfireService.getServerList().getServerNumberByServer(server);
        this.serverListSubviewController.setSelectedServer(num);

    }

    private void delayedConnect(Server selectedServer, VpnProtocol protocol, Reason reason) {
        popupConnectItem.setLabel(i18n.tr("Connecting..."));
        popupConnectItem.setEnabled(false);
        showNagScreenWithoutTimer();
    }

    public void prepareServerController(){
       try {
        Pair<Pane, Object> pair = FxUIManager.SwitchSubview("serverList_subview.fxml");

            this.serverListSubviewController = (ServerListSubviewController) pair.getValue();
            this.serverListSubviewController.setShellfireService((this.shellfireService));
            this.serverListSubviewController.initComponents();
            this.serverListSubviewController.initPremium(isFreeAccount());
            this.serverListSubviewController.setApp(this.application);
             } catch (IOException ex) {
            log.debug("ShellfireVPNMainFormFxmlController:  prepareServerController has error " + ex.getMessage());
        }
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
        } catch (IOException ex) {
            log.debug("ShellfireVPNMainFormFxmlController:  prepareControllers has error " + ex.getMessage());
        }
        
        try {
            Pair<Pane, Object> pair = FxUIManager.SwitchSubview("tvStreams_subview.fxml");
            this.tvStreasSubviewController = (TvStreasSubviewController) pair.getValue();
            //this.tvStreasSubviewController.initializeContents();
        } catch (IOException ex) {
            log.debug("ShellfireVPNMainFormFxmlController:  handleStreamsPaneClicked has error " + ex.getMessage());
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

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText(i18n.tr("Currently Connected"));
        alert.setContentText(i18n.tr("Disconnect and close Shellfire VPN?"));
        Optional<ButtonType> result = alert.showAndWait();

        if ((result.isPresent()) && (result.get() == ButtonType.OK)) {
            this.controller.disconnect(Reason.ApplicationExit);
            enableSystemProxyIfProxyConfig();
            Platform.exit();
            System.exit(0);
        }
    }
    
    	private void showTrayMessageWithoutCallback(String header, String content) {
		// This will run only on windows systems so it is ok to leave just this test
		if (Util.isWindows()) {
			trayIcon.displayMessage(header, content, MessageType.INFO);
		} 

	}

}
    enum SidePane 
    { 
        CONNECTION, SERVERLIST, TVSTREAM; 
    } 