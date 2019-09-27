/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Screen;

/**
 * FXML Controller class
 *
 * @author TList
 */
public class LogViewerFxmlController implements Initializable {
    @FXML
    private SplitPane splitContentPane;
    @FXML
    private AnchorPane clientLogPane;
    @FXML
    private Label clientLogLabel;
    @FXML
    private AnchorPane serviceLogPane;
    @FXML
    private Label serviceLogLabel;
    @FXML
    private Button sendLogButton;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Resize the window
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();

    }    

    @FXML
    private void sendLogButtonAction(ActionEvent event) {
    }
    
}
