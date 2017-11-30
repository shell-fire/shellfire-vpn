/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui.controller;

import de.shellfire.vpn.gui.LoginForms;
import de.shellfire.vpn.gui.controller.*;
import de.shellfire.vpn.i18n.VpnI18N;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import org.xnap.commons.i18n.I18n;

/**
 * FXML Controller class
 *
 * @author Tcheutchoua
 */
public class ProgressDialogController extends AnchorPane implements Initializable {

    @FXML
    private Pane headerPanel1;
    @FXML
    private ImageView headerImageView1;
    @FXML
    private Label dynamicLabel;
    @FXML
    private Button leftButton;
    @FXML
    private Button rightButton;
    @FXML
    private Label additionTextLabel;
    @FXML
    private Label bottomLabel;

    @FXML
    private ProgressBar progressBar;

    private boolean option1;
    private boolean option2;
    private Runnable optionCallback;
    private static I18n i18n = VpnI18N.getI18n();
    private LoginForms application;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
            
         initComponents();
        // Binding labels and buttons to their visibilities
        leftButton.managedProperty().bind(leftButton.visibleProperty());
        rightButton.managedProperty().bind(rightButton.visibleProperty());
        bottomLabel.managedProperty().bind(bottomLabel.visibleProperty());
        additionTextLabel.managedProperty().bind(additionTextLabel.visibleProperty());


        this.pack();
        setIndeterminate(true);
    }

    @FXML
    private void handleLeftButton(ActionEvent event) {
        this.option1 = true;
        //setVisible(false);
        this.callOptionCallback();
    }

    @FXML
    private void handleRightButton(ActionEvent event) {
        this.option2 = true;
        //setVisible(false);
        this.callOptionCallback();
    }

    public void setApp(LoginForms applic) {
        this.application = applic;
    }

    public void initComponents() {
        leftButton.setVisible(false);
        rightButton.setVisible(false);
        bottomLabel.setVisible(false);
        additionTextLabel.setVisible(false);

    }

    public void setDialogText(String string) {
        dynamicLabel.setText(string);
    }

    public void setAdditonalTextVisible(boolean visible, String text) {
        additionTextLabel.setVisible(visible);
        additionTextLabel.setText(i18n.tr(text));
    }

    public void setBottomLabelTextVisible(boolean visible, String text) {
        bottomLabel.setVisible(visible);
        bottomLabel.setText(i18n.tr(text));
    }

    public void setLeftButtonVisible(boolean visible, String text) {
        leftButton.setVisible(visible);
        leftButton.setText(i18n.tr(text));
    }

    public void setRighttButtonVisible(boolean visible, String text) {
        rightButton.setVisible(visible);
        rightButton.setText(i18n.tr(text));
    }

    public void pack() {
        this.application.getStage().sizeToScene();
        // TODO implement a custom version of swing pack.
    }

    public boolean isOption1() {
        return option1;
    }

    public boolean isOption2() {
        return option2;
    }

    public void setOptionCallback(Runnable runnable) {
        this.optionCallback = runnable;
    }

    private void callOptionCallback() {
        if (this.optionCallback != null);
        this.optionCallback.run();
    }

    public void updateProgress(double percentage) {
        // just set the update progress property
        progressBar.setProgress(percentage);
    }

    public void setIndeterminate(boolean b) {
        if (b == true) {
            progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        }

    }
}
