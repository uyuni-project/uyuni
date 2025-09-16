/*
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
package com.redhat.rhn.frontend.html.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.frontend.html.XmlTag;
import com.redhat.rhn.testing.RhnBaseTestCase;

import org.junit.jupiter.api.Test;

public class XmlTagTest extends RhnBaseTestCase {

    @Test
    public void testSpaceBefore() {
        XmlTag xml = new XmlTag("foo", true);
        assertEquals("<foo />", xml.render());
    }

    @Test
    public void testXmltag() {
        XmlTag xml = new XmlTag("foo");
        assertEquals("<foo/>", xml.render());
    }
}
