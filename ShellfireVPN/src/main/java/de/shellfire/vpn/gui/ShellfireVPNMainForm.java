/* * To change this template, choose Tools | Templates
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
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
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.swingx.JXMapKit;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.LocaleChangeEvent;
import org.xnap.commons.i18n.LocaleChangeListener;

import de.shellfire.vpn.Storage;
import de.shellfire.vpn.Util;
import de.shellfire.vpn.client.Client;
import de.shellfire.vpn.client.ConnectionState;
import de.shellfire.vpn.client.ConnectionStateChangedEvent;
import de.shellfire.vpn.client.ConnectionStateListener;
import de.shellfire.vpn.client.Controller;
import de.shellfire.vpn.exception.VpnException;
import de.shellfire.vpn.gui.helper.MoveMouseListener;
import de.shellfire.vpn.gui.helper.OpenSansFont;
import de.shellfire.vpn.gui.helper.OxygenFont;
import de.shellfire.vpn.gui.helper.TitiliumFont;
import de.shellfire.vpn.gui.model.ContentPane;
import de.shellfire.vpn.gui.model.ContentPaneList;
import de.shellfire.vpn.gui.model.ContentPaneType;
import de.shellfire.vpn.gui.model.MapController;
import de.shellfire.vpn.gui.model.ServerListTableModel;
import de.shellfire.vpn.gui.renderer.CountryImageRenderer;
import de.shellfire.vpn.gui.renderer.StarImageRenderer;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.proxy.ProxyConfig;
import de.shellfire.vpn.types.VpnProtocol;
import de.shellfire.vpn.types.Reason;
import de.shellfire.vpn.types.Server;
import de.shellfire.vpn.types.ServerType;
import de.shellfire.vpn.webservice.ServerList;
import de.shellfire.vpn.webservice.Vpn;
import de.shellfire.vpn.webservice.WebService;
import de.shellfire.vpn.webservice.model.TrayMessage;
import de.shellfire.vpn.webservice.model.WsGeoPosition;
import net.miginfocom.swing.MigLayout;

/**
 * 
 * @author bettmenn
 */
public class ShellfireVPNMainForm extends javax.swing.JFrame implements LocaleChangeListener, ConnectionStateListener {
    private static  Logger log = Util.getLogger(ShellfireVPNMainForm.class.getCanonicalName());
    private static LogViewer logViewer = LogViewer.getInstance();
    private ContentPaneList content;
    private WebService shellfireService;
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
            put("de", Util.getImageIcon("/icons/sf.png"));
            put("en", Util.getImageIcon("/icons/sf_en.png"));
            put("fr", Util.getImageIcon("/icons/sf_fr.png"));
        }
    };

	private MenuItem popupConnectItem;
	private PopupMenu popup;

    /**
     * Creates new form
     */
    ShellfireVPNMainForm(WebService service) throws VpnException {
        if (!service.isLoggedIn()) {
            throw new VpnException("ShellfireVPN Main Form required a logged in service. This should not happen!");
        }

        log.debug("ShellfireVPNMainForm starting up");
        if (Util.isWindows()) {
            log.debug("Running on Windows " + Util.getOsVersion());

            if (Util.isVistaOrLater()) {
                log.debug("Running on Vista Or Later Version");
            } else {
                log.debug("Running on XP");
            }

        } else {
            log.debug("Running on Mac OS X " + Util.getOsVersion());
        }

        log.debug("System Architecture: " + Util.getArchitecture());

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

        this.initContent();
        Storage.register(this);

        this.initShortCuts();
        this.initPremium();
        this.initConnection();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        this.setLocationRelativeTo(null);
        setVisible(true);

    }

  private void initConnection() {
		new Thread() {
			public void run() {
          controller.getCurrentConnectionState();
			}
		}.start();
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
        jContentPanel.setBorder(null);
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
        jPanel1.setBorder(null);
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
        jUpgradeButtonLabel = new javax.swing.JLabel();
        jConnectButtonLabel = new javax.swing.JLabel();
        jPremiumButtonLabel = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jConnectionStateImage = new javax.swing.JLabel();
        jMapPanel = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jXMapKit1 = new JXMapKit();
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
        jMenuPanel.setBorder(null);
        jLabel5 = new javax.swing.JLabel();
        jPanelConnect = new javax.swing.JPanel();
        jPanelConnect.setBorder(null);
        
        jLabelConnectHeader = new javax.swing.JLabel();
        jLabelConnectFooter = new javax.swing.JLabel();
        jButtonConnect = new javax.swing.JLabel();
        jButtonConnect.setBorder(null);
        jPanelServerList = new javax.swing.JPanel();
        jPanelServerList.setBorder(null);
        jLabelServerListHeader = new javax.swing.JLabel();
        jLabelServerListFooter = new javax.swing.JLabel();
        jButtonServerList = new javax.swing.JLabel();
        jButtonServerList.setBorder(null);
        jPanelMap = new javax.swing.JPanel();
        jPanelMap.setBorder(null);
        jLabelMapHeader = new javax.swing.JLabel();
        jLabelMapFooter = new javax.swing.JLabel();
        jButtonMap = new javax.swing.JLabel();
        jButtonMap.setBorder(null);
        jPanelUsa = new javax.swing.JPanel();
        jPanelUsa.setBorder(null);
        jLabelUsaHeader = new javax.swing.JLabel();
        jLabelUsaFooter = new javax.swing.JLabel();
        jButtonUsa = new javax.swing.JLabel();
        jButtonUsa.setBorder(null);
        jHeaderPanel = new javax.swing.JPanel();
        jHeaderPanel.setBorder(null);
        jLabelHelp = new javax.swing.JLabel();
        jLabelExit = new javax.swing.JLabel();
        jLabelHide = new javax.swing.JLabel();
        jLabelSettings = new javax.swing.JLabel();
        jLabelMinimize = new javax.swing.JLabel();

        jScrollBar1.setName("jScrollBar1"); // NOI18N

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("de/shellfire/vpn/gui/resources/ShellfireVPNMainForm"); // NOI18N
        setTitle(bundle.getString("title")); // NOI18N
        ResourceMap resourceMap = Application.getInstance().getContext().getResourceMap(ShellfireVPNMainForm.class);
        setBackground(resourceMap.getColor("Form.background")); // NOI18N
        setName("Form"); // NOI18N

        jPanel5.setBackground(resourceMap.getColor("jPanel5.background")); // NOI18N
        jPanel5.setBorder(new javax.swing.border.LineBorder(resourceMap.getColor("jPanel5.border.lineColor"), 3, true)); // NOI18N
        jPanel5.setName("jPanel5"); // NOI18N

        jContentPanel.setBackground(resourceMap.getColor("jContentPanel.background")); // NOI18N
        jContentPanel.setName("jContentPanel"); // NOI18N

        jPanel2.setBackground(resourceMap.getColor("jPanel2.background")); // NOI18N
        jPanel2.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED, null, resourceMap.getColor("jPanel2.border.highlightInnerColor"), null, null)); // NOI18N
        jPanel2.setMinimumSize(new java.awt.Dimension(660, 101));
        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.setLayout(new MigLayout("hidemode 3", "[grow][grow][grow][grow][grow][grow][grow]", "[][]"));

        jConnectionStateIcon.setIcon(Util.getImageIcon("/icons/small-globe-disconnected.png")); // NOI18N
        jConnectionStateIcon.setText(resourceMap.getString("jConnectionStateIcon.text")); // NOI18N
        jConnectionStateIcon.setName("jConnectionStateIcon"); // NOI18N
        jPanel2.add(jConnectionStateIcon, "cell 0 0 1 3,alignx left,aligny top");

        
        jLabel2.setFont(new Font("Arial", Font.PLAIN, Util.getFontSize())); // NOI18N
        jLabel2.setForeground(resourceMap.getColor("jLabel2.foreground")); // NOI18N
        jLabel2.setText(i18n.tr("Connection status"));
        jLabel2.setName("jLabel2"); // NOI18N
        jPanel2.add(jLabel2, "cell 1 0,grow,aligny top");

        jLabelConnectionState.setFont(new Font("Arial", Font.PLAIN, Util.getFontSize())); // NOI18N
        jLabelConnectionState.setForeground(resourceMap.getColor("jLabelConnectionState.foreground")); // NOI18N
        jLabelConnectionState.setText(i18n.tr("Disconnected"));
        jLabelConnectionState.setName("jLabelConnectionState"); // NOI18N
        jPanel2.add(jLabelConnectionState, "cell 1 1,grow,aligny top");

        jLabel14.setFont(new Font("Arial", Font.PLAIN, Util.getFontSize())); // NOI18N
        jLabel14.setForeground(resourceMap.getColor("jLabelVpnId.foreground")); // NOI18N
        jLabel14.setText(i18n.tr("Connected since:"));
        jLabel14.setName("jLabel14"); // NOI18N
        jPanel2.add(jLabel14, "cell 2 0,growx,aligny top");

        jLabelOnlineHost.setFont(new Font("Arial", Font.PLAIN, Util.getFontSize())); // NOI18N
        jLabelOnlineHost.setForeground(resourceMap.getColor("jLabelVpnId.foreground")); // NOI18N
        jLabelOnlineHost.setText(resourceMap.getString("jLabelOnlineHost.text")); // NOI18N
        jLabelOnlineHost.setName("jLabelOnlineHost"); // NOI18N
        jPanel2.add(jLabelOnlineHost, "cell 3 1,growx,aligny top");

        jLabel15.setFont(new Font("Arial", Font.PLAIN, Util.getFontSize())); // NOI18N
        jLabel15.setForeground(resourceMap.getColor("jLabelVpnId.foreground")); // NOI18N
        jLabel15.setText(i18n.tr("Online IP:"));
        jLabel15.setName("jLabel15"); // NOI18N
        jPanel2.add(jLabel15, "cell 3 0,growx,aligny top");

        jConnectedSince.setFont(new Font("Arial", Font.PLAIN, Util.getFontSize())); // NOI18N
        jConnectedSince.setForeground(resourceMap.getColor("jLabelVpnId.foreground")); // NOI18N
        jConnectedSince.setText(i18n.tr("(not connected)"));
        jConnectedSince.setName("jConnectedSince"); // NOI18N
        jPanel2.add(jConnectedSince, "cell 2 1,growx,aligny top");

        jLabel17.setFont(new Font("Arial", Font.PLAIN, Util.getFontSize())); // NOI18N
        jLabel17.setForeground(resourceMap.getColor("jLabelVpnId.foreground")); // NOI18N
        jLabel17.setText(i18n.tr("VPN Id:"));
        jLabel17.setName("jLabel17"); // NOI18N
        jPanel2.add(jLabel17, "cell 4 0,growx,aligny top");

        jLabelVpnId.setFont(new Font("Arial", Font.PLAIN, Util.getFontSize())); // NOI18N
        jLabelVpnId.setForeground(resourceMap.getColor("jLabelVpnId.foreground")); // NOI18N
        jLabelVpnId.setText(resourceMap.getString("jLabelVpnId.text")); // NOI18N
        jLabelVpnId.setName("jLabelVpnId"); // NOI18N
        jPanel2.add(jLabelVpnId, "cell 4 1,growx,aligny top");

        jLabelValidUntilDesc.setFont(new Font("Arial", Font.PLAIN, Util.getFontSize())); // NOI18N
        jLabelValidUntilDesc.setForeground(resourceMap.getColor("jLabelVpnId.foreground")); // NOI18N
        jLabelValidUntilDesc.setText(i18n.tr("Valid Until:"));
        jLabelValidUntilDesc.setName("jLabelValidUntilDesc"); // NOI18N
        jPanel2.add(jLabelValidUntilDesc, "cell 6 0,growx,aligny top");

        jLabelVpnTyp.setFont(new Font("Arial", Font.PLAIN, Util.getFontSize())); // NOI18N
        jLabelVpnTyp.setForeground(resourceMap.getColor("jLabelVpnId.foreground")); // NOI18N
        jLabelVpnTyp.setText(resourceMap.getString("jLabelVpnTyp.text")); // NOI18N
        jLabelVpnTyp.setName("jLabelVpnTyp"); // NOI18N
        jPanel2.add(jLabelVpnTyp, "cell 5 1,growx,aligny top");

        jLabel19.setFont(new Font("Arial", Font.PLAIN, Util.getFontSize())); // NOI18N
        jLabel19.setForeground(resourceMap.getColor("jLabelVpnId.foreground")); // NOI18N
        jLabel19.setText(i18n.tr("VPN type:"));
        jLabel19.setName("jLabel19"); // NOI18N
        jPanel2.add(jLabel19, "cell 5 0,growx,aligny top");

        jLabelVpnValidUntil.setFont(new Font("Arial", Font.PLAIN, Util.getFontSize())); // NOI18N
        jLabelVpnValidUntil.setForeground(resourceMap.getColor("jLabelVpnId.foreground")); // NOI18N
        jLabelVpnValidUntil.setText(resourceMap.getString("jLabelVpnValidUntil.text")); // NOI18N
        jLabelVpnValidUntil.setName("jLabelVpnValidUntil"); // NOI18N
        jPanel2.add(jLabelVpnValidUntil, "cell 6 1,growx,aligny top");

        jPanel1.setName("jPanel1"); // NOI18N

        jServerListPanel.setBackground(resourceMap.getColor("jServerListPanel.background"));
        jServerListPanel.setName("jServerListPanel");

        jLabel11.setBackground(resourceMap.getColor("jLabel11.background")); // NOI18N
        jLabel11.setFont(OxygenFont.getFontLargeBold());
        jLabel11.setForeground(resourceMap.getColor("jLabel11.foreground")); // NOI18N
        jLabel11.setText("   " + i18n.tr("Select a Server for your connection"));
        jLabel11.setName("jLabel11"); // NOI18N
        jLabel11.setOpaque(true);

        jScrollPane.setBackground(resourceMap.getColor("jScrollPane.background")); // NOI18N
        jScrollPane.setName("jScrollPane"); // NOI18N
        jServerListTable.setBackground(resourceMap.getColor("jServerListTable.background")); // NOI18N
        jServerListTable.setFont(new Font("Arial", Font.PLAIN, Util.getFontSize())); // NOI18N
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
        jRadioUdp.setText(i18n.tr("UDP (fast)"));
        jRadioUdp.setToolTipText(i18n.tr("<html>Select UDP for a faster connection.<br /> You should only select TCP if you have problems connecting with UDP.</html>"));
        jRadioUdp.setContentAreaFilled(false);
        jRadioUdp.setName("jRadioUdp"); // NOI18N
        jRadioUdp.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jRadioUdpStateChanged(evt);
            }
        });

        jNetworkTransportType.add(jRadioTcp);
        jRadioTcp.setForeground(resourceMap.getColor("jRadioTcp.foreground")); // NOI18N
        jRadioTcp.setText(i18n.tr("TCP (works with safe firewalls and proxies.)"));
        jRadioTcp.setToolTipText(i18n.tr("<html>Select UDP for a faster connection.<br /> You should only select TCP if you have problems connecting with UDP o when using a proxy-server.</html>"));
        jRadioTcp.setContentAreaFilled(false);
        jRadioTcp.setName("jRadioTcp"); // NOI18N

        jLabel12.setFont(OxygenFont.getFontLargeBold());
        jLabel12.setForeground(resourceMap.getColor("jRadioTcp.foreground")); // NOI18N
        jLabel12.setText(i18n.tr("Connection type"));
        jLabel12.setToolTipText(i18n.tr("<html>Select UDP for a faster connection.<br /> You should only select TCP if you have problems connecting with UDP.</html>"));
        jLabel12.setName("jLabel12"); // NOI18N

        jUpgradeButtonLabel1.setIcon(Util.getImageIcon("/buttons/button-serial-de.gif")); // NOI18N
        jUpgradeButtonLabel1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jUpgradeButtonLabel1.setName("jUpgradeButtonLabel1"); // NOI18N
        jUpgradeButtonLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jUpgradeButtonLabel1MouseClicked(evt);
            }
        });

        jConnectButtonLabel1.setIcon(Util.getImageIcon("/buttons/button-connect-de.gif")); // NOI18N
        jConnectButtonLabel1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jConnectButtonLabel1.setName("jConnectButtonLabel1"); // NOI18N
        jConnectButtonLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jConnectButtonLabel1MouseClicked(evt);
            }
        });

        jPremiumButtonLabel1.setIcon(Util.getImageIcon("/buttons/button-connect-de.gif")); // NOI18N
        jPremiumButtonLabel1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jPremiumButtonLabel1.setName("jPremiumButtonLabel1"); // NOI18N
        jPremiumButtonLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPremiumButtonLabel1MouseClicked(evt);
            }
        });

        //jServerListPanel.setBounds(0, 0, 770, 490);
        jPanel1.add(jServerListPanel, "grow");
        jServerListPanel.setLayout(new MigLayout("hidemode 3", "[][][][]", "[]"));
        jServerListPanel.add(jScrollPane, "cell 0 1 7 1,grow");
        jServerListPanel.add(jLabel11, "cell 0 0 7 1,growx,aligny top");
        jServerListPanel.add(jLabel12, "cell 0 2 7 1,growx,aligny top");
        jServerListPanel.add(jRadioUdp, "cell 0 3,growx,aligny top");
        jServerListPanel.add(jRadioTcp, "cell 2 3 5 1,alignx left,aligny top");
        jServerListPanel.add(jConnectButtonLabel1, "cell 0 4 3 1,alignx left,aligny top");
        jServerListPanel.add(jUpgradeButtonLabel1, "cell 4 4,alignx left,aligny top");
        jServerListPanel.add(jPremiumButtonLabel1, "cell 6 4,alignx left,aligny top");

        jConnectPanel.setBackground(resourceMap.getColor("jConnectPanel.background")); // NOI18N
        jConnectPanel.setFocusable(false);
        jConnectPanel.setName("jConnectPanel");
        jConnectPanel.setLayout(new MigLayout("hidemode 3", "[grow][grow][grow]", "[grow][]"));

        
        jUpgradeButtonLabel.setIcon(Util.getImageIcon("/buttons/button-serial-de.gif")); // NOI18N
        jUpgradeButtonLabel.setText(resourceMap.getString("jUpgradeButtonLabel.text")); // NOI18N
        jUpgradeButtonLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jUpgradeButtonLabel.setName("jUpgradeButtonLabel"); // NOI18N
        jUpgradeButtonLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jUpgradeButtonLabelMouseClicked(evt);
            }
        });
        jConnectPanel.add(jUpgradeButtonLabel, "cell 1 1,alignx left,aligny top");

        
        jConnectButtonLabel.setIcon(Util.getImageIcon("/buttons/button-connect-de.gif")); // NOI18N
        jConnectButtonLabel.setText(resourceMap.getString("jConnectButtonLabel.text")); // NOI18N
        jConnectButtonLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jConnectButtonLabel.setName("jConnectButtonLabel"); // NOI18N
        jConnectButtonLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jConnectButtonLabelMouseClicked(evt);
            }
        });
        jConnectPanel.add(jConnectButtonLabel, "cell 0 1,alignx left,aligny top");

        jPremiumButtonLabel.setIcon(Util.getImageIcon("/buttons/button-premium-infos.gif")); // NOI18N
        jPremiumButtonLabel.setText(resourceMap.getString("jPremiumButtonLabel.text")); // NOI18N
        jPremiumButtonLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jPremiumButtonLabel.setName("jPremiumButtonLabel"); // NOI18N
        jPremiumButtonLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPremiumButtonLabelMouseClicked(evt);
            }
        });
        jConnectPanel.add(jPremiumButtonLabel, "cell 2 1,alignx left,aligny top");

        jPanel4.setName("jPanel4"); // NOI18N
        jConnectionStateImage.setName("jConnectionStateImage");

        jConnectPanel.add(jPanel4, "cell 0 0 3 1,alignx left,aligny top");
        jPanel4.setLayout(new MigLayout("hidemode 3, insets 0, gapx 0, gapy 0", "[grow]", "[grow]"));
        jPanel4.add(jConnectionStateImage, "cell 0 0,alignx left,aligny top");

        //jConnectPanel.setBounds(0, 0, 770, 490);
        jPanel1.add(jConnectPanel, "growx");
        

        jMapPanel.setBackground(resourceMap.getColor("jMapPanel.background")); // NOI18N
        jMapPanel.setName("jMapPanel"); // NOI18N

        jLabel13.setBackground(resourceMap.getColor("jLabel13.background")); // NOI18N
        jLabel13.setFont(OxygenFont.getFontLargeBold());
        jLabel13.setForeground(resourceMap.getColor("jLabel13.foreground")); // NOI18N
        jLabel13.setText("   " + i18n.tr("Server map"));
        jLabel13.setName("jLabel13"); // NOI18N
        jLabel13.setOpaque(true);

        jXMapKit1.setDefaultProvider(JXMapKit.DefaultProviders.OpenStreetMaps);
        jXMapKit1.setMiniMapVisible(false);
        jXMapKit1.setZoomButtonsVisible(false);
        jXMapKit1.setDataProviderCreditShown(true);
        jXMapKit1.setName("jXMapKit1"); // NOI18N
        jXMapKit1.setZoom(15);

        jShowOwnPosition.setForeground(resourceMap.getColor("jShowOwnPosition.foreground")); // NOI18N
        jShowOwnPosition.setText(i18n.tr("Show data route and your location (when connected)"));
        jShowOwnPosition.setContentAreaFilled(false);
        jShowOwnPosition.setName("jShowOwnPosition"); // NOI18N
        jShowOwnPosition.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jShowOwnPositionActionPerformed(evt);
            }
        });

        jGotoOwnLocation.setIcon(Util.getImageIcon("/icons/icon-home.png")); // NOI18N
        jGotoOwnLocation.setText(i18n.tr("go to your location"));
        jGotoOwnLocation.setEnabled(false);
        jGotoOwnLocation.setName("jGotoOwnLocation"); // NOI18N
        jGotoOwnLocation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jGotoOwnLocationActionPerformed(evt);
            }
        });

        //jMapPanel.setBounds(0, 0, 770, 490);
        
        jPanel1.add(jMapPanel, "growx");
        jMapPanel.setLayout(new MigLayout("hidemode 3", "[grow][grow][grow]", "[grow][grow][grow]"));
        jMapPanel.add(jShowOwnPosition, "cell 0 1,alignx left,aligny top");
        jMapPanel.add(jGotoOwnLocation, "cell 2 1,growx,aligny top");
        jMapPanel.add(jLabel13, "cell 0 0 3 1,growx,aligny top");
        jMapPanel.add(jXMapKit1, "cell 0 2 3 1,grow");

        jUsaPanel.setBackground(resourceMap.getColor("jUsaPanel.background")); // NOI18N
        jUsaPanel.setName("jUsaPanel"); // NOI18N

        jLabel16.setBackground(resourceMap.getColor("jLabel16.background")); // NOI18N
        jLabel16.setFont(OxygenFont.getFontLargeBold());
        jLabel16.setForeground(new java.awt.Color(255, 255, 255));
        jLabel16.setText("     " + i18n.tr("List of US TV streams (english language)"));
        jLabel16.setName("jLabel16"); // NOI18N
        jLabel16.setOpaque(true);

        jLabel10.setForeground(resourceMap.getColor("jLabel10.foreground")); // NOI18N
        jLabel10.setText(i18n.tr("<html> Using these streams usually requires a US IP address.<br>You can get one by connecting to a Shellfire VPN server located in the USA.</html>"));
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

        //jUsaPanel.setBounds(0, 0, 770, 490);
        jPanel1.add(jUsaPanel, "grow");
        jUsaPanel.setLayout(new MigLayout("hidemode 3", "[]", "[grow][grow][grow]"));
        jUsaPanel.add(jPanel3, "cell 0 1,growx,aligny top");
        jPanel3.setLayout(new MigLayout("hidemode 3, insets 0, gapx 0, gapy 0", "[grow]", "[grow][grow][grow][grow][grow][grow][grow][grow]"));
        jPanel3.add(jButton4, "cell 0 0,growx,aligny top");
        jPanel3.add(jButton5, "cell 0 1,growx,aligny top");
        jPanel3.add(jButton6, "cell 0 2,growx,aligny top");
        jPanel3.add(jButton7, "cell 0 3,growx,aligny top");
        jPanel3.add(jButton8, "cell 0 4,growx,aligny top");
        jPanel3.add(jButton9, "cell 0 5,growx,aligny top");
        jPanel3.add(jButton10, "cell 0 6,growx,aligny top");
        jPanel3.add(jButton11, "cell 0 7,growx,aligny top");
        jUsaPanel.add(jLabel16, "cell 0 0,alignx left,aligny top");
        jUsaPanel.add(jLabel10, "cell 0 2,grow");
        jPanel1.setLayout(new MigLayout("hidemode 3, insets 0, gapx 0, gapy 0", "[grow]", "[grow][grow][grow][grow]"));
        

        jMenuPanel.setBackground(resourceMap.getColor("jMenuPanel.background")); // NOI18N
        jMenuPanel.setName("jMenuPanel");
        jMenuPanel.setLayout(new MigLayout("hidemode 3, insets 0, gapx 0, gapy 0", "[grow]", "[][][][][][grow]"));

        jLabel5.setBackground(resourceMap.getColor("jLabel5.background")); // NOI18N
        jLabel5.setIcon(getLogo());
        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setAlignmentY(0.0F);
        jLabel5.setName("jLabel5"); // NOI18N
        jMenuPanel.add(jLabel5, "cell 0 0,grow");

        jPanelConnect.setBackground(resourceMap.getColor("jPanelConnect.background")); // NOI18N
        jPanelConnect.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jPanelConnect.setMinimumSize(new java.awt.Dimension(250, 66));
        jPanelConnect.setName("jPanelConnect"); // NOI18N
        jPanelConnect.setLayout(new MigLayout("hidemode 3, insets 0, gapx 0, gapy 0", "[grow][grow]", "[grow][grow][grow]"));

        jLabelConnectHeader.setFont(new Font("Arial", Font.PLAIN, Util.getFontSize())); // NOI18N
        jLabelConnectHeader.setText("     " + i18n.tr("Connection"));
        jLabelConnectHeader.setName("jLabelConnectHeader"); // NOI18N
        jPanelConnect.add(jLabelConnectHeader, "cell 1 0,alignx left,aligny bottom");

        jLabelConnectFooter.setFont(new Font("Arial", Font.PLAIN, Util.getFontSize())); // NOI18N
        jLabelConnectFooter.setText("     " + i18n.tr("Connect to Shellfire VPN now"));
        jLabelConnectFooter.setName("jLabelConnectFooter"); // NOI18N
        jPanelConnect.add(jLabelConnectFooter, "cell 1 1,alignx left,aligny top");

        jButtonConnect.setIcon( Util.getImageIcon("/buttons/button-connect-idle.png")); // NOI18N
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
        jPanelConnect.add(jButtonConnect, "cell 0 0 2 2,grow");

        jMenuPanel.add(jPanelConnect, "cell 0 1,growx,aligny top");

        jPanelServerList.setBackground(resourceMap.getColor("jPanelServerList.background")); // NOI18N
        jPanelServerList.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jPanelServerList.setMinimumSize(new java.awt.Dimension(250, 66));
        jPanelServerList.setName("jPanelServerList"); // NOI18N
        jPanelServerList.setLayout(new MigLayout("hidemode 3, insets 0, gapx 0, gapy 0", "[grow][grow]", "[grow][grow]"));

        jLabelServerListHeader.setFont(new Font("Arial", Font.PLAIN, Util.getFontSize())); // NOI18N
        jLabelServerListHeader.setText("     " + i18n.tr("Server list"));
        jLabelServerListHeader.setName("jLabelServerListHeader"); // NOI18N
        jPanelServerList.add(jLabelServerListHeader, "cell 1 0,alignx left,aligny bottom");

        jLabelServerListFooter.setFont(new Font("Arial", Font.PLAIN, Util.getFontSize())); // NOI18N
        jLabelServerListFooter.setText("     " + i18n.tr("Show list of all VPN servers"));
        jLabelServerListFooter.setName("jLabelServerListFooter"); // NOI18N
        jPanelServerList.add(jLabelServerListFooter, "cell 1 1,alignx left,aligny top");

        jButtonServerList.setIcon(Util.getImageIcon("/buttons/button-serverlist-idle.png")); // NOI18N
        jButtonServerList.setText(resourceMap.getString("jButtonServerList.text")); // NOI18N
        jButtonServerList.setName("jButtonServerList"); // NOI18N
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
        jPanelServerList.add(jButtonServerList, "cell 0 0 2 2,grow");

        jMenuPanel.add(jPanelServerList, "cell 0 2,aligny top,grow");

        jPanelMap.setBackground(resourceMap.getColor("jPanelMap.background")); // NOI18N
        jPanelMap.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jPanelMap.setMinimumSize(new java.awt.Dimension(250, 66));
        jPanelMap.setName("jPanelMap"); // NOI18N
        jPanelMap.setLayout(new MigLayout("hidemode 3, insets 0, gapx 0, gapy 0", "[grow][grow]", "[grow][grow]"));

        jLabelMapHeader.setFont(new Font("Arial", Font.PLAIN, Util.getFontSize())); // NOI18N
        jLabelMapHeader.setText("     " + i18n.tr("Map"));
        jLabelMapHeader.setName("jLabelMapHeader"); // NOI18N
        jPanelMap.add(jLabelMapHeader, "cell 1 0,alignx left,aligny bottom");

        jLabelMapFooter.setFont(new Font("Arial", Font.PLAIN, Util.getFontSize())); // NOI18N
        jLabelMapFooter.setText("     " + i18n.tr("Show encryption route"));
        jLabelMapFooter.setName("jLabelMapFooter"); // NOI18N
        jPanelMap.add(jLabelMapFooter, "cell 1 1,alignx left,aligny top");

        jButtonMap.setIcon(Util.getImageIcon("/buttons/button-map-idle.png")); // NOI18N
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
        jPanelMap.add(jButtonMap, "cell 0 0 2 2,grow");

        jMenuPanel.add(jPanelMap, "cell 0 3,growx,aligny top");

        jPanelUsa.setBackground(resourceMap.getColor("jPanelUsa.background")); // NOI18N
        jPanelUsa.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jPanelUsa.setMinimumSize(new java.awt.Dimension(250, 66));
        jPanelUsa.setName("jPanelUsa"); // NOI18N
        jPanelUsa.setLayout(new MigLayout("hidemode 3, insets 0, gapx 0, gapy 0", "[grow][grow]", "[grow][grow]"));

        jLabelUsaHeader.setFont(new Font("Arial", Font.PLAIN, Util.getFontSize())); // NOI18N
        jLabelUsaHeader.setText("     " + i18n.tr("USA streams"));
        jLabelUsaHeader.setName("jLabelUsaHeader"); // NOI18N
        jPanelUsa.add(jLabelUsaHeader, "cell 1 0,alignx left,aligny bottom");

        jLabelUsaFooter.setFont(new Font("Arial", Font.PLAIN, Util.getFontSize())); // NOI18N
        jLabelUsaFooter.setText("     " + i18n.tr("List of american TV streams"));
        jLabelUsaFooter.setName("jLabelUsaFooter"); // NOI18N
        jPanelUsa.add(jLabelUsaFooter, "cell 1 1,alignx left,aligny top");

        jButtonUsa.setIcon(Util.getImageIcon("/buttons/button-usa-idle.png")); // NOI18N
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
        jPanelUsa.add(jButtonUsa, "cell 0 0 2 2,grow");

        jMenuPanel.add(jPanelUsa, "cell 0 4,growx,aligny top");

        jHeaderPanel.setBackground(resourceMap.getColor("jHeaderPanel.background")); // NOI18N
        jHeaderPanel.setName("jHeaderPanel"); // NOI18N

        jLabelHelp.setFont(new Font("Arial", Font.PLAIN, Util.getFontSize())); // NOI18N
        jLabelHelp.setForeground(resourceMap.getColor("jLabelHelp.foreground")); // NOI18N
        jLabelHelp.setIcon(Util.getImageIcon("/icons/help.png")); // NOI18N
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

        jLabelExit.setFont(new Font("Arial", Font.PLAIN, Util.getFontSize())); // NOI18N
        jLabelExit.setForeground(resourceMap.getColor("jLabelExit.foreground")); // NOI18N
        jLabelExit.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelExit.setIcon(Util.getImageIcon("/icons/exit.png")); // NOI18N
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

        jLabelHide.setFont(new java.awt.Font("Arial", 0, Util.getFontSize()));
        jLabelHide.setForeground(resourceMap.getColor("jLabelHide.foreground")); // NOI18N
        jLabelHide.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelHide.setIcon(Util.getImageIcon("/icons/hide.png")); // NOI18N
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
        jLabelSettings.setIcon(Util.getImageIcon("/icons/settings.png")); // NOI18N
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
        jLabelMinimize.setIcon(Util.getImageIcon("/icons/minimize.png")); // NOI18N
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
        getContentPane().setLayout(new MigLayout("hidemode 3, insets 0, gapx 0, gapy 0", "[grow]", "[grow]"));
        getContentPane().add(jPanel5, "cell 0 0,grow");
        jPanel5.setLayout(new MigLayout("hidemode 3, insets 0, gapx 0, gapy 0", "[grow][grow]", "[][grow]"));
        jPanel5.add(jMenuPanel, "cell 0 0 1 2,grow");
        jPanel5.add(jContentPanel, "cell 1 1,grow");
        jContentPanel.setLayout(new MigLayout("hidemode 3, insets 0", "[grow]", "[][grow]"));
        jContentPanel.add(jPanel2, "cell 0 0,grow");
        jContentPanel.add(jPanel1, "cell 0 1,grow");
        jPanel5.add(jHeaderPanel, "cell 1 0,growx,aligny top");
        jHeaderPanel.setLayout(new MigLayout("hidemode 3", "[][][grow][][][]", "[]"));
        jHeaderPanel.add(jLabelHelp, "cell 0 0,alignx left,growy");
        jHeaderPanel.add(jLabelSettings, "cell 1 0,grow");
        jHeaderPanel.add(jLabelHide, "cell 3 0,grow");
        jHeaderPanel.add(jLabelMinimize, "cell 4 0,grow");
        jHeaderPanel.add(jLabelExit, "cell 5 0,alignx right,growy");
        
        this.jConnectionStateImage.setIcon(new ImageIcon(this.iconEcncryptionInactive));

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
		log.debug("connectFromButton("+failIfPremiumServerForFreeUser+")");
		this.setWaitCursor();

		SwingWorker<ConnectionState, Void> sw = new SwingWorker<ConnectionState, Void>() {

			@Override
			protected ConnectionState doInBackground() throws Exception {
				ConnectionState state = controller.getCurrentConnectionState();
				log.debug("retrieved current connection state: " + state);
				return state;
			}        

			public void done() {

				try {
					ConnectionState state = get();
					if (state == null) {
					  state = ConnectionState.Disconnected;
					}
					
					switch (state) {
					case Disconnected:
						if (isFreeAccount()) {

							Server server = getSelectedServer();
                                                        log.debug("ShellfireVPNMainform: connect button, server selection returned " );
							if (server.getServerType() == ServerType.Premium || server.getServerType() == ServerType.PremiumPlus) {
								if (failIfPremiumServerForFreeUser) {
									setNormalCursor();
									if (JOptionPane.YES_OPTION == JOptionPane
											.showConfirmDialog(
													null,
													i18n.tr("Dieser Server steht nur für Shellfire VPN Premium Kunden zur Verfügung\n\nWeitere Informationen zu Shellfire VPN Premium anzeigen?"),
													i18n.tr("Premium server selected"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {

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
													i18n.tr("PremiumPlus server selected"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {

										showNagScreenWithoutTimer();

									}
									return;
								} else {
                                                                    log.debug("ShellfireVPNMainform: connect button selection of server was random");
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
			this.connectProgressDialog = new ProgressDialog(this, false, i18n.tr("Connection is being processed..."));
			this.connectProgressDialog.setOption(2, i18n.tr("cancel"));
			this.connectProgressDialog.setOptionCallback(new Runnable() {

				@Override
				public void run() {
				  SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {

			      @Override
			      protected Void doInBackground() throws Exception {
		            controller.disconnect(Reason.AbortButtonPressed);
		          
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
    private javax.swing.JLabel jLabel5;
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
    private JXMapKit jXMapKit1;
    // End of variables declaration//GEN-END:variables
	private Image iconIdleSmall;
	private Image iconConnectingSmall;
	private Image iconConnectedSmall;
	private Image buttonDisconnect;
	private Image buttonConnect;

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

        Image origImage = new ImageIcon(getClass().getResource("/layout/map-grey-big.png")).getImage();
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
		this.pack(); 
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
                        lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN, Util.getFontSize()));
                        lbl.setHorizontalAlignment(JLabel.LEFT);

                        return lbl;
                    }

		};
    
                jServerListTable.setRowHeight(38);
    
		jServerListTable.setBorder(null);
		jScrollPane.setBorder(null);
		cm.getColumn(0).setCellRenderer(new CountryImageRenderer());
		cm.getColumn(0).setPreferredWidth((int) (70*Util.getScalingFactor()));
		cm.getColumn(1).setPreferredWidth((int) (30*Util.getScalingFactor()));
		cm.getColumn(2).setPreferredWidth((int) (45*Util.getScalingFactor()));
		cm.getColumn(3).setPreferredWidth((int) (100*Util.getScalingFactor()));
		cm.getColumn(4).setPreferredWidth((int) (170*Util.getScalingFactor()));

		cm.getColumn(1).setCellRenderer(defaultRenderer);
		cm.getColumn(2).setCellRenderer(defaultRenderer);
		cm.getColumn(3).setCellRenderer(defaultRenderer);

		cm.getColumn(4).setCellRenderer(new StarImageRenderer());
		cm.getColumn(3).setCellRenderer(new StarImageRenderer());

		this.jServerListTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jScrollPane.getViewport().setBackground(new Color(255, 255, 255));
		//jServerListTable.setRowHeight(10,50);
		
		JTableHeader header = jServerListTable.getTableHeader();
		header.setReorderingAllowed(false);
		header.setBackground(ContentPane.colorDarkGrey);
		header.setOpaque(false);
	    header.setForeground(Color.white);
	    header.setFont(OpenSansFont.getFont());
	    log.debug("Setting header preferred size to: " + cm.getTotalColumnWidth());
	    header.setPreferredSize(new Dimension(cm.getTotalColumnWidth(), (int) (25*Util.getScalingFactor())));
	    header.setBorder(null);
	    
	    JPanel panel = new JPanel();
	    panel.setBackground(ContentPane.colorDarkGrey);
	    jScrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, panel);
	    panel.setLayout(new MigLayout("hidemode 3, insets 0, gapx 0, gapy 0", "[]", "[]"));
	   
	}

	private void setLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
		}
	}

	WebService getShellfireService() {
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
	 */
	public void connectionStateChanged(ConnectionStateChangedEvent e)  {
		initController();
		
		ConnectionState state = e.getConnectionState();
		log.debug("connectionStateChanged " + state + ", reason=" + e.getReason());
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
      
	
	private void setStateDisconnected()  {
	  log.debug("setStateDisconnected() - start");
            enableSystemProxyIfProxyConfig();
            this.hideConnectProgress();
            this.jConnectButtonLabel.setIcon(new ImageIcon(buttonConnect));
            this.jConnectButtonLabel1.setIcon(new ImageIcon(buttonConnect));

            this.jConnectButtonLabel.setEnabled(true);
            this.jConnectButtonLabel1.setEnabled(true);
            this.jLabelConnectionState.setText(i18n.tr("Not connected"));
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
                            message = i18n.tr("Invalid password");
                            break;
                    case NotEnoughPrivileges:
                            showMessage = true;
                            message = i18n.tr("Process is being executed without administrator rights.");
                            break;
                    case CertificateFailed:
                            showMessage = true;
                            message = i18n.tr("Unknown certificate error");
                            break;
                    case AllTapInUse:
                            showMessage = true;
                            message = i18n.tr("All Tap devices in use. Please close openvpn.exe using the task manager or reboot your PC.");
                            break;
                    case DisconnectDetected:
                            showMessage = true;
                            message = i18n.tr("Connection interrupted.");
                            break;
                    case OpenVpnNotFound:
                            showMessage = true;
                            message = i18n.tr("No OpenVPN installation found. Please reinstall Shellfire VPN.");
                            break;
                    case NoOpenVpnParameters:
                            showMessage = true;
                            message = i18n.tr("OpenVPN startup parameters could not be downloaded - Please check your internet connection.");
                            break;
                    case TapDriverTooOld:
                            showMessage = true;
                            message = i18n.tr("The installed Tap driver is out of date. Please reinstall Shellfire VPN.");
                            break;
                    case TapDriverNotFound:
                        showMessage = true;
                        message = i18n.tr("No Tap driver installed. Please reinstall Shellfire VPN.");
                        break;
                    case TapDriverNotFoundPleaseRetry:
                        connectFromButton(true);
                        break;
                    case GatewayRedirectFailed:
                            showMessage = true;
                            message = i18n
                                            .tr("The gateway coul not be switched. Please set a gateway in the TCP/IP settings of the current network adapter.");
                            break;
                    case UnknownOpenVPNError:
                            showMessage = true;
                            message = i18n
                                            .tr("An unknown error has occured while establishing the VPN connection. Please reboot and/or reinstall Shellfire VPN.");
                            break;

                    default:
                            break;
                    }

                    log.debug("setStateDisconnected() - end");
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
            popupConnectItem.setLabel(i18n.tr("Connect"));
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

                                        showTrayMessageWithoutCallback(i18n.tr("Disconnected"),
                                                        i18n.tr("Shellfire VPN connection terminated. Your internet connection is no longer secured!"));
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

		this.jLabelConnectionState.setText(i18n.tr("Connection is being processed..."));
		mySetIconImage(iconConnecting);
		
		if (this.trayIcon != null) {
			this.trayIcon.setImage(this.iconConnecting);
		}
		this.setWaitCursor();
		this.jConnectionStateIcon.setIcon(new ImageIcon(this.iconConnectingSmall));

		popupConnectItem.setLabel(i18n.tr("Connecting..."));
		popupConnectItem.setEnabled(false);
		jServerListTable.setEnabled(false);
		jScrollPane.getViewport().setBackground(Color.lightGray);
		jRadioUdp.setEnabled(false);
		jRadioTcp.setEnabled(false);

	}

	private void setStateConnected() {
		this.hideConnectProgress();
		this.jConnectButtonLabel.setIcon(new ImageIcon(buttonDisconnect));
		this.jConnectButtonLabel1.setIcon(new ImageIcon(buttonDisconnect));
		this.jConnectButtonLabel.setEnabled(true);
		this.jConnectButtonLabel1.setEnabled(true);

		if (!this.jShowOwnPosition.isSelected())
			this.jShowOwnPosition.setEnabled(false);

		this.jLabelConnectionState.setText(i18n.tr("Connected"));
		
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

		popupConnectItem.setLabel(i18n.tr("Disconnect"));
		popupConnectItem.setEnabled(true);

		jServerListTable.setEnabled(false);
		jScrollPane.getViewport().setBackground(Color.lightGray);
		jRadioUdp.setEnabled(false);
		jRadioTcp.setEnabled(false);

		showTrayMessageWithoutCallback(i18n.tr("Connection successful"),
				i18n.tr("You are now connected to Shellfire VPN. Your internet connection is encrypted."));

		showStatusUrlIfEnabled();

		disableSystemProxyIfProxyConfig();
	}

	private void disableSystemProxyIfProxyConfig() {
		if (ProxyConfig.isProxyEnabled()) {
		  Client.disableSystemProxy();
		}
	}

	private void enableSystemProxyIfProxyConfig()  {
		if (ProxyConfig.isProxyEnabled()) {
		  Client.enableSystemProxy();
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
			MenuItem defaultItem = new MenuItem(i18n.tr("Exit"));
			defaultItem.addActionListener(exitListener);

			ActionListener nagListener = new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					showNagScreenWithoutTimer();
				}
			};

			MenuItem nagItem = new MenuItem(i18n.tr("Shellfire VPN premium infos"));
			nagItem.addActionListener(nagListener);

			ActionListener helpListener = new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					openHelp();
				}
			};

			MenuItem helpItem = new MenuItem(i18n.tr("Help"));
			helpItem.addActionListener(helpListener);

			ActionListener popupConnectListener = new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					connectFromButton(false);
				}
			};

			popupConnectItem = new MenuItem(i18n.tr("Connect"));
			popupConnectItem.addActionListener(popupConnectListener);

			ActionListener statusListener = new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					Util.openUrl(shellfireService.getUrlSuccesfulConnect());
				}
			};

			MenuItem statusItem = new MenuItem(i18n.tr("Show VPN state in your browser"));
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

			MenuItem openItem = new MenuItem(i18n.tr("Shellfire VPN to front"));
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

			MouseListener   mouseListener = new MouseListener() {
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
		jXMapKit1.setPreferredSize(new Dimension(800,800));

		jShowOwnPosition.setSelected(showOwnPositionOnMap);
		jShowOwnPositionActionPerformed(null);
	}

	public Server getSelectedServer() {
		int serverNum = this.jServerListTable.getSelectedRow();
		Server server = this.shellfireService.getServerList().getServer(serverNum);
		log.debug("getSelectedServer() - returning: " + server);
		return server;
	}

	public void setSelectedServer(Server server) {
		log.debug("setSelectedServer(" + server + ")");
		int num = this.shellfireService.getServerList().getServerNumberByServer(server);
		this.jServerListTable.setRowSelectionInterval(num, num);

	}

	private void loadIcons() {
		this.iconIdleSmall = Util.getImageIcon("/icons/small-globe-disconnected.png").getImage();
		this.iconIdle = Util.getImageIcon("/icons/sfvpn2-idle-big.png").getImage();
		
		this.iconConnectingSmall = Util.getImageIcon("/icons/small-globe-connecting.png").getImage();
		this.iconConnecting = Util.getImageIcon("/icons/sfvpn2-connecting-big.png").getImage();
		
		this.iconConnectedSmall = Util.getImageIcon("/icons/small-globe-connected.png").getImage();
		this.iconConnected = Util.getImageIcon("/icons/sfvpn2-connected-big.png").getImage();
		
		this.iconDisconnected = Util.getImageIcon("/icons/sfvpn2-disconnected-big.png").getImage();
		
		double scaleFactor = Util.getScalingFactor();
		log.debug("ScalingFactor: " + scaleFactor);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double width = screenSize.getWidth();
		
		String size = "736";
		if (width > 3000) {
		  size = "1472";
		}
		
		this.iconEcncryptionActive = new javax.swing.ImageIcon(ShellfireVPNMainForm.class.getResource("/icons/status-encrypted-width"+size+".gif")).getImage();
		this.iconEcncryptionInactive = new javax.swing.ImageIcon(ShellfireVPNMainForm.class.getResource("/icons/status-unencrypted-width"+size+".gif")).getImage();
		
		String langKey = VpnI18N.getLanguage().getKey();
		log.debug("langKey: " + langKey);
		this.buttonDisconnect = Util.getImageIcon("/buttons/button-disconnect-" + langKey + ".gif").getImage();
		this.buttonConnect = Util.getImageIcon("/buttons/button-connect-" + langKey + ".gif").getImage();
		
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

			SimpleDateFormat df = new SimpleDateFormat(i18n.tr("d/MM/yyyy"), VpnI18N.getLanguage().getLocale());
			String date = df.format(vpn.getPremiumUntil());

			this.jLabelVpnValidUntil.setText(date);
		}
	}

	private void showSettingsDialog() {
		new SettingsDialog(this, true);

	}

	@Override
	public void localeChanged(LocaleChangeEvent lce) {
		JOptionPane.showMessageDialog(null, i18n.tr("Changed language settings require a restart of Shellfire VPN to take effect.",
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
          showTrayIconNagScreen();
			}
		};

		Timer nagScreenTimer = new Timer(oneHour, taskPerformer);
		nagScreenTimer.setRepeats(true);
		nagScreenTimer.start();
	}

	private void showTrayIconNagScreen() {
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
			messages.add(new VpnTrayMessage(i18n.tr("Not connected"), i18n.tr("You are not connected to Shellfire VPN.")));
		}

		if (messages.size() > 0) {
			Random generator = new Random((new Date()).getTime());
			int num = generator.nextInt(messages.size());
			VpnTrayMessage msgToShow = messages.get(num);
			msgToShow.run();
		}

	}

	private void askForDisconnectedAndQuit() {
		int result = JOptionPane.showConfirmDialog(null, i18n.tr("Disconnect and close Shellfire VPN?"), i18n.tr("Currently Connected"),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

  		if (result == JOptionPane.OK_OPTION) {
  			this.controller.disconnect(Reason.ApplicationExit);
  			enableSystemProxyIfProxyConfig();
  			System.exit(0);
  		}
	}
	
	private void exitHandler() {
            boolean connected;

            connected = this.controller.getCurrentConnectionState() != ConnectionState.Disconnected;
            if (connected) {
                    askForDisconnectedAndQuit();
            } else {
                enableSystemProxyIfProxyConfig();  
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
		popupConnectItem.setLabel(i18n.tr("Connecting..."));
		popupConnectItem.setEnabled(false);

		nagScreen = new PremiumVPNNagScreen(this, true, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (nagScreentimer != null) {
					nagScreentimer.stop();
					nagScreentimer = null;
          controller.disconnect(Reason.DisconnectButtonPressed);
				}
				if (nagScreen != null) {
					nagScreen.dispose();
					nagScreen = null;
				}

				popupConnectItem.setLabel(i18n.tr("Connect"));
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


	private void initConsole() {
	    log.debug("showing logviewer...");
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
			  try {
	        log.debug("setting logViewer to visible");
	        logViewer.setVisible(true);
			    
			  } catch (Exception e) {
			    log.error("Erro occured while displaying logviewer", e);
			  }
			}
		});

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
            log.debug("Charter " + c + " pressed");
		this.typedStrings.append(c);
		if (typedStrings.toString().toLowerCase().endsWith("showconsole")) {
			this.initConsole();
		}
	}

	private void initPremium() {
		if (!this.isFreeAccount()) {
			this.jPremiumButtonLabel.setVisible(false);
			this.jPremiumButtonLabel1.setVisible(false);
		}
		this.jUpgradeButtonLabel.setVisible(false);
		this.jUpgradeButtonLabel1.setVisible(false);
	}

	public static ImageIcon getLogo() {
	  ImageIcon imageIcon = ShellfireVPNMainForm.mainIconMap.get(VpnI18N.getLanguage().getKey());
	  
		return imageIcon;
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
	  /*
		jLayeredPane1.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				Rectangle r = jLayeredPane1.getBounds();

				jServerListPanel.setBounds(r);
				jConnectPanel.setBounds(r);
				jMapPanel.setBounds(r);
				jUsaPanel.setBounds(r);
			}

		});
		*/
	}

}
