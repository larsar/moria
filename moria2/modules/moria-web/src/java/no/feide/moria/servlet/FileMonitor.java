/*
 * Copyright (c) 2004 UNINETT FAS
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * $Id$
 */
package no.feide.moria.servlet;

import java.io.File;
import java.io.FileNotFoundException;

/**
 *
 * Based (in part) on ConfigurationManager#FileListenerTask
 * 
 * @author Eva Indal
 * @version $Revision$
 */
public class FileMonitor {
    
    /**
     * The monitored file
     */
    private final File monitoredFile;

    /**
     * Modification time of the watched file.
     */
    private long lastModified;

    /**
     * Constructor.
     * 
     * @param fileURI The file to monitor
     * @throws FileNotFoundException
     */
    public FileMonitor(final String fileURI) throws FileNotFoundException {
        monitoredFile = fileForURI(fileURI);
        this.lastModified = monitoredFile.lastModified();
    }
    
    /**
     * Returns <code>true</code> if the monitored file has changed.
     * 
     * @return <code>true</code> if file has changed 
     */
    public boolean hasChanged() {
        return monitoredFile.lastModified() != lastModified;
    }
    
    /**
     * Resolves a fileURI to a <code>File</code> object.
     * @param fileURI
     *            Reference to the file (full path or relative within the
     *            classpath).
     * @return A <code>File</code> object referenced by the fileURI.
     * @throws FileNotFoundException
     *             If the fileURI cannot be resolved to a readable file.
     */
    private static File fileForURI(final String fileURI) throws FileNotFoundException {

        if (fileURI == null || fileURI.equals("")) { throw new FileNotFoundException("File reference cannot be null."); }

        final File file = new File(fileURI);
        return file;
    }


}
