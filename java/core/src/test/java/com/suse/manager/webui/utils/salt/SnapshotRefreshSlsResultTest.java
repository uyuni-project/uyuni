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
                  }
                }
        """.formatted(SnapshotRefreshSlsResult.SNAPPER_LIST_SNAPSHOTS), SnapshotRefreshSlsResult.class);

        assertEquals("{\"root\":[{\"number\":7}]}", result.getSnapperRawStdout().orElseThrow());
    }

    @Test
    public void testParsesPackageRefreshCommandResultsWithNewKey() {
        SnapshotRefreshSlsResult result = resultWithSnapshotResults("""
                {
                  "%s": {
                    "changes": {"stdout": "new"}
                  }
                }
                """.formatted(SnapshotRefreshSlsResult.PACKAGE_SNAPPER_LIST_SNAPSHOTS));

        assertEquals("new", result.getPackageRefreshSnapperRawStdout().orElseThrow());
    }

    @Test
    public void testDedicatedSnapshotKeyDoesNotProvidePackageRefreshResult() {
        SnapshotRefreshSlsResult result = resultWithSnapshotResults("""
                {
                  "%s": {
                    "changes": {"stdout": "legacy"}
                  }
                }
                """.formatted(SnapshotRefreshSlsResult.SNAPPER_LIST_SNAPSHOTS));

        assertEquals("legacy", result.getSnapperRawStdout().orElseThrow());
        assertFalse(result.getPackageRefreshSnapperRawStdout().isPresent());
    }

    @Test
    public void testSnapshotRefreshCommandsKeepSeparateResults() {
        SnapshotRefreshSlsResult result = resultWithSnapshotResults("""
                {
                  "%s": {"changes": {"stdout": "legacy"}},
                  "%s": {"changes": {"stdout": "new"}}
                }
                """.formatted(
                SnapshotRefreshSlsResult.SNAPPER_LIST_SNAPSHOTS,
                SnapshotRefreshSlsResult.PACKAGE_SNAPPER_LIST_SNAPSHOTS));

        assertEquals("legacy", result.getSnapperRawStdout().orElseThrow());
        assertEquals("new", result.getPackageRefreshSnapperRawStdout().orElseThrow());
    }

    @Test
    public void testPackageRefreshCommandResultsAreOptional() {
        SnapshotRefreshSlsResult result = resultWithSnapshotResults("{}");

        assertFalse(result.getPackageRefreshSnapperRawStdout().isPresent());
    }

    @Test
    public void testIgnoresBlankSnapshotOutput() {
        SnapshotRefreshSlsResult result = Json.GSON.fromJson("""
                {
                  "%s": {
                    "result": true,
                    "changes": {
                      "stdout": "   "
                    }
                  },
                  "%s": {
                    "changes": {
                      "stdout": ""
                    }
                  }
                }
                """.formatted(
                SnapshotRefreshSlsResult.SNAPPER_LIST_SNAPSHOTS,
                SnapshotRefreshSlsResult.PACKAGE_SNAPPER_LIST_SNAPSHOTS), SnapshotRefreshSlsResult.class);

        assertFalse(result.getSnapperRawStdout().isPresent());
        assertFalse(result.getPackageRefreshSnapperRawStdout().isPresent());
    }

    private SnapshotRefreshSlsResult resultWithSnapshotResults(String json) {
        return Json.GSON.fromJson(json, SnapshotRefreshSlsResult.class);
    }
}
