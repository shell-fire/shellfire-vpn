/**
 * WsUpgradeResult.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.shellfire.www.webservice.sf_soap_php;

public class WsUpgradeResult  implements java.io.Serializable {
    private java.lang.String error;

    private java.lang.String eAccountType;

    private int iUpgradeUntil;

    private int upgradeSuccesful;

    public WsUpgradeResult() {
    }

    public WsUpgradeResult(
           java.lang.String error,
           java.lang.String eAccountType,
           int iUpgradeUntil,
           int upgradeSuccesful) {
           this.error = error;
           this.eAccountType = eAccountType;
           this.iUpgradeUntil = iUpgradeUntil;
           this.upgradeSuccesful = upgradeSuccesful;
    }


    /**
     * Gets the error value for this WsUpgradeResult.
     * 
     * @return error
     */
    public java.lang.String getError() {
        return error;
    }


    /**
     * Sets the error value for this WsUpgradeResult.
     * 
     * @param error
     */
    public void setError(java.lang.String error) {
        this.error = error;
    }


    /**
     * Gets the eAccountType value for this WsUpgradeResult.
     * 
     * @return eAccountType
     */
    public java.lang.String getEAccountType() {
        return eAccountType;
    }


    /**
     * Sets the eAccountType value for this WsUpgradeResult.
     * 
     * @param eAccountType
     */
    public void setEAccountType(java.lang.String eAccountType) {
        this.eAccountType = eAccountType;
    }


    /**
     * Gets the iUpgradeUntil value for this WsUpgradeResult.
     * 
     * @return iUpgradeUntil
     */
    public int getIUpgradeUntil() {
        return iUpgradeUntil;
    }


    /**
     * Sets the iUpgradeUntil value for this WsUpgradeResult.
     * 
     * @param iUpgradeUntil
     */
    public void setIUpgradeUntil(int iUpgradeUntil) {
        this.iUpgradeUntil = iUpgradeUntil;
    }


    /**
     * Gets the upgradeSuccesful value for this WsUpgradeResult.
     * 
     * @return upgradeSuccesful
     */
    public int getUpgradeSuccesful() {
        return upgradeSuccesful;
    }


    /**
     * Sets the upgradeSuccesful value for this WsUpgradeResult.
     * 
     * @param upgradeSuccesful
     */
    public void setUpgradeSuccesful(int upgradeSuccesful) {
        this.upgradeSuccesful = upgradeSuccesful;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof WsUpgradeResult)) return false;
        WsUpgradeResult other = (WsUpgradeResult) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.error==null && other.getError()==null) || 
             (this.error!=null &&
              this.error.equals(other.getError()))) &&
            ((this.eAccountType==null && other.getEAccountType()==null) || 
             (this.eAccountType!=null &&
              this.eAccountType.equals(other.getEAccountType()))) &&
            this.iUpgradeUntil == other.getIUpgradeUntil() &&
            this.upgradeSuccesful == other.getUpgradeSuccesful();
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
        if (getError() != null) {
            _hashCode += getError().hashCode();
        }
        if (getEAccountType() != null) {
            _hashCode += getEAccountType().hashCode();
        }
        _hashCode += getIUpgradeUntil();
        _hashCode += getUpgradeSuccesful();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(WsUpgradeResult.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("https://www.shellfire.de/webservice/sf_soap.php", "WsUpgradeResult"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("error");
        elemField.setXmlName(new javax.xml.namespace.QName("", "error"));
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
        elemField.setFieldName("IUpgradeUntil");
        elemField.setXmlName(new javax.xml.namespace.QName("", "iUpgradeUntil"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("upgradeSuccesful");
        elemField.setXmlName(new javax.xml.namespace.QName("", "upgradeSuccesful"));
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
