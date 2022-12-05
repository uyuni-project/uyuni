/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.redhat.rhn.common.localization;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Class that extends the java.util.ResourceBundle class that stores
 * the strings in an XML format similar to a property file.
 * <p>
 * The format is as follows:
 * <pre>{@literal
 * <messages>
 *   <msg id="getMessage">Get this</msg>
 *   <msg id="oneArg">one arg: {0}</msg>
 *   <msg id="twoArg">two arg: {0} {1}</msg>
 *   <msg id="threeArg">three arg: {0} {1} {2}</msg>
 * </messages>
 * }</pre>
 * Where the bundle gets built with the keys being the "id" attribute
 * of the XML tag and the values being contained within the value of the
 * {@literal <msg>} tag itself.   Message substitution is supported.
 *
 */
public final class XmlResourceBundle extends java.util.ResourceBundle {

    private static Logger log = LogManager.getLogger(XmlResourceBundle.class);

    /**
     * Map of key/value pairs
     */
    private Map<String, String> strings;

    /** Constructor
     */
    public XmlResourceBundle() {
        // empty
    }

   /**
     * Creates a property resource bundle.
     * @param filelocation location of XML file to parse
     * @throws IOException if the file can't be parsed/loaded
     */
    public XmlResourceBundle(String filelocation) throws IOException {
        strings = new HashMap<>();
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            SAXParser parser = factory.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            XmlResourceBundleParser handler = new XmlResourceBundleParser();
            reader.setContentHandler(handler);
            reader.parse(new InputSource(this.getClass().getResourceAsStream(filelocation)));
            strings = handler.getMessages();
        }
        catch (SAXException | ParserConfigurationException e) {
            // This really should never happen, because without this file,
            // the whole UI stops working.
            log.error("Could not setup parser");
            throw new IOException("Could not load XML bundle: " + filelocation);
        }
    }

    /**
     * Overrides the java.util.ResourceBundle.handleGetObject.
     * @param key the key to lookup out of the bundle
     * @return The value found. This will be a java.lang.String and can be cased
     * accordingly.
     */
    public Object handleGetObject(String key) {
        return strings.get(key);
    }

    /**
     * ResourceBundle.getKeys() implemenatation
     * @return Enumeration of the keys contained in this bundle.
     *         Useful for searching for a partial match.
     */
    public Enumeration<String> getKeys() {
        List<String> keys = new LinkedList<>();

        if (parent != null) {
            Enumeration<String> e = parent.getKeys();
            while (e.hasMoreElements()) {
                keys.add(e.nextElement());
            }
        }

        for (String sIn : strings.keySet()) {
            keys.add(sIn);
        }
        // Ugh, have to convert back to the old Enumeration interface
        // This isn't pretty but it works.
        return new Vector<>(keys).elements();
    }

}
