/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui.controller;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.gui.model.VpnComparisonFXTableModel;
import de.shellfire.vpn.webservice.WebService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

/**
 *
 * @author Tcheutchoua Steve
 */
public class PremiumScreenController {

    @FXML
    private Pane headerPanel;
    @FXML
    private ImageView headerImageView;
    @FXML
    private ScrollPane premiumScrollPane;
    @FXML
    private Label remainingTimeLabel;
    @FXML
    private Button cancelButton;
    @FXML
    private Button premiumButton;
    @FXML
    private Label remainingTimeValue;
    @FXML
    private Label premiumHeaderMessage;
    @FXML
    private TableView<VpnComparisonFXTableModel> premiumTableView;
    @FXML
    private TableColumn<VpnComparisonFXTableModel, String> connectionColumn;
    @FXML
    private TableColumn<VpnComparisonFXTableModel, String> freeColumn;
    @FXML
    private TableColumn<VpnComparisonFXTableModel, String> premiumColumn;
    @FXML
    private TableColumn<VpnComparisonFXTableModel, String> premiumPlusColumn;

    @FXML
    private void handleAcceptButton(ActionEvent event) {
    }
    
    void setDelay(int i) {
     String text = i + "s";
    this.remainingTimeValue.setText(text);
  }

    @FXML
    private void handleBuyPremium(ActionEvent event) {
            WebService service = WebService.getInstance();
    Util.openUrl(service.getUrlPremiumInfo());
    }
}
