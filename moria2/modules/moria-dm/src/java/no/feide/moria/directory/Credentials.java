package no.feide.moria.directory;

/**
 * Represents a set of user credentials - for now, only username/password, which
 * is a dumbed-down version of the more general mechanism in Moria 1.
 */
public class Credentials {

    /** Internal representation of the username . */
    private String username;

    /** Internal representation of the user's password. */
    private String password;


    /**
     * Constructor. Creates a new set of credentials.
     * @param username
     *            The username. May be <code>null</code>.
     * @param password
     *            The user's password. May be <code>null</code>.
     */
    public Credentials(String username, String password) {

        this.username = username;
        this.password = password;

    }


    /**
     * Retrieve the username part of the credentials.
     * @return A new <code>String</code> containing the user name.
     */
    public String getUsername() {

        return new String(username);

    }


    /**
     * Retrieve the password part of the credentials.
     * @return A new <code>String</code> containing the password.
     */
    public String getPassword() {

        return new String(password);

    }

}