package de.shellfire.vpn.gui.controller;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.gui.LoginForms;
import de.shellfire.vpn.i18n.VpnI18N;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
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

public class ProgressDialogController extends AnchorPane implements Initializable {

	
	private boolean option2;
	private static I18n i18n = VpnI18N.getI18n();
	private static LoginForms application;
	private static Stage instanceStage;
	private static ProgressDialogController instance;

	@FXML
	private Pane headerPanel1;
	@FXML
	private ImageView headerImageView1;
	@FXML
	private Label dynamicLabel;
	@FXML
	private ProgressBar progressBar;
	@FXML
	private Pane contentPane;
	private static final Logger log = Util.getLogger(ProgressDialogController.class.getCanonicalName());
	
	private Map<ProgressButtonType, Task> optionCallbackMap = new HashMap<ProgressButtonType, Task>();
	private Map<ProgressButtonType, Button> buttonMap = new HashMap<ProgressButtonType, Button>();

	public ProgressDialogController() {
		log.debug("ProgressDialogController: In netbeans");
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
		dynamicLabel.setText(i18n.tr("Logging in..."));
		// additionTextLabel.setText("<dynamic>");
		this.headerImageView1.setImage(ShellfireVPNMainFormFxmlController.getLogo());
		log.debug("\n " + com.sun.javafx.runtime.VersionInfo.getRuntimeVersion());
	}

	public Button getButton(ProgressButtonType buttonType) {
		ensureButtonExists(buttonType);
		return buttonMap.get(buttonType);
	}
	
	public ProgressBar getProgressBar() {
		return progressBar;
	}

	public void setOptionCallback(ProgressButtonType buttonType, Task task) {
		// make the button visible when a task has to be assigned to the respective button
		ensureButtonExists(buttonType);
		log.debug("setOptionCallback: Runnable has been initialised " + task.toString());
		this.optionCallbackMap.put(buttonType, task);
	}

	public void callOptionCallback(ProgressButtonType buttonType) {
		Task callBack = optionCallbackMap.get(buttonType);
		
		if (callBack != null)
			callBack.run();
	}

	public void updateProgress(double percentage) {
		// just set the update progress property
		progressBar.setProgress(percentage);
	}

	public Label getDynamicLabel() {
		return dynamicLabel;
	}

	public void setButtonText(ProgressButtonType buttonType, String text) {
		ensureButtonExists(buttonType);
		Button button = buttonMap.get(buttonType);
		button.setText(text);
	}

	public void setIndeterminate(boolean b) {
		if (b == true) {
			progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
		}
	}

	// Removed because the dynamic label has a binding in EntityManager
	public void setDialogText(String string) {
		this.dynamicLabel.setText(string);
	}

	public static Stage getDialogStage() {
		instanceStage.sizeToScene();
		return instanceStage;
	}

	public static ProgressDialogController getInstance(String dialogText, Task task, Window owner, boolean createNew) throws IOException {
		if (instance == null || createNew) {
			// Load the fxml file and create a new stage for the popup dialog.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(LoginForms.class.getResource("/fxml/ProgressDialog.fxml"));
			AnchorPane page = (AnchorPane) loader.load();
			instance = (ProgressDialogController) loader.getController();
			instance.setDialogText(dialogText);
			instance.getProgressBar().setProgress(ProgressBar.INDETERMINATE_PROGRESS);
			if (null != task) {
				instance.setOptionCallback(ProgressButtonType.Right, task);
			}
			instanceStage = new Stage();
			instanceStage.initStyle(StageStyle.UNDECORATED);
			instanceStage.initModality(Modality.WINDOW_MODAL);
			instanceStage.initOwner(owner);
			Scene scene = new Scene(page);
			instanceStage.setScene(scene);
			instance.getProgressBar().progressProperty().unbind();
			log.debug("ProgressDialogController instance and Stage created");
		}
		return instance;
	}
	

	private void ensureButtonExists(ProgressButtonType buttonType) {
		if (buttonMap.get(buttonType) == null) {
			Button button = new Button(i18n.tr("Cancel"));
			buttonMap.put(buttonType, button);
			button.setPrefWidth(100);
			contentPane.getChildren().add(button);
			Pane spacePane = new Pane();
			spacePane.setPrefHeight(3);
			spacePane.setMinHeight(3);
			spacePane.setMaxHeight(3);
			contentPane.getChildren().add(spacePane);
		}
	}

}