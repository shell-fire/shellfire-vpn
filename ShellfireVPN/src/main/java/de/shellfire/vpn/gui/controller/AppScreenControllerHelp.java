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
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;

public class AppScreenControllerHelp implements Initializable, AppScreenController {

	private List<HelpItem> helpItemList;
	private static final I18n i18n = VpnI18N.getI18n();
	
	
	@FXML
	private VBox helpItemContainerVBox;

	
	public void setShellfireService(WebService webService) {

		
	}

	public void initComponents() {
		
		// TODO: replace by retrieving helpItems from web-service
		helpItemList = new LinkedList<HelpItem>();
		helpItemList.add(new HelpItem(this, i18n.tr("Help Header 1"), i18n.tr("Help Text 1 ... Help Text 1 ... Help Text 1 ... Help Text 1 ... Help Text 1 ... Help Text 1 ... Help Text 1 ... ")));
		helpItemList.add(new HelpItem(this, i18n.tr("Help Header 2"), i18n.tr("Help Text 2 ... Help Text 2 ... Help Text 2 ... Help Text 2 ... Help Text 2 ... Help Text 2 ... Help Text 2 ... ")));
		helpItemList.add(new HelpItem(this, i18n.tr("Help Header 3"), i18n.tr("Help Text 3 ... Help Text 3 ... Help Text 3 ... Help Text 3 ... Help Text 3 ... Help Text 3 ... Help Text 3 ... ")));
		helpItemList.add(new HelpItem(this, i18n.tr("Help Header 4"), i18n.tr("Help Text 4 ... Help Text 4 ... Help Text 4 ... Help Text 4 ... Help Text 4 ... Help Text 4 ... Help Text 4 ... ")));

		for (HelpItem currentItem : helpItemList) {
			helpItemContainerVBox.getChildren().add(currentItem);
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

	public void hideAllHelpItems() {
		for (HelpItem currentItem : helpItemList) {
			currentItem.hide();
		}
		
	}
}
