package no.feide.moria.directory.backend;

import no.feide.moria.directory.DirectoryManagerException;

/**
 * Represents an exception originating form the directory manager's backend.
 */
public class DirectoryManagerBackendException
extends DirectoryManagerException {

    /**
     * Constructor.
     * @param message
     *            The exception message.
     * @param cause
     *            The exception cause.
     */
    public DirectoryManagerBackendException(String message, Throwable cause) {

        super(message, cause);

    }

}