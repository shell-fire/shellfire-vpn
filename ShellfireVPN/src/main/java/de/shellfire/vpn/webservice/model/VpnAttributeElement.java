/**
 * VpnAttributeElement.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.shellfire.vpn.webservice.model;

public class VpnAttributeElement  implements java.io.Serializable {
    private java.lang.String name;

    private Entry free;

    private Entry premium;

    private Entry pp;

    public VpnAttributeElement() {
    }

    public VpnAttributeElement(
           java.lang.String name,
           Entry free,
           Entry premium,
           Entry pp) {
           this.name = name;
           this.free = free;
           this.premium = premium;
           this.pp = pp;
    }


    /**
     * Gets the name value for this VpnAttributeElement.
     * 
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this VpnAttributeElement.
     * 
     * @param name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }


    /**
     * Gets the free value for this VpnAttributeElement.
     * 
     * @return free
     */
    public Entry getFree() {
        return free;
    }


    /**
     * Sets the free value for this VpnAttributeElement.
     * 
     * @param free
     */
    public void setFree(Entry free) {
        this.free = free;
    }


    /**
     * Gets the premium value for this VpnAttributeElement.
     * 
     * @return premium
     */
    public Entry getPremium() {
        return premium;
    }


    /**
     * Sets the premium value for this VpnAttributeElement.
     * 
     * @param premium
     */
    public void setPremium(Entry premium) {
        this.premium = premium;
    }


    /**
     * Gets the pp value for this VpnAttributeElement.
     * 
     * @return pp
     */
    public Entry getPp() {
        return pp;
    }


    /**
     * Sets the pp value for this VpnAttributeElement.
     * 
     * @param pp
     */
    public void setPp(Entry pp) {
        this.pp = pp;
    }

}
