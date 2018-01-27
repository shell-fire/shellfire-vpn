package de.shellfire.vpn.gui.controller;

import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.exception.VpnException;
import de.shellfire.vpn.gui.LoginForms;
import de.shellfire.vpn.i18n.VpnI18N;
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

public class ShellfireVPNMainFormFxmlController extends AnchorPane implements Initializable {

        private static final I18n I18N = VpnI18N.getI18n();

    private LoginForms application;
  private static final Logger log = Util.getLogger(ShellfireVPNMainFormFxmlController.class.getCanonicalName());
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
    private Label vpnType;
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
		// TODO Auto-generated method stub
		
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
		/* Continure from here after MainForm UI Modelling
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
                
		***/
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
}
