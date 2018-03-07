package de.shellfire.vpn.gui.controller;

import de.shellfire.vpn.Storage;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.client.ConnectionState;
import de.shellfire.vpn.client.ConnectionStateChangedEvent;
import de.shellfire.vpn.client.ConnectionStateListener;
import de.shellfire.vpn.client.Controller;
import de.shellfire.vpn.exception.VpnException;
import de.shellfire.vpn.gui.LoginForms;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.types.Reason;
import de.shellfire.vpn.types.Server;
import de.shellfire.vpn.types.ServerType;
import de.shellfire.vpn.webservice.Vpn;
import de.shellfire.vpn.webservice.WebService;
import java.awt.AWTEvent;
import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Date;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.swing.Timer;
import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.LocaleChangeEvent;
import org.xnap.commons.i18n.LocaleChangeListener;

public class ShellfireVPNMainFormFxmlController extends AnchorPane implements Initializable, LocaleChangeListener, ConnectionStateListener {

    private static final I18n I18N = VpnI18N.getI18n();

    private LoginForms application;
    private static final Logger log = Util.getLogger(ShellfireVPNMainFormFxmlController.class.getCanonicalName());
    private static I18n i18n = VpnI18N.getI18n();
    private Controller controller;
    private static WebService shellfireService;
    private MenuItem popupConnectItem;
    private PopupMenu popup;
    private TrayIcon trayIcon;
    private StringBuffer typedStrings = new StringBuffer();
    private ProgressDialogController connectProgressDialog;
    private MapEncryptionSubviewController mapEncryptionSubviewController;
    private java.awt.Image iconConnecting;
    private Date connectedSince;
    private Image iconEcncryptionActive ; 
    private Image iconEcncryptionInactive ;
    private Image iconConnectedSmall ; 
    private java.awt.Image iconConnected; 
    //ConnectionSubviewController connectionSubviewController  = null ; 
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
    private Pane mapPane;
    @FXML
    private ImageView mapBackgroundImageView;
    @FXML
    private Label mapHeaderLabel;
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
    @FXML
    private Label mapFooterLabel;

    // Access to embedded controller and variables in subviews
    @FXML
    private Parent connectionSubview;

    @FXML
    private ConnectionSubviewController connectionSubviewController;

    public ShellfireVPNMainFormFxmlController() {
        log.debug("No argumenent controller of shellfire has been called");
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
        String baseImageUrl = "src/main/resources";
        // initializing images of the form
        this.connectoinBackgroundImageView.setImage(Util.getImageIconFX(baseImageUrl + "/buttons/button-connect-idle.png"));
        this.serverListBackgroundImage.setImage(Util.getImageIconFX(baseImageUrl + "/buttons/button-serverlist-idle.png"));
        this.mapBackgroundImageView.setImage(Util.getImageIconFX(baseImageUrl + "/buttons/button-map-idle.png"));
        this.streamsBackgroundImageView.setImage(Util.getImageIconFX(baseImageUrl + "/buttons/button-usa-idle.png"));
        this.globeConnectionImageView.setImage(Util.getImageIconFX(baseImageUrl + "/icons/small-globe-disconnected.png"));
        
        this.iconEcncryptionActive  = Util.getImageIconFX(baseImageUrl + "/icons/status-encrypted-width"+size+".gif");
        this.iconEcncryptionInactive = Util.getImageIconFX(baseImageUrl + "/icons/status-unencrypted-width"+size+".gif");
        // initializing text of the form 
        this.connectionStatusLabel.setText(i18n.tr("Verbindungsstatus"));
        this.connectedSinceLabel.setText(i18n.tr("Verbunden seit:"));
        this.onlineIpLabel.setText(i18n.tr("Online IP"));
        this.vpnIdLabel.setText(i18n.tr("VPN Id:"));
        //this.vpnTypeLabel.setText(i18n.tr("VPN Typ:"));
        //this.validUntilLabel.setText(i18n.tr("Gültig bis:"));

        this.connectionHeaderLabel.setText(i18n.tr("Verbindung"));
        this.connectionFooter.setText(i18n.tr("Jetzt zu Shellfire VPN verbinden"));
        this.serverListHeaderLabel.setText(i18n.tr("Server Liste"));
        this.serverListFooterLabel.setText(i18n.tr("Liste aller VPN Server anzeigen"));
        this.mapHeaderLabel.setText(i18n.tr("Karte"));
        this.mapFooterLabel.setText(i18n.tr("Zeigt Verschlüsselungsroute"));
        this.streamsHeaderLabel.setText(i18n.tr("Streams aus den USA"));
        this.streamsFooterLabel.setText(i18n.tr("Liste amerikanischer TV Streams"));

        log.debug(connectionSubviewController.toString());
        log.debug(connectionSubviewController.displayCreationMessage("Object refreence properly created"));
        this.iconConnected = Util.getImageIcon("/icons/sfvpn2-connected-big.png").getImage();
        this.iconConnectedSmall = Util.getImageIconFX(baseImageUrl + "/icons/small-globe-connected.png");
        this.iconConnecting = Util.getImageIcon("/icons/sfvpn2-connecting-big.png").getImage();
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
        //TODO uncomment initPrimium and add corresponding logic
        connectionSubviewController.initPremium(isFreeAccount());
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
        /*
		if (ProxyConfig.isProxyEnabled()) {
			this.setSelectedProtocol(VpnProtocol.TCP);
			this.jRadioUdp.setEnabled(false);
		} else {
			VpnProtocol selectedProtocol = vpn.getProtocol();
			this.setSelectedProtocol(selectedProtocol);
		}

		Server server = vpn.getServer();
		int row = this.serverListTableModel.getRowForServer(server);

		if (row != -1) {
			//this.jServerListTable.addRowSelectionInterval(row, row);
		}

		if (autoConnect) {
			//this.connectFromButton(false);
		}**/

    }

    @FXML
    private void handleConnectionPaneMouseExited(MouseEvent event) {
        this.application.getStage().getScene().setCursor(Cursor.DEFAULT);
    }

    @FXML
    private void handleConnectionPaneMouseEntered(MouseEvent event) {
        this.application.getStage().getScene().setCursor(Cursor.HAND);
    }

    @FXML
    private void handleConnectionPaneContext(ContextMenuEvent event) {
    }

    @FXML
    private void handleConnectionPaneClicked(MouseEvent event) {
    }

    @FXML
    private void handleServerListPaneMouseExited(MouseEvent event) {
        this.application.getStage().getScene().setCursor(Cursor.DEFAULT);
    }

    @FXML
    private void handleServerListPaneMouseEntered(MouseEvent event) {
        this.application.getStage().getScene().setCursor(Cursor.HAND);
    }

    @FXML
    private void handleServerListPaneContext(ContextMenuEvent event) {
    }

    @FXML
    private void handleServerListPaneClicked(MouseEvent event) {
    }

    @FXML
    private void handleMapPaneExited(MouseEvent event) {
        this.application.getStage().getScene().setCursor(Cursor.DEFAULT);
    }

    @FXML
    private void handleMapPaneEntered(MouseEvent event) {
        this.application.getStage().getScene().setCursor(Cursor.HAND);
    }

    @FXML
    private void handleMapPaneContext(ContextMenuEvent event) {
    }

    @FXML
    private void handleMapPaneClicked(MouseEvent event) {
    }

    @FXML
    private void handleStreamsPaneMouseExited(MouseEvent event) {
        this.application.getStage().getScene().setCursor(Cursor.DEFAULT);
    }

    @FXML
    private void handleStreamsPaneMouseEntered(MouseEvent event) {
        this.application.getStage().getScene().setCursor(Cursor.HAND);
    }

    @FXML
    private void handleStreamsPaneContext(ContextMenuEvent event) {
    }

    @FXML
    private void handleStreamsPaneClicked(MouseEvent event) {
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
        Platform.exit();
    }

    private void handleConnectImageViewMouseExited(MouseEvent event) {
        this.application.getStage().getScene().setCursor(Cursor.DEFAULT);
    }

    private void handleConnectImageViewMouseEntered(MouseEvent event) {
        this.application.getStage().getScene().setCursor(Cursor.HAND);
    }

    private void handleProductKeyImageViewMouseExited(MouseEvent event) {
        this.application.getStage().getScene().setCursor(Cursor.DEFAULT);
    }

    private void handleProductKeyImageViewMouseEntered(MouseEvent event) {
        this.application.getStage().getScene().setCursor(Cursor.HAND);
    }

    private void handlePremiumInfoImageViewMouseExited(MouseEvent event) {
        this.application.getStage().getScene().setCursor(Cursor.DEFAULT);
    }

    private void handlePremiumInfoImageViewMouseEntered(MouseEvent event) {
        this.application.getStage().getScene().setCursor(Cursor.HAND);
    }

    private void initController() {
        if (this.controller == null) {
            this.controller = Controller.getInstanceFX(this, this.shellfireService);
            this.controller.registerConnectionStateListener(this);
        }
    }

    @Override
    public void localeChanged(LocaleChangeEvent lce) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void connectionStateChanged(ConnectionStateChangedEvent e) {
        initController();

        ConnectionState state = e.getConnectionState();
        log.debug("connectionStateChanged " + state + ", reason=" + e.getReason());
        switch (state) {
            case Disconnected:
                this.setStateDisconnected();
                break;
            case Connecting:
                this.setStateConnecting();
                break;
            case Connected:
                this.setStateConnected();
                break;
        }

    }

    public void connectFromButton(final boolean failIfPremiumServerForFreeUser) {

    }

    private boolean isFreeAccount() {
        return this.shellfireService.getVpn().getAccountType() == ServerType.Free;
    }

    private void setStateDisconnected() {

    }

    private void setStateConnecting() {
        this.showConnectProgress();
        //this.jConnectButtonLabel.setIcon(new ImageIcon(buttonConnect));
        //this.jConnectButtonLabel1.setIcon(new ImageIcon(buttonConnect));
        this.connectionSubviewController.getConnectImageView().setDisable(true);
        //TODO implement this in the other subview controller
        //this.jConnectButtonLabel1.setEnabled(false);

        if (null != mapEncryptionSubviewController){ // test if the controller has been initialized before doing any work.
            if(!this.mapEncryptionSubviewController.getShowOwnPosition().isSelected())
                this.mapEncryptionSubviewController.getShowOwnPosition().setDisable(true);
        }

        this.connectionStatusLabel.setText(i18n.tr("Verbindung wird hergestellt..."));
        mySetIconImage("/icons/sfvpn2-connecting-big.png");

        if (this.trayIcon != null) {
            this.trayIcon.setImage(this.iconConnecting);
        }
        this.setWaitCursor();
        //TODO find FX equivalant variable
        //this.jConnectionStateIcon.setIcon(new ImageIcon(this.iconConnectingSmall));

        popupConnectItem.setLabel(i18n.tr("Verbinde..."));
        popupConnectItem.setEnabled(false);
        /*jServerListTable.setEnabled(false);
        jScrollPane.getViewport().setBackground(Color.lightGray);
        jRadioUdp.setEnabled(false);
        jRadioTcp.setEnabled(false);*/

    }

    /**
     * Displays the progress of execution as a task is being ran
     *
     * @return no return value
     */
    private void showConnectProgress() {
        if (this.connectProgressDialog == null) {
            // create an instance of progress dialog 
            try {
                // Load the fxml file and create a new stage for the popup dialog.
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(LoginForms.class.getResource("/fxml/ProgressDialog.fxml"));
                AnchorPane page = (AnchorPane) loader.load();

                // Create the dialog Stage.
                Stage dialogStage = new Stage();
                dialogStage.setTitle("Connecting");
                dialogStage.initModality(Modality.WINDOW_MODAL);
                dialogStage.initOwner(this.application.getStage());
                Scene scene = new Scene(page);
                dialogStage.setScene(scene);

                // Set the dialog into the controller.
                connectProgressDialog = loader.getController();
                connectProgressDialog.setOption(2, i18n.tr("abbrechen"));
                
                Task<Void> task = new Task<Void>(){
                    @Override
                    protected Void call() throws Exception {
                        controller.disconnect(Reason.AbortButtonPressed);
                        
                        // TODO investgite if it should be in setOnsucceed of task, 
                        // if it's correct at it's place
                        setNormalCursor();
                        return null;
                    }
                    
                };
                // unbind any previous progress bar
                connectProgressDialog.getProgressBar().progressProperty().unbind();
                connectProgressDialog.getProgressBar().progressProperty().bind(task.progressProperty());
            } catch (IOException ex) {
                log.debug(ex.getMessage());
                //return false;
            }
            
        }

        connectProgressDialog.setVisible(true);

    }

    public void mySetIconImage(String imagePath) {
        this.application.getStage().getIcons().add(new Image(imagePath));
    }
  
    private void initTray() {
        if (!Util.isWindows()) {
            this.hideImageView.setVisible(false);

        }

        if (SystemTray.isSupported()) {

            SystemTray tray = SystemTray.getSystemTray();
            // Image image2 = new Image("src/main/resources/icons/sfvpn2-idle-big.png");
            Image image2 = new Image("/icons/sfvpn2-idle-big.png");
            BufferedImage image = SwingFXUtils.fromFXImage(image2, null);
            ActionListener exitListener = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    // TODO
                    //exitHandler();
                }
            };

            popup = new PopupMenu();
            MenuItem defaultItem = new MenuItem(i18n.tr("Beenden"));
            defaultItem.addActionListener(exitListener);

            ActionListener nagListener = new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    //TODO
                    //showNagScreenWithoutTimer();
                }
            };

            MenuItem nagItem = new MenuItem(i18n.tr("Shellfire VPN Premium Infos"));
            nagItem.addActionListener(nagListener);

            ActionListener helpListener = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    //TODO
                    //openHelp();
                }
            };

            MenuItem helpItem = new MenuItem(i18n.tr("Hilfe"));
            helpItem.addActionListener(helpListener);

            ActionListener popupConnectListener = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    // TODO
                    //connectFromButton(false);
                }
            };

            popupConnectItem = new MenuItem(i18n.tr("Verbinden"));
            popupConnectItem.addActionListener(popupConnectListener);

            ActionListener statusListener = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    Util.openUrl(shellfireService.getUrlSuccesfulConnect());
                }
            };

            MenuItem statusItem = new MenuItem(i18n.tr("Zeige VPN Status im Browser"));
            statusItem.addActionListener(statusListener);

            ActionListener openListener = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    setVisible(true);
                    toFront();
                    //TODO
                    //setState(Frame.NORMAL);

                    if (!Util.isWindows()) {
                        com.apple.eawt.Application app = com.apple.eawt.Application.getApplication();
                        app.requestForeground(true);
                    }
                }
            };

            MenuItem openItem = new MenuItem(i18n.tr("Shellfire VPN in den Vordergrund"));
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
                    //TODO
                    //setState(Frame.NORMAL);
                }
            };

            MouseListener mouseListener = new MouseListener() {
                //@Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        setVisible(true);
                        toFront();
                        //TODO
                        //setState(Frame.NORMAL);

                        if (!Util.isWindows()) {
                            com.apple.eawt.Application app = com.apple.eawt.Application.getApplication();
                            app.requestForeground(true);
                        }
                    }

                }

                /*
				public void mouseReleased(MouseEvent e) {
				}

				public void mouseEntered(MouseEvent e) {
				}

				public void mouseExited(MouseEvent e) {
				}

				public void mousePressed(MouseEvent e) {
				}*/
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public void mousePressed(java.awt.event.MouseEvent e) {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public void mouseReleased(java.awt.event.MouseEvent e) {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

            };

            trayIcon = new TrayIcon(image, "Shellfire VPN", popup);
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(actionListener);
            trayIcon.addMouseListener(mouseListener);

            //TODO
            //startNagScreenTimer();
            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                System.err.println("TrayIcon could not be added.");
            }

            //TODO
            //pack();
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

    public Server getSelectedServer() {
        //TODO check equivalence in javafx
        /* 
        int serverNum = this.jServerListTable.getSelectedRow();
        Server server = this.shellfireService.getServerList().getServer(serverNum);
        log.debug("getSelectedServer() - returning: " + server);
        return server;
        */
        return null;
    }

    private void setNormalCursor() {
        this.application.getStage().getScene().setCursor(Cursor.DEFAULT);
    }
    
    private void setWaitCursor() {
	this.application.getStage().getScene().setCursor(Cursor.WAIT);
    }
    
   	private void setStateConnected() {
		this.hideConnectProgress();
                // TODO - buttonlables already loaded from scenebuilder (check and verify)
		//this.jConnectButtonLabel.setIcon(new ImageIcon(buttonDisconnect));
		//this.jConnectButtonLabel1.setIcon(new ImageIcon(buttonDisconnect));
		//this.jConnectButtonLabel.setEnabled(true);
		//this.jConnectButtonLabel1.setEnabled(true);

		if (!this.mapEncryptionSubviewController.getShowOwnPosition().isSelected())
			this.mapEncryptionSubviewController.getShowOwnPosition().setDisable(true);

		this.connectionStatusLabel.setText(i18n.tr("Verbunden"));
		
                //TODO check if image not already loaddd
		mySetIconImage("/icons/sfvpn2-connected-big.png");
		this.connectionSubviewController.getStatusConnectionImageView().setImage(this.iconEcncryptionActive);
		
		if (this.trayIcon != null) {
			this.trayIcon.setImage(this.iconConnected);
		}

		this.setNormalCursor();
		this.globeConnectionImageView.setImage(this.iconConnectedSmall);
		

		this.startConnectedSinceTimer();
                
                //TODO
                /*
		this.updateOnlineHost();

		this.mapController.updateMap();

		popupConnectItem.setLabel(i18n.tr("Verbindung trennen"));
		popupConnectItem.setEnabled(true);

		jServerListTable.setEnabled(false);
		jScrollPane.getViewport().setBackground(Color.lightGray);
		jRadioUdp.setEnabled(false);
		jRadioTcp.setEnabled(false);

		showTrayMessageWithoutCallback(i18n.tr("Verbindung Erfolgreich"),
				i18n.tr("Du bist jetzt mit Shellfire VPN verbunden. Deine Internet-Verbindung ist verschlüsselt."));

		showStatusUrlIfEnabled();

		disableSystemProxyIfProxyConfig();
                */
	}
    
   	private void hideConnectProgress() {
		if (this.connectProgressDialog != null)
			this.connectProgressDialog.setDisable(true);
	}
        	private void startConnectedSinceTimer() {
		int delay = 1000; // milliseconds
		connectedSince = new Date();

		ActionListener taskPerformer = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				//updateConnectedSince();
			}
		};
                //TODO
		/*this.currentConnectedSinceTimer = new Timer(delay, taskPerformer);
		this.currentConnectedSinceTimer.setRepeats(true);
		this.currentConnectedSinceTimer.start();*/
	}

}
