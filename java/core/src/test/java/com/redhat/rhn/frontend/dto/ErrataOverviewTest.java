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
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.frontend.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.testing.RhnBaseTestCase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * Tests for {@link ErrataOverview}.
 */
public class ErrataOverviewTest extends RhnBaseTestCase {

    private ErrataOverview overview;

    @Override
    @BeforeEach
    public void setUp() {
        overview = new ErrataOverview();
    }

    @Test
    public void testGetCveNamesWithMultipleCves() {
        overview.setCves(Arrays.asList("CVE-2026-0001", "CVE-2026-0002", "CVE-2026-0003"));
        assertEquals("CVE-2026-0001 CVE-2026-0002 CVE-2026-0003", overview.getCveNames());
    }

    @Test
    public void testGetCveNamesWithSingleCve() {
        overview.setCves(Collections.singletonList("CVE-2026-0001"));
        assertEquals("CVE-2026-0001", overview.getCveNames());
    }

    @Test
    public void testGetCveNamesWithNoCves() {
        // freshly constructed instance starts with an empty list
        assertEquals("", overview.getCveNames());
    }

    @Test
    public void testGetCveNamesWithEmptyList() {
        overview.setCves(Collections.emptyList());
        assertEquals("", overview.getCveNames());
    }

    @Test
    public void testGetCveNamesWithNullList() {
        overview.setCves(null);
        assertEquals("", overview.getCveNames());
    }

    @Test
    public void testAddCveAccumulates() {
        overview.addCve("CVE-2026-1111");
        overview.addCve("CVE-2026-2222");
        // null entries are silently ignored
        overview.addCve(null);
        assertEquals("CVE-2026-1111 CVE-2026-2222", overview.getCveNames());
    }
}
