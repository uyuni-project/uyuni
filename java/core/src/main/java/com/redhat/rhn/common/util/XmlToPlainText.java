/*
 * Copyright (c) 2026 SUSE LLC
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.common.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * XmlToPlainText - Helper class that uses StringResources XML
 */
class XmlToPlainText {

    private static final Logger LOG = LogManager.getLogger(XmlToPlainText.class);

    private static final String IGNORABLES = ".,;'\"?";

    private StringBuilder plainText;
    private String href;

    /**
     * Converts an xml/html snippet to a plain text string.
     *
     * @param snippet the xml snippet to convert..
     * @return returns the converted plain text or the original xml in the case of an error.
     */
    public String convert(String snippet) {
        plainText = new StringBuilder();

        try (Reader reader = new StringReader("<foo>" + snippet + "</foo>")) {
            var documentBuilder = getDocumentBuilderFactory().newDocumentBuilder();
            Document doc = documentBuilder.parse(new InputSource(reader));

            toPlainText(doc.getDocumentElement());

            return plainText.toString();
        }
        catch (SAXException | ParserConfigurationException | IOException ex) {
            LOG.warn("Couldn't parse the snippet -> [{}]", snippet, ex);
            return snippet;
        }
    }

    private void toPlainText(Node current) {
        if (current instanceof Text text) {
            process(text);
        }
        else if (current instanceof Element elem) {
            if ("a".equalsIgnoreCase(elem.getTagName())) {
                href = elem.getAttribute("href").trim();
            }
            NodeList children = elem.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                toPlainText(children.item(i));
            }
        }
    }

    private void process(Text current) {
        String text = StringUtils.trimToNull(current.getTextContent());
        if (text == null) {
            return;
        }

        if (!plainText.isEmpty() && !IGNORABLES.contains(text)) {
            plainText.append(" ");
        }

        plainText.append(text);

        if (!StringUtils.isBlank(href)) {
            plainText.append(" (").append(href).append(")");
            href = null;
        }
    }

    private static DocumentBuilderFactory getDocumentBuilderFactory() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        factory.setNamespaceAware(false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);

        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

        return factory;
    }
}
