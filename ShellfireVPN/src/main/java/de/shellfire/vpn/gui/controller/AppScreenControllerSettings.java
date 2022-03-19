/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package de.shellfire.vpn.gui.controller;

import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Optional;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.VpnProperties;
import de.shellfire.vpn.client.Client;
import de.shellfire.vpn.client.ConnectionState;
import de.shellfire.vpn.gui.LoginForms;
import de.shellfire.vpn.gui.helper.CurrentConnectionState;
import de.shellfire.vpn.gui.renderer.CrownImageRendererVpn;
import de.shellfire.vpn.i18n.Language;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.types.Reason;
import de.shellfire.vpn.types.Server;
import de.shellfire.vpn.types.VpnProtocol;
import de.shellfire.vpn.webservice.WebService;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;

/**
 * FXML Controller class
 *
 * @author Tcheutchoua Steve
 */
public class AppScreenControllerSettings implements Initializable, AppScreenController {

	private LogViewerFxmlController logViewer;
	@FXML
	private CheckBox startOnBoot;
	@FXML
	private CheckBox connectAutomatically;
	@FXML
	private CheckBox showStatusSite;
	@FXML
	private Label languageLabel;
	@FXML
	private Label selectedVpnId;
	@FXML
	private Label loggedInUser;
	
	
	@FXML
	private Label headerLabel;
	@FXML
	private Label connectionTypeLabel;
	@FXML
	private Label triggerLabel;
	@FXML
	private Label accountLabel;
	@FXML
	private Label userLabel;
	
	
	@FXML
	private ImageView selectedVpnType;
	@FXML
	private ComboBox<Language> languageComboBox;
	@FXML
	private AnchorPane serverListAnchorPane;
	@FXML
	private RadioButton WireguardRadioButton;
	@FXML
	private RadioButton UDPRadioButton;
	@FXML
	private RadioButton TCPRadioButton;
	@FXML
	private Button showLogButton;
	@FXML
	private Button selectVpnButton;
	@FXML
	private Button logoutButton;
	@FXML
	private ToggleGroup networkTypeToggleGroup;
	
	private Language currentLanguage;

	private static I18n i18n = VpnI18N.getI18n();
	private WebService shellfireService;
	private LoginForms application;
	private static final Logger log = Util.getLogger(AppScreenControllerSettings.class.getCanonicalName());
	private ShellfireVPNMainFormFxmlController mainFormController;
	private Image buttonDisconnect = new Image("/buttons/button-disconnect-" + VpnI18N.getLanguage().getKey() + ".gif");
	private boolean languagesInitialized = false;


	public AppScreenControllerSettings() throws IOException  {
		this.logViewer = LogViewerFxmlController.getInstance();

		initComponents();
	}


	private void initConsole() {
		log.debug("showing logviewer...");
		try {
			log.debug("setting logViewer to visible");
			logViewer.getInstanceStage().show();
			logViewer.enable();
			log.debug("Logviewer has been shown");
		} catch (Exception e) {
			log.error("Error occured while displaying logviewer", e);
		}
	}


	public void setShellfireService(WebService shellfireService) {
		this.shellfireService = shellfireService;
		
		this.updateSelectedVpn();
	}


	void updateSelectedVpn() {
		this.selectedVpnId.setText("sf" + shellfireService.getVpn().getVpnId());
		this.selectedVpnType.setImage(CrownImageRendererVpn.getIcon(shellfireService.getVpn().getAccountType(), false, false));
	}

	void setLoggedInUser(String username) {
		loggedInUser.setText(username);
	}

	public RadioButton getWireguardRadioButton() {
		return WireguardRadioButton;
	}

	public RadioButton getUDPRadioButton() {
		return UDPRadioButton;
	}

	public RadioButton getTCPRadioButton() {
		return TCPRadioButton;
	}

	public ToggleGroup getNetworkTypeToggleGroup() {
		return networkTypeToggleGroup;
	}

	public void initComponents() {
	}

	/**
	 * Initializes the controller class.
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		currentLanguage = VpnI18N.getLanguage();
		this.showLogButton.setText(i18n.tr("Show Log Window"));
		this.TCPRadioButton.setText(i18n.tr("OpenVPN TCP (works best with firewalls and proxies)"));
		this.UDPRadioButton.setText(i18n.tr("OpenVPN UDP (fast)"));
		this.WireguardRadioButton.setText(i18n.tr("Wireguard (fastest)"));
		this.startOnBoot.setText(i18n.tr("Start on boot"));
		this.showStatusSite.setText(i18n.tr("Once connected, show status in browser"));
		this.languageLabel.setText(i18n.tr("Language"));
		this.connectAutomatically.setText(i18n.tr("Connect automatically"));
		this.languageComboBox.setEditable(false);
	
		headerLabel.setText(i18n.tr("Settings"));
		connectionTypeLabel.setText(i18n.tr("Connection Type"));
		triggerLabel.setText(i18n.tr("Automation"));
		accountLabel.setText(i18n.tr("Account"));
		userLabel.setText(i18n.tr("User"));
		selectVpnButton.setText(i18n.tr("Change VPN"));
		logoutButton.setText(i18n.tr("Logout"));
		
		
		initValues();
	}

	@FXML
	private void handleLoginAutomatically(ActionEvent event) {
		save();
	}
	

	@FXML
	private void handleLanguageComboBox(ActionEvent event) {
		save();
	}
	
	@FXML
	private void onClickShowLogButton(ActionEvent event) {
		this.initConsole();
	}
	
	@FXML
	private void onClickSelectVpnButton(ActionEvent event) {
		showVpnSelectScreen();
	}
	
	public void showVpnSelectScreen() {
		VpnProperties props = VpnProperties.getInstance();
		props.remove(LoginForms.REG_REMEMBERSELECTION);
		
		
	    Stage stage = new Stage();
	    Parent root;
		try {
			FXMLLoader loader = new FXMLLoader(AppScreenControllerSettings.class.getResource("/fxml/VpnSelectDialogFxml.fxml"));
			root = loader.load();
			
		    stage.setScene(new Scene(root));
		    stage.initStyle(StageStyle.UTILITY);
		    stage.initModality(Modality.WINDOW_MODAL);
		    stage.initOwner(LoginForms.getStage().getScene().getWindow() );
		    stage.setTitle(i18n.tr("Select VPN"));
		    stage.show();
		    
		    VpnSelectDialogController vpnSelectController = (VpnSelectDialogController) loader.getController();
			vpnSelectController.setApp(this.application);
			vpnSelectController.setMainForm(this.mainFormController);
			vpnSelectController.setService(this.shellfireService);
			vpnSelectController.setSelectedVpn(Integer.parseInt(this.selectedVpnId.getText().substring(2)));
			
		} catch (IOException e) {
			log.error("onClickSelectVpnButton - Could not switch subview to VPN Select Screen", e);
		}
	}

	@FXML
	private void onClickLogoutButton(ActionEvent event) {
		boolean performLogout = true;
		boolean isConnected = CurrentConnectionState.getConnectionState() == ConnectionState.Connected;
		
		String text, titleText;
		titleText = i18n.tr("Shellfire VPN - Logout Confirmation");
		if (isConnected) {
			text = i18n.tr("You are currently connected. Disconnect and logout?");
		} else {
			text = i18n.tr("Are you sure you want to logout?");
		}
		
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION, text,	ButtonType.YES,	ButtonType.NO);
		alert.initStyle(StageStyle.UTILITY);
		alert.setTitle(titleText);
		alert.setHeaderText(null);
		
		Optional<ButtonType> result = alert.showAndWait();
		performLogout = ((result.isPresent()) && (result.get() == ButtonType.YES));

		
		if (performLogout) {
			if (isConnected) {
				this.mainFormController.getController().disconnect(Reason.ClientLogout);
			}
			
			VpnProperties props = VpnProperties.getInstance();
			props.remove(LoginController.REG_USER);
			props.remove(LoginController.REG_PASS);
			props.setBoolean(LoginController.REG_AUTOlOGIN, false);
			
			if ((result.isPresent()) && (result.get() == ButtonType.YES)) {
				alert.close();
				LoginController.restart();
			}
		}
	}

	@FXML
	private void handleLanguageShown(Event event) {
		this.languageComboBox.getSelectionModel().select(VpnI18N.getLanguage());
		save();
	}
	

	@FXML
	private void handleSaveVpnChoice(ActionEvent event) {
		save();
	}

	@FXML
	private void handleStartOnBoot(ActionEvent event) {
		save();
	}

	@FXML
	private void handleConnectAutomatically(ActionEvent event) {
		save();
	}

	@FXML
	private void handleShowStatusSite(ActionEvent event) {
		save();
	}
	
	public void afterInitialization() {
	}

	/**
	 * Updates buttons and other components when connection status changes
	 * 
	 * @param isConnected
	 *            boolean variable for the connection status
	 */
	public void notifyThatNowVisible(boolean isConnected) {
		if (isConnected) {
			TCPRadioButton.disableProperty().set(isConnected);
			UDPRadioButton.disableProperty().set(isConnected);
			WireguardRadioButton.disableProperty().set(isConnected);
		}
	}

	public VpnProtocol getSelectedProtocol() {
		if (this.WireguardRadioButton.isSelected()) {
			return VpnProtocol.WireGuard;
		} else if (this.UDPRadioButton.isSelected()) {
			return VpnProtocol.UDP;
		} else if (this.TCPRadioButton.isSelected()) {
			return VpnProtocol.TCP;
		}

		return null;
	}


	public void setApp(LoginForms app) {
		this.application = app;
	}

	public void setMainFormController(ShellfireVPNMainFormFxmlController mainController) {
		this.mainFormController = mainController;
	}


	private void initValues() {
		VpnProperties props = VpnProperties.getInstance();

		// TODO: autoConnect aktuell ohne funktion
		boolean autoConnect = props.getBoolean(LoginController.REG_AUTOCONNECT, false);
		this.connectAutomatically.setSelected(autoConnect);

		boolean autoStart = Client.vpnAutoStartEnabled();
		this.startOnBoot.setSelected(autoStart);

		boolean showStatusUrlOnConnect = props.getBoolean(LoginController.REG_SHOWSTATUSURL, false);
		this.showStatusSite.setSelected(showStatusUrlOnConnect);

		this.initLanguages();
	}

	private void initLanguages() {
		if (this.languagesInitialized) {
			return;
		}
		
		log.debug("initLanguages - method called");
		LinkedList<Language> languages = VpnI18N.getAvailableTranslations();
		this.languageComboBox.getItems().addAll(languages);

		languageComboBox.setCellFactory(new Callback<ListView<Language>, ListCell<Language>>() {
			@Override
			public ListCell<Language> call(ListView<Language> param) {
				final ListCell<Language> cell = new ListCell<Language>() {
					@Override
					public void updateItem(Language item, boolean empty) {
						super.updateItem(item, empty);
						if (item != null) {
							setText(i18n.tr(item.getName()));
							currentLanguage = item;
						} else {
							log.debug("Item is null here");
						}
					}
				};
				return cell;
			}
		});
		languageComboBox.setValue(currentLanguage);
		
		this.languagesInitialized  = true;
	}

	private void save() {
		VpnProperties props = VpnProperties.getInstance();
		this.currentLanguage = this.languageComboBox.getValue();

		props.setBoolean(LoginController.REG_AUTOCONNECT, this.connectAutomatically.isSelected());
		props.setBoolean(LoginController.REG_SHOWSTATUSURL, this.showStatusSite.isSelected());

		if (this.startOnBoot.isSelected()) {
			Client.addVpnToAutoStart();
		} else {
			Client.removeVpnFromAutoStart();
		}

		Language oldLanguage = VpnI18N.getLanguage();

		if (!currentLanguage.equals(oldLanguage)) {
			VpnI18N.setLanguage(currentLanguage);
			log.debug("save() - language changed to " + currentLanguage.getName());
			Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
					i18n.tr("Changed language settings require a restart of ShellfireVPN to take effect. Restart now?"), ButtonType.YES,
					ButtonType.NO);
			alert.setHeaderText(i18n.tr("Changed language settings require a restart of Shellfire VPN to take effect."));
			Optional<ButtonType> result = alert.showAndWait();
			if ((result.isPresent()) && (result.get() == ButtonType.YES)) {
				alert.close();
				LoginController.restart();
			}
		}
		
		initValues();
	}
	
	class ServerListComparator implements Comparator<Server> {
		@Override
		public int compare(Server o1, Server o2) {
			return o1.getCountry().name().compareTo(o2.getCountry().name());
		}
	}

}
