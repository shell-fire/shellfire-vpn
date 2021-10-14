package de.shellfire.vpn.webservice.model;

public class SetWireGuardPublicKeyUserRequest {
	public SetWireGuardPublicKeyUserRequest(int productId, String wireguardPublicKeyUser) {
		this.productId = productId;
		this.wireguardPublicKeyUser = wireguardPublicKeyUser;
	}

	public int productId;
	public String wireguardPublicKeyUser;
}
