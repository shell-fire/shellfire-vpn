/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui.controller;

import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.TrayIcon;
import java.net.URL;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.client.Controller;
import de.shellfire.vpn.gui.LoginForms;
import de.shellfire.vpn.gui.ServerImageBackgroundManager;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.types.Server;
import de.shellfire.vpn.types.ServerType;
import de.shellfire.vpn.webservice.WebService;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 * FXML Controller class
 *
 * @author Tcheutchoua Steve
 */
public class AppScreenControllerStatus implements Initializable, AppScreenController {

	@FXML
	private Pane contentDetailsPane;
	@FXML
	private ImageView statusConnectionImageView;
	@FXML
	private WebView locationMap;
	@FXML
	private Label labelConnect;
	@FXML
	private Rectangle rectConnectButton;
	@FXML
	private Label labelConnectionStatus;
	@FXML
	private Label labelConnectionStatusText;
	@FXML
	private Rectangle rectCrowns;
	
	private LoginForms application;
	private static final Logger log = Util.getLogger(AppScreenControllerStatus.class.getCanonicalName());
	private static I18n i18n = VpnI18N.getI18n();
	private Controller controller;
	private WebService shellfireService;
	private MenuItem popupConnectItem;
	private PopupMenu popup;
	private TrayIcon trayIcon;
	private ShellfireVPNMainFormFxmlController mainController;
	String baseImageUrl = "src/main/resources";

	String langKey = VpnI18N.getLanguage().getKey();
	private Image imageStatusEncrypted = imageStatusEncrypted = new Image("/icons/status-encrypted-width736.gif");
	private Image imageButtonDisconnect = new Image("/buttons/button-disconnect-" + langKey + ".gif");
	private Image imageStatusUnencrypted = new Image("/icons/status-unencrypted-width736.gif");
	private Image imageButtonConnect = new Image("/buttons/button-connect-" + langKey + ".gif");
	private boolean initialized;
	private WebEngine webEngine;
	protected boolean mapLoaded = false;
	private double mapLat;
	private double mapLng;

	public ImageView getStatusConnectionImageView() {
		return statusConnectionImageView;
	}


	public void connectButtonDisable(boolean disable) {
		// this.connectButton.setDisable(disable);
	}

	public void setConnectImageView(ImageView connectImageView) {
		// this.connectImageView = connectImageView;
	}

	public void setStatusConnectionImageView(ImageView statusConnectionImageView) {
		this.statusConnectionImageView = statusConnectionImageView;
	}

	/**
	 * Initializes the controller class.
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		log.debug("initialize(url={}) - initialized: {} - start", url.toString(), this.initialized);
		if (!this.initialized) {
			
			// makes product key to be disable when disable is set to true
			// this.connectImageView.managedProperty().bind(this.connectImageView.visibleProperty());
			
			 webEngine = locationMap.getEngine();
			 webEngine.setJavaScriptEnabled(true);
			 webEngine.load(getClass().getResource("map.html").toString());
			 webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
				    @Override
				    public void changed(ObservableValue<? extends Worker.State> ov, Worker.State t, Worker.State t1) {
				        if (t1 == Worker.State.SUCCEEDED) {
				            mapLoaded  = true;        
				            log.debug("Map has now been loaded - will process changes from now on");
				            setLocation();
				            application.instance.loginProgressDialog.hide();

				        }
				    }
			});
			 
						 
			// this.premiumInfoImageView.setVisible(false);
			log.debug("After initialization of images");
		}
		this.initialized = true;
	}
	
	public void setMapConnected() {
		if (mapLoaded) {
			Platform.runLater(() -> {
				webEngine.executeScript("document.setConnected();");
			});
		}
	}

	public void setMapDisconnected() {
		if (mapLoaded) {
			Platform.runLater(() -> {
				webEngine.executeScript("document.setDisconnected();");
			});

		}
	}

	public void setLocation() {
		log.debug("setLocation() called without params - checking if remembered coordinates are stored");
		if (this.mapLng != 0 && this.mapLat != 0) {
			log.debug("coordinates stored - setting them on the map");
			this.setLocation(this.mapLng, this.mapLat);
			this.mapLng = 0;
			this.mapLat = 0;
		} else {
			log.debug("no coordinates available - doing nothing");
		}
	}
	
	public void setLocation(double lng, double lat) {
		log.debug("setLocation({}, {})", lng, lat);
		if (mapLoaded) {
			Platform.runLater(() -> {
				log.debug("executing: setLocation({}, {})", lng, lat);
				webEngine.executeScript("document.setPosition("+lng+", " + lat + ");");
			});

		} else {
			log.debug("setLocation - map not yet loaded - remembering coordinates for later setting");
			this.mapLng = lng;
			this.mapLat = lat;
		}
	}
	
	public void notifyThatNowVisible(boolean connected) {
		if (connected) {
			this.setMapConnected();
			Platform.runLater(() -> {
				this.rectConnectButton.setFill(Color.web("#c76673"));
				this.labelConnect.setText(i18n.tr("DISCONNECT"));
				this.labelConnectionStatus.setStyle("-fx-background-color: #74e495");
				this.labelConnectionStatusText.setText(i18n.tr("CONNECTED"));
				
			});
			
		} else {
			this.setMapDisconnected();
			Platform.runLater(() -> {
				this.rectConnectButton.setFill(Color.web("#74e495"));
				this.labelConnect.setText(i18n.tr("CONNECT"));
				this.labelConnectionStatus.setStyle("-fx-background-color: #c76673");
				this.labelConnectionStatusText.setText(i18n.tr("DISCONNECTED"));
			});
		}
	}

	public void setSelectedServer(int serverId) {
		log.debug("setSelectedServer(" + serverId + ") - updating background image");
		
		Image image;
		try {
			image = ServerImageBackgroundManager.getImage(serverId);
			Server server = shellfireService.getServerList().getServerByServerId(serverId);
			setLocation(server.getLatitude(), server.getLongitude());
			this.statusConnectionImageView.setImage(image);
			this.setServerType(server.getServerType());
			log.debug("background image updated");
		} catch (Exception e) {
			log.error("Error occured during loading of background image", e);
		}
	}	
	
	
	private void setServerType(ServerType serverType) {
		ImagePattern pattern = new ImagePattern(
				Util.getImageIconFX("/images/crowns_3_status.png")
			);
		
		//this.rectCrowns.setFill(pattern);
		
		// TODO Auto-generated method stub
		
	}


	public void setShellfireService(WebService shellfireService) {
		this.shellfireService = shellfireService;
	}


	
	public void initPremium(boolean freeAccount) {
		log.debug("AppScreenControllerStatus: initPremium is free? " + freeAccount);
		if (!freeAccount) {
			// this.productKeyImageView.setVisible(false);
			// this.premiumInfoImageView.setVisible(false);
		} else {
			// this.productKeyImageView.setVisible(false);
		}
	}

	public String displayCreationMessage(String msg) {
		return ("AppScreenControllerStatus: " + msg);
	}

	@FXML
	private void handleProductKeyImageViewMouseExited(MouseEvent event) {
		this.application.getStage().getScene().setCursor(Cursor.DEFAULT);
	}

	@FXML
	private void handleProductKeyImageViewMouseEntered(MouseEvent event) {
		this.application.getStage().getScene().setCursor(Cursor.HAND);
	}

	@FXML
	private void handleProductKeyImageViewContext(ContextMenuEvent event) {
	}

	@FXML
	private void handleProductKeyImageViewClicked(MouseEvent event) {
	}

	public void setApp(LoginForms app) {
		this.application = app;
	}

	public void setParentController(ShellfireVPNMainFormFxmlController shellController) {
		this.mainController = shellController;
	}

	@FXML
	private void handleConnectButtonExited(MouseEvent event) {
		this.application.getStage().getScene().setCursor(Cursor.DEFAULT);
	}

	@FXML
	private void handleConnectButtonEntered(MouseEvent event) {
		this.application.getStage().getScene().setCursor(Cursor.HAND);
	}

	@FXML
	private void handleConnectButtonAction(MouseEvent event) {

		Platform.runLater(() -> {
			this.application.shellfireVpnMainController.connectFromButton();
		});

	}

	@FXML
	private void premiumButtonExited(MouseEvent event) {
		this.application.getStage().getScene().setCursor(Cursor.DEFAULT);
	}

	@FXML
	private void premiumButtonEntered(MouseEvent event) {
		this.application.getStage().getScene().setCursor(Cursor.HAND);
	}

	@FXML
	private void premiumButtonClicked(MouseEvent event) {
		WebService service = WebService.getInstance();
		Util.openUrl(service.getUrlPremiumInfo());
	}

	@FXML
	private void premiumButtonOnAction(ActionEvent event) {
		premiumButtonClicked(null);
	}

}