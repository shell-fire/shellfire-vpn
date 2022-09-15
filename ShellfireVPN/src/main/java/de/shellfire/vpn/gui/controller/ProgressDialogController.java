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
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
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
		log.debug(System.getProperty("javafx.runtime.version"));
	}

	public Button getButton() {
		ensureButtonExists();
		return button;
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
			progressBar.setVisible(false);
			progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
			
			
		} else {
			progressBar.setVisible(true);
		}
	}

	public void setDialogText(String string) {
		this.dynamicLabel.setText(string);
	}

	public Stage getDialogStage() {
		instanceStage.sizeToScene();
		return instanceStage;
	}

	public static ProgressDialogController getInstance(String dialogText, Task task, Window owner, boolean createNew) throws IOException {
		if (instance == null || createNew) {
			StackPane root = new StackPane();
			root.setStyle("-fx-background-color:transparent");
			SVGPath p = new SVGPath();
			p.setContent("M 265.873 176.686 c -4.268 -12.383 -14.609 -40.019 -35.033 -64.999 c 3.455 18.85 -5.904 20.615 -12.156 13.694 c -11.153 -12.347 -14.412 -40.654 -36.333 -46.032 c 5.969 15.281 -5.869 55.82 -23.282 63.808 c -2.332 1.368 -6.64 -0.13 -9.28 -7.971 c -3.98 4.546 -4.818 16.973 -8.04 24.582 c -11.04 12.747 -17.731 29.279 -17.731 47.337 c 0 40.196 33.081 72.898 73.742 72.898 s 73.741 -32.702 73.741 -72.898 C 271.5 196.286 270.046 188.072 265.873 176.686 z M 198 203 z z");
			p.setFill(Color.web("#4381e5"));
			p.setScaleX(1.5);
			p.setScaleY(1.5);
			p.setScaleZ(1.5);
			p.setEffect(new DropShadow());

			// Load the fxml file and create a new stage for the popup dialog.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(LoginForms.class.getResource("/fxml/ProgressDialog.fxml"));
			StackPane pane = (StackPane) loader.load();

			root.getChildren().add(p);
			root.getChildren().add(pane);
			
			// transparent scene and stage
			Scene scene = null;
			scene = new Scene(root, 400, 400, Color.TRANSPARENT);
			scene.setFill(Color.TRANSPARENT);
			
			instance = (ProgressDialogController) loader.getController();
			instance.setDialogText(dialogText);
			instance.setIndeterminate(true);
			if (null != task) {
				instance.setOptionCallback(task);
			}
		    Stage parent = new Stage();

		    parent.initStyle(StageStyle.UTILITY);
		    parent.setMaxHeight(0);
		    parent.setMaxWidth(0);
		    parent.setX(Double.MAX_VALUE);
		    parent.show();
			
			
			Stage stage = new Stage();
			stage.setScene(scene);
			stage.initStyle(StageStyle.TRANSPARENT);

			stage.initModality(Modality.WINDOW_MODAL);
			stage.initOwner(parent);
			stage.show();
			
			instance.unbindProgressProperty();
			instance.setInstanceAndStage(stage);
			log.debug("ProgressDialogController instance and Stage created");
		}
		instance.setDialogText(dialogText);
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
	

	public void unbindProgressProperty() {
		this.progressBar.progressProperty().unbind();
	}
	
	
	

}