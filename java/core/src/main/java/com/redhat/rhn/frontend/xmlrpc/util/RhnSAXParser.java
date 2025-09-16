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

import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

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
public class RhnSAXParser extends SAXParser {
    // This is unnecessary functionality for XMLRPC XML parser.
    private String DISALLOW_DOCTYPE_DECL
        = "http://apache.org/xml/features/disallow-doctype-decl";

    /**
     * Constructor. In addition sets parameters default parameters.
     *
     * @throws SAXNotRecognizedException as SAXParser does.
     * @throws SAXNotSupportedException as SAXParser does.
     */
    public RhnSAXParser() throws SAXNotRecognizedException, SAXNotSupportedException {
        super();
        this.setFeature(DISALLOW_DOCTYPE_DECL, true);
    }
}
