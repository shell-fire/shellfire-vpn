/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui.controller;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.gui.model.ServerListFXModel;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.types.Server;
import de.shellfire.vpn.webservice.ServerList;
import de.shellfire.vpn.webservice.WebService;
import java.net.URL;
import java.util.LinkedList;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;

/**
 * FXML Controller class
 *
 * @author Tcheutchoua Steve
 */
public class ServerListSubviewController implements Initializable {

    @FXML
    private AnchorPane serverListAnchorPane;
    @FXML
    private TableView<ServerListFXModel> serverListTableView;
    @FXML
    private Label selectServerLabel;
    @FXML
    private Label connectionTypeLabel;
    @FXML
    private RadioButton UDPRadioButton;
    @FXML
    private RadioButton TCPRadioButton;
    @FXML
    private ImageView connectImage1;
    @FXML
    private ImageView keyBuyRadioButton;
    @FXML
    private ImageView connectImage2;
    @FXML
    private ToggleGroup networkTypeToggleGroup;
    @FXML
    private TableColumn<ServerListFXModel, String> countryColunm;
    @FXML
    private TableColumn<ServerListFXModel, String> nameColumn;
    @FXML
    private TableColumn<ServerListFXModel, String> serverColumn;
    @FXML
    private TableColumn<ServerListFXModel, String> securityColumn;
    @FXML
    private TableColumn<ServerListFXModel, String> speedColumn;

    private static I18n i18n = VpnI18N.getI18n();
    private WebService shellfireService;
    private ServerList serverList;
    private static final Logger log = Util.getLogger(ServerListSubviewController.class.getCanonicalName());
    private ObservableList<ServerListFXModel> serverListData = FXCollections.observableArrayList();

     /**
     * Constructor used to initialize serverListTable data from Webservice
     * @param shellfireService used to get the serverList data
     */
    public ServerListSubviewController(WebService shellfireService) {
        this.shellfireService = shellfireService;
        initComponents();
    }

     /**
     * No argument constructor used by javafx framework 
     * 
     */
    public ServerListSubviewController() {
    }
    
    
    
    public TableView<ServerListFXModel> getServerListTableView() {
        return serverListTableView;
    }

    public RadioButton getUDPRadioButton() {
        return UDPRadioButton;
    }

    public RadioButton getTCPRadioButton() {
        return TCPRadioButton;
    }

    public ImageView getConnectImage1() {
        return connectImage1;
    }

    public ImageView getKeyBuyRadioButton() {
        return keyBuyRadioButton;
    }

    public ImageView getConnectImage2() {
        return connectImage2;
    }

    public ToggleGroup getNetworkTypeToggleGroup() {
        return networkTypeToggleGroup;
    }
    
    public void initComponents(){
        this.serverList = this.shellfireService.getServerList();
        LinkedList<ServerListFXModel> serverData = initServerTable(this.shellfireService.getServerList().getAll());
        serverListData.addAll(serverData);
        serverListTableView.setItems(serverListData);
    }
    
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.selectServerLabel.setText(i18n.tr("Wähle einen Server für deine Verbindung"));
        this.connectionTypeLabel.setText(i18n.tr("Verbindungstyp"));
        this.TCPRadioButton.setText(i18n.tr("TCP (funktioniert auch bei sicheren Firewalls und Proxy-Servern.)"));
        this.UDPRadioButton.setText(i18n.tr("UDP (schnell)"));
        
    }    
    
    	private LinkedList<ServerListFXModel> initServerTable(LinkedList<Server> servers) {
            LinkedList<ServerListFXModel> allModels = new LinkedList<>();
            log.debug("ServerListSubviewController: The size of all servers is " + servers.size());
            for(Server server : servers){
                ServerListFXModel serverModel = new ServerListFXModel();
                serverModel.setLand(server.getCountryString());
                serverModel.setName(server.getName());
                // Will change security to match model later
                serverModel.setSecurity(server.getSecurity().toString());
                serverModel.setSpeed(server.getServerSpeed().toString());
                allModels.add(serverModel);
            }
            return allModels;
	}


    
}
