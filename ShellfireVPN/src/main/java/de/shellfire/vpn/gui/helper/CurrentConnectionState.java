package de.shellfire.vpn.gui.helper;

import java.util.ArrayList;
import java.util.List;

import de.shellfire.vpn.client.ConnectionState;
import de.shellfire.vpn.client.ConnectionStateChangedEvent;
import de.shellfire.vpn.client.ConnectionStateListener;
import de.shellfire.vpn.client.Controller;
import de.shellfire.vpn.gui.controller.ShellfireVPNMainFormFxmlController;

public class CurrentConnectionState implements ConnectionStateListener {

	private static ConnectionState currentConnectionState = ConnectionState.Disconnected;
	private List<ConnectionStateChangedEvent> connectionStateChangeEventList;
	private static CurrentConnectionState instance;
	
	private CurrentConnectionState(ShellfireVPNMainFormFxmlController mainForm) {
		Controller controller = mainForm.getController();
		
		connectionStateChangeEventList = new ArrayList<ConnectionStateChangedEvent>();
		controller.registerConnectionStateListener(this);
	}
	
	public static CurrentConnectionState getInstance(ShellfireVPNMainFormFxmlController mainForm) {
		if (instance == null && mainForm != null) {
			instance = new CurrentConnectionState(mainForm);
		}

		return instance;
	}

	@Override
	public void connectionStateChanged(ConnectionStateChangedEvent e) {
		this.connectionStateChangeEventList.add(e);
		currentConnectionState = e.getConnectionState();
	}
	
	public static ConnectionState getConnectionState() {
		if (currentConnectionState == null) {
			return ConnectionState.Disconnected;
		} else {
			return currentConnectionState;
		}
	}
}
