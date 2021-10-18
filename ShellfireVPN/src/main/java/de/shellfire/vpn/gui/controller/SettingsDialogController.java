/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui.controller;

import java.net.URL;
import java.util.LinkedList;
import java.util.Optional;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.VpnProperties;
import de.shellfire.vpn.client.Client;
import de.shellfire.vpn.gui.LoginForms;
import de.shellfire.vpn.i18n.Language;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.webservice.Vpn;
import de.shellfire.vpn.webservice.WebService;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 *
 * @author TcheutchouaSteve
 */
public class SettingsDialogController implements Initializable {

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
	private Button saveSettingsButton;
	@FXML
	private Button cancelButton;
	@FXML
	private ComboBox<Language> languageComboBox;

	private static I18n i18n = VpnI18N.getI18n();
	private Language currentLanguage;
	private static final Logger log = Util.getLogger(SettingsDialogController.class.getCanonicalName());

	public SettingsDialogController() {
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.saveLoginData.setText(i18n.tr("Save login data"));
		this.loginAutomatically.setText(i18n.tr("Login automatically"));
		this.saveVpnChoice.setText(i18n.tr("Save VPN choice"));
		this.startOnBoot.setText(i18n.tr("Start on boot"));
		this.showStatusSite.setText(i18n.tr("Show status site after connection has been established"));
		this.languageLabel.setText(i18n.tr("Language") + ":");
		this.saveSettingsButton.setText(i18n.tr("Save settings"));
		this.cancelButton.setText(i18n.tr("Cancel"));
		this.connectAutomatically.setText(i18n.tr("Connect automatically"));
		this.languageComboBox.setEditable(false);
		currentLanguage = VpnI18N.getLanguage();
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
	}

	@FXML
	private void handleLoginAutomatically(ActionEvent event) {
		if (!this.loginAutomatically.isDisabled() && loginAutomatically.isSelected() && !this.saveLoginData.isDisabled()) {
			this.saveLoginData.setSelected(true);
		}
	}

	@FXML
	private void handleSaveVpnChoice(ActionEvent event) {
		if (!this.saveVpnChoice.isDisabled() && saveVpnChoice.isSelected() && !this.saveLoginData.isDisabled()) {
			this.saveLoginData.setSelected(true);
		}
	}

	@FXML
	private void handleStartOnBoot(ActionEvent event) {
	}

	@FXML
	private void handleConnectAutomatically(ActionEvent event) {
	}

	@FXML
	private void handleShowStatusSite(ActionEvent event) {
	}

	@FXML
	private void handleSaveSettingsButton(ActionEvent event) {
		log.debug("Save button has been clicked");
		Stage stage = (Stage) this.saveSettingsButton.getScene().getWindow();
		stage.hide();
		log.debug("About to call the save function");
		save();
	}

	@FXML
	private void handleCancelButton(ActionEvent event) {
		Stage stage = (Stage) this.cancelButton.getScene().getWindow();
		stage.hide();
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

	@FXML
	private void handleLanguageComboBox(ActionEvent event) {
	}

	@FXML
	private void handleLanguageShown(Event event) {
		this.languageComboBox.getSelectionModel().select(VpnI18N.getLanguage());
	}
}
