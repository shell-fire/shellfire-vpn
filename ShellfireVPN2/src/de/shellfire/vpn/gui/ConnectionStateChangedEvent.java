/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui;

import de.shellfire.vpn.ConnectionState;
import de.shellfire.vpn.Reason;
import de.shellfire.vpn.Server;

/**
 * 
 * @author bettmenn
 */
public class ConnectionStateChangedEvent {
  private Reason reason;
  private ConnectionState connectionState;
  private Server server;

  public ConnectionStateChangedEvent(Reason reason, ConnectionState connectionState) {
    this.reason = reason;
    this.connectionState = connectionState;
  }

  public ConnectionStateChangedEvent(ConnectionState connectionState) {
    this.connectionState = connectionState;
  }

  public Reason getReason() {
    return reason;
  }

  public ConnectionState getConnectionState() {
    return connectionState;
  }

  public Server getServer() {
    return server;
  }

  public void setServer(Server server) {
    this.server = server;
    
  }

}
