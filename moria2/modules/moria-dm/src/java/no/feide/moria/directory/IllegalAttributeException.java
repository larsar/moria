package no.feide.moria.directory;

/**
 * Represents an exception related to an illegal attribute.
 */
public class IllegalAttributeException
extends DirectoryManagerException {

    /**
     * Constructor.
     * @param message
     *            The exception message, if any.
     */
    public IllegalAttributeException(String message) {

        super(message);

    }


    /**
     * Constructor.
     * @param message
     *            The exception message, if any.
     * @param cause
     *            The exception cause, if any.
     */
    public IllegalAttributeException(String message, Throwable cause) {

        super(message, cause);

    }

}