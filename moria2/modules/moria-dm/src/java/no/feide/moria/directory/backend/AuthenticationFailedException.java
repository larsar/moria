package no.feide.moria.directory.backend;

/**
 * Used to signal a failed authentication attempt.
 */
public class AuthenticationFailedException
extends BackendException {

    /**
     * Constructor.
     * @param message
     *            The exception message.
     */
    public AuthenticationFailedException(String message) {

        super(message);

    }

}