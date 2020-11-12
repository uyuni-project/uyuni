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
package com.redhat.rhn.frontend.xmlrpc.apitest.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.redhat.rhn.frontend.xmlrpc.apitest.TestHandler;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * TestHandlerTest
 */
public class TestHandlerTest extends BaseHandlerTestCase {

    @Test
    public void testAddition() {
        TestHandler th = new TestHandler();
        int[] numbers = {1, 2, 3, 4, 5, 6};
        assertEquals(21, th.addition(numbers));

        assertEquals(0, th.multiplication(null));

        numbers = new int[0];
        assertEquals(0, th.addition(numbers));
    }

    @Test
    public void testEnvIsSatellite() {
        TestHandler th = new TestHandler();
        assertEquals(1, th.envIsSatellite());
    }

    @Test
    public void testHashChecking() {
        TestHandler th = new TestHandler();
        Map map = th.hashChecking(new HashMap());
        assertNotNull(map);
        assertEquals("baz", map.get("foobar"));
    }

    @Test
    public void testMultiplication() {
        TestHandler th = new TestHandler();
        int[] numbers = {1, 2, 3, 4, 5, 6};
        assertEquals(720, th.multiplication(numbers));

        assertEquals(0, th.multiplication(null));

        numbers = new int[0];
        assertEquals(0, th.multiplication(numbers));
    }

    @Test
    public void testSingleIdentityFunction() {
        TestHandler th = new TestHandler();
        String foo = "foobar";
        assertEquals(foo, th.singleIdentityFunction(foo));
    }
}
