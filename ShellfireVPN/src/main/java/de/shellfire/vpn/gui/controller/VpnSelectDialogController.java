package de.shellfire.vpn.gui.controller;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Optional;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.VpnProperties;
import de.shellfire.vpn.client.ConnectionState;
import de.shellfire.vpn.gui.LoginForms;
import de.shellfire.vpn.gui.helper.CurrentConnectionState;
import de.shellfire.vpn.gui.model.ServerRow;
import de.shellfire.vpn.gui.model.VpnSelectionFXModel;
import de.shellfire.vpn.gui.renderer.CrownImageRendererVpn;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.types.Reason;
import de.shellfire.vpn.webservice.Vpn;
import de.shellfire.vpn.webservice.WebService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class VpnSelectDialogController extends AnchorPane implements Initializable {

	@FXML
	private Button selectVpnButton;
	@FXML
	private TableView<VpnSelectionFXModel> vpnListTable;
	@FXML
	private TableColumn<VpnSelectionFXModel, String> idTbleColumn;
	@FXML
	private TableColumn<VpnSelectionFXModel, Vpn> accArtTbleColumn;
	@FXML
	private Pane headerPanel;

	private WebService shellfireService;
	private static I18n i18n = VpnI18N.getI18n();

	private LoginForms application;
	private static final long serialVersionUID = 1L;
	private static Logger log = Util.getLogger(VpnSelectDialogController.class.getCanonicalName());
	@FXML
	private Pane backLabelPane;
	@FXML
	private ImageView backImageVeiw;

	private ObservableList<VpnSelectionFXModel> vpnData = FXCollections.observableArrayList();
	private Vpn newVpn;
	private ShellfireVPNMainFormFxmlController shellfireVpnMainForm;

	/**
	 * The constructor. The constructor is called before the initialize() method.
	 */
	public VpnSelectDialogController() {
		log.debug("constructor has been accessed");
	}
	// Event Listener on Button[#selectVpnButton].onAction

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

		VpnSelectionFXModel selectedItem = this.vpnListTable.getSelectionModel().getSelectedItem();
		if (selectedItem == null) {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setHeaderText(i18n.tr("No VPN selected"));
			alert.setContentText(i18n.tr("Please select a VPN from the list to proceed."));
			alert.showAndWait();
		} else {
			int oldVpnId = this.shellfireService.getVpn().getVpnId();
			int newVpnId = selectedItem.getVpn().getVpnId();
			
			if (oldVpnId != newVpnId) {
				boolean performChange = true;
				boolean isConnected = CurrentConnectionState.getConnectionState() == ConnectionState.Connected;
				if (isConnected) {
					Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
							i18n.tr("You are currently connected. Disconnect and use selected vpn?"), 
							ButtonType.YES,
							ButtonType.NO);
					alert.setTitle(i18n.tr("Currently Connected"));
					Optional<ButtonType> result = alert.showAndWait();
					performChange = ((result.isPresent()) && (result.get() == ButtonType.YES));
				}
				
				if (performChange) {
					if (isConnected) {
						this.shellfireVpnMainForm.getController().disconnect(Reason.OtherVpnSelected);
					}
					
					this.shellfireVpnMainForm.setVpn(selectedItem.getVpn().getVpnId());
					this.closeStage(event);
				}
			}
					
		}

	}

	private void closeStage(ActionEvent event) {
        Node  source = (Node)  event.getSource(); 
        Stage stage  = (Stage) source.getScene().getWindow();
        stage.close();
    }

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		idTbleColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty());
		idTbleColumn.setStyle( "-fx-alignment: CENTER;");
		accArtTbleColumn.setCellValueFactory(cellData -> cellData.getValue().accountArtProperty());

		initComponents();

		// Listen for selection changes and store the selected VPN.
		vpnListTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				newVpn = newValue.getVpn();
				vpnListTable.refresh();
			}
		});
		
		accArtTbleColumn.setStyle( "-fx-alignment: baseline-right;");
		accArtTbleColumn.setCellFactory(column -> {
			return new CrownImageRendererVpn(this);
		});

		vpnListTable.setFixedCellSize(40.0);
	}

	public void initComponents() {
		this.selectVpnButton.setText(i18n.tr("Select VPN"));
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
		LinkedList<VpnSelectionFXModel> allModels = new LinkedList<VpnSelectionFXModel>();
		log.debug("The size of all VPN is " + allVpn.size());
		for (int i = 0; i < allVpn.size(); i++) {
			VpnSelectionFXModel vpnModel = new VpnSelectionFXModel();
			vpnModel.setId("sf" + allVpn.get(i).getVpnId());
			// Converting the Vpn Account Type enum to String
			vpnModel.setAccount_art(allVpn.get(i));
			vpnModel.setVpn(allVpn.get(i));
			allModels.add(vpnModel);
		}
		return allModels;
	}

	public void setApp(LoginForms applic) {
		log.debug("VpnSelectDialogController: Application set up appropriately");
		this.application = applic;
	}

	public Vpn getSelectedVpn() {
		VpnSelectionFXModel serverModel = this.vpnListTable.getSelectionModel().getSelectedItem();
		if (null == serverModel) {
			return null;
		} else {
			Vpn selectedVpn = serverModel.getVpn();
			return selectedVpn; 
		}
	}

	public void setMainForm(ShellfireVPNMainFormFxmlController shellfireVpnMainForm) {
		this.shellfireVpnMainForm = shellfireVpnMainForm;
	}
	
	// Selects a server on serverlist table based on the index (position) of the server
	public void setSelectedVpn(int vpnId) {
		for (int i = 0; i < vpnData.size(); i++) {
			VpnSelectionFXModel curVpn = vpnData.get(i);
			if (Integer.parseInt(curVpn.getId().substring(2)) == vpnId) {
				vpnListTable.requestFocus();
				vpnListTable.getSelectionModel().select(i);
				vpnListTable.getFocusModel().focus(i);
			}
		}
	}
}