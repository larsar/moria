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

import java.io.Serializable;

/**
 * This class is used for holding the state through an authentication. From
 * initiation by the service through to the final retrival of user data.
 * 
 * @author Bjørn Ola Smievoll &lt;b.o@smievoll.no&gt;
 * @version $Revision$
 */
public final class MoriaAuthnAttempt implements Serializable {

    /** The inital attributes requested by the initiating service */
    private final String[] requestedAttributes;

    /** The initial part of the url the user is to be redirected to */
    private final String returnURLPrefix;

    /** The final part of the url the user is to be redirected to */
    private final String returnURLPostfix;

    /** Wheter or not SSO is to be used when user arrives at login servlet */
    private final boolean forceInterativeAuthentication;

    /** Transient attributes returned from a directory that is not to be cached */
    private String[] transientAttributes;

    /**
     * Construct an instance. Usually based on data given in an initial request
     * by a remote service.
     * 
     * @param requestedAttributes
     *            the attributes the remote service requires
     * @param returnURLPrefix
     *            the initial part of the url the user is to be redirected to
     * @param returnURLPostfix
     *            the final part of the url the user is to be redirected to.
     *            May be null
     * @param forceInteractiveAuthentication
     *            wheter or not SSO is to be used
     */
    public MoriaAuthnAttempt(final String[] requestedAttributes, final String returnURLPrefix, final String returnURLPostfix,
            final boolean forceInteractiveAuthentication) {
        this.requestedAttributes = requestedAttributes;
        this.returnURLPrefix = returnURLPrefix;
        this.returnURLPostfix = returnURLPostfix;
        this.forceInterativeAuthentication = forceInteractiveAuthentication;
    }

    /**
     * Get the string array containing the requested attributes.
     * 
     * @return the attributes requested by the invoking service
     */
    public String[] getRequestedAttributes() {
        return (String[]) requestedAttributes.clone();
    }

    /**
     * Get the string array containing the transient attributes.
     * 
     * @return the short-lived user attributes
     */
    public String[] getTransientAttributes() {
        return (String[]) transientAttributes.clone();
    }

    /**
     * Set the user data that have been retrived from a directory for this
     * authentication attempt.
     * 
     * @param transientAttributes
     *            the short-lived user attributes
     */
    void setTransientAttributes(String[] transientAttributes) {
        this.transientAttributes = transientAttributes;
    }

    /**
     * Get the initial part of the return url.
     * 
     * @return the return url prefix
     */
    public String getReturnURLPrefix() {
        return returnURLPrefix;
    }

    /**
     * Get the end part of the return url.
     * 
     * @return the return url postfix
     */
    public String getReturnURLPostfix() {
        return returnURLPostfix;
    }

    /* TODO: Check spelling */

    /**
     * Check wheter or not SSO should be refused even if possible.
     * 
     * @return true for forced authentication
     */
    public boolean isForceInterativeAuthentication() {
        return forceInterativeAuthentication;
    }
}
