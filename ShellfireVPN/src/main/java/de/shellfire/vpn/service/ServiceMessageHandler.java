package de.shellfire.vpn.service;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.shellfire.vpn.client.ConnectionState;
import de.shellfire.vpn.client.ConnectionStateChangedEvent;
import de.shellfire.vpn.client.ConnectionStateListener;
import de.shellfire.vpn.messaging.Message;
import de.shellfire.vpn.messaging.MessageBroker;
import de.shellfire.vpn.messaging.MessageListener;
import de.shellfire.vpn.messaging.MessageType;
import de.shellfire.vpn.types.Reason;

@SuppressWarnings("unchecked")
public class ServiceMessageHandler implements MessageListener<Object>, ConnectionStateListener {

  private static Logger log = LoggerFactory.getLogger(ServiceMessageHandler.class.getCanonicalName());

  private final MessageBroker messageBroker;
  private final IVpnController vpnController;

  public ServiceMessageHandler(MessageBroker messageBroker) {
    this.messageBroker = messageBroker;

    this.vpnController = VpnControllerFactory.getVpnController();
    this.vpnController.addConnectionStateListener(this);

    messageBroker.addMessageListener(this);
    messageBroker.startReaderThread();
  }

  @Override
  public void messageReceived(Message<?, ?> message) {
    try {
      switch (message.getMessageType()) {
      case Ping:
        handlePing(message);
        break;
      case Connect:
        handleConnect(message);
        break;
      case SetAppDataFolder:
        handleSetAppDataFolder(message);
        break;
      case Error:
        handleError(message);
        break;
      case Disconnect:
        handleDisconnect(message);
        break;
      case GetConnectionState:
        handleGetConnectionState(message);
        break;
      case SetParametersForOpenVpn:
        handleSetParametersForOpenVpn(message);
        break;
      case ReinstallTapDriver:
        handleReinstallTapDriver(message);
        break;
      case EnableAutoStart:
        handleEnableAutoStart(message);
        break;
      case DisableAutoStart:
        handleDisableAutoStart(message);
        break;
      case AutoStartEnabled:
        handleAutoStartEnabled(message);
        break;
      default:
        handleUnknownMessageType(message);
        break;
      }

    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  private void handleReinstallTapDriver(Message<?, ?> message) {
    log.info("handleSetParametersForOpenVpn()");
    vpnController.reinstallTapDriver();
  }

  private void handleSetParametersForOpenVpn(Message<?, ?> message) {
    log.info("handleSetParametersForOpenVpn()");

    Message<String, Void> msg = (Message<String, Void>) message;
    String params = msg.getPayload();
    log.info("params: {}", params);
    vpnController.setParametersForOpenVpn(params);
  }

  private void handleGetConnectionState(Message<?, ?> message) throws IOException {
    log.info("handleGetConnectionState() - sending connection state");

    ConnectionState connectionState = vpnController.getConnectionState();
    log.info("ConnectionState: {}", connectionState);
    
    Message<ConnectionState, Void> msg = (Message<ConnectionState, Void>) message;
    Message<ConnectionState, Void> response = msg.createResponse(connectionState);
    messageBroker.sendResponse(response);
  }

  private void handleDisconnect(Message<?, ?> message) {
    log.info("handleDisconnect()");
    Message<Reason, Void> msg = (Message<Reason, Void>) message;
    Reason reason = msg.getPayload();
    log.info("Reason: {}", reason.name());
    vpnController.disconnect(reason);
  }

  private void handleError(Message<?, ?> message) {
    log.info("handleError() - received an error message from the client. Logging it, but otherwise ignoring it.");
    Message<Exception, Void> msg = (Message<Exception, Void>) message;
    Exception e = msg.getPayload();
    log.error(e.getMessage(), e);
  }

  private void handleSetAppDataFolder(Message<?, ?> message) {
    log.info("handleSetAppDataFolder()");
    Message<String, Void> msg = (Message<String, Void>) message;
    
    String appDataFolder = msg.getPayload();
    log.info("appDataFolder: {}", appDataFolder);
    vpnController.setAppDataFolder(appDataFolder);
  }

  private void handleConnect(Message<?, ?> message) {
    log.info("handleConnect()");
    Message<Reason, Void> msg = (Message<Reason, Void>) message;
    
    Reason reason = msg.getPayload();
    log.info("Reason: {}", reason.name());
    vpnController.connect(reason);
  }

  private void handleEnableAutoStart(Message<?, ?> message) {
    log.info("handleEnableAutoStart()");
    vpnController.enableAutoStart();
  }

  private void handleDisableAutoStart(Message<?, ?> message) {
    log.info("handleDisableAutoStart()");
    vpnController.disableAutoStart();
  }

  private void handleUnknownMessageType(Message<?, ?> message) {
    log.warn("Warning: Received message of unknown type: " + message);
  }

  private void handlePing(Message<?, ?> message) throws IOException {
    log.info("handlePing() - sending pingback");

    Message<Void, Void> msg = (Message<Void, Void>) message;
    Message<Void, Void> response = msg.createResponse();
    messageBroker.sendResponse(response);
  }

  private void handleAutoStartEnabled(Message<?, ?> message) throws IOException {
    log.info("Received request IsAutoStartEnabled");
    Boolean isAutoStartEnabled = vpnController.autoStartEnabled();
    log.info("Sending response: " + isAutoStartEnabled);

    Message<Boolean, Void> msg = (Message<Boolean, Void>) message;
    Message<Boolean, Void> response = msg.createResponse(isAutoStartEnabled);

    messageBroker.sendResponse(response);
  }

  @Override
  public void connectionStateChanged(ConnectionStateChangedEvent event) {
    log.debug("connectionStateChanged(ConnectionStateChangedEvent={})", event.toString());
    Message<ConnectionStateChangedEvent,Void> message = new Message<ConnectionStateChangedEvent,Void>(MessageType.ConnectionStateChanged, event);
    try {
      messageBroker.sendMessage(message);
    } catch (IOException e) {
      log.error("Error occured while sending connectionStateChanged message to client: {}", e.getMessage(), e);
    }
  }


}
