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
package com.redhat.rhn.common.finder.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.finder.Finder;
import com.redhat.rhn.common.finder.FinderFactory;
import com.redhat.rhn.testing.RhnBaseTestCase;

import org.junit.jupiter.api.Test;

import java.util.List;

public class FileFinderTest extends RhnBaseTestCase {

    @Test
    public void testGetFinder() {
        Finder f = FinderFactory.getFinder("com.redhat.rhn.common.finder.test");
        assertNotNull(f);
    }

    @Test
    public void testFindFiles() {
        Finder f = FinderFactory.getFinder("com.redhat.rhn.common.finder.test");
        assertNotNull(f);

        List<String> result = f.find("Test.class");
        String first = result.get(0);
        assertTrue(first.startsWith("com/redhat/rhn/common/finder/test"));
        assertEquals(2, result.size());
    }

    @Test
    public void testFindFilesSubDir() {
        Finder f = FinderFactory.getFinder("com.redhat.rhn.common.finder");
        assertNotNull(f);

        List<String> result = f.find(".class");
        assertEquals(7, result.size());
    }

    @Test
    public void testFindExcluding() {
        Finder f = FinderFactory.getFinder("com.redhat.rhn.common.finder");
        assertNotNull(f);
        String[] sarr = {"Test"};
        List<String> result = f.findExcluding(sarr, "class");
        assertEquals(5, result.size());
    }
}


