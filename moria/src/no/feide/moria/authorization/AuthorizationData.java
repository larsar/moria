package no.feide.moria.authorization;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import java.io.IOException;

import java.util.HashMap;
import java.util.Collections;
import java.util.Map;
import java.util.Iterator;

import java.util.logging.Logger;

/** 
 * This class is used for storing authorization data for all
 * configured web services. */
public class AuthorizationData {

    /** Used for logging. */
    private static Logger log = Logger.getLogger(AuthorizationData.class.toString());

    /** All configured web services.*/
    private Map webServices = Collections.synchronizedMap(new HashMap());
   


    /** 
     * Creates a new web service register from an xml file. When the
     * register is rebuilt, replace the existing register with the new
     * one.
     * @param xmlfile Filename for the xml configuration file.
     * @return a synchronized Map with all WebService object (from
     * configuration file).
     */
    private Map updateWebServices(String xmlFile) throws Exception{
        log.finer("updateWebServices(String)");

        HashMap attributes = null;
        HashMap profiles = null;
        Map newWebServices = null;

        /* Try to parse the xml file. If it doesn't parse, abort. */
        DOMParser parser = new DOMParser();
        try {
            parser.parse(xmlFile);
        } 
        catch (SAXException e) {
            log.severe("Error parsing \""+xmlFile+"\"\n"+e);
            throw e;
        } 
        catch (IOException e) {
            log.severe("IOException during parsing of \""+xmlFile+"\"\n"+e);
            throw e;
        }


        Document document = parser.getDocument();
        Element root = document.getDocumentElement();

        /* Extract the three main elements; <Attributes> <Profiles>
         * and <WebServices>.*/
        NodeList attributeElems = root.getElementsByTagName("Attributes");
        NodeList profileElems = root.getElementsByTagName("Profiles");
        NodeList wsElems = root.getElementsByTagName("WebServices");

        /* Generate a hashmap of all registered attributes. */
        attributes = getAttributes((Element) attributeElems.item(0), null);

        /* Generate a hashmap of all registered profiles. */
        profiles   = getProfiles((Element) profileElems.item(0), attributes, true);
        
        /* Generate a hashmap of all web services. The WebService
         * objects contains profiles, legal and illegal attributes. */
        newWebServices = getWebServices((Element) wsElems.item(0), attributes, profiles);

        /* Flatten datastructure for all web service objects. */
        for (Iterator iterator = newWebServices.keySet().iterator(); iterator.hasNext();) {
            WebService ws = (WebService) newWebServices.get((String) iterator.next());
            ws.generateAttributeList(attributes);
        }

        return newWebServices;
    }





    /** 
     * Generate a hashmap with all attributes of a dom element. If an
     * existing hash of attributes are supplied, the method performs
     * sanity check - log a warning if the attribute, read from the
     * dom element, doesn't exist. If no existing attributes are
     * supplied (null), then all attributes should be accepted. (This
     * is due to the fact that the method is used to read attributes
     * from the list of legal attributes and from the attributes
     * specified in profiles and web services.
     * @param attrsElem The element that the attributes are located in
     * @param existing The existing attributes used for sanity check.
     */
    private HashMap getAttributes(Element attrsElem, HashMap existing) {
        log.finer("getAttributes(Element, HashMap)");

        HashMap attributes = new HashMap();
        NodeList attrElems = attrsElem.getElementsByTagName("Attribute");

        /* Go through all attribute elements */
        for (int i = 0; i < attrElems.getLength(); i++) { 
            Element attribute = (Element) attrElems.item(i);

            String name = attribute.getAttribute("name");
            String sso = attribute.getAttribute("SSO");
                
            /* Warn if we shuld do sanity check and attribute doesn't
               already exist. */
            if (existing != null &&
                !existing.containsKey(name)) { 
                log.warning("No such attribute: "+name); }

            else if (sso != null) {
                if (sso.equals("true"))
                    attributes.put(name, new Attribute(name, true));
                else
                    /* Default value for SSO is false. */
                    attributes.put(name, new Attribute(name, false));
            }
            else
                attributes.put(name, new Attribute(name));
        }

        return attributes;
    }
 


    /** 
     * Read all profiles from xml. Each profile can contain a list of
     * attributes. If a set of existing profiles are supplied the
     * method performs sanity check on each profile (check for
     * existence). If not a new profile is added to the returning
     * hashmap instead of returning a hashmap with pointers to the
     * existing profile objects. 
     * @param profilesElem The element with all the profile elements.
     * @param existing The existing profiles to use profile pointers from.
     * @param newProfiles Flag if new profiles are going to be created.
     */
    private HashMap getProfiles(Element profilesElem, HashMap existing, boolean newProfiles) {
        log.finer("getProfiles(Element, HashMap, boolean)");

        HashMap profiles = new HashMap();
        NodeList profileElems = profilesElem.getElementsByTagName("Profile");


        for (int i = 0; i < profileElems.getLength(); i++) { 
            Element profileElem = (Element) profileElems.item(i);
            
            /* Generate new profile objects and store in hash. */
            if (newProfiles) {
                Profile profile = new Profile(profileElem.getAttribute("name"));
                profiles.put(profile.getName(), profile);

                profile.setAttributes(getAttributes(profileElem, existing));


            }

            /* Use pointers to existing profile objects. */
            else {
                String name = profileElem.getAttribute("name");

                if (existing.containsKey(name)) 
                    profiles.put(name, existing.get(name));
                else 
                    log.warning("No such profile: "+name);
            }

        }
        return profiles;
    }



    
    /** 
     * Read all webservices from xml. Each web service has an id,
     * name and might be assigned to a set of profiles and attributes.
     * It can also contain a list of illegal attributes.
     * @param wssElem The element that contains the web services
     * @param attributes Legal attributes
     * @param profiles Legal profiles
     * @return HashMap All web services
     */
    private HashMap getWebServices(Element wssElem, HashMap attributes, 
                                   HashMap profiles) {
        log.finer("getWebServices(Element, HashMap, HashMap");

        HashMap webServices = new HashMap();
        NodeList wsElems = wssElem.getElementsByTagName("WebService");

        /* All profiles */
        for (int i = 0; i < wsElems.getLength(); i++) { 
            Element wsElem = (Element) wsElems.item(i);

            /* Create a new WebService with id */
            WebService ws = new WebService(wsElem.getAttribute("id"));
            webServices.put(ws.getId(), ws);

            /* Set WebService's name and url */
            String name = wsElem.getElementsByTagName("Name").item(0).getFirstChild().getNodeValue();
            String url = wsElem.getElementsByTagName("URL").item(0).getFirstChild().getNodeValue();
            ws.setName(name);
            ws.setUrl(url);

            /* Set WebService's attributes (allowed and denied) and
             * profiles */
            NodeList aaElems = wsElem.getElementsByTagName("AllowedAttributes");
            NodeList daElems = wsElem.getElementsByTagName("DeniedAttributes");
            NodeList profileElems = wsElem.getElementsByTagName("Profiles");

            ws.setAllowedAttributes(getAttributes((Element) aaElems.item(0), attributes));
            ws.setDeniedAttributes(getAttributes((Element) daElems.item(0), attributes));
            ws.setProfiles(getProfiles((Element) profileElems.item(0), profiles, false));

        }
        return webServices;
    }

    

    /********************************************************
     * Static method for "offline" testing.                 *
     ********************************************************/


    
    /** 
     * Used for test generation of data structure. (Typically run
     * test before putting a new xml configuration file in
     * production.) 
     */
    public static void main (String[] args) throws Exception {

        /* At least file name is required. */
        if (args.length == 0) {
            usage();
            return;
        }

        AuthorizationData wa = new AuthorizationData();        
        Map allWS = wa.updateWebServices(args[0]);
        
        /* If web service id is specified, only display data for one
         * web service. */
        if (args.length >= 2) {
            if (allWS.containsKey(args[1])) 
                printWSData((WebService) allWS.get(args[1]));
            else 
                System.err.println("No WebService with id: "+args[1]);
        }
                
        /* Display data for all web services. */
        else {
            for (Iterator iterator = allWS.keySet().iterator(); iterator.hasNext();) 
                printWSData((WebService) allWS.get(iterator.next()));
        }
    }



    /** Display usage help.*/
    private static void usage() {
        System.err.println("Usage: java AuthorizationData XML_FILE [wsid]");
    }



    /** 
     * Print all web service data to standard out. Name, id, URL and
     * flattened attribute structure is displayed. 
     */
    private static void printWSData(WebService ws) {
        System.out.println("**********************");
        System.out.println(ws.getName());
        System.out.println("**********************");
        System.out.println("ID:        "+ws.getId());
        System.out.println("URL:       "+ws.getUrl());
        System.out.println("Attributes (allow SSO)");
        System.out.println("......................");

        HashMap attributes = ws.getAttributes();

        for (Iterator iterator = attributes.keySet().iterator(); iterator.hasNext();) { 
            String key = (String) iterator.next();
            System.out.println(key+" ("+attributes.get(key)+")");
        }

        System.out.println("");
    }
}


