package no.feide.moria.directory.backend;

import no.feide.moria.directory.DirectoryManagerException;

/**
 * Used to signal a failed authentication attempt.
 */
public class AuthenticationFailedException
extends DirectoryManagerException {

    /**
     * Constructor.
     * @param message
     *            The exception message.
     */
    public AuthenticationFailedException(String message) {

        super(message);

    }

}