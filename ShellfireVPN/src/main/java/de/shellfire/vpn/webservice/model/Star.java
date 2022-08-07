/**
 * Star.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.shellfire.vpn.webservice.model;

public class Star implements java.io.Serializable {
	private int numStars;

	private String text;

	public Star() {
	}

	public Star(int numStars, String text) {
		this.numStars = numStars;
		this.text = text;
	}

	/**
	 * Gets the numStars value for this Star.
	 * 
	 * @return numStars
	 */
	public int getNumStars() {
		return numStars;
	}

	/**
	 * Sets the numStars value for this Star.
	 * 
	 * @param numStars
	 */
	public void setNumStars(int numStars) {
		this.numStars = numStars;
	}

	/**
	 * Gets the text value for this Star.
	 * 
	 * @return text
	 */
	public String getText() {
		return text;
	}

	/**
	 * Sets the text value for this Star.
	 * 
	 * @param text
	 */
	public void setText(String text) {
		this.text = text;
	}

}
