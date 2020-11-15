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
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.types.Reason;
import de.shellfire.vpn.webservice.Response;
import de.shellfire.vpn.webservice.WebService;
import de.shellfire.vpn.webservice.model.LoginResponse;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 * FXML Controller class
 *
 * @author Tcheutchoua
 */
public class RegisterFormController extends AnchorPane implements Initializable {

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
	private Pane headerPanel;
	@FXML
	private ImageView headerImageView;
	@FXML
	private Pane backLabelPane;
	@FXML
	private ImageView backImageVeiw;

	@FXML
	private PasswordField passwordField;
	@FXML
	private PasswordField confirmPasswordField;
	@FXML
	private Label registerBackLabel;
	@FXML
	private WebView policyWebView;

	private static Logger log = Util.getLogger(RegisterFormController.class.getCanonicalName());
	public static final String REG_PASS = "pass";
	public static final String REG_USER = "user";
	WebService service;
	private ProgressDialogController progressDialog;
	private String activationToken;
	private LoginForms application;
	private boolean isResend = false;
	private boolean accountActive = false;
	private RegisterFormController.AccountActiveServicePollerTask poller;
	private static I18n i18n = VpnI18N.getI18n();

	WebViewHyperlinkListener eventPrintingListener;

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

		this.registerBackLabel.setText(i18n.tr("back"));

		// Load web content to webView
		WebEngine webEngine = policyWebView.getEngine();
		String webContent = "<html>" + "  <body>"
				+ i18n.tr(
						"I accept the <a onclick='return false;' target='_agb' href='https://www.shellfire.de/agb/'>Terms and Conditions</a> and have read and noted the <a onclick='return false;' target='_datenschutzerklaerung' href='https://www.shellfire.de/datenschutzerklaerung/'>Privacy Policy</a> <br /> and the <a onclick='return false;' target='_widerrufsrecht' href='https://www.shellfire.de/widerrufsrecht/'>Right of Withdrawal</a>.")
				+ "  </body>" + "</html>";
		webEngine.loadContent(webContent);

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
		return emailLabel.getText();
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
	private void handleRegisterButton(ActionEvent event) {
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

	private void hideProgress() {
		progressDialog.getDialogStage().hide();
	}

	class RequestNewAccountTask extends Task<Void> {

		@Override
		protected Void call() throws Exception {
			Response<LoginResponse> registrationResult = service.registerNewFreeAccount(emailTextField.getText(), passwordField.getText(),
					newsLetterCheckBox.isSelected());
			if (registrationResult.isSuccess()) {
				activationToken = registrationResult.getData().getToken();
			} else {
				if (progressDialog != null) {
					progressDialog.getDialogStage().show();
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
			super.succeeded(); // To change body of generated methods, choose Tools | Templates.
			hideProgress();
			if (activationToken != null) {
				waitForActivation();
			}
		}
	}

	// needs a swing pane for successful execution.
	private void waitForActivation() {
		
		
		// What does this do?
		// - displayed indefinite ProgrssBar, wait for user background action = click email link to perform
		// - automatically disappear and continue process when this is done 
		// -> this is the job of "poller", which polls regularly the backend for activation-status
		// offer 2 buttons:
		// left button: change email address -> basically back to previous form
		// rigth button: request email again in case not arrived (under same email address)
		// some additional texts here and there
		// 
		// as this is the only use case really for such a generic progress bar, it might be easier to just 
		// specifically design this one in the FX Editor and use a dedicated controller, rather then the generic ProgressBarController...
		
		
/*
		Platform.runLater(() -> {
			try {

				task =

						progressDialog = ProgressDialogController.getInstance(
								i18n.tr("You have just received an email from the Shellfire VPN system, please follow the instructions in this email."),
								task, this.application.getStage(), true);
				connectProgressDialog.getButton().setOnAction(new EventHandler<javafx.event.ActionEvent>() {

					@Override
					public void handle(javafx.event.ActionEvent event) {
						controller.disconnect(Reason.DisconnectButtonPressed);
						task.cancel(true);
						log.debug("showConnectProgress: Cancel button clicked");
					}
				});
			} catch (IOException ex) {
				log.debug("connectFromButton. Error is " + ex.getMessage());
				ex.printStackTrace(System.out);
			}
		});

		this.progressDialog.addInfo(i18n.tr("Waiting for account activation..."));
		this.progressDialog.addBottomText(i18n.tr("No email received?"));
		this.progressDialog.setOption(ProgressButtonType.Left, i18n.tr("Request new email"), 30);
		this.progressDialog.setOption(ProgressButtonType.Right, i18n.tr("Change email address"), 30);
		this.progressDialog.setOptionCallback(new Runnable() {

			@Override
			public void run() {
				if (poller != null) {
					poller.stopIt();
				}
				if (progressDialog.isOption1()) {
					progressDialog.setVisible(false);
					isResend = true;
					jButtonRequestRegKeyActionPerformed(null);
				} else if (progressDialog.isOption2()) {
					progressDialog.setVisible(false);
					isResend = false;
					JOptionPane.showMessageDialog(null, i18n.tr("Please select a different email address and try again."),
							i18n.tr("Change email address"), JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		this.progressDialog.setVisible(true);
		poller = new RegisterForm.AccountActiveServicePollerTask();
		poller.execute();
*/
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
			// super.succeeded(); //To change body of generated methods, choose Tools | Templates.
			if (accountActive) {
				activationSuccesful();
			}
		}
	}

	private void activationSuccesful() {
		this.progressDialog.getDialogStage().show();
		// bringToFront();
		Alert alert = new Alert(Alert.AlertType.INFORMATION,
				i18n.tr("Your Shellfire account has been successfully activated. Please log in with your email address and password to start Shellfire VPN."),
				ButtonType.OK);
		alert.setHeaderText(i18n.tr("Registration successful."));
		this.application.getStage().hide();
		this.application.loadLoginController();
		LoginForms.instance.setUsername(this.getUser());
		LoginForms.instance.setPassword(this.getPassword());
		LoginForms.instance.setAutoLogin(false);
		this.application.getStage().show();
	}

	private void showRequestProgress() {
		try {
			this.progressDialog = ProgressDialogController.getInstance(i18n.tr("Requesting activation key..."), null, LoginForms.getStage(),
					false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.progressDialog.getDialogStage().show();
	}
}
