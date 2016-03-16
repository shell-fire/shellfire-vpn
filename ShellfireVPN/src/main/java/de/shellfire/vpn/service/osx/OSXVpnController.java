package de.shellfire.vpn.service.osx;

import de.shellfire.vpn.client.ConnectionState;
import de.shellfire.vpn.client.ConnectionStateListener;
import de.shellfire.vpn.service.IVpnController;
import de.shellfire.vpn.types.Reason;

public class OSXVpnController implements IVpnController {

  private static OSXVpnController instance;

  @Override
  public void connect(Reason reason) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void disconnect(Reason reason) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public ConnectionState getConnectionState() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setParametersForOpenVpn(String params) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void reinstallTapDriver() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void setAppDataFolder(String appData) {
    // TODO Auto-generated method stub
    
  }

  public static IVpnController getInstance() {
    if (instance == null) {
      instance = new OSXVpnController();
    }

    return instance;
  }

  @Override
  public boolean autoStartEnabled() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void enableAutoStart() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void disableAutoStart() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void setConnectionState(ConnectionState connectionState, Reason reason) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void addConnectionStateListener(ConnectionStateListener connectionStateListener) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void close() {
    // TODO Auto-generated method stub
    
  }

}
