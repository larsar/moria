package no.feide.moria.directory.backend;

/**
 * Used to signal a failed authentication attempt.
 */
public class AuthenticationFailedException
extends DirectoryManagerBackendException {

    /**
     * Constructor.
     * @param message
     *            The exception message.
     */
    public AuthenticationFailedException(String message) {

        super(message);

    }

}