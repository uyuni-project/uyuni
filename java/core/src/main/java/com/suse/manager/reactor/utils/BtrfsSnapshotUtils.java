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
import java.util.List;
import java.util.Optional;

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
        private final List<Long> snapshotNumbers;
        private final JsonArray detailsJson;

        /**
         * @param activeSnapshotIn   the active snapshot number, or null
         * @param defaultSnapshotIn  the default snapshot number, or null
         * @param snapshotNumbersIn  list of all valid snapshot numbers
         * @param detailsJsonIn      JSON array with per-snapshot detail objects
         */
        public ParseResult(Long activeSnapshotIn, Long defaultSnapshotIn,
                           List<Long> snapshotNumbersIn, JsonArray detailsJsonIn) {
            this.activeSnapshot = activeSnapshotIn;
            this.defaultSnapshot = defaultSnapshotIn;
            this.snapshotNumbers = snapshotNumbersIn;
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
         * @return list of all valid snapshot numbers
         */
        public List<Long> getSnapshotNumbers() {
            return snapshotNumbers;
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
     * Snapshots that have both {@code "transactional-update-in-progress": "yes"} in
     * {@code userdata} and {@code "active": true} are excluded: they represent in-progress
     * transaction overlays that mask the real active snapshot and will be discarded on rollback.
     * Snapshot 0 (the "current running subvolume" meta-entry) is also excluded.
     *
     * The real active snapshot is determined from {@code activeSnapshotNum} (read from
     * field 4 of {@code /proc/1/mountinfo} for the root mount) rather than from snapper's
     * own {@code "active"} flag, which is unreliable inside the chroot context Salt uses.
     *
     * @param rawJson          raw stdout from {@code snapper --json --no-dbus list}
     * @param activeSnapshotNum real active snapshot number from /proc/1/mountinfo, or empty
     * @return parsed result, or empty if the JSON is absent / contains no valid snapshots
     */
    public static Optional<ParseResult> parse(Optional<String> rawJson, Optional<Long> activeSnapshotNum) {
        String json = rawJson.orElse(null);
        if (json == null || json.isBlank()) {
            return Optional.empty();
        }

        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        JsonArray arr = obj.getAsJsonArray("root");
        if (arr == null) {
            return Optional.empty();
        }

        Long activeSnapshot = activeSnapshotNum.orElse(null);
        Long defaultSnapshot = null;
        List<Long> snapshotNumbers = new ArrayList<>();
        JsonArray details = new JsonArray();

        for (JsonElement element : arr) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject snap = element.getAsJsonObject();

            // Exclude in-progress transactional update overlay that is also flagged active —
            // a temporary chroot mount that will be discarded and should not appear in the list.
            JsonElement userdata = snap.get("userdata");
            boolean inProgress = false;
            if (userdata != null && userdata.isJsonObject()) {
                JsonElement flag = userdata.getAsJsonObject().get("transactional-update-in-progress");
                inProgress = flag != null && "yes".equals(flag.getAsString());
            }
            JsonElement activeEl = snap.get("active");
            boolean isActiveInSnapper = activeEl != null && activeEl.isJsonPrimitive() && activeEl.getAsBoolean();
            if (inProgress && isActiveInSnapper) {
                continue;
            }

            JsonElement numEl = snap.get("number");
            if (numEl == null || !numEl.isJsonPrimitive()) {
                continue;
            }
            long num = numEl.getAsLong();
            if (num == 0) {
                // Snapshot 0 is the "current running subvolume" meta-entry, not a real snapshot.
                continue;
            }
            snapshotNumbers.add(num);

            JsonElement defEl = snap.get("default");
            boolean isDefault = defEl != null && defEl.isJsonPrimitive() && defEl.getAsBoolean();
            if (isDefault) {
                defaultSnapshot = num;
            }

            JsonElement descEl = snap.get("description");
            JsonElement dateEl = snap.get("date");
            JsonObject entry = new JsonObject();
            entry.addProperty("number", num);
            entry.addProperty("active", activeSnapshot != null && activeSnapshot == num);
            entry.addProperty("default", isDefault);
            entry.addProperty("description",
                    descEl != null && descEl.isJsonPrimitive() ? descEl.getAsString() : "");
            entry.addProperty("date",
                    dateEl != null && dateEl.isJsonPrimitive() ? dateEl.getAsString() : "");
            details.add(entry);
        }

        if (snapshotNumbers.isEmpty()) {
            LOG.debug("No valid snapshots found in snapper output");
            return Optional.empty();
        }

        return Optional.of(new ParseResult(activeSnapshot, defaultSnapshot, snapshotNumbers, details));
    }

    /**
     * Parse and persist Btrfs snapshot information in the minion transactional info.
     *
     * @param server            the minion server to update
     * @param rawJson           raw stdout from {@code snapper --json --no-dbus list}
     * @param activeSnapshotNum real active snapshot number
     */
    public static void updateSnapshotInfo(MinionServer server, Optional<String> rawJson,
                                          Optional<Long> activeSnapshotNum) {
        parse(rawJson, activeSnapshotNum).ifPresent(result -> {
            server.setActiveSnapshot(result.getActiveSnapshot());
            server.setDefaultSnapshot(result.getDefaultSnapshot());
            server.setSnapshots(result.getSnapshotNumbers().toArray(Long[]::new));
            server.setSnapshotDetails(result.getDetailsJsonString());
            server.setSnapshotUpdated(new Date());
            LOG.debug("Updated snapshot info for minion {}: active={}, default={}, all={}",
                    server.getMinionId(), result.getActiveSnapshot(),
                    result.getDefaultSnapshot(), result.getSnapshotNumbers());
        });
    }
}
