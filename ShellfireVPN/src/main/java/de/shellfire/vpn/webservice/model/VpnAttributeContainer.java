/**
 * VpnAttributeContainer.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.shellfire.vpn.webservice.model;

public class VpnAttributeContainer implements java.io.Serializable {
	private String containerName;

	private VpnAttributeElement[] elements;

	public VpnAttributeContainer() {
	}

	public VpnAttributeContainer(String containerName, VpnAttributeElement[] elements) {
		this.containerName = containerName;
		this.elements = elements;
	}

	/**
	 * Gets the containerName value for this VpnAttributeContainer.
	 * 
	 * @return containerName
	 */
	public String getContainerName() {
		return containerName;
	}

	/**
	 * Sets the containerName value for this VpnAttributeContainer.
	 * 
	 * @param containerName
	 */
	public void setContainerName(String containerName) {
		this.containerName = containerName;
	}

	/**
	 * Gets the elements value for this VpnAttributeContainer.
	 * 
	 * @return elements
	 */
	public VpnAttributeElement[] getElements() {
		return elements;
	}

	/**
	 * Sets the elements value for this VpnAttributeContainer.
	 * 
	 * @param elements
	 */
	public void setElements(VpnAttributeElement[] elements) {
		this.elements = elements;
	}

}
