import no.feide.mellon.Moria;
import no.feide.mellon.MoriaException;
import java.util.HashMap;


/** This class only provides a simple example of the required code for
 * use of FEIDE authentication via Moria. Remember that the properties
 * file must contain legal username password for the Moria service or
 * else the request will be denied (an exception is thrown). */
public class SimpleCreateAuthSession {

    public SimpleCreateAuthSession() throws Exception {
        
        /* Read system properties from file */
        System.getProperties().load(getClass().getResourceAsStream("/mellon.properties"));

        /* Create a new instance of Moria */
        Moria moria = Moria.getInstance();

        /* Fetch redirect URL from Moria. A URL to the service has to
         * be specified in the request. After Moria has performed user
         * authentication, the user will be redirected back to this
         * URL. The URL is constructed from the two last arguments
         * (prefix and pstfix) to requestSession. The sessionID will
         * be concatinated with the prefix and postfix values. */
        String redirectURL = moria.requestSession(new String[] {"eduPersonAffiliation", "eduPersonOrgDN"}, "http://back.to.webservice?moriaID=", "");
        
        System.out.println(redirectURL);

        /* When the user returns, the sessionID must be extracted and
         * sent to Moria. The method moria.getAttributes(moriaID)
         * returns a HashMap with the user data. If this HashMap is
         * not null, the user has been successfully authenticated. If
         * the webservice is allowed to request attributes from Moria,
         * the HashMap will contain the requested attributes. */
    }

    
    static public void main(String[] args) throws Exception {
        new SimpleCreateAuthSession();
    }
}
