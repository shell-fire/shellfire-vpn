/**
 * VpnAttributeContainer.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.shellfire.www.webservice.sf_soap_php;

public class VpnAttributeContainer  implements java.io.Serializable {
    private java.lang.String containerName;

    private de.shellfire.www.webservice.sf_soap_php.VpnAttributeElement[] elements;

    public VpnAttributeContainer() {
    }

    public VpnAttributeContainer(
           java.lang.String containerName,
           de.shellfire.www.webservice.sf_soap_php.VpnAttributeElement[] elements) {
           this.containerName = containerName;
           this.elements = elements;
    }


    /**
     * Gets the containerName value for this VpnAttributeContainer.
     * 
     * @return containerName
     */
    public java.lang.String getContainerName() {
        return containerName;
    }


    /**
     * Sets the containerName value for this VpnAttributeContainer.
     * 
     * @param containerName
     */
    public void setContainerName(java.lang.String containerName) {
        this.containerName = containerName;
    }


    /**
     * Gets the elements value for this VpnAttributeContainer.
     * 
     * @return elements
     */
    public de.shellfire.www.webservice.sf_soap_php.VpnAttributeElement[] getElements() {
        return elements;
    }


    /**
     * Sets the elements value for this VpnAttributeContainer.
     * 
     * @param elements
     */
    public void setElements(de.shellfire.www.webservice.sf_soap_php.VpnAttributeElement[] elements) {
        this.elements = elements;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof VpnAttributeContainer)) return false;
        VpnAttributeContainer other = (VpnAttributeContainer) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.containerName==null && other.getContainerName()==null) || 
             (this.containerName!=null &&
              this.containerName.equals(other.getContainerName()))) &&
            ((this.elements==null && other.getElements()==null) || 
             (this.elements!=null &&
              java.util.Arrays.equals(this.elements, other.getElements())));
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
        if (getContainerName() != null) {
            _hashCode += getContainerName().hashCode();
        }
        if (getElements() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getElements());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getElements(), i);
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
        new org.apache.axis.description.TypeDesc(VpnAttributeContainer.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("https://www.shellfire.de/webservice/sf_soap.php", "VpnAttributeContainer"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("containerName");
        elemField.setXmlName(new javax.xml.namespace.QName("", "containerName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("elements");
        elemField.setXmlName(new javax.xml.namespace.QName("", "elements"));
        elemField.setXmlType(new javax.xml.namespace.QName("https://www.shellfire.de/webservice/sf_soap.php", "VpnAttributeElement"));
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
