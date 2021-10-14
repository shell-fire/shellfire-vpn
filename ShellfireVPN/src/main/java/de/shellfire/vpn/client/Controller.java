/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.client;

import java.util.LinkedList;

import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.gui.controller.ShellfireVPNMainFormFxmlController;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.types.ProductType;
import de.shellfire.vpn.types.Reason;
import de.shellfire.vpn.types.Server;
import de.shellfire.vpn.types.VpnProtocol;
import de.shellfire.vpn.webservice.Vpn;
import de.shellfire.vpn.webservice.WebService;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;

/**
 *
 * @author bettmenn
 */
public class Controller {

	private static Logger log = Util.getLogger(Controller.class.getCanonicalName());
	private static Controller instance;
	private final ShellfireVPNMainFormFxmlController viewFX;
	private final WebService service;
	private Client client;
	private LinkedList<ConnectionStateListener> connectionStateListeners = new LinkedList<ConnectionStateListener>();
	protected boolean disconnectedDueToSleep;
	protected Server lastServerConnectedTo;
	private Boolean sleepBeingHandled = false;
	private Reason reasonForStateChange = Reason.None;
	private static final I18n i18n = VpnI18N.getI18n();
	
	private Controller(ShellfireVPNMainFormFxmlController viewFX, WebService service) {
		this.viewFX = viewFX;
		this.service = service;
	}

	public static Controller getInstanceFX(ShellfireVPNMainFormFxmlController viewFX, WebService service) {
		if (instance == null) {
			instance = new Controller(viewFX, service);
		}
		return instance;
	}

	public void connect(Server server, VpnProtocol protocol, Reason reason) {
		log.debug("connect(Server {}, Protocol {}, Reason {}) - setting connecting", server.toString(), protocol.toString(), reason.toString());
		connectionStateChanged(ConnectionState.Connecting, reason);

		try {
			this.client = Client.getInstance();
			this.client.setController(this);

			class ConnectionPreparer extends Thread {

				private Server server;
				private VpnProtocol protocol;
				private Reason reason;

				public ConnectionPreparer(Server server, VpnProtocol protocol, Reason reason) {
					this.server = server;
					this.protocol = protocol;
					this.reason = reason;
				}

				@Override
				public void run() {
					Vpn vpn = service.getVpn();

					boolean success = true;
					boolean downloadAndStoreCertificates = false;

					if (protocol != VpnProtocol.WireGuard && !service.certificatesDownloaded()) {
						downloadAndStoreCertificates = true;
					}
					
					// change server if required, protocol will be unchanged
					// here
					if (vpn.getServerId() != server.getServerId()) {
						success &= switchServerTo(server);
						
						if (protocol != VpnProtocol.WireGuard) {
							downloadAndStoreCertificates = true;
						}
					}

					if (protocol == VpnProtocol.WireGuard) {
						if (vpn.getProductType() != ProductType.WireGuard) {
							vpn.setProductType(ProductType.WireGuard);
						}
					} else {
						if (vpn.getProductType() != ProductType.OpenVpn) {
							downloadAndStoreCertificates = true;
							vpn.setProductType(ProductType.OpenVpn);
						}

						if (vpn.getProtocol() != protocol) {
							boolean switchProtocolSuccesful = switchProtocolTo(protocol);

							if (switchProtocolSuccesful) {
								success &= switchProtocolSuccesful;
								vpn.setProtocol(protocol);
							}

							downloadAndStoreCertificates = true;
						}
					}

					if (success) {
						connect(downloadAndStoreCertificates, reason);
					} else {
						// show alert that action could not be performed.
						Platform.runLater(() -> {
							Alert alert = new Alert(Alert.AlertType.ERROR, i18n.tr("An unknown error occured"), ButtonType.OK);
							alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
							alert.show();
						});
						
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
		
		if (vpn.getProductType() == ProductType.WireGuard) {
			try {
				String vpnName = "sf"+vpn.getVpnId();
				String 	pubKey = Util.getWireGuardPublicKeyUser(vpnName);
				service.setWireGuardPublicKeyUser(pubKey);

				// wireguard settings of vpn may have changed, so reload the object
				vpn = this.service.getVpn();
				
				Server server = this.service.getServerList().getServerByServerId(vpn.getServerId());
				
				String configPath = updateWireGuardConfig(vpnName, server, vpn);
				
				this.client.setWireguardConfigFilePath(configPath);
				
			} catch (Exception e) {
				Util.handleException(e);
				log.debug("Could not wireguard get public-key - not updating on server-side, aborting connect ");
				this.client.disconnect(Reason.WireGuardError);
				return;
			}
			
			
		} else {
			String params = this.service.getParametersForOpenVpn();
			this.client.setParametersForOpenVpn(params);
		}

		this.client.connect(reason);
	}

	private String updateWireGuardConfig(String vpnName, Server server, Vpn vpn) throws Exception {
		String configContent = "[Interface]" + "\n"
			+ "PrivateKey = " + Util.getWireGuardPrivateKeyUser(vpnName) + "\n"
			+ "Address = " + vpn.getWireguardIp() + "\n"
			+ "DNS = 8.8.8.8" + "\n"
			+ "\n"
			+ "[Peer]" + "\n"
			+ "# Server " + server.getServerId() + " / " + server.getCountryString() + "\n"
			+ "PublicKey = " + server.getWireguardPublicKey() + "\n"
			+ "AllowedIPs = 0.0.0.0/0" + "\n"
			+ "Endpoint = " + vpn.getListenHost() + ":51820";
		
		String pathToConfig = Util.getWireGuardFileNameConfig(vpnName);
		log.debug("Generated Wireguard config file will be stored at: {} - content:", pathToConfig);
		log.debug(configContent);
		
		Util.stringToFile(configContent, pathToConfig);
		
		return pathToConfig;
	}

	public ConnectionState getCurrentConnectionState() {
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

	public void connectionStateChanged(ConnectionStateChangedEvent e) {
		this.reasonForStateChange = e.getReason();

		if (this.client != null) {
			e.setServer(this.client.getServer());
		}

		for (ConnectionStateListener listener : this.connectionStateListeners) {
			listener.connectionStateChanged(e);
		}

	}

	public void disconnect(final Reason reason) {
		connectionStateChanged(ConnectionState.Disconnected, reason);

		if (this.client != null) {
			new Thread("Disconnecter") {
				public void run() {
					client.disconnect(reason);
				}
			}.start();

		}
	}

	public Reason getReasonForStateChange() {
		return this.reasonForStateChange;
	}

	/**
	 * switches to the specified server
	 *
	 * @return returns true if switch okay, false if not allowed to or other error
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
	 * @return returns true if switch okay, false if not allowed to or other error
	 */
	private boolean switchProtocolTo(VpnProtocol protocol) {
		return this.service.setProtocolTo(protocol);
	}

	public Server connectedTo() {
		if (this.client == null) {
			return null;
		} else if (this.client.getConnectionState() != ConnectionState.Connected) {
			return null;
		} else {
			return this.client.getServer();
		}
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
