/*
 * Created on 20.mai.2004 TODO To change the template for this generated file go
 * to Window - Preferences - Java - Code Generation - Code and Comments
 */
package no.feide.moria.directory.backend;

import java.util.Properties;

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
    public void setConfig(Properties config);


    /**
     * Creates a new instance of a proper directory manager backend.
     * @param reference
     *            The backend reference.
     * @return A new instance of the backend, tied to the proper reference.
     */
    public DirectoryManagerBackend createBackend();

}