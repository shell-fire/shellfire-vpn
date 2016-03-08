/**
 * WsLoginResult.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.shellfire.www.webservice.sf_soap_php;

public class WsLoginResult  implements java.io.Serializable {
    private boolean loggedIn;

    private java.lang.String errorMessage;

    public WsLoginResult() {
    }

    public WsLoginResult(
           boolean loggedIn,
           java.lang.String errorMessage) {
           this.loggedIn = loggedIn;
           this.errorMessage = errorMessage;
    }


    /**
     * Gets the loggedIn value for this WsLoginResult.
     * 
     * @return loggedIn
     */
    public boolean isLoggedIn() {
        return loggedIn;
    }


    /**
     * Sets the loggedIn value for this WsLoginResult.
     * 
     * @param loggedIn
     */
    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }


    /**
     * Gets the errorMessage value for this WsLoginResult.
     * 
     * @return errorMessage
     */
    public java.lang.String getErrorMessage() {
        return errorMessage;
    }


    /**
     * Sets the errorMessage value for this WsLoginResult.
     * 
     * @param errorMessage
     */
    public void setErrorMessage(java.lang.String errorMessage) {
        this.errorMessage = errorMessage;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof WsLoginResult)) return false;
        WsLoginResult other = (WsLoginResult) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            this.loggedIn == other.isLoggedIn() &&
            ((this.errorMessage==null && other.getErrorMessage()==null) || 
             (this.errorMessage!=null &&
              this.errorMessage.equals(other.getErrorMessage())));
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
        _hashCode += (isLoggedIn() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        if (getErrorMessage() != null) {
            _hashCode += getErrorMessage().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(WsLoginResult.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("https://www.shellfire.de/webservice/sf_soap.php", "WsLoginResult"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("loggedIn");
        elemField.setXmlName(new javax.xml.namespace.QName("", "loggedIn"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("errorMessage");
        elemField.setXmlName(new javax.xml.namespace.QName("", "errorMessage"));
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
