import no.feide.mellon.Moria;
import no.feide.mellon.MoriaException;
import java.util.HashMap;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.Iterator;


/** This class only provides a simple example of the required code for
 * use of FEIDE authentication via Moria. Remember that the properties
 * file must contain legal username password for the Moria service or
 * else the request will be denied (an exception is thrown). */
public class SimpleCreateAuthSession {

    public SimpleCreateAuthSession() throws Exception {
        
        /* Set username and password to use Moria */
        System.setProperty("no.feide.mellon.serviceUsername", "demo");
        System.setProperty("no.feide.mellon.servicePassword", "demo");

        /* Create a new instance of Moria */
        Moria moria = Moria.getInstance();

        /* Fetch redirect URL from Moria. A URL to the service has to
         * be specified in the request. After Moria has performed user
         * authentication, the user will be redirected back to this
         * URL. The URL is constructed from the two last arguments
         * (prefix and pstfix) to requestSession. The sessionID will
         * be concatinated with the prefix and postfix values. */
        String redirectURL = moria.requestSession(new String[] {"eduPersonAffiliation", "eduPersonOrgDN"}, "http://www.feide.no/?moriaID=", "", false);
        
        
        System.out.println("\nOpen a web browser and direct it to the following URL: \n"+redirectURL);

        
        /* Read moriaID from prompt */
        System.out.println("\nAfter FEIDE authentication, cut the moriaID from the URL and paste it here: ");
        String moriaID = new BufferedReader(new InputStreamReader(System.in)).readLine();

        /* Get user data */
        HashMap userData = moria.getAttributes(moriaID);

        /* Print all user data */
        System.out.println("\nThe following data was delivered from Moria:");
        for (Iterator iterator = userData.keySet().iterator(); iterator.hasNext();) {
            String key = (String) iterator.next();
            System.out.println(key+": "+userData.get(key));
        }

    }

    
    static public void main(String[] args) throws Exception {
        new SimpleCreateAuthSession();
    }
}
