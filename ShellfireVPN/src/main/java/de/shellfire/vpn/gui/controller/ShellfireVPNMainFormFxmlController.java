package de.shellfire.vpn.gui.controller;

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
import de.shellfire.vpn.webservice.Vpn;
import de.shellfire.vpn.webservice.WebService;
import java.util.logging.Level;
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

public class ShellfireVPNMainFormFxmlController extends AnchorPane implements Initializable,LocaleChangeListener, ConnectionStateListener {

        private static final I18n I18N = VpnI18N.getI18n();

    private LoginForms application;
  private static final Logger log = Util.getLogger(ShellfireVPNMainFormFxmlController.class.getCanonicalName());
  private static I18n i18n = VpnI18N.getI18n();
  private Controller controller;
private WebService shellfireService;
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
    private Label mapFooterLabelPane;
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
    @FXML
    private Label vpnTypeLabel;
    @FXML
    private Label validUntilLabel;
    @FXML
    private Label validUntilValue;

    public ShellfireVPNMainFormFxmlController() {
    }

  
    public ShellfireVPNMainFormFxmlController(WebService service) throws VpnException {
		if (!service.isLoggedIn()) {
			throw new VpnException("ShellfireVPN Main Form required a logged in service. This should not happen!");
		}

		/*log.debug("ShellfireVPNMainForm starting up");
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

		this.setUndecorated(true);
		this.enableMouseMoveListener();

		CustomLayout.register();
		this.setFont(TitiliumFont.getFont());
		this.loadIcons();
		this.setLookAndFeel();

		initComponents();
		this.initTray();

		this.initLayeredPaneSize();
                */
		
		/*
		this.initContent();
		Storage.register(this);

		this.initShortCuts();
		this.initPremium();
		this.initConnection();
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		this.setLocationRelativeTo(null);
		setVisible(true);
                */
	}

    
    
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		
            // initializing images of the form
		this.connectoinBackgroundImageView.setImage(Util.getImageIconFX("src/main/resources/buttons/button-connect-idle.png"));
                this.serverListBackgroundImage.setImage(Util.getImageIconFX("src/main/resources/buttons/button-serverlist-idle.png"));
                this.mapBackgroundImageView.setImage(Util.getImageIconFX("src/main/resources/buttons/button-map-idle.png"));
                this.streamsBackgroundImageView.setImage(Util.getImageIconFX("src/main/resources/buttons/button-usa-idle.png"));
                this.globeConnectionImageView.setImage(Util.getImageIconFX("src/main/resources/icons/small-globe-disconnected.png"));
                
            // initializing text of the form 
            this.connectionStatusLabel.setText(i18n.tr("Verbindungsstatus"));
            this.connectedSinceLabel.setText(i18n.tr("Verbunden seit:"));
            this.onlineIpLabel.setText(i18n.tr("Online IP"));
            this.vpnIdLabel.setText(i18n.tr("VPN Id:"));
            this.vpnTypeLabel.setText(i18n.tr("VPN Typ:"));
            this.validUntilLabel.setText(i18n.tr("Gültig bis:"));
            
            this.connectionHeaderLabel.setText(i18n.tr("Verbindung"));
            this.connectionFooter.setText(i18n.tr("Jetzt zu Shellfire VPN verbinden"));
            this.serverListHeaderLabel.setText(i18n.tr("Server Liste"));
            //this.serverListFooter.setText(i18n.tr("Liste aller VPN Server anzeigen"));
            this.mapHeaderLabel.setText(i18n.tr("Karte"));
            //this.mapFooterLabel.setText(i18n.tr("Zeigt Verschlüsselungsroute"));
            this.streamsHeaderLabel.setText(i18n.tr("Streams aus den USA"));
            this.streamsFooterLabel.setText(i18n.tr("Liste amerikanischer TV Streams"));
            
            
	}
	/**
         * Initialized the service and other variables. Supposed to be an overloading of constructor
         * 
         * @param WebService service
         */
        public void setSerciceAndInitialize(WebService service){
            	if (!service.isLoggedIn()) {
                        try {
                            throw new VpnException("ShellfireVPN Main Form required a logged in service. This should not happen!");
                        } catch (VpnException ex) {
                            java.util.logging.Logger.getLogger(ShellfireVPNMainFormFxmlController.class.getName()).log(Level.SEVERE, null, ex);
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
                    
                /*
		CustomLayout.register();
		this.setFont(TitiliumFont.getFont());
		this.loadIcons();
		this.setLookAndFeel();

		initComponents();
		this.initTray();

		this.initLayeredPaneSize();
                
		
		
		this.initContent();
		Storage.register(this);

		this.initShortCuts();
		this.initPremium();
		this.initConnection();
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		this.setLocationRelativeTo(null);
		setVisible(true);
                */
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
		  System.out.println("The image key is found at "+VpnI18N.getLanguage().getKey());
		  
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
    
    	private void setStateDisconnected()  
       {
            /*
	  log.debug("setStateDisconnected() - start");
		enableSystemProxyIfProxyConfig();
		this.hideConnectProgress();
		this.jConnectButtonLabel.setIcon(new ImageIcon(buttonConnect));
		this.jConnectButtonLabel1.setIcon(new ImageIcon(buttonConnect));
		
		this.jConnectButtonLabel.setEnabled(true);
		this.jConnectButtonLabel1.setEnabled(true);
		this.jLabelConnectionState.setText(i18n.tr("Nicht verbunden"));
		mySetIconImage(iconDisconnected);
		this.jConnectionStateIcon.setIcon(new ImageIcon(this.iconIdleSmall));
		this.jConnectionStateImage.setIcon(new ImageIcon(this.iconEcncryptionInactive));
		
		
		this.jShowOwnPosition.setEnabled(true);

		boolean showMessage = false;
		String message = "";
		if (this.controller != null) {
			switch (this.controller.getReasonForStateChange()) {
			case PasswordWrong:
				showMessage = true;
				message = i18n.tr("Passwort Falsch");
				break;
			case NotEnoughPrivileges:
				showMessage = true;
				message = i18n.tr("Prozess wird ohne Administrator-Rechte ausgeführt.");
				break;
			case CertificateFailed:
				showMessage = true;
				message = i18n.tr("Unbekannter Zertifikate-Fehler");
				break;
			case AllTapInUse:
				showMessage = true;
				message = i18n.tr("Alle Tap-Geräte in Verwendung. Bitte alle openvpn.exe Prozesse im Task Manager schließen oder PC neu starten.");
				break;
			case DisconnectDetected:
				showMessage = true;
				message = i18n.tr("Verbindung wurde unterbrochen.");
				break;
			case OpenVpnNotFound:
				showMessage = true;
				message = i18n.tr("OpenVPN Installation wurde nicht gefunden. Bitte Shellfire VPN neu installieren.");
				break;
			case NoOpenVpnParameters:
				showMessage = true;
				message = i18n.tr("OpenVPN Startparameter konnten nicht geladen werden - Bitte überprüfe deine Internet-Verbindung.");
				break;
			case TapDriverTooOld:
				showMessage = true;
				message = i18n.tr("Der installierte Tap Treiber ist zu alt. Bitte installiere Shellfire VPN neu.");
				break;
      case TapDriverNotFound:
        showMessage = true;
        message = i18n.tr("Es wurde kein Tap Treiber installiert. Bitte installiere Shellfire VPN neu.");
        break;
      case TapDriverNotFoundPleaseRetry:
        connectFromButton(true);
        break;
			case GatewayRedirectFailed:
				showMessage = true;
				message = i18n
						.tr("Das Gateway konnte nicht umgeleitet werden. Bitte bei den TCP/IP Einstellungen der aktuellen Netzwerkverbindung ein Gateway einstellen.");
				break;
			case UnknownOpenVPNError:
				showMessage = true;
				message = i18n
						.tr("Es ist ein unbekannter Fehler mit der VPN Verbindung aufgetreten. Bitte versuche einen Reboot und/oder Shellfire VPN neu zu installieren.");
				break;

			default:
				break;
			}
			
			log.debug("setStateDisconnected() - end");
		}

		if (showMessage) {
			JOptionPane.showMessageDialog(null, message, "Fehler: Verbindung fehlgeschlagen", JOptionPane.ERROR_MESSAGE);

			if (this.trayIcon != null) {
				this.trayIcon.setImage(this.iconDisconnected);
			}
		} else {
			if (this.trayIcon != null) {
				this.trayIcon.setImage(this.iconIdle);
			}
		}

		this.stopConnectedSinceTimer();

		this.setNormalCursor();
		this.updateOnlineHost();
		this.mapController.updateMap();
		popupConnectItem.setLabel(i18n.tr("Verbinden"));
		popupConnectItem.setEnabled(true);
		jServerListTable.setEnabled(true);
		if (!ProxyConfig.isProxyEnabled()) {
			this.jRadioUdp.setEnabled(true);
		}
		jRadioTcp.setEnabled(true);

		jScrollPane.getViewport().setBackground(Color.white);

		SwingWorker<Reason, Void> worker = new SwingWorker<Reason, Void>() {
			protected Reason doInBackground() throws Exception {
				Reason reasonForChange = controller.getReasonForStateChange();
				return reasonForChange;
			}

			public void done() {
				try {
					Reason reasonForChange = get();
					if (reasonForChange == Reason.DisconnectButtonPressed || reasonForChange == Reason.DisconnectDetected) {

						showTrayMessageWithoutCallback(i18n.tr("Verbindung getrennt"),
								i18n.tr("Shellfire VPN Verbindung getrennt. Deine Internet-Verbindung ist nicht mehr geschützt!"));
					}
				} catch (Exception e) {
					Util.handleException(e);
				}

			}
		};

		worker.execute();

	}
	private void enableSystemProxyIfProxyConfig()  {
            /*
		if (ProxyConfig.isProxyEnabled()) {
		  Client.enableSystemProxy();
		}
*/
	}
        
        	private void loadIcons() {
                    /*
		this.iconIdleSmall = Util.getImageIcon("/icons/small-globe-disconnected.png").getImage();
		this.iconIdle = Util.getImageIcon("/icons/sfvpn2-idle-big.png").getImage();
		
		this.iconConnectingSmall = Util.getImageIcon("/icons/small-globe-connecting.png").getImage();
		this.iconConnecting = Util.getImageIcon("/icons/sfvpn2-connecting-big.png").getImage();
		
		this.iconConnectedSmall = Util.getImageIcon("/icons/small-globe-connected.png").getImage();
		this.iconConnected = Util.getImageIcon("/icons/sfvpn2-connected-big.png").getImage();
		
		this.iconDisconnected = Util.getImageIcon("/icons/sfvpn2-disconnected-big.png").getImage();
		
		double scaleFactor = Util.getScalingFactor();
		log.debug("ScalingFactor: " + scaleFactor);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double width = screenSize.getWidth();
		
		String size = "736";
		if (width > 3000) {
		  size = "1472";
		}
		
		this.iconEcncryptionActive = new javax.swing.ImageIcon(ShellfireVPNMainForm.class.getResource("/icons/status-encrypted-width"+size+".gif")).getImage();
		this.iconEcncryptionInactive = new javax.swing.ImageIcon(ShellfireVPNMainForm.class.getResource("/icons/status-unencrypted-width"+size+".gif")).getImage();
		
		String langKey = VpnI18N.getLanguage().getKey();
		log.debug("langKey: " + langKey);
		this.buttonDisconnect = Util.getImageIcon("/buttons/button-disconnect-" + langKey + ".gif").getImage();
		this.buttonConnect = Util.getImageIcon("/buttons/button-connect-" + langKey + ".gif").getImage();
		
		mySetIconImage(iconIdle);
                */
	}

}
