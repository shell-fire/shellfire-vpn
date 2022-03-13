package de.shellfire.vpn.gui.controller;

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.LocaleChangeEvent;
import org.xnap.commons.i18n.LocaleChangeListener;

import de.shellfire.vpn.Storage;
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
import de.shellfire.vpn.gui.helper.CurrentConnectionState;
import de.shellfire.vpn.gui.model.ServerRow;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.proxy.ProxyConfig;
import de.shellfire.vpn.types.Reason;
import de.shellfire.vpn.types.Server;
import de.shellfire.vpn.types.ServerType;
import de.shellfire.vpn.types.VpnProtocol;
import de.shellfire.vpn.webservice.Vpn;
import de.shellfire.vpn.webservice.WebService;
import de.shellfire.vpn.webservice.model.TrayMessage;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Pair;

public class ShellfireVPNMainFormFxmlController extends AnchorPane implements Initializable, LocaleChangeListener, ConnectionStateListener, ListChangeListener<ServerRow>
	{

	Map<AppScreen, Pair<Pane, Object>> menuAppScreenMap = new HashMap<>();
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

	private AppScreenControllerStatus appScreenControllerStatus;
	private AppScreenControllerServerList appScreenControllerServerList;
	private AppScreenControllerPremium appScreenControllerPremium;
	private AppScreenControllerSettings appScreenControllerSettings;
	private AppScreenControllerAbout appScreenControllerAbout;
	private AppScreenControllerHelp appScreenControllerHelp;
	
	private Color colorMenuBlue = Color.web("#4581f8");
	private Color colorMenuGrey = Color.web("#757575");
	
	private Date connectedSince;
	java.awt.Image iconConnected = Util.getImageIcon("/icons/sfvpn2-connected-big.png").getImage();
	private java.awt.Image iconConnecting = Util.getImageIcon("/icons/sfvpn2-connecting-big.png").getImage();
	Map<AppScreen, Map<MenuStatus, Image>> menuImageStatusMap = null;
	private java.awt.Image iconDisconnectedAwt = Util.getImageIcon("/icons/sfvpn2-disconnected-big.png").getImage();
	private java.awt.Image iconIdleAwt = Util.getImageIcon("/icons/sfvpn2-idle-big.png").getImage();

	
	private ScheduledExecutorService currentConnectedSinceTimerFX = Executors.newSingleThreadScheduledExecutor();
	private boolean connectionStatus;
	private boolean disconnectDetectExpected;
	private Integer serverIdRejectedDueToPrivileges;

	
	static AppScreen currentAppScreen = AppScreen.STATUS;
	@FXML
	private Label menuLabelStatus;
	@FXML
	private Label menuLabelServerList;
	@FXML
	private Label menuLabelPremium;
	@FXML
	private Label menuLabelSettings;
	@FXML
	private Label menuLabelAbout;
	@FXML
	private Label menuLabelHelp;
	
	@FXML
	private Pane leftMenuPane;
	@FXML
	private Pane leftConnectionPane;
	@FXML
	private ImageView menuImageStatus;
	@FXML
	private Pane serverListPane;
	@FXML
	private ImageView menuImageServerList;
	@FXML
	private ImageView menuImagePremium;
	@FXML
	private ImageView menuImageSettings;
	@FXML
	private ImageView menuImageAbout;
	@FXML
	private ImageView menuImageHelp;
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
	private Pane contentDetailsPane;

	// Access to embedded controller and variables in subviews
	@FXML
	private Parent connectionSubview;
	
	@FXML
	private AppScreenControllerStatus connectionSubviewController;
	
	@FXML
	private HashMap<AppScreen, ImageView> menuImageViewMap;
	private HashMap<AppScreen, Label> menuLabelMap;

	private boolean subViewControllersPrepared;
	private int selectedServer;

	public ShellfireVPNMainFormFxmlController() {

		log.debug("No argumenent constructer of ShellfireVPNMainFormFxmlController has been called");
	}

	public AppScreenControllerStatus getAppScreenControllerStatus() {
		return appScreenControllerStatus;
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

		String size = "736";

		String langKey = VpnI18N.getLanguage().getKey();
		log.debug("langKey: " + langKey);

		mySetIconImage("/icons/sfvpn2-idle-big.png");

		initializeMenuImages();
		currentAppScreen = AppScreen.STATUS;
		
	}
	
	enum MenuStatus { SELECTED, UNSELECTED, HOVER }
	
	private void initializeMenuImages() {
		menuImageViewMap = new HashMap<AppScreen, ImageView>();
		menuImageViewMap.put(AppScreen.STATUS, menuImageStatus);
		menuImageViewMap.put(AppScreen.SERVERLIST, menuImageServerList);
		menuImageViewMap.put(AppScreen.PREMIUM, menuImagePremium);
		menuImageViewMap.put(AppScreen.SETTINGS, menuImageSettings);
		menuImageViewMap.put(AppScreen.ABOUT, menuImageAbout);
		menuImageViewMap.put(AppScreen.HELP, menuImageHelp);
		
		menuLabelMap = new HashMap<AppScreen, Label>();
		menuLabelMap.put(AppScreen.STATUS, menuLabelStatus);
		menuLabelMap.put(AppScreen.SERVERLIST, menuLabelServerList);
		menuLabelMap.put(AppScreen.PREMIUM, menuLabelPremium);
		menuLabelMap.put(AppScreen.SETTINGS, menuLabelSettings);
		menuLabelMap.put(AppScreen.ABOUT, menuLabelAbout);
		menuLabelMap.put(AppScreen.HELP, menuLabelHelp);
		
		menuImageStatusMap = new HashMap<AppScreen, Map<MenuStatus, Image>>();
		
		Map<MenuStatus, Image> statusImageMap = new HashMap<MenuStatus, Image>();
		statusImageMap.put(MenuStatus.UNSELECTED, Util.getImageIconFX("/menu/status.png"));
		statusImageMap.put(MenuStatus.SELECTED, Util.getImageIconFX("/menu/status_selected.png"));
		statusImageMap.put(MenuStatus.HOVER, Util.getImageIconFX("/menu/status_selected.png"));
		menuImageStatusMap.put(AppScreen.STATUS, statusImageMap);
		
		Map<MenuStatus, Image> serverListImageMap = new HashMap<MenuStatus, Image>();
		serverListImageMap.put(MenuStatus.UNSELECTED, Util.getImageIconFX("/menu/server.png"));
		serverListImageMap.put(MenuStatus.SELECTED, Util.getImageIconFX("/menu/server_selected.png"));
		serverListImageMap.put(MenuStatus.HOVER, Util.getImageIconFX("/menu/server_selected.png"));
		menuImageStatusMap.put(AppScreen.SERVERLIST, serverListImageMap);
		
		Map<MenuStatus, Image> premiumImageMap = new HashMap<MenuStatus, Image>();
		premiumImageMap.put(MenuStatus.UNSELECTED, Util.getImageIconFX("/menu/premium.png"));
		premiumImageMap.put(MenuStatus.SELECTED, Util.getImageIconFX("/menu/premium_selected.png"));
		premiumImageMap.put(MenuStatus.HOVER, Util.getImageIconFX("/menu/premium_selected.png"));
		menuImageStatusMap.put(AppScreen.PREMIUM, premiumImageMap);
		
		Map<MenuStatus, Image> settingsImageMap = new HashMap<MenuStatus, Image>();
		settingsImageMap.put(MenuStatus.UNSELECTED, Util.getImageIconFX("/menu/settings.png"));
		settingsImageMap.put(MenuStatus.SELECTED, Util.getImageIconFX("/menu/settings_selected.png"));
		settingsImageMap.put(MenuStatus.HOVER, Util.getImageIconFX("/menu/settings_selected.png"));
		menuImageStatusMap.put(AppScreen.SETTINGS, settingsImageMap);
		
		Map<MenuStatus, Image> aboutImageMap = new HashMap<MenuStatus, Image>();
		aboutImageMap.put(MenuStatus.UNSELECTED, Util.getImageIconFX("/menu/about.png"));
		aboutImageMap.put(MenuStatus.SELECTED, Util.getImageIconFX("/menu/about_selected.png"));
		aboutImageMap.put(MenuStatus.HOVER, Util.getImageIconFX("/menu/about_selected.png"));
		menuImageStatusMap.put(AppScreen.ABOUT, aboutImageMap);
		
		Map<MenuStatus, Image> helpImageMap = new HashMap<MenuStatus, Image>();
		helpImageMap.put(MenuStatus.UNSELECTED, Util.getImageIconFX("/menu/help.png"));
		helpImageMap.put(MenuStatus.SELECTED, Util.getImageIconFX("/menu/help_selected.png"));
		helpImageMap.put(MenuStatus.HOVER, Util.getImageIconFX("/menu/help_selected.png"));
		menuImageStatusMap.put(AppScreen.HELP, helpImageMap);
	}

	public void initializeComponents() {
		log.debug("initializeComponents() - start - before Platform.runlater");
	    Platform.runLater(() -> {
	    	log.debug("initializeComponents() - in Platform.runlater");
	    	// Notice that you manipulate the javaObjects out of the initialize if not it will raise an InvocationTargetException
	    	
	    	// Bind visibility of buttons to their manage properties so that they are easilty rendered visible or invisible
	    	this.appScreenControllerStatus.initPremium(isFreeAccount());
	    	this.appScreenControllerStatus.setApp(this.application);
	    	this.appScreenControllerStatus.notifyThatNowVisible(false);
	    	
	    });
	}

	public void setShellfireService(WebService shellfireService) {
		this.shellfireService = shellfireService;
		log.debug("ShellfireVPNMainFormFxmlController:" + "service initialized");
	}

	/**
	 * Initialized the service and other variables. Supposed to be an overloading of constructor
	 *
	 * @param WebService
	 *            service
	 */
	public void setServiceAndInitialize(WebService service) {
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
		
		// this ensures early and correct initialization
		CurrentConnectionState.getInstance(this);

		
		this.initTray();

		Storage.register(this);


		this.initConnection();
		this.initVpn();

		this.application.getStage().resizableProperty().setValue(Boolean.FALSE);
		this.application.getStage().show();
	}



	public int getRememberedVpnSelection() {
		VpnProperties props = VpnProperties.getInstance();
		int remembered = props.getInt(LoginForms.REG_REMEMBERSELECTION, 0);

		return remembered;
	}
	
	public void setRememberedVpnSelection(int vpnId) {
		VpnProperties props = VpnProperties.getInstance();
		props.setInt(LoginForms.REG_REMEMBERSELECTION, vpnId);
	}
	
	private void initVpn() {
		int rememberedVpn = getRememberedVpnSelection();
		boolean setRememberedVpnIsSuccess = false;
		if (rememberedVpn > 0) {
			setRememberedVpnIsSuccess = this.shellfireService.selectVpn(rememberedVpn);
		}

		if (!setRememberedVpnIsSuccess) {
			this.shellfireService.autoSelectBestVpn();
			setRememberedVpnSelection(this.shellfireService.getVpn().getVpnId());
		}
	}
	
	public void setVpn(int vpnId) {
		log.debug("setVpn() called {}", vpnId);
		this.shellfireService.selectVpn(vpnId);
		this.appScreenControllerSettings.updateSelectedVpn();
		this.appScreenControllerServerList.updateSelectedVpn();
		setRememberedVpnSelection(vpnId);
		this.initServer();
	}

	private void initServer() {
		log.debug("initServer() - start");
		Vpn vpn = shellfireService.getVpn();

		log.debug("Vpn: {}, Server: {}", vpn.getVpnId(), vpn.getServerId());

		setSelectedServer(vpn.getServerId());
	}

	public void setApp(LoginForms applic) {
		this.application = applic;
	}

	public void afterLogin(boolean autoConnect) {
		log.debug("afterLogin(autoConnect=" + autoConnect + ")");
		Vpn vpn = this.shellfireService.getVpn();
		
		log.debug("afterLogin() - if proxy enabled, enforce TCP");
		if (ProxyConfig.isProxyEnabled()) {
			this.setSelectedProtocol(VpnProtocol.TCP);
			this.appScreenControllerSettings.getUDPRadioButton().setDisable(true);
			this.appScreenControllerSettings.getWireguardRadioButton().setDisable(true);
		} else {
			VpnProtocol selectedProtocol = vpn.getProtocol();
			this.setSelectedProtocol(selectedProtocol);
		}

		
		log.debug("afterLogin() - if autoConnect do it");
		if (autoConnect) {
			Platform.runLater(() -> {
				this.connectFromButton();
			});

		}
		
		log.debug("afterLogin() - finished");
	}

	private void onMenuPaneMouseExited(AppScreen appScreen) {
		application.getStage().getScene().setCursor(Cursor.DEFAULT);
		if (!currentAppScreen.equals(appScreen)) {
			this.menuImageViewMap.get(appScreen).setImage(menuImageStatusMap.get(appScreen).get(MenuStatus.UNSELECTED));
			this.menuLabelMap.get(appScreen).setTextFill(colorMenuGrey);
		}
	}

	private void onMenuPaneMouseEntered(AppScreen appScreen) {
		application.getStage().getScene().setCursor(Cursor.HAND);
		if (!currentAppScreen.equals(appScreen)) {
			this.menuImageViewMap.get(appScreen).setImage(menuImageStatusMap.get(appScreen).get(MenuStatus.HOVER));
			this.menuLabelMap.get(appScreen).setTextFill(colorMenuBlue);
		}
	}

	void showAppScreen(AppScreen pane) {
		contentDetailsPane.getChildren().setAll(menuAppScreenMap.get(pane).getKey());
		
		AppScreenController fxController = (AppScreenController) menuAppScreenMap.get(pane).getValue();
		if (fxController != null) {
			fxController.notifyThatNowVisible(connectionStatus);			
		}
		currentAppScreen = pane;
		
		for (AppScreen currentAppScreen : AppScreen.values()) {
			ImageView imageView = menuImageViewMap.get(currentAppScreen);
			if (currentAppScreen.equals(pane)) {
				imageView.setImage(menuImageStatusMap.get(currentAppScreen).get(MenuStatus.SELECTED));
				this.menuLabelMap.get(currentAppScreen).setTextFill(colorMenuBlue);
			} else {
				imageView.setImage(menuImageStatusMap.get(currentAppScreen).get(MenuStatus.UNSELECTED));
				this.menuLabelMap.get(currentAppScreen).setTextFill(colorMenuGrey);
			}
		}
	}
	
	// Status
	@FXML
	private void onMenuPaneStatusMouseExited(MouseEvent event) {
		onMenuPaneMouseExited(AppScreen.STATUS);
	}

	@FXML
	private void onMenuPaneStatusMouseEntered(MouseEvent event) {
		onMenuPaneMouseEntered(AppScreen.STATUS);
	}
	
	@FXML
	private void onMenuPaneStatusClicked(MouseEvent event) {
		showAppScreen(AppScreen.STATUS);
	}

	// Server List
	@FXML
	private void onMenuPaneServerListMouseExited(MouseEvent event) {
		onMenuPaneMouseExited(AppScreen.SERVERLIST);
	}

	@FXML
	private void onMenuPaneServerListMouseEntered(MouseEvent event) {
		onMenuPaneMouseEntered(AppScreen.SERVERLIST);
	}

	@FXML
	private void onMenuPaneServerListClicked(MouseEvent event) {
		showAppScreen(AppScreen.SERVERLIST);
	}
	
	// Premium
	@FXML
	private void onMenuPanePremiumMouseExited(MouseEvent event) {
		onMenuPaneMouseExited(AppScreen.PREMIUM);
	}

	@FXML
	private void onMenuPanePremiumMouseEntered(MouseEvent event) {
		onMenuPaneMouseEntered(AppScreen.PREMIUM);
	}

	@FXML
	private void onMenuPanePremiumClicked(MouseEvent event) {
		showAppScreen(AppScreen.PREMIUM);
	}
	
	// Settings
	@FXML
	private void onMenuPaneSettingsClicked(MouseEvent event) {
		showAppScreen(AppScreen.SETTINGS);
	}

	@FXML
	private void onMenuPaneSettingsMouseExited(MouseEvent event) {
		onMenuPaneMouseExited(AppScreen.SETTINGS);
	}

	@FXML
	private void onMenuPaneSettingsMouseEntered(MouseEvent event) {
		onMenuPaneMouseEntered(AppScreen.SETTINGS);
	}
	
	// About
	@FXML
	private void onMenuPaneAboutClicked(MouseEvent event) {
		showAppScreen(AppScreen.ABOUT);
	}

	@FXML
	private void onMenuPaneAboutMouseExited(MouseEvent event) {
		onMenuPaneMouseExited(AppScreen.ABOUT);
	}

	@FXML
	private void onMenuPaneAboutMouseEntered(MouseEvent event) {
		onMenuPaneMouseEntered(AppScreen.ABOUT);
	}

	
	// Help
	@FXML
	private void onMenuPaneHelpClicked(MouseEvent event) {
		showAppScreen(AppScreen.HELP);
	}

	@FXML
	private void onMenuPaneHelpMouseExited(MouseEvent event) {
		onMenuPaneMouseExited(AppScreen.HELP);
	}

	@FXML
	private void onMenuPaneHelpMouseEntered(MouseEvent event) {
		onMenuPaneMouseEntered(AppScreen.HELP);
	}	

	@FXML
	private void handleHelpImageViewMouseExited(MouseEvent event) {
		this.application.getStage().getScene().setCursor(Cursor.DEFAULT);
	}

	@FXML
	private void handleHelpImageViewMouseEntered(MouseEvent event) {
		this.application.getStage().getScene().setCursor(Cursor.HAND);
	}


	// TODO: replace by loader from somewhere else, e.g. "more" screen?
	@FXML
	private void handleHelpImageViewClicked(MouseEvent event) {
		this.openHelp();
	}


	// TODO: Bind this hideImageView code to the minimize button from Stage, so minimize will always be minimize to tray...
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
		// throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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

					Server server = this.appScreenControllerServerList.getSelectedServer();
					if (server.getServerType() == ServerType.Premium || server.getServerType() == ServerType.PremiumPlus) {
						setNormalCursor();
						Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
								i18n.tr("This server is only available for Shellfire VPN Premium customers\n\nShow more information about Shellfire VPN Premium?"),
								ButtonType.YES, ButtonType.NO);
						alert.setHeaderText(i18n.tr("Premium server selected"));
						Optional<ButtonType> result = alert.showAndWait();

						if ((result.isPresent()) && (result.get() == ButtonType.YES)) {
							Util.openUrl(shellfireService.getUrlPremiumInfo());
						}

						return;
					}

					controller.connect(appScreenControllerServerList.getSelectedServer(), appScreenControllerSettings.getSelectedProtocol(),
							Reason.ConnectButtonPressed);
				} else if (isPremiumAccount()) {
					log.debug("ServerList Subview controller  has the object " + appScreenControllerServerList.toString());
					Server server = this.appScreenControllerServerList.getSelectedServer();

					if (server.getServerType() == ServerType.PremiumPlus) {
						setNormalCursor();

						Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
								i18n.tr("This server is only available for Shellfire VPN PremiumPlus customers\n\nShow more information about Shellfire VPN PremiumPlus?"),
								ButtonType.YES, ButtonType.NO);
						alert.setHeaderText(i18n.tr("PremiumPlus server selected"));
						Optional<ButtonType> result = alert.showAndWait();
						if ((result.isPresent()) && (result.get() == ButtonType.YES)) {
							Util.openUrl(shellfireService.getUrlPremiumInfo());
						}
						return;

					}

					controller.connect(appScreenControllerServerList.getSelectedServer(), appScreenControllerSettings.getSelectedProtocol(),
							Reason.ConnectButtonPressed);
				} else {
					controller.connect(appScreenControllerServerList.getSelectedServer(), appScreenControllerSettings.getSelectedProtocol(),
							Reason.ConnectButtonPressed);
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
		
		Platform.runLater(() -> {
			try {
				
				connectProgressDialog = ProgressDialogController.getInstance(i18n.tr("Connecting..."), task, this.application.getStage(), true);
				connectProgressDialog.getButton().setOnAction(new EventHandler<javafx.event.ActionEvent>() {

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

		this.appScreenControllerStatus.connectButtonDisable(false);
		Platform.runLater(() -> {
			mySetIconImage("/icons/sfvpn2-disconnected-big.png");
		});
		this.appScreenControllerStatus.notifyThatNowVisible(false);
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
			case WireGuardError:
					showMessage = true;
					message = i18n.tr("WireGuard error. Please contact support for assistance or try another connection type.");
					break;
			case TapDriverNotFoundPleaseRetry:
				Platform.runLater(() -> {
					connectFromButton();
				});

				break;
			case GatewayRedirectFailed:
				showMessage = true;
				message = i18n.tr(
						"The gateway coul not be switched. Please set a gateway in the TCP/IP settings of the current network adapter.");
				break;
			case UnknownOpenVPNError:
				showMessage = true;
				message = i18n.tr(
						"An unknown error has occured while establishing the VPN connection. Please reboot and/or reinstall Shellfire VPN.");
				break;

			default:
				break;
			}

			log.debug("setStateDisconnected() - end");
		}
		final boolean text = showMessage;
		if (text) {
			final String finalMessage = message;
			Platform.runLater(() -> {
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
		appScreenControllerServerList.getServerListTableView().disableProperty().set(false);
		this.setNormalCursor();
		if (!ProxyConfig.isProxyEnabled()) {
			this.appScreenControllerSettings.getUDPRadioButton().setDisable(false);
			this.appScreenControllerSettings.getWireguardRadioButton().setDisable(false);
		}
		this.appScreenControllerSettings.getTCPRadioButton().setDisable(false);
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
					if (reasonForChange == Reason.DisconnectButtonPressed && !disconnectDetectExpected) {
						disconnectDetectExpected = true;
						showTrayMessageWithoutCallback(i18n.tr("Disconnected"),
								i18n.tr("Shellfire VPN connection terminated. Your internet connection is no longer secured!"));
					} else if (reasonForChange == Reason.DisconnectDetected && !disconnectDetectExpected) {
						showTrayMessageWithoutCallback(i18n.tr("Disconnected"),
								i18n.tr("Shellfire VPN connection terminated. Your internet connection is no longer secured!"));
					} else if (reasonForChange == Reason.DisconnectDetected && disconnectDetectExpected) {
						log.debug(
								"succeeeded() - Reason=DisconnectDetected and disconnectDetectExpected - not showing tray, but setting disconnectDetected=false");
						disconnectDetectExpected = false;
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
		this.appScreenControllerStatus.connectButtonDisable(true);

		Platform.runLater(() -> {
			mySetIconImage("/icons/sfvpn2-connecting-big.png");
		});

		if (this.trayIcon != null) {
			this.trayIcon.setImage(this.iconConnecting);
		}
		this.setWaitCursor();

		popupConnectItem.setLabel(i18n.tr("Connecting..."));
		popupConnectItem.setEnabled(false);
		popup.add(abortItem);
		appScreenControllerServerList.getServerListTableView().disableProperty().set(true);
		appScreenControllerSettings.getWireguardRadioButton().setDisable(true);
		appScreenControllerSettings.getUDPRadioButton().setDisable(true);
		appScreenControllerSettings.getTCPRadioButton().setDisable(true);
	}

	/**
	 * Displays the progress of execution as a task is being ran
	 *
	 * @return no return value
	 */
	private void showConnectProgress() throws IOException {
		// create an instance of progress dialog
		// TODO using tasks methods to update application thread
		Platform.runLater(() -> {
			log.debug("showConnectProgress: Entrance of method");
			if (connectProgressDialog != null) {
				connectProgressDialog.show();
			}
		});
	}

	public void mySetIconImage(String imagePath) {
		log.debug("mySetIconImage: the icon Image  path is " + imagePath);
		Platform.runLater(() -> {
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
					Platform.runLater(() -> {
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
				connectProgressDialog.getButton().fire();
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
		Platform.runLater(() -> {
			if (!LoginForms.getStage().isShowing()) {
				LoginForms.getStage().show();
			} else {
				LoginForms.getStage().toFront();
			}

			((Stage) LoginForms.getStage()).setIconified(false);
			setVisible(true);
			toFront();

		});

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
			mySetIconImage("/icons/sfvpn2-connected-big.png");
		});

		this.appScreenControllerStatus.notifyThatNowVisible(true);
		this.appScreenControllerStatus.connectButtonDisable(false);
		appScreenControllerServerList.getServerListTableView().disableProperty().set(true);
		if (this.trayIcon != null) {
			this.trayIcon.setImage(this.iconConnected);
		}

		this.setNormalCursor();

		this.startConnectedSinceTimer();

		appScreenControllerSettings.getWireguardRadioButton().disableProperty().set(true);
		appScreenControllerSettings.getUDPRadioButton().disableProperty().set(true);
		appScreenControllerSettings.getTCPRadioButton().disableProperty().set(true);
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
		Platform.runLater(() -> {
			if (connectProgressDialog != null) {
				connectProgressDialog.hide();
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
			// terminate all existing timer tasks
			this.currentConnectedSinceTimerFX.shutdown();
			this.currentConnectedSinceTimerFX = Executors.newSingleThreadScheduledExecutor();
		}
	}

	public void setSelectedProtocol(VpnProtocol protocol) {
		if (protocol == null) {
			protocol = VpnProtocol.WireGuard;
		}

		switch (protocol) {
		case WireGuard:
			this.appScreenControllerSettings.getWireguardRadioButton().setSelected(true);
			break;
		case UDP:
			this.appScreenControllerSettings.getUDPRadioButton().setSelected(true);
			break;
		case TCP:
			this.appScreenControllerSettings.getTCPRadioButton().setSelected(true);
			break;
		}

	}

	public void updateConnectedSince() {
		Date now = new Date();
		long diffInSeconds = (now.getTime() - connectedSince.getTime()) / 1000;

		long diff[] = new long[] { 0, 0, 0, 0 };
		diff[3] = (diffInSeconds >= 60 ? diffInSeconds % 60 : diffInSeconds);
		diff[2] = (diffInSeconds = (diffInSeconds / 60)) >= 60 ? diffInSeconds % 60 : diffInSeconds;
		diff[1] = (diffInSeconds = (diffInSeconds / 60));
		String since = String.format("%dh %dm %ds", diff[1], diff[2], diff[3]);

		SimpleDateFormat df = new SimpleDateFormat("E, H:m", VpnI18N.getLanguage().getLocale());
		String start = df.format(connectedSince);
		String text = start + " " + "(" + since + ")";
	}

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

	public void setSelectedServer(int server) {
		log.debug("setSelectedServer(" + server + ")");
		
		
		if (selectedServer == server) {
			log.debug("setSelectedServer() - (Server {} already set - returning", server);
			return;
		}
		
		if (serverIdRejectedDueToPrivileges != null) {
			log.debug("overriding selected server to {} because it was previusly requested, then the vpn tab was shown and another vpn has now been selected", serverIdRejectedDueToPrivileges);
			
			ServerType serverType = this.shellfireService.getServerList().getServerByServerId(serverIdRejectedDueToPrivileges).getServerType();
			Vpn vpn = this.shellfireService.getVpn();
			
			boolean allowed = false;
			if (vpn.getAccountType() == ServerType.PremiumPlus) {
				allowed = true;
			}
			if (vpn.getAccountType() == ServerType.Premium && serverType != ServerType.PremiumPlus) {
				allowed = true;
			}
			if (vpn.getAccountType() == ServerType.Free && serverType == ServerType.Free) {
				allowed = true;
			}
			
			if (allowed) {
				server = serverIdRejectedDueToPrivileges;
			}
			serverIdRejectedDueToPrivileges = null;
		}
		
		this.selectedServer = server;
		
		if (appScreenControllerStatus != null) {
			appScreenControllerStatus.setSelectedServer(server);
		}		
		
		if (appScreenControllerServerList != null) {
			appScreenControllerServerList.setSelectedServer(server);
		}
		
		final int finalServer = server;
		Task<Void> task = new Task<Void>() {
		    @Override public Void call() {
		    	shellfireService.setServerTo(finalServer);
		    	return null;
		    }
		};

		new Thread(task).start();
		
	}

	
	/**
	 * Prepare controllers so that they load controllers so that controller objects can be accessed.
	 */
	public void prepareSubviewControllers() {
		if (this.subViewControllersPrepared) {
			return;
		}
		try {
	
			// load the serverList pane
			Pair<Pane, Object> pairServerList = FxUIManager.SwitchSubview("appscreen_subview_serverlist.fxml");
			this.appScreenControllerServerList = (AppScreenControllerServerList) pairServerList.getValue();
			this.appScreenControllerServerList.initComponents();
			this.appScreenControllerServerList.setMainFormController(this);
			menuAppScreenMap.put(AppScreen.SERVERLIST, pairServerList);
			log.debug("Serverlist controller defined");

			// load the premium pane
			Pair<Pane, Object> pairPremium = FxUIManager.SwitchSubview("appscreen_subview_premium.fxml");
			this.appScreenControllerPremium = (AppScreenControllerPremium) pairPremium.getValue();
			this.appScreenControllerPremium.setShellfireService((this.shellfireService));
			this.appScreenControllerPremium.initComponents();
			this.appScreenControllerPremium.setApp(this.application);
			this.appScreenControllerPremium.setMainFormController(this);
			menuAppScreenMap.put(AppScreen.PREMIUM, pairPremium);
			log.debug("premium controller defined");

			// load the settings pane
			Pair<Pane, Object> pairSettings = FxUIManager.SwitchSubview("appscreen_subview_settings.fxml");
			this.appScreenControllerSettings = (AppScreenControllerSettings) pairSettings.getValue();
			this.appScreenControllerSettings.setShellfireService((this.shellfireService));
			this.appScreenControllerSettings.initComponents();
			this.appScreenControllerSettings.setApp(this.application);
			this.appScreenControllerSettings.setMainFormController(this);
			menuAppScreenMap.put(AppScreen.SETTINGS, pairSettings);
			
			log.debug("settings controller defined");

			// load the about pane
			Pair<Pane, Object> pairAbout = FxUIManager.SwitchSubview("appscreen_subview_about.fxml");
			this.appScreenControllerAbout = (AppScreenControllerAbout) pairAbout.getValue();
			this.appScreenControllerAbout.setShellfireService((this.shellfireService));
			this.appScreenControllerAbout.initComponents();
			this.appScreenControllerAbout.setApp(this.application);
			this.appScreenControllerAbout.setMainFormController(this);
			menuAppScreenMap.put(AppScreen.ABOUT, pairAbout);
			log.debug("about controller defined");

			// load the help pane
			Pair<Pane, Object> pairHelp = FxUIManager.SwitchSubview("appscreen_subview_help.fxml");
			this.appScreenControllerHelp = (AppScreenControllerHelp) pairHelp.getValue();
			this.appScreenControllerHelp.setShellfireService((this.shellfireService));
			this.appScreenControllerHelp.initComponents();
			this.appScreenControllerHelp.setApp(this.application);
			this.appScreenControllerHelp.setMainFormController(this);
			menuAppScreenMap.put(AppScreen.HELP, pairHelp);
			log.debug("help controller defined");

			
			// load status pane
			// what is in connectionSubview ??
			// Pair<Pane, Object> pairStatus = FxUIManager.SwitchSubview("appscreen_subview_status.fxml");
			this.appScreenControllerStatus = connectionSubviewController;
			
			 Pair<Pane, Object> pairStatus = new Pair<Pane, Object>((AnchorPane)connectionSubview, appScreenControllerStatus);
			contentDetailsPane.getChildren().setAll(menuAppScreenMap.get(AppScreen.SERVERLIST).getKey());
			this.appScreenControllerStatus.initPremium(isFreeAccount());
			this.appScreenControllerStatus.setApp(this.application);
			this.appScreenControllerStatus.setShellfireService((this.shellfireService));
			log.debug("handleConnectionPanelClicked: VPN connection status is " + connectionStatus);
			this.appScreenControllerStatus.notifyThatNowVisible(connectionStatus);
			log.debug("status controller defined");
		
			
			menuAppScreenMap.put(AppScreen.STATUS, pairStatus);
			onMenuPaneStatusClicked(null);
			
			this.initServer();
			
		} catch (IOException ex) {
			log.error("ShellfireVPNMainFormFxmlController:  prepareControllers has error", ex);
		}
		
		subViewControllersPrepared = true;
		
	}

	public void exitHandler() {
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

		Alert alert = new Alert(Alert.AlertType.CONFIRMATION, i18n.tr("Disconnect and close Shellfire VPN?"), ButtonType.YES,
				ButtonType.NO);
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
		log.debug("showTrayMessageWithoutCallback(String header=" + header + ", String content=" + content + ")");
		trayIcon.displayMessage(header, content, MessageType.INFO);
	}

	public void minimizeToTray() {
		log.debug("ShellfireVPNMainController.minimizeToTray() - start");
		if (leftMenuPane != null) {
			Scene scene = leftMenuPane.getScene();
			if (scene != null) {
				Stage stage = (Stage)scene.getWindow();
				if (stage != null) {
					hide(stage);
				} else {
					log.debug("stage is null, cannot hide()");
				}
			} else {
				log.debug("scene is null, cannot hide()");
			}
		} else {
			log.debug("leftMenuPane is null, cannot hide()");
		}
		log.debug("ShellfireVPNMainController.minimizeToTray() - return");
	}

	@Override
	public void onChanged(javafx.collections.ListChangeListener.Change<? extends ServerRow> arg0) {
		System.out.println("onChange of server registered in Main Form, nice!");
		
	}

	public void setUserName(String username) {
		this.appScreenControllerSettings.setLoggedInUser(username);
	}

	public AppScreenControllerSettings getAppScreenControllerSettings() {
		return this.appScreenControllerSettings;
	}

	public void setServerIdRejectedDueToPrivileges(int serverId) {
		this.serverIdRejectedDueToPrivileges = serverId;
	}



}

enum AppScreen {
	STATUS, SERVERLIST, SETTINGS, PREMIUM, ABOUT, HELP;
}