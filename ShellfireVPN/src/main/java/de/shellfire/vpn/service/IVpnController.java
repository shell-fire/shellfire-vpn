package de.shellfire.vpn.service;

import de.shellfire.vpn.client.ConnectionState;
import de.shellfire.vpn.client.ConnectionStateListener;
import de.shellfire.vpn.types.Reason;

public interface IVpnController {
  
  public void connect(Reason reason);
  public void disconnect(Reason reason);
  
  public ConnectionState getConnectionState();
  public void setParametersForOpenVpn(String params);
 
  public void reinstallTapDriver();
  public void setAppDataFolder(String appData);
  
  
  public boolean autoStartEnabled();
  
  public void enableAutoStart();
  public void disableAutoStart();
  public void setConnectionState(ConnectionState connectionState, Reason reason);
  public void addConnectionStateListener(ConnectionStateListener connectionStateListener);
  public void close();
  
}
