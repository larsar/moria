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

package no.feide.moria.store;

import java.util.HashMap;

/**
 * This class is used for holding state through an authentication. From
 * initialization by the service through to final retrieval of user data.
 *
 * @author Bj&oslash;rn Ola Smievoll &lt;b.o@smievoll.no&gt;
 * @version $Revision$
 */
public final class MoriaAuthnAttempt implements MoriaStoreData {

    /**
     * The inital attributes requested by the initiating service.
     */
    private final String[] requestedAttributes;

    /**
     * The initial part of the url the user is to be redirected to.
     */
    private final String returnURLPrefix;

    /**
     * The final part of the url the user is to be redirected to.
     */
    private final String returnURLPostfix;

    /**
     * Whether or not single sign-on (SSO) is to be used when user arrives 
     * at login servlet.
     */
    private final boolean forceInterativeAuthentication;

    /**
     * Transient attributes returned from a directory that are not to be cached.
     */
    private HashMap transientAttributes;

    /**
     * Principal for the client that requests the authentication attempt.
     */
    private final String servicePrincipal;

    /**
     * Constructs an instance. Usually based on data given in an initial request
     * by a remote service.
     *
     * @param requestedAttributes
     *          the attributes the remote service requires
     * @param returnURLPrefix
     *          the initial part of the url the user is to be redirected to
     * @param returnURLPostfix
     *          the final part of the url the user is to be redirected to. May be null
     * @param forceInteractiveAuthentication
     *          whether or not SSO is to be used
     * @param servicePrincipal
     *          the name of the service initiating this authentication attempt.
     */
    public MoriaAuthnAttempt(final String[] requestedAttributes, final String returnURLPrefix, final String returnURLPostfix,
            final boolean forceInteractiveAuthentication, final String servicePrincipal) {
        this.requestedAttributes = requestedAttributes;
        this.returnURLPrefix = returnURLPrefix;
        this.returnURLPostfix = returnURLPostfix;
        this.forceInterativeAuthentication = forceInteractiveAuthentication;
        this.servicePrincipal = servicePrincipal;
    }

    /**
     * Gets the string array containing the requested attributes.
     *
     * @return The attributes requested by the invoking service.
     */
    public String[] getRequestedAttributes() {
        return (String[]) requestedAttributes.clone();
    }

    /**
     * Gets the transient attributes.
     *
     * @return The short-lived user attributes.
     */
    public HashMap getTransientAttributes() {
        return (HashMap) transientAttributes.clone();
    }

    /**
     * Sets the user data that have been retrieved from a directory for this
     * authentication attempt.
     *
     * @param transientAttributes The short-lived user attributes.
     */
    void setTransientAttributes(final HashMap transientAttributes) {
        this.transientAttributes = transientAttributes;
    }

    /**
     * Gets the initial part of the return url.
     *
     * @return The return url prefix.
     */
    public String getReturnURLPrefix() {
        return returnURLPrefix;
    }

    /**
     * Gets the end part of the return url.
     *
     * @return The return url postfix.
     */
    public String getReturnURLPostfix() {
        return returnURLPostfix;
    }

    /**
     * Gets the servicePrincipal.
     *
     * @return The service principal name.
     */
    public String getServicePrincipal() {
        return servicePrincipal;
    }

    /**
     * Checks whether or not single sign-on (SSO) should be refused even if 
     * possible.
     *
     * @return True for forced authentication.
     */
    public boolean isForceInterativeAuthentication() {
        return forceInterativeAuthentication;
    }
}
