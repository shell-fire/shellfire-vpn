/**
 * ShellfireWebServiceServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.shellfire.www.webservice.sf_soap_php;

public class ShellfireWebServiceServiceLocator extends org.apache.axis.client.Service implements de.shellfire.www.webservice.sf_soap_php.ShellfireWebServiceService {

    public ShellfireWebServiceServiceLocator() {
    }


    public ShellfireWebServiceServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public ShellfireWebServiceServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for ShellfireWebServicePort
    private java.lang.String ShellfireWebServicePort_address = "https://www.shellfire.de/webservice/sf_soap.php";

    public java.lang.String getShellfireWebServicePortAddress() {
        return ShellfireWebServicePort_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String ShellfireWebServicePortWSDDServiceName = "ShellfireWebServicePort";

    public java.lang.String getShellfireWebServicePortWSDDServiceName() {
        return ShellfireWebServicePortWSDDServiceName;
    }

    public void setShellfireWebServicePortWSDDServiceName(java.lang.String name) {
        ShellfireWebServicePortWSDDServiceName = name;
    }

    public de.shellfire.www.webservice.sf_soap_php.ShellfireWebServicePort getShellfireWebServicePort() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(ShellfireWebServicePort_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getShellfireWebServicePort(endpoint);
    }

    public de.shellfire.www.webservice.sf_soap_php.ShellfireWebServicePort getShellfireWebServicePort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            de.shellfire.www.webservice.sf_soap_php.ShellfireWebServiceBindingStub _stub = new de.shellfire.www.webservice.sf_soap_php.ShellfireWebServiceBindingStub(portAddress, this);
            _stub.setPortName(getShellfireWebServicePortWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setShellfireWebServicePortEndpointAddress(java.lang.String address) {
        ShellfireWebServicePort_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (de.shellfire.www.webservice.sf_soap_php.ShellfireWebServicePort.class.isAssignableFrom(serviceEndpointInterface)) {
                de.shellfire.www.webservice.sf_soap_php.ShellfireWebServiceBindingStub _stub = new de.shellfire.www.webservice.sf_soap_php.ShellfireWebServiceBindingStub(new java.net.URL(ShellfireWebServicePort_address), this);
                _stub.setPortName(getShellfireWebServicePortWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("ShellfireWebServicePort".equals(inputPortName)) {
            return getShellfireWebServicePort();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("https://www.shellfire.de/webservice/sf_soap.php", "ShellfireWebServiceService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("https://www.shellfire.de/webservice/sf_soap.php", "ShellfireWebServicePort"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("ShellfireWebServicePort".equals(portName)) {
            setShellfireWebServicePortEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
