package no.feide.moria.authorization;

import java.util.logging.Logger;


/**
 * This class represents a LDAP attribute and is used for
 * authorization of a web service. Both Profile and WebService have
 * lists of attributes.
 */
public class Attribute {

    /** Used for logging. */
    private static Logger log = Logger.getLogger(Attribute.class.toString());

    /** Name of attribute */
    private String name = null;

    /** Is this attribute allowd in use with SSO */
    private Boolean sso = null;



    /**
     * Constructor
     * @param name Attribute name
     * @param sso Allow use of SSO with this attribute
     */
    protected Attribute(String name, boolean sso) {
        log.finer("Attribute(String, boolean)");

        this.name = name;
        this.sso = new Boolean(sso);
    }



    /**
     * Constructor
     * @param name Attribute name
     */
    public Attribute(String name) {
        log.finer("Attribute(String)");

        this.name = name;
        this.sso = null;
    }


    
    /**
     * Get name of attribute.
     */
    public String getName() {
        log.finer("getName()");

        return name;
    }



    /**
     * Is the attribute allowed in use with SSO?
     */
    public boolean allowSso() {
        log.finer("allowSso()");

        return sso.booleanValue();
    }


}
