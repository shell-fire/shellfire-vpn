package de.shellfire.vpn.webservice.model;

public class SetProtocolToRequest {

  private int vpnProductId;
  private String protocol;

  public SetProtocolToRequest(int vpnProductId, String protocol) {
    this.vpnProductId = vpnProductId;
    this.protocol = protocol;
  }

  public int getVpnProductId() {
    return vpnProductId;
  }

  public void setVpnProductId(int vpnProductId) {
    this.vpnProductId = vpnProductId;
  }

  public String getProtocol() {
    return protocol;
  }

  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }
  

}
