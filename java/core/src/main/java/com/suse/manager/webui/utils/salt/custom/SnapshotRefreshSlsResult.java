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
package com.suse.manager.webui.utils.salt.custom;

import com.suse.salt.netapi.results.CmdResult;
import com.suse.salt.netapi.results.StateApplyResult;

import com.google.gson.annotations.SerializedName;

import java.util.Optional;

/**
 * Object representation of the results of a call to state.apply snapshots.refresh.
 */
public class SnapshotRefreshSlsResult {

    public static final String SNAPPER_LIST_SNAPSHOTS =
            "cmd_|-snapper-list-snapshots_|-snapper --json --no-dbus list_|-run";
    public static final String GET_ACTIVE_SNAPSHOT =
            "cmd_|-get-active-snapshot_|-" +
            "awk '$5==\"/\" {print $4}' /proc/1/mountinfo | grep -oP '\\.snapshots/\\K\\d+'_|-run";

    @SerializedName(SNAPPER_LIST_SNAPSHOTS)
    private Optional<StateApplyResult<CmdResult>> snapperSnapshots = Optional.empty();

    @SerializedName(GET_ACTIVE_SNAPSHOT)
    private Optional<StateApplyResult<CmdResult>> activeSnapshotResult = Optional.empty();

    /**
     * Get the active snapshot number from /proc/1/mountinfo.
     * @return optional active snapshot number
     */
    public Optional<Long> getActiveSnapshotNumber() {
        return activeSnapshotResult
                .map(r -> r.getChanges().getStdout())
                .filter(out -> out != null && !out.isBlank())
                .map(out -> {
                    try {
                        return Long.parseLong(out.trim());
                    }
                    catch (NumberFormatException e) {
                        return null;
                    }
                });
    }

    /**
     * Get the raw stdout from {@code snapper --json --no-dbus list}.
     * @return raw snapper JSON output, or empty
     */
    public Optional<String> getSnapperRawStdout() {
        return snapperSnapshots
                .map(r -> r.getChanges().getStdout())
                .filter(out -> out != null && !out.isBlank());
    }
}
