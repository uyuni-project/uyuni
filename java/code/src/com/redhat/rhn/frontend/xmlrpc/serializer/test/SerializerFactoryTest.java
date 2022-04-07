/*
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.redhat.rhn.frontend.xmlrpc.serializer.SerializerFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;


public class SerializerFactoryTest  {

    private SerializerFactory factory = null;

    @BeforeEach
    public void setUp() throws Exception {

        factory = new SerializerFactory();
    }

    @Test
    public void testFactory() {
        List serializers = factory.getSerializers();
        assertNotNull(serializers);
        assertFalse(serializers.isEmpty());
        for (Object o : serializers) {
            assertNotNull(o);
        }
    }
}
