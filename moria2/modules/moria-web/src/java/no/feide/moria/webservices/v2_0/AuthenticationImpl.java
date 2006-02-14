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

package no.feide.moria.webservices.v2_0;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import no.feide.moria.controller.AuthenticationException;
import no.feide.moria.controller.AuthorizationException;
import no.feide.moria.controller.DirectoryUnavailableException;
import no.feide.moria.controller.IllegalInputException;
import no.feide.moria.controller.InoperableStateException;
import no.feide.moria.controller.MoriaController;
import no.feide.moria.controller.UnknownTicketException;
import no.feide.moria.log.MessageLogger;
import no.feide.moria.servlet.RequestUtil;

import org.apache.axis.MessageContext;
import org.apache.axis.session.Session;
import org.apache.axis.transport.http.AxisHttpSession;
import org.apache.log4j.Level;

/**
 * @author Bj&oslash;rn Ola Smievoll &lt;b.o@smievoll.no&gt;
 * @version $Revision$
 */
public final class AuthenticationImpl implements Authentication {

    /** Class wide logger. */
    private MessageLogger messageLogger;

    /** Log message for AuthorizationExceptions. */
    private static final String AUTHZ_EX_MSG = "Authorization failed. Throwing RemoteException to service: ";

    /** Log message for AuthenticationExceptions. */
    private static final String AUTHN_EX_MSG = "AuthenticationImpl failed. Throwing RemoteException to service: ";

    /** Log message for DirectoryUnavailableExceptions. */
    private static final String DIR_UNAV_EX_MSG = "Directory unavailable. Throwing RemoteException to service: ";

    /** Log message for IllegalInputExceptions. */
    private static final String ILLEGAL_INPUT_EX_MSG = "Illegal input. Throwing RemoteException to service: ";

    /** Log message for InoperableStateExceptions. */
    private static final String INOP_STATE_EX_MSG = "Controller in inoperable state. Throwing RemoteException to service: ";

    /** Log message for UnknownTicketExceptions. */
    private static final String UNKNOWN_TICKET_EX_MSG = "Ticket is unknown. Throwing RemoteException to service: ";

    /**
     * Default constructor.
     * Initializes the logger.
     */
    public AuthenticationImpl() {
        messageLogger = new MessageLogger(AuthenticationImpl.class);
    }

    /**
     * Initiates authentication.
     *
     * The initial call done by a service to start a login attempt.
     *
     * @param attributes
     *          The attributes the service wants returned on login
     * @param returnURLPrefix
     *          The prefix of the url the user is to be returned to
     * @param returnURLPostfix
     *          The optional postfix of the return url
     * @param forceInteractiveAuthentication
     *          Whether or not cookie based authentication (SSO Light)
     *          should be allowed.
     * @return The Moria url the client is to be redirected to.
     * @throws RemoteException
     *          If anything fails during the call.
     * @see no.feide.moria.webservices.v2_0.Authentication#initiateAuthentication(java.lang.String[],
     *      java.lang.String, java.lang.String, boolean)
     */
    public String initiateAuthentication(final String[] attributes, final String returnURLPrefix, final String returnURLPostfix,
            final boolean forceInteractiveAuthentication) throws RemoteException {

        /* Axis message context containg request data. */
        MessageContext messageContext = MessageContext.getCurrentContext();
        String servicePrincipal = messageContext.getUsername();

        String urlPrefix = null;
        Session genericSession = messageContext.getSession();

        if (genericSession instanceof AxisHttpSession) {
            AxisHttpSession axisHttpSession = (AxisHttpSession) genericSession;
            Properties properties = (Properties) axisHttpSession.getRep().getServletContext().getAttribute(
                    "no.feide.moria.web.config");
            urlPrefix = (properties.getProperty(RequestUtil.PROP_LOGIN_URL_PREFIX) + "?"
                    + properties.getProperty(RequestUtil.PROP_LOGIN_TICKET_PARAM) + "=");
        }

        try {
            return urlPrefix
                    + MoriaController.initiateAuthentication(attributes, returnURLPrefix, returnURLPostfix,
                            forceInteractiveAuthentication, servicePrincipal);
        } catch (AuthorizationException ae) {
            messageLogger.logWarn(AUTHZ_EX_MSG + servicePrincipal, ae);
            throw new RemoteException(ae.getMessage());
        } catch (IllegalInputException iie) {
            messageLogger.logWarn(ILLEGAL_INPUT_EX_MSG + servicePrincipal, iie);
            throw new RemoteException(iie.getMessage());
        } catch (InoperableStateException ise) {
            messageLogger.logCritical(INOP_STATE_EX_MSG + servicePrincipal, ise);
            throw new RemoteException(ise.getMessage());
        }
    }

    /**
     * Performs direct non-interactive authentication.
     *
     * A redirect- and html-less login method.  Only to be used in
     * special cases where the client for some reason does not
     * support the standard login procedure.  Inherently insecure as
     * the service will have knowledge of the plaintext password.
     *
     * @param attributes
     *          The attributes the service wants returned on login.
     * @param username
     *          The user name of the user to be authenticated.
     * @param password
     *          The password of the user to be authenticated.
     * @return Array of attributes as requested.
     * @throws RemoteException
     *          If anything fails during the call.
     * @see no.feide.moria.webservices.v2_0.Authentication#directNonInteractiveAuthentication(java.lang.String[],
     *      java.lang.String, java.lang.String)
     */
    public Attribute[] directNonInteractiveAuthentication(final String[] attributes, final String username, final String password)
            throws RemoteException {

        /* Axis message context containg request data. */
        MessageContext messageContext = MessageContext.getCurrentContext();
        String servicePrincipal = messageContext.getUsername();

        try {
            Map returnAttributes = MoriaController.directNonInteractiveAuthentication(attributes, username, password,
                    servicePrincipal);
            return mapToAttributeArray(returnAttributes, null);
        } catch (AuthenticationException ae) {
            messageLogger.logWarn(AUTHN_EX_MSG + servicePrincipal, ae);
            throw new RemoteException(ae.getMessage());
        } catch (AuthorizationException ae) {
            messageLogger.logWarn(AUTHZ_EX_MSG + servicePrincipal, ae);
            throw new RemoteException(ae.getMessage());
        } catch (DirectoryUnavailableException due) {
            messageLogger.logWarn(DIR_UNAV_EX_MSG + servicePrincipal, due);
            throw new RemoteException(due.getMessage());
        } catch (IllegalInputException iie) {
            messageLogger.logWarn(ILLEGAL_INPUT_EX_MSG + servicePrincipal, iie);
            throw new RemoteException(iie.getMessage());
        } catch (InoperableStateException ise) {
            messageLogger.logCritical(INOP_STATE_EX_MSG + servicePrincipal, ise);
            throw new RemoteException(ise.getMessage());
        }
    }

    /**
     * Gets user attributes.
     *
     * Called by the service when the user returns after a successful
     * login.
     *
     * @param serviceTicket
     *          The ticket included in the return request issued by the client.
     * @return Array of attributes as requested in initiateAuthentication.
     * @throws RemoteException
     *          If anything fails during the call.
     * @see no.feide.moria.webservices.v2_0.Authentication#getUserAttributes(java.lang.String)
     */
    public Attribute[] getUserAttributes(final String serviceTicket) throws RemoteException {

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
        } catch (IllegalInputException iie) {
            messageLogger.logWarn(ILLEGAL_INPUT_EX_MSG + servicePrincipal, iie);
            throw new RemoteException(iie.getMessage());
        } catch (InoperableStateException ise) {
            messageLogger.logCritical(INOP_STATE_EX_MSG + servicePrincipal, ise);
            throw new RemoteException(ise.getMessage());
        } catch (UnknownTicketException ute) {
            messageLogger.logWarn(UNKNOWN_TICKET_EX_MSG + servicePrincipal, ute);
            throw new RemoteException(ute.getMessage());
        } catch (AuthorizationException e) {
            messageLogger.logWarn("Service not allowed for organization. Throwing RemoteException to service: ", e);
            throw new RemoteException(e.getMessage());
        }
    }

    /**
     * Verifies the existence of a given user in the underlying directories.
     *
     * @param username
     *          The username to be validated.
     * @return true if the user is found.
     * @throws RemoteException
     *          If anything fails during the call.
     * @see no.feide.moria.webservices.v2_0.Authentication#verifyUserExistence(java.lang.String)
     */
    public boolean verifyUserExistence(final String username) throws RemoteException {

        /* Axis message context containg request data. */
        MessageContext messageContext = MessageContext.getCurrentContext();
        String servicePrincipal = messageContext.getUsername();

        try {
            return MoriaController.verifyUserExistence(username, servicePrincipal);
        } catch (AuthorizationException ae) {
            messageLogger.logWarn(AUTHZ_EX_MSG + servicePrincipal, ae);
            throw new RemoteException(ae.getMessage());
        } catch (DirectoryUnavailableException due) {
            messageLogger.logWarn(DIR_UNAV_EX_MSG + servicePrincipal, due);
            throw new RemoteException(due.getMessage());
        } catch (IllegalInputException iie) {
            messageLogger.logWarn(ILLEGAL_INPUT_EX_MSG + servicePrincipal, iie);
            throw new RemoteException(iie.getMessage());
        } catch (InoperableStateException ise) {
            messageLogger.logCritical(INOP_STATE_EX_MSG + servicePrincipal, ise);
            throw new RemoteException(ise.getMessage());
        }
    }

    /**
     * Converts a Map to an array of Attributes.
     * Utility method.
     *
     * @param map
     *          The Map to be converted.
     * @param activeTicketId
     *          Optional variable for logging purposes.
     * @return Array of attribute objects.
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
                 * Check type of value. If not String or String[] we don't add it to the attribute
                 * list resulting in the whole entry beeing ignored.
                 */
                if (value instanceof String) {
                    /* Create one-element String[] of value before setting. */
                    attribute.setValues(new String[] {(String) value});
                    attributeList.add(attribute);
                } else if (value instanceof String[]) {
                    attribute.setValues((String[]) value);
                    attributeList.add(attribute);
                } else if (value != null) {
                    messageLogger.logInfo("Attribute value not String or String[]. Entry not added to Attribute[]. ",
                            activeTicketId);
                }
            } else if (value != null) {
                messageLogger.logInfo("Attribute key not String. Entry not added to Attribute[]", activeTicketId);
            }
        }
        return (Attribute[]) attributeList.toArray(new Attribute[] {});
    }
}
