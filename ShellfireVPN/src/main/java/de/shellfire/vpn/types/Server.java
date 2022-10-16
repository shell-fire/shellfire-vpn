package de.shellfire.vpn.types;

import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.client.Controller;
import de.shellfire.vpn.gui.model.ServerRow;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.webservice.model.VpnStar;
import de.shellfire.vpn.webservice.model.WsServer;

public class Server {
	private static final Logger log = Util.getLogger(Server.class.getCanonicalName());
	private int serverId;
	private Country country;
	private String city;
	private String name;
	private String host;
	private ServerType serverType;
	private double longitude;
	private double latitude;
	private String wireguardPublicKey;
	private Controller controller;
	private static I18n i18n = VpnI18N.getI18n();

	public Server(WsServer wss) {
		this.serverId = wss.getVpnServerId();

		String country = wss.getCountry();
		country = country.replace(" ", "");
		try {
			this.country = Enum.valueOf(Country.class, country);
		} catch (Exception e) {
			this.country = Country.Germany;
		}

		this.city = wss.getCity();
		this.name = wss.getName();
		this.host = wss.getHost();
		this.serverType = Enum.valueOf(ServerType.class, wss.getServertype());
		this.longitude = wss.getLongitude();
		this.latitude = wss.getLatitude();
		this.wireguardPublicKey = wss.getWireguardPublicKey();
	}

	public int getServerId() {
		return serverId;
	}

	public void setVpnServerId(int serverId) {
		this.serverId = serverId;
	}

	public Country getCountry() {
		return this.country;
	}

	public void setCountry(Country country) {
		this.country = country;
	}
	public String getCity() {
		return this.city;
	}
	public void setCity(String city) {
		this.city = city;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public ServerType getServerType() {
		return serverType;
	}

	public void setServerType(ServerType serverType) {
		this.serverType = serverType;
	}

	public VpnStar getServerSpeed() {
		switch (this.serverType) {
		case PremiumPlus:
			return new VpnStar(5, i18n.tr("unlimited kbit/sec"));
		case Premium:
			return new VpnStar(3, i18n.tr("up to 10,000 kbit/sec"));
		case Free:
		default:
			return new VpnStar(1, i18n.tr("up to 768 kbit/sec"));
		}
	}

	@Override
	public boolean equals(Object server) {
		if (server == null) {
			return false;
		} else if (server instanceof Server) {
			Server srv = (Server)server;
			return this.getServerId() == srv.getServerId();
		} else if (server instanceof ServerRow) {
			ServerRow srvRow = (ServerRow)server;
			return this.getServerId() == srvRow.getServer().getServerId();
		} else {
			return false;
		}
	}

	public double getLatitude() {
		return this.latitude;
	}

	public double getLongitude() {
		return this.longitude;
	}
	
	public String getWireguardPublicKey() {
		return this.wireguardPublicKey;
	}

	public String getCountryString() {
		return this.getCountry().toString();
	}

	public Controller getController() {
		return controller;
	}

	public void setController(Controller controller) {
		this.controller = controller;
	}

	public VpnStar getSecurity() {
		switch (this.serverType) {
		case PremiumPlus:
			return new VpnStar(5, i18n.tr("256 bit"));
		case Premium:
			return new VpnStar(3, i18n.tr("192 bit"));
		case Free:
		default:
			return new VpnStar(2, i18n.tr("128 bit"));
		}
	}

	public String toString() {
		return "serverId= " + serverId + "\r\n" + "country= " + country + "\r\n" + "name= " + name + "\r\n" + "host= " + host + "\r\n"
				+ "serrverType= " + serverType + "\r\n";
	}

	public boolean matchesFilter(String filter) {
		String lower = filter.toLowerCase();
		boolean cityContainsFilter = city != null && city.toLowerCase().contains(lower);
		boolean countryContainsFilter = country != null && country.name().toLowerCase().contains(lower);
		boolean country2ContainsFilter = country != null && VpnI18N.getCountryI18n().getCountryName(country).toLowerCase().contains(lower);
		boolean serverIdContainsFilter = (""+this.serverId).contains(lower);
		
		boolean matchesFilter = cityContainsFilter || countryContainsFilter || country2ContainsFilter|| serverIdContainsFilter;

		return matchesFilter;
	}
	
	public boolean matchesFilter(boolean includeFree, boolean includePremium, boolean includePremiumPlus) {
		
		if (!includeFree && !includePremium && !includePremiumPlus) {
			return true;	
		}
		
		if (serverType == ServerType.Free && includeFree) {
			return true;
		}
		if (serverType == ServerType.Premium && includePremium) {
			return true;
		}
		if (serverType == ServerType.PremiumPlus && includePremiumPlus) {
			return true;
		}
		
		return false;
	}

}
