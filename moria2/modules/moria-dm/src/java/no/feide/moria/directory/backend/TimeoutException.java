package no.feide.moria.directory.backend;

/**
 * Used to signal that a backend connection has timed out.
 */
public class TimeoutException
extends BackendException {

    /**
     * Constructor.
     * @param message
     *            The error message.
     * @param cause
     *            The cause of this exception.
     */
    public TimeoutException(String message, Throwable cause) {

        super(message, cause);

    }

}