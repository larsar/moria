package no.feide.moria.directory.backend;

import java.util.Properties;

/**
 * Factory class for dummy backends.
 */
public class DummyBackendFactory
implements DirectoryManagerBackendFactory {
    
    
    /**
     * Does nothing.
     * @see no.feide.moria.directory.backend.DirectoryManagerBackend#setConfig(java.util.Properties)
     */
    public void setConfig(Properties config) {
        
        // Does nothing.
        
    }

    /**
     * Creates a new <code>DummyBackend</code> instance.
     * @see no.feide.moria.directory.backend.DirectoryManagerBackendFactory#createBackend()
     */
    public DirectoryManagerBackend createBackend() {

        return new DummyBackend();

    }

}