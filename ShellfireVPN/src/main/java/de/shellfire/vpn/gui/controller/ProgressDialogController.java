package de.shellfire.vpn.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

import java.net.URL;
import java.util.ResourceBundle;

import org.xnap.commons.i18n.I18n;

import de.shellfire.vpn.gui.LoginForms;
import de.shellfire.vpn.i18n.VpnI18N;
import java.awt.event.ActionListener;
import javafx.event.ActionEvent;

import javafx.scene.control.Label;

import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javax.swing.Timer;

public class ProgressDialogController extends AnchorPane implements Initializable {
	private boolean option1;
    private boolean option2;
    private Runnable optionCallback;
	private static I18n i18n = VpnI18N.getI18n();
	private LoginForms application ; 
        
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
    private ProgressBar progressBar;
    @FXML
    private Label additionTextLabel;
	@FXML
	private Label bottomLabel;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		initComponenets();
				
	}
	
	public void setApp(LoginForms applic){
		this.application = applic ; 
	}
	
	public  void initComponenets(){
		dynamicLabel.setText(i18n.tr("Einloggen...."));
		additionTextLabel.setText("jLabel2");
		rightButton.setDisable(true);
		bottomLabel.setDisable(true);
                
                

	}

    public ProgressBar getProgressBar() {
        return progressBar;
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
    	progressBar.setProgress(percentage);
    }
    
    void addInfo(String text){
    	this.setTextAndShowComponent(this.additionTextLabel, text);
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
		if (b == true)
			progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
				
	}
    // Event Listener on Button[#button1].onAction
    @FXML
    private void handleLeftButton(ActionEvent event) {
                this.option1 = true;
        leftButton.setVisible(false);
        this.callOptionCallback();
    }
    	// Event Listener on Button[#button2].onAction
    @FXML
    private void handleRightButton(ActionEvent event) {
        		this.option2 = true;
		rightButton.setVisible(false);
		this.callOptionCallback();
    }
    
        public void setDialogText(String string) {
        dynamicLabel.setText(string);
    }
}
