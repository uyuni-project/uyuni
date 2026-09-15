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
package com.suse.manager.reactor.utils;

import com.redhat.rhn.domain.server.MinionServer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Utility methods for processing Btrfs snapshot information on
 * transactional minions (SLE Micro, Leap Micro, openSUSE MicroOS).
 */
public class BtrfsSnapshotUtils {

    private static final Logger LOG = LogManager.getLogger(BtrfsSnapshotUtils.class);

    private BtrfsSnapshotUtils() { }

    /**
     * Result of parsing a snapper JSON output.
     */
    public static class ParseResult {
        private final Long activeSnapshot;
        private final Long defaultSnapshot;
        private final List<Map<String, Object>> snapshotDetails;

        /**
         * @param activeSnapshotIn   the active snapshot number, or null
         * @param defaultSnapshotIn  the default snapshot number, or null
         * @param snapshotDetailsIn per-snapshot detail objects
         */
        public ParseResult(Long activeSnapshotIn, Long defaultSnapshotIn,
                           List<Map<String, Object>> snapshotDetailsIn) {
            this.activeSnapshot = activeSnapshotIn;
            this.defaultSnapshot = defaultSnapshotIn;
            this.snapshotDetails = snapshotDetailsIn;
        }

        /**
         * @return the number of the currently active (booted) Btrfs snapshot, or null
         */
        public Long getActiveSnapshot() {
            return activeSnapshot;
        }

        /**
         * @return the number of the default (next-boot) Btrfs snapshot, or null
         */
        public Long getDefaultSnapshot() {
            return defaultSnapshot;
        }

        /**
         * @return per-snapshot detail objects
         */
        public List<Map<String, Object>> getSnapshotDetails() {
            return snapshotDetails;
        }
    }

    /**
     * Parse raw snapper JSON output and determine active/default/all snapshots.
     *
     * Snapshot 0 (the "current running subvolume" meta-entry) is excluded.
     *
     * @param rawJson          raw stdout from {@code snapper --json --no-dbus list}
     * @return parsed result, or empty if the JSON is absent / contains no valid snapshots
     */
    public static Optional<ParseResult> parse(Optional<String> rawJson) {
        String json = rawJson.orElse(null);
        if (json == null || json.isBlank()) {
            return Optional.empty();
        }

        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        JsonArray arr = obj.getAsJsonArray("root");
        if (arr == null) {
            return Optional.empty();
        }

        Long activeSnapshot = null;
        Long defaultSnapshot = null;
        List<Map<String, Object>> details = new ArrayList<>();

        for (JsonElement element : arr) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject snap = element.getAsJsonObject();
            JsonElement activeEl = snap.get("active");
            boolean isActiveInSnapper = activeEl != null && activeEl.isJsonPrimitive() && activeEl.getAsBoolean();

            JsonElement numEl = snap.get("number");
            if (numEl == null || !numEl.isJsonPrimitive()) {
                continue;
            }
            long num = numEl.getAsLong();
            if (num == 0) {
                // Snapshot 0 is the "current running subvolume" meta-entry, not a real snapshot.
                continue;
            }

            JsonElement defEl = snap.get("default");
            boolean isDefault = defEl != null && defEl.isJsonPrimitive() && defEl.getAsBoolean();
            if (isDefault) {
                defaultSnapshot = num;
            }
            if (activeSnapshot == null && isActiveInSnapper) {
                activeSnapshot = num;
            }

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("number", num);
            entry.put("active", isActiveInSnapper);
            entry.put("default", isDefault);
            entry.put("type", getString(snap, "type"));
            entry.put("preNumber", getLong(snap, "pre-number"));
            entry.put("user", getString(snap, "user"));
            entry.put("usedSpace", getLong(snap, "used-space"));
            entry.put("cleanup", getString(snap, "cleanup"));
            entry.put("description", getString(snap, "description"));
            entry.put("date", getString(snap, "date"));
            entry.put("userdata", formatUserdata(snap.get("userdata")));
            details.add(entry);
        }

        if (details.isEmpty()) {
            LOG.debug("No valid snapshots found in snapper output");
            return Optional.empty();
        }

        return Optional.of(new ParseResult(activeSnapshot, defaultSnapshot, details));
    }

    /**
     * Parse and persist Btrfs snapshot information in the minion transactional info.
     *
     * @param server            the minion server to update
     * @param rawJson           raw stdout from {@code snapper --json --no-dbus list}
     * @return parsed snapshot information, if present
     */
    public static Optional<ParseResult> updateSnapshotInfo(MinionServer server, Optional<String> rawJson) {
        Optional<ParseResult> parseResult = parse(rawJson);
        parseResult.ifPresent(result -> {
            server.setActiveSnapshot(result.getActiveSnapshot());
            server.setDefaultSnapshot(result.getDefaultSnapshot());
            server.setSnapshotDetails(result.getSnapshotDetails());
            server.setSnapshotUpdated(new Date());
            LOG.debug("Updated snapshot info for minion {}: active={}, default={}",
                    server.getMinionId(), result.getActiveSnapshot(),
                    result.getDefaultSnapshot());
        });
        return parseResult;
    }

    /**
     * Check whether snapshot information shows an unactivated transactional snapshot.
     *
     * @param result parsed snapshot information
     * @return true when the default snapshot differs from the active snapshot
     */
    public static boolean hasPendingTransactionalReboot(ParseResult result) {
        return result.getActiveSnapshot() != null &&
                result.getDefaultSnapshot() != null &&
                !result.getActiveSnapshot().equals(result.getDefaultSnapshot());
    }

    private static String getString(JsonObject object, String property) {
        JsonElement element = object.get(property);
        return element != null && element.isJsonPrimitive() ? element.getAsString() : "";
    }

    private static Long getLong(JsonObject object, String property) {
        JsonElement element = object.get(property);
        return element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber() ?
                element.getAsLong() : null;
    }

    private static String formatUserdata(JsonElement userdata) {
        if (userdata == null || userdata.isJsonNull()) {
            return "";
        }
        if (!userdata.isJsonObject()) {
            return userdata.isJsonPrimitive() ? userdata.getAsString() : userdata.toString();
        }
        return userdata.getAsJsonObject().entrySet().stream()
                .map(entry -> entry.getKey() + "=" + formatUserdataValue(entry.getValue()))
                .collect(Collectors.joining(", "));
    }

    private static String formatUserdataValue(JsonElement value) {
        if (value == null || value.isJsonNull()) {
            return "";
        }
        return value.isJsonPrimitive() ? value.getAsString() : value.toString();
    }
}
