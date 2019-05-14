  /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui.controller;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.gui.LoginForms;
import static de.shellfire.vpn.gui.controller.ShellfireVPNMainFormFxmlController.currentSidePane;
import de.shellfire.vpn.gui.model.CountryMap;
import de.shellfire.vpn.gui.model.ServerListFXModel;
import de.shellfire.vpn.gui.renderer.StarImageRendererFX;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.types.Country;
import de.shellfire.vpn.types.Server;
import de.shellfire.vpn.types.ServerType;
import de.shellfire.vpn.types.VpnProtocol;
import de.shellfire.vpn.webservice.ServerList;
import de.shellfire.vpn.webservice.Vpn;
import de.shellfire.vpn.webservice.WebService;
import de.shellfire.vpn.webservice.model.VpnStar;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.Random;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;
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
    private ImageView connectImage2;
    @FXML
    private ToggleGroup networkTypeToggleGroup;
    @FXML
    private TableColumn<ServerListFXModel, Server> countryColumn;
    @FXML
    private TableColumn<ServerListFXModel, String> nameColumn;
    @FXML
    private TableColumn<ServerListFXModel, String> serverColumn;
    @FXML
    private TableColumn<ServerListFXModel, VpnStar> securityColumn;
    @FXML
    private TableColumn<ServerListFXModel, VpnStar> speedColumn;
    @FXML
    private ImageView keyBuyImgeButton;
    @FXML
    private Button connectButton1;


    @FXML
    private void handleKeyBuyImgeButtonExited(MouseEvent event) {
    }

    @FXML
    private void handleKeyBuyImgeButtonEnterd(MouseEvent event) {
    }

    @FXML
    private void handleKeyBuyImgeButtonContextRequested(ContextMenuEvent event) {
    }

    @FXML
    private void handleKeyBuyImgeButtonClicked(MouseEvent event) {
    }

    @FXML
    private void handleConnectImage2MouseExited(MouseEvent event) {
    }

    @FXML
    private void handleConnectImage2MouseEntered(MouseEvent event) {
    }

    @FXML
    private void handleConnectImage2ContextRequested(ContextMenuEvent event) {
    }

    @FXML
    private void handleConnectImage2MouseClicked(MouseEvent event) {
    }
    
    private static I18n i18n = VpnI18N.getI18n();
    public static Vpn currentVpn;
    private WebService shellfireService;
    private ServerList serverList;
    private LoginForms application;
    private static final Logger log = Util.getLogger(ServerListSubviewController.class.getCanonicalName());
    private ObservableList<ServerListFXModel> serverListData = FXCollections.observableArrayList();
    private ShellfireVPNMainFormFxmlController mainFormController ; 

    /**
     * Constructor used to initialize serverListTable data from Webservice
     *
     * @param shellfireService used to get the serverList data
     */
    public ServerListSubviewController(WebService shellfireService) {
        this.shellfireService = shellfireService;
        currentVpn = shellfireService.getVpn();        
        initComponents();
    }

    /**
     * No argument constructor used by javafx framework
     *
     */
    public ServerListSubviewController() {
    }
    
    public void setShellfireService(WebService shellfireService) {
        this.shellfireService = shellfireService;
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
     
    public ImageView getConnectImage2() {
        return connectImage2;
    }
    
    public ToggleGroup getNetworkTypeToggleGroup() {
        return networkTypeToggleGroup;
    }
    
    public void setsetConnetImage1Disable(boolean enable){
         this.connectButton1.setDisable(enable);
    }
    
    public void initComponents() {
        this.serverList = this.shellfireService.getServerList();
        this.serverListData.addAll(initServerTable(this.shellfireService.getServerList().getAll()));
        this.serverListTableView.setItems(serverListData);

    }
      
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.selectServerLabel.setText(i18n.tr("Select a Server for your connection"));
        this.connectionTypeLabel.setText(i18n.tr("Connection type"));
        this.TCPRadioButton.setText(i18n.tr("TCP (works with safe firewalls and proxies.)"));
        this.UDPRadioButton.setText(i18n.tr("UDP (fast)"));
        //this.connectImage1.setImage(new Image("\\buttons\\button-connect-de.gif"));
        this.connectButton1.setGraphic(connectImage1);
        this.connectButton1.setPadding(Insets.EMPTY);
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        serverColumn.setCellValueFactory(cellData -> cellData.getValue().serverTypeProperty());
        securityColumn.setCellValueFactory(cellData -> cellData.getValue().securityProperty());
        speedColumn.setCellValueFactory(cellData -> cellData.getValue().speedProperty());
        countryColumn.setCellValueFactory(cellData -> cellData.getValue().countryProperty());
        
        countryColumn.setCellFactory(column -> {
            //Set up the Table
            return new TableCell<ServerListFXModel, Server>() {
                                
                @Override
                protected void updateItem(Server item, boolean empty) {
                    super.updateItem(item, empty); //To change body of generated methods, choose Tools | Templates.
                    if (item == null) {
                        log.debug("ServerListSubviewController: Country Image and text could not be rendered");
                        setText("Empty");
                    } else {
                        if(shellfireService.getVpn().getServer().equals(item))
                            log.debug("****The current VPN has server " + item +" and id " + shellfireService.getVpn().getVpnId());
                        // get the corresponding country of this server
                        Country country = item.getCountry();
                        // Attach the imageview to the cell
                        setGraphic(new ImageView(CountryMap.getIconFX(country)));
                        getGraphic().setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                        setText(VpnI18N.getCountryI18n().getCountryName(country));
                    }
                    this.setDisable(true);
                    this.setDisabled(true);
                }
            };
        });
        
        speedColumn.setCellFactory(column -> {
            return new StarImageRendererFX() ;
        });
        
        securityColumn.setCellFactory(column -> {
            return new StarImageRendererFX() ;
        });
        
        this.connectImage2.managedProperty().bind(this.connectImage2.visibleProperty());
        this.connectImage1.managedProperty().bind(this.connectImage1.visibleProperty());
        this.keyBuyImgeButton.managedProperty().bind(this.keyBuyImgeButton.visibleProperty());
        this.keyBuyImgeButton.setVisible(false);
        this.connectImage2.setVisible(false);
    }
    
    public void afterInitialization(){
        this.connectImage1.imageProperty().bindBidirectional(this.mainFormController.getConnectionSubviewController().getConnectImageView().imageProperty());
    }   
    
    /**Updates buttons and other components when connection status changes 
     * @param isConnected boolean variable for the connection status
     */
    public void updateComponents(boolean isConnected){
          if (isConnected){
          this.connectImage1.setImage(new Image("/buttons/button-disconnect-" + VpnI18N.getLanguage().getKey() + ".gif"));     
          }
    }
    
    private LinkedList<ServerListFXModel> initServerTable(LinkedList<Server> servers) {
        LinkedList<ServerListFXModel> allModels = new LinkedList<>();
        //log.debug("ServerListSubviewController: The size of all servers is " + servers.size());
        for (int i = 0; i < servers.size(); i++) {
            ServerListFXModel serverModel = new ServerListFXModel();
            serverModel.setCountry(servers.get(i));
            serverModel.setName(servers.get(i).getName());
            serverModel.setServerType(servers.get(i).getServerType().toString());
            serverModel.setSecurity(servers.get(i).getSecurity());
            serverModel.setSpeed(servers.get(i).getServerSpeed());
            //log.debug("ServerListSubviewController: " + serverModel.getCountry());
            allModels.add(serverModel);
        }
        return allModels;
    }
    
        public void initPremium(boolean freeAccount) {
        if (!freeAccount) {
            this.connectImage2.setVisible(false);
        } else {
            this.connectImage2.setVisible(true);
        }
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
    
    //Selects a server on serverlist table based on the index (position) of the server
    public void setSelectedServer(int number){
        //Embeded in a Platform runner because we are modifying the UI thread. 
        Platform.runLater(new Runnable()
{
    @Override
    public void run()
    {
        serverListTableView.requestFocus();
        serverListTableView.getSelectionModel().select(number);
        serverListTableView.getFocusModel().focus(number);
    }
});
    }
            public VpnProtocol getSelectedProtocol() {
		if (this.UDPRadioButton.isSelected()) {
			return VpnProtocol.UDP;
		} else if (this.TCPRadioButton.isSelected()) {
			return VpnProtocol.TCP;
		}

		return null;
	}
            
    	public Server getSelectedServer() {
		
            log.debug("About to test server model to load");
                if (null == this.serverListTableView.getSelectionModel().getSelectedItem()){
                    log.debug("Return default server 18");
                   return this.shellfireService.getServerList().getServer(18);
                } else {
                ServerListFXModel serverModel = this.serverListTableView.getSelectionModel().getSelectedItem();                
                //The getCountry method of ServerListFXModel returns the server object
                log.debug("getSelectedServer() - returning: " + serverModel.getCountry());
		return serverModel.getCountry();
                }
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
     
     public void setApp(LoginForms app){
        this.application = app;
    }
     
    public void setMainFormController(ShellfireVPNMainFormFxmlController mainController){
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
    private void connectButton1Clicked(MouseEvent event) {
        this.application.shellFireMainController.connectFromButton(false);
    }

    @FXML
    private void connectButton1OnAction(ActionEvent event) {
        connectButton1Clicked(null);
    }
}
