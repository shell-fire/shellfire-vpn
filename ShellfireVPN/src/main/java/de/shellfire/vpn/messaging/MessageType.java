package de.shellfire.vpn.messaging;

/**
 * The elements of this enum mostly match the public methods in IVpnController
 */
public enum MessageType {
   
  Ping, 
  Connect,
  Disconnect,
  GetConnectionState,
  SetParametersForOpenVpn,
  ReinstallTapDriver,
  SetAppDataFolder,
  EnableAutoStart,
  DisableAutoStart,
  AutoStartEnabled,
  

  Error,
  ConnectionStateChanged
}
