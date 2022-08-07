/**
 * WsServer.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.shellfire.vpn.webservice.model;

public class WsHelpItem implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6159966437752302716L;

	private String header;

	private String text;

	public WsHelpItem() {
	}

	public WsHelpItem(String header, String text) {
			this.header = header;
			this.text = text;
	}

	public String getHeader() {
		return this.header;
	}
	public String getText() {
		return this.text;
	}
	
	public void setHeader(String header) {
		this.header = header;
	}
	public void setText(String text) {
		this.text = text;
	}
	
}
