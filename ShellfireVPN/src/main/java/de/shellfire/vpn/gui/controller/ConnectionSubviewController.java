/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui.controller;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.client.Controller;
import de.shellfire.vpn.gui.LoginForms;
import de.shellfire.vpn.gui.controller.*;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.webservice.WebService;
import java.awt.Dimension;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
    
    private LoginForms application;
    private static final Logger log = Util.getLogger(ShellfireVPNMainFormFxmlController.class.getCanonicalName());
    private static I18n i18n = VpnI18N.getI18n();
    private Controller controller;
    private static WebService shellfireService;
    private MenuItem popupConnectItem;
    private PopupMenu popup;
    private TrayIcon trayIcon;

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

        String size = "736";
        if (width > 3000) {
            size = "1472";
        }

        String langKey = VpnI18N.getLanguage().getKey();
        log.debug("langKey: " + langKey);
        
        //mySetIconImage("/icons/sfvpn2-idle-big.png");
        String baseImageUrl = "src/main/resources";
        
        this.connectImageView.setId(baseImageUrl + "/buttons/button-disconnect-" + langKey + ".gif");
        this.statusConnectionImageView.setId(baseImageUrl + "/icons/status-unencrypted-width" + size + ".gif");
    }    

    @FXML
    private void handleConnectImageViewMouseExited(MouseEvent event) {
    }

    @FXML
    private void handleConnectImageViewMouseEntered(MouseEvent event) {
    }

    @FXML
    private void handleConnectImageViewContext(ContextMenuEvent event) {
    }

    @FXML
    private void handleConnectImageViewClicked(MouseEvent event) {
    }

    @FXML
    private void handleProductKeyImageViewMouseExited(MouseEvent event) {
    }

    @FXML
    private void handleProductKeyImageViewMouseEntered(MouseEvent event) {
    }

    @FXML
    private void handleProductKeyImageViewContext(ContextMenuEvent event) {
    }

    @FXML
    private void handleProductKeyImageViewClicked(MouseEvent event) {
    }

    @FXML
    private void handlePremiumInfoImageViewMouseExited(MouseEvent event) {
    }

    @FXML
    private void handlePremiumInfoImageViewMouseEntered(MouseEvent event) {
    }

    @FXML
    private void handlePremiumInfoImageViewContext(ContextMenuEvent event) {
    }

    @FXML
    private void handlePremiumInfoImageViewClicked(MouseEvent event) {
    }
    
        private void initPremium(boolean freeAccount) {
        if (!freeAccount) {
            this.premiumInfoImageView.setVisible(false);
            this.connectImageView.setVisible(false);
        }
        this.productKeyImageView.setVisible(false);
        this.productKeyImageView.setVisible(false);
    }
        
   public String displayCreationMessage(String msg){
       return("ConnectionSubviewController: " + msg);
   }
}
