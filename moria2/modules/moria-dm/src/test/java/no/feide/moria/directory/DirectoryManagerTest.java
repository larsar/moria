/*
 * Copyright (c) 2004 FEIDE This program is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program; if not, write
 * to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 * MA 02111-1307, USA.
 */
package no.feide.moria.directory;

import java.util.Properties;
//import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import no.feide.moria.directory.DirectoryManagerConfiguration;

/**
 * JUnit tests for the DirectoryManager class.
 * @author Cato Olsen
 */
public class DirectoryManagerTest
extends TestCase {

    private Properties config;


    /**
     * Returns the full test suite.
     * @return The test suite.
     */
    public static Test suite() {

        return new TestSuite(DirectoryManagerTest.class);
    }


    public void setUp() {

        config = new Properties();
        config.setProperty("directoryConfiguration", "src/test/conf/DirectoryManagerConfiguration.xml");

    }


    public void tearDown() {

        config = null;

    }


    public void testConfiguration()
    throws DirectoryManagerException {

        DirectoryManagerConfiguration.read(config);

    }

}