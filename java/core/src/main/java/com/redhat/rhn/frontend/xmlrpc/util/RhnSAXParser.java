/*
 * Copyright (c) 2015 Red Hat, Inc.
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
package com.redhat.rhn.frontend.xmlrpc.util;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

/**
 * Class to be used as XML parser.
 *
 * It should be set by runtime value org.xml.sax.driver
 * for example in /etc/sysconfig/tomcat* as
 * -Dorg.xml.sax.driver=com.redhat.rhn.frontend.xmlrpc.util.RhnSAXParser
 *
 * Just extending existing parser and setting some handy parameters.
 *
 * It is used by Redstone XMLRPC library.
 */
public class RhnSAXParser extends XMLFilterImpl {

    /**
     * Constructor. In addition sets parameters default parameters.
     *
     * @throws SAXException when an error occurs creating a secure {@link XMLReader}.
     */
    public RhnSAXParser() throws SAXException {
        super(createSecureReader());
    }

    private static XMLReader createSecureReader() throws SAXException {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();

            factory.setNamespaceAware(false);
            factory.setValidating(false);

            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

            return factory.newSAXParser().getXMLReader();

        }
        catch (ParserConfigurationException e) {
            throw new SAXException("Failed to create secure XMLReader", e);
        }
    }
}
