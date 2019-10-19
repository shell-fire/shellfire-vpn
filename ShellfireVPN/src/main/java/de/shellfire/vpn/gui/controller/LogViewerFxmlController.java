/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui.controller;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.gui.LoginForms;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.messaging.UserType;
import de.shellfire.vpn.webservice.WebService;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
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
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;

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
    private TextArea clientLogTextAruea;
    @FXML
    private TextArea serviceLogTextArea;
    
    private static I18n i18n = VpnI18N.getI18n(); 
    // The Stage for the logViewer
    protected static Stage instanceStage;
    protected static ProgressDialogController sendLogProgressDialog;
    private static Logger log = Util.getLogger(LogViewerFxmlController.class.getCanonicalName());
    private static LoginForms application;
    private static LogViewerFxmlController instance;
    SendLogTask sendLogTask = new SendLogTask();
    
    class LogListener extends TailerListenerAdapter {
        private TextArea textArea;

        public LogListener(TextArea textArea) {
          this.textArea = textArea;
        }

        public void handle(String line) {
            Platform.runLater(()->{
            if(line!=null){
                this.textArea.appendText(line + "\n");
                this.textArea.end();
            }});
        }
    }  
  
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    
        LogListener clientListener = new LogListener(clientLogTextAruea);
        String clientLog = Util.getLogFilePath(UserType.Client);
        log.debug("Client is " + UserType.Client);
        File clientLogFile = new File(clientLog);
        Tailer.create(clientLogFile, clientListener);


        LogListener serviceListener = new LogListener(serviceLogTextArea);
        String serviceLog = Util.getLogFilePath(UserType.Service);
        File serviceLogFile = new File(serviceLog);
        Tailer.create(serviceLogFile, serviceListener);
    }    

    @FXML
    private void sendLogButtonAction(ActionEvent event) {
        //sendLogToShellfire();
    }
    
    private void sendLogToShellfire() {

        Platform.runLater(()->{
            try {
                // Load the fxml file and create a new stage for the popup dialog.
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(LoginForms.class.getResource("/fxml/ProgressDialog.fxml"));
                AnchorPane page = (AnchorPane) loader.load();
                sendLogProgressDialog = (ProgressDialogController)loader.getController();
                sendLogProgressDialog.setDialogText(i18n.tr("Upload log.."));
                sendLogProgressDialog.getProgressBar().setProgress(ProgressBar.INDETERMINATE_PROGRESS);                
                sendLogProgressDialog.setOptionCallback(sendLogTask);
                sendLogProgressDialog.getLeftButton().setOnAction(new EventHandler<javafx.event.ActionEvent>() {

                    @Override
                    public void handle(javafx.event.ActionEvent event) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION, i18n.tr("Log upload cancelled."));
                        alert.show();
                        if (sendLogTask != null && !sendLogTask.isDone() )
                        sendLogTask.cancel(true);
                    }
                });
//                instanceStage = new Stage();
//                instanceStage.initStyle(StageStyle.UNDECORATED);
//                instanceStage.setTitle("Message");
//                instanceStage.initModality(Modality.WINDOW_MODAL);
//                instanceStage.initOwner(this.application.getStage());
//                Scene scene = new Scene(page);
//                instanceStage.setScene(scene);
            
                sendLogProgressDialog.setOption(2, i18n.tr("cancel"));
                sendLogProgressDialog.setVisible(true);
            } catch (IOException ex) {
                log.debug("connectFromButton. Error is " + ex.getMessage());
            }
        });    

    Thread t = new Thread(sendLogTask);
    t.start();
  }

    public void setApp(LoginForms app){
        this.application = app;
    }
    
    public class SendLogTask extends Task<Void>{

        private boolean finished = false;

        @Override
        protected Void call() throws Exception {
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            WebService service = WebService.getInstance();
            service.sendLogToShellfire();
            finished = true;
            return null;
        }

        @Override
        protected void succeeded() {
            super.succeeded(); //To change body of generated methods, choose Tools | Templates.
            if (finished == true) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, i18n.tr("Log sent."));
                alert.show();
            }
        }
        
        
    }

    public static Stage getInstanceStage() {
        return instanceStage;
    }
    
    public static LogViewerFxmlController getInstance() throws IOException{
        log.debug("getInstance()");
        if (instance == null) {
            log.debug("creating new instance");
            
            // setting the width and height of stage 
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int width = (int) (screenSize.getWidth() / 1.5);
            int height = (int) (screenSize.getHeight() / 1.5);
        
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(LoginForms.class.getResource("/fxml/LogViewerFxml.fxml"));
            AnchorPane page = (AnchorPane) loader.load(); 
            instance = (LogViewerFxmlController)loader.getController();
            instanceStage = new Stage();
            instanceStage.setTitle("Log Viewer");
            Scene scene = new Scene(page, width, height);
            instanceStage.setScene(scene);    
        }
        log.debug("returning instance");
        return instance;
    }
}
