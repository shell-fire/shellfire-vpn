package de.shellfire.vpn.gui.controller;

import java.net.URL;
import java.text.DateFormat;
import java.util.ResourceBundle;

import org.xnap.commons.i18n.I18n;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.gui.LoginForms;
import de.shellfire.vpn.gui.renderer.CrownImageRendererVpn;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.types.ServerType;
import de.shellfire.vpn.webservice.Vpn;
import de.shellfire.vpn.webservice.WebService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class AppScreenControllerPremium implements Initializable, AppScreenController {
	
	private static final I18n i18n = VpnI18N.getI18n();
	
	@FXML
	private Label labelPremiumHeader;
	
	@FXML
	private Label currentPlanLabel;
	
	@FXML
	private ImageView selectedVpnTypeImage;
	@FXML
	private Label actualPlanLabel;
	@FXML
	private Label paymentInfoLabel;
	@FXML
	private Label freeLabel;
	@FXML
	private Button premiumInfoButton;
	@FXML
	private Label paidUntilLabel;
	@FXML
	private Label actualPaidUntilDateLabel;
	@FXML
	private Button manageAccountButton;
	@FXML
	private HBox paidUntilHBox;

	private WebService shellfireService;
	
	
	
	public void setShellfireService(WebService webService) {
		this.shellfireService = webService;
		
	}

	public void initComponents() {
		updateStaticContent();
		updateDynamicContent();
	}

	private void updateStaticContent() {
		// static texts
		labelPremiumHeader.setText(i18n.tr("Premium Infos"));
		currentPlanLabel.setText(i18n.tr("Current Plan"));
		paymentInfoLabel.setText(i18n.tr("Payment Info"));
		freeLabel.setText(i18n.tr("Free Forever, no payment"));
		premiumInfoButton.setText(i18n.tr("Click here to check out our Premium offer"));
		paidUntilLabel.setText(i18n.tr("Paid Until"));
		manageAccountButton.setText(i18n.tr("Click here to manage your account"));
	}

	private void updateDynamicContent() {
		Vpn vpn = shellfireService.getVpn();
		ServerType currentAccountType = vpn.getAccountType();
		
		this.selectedVpnTypeImage.setImage(CrownImageRendererVpn.getIcon(currentAccountType, false, false));
		
		actualPlanLabel.setText(currentAccountType.name());
		
		if (currentAccountType == ServerType.Free) {
			freeLabel.setVisible(true);
			premiumInfoButton.setVisible(true);
			freeLabel.setManaged(true);
			premiumInfoButton.setManaged(true);
			
			paidUntilHBox.setVisible(false);
			manageAccountButton.setVisible(false);
			paidUntilHBox.setManaged(false);
			manageAccountButton.setManaged(false);
		} else {
			freeLabel.setVisible(false);
			premiumInfoButton.setVisible(false);
			freeLabel.setManaged(false);
			premiumInfoButton.setManaged(false);

			paidUntilHBox.setVisible(true);
			manageAccountButton.setVisible(true);
			paidUntilHBox.setManaged(true);
			manageAccountButton.setManaged(true);
			
			DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, i18n.getLocale());
			String paidUntil = df.format(vpn.getPremiumUntil());
			actualPaidUntilDateLabel.setText(paidUntil);
			
		}
	}
	
	@FXML
	private void onClickPremiumInfoButton(ActionEvent event) {
		showPremiumInfo();
	}
	
	@FXML
	private void onClickManageAccountButton(ActionEvent event) {
		showManageAccount();
	}

	private void showManageAccount() {
		Util.openUrl(this.shellfireService.getUrlManageAccount());
		
	}

	private void showPremiumInfo() {
		Util.openUrl(this.shellfireService.getUrlPremiumInfo());
		
	}

	public void setApp(LoginForms application) {
		
		
	}

	public void setMainFormController(ShellfireVPNMainFormFxmlController shellfireVPNMainFormFxmlController) {
		
		
	}

	@Override
	public void notifyThatNowVisible(boolean isConnected) {
		updateDynamicContent();
		
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		
		
	}
}
