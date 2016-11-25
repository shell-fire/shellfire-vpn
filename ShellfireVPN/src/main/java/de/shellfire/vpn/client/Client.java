/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.client;

import java.io.IOException;

import org.slf4j.Logger;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.Util.ExceptionThrowingReturningRunnable;
import de.shellfire.vpn.messaging.Message;
import de.shellfire.vpn.messaging.MessageBroker;
import de.shellfire.vpn.messaging.MessageListener;
import de.shellfire.vpn.messaging.MessageType;
import de.shellfire.vpn.service.IVpnRegistry;
import de.shellfire.vpn.types.Reason;
import de.shellfire.vpn.types.Server;
import de.shellfire.vpn.webservice.Vpn;

@SuppressWarnings("unchecked")
public class Client implements MessageListener<Object> {

  private static Logger log = Util.getLogger(Client.class.getCanonicalName());
  private Server server;
  private Controller controller;
  private static IVpnRegistry registry = Util.getRegistry();

  private MessageBroker messageBroker;
  private static Client instance;
  private boolean appDataSet = false;

  private Client() throws IOException {
    this.messageBroker = MessageBroker.getInstance();
    messageBroker.addMessageListener(this);
    messageBroker.startReaderThread();
  }
  
  public static Client getInstance() throws IOException {
    if (instance == null) {
      instance = new Client();
    }
    
    return instance;
  }
  
  public void setController(Controller controller) {
    this.controller = controller;
  }

  public void setVpn(Vpn vpn) {
    this.server = vpn.getServer();
  }

  public void disconnect(final Reason reason) {
    Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<Void>() {
      public Void run() throws Exception {
        Message<Reason, Void> message = new Message<Reason, Void>(MessageType.Disconnect, reason);
        messageBroker.sendMessage(message);

        return null;
      }
    }, 10, 50);

  }

  public ConnectionState getConnectionState() {
    log.debug("getConnectionState() - start");
    ConnectionState newState = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<ConnectionState>() {
      public ConnectionState run() throws Exception {
        log.debug("ConnectionState run() - start");
        Message<Void, ConnectionState> message = new Message<Void, ConnectionState>(MessageType.GetConnectionState);
        ConnectionState result = messageBroker.sendMessageWithResponse(message);
        log.debug("ConnectionState run() - finish");
        return result;
      }
    }, 4, 50);

    log.debug("getConnectionState() - finish - returning {}", newState);
    return newState;
  }

  public void setParametersForOpenVpn(final String params) {
    Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<Void>() {
      public Void run() throws Exception {
        Message<String, Void> message = new Message<String, Void>(MessageType.SetParametersForOpenVpn, params);
        messageBroker.sendMessage(message);

        return null;
      }
    }, 10, 50);

  }

  Server getServer() {
    return this.server;
  }

  public static void addVpnToAutoStart() {
    registry.enableAutoStart();
  }

  public static void removeVpnFromAutoStart() {
    registry.disableAutoStart();
  }

  public static boolean vpnAutoStartEnabled() {
    return registry.autoStartEnabled();
  }

  public static void disableSystemProxy() {
    registry.disableSystemProxy();
  }

  public static void enableSystemProxy() {
    registry.enableSystemProxy();
  }

  public static boolean isAutoProxyConfigEnabled() {
    return registry != null && registry.autoProxyConfigEnabled();
  }

  public static String getAutoProxyConfigPath() {
    return registry.getAutoProxyConfigPath();
  }

  @Override
  public void messageReceived(Message<?, ?> message) {

    switch (message.getMessageType()) {
    case ConnectionStateChanged:
      handleConnectionStateChanged(message);
      break;
    case Ping:
    case Connect:
    case SetAppDataFolder:
    case Error:
    case Disconnect:
    case GetConnectionState:
    case SetParametersForOpenVpn:
    case ReinstallTapDriver:
    case EnableAutoStart:
    case DisableAutoStart:
    case AutoStartEnabled:
    default:
      handleUnknownMessageType(message);
      break;
    }
  }

  private void handleConnectionStateChanged(Message<?, ?> message) {
    log.info("handleConnectionStateChanged()");

    Message<ConnectionStateChangedEvent, Void> msg = (Message<ConnectionStateChangedEvent, Void>) message;
    ConnectionStateChangedEvent event = msg.getPayload();
    log.info("event: {}", event);

    this.controller.connectionStateChanged(event);
  }

  private void handleUnknownMessageType(Message<?, ?> message) {
    log.warn("Warning: Received message of unknown type: " + message);
  }

  public void connect(final Reason reason) {
    log.debug("connect() - start");
    Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<Void>() {
      public Void run() throws Exception {
        Message<Reason, Void> message = new Message<Reason, Void>(MessageType.Connect, reason);
        messageBroker.sendMessage(message);

        return null;
      }
    }, 10, 50);
    log.debug("connect() - finish");
  }
  
  public boolean ping() {
    log.debug("ping() - start");
    Boolean result = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<Boolean>() {
      public Boolean run() throws Exception {
        Message<Void, Boolean> message = new Message<Void, Boolean>(MessageType.Ping);
        Boolean result = messageBroker.sendMessageWithResponse(message);
        return result;
      }
    }, 3, 50);
    
    if (result == null) {
      result = false;
    } else {
      // ping received - if first, set app data
      if (!this.appDataSet) {
        setAppDataFolder();
      }
    }
    
    log.debug("ping() - finished - returning {}", result);
    return result;
  }
  
  public void setAppDataFolder() {
    log.debug("setAppDataFolder() - start");
    Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<Void>() {
      public Void run() throws Exception {
        String appDataFolder = Util.getConfigDir();
        log.debug("appDataFolder: {}", appDataFolder);
        Message<String, Void> message = new Message<String, Void>(MessageType.SetAppDataFolder, appDataFolder);
        messageBroker.sendMessage(message);

        return null;
      }
    }, 10, 50);
    log.debug("setAppDataFolder() - finish");
  }
  

}
