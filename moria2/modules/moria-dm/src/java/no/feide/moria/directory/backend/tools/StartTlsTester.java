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
public class StartTlsTester {

    public static void main(String[] args) throws NamingException, IOException {
        
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
        System.out.println("Using truststore "+truststoreFilename);
        final String truststorePassword = args[1];
        final String url = args[2]; 
        System.out.println("Connecting to "+url);
        
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
