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
 */

package no.feide.mellon;

import java.net.URL;
import java.util.HashMap;

import no.feide.moria.webservices.v2_1.Attribute;
import no.feide.moria.webservices.v2_1.AuthenticationSoapBindingStub;

/**
 * Represents the interface to Moria2, hiding the internals of the client-server
 * communication. The class is created to be as compatible with the original
 * Moria1 client API as possible, with one exception: <br>
 * <br>
 * Since the service endpoint address is not hard-coded into the stubs anymore,
 * the system property <code>no.feide.mellon.endpoint</code> must now be set
 * to the service endpoint URL for the proper Moria2 instance used.  
 */
public class Moria {
    
    /**
     * The singleton Moria instance.
     */
    private static Moria me;

    /**
     * Internal representation of the Moria2 service.
     */
    private AuthenticationSoapBindingStub moria2;

    /**
     * The name of the system property giving the Moria2 service endpoint. <br>
     * <br>
     * Current value is <code>"no.feide.mellon.endpoint"</code>.
     */
    public static final String SERVICE_ENDPOINT_PROPERTY = "no.feide.mellon.endpoint";

    /**
     * The name of the system property giving the client username to be used
     * when accessing Moria2. <br>
     * <br>
     * Current value is <code>"no.feide.mellon.serviceUsername"</code>.
     */
    public static final String CLIENT_USERNAME = "no.feide.mellon.serviceUsername";

    /**
     * The name of the system property giving the client password to be used
     * when accessing Moria2. <br>
     * <br>
     * Current value is <code>"no.feide.mellon.servicePassword"</code>.
     */
    public static final String CLIENT_PASSWORD = "no.feide.mellon.servicePassword";


    /**
     * Private constructor.
     * @throws MoriaException
     *             If the system property given by
     *             <code>SERVICE_ENDPOINT_PROPERTY</code> is not set, or
     *             unable to create a connection to Moria2.
     */
    private Moria() throws MoriaException {

        super();

        // Check for configuration.
        final String endpointAddress = System.getProperty(SERVICE_ENDPOINT_PROPERTY);
        if ((endpointAddress == null) || (endpointAddress.length() == 0))
            throw new MoriaException("System property " + SERVICE_ENDPOINT_PROPERTY + " must be set");

        // Create connection to service.
        try {
            moria2 = new AuthenticationSoapBindingStub(new URL(endpointAddress), null);
        } catch (Throwable e) {

            // Simple mapping from Throwable to MoriaException.
            throw new MoriaException(e);

        }

        // Set service credentials.
        moria2.setUsername(System.getProperty(CLIENT_USERNAME));
        moria2.setPassword(System.getProperty(CLIENT_PASSWORD));

    }


    /**
     * Get an instance of Moria.
     * @return An instance of the Moria interface.
     * @throws MoriaException
     *             If the instance couldn't be constructed.
     */
    public static Moria getInstance() throws MoriaException {

        if (me == null)
            me = new Moria();
        return me;

    }


    /**
     * Request an authentication session from Moria2. A remote procedure call is
     * sent to Moria2. If this is successful a URL is returned. The user should
     * be redirected to this URL for authentication. After successful
     * authentication, the user will be redirected back to the service using an
     * URL on the form <code>"prefix+<i>sessionID</i>+postfix"</code>,
     * where <code><i>sessionID</i></code> is the session ID for use with
     * <code>getAttributes</code>.
     * @param attributes
     *            The names of requested attributes, to be returned later
     *            throught <code>getAttributes</code>.
     * @param prefix
     *            The prefix, used to build the redirect URL for use after
     *            successful authentication.
     * @param postfix
     *            The postfix, used to build the redirect URL for use after
     *            successful authentication.
     * @param denySso
     *            If <code>true</code>, force the user through authentication
     *            even if a SSO session is present.
     * @return An URL to which the user should be redirected for authentication.
     * @throws MoriaException
     *             If unable to initiate authentication.
     */
    public String requestSession(String[] attributes, String prefix, String postfix, boolean denySso)
    throws MoriaException {

        try {

            // Perform call, Moria2 style.
            return moria2.initiateAuthentication(attributes, prefix, postfix, denySso);

        } catch (Throwable e) {

            // Simple mapping from Throwable to MoriaException.
            throw new MoriaException(e);

        }

    }


    /**
     * Returns user data from Moria2. A remote procedure call is sent to Moria2,
     * requesting user data. If an empty <code>HashMap</code> is returned, the
     * user has been authenticated but no more information is available to the
     * client service. If the client service has requested (and been authorized
     * to read) user attributes, the <code>HashMap</code> will contain the
     * requested user data.
     * @param id
     *            The session ID returned from Moria2 following a successful
     *            authentication.
     * @return The attributes requested when the session was established, or an
     *         empty set if no attributes were requested.
     * @throws MoriaException
     *             If unable to get the requested attributes.
     */
    public HashMap getAttributes(final String id) throws MoriaException {

        final Attribute[] moria2Attributes;
        try {

            // Get attributes, Moria2 style.
            moria2Attributes = moria2.getUserAttributes(id);

        } catch (Throwable e) {

            // Simple mapping from Throwable to MoriaException.
            throw new MoriaException(e);

        }

        // Convert attributes to Moria1 style.
        HashMap moria1Attributes = new HashMap(moria2Attributes.length);
        for (int i = 0; i < moria2Attributes.length; i++)
            moria1Attributes.put(moria2Attributes[i].getName(), moria2Attributes[i].getValues());
        return new HashMap(moria1Attributes);

    }


    /**
     * Returns the service endpoint address of the Moria2 instance used.
     * @return Moria2's endpoint address, as given by the system property
     *         <code>SERVICE_ENDPOINT_PROPERTY</code>.
     */
    public String getServiceAddress() {

        return new String(System.getProperty(SERVICE_ENDPOINT_PROPERTY));

    }

}
