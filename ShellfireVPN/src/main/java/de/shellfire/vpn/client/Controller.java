/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.client;

import java.util.LinkedList;

import org.slf4j.Logger;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.gui.ShellfireVPNMainForm;
import de.shellfire.vpn.types.Protocol;
import de.shellfire.vpn.types.Reason;
import de.shellfire.vpn.types.Server;
import de.shellfire.vpn.webservice.WebService;
import de.shellfire.vpn.webservice.Vpn;

/**
 * 
 * @author bettmenn
 */
public class Controller {
  private static Logger log = Util.getLogger(Controller.class.getCanonicalName());
	private static Controller instance;
	private final ShellfireVPNMainForm view;
	private final WebService service;
	private Client client;
	private LinkedList<ConnectionStateListener> connectionStateListeners = new LinkedList<ConnectionStateListener>();
	protected boolean disconnectedDueToSleep;
	protected Server lastServerConnectedTo;
	private Boolean sleepBeingHandled = false;
  private Reason reasonForStateChange = Reason.None;;

	private Controller(ShellfireVPNMainForm view, WebService service) {
		this.view = view;
		this.service = service;
	}

	public static Controller getInstance(ShellfireVPNMainForm view, WebService service) {
		if (instance == null) {
			instance = new Controller(view, service);
		}
		return instance;
	}
	
	public void connect(Server server, Reason reason) {
		Protocol procotol = this.view.getSelectedProtocol();

		this.connect(server, procotol, reason);
	}

	public void connect(Server server, Protocol protocol, Reason reason) {

		log.debug("connect(Server, Protocol, Reason) - setting connected");

		try {
			this.client = Client.getInstance();
			this.client.setController(this);

			class ConnectionPreparer extends Thread {

				private Server server;
				private Protocol protocol;
				private Reason reason;

				public ConnectionPreparer(Server server, Protocol protocol, Reason reason) {
					this.server = server;
					this.protocol = protocol;
					this.reason = reason;
				}

				@Override
				public void run() {
					Vpn vpn = service.getVpn();

					boolean success = true;
					boolean downloadAndStoreCertificates = false;

					if (!service.certificatesDownloaded())
						downloadAndStoreCertificates = true;

					// change server if required, protocol will be unchanged
					// here
					if (vpn.getServerId() != server.getServerId()) {
						success &= switchServerTo(server);
						downloadAndStoreCertificates = true;
					}

					if (vpn.getProtocol() != protocol) {
						boolean switchProtocolSuccesful = switchProtocolTo(protocol);

						if (switchProtocolSuccesful) {
							success &= switchProtocolSuccesful;
							vpn.setProtocol(protocol);
						}

						downloadAndStoreCertificates = true;
					}

					if (success) {
						connect(downloadAndStoreCertificates, reason);
					} else {
						// inform view that we are disconnected right now
					  
						connectionStateChanged(getCurrentConnectionState(), Reason.ConnectionFailed);
					}
				}
			}

			(new ConnectionPreparer(server, protocol, reason)).start();
		} catch (Exception e) {
			Util.handleException(e);
		}
	}

	private void connect(boolean downloadAndStoreCertificates, Reason reason) {
		if (downloadAndStoreCertificates) {
			service.downloadAndStoreCertificates();
		}

		Vpn vpn = this.service.getVpn();
		this.client.setVpn(vpn);

		String params = this.service.getParametersForOpenVpn();

		this.client.setParametersForOpenVpn(params);
		this.client.connect(reason);

	}

	public ConnectionState getCurrentConnectionState()  {
		if (this.client == null) {
			try {
				this.client = Client.getInstance();
				client.setController(this);
			} catch (Exception e) {
				Util.handleException(e);
			}
		}
		if (this.client == null) {
			return ConnectionState.Disconnected;
		} else {
			return this.client.getConnectionState();
		}
		
	}

	public void connectionStateChanged(ConnectionStateChangedEvent e)  {
	  this.reasonForStateChange = e.getReason();
	  
		if (this.client != null) {
			e.setServer(this.client.getServer());
		}

		for (ConnectionStateListener listener : this.connectionStateListeners) {
			listener.connectionStateChanged(e);
		}

	}

	public void disconnect(Reason reason)  {
		if (this.client != null) {
			this.client.disconnect(reason);
		}
	}

	public Reason getReasonForStateChange() {
	  return this.reasonForStateChange;
	}

	/**
	 * switches to the specified server
	 * 
	 * @return returns true if switch okay, false if not allowed to or other
	 *         error
	 */
	private boolean switchServerTo(Server server) {
		boolean switchWorked = this.service.setServerTo(server);
		log.debug("Switch to server worked: " + switchWorked);

		if (!switchWorked) {
			return false;
		}

		return true;
	}

	/**
	 * switches to the specified Protocol
	 * 
	 * @return returns true if switch okay, false if not allowed to or other
	 *         error
	 */
	private boolean switchProtocolTo(Protocol protocol) {
		boolean switchWorked = this.service.setProtocolTo(protocol);

		return switchWorked;
	}

	public Server connectedTo()  {
		if (this.client == null)
			return null;
		else if (this.client.getConnectionState() != ConnectionState.Connected)
			return null;
		else
			return this.client.getServer();
	}

	public void setConnection(Client c) {
		this.client = c;
	}

	public void registerConnectionStateListener(ConnectionStateListener listener) {
		if (!this.connectionStateListeners.contains(listener)) {
			this.connectionStateListeners.add(listener);
		}
	}

  public void connectionStateChanged(ConnectionState newState, Reason reason) {
    ConnectionStateChangedEvent event = new ConnectionStateChangedEvent(reason, newState);
    
    connectionStateChanged(event);
  }
	

}
