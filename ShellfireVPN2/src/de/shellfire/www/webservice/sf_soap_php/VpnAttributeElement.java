/**
 * VpnAttributeElement.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.shellfire.www.webservice.sf_soap_php;

public class VpnAttributeElement  implements java.io.Serializable {
    private java.lang.String name;

    private de.shellfire.www.webservice.sf_soap_php.Entry free;

    private de.shellfire.www.webservice.sf_soap_php.Entry premium;

    private de.shellfire.www.webservice.sf_soap_php.Entry pp;

    public VpnAttributeElement() {
    }

    public VpnAttributeElement(
           java.lang.String name,
           de.shellfire.www.webservice.sf_soap_php.Entry free,
           de.shellfire.www.webservice.sf_soap_php.Entry premium,
           de.shellfire.www.webservice.sf_soap_php.Entry pp) {
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
    public de.shellfire.www.webservice.sf_soap_php.Entry getFree() {
        return free;
    }


    /**
     * Sets the free value for this VpnAttributeElement.
     * 
     * @param free
     */
    public void setFree(de.shellfire.www.webservice.sf_soap_php.Entry free) {
        this.free = free;
    }


    /**
     * Gets the premium value for this VpnAttributeElement.
     * 
     * @return premium
     */
    public de.shellfire.www.webservice.sf_soap_php.Entry getPremium() {
        return premium;
    }


    /**
     * Sets the premium value for this VpnAttributeElement.
     * 
     * @param premium
     */
    public void setPremium(de.shellfire.www.webservice.sf_soap_php.Entry premium) {
        this.premium = premium;
    }


    /**
     * Gets the pp value for this VpnAttributeElement.
     * 
     * @return pp
     */
    public de.shellfire.www.webservice.sf_soap_php.Entry getPp() {
        return pp;
    }


    /**
     * Sets the pp value for this VpnAttributeElement.
     * 
     * @param pp
     */
    public void setPp(de.shellfire.www.webservice.sf_soap_php.Entry pp) {
        this.pp = pp;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof VpnAttributeElement)) return false;
        VpnAttributeElement other = (VpnAttributeElement) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.name==null && other.getName()==null) || 
             (this.name!=null &&
              this.name.equals(other.getName()))) &&
            ((this.free==null && other.getFree()==null) || 
             (this.free!=null &&
              this.free.equals(other.getFree()))) &&
            ((this.premium==null && other.getPremium()==null) || 
             (this.premium!=null &&
              this.premium.equals(other.getPremium()))) &&
            ((this.pp==null && other.getPp()==null) || 
             (this.pp!=null &&
              this.pp.equals(other.getPp())));
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
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        if (getFree() != null) {
            _hashCode += getFree().hashCode();
        }
        if (getPremium() != null) {
            _hashCode += getPremium().hashCode();
        }
        if (getPp() != null) {
            _hashCode += getPp().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(VpnAttributeElement.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("https://www.shellfire.de/webservice/sf_soap.php", "VpnAttributeElement"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("name");
        elemField.setXmlName(new javax.xml.namespace.QName("", "name"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("free");
        elemField.setXmlName(new javax.xml.namespace.QName("", "free"));
        elemField.setXmlType(new javax.xml.namespace.QName("https://www.shellfire.de/webservice/sf_soap.php", "Entry"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("premium");
        elemField.setXmlName(new javax.xml.namespace.QName("", "premium"));
        elemField.setXmlType(new javax.xml.namespace.QName("https://www.shellfire.de/webservice/sf_soap.php", "Entry"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("pp");
        elemField.setXmlName(new javax.xml.namespace.QName("", "pp"));
        elemField.setXmlType(new javax.xml.namespace.QName("https://www.shellfire.de/webservice/sf_soap.php", "Entry"));
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
