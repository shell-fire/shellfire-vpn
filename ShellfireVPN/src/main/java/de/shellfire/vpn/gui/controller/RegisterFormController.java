/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui.controller;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.gui.LoginForms;
import de.shellfire.vpn.gui.ProgressDialog;
import de.shellfire.vpn.gui.RegisterForm;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.webservice.Response;
import de.shellfire.vpn.webservice.WebService;
import de.shellfire.vpn.webservice.model.LoginResponse;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.HyperlinkEvent;
import org.apache.commons.validator.GenericValidator;
import org.codefx.libfx.control.webview.WebViewHyperlinkListener;
import org.codefx.libfx.control.webview.WebViews;
import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;

/**
 * FXML Controller class
 *
 * @author Tcheutchoua
 */
public class RegisterFormController extends AnchorPane implements Initializable {

    @FXML
    private Button registerButton;
    @FXML
    private Label registerHeadingLabel;
    @FXML
    private CheckBox newsLetterCheckBox;
    @FXML
    private CheckBox fAutoconnect1;
    @FXML
    private TextField emailTextField;
    @FXML
    private Label emailLabel;
    @FXML
    private Label passwordLabel;
    @FXML
    private Label confirmPasswordLabel;
    @FXML
    private Pane headerPanel;
    @FXML
    private ImageView headerImageView;
    @FXML
    private Pane backLabelPane;
    @FXML
    private ImageView backImageVeiw;

    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label registerBackLabel;
    @FXML
    private WebView policyWebView;
        
    private static Logger log = Util.getLogger(RegisterForm.class.getCanonicalName());
    public static final String REG_PASS = "pass";
    public static final String REG_USER = "user";
    WebService service;
    private ProgressDialog progressDialog;
    private String activationToken;
    private LoginForms application;
    private boolean isResend = false;
    private boolean accountActive = false;
    //private RegisterForm.AccountActiveServicePollerTask poller;
    private static I18n i18n = VpnI18N.getI18n();
    
    WebViewHyperlinkListener eventPrintingListener ;

    public RegisterFormController() {
        System.out.println("*********No arg constructor was used");
    }

    /**
     * Constructor of RegisterFormController
     *
     * params: Application class , LoginForms
     */

    public RegisterFormController(LoginForms parentFrame) {
        this.application = parentFrame;

        this.application.getStage().setTitle("Shellfire VPN Registrierung");
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.service = WebService.getInstance();
        initComponents();

    }

    public void initComponents() {
        this.registerHeadingLabel.setText(i18n.tr("Registrierung"));
        //this.emailTextField.setText(i18n.tr("Registrierung"));

        this.passwordLabel.setText(i18n.tr("Passwort:"));

        this.confirmPasswordLabel.setText(i18n.tr("Passwort-Check:"));

        this.registerButton.setText(i18n.tr("Jetzt Registrieren"));

        this.newsLetterCheckBox.setText(i18n.tr("Ich abonniere den Newsletter"));

        this.registerBackLabel.setText(i18n.tr("zurück"));

        // Load web content to webView
        WebEngine webEngine = policyWebView.getEngine();
        String webContent = "<html>" + "  <body>"
                + i18n
                .tr("Ich akzeptiere die <a onclick='return false;' target='_agb' href='https://www.shellfire.de/agb/'>AGB</a> und habe die <a onclick='return false;' target='_datenschutzerklaerung' href='https://www.shellfire.de/datenschutzerklaerung/'>Datenschutzerklärung</a><br />sowie das <a onclick='return false;' target='_widerrufsrecht' href='https://www.shellfire.de/widerrufsrecht/'>Widerrufsrecht</a> zur Kenntnis genommen")
                + "  </body>" + "</html>";
        webEngine.loadContent(webContent);        
      
         HostServices hostServices = (HostServices)this.application.getStage().getProperties().get("hostServices");        
         
         eventPrintingListener = event -> {
             // Check if the link has been clicked then, open in external browser.
             if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
             hostServices.showDocument(event.getURL().toString());
             }
	
	return false;
    };
          WebViews.addHyperlinkListener(policyWebView, eventPrintingListener);
    }


    @FXML
    private void handlefAutoconnect(ActionEvent event) {
    }

    @FXML
    private void handleBackLabel(MouseEvent event) {
    }

    private String getUser() {
        return emailLabel.getText();
    }

    // application has been declared final,
    // so it will be passed int he controller 
    // and will not neet to be modified latter
    public void setApp(LoginForms applic) {
        this.application = applic;
    }

    private String getPassword() {
        return passwordField.getText();
    }

    private String getPasswordCheck() {
        return confirmPasswordField.getText();
    }

    private boolean validateForm() {
        boolean error = false;
        String message = "";
        TextField jumpTo = null;
        String email = emailTextField.getText();
        if (!GenericValidator.isEmail(email)) {
            error = true;
            message = i18n.tr("Bitte gib eine gültige Email-Adresse ein.");
            jumpTo = emailTextField;
        }

        String password = this.getPassword();
        String passwordCheck = this.getPasswordCheck();
        if (!error && GenericValidator.isBlankOrNull(password)) {
            error = true;
            message = i18n.tr("Bitte gib ein Passwort ein.");
            jumpTo = passwordField;
        }

        if (!error && password.length() < 5) {
            error = true;
            message = i18n.tr("Dein Passwort muss mindestens 5 Zeichen lang sein.");
            jumpTo = passwordField;
        }

        if (!error && GenericValidator.isBlankOrNull(passwordCheck)) {
            error = true;
            message = i18n.tr("Bitte gib dein Passwort zur Sicherheit noch einmal im Feld Passwort-Check ein.");
            jumpTo = confirmPasswordField;
        }

        if (!error && !password.equals(passwordCheck)) {
            error = true;
            message = i18n.tr("Passwort und Passwort-Check stimmen nicht überein.");
            jumpTo = confirmPasswordField;
        }

        if (!error && !fAutoconnect1.isSelected()) {
            error = true;
            message = i18n.tr("Die AGB, Datenschutzerklärung und Widerrufstrecht müssen akzeptiert werden, um die Registrierung abzuschließen.");
            jumpTo = null;
        }

        if (error) {
            JOptionPane.showMessageDialog(null, message, i18n.tr("Fehler"), JOptionPane.ERROR_MESSAGE);
            if (jumpTo != null) {
                SwingUtilities.invokeLater(new RegisterFormController.FocusRequester(jumpTo));
            }
        }

        return !error;
    }

    @FXML
    private void handleBackLabelClicked(MouseEvent event) {
        this.application.loadLoginController();
        this.application.getStage().show();
    }

    @FXML
    private void handleMouseExited(MouseEvent event) {
        this.application.getStage().getScene().setCursor(Cursor.DEFAULT);
    }

    @FXML
    private void handleMouseEntered(MouseEvent event) {
        this.application.getStage().getScene().setCursor(Cursor.HAND);
    }

    @FXML
    private void handleMouseImgExited(MouseEvent event) {
        this.application.getStage().getScene().setCursor(Cursor.DEFAULT);
    }

    @FXML
    private void handleMouseImgEntered(MouseEvent event) {
        this.application.getStage().getScene().setCursor(Cursor.HAND);
    }

    @FXML
    private void handleBackLabelImgClicked(MouseEvent event) {
        this.application.loadLoginController();
        this.application.getStage().show();
    }


    @FXML
    private void handleRegisterButton(ActionEvent event) {
    }

    private static class FocusRequester implements Runnable {

        private final TextField jumpTo;

        public FocusRequester(TextField jumpTo) {
            this.jumpTo = jumpTo;
        }

        @Override
        public void run() {
            jumpTo.requestFocus();
        }
    }

    private void hideProgress() {
        progressDialog.setVisible(false);

    }

    class RequestNewAccountTask extends SwingWorker<Void, Void> {

        /*
     * Executed in event dispatch thread
         */
        public void done() {
            hideProgress();

            if (activationToken != null) {
                waitForActivation();
            }
        }

        @Override
        protected Void doInBackground() throws Exception {
            Response<LoginResponse> registrationResult = service.registerNewFreeAccount(emailTextField.getText(), passwordField.getText(),
                    newsLetterCheckBox.isSelected());

            if (registrationResult.isSuccess()) {
                activationToken = registrationResult.getData().getToken();
            } else {
                if (progressDialog != null) {
                    progressDialog.setVisible(false);
                }

                //JOptionPane.showMessageDialog(null, i18n.tr("Fehler bei Registrierung:") + " " + i18n.tr(registrationResult.getMessage()),
                      //  i18n.tr("Fehler"), JOptionPane.ERROR_MESSAGE);
                Alert alert = new Alert(Alert.AlertType.ERROR);
                    //alert.setTitle("Error");
                    alert.setHeaderText(i18n.tr("Fehler"));
                    alert.setContentText(i18n.tr("Fehler bei Registrierung:") + " " + i18n.tr(registrationResult.getMessage()));
                    alert.showAndWait();
                    //Platform.exit();
            }

            return null;
        }
    }

    // needs a swing pane for successful execution. 
    private void waitForActivation() {

        /*    this.progressDialog = new ProgressDialog(this, false,
                i18n.tr("Das Shellfire VPN System hat dir soeben eine Email geschickt, bitte folge den Anweisungen dort."));
        this.progressDialog.addInfo(i18n.tr("Warte auf Aktivierung des Accounts..."));
        this.progressDialog.addBottomText(i18n.tr("Keine Email erhalten?"));
        this.progressDialog.setOption(1, i18n.tr("Email neu anfordern"), 30);
        this.progressDialog.setOption(2, i18n.tr("Emailadresse ändern"), 30);
        this.progressDialog.setOptionCallback(new Runnable() {

            @Override
            public void run() {
                if (poller != null) {
                    poller.stopIt();
                }
                if (progressDialog.isOption1()) {
                    progressDialog.setVisible(false);
                    isResend = true;
                    jButtonRequestRegKeyActionPerformed(null);
                } else if (progressDialog.isOption2()) {
                    progressDialog.setVisible(false);
                    isResend = false;
                    JOptionPane.showMessageDialog(null, i18n.tr("Bitte wähle nun eine andere Email-Adresse und versuche es erneut."),
                            i18n.tr("Email-Adresse ändern"), JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        this.progressDialog.setVisible(true);
        poller = new RegisterForm.AccountActiveServicePollerTask();
        poller.execute();
         */
    }

}
