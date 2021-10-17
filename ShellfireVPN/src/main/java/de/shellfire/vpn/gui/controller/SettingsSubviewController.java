/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package de.shellfire.vpn.gui.controller;

import java.net.URL;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Random;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.VpnProperties;
import de.shellfire.vpn.client.Client;
import de.shellfire.vpn.gui.LoginForms;
import de.shellfire.vpn.gui.model.CountryMap;
import de.shellfire.vpn.gui.model.ServerListFXModel;
import de.shellfire.vpn.gui.renderer.StarImageRendererFX;
import de.shellfire.vpn.i18n.Language;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.types.Country;
import de.shellfire.vpn.types.Server;
import de.shellfire.vpn.types.ServerType;
import de.shellfire.vpn.types.VpnProtocol;
import de.shellfire.vpn.webservice.ServerList;
import de.shellfire.vpn.webservice.Vpn;
import de.shellfire.vpn.webservice.WebService;
import de.shellfire.vpn.webservice.model.VpnStar;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

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
	@FXML
	private Button saveSettingsButton;
	@FXML
	private Button cancelButton;
	
	private Language currentLanguage;

	
	@FXML
	private void handleConnectImage2MouseExited(MouseEvent event) {
		this.application.getStage().getScene().setCursor(Cursor.DEFAULT);
	}

	@FXML
	private void handleConnectImage2MouseEntered(MouseEvent event) {
		this.application.getStage().getScene().setCursor(Cursor.HAND);
	}

	@FXML
	private void handleConnectImage2ContextRequested(ContextMenuEvent event) {
	}

	@FXML
	private void handleConnectImage2MouseClicked(MouseEvent event) {
		WebService service = WebService.getInstance();
		Util.openUrl(service.getUrlPremiumInfo());
	}

	private static I18n i18n = VpnI18N.getI18n();
	public static Vpn currentVpn;
	private WebService shellfireService;
	private ServerList serverList;
	private LoginForms application;
	private static final Logger log = Util.getLogger(SettingsSubviewController.class.getCanonicalName());
	private ObservableList<ServerListFXModel> serverListData = FXCollections.observableArrayList();
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
		this.serverList = this.shellfireService.getServerList();
		this.serverListData.addAll(initServerTable(this.shellfireService.getServerList().getAll()));
		// this.serverListTableView.setItems(serverListData);
		// this.serverListTableView.comp
	}

	/**
	 * Initializes the controller class.
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		this.TCPRadioButton.setText(i18n.tr("OpenVPN TCP (works with secure firewalls and proxies.)"));
		this.UDPRadioButton.setText(i18n.tr("OpenVPN UDP (fast)"));
		this.WireguardRadioButton.setText(i18n.tr("Wireguard (fastest)"));
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
	private void handleLanguageComboBox(ActionEvent event) {
	}

	@FXML
	private void handleLanguageShown(Event event) {
		this.languageComboBox.getSelectionModel().select(VpnI18N.getLanguage());
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

	private LinkedList<ServerListFXModel> initServerTable(LinkedList<Server> servers) {
		LinkedList<ServerListFXModel> allModels = new LinkedList<>();
		for (int i = 0; i < servers.size(); i++) {
			ServerListFXModel serverModel = new ServerListFXModel();
			serverModel.setCountry(servers.get(i));
			serverModel.setName(servers.get(i).getName());
			serverModel.setServerType(servers.get(i).getServerType().toString());
			serverModel.setSecurity(servers.get(i).getSecurity());
			serverModel.setSpeed(servers.get(i).getServerSpeed());
			allModels.add(serverModel);
		}
		return allModels;
	}

	public Server getRandomFreeServer() {
		Server[] arrServer = new Server[this.getNumberOfServers()];
		int i = 0;
		for (Server server : this.shellfireService.getServerList().getAll()) {
			if (server.getServerType() == ServerType.Free) {
				arrServer[i++] = server;
			}
		}

		Random generator = new Random((new Date()).getTime());
		int num = generator.nextInt(i);

		return arrServer[num];

	}

	public int getNumberOfServers() {
		if (this.shellfireService == null) {
			return 0;
		} else {
			return this.shellfireService.getServerList().getAll().size();
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

	public Server getRandomPremiumServer() {
		Server[] arrServer = new Server[this.getNumberOfServers()];
		int i = 0;
		for (Server server : this.shellfireService.getServerList().getAll()) {
			if (server.getServerType() == ServerType.Premium) {
				arrServer[i++] = server;
			}
		}

		Random generator = new Random((new Date()).getTime());
		int num = generator.nextInt(i);

		return arrServer[num];

	}

	public void setApp(LoginForms app) {
		this.application = app;
	}

	public void setMainFormController(ShellfireVPNMainFormFxmlController mainController) {
		this.mainFormController = mainController;
	}

	@FXML
	private void connectButton1Exited(MouseEvent event) {
		this.application.getStage().getScene().setCursor(Cursor.DEFAULT);
	}

	@FXML
	private void connectButton1Entered(MouseEvent event) {
		this.application.getStage().getScene().setCursor(Cursor.HAND);
	}

	@FXML
	private void connectButton1OnAction(ActionEvent event) {
		log.debug("connectButton1OnAction");
		Platform.runLater(() -> {
			application.shellfireVpnMainController.connectFromButton();
		});

	}


	private void save() {
		VpnProperties props = VpnProperties.getInstance();
		this.currentLanguage = this.languageComboBox.getValue();
		if (!this.loginAutomatically.isDisabled()) { // Not disabled means the checkbox is enabled.
			props.setBoolean(LoginController.REG_AUTOlogIN, this.loginAutomatically.isSelected());
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
