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
 */

package no.feide.moria.directory.index.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import no.feide.moria.directory.index.SerializableIndex;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * Index creator. Will read a file (given by the first parameter) and parse it
 * into a <code>SerializableIndex</code> object (actually uses
 * <code>WritableSerializableIndex</code> internally). The index object is
 * then written to file (given by the second parameter) for use with the
 * Directory Manager.
 */
public class SerializableIndexCreator {

    /** Internal representation of the index. */
    private static SerializableIndex generatedIndex = new SerializableIndex();


    /**
     * Main method. Will read the index file, write the index object, and
     * finally verify that the generated and written contents match.
     * @param args
     *            First element should be the index specification file, second
     *            element should be the index object output file.
     * @throws IOException
     *             If unable to read from or write to file.
     * @throws JDOMException
     *             If unable to parse the index file.
     * @throws ClassNotFoundException
     *             If unable to read the <code>SerializableIndex</code> object
     *             from file when verifying results.
     */
    public static void main(String[] args)
    throws IOException, JDOMException, ClassNotFoundException {

        // Show usage.
        if (args.length != 2) {
            System.out.println("Usage:\nSerializableIndexCreator indexFile outputFile");
            return;
        }

        // Read index file.
        System.out.println("Reading file " + args[0]);
        final Element rootElement = (new SAXBuilder()).build(new File(args[0])).getRootElement();

        // Process association elements (yes, we support more than one...)
        final List associations = rootElement.getChildren("Associations");
        for (int i = 0; i < associations.size(); i++) {

            // Process realms in this association.
            final List realms = ((Element) associations.get(i)).getChildren("Realm");
            for (int j = 0; j < realms.size(); j++) {

                // Process bases in this realm.
                Element realm = (Element) realms.get(j);
                final List bases = realm.getChildren("Base");
                for (int k = 0; k < bases.size(); k++) {
                    Element base = (Element) bases.get(k);
                    generatedIndex.addAssociation(realm.getAttributeValue("name"), base.getAttributeValue("name"));
                }

            }

        }

        // Process exception elements.
        final List exceptions = rootElement.getChildren("Exception");
        for (int i = 0; i < exceptions.size(); i++) {

            Element exception = (Element) exceptions.get(i);
            generatedIndex.addException(exception.getAttributeValue("id"), exception.getAttributeValue("reference"), exception.getAttributeValue("realm"));

        }

        // Dump the index to console.
        System.out.println(generatedIndex.toString());

        // Write the index to file.
        System.out.println("Writing to file " + args[1]);
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(args[1]));
        out.writeObject(generatedIndex);
        out.close();

        // Read the index from file.
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(args[1]));
        final SerializableIndex writtenIndex = (SerializableIndex) in.readObject();
        in.close();

        // Dump written index to console.
        System.out.println(writtenIndex.toString());

        // Verify index contents.
        if (generatedIndex.equals(writtenIndex))
            System.out.println("Generated and written indexes match");
        else
            System.err.println("Generated and written indexes DO NOT match!");

    }
}