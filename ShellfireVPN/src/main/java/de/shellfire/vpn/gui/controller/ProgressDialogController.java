package de.shellfire.vpn.gui.controller;

import de.shellfire.vpn.Util;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

import java.net.URL;
import java.util.ResourceBundle;

import org.xnap.commons.i18n.I18n;

import de.shellfire.vpn.gui.LoginForms;
import de.shellfire.vpn.i18n.VpnI18N;
import java.io.IOException;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

import javafx.scene.control.Label;

import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.slf4j.Logger;

public class ProgressDialogController extends AnchorPane implements Initializable {

    private boolean option1;
    private boolean option2;
    private Task optionCallback;
    private static I18n i18n = VpnI18N.getI18n();
    private static LoginForms application;
    private static Stage instanceStage ;
    private static ProgressDialogController instance ;
    
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
    private Button rightButton;
    
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
        //additionTextLabel.setText("<dynamic>");
        this.headerImageView1.setImage(ShellfireVPNMainFormFxmlController.getLogo());
        log.debug("\n "+com.sun.javafx.runtime.VersionInfo.getRuntimeVersion());
    }


    public Button getRightButton() {
        rightButtonExist();
        return rightButton;
    }
    
    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public void setOptionCallback(Task task) {
        // make the button visible when a task has to be assigned to the cancel button 
        rightButtonExist();
        log.debug("setOptionCallback: Runnable has been initialised " + task.toString());
        this.optionCallback = task;
    }

    public void callOptionCallback() {
        if (this.optionCallback != null)
            this.optionCallback.run();
    }

    public void updateProgress(double percentage) {
        // just set the update progress property
        progressBar.setProgress(percentage);
    }

    public Label getDynamicLabel() {
        return dynamicLabel;
    }



    public void setOption(int i, String text) {
        this.setOption(i, text, 0);
    }

    void setOption(int i, final String text, int waitTime) {
        Button button = null;
        if (i == 2 && null!=rightButton) {
            button = rightButton;
        }
    }
    public void setIndeterminate(boolean b) {
        if (b == true) {
            progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        }
    }
  
    private void handleRightButton(ActionEvent event) {
        this.option2 = true;
        rightButton.setVisible(false);
        this.optionCallback.cancel(true);
    }

    /**
     * Corresponds to Swing setText() method
     * Text used in constructor of progressDialog swing
     */
    
    // Removed because the dynamic label has a binding in EntityManager
    public void setDialogText(String string) {
        this.dynamicLabel.setText(string);
    }

    public static Stage getDialogStage() {
        instanceStage.sizeToScene();
        return instanceStage;
    }
    
    public static ProgressDialogController getInstance(String dialogText, Task task, Window owner, boolean createNew) throws IOException{
        if(instance == null || createNew){
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(LoginForms.class.getResource("/fxml/ProgressDialog.fxml"));
            AnchorPane page = (AnchorPane) loader.load();
            instance = (ProgressDialogController)loader.getController();
            instance.setDialogText(dialogText);
            instance.getProgressBar().setProgress(ProgressBar.INDETERMINATE_PROGRESS);
            if (null != task){instance.setOptionCallback(task);}
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
    
    private  void rightButtonExist(){
        if(rightButton == null){
            rightButton = new Button(i18n.tr("Cancel"));
            rightButton.setPrefWidth(100);
            contentPane.getChildren().add(rightButton);
            Pane spacePane = new Pane();
            spacePane.setPrefHeight(3);
            spacePane.setMinHeight(3);
            spacePane.setMaxHeight(3);
            contentPane.getChildren().add(spacePane);
        }
    }
}