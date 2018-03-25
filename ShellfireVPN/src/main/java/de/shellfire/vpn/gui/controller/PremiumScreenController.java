/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

/**
 *
 * @author Tcheutchoua Steve
 */
public class PremiumScreenController {

    @FXML
    private TextArea licenceTextArea;
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
    private void handleAcceptButton(ActionEvent event) {
    }
    
    void setDelay(int i) {
     String text = i + "s";
    this.remainingTimeValue.setText(text);
  }

    @FXML
    private void handleBuyPremium(ActionEvent event) {
    }
}
