/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * GizmpVPNMainForm.java
 *
 * Created on 31.05.2011, 19:15:50
 */
package de.shellfire.vpn.gui;

import java.awt.AWTEvent;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

import org.jdesktop.application.Action;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.LocaleChangeEvent;
import org.xnap.commons.i18n.LocaleChangeListener;

import de.shellfire.vpn.Connection;
import de.shellfire.vpn.ConnectionState;
import de.shellfire.vpn.Controller;
import de.shellfire.vpn.OpenSansFont;
import de.shellfire.vpn.OxygenFont;
import de.shellfire.vpn.VpnProtocol;
import de.shellfire.vpn.Reason;
import de.shellfire.vpn.Server;
import de.shellfire.vpn.ServerList;
import de.shellfire.vpn.ServerType;
import de.shellfire.vpn.ShellfireService;
import de.shellfire.vpn.Storage;
import de.shellfire.vpn.TitiliumFont;
import de.shellfire.vpn.Vpn;
import de.shellfire.vpn.proxy.ProxyConfig;
import de.shellfire.vpn.rmi.Console;
import de.shellfire.vpn.rmi.OpenVpnProcessHost;
import de.shellfire.vpn.rmi.ServiceTools;
import de.shellfire.www.webservice.sf_soap_php.TrayMessage;
import de.shellfire.www.webservice.sf_soap_php.WsGeoPosition;

/**
 * 
 * @author bettmenn
 */
public class ShellfireVPNMainForm extends javax.swing.JFrame implements LocaleChangeListener, ConnectionStateListener {
  private static Logger log = Util.getLogger(ShellfireVPNMainForm.class);
	private ContentPaneList content;
	private ShellfireService shellfireService;
	private ServerList serverList;
	private ServerListTableModel serverListTableModel;
	private Controller controller;
	private TrayIcon trayIcon;
	private Image iconDisconnected;
	private Image iconConnected;
	private Image iconConnecting;
	private Image iconIdle;
	private Image iconEcncryptionInactive;
	private Image iconEcncryptionActive;
	private Date connectedSince;
	private Timer currentConnectedSinceTimer;
	private Preferences preferences;
	private String REG_SHOWONMAP = "show_own_position_on_map";

	private WsGeoPosition ownPosition;
	private MapController mapController;
	private static I18n i18n = VpnI18N.getI18n();
	private int nagScreenDelay;
	private PremiumVPNNagScreen nagScreen;
	private Timer nagScreentimer;
	private MoveMouseListener mml;
	private StringBuffer typedStrings = new StringBuffer();
	private ProgressDialog connectProgressDialog;
	private Image image;
	
	
	private final static HashMap<String, ImageIcon> mainIconMap = new HashMap<String, ImageIcon>() {
		{
			put("de", new javax.swing.ImageIcon(getClass().getResource("/de/shellfire/vpn/gui/resources/sf.png")));
			put("en", new javax.swing.ImageIcon(getClass().getResource("/de/shellfire/vpn/gui/resources/sf_en.png")));
			put("fr", new javax.swing.ImageIcon(getClass().getResource("/de/shellfire/vpn/gui/resources/sf_fr.png")));
		}
	};

	private MenuItem popupConnectItem;
	private PopupMenu popup;

	/** Creates new form GizmpVPNMainForm */
	ShellfireVPNMainForm(ShellfireService service) throws VpnException {
		if (!service.isLoggedIn()) {
			throw new VpnException("ShellfireVPN Main Form required a logged in service. This should not happen!");
		}

		this.getConsole();
		
		vpnConsole.append("ShellfireVPNMainForm starting up");
		if (Util.isWindows()) {
		  vpnConsole.append("Running on Windows " + Util.getOsVersion());
		  
		  if (Util.isVistaOrLater()) {
		    vpnConsole.append("Running on Vista Or Later Version");
		  } else {
		    vpnConsole.append("Running on XP");
		  }
		  
		} else {
		  vpnConsole.append("Running on Mac OS X " + Util.getOsVersion());
		}
		
		vpnConsole.append("System Architecture: " + Util.getArchitecture());
		
		this.shellfireService = service;
		this.initController();

		this.setUndecorated(true);
		this.enableMouseMoveListener();

		CustomLayout.register();
		this.setFont(TitiliumFont.getFont());
		this.loadIcons();
		this.setLookAndFeel();

		initComponents();
		this.initTray();

		this.initLayeredPaneSize();

		this.setLocationRelativeTo(null);
		
		this.initContent();
		Storage.register(this);

		this.initShortCuts();
		this.initPremium();
		this.initConnection();
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setVisible(true);

	}

  private void initConnection() {
		new Thread() {
			public void run() {
				try {
          controller.getCurrentConnectionState();
        } catch (RemoteException e) {
          Util.handleException(e);
        }
			}
		}.start();
	}

	public void simulateConnected() {
		Connection c;
		try {
			c = new Connection(this.controller, Reason.ConnectButtonPressed);
			c.setVpn(this.shellfireService.getVpn());
			this.controller.setConnection(c);
			c.pushConnectionState(ConnectionState.Connected, Reason.ConnectButtonPressed);

		} catch (Exception e) {
			Util.handleException(e);
		}
	}

	public void simulateConnecting() {
		Connection c;
		try {
			c = new Connection(this.controller, Reason.ConnectButtonPressed);
			this.controller.setConnection(c);
			c.pushConnectionState(ConnectionState.Connecting, Reason.ConnectButtonPressed);

		} catch (Exception e) {
			Util.handleException(e);
		}

	}

	public void simulateDisconnected() throws RemoteException {
		this.controller.setConnection(null);
		this.controller.connectionStateChanged();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	// <editor-fold defaultstate="collapsed"
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jNetworkTransportType = new javax.swing.ButtonGroup();
        jScrollBar1 = new javax.swing.JScrollBar();
        jPanel5 = new javax.swing.JPanel();
        jContentPanel = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jConnectionStateIcon = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabelConnectionState = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabelOnlineHost = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jConnectedSince = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabelVpnId = new javax.swing.JLabel();
        jLabelValidUntilDesc = new javax.swing.JLabel();
        jLabelVpnTyp = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabelVpnValidUntil = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLayeredPane1 = new javax.swing.JLayeredPane();
        jServerListPanel = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jScrollPane = new javax.swing.JScrollPane();
        jServerListTable = new javax.swing.JTable();
        jRadioUdp = new javax.swing.JRadioButton();
        jRadioTcp = new javax.swing.JRadioButton();
        jLabel12 = new javax.swing.JLabel();
        jUpgradeButtonLabel1 = new javax.swing.JLabel();
        jConnectButtonLabel1 = new javax.swing.JLabel();
        jPremiumButtonLabel1 = new javax.swing.JLabel();
        jConnectPanel = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jUpgradeButtonLabel = new javax.swing.JLabel();
        jConnectButtonLabel = new javax.swing.JLabel();
        jPremiumButtonLabel = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jConnectionStateImage = new javax.swing.JLabel();
        jMapPanel = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jXMapKit1 = new org.jdesktop.swingx.JXMapKit();
        jShowOwnPosition = new javax.swing.JCheckBox();
        jGotoOwnLocation = new javax.swing.JButton();
        jUsaPanel = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        jMenuPanel = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jPanelConnect = new javax.swing.JPanel();
        jLabelConnectHeader = new javax.swing.JLabel();
        jLabelConnectFooter = new javax.swing.JLabel();
        jButtonConnect = new javax.swing.JLabel();
        jPanelServerList = new javax.swing.JPanel();
        jLabelServerListHeader = new javax.swing.JLabel();
        jLabelServerListFooter = new javax.swing.JLabel();
        jButtonServerList = new javax.swing.JLabel();
        jPanelMap = new javax.swing.JPanel();
        jLabelMapHeader = new javax.swing.JLabel();
        jLabelMapFooter = new javax.swing.JLabel();
        jButtonMap = new javax.swing.JLabel();
        jPanelUsa = new javax.swing.JPanel();
        jLabelUsaHeader = new javax.swing.JLabel();
        jLabelUsaFooter = new javax.swing.JLabel();
        jButtonUsa = new javax.swing.JLabel();
        jHeaderPanel = new javax.swing.JPanel();
        jLabelHelp = new javax.swing.JLabel();
        jLabelExit = new javax.swing.JLabel();
        jLabelHide = new javax.swing.JLabel();
        jLabelSettings = new javax.swing.JLabel();
        jLabelMinimize = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();

        jScrollBar1.setName("jScrollBar1"); // NOI18N

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("de/shellfire/vpn/gui/resources/ShellfireVPNMainForm"); // NOI18N
        setTitle(bundle.getString("title")); // NOI18N
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance().getContext().getResourceMap(ShellfireVPNMainForm.class);
        setBackground(resourceMap.getColor("Form.background")); // NOI18N
        setName("Form"); // NOI18N

        jPanel5.setBackground(resourceMap.getColor("jPanel5.background")); // NOI18N
        jPanel5.setBorder(new javax.swing.border.LineBorder(resourceMap.getColor("jPanel5.border.lineColor"), 3, true)); // NOI18N
        jPanel5.setName("jPanel5"); // NOI18N
        jPanel5.setPreferredSize(new java.awt.Dimension(0, 0));

        jContentPanel.setBackground(resourceMap.getColor("jContentPanel.background")); // NOI18N
        jContentPanel.setName("jContentPanel"); // NOI18N

        jPanel2.setBackground(resourceMap.getColor("jPanel2.background")); // NOI18N
        jPanel2.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED, null, resourceMap.getColor("jPanel2.border.highlightInnerColor"), null, null)); // NOI18N
        jPanel2.setMinimumSize(new java.awt.Dimension(660, 101));
        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.setPreferredSize(new java.awt.Dimension(660, 101));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jConnectionStateIcon.setIcon(resourceMap.getIcon("jConnectionStateIcon.icon")); // NOI18N
        jConnectionStateIcon.setText(resourceMap.getString("jConnectionStateIcon.text")); // NOI18N
        jConnectionStateIcon.setName("jConnectionStateIcon"); // NOI18N
        jPanel2.add(jConnectionStateIcon, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, 60));

        jLabel2.setFont(resourceMap.getFont("jLabel2.font")); // NOI18N
        jLabel2.setForeground(resourceMap.getColor("jLabel2.foreground")); // NOI18N
        jLabel2.setText(i18n.tr("Verbindungsstatus"));
        jLabel2.setName("jLabel2"); // NOI18N
        jPanel2.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 10, 210, 30));

        jLabelConnectionState.setFont(resourceMap.getFont("jLabelConnectionState.font")); // NOI18N
        jLabelConnectionState.setForeground(resourceMap.getColor("jLabelConnectionState.foreground")); // NOI18N
        jLabelConnectionState.setText(i18n.tr("Getrennt"));
        jLabelConnectionState.setName("jLabelConnectionState"); // NOI18N
        jPanel2.add(jLabelConnectionState, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 40, 210, 20));

        jLabel14.setFont(resourceMap.getFont("jLabel15.font")); // NOI18N
        jLabel14.setForeground(resourceMap.getColor("jLabelVpnId.foreground")); // NOI18N
        jLabel14.setText(i18n.tr("Verbunden seit:"));
        jLabel14.setName("jLabel14"); // NOI18N
        jPanel2.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 20, 100, -1));

        jLabelOnlineHost.setFont(resourceMap.getFont("jLabelVpnTyp.font")); // NOI18N
        jLabelOnlineHost.setForeground(resourceMap.getColor("jLabelVpnId.foreground")); // NOI18N
        jLabelOnlineHost.setText(resourceMap.getString("jLabelOnlineHost.text")); // NOI18N
        jLabelOnlineHost.setName("jLabelOnlineHost"); // NOI18N
        jPanel2.add(jLabelOnlineHost, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 40, 160, -1));

        jLabel15.setFont(resourceMap.getFont("jLabel15.font")); // NOI18N
        jLabel15.setForeground(resourceMap.getColor("jLabelVpnId.foreground")); // NOI18N
        jLabel15.setText(i18n.tr("Online IP:"));
        jLabel15.setName("jLabel15"); // NOI18N
        jPanel2.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 40, 100, -1));

        jConnectedSince.setFont(resourceMap.getFont("jConnectedSince.font")); // NOI18N
        jConnectedSince.setForeground(resourceMap.getColor("jLabelVpnId.foreground")); // NOI18N
        jConnectedSince.setText(i18n.tr("(nicht verbunden)"));
        jConnectedSince.setName("jConnectedSince"); // NOI18N
        jPanel2.add(jConnectedSince, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 20, 160, -1));

        jLabel17.setFont(resourceMap.getFont("jLabelVpnTyp.font")); // NOI18N
        jLabel17.setForeground(resourceMap.getColor("jLabelVpnId.foreground")); // NOI18N
        jLabel17.setText(i18n.tr("VPN Id:"));
        jLabel17.setName("jLabel17"); // NOI18N
        jPanel2.add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 20, 90, -1));

        jLabelVpnId.setFont(resourceMap.getFont("jLabelVpnTyp.font")); // NOI18N
        jLabelVpnId.setForeground(resourceMap.getColor("jLabelVpnId.foreground")); // NOI18N
        jLabelVpnId.setText(resourceMap.getString("jLabelVpnId.text")); // NOI18N
        jLabelVpnId.setName("jLabelVpnId"); // NOI18N
        jPanel2.add(jLabelVpnId, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 20, 80, -1));

        jLabelValidUntilDesc.setFont(resourceMap.getFont("jLabelVpnTyp.font")); // NOI18N
        jLabelValidUntilDesc.setForeground(resourceMap.getColor("jLabelVpnId.foreground")); // NOI18N
        jLabelValidUntilDesc.setText(i18n.tr("Gültig bis:"));
        jLabelValidUntilDesc.setName("jLabelValidUntilDesc"); // NOI18N
        jPanel2.add(jLabelValidUntilDesc, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 60, 90, -1));

        jLabelVpnTyp.setFont(resourceMap.getFont("jLabelVpnTyp.font")); // NOI18N
        jLabelVpnTyp.setForeground(resourceMap.getColor("jLabelVpnId.foreground")); // NOI18N
        jLabelVpnTyp.setText(resourceMap.getString("jLabelVpnTyp.text")); // NOI18N
        jLabelVpnTyp.setName("jLabelVpnTyp"); // NOI18N
        jPanel2.add(jLabelVpnTyp, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 40, 80, -1));

        jLabel19.setFont(resourceMap.getFont("jLabelVpnTyp.font")); // NOI18N
        jLabel19.setForeground(resourceMap.getColor("jLabelVpnId.foreground")); // NOI18N
        jLabel19.setText(i18n.tr("VPN Typ:"));
        jLabel19.setName("jLabel19"); // NOI18N
        jPanel2.add(jLabel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 40, 90, -1));

        jLabelVpnValidUntil.setFont(resourceMap.getFont("jLabelVpnTyp.font")); // NOI18N
        jLabelVpnValidUntil.setForeground(resourceMap.getColor("jLabelVpnId.foreground")); // NOI18N
        jLabelVpnValidUntil.setText(resourceMap.getString("jLabelVpnValidUntil.text")); // NOI18N
        jLabelVpnValidUntil.setName("jLabelVpnValidUntil"); // NOI18N
        jPanel2.add(jLabelVpnValidUntil, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 60, 80, -1));

        jPanel1.setName("jPanel1"); // NOI18N

        jLayeredPane1.setName("jLayeredPane1"); // NOI18N
        jLayeredPane1.setOpaque(true);

        jServerListPanel.setBackground(resourceMap.getColor("jServerListPanel.background")); // NOI18N
        jServerListPanel.setMinimumSize(new java.awt.Dimension(310, 430));
        jServerListPanel.setName("jServerListPanel"); // NOI18N
        jServerListPanel.setPreferredSize(new java.awt.Dimension(313, 430));

        jLabel11.setBackground(resourceMap.getColor("jLabel11.background")); // NOI18N
        jLabel11.setFont(OxygenFont.getFontLargeBold());
        jLabel11.setForeground(resourceMap.getColor("jLabel11.foreground")); // NOI18N
        jLabel11.setText("   " + i18n.tr("Wähle einen Server für deine Verbindung"));
        jLabel11.setName("jLabel11"); // NOI18N
        jLabel11.setOpaque(true);

        jScrollPane.setBackground(resourceMap.getColor("jScrollPane.background")); // NOI18N
        jScrollPane.setName("jScrollPane"); // NOI18N
        jScrollPane.setPreferredSize(new java.awt.Dimension(454, 440));

        jServerListTable.setBackground(resourceMap.getColor("jServerListTable.background")); // NOI18N
        jServerListTable.setFont(resourceMap.getFont("jServerListTable.font")); // NOI18N
        jServerListTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jServerListTable.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jServerListTable.setName("jServerListTable"); // NOI18N
        jServerListTable.setOpaque(false);
        jServerListTable.setSelectionBackground(resourceMap.getColor("jServerListTable.selectionBackground")); // NOI18N
        jScrollPane.setViewportView(jServerListTable);

        jNetworkTransportType.add(jRadioUdp);
        jRadioUdp.setForeground(resourceMap.getColor("jRadioTcp.foreground")); // NOI18N
        jRadioUdp.setText(i18n.tr("UDP (schnell)"));
        jRadioUdp.setToolTipText(i18n.tr("<html>Wähle UDP für eine schnellere Verbindung.<br /> TCP solltest du nur wählen, wenn du Probleme hast, die Verbindung mit UDP aufzubauen.</html>"));
        jRadioUdp.setContentAreaFilled(false);
        jRadioUdp.setName("jRadioUdp"); // NOI18N
        jRadioUdp.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jRadioUdpStateChanged(evt);
            }
        });

        jNetworkTransportType.add(jRadioTcp);
        jRadioTcp.setForeground(resourceMap.getColor("jRadioTcp.foreground")); // NOI18N
        jRadioTcp.setText(i18n.tr("TCP (funktioniert auch bei sicheren Firewalls und Proxy-Servern.)"));
        jRadioTcp.setToolTipText(i18n.tr("<html>Wähle UDP für eine schnellere Verbindung.<br /> TCP solltest du nur wählen, wenn du Probleme hast, die Verbindung mit UDP aufzubauen oder wenn ein Proxy-Server eingesetzt wird.</html>"));
        jRadioTcp.setContentAreaFilled(false);
        jRadioTcp.setName("jRadioTcp"); // NOI18N

        jLabel12.setFont(OxygenFont.getFontLargeBold());
        jLabel12.setForeground(resourceMap.getColor("jRadioTcp.foreground")); // NOI18N
        jLabel12.setText(i18n.tr("Verbindungstyp"));
        jLabel12.setToolTipText(i18n.tr("<html>Wähle UDP für eine schnellere Verbindung.<br /> TCP solltest du nur wählen, wenn du Probleme hast, die Verbindung mit UDP aufzubauen.</html>"));
        jLabel12.setName("jLabel12"); // NOI18N

        jUpgradeButtonLabel1.setIcon(resourceMap.getIcon("jUpgradeButtonLabel1.icon")); // NOI18N
        jUpgradeButtonLabel1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jUpgradeButtonLabel1.setName("jUpgradeButtonLabel1"); // NOI18N
        jUpgradeButtonLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jUpgradeButtonLabel1MouseClicked(evt);
            }
        });

        jConnectButtonLabel1.setIcon(resourceMap.getIcon("jConnectButtonLabel1.icon")); // NOI18N
        jConnectButtonLabel1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jConnectButtonLabel1.setName("jConnectButtonLabel1"); // NOI18N
        jConnectButtonLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jConnectButtonLabel1MouseClicked(evt);
            }
        });

        jPremiumButtonLabel1.setIcon(resourceMap.getIcon("jPremiumButtonLabel1.icon")); // NOI18N
        jPremiumButtonLabel1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jPremiumButtonLabel1.setName("jPremiumButtonLabel1"); // NOI18N
        jPremiumButtonLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPremiumButtonLabel1MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jServerListPanelLayout = new javax.swing.GroupLayout(jServerListPanel);
        jServerListPanel.setLayout(jServerListPanelLayout);
        jServerListPanelLayout.setHorizontalGroup(
            jServerListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jServerListPanelLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jServerListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jServerListPanelLayout.createSequentialGroup()
                        .addGroup(jServerListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 750, Short.MAX_VALUE)
                            .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, 750, Short.MAX_VALUE))
                        .addGap(20, 20, 20))
                    .addGroup(jServerListPanelLayout.createSequentialGroup()
                        .addGroup(jServerListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 555, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jServerListPanelLayout.createSequentialGroup()
                                .addComponent(jRadioUdp, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(7, 7, 7)
                                .addComponent(jRadioTcp))
                            .addGroup(jServerListPanelLayout.createSequentialGroup()
                                .addComponent(jConnectButtonLabel1)
                                .addGap(2, 2, 2)
                                .addComponent(jUpgradeButtonLabel1)
                                .addGap(2, 2, 2)
                                .addComponent(jPremiumButtonLabel1)))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        jServerListPanelLayout.setVerticalGroup(
            jServerListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jServerListPanelLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel11)
                .addGap(20, 20, 20)
                .addComponent(jScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 306, Short.MAX_VALUE)
                .addGap(10, 10, 10)
                .addComponent(jLabel12)
                .addGap(1, 1, 1)
                .addGroup(jServerListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRadioUdp)
                    .addComponent(jRadioTcp))
                .addGap(20, 20, 20)
                .addGroup(jServerListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jUpgradeButtonLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jConnectButtonLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPremiumButtonLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10))
        );

        jServerListPanel.setBounds(0, 0, 770, 490);
        jLayeredPane1.add(jServerListPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);

        jConnectPanel.setBackground(resourceMap.getColor("jConnectPanel.background")); // NOI18N
        jConnectPanel.setFocusable(false);
        jConnectPanel.setName("jConnectPanel"); // NOI18N
        jConnectPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel8.setFont(OxygenFont.getFontLargeBold());
        jLabel8.setForeground(resourceMap.getColor("jLabel8.foreground")); // NOI18N
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N
        jConnectPanel.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 50, 160, 20));

        jLabel4.setFont(OxygenFont.getFontLargeBold());
        jLabel4.setForeground(resourceMap.getColor("jLabel4.foreground")); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText(i18n.tr("Lokaler Computer"));
        jLabel4.setName("jLabel4"); // NOI18N
        jConnectPanel.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 300, 160, 20));

        jLabel7.setFont(OxygenFont.getFontLargeBold());
        jLabel7.setForeground(resourceMap.getColor("jLabel7.foreground")); // NOI18N
        jLabel7.setText(i18n.tr("Internet"));
        jLabel7.setName("jLabel7"); // NOI18N
        jConnectPanel.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 390, -1, -1));

        jUpgradeButtonLabel.setIcon(resourceMap.getIcon("jUpgradeButtonLabel.icon")); // NOI18N
        jUpgradeButtonLabel.setText(resourceMap.getString("jUpgradeButtonLabel.text")); // NOI18N
        jUpgradeButtonLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jUpgradeButtonLabel.setName("jUpgradeButtonLabel"); // NOI18N
        jUpgradeButtonLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jUpgradeButtonLabelMouseClicked(evt);
            }
        });
        jConnectPanel.add(jUpgradeButtonLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 430, 257, 50));

        jConnectButtonLabel.setIcon(resourceMap.getIcon("jConnectButtonLabel.icon")); // NOI18N
        jConnectButtonLabel.setText(resourceMap.getString("jConnectButtonLabel.text")); // NOI18N
        jConnectButtonLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jConnectButtonLabel.setName("jConnectButtonLabel"); // NOI18N
        jConnectButtonLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jConnectButtonLabelMouseClicked(evt);
            }
        });
        jConnectPanel.add(jConnectButtonLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 430, 214, 50));

        jPremiumButtonLabel.setIcon(resourceMap.getIcon("jPremiumButtonLabel.icon")); // NOI18N
        jPremiumButtonLabel.setText(resourceMap.getString("jPremiumButtonLabel.text")); // NOI18N
        jPremiumButtonLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jPremiumButtonLabel.setName("jPremiumButtonLabel"); // NOI18N
        jPremiumButtonLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPremiumButtonLabelMouseClicked(evt);
            }
        });
        jConnectPanel.add(jPremiumButtonLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 430, 267, 50));

        jPanel4.setName("jPanel4"); // NOI18N

        jConnectionStateImage.setIcon(resourceMap.getIcon("jConnectionStateImage.icon")); // NOI18N
        jConnectionStateImage.setText(resourceMap.getString("jConnectionStateImage.text")); // NOI18N
        jConnectionStateImage.setName("jConnectionStateImage"); // NOI18N

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 736, Short.MAX_VALUE)
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel4Layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jConnectionStateImage)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 404, Short.MAX_VALUE)
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel4Layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jConnectionStateImage)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );

        jConnectPanel.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, -1, -1));

        jConnectPanel.setBounds(0, 0, 770, 490);
        jLayeredPane1.add(jConnectPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);

        jMapPanel.setBackground(resourceMap.getColor("jMapPanel.background")); // NOI18N
        jMapPanel.setName("jMapPanel"); // NOI18N

        jLabel13.setBackground(resourceMap.getColor("jLabel13.background")); // NOI18N
        jLabel13.setFont(OxygenFont.getFontLargeBold());
        jLabel13.setForeground(resourceMap.getColor("jLabel13.foreground")); // NOI18N
        jLabel13.setText("   " + i18n.tr("Server Übersichtskarte"));
        jLabel13.setName("jLabel13"); // NOI18N
        jLabel13.setOpaque(true);

        jXMapKit1.setDefaultProvider(org.jdesktop.swingx.JXMapKit.DefaultProviders.OpenStreetMaps);
        jXMapKit1.setInheritAlpha(false);
        jXMapKit1.setMiniMapVisible(false);
        jXMapKit1.setZoomButtonsVisible(false);
        jXMapKit1.setDataProviderCreditShown(true);
        jXMapKit1.setName("jXMapKit1"); // NOI18N
        jXMapKit1.setZoom(15);

        jShowOwnPosition.setForeground(resourceMap.getColor("jShowOwnPosition.foreground")); // NOI18N
        jShowOwnPosition.setText(i18n.tr("Datenroute und eigenen Standort anzeigen (falls verbunden)"));
        jShowOwnPosition.setContentAreaFilled(false);
        jShowOwnPosition.setName("jShowOwnPosition"); // NOI18N
        jShowOwnPosition.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jShowOwnPositionActionPerformed(evt);
            }
        });

        jGotoOwnLocation.setIcon(resourceMap.getIcon("jGotoOwnLocation.icon")); // NOI18N
        jGotoOwnLocation.setText(i18n.tr("gehe zu eigenem Standort"));
        jGotoOwnLocation.setEnabled(false);
        jGotoOwnLocation.setName("jGotoOwnLocation"); // NOI18N
        jGotoOwnLocation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jGotoOwnLocationActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jMapPanelLayout = new javax.swing.GroupLayout(jMapPanel);
        jMapPanel.setLayout(jMapPanelLayout);
        jMapPanelLayout.setHorizontalGroup(
            jMapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jMapPanelLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jMapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jMapPanelLayout.createSequentialGroup()
                        .addGroup(jMapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jMapPanelLayout.createSequentialGroup()
                                .addComponent(jShowOwnPosition, javax.swing.GroupLayout.DEFAULT_SIZE, 533, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jGotoOwnLocation, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(3, 3, 3))
                            .addComponent(jLabel13, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 730, Short.MAX_VALUE))
                        .addGap(20, 20, 20))
                    .addGroup(jMapPanelLayout.createSequentialGroup()
                        .addComponent(jXMapKit1, javax.swing.GroupLayout.DEFAULT_SIZE, 726, Short.MAX_VALUE)
                        .addGap(24, 24, 24))))
        );
        jMapPanelLayout.setVerticalGroup(
            jMapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jMapPanelLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel13)
                .addGap(10, 10, 10)
                .addGroup(jMapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jShowOwnPosition)
                    .addComponent(jGotoOwnLocation))
                .addGap(10, 10, 10)
                .addComponent(jXMapKit1, javax.swing.GroupLayout.DEFAULT_SIZE, 368, Short.MAX_VALUE)
                .addGap(40, 40, 40))
        );

        jMapPanel.setBounds(0, 0, 770, 490);
        jLayeredPane1.add(jMapPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);

        jUsaPanel.setBackground(resourceMap.getColor("jUsaPanel.background")); // NOI18N
        jUsaPanel.setName("jUsaPanel"); // NOI18N

        jLabel16.setBackground(resourceMap.getColor("jLabel16.background")); // NOI18N
        jLabel16.setFont(OxygenFont.getFontLargeBold());
        jLabel16.setForeground(new java.awt.Color(255, 255, 255));
        jLabel16.setText("   " + i18n.tr("Liste von TV Streams aus den USA (englischsprachig)"));
        jLabel16.setName("jLabel16"); // NOI18N
        jLabel16.setOpaque(true);

        jLabel10.setForeground(resourceMap.getColor("jLabel10.foreground")); // NOI18N
        jLabel10.setText(i18n.tr("<html>Für die Verwendung dieser Streams ist in der Regel eine US-IP-Adresse nötig. Diese erhälst du, in dem du dich auf einen Shellfire VPN Server in den USA verbindest.</html>"));
        jLabel10.setName("jLabel10"); // NOI18N

        jPanel3.setBackground(resourceMap.getColor("jPanel3.background")); // NOI18N
        jPanel3.setName("jPanel3"); // NOI18N

        jButton4.setBackground(resourceMap.getColor("jButton4.background")); // NOI18N
        jButton4.setFont(OxygenFont.getFont());
        jButton4.setText(resourceMap.getString("jButton4.text")); // NOI18N
        jButton4.setAlignmentY(0.0F);
        jButton4.setBorderPainted(false);
        jButton4.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton4.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton4.setName("jButton4"); // NOI18N
        jButton4.setOpaque(true);
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton5.setBackground(resourceMap.getColor("jButton9.background")); // NOI18N
        jButton5.setFont(OxygenFont.getFont());
        jButton5.setText(resourceMap.getString("jButton5.text")); // NOI18N
        jButton5.setAlignmentY(0.0F);
        jButton5.setBorderPainted(false);
        jButton5.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton5.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton5.setName("jButton5"); // NOI18N
        jButton5.setOpaque(true);
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jButton6.setBackground(resourceMap.getColor("jButton9.background")); // NOI18N
        jButton6.setFont(OxygenFont.getFont());
        jButton6.setText(resourceMap.getString("jButton6.text")); // NOI18N
        jButton6.setBorderPainted(false);
        jButton6.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton6.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton6.setName("jButton6"); // NOI18N
        jButton6.setOpaque(true);
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jButton7.setBackground(resourceMap.getColor("jButton9.background")); // NOI18N
        jButton7.setFont(OxygenFont.getFont());
        jButton7.setText(resourceMap.getString("jButton7.text")); // NOI18N
        jButton7.setAlignmentY(0.0F);
        jButton7.setBorderPainted(false);
        jButton7.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton7.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton7.setName("jButton7"); // NOI18N
        jButton7.setOpaque(true);
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jButton8.setBackground(resourceMap.getColor("jButton9.background")); // NOI18N
        jButton8.setFont(OxygenFont.getFont());
        jButton8.setText(resourceMap.getString("jButton8.text")); // NOI18N
        jButton8.setAlignmentY(0.0F);
        jButton8.setBorderPainted(false);
        jButton8.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton8.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton8.setName("jButton8"); // NOI18N
        jButton8.setOpaque(true);
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        jButton9.setBackground(resourceMap.getColor("jButton9.background")); // NOI18N
        jButton9.setFont(OxygenFont.getFont());
        jButton9.setText(resourceMap.getString("jButton9.text")); // NOI18N
        jButton9.setAlignmentY(0.0F);
        jButton9.setBorderPainted(false);
        jButton9.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton9.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton9.setName("jButton9"); // NOI18N
        jButton9.setOpaque(true);
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });

        jButton10.setBackground(resourceMap.getColor("jButton9.background")); // NOI18N
        jButton10.setFont(OxygenFont.getFont());
        jButton10.setText(resourceMap.getString("jButton10.text")); // NOI18N
        jButton10.setAlignmentY(0.0F);
        jButton10.setBorderPainted(false);
        jButton10.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton10.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton10.setName("jButton10"); // NOI18N
        jButton10.setOpaque(true);
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });

        jButton11.setBackground(resourceMap.getColor("jButton9.background")); // NOI18N
        jButton11.setFont(OxygenFont.getFont());
        jButton11.setText(resourceMap.getString("jButton11.text")); // NOI18N
        jButton11.setAlignmentY(0.0F);
        jButton11.setBorderPainted(false);
        jButton11.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton11.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton11.setName("jButton11"); // NOI18N
        jButton11.setOpaque(true);
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, 730, Short.MAX_VALUE)
            .addComponent(jButton5, javax.swing.GroupLayout.DEFAULT_SIZE, 730, Short.MAX_VALUE)
            .addComponent(jButton6, javax.swing.GroupLayout.DEFAULT_SIZE, 730, Short.MAX_VALUE)
            .addComponent(jButton7, javax.swing.GroupLayout.DEFAULT_SIZE, 730, Short.MAX_VALUE)
            .addComponent(jButton8, javax.swing.GroupLayout.DEFAULT_SIZE, 730, Short.MAX_VALUE)
            .addComponent(jButton9, javax.swing.GroupLayout.DEFAULT_SIZE, 730, Short.MAX_VALUE)
            .addComponent(jButton10, javax.swing.GroupLayout.DEFAULT_SIZE, 730, Short.MAX_VALUE)
            .addComponent(jButton11, javax.swing.GroupLayout.DEFAULT_SIZE, 730, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jButton4)
                .addGap(2, 2, 2)
                .addComponent(jButton5)
                .addGap(2, 2, 2)
                .addComponent(jButton6)
                .addGap(2, 2, 2)
                .addComponent(jButton7)
                .addGap(2, 2, 2)
                .addComponent(jButton8)
                .addGap(2, 2, 2)
                .addComponent(jButton9)
                .addGap(2, 2, 2)
                .addComponent(jButton10)
                .addGap(2, 2, 2)
                .addComponent(jButton11))
        );

        javax.swing.GroupLayout jUsaPanelLayout = new javax.swing.GroupLayout(jUsaPanel);
        jUsaPanel.setLayout(jUsaPanelLayout);
        jUsaPanelLayout.setHorizontalGroup(
            jUsaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jUsaPanelLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jUsaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, 730, Short.MAX_VALUE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, 730, Short.MAX_VALUE))
                .addGap(20, 20, 20))
        );
        jUsaPanelLayout.setVerticalGroup(
            jUsaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jUsaPanelLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel16)
                .addGap(0, 0, 0)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(189, 189, 189))
        );

        jUsaPanel.setBounds(0, 0, 770, 490);
        jLayeredPane1.add(jUsaPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLayeredPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 771, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLayeredPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 487, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        javax.swing.GroupLayout jContentPanelLayout = new javax.swing.GroupLayout(jContentPanel);
        jContentPanel.setLayout(jContentPanelLayout);
        jContentPanelLayout.setHorizontalGroup(
            jContentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jContentPanelLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 731, Short.MAX_VALUE)
                .addGap(20, 20, 20))
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jContentPanelLayout.setVerticalGroup(
            jContentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jContentPanelLayout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jMenuPanel.setBackground(resourceMap.getColor("jMenuPanel.background")); // NOI18N
        jMenuPanel.setName("jMenuPanel"); // NOI18N
        jMenuPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel5.setBackground(resourceMap.getColor("jLabel5.background")); // NOI18N
        jLabel5.setIcon(getLogo());
        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setAlignmentY(0.0F);
        jLabel5.setName("jLabel5"); // NOI18N
        jMenuPanel.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 250, 60));

        jPanelConnect.setBackground(resourceMap.getColor("jPanelConnect.background")); // NOI18N
        jPanelConnect.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jPanelConnect.setMinimumSize(new java.awt.Dimension(250, 66));
        jPanelConnect.setName("jPanelConnect"); // NOI18N
        jPanelConnect.setPreferredSize(new java.awt.Dimension(250, 66));
        jPanelConnect.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabelConnectHeader.setFont(resourceMap.getFont("jLabelConnectHeader.font")); // NOI18N
        jLabelConnectHeader.setText(i18n.tr("Verbindung"));
        jLabelConnectHeader.setName("jLabelConnectHeader"); // NOI18N
        jPanelConnect.add(jLabelConnectHeader, new org.netbeans.lib.awtextra.AbsoluteConstraints(65, 15, 175, 20));

        jLabelConnectFooter.setFont(resourceMap.getFont("jLabelConnectFooter.font")); // NOI18N
        jLabelConnectFooter.setText(i18n.tr("Jetzt zu Shellfire VPN verbinden"));
        jLabelConnectFooter.setName("jLabelConnectFooter"); // NOI18N
        jPanelConnect.add(jLabelConnectFooter, new org.netbeans.lib.awtextra.AbsoluteConstraints(65, 40, 177, 15));

        jButtonConnect.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/shellfire/vpn/gui/resources/button-connect-idle.png"))); // NOI18N
        jButtonConnect.setText(resourceMap.getString("jButtonConnect.text")); // NOI18N
        jButtonConnect.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jButtonConnect.setName("jButtonConnect"); // NOI18N
        jButtonConnect.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButtonConnectMouseClicked(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonConnectMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonConnectMouseEntered(evt);
            }
        });
        jPanelConnect.add(jButtonConnect, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 250, 66));

        jMenuPanel.add(jPanelConnect, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 60, 250, 66));

        jPanelServerList.setBackground(resourceMap.getColor("jPanelServerList.background")); // NOI18N
        jPanelServerList.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jPanelServerList.setMinimumSize(new java.awt.Dimension(250, 66));
        jPanelServerList.setName("jPanelServerList"); // NOI18N
        jPanelServerList.setPreferredSize(new java.awt.Dimension(250, 66));
        jPanelServerList.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabelServerListHeader.setFont(resourceMap.getFont("jLabelServerListHeader.font")); // NOI18N
        jLabelServerListHeader.setText(i18n.tr("Server Liste"));
        jLabelServerListHeader.setName("jLabelServerListHeader"); // NOI18N
        jPanelServerList.add(jLabelServerListHeader, new org.netbeans.lib.awtextra.AbsoluteConstraints(65, 15, 175, 20));

        jLabelServerListFooter.setFont(resourceMap.getFont("jLabelConnectFooter.font")); // NOI18N
        jLabelServerListFooter.setText(i18n.tr("Liste aller VPN Server anzeigen"));
        jLabelServerListFooter.setName("jLabelServerListFooter"); // NOI18N
        jPanelServerList.add(jLabelServerListFooter, new org.netbeans.lib.awtextra.AbsoluteConstraints(65, 40, 177, 15));

        jButtonServerList.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/shellfire/vpn/gui/resources/button-serverlist-idle.png"))); // NOI18N
        jButtonServerList.setText(resourceMap.getString("jButtonServerList.text")); // NOI18N
        jButtonServerList.setName("jButtonServerList"); // NOI18N
        jButtonServerList.setPreferredSize(new java.awt.Dimension(250, 60));
        jButtonServerList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButtonServerListMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonServerListMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonServerListMouseExited(evt);
            }
        });
        jPanelServerList.add(jButtonServerList, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 250, 66));

        jMenuPanel.add(jPanelServerList, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 126, 250, 66));

        jPanelMap.setBackground(resourceMap.getColor("jPanelMap.background")); // NOI18N
        jPanelMap.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jPanelMap.setMinimumSize(new java.awt.Dimension(250, 66));
        jPanelMap.setName("jPanelMap"); // NOI18N
        jPanelMap.setPreferredSize(new java.awt.Dimension(250, 66));
        jPanelMap.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabelMapHeader.setFont(resourceMap.getFont("jLabelServerListHeader.font")); // NOI18N
        jLabelMapHeader.setText(i18n.tr("Karte"));
        jLabelMapHeader.setName("jLabelMapHeader"); // NOI18N
        jPanelMap.add(jLabelMapHeader, new org.netbeans.lib.awtextra.AbsoluteConstraints(65, 15, 175, 20));

        jLabelMapFooter.setFont(resourceMap.getFont("jLabelConnectFooter.font")); // NOI18N
        jLabelMapFooter.setText(i18n.tr("Zeigt Verschlüsselungsroute"));
        jLabelMapFooter.setName("jLabelMapFooter"); // NOI18N
        jPanelMap.add(jLabelMapFooter, new org.netbeans.lib.awtextra.AbsoluteConstraints(65, 40, 177, 15));

        jButtonMap.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/shellfire/vpn/gui/resources/button-map-idle.png"))); // NOI18N
        jButtonMap.setText(resourceMap.getString("jButtonMap.text")); // NOI18N
        jButtonMap.setName("jButtonMap"); // NOI18N
        jButtonMap.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButtonMapMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMapMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMapMouseExited(evt);
            }
        });
        jPanelMap.add(jButtonMap, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 250, 66));

        jMenuPanel.add(jPanelMap, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 192, 250, 66));

        jPanelUsa.setBackground(resourceMap.getColor("jPanelUsa.background")); // NOI18N
        jPanelUsa.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jPanelUsa.setMinimumSize(new java.awt.Dimension(250, 66));
        jPanelUsa.setName("jPanelUsa"); // NOI18N
        jPanelUsa.setPreferredSize(new java.awt.Dimension(250, 66));
        jPanelUsa.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabelUsaHeader.setFont(resourceMap.getFont("jLabelServerListHeader.font")); // NOI18N
        jLabelUsaHeader.setText(i18n.tr("Streams aus den USA"));
        jLabelUsaHeader.setName("jLabelUsaHeader"); // NOI18N
        jPanelUsa.add(jLabelUsaHeader, new org.netbeans.lib.awtextra.AbsoluteConstraints(65, 15, 175, 20));

        jLabelUsaFooter.setFont(resourceMap.getFont("jLabelConnectFooter.font")); // NOI18N
        jLabelUsaFooter.setText(i18n.tr("Liste amerikanischer TV Streams"));
        jLabelUsaFooter.setName("jLabelUsaFooter"); // NOI18N
        jPanelUsa.add(jLabelUsaFooter, new org.netbeans.lib.awtextra.AbsoluteConstraints(65, 40, 177, 15));

        jButtonUsa.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/shellfire/vpn/gui/resources/button-usa-idle.png"))); // NOI18N
        jButtonUsa.setText(resourceMap.getString("jButtonUsa.text")); // NOI18N
        jButtonUsa.setName("jButtonUsa"); // NOI18N
        jButtonUsa.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButtonUsaMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonUsaMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonUsaMouseExited(evt);
            }
        });
        jPanelUsa.add(jButtonUsa, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 250, 66));

        jMenuPanel.add(jPanelUsa, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 256, 250, 66));

        jHeaderPanel.setBackground(resourceMap.getColor("jHeaderPanel.background")); // NOI18N
        jHeaderPanel.setName("jHeaderPanel"); // NOI18N

        jLabelHelp.setFont(resourceMap.getFont("jLabelHelp.font")); // NOI18N
        jLabelHelp.setForeground(resourceMap.getColor("jLabelHelp.foreground")); // NOI18N
        jLabelHelp.setIcon(resourceMap.getIcon("jLabelHelp.icon")); // NOI18N
        jLabelHelp.setText(resourceMap.getString("jLabelHelp.text")); // NOI18N
        jLabelHelp.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabelHelp.setName("jLabelHelp"); // NOI18N
        jLabelHelp.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabelHelpMouseClicked(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabelHelpMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabelHelpMouseEntered(evt);
            }
        });

        jLabelExit.setFont(resourceMap.getFont("jLabelExit.font")); // NOI18N
        jLabelExit.setForeground(resourceMap.getColor("jLabelExit.foreground")); // NOI18N
        jLabelExit.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelExit.setIcon(resourceMap.getIcon("jLabelExit.icon")); // NOI18N
        jLabelExit.setText(resourceMap.getString("jLabelExit.text")); // NOI18N
        jLabelExit.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabelExit.setName("jLabelExit"); // NOI18N
        jLabelExit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabelExitMouseClicked(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabelExitMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabelExitMouseEntered(evt);
            }
        });

        jLabelHide.setFont(new java.awt.Font("Arial", 0, 14));
        jLabelHide.setForeground(resourceMap.getColor("jLabelHide.foreground")); // NOI18N
        jLabelHide.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelHide.setIcon(resourceMap.getIcon("jLabelHide.icon")); // NOI18N
        jLabelHide.setText(resourceMap.getString("jLabelHide.text")); // NOI18N
        jLabelHide.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabelHide.setName("jLabelHide"); // NOI18N
        jLabelHide.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabelHideMouseClicked(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabelHideMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabelHideMouseEntered(evt);
            }
        });

        jLabelSettings.setFont(new java.awt.Font("Arial", 0, 14));
        jLabelSettings.setForeground(resourceMap.getColor("jLabelSettings.foreground")); // NOI18N
        jLabelSettings.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelSettings.setIcon(resourceMap.getIcon("jLabelSettings.icon")); // NOI18N
        jLabelSettings.setText(resourceMap.getString("jLabelSettings.text")); // NOI18N
        jLabelSettings.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabelSettings.setName("jLabelSettings"); // NOI18N
        jLabelSettings.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabelSettingsMouseClicked(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabelSettingsMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabelSettingsMouseEntered(evt);
            }
        });

        jLabelMinimize.setFont(new java.awt.Font("Arial 14 14", 0, 14));
        jLabelMinimize.setForeground(resourceMap.getColor("jLabelMinimize.foreground")); // NOI18N
        jLabelMinimize.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelMinimize.setIcon(resourceMap.getIcon("jLabelMinimize.icon")); // NOI18N
        jLabelMinimize.setText(resourceMap.getString("jLabelMinimize.text")); // NOI18N
        jLabelMinimize.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabelMinimize.setName("jLabelMinimize"); // NOI18N
        jLabelMinimize.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabelMinimizeMouseClicked(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabelMinimizeMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabelMinimizeMouseEntered(evt);
            }
        });

        jLabel1.setForeground(resourceMap.getColor("jLabel1.foreground")); // NOI18N
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel6.setBackground(resourceMap.getColor("jLabel6.background")); // NOI18N
        jLabel6.setForeground(resourceMap.getColor("jLabel6.foreground")); // NOI18N
        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        javax.swing.GroupLayout jHeaderPanelLayout = new javax.swing.GroupLayout(jHeaderPanel);
        jHeaderPanel.setLayout(jHeaderPanelLayout);
        jHeaderPanelLayout.setHorizontalGroup(
            jHeaderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jHeaderPanelLayout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelHelp)
                .addGap(18, 18, 18)
                .addComponent(jLabelSettings, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 495, Short.MAX_VALUE)
                .addComponent(jLabelHide, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelMinimize, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelExit, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6)
                .addContainerGap())
        );
        jHeaderPanelLayout.setVerticalGroup(
            jHeaderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabelHelp, javax.swing.GroupLayout.DEFAULT_SIZE, 59, Short.MAX_VALUE)
            .addComponent(jLabelSettings, javax.swing.GroupLayout.DEFAULT_SIZE, 59, Short.MAX_VALUE)
            .addComponent(jLabelHide, javax.swing.GroupLayout.DEFAULT_SIZE, 59, Short.MAX_VALUE)
            .addComponent(jLabelMinimize, javax.swing.GroupLayout.DEFAULT_SIZE, 59, Short.MAX_VALUE)
            .addComponent(jLabelExit, javax.swing.GroupLayout.DEFAULT_SIZE, 59, Short.MAX_VALUE)
            .addGroup(jHeaderPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addContainerGap(23, Short.MAX_VALUE))
            .addGroup(jHeaderPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6)
                .addContainerGap(23, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jMenuPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jContentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jHeaderPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jHeaderPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jContentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 579, Short.MAX_VALUE))
                    .addComponent(jMenuPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 638, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, 1027, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, 640, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jConnectButtonLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jConnectButtonLabelMouseClicked
    	this.connectFromButton(false);
    }//GEN-LAST:event_jConnectButtonLabelMouseClicked

    private void jUpgradeButtonLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jUpgradeButtonLabelMouseClicked
    	showUpgradeDialog();
    }//GEN-LAST:event_jUpgradeButtonLabelMouseClicked

    private void jPremiumButtonLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPremiumButtonLabelMouseClicked
    	showNagScreenWithoutTimer();
    }//GEN-LAST:event_jPremiumButtonLabelMouseClicked

    private void jUpgradeButtonLabel1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jUpgradeButtonLabel1MouseClicked
    	showUpgradeDialog();
    }//GEN-LAST:event_jUpgradeButtonLabel1MouseClicked

    private void jConnectButtonLabel1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jConnectButtonLabel1MouseClicked
    	this.connectFromButton(true);
    }//GEN-LAST:event_jConnectButtonLabel1MouseClicked

    private void jPremiumButtonLabel1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPremiumButtonLabel1MouseClicked
    	showNagScreenWithoutTimer();
    }//GEN-LAST:event_jPremiumButtonLabel1MouseClicked

	private void jUpgradButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jUpgradButtonActionPerformed
		showUpgradeDialog();
	}// GEN-LAST:event_jUpgradButtonActionPerformed

	private void jUpgradButton1ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jUpgradButton1ActionPerformed
		showUpgradeDialog();
	}// GEN-LAST:event_jUpgradButton1ActionPerformed

	private void jPremiumButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jPremiumButtonActionPerformed
		showNagScreenWithoutTimer();
	}// GEN-LAST:event_jPremiumButtonActionPerformed

	private void jPremiumButton1ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jPremiumButton1ActionPerformed
		showNagScreenWithoutTimer();
	}// GEN-LAST:event_jPremiumButton1ActionPerformed

	private boolean isFreeAccount() {
		return this.shellfireService.getVpn().getAccountType() == ServerType.Free;
	}

	private boolean isPremiumAccount() {
		return this.shellfireService.getVpn().getAccountType() == ServerType.Premium;
	}

	private void jLabelHelpMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_jLabelHelpMouseClicked
		this.openHelp();
	}// GEN-LAST:event_jLabelHelpMouseClicked

	private void jLabelExitMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_jLabelExitMouseClicked
		exitHandler();

	}// GEN-LAST:event_jLabelExitMouseClicked

	private void jLabelHideMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_jLabelHideMouseClicked
		this.setVisible(false);
		this.toBack();
	}// GEN-LAST:event_jLabelHideMouseClicked

	private void jLabelHelpMouseEntered(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_jLabelHelpMouseEntered
		jLabelHelp.setForeground(Color.LIGHT_GRAY);
	}// GEN-LAST:event_jLabelHelpMouseEntered

	private void jLabelHideMouseEntered(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_jLabelHideMouseEntered
		jLabelHide.setForeground(Color.LIGHT_GRAY);
	}// GEN-LAST:event_jLabelHideMouseEntered

	private void jLabelExitMouseEntered(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_jLabelExitMouseEntered
		jLabelExit.setForeground(Color.LIGHT_GRAY);
	}// GEN-LAST:event_jLabelExitMouseEntered

	private void jLabelHelpMouseExited(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_jLabelHelpMouseExited
		jLabelHelp.setForeground(Color.white);
	}// GEN-LAST:event_jLabelHelpMouseExited

	private void jLabelHideMouseExited(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_jLabelHideMouseExited
		jLabelHide.setForeground(Color.white);
	}// GEN-LAST:event_jLabelHideMouseExited

	private void jLabelExitMouseExited(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_jLabelExitMouseExited
		jLabelExit.setForeground(Color.white);
	}// GEN-LAST:event_jLabelExitMouseExited

	private void jLabelSettingsMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_jLabelSettingsMouseClicked
		this.showSettingsDialog();
	}// GEN-LAST:event_jLabelSettingsMouseClicked

	private void jLabelSettingsMouseEntered(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_jLabelSettingsMouseEntered
		jLabelSettings.setForeground(Color.LIGHT_GRAY);
	}// GEN-LAST:event_jLabelSettingsMouseEntered

	private void jLabelSettingsMouseExited(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_jLabelSettingsMouseExited
		jLabelSettings.setForeground(Color.white);
	}// GEN-LAST:event_jLabelSettingsMouseExited

	private void jButtonConnectMouseEntered(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_jButtonConnectMouseEntered
		this.setHoverStartFromMouseEnteredEvent(evt);
	}// GEN-LAST:event_jButtonConnectMouseEntered

	private void jButtonConnectMouseExited(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_jButtonConnectMouseExited
		this.setHoverEnd();
	}// GEN-LAST:event_jButtonConnectMouseExited

	private void jButtonServerListMouseEntered(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_jButtonServerListMouseEntered
		this.setHoverStartFromMouseEnteredEvent(evt);
	}// GEN-LAST:event_jButtonServerListMouseEntered

	private void jButtonMapMouseEntered(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_jButtonMapMouseEntered
		this.setHoverStartFromMouseEnteredEvent(evt);
	}// GEN-LAST:event_jButtonMapMouseEntered

	private void jButtonUsaMouseEntered(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_jButtonUsaMouseEntered
		this.setHoverStartFromMouseEnteredEvent(evt);
	}// GEN-LAST:event_jButtonUsaMouseEntered

	private void jButtonServerListMouseExited(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_jButtonServerListMouseExited
		this.setHoverEnd();
	}// GEN-LAST:event_jButtonServerListMouseExited

	private void jButtonMapMouseExited(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_jButtonMapMouseExited
		this.setHoverEnd();
	}// GEN-LAST:event_jButtonMapMouseExited

	private void jButtonUsaMouseExited(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_jButtonUsaMouseExited
		this.setHoverEnd();
	}// GEN-LAST:event_jButtonUsaMouseExited

	private void jButtonConnectMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_jButtonConnectMouseClicked
		this.setActiveContentFromMouseEnteredEvent(evt);
	}// GEN-LAST:event_jButtonConnectMouseClicked

	private void jButtonServerListMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_jButtonServerListMouseClicked
		this.setActiveContentFromMouseEnteredEvent(evt);
	}// GEN-LAST:event_jButtonServerListMouseClicked

	private void jButtonMapMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_jButtonMapMouseClicked
		this.setActiveContentFromMouseEnteredEvent(evt);
	}// GEN-LAST:event_jButtonMapMouseClicked

	private void jButtonUsaMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_jButtonUsaMouseClicked
		this.setActiveContentFromMouseEnteredEvent(evt);
	}// GEN-LAST:event_jButtonUsaMouseClicked

	private void jConnectButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jConnectButtonActionPerformed
		this.connectFromButton(false);
	}// GEN-LAST:event_jConnectButtonActionPerformed

	public void connectFromButton(final boolean failIfPremiumServerForFreeUser) {
		this.setWaitCursor();

		SwingWorker<ConnectionState, Void> sw = new SwingWorker<ConnectionState, Void>() {

			@Override
			protected ConnectionState doInBackground() throws Exception {
				ConnectionState state = controller.getCurrentConnectionState();

				return state;
			}

			public void done() {

				try {
					ConnectionState state = get();

					switch (state) {
					case Disconnected:
						if (isFreeAccount()) {

							Server server = getSelectedServer();
							if (server.getServerType() == ServerType.Premium || server.getServerType() == ServerType.PremiumPlus) {
								if (failIfPremiumServerForFreeUser) {
									setNormalCursor();
									if (JOptionPane.YES_OPTION == JOptionPane
											.showConfirmDialog(
													null,
													i18n.tr("Dieser Server steht nur für Shellfire VPN Premium Kunden zur Verfügung\n\nWeitere Informationen zu Shellfire VPN Premium anzeigen?"),
													i18n.tr("Premium Server ausgewählt"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {

										showNagScreenWithoutTimer();

									}
									return;
								} else {
									server = serverList.getRandomFreeServer();
									setSelectedServer(server);
								}

							}

							delayedConnect(server, getSelectedProtocol(), Reason.ConnectButtonPressed);
						} else if (isPremiumAccount()) {

							Server server = getSelectedServer();

							if (server.getServerType() == ServerType.PremiumPlus) {
								if (failIfPremiumServerForFreeUser) {
									setNormalCursor();
									if (JOptionPane.YES_OPTION == JOptionPane
											.showConfirmDialog(
													null,
													i18n.tr("Dieser Server steht nur für Shellfire VPN PremiumPlus Kunden zur Verfügung\n\nWeitere Informationen zu Shellfire VPN PremiumPlus anzeigen?"),
													i18n.tr("PremiumPlus Server ausgewählt"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {

										showNagScreenWithoutTimer();

									}
									return;
								} else {
									server = serverList.getRandomPremiumServer();
									setSelectedServer(server);
								}

							}

							controller.connect(getSelectedServer(), getSelectedProtocol(), Reason.ConnectButtonPressed);
						} else {
							controller.connect(getSelectedServer(), getSelectedProtocol(), Reason.ConnectButtonPressed);
						}

						break;
					case Connecting:

						// not possible to click
					case Connected:
						controller.disconnect(Reason.DisconnectButtonPressed);
						break;
					}
				} catch (Exception e) {
					Util.handleException(e);
				}
			}

		};
		sw.execute();

	}

	private void showConnectProgress() {
		if (this.connectProgressDialog == null) {
			this.connectProgressDialog = new ProgressDialog(this, false, i18n.tr("Verbindung wird hergestellt..."));
			this.connectProgressDialog.setOption(2, i18n.tr("abbrechen"));
			this.connectProgressDialog.setOptionCallback(new Runnable() {

				@Override
				public void run() {
				  SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {

			      @Override
			      protected Void doInBackground() throws Exception {
		          try {
		            controller.disconnect(Reason.AbortButtonPressed);
		          } catch (RemoteException e) {
		            Util.handleException(e);
		          }
		          
		          return null;
			      }
				  };
				  
				  sw.execute();
				  setNormalCursor();
				}
			});
		}

		connectProgressDialog.setVisible(true);

	}

	private void hideConnectProgress() {
		if (this.connectProgressDialog != null)
			this.connectProgressDialog.setVisible(false);
	}

	private void jServerListConnectButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jConnectButton1ActionPerformed
		this.connectFromButton(true);
	}// GEN-LAST:event_jConnectButton1ActionPerformed

	private void jRadioUdpStateChanged(javax.swing.event.ChangeEvent evt) {// GEN-FIRST:event_jRadioUdpStateChanged
	
	}// GEN-LAST:event_jRadioUdpStateChanged

	private void jShowOwnPositionActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jShowOwnPositionActionPerformed
		if (jShowOwnPosition.isSelected()) {
			this.showOwnPositionOnMap();
			// this.drawConnectionRouteOnMap();
		} else {
			this.hideOwnPositionOnMap();
			// this.hideConnectionRouteOnMap();
		}

		this.jGotoOwnLocation.setEnabled(jShowOwnPosition.isSelected());
	}// GEN-LAST:event_jShowOwnPositionActionPerformed

	private void jGotoOwnLocationActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jGotoOwnLocationActionPerformed
		if (this.ownPosition != null) {
			this.jXMapKit1.getMainMap().setCenterPosition(new GeoPosition(this.ownPosition.getLatitude(), this.ownPosition.getLongitude()));
			this.jXMapKit1.getMainMap().repaint();
		}

	}// GEN-LAST:event_jGotoOwnLocationActionPerformed

	private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton4ActionPerformed
		this.openUsTvStream(evt);
	}// GEN-LAST:event_jButton4ActionPerformed

	private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton5ActionPerformed
		this.openUsTvStream(evt);
	}// GEN-LAST:event_jButton5ActionPerformed

	private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton6ActionPerformed
		this.openUsTvStream(evt);
	}// GEN-LAST:event_jButton6ActionPerformed

	private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton7ActionPerformed
		this.openUsTvStream(evt);
	}// GEN-LAST:event_jButton7ActionPerformed

	private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton8ActionPerformed
		this.openUsTvStream(evt);
	}// GEN-LAST:event_jButton8ActionPerformed

	private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton9ActionPerformed
		this.openUsTvStream(evt);
	}// GEN-LAST:event_jButton9ActionPerformed

	private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton10ActionPerformed
		this.openUsTvStream(evt);
	}// GEN-LAST:event_jButton10ActionPerformed

	private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton11ActionPerformed
    this.openUsTvStream(evt);
  }// GEN-LAST:event_jButton11ActionPerformed

	private void jLabelMinimizeMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_jLabelMinimizeMouseClicked
		this.setState(Frame.ICONIFIED);

	}// GEN-LAST:event_jLabelMinimizeMouseClicked

	private void jLabelMinimizeMouseEntered(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_jLabelMinimizeMouseEntered
		jLabelMinimize.setForeground(Color.LIGHT_GRAY);
	}// GEN-LAST:event_jLabelMinimizeMouseEntered

	private void jLabelMinimizeMouseExited(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_jLabelMinimizeMouseExited
		jLabelMinimize.setForeground(Color.white);
	}// GEN-LAST:event_jLabelMinimizeMouseExited

	@Action
	public void openHelp() {
		org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance().getContext()
				.getResourceMap(ShellfireVPNMainForm.class);

		Util.openUrl(shellfireService.getUrlHelp());

	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JLabel jButtonConnect;
    private javax.swing.JLabel jButtonMap;
    private javax.swing.JLabel jButtonServerList;
    private javax.swing.JLabel jButtonUsa;
    private javax.swing.JLabel jConnectButtonLabel;
    private javax.swing.JLabel jConnectButtonLabel1;
    private javax.swing.JPanel jConnectPanel;
    private javax.swing.JLabel jConnectedSince;
    private javax.swing.JLabel jConnectionStateIcon;
    private javax.swing.JLabel jConnectionStateImage;
    private javax.swing.JPanel jContentPanel;
    private javax.swing.JButton jGotoOwnLocation;
    private javax.swing.JPanel jHeaderPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabelConnectFooter;
    private javax.swing.JLabel jLabelConnectHeader;
    private javax.swing.JLabel jLabelConnectionState;
    private javax.swing.JLabel jLabelExit;
    private javax.swing.JLabel jLabelHelp;
    private javax.swing.JLabel jLabelHide;
    private javax.swing.JLabel jLabelMapFooter;
    private javax.swing.JLabel jLabelMapHeader;
    private javax.swing.JLabel jLabelMinimize;
    private javax.swing.JLabel jLabelOnlineHost;
    private javax.swing.JLabel jLabelServerListFooter;
    private javax.swing.JLabel jLabelServerListHeader;
    private javax.swing.JLabel jLabelSettings;
    private javax.swing.JLabel jLabelUsaFooter;
    private javax.swing.JLabel jLabelUsaHeader;
    private javax.swing.JLabel jLabelValidUntilDesc;
    private javax.swing.JLabel jLabelVpnId;
    private javax.swing.JLabel jLabelVpnTyp;
    private javax.swing.JLabel jLabelVpnValidUntil;
    private javax.swing.JLayeredPane jLayeredPane1;
    private javax.swing.JPanel jMapPanel;
    private javax.swing.JPanel jMenuPanel;
    private javax.swing.ButtonGroup jNetworkTransportType;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanelConnect;
    private javax.swing.JPanel jPanelMap;
    private javax.swing.JPanel jPanelServerList;
    private javax.swing.JPanel jPanelUsa;
    private javax.swing.JLabel jPremiumButtonLabel;
    private javax.swing.JLabel jPremiumButtonLabel1;
    private javax.swing.JRadioButton jRadioTcp;
    private javax.swing.JRadioButton jRadioUdp;
    private javax.swing.JScrollBar jScrollBar1;
    private javax.swing.JScrollPane jScrollPane;
    private javax.swing.JPanel jServerListPanel;
    private javax.swing.JTable jServerListTable;
    private javax.swing.JCheckBox jShowOwnPosition;
    private javax.swing.JLabel jUpgradeButtonLabel;
    private javax.swing.JLabel jUpgradeButtonLabel1;
    private javax.swing.JPanel jUsaPanel;
    private org.jdesktop.swingx.JXMapKit jXMapKit1;
    // End of variables declaration//GEN-END:variables
	private Image iconIdleSmall;
	private Image iconConnectingSmall;
	private Image iconConnectedSmall;
	private Image buttonDisconnect;
	private Image buttonConnect;
  private IConsole vpnConsole;

	private void initContent() {
		this.content = new ContentPaneList();

		ContentPane connectPane = new ContentPane("button-connect", jButtonConnect, jConnectPanel, jLabelConnectHeader, jLabelConnectFooter);
		content.addPane(ContentPaneType.Connect, connectPane);

		ContentPane serverListPane = new ContentPane("button-serverlist", jButtonServerList, jServerListPanel, jLabelServerListHeader, jLabelServerListFooter);
		content.addPane(ContentPaneType.ServerList, serverListPane);

		ContentPane usaPane = new ContentPane("button-usa", jButtonUsa, jUsaPanel, jLabelUsaHeader, jLabelUsaFooter);
		content.addPane(ContentPaneType.Usa, usaPane);

		ContentPane mapPane = new ContentPane("button-map", jButtonMap, jMapPanel, jLabelMapHeader, jLabelMapFooter);
		content.addPane(ContentPaneType.Map, mapPane);

		content.setActivePane(ContentPaneType.Connect);
		content.setHoveredPane(ContentPaneType.None);

		this.updateButtons();
		this.updateContentPanes();

		this.serverList = this.shellfireService.getServerList();
	
		this.initServerTable();
		this.initMap();
		this.jConnectionStateIcon.setIcon(new ImageIcon(this.iconIdleSmall));

		String localHost = this.shellfireService.getLocalIpAddress();
		
		this.updateOnlineHost();
		this.updateLoginDetail();
	}

   class MyDrawPanel extends JPanel {

        Image origImage = new ImageIcon(getClass().getResource("/de/shellfire/vpn/gui/resources/map-grey-big.png")).getImage();
        BufferedImage scaledImage;
        
        public MyDrawPanel() {
        	updateScaledImage();
        	
        	addComponentListener(new ComponentAdapter() {
    			public void componentResized(ComponentEvent e) {
    				updateScaledImage();
    			}
        	});
        }
        private void updateScaledImage() {
        	if (getHeight() > 0 && getWidth() > 0) {
        		scaledImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = (Graphics2D)scaledImage.createGraphics();
                g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
                boolean b = g2d.drawImage(origImage, 0, 0, getWidth(), getHeight(), null);
        	}
        	
        }

        @Override
        public void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.drawImage(scaledImage, 0, 0, this);
        }

        
    }


	private void updateOnlineHost() {
		SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
			protected String doInBackground() throws Exception {
				String host = shellfireService.getLocalIpAddress();
				return host;
			}

			public void done() {
				try {
					String host = get();
					jLabelOnlineHost.setText(host);
				} catch (Exception e) {
					Util.handleException(e);
				}

			}
		};

		worker.execute();
	}

	private void updateButtons() {
		this.content.updateButtons();
	}

	private void setHoverStartFromMouseEnteredEvent(MouseEvent evt) {
		JLabel button = (JLabel) evt.getComponent();
		content.setHoveredPane(button);
		this.updateButtons();
	}

	private void setHoverEnd() {
		content.setHoveredPane(ContentPaneType.None);
		this.updateButtons();
	}

	private void updateContentPanes() {
		this.content.updateContentPanes();
	}

	private void setActiveContentFromMouseEnteredEvent(MouseEvent evt) {
		JLabel button = (JLabel) evt.getComponent();
		content.setActivePane(button);
		this.updateContentPanes();
		this.updateButtons();
	}

	private void initServerTable() {
		serverListTableModel = new ServerListTableModel(this.serverList);
		this.jServerListTable.setModel(serverListTableModel);
		TableColumnModel cm = this.jServerListTable.getColumnModel();

		DefaultTableCellRenderer defaultRenderer = new DefaultTableCellRenderer() {
			JLabel lbl = new JLabel();

			public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column) {
				setEnabled(table == null || table.isEnabled());

				lbl.setOpaque(true);
			    if (isEnabled()) {
			    	lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ContentPane.colorLightGray));
			    } else {
			    	lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ContentPane.colorVeryLightGray));
			    }
			    
				if (selected) {
					lbl.setBackground(table.getSelectionBackground());
					if (isEnabled()) {
						lbl.setForeground(ContentPane.colorDarkGrey);
					} else {
						lbl.setForeground(Color.darkGray);
					}

				} else {
					if (isEnabled()) {
						lbl.setBackground(table.getBackground());
						lbl.setForeground(table.getForeground());
					} else {
						lbl.setBackground(Color.white);
						lbl.setForeground(Color.lightGray);
					}
				}

				String text = (value == null) ? "" : value.toString();

				lbl.setText(text);
				lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN, 11));
				lbl.setHorizontalAlignment(JLabel.LEFT);
				
				return lbl;
			}

		};
		
		for (int row = 0; row < jServerListTable.getRowCount(); row++)
        {
			jServerListTable.setRowHeight(25);
        }
		jServerListTable.setBorder(null);
		jScrollPane.setBorder(null);
		cm.getColumn(0).setCellRenderer(new CountryImageRenderer());
		cm.getColumn(0).setPreferredWidth(70);
		cm.getColumn(1).setPreferredWidth(30);
		cm.getColumn(2).setPreferredWidth(45);
		cm.getColumn(3).setPreferredWidth(100);
		cm.getColumn(4).setPreferredWidth(170);

		cm.getColumn(1).setCellRenderer(defaultRenderer);
		cm.getColumn(2).setCellRenderer(defaultRenderer);
		cm.getColumn(3).setCellRenderer(defaultRenderer);

		cm.getColumn(4).setCellRenderer(new StarImageRenderer());
		cm.getColumn(3).setCellRenderer(new StarImageRenderer());

		this.jServerListTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jScrollPane.getViewport().setBackground(new Color(255, 255, 255));
		jServerListTable.setRowHeight(10,30);
		
		JTableHeader header = jServerListTable.getTableHeader();
		header.setReorderingAllowed(false);
		header.setBackground(ContentPane.colorDarkGrey);
		header.setOpaque(false);
	    header.setForeground(Color.white);
	    header.setFont(OpenSansFont.getFont());
	    System.out.println("Setting header preferred size to: " + cm.getTotalColumnWidth());
	    header.setPreferredSize(new Dimension(cm.getTotalColumnWidth(), 25));
	    header.setBorder(null);
	    
	    JPanel panel = new JPanel();
	    panel.setBackground(ContentPane.colorDarkGrey);
	    jScrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, panel);
	   
	}

	private void setLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
		}
	}

	ShellfireService getShellfireService() {
		return this.shellfireService;
	}

	public void setSelectedProtocol(VpnProtocol protocol) {
		if (protocol == null)
			protocol = VpnProtocol.UDP;

		switch (protocol) {
		case UDP:
			this.jNetworkTransportType.setSelected(jRadioUdp.getModel(), true);
			break;
		case TCP:
			this.jNetworkTransportType.setSelected(jRadioTcp.getModel(), true);
			break;
		}

	}

	public VpnProtocol getSelectedProtocol() {
		if (this.jRadioUdp.isSelected()) {
			return VpnProtocol.UDP;
		} else if (this.jRadioTcp.isSelected()) {
			return VpnProtocol.TCP;
		}

		return null;
	}

	public void afterLogin(boolean autoConnect) {
		Vpn vpn = this.shellfireService.getVpn();

		if (ProxyConfig.isProxyEnabled()) {
			this.setSelectedProtocol(VpnProtocol.TCP);
			this.jRadioUdp.setEnabled(false);
		} else {
			VpnProtocol selectedProtocol = vpn.getProtocol();
			this.setSelectedProtocol(selectedProtocol);
		}

		Server server = vpn.getServer();
		int row = this.serverListTableModel.getRowForServer(server);

		if (row != -1) {
			this.jServerListTable.addRowSelectionInterval(row, row);
		}

		if (autoConnect) {
			this.connectFromButton(false);
		}

	}

	private void initController() {
		if (this.controller == null) {
			this.controller = Controller.getInstance(this, this.shellfireService);
			this.controller.registerConnectionStateListener(this);
		}
	}

	public JTable getServerListTable() {
		return this.jServerListTable;
	}

	/**
	 * this is called when the connection state has been updated
	 * @throws RemoteException 
	 */
	public void connectionStateChanged(ConnectionStateChangedEvent e) throws RemoteException {
		initController();
		ConnectionState state = e.getConnectionState();
		vpnConsole.append("connectionStateChanged " + state + ", reason=" + e.getReason());
		switch (state) {
		case Disconnected:
			this.setStateDisconnected();
			break;
		case Connecting:
			this.setStateConnecting();
			break;
		case Connected:
			this.setStateConnected();
			break;
		}

	}
	public void setImage(final Image newImage)
    {
		if (newImage != null) {
			SwingUtilities.invokeLater(new Runnable()
		      {
		        public void run()
		        {
		       		//ShellfireVPNMainForm.this.image = newImage.getScaledInstance(jMediaPanel.getWidth(), jMediaPanel.getHeight(), Image.SCALE_DEFAULT);;
		       		ShellfireVPNMainForm.this.image = newImage;
		       		//jMediaPanel.repaint();
		        }
		      });
		}
      
    }
      
	
	private void setStateDisconnected() throws RemoteException {
	  vpnConsole.append("setStateDisconnected() - start");
		enableSystemProxyIfProxyConfig();
		this.hideConnectProgress();
		this.jConnectButtonLabel.setIcon(new ImageIcon(buttonConnect));
		this.jConnectButtonLabel1.setIcon(new ImageIcon(buttonConnect));
		
		this.jConnectButtonLabel.setEnabled(true);
		this.jConnectButtonLabel1.setEnabled(true);
		this.jLabelConnectionState.setText(i18n.tr("Nicht verbunden"));
		mySetIconImage(iconDisconnected);
		this.jConnectionStateIcon.setIcon(new ImageIcon(this.iconIdleSmall));
		this.jConnectionStateImage.setIcon(new ImageIcon(this.iconEcncryptionInactive));
		
		
		this.jShowOwnPosition.setEnabled(true);

		boolean showMessage = false;
		String message = "";
		if (this.controller != null) {
			switch (this.controller.getReasonForStateChange()) {
			case PasswordWrong:
				showMessage = true;
				message = i18n.tr("Passwort Falsch");
				break;
			case NotEnoughPrivileges:
				showMessage = true;
				message = i18n.tr("Prozess wird ohne Administrator-Rechte ausgeführt.");
				break;
			case CertificateFailed:
				showMessage = true;
				message = i18n.tr("Unbekannter Zertifikate-Fehler");
				break;
			case AllTapInUse:
				showMessage = true;
				message = i18n.tr("Alle Tap-Geräte in Verwendung. Bitte alle openvpn.exe Prozesse im Task Manager schließen oder PC neu starten.");
				break;
			case DisconnectDetected:
				showMessage = true;
				message = i18n.tr("Verbindung wurde unterbrochen.");
				break;
			case OpenVpnNotFound:
				showMessage = true;
				message = i18n.tr("OpenVPN Installation wurde nicht gefunden. Bitte Shellfire VPN neu installieren.");
				break;
			case NoOpenVpnParameters:
				showMessage = true;
				message = i18n.tr("OpenVPN Startparameter konnten nicht geladen werden - Bitte überprüfe deine Internet-Verbindung.");
				break;
			case TapDriverTooOld:
				showMessage = true;
				message = i18n.tr("Der installierte Tap Treiber ist zu alt. Bitte installiere Shellfire VPN neu.");
				break;
      case TapDriverNotFound:
        showMessage = true;
        message = i18n.tr("Es wurde kein Tap Treiber installiert. Bitte installiere Shellfire VPN neu.");
        break;
      case TapDriverNotFoundPleaseRetry:
        connectFromButton(true);
        break;
			case GatewayRedirectFailed:
				showMessage = true;
				message = i18n
						.tr("Das Gateway konnte nicht umgeleitet werden. Bitte bei den TCP/IP Einstellungen der aktuellen Netzwerkverbindung ein Gateway einstellen.");
				break;
			case UnknownOpenVPNError:
				showMessage = true;
				message = i18n
						.tr("Es ist ein unbekannter Fehler mit der VPN Verbindung aufgetreten. Bitte versuche einen Reboot und/oder Shellfire VPN neu zu installieren.");
				break;

			default:
				break;
			}
			
			vpnConsole.append("setStateDisconnected() - end");
		}

		if (showMessage) {
			JOptionPane.showMessageDialog(null, message, "Fehler: Verbindung fehlgeschlagen", JOptionPane.ERROR_MESSAGE);

			if (this.trayIcon != null) {
				this.trayIcon.setImage(this.iconDisconnected);
			}
		} else {
			if (this.trayIcon != null) {
				this.trayIcon.setImage(this.iconIdle);
			}
		}

		this.stopConnectedSinceTimer();

		this.setNormalCursor();
		this.updateOnlineHost();
		this.mapController.updateMap();
		popupConnectItem.setLabel(i18n.tr("Verbinden"));
		popupConnectItem.setEnabled(true);
		jServerListTable.setEnabled(true);
		if (!ProxyConfig.isProxyEnabled()) {
			this.jRadioUdp.setEnabled(true);
		}
		jRadioTcp.setEnabled(true);

		jScrollPane.getViewport().setBackground(Color.white);

		SwingWorker<Reason, Void> worker = new SwingWorker<Reason, Void>() {
			protected Reason doInBackground() throws Exception {
				Reason reasonForChange = controller.getReasonForStateChange();
				return reasonForChange;
			}

			public void done() {
				try {
					Reason reasonForChange = get();
					if (reasonForChange == Reason.DisconnectButtonPressed || reasonForChange == Reason.DisconnectDetected) {

						showTrayMessageWithoutCallback(i18n.tr("Verbindung getrennt"),
								i18n.tr("Shellfire VPN Verbindung getrennt. Deine Internet-Verbindung ist nicht mehr geschützt!"));
					}
				} catch (Exception e) {
					Util.handleException(e);
				}

			}
		};

		worker.execute();

	}

	private void showTrayMessageWithoutCallback(String header, String content) {
		// TrayMessage tm = new TrayMessage(header, content);
		// tm.run();
		if (Util.isWindows()) {
			trayIcon.displayMessage(header, content, MessageType.INFO);
		} else {
			// Mac OS has a stupid OK button that we dont want our users to
			// click on, so fall back to the custom SimpleTrayNotifiy
			new VpnTrayMessage(header, content).run();
		}

	}

	private void setStateConnecting() {
		this.showConnectProgress();
		this.jConnectButtonLabel.setIcon(new ImageIcon(buttonConnect));
		this.jConnectButtonLabel1.setIcon(new ImageIcon(buttonConnect));
		this.jConnectButtonLabel.setEnabled(false);
		this.jConnectButtonLabel1.setEnabled(false);

		if (!this.jShowOwnPosition.isSelected())
			this.jShowOwnPosition.setEnabled(false);

		this.jLabelConnectionState.setText(i18n.tr("Verbindung wird hergestellt..."));
		mySetIconImage(iconConnecting);
		
		if (this.trayIcon != null) {
			this.trayIcon.setImage(this.iconConnecting);
		}
		this.setWaitCursor();
		this.jConnectionStateIcon.setIcon(new ImageIcon(this.iconConnectingSmall));

		popupConnectItem.setLabel(i18n.tr("Verbinde..."));
		popupConnectItem.setEnabled(false);
		jServerListTable.setEnabled(false);
		jScrollPane.getViewport().setBackground(Color.lightGray);
		jRadioUdp.setEnabled(false);
		jRadioTcp.setEnabled(false);

	}

	private void setStateConnected() throws RemoteException {
		this.hideConnectProgress();
		this.jConnectButtonLabel.setIcon(new ImageIcon(buttonDisconnect));
		this.jConnectButtonLabel1.setIcon(new ImageIcon(buttonDisconnect));
		this.jConnectButtonLabel.setEnabled(true);
		this.jConnectButtonLabel1.setEnabled(true);

		if (!this.jShowOwnPosition.isSelected())
			this.jShowOwnPosition.setEnabled(false);

		this.jLabelConnectionState.setText(i18n.tr("Verbunden"));
		
		mySetIconImage(iconConnected);
		this.jConnectionStateImage.setIcon(new ImageIcon(this.iconEcncryptionActive));
		
		if (this.trayIcon != null) {
			this.trayIcon.setImage(this.iconConnected);
		}

		this.setNormalCursor();
		this.jConnectionStateIcon.setIcon(new ImageIcon(this.iconConnectedSmall));
		

		this.startConnectedSinceTimer();

		this.updateOnlineHost();

		this.mapController.updateMap();

		popupConnectItem.setLabel(i18n.tr("Verbindung trennen"));
		popupConnectItem.setEnabled(true);

		jServerListTable.setEnabled(false);
		jScrollPane.getViewport().setBackground(Color.lightGray);
		jRadioUdp.setEnabled(false);
		jRadioTcp.setEnabled(false);

		showTrayMessageWithoutCallback(i18n.tr("Verbindung Erfolgreich"),
				i18n.tr("Du bist jetzt mit Shellfire VPN verbunden. Deine Internet-Verbindung ist verschlüsselt."));

		showStatusUrlIfEnabled();

		disableSystemProxyIfProxyConfig();
	}

	private void disableSystemProxyIfProxyConfig() throws RemoteException {
		if (ProxyConfig.isProxyEnabled()) {
		  Connection.disableSystemProxy();
		}
	}

	private void enableSystemProxyIfProxyConfig() throws RemoteException {
		if (ProxyConfig.isProxyEnabled()) {
		  Connection.enableSystemProxy();
		}

	}

	private void showStatusUrlIfEnabled() {
		if (showStatusUrl())
			Util.openUrl(shellfireService.getUrlSuccesfulConnect());

	}

	private boolean showStatusUrl() {
		Preferences prefs = this.getPreferences();

		boolean showStatus = prefs.getBoolean(LoginForm.REG_SHOWSTATUSURL, false);
		return showStatus;
	}

	private void initTray() {
		if (!Util.isWindows()) {
			jLabelHide.setVisible(false);

		}

		if (SystemTray.isSupported()) {

			SystemTray tray = SystemTray.getSystemTray();
			Image image = this.iconIdle;

			ActionListener exitListener = new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					exitHandler();
				}
			};

			popup = new PopupMenu();
			MenuItem defaultItem = new MenuItem(i18n.tr("Beenden"));
			defaultItem.addActionListener(exitListener);

			ActionListener nagListener = new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					showNagScreenWithoutTimer();
				}
			};

			MenuItem nagItem = new MenuItem(i18n.tr("Shellfire VPN Premium Infos"));
			nagItem.addActionListener(nagListener);

			ActionListener helpListener = new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					openHelp();
				}
			};

			MenuItem helpItem = new MenuItem(i18n.tr("Hilfe"));
			helpItem.addActionListener(helpListener);

			ActionListener popupConnectListener = new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					connectFromButton(false);
				}
			};

			popupConnectItem = new MenuItem(i18n.tr("Verbinden"));
			popupConnectItem.addActionListener(popupConnectListener);

			ActionListener statusListener = new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					Util.openUrl(shellfireService.getUrlSuccesfulConnect());
				}
			};

			MenuItem statusItem = new MenuItem(i18n.tr("Zeige VPN Status im Browser"));
			statusItem.addActionListener(statusListener);

			ActionListener openListener = new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					setVisible(true);
					toFront();
					setState(Frame.NORMAL);

					if (!Util.isWindows()) {
						com.apple.eawt.Application app = com.apple.eawt.Application.getApplication();
						app.requestForeground(true);
					}
				}
			};

			MenuItem openItem = new MenuItem(i18n.tr("Shellfire VPN in den Vordergrund"));
			openItem.addActionListener(openListener);
			popup = new PopupMenu();
			popup.add(openItem);
			popup.add(popupConnectItem);
			popup.add(statusItem);
			popup.add(helpItem);
			popup.add(nagItem);
			popup.add(defaultItem);

			ActionListener actionListener = new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					setVisible(true);
					toFront();
					setState(Frame.NORMAL);
				}
			};

			MouseListener mouseListener = new MouseListener() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 2) {
						setVisible(true);
						toFront();
						setState(Frame.NORMAL);

						if (!Util.isWindows()) {
							com.apple.eawt.Application app = com.apple.eawt.Application.getApplication();
							app.requestForeground(true);
						}
					}

				}

				public void mouseReleased(MouseEvent e) {
				}

				public void mouseEntered(MouseEvent e) {
				}

				public void mouseExited(MouseEvent e) {
				}

				public void mousePressed(MouseEvent e) {
				}

			};

			trayIcon = new TrayIcon(image, "Shellfire VPN", popup);
			trayIcon.setImageAutoSize(true);
			trayIcon.addActionListener(actionListener);
			trayIcon.addMouseListener(mouseListener);

			startNagScreenTimer();

			try {
				tray.add(trayIcon);
			} catch (AWTException e) {
				System.err.println("TrayIcon could not be added.");
			}

			pack();
		}
	}

	private void setWaitCursor() {
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}

	private void setNormalCursor() {
		this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	private void initMap() {
		this.mapController = new MapController(this.jXMapKit1, this.controller);
		this.mapController.setServers(serverList);
		boolean showOwnPositionOnMap = this.getShowPositionOnMap();
		this.mapController.setShowOwnPositionOnMap(showOwnPositionOnMap);

		// use the middle of the atlatnci ocean so that Europe and America are
		// both visible by default
		GeoPosition someWhereInTheAtlantic = new GeoPosition(46.352995, -36.591800);
		jXMapKit1.setCenterPosition(someWhereInTheAtlantic);
		jXMapKit1.setZoom(15);

		jShowOwnPosition.setSelected(showOwnPositionOnMap);
		jShowOwnPositionActionPerformed(null);
	}

	public Server getSelectedServer() {
		int serverNum = this.jServerListTable.getSelectedRow();
		Server server = this.shellfireService.getServerList().getServer(serverNum);
		System.out.println("getSelectedServer() - returning: " + server);
		return server;
	}

	public void setSelectedServer(Server server) {
		System.out.println("setSelectedServer(" + server + ")");
		int num = this.shellfireService.getServerList().getServerNumberByServer(server);
		this.jServerListTable.setRowSelectionInterval(num, num);

	}

	private void loadIcons() {
		this.iconIdleSmall = new ImageIcon(getClass().getResource("/de/shellfire/vpn/gui/resources/small-globe-disconnected.png")).getImage();
		this.iconIdle = new ImageIcon(getClass().getResource("/de/shellfire/vpn/gui/resources/sfvpn2-idle-big.png")).getImage();
		
		this.iconConnectingSmall = new ImageIcon(getClass().getResource("/de/shellfire/vpn/gui/resources/small-globe-connecting.png")).getImage();
		this.iconConnecting = new ImageIcon(getClass().getResource("/de/shellfire/vpn/gui/resources/sfvpn2-connecting-big.png")).getImage();
		
		this.iconConnectedSmall = new ImageIcon(getClass().getResource("/de/shellfire/vpn/gui/resources/small-globe-connected.png")).getImage();
		this.iconConnected = new ImageIcon(getClass().getResource("/de/shellfire/vpn/gui/resources/sfvpn2-connected-big.png")).getImage();
		
		this.iconDisconnected = new ImageIcon(getClass().getResource("/de/shellfire/vpn/gui/resources/sfvpn2-disconnected-big.png")).getImage();
		
		this.iconEcncryptionActive = new ImageIcon(getClass().getResource("/de/shellfire/vpn/gui/resources/status-encrypted-width736.gif")).getImage();
		this.iconEcncryptionInactive = new ImageIcon(getClass().getResource("/de/shellfire/vpn/gui/resources/status-unencrypted-width736.gif")).getImage();
		
		String langKey = VpnI18N.getLanguage().getKey();
		System.out.println("langKey: " + langKey);
		this.buttonDisconnect = new ImageIcon(getClass().getResource("/de/shellfire/vpn/gui/resources/button-disconnect-" + langKey + ".gif")).getImage();
		this.buttonConnect = new ImageIcon(getClass().getResource("/de/shellfire/vpn/gui/resources/button-connect-" + langKey + ".gif")).getImage();
		
		mySetIconImage(iconIdle);
	}

	private Preferences getPreferences() {

		if (preferences == null) {
			preferences = Preferences.userNodeForPackage(this.getClass());
		}

		return preferences;

	}

	private boolean getShowPositionOnMap() {
		Preferences prefs = this.getPreferences();

		boolean showOnMap = prefs.getBoolean(REG_SHOWONMAP, false);
		return showOnMap;
	}

	private void setShowPositionOnMap(boolean showOnMap) {
		Preferences prefs = this.getPreferences();

		prefs.putBoolean(REG_SHOWONMAP, showOnMap);

		this.mapController.setShowOwnPositionOnMap(showOnMap);
	}

	private void showOwnPositionOnMap() {
		if (this.ownPosition == null) {
			this.ownPosition = this.shellfireService.getOwnPosition();
		}

		this.mapController.setOwnPosition(this.ownPosition);

		this.setShowPositionOnMap(true);
		return;
	}

	private void hideOwnPositionOnMap() {
		this.setShowPositionOnMap(false);
	}

	private void openUsTvStream(ActionEvent evt) {
		JButton pressedButton = (JButton) evt.getSource();
		String address = "http://" + pressedButton.getText();

		Util.openUrl(address);
	}

	private void updateLoginDetail() {
		Vpn vpn = this.shellfireService.getVpn();
		this.jLabelVpnId.setText("sf" + vpn.getVpnId());
		this.jLabelVpnTyp.setText(vpn.getAccountType().toString());

		if (vpn.getAccountType() == ServerType.Free) {
			this.jLabelVpnValidUntil.setVisible(false);
			this.jLabelValidUntilDesc.setVisible(false);
		} else {

			this.jLabelValidUntilDesc.setVisible(true);
			this.jLabelVpnValidUntil.setVisible(true);

			SimpleDateFormat df = new SimpleDateFormat(i18n.tr("d.MM.yyyy"), VpnI18N.getLanguage().getLocale());
			String date = df.format(vpn.getPremiumUntil());

			this.jLabelVpnValidUntil.setText(date);
		}
	}

	private void showSettingsDialog() {
		new SettingsDialog(this, true);

	}

	@Override
	public void localeChanged(LocaleChangeEvent lce) {
		JOptionPane.showMessageDialog(null, i18n.tr("Geänderte Spracheinstellungen werden erst nach einem Neustart von Shellfire VPN aktiv.",
				"Geänderte Sprache", JOptionPane.INFORMATION_MESSAGE));
	}

	private void stopConnectedSinceTimer() {
		if (this.currentConnectedSinceTimer != null) {
			this.currentConnectedSinceTimer.stop();
			this.currentConnectedSinceTimer = null;
		}
	}

	public void updateConnectedSince() {
		Date now = new Date();
		long diffInSeconds = (now.getTime() - connectedSince.getTime()) / 1000;

		long diff[] = new long[] { 0, 0, 0, 0 };
		diff[3] = (diffInSeconds >= 60 ? diffInSeconds % 60 : diffInSeconds);
		diff[2] = (diffInSeconds = (diffInSeconds / 60)) >= 60 ? diffInSeconds % 60 : diffInSeconds;
		diff[1] = (diffInSeconds = (diffInSeconds / 60));
		String since = String.format("%dh %dm %ds", diff[1], diff[2], diff[3]);

		SimpleDateFormat df = new SimpleDateFormat("E, H:m", VpnI18N.getLanguage().getLocale());
		String start = df.format(connectedSince);
		String text = start + " " + "(" + since + ")";
		jConnectedSince.setText(text);
	}

	private void startConnectedSinceTimer() {
		int delay = 1000; // milliseconds
		connectedSince = new Date();

		ActionListener taskPerformer = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				updateConnectedSince();
			}
		};

		this.currentConnectedSinceTimer = new Timer(delay, taskPerformer);
		this.currentConnectedSinceTimer.setRepeats(true);
		this.currentConnectedSinceTimer.start();
	}

	private void startNagScreenTimer() {
		int oneHour = 1000 * 60 * 60;

		ActionListener taskPerformer = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
          showTrayIconNagScreen();
        } catch (RemoteException e) {
          Util.handleException(e);
        }
			}
		};

		Timer nagScreenTimer = new Timer(oneHour, taskPerformer);
		nagScreenTimer.setRepeats(true);
		nagScreenTimer.start();
	}

	private void showTrayIconNagScreen() throws RemoteException {
		LinkedList<VpnTrayMessage> messages = new LinkedList<VpnTrayMessage>();
		if (controller.getCurrentConnectionState() == ConnectionState.Connected) {
			ActionListener premiumInfoClicked = new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					showNagScreenWithoutTimer();
				}
			};

			if (this.shellfireService.getVpn().getAccountType() == ServerType.Free) {
				List<TrayMessage> trayMessages = this.shellfireService.getTrayMessages();

				for (TrayMessage msg : trayMessages) {
					messages.add(new VpnTrayMessage(msg.getHeader(), msg.getText(), msg.getButtontext(), premiumInfoClicked));
				}
			}
		} else {
			messages.add(new VpnTrayMessage(i18n.tr("Nicht verbunden"), i18n.tr("Du bist nicht zu Shellfire VPN verbunden.")));
		}

		if (messages.size() > 0) {
			Random generator = new Random((new Date()).getTime());
			int num = generator.nextInt(messages.size());
			VpnTrayMessage msgToShow = messages.get(num);
			msgToShow.run();
		}

	}

	private void askForDisconnectedAndQuit() {
		int result = JOptionPane.showConfirmDialog(null, i18n.tr("Verbindung trennen und Shellfire VPN schließen?"), i18n.tr("Verbindung besteht"),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

  		if (result == JOptionPane.OK_OPTION) {
  	    try {
    			this.controller.disconnect(Reason.ApplicationExit);
    			enableSystemProxyIfProxyConfig();
    			System.exit(0);
  	    } catch (RemoteException e) {
  	      Util.handleException(e);
  	      System.exit(0);
  	    }
  		}
	}
	
	private void exitHandler() {
		boolean connected;

    try {
      connected = this.controller.getCurrentConnectionState() != ConnectionState.Disconnected;
  		if (connected) {
  			askForDisconnectedAndQuit();
  		} else {
  		    enableSystemProxyIfProxyConfig();  
  			System.exit(0);
  		}
    } catch (RemoteException e) {
      Util.handleException(e);
      System.exit(0);
    }
		
	}

	private void showNagScreenWithoutTimer() {
		if (nagScreen == null) {

			nagScreen = new PremiumVPNNagScreen(this, true, new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {

					if (nagScreen != null) {
						nagScreen.dispose();
						nagScreen = null;
					}
					setNormalCursor();

				}
			});
			nagScreen.disableTimer();
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					nagScreen.setVisible(true);
				}
			});

		}

	}

	private void delayedConnect(Server selectedServer, VpnProtocol protocol, Reason reason) {
		popupConnectItem.setLabel(i18n.tr("Verbinde..."));
		popupConnectItem.setEnabled(false);

		nagScreen = new PremiumVPNNagScreen(this, true, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (nagScreentimer != null) {
					nagScreentimer.stop();
					nagScreentimer = null;
					try {
            controller.disconnect(Reason.DisconnectButtonPressed);
          } catch (RemoteException e1) {
           Util.handleException(e1);
          }
				}
				if (nagScreen != null) {
					nagScreen.dispose();
					nagScreen = null;
				}

				popupConnectItem.setLabel(i18n.tr("Verbinden"));
				popupConnectItem.setEnabled(true);
				setNormalCursor();
			}
		});

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				if (nagScreen != null) {
					nagScreen.setAlwaysOnTop(true);
					nagScreen.setVisible(true);

				}
			}
		});
		nagScreenDelay = 25;

		nagScreentimer = new Timer(1000, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (nagScreenDelay == -1 && nagScreen != null) {
					nagScreen.setVisible(false);
					nagScreen.dispose();
					nagScreen = null;
					Timer t = (Timer) e.getSource();
					t.stop();
					controller.connect(getSelectedServer(), getSelectedProtocol(), Reason.ConnectButtonPressed);
				}
				if (nagScreen != null)
					nagScreen.setDelay(nagScreenDelay--);
			}
		});

		nagScreentimer.setRepeats(true);
		nagScreentimer.setInitialDelay(0);
		nagScreentimer.start();

	}

	private void enableMouseMoveListener() {

		if (mml == null) {
			mml = new MoveMouseListener(this);
		}

		this.addMouseListener(mml);
		this.addMouseMotionListener(mml);

	}


  private void getConsole() {
    this.vpnConsole = VpnConsole.getInstance();
  }
	
	private void initConsole() {
		if (Storage.get(VpnConsole.class) == null || ((VpnConsole) Storage.get(VpnConsole.class)).isVisible() == false) {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					vpnConsole = VpnConsole.getInstance();
					vpnConsole.setVisible(true);
				}
			});
		}

	}

	private void initShortCuts() {
		EventQueue ev = Toolkit.getDefaultToolkit().getSystemEventQueue();

		ev.push(new EventQueue() {

			protected void dispatchEvent(AWTEvent event) {
				if (event instanceof KeyEvent) {

					final KeyEvent oKeyEvent = (KeyEvent) event;
					if (oKeyEvent.getID() == KeyEvent.KEY_PRESSED) {
						final int iKeyCode = oKeyEvent.getKeyCode();
						appendKey((char) iKeyCode);
					}
				}

				super.dispatchEvent(event);
			}
		});

	}

	private void appendKey(char c) {
		this.typedStrings.append(c);
		if (typedStrings.toString().toLowerCase().endsWith("showconsole")) {
			this.initConsole();
		}
	}

	private void initPremium() {
		if (!this.isFreeAccount()) {
			this.jPremiumButtonLabel.setVisible(false);
			this.jPremiumButtonLabel1.setVisible(false);
			this.jUpgradeButtonLabel.setVisible(false);
			this.jUpgradeButtonLabel1.setVisible(false);
		}
	}

	public static ImageIcon getLogo() {
		return ShellfireVPNMainForm.mainIconMap.get(VpnI18N.getLanguage().getKey());
	}

	public void mySetIconImage(Image img) {
		setIconImage(img);

		if (!Util.isWindows()) {
			com.apple.eawt.Application app = com.apple.eawt.Application.getApplication();
			app.setDockIconImage(img);
		}

	}

	private void showUpgradeDialog() {
		UpgradeDialog upgradeDialog = new UpgradeDialog(this, true);
		upgradeDialog.setVisible(true);

	}

	public Controller getController() {
		return this.controller;
	}

	private void initLayeredPaneSize() {
		jLayeredPane1.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				Rectangle r = jLayeredPane1.getBounds();

				jServerListPanel.setBounds(r);
				jConnectPanel.setBounds(r);
				jMapPanel.setBounds(r);
				jUsaPanel.setBounds(r);
			}

		});
	}

}
