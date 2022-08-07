/**
 * WsServer.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.shellfire.vpn.webservice.model;

public class WsServer implements java.io.Serializable {
	private int vpnServerId;

	private String country;

	private String city;

	private String name;

	private String host;

	private String servertype;

	private float longitude;

	private float latitude;
	
	private String wireguardPublicKey;

	public WsServer() {
	}

	public WsServer(int vpnServerId, String country, String city, String name, String host,
			String servertype, float longitude, float latitude, String wireguardPublicKey) {
		this.vpnServerId = vpnServerId;
		this.country = country;
		this.city = city;
		this.name = name;
		this.host = host;
		this.servertype = servertype;
		this.longitude = longitude;
		this.latitude = latitude;
		this.wireguardPublicKey = wireguardPublicKey;
	}

	/**
	 * Gets the vpnServerId value for this WsServer.
	 * 
	 * @return vpnServerId
	 */
	public int getVpnServerId() {
		return vpnServerId;
	}

	/**
	 * Sets the vpnServerId value for this WsServer.
	 * 
	 * @param vpnServerId
	 */
	public void setVpnServerId(int vpnServerId) {
		this.vpnServerId = vpnServerId;
	}

	/**
	 * Gets the country value for this WsServer.
	 * 
	 * @return country
	 */
	public String getCountry() {
		return country;
	}

	/**
	 * Sets the country value for this WsServer.
	 * 
	 * @param country
	 */
	public void setCountry(String country) {
		this.country = country;
	}

	/**
	 * Gets the city value for this WsServer.
	 * 
	 * @return city
	 */
	public String getCity() {
		return city;
	}

	/**
	 * Sets the city value for this WsServer.
	 * 
	 * @param city
	 */
	public void setCity(String city) {
		this.city = city;
	}

	/**
	 * Gets the name value for this WsServer.
	 * 
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name value for this WsServer.
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the host value for this WsServer.
	 * 
	 * @return host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Sets the host value for this WsServer.
	 * 
	 * @param host
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * Gets the servertype value for this WsServer.
	 * 
	 * @return servertype
	 */
	public String getServertype() {
		return servertype;
	}

	/**
	 * Sets the servertype value for this WsServer.
	 * 
	 * @param servertype
	 */
	public void setServertype(String servertype) {
		this.servertype = servertype;
	}

	/**
	 * Gets the longitude value for this WsServer.
	 * 
	 * @return longitude
	 */
	public float getLongitude() {
		return longitude;
	}

	/**
	 * Sets the longitude value for this WsServer.
	 * 
	 * @param longitude
	 */
	public void setLongitude(float longitude) {
		this.longitude = longitude;
	}

	/**
	 * Gets the latitude value for this WsServer.
	 * 
	 * @return latitude
	 */
	public float getLatitude() {
		return latitude;
	}

	/**
	 * Sets the latitude value for this WsServer.
	 * 
	 * @param latitude
	 */
	public void setLatitude(float latitude) {
		this.latitude = latitude;
	}
	

	/**
	 * Gets the wireguardPublicKey value for this WsServer.
	 * 
	 * @return name
	 */
	public String getWireguardPublicKey() {
		return wireguardPublicKey;
	}

	/**
	 * Sets the wireguardPublicKey value for this WsServer.
	 * 
	 * @param name
	 */
	public void setWireguardPublicKey(String wireguardPublicKey) {
		this.wireguardPublicKey = wireguardPublicKey;
	}

}
