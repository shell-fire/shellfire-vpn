package de.shellfire.vpn.messaging;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.shellfire.vpn.Util;
import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ChronicleQueueBuilder;
import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.ExcerptTailer;

public class MessageBroker {
  private static final String SHELLFIRE_VPN = "shellfire-vpn";
  private static Logger log = LoggerFactory.getLogger(MessageBroker.class.getCanonicalName());
  private static final String FILE_PATH_SERVICE_TO_CLIENT = SHELLFIRE_VPN + File.separator + "sfvpn-chronicle-service-to-client";
  private static final String FILE_PATH_CLIENT_TO_SERVICE = SHELLFIRE_VPN + File.separator + "sfvpn-chronicle-client-to-service";
  ExcerptAppender writer;
  ExcerptTailer tailer;

  private final UserType userType;
  private final static int MAX_MESSAGE_SIZE = 1000;

  private Map<UUID, Message<?, ?>> receivedMessageMap = new ConcurrentHashMap<UUID, Message<?, ?>>();
  private ArrayList<MessageListener<?>> messageListeners = new ArrayList<MessageListener<?>>();

  public MessageBroker(UserType userType) throws IOException {
    this.userType = userType;

    init();
  }

  private void init() throws IOException {
    String readerPath = null;;
    String writerPath = null;;
    
    switch (this.userType) {
      case Service:
        readerPath = FILE_PATH_CLIENT_TO_SERVICE;
        writerPath = FILE_PATH_SERVICE_TO_CLIENT;
        break;
      case Client:
        writerPath = FILE_PATH_CLIENT_TO_SERVICE;
        readerPath = FILE_PATH_SERVICE_TO_CLIENT;
        
        break;
    }
    
    if (Util.isWindows()) {
      // Some Windows versions have user specific temp files, so always use C:\Temp
      readerPath = System.getenv("SystemDrive") + "\\Temp\\" + readerPath;
      writerPath = System.getenv("SystemDrive") + "\\Temp\\" + writerPath;
    } else {
      readerPath = System.getProperty("java.io.tmpdir") + readerPath;
      writerPath = System.getProperty("java.io.tmpdir") + writerPath;
    }
    
    log.debug("Opening Chronicle Writer Queue at {}", writerPath);
    Chronicle chronicleWriter = ChronicleQueueBuilder.indexed(writerPath).build();
    
    log.debug("Opening Chronicle Reader Queue at {}", readerPath);
    Chronicle chronicleReader = ChronicleQueueBuilder.indexed(readerPath).build();
    
    
    // Obtain an ExcerptAppender
    writer = chronicleWriter.createAppender();

    // Configure the appender to write up to 1000 bytes
    writer.startExcerpt(MAX_MESSAGE_SIZE);
    
    // Obtain an ExcerptTailer
    tailer = chronicleReader.createTailer();
  }

  private void log(String s) {
    System.out.println(this.userType + " - " + s);
  }
  
  public void startReaderThread() {
    log("Starting reader thread...");
    Runnable r = new Runnable() {
      public void run() {
        while (true) {
          // While until there is a new Excerpt to read
          while(!tailer.nextIndex());
          // Read the objecy
          Object o = tailer.readObject();

          // Make the reader ready for next read
          tailer.finish();

          if (o instanceof Message) {
            Message<?, ?> message = (Message<?, ?>) o;
            System.out.println(message);
            // only handle this message if it did not
            // originate from us
            if (message.getSender() != userType) {
              if (message.isResponse()) {
                receivedMessageMap.put(message.getMessageId(), message);
              } else {
                notifyListeners(message);
              }
            }
          }
        }
      }
    };

    new Thread(r).start();
  }

  public void addMessageListener(MessageListener<?> listener) {
    this.messageListeners.add(listener);
  }

  private void notifyListeners(Message<?, ?> message) {
    if (message.isRecent()) {
      try {
        for (MessageListener<?> listener : this.messageListeners) {
          listener.messageReceived(message);
        }

      } catch (Exception e) {
        e.printStackTrace();
      }
      
    } else {
      log.debug("ignoring old message: {}", message.toString());
    }
  }

  public <E, T> E sendMessage(Message<T, E> message) throws IOException {
    message.setSender(this.userType);
    log.debug(message.toString());
    
    writer.writeObject(message);
    writer.finish();

    return null;
  }

  public <E, T> E sendMessageWithResponse(Message<T, E> message) throws IOException {
    sendMessage(message);

    while (!responseReceived(message));
    E response = getResponse(message);

    return response;
  }

  @SuppressWarnings("unchecked")
  private <T, E> E getResponse(Message<T, E> message) {

    Message<?, ?> response = receivedMessageMap.get(message.getMessageId());
    return (E) response.getPayload();
  }

  public <E, T> boolean responseReceived(Message<T, E> m) {
    return receivedMessageMap.containsKey(m.getMessageId());
  }

  public void sendResponse(Message<?, ?> response) throws IOException {
    sendMessage(response);
  }

  public void startHeatBeat() {
    final Message<Void, Void> heartbeat = new Message<Void, Void>(MessageType.Ping);

    Runnable r = new Runnable() {
      public void run() {
        while (true) {
          try {
            sendMessage(heartbeat);
            Thread.sleep(1000);
          } catch (IOException | InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
    };
    new Thread(r).start();
  }

}
