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
        private final JsonArray detailsJson;

        /**
         * @param activeSnapshotIn   the active snapshot number, or null
         * @param defaultSnapshotIn  the default snapshot number, or null
         * @param detailsJsonIn      JSON array with per-snapshot detail objects
         */
        public ParseResult(Long activeSnapshotIn, Long defaultSnapshotIn, JsonArray detailsJsonIn) {
            this.activeSnapshot = activeSnapshotIn;
            this.defaultSnapshot = defaultSnapshotIn;
            this.detailsJson = detailsJsonIn;
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
         * @return JSON array string of per-snapshot detail objects suitable for storage
         */
        public String getDetailsJsonString() {
            return detailsJson.toString();
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
        JsonArray details = new JsonArray();

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

            JsonObject entry = new JsonObject();
            entry.addProperty("number", num);
            entry.addProperty("active", isActiveInSnapper);
            entry.addProperty("default", isDefault);
            addStringProperty(entry, snap, "type");
            addLongProperty(entry, snap, "pre-number", "preNumber");
            addStringProperty(entry, snap, "user");
            addLongProperty(entry, snap, "used-space", "usedSpace");
            addStringProperty(entry, snap, "cleanup");
            addStringProperty(entry, snap, "description");
            addStringProperty(entry, snap, "date");
            entry.addProperty("userdata", formatUserdata(snap.get("userdata")));
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
            server.setSnapshotDetails(result.getDetailsJsonString());
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

    /**
     * Parse persisted snapshot details into the public API representation.
     *
     * @param detailsJson JSON array previously returned by {@link ParseResult#getDetailsJsonString()}
     * @return snapshot details, or an empty list when absent or invalid
     */
    public static List<Map<String, Object>> parseSnapshotDetails(Optional<String> detailsJson) {
        String json = detailsJson.orElse(null);
        if (json == null || json.isBlank()) {
            return List.of();
        }

        try {
            JsonElement parsed = JsonParser.parseString(json);
            if (!parsed.isJsonArray()) {
                LOG.warn("Ignoring persisted Btrfs snapshot details because it is not a JSON array");
                return List.of();
            }

            List<Map<String, Object>> snapshots = new ArrayList<>();
            for (JsonElement element : parsed.getAsJsonArray()) {
                if (element.isJsonObject()) {
                    snapshots.add(snapshotDetailToMap(element.getAsJsonObject()));
                }
            }
            return snapshots;
        }
        catch (RuntimeException e) {
            LOG.warn("Unable to parse persisted Btrfs snapshot details", e);
            return List.of();
        }
    }

    private static void addStringProperty(JsonObject target, JsonObject source, String property) {
        JsonElement element = source.get(property);
        target.addProperty(property, element != null && element.isJsonPrimitive() ? element.getAsString() : "");
    }

    private static void addLongProperty(JsonObject target, JsonObject source, String sourceProperty,
                                        String targetProperty) {
        JsonElement element = source.get(sourceProperty);
        if (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
            target.addProperty(targetProperty, element.getAsLong());
        }
        else {
            target.add(targetProperty, null);
        }
    }

    private static Map<String, Object> snapshotDetailToMap(JsonObject snapshot) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("number", getLong(snapshot, "number"));
        entry.put("active", getBoolean(snapshot, "active"));
        entry.put("default", getBoolean(snapshot, "default"));
        entry.put("type", getString(snapshot, "type"));
        entry.put("preNumber", getLong(snapshot, "preNumber"));
        entry.put("date", getString(snapshot, "date"));
        entry.put("user", getString(snapshot, "user"));
        entry.put("usedSpace", getLong(snapshot, "usedSpace"));
        entry.put("cleanup", getString(snapshot, "cleanup"));
        entry.put("description", getString(snapshot, "description"));
        entry.put("userdata", getString(snapshot, "userdata"));
        return entry;
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

    private static boolean getBoolean(JsonObject object, String property) {
        JsonElement element = object.get(property);
        return element != null && element.isJsonPrimitive() && element.getAsBoolean();
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
