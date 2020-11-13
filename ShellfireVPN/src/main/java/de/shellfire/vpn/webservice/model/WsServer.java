/**
 * WsServer.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.shellfire.vpn.webservice.model;

public class WsServer implements java.io.Serializable {
	private int vpnServerId;

	private java.lang.String country;

	private java.lang.String city;

	private java.lang.String name;

	private java.lang.String host;

	private java.lang.String servertype;

	private float longitude;

	private float latitude;

	public WsServer() {
	}

	public WsServer(int vpnServerId, java.lang.String country, java.lang.String city, java.lang.String name, java.lang.String host,
			java.lang.String servertype, float longitude, float latitude) {
		this.vpnServerId = vpnServerId;
		this.country = country;
		this.city = city;
		this.name = name;
		this.host = host;
		this.servertype = servertype;
		this.longitude = longitude;
		this.latitude = latitude;
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
	public java.lang.String getCountry() {
		return country;
	}

	/**
	 * Sets the country value for this WsServer.
	 * 
	 * @param country
	 */
	public void setCountry(java.lang.String country) {
		this.country = country;
	}

	/**
	 * Gets the city value for this WsServer.
	 * 
	 * @return city
	 */
	public java.lang.String getCity() {
		return city;
	}

	/**
	 * Sets the city value for this WsServer.
	 * 
	 * @param city
	 */
	public void setCity(java.lang.String city) {
		this.city = city;
	}

	/**
	 * Gets the name value for this WsServer.
	 * 
	 * @return name
	 */
	public java.lang.String getName() {
		return name;
	}

	/**
	 * Sets the name value for this WsServer.
	 * 
	 * @param name
	 */
	public void setName(java.lang.String name) {
		this.name = name;
	}

	/**
	 * Gets the host value for this WsServer.
	 * 
	 * @return host
	 */
	public java.lang.String getHost() {
		return host;
	}

	/**
	 * Sets the host value for this WsServer.
	 * 
	 * @param host
	 */
	public void setHost(java.lang.String host) {
		this.host = host;
	}

	/**
	 * Gets the servertype value for this WsServer.
	 * 
	 * @return servertype
	 */
	public java.lang.String getServertype() {
		return servertype;
	}

	/**
	 * Sets the servertype value for this WsServer.
	 * 
	 * @param servertype
	 */
	public void setServertype(java.lang.String servertype) {
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
}
