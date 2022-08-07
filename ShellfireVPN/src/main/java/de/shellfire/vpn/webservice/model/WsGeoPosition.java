/**
 * WsGeoPosition.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.shellfire.vpn.webservice.model;

public class WsGeoPosition implements java.io.Serializable {
	private String country;

	private String city;

	private float longitude;

	private float latitude;

	public WsGeoPosition() {
	}

	public WsGeoPosition(String country, String city, float longitude, float latitude) {
		this.country = country;
		this.city = city;
		this.longitude = longitude;
		this.latitude = latitude;
	}

	/**
	 * Gets the country value for this WsGeoPosition.
	 * 
	 * @return country
	 */
	public String getCountry() {
		return country;
	}

	/**
	 * Sets the country value for this WsGeoPosition.
	 * 
	 * @param country
	 */
	public void setCountry(String country) {
		this.country = country;
	}

	/**
	 * Gets the city value for this WsGeoPosition.
	 * 
	 * @return city
	 */
	public String getCity() {
		return city;
	}

	/**
	 * Sets the city value for this WsGeoPosition.
	 * 
	 * @param city
	 */
	public void setCity(String city) {
		this.city = city;
	}

	/**
	 * Gets the longitude value for this WsGeoPosition.
	 * 
	 * @return longitude
	 */
	public float getLongitude() {
		return longitude;
	}

	/**
	 * Sets the longitude value for this WsGeoPosition.
	 * 
	 * @param longitude
	 */
	public void setLongitude(float longitude) {
		this.longitude = longitude;
	}

	/**
	 * Gets the latitude value for this WsGeoPosition.
	 * 
	 * @return latitude
	 */
	public float getLatitude() {
		return latitude;
	}

	/**
	 * Sets the latitude value for this WsGeoPosition.
	 * 
	 * @param latitude
	 */
	public void setLatitude(float latitude) {
		this.latitude = latitude;
	}

}
