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


public class AuthorizationData {

    /** Used for logging. */
    private static Logger log = Logger.getLogger(AuthorizationData.class.toString());

    private Map webServices = Collections.synchronizedMap(new HashMap());
   


    private synchronized HashMap updateWebServices(String xml) {
        HashMap attributes = null;
        HashMap profiles = null;
        Map newWebservices = null;

        DOMParser parser = new DOMParser();
      
        try {
            parser.parse(xml);
        } 
        catch (SAXException e) {
            System.err.println (e);
        } 
        catch (IOException e) {
            System.err.println (e);
        }


        Document document = parser.getDocument();
        Node root = document.getDocumentElement(); // WebServices

        NodeList children = root.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {

            Node node = children.item(i);
            String nodeName = node.getNodeName();

            if (nodeName.equals("Attributes")) {
                attributes = getAttributes(node);
            }

            else if (nodeName.equals("Profiles")) {
                profiles = getProfiles(node, attributes);
            }

            if (nodeName.equals("WebServices")) {
                System.out.println("Foobat");
                newWebservices = Collections.synchronizedMap(getWebServices(node, attributes, profiles));
            }

        }

 

      return null;

    }




    private HashMap getAttributes(Node attrNode) {
        HashMap attributes = new HashMap();
        NodeList attrNodes = attrNode.getChildNodes();

        for (int i = 0; i < attrNodes.getLength(); i++) { 
            Node node = attrNodes.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                String name = node.getAttributes().getNamedItem("name").getNodeValue();
                String sso = node.getAttributes().getNamedItem("SSO").getNodeValue();
                attributes.put(name, new Attribute(name, sso));
            }
        }

        return attributes;
    }
 

    private HashMap getProfiles(Node profileNode, HashMap attributes) {
        HashMap profiles = new HashMap();
        NodeList profileNodes = profileNode.getChildNodes();


        for (int i = 0; i < profileNodes.getLength(); i++) { 
            Node node = profileNodes.item(i);
            
            /* <Profile> */
            if (node.getNodeType() == Node.ELEMENT_NODE) {

                Profile profile = new Profile(node.getAttributes().getNamedItem("name").getNodeValue());
                profiles.put(profile.getName(), profile);
                NodeList attrNodes = node.getChildNodes();

                HashMap profAttributes = getAttributes(node);

                for (Iterator iterator = profAttributes.keySet().iterator(); iterator.hasNext();) {
                    Attribute profAttribute = (Attribute) profAttributes.get((String) iterator.next());
                    if (attributes.containsKey(profAttribute.getName())) 
                        profile.addAttribute(profAttribute, profAttribute.getSso());
                    else
                        log.severe("No such attribute: "+profAttribute.getName());
                }
                        
            }
        }
       
        return profiles;

    }


    private HashMap getWebServices(Node wsNode, HashMap attributes, HashMap profiles) {
        HashMap webServices = new HashMap();
        NodeList wsNodes = wsNode.getChildNodes();

        for (int i = 0; i < wsNodes.getLength(); i++) { 
            Node node = wsNodes.item(i);

            /* <WebService> */
            if (node.getNodeType() == Node.ELEMENT_NODE) {

                WebService ws = new WebService(node.getAttributes().getNamedItem("id").getNodeValue());
                NodeList elements = node.getChildNodes();
                

          /* <WebService> */
                for (int j = 0; j < elements.getLength(); j++) {
                    Node element = elements.item(j);


                    if (element.getNodeType() == Node.ELEMENT_NODE) {
                        if (element.getNodeName().equals("name")) {
                            ws.setName(element.getFirstChild().getNodeValue());
                        }
                        else if (element.getNodeName().equals("URL")) {
                            //                          ws.setURL(element.getFirstChild().getNodeValue());
                        }

                    }

                     
                }
                
               


            }

        }

        return webServices;
        
    }


   // Main Method
   public static void main (String[] args) {
       AuthorizationData wa = new AuthorizationData();
       wa.updateWebServices(args[0]);
   }
}


