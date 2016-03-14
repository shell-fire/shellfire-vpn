package de.shellfire.vpn.webservice.model;

public class GetCertificatesForOpenVpnRequest {
  private int productId;

  public GetCertificatesForOpenVpnRequest(int productId) {
    this.productId = productId;
  }

  public void setProductId(int vpnProductId) {
    this.productId = vpnProductId;
  }

  public int getProductId() {
    return productId;

  }
}
