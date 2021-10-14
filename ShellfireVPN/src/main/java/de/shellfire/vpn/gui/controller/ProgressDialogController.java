package de.shellfire.vpn.gui.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.gui.LoginForms;
import de.shellfire.vpn.i18n.VpnI18N;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public class ProgressDialogController extends AnchorPane implements Initializable {

	private static I18n i18n = VpnI18N.getI18n();
	private static LoginForms application;
	private Stage instanceStage;
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
	
	private Task optionCallback;
	private Button button;

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
		this.headerImageView1.setImage(ShellfireVPNMainFormFxmlController.getLogo());
		log.debug("\n " + com.sun.javafx.runtime.VersionInfo.getRuntimeVersion());
	}

	public Button getButton() {
		ensureButtonExists();
		return button;
	}
	
	public ProgressBar getProgressBar() {
		return progressBar;
	}

	public void setOptionCallback(Task task) {
		// make the button visible when a task has to be assigned to the respective button
		ensureButtonExists();
		log.debug("setOptionCallback: Runnable has been initialised " + task.toString());
		this.optionCallback = task;
	}

	public void callOptionCallback() {
		if (optionCallback != null)
			optionCallback.run();
	}



	public Label getDynamicLabel() {
		return dynamicLabel;
	}

	public void setButtonText(String text) {
		ensureButtonExists();
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

	public Stage getDialogStage() {
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
				instance.setOptionCallback(task);
			}
			Stage instanceStage = new Stage();
			instanceStage.initStyle(StageStyle.UNDECORATED);
			instanceStage.initModality(Modality.WINDOW_MODAL);
			instanceStage.initOwner(owner);
			Scene scene = new Scene(page);
			instanceStage.setScene(scene);
			instance.getProgressBar().progressProperty().unbind();
			
			instance.setInstanceAndStage(instanceStage);
			log.debug("ProgressDialogController instance and Stage created");
		}
		return instance;
	}
	

	private void setInstanceAndStage(Stage instanceStage2) {
		this.instanceStage = instanceStage2;
	}

	private void ensureButtonExists() {
		if (this.button == null) {
			this.button = new Button(i18n.tr("Cancel"));
			button.setPrefWidth(100);
			contentPane.getChildren().add(this.button);
			Pane spacePane = new Pane();
			spacePane.setPrefHeight(3);
			spacePane.setMinHeight(3);
			spacePane.setMaxHeight(3);
			contentPane.getChildren().add(spacePane);
			
			EventHandler<ActionEvent> handler = new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent arg0) {
					callOptionCallback();
				}
				
			};
			button.setOnAction(handler);
		}
	}

	public void show() {
		Platform.runLater(() -> {
			getDialogStage().show();
		});

		
	}

	public void hide() {
		Platform.runLater(() -> {
			getDialogStage().hide();
		});
		
	}

	public void bindProgressProperty(ReadOnlyDoubleProperty progressProperty) {
		this.progressBar.progressProperty().bind(progressProperty);
	}

}