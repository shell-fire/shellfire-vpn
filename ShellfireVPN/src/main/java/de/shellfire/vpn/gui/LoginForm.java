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
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;

import de.shellfire.vpn.Storage;
import de.shellfire.vpn.Util;
import de.shellfire.vpn.VpnProperties;
import de.shellfire.vpn.client.Client;
import de.shellfire.vpn.client.Controller;
import de.shellfire.vpn.client.ServiceTools;
import de.shellfire.vpn.exception.VpnException;
import de.shellfire.vpn.gui.controller.ProgressDialogController;
import de.shellfire.vpn.gui.helper.MoveMouseListener;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.proxy.ProxyConfig;
import de.shellfire.vpn.service.CryptFactory;
import de.shellfire.vpn.types.Reason;
import de.shellfire.vpn.types.ServerType;
import de.shellfire.vpn.updater.Updater;
import de.shellfire.vpn.webservice.EndpointManager;
import de.shellfire.vpn.webservice.Response;
import de.shellfire.vpn.webservice.WebService;
import de.shellfire.vpn.webservice.model.LoginResponse;
import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;

/**
 * 
 * @author bettmenn
 */
public class LoginForm extends javax.swing.JFrame implements CanContinueAfterBackEndAvailable {

  private static Logger log = Util.getLogger(LoginForm.class.getCanonicalName());
  private static final long serialVersionUID = 1L;
	public static final String REG_PASS = "pass";
	public static final String REG_USER = "user";
	public static final String REG_AUTOLOGIN = "autologin";
	public static final String REG_AUTOCONNECT = "autoConnect";
	public static final String REG_INSTDIR = "instdir";
	public static final String REG_SHOWSTATUSURL = "show_status_url_on_connect";
	private static final String REG_FIRST_START = "firststart";
	private ShellfireVPNMainForm mainForm;
	private static Preferences preferences;
	WebService service;
	private ProgressDialog loginProgressDialog;
	private LoginTask task;
	private String username;
	private String password;
	private boolean passWordBogus;
	private LoginForm currentForm = this;
	private boolean minimize;
	private static LoginForm instance;
	private static I18n i18n = VpnI18N.getI18n();
	public static ProgressDialog initDialog;
	
        private boolean licenseAccepted;
    
	/** Creates new form LoginForm */
	private LoginForm() {
		this.setUndecorated(true);
		initComponents();

		MoveMouseListener mml = new MoveMouseListener(this);
		this.addMouseListener(mml);
		this.addMouseMotionListener(mml);
		this.loadIcon();
		
		
		
		jLoginPanel.setPreferredSize(new Dimension((int) jLoginPanel.getPreferredSize().getWidth(), 200));
		jLoginPanel.setLayout(new MigLayout("", "[][][grow]", "[][][][][][][][][][]"));
		jLoginPanel.add(jLabel16, "flowx,cell 1 0 2 1,growx");
		jLoginPanel.add(jLabel15, "cell 1 2,alignx right,aligny center");
		jLoginPanel.add(jLabel7, "cell 1 1,grow");
		jLoginPanel.add(jLostUsernamePasswordButton, "cell 2 9,growx,aligny top");
		jLoginPanel.add(jStoreLoginData, "cell 2 3,growx,aligny top");
		jLoginPanel.add(jPassword, "cell 2 2,growx,aligny top");
		jLoginPanel.add(jUsername, "cell 2 1,growx,aligny top");
		jLoginPanel.add(jAutoLogin, "cell 2 4,growx,aligny top");
		jLoginPanel.add(jAutoStart, "cell 2 5,alignx left,aligny top");
		jLoginPanel.add(jAutoConnect, "cell 2 6,growx,aligny top");
		jLoginPanel.add(jButtonLogin, "cell 2 7,growx,aligny top");
		jLoginPanel.add(jOpenRegisterFormButton, "cell 2 8,growx,aligny top");
		//this.setSize(new Dimension((int) getSize().getWidth(), 1500));
		this.pack();
		this.setLocationRelativeTo(null);

	}
	
	private void init() {
		// before doing anything else, we should test for an internet connection. without internet, we cant do anything!
	  
		boolean internetAvailable = Util.internetIsAvailable();
		
		if (internetAvailable) {
		  initDialog.setText(i18n.tr("Initialisiere ShellfireVPNService..."));
	    ServiceTools.getInstanceForOS().ensureServiceEnvironment(this);
		} else {
			JOptionPane.showMessageDialog(this, i18n.tr("Keine Internet-Verbindung verfügbar - ShellfireVPN wird beendet."), i18n.tr("Kein Internet"), JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}

	public void afterShellfireServiceEnvironmentEnsured() {
	  log.debug("Ensured that ShellfireVPNService is running. Trying to connect to the Shellfire webservice backend...");

	  EndpointManager.getInstance().ensureShellfireBackendAvailable(this);
	}
	
	 
  public void continueAfterBackEndAvailabled() {
    this.service = WebService.getInstance();
    Storage.register(service);
    this.restoreCredentialsFromRegistry();
    this.restoreAutoConnectFromRegistry();
    this.restoreAutoStartFromRegistry();
    this.licenseAccepted = false;
    
    if (initDialog != null) {
      initDialog.dispose();
      instance.setEnabled(true);
    }
    try {
      //Connection.initRmi();
    } catch (Exception e) {
      Util.handleException(e);
    } 
    
    if (!this.autoLoginIfActive()) {
      this.setVisible(true);
      askForNewAccountAndAutoStartIfFirstStart();
    }   
  }


	private void askForNewAccountAndAutoStartIfFirstStart() {
		if (firstStart()) {
		  if (!Util.isWindows()) {
		    askForLicense();
		    
	      if (!this.licenseAccepted) {
	          JOptionPane.showMessageDialog(null, i18n.tr("Lizenz nicht akzeptiert - Shellfire VPN wird jetzt beendet."));
	          System.exit(0);
	        }
	  	  }
	  	  askForAutoStart();
	  	  askForNewAccount();
		}

		setFirstStart(false);
	}

	private void setFirstStart(boolean b) {
		VpnProperties props = VpnProperties.getInstance();
		props.setBoolean(LoginForm.REG_FIRST_START, b);
	}

	private void askForNewAccount() {
		int answer = JOptionPane
				.showConfirmDialog(
						null,
						i18n.tr("Dies ist dein erster Start von Shellfire VPN. Neuen Shellfire VPN Account anlegen?"),
						i18n.tr("Willkommen: Erster Start"),
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

		if (answer == JOptionPane.YES_OPTION) {
			jOpenRegisterFormButtonActionPerformed(null);
		}
	}

	private void askForAutoStart() {
		int answer = JOptionPane
				.showConfirmDialog(
						null,
						i18n.tr("Shellfire VPN beim Hochfahren starten und automatisch verbinden?"),
						i18n.tr("Autostart"), JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE);

		if (answer == JOptionPane.YES_OPTION) {
			Client.addVpnToAutoStart();
			jAutoStart.setSelected(true);
			setAutoLogin(true);
			jAutoConnect.setSelected(true);
			jStoreLoginData.setSelected(true);
		}
	}

	private boolean firstStart() {
		VpnProperties props = VpnProperties.getInstance();
		boolean firstStart = props.getBoolean(LoginForm.REG_FIRST_START, true);
		String autoLogin = props.getProperty(LoginForm.REG_AUTOLOGIN, null);

		return firstStart && autoLogin == null;
	}

	private LoginForm(boolean minimize) {
		this();
		this.minimize = minimize;
	}

	public static LoginForm getInstance() {
		if (LoginForm.instance == null) {
			LoginForm.instance = new LoginForm();
		}

		return LoginForm.instance;
	}

	protected static LoginForm getInstance(boolean minimize) {
		if (LoginForm.instance == null) {
			LoginForm.instance = new LoginForm(minimize);
		}

		return LoginForm.instance;
	}

	public static void restart() {
		if (Util.isWindows()) {
			
			if (LoginForm.instance != null) {

				if (instance.mainForm != null) {
					Controller c = instance.mainForm.getController();
					if (c != null) {
					    c.disconnect(Reason.GuiRestarting);  
						
					}

					instance.mainForm.dispose();
					instance.mainForm = null;
				}

				LoginForm.instance.dispose();
				LoginForm.instance = null;
				
				
	      List<String> restart = new ArrayList<String>();
	      restart.add("ShellfireVPN2.exe");
	      Process p;
	      try {
	        p = new ProcessBuilder(restart).directory(new File(LoginForm.getInstDir())).start();
	        Util.digestProcess(p);
	        
	        System.exit(0);
	      } catch (IOException e) {
	        Util.handleException(e);
	      } 
				
				
			}		
		} else {
			List<String> restart = new ArrayList<String>();
			restart.add("/usr/bin/open");
			restart.add("-n");
			restart.add(com.apple.eio.FileManager.getPathToApplicationBundle());
			Process p;
			try {
				p = new ProcessBuilder(restart).directory(new File(com.apple.eio.FileManager.getPathToApplicationBundle())).start();
				Util.digestProcess(p);
				
				System.exit(0);
			} catch (IOException e) {
				Util.handleException(e);
			} 
			
			
		}
		

	}

	private void restoreAutoConnectFromRegistry() {
		VpnProperties props = VpnProperties.getInstance();
		boolean autoConnect = props.getBoolean(REG_AUTOCONNECT, false);
		this.jAutoConnect.setSelected(autoConnect);

	}

	public static String getInstDir() {
		VpnProperties props = VpnProperties.getInstance();
		String instDir = props.getProperty(REG_INSTDIR, null);

		if (instDir == null) {
			if (Util.isWindows()) {
				instDir = new File("").getAbsolutePath();
			} else {
				instDir = WebService.macOsAppDirectory() + "/ShellfireVPN";
			}
		}

		return instDir;
	}

	private void restoreAutoStartFromRegistry() {
		boolean autoStart = Client.vpnAutoStartEnabled();
		this.jAutoStart.setSelected(autoStart);
	}


	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	// <editor-fold defaultstate="collapsed"
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

      
        jPanel7 = new javax.swing.JPanel();
        jLoginPanel = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        jButtonLogin = new javax.swing.JButton();
        jUsername = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jPassword = new javax.swing.JPasswordField();
        jAutoLogin = new javax.swing.JCheckBox();
        jOpenRegisterFormButton = new javax.swing.JButton();
        jLostUsernamePasswordButton = new javax.swing.JButton();
        jStoreLoginData = new javax.swing.JCheckBox();
        jAutoStart = new javax.swing.JCheckBox();
        jAutoConnect = new javax.swing.JCheckBox();
        jHeaderPanel = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Shellfire VPN 2 Login"); // NOI18N
        setResizable(false);

        jPanel7.setBackground(new java.awt.Color(64, 69, 73));
        jPanel7.setName("jPanel7");

        jLoginPanel.setBackground(new java.awt.Color(244, 244, 244));
        jLoginPanel.setName("jLoginPanel"); // NOI18N

        jLabel16.setFont(new java.awt.Font("Arial", 1, Util.getFontSize()*2)); // NOI18N
        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel16.setText("Login");
        jLabel16.setName("jLabel16"); // NOI18N

        jButtonLogin.setText(i18n.tr("Login"));
        jButtonLogin.setName("jButtonLogin"); // NOI18N
        jButtonLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLoginActionPerformed(evt);
            }
        });

        jUsername.setName("jUsername"); // NOI18N
        jUsername.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jUsernameFocusLost(evt);
            }
        });
        jUsername.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jUsernameKeyPressed(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Arial", 0, Util.getFontSize())); // NOI18N
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel7.setText(i18n.tr("Email / Benutzername:"));
        jLabel7.setName("jLabel7"); // NOI18N

        jLabel15.setFont(new java.awt.Font("Arial", 0, Util.getFontSize())); // NOI18N
        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel15.setText(i18n.tr("Passwort:"));
        jLabel15.setName("jLabel15"); // NOI18N

        jPassword.setName("jPassword"); // NOI18N
        jPassword.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jPasswordFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jPasswordFocusLost(evt);
            }
        });
        jPassword.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jPasswordKeyPressed(evt);
            }
        });

        jAutoLogin.setText(i18n.tr("Automatisch einloggen"));
        jAutoLogin.setName("jAutoLogin"); // NOI18N
        jAutoLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jAutoLoginActionPerformed(evt);
            }
        });

        jOpenRegisterFormButton.setText(i18n.tr("Keine Zugangsdaten?"));
        jOpenRegisterFormButton.setName("jOpenRegisterFormButton"); // NOI18N
        jOpenRegisterFormButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jOpenRegisterFormButtonActionPerformed(evt);
            }
        });

        jLostUsernamePasswordButton.setText(i18n.tr("Zugangsdaten verloren?"));
        jLostUsernamePasswordButton.setActionCommand(i18n.tr("Zugangsdaten verloren?"));
        jLostUsernamePasswordButton.setName("jLostUsernamePasswordButton"); // NOI18N
        jLostUsernamePasswordButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jLostUsernamePasswordButtonActionPerformed(evt);
            }
        });

        jStoreLoginData.setText(i18n.tr("Logindaten speichern"));
        jStoreLoginData.setName("jStoreLoginData"); // NOI18N
        jStoreLoginData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jStoreLoginDataActionPerformed(evt);
            }
        });

        jAutoStart.setSelected(true);
        jAutoStart.setText(i18n.tr("Beim Hochfahren starten"));
        jAutoStart.setName("jAutoStart"); // NOI18N
        jAutoStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jAutoStartActionPerformed(evt);
            }
        });

        jAutoConnect.setSelected(true);
        jAutoConnect.setText(i18n.tr("Automatisch verbinden"));
        jAutoConnect.setName("jAutoConnect"); // NOI18N
        jAutoConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jAutoConnectActionPerformed(evt);
            }
        });
        getContentPane().setLayout(new MigLayout("insets 0, gapy 0", "[]", "[][grow]"));
        jPanel7.setLayout(new MigLayout("", "[grow]", "[]"));

        jPanel7.add(jLoginPanel, "cell 0 0,grow");

        getContentPane().add(jPanel7, "cell 0 1,grow");

        jHeaderPanel.setBackground(new java.awt.Color(18, 172, 229));
        jHeaderPanel.setName("jHeaderPanel");
        jHeaderPanel.setLayout(new MigLayout("insets 0", "[grow][]", "[]"));

        jLabel5.setIcon(ShellfireVPNMainForm.getLogo());
        jLabel5.setAlignmentY(0.0F);
        jLabel5.setName("jLabel5"); // NOI18N
        jHeaderPanel.add(jLabel5, "cell 0 0,aligny top");

        jPanel6.setBackground(new java.awt.Color(255, 255, 255));
        jPanel6.setName("jPanel6");

        jLabel6.setFont(new java.awt.Font("Arial", 0, Util.getFontSize())); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setIcon(Util.getImageIcon("/icons/exit.png")); // NOI18N
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
        jPanel6.setLayout(new MigLayout("", "", ""));
        jPanel6.add(jLabel6, "cell 0 0,grow");

        jHeaderPanel.add(jPanel6, "cell 1 0,alignx right,aligny top");

        getContentPane().add(jHeaderPanel, "cell 0 0,growx,aligny top");

        pack();
    }// </editor-fold>//GEN-END:initComponents

	private void jAutoStartActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jAutoStartActionPerformed
		// TODO add your handling code here:
	}// GEN-LAST:event_jAutoStartActionPerformed

	private void jAutoConnectActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jAutoConnectActionPerformed
		// TODO add your handling code here:
	}// GEN-LAST:event_jAutoConnectActionPerformed

	private String getUser() {
		return this.username;
	}

	private String getPassword() {
		return this.password;
	}

	private void jButtonLoginActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButtonLoginActionPerformed
	  jButtonLogin.setEnabled(false);
		this.showLoginProgress();

		task = new LoginTask();
		task.execute();
	}// GEN-LAST:event_jButtonLoginActionPerformed

	private void jLabel6MouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_jLabel6MouseClicked
		System.exit(0);
	}// GEN-LAST:event_jLabel6MouseClicked

	private void jLabel6MouseEntered(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_jLabel6MouseEntered
		jLabel6.setForeground(Color.LIGHT_GRAY);
	}// GEN-LAST:event_jLabel6MouseEntered

	private void jLabel6MouseExited(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_jLabel6MouseExited
		jLabel6.setForeground(Color.white);
	}// GEN-LAST:event_jLabel6MouseExited

	private void jOpenRegisterFormButtonActionPerformed(
			java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jOpenRegisterFormButtonActionPerformed
		this.setVisible(false);
		RegisterForm regForm = new RegisterForm(this);
		regForm.setVisible(true);
	}// GEN-LAST:event_jOpenRegisterFormButtonActionPerformed

	private void jPasswordKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_jPasswordKeyPressed
		if (evt.getKeyChar() == KeyEvent.VK_ENTER) {
			jPasswordFocusLost(null);
			jButtonLoginActionPerformed(null);
		}
	}// GEN-LAST:event_jPasswordKeyPressed

	private void jPasswordFocusGained(java.awt.event.FocusEvent evt) {// GEN-FIRST:event_jPasswordFocusGained
		if (this.passWordBogus)
			this.jPassword.setText("");
	}// GEN-LAST:event_jPasswordFocusGained

	private void jPasswordFocusLost(java.awt.event.FocusEvent evt) {// GEN-FIRST:event_jPasswordFocusLost
		this.password = new String(jPassword.getPassword());
		this.passWordBogus = false;
	}// GEN-LAST:event_jPasswordFocusLost

	private void jUsernameFocusLost(java.awt.event.FocusEvent evt) {// GEN-FIRST:event_jUsernameFocusLost
		this.username = jUsername.getText();
	}// GEN-LAST:event_jUsernameFocusLost

	private void jUsernameKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_jUsernameKeyPressed
		jUsernameFocusLost(null);
	}// GEN-LAST:event_jUsernameKeyPressed

	private void jAutoLoginActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jAutoLoginActionPerformed
		if (jAutoLogin.isSelected())
			this.jStoreLoginData.setSelected(true);

	}// GEN-LAST:event_jAutoLoginActionPerformed

	private void jStoreLoginDataActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jStoreLoginDataActionPerformed
		if (!this.jStoreLoginData.isSelected())
			this.jAutoLogin.setSelected(false);
	}// GEN-LAST:event_jStoreLoginDataActionPerformed

	private void jLostUsernamePasswordButtonActionPerformed(
			java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jLostUsernamePasswordButtonActionPerformed
		Util.openUrl(service.getUrlPasswordLost());
	}// GEN-LAST:event_jLostUsernamePasswordButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jAutoConnect;
    private javax.swing.JCheckBox jAutoLogin;
    private javax.swing.JCheckBox jAutoStart;
    private javax.swing.JButton jButtonLogin;
    private javax.swing.JPanel jHeaderPanel;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jLoginPanel;
    private javax.swing.JButton jLostUsernamePasswordButton;
    private javax.swing.JButton jOpenRegisterFormButton;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPasswordField jPassword;
    private javax.swing.JCheckBox jStoreLoginData;
    private javax.swing.JTextField jUsername;
    // End of variables declaration//GEN-END:variables

	private void removeCredentialsFromRegistry() {
		VpnProperties props = VpnProperties.getInstance();
		props.remove(REG_USER);
		props.remove(REG_PASS);
		props.remove(REG_AUTOLOGIN);
	}

	private void storeCredentialsInRegistry(String user, String password) {
		VpnProperties props = VpnProperties.getInstance();
		props.setProperty(REG_USER, CryptFactory.encrypt(user));
		props.setProperty(REG_PASS, CryptFactory.encrypt(password));
		props.setBoolean(REG_AUTOLOGIN, jAutoLogin.isSelected());

	}

	private void restoreCredentialsFromRegistry() {
		VpnProperties props = VpnProperties.getInstance();
		String user = props.getProperty(REG_USER, null);
		String pass = props.getProperty(REG_PASS, null);

		if (user != null && pass != null) {
			user = CryptFactory.decrypt(user);
			pass = CryptFactory.decrypt(pass);

			if (user != null && pass != null) { // decryption worked
				this.setUsername(user);
				this.setPassword(pass);
				this.jStoreLoginData.setSelected(true);
			} else {
				this.removeCredentialsFromRegistry();
			}

		}
	}

	protected void setUsername(String username) {
		this.username = username;
		this.jUsername.setText(username);
	}

	protected void setPassword(String password) {
		this.password = password;
		this.setPasswordBogus();
	}

  private static void setLookAndFeel() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      Util.setDefaultSize(Util.getFontSize());

    } catch (Exception ex) {
    }
  }

	public static void main(String args[]) {
            // temporarily setting java path for dll libraries
            System.setProperty("java.library.path", "C:\\Users\\Tcheutchoua\\Documents\\NetBeansProjects\\ShellFire\\shellfire-vpn\\ShellfireVPN\\lib");

    final boolean minimize;
		if (args.length > 0) {
			String cmd = args[0];

			minimize = cmd.equals("minimize");
		} else {
			minimize = false;
		}

		ProxyConfig.perform();
		setLookAndFeel();

		initDialog = new ProgressDialog(instance, true, "Init...");
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				initDialog.setVisible(true);
			}
		});
		
		if (args.length > 0) {
			
	        String cmd = args[0];

	        if (cmd.equals("uninstallservice")) {
	          ServiceTools.getInstanceForOS().uninstall();
	          initDialog.dispose();
	          return;
	        } else if (cmd.equals("installservice")) {
	        	initDialog.dispose();
	        	String path = "";
	
	        	if (args.length > 1) {
	        		for (int i = 1; i < args.length; i++) {
	        			path += args[i];
	        			
	        			if (i+1 < args.length) {
	        				path += " ";
	        			}
	        		}
	        	}
	        	
	        	log.debug("Retrieved installation path from args parameter: " + path);
	        	
	        	if (cmd.equals("installservice"))
	        		ServiceTools.getInstanceForOS().install(path); 
	        	
	        	System.exit(0);
	          return;
	        }  else if (cmd.equals("doupdate")) {
	        	
	        	String path = "";
	        	String user = "";
	        	if (args.length > 2) {
	        		user = args[1];
	        		
	        		for (int i = 2; i < args.length; i++) {
	        			path += args[i];
	        			
	        			if (i+1 < args.length) {
	        				path += " ";
	        			}
	        		}
	        	}
	        	
	        	log.debug("Retrieved installation path from args parameter: " + path);
	        	initDialog.dispose();
        		new Updater().performUpdate(path, user);
        		
        		return;
	        } 
	      }
		
		
		
		instance = getInstance(minimize);
		  
		instance.setEnabled(false);
		
		boolean internetAvailable = Util.internetIsAvailable();
		if (internetAvailable) {
	    Updater updater = new Updater();
	    if (updater.newVersionAvailable()) {
	    
	      int answer = JOptionPane
	          .showConfirmDialog(
	              null,
	              i18n.tr("Es ist eine neue Version von Shellfire VPN verfügbar. Ein Update ist zwingend erforderlich. Möchtest du jetzt updaten?"),
	              i18n.tr("Neue Version"),
	              JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

	      if (answer == JOptionPane.YES_OPTION) {
	        JOptionPane.showMessageDialog(
	            null,
	            i18n.tr("Du hast dich entschieden, zu updaten. Shellfire VPN wird jetzt mit Admin-Privilegien neugestartet um das Update durchzuführen."),
	            i18n.tr("Update wird durchgeführt"), JOptionPane.INFORMATION_MESSAGE);
	        
	        
	        String installerPath = com.apple.eio.FileManager.getPathToApplicationBundle() + "/Contents/Java/ShellfireVPN2-Updater.app";
	        log.debug("Opening updater using Desktop.open(): " +  installerPath);

	        List<String> cmds = new LinkedList<String>();
	        cmds.add("/usr/bin/open");
	        cmds.add(installerPath);
	        Process p;
	        try {
	          p = new ProcessBuilder(cmds).directory(new File(com.apple.eio.FileManager.getPathToApplicationBundle() + "/Contents/Java/")).start();
	          Util.digestProcess(p);
	        } catch (IOException e) {
	          Util.handleException(e);
	        }
	        System.exit(0);
	        
	              
	      } else {
	        JOptionPane.showMessageDialog(
	            null,
	            i18n.tr("Du hast dich entschieden, nicht zu updaten. Shellfire VPN wird jetzt beendet."),
	            i18n.tr("Update abgelehnt"), JOptionPane.ERROR_MESSAGE);
	        System.exit(0);
	      }
	      
	      return;
	    }
		} else {
		  log.debug("No internet available, skipping update check");
		}

    instance.init();
	}

	private void showLoginProgress() {
		this.loginProgressDialog = new ProgressDialog(this, false, i18n.tr("Einloggen..."));
		this.loginProgressDialog.setVisible(true);
	}

	private void hideLoginProgress() {
		this.loginProgressDialog.setVisible(false);
	}

	private void loadIcon() {
		ImageIcon icon = new ImageIcon(getClass().getResource("/icons/sfvpn2-idle.png"));
		Image img = icon.getImage();

		mySetIconImage(img);
	}

	public void mySetIconImage(Image img) {
		setIconImage(img);

		if (!Util.isWindows()) {
			// Application app = Application.getApplication();
			// app.setDockIconImage(img);
		}

	}

	void setPasswordBogus() {
		this.jPassword.setText("boguspass");
		this.passWordBogus = true;
	}

	void setAutoLogin(boolean autologinEnabled) {
		this.jAutoLogin.setSelected(autologinEnabled);
	}

	private boolean autoLoginIfActive() {
		VpnProperties props = VpnProperties.getInstance();
		boolean doAutoLogin = props.getBoolean(REG_AUTOLOGIN, false);

		if (doAutoLogin) {
			this.jAutoLogin.setSelected(true);
			this.setVisible(false);
			jButtonLoginActionPerformed(null);
		}

		return doAutoLogin;

	}

    void licenseAccepted() {
        this.licenseAccepted = true;
    }

    void licenseNotAccepted() {
    	this.licenseAccepted = false;
    }

    private void askForLicense() {
        new LicenseAcceptScreen(this, true, null).setVisible(true);
    }

	class LoginTask extends SwingWorker<Response<LoginResponse>, Object> {
		/*
		 * Main task. Executed in background thread.
		 */

		/*
		 * Executed in event dispatch thread
		 */
		public void done() {
		  Response<LoginResponse> loginResult = null;
			try {
				loginResult = get();
			} catch (Exception ignore) {
				ignore.printStackTrace();
			}
			hideLoginProgress();
			String user = getUser();
			String password = getPassword();
			if (loginResult != null) {
				if (service.isLoggedIn()) {

					if (jStoreLoginData.isSelected()) {
						storeCredentialsInRegistry(user, password);
					} else {
						removeCredentialsFromRegistry();
					}
            if (jAutoStart.isSelected()) {
              Client.addVpnToAutoStart();
            } else {
              Client.removeVpnFromAutoStart();
            }
					if (jAutoConnect.isSelected()) {
						setAutoConnectInRegistry(true);
					} else {
						setAutoConnectInRegistry(false);
					}

					VpnSelectDialog dia = new VpnSelectDialog(currentForm, service, jAutoConnect.isSelected());
					int rememberedVpnSelection = dia.rememberedVpnSelection();

					boolean selectionRequired = service.vpnSelectionRequired();

					if (selectionRequired && rememberedVpnSelection == 0) {

						setVisible(false);
						dia.setVisible(true);

					} else {
						try {
							if (selectionRequired
									&& rememberedVpnSelection != 0) {
								if (!service.selectVpn(rememberedVpnSelection)) {
									// remembered vpn id is invalid
									dispose();
									dia.setVisible(true);
								}
							}

							if (!dia.isVisible()) {
								//setVisible(false);
								dispose();
								mainForm = new ShellfireVPNMainForm(service);
								boolean vis = true;
								if (minimize
										&& service.getVpn().getAccountType() != ServerType.Free) {
									vis = false;
								}

								mainForm.setVisible(vis);
								mainForm.afterLogin(jAutoConnect.isSelected());
							}
						} catch (VpnException ex) {
							Util.handleException(ex);
						}

					}

				} else {
					JOptionPane.showMessageDialog(
							null,
							i18n.tr("Login fehlgeschlagen: ")
									+ loginResult.getMessage(),
							i18n.tr("Fehler"), JOptionPane.ERROR_MESSAGE);
					setVisible(true);
				}
			}
			jButtonLogin.setEnabled(true);

		}

		private void setAutoConnectInRegistry(boolean autoConnect) {
			VpnProperties props = VpnProperties.getInstance();
			props.setBoolean(REG_AUTOCONNECT, autoConnect);

		}

		@Override
		protected Response<LoginResponse> doInBackground() throws Exception {
			String user = getUser();
			String password = getPassword();
			log.debug("service.login() - start()");
			Response<LoginResponse> loginResult = service.login(user, password);
			log.debug("service.login() - finished()");
			return loginResult;
		}
	}

  @Override
  public ProgressDialog getDialog() {
    return initDialog;
  }
}
