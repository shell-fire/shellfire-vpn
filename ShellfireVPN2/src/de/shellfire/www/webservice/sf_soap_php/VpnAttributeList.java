/**
 * VpnAttributeList.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.shellfire.www.webservice.sf_soap_php;

public class VpnAttributeList  implements java.io.Serializable {
    private de.shellfire.www.webservice.sf_soap_php.VpnAttributeContainer[] containers;

    public VpnAttributeList() {
    }

    public VpnAttributeList(
           de.shellfire.www.webservice.sf_soap_php.VpnAttributeContainer[] containers) {
           this.containers = containers;
    }


    /**
     * Gets the containers value for this VpnAttributeList.
     * 
     * @return containers
     */
    public de.shellfire.www.webservice.sf_soap_php.VpnAttributeContainer[] getContainers() {
        return containers;
    }


    /**
     * Sets the containers value for this VpnAttributeList.
     * 
     * @param containers
     */
    public void setContainers(de.shellfire.www.webservice.sf_soap_php.VpnAttributeContainer[] containers) {
        this.containers = containers;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof VpnAttributeList)) return false;
        VpnAttributeList other = (VpnAttributeList) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.containers==null && other.getContainers()==null) || 
             (this.containers!=null &&
              java.util.Arrays.equals(this.containers, other.getContainers())));
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
        if (getContainers() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getContainers());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getContainers(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(VpnAttributeList.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("https://www.shellfire.de/webservice/sf_soap.php", "VpnAttributeList"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("containers");
        elemField.setXmlName(new javax.xml.namespace.QName("", "containers"));
        elemField.setXmlType(new javax.xml.namespace.QName("https://www.shellfire.de/webservice/sf_soap.php", "VpnAttributeContainer"));
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
