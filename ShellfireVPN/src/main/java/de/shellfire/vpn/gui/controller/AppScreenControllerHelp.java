package de.shellfire.vpn.gui.controller;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import org.xnap.commons.i18n.I18n;

import de.shellfire.vpn.gui.LoginForms;
import de.shellfire.vpn.gui.helper.HelpItem;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.webservice.WebService;
import de.shellfire.vpn.webservice.model.WsHelpItem;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class AppScreenControllerHelp implements Initializable, AppScreenController {

	private List<HelpItem> helpItemList;
	private static final I18n i18n = VpnI18N.getI18n();
	
	
	@FXML
	private VBox helpItemContainerVBox;
	private WebService webService;
	private LoginForms application;
	@FXML
	private Label labelHelpHeader;
	
	public void setShellfireService(WebService webService) {
		this.webService = webService;
		
	}

	public void initComponents() {
		labelHelpHeader.setText(i18n.tr("Help"));
		List<WsHelpItem> wsHelpItemList = webService.getHelpDetails();
		helpItemList = new LinkedList<HelpItem>();
		for (WsHelpItem wsHelpItem : wsHelpItemList) {

			HelpItem helpItem = new HelpItem(this, wsHelpItem.getHeader(), wsHelpItem.getText());
			helpItemList.add(helpItem);
			helpItemContainerVBox.getChildren().add(helpItem);
		}
	}

	public void setApp(LoginForms application) {
		this.application = application;
	}

	public LoginForms getApp() {
		return this.application;
	}

	public void setMainFormController(ShellfireVPNMainFormFxmlController shellfireVPNMainFormFxmlController) {
		
		
	}

	@Override
	public void notifyThatNowVisible(boolean isConnected) {
		
		
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		
		
	}

	public void hideAllHelpItems() {
		for (HelpItem currentItem : helpItemList) {
			currentItem.hide();
		}
		
	}
}
