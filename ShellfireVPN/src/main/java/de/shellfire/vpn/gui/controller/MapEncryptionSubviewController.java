/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.AnchorPane;

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
    private RadioButton ShowOwnPosition;
    @FXML
    private Button goToOurLocationButton;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

    public Label getSelectServerLabel() {
        return selectServerLabel;
    }

    public RadioButton getShowOwnPosition() {
        return ShowOwnPosition;
    }

    public Button getGoToOurLocationButton() {
        return goToOurLocationButton;
    }
    
    
}
