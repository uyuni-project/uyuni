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
package com.suse.manager.webui.utils.salt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.suse.manager.webui.utils.salt.custom.SnapshotRefreshSlsResult;
import com.suse.utils.Json;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link SnapshotRefreshSlsResult}.
 */
public class SnapshotRefreshSlsResultTest {

    @Test
    public void testParsesSnapshotRefreshCommandResults() {
        SnapshotRefreshSlsResult result = Json.GSON.fromJson("""
                {
                  "%s": {
                    "name": "snapper --json --no-dbus list",
                    "result": true,
                    "changes": {
                      "pid": 123,
                      "retcode": 0,
                      "stdout": "{\\"root\\":[{\\"number\\":7}]}",
                      "stderr": ""
                    },
                    "comment": ""
                  },
                  "%s": {
                    "name": "awk",
                    "result": true,
                    "changes": {
                      "pid": 124,
                      "retcode": 0,
                      "stdout": "42\\n",
                      "stderr": ""
                    },
                    "comment": ""
                  }
                }
                """.formatted(SnapshotRefreshSlsResult.SNAPPER_LIST_SNAPSHOTS,
                SnapshotRefreshSlsResult.GET_ACTIVE_SNAPSHOT), SnapshotRefreshSlsResult.class);

        assertEquals("{\"root\":[{\"number\":7}]}", result.getSnapperRawStdout().orElseThrow());
        assertEquals(42L, result.getActiveSnapshotNumber().orElseThrow());
    }

    @Test
    public void testIgnoresBlankSnapshotOutputAndInvalidActiveSnapshot() {
        SnapshotRefreshSlsResult result = Json.GSON.fromJson("""
                {
                  "%s": {
                    "result": true,
                    "changes": {
                      "stdout": "   "
                    }
                  },
                  "%s": {
                    "result": true,
                    "changes": {
                      "stdout": "not-a-number"
                    }
                  }
                }
                """.formatted(SnapshotRefreshSlsResult.SNAPPER_LIST_SNAPSHOTS,
                SnapshotRefreshSlsResult.GET_ACTIVE_SNAPSHOT), SnapshotRefreshSlsResult.class);

        assertFalse(result.getSnapperRawStdout().isPresent());
        assertFalse(result.getActiveSnapshotNumber().isPresent());
    }
}
