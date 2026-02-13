/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.taskomatic.task.repomd;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

public final class SaxSerializerFactory {

    private SaxSerializerFactory() {
        // Prevent instantiation
    }

    public static TransformerHandler newTransformerHandler(boolean omitXmlDeclaration) throws TransformerException {

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        if (transformerFactory == null) {
            throw new TransformerConfigurationException("TransformerFactory returned a null instance");
        }

        // Validate that the factory supports SAX-based transformation
        if (!(transformerFactory instanceof SAXTransformerFactory saxTransformerFactory)) {
            throw new TransformerConfigurationException("Unexpected TransformerFactory instance " +
                transformerFactory.getClass().getName());
        }

        TransformerHandler transformerHandler = saxTransformerFactory.newTransformerHandler();
        Transformer transformer = transformerHandler.getTransformer();

        // Set serialization properties
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, omitXmlDeclaration ? "yes" : "no");
        transformer.setOutputProperty(OutputKeys.INDENT, "no");

        return transformerHandler;
    }
}
