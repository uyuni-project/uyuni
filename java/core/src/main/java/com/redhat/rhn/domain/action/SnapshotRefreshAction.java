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
package com.redhat.rhn.domain.action;

import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.server.MinionSummary;
import com.redhat.rhn.domain.server.ServerFactory;

import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.manager.reactor.utils.BtrfsSnapshotUtils;
import com.suse.manager.webui.utils.salt.custom.SnapshotRefreshSlsResult;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.utils.Json;

import com.google.gson.JsonElement;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Refreshes Btrfs snapshot information for a transactional minion.
 */
@Entity
@DiscriminatorValue("528")
public class SnapshotRefreshAction extends Action {

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<LocalCall<?>, List<MinionSummary>> getSaltCalls(List<MinionSummary> minionSummaries) {
        return Map.of(
                State.apply(List.of(ApplyStatesEventMessage.SNAPSHOTS_REFRESH), Optional.empty()),
                minionSummaries
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleUpdateServerAction(ServerAction serverAction, JsonElement jsonResult, UpdateAuxArgs auxArgs) {
        serverAction.setResultMsg(serverAction.isStatusFailed() ? "Failure" : "Success");
        if (serverAction.isStatusFailed()) {
            return;
        }

        serverAction.getServer().asMinionServer().ifPresent(minionServer -> {
            SnapshotRefreshSlsResult result = Json.GSON.fromJson(jsonResult, SnapshotRefreshSlsResult.class);
            BtrfsSnapshotUtils.updateSnapshotInfo(minionServer, result.getSnapperRawStdout(),
                    result.getActiveSnapshotNumber());
            ServerFactory.save(minionServer);
        });
    }
}
