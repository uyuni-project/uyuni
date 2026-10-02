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

import java.util.List;
import java.util.Map;
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
                  "type": "single",
                  "pre-number": null,
                  "description": "after update",
                  "date": "2024-06-01 12:00:00",
                  "user": "root",
                  "used-space": 12345678,
                  "cleanup": "number",
                  "userdata": {
                    "important": "yes"
                  }
                },
                {
                  "number": 3,
                  "active": true,
                  "default": false,
                  "description": "post-update",
                  "date": "2024-09-01 08:00:00",
                  "userdata": {}
                }
              ]
            }
            """;

    private static final String SNAPPER_JSON_WITH_MATCHING_ACTIVE_DEFAULT = """
            {
              "root": [
                {
                  "number": 1,
                  "active": true,
                  "default": true,
                  "description": "stable",
                  "date": "2024-01-01 10:00:00",
                  "userdata": {}
                }
              ]
            }
            """;

    @Test
    public void testParseEmptyRawJson() {
        assertFalse(BtrfsSnapshotUtils.parse(Optional.empty()).isPresent());
        assertFalse(BtrfsSnapshotUtils.parse(Optional.of("")).isPresent());
        assertFalse(BtrfsSnapshotUtils.parse(Optional.of("  ")).isPresent());
    }

    @Test
    public void testParseTypicalOutput() {
        Optional<ParseResult> result = BtrfsSnapshotUtils.parse(Optional.of(SNAPPER_JSON_TYPICAL));

        assertTrue(result.isPresent());
        ParseResult r = result.get();

        // Snapshot 0 is filtered out; snapshots 1, 2, 3 remain.
        List<Map<String, Object>> details = r.getSnapshotDetails();
        assertEquals(3, details.size());
        assertEquals(1L, details.get(0).get("number"));
        assertEquals(2L, details.get(1).get("number"));
        assertEquals(3L, details.get(2).get("number"));

        // Active snapshot comes from snapper's "active": true flag.
        assertEquals(Long.valueOf(3), r.getActiveSnapshot());

        // Default snapshot comes from snapper's "default": true flag.
        assertEquals(Long.valueOf(2), r.getDefaultSnapshot());
    }

    @Test
    public void testParseSnapshotZeroExcluded() {
        Optional<ParseResult> result = BtrfsSnapshotUtils.parse(Optional.of(SNAPPER_JSON_TYPICAL));

        assertTrue(result.isPresent());
        assertTrue(result.get().getSnapshotDetails().stream()
                .noneMatch(snapshot -> Long.valueOf(0).equals(snapshot.get("number"))),
                "Snapshot 0 (meta-entry) must be excluded");
    }

    @Test
    public void testPendingTransactionalRebootIsDetectedFromActiveAndDefaultSnapshots() {
        Optional<ParseResult> result = BtrfsSnapshotUtils.parse(Optional.of(SNAPPER_JSON_TYPICAL));

        assertTrue(result.isPresent());
        assertTrue(BtrfsSnapshotUtils.hasPendingTransactionalReboot(result.get()));
    }

    @Test
    public void testPendingTransactionalRebootIsFalseWhenActiveAndDefaultMatch() {
        Optional<ParseResult> result = BtrfsSnapshotUtils.parse(Optional.of(SNAPPER_JSON_WITH_MATCHING_ACTIVE_DEFAULT));

        assertTrue(result.isPresent());
        assertFalse(BtrfsSnapshotUtils.hasPendingTransactionalReboot(result.get()));
    }

    @Test
    public void testSnapshotDetailsContainExpectedFields() {
        Optional<ParseResult> result = BtrfsSnapshotUtils.parse(Optional.of(SNAPPER_JSON_TYPICAL));

        assertTrue(result.isPresent());
        List<Map<String, Object>> details = result.get().getSnapshotDetails();

        Map<String, Object> snapshot = details.get(1);
        assertEquals(2L, snapshot.get("number"));
        assertEquals(false, snapshot.get("active"));
        assertEquals(true, snapshot.get("default"));
        assertEquals("single", snapshot.get("type"));
        assertNull(snapshot.get("preNumber"));
        assertEquals("root", snapshot.get("user"));
        assertEquals(12345678L, snapshot.get("usedSpace"));
        assertEquals("number", snapshot.get("cleanup"));
        assertEquals("after update", snapshot.get("description"));
        assertEquals("2024-06-01 12:00:00", snapshot.get("date"));
        assertEquals("important=yes", snapshot.get("userdata"));

        assertEquals(3L, details.get(2).get("number"));
        assertEquals(true, details.get(2).get("active"));

        // Snapshot 0 must not appear in details.
        assertTrue(details.stream()
                .noneMatch(entry -> Long.valueOf(0).equals(entry.get("number"))));
    }

    @Test
    public void testParseSnapshotDetailsForPublicApi() {
        Optional<ParseResult> result = BtrfsSnapshotUtils.parse(Optional.of(SNAPPER_JSON_TYPICAL));

        assertTrue(result.isPresent());
        List<Map<String, Object>> details = result.get().getSnapshotDetails();

        assertEquals(3, details.size());
        assertEquals(2L, details.get(1).get("number"));
        assertEquals(false, details.get(1).get("active"));
        assertEquals(true, details.get(1).get("default"));
        assertEquals("single", details.get(1).get("type"));
        assertEquals("root", details.get(1).get("user"));
        assertEquals(12345678L, details.get(1).get("usedSpace"));
        assertEquals("number", details.get(1).get("cleanup"));
        assertEquals("after update", details.get(1).get("description"));
        assertEquals("2024-06-01 12:00:00", details.get(1).get("date"));
        assertEquals("important=yes", details.get(1).get("userdata"));
    }

}
