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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import org.apache.commons.validator.GenericValidator;
import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;

/**
 * FXML Controller class
 *
 * @author Tcheutchoua
 */
public class RegiesterFormController extends AnchorPane implements Initializable {

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
    private TextFlow policyTextFlow;
    @FXML
    private Label registerBackLabel;

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

    public RegiesterFormController() {
    }

    /**
     * Constructor of RegisterFormController
     *
     * params: Application class , LoginForms
     */

    public RegiesterFormController(LoginForms parentFrame) {
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

        this.registerBackLabel.setText(i18n.tr("zurÃ¼ck"));

        // adding components of the policy and terms of agreement textflow
        //policyTextFlow
        Text t1 = new Text(i18n.tr("Ich akzeptiere die"));
        Hyperlink termsAndConditions = new Hyperlink(i18n.tr("AGB"));
        termsAndConditions.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                Util.openUrl("https://www.shellfire.de/agb/");
            }
        });

        Text t2 = new Text(i18n.tr("und habe die"));
        Hyperlink privacyPolicy = new Hyperlink(i18n.tr("Datenschutzerklärung"));
        privacyPolicy.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                Util.openUrl("https://www.shellfire.de/datenschutzerklaerung/");
            }
        });

        Text t3 = new Text(i18n.tr("sowie das"));
        Hyperlink rightOfWidthdrawal = new Hyperlink(i18n.tr("Widerrufsrecht"));
        rightOfWidthdrawal.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                Util.openUrl("https://www.shellfire.de/widerrufsrecht/");
            }
        });
        Text t4 = new Text(i18n.tr("zur Kenntnis genommen"));

        // because policyTextFlow was defined in fxml , we use the add method
        policyTextFlow.getChildren().addAll(t1, termsAndConditions, t2, privacyPolicy, t3, rightOfWidthdrawal, t4);
        //policyTextFlow = new TextFlow(t1,termsAndConditions,t2,privacyPolicy,t3,rightOfWidthdrawal, t4);

        //policyTextFlow.setVisible(true);
    }

    @FXML
    private void handleSelectVpnButton(ActionEvent event) {
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
                SwingUtilities.invokeLater(new RegiesterFormController.FocusRequester(jumpTo));
            }
        }

        return !error;
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

                JOptionPane.showMessageDialog(null, i18n.tr("Fehler bei Registrierung:") + " " + i18n.tr(registrationResult.getMessage()),
                        i18n.tr("Fehler"), JOptionPane.ERROR_MESSAGE);
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
