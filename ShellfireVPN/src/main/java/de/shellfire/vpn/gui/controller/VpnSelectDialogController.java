package de.shellfire.vpn.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

import java.net.URL;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;

import de.shellfire.vpn.Util;
<<<<<<< HEAD
import de.shellfire.vpn.VpnProperties;
import de.shellfire.vpn.gui.LoginForms;
import de.shellfire.vpn.gui.model.VpnSelectionFXModel;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.proxy.ProxyConfig;
import de.shellfire.vpn.types.Server;
import de.shellfire.vpn.types.VpnProtocol;
import de.shellfire.vpn.webservice.Vpn;
import de.shellfire.vpn.webservice.WebService;
import java.io.IOException;
import java.util.LinkedList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableView;
=======
import de.shellfire.vpn.gui.LoginForms;
import de.shellfire.vpn.gui.model.VpnSelectionFModel;
import de.shellfire.vpn.i18n.VpnI18N;
import javafx.event.ActionEvent;

import javafx.scene.control.Label;

import javafx.scene.control.ScrollPane;

import javafx.scene.image.ImageView;

import javafx.scene.control.CheckBox;

import javafx.scene.control.TableView;

>>>>>>> 32656c998715dfdf2cb3c2b13af96c74a646dc3b
import javafx.scene.control.TableColumn;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

<<<<<<< HEAD
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

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

    private static WebService shellfireService;
    private boolean autoConnect;
    public static final String REG_REMEMBERSELECTION = "SelectedVpnId";
    private static I18n i18n = VpnI18N.getI18n();

    private static LoginForms application;
    private static final long serialVersionUID = 1L;
    private static Logger log = Util.getLogger(VpnSelectDialogController.class.getCanonicalName());
=======
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;

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
	private TableView vpnListTable;
	@FXML
	private TableColumn<VpnSelectionFModel,Integer> idTbleColumn;
	@FXML
	private TableColumn<VpnSelectionFModel,String> typeTbleColumn;
	@FXML
	private TableColumn<VpnSelectionFModel,String> accArtTbleColumn;
	@FXML
	private Label numAccountVpnLabel;
	@FXML
	private Pane headerPanel;
	@FXML
	private ImageView headerImageView;

	private LoginForms application;
	private static I18n i18n = VpnI18N.getI18n();
	private static Logger log = Util.getLogger(LoginForms.class.getCanonicalName());
>>>>>>> 32656c998715dfdf2cb3c2b13af96c74a646dc3b
    @FXML
    private Pane backLabelPane;
    @FXML
    private ImageView backImageVeiw;
<<<<<<< HEAD

    private ObservableList<VpnSelectionFXModel> vpnData = FXCollections.observableArrayList();

    /**
     * The constructor. The constructor is called before the initialize()
     * method.
     */
    public VpnSelectDialogController() {

    }
    // Event Listener on Button[#selectVpnButton].onAction

    // Set Autoconnect like former constructor 
    public boolean isAutoConnect() {
        return autoConnect;
    }

    public void setAutoConnect(boolean autoConnect) {
        log.debug("We have set autoconnect");
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
            alert.setHeaderText(i18n.tr("Kein Vpn ausgewählt"));
            alert.setContentText(i18n.tr("Bitte wähle einen VPN aus der Liste um fortzufahren"));
            alert.showAndWait();
        } else {
            rememberSelectionIfDesired(selectedItem.getVpn());

            this.shellfireService.selectVpn(selectedItem.getVpn());
            /*
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ShellfireVPNMainFormFxml.fxml"));
        log.debug("Resource is found in: " +loader.getLocation());
        Parent mainFormParent = (Parent)loader.load();
        ShellfireVPNMainFormFxmlController mainFormController = loader.<ShellfireVPNMainFormFxmlController>getController();
        mainFormController.displayMessage("Loaded from vpnSelect Dialog");
        Scene mainFormScene = new Scene(mainFormParent);
        Stage mainFormStage = (Stage)((Node)event.getSource()).getScene().getWindow();
       this.application.shellFireMainController = mainFormController;
        mainFormStage.setScene(mainFormScene);
        mainFormStage.show();
             */
            this.application.loadShellFireMainController();
            this.application.shellFireMainController.displayMessage("Creation of object successful");
            this.application.shellFireMainController.setSerciceAndInitialize(this.shellfireService);
            this.application.getStage().show();
            log.debug("Testing APP mainForm " + this.application.shellFireMainController.getId());

            this.application.shellFireMainController.afterLogin(autoConnect);

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
        //this.backLabel.setTextFill(Color.gray(USE_PREF_SIZE, USE_PREF_SIZE));
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
        // firstNameColumn.setCellValueFactory(cellData -> cellData.getValue().firstNameProperty());
        idTbleColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        typeTbleColumn.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        accArtTbleColumn.setCellValueFactory(cellData -> cellData.getValue().accountArtProperty());

        //this.shellfireService = WebService.getInstance();
        initComponents();

        // Listen for selection changes and store the selected VPN.
        vpnListTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue)
                -> {
            rememberSelectionIfDesired(newValue.getVpn());
            this.shellfireService.selectVpn(newValue.getVpn());
            //TODO
            // Implement after login of ShellfireMainForm
            //handleSelectVpnButton(null);
        });

    }

    public void initComponents() {

        this.headerImageView.setImage(ShellfireVPNMainFormFxmlController.getLogo());
        this.numAccountVpnLabel.setText(
                i18n.tr("In deinem Shellfire Account wurden mehrere VPN gefunden. \nBitte wähle den VPN aus, den du benutzen möchtest"));
        this.backLabel.setText(i18n.tr("zurück"));
        this.selectVpnButton.setText(i18n.tr("VPN auswählen"));
        this.fAutoconnect.setText(i18n.tr("Auswahl merken"));
        this.vpnSelectLabel.setText(i18n.tr("vpn auswahl"));
        this.vpnTypeLabel.setText(
                i18n.tr("Hinweis: Die VPN Typen PPTP und L2TP/IPSec müssen \n nach der Auswahl zunächst auf OpenVPN gewechselt werden,\n damit sf vpn die Verbindung herstellen kann."));
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
        this.backImageVeiw.setImage(Util.getImageIconFX("src/main/resources//icons/sfvpn2-idle.png"));
    }

    private void initVpnSelectTable(LinkedList<Vpn> allVpn) {
        this.vpnData.addAll(getVpnSelectionModelFromVpn(allVpn));
        this.vpnListTable.setItems(vpnData);
    }

    /**
     * Generate VpnSelectionFXModels From a set of given VPNs The
     * VpnSelectionFXModel are units to be displayed on VpnSelect Table
     *
     * @param LinkedList<Vpn> the list of available VPNs
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
        this.application = applic;
    }

=======
	
	/**
	 * The constructor.
	 * The constructor is called before the initialize() method.
	 */
	public VpnSelectDialogController() {
		super();
	}
	// Event Listener on Button[#selectVpnButton].onAction
	@FXML
	public void handleSelectVpnButton(ActionEvent event) {
		// TODO Autogenerated
	}
	// Event Listener on CheckBox[#fAutoconnect].onAction
	@FXML
	public void handlefAutoconnect(ActionEvent event) {
		// TODO Autogenerated
	}
	// Event Listener on ImageView[#exitImageView].onContextMenuRequested
	public void handleEXitButtonClicked(ContextMenuEvent event) {
		// TODO Autogenerated
	}
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
		// firstNameColumn.setCellValueFactory(cellData -> cellData.getValue().firstNameProperty());
		//idTbleColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject() );
		//typeTbleColumn.setCellValueFactory(cellData -> cellData.getValue().typeProperty() );
		//accArtTbleColumn.setCellValueFactory(cellData -> cellData.getValue().accountArtProperty() );
		
		initComponents();
	}
	
	public void initComponents(){
		
		this.headerImageView.setImage(ShellfireVPNMainFormFxmlController.getLogo());
		this.numAccountVpnLabel.setText(
				i18n.tr("<html>In deinem Shellfire Account wurden mehrere VPN gefunden.<br />Bitte wähle den VPN aus, den du benutzen möchtest</html>"));
	}

    @FXML
    private void handleBackLabel(MouseEvent event) {
    }
>>>>>>> 32656c998715dfdf2cb3c2b13af96c74a646dc3b
}
