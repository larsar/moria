/*
 * Created on 20.mai.2004 TODO To change the template for this generated file go
 * to Window - Preferences - Java - Code Generation - Code and Comments
 */
package no.feide.moria.directory.backend;

/**
 * The interface to the backend factory.
 */
public interface DirectoryManagerBackendFactory {

    /**
     * Creates a new instance of a proper directory manager backend.
     * @param reference
     *            The backend reference.
     * @return A new instance of the backend, tied to the proper reference.
     */
    public DirectoryManagerBackend createBackend();

}