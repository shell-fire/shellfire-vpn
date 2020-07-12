package de.shellfire.vpn.service;

import de.shellfire.vpn.Util;
//import de.shellfire.vpn.service.osx.OSXVpnController;
import de.shellfire.vpn.service.win.WindowsVpnController;

public class VpnControllerFactory {
  /**
   * @return the correct IVpnController implementation for the current Operating System
   */
  public static IVpnController getVpnController() {
    return WindowsVpnController.getInstance();
  }
}