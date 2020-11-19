/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui.controller;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.gui.LoginForms;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.messaging.UserType;
import de.shellfire.vpn.webservice.WebService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author TList
 */
public class LogViewerFxmlController implements Initializable {

	@FXML
	private SplitPane splitContentPane;
	@FXML
	private AnchorPane clientLogPane;
	@FXML
	private Label clientLogLabel;
	@FXML
	private AnchorPane serviceLogPane;
	@FXML
	private Label serviceLogLabel;
	@FXML
	private Button sendLogButton;
	@FXML
	private TextArea clientLogTextArea;
	@FXML
	private TextArea serviceLogTextArea;

	private static I18n i18n = VpnI18N.getI18n();
	// The Stage for the logViewer
	protected static Stage instanceStage;
	protected static ProgressDialogController sendLogProgressDialog;
	private static Logger log = Util.getLogger(LogViewerFxmlController.class.getCanonicalName());
	private static LoginForms application;
	private static LogViewerFxmlController instance;

	class LogListener extends TailerListenerAdapter {

		private TextArea textArea;

		public LogListener(TextArea textArea) {
			this.textArea = textArea;
		}

		public void handle(String line) {
			if (line != null) {
				Platform.runLater(() -> {
					this.textArea.appendText(line + "\n");
					this.textArea.end();
				});
			}
		}
	}

	/**
	 * Initializes the controller class.
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {

		LogListener clientListener = new LogListener(clientLogTextArea);
		String clientLog = Util.getLogFilePath(UserType.Client);
		log.debug("Client is " + UserType.Client);
		File clientLogFile = new File(clientLog);
		Tailer.create(clientLogFile, clientListener);
		// making the textareas non-editable
		clientLogTextArea.setEditable(false);
		serviceLogTextArea.setEditable(false);

		LogListener serviceListener = new LogListener(serviceLogTextArea);
		String serviceLog = Util.getLogFilePath(UserType.Service);
		File serviceLogFile = new File(serviceLog);
		Tailer.create(serviceLogFile, serviceListener);
	}

	@FXML
	private void sendLogButtonAction(ActionEvent event) {
		sendLogToShellfire();
	}

	private void sendLogToShellfire() {
		final SendLogTask sendLogTask = new SendLogTask();
		Thread t = new Thread(sendLogTask);

		if (sendLogProgressDialog == null) {
			try {
				sendLogProgressDialog = ProgressDialogController.getInstance(i18n.tr("Upload log.."), sendLogTask, instanceStage, true);
				sendLogProgressDialog.setButtonText(i18n.tr("Cancel"));

			} catch (IOException ex) {
				log.debug("connectFromButton. Error is " + ex.getMessage());
			}
		}
		Platform.runLater(() -> {
			sendLogProgressDialog.getButton().setOnAction(new EventHandler<javafx.event.ActionEvent>() {

				@Override
				public void handle(javafx.event.ActionEvent event) {
					sendLogProgressDialog.getDialogStage().hide();
					Alert alert = new Alert(Alert.AlertType.INFORMATION, i18n.tr("Log upload cancelled."));
					alert.show();
					if (sendLogTask != null && !sendLogTask.isDone()) {
						sendLogTask.cancel(true);
					}
				}
			});

			sendLogProgressDialog.getDialogStage().show();
			t.start();
		});

	}

	public void setApp(LoginForms app) {
		this.application = app;
	}

	public class SendLogTask extends Task<Boolean> {

		@Override
		protected Boolean call() throws Exception {
			// throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools |
			// Templates.
			log.debug("sending logs to shellfire");
			WebService service = WebService.getInstance();
			
			return service.sendLogToShellfire();
		}

		@Override
		protected void succeeded() {
			Boolean result = false;
			
			try {
				result = get();
			} catch (InterruptedException e) {
				log.error("Could not retrieve result", e);
			} catch (ExecutionException e) {
				log.error("Could not retrieve result", e);
				e.printStackTrace();
			}
			
			sendLogProgressDialog.getDialogStage().hide();
			if (result == true) {
				Alert alert = new Alert(Alert.AlertType.INFORMATION, i18n.tr("Log sent."));
				alert.show();
			} else {
				Alert alert = new Alert(Alert.AlertType.ERROR, i18n.tr("Could not sent log."));
				alert.show();
			}
		}
	}

	public static Stage getInstanceStage() {
		return instanceStage;
	}

	public static LogViewerFxmlController getInstance() throws IOException {
		log.debug("getInstance()");
		if (instance == null) {
			log.debug("creating new instance");

			// setting the width and height of stage
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			int width = (int) (screenSize.getWidth() / 2);
			int height = (int) (screenSize.getHeight() / 2);

			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(LoginForms.class.getResource("/fxml/LogViewerFxml.fxml"));
			AnchorPane page = (AnchorPane) loader.load();
			instance = (LogViewerFxmlController) loader.getController();
			instanceStage = new Stage();
			instanceStage.setTitle("Log Viewer");
			Scene scene = new Scene(page, width, height);
			instanceStage.setScene(scene);
		}
		log.debug("returning instance");
		return instance;
	}
}
