package de.shellfire.vpn.gui.controller;

import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.exception.VpnException;
import de.shellfire.vpn.gui.LoginForms;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.proxy.ProxyConfig;
import de.shellfire.vpn.types.Server;
import de.shellfire.vpn.types.VpnProtocol;
import de.shellfire.vpn.webservice.Vpn;
import de.shellfire.vpn.webservice.WebService;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;

public class ShellfireVPNMainFormFxmlController extends AnchorPane implements Initializable {

        private static final I18n I18N = VpnI18N.getI18n();

    private LoginForms application;
  private static final Logger log = Util.getLogger(ShellfireVPNMainFormFxmlController.class.getCanonicalName());
private WebService shellfireService;

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
}
