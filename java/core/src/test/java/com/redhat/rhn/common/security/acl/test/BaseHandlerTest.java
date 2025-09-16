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
package com.redhat.rhn.common.security.acl.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.security.acl.BaseHandler;
import com.redhat.rhn.testing.RhnBaseTestCase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * BaseHandlerTest
 */
public class BaseHandlerTest extends RhnBaseTestCase {

    private TestHandler th;

    @Override
    @BeforeEach
    public void setUp() {
        th = new TestHandler();
    }

    @Test
    public void testMutlivaluedStringArray() {
        String[] s = { "10", "20" };
        Long rc = th.getAsLong(s);
        assertNotNull(rc);
        assertEquals(Long.valueOf(10), rc);
    }

    @Test
    public void testSingleStringArray() {
        String[] s = { "20" };
        Long rc = th.getAsLong(s);
        assertNotNull(rc);
        assertEquals(Long.valueOf(20), rc);
    }

    @Test
    public void testString() {
        Long rc = th.getAsLong("20");
        assertNotNull(rc);
        assertEquals(Long.valueOf(20), rc);
    }

    @Test
    public void testNull() {
        Long rc = th.getAsLong(null);
        assertNull(rc);
    }

    @Test
    public void testUnparsable() {
        try {
            th.getAsLong("foobar");
            fail("somehow foobar was converted to a Long");
        }
        catch (NumberFormatException nfe) {
            assertTrue(true);
        }

        try {
            String[] s = { "foobar", "10" };
            th.getAsLong(s);
            fail("somehow foobar was converted to a Long");
        }
        catch (NumberFormatException nfe) {
            assertTrue(true);
        }
    }

    @Test
    public void testLongParam() {
        Long param = 10L;
        Long rc = th.getAsLong(param);
        assertEquals(param, rc);
    }

    public static class TestHandler extends BaseHandler {
    }
}
