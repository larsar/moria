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
 */
package no.feide.moria.directory.backend.tools;

import java.io.IOException;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.StartTlsRequest;
import javax.naming.ldap.StartTlsResponse;
import javax.net.ssl.SSLSession;


/**
 * A simple tool to test TLS (StartTLS) behaviour against LDAP servers.
 * @author Cato Olsen
 */
public final class StartTlsTester {

    /** Default private constructor.  */
    private StartTlsTester() { };

    /**
     * Main method. Reads the index file, writes the index object, and
     * finally verifies that the generated and written contents match.
     * @param args
     *            <ol><li>truststore filename</li>
     *            <li>truststore password</li>
     *            <li>LDAP URL</li></ol>
     * @throws NamingException
     *             If there is an LDAP problem.
     * @throws IOException
     *             If unable to read from or write to truststore file.
     */
    public static void main(final String[] args) throws NamingException, IOException {

        // Show usage?
        if (args.length < 3) {
            System.out.println("Usage:");
            System.out.println("Parameter 1 - truststore filename");
            System.out.println("Parameter 2 - truststore password");
            System.out.println("Parameter 3 - LDAP URL");
            System.exit(0);
        }

        // Uncomment (or run with -D) to enable SSL debugging.
        //System.setProperty("javax.net.debug", "ssl");

        // Status.
        final String truststoreFilename = args[0];
        System.out.println("Using truststore " + truststoreFilename);
        final String truststorePassword = args[1];
        final String url = args[2];
        System.out.println("Connecting to " + url);

        // Setting global truststore properties.
        System.setProperty("javax.net.ssl.trustStore", truststoreFilename);
        System.setProperty("javax.net.ssl.trustStorePassword", truststorePassword);

        // Prepare environment.
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put("java.naming.ldap.derefAliases", "never");  // Due to OpenSSL problems.
        env.put(Context.PROVIDER_URL, url);

        // Opening.
        InitialLdapContext ldap = new InitialLdapContext(env, null);

        // Doing StartTLS.
        System.out.println("Doing StartTLS");
        StartTlsResponse tls = (StartTlsResponse) ldap.extendedOperation(new StartTlsRequest());

        // Opening TLS connection.
        System.out.println("Opening SSL connection");
        SSLSession ssl = tls.negotiate();

        // Closing.
        System.out.println("Closing");
        tls.close();
        ldap.close();

        // All done.
        System.out.println("Done");

    }
}
