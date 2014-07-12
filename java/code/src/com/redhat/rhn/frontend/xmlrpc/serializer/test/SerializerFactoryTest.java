/**
 * Copyright (c) 2009--2013 Red Hat, Inc.
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
package com.redhat.rhn.frontend.xmlrpc.serializer.test;

import com.redhat.rhn.frontend.xmlrpc.serializer.SerializerFactory;

import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;


public class SerializerFactoryTest extends TestCase {

    private SerializerFactory factory = null;

    public void setUp() throws Exception {
        super.setUp();
        factory = new SerializerFactory();
    }

    public void testFactory() {
        List serializers = factory.getSerializers();
        assertNotNull(serializers);
        assertFalse(serializers.isEmpty());
        for (Iterator itr = serializers.iterator(); itr.hasNext();) {
            Object o = itr.next();
            assertNotNull(o);
        }
    }
}
