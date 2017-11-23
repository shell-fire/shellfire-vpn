package de.shellfire.vpn.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

import java.net.URL;
import java.util.ResourceBundle;

import org.xnap.commons.i18n.I18n;

import de.shellfire.vpn.gui.LoginForms;
import de.shellfire.vpn.i18n.VpnI18N;
import javafx.event.ActionEvent;

import javafx.scene.control.Label;

import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

public class ProgressDiagogVController extends AnchorPane implements Initializable {
	@FXML
	private Pane headerPane;
	@FXML
	private Label headerImgLabel;
	@FXML
	private ProgressBar progressDialogBar;
	@FXML
	private Button button1;
	@FXML
	private Button button2;
	@FXML
	private Label bottomLabel;
	@FXML
	private Label label1;
	@FXML
	private Label additonalTextLabel;

	private boolean option1;
    private boolean option2;
    private Runnable optionCallback;
	private static I18n i18n = VpnI18N.getI18n();
	private LoginForms application ; 

	// Event Listener on Button[#button1].onAction
	@FXML
	public void handleButton1(ActionEvent event) {
        this.option1 = true;
        button1.setVisible(false);
        this.callOptionCallback();
	}
	// Event Listener on Button[#button2].onAction
	@FXML
	public void handleButton2(ActionEvent event) {
		this.option2 = true;
		button2.setVisible(false);
		this.callOptionCallback();
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		initComponenets();
				
	}
	
	public void setApp(LoginForms applic){
		this.application = applic ; 
	}
	
	public  void initComponenets(){
		label1.setText(i18n.tr("Einloggen...."));
		additonalTextLabel.setText("jLabel2");
		button2.setText("jButton1");
		bottomLabel.setText("jLabel2");
		//headerPane.setLayout();
		//ImageI logoImg = ShellfireVPNMainForm.getLogo();
		//headerImgLabel.
	}
	
    public void setOptionCallback(Runnable runnable) {
        this.optionCallback = runnable;
    }

    private void callOptionCallback() {
        if (this.optionCallback != null);
            this.optionCallback.run();
    }
    
    public void updateProgress(double percentage){
    	// just set the update progress property
    	progressDialogBar.setProgress(percentage);
    }
    
    void addInfo(String text){
    	this.setTextAndShowComponent(this.additonalTextLabel, text);
    }
    
    void setTextAndShowComponent(Label lbl, String text){
    	lbl.setText(text);
    	lbl.setVisible(true);
    }
    
    void setTextAndShowComponent(Button btn, String text){
    	btn.setText(text);
    	btn.setVisible(true);
        btn.setDisable(true);
    }
    
    void addBottomText(String text) {
        this.setTextAndShowComponent(this.bottomLabel, text);
    }
    
    public void setOption(int i, String text) {
       /// this.setOption(i, text, 0);        
    }
    public boolean isOption1() {
        return option1;
    }
    public boolean isOption2() {
        return option2;
    }

	public void setIndeterminate(boolean b) {
		if (b == true)
			progressDialogBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
				
	}
}
