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
package com.redhat.rhn.frontend.dto.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.frontend.dto.BooleanWrapper;
import com.redhat.rhn.testing.RhnBaseTestCase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * BooleanWrapperTest
 */
public class BooleanWrapperTest extends RhnBaseTestCase {

    private BooleanWrapper bw;

    @BeforeEach
    public void setUp() throws Exception {
        bw = new BooleanWrapper();
    }

    @Test
    public void testIntegerSet() {
        bw.setBool(1);
        assertTrue(bw.booleanValue());
        assertNotNull(bw.getBool());

        bw.setBool(0);
        assertFalse(bw.booleanValue());
        assertNotNull(bw.getBool());

        bw.setBool(-1);
        assertTrue(bw.booleanValue());
        assertNotNull(bw.getBool());

        bw.setBool(10);
        assertTrue(bw.booleanValue());
        assertNotNull(bw.getBool());

        bw.setBool((Integer)null);
        assertFalse(bw.booleanValue());
        assertNull(bw.getBool());
    }

    @Test
    public void testBooleanSet() {
        bw.setBool(Boolean.TRUE);
        assertTrue(bw.booleanValue());
        assertNotNull(bw.getBool());

        bw.setBool(Boolean.FALSE);
        assertFalse(bw.booleanValue());
        assertNotNull(bw.getBool());

        bw.setBool((Boolean)null);
        assertFalse(bw.booleanValue());
        assertNull(bw.getBool());

    }
}
