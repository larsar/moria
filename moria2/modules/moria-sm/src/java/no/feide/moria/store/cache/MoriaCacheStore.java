/*
 * $Id$
 */
package no.feide.moria.store.cache;

import java.util.HashMap;

import no.feide.moria.store.CachedUserData;
import no.feide.moria.store.InvalidTicketException;
import no.feide.moria.store.MoriaAuthnAttempt;
import no.feide.moria.store.MoriaStore;
import no.feide.moria.store.MoriaTicket;

import org.jboss.cache.PropertyConfigurator;
import org.jboss.cache.TreeCache;
import org.jboss.cache.lock.LockingException;
import org.jboss.cache.lock.TimeoutException;

/**
 * @author Bjørn Ola Smievoll &lt;b.o@smievoll.no&gt;
 * @version $Revision$
 *  
 */
public class MoriaCacheStore implements MoriaStore {

    private TreeCache tree;

    /* The time to live for different tickets */
    private final long loginTicketTTL, serviceTicketTTL, ssoTicketTTL, tgTicketTTL, proxyTicketTTL;

    /**
     * Constructs a new instance
     *  
     */
    public MoriaCacheStore() {
        try {
            tree = new TreeCache();
            PropertyConfigurator config = new PropertyConfigurator();
            config.configure(tree, "jboss-cache.xml");
            tree.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
            // TODO: Handle exception properly
        }

        // TODO: Read values for ticket TTL from property file
        loginTicketTTL = 300; /* 5 min */
        serviceTicketTTL = 300; /* 5 min */
        ssoTicketTTL = 28800; /* 8 hours */
        tgTicketTTL = 7200; /* 2 hours */
        proxyTicketTTL = 300; /* 5 min */
    }

    /**
     * @see no.feide.moria.store.MoriaStore#createAuthnAttempt(java.lang.String[],
     *      java.lang.String, java.lang.String, java.lang.String, boolean)
     */
    public MoriaTicket createAuthnAttempt(
        String[] requestedAttributes,
        String responseURLPrefix,
        String responseURLPostfix,
        String servicePrincipal,
        boolean forceInteractiveAuthentication)
        throws IllegalArgumentException {

        MoriaTicket ticket = null;
        MoriaAuthnAttempt authnAttempt;

        if (requestedAttributes == null)
            throw new IllegalArgumentException("requestedAttributes cannot be null");

        if (responseURLPrefix == null)
            throw new IllegalArgumentException("responseURLPrefix cannot be null");

        if (responseURLPostfix == null)
            throw new IllegalArgumentException("responseURLPostfix cannot be null");

        if (servicePrincipal == null || servicePrincipal.equals(""))
            throw new IllegalArgumentException("servicePrincipal cannot be null or empty string");

        try {
            ticket = new MoriaTicket(MoriaTicket.LOGIN_TICKET, servicePrincipal, loginTicketTTL);
        } catch (InvalidTicketException e) {
            // TODO: Handle this exception properly
            throw new RuntimeException(e);
        }

        authnAttempt =
            new MoriaAuthnAttempt(
                requestedAttributes,
                responseURLPrefix,
                responseURLPostfix,
                forceInteractiveAuthentication);

        try {
            tree.put("moria", ticket, authnAttempt);
        } catch (Exception e) {
            throw new RuntimeException(e);
            // TODO: Handle this exception properly
        }
        return ticket;
    }

    /**
     * @see no.feide.moria.store.MoriaStore#getAuthnAttempt(no.feide.moria.store.MoriaTicket)
     */
    public MoriaAuthnAttempt getAuthnAttempt(MoriaTicket loginTicket, boolean keep)
        throws InvalidTicketException {

        validateTicket(loginTicket, MoriaTicket.LOGIN_TICKET);

        MoriaAuthnAttempt authnAttempt = null;

        /* If the authn attempt is not to be kept, call remove on the store */
        if (!keep) {
            try {
                authnAttempt = (MoriaAuthnAttempt) tree.remove("moria", loginTicket);
            } catch (Exception e) {
                throw new RuntimeException(e);
                // TODO: Handle this exception properly
            }
        } else {
            try {
                authnAttempt = (MoriaAuthnAttempt) tree.get("moria", loginTicket);
            } catch (LockingException e) {
                throw new RuntimeException(e);
                // TODO: Handle this exception properly
            } catch (TimeoutException e) {
                throw new RuntimeException(e);
                // TODO: Handle this exception properly
            }
        }

        return authnAttempt;
    }

    /**
     * @see no.feide.moria.store.MoriaStore#cacheUserData(java.util.HashMap)
     */
    public MoriaTicket cacheUserData(HashMap attributes) {

        if (attributes == null)
            throw new IllegalArgumentException("attributes cannot be null");

        CachedUserData userData = new CachedUserData(attributes);

        /* Try / catch block for ticket creation */
        try {
            MoriaTicket ssoTicket = new MoriaTicket(MoriaTicket.SSO_TICKET, null, ssoTicketTTL);

            /* Try / catch block for tree cache data addition */
            try {
                tree.put("moria", ssoTicket, userData);
            } catch (Exception e) {
                throw new RuntimeException(e);
                // TODO: Handle this exception properly
            }

            return ssoTicket;

        } catch (InvalidTicketException e) {
            throw new RuntimeException(e);
            // TODO: Handle this exception properly
        }
    }

    /**
     * @see no.feide.moria.store.MoriaStore#getUserData(no.feide.moria.store.MoriaTicket)
     */
    public CachedUserData getUserData(MoriaTicket ticket) throws InvalidTicketException {

        validateTicket(
            ticket,
            new int[] {
                MoriaTicket.SSO_TICKET,
                MoriaTicket.TICKET_GRANTING_TICKET,
                MoriaTicket.PROXY_TICKET });

        /*
         * If the returned value isn't a CachedUserData object, we can't do
         * much else than throw an exception, so we just go for the default
         * ClassCastException
         *  
         */
        try {
            return (CachedUserData) tree.get("moria", ticket);
        } catch (LockingException e) {
            throw new RuntimeException(e);
            // TODO: Handle this exception properly
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
            // TODO: Handle this exception properly
        }
    }

    /**
     * @see no.feide.moria.store.MoriaStore#createServiceTicket(no.feide.moria.store.MoriaTicket)
     */
    public MoriaTicket createServiceTicket(MoriaTicket loginTicket) throws InvalidTicketException {

        validateTicket(loginTicket, MoriaTicket.LOGIN_TICKET);

        /*
         * Create new service ticket and assosiate it with the same
         * authentication attempt as the login ticket
         */
        try {
            MoriaTicket serviceTicket =
                new MoriaTicket(
                    MoriaTicket.SERVICE_TICKET,
                    loginTicket.getServicePrincipal(),
                    serviceTicketTTL);
            tree.put("moria", serviceTicket, tree.get("moria", loginTicket));
            return serviceTicket;
        } catch (InvalidTicketException e) {
            throw new RuntimeException(e);
            // TODO: Handle this exception properly
        } catch (LockingException e) {
            throw new RuntimeException(e);
            // TODO: Handle this exception properly
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
            // TODO: Handle this exception properly
        } catch (Exception e) {
            throw new RuntimeException(e);
            // TODO: Handle this exception properly
        }
    }

    /**
     * @see no.feide.moria.store.MoriaStore#createTicketGrantingTicket(no.feide.moria.store.MoriaTicket,
     *      java.lang.String)
     */
    public MoriaTicket createTicketGrantingTicket(MoriaTicket ssoTicket, String servicePrincipal)
        throws InvalidTicketException {

        validateTicket(ssoTicket, MoriaTicket.SSO_TICKET);

        /*
         * Create new ticket granting ticket and assosiate it with the same
         * user data as the sso ticket
         */

        MoriaTicket tgTicket;
        Object userData;

        /* Create ticket */
        try {
            tgTicket = new MoriaTicket(MoriaTicket.TICKET_GRANTING_TICKET, servicePrincipal, tgTicketTTL);
        } catch (InvalidTicketException e) {
            throw new RuntimeException(e);
            // TODO: Handle this exception properly
        }

        /* Get userdata form treecache */
        try {
            userData = tree.get("moria", ssoTicket);
            if (userData == null)
                throw new InvalidTicketException("SSO ticket has no data assosiated with it");
        } catch (LockingException e) {
            throw new RuntimeException(e);
            // TODO: Handle this exception properly
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
            // TODO: Handle this exception properly
        }

        /* Add new ticket and userdata to store */
        try {
            tree.put("moria", tgTicket, userData);
        } catch (Exception e) {
            throw new RuntimeException(e);
            // TODO: Handle this exception properly
        }

        return tgTicket;
    }

    /**
     * @see no.feide.moria.store.MoriaStore#createProxyTicket(no.feide.moria.store.MoriaTicket)
     */
    public MoriaTicket createProxyTicket(MoriaTicket tgTicket, String servicePrincipal)
        throws InvalidTicketException {

        validateTicket(tgTicket, MoriaTicket.TICKET_GRANTING_TICKET);

        /*
         * Create new proxy ticket and assosiate it with the same cached user
         * data as the TGT
         */
        try {
            MoriaTicket proxyTicket =
                new MoriaTicket(MoriaTicket.PROXY_TICKET, servicePrincipal, proxyTicketTTL);
            tree.put("moria", proxyTicket, tree.get("moria", tgTicket));
            return proxyTicket;
        } catch (InvalidTicketException e) {
            throw new RuntimeException(e);
            // TODO: Handle this exception properly
        } catch (LockingException e) {
            throw new RuntimeException(e);
            // TODO: Handle this exception properly
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
            // TODO: Handle this exception properly
        } catch (Exception e) {
            throw new RuntimeException(e);
            // TODO: Handle this exception properly
        }
    }

    /**
     * Check validity of ticket against type and expiry time
     * 
     * @param ticket ticket to be checked
     * @param type the expected type of the ticket
     * @throws InvalidTicketException thrown if ticket is found invalid
     */
    private void validateTicket(MoriaTicket ticket, int type) throws InvalidTicketException {

        /* First check that the ticket has the correct type */
        if (ticket.getTicketType() != type)
            throw new InvalidTicketException("Ticket has wrong type: " + ticket.getTicketType());

        /*
         * Then check if it still is valid. We let the dedicated
         * vacuuming-service take care of removing it at later time, so we just
         * throw an exception
         */
        if (ticket.hasExpired())
            throw new InvalidTicketException("Ticket has expired");
    }

    /**
     * Check validity of ticket against a set of types and expiry time
     * 
     * @param ticket ticket to be checked
     * @param types array of valid types for the ticket
     * @throws InvalidTicketException thrown if the ticket is found invalid
     */
    private void validateTicket(MoriaTicket ticket, int[] types) throws InvalidTicketException {
        boolean valid = false;

        /* Loop through ticket types and count invalid ones */
        for (int i = 0; i < types.length; i++) {
            if (ticket.getTicketType() == types[i]) {
                valid = true;
                break;
            }
        }

        /* Throw exception if all types were invalid */
        if (!valid)
            throw new InvalidTicketException("Ticket has wrong type: " + ticket.getTicketType());

        /*
         * Then check if it still is valid. We let the dedicated
         * vacuuming-service take care of removing it at later time, so we just
         * throw an exception
         */
        if (ticket.hasExpired())
            throw new InvalidTicketException("Ticket has expired");

    }
}
