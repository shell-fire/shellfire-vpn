package de.shellfire.vpn;

import java.net.Socket;

import de.shellfire.vpn.proxy.ProxyConfig;

public class SocketTest {
  public static void main(String[] args) {
    try {
      //System.setProperty("java.net.useSystemProxies", "true");
      //System.setProperty("java.net.useSystemProxies", "false");
      //
      ProxyConfig.perform();
      Socket tunnel = new Socket("192.168.1.101", 8080);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
