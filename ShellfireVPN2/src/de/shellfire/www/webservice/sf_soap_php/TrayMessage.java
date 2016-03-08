/**
 * TrayMessage.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.shellfire.www.webservice.sf_soap_php;

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

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof TrayMessage)) return false;
        TrayMessage other = (TrayMessage) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.header==null && other.getHeader()==null) || 
             (this.header!=null &&
              this.header.equals(other.getHeader()))) &&
            ((this.text==null && other.getText()==null) || 
             (this.text!=null &&
              this.text.equals(other.getText()))) &&
            ((this.buttontext==null && other.getButtontext()==null) || 
             (this.buttontext!=null &&
              this.buttontext.equals(other.getButtontext())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getHeader() != null) {
            _hashCode += getHeader().hashCode();
        }
        if (getText() != null) {
            _hashCode += getText().hashCode();
        }
        if (getButtontext() != null) {
            _hashCode += getButtontext().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(TrayMessage.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("https://www.shellfire.de/webservice/sf_soap.php", "TrayMessage"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("header");
        elemField.setXmlName(new javax.xml.namespace.QName("", "header"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("text");
        elemField.setXmlName(new javax.xml.namespace.QName("", "text"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("buttontext");
        elemField.setXmlName(new javax.xml.namespace.QName("", "buttontext"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
