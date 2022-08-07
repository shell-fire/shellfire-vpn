/**
 * TrayMessage.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.shellfire.vpn.webservice.model;

public class TrayMessage implements java.io.Serializable {
	private String header;

	private String text;

	private String buttontext;

	public TrayMessage() {
	}

	public TrayMessage(String header, String text, String buttontext) {
		this.header = header;
		this.text = text;
	}

	/**
	 * Gets the header value for this TrayMessage.
	 * 
	 * @return header
	 */
	public String getHeader() {
		return header;
	}

	/**
	 * Sets the header value for this TrayMessage.
	 * 
	 * @param header
	 */
	public void setHeader(String header) {
		this.header = header;
	}

	/**
	 * Gets the text value for this TrayMessage.
	 * 
	 * @return text
	 */
	public String getText() {
		return text;
	}

	/**
	 * Sets the text value for this TrayMessage.
	 * 
	 * @param text
	 */
	public void setText(String text) {
		this.text = text;
	}

}
