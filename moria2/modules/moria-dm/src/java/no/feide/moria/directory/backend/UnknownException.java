package no.feide.moria.directory.backend;

/**
 * Used to signal that we have received any number of unexpected backend
 * exception that we do not know how to explicitly handle.
 */
public class UnknownException
extends BackendException {

    /**
     * Constructor. Will create an exception with the error message "Unknown
     * exception" and the given cause.
     * @param cause
     *            The exception cause.
     */
    public UnknownException(Throwable cause) {

        super("Unknown exception", cause);

    }

}