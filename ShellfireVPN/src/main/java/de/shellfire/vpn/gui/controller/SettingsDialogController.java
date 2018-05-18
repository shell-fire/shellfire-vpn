/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui.controller;

import de.shellfire.vpn.VpnProperties;
import de.shellfire.vpn.client.Client;
import de.shellfire.vpn.i18n.Language;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.webservice.Vpn;
import de.shellfire.vpn.webservice.WebService;
import java.net.URL;
import java.util.LinkedList;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import org.xnap.commons.i18n.I18n;

/**
 *
 * @author TcheutchouaSteve
 */
public class SettingsDialogController implements Initializable{

    @FXML
    private CheckBox saveLoginData;
    @FXML
    private CheckBox loginAutomatically;
    @FXML
    private CheckBox saveVpnChoice;
    @FXML
    private CheckBox startOnBoot;
    @FXML
    private CheckBox connectAutomatically;
    @FXML
    private CheckBox showStatusSite;
    @FXML
    private Label languageLabel;
    @FXML
    private MenuButton languageMenuButton;
    @FXML
    private Button saveSettingsButton;
    @FXML
    private Button cancelButton;
    
    private static final I18n I18N = VpnI18N.getI18n();
    private Language currentLanguage;

    public SettingsDialogController() {
    }
    
    
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.saveLoginData.setText(I18N.tr("Save login data"));
        this.loginAutomatically.setText(I18N.tr("Login automatically"));
        this.saveVpnChoice.setText(I18N.tr("Save VPN choice"));
        this.startOnBoot.setText(I18N.tr("Start on boot"));
        this.showStatusSite.setText(I18N.tr("Show status site after connection has been established"));
        this.languageLabel.setText(I18N.tr("Language :"));
        this.saveSettingsButton.setText(I18N.tr("save settings"));
        this.cancelButton.setText(I18N.tr("cancel"));
        
       initValues();
    }

    @FXML
    private void handleSaveLoginData(ActionEvent event) {
        if (!this.saveLoginData.isDisabled()&& !saveLoginData.isSelected()) {
          if (!this.loginAutomatically.isDisabled()) {
            this.loginAutomatically.setSelected(false);
          }
          if (!this.saveLoginData.isDisabled()) {
            this.saveLoginData.setSelected(false);
          }
        }
    }

    @FXML
    private void handleLoginAutomatically(ActionEvent event) {
        if (!this.loginAutomatically.isDisabled() && loginAutomatically.isSelected() && !this.saveLoginData.isDisabled()) {
            this.saveLoginData.setSelected(true);
        }
    }

    @FXML
    private void handleSaveVpnChoice(ActionEvent event) {
        if (!this.saveVpnChoice.isDisabled() && saveVpnChoice.isSelected() && !this.saveLoginData.isDisabled()) {
        this.saveLoginData.setSelected(true);
      }
    }

    @FXML
    private void handleStartOnBoot(ActionEvent event) {
    }

    @FXML
    private void handleConnectAutomatically(ActionEvent event) {
    }

    @FXML
    private void handleShowStatusSite(ActionEvent event) {
    }

    @FXML
    private void hanldeLanguageMenuButton(ActionEvent event) {
    }

    @FXML
    private void handleSaveSettingsButton(ActionEvent event) {
        // get a handle to the stage
    Stage stage = (Stage) this.saveSettingsButton.getScene().getWindow();
    // do what you have to do
    stage.hide();
    save();
    
    }

    @FXML
    private void handleCancelButton(ActionEvent event) {
        // get a handle to the stage
    Stage stage = (Stage) this.cancelButton.getScene().getWindow();
    // do what you have to do
    stage.hide();
    }
    
      private void initValues() {
      VpnProperties props = VpnProperties.getInstance();
        
        if (props.getProperty(LoginController.REG_USER, null) != null) {
            this.saveLoginData.setDisable(true);
        } else {
            this.saveLoginData.setSelected(true); // can only be enabled from login dialog
        }
        
        if (props.getBoolean(LoginController.REG_AUTOLOGIN, false)) {
          this.loginAutomatically.setSelected(true);
        } else if (props.getProperty(LoginController.REG_USER, null) == null){
            this.loginAutomatically.setDisable(true); // disable if login data not remembered, because then it makes no sense
        }

        if (props.getInt(VpnSelectDialogController.REG_REMEMBERSELECTION, 0) != 0) {
            this.saveVpnChoice.setSelected(true);
        } else if (props.getProperty(LoginController.REG_USER, null) == null){
            this.saveVpnChoice.setDisable(true); // disable if login data not remembered, because then it makes no sense
        }
        
        boolean autoConnect = props.getBoolean(LoginController.REG_AUTOCONNECT, false);
        this.connectAutomatically.setSelected(autoConnect);
        
        boolean autoStart = Client.vpnAutoStartEnabled();
        this.startOnBoot.setSelected(autoStart);  

        boolean showStatusUrlOnConnect = props.getBoolean(LoginController.REG_SHOWSTATUSURL, false);
        this.showStatusSite.setSelected(showStatusUrlOnConnect);

        initLanguages();
    }
      
       private void initLanguages() {
        LinkedList<Language> languages = VpnI18N.getAvailableTranslations();

        for (Language language : languages) {
            languageMenuButton.getItems().add(new MenuItem(language.getName()));
        }

        currentLanguage = VpnI18N.getLanguage();
        //languageMenuButton. (currentLanguage);

    } 
       
 private void save() {
      VpnProperties props = VpnProperties.getInstance();

        if (!this.loginAutomatically.isDisabled()) { // Not disabled means the checkbox is enabled.
          props.setBoolean(LoginController.REG_AUTOLOGIN, this.loginAutomatically.isSelected());
        }

        if (!this.saveLoginData.isDisabled()&& this.saveLoginData.isSelected() == false) {
          props.remove(LoginController.REG_USER);
          props.remove(LoginController.REG_PASS);
        }

        if (!this.saveVpnChoice.isDisabled()&& !this.saveVpnChoice.isSelected()) {
          props.remove(VpnSelectDialogController.REG_REMEMBERSELECTION);
        }
        
        props.setBoolean(LoginController.REG_AUTOCONNECT, this.connectAutomatically.isSelected());
        props.setBoolean(LoginController.REG_SHOWSTATUSURL, this.showStatusSite.isSelected());
        
        if (this.saveVpnChoice.isSelected()) {
          WebService service = WebService.getInstance();
          Vpn vpn = service.getVpn();
          props.setInt(VpnSelectDialogController.REG_REMEMBERSELECTION, vpn.getVpnId());
        }
        
        
        if (this.startOnBoot.isSelected()) {
          Client.addVpnToAutoStart();
        } else {
          Client.removeVpnFromAutoStart();
        }
        

        Language oldLanguage = VpnI18N.getLanguage();
        
        if (!currentLanguage.equals(oldLanguage)) {
          VpnI18N.setLanguage(currentLanguage);  
          
          Alert alert = new Alert(Alert.AlertType.NONE);
                            alert.setHeaderText(I18N.tr("Changed language settings require a restart of Shellfire VPN to take effect. Restart now?"));
                            alert.setContentText(I18N.tr("Changed language settings require a restart of ShellfireVPN to take effect."));
                            alert.showAndWait();
            Optional<ButtonType> result = alert.showAndWait();
        if ((result.isPresent()) && (result.get() == ButtonType.OK)) {
            alert.close();
            //TODO LoginController.restart();
        }
                   
        }
        
    }

}
