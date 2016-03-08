/**
 * WsGeoPosition.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.shellfire.www.webservice.sf_soap_php;

public class WsGeoPosition  implements java.io.Serializable {
    private java.lang.String country;

    private java.lang.String city;

    private float longitude;

    private float latitude;

    public WsGeoPosition() {
    }

    public WsGeoPosition(
           java.lang.String country,
           java.lang.String city,
           float longitude,
           float latitude) {
           this.country = country;
           this.city = city;
           this.longitude = longitude;
           this.latitude = latitude;
    }


    /**
     * Gets the country value for this WsGeoPosition.
     * 
     * @return country
     */
    public java.lang.String getCountry() {
        return country;
    }


    /**
     * Sets the country value for this WsGeoPosition.
     * 
     * @param country
     */
    public void setCountry(java.lang.String country) {
        this.country = country;
    }


    /**
     * Gets the city value for this WsGeoPosition.
     * 
     * @return city
     */
    public java.lang.String getCity() {
        return city;
    }


    /**
     * Sets the city value for this WsGeoPosition.
     * 
     * @param city
     */
    public void setCity(java.lang.String city) {
        this.city = city;
    }


    /**
     * Gets the longitude value for this WsGeoPosition.
     * 
     * @return longitude
     */
    public float getLongitude() {
        return longitude;
    }


    /**
     * Sets the longitude value for this WsGeoPosition.
     * 
     * @param longitude
     */
    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }


    /**
     * Gets the latitude value for this WsGeoPosition.
     * 
     * @return latitude
     */
    public float getLatitude() {
        return latitude;
    }


    /**
     * Sets the latitude value for this WsGeoPosition.
     * 
     * @param latitude
     */
    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof WsGeoPosition)) return false;
        WsGeoPosition other = (WsGeoPosition) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.country==null && other.getCountry()==null) || 
             (this.country!=null &&
              this.country.equals(other.getCountry()))) &&
            ((this.city==null && other.getCity()==null) || 
             (this.city!=null &&
              this.city.equals(other.getCity()))) &&
            this.longitude == other.getLongitude() &&
            this.latitude == other.getLatitude();
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
        if (getCountry() != null) {
            _hashCode += getCountry().hashCode();
        }
        if (getCity() != null) {
            _hashCode += getCity().hashCode();
        }
        _hashCode += new Float(getLongitude()).hashCode();
        _hashCode += new Float(getLatitude()).hashCode();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(WsGeoPosition.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("https://www.shellfire.de/webservice/sf_soap.php", "WsGeoPosition"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("country");
        elemField.setXmlName(new javax.xml.namespace.QName("", "country"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("city");
        elemField.setXmlName(new javax.xml.namespace.QName("", "city"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("longitude");
        elemField.setXmlName(new javax.xml.namespace.QName("", "longitude"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "float"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("latitude");
        elemField.setXmlName(new javax.xml.namespace.QName("", "latitude"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "float"));
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
