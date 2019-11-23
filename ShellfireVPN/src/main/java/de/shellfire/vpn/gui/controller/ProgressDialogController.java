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
import java.awt.event.ActionListener;
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
import javax.swing.Timer;
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
    // corresponds to the cancel button
    private Button leftButton;
    @FXML
    private Button rightButton;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label bottomLabel;
    private static final Logger log = Util.getLogger(ProgressDialogController.class.getCanonicalName());

    public ProgressDialogController() {
        log.debug("ProgressDialogController: In netbeans");
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        initComponenets();

    }

    public static void setApp(LoginForms applic) {
        application = applic;
    }
    
    public static LoginForms getApplication() {
        return application;
    }
    
    public void initComponenets() {
        dynamicLabel.setText(i18n.tr("Logging in..."));
        //additionTextLabel.setText("<dynamic>");
        rightButton.setDisable(true);
        leftButton.setDisable(true);
        bottomLabel.setDisable(true);
    }

    public Button getLeftButton() {
        return leftButton;
    }

    public Button getRightButton() {
        return rightButton;
    }
    
    public void setLeftButton(Button leftButton) {
        this.leftButton = leftButton;
    }
    
    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public void setOptionCallback(Task task) {
        // make the button visible when a task has to be assigned to the cancel button
        this.rightButton.setDisable(false);
        this.rightButton.setVisible(true);
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


    void setTextAndShowComponent(Label lbl, String text) {
        lbl.setText(text);
        lbl.setVisible(true);
    }

    public Label getDynamicLabel() {
        return dynamicLabel;
    }

    void setTextAndShowComponent(Button btn, String text) {
        btn.setText(text);
        btn.setVisible(true);
        btn.setDisable(true);
    }

    public void addBottomText(String text) {
        this.setTextAndShowComponent(this.bottomLabel, text);
    }

    public void setOption(int i, String text) {
        this.setOption(i, text, 0);
    }

    void setOption(int i, final String text, int waitTime) {
        Button button = null;
        if (i == 1) {
            button = leftButton;
        } else if (i == 2) {
            button = rightButton;
        }

        class OptionListener implements ActionListener {

            private Button button;
            private String text;

            public OptionListener(Button b, String t) {
                this.button = b;
                this.text = t;
            }

            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                this.button.setDisable(false);
            }
        };
        setTextAndShowComponent(button, text);
        Timer t = new Timer(waitTime * 1000, new OptionListener(button, text));
        t.setRepeats(false);
        t.start();
    }

    public boolean isOption1() {
        return option1;
    }

    public boolean isOption2() {
        return option2;
    }

    public void setIndeterminate(boolean b) {
        if (b == true) {
            progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        }

    }

    // Event Listener on Button[#button1].onAction

    @FXML
    private void handleLeftButton(ActionEvent event) {
        log.debug("handleLeftButton has been clicked");
        this.option1 = true;
        leftButton.setVisible(false);
        this.optionCallback.cancel(true);
    }

    // Event Listener on Button[#button2].onAction

    @FXML
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
}