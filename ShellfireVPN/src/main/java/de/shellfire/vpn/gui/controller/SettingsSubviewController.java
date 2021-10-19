/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package de.shellfire.vpn.gui.controller;

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
import de.shellfire.vpn.gui.LoginForms;
import de.shellfire.vpn.gui.model.ServerListFXModel;
import de.shellfire.vpn.i18n.Language;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.types.Server;
import de.shellfire.vpn.types.VpnProtocol;
import de.shellfire.vpn.webservice.Vpn;
import de.shellfire.vpn.webservice.WebService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;

/**
 * FXML Controller class
 *
 * @author Tcheutchoua Steve
 */
public class SettingsSubviewController implements Initializable {

	@FXML
	private CheckBox saveLoginData;
	@FXML
	private CheckBox loginAutomatically;
	@FXML
	private CheckBox saveVpnChoice;
	@FXML
	private CheckBox startOnBoot;
	@FXML
	private CheckBox connectAutomatically;
	@FXML
	private CheckBox showStatusSite;
	@FXML
	private Label languageLabel;
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
	private ToggleGroup networkTypeToggleGroup;
	
	private Language currentLanguage;

	private static I18n i18n = VpnI18N.getI18n();
	public static Vpn currentVpn;
	private WebService shellfireService;
	private LoginForms application;
	private static final Logger log = Util.getLogger(SettingsSubviewController.class.getCanonicalName());
	private ShellfireVPNMainFormFxmlController mainFormController;
	private Image buttonDisconnect = new Image("/buttons/button-disconnect-" + VpnI18N.getLanguage().getKey() + ".gif");

	/**
	 * Constructor used to initialize serverListTable data from Webservice
	 *
	 * @param shellfireService
	 *            used to get the serverList data
	 */
	public SettingsSubviewController(WebService shellfireService) {
		this.shellfireService = shellfireService;
		currentVpn = shellfireService.getVpn();
		initComponents();
	}

	/**
	 * No argument constructor used by javafx framework
	 *
	 */
	public SettingsSubviewController() {
	}

	public void setShellfireService(WebService shellfireService) {
		this.shellfireService = shellfireService;
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
		this.TCPRadioButton.setText(i18n.tr("OpenVPN TCP (works with secure firewalls and proxies.)"));
		this.UDPRadioButton.setText(i18n.tr("OpenVPN UDP (fast)"));
		this.WireguardRadioButton.setText(i18n.tr("Wireguard (fastest)"));
		this.saveLoginData.setText(i18n.tr("Save login data"));
		this.loginAutomatically.setText(i18n.tr("Login automatically"));
		this.saveVpnChoice.setText(i18n.tr("Save VPN choice"));
		this.startOnBoot.setText(i18n.tr("Start on boot"));
		this.showStatusSite.setText(i18n.tr("Show status site after connection has been established"));
		this.languageLabel.setText(i18n.tr("Language") + ":");
		this.connectAutomatically.setText(i18n.tr("Connect automatically"));
		this.languageComboBox.setEditable(false);
	
		initValues();
	}

	@FXML
	private void handleSaveLoginData(ActionEvent event) {
		if (!this.saveLoginData.isDisabled() && !saveLoginData.isSelected()) {
			if (!this.loginAutomatically.isDisabled()) {
				this.loginAutomatically.setSelected(false);
			}
			if (!this.saveLoginData.isDisabled()) {
				this.saveLoginData.setSelected(false);
			}
		}
		save();
	}

	@FXML
	private void handleLoginAutomatically(ActionEvent event) {
		if (!this.loginAutomatically.isDisabled() && loginAutomatically.isSelected() && !this.saveLoginData.isDisabled()) {
			this.saveLoginData.setSelected(true);
		}
		save();
	}
	

	@FXML
	private void handleLanguageComboBox(ActionEvent event) {
		save();
	}

	@FXML
	private void handleLanguageShown(Event event) {
		this.languageComboBox.getSelectionModel().select(VpnI18N.getLanguage());
		save();
	}
	

	@FXML
	private void handleSaveVpnChoice(ActionEvent event) {
		if (!this.saveVpnChoice.isDisabled() && saveVpnChoice.isSelected() && !this.saveLoginData.isDisabled()) {
			this.saveLoginData.setSelected(true);
		}
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
	public void updateComponents(boolean isConnected) {
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

		if (props.getProperty(LoginController.REG_USER, null) != null) {
			this.saveLoginData.setDisable(true);
		} else {
			this.saveLoginData.setSelected(true); // can only be enabled from login dialog
		}

		if (props.getBoolean(LoginController.REG_AUTOlOGIN, false)) {
			this.loginAutomatically.setSelected(true);
		} else if (props.getProperty(LoginController.REG_USER, null) == null) {
			this.loginAutomatically.setDisable(true); // disable if login data not remembered, because then it makes no sense
		}

		if (props.getInt(LoginForms.REG_REMEMBERSELECTION, 0) != 0) {
			this.saveVpnChoice.setSelected(true);
		} else if (props.getProperty(LoginController.REG_USER, null) == null) {
			this.saveVpnChoice.setDisable(true); // disable if login data not remembered, because then it makes no sense
		}

		boolean autoConnect = props.getBoolean(LoginController.REG_AUTOCONNECT, false);
		this.connectAutomatically.setSelected(autoConnect);

		boolean autoStart = Client.vpnAutoStartEnabled();
		this.startOnBoot.setSelected(autoStart);

		boolean showStatusUrlOnConnect = props.getBoolean(LoginController.REG_SHOWSTATUSURL, false);
		this.showStatusSite.setSelected(showStatusUrlOnConnect);

		this.initLanguages();
	}

	private void initLanguages() {
		log.debug("SettingsDialogController: initLanguages - method called");
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
	}
	
	private void save() {
		VpnProperties props = VpnProperties.getInstance();
		this.currentLanguage = this.languageComboBox.getValue();
		if (!this.loginAutomatically.isDisabled()) { // Not disabled means the checkbox is enabled.
			props.setBoolean(LoginController.REG_AUTOlOGIN, this.loginAutomatically.isSelected());
		}

		if (!this.saveLoginData.isDisabled() && this.saveLoginData.isSelected() == false) {
			props.remove(LoginController.REG_USER);
			props.remove(LoginController.REG_PASS);
		}

		if (!this.saveVpnChoice.isDisabled() && !this.saveVpnChoice.isSelected()) {
			props.remove(LoginForms.REG_REMEMBERSELECTION);
		}

		props.setBoolean(LoginController.REG_AUTOCONNECT, this.connectAutomatically.isSelected());
		props.setBoolean(LoginController.REG_SHOWSTATUSURL, this.showStatusSite.isSelected());

		if (this.saveVpnChoice.isSelected()) {
			WebService service = WebService.getInstance();
			Vpn vpn = service.getVpn();
			props.setInt(LoginForms.REG_REMEMBERSELECTION, vpn.getVpnId());
		}

		if (this.startOnBoot.isSelected()) {
			Client.addVpnToAutoStart();
		} else {
			Client.removeVpnFromAutoStart();
		}

		Language oldLanguage = VpnI18N.getLanguage();

		if (!currentLanguage.equals(oldLanguage)) {
			VpnI18N.setLanguage(currentLanguage);
			log.debug("SettingsDialogController: save() - language changed to " + currentLanguage.getName());
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
	}
	
	class ServerListComparator implements Comparator<Server> {
		@Override
		public int compare(Server o1, Server o2) {
			return o1.getCountry().name().compareTo(o2.getCountry().name());
		}
	}
}
