package no.feide.moria.directory.backend;

/**
 * Represents an exception originating form the directory manager's backend.
 */
public class BackendException
extends Exception {

    /**
     * Constructor.
     * @param message
     *            The exception message.
     */
    public BackendException(String message) {

        super(message);

    }


    /**
     * Constructor.
     * @param message
     *            The exception message.
     * @param cause
     *            The exception cause.
     */
    public BackendException(String message, Throwable cause) {

        super(message, cause);

    }

}