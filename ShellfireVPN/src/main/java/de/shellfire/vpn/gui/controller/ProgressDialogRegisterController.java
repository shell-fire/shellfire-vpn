package de.shellfire.vpn.gui.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.gui.LoginForms;
import de.shellfire.vpn.i18n.VpnI18N;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public class ProgressDialogRegisterController extends AnchorPane implements Initializable {

	private static I18n i18n = VpnI18N.getI18n();
	private static LoginForms application;
	private static Stage instanceStage;
	private static ProgressDialogRegisterController instance;

	@FXML
	private Pane headerPanel1;
	@FXML
	private ImageView headerImageView1;
	@FXML
	private Label waitingLabel;
	@FXML
	private Label infoLabel;
	@FXML
	private ProgressBar progressBar;
	@FXML
	private Label noMailReceivedLabel;
	@FXML
	private Button reRequestEmailButton;
	@FXML 
	private Button changeEmailAddressButton;
	
	@FXML
	private Pane contentPane;
	private RegisterFormController registerFormController;
	private static final Logger log = Util.getLogger(ProgressDialogRegisterController.class.getCanonicalName());

	public ProgressDialogRegisterController() {
		log.debug("ProgressDialogRegisterController: In netbeans");
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		initComponents();
	}

	public static void setApp(LoginForms applic) {
		application = applic;
	}

	public static LoginForms getApplication() {
		return application;
	}

	public void initComponents() {
		setWaitingLabelText(i18n.tr("Waiting for account activation..."));
		setInfoLabelText(i18n.tr("We have just sent you an email - please follow the instructions in it."));
		setNoMailReceivedLabelText(i18n.tr("No email received?"));
		getProgressBar().setProgress(ProgressBar.INDETERMINATE_PROGRESS);
		instanceStage = new Stage();
		instanceStage.initStyle(StageStyle.UNDECORATED);
		instanceStage.initModality(Modality.WINDOW_MODAL);

		this.headerImageView1.setImage(ShellfireVPNMainFormFxmlController.getLogo());
		log.debug("\n " + com.sun.javafx.runtime.VersionInfo.getRuntimeVersion());
	}

	
	public ProgressBar getProgressBar() {
		return progressBar;
	}

	public void updateProgress(double percentage) {
		// just set the update progress property
		progressBar.setProgress(percentage);
	}

	public Label getDynamicLabel() {
		return waitingLabel;
	}

	public void setWaitingLabelText(String string) {
		this.waitingLabel.setText(string);
	}

	public void setInfoLabelText(String string) {
		this.infoLabel.setText(string);
	}

	public void setNoMailReceivedLabelText(String string) {
		this.noMailReceivedLabel.setText(string);
	}
	
	public static Stage getDialogStage() {
		instanceStage.sizeToScene();
		return instanceStage;
	}
	
	@FXML
	private void onChangeEmailAddressButtonAction() {
		Platform.runLater(() -> {
			registerFormController.stopWaitForActivationAndHide();
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setHeaderText(i18n.tr("Please enter a different email-address and try again."));
			alert.showAndWait();
		});
	}	
	
	@FXML
	private void onRequestNewEmailButtonAction() {
		Platform.runLater(() -> {
			registerFormController.requestEmailAgain();
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setHeaderText(i18n.tr("We sent you the email again."));
			alert.showAndWait();
		});
	}	

	/** call with Poller task 
	 * @param registerFormController */
	public static ProgressDialogRegisterController getInstance(Window owner, RegisterFormController registerFormController) throws IOException {
		if (instance == null) {
			// Load the fxml file and create a new stage for the popup dialog.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(LoginForms.class.getResource("/fxml/ProgressDialogRegister.fxml"));
			AnchorPane page = (AnchorPane) loader.load();
			instance = (ProgressDialogRegisterController) loader.getController();
			instance.setRegisterForm(registerFormController);
			instanceStage.initOwner(owner);
			Scene scene = new Scene(page);
			instanceStage.setScene(scene);
			instance.getProgressBar().progressProperty().unbind();
		
			Platform.runLater(() -> {
				new Timer().schedule(new TimerTask() {
					public void run() {
						instance.reRequestEmailButton.setDisable(false);
						instance.changeEmailAddressButton.setDisable(false);
					}
					
				}, 5000);
			});

			
			log.debug("ProgressDialogRegisterController instance and Stage created");
		}
		return instance;
	}

	private void setRegisterForm(RegisterFormController registerFormController) {
		this.registerFormController = registerFormController;
		
	}

}