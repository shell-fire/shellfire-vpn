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

import com.sun.javafx.collections.MappingChange;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.gui.LoginForms;
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
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

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

	private boolean inSelectionChangeListener;
	private static I18n i18n = VpnI18N.getI18n();
	public static Vpn currentVpn;
	private WebService shellfireService;
	private ServerList serverList;
	private static final Logger log = Util.getLogger(AppScreenControllerServerList.class.getCanonicalName());
	private ObservableList<ServerRow> serverListData = FXCollections.observableArrayList();
	private FilteredList<ServerRow> filteredData;
	private ShellfireVPNMainFormFxmlController mainFormController;
	private int selectedServerId;

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
                    	boolean isSelected = getSelectedServer().equals(item);
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

		filteredData = new FilteredList<>(serverListData, p -> true);

		searchField.textProperty().addListener((observable, oldValue, newValue) -> {
			filteredData.setPredicate(server -> {
				// If filter text is empty, display all servers.
				if (newValue == null || newValue.isEmpty()) {
					return true;
				}
				
				return server.getServer().matchesFilter(newValue);
			});

			selectServerOfCurrentVpn();

		});		
		
	
		// Add sorted (and filtered) data to the table.
		serverListTableView.setItems(filteredData);
		
		serverListTableView.refresh();
		serverListTableView.getStyleClass().add("noheader");
		
		ObservableList<ServerRow> selectedItems = serverListTableView.getSelectionModel().getSelectedItems();

		selectedItems.addListener(new ListChangeListener<ServerRow>() {
		  @Override
		  public void onChanged(Change<? extends ServerRow> change) {
			  if (inSelectionChangeListener) {
				  return; 
			  }
			  if (mainFormController != null) {
				  if (change instanceof MappingChange<?, ?>) {
					  while (change.next()) {
						  ObservableList<? extends ServerRow> changes = change.getList();

						  for (ServerRow curChange : changes) {
							  inSelectionChangeListener = true;
							  mainFormController.setSelectedServer(curChange.getServer().getServerId());

							  inSelectionChangeListener = false;
						  }
					  }
					  
				  }
			  }
			  
			  
		  }
		});
	}

	
	public void selectServerOfCurrentVpn() {
		try {
			Server server = shellfireService.getVpn().getServer();
			selectServer(server);
		} catch (Exception e) {
			log.error("ignore this... !? better check before...");
		}
	}	
	
	private void selectServer(Server server) {
		try {
			int index = filteredData.indexOf(server);

			if (index != -1) {
				serverListTableView.getSelectionModel().clearAndSelect(index);
			}
			serverListTableView.refresh();
			
		} catch (Exception e) {
			log.error("ignore this... !? better check before...");
		}
	}
	
	private void scrollToCurrentServer() {
		Server server = shellfireService.getServerList().getServerByServerId(selectedServerId);
		int index = filteredData.indexOf(server);
		if (index != -1) {
			scrollToPosition(index);
		}
		
	}
	private void scrollToPosition(int index) {
        final int numElementsVisible = 10;
        final int offset = numElementsVisible / 3;
        final int scrollPosition = java.lang.Math.max(0, index-offset);
       	serverListTableView.scrollTo(scrollPosition);
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
			return this.shellfireService.getServerList().getServerByServerId(18);
		} else {
			// The getCountry method of ServerListFXModel returns the server object
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
}
