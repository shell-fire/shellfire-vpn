/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui.controller;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.i18n.VpnI18N;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import org.jdesktop.application.ResourceMap;
import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;

/**
 * FXML Controller class
 *
 * @author Tcheutchoua Steve
 */
public class TvStreasSubviewController implements Initializable {

  @FXML
  private AnchorPane serverListAnchorPane;
  @FXML
  private Button streamButton1;
  @FXML
  private Button streamButton2;
  @FXML
  private Button streamButton3;
  @FXML
  private Button streamButton4;
  @FXML
  private Button streamButton5;
  @FXML
  private Button streamButton6;
  @FXML
  private Button streamButton7;
  @FXML
  private Button streamButton8;
  @FXML
  private Label streamSelectLabel;
  @FXML
  private Label streamRequireUsIdLabel;
  private static final I18n i18n = VpnI18N.getI18n();

  /**
   * Initializes the controller class.
   */
  @Override
  public void initialize(URL url, ResourceBundle rb) {
    this.streamSelectLabel.setText("     " + i18n.tr("List of US TV streams (english language)"));
    this.streamRequireUsIdLabel.setText(i18n.tr(
        "Using these streams usually requires a US IP address.You can get one by connecting to a Shellfire VPN server located in the USA"));
    this.streamRequireUsIdLabel.setWrapText(true);
    // this.streamsRequiredUsIdView.getEngine().loadContent(i18n.tr("<html style=\"-fx-background-color:grey;\"> Using these streams usually
    // requires a US IP address.<br>You can get one by connecting to a Shellfire VPN server located in the USA.</html>"));
  }

  @FXML
  private void handleStreamButton1(ActionEvent event) {
    this.openUsTvStream(event, streamButton1);
  }

  @FXML
  private void handleStreamButton2(ActionEvent event) {
    this.openUsTvStream(event, streamButton2);
  }

  @FXML
  private void handleStreamButton3(ActionEvent event) {
    this.openUsTvStream(event, streamButton3);
  }

  @FXML
  private void handleStreamButton4(ActionEvent event) {
    this.openUsTvStream(event, streamButton4);
  }

  @FXML
  private void handleStreamButton5(ActionEvent event) {
    this.openUsTvStream(event, streamButton5);
  }

  @FXML
  private void handleStreamButton6(ActionEvent event) {
    this.openUsTvStream(event, streamButton6);
  }

  @FXML
  private void handleStreamButton7(ActionEvent event) {
    this.openUsTvStream(event, streamButton7);
  }

  @FXML
  private void handleStreamButton8(ActionEvent event) {
    this.openUsTvStream(event, streamButton8);
  }

  private void openUsTvStream(ActionEvent evt, Button btn) {
    String address = "http://" + btn.getText();
    Util.openUrl(address);
  }
}