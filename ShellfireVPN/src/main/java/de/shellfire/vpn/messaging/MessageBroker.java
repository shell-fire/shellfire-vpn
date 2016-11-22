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
import net.openhft.chronicle.ExcerptCommon;
import net.openhft.chronicle.ExcerptTailer;

public class MessageBroker {
  
  private static Logger log = Util.getLogger(MessageBroker.class.getCanonicalName());
  private static MessageBroker instance;
  private static final String FILE_PATH_SERVICE_TO_CLIENT = "sfvpn-chronicle-service-to-client";
  private static final String FILE_PATH_CLIENT_TO_SERVICE = "sfvpn-chronicle-client-to-service";
  ExcerptAppender writer;
  ExcerptTailer tailer;

  private final static int MAX_MESSAGE_SIZE = 1000;
  private static final long TIMEOUT = 1000;

  private Map<UUID, Message<?, ?>> receivedMessageMap = new ConcurrentHashMap<UUID, Message<?, ?>>();
  private ArrayList<MessageListener<?>> messageListeners = new ArrayList<MessageListener<?>>();
  private ReaderThread readerThread;
  private Chronicle chronicleReader;
  private Chronicle chronicleWriter;
  private String readerPath;
  private String writerPath;

  private MessageBroker() throws IOException {
    init();
  }
  
  public static MessageBroker getInstance() throws IOException {
    if (instance == null) {
      instance = new MessageBroker();
    }
    
    return instance;
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
    switch (Util.getUserType()) {
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
    case Updater:
      break;
    default:
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
    readerPath = getChronicleFiles(Direction.Read);
    writerPath = getChronicleFiles(Direction.Write);

    log.debug("Opening Chronicle Writer Queue at {}", writerPath);
    chronicleWriter = ChronicleQueueBuilder.indexed(writerPath).build();

    log.debug("Opening Chronicle Reader Queue at {}", readerPath);
    chronicleReader = ChronicleQueueBuilder.indexed(readerPath).build();

    // Obtain an ExcerptAppender
    writer = chronicleWriter.createAppender();

    // Configure the appender to write up to 1000 bytes
    writer.startExcerpt(MAX_MESSAGE_SIZE);

    // Obtain an ExcerptTailer
    tailer = chronicleReader.createTailer();
  }

  class ReaderThread extends Thread {
    private boolean stop = false;

    public ReaderThread() {
      super("ReaderThread");
    }

    public void stopRequested() {
      this.stop = true;
    }

    public void run() {
      while (!stop) {
        // While until there is a new Excerpt to read, or stop is requested
        while (!stop && !tailer.nextIndex()) {
          // 10 ms is enough to not use ANY cpu during sleep.
          Util.sleep(50);
        }

        // Read the object
        Object o = null;
        o = tailer.readObject();
        
        // Make the reader ready for next read
        tailer.finish();

        if (o != null && o instanceof Message) {
          Message<?, ?> message = (Message<?, ?>) o;
          // only handle this message if it did not
          // originate from us
          if (message.getSender() != Util.getUserType()) {
            if (message.isResponse()) {
              receivedMessageMap.put(message.getMessageId(), message);
            } else {
              notifyListeners(message);
            }
          }
        }
        
      }
    }
  }
  
  public void startReaderThread() {
    log.debug("Starting reader thread...");

    readerThread = new ReaderThread();
    readerThread.start();
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
    message.setSender(Util.getUserType());
    log.debug(message.toString());

    writer.writeObject(message);
    writer.finish();

    return null;
  }

  public <E, T> E sendMessageWithResponse(Message<T, E> message) throws IOException {
    sendMessage(message);
    
    long start = System.currentTimeMillis();
    long timePassed = 0;
    while (!responseReceived(message) && timePassed  < TIMEOUT) {
      timePassed = System.currentTimeMillis() - start;
    }
    if (timePassed >= TIMEOUT) {
      log.warn("no answer received");
      throw new IOException("no answer received");
    }

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

  public void close() {
    log.debug("close() - start");
    
    closeReaderThread();

    log.info("closing tailer...");
    this.closeExcerpt(tailer);
    log.info("closing writer...");
    this.closeExcerpt(writer);
    
    log.info("closing chronicleReader");
    closeChronicle(chronicleReader);
    log.info("closing chronicleWriter");
    closeChronicle(chronicleWriter);
    
    log.info("marking reader file to be deleted on exit");
    
    
    log.info("marking readerPath to be deleted on exit");
    deleteOnExit(readerPath);
    log.info("marking writerPath to be deleted on exit");
    deleteOnExit(writerPath);
    
    log.debug("close() - finish");
  }

  private void deleteOnExit(String path) {
    if (path == null) {
        log.debug("deleteOnExit() - path is null, not deleting on exit");
    } else {
      log.debug("deleteOnExit({})", path);
      new File(path).deleteOnExit();  
    }
  }

  private void closeReaderThread() {
    log.info("closeReaderThread() - start");
    if (readerThread == null) {
      log.warn("readerThread is null - unable to shut down gracefully");
    } else {
      log.info("shutdown down readerThread...");
      readerThread.stopRequested();      
      log.info("...done");
    }
    log.info("closeReaderThread() - finish");
  }

  private void closeExcerpt(ExcerptCommon excerpt) {
    log.debug("closeExcerpt() - start");
    if (excerpt == null) {
      log.warn("excerpt is null, not closing");
    } else {
      log.info("closing excerpt");
      excerpt.close();
      excerpt = null;
    }
    log.debug("closeExcerpt() - finish");
  }

  private void closeChronicle(Chronicle chronicle) {
    log.debug("closeChronicle() - start");
    try {
      if (chronicle == null) {
        log.warn("chronicle is null, not closing");
      } else {
        log.info("closing chroncile");
        chronicle.close();
        chronicle = null;
      }
      chronicleReader.close();
    } catch (IOException e) {
      log.error("IOException occured during chronicle.close()", e);
    }
    
    log.debug("closeChronicle() - finish");
  }

}
