package no.feide.moria.directory;

/**
 * Used to signal an exception related to the Directory Manager's configuration.
 * <br>
 * <br>
 * Note that this exception is thrown whenever an unrecoverable internal error
 * is encountered, as this will invariably be related to faulty configuration of
 * the Directory Manager. Recoverable internal errors will generally
 * <em>not</em> result in a
 * <code>DirectoryManagerConfigurationException</code>, but in a log message
 * as the Directory Manager attempts to continue with its existing, presumably
 * working, configuration settings.
 */
public class DirectoryManagerConfigurationException
extends RuntimeException {

    /**
     * Constructor. Creates a new exception with only an exception message.
     * @param message
     *            The exception message.
     * @see Exception#Exception(java.lang.String)
     */
    public DirectoryManagerConfigurationException(String message) {

        super(message);

    }


    /**
     * Constructor. Creates a new exception with both an exception message and a
     * cause.
     * @param message
     *            The exception message.
     * @param cause
     *            The exception cause.
     * @see Exception#Exception(java.lang.String, java.lang.Throwable)
     */
    public DirectoryManagerConfigurationException(String message, Throwable cause) {

        super(message, cause);

    }

}