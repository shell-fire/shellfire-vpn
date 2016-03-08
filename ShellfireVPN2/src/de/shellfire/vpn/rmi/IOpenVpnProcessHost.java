package de.shellfire.vpn.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import de.shellfire.vpn.ConnectionState;
import de.shellfire.vpn.Reason;


public interface IOpenVpnProcessHost extends Remote {

  public void disconnect(Reason reason) throws RemoteException;
  
  public ConnectionState getConnectionState() throws RemoteException;
  
  public void setParametersForOpenVpn(String params) throws RemoteException;
  
  public void setConnectionState(ConnectionState newState, Reason reason) throws RemoteException;
  
  public Reason getReasonForStateChange() throws RemoteException;

  public void connect() throws RemoteException;
  
  public boolean getConnectionStateChanged() throws RemoteException;
  
  public StringBuffer getNewConsoleLines() throws RemoteException;

  public void setConnecting() throws RemoteException;

  public void exit() throws RemoteException;
  
  public void reinstallTapDriver() throws RemoteException;
  
  public void setAppDataFolder(String appData) throws RemoteException;

  public void addVpnToAutoStart() throws RemoteException;

  public void removeVpnFromAutoStart() throws RemoteException;

  public boolean vpnAutoStartEnabled() throws RemoteException;

  public void disableSystemProxy() throws RemoteException;

  public void enableSystemProxy() throws RemoteException;

  public boolean isAutoProxyConfigEnabled() throws RemoteException;

  public String getAutoProxyConfigPath() throws RemoteException;  
  
}
