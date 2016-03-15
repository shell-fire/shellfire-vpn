package de.shellfire.vpn.messaging;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;

import de.shellfire.vpn.Util;
import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ChronicleQueueBuilder;
import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.ExcerptTailer;

public class MessageBroker {
  
  private static Logger log = Util.getLogger(MessageBroker.class.getCanonicalName());
  private static final String FILE_PATH_SERVICE_TO_CLIENT = "sfvpn-chronicle-service-to-client";
  private static final String FILE_PATH_CLIENT_TO_SERVICE = "sfvpn-chronicle-client-to-service";
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

  private void deleteChronicleFiles(String path) {
    log.debug("deleteChronicleFiles({})", path);
    String index = path + ".index";
    String data = path + ".data";

    File fileIndex = new File(index);
    File fileData = new File(data);

    fileIndex.delete();
    fileData.delete();

    log.debug("Result: fileIndex.exists(): {} - fileData.exists(): {}", fileIndex.exists(), fileData.exists());
  }

  private String getChronicleFiles(Direction direction) {
    log.debug("getChronicleFiles({})", direction.name());

    String path = "";
    switch (this.userType) {
    case Service:
      switch (direction) {

      case Read:
        path = FILE_PATH_CLIENT_TO_SERVICE;
        break;
      case Write:
        path = FILE_PATH_SERVICE_TO_CLIENT;
        break;
      }

      break;
    case Client:
      switch (direction) {

      case Read:
        path = FILE_PATH_SERVICE_TO_CLIENT;
        break;
      case Write:
        path = FILE_PATH_CLIENT_TO_SERVICE;
        break;
      }

      break;
    }

    log.debug("path is: {}", path);
    String result = Util.getTempDir() + path;
    deleteChronicleFiles(result);

    log.debug("getChronicleFiles() - returning", result);
    return result;
  }

  enum Direction {
    Read, Write
  }

  private void init() throws IOException {
    String readerPath = getChronicleFiles(Direction.Read);
    String writerPath = getChronicleFiles(Direction.Write);

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

  public void startReaderThread() {
    log.debug("Starting reader thread...");
    Runnable r = new Runnable() {
      public void run() {
        while (true) {
          // While until there is a new Excerpt to read
          while (!tailer.nextIndex())
            ;

          // Read the object
          Object o = tailer.readObject();

          // Make the reader ready for next read
          tailer.finish();

          if (o instanceof Message) {
            Message<?, ?> message = (Message<?, ?>) o;
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

    new Thread(r, "ReaderThread").start();
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

    while (!responseReceived(message))
      ;
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

}
