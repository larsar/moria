package no.feide.moria.directory;

/**
 * @author Cato Olsen Used to signal an exception related to the directory
 *         manager's configuration.
 */
public class DirectoryManagerConfigurationException
extends DirectoryManagerException {

    /**
     * Constructor.
     * @param message
     *            The exception message.
     * @param cause
     *            The exception cause.
     */
    public DirectoryManagerConfigurationException(String message, Throwable cause) {

        super(message, cause);

    }

}