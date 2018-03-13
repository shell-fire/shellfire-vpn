/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui.controller;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.gui.LoginForms;
import de.shellfire.vpn.i18n.Language;
import de.shellfire.vpn.i18n.VpnI18N;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;

/**
 *
 * @author Tcheutchoua
 */
public class LicenseAcceptanceController extends AnchorPane implements Initializable{

    @FXML
    private Button declineButton;
    @FXML
    private Button acceptButton;
    @FXML
    private Pane headerPanel;
    @FXML
    private ImageView headerImageView;
    @FXML
    private Label acceptLicenceLabel;
    @FXML
    private ScrollPane licenceMessageScrollPane;
    @FXML
    private TextArea licenceTextArea;
    
    private LoginForms application;
    private static I18n i18n = VpnI18N.getI18n();
    private static Logger log = Util.getLogger(LoginForms.class.getCanonicalName());
    
    @FXML
    private void handleDeclineButtonButton(ActionEvent event) {
        this.application.licenseNotAccepted();
        // TODO add handline logic after licence is declined
    }

    @FXML
    private void handleAcceptButton(ActionEvent event) {
        this.application.licenseAccepted();
        // TODO add handline logic after licence is accepted
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
       
        // Initialise compoenents with appropriate language
        initComponents();
    }
    
    public void initComponents(){
        declineButton.setText(i18n.tr("Ablehnen"));
        acceptButton.setText(i18n.tr("Akzeptieren"));
        acceptLicenceLabel.setText(i18n.tr("Lizenz akzeptieren"));
    }
    
    private void initLicense() {
        Language lang = VpnI18N.getLanguage();
        String name = "de";
        if (lang != null && lang.getName() != null)
            name = lang.getKey();
        
        log.debug("Name: " +name);
        String text = "License";
        String filename = com.apple.eio.FileManager.getPathToApplicationBundle() + "/Contents/Java/openvpn/license_" + name + ".txt";
        try {
        	text = Util.fileToString(filename);
        } catch (FileNotFoundException ex) {
            log.debug("Could not find licence file at " + filename);
        } catch (IOException ex) {
            log.debug("Could not open licence file at " + filename);
        }
        
        licenceTextArea.setText(text);
        licenceTextArea.positionCaret(0);            
                
    }
    
    public void setApp(LoginForms applic){
		this.application = applic ; 
	}
}
