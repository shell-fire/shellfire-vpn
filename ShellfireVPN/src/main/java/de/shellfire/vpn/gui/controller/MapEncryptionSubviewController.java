/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui.controller;

import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.webservice.WebService;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import org.xnap.commons.i18n.I18n;

/**
 * FXML Controller class
 *
 * @author Tcheutchoua Steve
 */
public class MapEncryptionSubviewController implements Initializable {

  @FXML
  private AnchorPane serverListAnchorPane;
  @FXML
  private Label selectServerLabel;
  @FXML
  private Button goToOurLocationButton;
  @FXML
  private RadioButton showOwnPosition;
  @FXML
  private ImageView homeImageView;
  @FXML
  private AnchorPane mapAnchorPane;

  private static I18n i18n = VpnI18N.getI18n();
  private WebService shellfireService;

  /**
   * Initializes the controller class.
   */
  @Override
  public void initialize(URL url, ResourceBundle rb) {
    this.showOwnPosition.setText(i18n.tr("Show data route and your location (when connected)"));
    this.selectServerLabel.setText("   " + i18n.tr("Server map"));
    this.goToOurLocationButton.setText(i18n.tr("go to your location"));
  }

  public Label getSelectServerLabel() {
    return selectServerLabel;
  }

  public RadioButton getShowOwnPosition() {
    return showOwnPosition;
  }

  public Button getGoToOurLocationButton() {
    return goToOurLocationButton;
  }

}
