/**
 * TrayMessage.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.shellfire.vpn.webservice.model;

public class TrayMessage  implements java.io.Serializable {
    private java.lang.String header;

    private java.lang.String text;

    private java.lang.String buttontext;

    public TrayMessage() {
    }

    public TrayMessage(
           java.lang.String header,
           java.lang.String text,
           java.lang.String buttontext) {
           this.header = header;
           this.text = text;
           this.buttontext = buttontext;
    }


    /**
     * Gets the header value for this TrayMessage.
     * 
     * @return header
     */
    public java.lang.String getHeader() {
        return header;
    }


    /**
     * Sets the header value for this TrayMessage.
     * 
     * @param header
     */
    public void setHeader(java.lang.String header) {
        this.header = header;
    }


    /**
     * Gets the text value for this TrayMessage.
     * 
     * @return text
     */
    public java.lang.String getText() {
        return text;
    }


    /**
     * Sets the text value for this TrayMessage.
     * 
     * @param text
     */
    public void setText(java.lang.String text) {
        this.text = text;
    }


    /**
     * Gets the buttontext value for this TrayMessage.
     * 
     * @return buttontext
     */
    public java.lang.String getButtontext() {
        return buttontext;
    }


    /**
     * Sets the buttontext value for this TrayMessage.
     * 
     * @param buttontext
     */
    public void setButtontext(java.lang.String buttontext) {
        this.buttontext = buttontext;
    }
}
