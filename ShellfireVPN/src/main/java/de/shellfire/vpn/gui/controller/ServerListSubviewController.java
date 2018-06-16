/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui.controller;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.gui.model.CountryMap;
import de.shellfire.vpn.gui.model.ServerListFXModel;
import de.shellfire.vpn.gui.renderer.StarImageRendererFX;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.types.Country;
import de.shellfire.vpn.types.Server;
import de.shellfire.vpn.types.ServerType;
import de.shellfire.vpn.types.VpnProtocol;
import de.shellfire.vpn.webservice.ServerList;
import de.shellfire.vpn.webservice.WebService;
import de.shellfire.vpn.webservice.model.VpnStar;
import java.net.URL;
import java.util.Date;
import java.util.LinkedList;
import java.util.Random;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableCell;
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
    private TableColumn<ServerListFXModel, Server> countryColumn;
    @FXML
    private TableColumn<ServerListFXModel, String> nameColumn;
    @FXML
    private TableColumn<ServerListFXModel, String> serverColumn;
    @FXML
    private TableColumn<ServerListFXModel, VpnStar> securityColumn;
    @FXML
    private TableColumn<ServerListFXModel, VpnStar> speedColumn;
    
    private static I18n i18n = VpnI18N.getI18n();
    private WebService shellfireService;
    private ServerList serverList;
    private static final Logger log = Util.getLogger(ServerListSubviewController.class.getCanonicalName());
    private ObservableList<ServerListFXModel> serverListData = FXCollections.observableArrayList();

    /**
     * Constructor used to initialize serverListTable data from Webservice
     *
     * @param shellfireService used to get the serverList data
     */
    public ServerListSubviewController(WebService shellfireService) {
        this.shellfireService = shellfireService;
        //initComponents();
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
    
    public ImageView getKeyBuyRadioButton() {
        return keyBuyRadioButton;
    }
    
    public ImageView getConnectImage2() {
        return connectImage2;
    }
    
    public ToggleGroup getNetworkTypeToggleGroup() {
        return networkTypeToggleGroup;
    }
    
    public void initComponents() {
        this.serverList = this.shellfireService.getServerList();
        //LinkedList<ServerListFXModel> serverData = 
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

        //accArtTbleColumn.setCellValueFactory(cellData -> cellData.getValue().accountArtProperty());
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
                        // get the corresponding country of this server
                        Country country = item.getCountry();
                        // Attach the imageview to the cell
                        setGraphic(new ImageView(CountryMap.getIconFX(country)));
                        getGraphic().setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                        setText(VpnI18N.getCountryI18n().getCountryName(country));
                    }
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
        this.keyBuyRadioButton.managedProperty().bind(this.keyBuyRadioButton.visibleProperty());
        this.keyBuyRadioButton.setVisible(false);
        this.connectImage2.setVisible(false);
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
            log.debug("ServerListSubviewController: " + serverModel.getCountry());
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
                ServerListFXModel serverModel = serverListTableView.getSelectionModel().getSelectedItem();

		//The getCountry method of ServerListFXModel returns the server object
		log.debug("getSelectedServer() - returning: " + serverModel.getCountry());
		return serverModel.getCountry();
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
}
