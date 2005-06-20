/**
 * Authentication_Service.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Nov 16, 2004 (12:19:44 EST) WSDL2Java emitter.
 */

package no.feide.moria.webservices.v2_2;

public interface Authentication_Service extends javax.xml.rpc.Service {
    public java.lang.String getAuthenticationAddress();

    public no.feide.moria.webservices.v2_2.Authentication_PortType getAuthentication() throws javax.xml.rpc.ServiceException;

    public no.feide.moria.webservices.v2_2.Authentication_PortType getAuthentication(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
