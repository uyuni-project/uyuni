/*
 * Copyright (c) 2009--2017 Red Hat, Inc.
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
import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Internal class for SAX parsing the XML ResourceBundles.
 * See XmlResourceBundle for usage.
 */

class XmlResourceBundleParser  extends DefaultHandler {

    private final Map<String, String> messages = new HashMap<>();
    private StringBuilder thisText = new StringBuilder();
    private String currKey;
    private static final Logger LOG = LogManager.getLogger(XmlResourceBundleParser.class);

    /** constructor
     */
    XmlResourceBundleParser() {
        super();
    }


    /** {@inheritDoc} */
    @Override
    public void startElement(String namespaceUri, String localName,
                             String qualifiedName, Attributes attributes) {

        thisText = new StringBuilder();
        if (qualifiedName.equals("trans-unit")) {
            currKey = attributes.getValue("id");
        }

    }

    /** {@inheritDoc} */
    @Override
    public void endElement(String namespaceUri, String localName,
                           String qualifiedName) {

        if (thisText.length() > 0) {
            // For the en_US files we use source
            if (qualifiedName.equals("source")) {
                if (messages.containsKey(currKey)) {
                    LOG.warn("Duplicate message key found in XML Resource file: {}", currKey);
                }
                LOG.debug("Adding: [{}] value: [{}]", currKey, thisText);
                messages.put(currKey, thisText.toString());
            }
            // For other languages we use target and overwrite the previously
            // placed "source" tag.  Depends on the fact that the target tag
            // comes after the source tag.
            if (qualifiedName.equals("target")) {
                LOG.debug("Adding: [{}] value: [{}]", currKey, thisText);
                messages.put(currKey, thisText.toString());
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void warning(SAXParseException e) {
        LOG.error("SAXParseException Warning: ");
        printInfo(e);
    }

    /** {@inheritDoc} */
    @Override
    public void error(SAXParseException e) {
        LOG.error("SAXParseException Error: ");
        printInfo(e);
    }

    /** {@inheritDoc} */
    @Override
    public void fatalError(SAXParseException e) {
        LOG.error("SAXParseException Fatal error: ");
        printInfo(e);
    }

    private void printInfo(SAXParseException e) {
        LOG.error("   Message key: {}", currKey);
        LOG.error("   Public ID: {}", e.getPublicId());
        LOG.error("   System ID: {}", e.getSystemId());
        LOG.error("   Line number: {}", e.getLineNumber());
        LOG.error("   Column number: {}", e.getColumnNumber());
        LOG.error("   Message: {}", e.getMessage());
    }



    /** {@inheritDoc} */
    @Override
    public void characters(char[] ch, int start, int length) {
        String appendme = new String(ch, start, length);
        thisText.append(appendme);
    }

    /**
     * Return the Map of the messages that was
     * produced while parsing the file
     * @return The map ..
     */
    public Map<String, String> getMessages() {
        return messages;
    }

}
