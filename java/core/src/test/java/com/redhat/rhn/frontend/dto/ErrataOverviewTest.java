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
package com.redhat.rhn.frontend.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.redhat.rhn.testing.BaseTestCase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

/**
 * Tests for {@link ErrataOverview}.
 */
public class ErrataOverviewTest extends BaseTestCase {

    private ErrataOverview overview;

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

    @Test
    public void testIsoFormatGetters() {
        overview.setIssueDate(localDate(2021, 11, 22, 23, 59, 59));
        overview.setUpdateDate(localDate(2024, 3, 5, 0, 0, 0));

        assertEquals("2021-11-22", overview.getIssueDateIsoFormat());
        assertEquals("2024-03-05", overview.getUpdateDateIsoFormat());
    }

    @Test
    public void testIsoFormatGettersWithoutDates() {
        assertNull(overview.getIssueDateIsoFormat());
        assertNull(overview.getUpdateDateIsoFormat());
    }

    @Test
    public void testSetIssueDateFromDbString() throws ParseException {
        overview.setIssueDate("2021-11-22 10:20:30");
        assertEquals(localDate(2021, 11, 22, 10, 20, 30), overview.getIssueDateObj());

        // java.sql.Timestamp.toString() format, reaches this setter when the datasource converts a raw timestamp
        overview.setIssueDate("2021-11-22 10:20:30.123456");
        assertEquals(localDate(2021, 11, 22, 10, 20, 30), overview.getIssueDateObj());
    }

    @Test
    public void testSetIssueDateFromDateOnlyString() throws ParseException {
        overview.setIssueDate("2021-11-22");

        assertEquals(localDate(2021, 11, 22, 0, 0, 0), overview.getIssueDateObj());
    }

    @Test
    public void testSetIssueDateFromStringRejectsOtherFormats() {
        assertThrows(ParseException.class, () -> overview.setIssueDate("22/11/2021"));
    }

    private static Date localDate(int year, int month, int day, int hour, int minute, int second) {
        LocalDateTime local = LocalDateTime.of(year, month, day, hour, minute, second);
        return Date.from(local.atZone(ZoneId.systemDefault()).toInstant());
    }
}
