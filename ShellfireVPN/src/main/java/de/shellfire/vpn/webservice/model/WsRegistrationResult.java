/**
 * WsRegistrationResult.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.shellfire.vpn.webservice.model;

public class WsRegistrationResult implements java.io.Serializable {
	private boolean registrationOk;

	private String errorMessage;

	private String token;

	public WsRegistrationResult() {
	}

	public WsRegistrationResult(boolean registrationOk, String errorMessage, String token) {
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
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * Sets the errorMessage value for this WsRegistrationResult.
	 * 
	 * @param errorMessage
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	/**
	 * Gets the token value for this WsRegistrationResult.
	 * 
	 * @return token
	 */
	public String getToken() {
		return token;
	}

	/**
	 * Sets the token value for this WsRegistrationResult.
	 * 
	 * @param token
	 */
	public void setToken(String token) {
		this.token = token;
	}

}
