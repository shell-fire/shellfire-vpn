package de.shellfire.vpn.messaging;


import java.io.Serializable;
import java.util.UUID;

public class Message<T, E> implements Serializable {

  private static final long serialVersionUID = -1627288370966014287L;
  private static Integer currentMessageId = 0;

  private final MessageType messageType;
  private final T payload;
  private UUID messageId;

  private UserType sender;
  private boolean isResponse;
  private final long creationTime;

  public Message(MessageType messageType, T payload) {
    this.messageType = messageType;
    this.payload = payload;
    synchronized (Message.currentMessageId) {
      this.messageId = UUID.randomUUID();
    }
    this.creationTime = System.currentTimeMillis();
  }

  public Message(MessageType messageType) {
    this(messageType, null);
  }

  public final MessageType getMessageType() {
    return messageType;
  }

  public final T getPayload() {
    return payload;
  }

  public UUID getMessageId() {
    return this.messageId;
  }

  public void setSender(UserType userType) {
    this.sender = userType;
  }

  public UserType getSender() {
    return this.sender;
  }

  public void setIsResponse(boolean isResponse, UUID forMessageId) {
    this.isResponse = isResponse;
    this.messageId = forMessageId;
  }
  
  public boolean isResponse() {
    return this.isResponse;
  }

  public String toString() {
    return "messageType=" + messageType.name() + ", payload=" + payload + ", isResponse=" + isResponse + ", messageId="+messageId + ",sender="+sender.name();
  }

  public Message<T, E> createResponse(T payload) {
    Message<T,E> response = new Message<T,E>(this.messageType, payload);
    response.setIsResponse(true, this.getMessageId());
    return response;
  }

  public Message<T, E> createResponse() {
    return createResponse(null);
  }

  public boolean isRecent() {
    return System.currentTimeMillis()-creationTime < 2000;
  }
}
