package de.shellfire.vpn.webservice;

import java.util.Date;

import de.shellfire.vpn.types.ProductType;
import de.shellfire.vpn.types.Server;
import de.shellfire.vpn.types.ServerType;
import de.shellfire.vpn.types.VpnProtocol;
import de.shellfire.vpn.webservice.model.WsVpn;

public class Vpn {

	private int vpnId;
	private int serverId;
	private ServerType accountType;
	private String listenHost;
	private VpnProtocol protocol;
	private Server server;
	private ProductType productType;
	private final Date premiumUntil;
	private String wireguardIp;

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	public Vpn(WsVpn vpn) {
		this.vpnId = vpn.getVpnId();
		this.serverId = vpn.getServerId();
		this.accountType = Enum.valueOf(ServerType.class, vpn.getAccountType());
		this.listenHost = vpn.getListenHost();
		this.wireguardIp = vpn.getWireguardIP();
		if (vpn.getProtocol() != null && vpn.getProtocol().length() > 0)
			this.protocol = Enum.valueOf(VpnProtocol.class, vpn.getProtocol());

		ProductType[] vals = ProductType.values();
		this.productType = vals[vpn.getProductTypeId() - 1];
		long lngUntil = (long) vpn.getPremiumUntil() * 1000;
		this.premiumUntil = new Date(lngUntil);
	}

	public int getVpnId() {
		return vpnId;
	}

	public void setVpnId(int vpnId) {
		this.vpnId = vpnId;
	}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public ServerType getAccountType() {
		return accountType;
	}

	public void setAccountType(ServerType accountType) {
		this.accountType = accountType;
	}

	public String getListenHost() {
		return listenHost;
	}

	public void setListenHost(String listenHost) {
		this.listenHost = listenHost;
	}

	public VpnProtocol getProtocol() {
		return protocol;
	}

	public void setProtocol(VpnProtocol protocol) {
		this.protocol = protocol;
	}

	public void loadServerObject(ServerList serverList) {

		this.server = serverList.getServerByServerId(this.serverId);
	}

	public ProductType getProductType() {
		return this.productType;
	}

	public void setProductType(ProductType productType) {
		this.productType = productType;
	}

	public Date getPremiumUntil() {
		return this.premiumUntil;
	}

	public String getWireguardIp() {
		return this.wireguardIp;
	}
	public void setWireguardIp(String wireguardIp) {
		this.wireguardIp = wireguardIp;
	}
	
	public String toString() {
		String result ="private int vpnId="+vpnId+";\r\n" +
			"private int serverId="+serverId+";\r\n" +
			"private ServerType accountType="+accountType+";\r\n" +
			"private String listenHost="+listenHost+";\r\n" +
			"private VpnProtocol protocol="+protocol+";\r\n" +
			"private Server server="+server+";\r\n" +
			"private ProductType productType="+productType+";\r\n" +
			"private final Date premiumUntil="+premiumUntil+";\r\n" +
			"private String wireguardIp="+wireguardIp+";\r\n";
		
		return result;
	}
}
