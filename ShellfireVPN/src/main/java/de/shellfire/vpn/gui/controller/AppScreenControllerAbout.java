package de.shellfire.vpn.gui.controller;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import org.xnap.commons.i18n.I18n;

import de.shellfire.vpn.gui.LoginForms;
import de.shellfire.vpn.gui.helper.AboutItem;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.webservice.WebService;
import de.shellfire.vpn.webservice.model.WsHelpItem;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class AppScreenControllerAbout implements Initializable, AppScreenController {

	private List<AboutItem> aboutItemList;
	private static final I18n i18n = VpnI18N.getI18n();
	
	
	@FXML
	private VBox aboutItemContainerVBox;
	@FXML
	private Label labelAboutHeader;
	private WebService webService;
	private LoginForms application;

	
	public void setShellfireService(WebService webService) {
		this.webService = webService;
		
	}

	public void initComponents() {
		
		labelAboutHeader.setText(i18n.tr("About"));
		
		List<WsHelpItem> wsAboutItemList = webService.getAbout();
		aboutItemList = new LinkedList<AboutItem>();
		for (WsHelpItem wsHelpItem : wsAboutItemList) {

			AboutItem aboutItem = new AboutItem(this, wsHelpItem.getHeader(), wsHelpItem.getText());
			VBox.setVgrow(aboutItem, Priority.ALWAYS);
			aboutItemList.add(aboutItem);
			aboutItemContainerVBox.getChildren().add(aboutItem);
		}
	}

	public void setApp(LoginForms application) {
		// TODO Auto-generated method stub
		
	}

	public void setMainFormController(ShellfireVPNMainFormFxmlController shellfireVPNMainFormFxmlController) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyThatNowVisible(boolean isConnected) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
		
	}

}
