package de.shellfire.vpn.gui;

public class VpnAttributeHeader {

  private String containerName;

  public VpnAttributeHeader(String containerName) {
    this.containerName = containerName;
  }
  
  public String toString() {
    return containerName;
  }

}
