package no.feide.moria.directory.backend;

/**
 * Used to indicate a problem reading attributes from a backend.
 */
public class AttributeReadException
extends BackendException {

    /**
     * Constructor.
     * @param message
     *            The exception message.
     * @param cause
     *            The exception cause.
     */
    public AttributeReadException(String message, Throwable cause) {

        super(message, cause);

    }

}