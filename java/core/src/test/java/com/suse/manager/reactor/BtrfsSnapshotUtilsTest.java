/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.reactor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.suse.manager.reactor.utils.BtrfsSnapshotUtils;
import com.suse.manager.reactor.utils.BtrfsSnapshotUtils.ParseResult;

import org.junit.jupiter.api.Test;

import java.util.Optional;

/**
 * Unit tests for {@link BtrfsSnapshotUtils}.
 */
public class BtrfsSnapshotUtilsTest {

    private static final String SNAPPER_JSON_TYPICAL = """
            {
              "root": [
                {
                  "number": 0,
                  "active": true,
                  "default": false,
                  "description": "current",
                  "date": "",
                  "userdata": {}
                },
                {
                  "number": 1,
                  "active": false,
                  "default": false,
                  "description": "first boot",
                  "date": "2024-01-01 10:00:00",
                  "userdata": {}
                },
                {
                  "number": 2,
                  "active": false,
                  "default": true,
                  "description": "after update",
                  "date": "2024-06-01 12:00:00",
                  "userdata": {}
                },
                {
                  "number": 3,
                  "active": false,
                  "default": false,
                  "description": "post-update",
                  "date": "2024-09-01 08:00:00",
                  "userdata": {}
                }
              ]
            }
            """;

    private static final String SNAPPER_JSON_WITH_IN_PROGRESS = """
            {
              "root": [
                {
                  "number": 1,
                  "active": false,
                  "default": true,
                  "description": "stable",
                  "date": "2024-01-01 10:00:00",
                  "userdata": {}
                },
                {
                  "number": 2,
                  "active": true,
                  "default": false,
                  "description": "in-progress overlay",
                  "date": "2024-06-01 12:00:00",
                  "userdata": {
                    "transactional-update-in-progress": "yes"
                  }
                }
              ]
            }
            """;

    @Test
    public void testParseEmptyRawJson() {
        assertFalse(BtrfsSnapshotUtils.parse(Optional.empty(), Optional.empty()).isPresent());
        assertFalse(BtrfsSnapshotUtils.parse(Optional.of(""), Optional.empty()).isPresent());
        assertFalse(BtrfsSnapshotUtils.parse(Optional.of("  "), Optional.empty()).isPresent());
    }

    @Test
    public void testParseTypicalOutput() {
        Optional<ParseResult> result = BtrfsSnapshotUtils.parse(
                Optional.of(SNAPPER_JSON_TYPICAL), Optional.of(3L));

        assertTrue(result.isPresent());
        ParseResult r = result.get();

        // Snapshot 0 is filtered out; snapshots 1, 2, 3 remain.
        assertEquals(3, r.getSnapshotNumbers().size());
        assertTrue(r.getSnapshotNumbers().contains(1L));
        assertTrue(r.getSnapshotNumbers().contains(2L));
        assertTrue(r.getSnapshotNumbers().contains(3L));

        // Active is determined from /proc/1/mountinfo (passed in), not from snapper's flag.
        assertEquals(Long.valueOf(3), r.getActiveSnapshot());

        // Default snapshot comes from snapper's "default": true flag.
        assertEquals(Long.valueOf(2), r.getDefaultSnapshot());
    }

    @Test
    public void testParseSnapshotZeroExcluded() {
        Optional<ParseResult> result = BtrfsSnapshotUtils.parse(
                Optional.of(SNAPPER_JSON_TYPICAL), Optional.empty());

        assertTrue(result.isPresent());
        assertFalse(result.get().getSnapshotNumbers().contains(0L),
                "Snapshot 0 (meta-entry) must be excluded");
    }

    @Test
    public void testParseInProgressOverlayExcluded() {
        // JSON has snapshots 1 and 2; snapshot 2 is the in-progress overlay (active=true +
        // transactional-update-in-progress=yes) and must be excluded.
        Optional<ParseResult> result = BtrfsSnapshotUtils.parse(
                Optional.of(SNAPPER_JSON_WITH_IN_PROGRESS), Optional.of(1L));

        assertTrue(result.isPresent());
        ParseResult r = result.get();

        assertEquals(1, r.getSnapshotNumbers().size());
        assertEquals(Long.valueOf(1), r.getSnapshotNumbers().get(0));
        assertEquals(Long.valueOf(1), r.getDefaultSnapshot());
        assertEquals(Long.valueOf(1), r.getActiveSnapshot());
    }

    @Test
    public void testParseNoActiveSnapshotNum() {
        Optional<ParseResult> result = BtrfsSnapshotUtils.parse(
                Optional.of(SNAPPER_JSON_TYPICAL), Optional.empty());

        assertTrue(result.isPresent());
        // Active should be null when /proc/1/mountinfo did not yield a number.
        assertNull(result.get().getActiveSnapshot());
    }

    @Test
    public void testDetailsJsonContainsExpectedFields() {
        Optional<ParseResult> result = BtrfsSnapshotUtils.parse(
                Optional.of(SNAPPER_JSON_TYPICAL), Optional.of(2L));

        assertTrue(result.isPresent());
        String detailsJson = result.get().getDetailsJsonString();

        // The active snapshot (2) should be flagged active in the JSON details.
        assertTrue(detailsJson.contains("\"number\":2"));
        assertTrue(detailsJson.contains("\"active\":true"));
        assertTrue(detailsJson.contains("\"default\":true"));
        assertTrue(detailsJson.contains("\"description\":\"after update\""));
        assertTrue(detailsJson.contains("\"date\":\"2024-06-01 12:00:00\""));

        // Snapshot 0 must not appear in details.
        assertFalse(detailsJson.contains("\"number\":0"));
    }
}
