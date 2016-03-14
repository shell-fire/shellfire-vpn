/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * LoginForm.java
 *
 * Created on 09.06.2011, 12:49:38
 */
package de.shellfire.vpn.gui;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.html.HTMLDocument;

import org.apache.commons.validator.GenericValidator;
import org.xnap.commons.i18n.I18n;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.gui.helper.MoveMouseListener;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.webservice.Response;
import de.shellfire.vpn.webservice.ShellfireService;
import de.shellfire.vpn.webservice.model.LoginResponse;
import de.shellfire.vpn.webservice.model.WsRegistrationResult;

/**
 *
 * @author bettmenn
 */
public class RegisterForm extends javax.swing.JFrame {

    public static final String REG_PASS = "pass";
    public static final String REG_USER = "user";

    ShellfireService service;
    private ProgressDialog progressDialog;
    private String activationToken;
    private final LoginForm parentFrame;
    private boolean isResend = false;
    private boolean accountActive = false;
    private AccountActiveServicePollerTask poller;
    private static I18n i18n = VpnI18N.getI18n();

    /** Creates new form RegisterForm */
    RegisterForm(LoginForm parent) {
        this.parentFrame = parent;
        this.service = ShellfireService.getInstance();
        this.setUndecorated(true);
        MoveMouseListener mml = new MoveMouseListener(this);
        this.addMouseListener(mml);
        this.addMouseMotionListener(mml);
        this.loadIcon();


        initComponents();
        this.setLocationRelativeTo(null);
        this.pack();
        this.setVisible(true);


    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel6 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jLoginPanel = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        jButtonRequestRegKey = new javax.swing.JButton();
        jEmailAddress = new javax.swing.JTextField();
        jLabelEmailAddress = new javax.swing.JLabel();
        jNewsletterCheckbox = new javax.swing.JCheckBox();
        jLabelEmailAddress1 = new javax.swing.JLabel();
        jLabelEmailAddress2 = new javax.swing.JLabel();
        jPassword = new javax.swing.JPasswordField();
        jPasswordCheck = new javax.swing.JPasswordField();
        jAGBCheckbox = new javax.swing.JCheckBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        jEditorPane1 = new javax.swing.JEditorPane();
        jHeaderPanel = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();

        setTitle("Shellfire VPN Registrierung");
        setResizable(false);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel6.setBackground(new java.awt.Color(255, 255, 255));
        jPanel6.setName("jPanel6"); // NOI18N
        jPanel6.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel6.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(77, 77, 77));
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/shellfire/vpn/gui/resources/exit.png"))); // NOI18N
        jLabel6.setText(i18n.tr("zurück"));
        jLabel6.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel6.setName("jLabel6"); // NOI18N
        jLabel6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel6MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel6MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel6MouseExited(evt);
            }
        });
        jPanel6.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 100, 30));

        getContentPane().add(jPanel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 0, 100, 30));

        jPanel7.setBackground(new java.awt.Color(64, 69, 73));
        jPanel7.setName("jPanel7"); // NOI18N
        jPanel7.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLoginPanel.setBackground(new java.awt.Color(244, 244, 244));
        jLoginPanel.setName("jLoginPanel"); // NOI18N
        jLoginPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel16.setFont(new java.awt.Font("Arial", 1, 24));
        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel16.setText(i18n.tr("registrierung"));
        jLabel16.setName("jLabel16"); // NOI18N
        jLoginPanel.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 330, 33));

        jButtonRequestRegKey.setText(i18n.tr("Jetzt Registrieren"));
        jButtonRequestRegKey.setName("jButtonRequestRegKey"); // NOI18N
        jButtonRequestRegKey.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRequestRegKeyActionPerformed(evt);
            }
        });
        jLoginPanel.add(jButtonRequestRegKey, new org.netbeans.lib.awtextra.AbsoluteConstraints(126, 268, 203, -1));

        jEmailAddress.setName("jEmailAddress"); // NOI18N
        jEmailAddress.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jEmailAddressActionPerformed(evt);
            }
        });
        jEmailAddress.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jEmailAddressKeyPressed(evt);
            }
        });
        jLoginPanel.add(jEmailAddress, new org.netbeans.lib.awtextra.AbsoluteConstraints(126, 61, 207, -1));

        jLabelEmailAddress.setFont(new java.awt.Font("Arial", 0, 11));
        jLabelEmailAddress.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelEmailAddress.setText(i18n.tr("Email-Adresse:"));
        jLabelEmailAddress.setName("jLabelEmailAddress"); // NOI18N
        jLoginPanel.add(jLabelEmailAddress, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 70, 94, -1));

        jNewsletterCheckbox.setSelected(true);
        jNewsletterCheckbox.setText(i18n.tr("Ich abonniere den Newsletter"));
        jNewsletterCheckbox.setName("jNewsletterCheckbox"); // NOI18N
        jLoginPanel.add(jNewsletterCheckbox, new org.netbeans.lib.awtextra.AbsoluteConstraints(99, 247, 230, 15));

        jLabelEmailAddress1.setFont(new java.awt.Font("Arial", 0, 11));
        jLabelEmailAddress1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelEmailAddress1.setText(i18n.tr("Passwort:"));
        jLabelEmailAddress1.setName("jLabelEmailAddress1"); // NOI18N
        jLoginPanel.add(jLabelEmailAddress1, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 106, 94, -1));

        jLabelEmailAddress2.setFont(new java.awt.Font("Arial", 0, 11));
        jLabelEmailAddress2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelEmailAddress2.setText(i18n.tr("Passwort-Check:"));
        jLabelEmailAddress2.setName("jLabelEmailAddress2"); // NOI18N
        jLoginPanel.add(jLabelEmailAddress2, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 142, 94, -1));

        jPassword.setName("jPassword"); // NOI18N
        jPassword.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jPasswordKeyPressed(evt);
            }
        });
        jLoginPanel.add(jPassword, new org.netbeans.lib.awtextra.AbsoluteConstraints(126, 97, 207, -1));

        jPasswordCheck.setName("jPasswordCheck"); // NOI18N
        jPasswordCheck.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jPasswordCheckKeyPressed(evt);
            }
        });
        jLoginPanel.add(jPasswordCheck, new org.netbeans.lib.awtextra.AbsoluteConstraints(126, 133, 207, -1));

        jAGBCheckbox.setName("jAGBCheckbox"); // NOI18N
        jAGBCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jAGBCheckboxActionPerformed(evt);
            }
        });
        jLoginPanel.add(jAGBCheckbox, new org.netbeans.lib.awtextra.AbsoluteConstraints(99, 175, -1, 15));

        jScrollPane1.setBackground(new java.awt.Color(238, 238, 238));
        jScrollPane1.setBorder(null);
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jEditorPane1.setBackground(new java.awt.Color(244, 244, 244));
        jEditorPane1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 0));
        jEditorPane1.setContentType("text/html");
        jEditorPane1.setEditable(false);
        jEditorPane1.setFont(new java.awt.Font("Arial", 0, 10)); // NOI18N
        jEditorPane1.setText("<html>"+
            "  <body>"+
            i18n.tr("Ich akzeptiere die <a target='_agb' href='https://www.shellfire.de/agb/'>AGB</a> und habe die <a target='_datenschutzerklaerung' href='https://www.shellfire.de/datenschutzerklaerung/'>Datenschutzerklärung</a> sowie das <a target='_widerrufsrecht' href='https://www.shellfire.de/widerrufsrecht/'>Widerrufsrecht</a> zur Kenntnis genommen") +
            "  </body>"+
            "</html>"
        );
        jEditorPane1.setDisabledTextColor(new java.awt.Color(238, 238, 238));
        jEditorPane1.setName("jEditorPane1"); // NOI18N
        java.awt.Font font = UIManager.getFont("Label.font");
          String bodyRule = "body { font-family: " + font.getFamily() + "; " +
                  "font-size: " + font.getSize() + "pt; }";
          ((HTMLDocument)jEditorPane1.getDocument()).getStyleSheet().addRule(bodyRule);
        jEditorPane1.addHyperlinkListener(new javax.swing.event.HyperlinkListener() {
            public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {
                jEditorPane1HyperlinkUpdate(evt);
            }
        });
        jScrollPane1.setViewportView(jEditorPane1);

        jLoginPanel.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(129, 175, 200, 62));

        jPanel7.add(jLoginPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 350, 300));

        getContentPane().add(jPanel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 60, 390, 340));

        jHeaderPanel.setBackground(new java.awt.Color(18, 172, 229));
        jHeaderPanel.setName("jHeaderPanel"); // NOI18N
        jHeaderPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel5.setIcon(ShellfireVPNMainForm.getLogo());
        jLabel5.setAlignmentY(0.0F);
        jLabel5.setName("jLabel5"); // NOI18N
        jHeaderPanel.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 250, 60));

        getContentPane().add(jHeaderPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 390, 60));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private String getUser() {
        return jEmailAddress.getText();
    }

    private void jButtonRequestRegKeyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRequestRegKeyActionPerformed
        if (validateForm()) {
            this.showRequestProgress();

            RequestNewAccountTask task = new RequestNewAccountTask();
            task.execute();
        }

    }//GEN-LAST:event_jButtonRequestRegKeyActionPerformed

    private void jLabel6MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel6MouseClicked
        this.setVisible(false);
        parentFrame.setVisible(true);
}//GEN-LAST:event_jLabel6MouseClicked

    private void jLabel6MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel6MouseEntered
        jLabel6.setForeground(Color.LIGHT_GRAY);
}//GEN-LAST:event_jLabel6MouseEntered

    private void jLabel6MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel6MouseExited
        jLabel6.setForeground(Color.white);
}//GEN-LAST:event_jLabel6MouseExited

    private void jEmailAddressActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jEmailAddressActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jEmailAddressActionPerformed

    private void jEmailAddressKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jEmailAddressKeyPressed
        if (evt.getKeyChar() == KeyEvent.VK_ENTER) {
            jButtonRequestRegKeyActionPerformed(null);
        }
    }//GEN-LAST:event_jEmailAddressKeyPressed

    private void jPasswordKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jPasswordKeyPressed
        if (evt.getKeyChar() == KeyEvent.VK_ENTER){
            jButtonRequestRegKeyActionPerformed(null);
        }
}//GEN-LAST:event_jPasswordKeyPressed

    private void jPasswordCheckKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jPasswordCheckKeyPressed
        if (evt.getKeyChar() == KeyEvent.VK_ENTER){
            jButtonRequestRegKeyActionPerformed(null);
        }
}//GEN-LAST:event_jPasswordCheckKeyPressed

    private void jAGBCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jAGBCheckboxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jAGBCheckboxActionPerformed

    private void jEditorPane1HyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {//GEN-FIRST:event_jEditorPane1HyperlinkUpdate
        if (HyperlinkEvent.EventType.ACTIVATED.equals(evt.getEventType())) {  
            System.out.println(evt.getURL());  
            
            Util.openUrl(evt.getURL());
            
        }  
    }//GEN-LAST:event_jEditorPane1HyperlinkUpdate

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jAGBCheckbox;
    private javax.swing.JButton jButtonRequestRegKey;
    private javax.swing.JEditorPane jEditorPane1;
    private javax.swing.JTextField jEmailAddress;
    private javax.swing.JPanel jHeaderPanel;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabelEmailAddress;
    private javax.swing.JLabel jLabelEmailAddress1;
    private javax.swing.JLabel jLabelEmailAddress2;
    private javax.swing.JPanel jLoginPanel;
    private javax.swing.JCheckBox jNewsletterCheckbox;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPasswordField jPassword;
    private javax.swing.JPasswordField jPasswordCheck;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

    private void showRequestProgress() {
      if (this.progressDialog == null)
        this.progressDialog = new ProgressDialog(this, false, i18n.tr("Fordere Aktivierungsschlüssel an..."));
      
      this.progressDialog.setVisible(true);
    }

    private void hideProgress() {
        progressDialog.setVisible(false);
       
    }

    private void loadIcon() {
        ImageIcon icon = new ImageIcon(getClass().getResource("/de/shellfire/vpn/gui/resources/sfvpn2-idle.png"));
        Image img = icon.getImage();

        setIconImage(img);

    }

    private String getPassword() {
        return new String(jPassword.getPassword());
    }
    private String getPasswordCheck() {
        return new String(jPasswordCheck.getPassword());
    }    
    
    private boolean validateForm() {
        boolean error = false;
        String message = "";
        JTextField jumpTo = null;
        String email = jEmailAddress.getText();
        if (!GenericValidator.isEmail(email)) {
            error = true;
            message = i18n.tr("Bitte gib eine gültige Email-Adresse ein.");
            jumpTo = jEmailAddress;
        }

        String password = this.getPassword();
        String passwordCheck = this.getPasswordCheck();
        if (!error && GenericValidator.isBlankOrNull(password)) {
            error = true;
            message = i18n.tr("Bitte gib ein Passwort ein.");
            jumpTo = jPassword;
        }

        if (!error && password.length() < 5) {
            error = true;
            message = i18n.tr("Dein Passwort muss mindestens 5 Zeichen lang sein.");
            jumpTo = jPassword;
        }

        if (!error && GenericValidator.isBlankOrNull(passwordCheck)) {
            error = true;
            message = i18n.tr("Bitte gib dein Passwort zur Sicherheit noch einmal im Feld Passwort-Check ein.");
            jumpTo = jPasswordCheck;
        }

        if (!error && !password.equals(passwordCheck)) {
            error = true;
            message = i18n.tr("Passwort und Passwort-Check stimmen nicht überein.");
            jumpTo = jPasswordCheck;
        }

        if (!error && !jAGBCheckbox.isSelected()) {
            error = true;
            message = i18n.tr("Die AGB, Datenschutzerklärung und Widerrufstrecht müssen akzeptiert werden, um die Registrierung abzuschließen.");
            jumpTo = null;
        }
        
        
        if (error) {
            JOptionPane.showMessageDialog(null, message, i18n.tr("Fehler"), JOptionPane.ERROR_MESSAGE);
            if (jumpTo != null)
                SwingUtilities.invokeLater(new FocusRequester(jumpTo));
        }

        return !error;
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
          Response<LoginResponse> registrationResult = service.registerNewFreeAccount(jEmailAddress.getText(), jPassword.getText(), jNewsletterCheckbox.isSelected());
            
            if (registrationResult.isSuccess()) {
            	activationToken = registrationResult.getData().getToken();            	
            } else {
                if (progressDialog != null) {
                    progressDialog.setVisible(false);
                }

                JOptionPane.showMessageDialog(null, i18n.tr("Fehler bei Registrierung:") + " " + i18n.tr(registrationResult.getMessage()), i18n.tr("Fehler"), JOptionPane.ERROR_MESSAGE);
            }
            
            return null;
        }
    }

    private void waitForActivation() {
        this.progressDialog = new ProgressDialog(this, false, i18n.tr("Das Shellfire VPN System hat dir soeben eine Email geschickt, bitte folge den Anweisungen dort."));
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
                    JOptionPane.showMessageDialog(null, i18n.tr("Bitte wähle nun eine andere Email-Adresse und versuche es erneut."), i18n.tr("Email-Adresse ändern"), JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        this.progressDialog.setVisible(true);
        poller = new AccountActiveServicePollerTask();
        poller.execute();

    }

    private void activationSuccesful() {
        this.progressDialog.setVisible(false);
        bringToFront();
        JOptionPane.showMessageDialog(null, i18n.tr("Dein Shellfire Account wurde soeben erfolgreich aktiviert. Logge dich jetzt mit Email-Adresse und Passwort ein, um Shellfire VPN zu starten."), i18n.tr("Registrierung erfolgreich."), JOptionPane.INFORMATION_MESSAGE);
        parentFrame.setUsername(this.getUser());
        parentFrame.setPassword(this.getPassword());
        parentFrame.setAutoLogin(false);
        
        this.setVisible(false);
        parentFrame.setVisible(true);
    }

    private void bringToFront() {
        parentFrame.toFront();
        parentFrame.setAlwaysOnTop(true);
        parentFrame.setAlwaysOnTop(false);
        this.toFront();
        this.setAlwaysOnTop(true);
        this.setAlwaysOnTop(false);
    }

    /**
     * Regularly polls the web service if the requested account is active and only stops
     * once it actually is active
     */
    class AccountActiveServicePollerTask extends SwingWorker<Void, Void> {

        private boolean cont = true;

        @Override
        protected void done() {
            if (accountActive) {
                activationSuccesful();
            }

        }

        @Override
        protected Void doInBackground() throws Exception {
            while (cont && !(accountActive = service.accountActive())) {
                Thread.sleep(3000);
            }

            return null;
        }

        public void stopIt() {
            this.cont = false;
        }
    }
    
    private static class FocusRequester implements Runnable {
        private final JTextField jumpTo;

        public FocusRequester(JTextField jumpTo) {
            this.jumpTo = jumpTo;
        }

        @Override
        public void run() {
            jumpTo.requestFocus();
        }
    }
    
}