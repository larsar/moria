package no.feide.moria.directory;

/**
 * Superclass for all exceptions generated by the directory manager.
 */
public class DirectoryManagerException
extends Exception {

    /**
     * Constructor.
     * @param message
     *            The exception message.
     */
    public DirectoryManagerException(String message) {

        super(message);

    }


    /**
     * Constructor.
     * @param message
     *            The exception message.
     * @param cause
     *            The cause (if any).
     */
    public DirectoryManagerException(String message, Throwable cause) {

        super(message, cause);

    }

}