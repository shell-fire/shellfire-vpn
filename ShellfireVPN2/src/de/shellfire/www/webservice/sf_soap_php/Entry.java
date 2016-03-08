/**
 * Entry.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.shellfire.www.webservice.sf_soap_php;

public class Entry  implements java.io.Serializable {
    private boolean boolEntry;

    private boolean starEntry;

    private boolean stringEntry;

    private boolean bool;

    private de.shellfire.www.webservice.sf_soap_php.Star star;

    private java.lang.String text;

    public Entry() {
    }

    public Entry(
           boolean boolEntry,
           boolean starEntry,
           boolean stringEntry,
           boolean bool,
           de.shellfire.www.webservice.sf_soap_php.Star star,
           java.lang.String text) {
           this.boolEntry = boolEntry;
           this.starEntry = starEntry;
           this.stringEntry = stringEntry;
           this.bool = bool;
           this.star = star;
           this.text = text;
    }


    /**
     * Gets the boolEntry value for this Entry.
     * 
     * @return boolEntry
     */
    public boolean isBoolEntry() {
        return boolEntry;
    }


    /**
     * Sets the boolEntry value for this Entry.
     * 
     * @param boolEntry
     */
    public void setBoolEntry(boolean boolEntry) {
        this.boolEntry = boolEntry;
    }


    /**
     * Gets the starEntry value for this Entry.
     * 
     * @return starEntry
     */
    public boolean isStarEntry() {
        return starEntry;
    }


    /**
     * Sets the starEntry value for this Entry.
     * 
     * @param starEntry
     */
    public void setStarEntry(boolean starEntry) {
        this.starEntry = starEntry;
    }


    /**
     * Gets the stringEntry value for this Entry.
     * 
     * @return stringEntry
     */
    public boolean isStringEntry() {
        return stringEntry;
    }


    /**
     * Sets the stringEntry value for this Entry.
     * 
     * @param stringEntry
     */
    public void setStringEntry(boolean stringEntry) {
        this.stringEntry = stringEntry;
    }


    /**
     * Gets the bool value for this Entry.
     * 
     * @return bool
     */
    public boolean isBool() {
        return bool;
    }


    /**
     * Sets the bool value for this Entry.
     * 
     * @param bool
     */
    public void setBool(boolean bool) {
        this.bool = bool;
    }


    /**
     * Gets the star value for this Entry.
     * 
     * @return star
     */
    public de.shellfire.www.webservice.sf_soap_php.Star getStar() {
        return star;
    }


    /**
     * Sets the star value for this Entry.
     * 
     * @param star
     */
    public void setStar(de.shellfire.www.webservice.sf_soap_php.Star star) {
        this.star = star;
    }


    /**
     * Gets the text value for this Entry.
     * 
     * @return text
     */
    public java.lang.String getText() {
        return text;
    }


    /**
     * Sets the text value for this Entry.
     * 
     * @param text
     */
    public void setText(java.lang.String text) {
        this.text = text;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Entry)) return false;
        Entry other = (Entry) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            this.boolEntry == other.isBoolEntry() &&
            this.starEntry == other.isStarEntry() &&
            this.stringEntry == other.isStringEntry() &&
            this.bool == other.isBool() &&
            ((this.star==null && other.getStar()==null) || 
             (this.star!=null &&
              this.star.equals(other.getStar()))) &&
            ((this.text==null && other.getText()==null) || 
             (this.text!=null &&
              this.text.equals(other.getText())));
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
        _hashCode += (isBoolEntry() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        _hashCode += (isStarEntry() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        _hashCode += (isStringEntry() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        _hashCode += (isBool() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        if (getStar() != null) {
            _hashCode += getStar().hashCode();
        }
        if (getText() != null) {
            _hashCode += getText().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Entry.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("https://www.shellfire.de/webservice/sf_soap.php", "Entry"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("boolEntry");
        elemField.setXmlName(new javax.xml.namespace.QName("", "boolEntry"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("starEntry");
        elemField.setXmlName(new javax.xml.namespace.QName("", "starEntry"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("stringEntry");
        elemField.setXmlName(new javax.xml.namespace.QName("", "stringEntry"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("bool");
        elemField.setXmlName(new javax.xml.namespace.QName("", "bool"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("star");
        elemField.setXmlName(new javax.xml.namespace.QName("", "star"));
        elemField.setXmlType(new javax.xml.namespace.QName("https://www.shellfire.de/webservice/sf_soap.php", "Star"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("text");
        elemField.setXmlName(new javax.xml.namespace.QName("", "text"));
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
