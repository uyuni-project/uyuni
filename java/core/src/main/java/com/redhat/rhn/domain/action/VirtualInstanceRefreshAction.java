/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */
package com.redhat.rhn.domain.action;

import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionSummary;
import com.redhat.rhn.domain.server.VirtualInstanceFactory;
import com.redhat.rhn.manager.system.VirtualInstanceManager;

import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.manager.webui.utils.salt.custom.GuestProperties;
import com.suse.manager.webui.utils.salt.custom.VmInfo;
import com.suse.manager.webui.utils.salt.custom.VmInfoSlsResult;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.utils.Json;

import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * VirtualInstanceRefreshAction
 */
@Entity
@DiscriminatorValue("527")
public class VirtualInstanceRefreshAction extends Action {

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<LocalCall<?>, List<MinionSummary>> getSaltCalls(List<MinionSummary> minionSummaries) {
        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();

        // salt-ssh minions in the 'true' partition
        // regular minions in the 'false' partition
        Map<Boolean, List<MinionSummary>> partitionBySSHPush = minionSummaries.stream()
                .collect(Collectors.partitioningBy(MinionSummary::isSshPush));

        // Separate SSH push minions from regular minions to apply different states
        List<MinionSummary> sshPushMinions = partitionBySSHPush.get(true);
        List<MinionSummary> regularMinions = partitionBySSHPush.get(false);

        if (!sshPushMinions.isEmpty()) {
            ret.put(State.apply(List.of(
                            ApplyStatesEventMessage.VIRTUAL_PROFILE_UPDATE),
                    Optional.empty()), minionSummaries);
        }
        if (!regularMinions.isEmpty()) {
            ret.put(State.apply(Arrays.asList(
                            ApplyStatesEventMessage.SYNC_ALL,
                            ApplyStatesEventMessage.VIRTUAL_PROFILE_UPDATE),
                    Optional.empty()), minionSummaries);
        }

        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleUpdateServerAction(ServerAction serverAction, JsonElement jsonResult, UpdateAuxArgs auxArgs) {
        if (serverAction.isStatusFailed()) {
            serverAction.setResultMsg("Failure");
        }
        else {
            serverAction.setResultMsg("Success");
        }
        serverAction.getServer().asMinionServer()
                .ifPresent(minionServer -> handleVirtualMachineUpdate(minionServer,
                        Json.GSON.fromJson(jsonResult, VmInfoSlsResult.class)));
    }

    private static void handleVirtualMachineUpdate(MinionServer server, VmInfoSlsResult result) {
        Map<String, Map<String, Object>> vmInfos = result.getVmInfos();

        if (vmInfos.isEmpty()) {
            LOG.info("No virtual machines found");
            return;
        }
        VirtualInstanceManager.updateHostVirtualInstance(server,
                VirtualInstanceFactory.getInstance().getFullyVirtType());

        List<VmInfo> plan = new ArrayList<>();
        plan.add(new VmInfo(0, VirtualInstanceManager.EVENT_TYPE_FULLREPORT, null, null));

        plan.addAll(
                vmInfos.entrySet().stream().map(entry -> {
                    Map<String, Object> vm = entry.getValue();
                    String state = vm.get("state").toString();
                    GuestProperties props = new GuestProperties(
                            ((Double)vm.get("maxMem")).longValue() / 1024,
                            entry.getKey(),
                            "shutdown".equals(state) ? "stopped" : state,
                            vm.get("uuid").toString(),
                            ((Double)vm.get("cpu")).intValue(),
                            null
                    );
                    return new VmInfo(0, VirtualInstanceManager.EVENT_TYPE_EXISTS, null, props);
                }).toList()
        );
        VirtualInstanceManager.updateGuestsVirtualInstances(server, plan);
    }
}
