/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javax.swing.event.HyperlinkEvent;

import org.apache.commons.validator.GenericValidator;
import org.codefx.libfx.control.webview.WebViewHyperlinkListener;
import org.codefx.libfx.control.webview.WebViews;
import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.gui.LoginForms;
import de.shellfire.vpn.gui.helper.Browser;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.webservice.Response;
import de.shellfire.vpn.webservice.WebService;
import de.shellfire.vpn.webservice.model.LoginResponse;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * FXML Controller class
 *
 * @author Tcheutchoua
 */
public class RegisterFormController extends AnchorPane implements Initializable {

	@FXML
	VBox vboxRegisterForm;
	@FXML
	private Button registerButton;
	@FXML
	private Label registerHeadingLabel;
	@FXML
	private CheckBox newsLetterCheckBox;
	@FXML
	private CheckBox fAutoconnect1;
	@FXML
	private TextField emailTextField;
	@FXML
	private Label emailLabel;
	@FXML
	private Label passwordLabel;
	@FXML
	private Label confirmPasswordLabel;

	@FXML
	private PasswordField passwordField;
	@FXML
	private PasswordField confirmPasswordField;
	@FXML
	private WebView policyWebView;

	private static Logger log = Util.getLogger(RegisterFormController.class.getCanonicalName());
	public static final String REG_PASS = "pass";
	public static final String REG_USER = "user";
	WebService service;
	private ProgressDialogRegisterController progressDialog;
	private String activationToken;
	private LoginForms application;
	private boolean accountActive = false;
	private RegisterFormController.AccountActiveServicePollerTask poller;
	private static I18n i18n = VpnI18N.getI18n();

	WebViewHyperlinkListener eventPrintingListener;
	private boolean isResend;
	private WebEngine webEngine;
	private Stage stage;

	public RegisterFormController() {
		System.out.println("*********No arg constructor was used");
	}

	/**
	 * Constructor of RegisterFormController
	 *
	 * params: Application class , LoginForms
	 */

	public RegisterFormController(LoginForms parentFrame) {
		this.application = parentFrame;

		this.application.getStage().setTitle("Shellfire VPN Registrierung");
	}

	/**
	 * Initializes the controller class.
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		this.service = WebService.getInstance();
		initComponents();

	}

	public void initComponents() {
		this.registerHeadingLabel.setText(i18n.tr("Registration"));

		this.passwordLabel.setText(i18n.tr("Password:"));

		this.confirmPasswordLabel.setText(i18n.tr("Password check:"));

		this.registerButton.setText(i18n.tr("Register now"));

		this.newsLetterCheckBox.setText(i18n.tr("I subscribe to the newsletter"));

		// Load web content to webView
		
		// webEngine = policyWebView.getEngine();
		
		String webContent = i18n.tr("I accept the <a onclick='return false;' target='_agb' href='https://www.shellfire.net/terms-and-conditions/'>Terms and Conditions</a> and have read and noted the <a onclick='return false;' target='_datenschutzerklaerung' href='https://www.shellfire.net/privacy-statement/'>Privacy Policy</a> and the <a onclick='return false;' target='_widerrufsrecht' href='https://www.shellfire.net/right-of-withdrawal/'>Right of Withdrawal</a>.");
				
		// webEngine.loadContent(webContent);
		Browser b = new Browser(policyWebView, webContent, vboxRegisterForm, this);
		
		HostServices hostServices = (HostServices) this.application.getStage().getProperties().get("hostServices");

		eventPrintingListener = event -> {
			// Check if the link has been clicked then, open in external browser.
			if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				hostServices.showDocument(event.getURL().toString());
			}
			return false;
		};
		WebViews.addHyperlinkListener(policyWebView, eventPrintingListener);
	}

	@FXML
	private void handlefAutoconnect(ActionEvent event) {
	}

	@FXML
	private void handleBackLabel(MouseEvent event) {
	}

	private String getUser() {
		return emailTextField.getText();
	}

	// application has been declared final,
	// so it will be passed int he controller
	// and will not neet to be modified latter
	public void setApp(LoginForms applic) {
		this.application = applic;
	}

	private String getPassword() {
		return passwordField.getText();
	}

	private String getPasswordCheck() {
		return confirmPasswordField.getText();
	}

	private boolean validateForm() {
		boolean error = false;
		String message = "";
		TextField jumpTo = null;
		String email = emailTextField.getText();
		if (!GenericValidator.isEmail(email)) {
			error = true;
			message = i18n.tr("Please enter a valid email address.");
			jumpTo = emailTextField;
		}

		String password = this.getPassword();
		String passwordCheck = this.getPasswordCheck();
		if (!error && GenericValidator.isBlankOrNull(password)) {
			error = true;
			message = i18n.tr("Please enter a password.");
			jumpTo = passwordField;
		}

		if (!error && password.length() < 5) {
			error = true;
			message = i18n.tr("Your password must contain at least 5 characters.");
			jumpTo = passwordField;
		}

		if (!error && GenericValidator.isBlankOrNull(passwordCheck)) {
			error = true;
			message = i18n.tr("Please enter the password again into the password check field for your safety.");
			jumpTo = confirmPasswordField;
		}

		if (!error && !password.equals(passwordCheck)) {
			error = true;
			message = i18n.tr("Password and password check do not match.");
			jumpTo = confirmPasswordField;
		}

		if (!error && !fAutoconnect1.isSelected()) {
			error = true;
			message = i18n
					.tr("You must accept the terms and conditions, privacy policy and right of withdrawal to complete your registration.");
			jumpTo = null;
		}

		if (error) {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setHeaderText(i18n.tr("Error"));
			alert.setContentText(message);
			alert.showAndWait();
			if (jumpTo != null) {
				Platform.runLater(new RegisterFormController.FocusRequester(jumpTo));
			}
		}

		return !error;
	}

	@FXML
	private void handleBackLabelClicked(MouseEvent event) {
		this.application.loadLoginController();
		this.application.getStage().show();
	}

	@FXML
	private void handleMouseExited(MouseEvent event) {
		this.application.getStage().getScene().setCursor(Cursor.DEFAULT);
	}

	@FXML
	private void handleMouseEntered(MouseEvent event) {
		this.application.getStage().getScene().setCursor(Cursor.HAND);
	}

	@FXML
	private void handleMouseImgExited(MouseEvent event) {
		this.application.getStage().getScene().setCursor(Cursor.DEFAULT);
	}

	@FXML
	private void handleMouseImgEntered(MouseEvent event) {
		this.application.getStage().getScene().setCursor(Cursor.HAND);
	}

	@FXML
	private void handleBackLabelImgClicked(MouseEvent event) {
		this.application.loadLoginController();
		this.application.getStage().show();
	}

	@FXML
	protected void handleRegisterButton(ActionEvent event) {
		if (validateForm()) {
			this.showRequestProgress();
			RequestNewAccountTask task = new RequestNewAccountTask();
			task.run();
		}
	}

	private static class FocusRequester implements Runnable {

		private final TextField jumpTo;

		public FocusRequester(TextField jumpTo) {
			this.jumpTo = jumpTo;
		}

		@Override
		public void run() {
			jumpTo.requestFocus();
		}
	}


	class RequestNewAccountTask extends Task<Void> {

		@Override
		protected Void call() throws Exception {
			Response<LoginResponse> registrationResult = service.registerNewFreeAccount(emailTextField.getText(), passwordField.getText(),
					newsLetterCheckBox.isSelected(), isResend);
			if (registrationResult.isSuccess()) {
				activationToken = registrationResult.getData().getToken();
			} else {
				if (progressDialog != null) {
					ProgressDialogRegisterController.getDialogStage().hide();
				}
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setHeaderText(i18n.tr("Error"));
				alert.setContentText(i18n.tr("Error registering:") + " " + i18n.tr(registrationResult.getMessage()));
				alert.showAndWait();
			}
			return null;
		}

		@Override
		protected void succeeded() {
			super.succeeded();
			if (activationToken != null) {
				waitForActivation();
			}
		}
	}

	// needs a swing pane for successful execution.
	private void waitForActivation() {
		if (poller != null) {
			poller.stopIt();
		}

		poller = new RegisterFormController.AccountActiveServicePollerTask();
		Thread t = new Thread(poller);
		t.start();
	}

	class AccountActiveServicePollerTask extends Task<Void> {
		private boolean cont = true;

		@Override
		protected Void call() throws Exception {
			while (cont && !(accountActive = service.accountActive())) {
				Thread.sleep(3000);
			}

			return null;
		}

		public void stopIt() {
			this.cont = false;
		}

		@Override
		protected void succeeded() {
			if (accountActive) {
				activationSuccesful();
			}
		}
	}

	private void activationSuccesful() {
		this.progressDialog.getDialogStage().hide();

		this.application.getStage().setAlwaysOnTop(true);
		this.application.getStage().setAlwaysOnTop(false);
		this.application.getStage().toFront();
		
		Alert alert = new Alert(Alert.AlertType.INFORMATION,
				i18n.tr("Your Shellfire account has been successfully activated. Please log in with your email address and password to start Shellfire VPN."),
				ButtonType.OK);
		alert.setHeaderText(i18n.tr("Registration successful."));
		alert.showAndWait();

		this.stage.hide();

		LoginForms.controllerInstance.setUsername(this.getUser());
		LoginForms.controllerInstance.setPassword(this.getPassword());
	}

	private void showRequestProgress() {
		try {
			this.progressDialog = ProgressDialogRegisterController.getInstance(this.stage, this);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void requestEmailAgain() {
		this.isResend = true;
		handleRegisterButton(null);
		
	}

	public void stopWaitForActivationAndHideProgressDialog() {
		if (poller != null) {
			poller.stopIt();
		}
		this.isResend = false;
		ProgressDialogRegisterController.getDialogStage().hide();
	}

	public void setStage(Stage stage) {
		this.stage = stage;
		
	}

	public Stage getStage() {
		return this.stage;
	}
}
