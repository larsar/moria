/**
 * Authentication_PortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Nov 16, 2004 (12:19:44 EST) WSDL2Java emitter.
 */

package no.feide.moria.webservices.v2_1;

public interface Authentication_PortType extends java.rmi.Remote {
    public no.feide.moria.webservices.v2_1.Attribute[] getUserAttributes(java.lang.String serviceTicket) throws java.rmi.RemoteException, no.feide.moria.servlet.soap.InternalException, no.feide.moria.servlet.soap.IllegalInputException, no.feide.moria.servlet.soap.UnknownTicketException, no.feide.moria.servlet.soap.AuthorizationFailedException;
    public java.lang.String initiateAuthentication(java.lang.String[] attributes, java.lang.String returnURLPrefix, java.lang.String returnURLPostfix, boolean forceInteractiveAuthentication) throws java.rmi.RemoteException, no.feide.moria.servlet.soap.SOAPException;
    public no.feide.moria.webservices.v2_1.Attribute[] directNonInteractiveAuthentication(java.lang.String[] attributes, java.lang.String username, java.lang.String password) throws java.rmi.RemoteException, no.feide.moria.servlet.soap.SOAPException;
    public no.feide.moria.webservices.v2_1.Attribute[] proxyAuthentication(java.lang.String[] attributes, java.lang.String proxyTicket) throws java.rmi.RemoteException, no.feide.moria.servlet.soap.SOAPException;
    public java.lang.String getProxyTicket(java.lang.String ticketGrantingTicket, java.lang.String proxyServicePrincipal) throws java.rmi.RemoteException, no.feide.moria.servlet.soap.SOAPException;
    public boolean verifyUserExistence(java.lang.String username) throws java.rmi.RemoteException, no.feide.moria.servlet.soap.SOAPException;
}
