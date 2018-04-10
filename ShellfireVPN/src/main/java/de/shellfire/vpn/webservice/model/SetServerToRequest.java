package de.shellfire.vpn.webservice.model;

public class SetServerToRequest {
  public SetServerToRequest(int productId, int serverId) {
    this.productId = productId;
    this.serverId = serverId;
  }
  public int productId;
  public int serverId;

}
