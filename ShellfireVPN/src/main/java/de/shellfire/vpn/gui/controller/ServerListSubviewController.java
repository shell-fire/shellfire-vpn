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
import java.util.Random;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.gui.LoginForms;
import de.shellfire.vpn.gui.model.CountryMap;
import de.shellfire.vpn.gui.model.ServerListFXModel;
import de.shellfire.vpn.gui.renderer.StarImageRendererFX;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.types.Country;
import de.shellfire.vpn.types.Server;
import de.shellfire.vpn.types.ServerType;
import de.shellfire.vpn.webservice.ServerList;
import de.shellfire.vpn.webservice.Vpn;
import de.shellfire.vpn.webservice.WebService;
import de.shellfire.vpn.webservice.model.VpnStar;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

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
	public static Vpn currentVpn;
	private WebService shellfireService;
	private ServerList serverList;
	private LoginForms application;
	private static final Logger log = Util.getLogger(ServerListSubviewController.class.getCanonicalName());
	private ObservableList<ServerListFXModel> serverListData = FXCollections.observableArrayList();
	private ShellfireVPNMainFormFxmlController mainFormController;
	private Image buttonDisconnect = new Image("/buttons/button-disconnect-" + VpnI18N.getLanguage().getKey() + ".gif");

	/**
	 * Constructor used to initialize serverListTable data from Webservice
	 *
	 * @param shellfireService
	 *            used to get the serverList data
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

	public ToggleGroup getNetworkTypeToggleGroup() {
		return networkTypeToggleGroup;
	}

	public void initComponents() {
		this.serverList = this.shellfireService.getServerList();
		this.serverListData.addAll(initServerTable(this.shellfireService.getServerList().getAll()));
		// this.serverListTableView.setItems(serverListData);
		// this.serverListTableView.comp
		selectServerOfCurrentVpn();
	}

	/**
	 * Initializes the controller class.
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		this.selectServerLabel.setText(i18n.tr("Select a Server for your connection"));
		nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
		serverColumn.setCellValueFactory(cellData -> cellData.getValue().serverTypeProperty());
		securityColumn.setCellValueFactory(cellData -> cellData.getValue().securityProperty());
		speedColumn.setCellValueFactory(cellData -> cellData.getValue().speedProperty());
		countryColumn.setCellValueFactory(cellData -> cellData.getValue().countryProperty());
		countryColumn.setComparator(new ServerListComparator());
		countryColumn.setCellFactory(column -> {
			// Set up the Table
			return new TableCell<ServerListFXModel, Server>() {

				@Override
				protected void updateItem(Server item, boolean empty) {
					if (item != null) {
						if (shellfireService == null) {
							log.debug("shellfireService is null, setting it");
							setShellfireService(WebService.getInstance());
						}
						
						Vpn vpn = shellfireService.getVpn();
						if (vpn == null) {
							log.debug("vpn retrieved from service is null, cannot check if current server to log details.");
						} else {
							Server server = vpn.getServer();
							if (server == null) {
								log.debug("server retrieved from vpn is null, cannot check if current server to log details");
								log.debug("vpn details: {}", vpn.toString());
							} else {
								if (server.equals(item)) {
									log.debug("****The current VPN has server " + item + " and id " + shellfireService.getVpn().getVpnId()
											+ " and the type is " + shellfireService.getVpn().getAccountType());
								} else {
									// log.debug("server not equal to item");
								}
							}
						}
						
						// get the corresponding country of this server
						Country country = item.getCountry();
						// Attach the imageview to the cell
						ImageView imageView = new ImageView(CountryMap.getIconFX(country));
						imageView.setFitHeight(14);
						imageView.setFitWidth(18);
						setGraphic(imageView);
						getGraphic().setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
						setText(VpnI18N.getCountryI18n().getCountryName(country));
					}
				}
			};
		});

		speedColumn.setCellFactory(column -> {
			return new StarImageRendererFX();
		});

		securityColumn.setCellFactory(column -> {
			return new StarImageRendererFX();
		});

		// Wrap the FilteredList in a SortedList.
		SortedList<ServerListFXModel> sortedData = new SortedList<>(serverListData);
		// Bind the SortedList comparator to the TableView comparator.
		sortedData.comparatorProperty().bind(serverListTableView.comparatorProperty());
		// Add sorted (and filtered) data to the table.
		serverListTableView.setItems(sortedData);
		
		ObservableList<ServerListFXModel> selectedItems = serverListTableView.getSelectionModel().getSelectedItems();

		selectedItems.addListener(new ListChangeListener<ServerListFXModel>() {
		  @Override
		  public void onChanged(Change<? extends ServerListFXModel> change) {
			  if (mainFormController != null) {
				  ObservableList<? extends ServerListFXModel> changes = change.getList();
				  
				  for (ServerListFXModel curChange : changes) {
					  mainFormController.setSelectedServer(curChange.getCountry());
				  }
			  }
		  }
		});

	}

	public void selectServerOfCurrentVpn() {
		serverListTableView.requestFocus();
		serverListTableView.getSelectionModel().select(serverList.getServerNumberByServer(shellfireService.getVpn().getServer()));
		serverListTableView.getFocusModel().focus(serverList.getServerNumberByServer(shellfireService.getVpn().getServer()));
	}

	/**
	 * Updates buttons and other components when connection status changes
	 * 
	 * @param isConnected
	 *            boolean variable for the connection status
	 */
	public void updateComponents(boolean isConnected) {
		if (isConnected) {
			serverListTableView.disableProperty().set(isConnected);
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

	// Selects a server on serverlist table based on the index (position) of the server
	public void setSelectedServer(int number) {
		log.debug("setSelectedServer setting the selected server");
		// Embeded in a Platform runner because we are modifying the UI thread.
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				serverListTableView.requestFocus();
				serverListTableView.getSelectionModel().select(number);
				serverListTableView.getFocusModel().focus(number);
			}
		});
	}

	public Server getSelectedServer() {
		log.debug("getSelectedServer: About to test server model to load");
		ServerListFXModel serverModel = this.serverListTableView.getSelectionModel().getSelectedItem();
		if (null == serverModel) {
			log.debug("Return default server 18");
			return this.shellfireService.getServerList().getServer(18);
		} else {
			// The getCountry method of ServerListFXModel returns the server object
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

	public void setApp(LoginForms app) {
		this.application = app;
	}

	public void setMainFormController(ShellfireVPNMainFormFxmlController mainController) {
		this.mainFormController = mainController;
	}
	class ServerListComparator implements Comparator<Server> {
		@Override
		public int compare(Server o1, Server o2) {
			return o1.getCountry().name().compareTo(o2.getCountry().name());
		}
	}
}
