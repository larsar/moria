/*
 * Created on May 26, 2004 TODO To change the template for this generated file
 * go to Window - Preferences - Java - Code Generation - Code and Comments
 */
package no.feide.moria.directory;

import java.util.TimerTask;

import no.feide.moria.log.MessageLogger;

/**
 * @author Cato Olsen TODO To change the template for this generated type
 *         comment go to Window - Preferences - Java - Code Generation - Code
 *         and Comments
 */
public class IndexUpdater
extends TimerTask {

    /** The message logger. */
    private final static MessageLogger log = new MessageLogger(IndexUpdater.class);

    /** The location of the index file. */
    private final String indexFilename;

    /** */
    private final DirectoryManager owner;


    public IndexUpdater(DirectoryManager dm, final String indexFilename) {

        super();

        // Sanity checks.
        if (dm == null)
            throw new IllegalArgumentException("Directory Manager cannot be NULL");
        if (indexFilename == null)
            throw new IllegalArgumentException("Index file name cannot be NULL");

        owner = dm;
        this.indexFilename = indexFilename;

    }


    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {

        owner.updateIndex();

    }

}