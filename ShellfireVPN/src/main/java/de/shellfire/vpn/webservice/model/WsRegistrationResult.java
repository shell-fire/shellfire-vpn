/**
 * WsRegistrationResult.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.shellfire.vpn.webservice.model;

public class WsRegistrationResult implements java.io.Serializable {
	private boolean registrationOk;

	private java.lang.String errorMessage;

	private java.lang.String token;

	public WsRegistrationResult() {
	}

	public WsRegistrationResult(boolean registrationOk, java.lang.String errorMessage, java.lang.String token) {
		this.registrationOk = registrationOk;
		this.errorMessage = errorMessage;
		this.token = token;
	}

	/**
	 * Gets the registrationOk value for this WsRegistrationResult.
	 * 
	 * @return registrationOk
	 */
	public boolean isRegistrationOk() {
		return registrationOk;
	}

	/**
	 * Sets the registrationOk value for this WsRegistrationResult.
	 * 
	 * @param registrationOk
	 */
	public void setRegistrationOk(boolean registrationOk) {
		this.registrationOk = registrationOk;
	}

	/**
	 * Gets the errorMessage value for this WsRegistrationResult.
	 * 
	 * @return errorMessage
	 */
	public java.lang.String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * Sets the errorMessage value for this WsRegistrationResult.
	 * 
	 * @param errorMessage
	 */
	public void setErrorMessage(java.lang.String errorMessage) {
		this.errorMessage = errorMessage;
	}

	/**
	 * Gets the token value for this WsRegistrationResult.
	 * 
	 * @return token
	 */
	public java.lang.String getToken() {
		return token;
	}

	/**
	 * Sets the token value for this WsRegistrationResult.
	 * 
	 * @param token
	 */
	public void setToken(java.lang.String token) {
		this.token = token;
	}

}
