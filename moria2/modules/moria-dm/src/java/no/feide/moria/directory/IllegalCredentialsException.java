package no.feide.moria.directory;

/**
 * Thrown if attempting to create an illegal set of credentials.
 */
public class IllegalCredentialsException
extends DirectoryManagerException {

    /**
     * Constructor.
     * @param message
     *            Exception message.
     */
    public IllegalCredentialsException(String message) {

        super(message);

    }

}