package no.feide.moria.directory.backend;

import org.jdom.Element;

/**
 * The interface to the backend factory.
 */
public interface DirectoryManagerBackendFactory {

    /**
     * Set the backend factory configuration. Must be called before
     * <code>createBackend()</code> is used.
     * @param config
     *            The backend configuration. See details for the actual backend
     *            implementation used.
     */
    public void setConfig(Element config);


    /**
     * Creates a new instance of a proper directory manager backend.
     * @param reference
     *            The backend reference.
     * @return A new instance of the backend, tied to the proper reference.
     */
    public DirectoryManagerBackend createBackend();

}