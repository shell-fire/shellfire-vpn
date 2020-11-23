package de.shellfire.vpn.gui.controller;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.VpnProperties;
import de.shellfire.vpn.gui.LoginForms;
import de.shellfire.vpn.gui.model.VpnSelectionFXModel;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.webservice.Vpn;
import de.shellfire.vpn.webservice.WebService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class VpnSelectDialogController extends AnchorPane implements Initializable {

	@FXML
	private Button selectVpnButton;
	@FXML
	private Label vpnSelectLabel;
	@FXML
	private CheckBox fAutoconnect;
	@FXML
	private Label vpnTypeLabel;
	@FXML
	private ScrollPane vpnScrollPane;
	@FXML
	private TableView<VpnSelectionFXModel> vpnListTable;
	@FXML
	private TableColumn<VpnSelectionFXModel, Integer> idTbleColumn;
	@FXML
	private TableColumn<VpnSelectionFXModel, String> typeTbleColumn;
	@FXML
	private TableColumn<VpnSelectionFXModel, String> accArtTbleColumn;
	@FXML
	private Label numAccountVpnLabel;
	@FXML
	private Pane headerPanel;
	@FXML
	private ImageView headerImageView;
	@FXML
	private Label backLabel;

	private WebService shellfireService;
	private boolean autoConnect;
	public static final String REG_REMEMBERSELECTION = "SelectedVpnId";
	private static I18n i18n = VpnI18N.getI18n();

	private LoginForms application;
	private static final long serialVersionUID = 1L;
	private static Logger log = Util.getLogger(VpnSelectDialogController.class.getCanonicalName());
	@FXML
	private Pane backLabelPane;
	@FXML
	private ImageView backImageVeiw;

	private ObservableList<VpnSelectionFXModel> vpnData = FXCollections.observableArrayList();

	/**
	 * The constructor. The constructor is called before the initialize() method.
	 */
	public VpnSelectDialogController() {
		log.debug("VpnSelectDialogController constructor has been accessed");
	}
	// Event Listener on Button[#selectVpnButton].onAction

	// Set Autoconnect like former constructor
	public boolean isAutoConnect() {
		return autoConnect;
	}

	public void setAutoConnect(boolean autoConnect) {
		log.debug("We have set autoconnect" + autoConnect);
		this.autoConnect = autoConnect;
	}

	// Set Service like former constructor
	public WebService getService() {
		return shellfireService;
	}

	public void setService(WebService service) {
		this.shellfireService = service;
		initVpnSelectTable(service.getAllVpn());
	}

	@FXML
	public void handleSelectVpnButton(ActionEvent event) throws IOException {
		// TODO Autogenerated
		VpnSelectionFXModel selectedItem = this.vpnListTable.getSelectionModel().getSelectedItem();
		if (selectedItem == null) {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setHeaderText(i18n.tr("No VPN selected"));
			alert.setContentText(i18n.tr("Please select a VPN from the list to proceed."));
			alert.showAndWait();
		} else {
			rememberSelectionIfDesired(selectedItem.getVpn());

			this.shellfireService.selectVpn(selectedItem.getVpn());
			this.loadMainForm();
		}

	}
	// Event Listener on CheckBox[#fAutoconnect].onAction

	@FXML
	public void handlefAutoconnect(ActionEvent event) {
		// TODO Autogenerated
	}
	// Event Listener on ImageView[#exitImageView].onContextMenuRequested

	@FXML
	private void backLabelMousexited(MouseEvent event) {
		this.backLabel.setTextFill(Color.WHITE);
	}

	@FXML
	private void backPaneMouseExited(MouseEvent event) {
		this.backLabel.setTextFill(Color.WHITE);
		this.application.getStage().getScene().setCursor(Cursor.DEFAULT);
	}

	@FXML
	private void backPaneMouseEntered(MouseEvent event) {
		// this.backLabel.setTextFill(Color.gray(USE_PREF_SIZE, USE_PREF_SIZE));
		this.application.getStage().getScene().setCursor(Cursor.HAND);
	}

	@FXML
	private void handleBackLabel(MouseEvent event) {
		this.application.loadLoginController();
		this.application.getStage().show();
	}

	@FXML
	private void handleMouseEntered(MouseEvent event) {
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
		idTbleColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
		typeTbleColumn.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
		accArtTbleColumn.setCellValueFactory(cellData -> cellData.getValue().accountArtProperty());

		initComponents();

		// Listen for selection changes and store the selected VPN.
		vpnListTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			rememberSelectionIfDesired(newValue.getVpn());
			this.shellfireService.selectVpn(newValue.getVpn());
		});

	}

	public void initComponents() {
		this.headerImageView.setImage(Util.getImageIconFX("/icons/sf_en.png"));
		this.numAccountVpnLabel
				.setText(i18n.tr("Multiple VPN are located in your Shellfire account. Please select the VPN you want to use."));
		this.numAccountVpnLabel.setWrapText(true);
		this.backLabel.setText(i18n.tr("back"));
		this.selectVpnButton.setText(i18n.tr("Select VPN"));
		this.fAutoconnect.setText(i18n.tr("Save my choice"));
		this.vpnSelectLabel.setText(i18n.tr("Choose VPN"));
		this.vpnTypeLabel.setText(
				i18n.tr("Note: VPN types PPTP and L2TP/IPSec have to be switched to OpenVPN before you can connect using sf vpn."));
		this.vpnTypeLabel.setWrapText(true);
	}

	private void rememberSelectionIfDesired(Vpn selectedVpn) {
		VpnProperties props = VpnProperties.getInstance();
		if (fAutoconnect.isSelected()) {

			int vpnId = selectedVpn.getVpnId();
			log.debug("Remembering Vpn ID:" + vpnId);

			props.setInt(REG_REMEMBERSELECTION, vpnId);
		} else {
			log.debug("Forgetting vpn selections");
			props.setInt(REG_REMEMBERSELECTION, 0);
		}
	}

	public int rememberedVpnSelection() {
		VpnProperties props = VpnProperties.getInstance();
		int remembered = props.getInt(REG_REMEMBERSELECTION, 0);

		return remembered;
	}

	public void loadIcon() {
		this.backImageVeiw.setImage(Util.getImageIconFX("/icons/sfvpn2-idle.png"));
	}

	private void initVpnSelectTable(LinkedList<Vpn> allVpn) {
		this.vpnData.addAll(getVpnSelectionModelFromVpn(allVpn));
		this.vpnListTable.setItems(vpnData);
	}

	/**
	 * Generate VpnSelectionFXModels From a set of given VPNs The VpnSelectionFXModel are units to be displayed on VpnSelect Table
	 *
	 * @param LinkedList<Vpn>
	 *            the list of available VPNs
	 */
	private LinkedList<VpnSelectionFXModel> getVpnSelectionModelFromVpn(LinkedList<Vpn> allVpn) {
		LinkedList allModels = new LinkedList<>();
		log.debug("The size of all VPN is " + allVpn.size());
		for (int i = 0; i < allVpn.size(); i++) {
			VpnSelectionFXModel vpnModel = new VpnSelectionFXModel();
			vpnModel.setId(allVpn.get(i).getVpnId());
			// Converting the Vpn Account Type enum to String
			vpnModel.setAccount_art(allVpn.get(i).getAccountType().toString());
			vpnModel.setType(allVpn.get(i).getProductType().toString());
			vpnModel.setVpn(allVpn.get(i));
			allModels.add(vpnModel);
		}
		return allModels;
	}

	public void setApp(LoginForms applic) {
		log.debug("VpnSelectDialogController: Application set up appropriately");
		this.application = applic;
	}

	public void displayVpnSelect() {
		this.application.getStage().show();
		log.debug("VpnSelectDialogController: displayVpnSelect(); displaying the select window");
		this.application.shellFireMainController.afterLogin(autoConnect);
	}

	private void loadMainForm() {
		if (this.application == null) {
			log.debug("VpnSelectDialogController: LoginForms empty");
			this.application = LoginController.application;
		} else {
			log.debug("VpnSelectDialogController: LoginForms already set");
		}
		this.application.toString();
		this.application.loadShellFireMainController();
		this.application.shellFireMainController.setShellfireService(this.shellfireService);
		this.application.shellFireMainController.initializeComponents();
		this.application.shellFireMainController.setServiceAndInitialize(this.shellfireService);
		this.application.shellFireMainController.prepareSubviewControllers();
		this.application.shellFireMainController.afterLogin(autoConnect);
	}
}