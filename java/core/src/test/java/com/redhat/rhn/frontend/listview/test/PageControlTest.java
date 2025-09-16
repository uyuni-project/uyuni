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

package com.redhat.rhn.frontend.listview.test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.testing.RhnBaseTestCase;

import org.junit.jupiter.api.Test;

public class PageControlTest extends RhnBaseTestCase {

    /**
     * Test the basic functionality of PageControl
     */
    @Test
    public void testPageControl() {
        PageControl pc = new PageControl();
        pc.setStart(5);
        pc.setFilterColumn("TestFilterColumn");
        pc.setFilterData("TestFilterData");
        pc.setIndexData(true);

        assertEquals(pc.getStart(), 5);
        assertEquals(pc.getEnd(), 29);
        assertEquals(pc.getFilterColumn(), "TestFilterColumn");
        assertEquals(pc.getFilterData(), "TestFilterData");
        assertTrue(pc.hasIndex());
    }

    /**
     * Test the exception case of setStart.
     */
    @Test
    public void testIllegalArgument() {
        PageControl pc = new PageControl();

        assertThrows(IllegalArgumentException.class, () -> pc.setStart(0));
        assertThrows(IllegalArgumentException.class, () -> pc.setStart(-10));
        assertDoesNotThrow(() -> pc.setStart(10));
    }
}
