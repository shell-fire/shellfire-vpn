package de.shellfire.vpn.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

import java.net.URL;
import java.util.ResourceBundle;

import org.xnap.commons.i18n.I18n;

import de.shellfire.vpn.gui.LoginForms;
import de.shellfire.vpn.i18n.VpnI18N;
<<<<<<< HEAD
import java.awt.event.ActionListener;
=======
>>>>>>> 32656c998715dfdf2cb3c2b13af96c74a646dc3b
import javafx.event.ActionEvent;

import javafx.scene.control.Label;

import javafx.scene.control.ProgressBar;
<<<<<<< HEAD
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javax.swing.Timer;

public class ProgressDialogController extends AnchorPane implements Initializable {
=======
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

public class ProgressDialogController extends AnchorPane implements Initializable {
	
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
	
>>>>>>> 32656c998715dfdf2cb3c2b13af96c74a646dc3b
	private boolean option1;
    private boolean option2;
    private Runnable optionCallback;
	private static I18n i18n = VpnI18N.getI18n();
<<<<<<< HEAD
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
				
=======
	
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
		button2.managedProperty().bind(button2.visibleProperty());
		button1.managedProperty().bind(button1.visibleProperty());
		this.button1.setDisable(true);
		this.button2.setDisable(true);		
>>>>>>> 32656c998715dfdf2cb3c2b13af96c74a646dc3b
	}
	
	public void setApp(LoginForms applic){
		this.application = applic ; 
	}
	
	public  void initComponenets(){
<<<<<<< HEAD
		dynamicLabel.setText(i18n.tr("Einloggen...."));
		additionTextLabel.setText("jLabel2");
		rightButton.setDisable(true);
		bottomLabel.setDisable(true);
                
                

	}

    public ProgressBar getProgressBar() {
        return progressBar;
    }
=======
		setDialogText(i18n.tr("Einloggen...."));
		additonalTextLabel.setText("jLabel2");
		button2.setText("jButton1");
		bottomLabel.setText("jLabel2");
		//headerPane.setLayout();
		//ImageI logoImg = ShellfireVPNMainForm.getLogo();
		//headerImgLabel.
	}
	
	public void setDialogText(String string){
		label1.setText(string);
	}
>>>>>>> 32656c998715dfdf2cb3c2b13af96c74a646dc3b
	
    public void setOptionCallback(Runnable runnable) {
        this.optionCallback = runnable;
    }

    private void callOptionCallback() {
        if (this.optionCallback != null);
            this.optionCallback.run();
    }
    
    public void updateProgress(double percentage){
    	// just set the update progress property
<<<<<<< HEAD
    	progressBar.setProgress(percentage);
    }
    
    void addInfo(String text){
    	this.setTextAndShowComponent(this.additionTextLabel, text);
=======
    	progressDialogBar.setProgress(percentage);
    }
    
    void addInfo(String text){
    	this.setTextAndShowComponent(this.additonalTextLabel, text);
>>>>>>> 32656c998715dfdf2cb3c2b13af96c74a646dc3b
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
<<<<<<< HEAD
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
=======
       /// this.setOption(i, text, 0);        
>>>>>>> 32656c998715dfdf2cb3c2b13af96c74a646dc3b
    }
    public boolean isOption1() {
        return option1;
    }
    public boolean isOption2() {
        return option2;
    }

	public void setIndeterminate(boolean b) {
		if (b == true)
<<<<<<< HEAD
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
=======
			progressDialogBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
				
	}
>>>>>>> 32656c998715dfdf2cb3c2b13af96c74a646dc3b
}
