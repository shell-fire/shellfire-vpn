package de.shellfire.vpn.webservice.model;

public class GetParametersForOpenVpnRequest {

	private int productId;

	public GetParametersForOpenVpnRequest(int productId) {
		this.productId = productId;
	}

	public void setVpnProductId(int vpnProductId) {
		this.productId = vpnProductId;
	}

	public int getVpnProductId() {
		return productId;

	}

}
