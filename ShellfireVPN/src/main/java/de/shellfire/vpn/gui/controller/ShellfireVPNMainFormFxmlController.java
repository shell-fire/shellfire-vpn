package de.shellfire.vpn.gui.controller;

import de.shellfire.vpn.Storage;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.client.Client;
import de.shellfire.vpn.client.ConnectionState;
import de.shellfire.vpn.client.ConnectionStateChangedEvent;
import de.shellfire.vpn.client.ConnectionStateListener;
import de.shellfire.vpn.client.Controller;
import de.shellfire.vpn.exception.VpnException;
import de.shellfire.vpn.gui.LoginForms;
import de.shellfire.vpn.gui.helper.TitiliumFont;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.proxy.ProxyConfig;
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
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
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
    private ImageView connectImageView;
    @FXML
    private ImageView productKeyImageView;
    @FXML
    private ImageView premiumInfoImageView;
    //private Label vpnTypeLabel;
    //private Label validUntilLabel;
    @FXML
    private Label vpnType;
    @FXML
    private Label serverListFooterLabel;
    @FXML
    private Label mapFooterLabel;
    @FXML
    private ImageView statusConnectionImageView;

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
        /*
        mySetIconImage("/icons/sfvpn2-idle-big.png");

        // initializing images of the form
        this.statusConnectionImageView.setId("/icons/status-unencrypted-width" + size + ".gif");
        this.connectoinBackgroundImageView.setImage(Util.getImageIconFX("/buttons/button-connect-idle.png"));
        this.serverListBackgroundImage.setImage(Util.getImageIconFX("/buttons/button-serverlist-idle.png"));
        this.mapBackgroundImageView.setImage(Util.getImageIconFX("/buttons/button-map-idle.png"));
        this.streamsBackgroundImageView.setImage(Util.getImageIconFX("/buttons/button-usa-idle.png"));
        this.globeConnectionImageView.setImage(Util.getImageIconFX("/icons/small-globe-disconnected.png"));
        this.connectImageView.setId("/buttons/button-disconnect-" + langKey + ".gif");
        
         */
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
        this.application.getStage().show();
        /*
        // continue here, cursor
        //CustomLayout.register();
        //this.setFont(TitiliumFont.getFont());
        //this.loadIcons();
        //this.setLookAndFeel();
        //initComponents();
        this.initTray();

        //TODO
        //this.initLayeredPaneSize();
        //this.initContent();
        Storage.register(this);

        this.initShortCuts();
        this.initPremium();
        this.initConnection();

        //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //pack();
        //this.setLocationRelativeTo(null);
        //setVisible(true);
        this.application.getStage().show(); */
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
    }

    @FXML
    private void handleConnectionPaneMouseEntered(MouseEvent event) {
    }

    @FXML
    private void handleConnectionPaneContext(ContextMenuEvent event) {
    }

    @FXML
    private void handleConnectionPaneClicked(MouseEvent event) {
    }

    @FXML
    private void handleServerListPaneMouseExited(MouseEvent event) {
    }

    @FXML
    private void handleServerListPaneMouseEntered(MouseEvent event) {
    }

    @FXML
    private void handleServerListPaneContext(ContextMenuEvent event) {
    }

    @FXML
    private void handleServerListPaneClicked(MouseEvent event) {
    }

    @FXML
    private void handleMapPaneExited(MouseEvent event) {
    }

    @FXML
    private void handleMapPaneEntered(MouseEvent event) {
    }

    @FXML
    private void handleMapPaneContext(ContextMenuEvent event) {
    }

    @FXML
    private void handleMapPaneClicked(MouseEvent event) {
    }

    @FXML
    private void handleStreamsPaneMouseExited(MouseEvent event) {
    }

    @FXML
    private void handleStreamsPaneMouseEntered(MouseEvent event) {
    }

    @FXML
    private void handleStreamsPaneContext(ContextMenuEvent event) {
    }

    @FXML
    private void handleStreamsPaneClicked(MouseEvent event) {
    }

    @FXML
    private void handleHelpImageViewMouseExited(MouseEvent event) {
    }

    @FXML
    private void handleHelpImageViewMouseEntered(MouseEvent event) {
    }

    @FXML
    private void handleHelpImageViewContext(ContextMenuEvent event) {
    }

    @FXML
    private void handleHelpImageViewClicked(MouseEvent event) {
    }

    @FXML
    private void handleSettingsImageViewMouseExited(MouseEvent event) {
    }

    @FXML
    private void handleSettingsImageViewMouseEntered(MouseEvent event) {
    }

    @FXML
    private void handleSettingsImageViewContext(ContextMenuEvent event) {
    }

    @FXML
    private void handleSettingsImageViewClicked(MouseEvent event) {
    }

    @FXML
    private void handleHideImageViewExited(MouseEvent event) {
    }

    @FXML
    private void handleHideImageViewEntered(MouseEvent event) {
    }

    @FXML
    private void handleHideImageViewContext(ContextMenuEvent event) {
    }

    @FXML
    private void handleHideImageViewClicked(MouseEvent event) {
    }

    @FXML
    private void handleMinimizeImageViewExited(MouseEvent event) {
    }

    @FXML
    private void handleMinimizeImageViewEntered(MouseEvent event) {
    }

    @FXML
    private void handleMinimizeImageViewContext(ContextMenuEvent event) {
    }

    @FXML
    private void handleMinimizeImageViewClicked(MouseEvent event) {
    }

    @FXML
    private void handleExitImageViewMouseExited(MouseEvent event) {
    }

    @FXML
    private void handleExitImageViewMouseEntered(MouseEvent event) {
    }

    @FXML
    private void handleExitImageViewContext(ContextMenuEvent event) {
    }

    @FXML
    private void handleExitImageViewClicked(MouseEvent event) {
    }

    @FXML
    private void handleConnectImageViewMouseExited(MouseEvent event) {
    }

    @FXML
    private void handleConnectImageViewMouseEntered(MouseEvent event) {
    }

    @FXML
    private void handleConnectImageViewContext(ContextMenuEvent event) {
    }

    @FXML
    private void handleConnectImageViewClicked(MouseEvent event) {
    }

    @FXML
    private void handleProductKeyImageViewMouseExited(MouseEvent event) {
    }

    @FXML
    private void handleProductKeyImageViewMouseEntered(MouseEvent event) {
    }

    @FXML
    private void handleProductKeyImageViewContext(ContextMenuEvent event) {
    }

    @FXML
    private void handleProductKeyImageViewClicked(MouseEvent event) {
    }

    @FXML
    private void handlePremiumInfoImageViewMouseExited(MouseEvent event) {
    }

    @FXML
    private void handlePremiumInfoImageViewMouseEntered(MouseEvent event) {
    }

    @FXML
    private void handlePremiumInfoImageViewContext(ContextMenuEvent event) {
    }

    @FXML
    private void handlePremiumInfoImageViewClicked(MouseEvent event) {
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
                //this.setStateConnecting();
                break;
            case Connected:
                //this.setStateConnected();
                break;
        }
    }

    private boolean isFreeAccount() {
        return this.shellfireService.getVpn().getAccountType() == ServerType.Free;
    }

    private void setStateDisconnected() {

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
            Image image2 = new Image("src/main/resources/icons/sfvpn2-idle-big.png");
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

    private void initPremium() {
        if (!this.isFreeAccount()) {
            this.premiumInfoImageView.setVisible(false);
            this.connectImageView.setVisible(false);
        }
        this.productKeyImageView.setVisible(false);
        this.productKeyImageView.setVisible(false);
    }

    private void initConnection() {
        new Thread() {
            public void run() {
                controller.getCurrentConnectionState();
            }
        }.start();
    }

}
