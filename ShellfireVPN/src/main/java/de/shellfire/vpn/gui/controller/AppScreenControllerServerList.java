/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package de.shellfire.vpn.gui.controller;

import java.net.URL;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Random;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.gui.model.CountryMap;
import de.shellfire.vpn.gui.model.ServerRow;
import de.shellfire.vpn.gui.renderer.CrownImageRendererServer;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.types.Country;
import de.shellfire.vpn.types.Server;
import de.shellfire.vpn.types.ServerType;
import de.shellfire.vpn.webservice.ServerList;
import de.shellfire.vpn.webservice.Vpn;
import de.shellfire.vpn.webservice.WebService;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.StageStyle;

/**
 * FXML Controller class
 *
 * @author Tcheutchoua Steve
 */
public class AppScreenControllerServerList implements Initializable, AppScreenController {

	@FXML
	private AnchorPane serverListAnchorPane;
	@FXML
	private TableView<ServerRow> serverListTableView;
	@FXML
	private ToggleGroup networkTypeToggleGroup;
	@FXML
	private TableColumn<ServerRow, Server> countryColumn;
	@FXML
	private TableColumn<ServerRow, Server> nameColumn;
	@FXML
	private TableColumn<ServerRow, Server> speedColumn;
	@FXML
	private TextField searchField;
	@FXML
	private Label filterLabel;
	@FXML
	private ImageView filterCrown_1;
	@FXML
	private ImageView filterCrown_2;
	@FXML
	private ImageView filterCrown_3;
	
	private HashMap<ServerType, Image> filterOnMap;
	private HashMap<ServerType, Image> filterOffMap;
	
	private Image crown1_selected = Util.getImageIconFX("/images/crowns_1.png");
	private Image crown2_selected = Util.getImageIconFX("/images/crowns_2.png");
	private Image crown3_selected = Util.getImageIconFX("/images/crowns_3.png");
	
	private Image crown1_deselected = Util.getImageIconFX("/images/crowns_1_disabled.png");
	private Image crown2_deselected = Util.getImageIconFX("/images/crowns_2_disabled.png");
	private Image crown3_deselected = Util.getImageIconFX("/images/crowns_3_disabled.png");
	
	private boolean inSelectionChangeListener;
	private static I18n i18n = VpnI18N.getI18n();
	public static Vpn currentVpn;
	private WebService shellfireService;
	private ServerList serverList;
	private static final Logger log = Util.getLogger(AppScreenControllerServerList.class.getCanonicalName());
	private ObservableList<ServerRow> serverListData = FXCollections.observableArrayList();
	private FilteredList<ServerRow> filteredDataByText;
	private FilteredList<ServerRow> filteredDataByServerType;
	private ShellfireVPNMainFormFxmlController mainFormController;
	private int selectedServerId;
	private boolean filterFree = false;
	private boolean filterPremium = false;
	private boolean filterPremiumPlus = false;
	private boolean currentlyUpdatingServerTypeFilter;
	private Vpn selectedVpn;
	private Server mostRecentSelectedServer;
	

	/**
	 * Constructor used to initialize serverListTable data from Webservice
	 *
	 * @param shellfireService
	 *            used to get the serverList data
	 */
	public AppScreenControllerServerList(WebService shellfireService) {
		this.shellfireService = shellfireService;
		currentVpn = shellfireService.getVpn();
		initComponents();
	}

	/**
	 * No argument constructor used by javafx framework
	 *
	 */
	public AppScreenControllerServerList() {
	}
	
	


	public TableView<ServerRow> getServerListTableView() {
		return serverListTableView;
	}

	public ToggleGroup getNetworkTypeToggleGroup() {
		return networkTypeToggleGroup;
	}

	public void initComponents() {
		
		
		// this.serverListTableView.setItems(serverListData);
		// this.serverListTableView.comp
		
	}

	/**
	 * Initializes the controller class.
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		shellfireService = WebService.getInstance();
		updateSelectedVpn();
		speedColumn.setCellValueFactory(cellData -> cellData.getValue().countryProperty());
		countryColumn.setCellValueFactory(cellData -> cellData.getValue().countryProperty());
		countryColumn.setComparator(new ServerListComparator());
		countryColumn.setStyle( "-fx-alignment: CENTER;");
		countryColumn.setCellFactory(column -> {
			// Set up the Table
			return new TableCell<ServerRow, Server>() {

				@Override
				protected void updateItem(Server item, boolean empty) {
					super.updateItem(item, empty);
					
					if (item != null) {
						if (shellfireService == null) {
							log.debug("shellfireService is null, setting it");
						}
						
						// get the corresponding country of this server
						Country country = item.getCountry();
						// Attach the imageview to the cell
						ImageView imageView = new ImageView(CountryMap.getIconFX(country));
						
						imageView.setFitHeight(50);
						imageView.setFitWidth(53);
						imageView.setPreserveRatio(false);
						
						setGraphic(imageView);
						getGraphic().setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);

					}
				}
			};
		});
		filterLabel.setText(i18n.tr("Filter"));
		
		nameColumn.setCellValueFactory(cellData -> cellData.getValue().countryProperty());
		nameColumn.setStyle( "-fx-alignment: CENTER;");
		nameColumn.setCellFactory(column -> {
            return new TableCell<ServerRow, Server>() {
                @Override
                protected void updateItem(Server item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setGraphic(null);
                    } else {
                    	Server selectedServer = getSelectedServer();
                    	boolean isSelected = false;
                    	if (selectedServer != null) {
                    		isSelected = selectedServer.equals(item);
                    	}
                        updateItemSelection(item, isSelected);
                    }
                }
                
                private void updateItemSelection(Server item, boolean selected) {
                    // update for HBox for non-empty cells based on selection
                    // Generate Textflow with variable length
                    TextFlow textFlow = new TextFlow();
                    textFlow.setPrefHeight(Region.USE_COMPUTED_SIZE);
                    
                    Text text1 = new Text(item.getCity() + " ");
                    text1.setFont(Font.font("Verdana", FontWeight.MEDIUM, 14));
                    
                    Text text2 = new Text(VpnI18N.getCountryI18n().getCountryName(item.getCountry()));
                    text2.setFont(Font.font("Verdana", FontWeight.MEDIUM, 14));
                    
                    Text text3 = new Text("\n" + item.getName());
                    text3.setFont(Font.font("Verdana", FontWeight.MEDIUM, 11));
                    
                    if (selected) {
                    	text1.setFill(Color.WHITE);
                    	text2.setFill(Color.WHITE);
                    	text3.setFill(Color.WHITE);
                    } else {
                        text1.setFill(Color.web("#666f78"));
                        text2.setFill(Color.web("#a6afb7"));
                        text3.setFill(Color.web("#a6afb7"));
                    }
                    
                    textFlow.getChildren().add(text1);
                    textFlow.getChildren().add(text2);
                    textFlow.getChildren().add(text3);
                    textFlow.setPrefHeight(30);
                    textFlow.setPadding(new Insets(5, 0, 0, 0));
                    textFlow.setStyle("-fx-text-alignment: left;");
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setGraphic(textFlow);
                }
            };
        });

		speedColumn.setStyle( "-fx-alignment: baseline-right;");
		speedColumn.setCellFactory(column -> {
			return new CrownImageRendererServer(this);
		});

		this.serverList = this.shellfireService.getServerList();
		this.serverListData.addAll(initServerTable(this.serverList.getAll()));

		filteredDataByText = new FilteredList<>(serverListData, p -> true);

		searchField.setPromptText(i18n.tr("Type to filter..."));
		searchField.textProperty().addListener((observable, oldValue, newValue) -> {
			filteredDataByText.setPredicate(server -> {
				// If filter text is empty, display all servers.
				if (newValue == null || newValue.isEmpty()) {
					return true;
				}
				
				return server.getServer().matchesFilter(newValue);
			});

			selectServerOfCurrentVpn();

		});		
		
		
		filteredDataByServerType = new FilteredList<>(filteredDataByText, p -> true);

		selectServerOfCurrentVpn();		
		
		
	
		// Add sorted (and filtered) data to the table.
		serverListTableView.setItems(filteredDataByServerType);
		
		serverListTableView.refresh();
		serverListTableView.getStyleClass().add("noheader");
		
		ObservableList<ServerRow> selectedItems = serverListTableView.getSelectionModel().getSelectedItems();

		selectedItems.addListener(new ListChangeListener<ServerRow>() {
		  @Override
		  public void onChanged(Change<? extends ServerRow> change) {
			
			  if (inSelectionChangeListener || currentlyUpdatingServerTypeFilter) {
				  return; 
			  }
			  
			  if (mainFormController != null) {

				  {
					  while (change.next()) {
						  ObservableList<? extends ServerRow> changes = change.getList();

						  for (ServerRow curChange : changes) {
							  inSelectionChangeListener = true;
							  Server newServer = curChange.getServer();
							  mainFormController.setSelectedServer(newServer.getServerId(), false);
							  inSelectionChangeListener = false;
						  }
					  }
					  
				  }
					  // else
				  {
					//  log.debug("change NOT instanceof MappingChange<?, ?>");
				  }
			  } else {
				  log.debug("mainFormController == null");
			  }
			  
			  
		  }
		});
		
		
		filterOnMap = new HashMap<ServerType, Image>();
		filterOnMap.put(ServerType.Free, crown1_selected);
		filterOnMap.put(ServerType.Premium, crown2_selected);
		filterOnMap.put(ServerType.PremiumPlus, crown3_selected);
		
		filterOffMap = new HashMap<ServerType, Image>();
		filterOffMap.put(ServerType.Free, crown1_deselected);
		filterOffMap.put(ServerType.Premium, crown2_deselected);
		filterOffMap.put(ServerType.PremiumPlus, crown3_deselected);
		
		
		this.searchField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
		    if (event.getCode() == KeyCode.ESCAPE) {
		        event.consume();
		        this.searchField.setText("");
		    }
		});
		
		this.serverListTableView.setRowFactory(tv -> {
		    TableRow<ServerRow> row = new TableRow<>();
		    row.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
		    	
		    	boolean abort = false;
		    	ServerType clickedServerType = row.getItem().getServerType();
		    	ServerType selectedVpnServerType = selectedVpn.getAccountType();
		    	
		    	if (selectedVpnServerType == ServerType.Free && clickedServerType != ServerType.Free) {
	    			abort = true;
		    	}
		    	else if (selectedVpnServerType == ServerType.Premium && clickedServerType == ServerType.PremiumPlus) {
	    			abort = true;
		    	}
		    	
				if (abort) {
					boolean userCouldSwitchToHigherClassifiedVpn = false;
					
					ServerType highestVpnTypeOfUser = ServerType.Free;
					LinkedList<Vpn> allVpn = this.shellfireService.getAllVpn(); 
					
					for (Vpn curVpn : allVpn) {
						if (curVpn.getAccountType() == ServerType.PremiumPlus) {
							highestVpnTypeOfUser = ServerType.PremiumPlus;
							break;
						} else if (curVpn.getAccountType() == ServerType.Premium && highestVpnTypeOfUser == ServerType.Free) {
							highestVpnTypeOfUser = ServerType.Premium;
							break;
						}
					}
					
					
					
					if (clickedServerType == ServerType.PremiumPlus && highestVpnTypeOfUser == ServerType.PremiumPlus) {
						userCouldSwitchToHigherClassifiedVpn = true;
					}
					if (clickedServerType == ServerType.Premium && (highestVpnTypeOfUser == ServerType.Premium || highestVpnTypeOfUser == ServerType.PremiumPlus)) {
						userCouldSwitchToHigherClassifiedVpn = true;
					}
					
					if (userCouldSwitchToHigherClassifiedVpn) {
						Alert alert = new Alert(Alert.AlertType.ERROR, i18n.tr("Select a different VPN?"), ButtonType.YES, ButtonType.NO);
						alert.initStyle(StageStyle.UTILITY);
						alert.setTitle(i18n.tr("Error: You're not allowed to use this server."));
						alert.setHeaderText(String.format(i18n.tr("Current VPN has type: %s\r\nYou selected a server of type: %s\r\nThis is not possible with your current VPN. Please go to settings and select a VPN with higher classification."), selectedVpnServerType.name(), clickedServerType.name()));
						Optional<ButtonType> result = alert.showAndWait();
						boolean doOpenVpnSelectDialog = ((result.isPresent()) && (result.get() == ButtonType.YES));
						
						if (doOpenVpnSelectDialog) {
							this.mainFormController.getAppScreenControllerSettings().showVpnSelectScreen();
							this.mainFormController.setServerIdRejectedDueToPrivileges(row.getItem().getServer().getServerId());
						}

					} else {
						Alert alert = new Alert(Alert.AlertType.ERROR, i18n.tr("Show Details about Shellfire VPN Premium?"), ButtonType.YES, ButtonType.NO);
						alert.initStyle(StageStyle.UTILITY);
						alert.setTitle(i18n.tr("Error: You're not allowed to use this server."));
						alert.setHeaderText(String.format(i18n.tr("Current VPN has type: %s\r\nYou selected a server of type: %s\r\nThis is not possible with your current VPN. Would you like to get information about our Premium offer?"), selectedVpnServerType.name(), clickedServerType.name()));
						Optional<ButtonType> result = alert.showAndWait();
						boolean showPremiumInfo = ((result.isPresent()) && (result.get() == ButtonType.YES));
						
						if (showPremiumInfo) {
							Util.openUrl(this.shellfireService.getUrlPremiumInfo());
						}
					}
					
					
					e.consume();
				}

		    });
		    return row ;
		});
		
   
	}
	
	private void updaterFilterServerType() {
		Server curServer = this.getSelectedServer();
		if (curServer != null) {
			mostRecentSelectedServer = curServer;
		}
		currentlyUpdatingServerTypeFilter = true;
		filteredDataByServerType.setPredicate(server -> {
			return server.getServer().matchesFilter(filterFree , filterPremium, filterPremiumPlus);
		});
		selectServerOfCurrentVpn();
		currentlyUpdatingServerTypeFilter = false;
	}

	@FXML
	private void handleClickFilterCrown_1(MouseEvent event) {
		filterFree = !filterFree;
		updaterFilterServerType();
		
		if (filterFree) {
			filterCrown_1.setImage(crown1_selected);
		} else {
			filterCrown_1.setImage(crown1_deselected);
		}
	}

	@FXML
	private void handleClickFilterCrown_2(MouseEvent event) {
		filterPremium = !filterPremium;
		updaterFilterServerType();
		
		if (filterPremium) {
			filterCrown_2.setImage(crown2_selected);
		} else {
			filterCrown_2.setImage(crown2_deselected);
		}
		
	}
	
	@FXML
	private void handleClickFilterCrown_3(MouseEvent event) {
		filterPremiumPlus = !filterPremiumPlus;
		updaterFilterServerType();
		
		if (filterPremiumPlus) {
			filterCrown_3.setImage(crown3_selected);
		} else {
			filterCrown_3.setImage(crown3_deselected);
		}

	}
	
	public void selectServerOfCurrentVpn() {
		try {
			Server server = shellfireService.getServerList().getServerByServerId(selectedServerId);
			selectServer(server);
		} catch (Exception e) {
			log.error("ignore this... !? better check before...");
		}
	}	
	
	private void selectServer(Server server) {
		try {
			int index = filteredDataByServerType.indexOf(server);

			if (index != -1) {
				serverListTableView.getSelectionModel().clearAndSelect(index);
				serverListTableView.getSelectionModel().focus(index);
			}
			serverListTableView.refresh();
			
		} catch (Exception e) {
			log.error("ignore this... !? better check before...");
		}
	}
	
	private void scrollToCurrentServer() {
		Server server = shellfireService.getServerList().getServerByServerId(selectedServerId);
		int index = filteredDataByServerType.indexOf(server);
		if (index != -1) {
			serverListTableView.scrollTo(index);
		}
		
	}


	/**
	 * Updates buttons and other components when connection status changes
	 * 
	 * @param isConnected
	 *            boolean variable for the connection status
	 */
	public void notifyThatNowVisible(boolean isConnected) {
		if (isConnected) {
			serverListTableView.disableProperty().set(isConnected);
		}
	}

	private LinkedList<ServerRow> initServerTable(LinkedList<Server> servers) {
		LinkedList<ServerRow> allModels = new LinkedList<>();
		for (int i = 0; i < servers.size(); i++) {
			ServerRow serverModel = new ServerRow(servers.get(i));
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
		log.debug("setSelectedServer setting the selected server: {}", number);

		if (this.selectedServerId == number) {
			log.debug("Server {} already selected - returning", number);
			return;
		}
		
		this.selectedServerId = number;
		selectServer(this.shellfireService.getServerList().getServerByServerId(selectedServerId));
		
		if (!inSelectionChangeListener) {
			scrollToCurrentServer();
		}
		
	}

	public Server getSelectedServer() {

		
		ServerRow serverModel = this.serverListTableView.getSelectionModel().getSelectedItem();
		if (null == serverModel) {
			if (this.mostRecentSelectedServer != null) {
				return this.mostRecentSelectedServer;
			} else {
				return null;	
			}
		} else {
			return serverModel.getServer();
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

	public void setMainFormController(ShellfireVPNMainFormFxmlController mainController) {
		this.mainFormController = mainController;
	}
	class ServerListComparator implements Comparator<Server> {
		@Override
		public int compare(Server o1, Server o2) {
			return o1.getCountry().name().compareTo(o2.getCountry().name());
		}
	}
	public void updateSelectedVpn() {
		this.selectedVpn = shellfireService.getVpn();
	}
}
