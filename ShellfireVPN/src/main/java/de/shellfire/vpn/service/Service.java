package de.shellfire.vpn.service;

import java.io.IOException;

import org.slf4j.Logger;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.messaging.Message;
import de.shellfire.vpn.messaging.MessageBroker;
import de.shellfire.vpn.messaging.MessageType;
import de.shellfire.vpn.messaging.UserType;

public class Service {

  private static Logger log = Util.getLogger(Service.class.getCanonicalName());
  private static MessageBroker messageBroker;

  public static void main(String[] args) {
    log.info("Service starting up");

    Service s = new Service();
    s.run();
  }

  private void run() {
    try {
      messageBroker = new MessageBroker(UserType.Service);
      new ServiceMessageHandler(messageBroker);
    } catch (IOException e) {
      Service.handleException(e);
    }
  }

  public static void handleException(Exception e) {
    log.error("Exception occured. Sending to client: {}", e.getMessage(), e);
    Message<Exception, Void> errorMessage = new Message<Exception, Void>(MessageType.Error, e);
    try {
      if (messageBroker != null) {
        messageBroker.sendMessage(errorMessage);
      }

    } catch (IOException e1) {
      log.error("Error occured while sending message to client: ", e.getMessage(), e);
    }
  }

}