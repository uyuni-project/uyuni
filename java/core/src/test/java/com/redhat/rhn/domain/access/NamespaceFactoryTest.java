/*
 * Copyright (c) 2026 SUSE LLC
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

package com.redhat.rhn.domain.access;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.testing.BaseTestCase;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NamespaceFactoryTest extends BaseTestCase {

    @Test
    public void generatedCoverageTestList() {
        // this test has been generated programmatically to test NamespaceFactory.list
        // containing a hibernate query that is not covered by any test so far
        // feel free to modify and/or complete it
        NamespaceFactory.list();
    }


    @Test
    public void generatedCoverageTestFind() {
        // this test has been generated programmatically to test NamespaceFactory.find
        // containing a hibernate query that is not covered by any test so far
        // feel free to modify and/or complete it
        NamespaceFactory.find("");
    }


    @Test
    public void generatedCoverageTestFind2() {
        // this test has been generated programmatically to test NamespaceFactory.find
        // containing a hibernate query that is not covered by any test so far
        // feel free to modify and/or complete it
        Set<Namespace.AccessMode> arg1 = new HashSet<>();
        NamespaceFactory.find("", arg1);
    }

    @Test
    public void testFindNamespaceWithUnderscore() {
        List<Namespace> found = NamespaceFactory.find("systems.activation_keys.list", Set.of(Namespace.AccessMode.R));

        assertEquals(1, found.size());
        assertEquals("systems.activation_keys.list", found.get(0).getNamespace());
        assertEquals(Namespace.AccessMode.R, found.get(0).getAccessMode());
    }

    @Test
    public void testFindTreatsOnlyAsteriskAsWildcard() {
        List<String> found = NamespaceFactory.find("*_config").stream().map(Namespace::getNamespace).toList();

        assertTrue(found.contains("users.group_config"));
        assertFalse(found.contains("admin.config"));
        assertTrue(NamespaceFactory.find("%").isEmpty());
    }

    @Test
    public void generatedCoverageTestlistByIds() {
        // this test has been generated programmatically to test NamespaceFactory.listByIds
        // containing a hibernate query that is not covered by any test so far
        // feel free to modify and/or complete it
        List<Long> arg1 = new ArrayList<>();
        arg1.add(0L);
        NamespaceFactory.listByIds(arg1);
    }

    @Test
    public void generatedCoverageTestListNamespace() {
        // this test has been generated programmatically to test NamespaceFactory.list
        // containing a hibernate query that is not covered by any test so far
        // feel free to modify and/or complete it
        NamespaceFactory.list("");
    }
}
