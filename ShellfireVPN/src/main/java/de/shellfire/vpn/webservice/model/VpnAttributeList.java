/**
 * VpnAttributeList.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.shellfire.vpn.webservice.model;

public class VpnAttributeList  implements java.io.Serializable {
    private VpnAttributeContainer[] containers;

    public VpnAttributeList() {
    }

    public VpnAttributeList(
           VpnAttributeContainer[] containers) {
           this.containers = containers;
    }


    /**
     * Gets the containers value for this VpnAttributeList.
     * 
     * @return containers
     */
    public VpnAttributeContainer[] getContainers() {
        return containers;
    }


    /**
     * Sets the containers value for this VpnAttributeList.
     * 
     * @param containers
     */
    public void setContainers(VpnAttributeContainer[] containers) {
        this.containers = containers;
    }

}
