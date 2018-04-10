package de.shellfire.vpn.messaging;

public interface MessageListener<E> {
  public void messageReceived(Message<?, ?> message);

}
