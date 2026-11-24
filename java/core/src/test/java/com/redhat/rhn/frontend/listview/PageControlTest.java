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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.redhat.rhn.frontend.listview;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.testing.BaseTestCase;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;

public class PageControlTest extends BaseTestCase {

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

    @Test
    public void testDefaultPageSizes() {
        assertFalse(PageControl.getPageSizes().isEmpty());
        assertTrue(PageControl.getPageSizes().
                contains(PageControl.getDefaultPageSize()));
    }

    @Test
    public void testConfigPageSizes() {
        List<Integer> custom = new LinkedList<>();
        custom.add(4);
        custom.add(6);
        custom.add(7);
        custom.add(88);
        custom.add(99);
        custom.add(300);
        Config.get().setString(ConfigDefaults.PAGE_SIZES,
                        StringUtils.join(custom.iterator(), ", "));
        assertEquals(custom, PageControl.getPageSizes());

        assertTrue(custom.contains(PageControl.getDefaultPageSize()));

        Config.get().setString(ConfigDefaults.DEFAULT_PAGE_SIZE,
                                String.valueOf(custom.get(4) - 5));
        assertTrue(custom.contains(PageControl.getDefaultPageSize()));
        assertEquals(custom.get(3),
                (Integer)PageControl.getDefaultPageSize());
    }
}
