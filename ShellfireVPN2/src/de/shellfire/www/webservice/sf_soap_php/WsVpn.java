/**
 * WsVpn.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.shellfire.www.webservice.sf_soap_php;

public class WsVpn  implements java.io.Serializable {
    private int iVpnId;

    private int iProductTypeId;

    private int iServerId;

    private java.lang.String sUserName;

    private java.lang.String sPassword;

    private java.lang.String eAccountType;

    private java.lang.String sListenHost;

    private java.lang.String eProtocol;

    private int iPremiumUntil;

    public WsVpn() {
    }

    public WsVpn(
           int iVpnId,
           int iProductTypeId,
           int iServerId,
           java.lang.String sUserName,
           java.lang.String sPassword,
           java.lang.String eAccountType,
           java.lang.String sListenHost,
           java.lang.String eProtocol,
           int iPremiumUntil) {
           this.iVpnId = iVpnId;
           this.iProductTypeId = iProductTypeId;
           this.iServerId = iServerId;
           this.sUserName = sUserName;
           this.sPassword = sPassword;
           this.eAccountType = eAccountType;
           this.sListenHost = sListenHost;
           this.eProtocol = eProtocol;
           this.iPremiumUntil = iPremiumUntil;
    }


    /**
     * Gets the iVpnId value for this WsVpn.
     * 
     * @return iVpnId
     */
    public int getIVpnId() {
        return iVpnId;
    }


    /**
     * Sets the iVpnId value for this WsVpn.
     * 
     * @param iVpnId
     */
    public void setIVpnId(int iVpnId) {
        this.iVpnId = iVpnId;
    }


    /**
     * Gets the iProductTypeId value for this WsVpn.
     * 
     * @return iProductTypeId
     */
    public int getIProductTypeId() {
        return iProductTypeId;
    }


    /**
     * Sets the iProductTypeId value for this WsVpn.
     * 
     * @param iProductTypeId
     */
    public void setIProductTypeId(int iProductTypeId) {
        this.iProductTypeId = iProductTypeId;
    }


    /**
     * Gets the iServerId value for this WsVpn.
     * 
     * @return iServerId
     */
    public int getIServerId() {
        return iServerId;
    }


    /**
     * Sets the iServerId value for this WsVpn.
     * 
     * @param iServerId
     */
    public void setIServerId(int iServerId) {
        this.iServerId = iServerId;
    }


    /**
     * Gets the sUserName value for this WsVpn.
     * 
     * @return sUserName
     */
    public java.lang.String getSUserName() {
        return sUserName;
    }


    /**
     * Sets the sUserName value for this WsVpn.
     * 
     * @param sUserName
     */
    public void setSUserName(java.lang.String sUserName) {
        this.sUserName = sUserName;
    }


    /**
     * Gets the sPassword value for this WsVpn.
     * 
     * @return sPassword
     */
    public java.lang.String getSPassword() {
        return sPassword;
    }


    /**
     * Sets the sPassword value for this WsVpn.
     * 
     * @param sPassword
     */
    public void setSPassword(java.lang.String sPassword) {
        this.sPassword = sPassword;
    }


    /**
     * Gets the eAccountType value for this WsVpn.
     * 
     * @return eAccountType
     */
    public java.lang.String getEAccountType() {
        return eAccountType;
    }


    /**
     * Sets the eAccountType value for this WsVpn.
     * 
     * @param eAccountType
     */
    public void setEAccountType(java.lang.String eAccountType) {
        this.eAccountType = eAccountType;
    }


    /**
     * Gets the sListenHost value for this WsVpn.
     * 
     * @return sListenHost
     */
    public java.lang.String getSListenHost() {
        return sListenHost;
    }


    /**
     * Sets the sListenHost value for this WsVpn.
     * 
     * @param sListenHost
     */
    public void setSListenHost(java.lang.String sListenHost) {
        this.sListenHost = sListenHost;
    }


    /**
     * Gets the eProtocol value for this WsVpn.
     * 
     * @return eProtocol
     */
    public java.lang.String getEProtocol() {
        return eProtocol;
    }


    /**
     * Sets the eProtocol value for this WsVpn.
     * 
     * @param eProtocol
     */
    public void setEProtocol(java.lang.String eProtocol) {
        this.eProtocol = eProtocol;
    }


    /**
     * Gets the iPremiumUntil value for this WsVpn.
     * 
     * @return iPremiumUntil
     */
    public int getIPremiumUntil() {
        return iPremiumUntil;
    }


    /**
     * Sets the iPremiumUntil value for this WsVpn.
     * 
     * @param iPremiumUntil
     */
    public void setIPremiumUntil(int iPremiumUntil) {
        this.iPremiumUntil = iPremiumUntil;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof WsVpn)) return false;
        WsVpn other = (WsVpn) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            this.iVpnId == other.getIVpnId() &&
            this.iProductTypeId == other.getIProductTypeId() &&
            this.iServerId == other.getIServerId() &&
            ((this.sUserName==null && other.getSUserName()==null) || 
             (this.sUserName!=null &&
              this.sUserName.equals(other.getSUserName()))) &&
            ((this.sPassword==null && other.getSPassword()==null) || 
             (this.sPassword!=null &&
              this.sPassword.equals(other.getSPassword()))) &&
            ((this.eAccountType==null && other.getEAccountType()==null) || 
             (this.eAccountType!=null &&
              this.eAccountType.equals(other.getEAccountType()))) &&
            ((this.sListenHost==null && other.getSListenHost()==null) || 
             (this.sListenHost!=null &&
              this.sListenHost.equals(other.getSListenHost()))) &&
            ((this.eProtocol==null && other.getEProtocol()==null) || 
             (this.eProtocol!=null &&
              this.eProtocol.equals(other.getEProtocol()))) &&
            this.iPremiumUntil == other.getIPremiumUntil();
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
        _hashCode += getIVpnId();
        _hashCode += getIProductTypeId();
        _hashCode += getIServerId();
        if (getSUserName() != null) {
            _hashCode += getSUserName().hashCode();
        }
        if (getSPassword() != null) {
            _hashCode += getSPassword().hashCode();
        }
        if (getEAccountType() != null) {
            _hashCode += getEAccountType().hashCode();
        }
        if (getSListenHost() != null) {
            _hashCode += getSListenHost().hashCode();
        }
        if (getEProtocol() != null) {
            _hashCode += getEProtocol().hashCode();
        }
        _hashCode += getIPremiumUntil();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(WsVpn.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("https://www.shellfire.de/webservice/sf_soap.php", "WsVpn"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("IVpnId");
        elemField.setXmlName(new javax.xml.namespace.QName("", "iVpnId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("IProductTypeId");
        elemField.setXmlName(new javax.xml.namespace.QName("", "iProductTypeId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("IServerId");
        elemField.setXmlName(new javax.xml.namespace.QName("", "iServerId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("SUserName");
        elemField.setXmlName(new javax.xml.namespace.QName("", "sUserName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("SPassword");
        elemField.setXmlName(new javax.xml.namespace.QName("", "sPassword"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("EAccountType");
        elemField.setXmlName(new javax.xml.namespace.QName("", "eAccountType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("SListenHost");
        elemField.setXmlName(new javax.xml.namespace.QName("", "sListenHost"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("EProtocol");
        elemField.setXmlName(new javax.xml.namespace.QName("", "eProtocol"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("IPremiumUntil");
        elemField.setXmlName(new javax.xml.namespace.QName("", "iPremiumUntil"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
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
