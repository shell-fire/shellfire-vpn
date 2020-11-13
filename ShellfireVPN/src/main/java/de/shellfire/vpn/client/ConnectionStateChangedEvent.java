/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.client;

import java.io.Serializable;

import de.shellfire.vpn.types.Reason;
import de.shellfire.vpn.types.Server;

/**
 * 
 * @author bettmenn
 */
public class ConnectionStateChangedEvent implements Serializable {
	private static final long serialVersionUID = 327656188288256987L;
	private Reason reason;
	private ConnectionState connectionState;
	private Server server;

	public String toString() {
		return "Reason: " + reason.name() + ", ConnectionState: " + connectionState.name();
	}

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
