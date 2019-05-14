/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui.controller;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.client.Controller;
import de.shellfire.vpn.gui.LoginForms;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.webservice.WebService;
import java.awt.Dimension;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;

/**
 * FXML Controller class
 *
 * @author Tcheutchoua Steve
 */
public class ConnectionSubviewController implements Initializable {

    @FXML
    private Pane contentDetailsPane;
    @FXML
    private ImageView statusConnectionImageView;
    @FXML
    private ImageView connectImageView;
    @FXML
    private ImageView productKeyImageView;
    @FXML
    private ImageView premiumInfoImageView;
    @FXML
    private Button premiumButton;
    @FXML
    private Button connectButton;
    private LoginForms application;
    private static final Logger log = Util.getLogger(ShellfireVPNMainFormFxmlController.class.getCanonicalName());
    private static I18n i18n = VpnI18N.getI18n();
    private Controller controller;
    private static WebService shellfireService;
    private MenuItem popupConnectItem;
    private PopupMenu popup;
    private TrayIcon trayIcon;
    private ShellfireVPNMainFormFxmlController mainController; 
    String baseImageUrl = "src/main/resources";
    String size = "736";
    String langKey = VpnI18N.getLanguage().getKey();

    
    
    public ImageView getStatusConnectionImageView() {
        return statusConnectionImageView;
    }
 
    public ImageView getConnectImageView() {
        return connectImageView;
    }

    public ImageView getProductKeyImageView() {
        return productKeyImageView;
    }

    public ImageView getPremiumInfoImageView() {
        return premiumInfoImageView;
    }
    
    public void connectButtonDisable(boolean disable){
        this.connectButton.setDisable(disable);
    }

    public void setConnectImageView(ImageView connectImageView) {
        this.connectImageView = connectImageView;
    }

    public void setStatusConnectionImageView(ImageView statusConnectionImageView) {
        this.statusConnectionImageView = statusConnectionImageView;
    }   
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // setting the scaling factor to adjust sizes 
        double scaleFactor = Util.getScalingFactor();
        log.debug("ScalingFactor: " + scaleFactor);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double width = screenSize.getWidth();

        
        if (width > 3000) {
            size = "1472";
        }
        log.debug("langKey: " + langKey);
        
        this.connectButton.setGraphic(connectImageView);
        this.connectButton.setPadding(Insets.EMPTY);
        this.premiumButton.setGraphic(premiumInfoImageView);
        this.premiumButton.setPadding(Insets.EMPTY);
       
        //makes product key to be disable when disable is set to true
        this.productKeyImageView.managedProperty().bind(this.productKeyImageView.visibleProperty());
        this.premiumInfoImageView.managedProperty().bind(this.premiumInfoImageView.visibleProperty());
        this.connectImageView.managedProperty().bind(this.connectImageView.visibleProperty());
        this.productKeyImageView.setVisible(false);
        this.premiumInfoImageView.setVisible(false);
        this.premiumButton.setVisible(false);
        log.debug("After initialization of images");
    }
    
    public void updateComponents(boolean connected){
          if (connected){
          this.statusConnectionImageView.setImage(new Image("/icons/status-encrypted-width" + size + ".gif"));
          this.connectImageView.setImage(new Image("/buttons/button-disconnect-" + langKey + ".gif"));     
          }
    }
    
    public void initPremium(boolean freeAccount) {
        log.debug("ConnectionSubviewController: initPremium is free? " + freeAccount);
        if (!freeAccount) {
            //this.productKeyImageView.setVisible(false);
            //this.premiumInfoImageView.setVisible(false);
        } else {
            //this.productKeyImageView.setVisible(false);
            this.premiumInfoImageView.setVisible(true);
            this.premiumButton.setVisible(true);
        }
    }

    public String displayCreationMessage(String msg) {
        return ("ConnectionSubviewController: " + msg);
    }

    public void productKeyDisable(boolean value) {
        this.productKeyImageView.setDisable(value);
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
    
    public void setApp(LoginForms app){
        this.application = app;
    }
    
    public void setParentController(ShellfireVPNMainFormFxmlController shellController){
        this.mainController = shellController ;
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
    private void handleConnectButtonClicked(MouseEvent event) {
        this.application.shellFireMainController.connectFromButton(false);
    }

    @FXML
    private void handleConnectButtonAction(ActionEvent event) {
        handleConnectButtonClicked(null);
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
