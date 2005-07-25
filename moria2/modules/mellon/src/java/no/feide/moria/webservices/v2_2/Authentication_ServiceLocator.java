/**
 * Authentication_ServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Nov 16, 2004 (12:19:44 EST) WSDL2Java emitter.
 */

package no.feide.moria.webservices.v2_2;

public class Authentication_ServiceLocator extends org.apache.axis.client.Service implements no.feide.moria.webservices.v2_2.Authentication_Service {

/**
 * Test!
 */

    public Authentication_ServiceLocator() {
    }


    public Authentication_ServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    // Use to get a proxy class for Authentication
    private java.lang.String Authentication_address = "http://localhost:8080/moria/v2_2/Authentication";

    public java.lang.String getAuthenticationAddress() {
        return Authentication_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String AuthenticationWSDDServiceName = "Authentication";

    public java.lang.String getAuthenticationWSDDServiceName() {
        return AuthenticationWSDDServiceName;
    }

    public void setAuthenticationWSDDServiceName(java.lang.String name) {
        AuthenticationWSDDServiceName = name;
    }

    public no.feide.moria.webservices.v2_2.Authentication_PortType getAuthentication() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(Authentication_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getAuthentication(endpoint);
    }

    public no.feide.moria.webservices.v2_2.Authentication_PortType getAuthentication(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            no.feide.moria.webservices.v2_2.AuthenticationSoapBindingStub _stub = new no.feide.moria.webservices.v2_2.AuthenticationSoapBindingStub(portAddress, this);
            _stub.setPortName(getAuthenticationWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setAuthenticationEndpointAddress(java.lang.String address) {
        Authentication_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (no.feide.moria.webservices.v2_2.Authentication_PortType.class.isAssignableFrom(serviceEndpointInterface)) {
                no.feide.moria.webservices.v2_2.AuthenticationSoapBindingStub _stub = new no.feide.moria.webservices.v2_2.AuthenticationSoapBindingStub(new java.net.URL(Authentication_address), this);
                _stub.setPortName(getAuthenticationWSDDServiceName());
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
        if ("Authentication".equals(inputPortName)) {
            return getAuthentication();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://v2_2.webservices.moria.feide.no", "Authentication");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://v2_2.webservices.moria.feide.no", "Authentication"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        if ("Authentication".equals(portName)) {
            setAuthenticationEndpointAddress(address);
        }
        else { // Unknown Port Name
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
