/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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
package com.redhat.rhn.frontend.taglibs.list.decorators.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.frontend.taglibs.list.decorators.PageSizeDecorator;
import com.redhat.rhn.testing.RhnBaseTestCase;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;


/**
 * PageSizeDecoratorTest
 */
public class PageSizeDecoratorTest extends RhnBaseTestCase {
    @Test
    public void testDefaultPageSizes() {
        assertFalse(PageSizeDecorator.getPageSizes().isEmpty());
        assertTrue(PageSizeDecorator.getPageSizes().
                contains(PageSizeDecorator.getDefaultPageSize()));
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
        assertEquals(custom, PageSizeDecorator.getPageSizes());

        assertTrue(custom.contains(PageSizeDecorator.getDefaultPageSize()));

        Config.get().setString(ConfigDefaults.DEFAULT_PAGE_SIZE,
                                String.valueOf(custom.get(4) - 5));
        assertTrue(custom.contains(PageSizeDecorator.getDefaultPageSize()));
        assertEquals(custom.get(3),
                (Integer)PageSizeDecorator.getDefaultPageSize());
    }
}
