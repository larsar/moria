package no.feide.moria.directory.index;

import java.util.HashMap;

/**
 * Manages the internal representation of affiliations; separate sets for user
 * and group affiliations.
 */
public class RegisterManager {

    private static RegisterManager me = null;

    private static HashMap users;

    private static HashMap groups;

    private static HashMap affiliations;


    /**
     * Private constructor. Creates the internal lists for user and group
     * affiliations.
     */
    private RegisterManager() {
        users = new HashMap();
        groups = new HashMap();
        affiliations = new HashMap();
    }


    /**
     * Provides access to the affiliation manager.
     * @return The running instance of the affiliation manager.
     */
    public static synchronized RegisterManager getInstance() {
        if (me == null)
            me = new RegisterManager();
        return me;
    }


    /**
     * Get the list of user affiliations.
     * @return A reference to the user affiliation object.
     */
    public static HashMap getusers() {
        return users;
    }


    /**
     * Get the list of group affiliations.
     * @return A reference to the group affiliation object.
     */
    public static HashMap getgroups() {
        return groups;
    }


    /**
     * @return
     */
    public static HashMap getAffiliations() {
        return affiliations;
    }

}
