/*
 * Copyright (c) 2004 UNINETT FAS
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * $Id$
 */

package no.feide.moria.webservices.v2_3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import no.feide.moria.controller.AuthenticationException;
import no.feide.moria.controller.AuthorizationException;
import no.feide.moria.controller.DirectoryUnavailableException;
import no.feide.moria.controller.InoperableStateException;
import no.feide.moria.controller.MoriaController;
import no.feide.moria.log.MessageLogger;
import no.feide.moria.servlet.RequestUtil;
import no.feide.moria.servlet.soap.AuthenticationFailedException;
import no.feide.moria.servlet.soap.AuthenticationUnavailableException;
import no.feide.moria.servlet.soap.AuthorizationFailedException;
import no.feide.moria.servlet.soap.IllegalInputException;
import no.feide.moria.servlet.soap.InternalException;
import no.feide.moria.servlet.soap.UnknownTicketException;

import org.apache.axis.MessageContext;
import org.apache.axis.session.Session;
import org.apache.axis.transport.http.AxisHttpSession;
import org.apache.log4j.Level;

/**
 * 
 */
public final class AuthenticationImpl
implements Authentication {

    /** Class wide logger. */
    private MessageLogger messageLogger;

    /** Log message for AuthorizationExceptions. */
    private static final String AUTHZ_EX_MESSAGE = "Authorization failed. Throwing RemoteException to service: ";

    /** Log message for AuthenticationExceptions. */
    private static final String AUTHN_EX_MSG = "Authentication failed. Throwing RemoteException to service: ";

    /** Log message for DirectoryUnavailableExceptions. */
    private static final String DIR_UNAV_EX_MSG = "Directory unavailable. Throwing RemoteException to service: ";

    /** Log message for MoriaControllerExceptions. */
    private static final String MORIACTRL_EX_MESSAGE = "Exception from MoriaController. Throwing RemoteException to service: ";

    /** Log message for InoperableStateExceptions. */
    private static final String INOP_STATE_EX_MSG = "Controller in inoperable state. Throwing RemoteException to service: ";

    /** Log message for UnknownTicketExceptions. */
    private static final String UNKNOWN_TICKET_EX_MSG = "Ticket is unknown. Throwing RemoteException to service: ";


    /**
     * Default constructor. Initializes the message logger.
     */
    public AuthenticationImpl() {

        messageLogger = new MessageLogger(AuthenticationImpl.class);
    }


    /**
     * @see no.feide.moria.webservices.v2_1.Authentication#initiateAuthentication(java.lang.String[],
     *      java.lang.String, java.lang.String, boolean)
     */
    public String initiateAuthentication(final String[] attributes, final String returnURLPrefix, final String returnURLPostfix, final boolean forceInteractiveAuthentication)
    throws AuthorizationFailedException, IllegalInputException,
    InternalException {

        /* Axis message context containg request data. */
        MessageContext messageContext = MessageContext.getCurrentContext();
        String servicePrincipal = messageContext.getUsername();

        String urlPrefix = null;
        Session genericSession = messageContext.getSession();

        if (genericSession instanceof AxisHttpSession) {
            AxisHttpSession axisHttpSession = (AxisHttpSession) genericSession;
            Properties properties = (Properties) axisHttpSession.getRep().getServletContext().getAttribute("no.feide.moria.web.config");
            urlPrefix = (properties.getProperty(RequestUtil.PROP_LOGIN_URL_PREFIX) + "?" + properties.getProperty(RequestUtil.PROP_LOGIN_TICKET_PARAM) + "=");
        }

        try {

            return urlPrefix + MoriaController.initiateAuthentication(attributes, returnURLPrefix, returnURLPostfix, forceInteractiveAuthentication, servicePrincipal);

        } catch (AuthorizationException e) {

            // Client service did something it was not authorized to do.
            messageLogger.logWarn(AUTHZ_EX_MESSAGE + servicePrincipal, e);
            throw new AuthorizationFailedException(e.getMessage());

        } catch (no.feide.moria.controller.IllegalInputException e) {

            // Illegal input from client service.
            messageLogger.logWarn(MORIACTRL_EX_MESSAGE + servicePrincipal, e);
            throw new IllegalInputException(e.getMessage());

        } catch (InoperableStateException e) {

            // Moria is in an inoperable state.
            messageLogger.logCritical(INOP_STATE_EX_MSG + servicePrincipal, e);
            throw new InternalException(e.getMessage());

        }
    }


    /**
     * @see no.feide.moria.webservices.v2_1.Authentication#directNonInteractiveAuthentication(java.lang.String[],
     *      java.lang.String, java.lang.String)
     */
    public Attribute[] directNonInteractiveAuthentication(final String[] attributes, final String username, final String password)
    throws AuthorizationFailedException, AuthenticationFailedException,
    AuthenticationUnavailableException, IllegalInputException,
    InternalException {

        /* Axis message context containg request data. */
        MessageContext messageContext = MessageContext.getCurrentContext();
        String servicePrincipal = messageContext.getUsername();

        try {
            Map returnAttributes = MoriaController.directNonInteractiveAuthentication(attributes, username, password, servicePrincipal);
            return mapToAttributeArray(returnAttributes, null);
        } catch (AuthorizationException e) {

            // Client service did something it was not authorized to do.
            messageLogger.logWarn(AUTHZ_EX_MESSAGE + servicePrincipal, e);
            throw new AuthorizationFailedException(e.getMessage());

        } catch (AuthenticationException e) {

            // User failed authentication.
            messageLogger.logWarn(AUTHN_EX_MSG + servicePrincipal, e);
            throw new AuthenticationFailedException(e.getMessage());

        } catch (DirectoryUnavailableException e) {

            // Authentication server was unavailable.
            messageLogger.logWarn(DIR_UNAV_EX_MSG + servicePrincipal, e);
            throw new AuthenticationUnavailableException(e.getMessage());

        } catch (no.feide.moria.controller.IllegalInputException e) {

            // Illegal input from client service.
            messageLogger.logWarn(MORIACTRL_EX_MESSAGE + servicePrincipal, e);
            throw new IllegalInputException(e.getMessage());

        } catch (InoperableStateException e) {

            // Moria is in an inoperable state.
            messageLogger.logCritical(INOP_STATE_EX_MSG + servicePrincipal, e);
            throw new InternalException(e.getMessage());

        }
    }


    /**
     * @see no.feide.moria.webservices.v2_1.Authentication#proxyAuthentication(java.lang.String[],
     *      java.lang.String)
     */
    public Attribute[] proxyAuthentication(final String[] attributes, final String proxyTicket)
    throws AuthorizationFailedException, IllegalInputException,
    InternalException, UnknownTicketException {

        /* Axis message context containg request data. */
        MessageContext messageContext = MessageContext.getCurrentContext();
        String servicePrincipal = messageContext.getUsername();

        try {

            Map returnAttributes = MoriaController.proxyAuthentication(attributes, proxyTicket, servicePrincipal);
            return mapToAttributeArray(returnAttributes, proxyTicket);

        } catch (AuthorizationException e) {

            // Client service did something it was not authorized to do.
            messageLogger.logWarn(AUTHZ_EX_MESSAGE + servicePrincipal, e);
            throw new AuthorizationFailedException(e.getMessage());

        } catch (no.feide.moria.controller.IllegalInputException e) {

            // Illegal input from client service.
            messageLogger.logWarn(MORIACTRL_EX_MESSAGE + servicePrincipal, e);
            throw new IllegalInputException(e.getMessage());

        } catch (InoperableStateException e) {

            // Moria is in an inoperable state.
            messageLogger.logCritical(INOP_STATE_EX_MSG + servicePrincipal, e);
            throw new InternalException(e.getMessage());

        } catch (no.feide.moria.controller.UnknownTicketException e) {

            // An unknown ticket was used by the client service.
            messageLogger.logWarn(UNKNOWN_TICKET_EX_MSG + servicePrincipal, e);
            throw new UnknownTicketException(e.getMessage());

        }
    }


    /**
     * @see no.feide.moria.webservices.v2_1.Authentication#getProxyTicket(java.lang.String,
     *      java.lang.String)
     */
    public String getProxyTicket(final String ticketGrantingTicket, final String proxyServicePrincipal)
    throws AuthorizationFailedException, IllegalInputException,
    InternalException, UnknownTicketException {

        /* Axis message context containg request data. */
        MessageContext messageContext = MessageContext.getCurrentContext();
        String servicePrincipal = messageContext.getUsername();

        try {

            return MoriaController.getProxyTicket(ticketGrantingTicket, proxyServicePrincipal, servicePrincipal);

        } catch (AuthorizationException e) {

            // Client service did something it was not supposed to do.
            messageLogger.logWarn(AUTHZ_EX_MESSAGE + servicePrincipal, e);
            throw new AuthorizationFailedException(e.getMessage());

        } catch (no.feide.moria.controller.IllegalInputException e) {

            // Illegal input from client service.
            messageLogger.logWarn(MORIACTRL_EX_MESSAGE + servicePrincipal, e);
            throw new IllegalInputException(e.getMessage());

        } catch (InoperableStateException e) {

            // Moria is in an inoperable state.
            messageLogger.logCritical(INOP_STATE_EX_MSG + servicePrincipal, e);
            throw new InternalException(e.getMessage());

        } catch (no.feide.moria.controller.UnknownTicketException e) {

            // Client service used an unknown ticket.
            messageLogger.logWarn(UNKNOWN_TICKET_EX_MSG + servicePrincipal, e);
            throw new UnknownTicketException(e.getMessage());

        }
    }


    /**
     * @see no.feide.moria.webservices.v2_1.Authentication#getUserAttributes(java.lang.String)
     */
    public Attribute[] getUserAttributes(final String serviceTicket)
    throws AuthorizationFailedException, IllegalInputException,
    InternalException, UnknownTicketException {

        /* Axis message context containg request data. */
        MessageContext messageContext = MessageContext.getCurrentContext();
        String servicePrincipal = messageContext.getUsername();

        try {
            Map returnAttributes = MoriaController.getUserAttributes(serviceTicket, servicePrincipal);
            
            // Prepare debug output.
            if (messageLogger.isEnabledFor(Level.DEBUG)) {
                String attributeNames = "";
                Set keySet = returnAttributes.keySet();
                if (keySet != null) {
                    Iterator i = keySet.iterator();
                    while (i.hasNext())
                        attributeNames = attributeNames + i.next() + ", ";
                    if (attributeNames.length() > 0)
                        attributeNames = attributeNames.substring(0, attributeNames.length() - 2);
                }
                messageLogger.logDebug("Returned attributes [" + attributeNames + "] to service '" + servicePrincipal + "'", serviceTicket);
            }
            
            return mapToAttributeArray(returnAttributes, serviceTicket);
        } catch (no.feide.moria.controller.IllegalInputException e) {

            // Illegal input used by service.
            messageLogger.logWarn(MORIACTRL_EX_MESSAGE + servicePrincipal, e);
            throw new IllegalInputException(e.getMessage());

        } catch (InoperableStateException e) {

            // Moria is in an inoperable state!
            messageLogger.logCritical(INOP_STATE_EX_MSG + servicePrincipal, e);
            throw new InternalException(e.getMessage());

        } catch (no.feide.moria.controller.UnknownTicketException e) {

            // An unknown ticket was used.
            messageLogger.logWarn(UNKNOWN_TICKET_EX_MSG + servicePrincipal, e);
            throw new UnknownTicketException(e.getMessage());

        } catch (AuthorizationException e) {

            // Service was not authorized for this operation.
            messageLogger.logWarn("Service not allowed for organization. Throwing RemoteException to service: ", e);
            throw new AuthorizationFailedException(e.getMessage());
        }

    }


    /**
     * @see no.feide.moria.webservices.v2_1.Authentication#verifyUserExistence(java.lang.String)
     */
    public boolean verifyUserExistence(final String username)
    throws AuthorizationFailedException, AuthenticationUnavailableException,
    IllegalInputException, InternalException {

        /* Axis message context containg request data. */
        MessageContext messageContext = MessageContext.getCurrentContext();
        String servicePrincipal = messageContext.getUsername();

        try {

            return MoriaController.verifyUserExistence(username, servicePrincipal);

        } catch (AuthorizationException e) {

            // Client service did something it was not supposed to do.
            messageLogger.logWarn(AUTHZ_EX_MESSAGE + servicePrincipal, e);
            throw new AuthorizationFailedException(e.getMessage());

        } catch (DirectoryUnavailableException e) {

            // Authentication server is unavailable.
            messageLogger.logWarn(DIR_UNAV_EX_MSG + servicePrincipal, e);
            throw new AuthenticationUnavailableException(e.getMessage());

        } catch (no.feide.moria.controller.IllegalInputException e) {

            // Illegal input from client service.
            messageLogger.logWarn(MORIACTRL_EX_MESSAGE + servicePrincipal, e);
            throw new IllegalInputException(e.getMessage());

        } catch (InoperableStateException e) {

            // Moria is in an inoperable state.
            messageLogger.logCritical(INOP_STATE_EX_MSG + servicePrincipal, e);
            throw new InternalException(e.getMessage());

        }
    }


    /**
     * Utility method to convert a <code>Map</code> to an array of
     * <code>Attribute</code>s.
     * @param map
     *            The <code>Map</code> to be converted.
     * @param activeTicketId
     *            Optional variable for logging purposes.
     * @return Array of <code>Attribute</code> objects.
     */
    private Attribute[] mapToAttributeArray(final Map map, final String activeTicketId) {

        /* Get iterator for map keys. */
        Iterator iterator = map.keySet().iterator();

        /* List to hold finished Attributes while processing map. */
        List attributeList = new ArrayList();

        /* Iterate over keys in map. */
        while (iterator.hasNext()) {
            Object key = iterator.next();
            Object value = map.get(key);

            Attribute attribute = new Attribute();

            /* Check that key is a String, if not will ignore the whole entry. */
            if (key instanceof String) {
                /* Add the key as name of attribute. */
                attribute.setName((String) key);

                /*
                 * Check type of value. If not String or String[] we don't add
                 * it to the attribute list resulting in the whole entry beeing
                 * ignored.
                 */
                if (value instanceof String) {
                    /* Create one-element String[] of value before setting. */
                    attribute.setValues(new String[] {(String) value});
                    attributeList.add(attribute);
                } else if (value instanceof String[]) {
                    attribute.setValues((String[]) value);
                    attributeList.add(attribute);
                } else if (value != null) {
                    messageLogger.logInfo("Attribute value not String or String[]. Entry not added to Attribute[]. ", activeTicketId);
                }
            } else if (value != null) {
                messageLogger.logInfo("Attribute key not String. Entry not added to Attribute[]", activeTicketId);
            }
        }
        return (Attribute[]) attributeList.toArray(new Attribute[] {});
    }
}
