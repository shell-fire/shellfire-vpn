/**
 * WsServer.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.shellfire.www.webservice.sf_soap_php;

public class WsServer  implements java.io.Serializable {
    private int vpnServerId;

    private java.lang.String country;

    private java.lang.String city;

    private java.lang.String name;

    private java.lang.String host;

    private java.lang.String servertype;

    private float longitude;

    private float latitude;

    public WsServer() {
    }

    public WsServer(
           int vpnServerId,
           java.lang.String country,
           java.lang.String city,
           java.lang.String name,
           java.lang.String host,
           java.lang.String servertype,
           float longitude,
           float latitude) {
           this.vpnServerId = vpnServerId;
           this.country = country;
           this.city = city;
           this.name = name;
           this.host = host;
           this.servertype = servertype;
           this.longitude = longitude;
           this.latitude = latitude;
    }


    /**
     * Gets the vpnServerId value for this WsServer.
     * 
     * @return vpnServerId
     */
    public int getVpnServerId() {
        return vpnServerId;
    }


    /**
     * Sets the vpnServerId value for this WsServer.
     * 
     * @param vpnServerId
     */
    public void setVpnServerId(int vpnServerId) {
        this.vpnServerId = vpnServerId;
    }


    /**
     * Gets the country value for this WsServer.
     * 
     * @return country
     */
    public java.lang.String getCountry() {
        return country;
    }


    /**
     * Sets the country value for this WsServer.
     * 
     * @param country
     */
    public void setCountry(java.lang.String country) {
        this.country = country;
    }


    /**
     * Gets the city value for this WsServer.
     * 
     * @return city
     */
    public java.lang.String getCity() {
        return city;
    }


    /**
     * Sets the city value for this WsServer.
     * 
     * @param city
     */
    public void setCity(java.lang.String city) {
        this.city = city;
    }


    /**
     * Gets the name value for this WsServer.
     * 
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this WsServer.
     * 
     * @param name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }


    /**
     * Gets the host value for this WsServer.
     * 
     * @return host
     */
    public java.lang.String getHost() {
        return host;
    }


    /**
     * Sets the host value for this WsServer.
     * 
     * @param host
     */
    public void setHost(java.lang.String host) {
        this.host = host;
    }


    /**
     * Gets the servertype value for this WsServer.
     * 
     * @return servertype
     */
    public java.lang.String getServertype() {
        return servertype;
    }


    /**
     * Sets the servertype value for this WsServer.
     * 
     * @param servertype
     */
    public void setServertype(java.lang.String servertype) {
        this.servertype = servertype;
    }


    /**
     * Gets the longitude value for this WsServer.
     * 
     * @return longitude
     */
    public float getLongitude() {
        return longitude;
    }


    /**
     * Sets the longitude value for this WsServer.
     * 
     * @param longitude
     */
    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }


    /**
     * Gets the latitude value for this WsServer.
     * 
     * @return latitude
     */
    public float getLatitude() {
        return latitude;
    }


    /**
     * Sets the latitude value for this WsServer.
     * 
     * @param latitude
     */
    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof WsServer)) return false;
        WsServer other = (WsServer) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            this.vpnServerId == other.getVpnServerId() &&
            ((this.country==null && other.getCountry()==null) || 
             (this.country!=null &&
              this.country.equals(other.getCountry()))) &&
            ((this.city==null && other.getCity()==null) || 
             (this.city!=null &&
              this.city.equals(other.getCity()))) &&
            ((this.name==null && other.getName()==null) || 
             (this.name!=null &&
              this.name.equals(other.getName()))) &&
            ((this.host==null && other.getHost()==null) || 
             (this.host!=null &&
              this.host.equals(other.getHost()))) &&
            ((this.servertype==null && other.getServertype()==null) || 
             (this.servertype!=null &&
              this.servertype.equals(other.getServertype()))) &&
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
        _hashCode += getVpnServerId();
        if (getCountry() != null) {
            _hashCode += getCountry().hashCode();
        }
        if (getCity() != null) {
            _hashCode += getCity().hashCode();
        }
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        if (getHost() != null) {
            _hashCode += getHost().hashCode();
        }
        if (getServertype() != null) {
            _hashCode += getServertype().hashCode();
        }
        _hashCode += new Float(getLongitude()).hashCode();
        _hashCode += new Float(getLatitude()).hashCode();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(WsServer.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("https://www.shellfire.de/webservice/sf_soap.php", "WsServer"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("vpnServerId");
        elemField.setXmlName(new javax.xml.namespace.QName("", "vpnServerId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
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
        elemField.setFieldName("name");
        elemField.setXmlName(new javax.xml.namespace.QName("", "name"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("host");
        elemField.setXmlName(new javax.xml.namespace.QName("", "host"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("servertype");
        elemField.setXmlName(new javax.xml.namespace.QName("", "servertype"));
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
