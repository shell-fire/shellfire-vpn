package de.shellfire.vpn;

import java.util.Date;

import de.shellfire.www.webservice.sf_soap_php.WsVpn;


public class Vpn {

    private int vpnId;
    private int serverId;
    private ServerType accountType;
    private String listenHost;
    private VpnProtocol protocol;
    private Server server;
    private ProductType productType;
    private final Date premiumUntil;

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public Vpn(WsVpn vpn) {
        this.vpnId = vpn.getIVpnId();
        this.serverId = vpn.getIServerId();
        this.accountType = Enum.valueOf(ServerType.class, vpn.getEAccountType());
        this.listenHost = vpn.getSListenHost();
        if (vpn.getEProtocol() != null && vpn.getEProtocol().length() > 0)
        	this.protocol = Enum.valueOf(VpnProtocol.class, vpn.getEProtocol());

        ProductType[] vals = ProductType.values();
        this.productType = vals[vpn.getIProductTypeId()-1];
        long lngUntil = (long)vpn.getIPremiumUntil() * 1000;
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

    public Date getPremiumUntil() {
        return this.premiumUntil;
    }
}
