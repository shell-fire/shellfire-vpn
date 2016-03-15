package de.shellfire.vpn.gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.messaging.UserType;
import de.shellfire.vpn.webservice.WebService;

public class LogViewer extends JFrame {

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
    setTitle("Logviewer");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setBounds(100, 100, 1079, 636);
    contentPane = new JPanel();
    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    contentPane.setLayout(new BorderLayout(0, 0));
    setContentPane(contentPane);
    
    JSplitPane splitPane = new JSplitPane();
    splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    splitPane.setResizeWeight(0.5);
    contentPane.add(splitPane, BorderLayout.CENTER);
    
    JScrollPane scrollPane = new JScrollPane();
    splitPane.setLeftComponent(scrollPane);
    
    JTextArea clientLogTextArea = new JTextArea();
    clientLogTextArea.setEditable(false);
    scrollPane.setViewportView(clientLogTextArea);
    
    JLabel lblClient = new JLabel("Client Log");
    lblClient.setFont(new Font("Arial", Font.PLAIN, 14));
    scrollPane.setColumnHeaderView(lblClient);
    
    JScrollPane scrollPane_1 = new JScrollPane();
    splitPane.setRightComponent(scrollPane_1);
    
    JTextArea serviceLogTextArea = new JTextArea();
    serviceLogTextArea.setEditable(false);
    scrollPane_1.setViewportView(serviceLogTextArea);
    
    JLabel lblNewLabel = new JLabel("Service Log");
    lblNewLabel.setFont(new Font("Arial", Font.PLAIN, 14));
    scrollPane_1.setColumnHeaderView(lblNewLabel);
    
    JButton btnSendLogTo = new JButton("Send Log to Shellfire");
    btnSendLogTo.setFont(new Font("Arial", Font.PLAIN, 14));
    btnSendLogTo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        sendLogToShellfire();
      }
    });
    contentPane.add(btnSendLogTo, BorderLayout.SOUTH);
    
    DefaultCaret caret = (DefaultCaret) clientLogTextArea.getCaret();
    caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE); 
    caret = (DefaultCaret) serviceLogTextArea.getCaret();
    caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE); 
    
    
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
    WebService service = WebService.getInstance();
    service.sendLogToShellfire();
  }

  public static LogViewer getInstance() {
    if (instance == null) {
      instance = new LogViewer();
    }
    
    return instance;
  }
}
