package no.feide.moria.directory.backend;


/**
 * Factory class for dummy backends.
 */
public class DummyBackendFactory
implements DirectoryManagerBackendFactory {

    /**
     * Constructor. Does nothing.
     */
    public DummyBackendFactory() {

        super();
        
    }


    /**
     * Creates a new <code>DummyBackend</code> instance.
     * @see no.feide.moria.directory.backend.DirectoryManagerBackendFactory#createBackend()
     */
    public DirectoryManagerBackend createBackend() throws BackendException {

        return new DummyBackend();
        
    }

}
