/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui.controller;

import de.shellfire.vpn.gui.model.ServerListFXModel;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

/**
 * FXML Controller class
 *
 * @author Tcheutchoua Steve
 */
public class ServerListSubviewController implements Initializable {

    @FXML
    private AnchorPane serverListAnchorPane;
    @FXML
    private TableView<ServerListFXModel> serverListTableView;
    @FXML
    private Label selectServerLabel;
    @FXML
    private Label connectionTypeLabel;
    @FXML
    private RadioButton UDPRadioButton;
    @FXML
    private RadioButton TCPRadioButton;
    @FXML
    private ImageView connectImage1;
    @FXML
    private ImageView keyBuyRadioButton;
    @FXML
    private ImageView connectImage2;
    @FXML
    private ToggleGroup networkTypeToggleGroup;
    @FXML
    private TableColumn<ServerListFXModel, String> countryColunm;
    @FXML
    private TableColumn<ServerListFXModel, String> nameColumn;
    @FXML
    private TableColumn<ServerListFXModel, String> serverColumn;
    @FXML
    private TableColumn<ServerListFXModel, String> securityColumn;
    @FXML
    private TableColumn<ServerListFXModel, String> speedColumn;

    public TableView<ServerListFXModel> getServerListTableView() {
        return serverListTableView;
    }

    public RadioButton getUDPRadioButton() {
        return UDPRadioButton;
    }

    public RadioButton getTCPRadioButton() {
        return TCPRadioButton;
    }

    public ImageView getConnectImage1() {
        return connectImage1;
    }

    public ImageView getKeyBuyRadioButton() {
        return keyBuyRadioButton;
    }

    public ImageView getConnectImage2() {
        return connectImage2;
    }

    public ToggleGroup getNetworkTypeToggleGroup() {
        return networkTypeToggleGroup;
    }
    
    
    
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
}
