package de.shellfire.vpn.messaging;

import java.io.Serializable;

/**
 * The elements of this enum mostly match the public methods in IVpnController
 */
public enum MessageType implements Serializable {

	Ping, Connect, Disconnect, GetConnectionState, SetParametersForOpenVpn, ReinstallTapDriver, SetAppDataFolder, EnableAutoStart, DisableAutoStart, AutoStartEnabled, SetCryptoMinerConfig,

	Error, ConnectionStateChanged, SetWireguardConfigFilePath
}
