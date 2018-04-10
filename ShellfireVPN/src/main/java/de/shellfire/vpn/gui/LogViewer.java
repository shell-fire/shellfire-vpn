package de.shellfire.vpn.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.messaging.UserType;
import de.shellfire.vpn.webservice.WebService;
import net.miginfocom.swing.MigLayout;

public class LogViewer extends JFrame {

  private static I18n i18n = VpnI18N.getI18n();  
  protected static ProgressDialog sendLogProgressDialog;
  private static Logger log = Util.getLogger(LogViewer.class.getCanonicalName());
  private static LogViewer instance;
  private JPanel contentPane;

  class LogListener extends TailerListenerAdapter {
    private JTextArea textArea;

    public LogListener(JTextArea textArea) {
      this.textArea = textArea;
    }
    
    public void handle(String line) {
        textArea.append(line+"\n");
    }
 
}   
  
  /**
   * Create the frame.
   */
  public LogViewer() {
    log.debug("Logviewer Constructor");
    setTitle("Logviewer");
    setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    

    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int width = (int) (screenSize.getWidth() / 1.5);
    int height = (int) (screenSize.getHeight() / 1.5);
    
    setBounds(100, 100, width, height);
    contentPane = new JPanel();
    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    setContentPane(contentPane);
    this.setLocationRelativeTo(null);
    contentPane.setLayout(new MigLayout("", "[grow]", "[grow][]"));
    JSplitPane splitPane = new JSplitPane();
    splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    splitPane.setResizeWeight(0.5);
    contentPane.add(splitPane, "cell 0 0,grow");
    
    JScrollPane scrollPane = new JScrollPane();
    splitPane.setLeftComponent(scrollPane);
    
    JTextArea clientLogTextArea = new JTextArea();
    clientLogTextArea.setEditable(false);
    scrollPane.setViewportView(clientLogTextArea);
    
    JLabel lblClient = new JLabel("Client Log");
    lblClient.setFont(new Font("Arial", Font.PLAIN, Util.getFontSize()));
    scrollPane.setColumnHeaderView(lblClient);
    
    JScrollPane scrollPane_1 = new JScrollPane();
    splitPane.setRightComponent(scrollPane_1);
    
    JTextArea serviceLogTextArea = new JTextArea();
    serviceLogTextArea.setEditable(false);
    scrollPane_1.setViewportView(serviceLogTextArea);
    
    JLabel lblNewLabel = new JLabel("Service Log");
    lblNewLabel.setFont(new Font("Arial", Font.PLAIN, Util.getFontSize()));
    scrollPane_1.setColumnHeaderView(lblNewLabel);
    
    JButton btnSendLogTo = new JButton("Send Log to Shellfire");
    btnSendLogTo.setFont(new Font("Arial", Font.PLAIN, Util.getFontSize()));
    btnSendLogTo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        sendLogToShellfire();
      }
    });
    contentPane.add(btnSendLogTo, "cell 0 1,growx,aligny top");
    
    DefaultCaret caret = (DefaultCaret) clientLogTextArea.getCaret();
    caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE); 
    
    DefaultCaret caret2 = (DefaultCaret) serviceLogTextArea.getCaret();
    caret2.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE); 
    
    
    LogListener clientListener = new LogListener(clientLogTextArea);
    String clientLog = Util.getLogFilePath(UserType.Client);
    File clientLogFile = new File(clientLog);
    Tailer.create(clientLogFile, clientListener);

    
    LogListener serviceListener = new LogListener(serviceLogTextArea);
    String serviceLog = Util.getLogFilePath(UserType.Service);
    File serviceLogFile = new File(serviceLog);
    Tailer.create(serviceLogFile, serviceListener);
    
    
  }

  private void sendLogToShellfire() {
    sendLogProgressDialog = new ProgressDialog(this, false, i18n.tr("Upload log.."));
    sendLogProgressDialog.setOption(2, i18n.tr("cancel"));
    
    final SendLogTask sendLogTask = new SendLogTask(this);
    
    sendLogProgressDialog.setOptionCallback(new Runnable() {

      @Override
      public void run() {
        JOptionPane.showMessageDialog(null, i18n.tr("Log upload cancelled."));
        if (sendLogTask != null && !sendLogTask.isDone() )
          sendLogTask.cancel(true);
      }
    });

    sendLogProgressDialog.setVisible(true);

    
    sendLogTask.execute();

  }

  public static LogViewer getInstance() {
    log.debug("getInstance()");
    if (instance == null) {
      log.debug("creating new instance");
      instance = new LogViewer();
    }
    log.debug("returning instance");
    return instance;
  }
  

  public class SendLogTask extends SwingWorker<Void, Object> {

    private boolean finished = false;
    private LogViewer viewer;

    public SendLogTask(LogViewer form) {
      this.viewer = form;
    }

    /*
     * Executed in event dispatch thread
     */
    public void done() {
      
      sendLogProgressDialog.setVisible(false);
      if (finished == true) {
        JOptionPane.showMessageDialog(viewer, i18n.tr("Log sent."));  
      }
      

    }
    
    @Override
    protected Void doInBackground() throws Exception {
      WebService service = WebService.getInstance();
      service.sendLogToShellfire();
      finished = true;
      return null;
    }
  } 
  
  
  
}
