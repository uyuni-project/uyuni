/*
 * Copyright (c) 2025 SUSE LLC
 * Copyright (c) 2009--2010 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.action;


import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionSummary;
import com.redhat.rhn.domain.server.SAPWorkload;

import com.suse.manager.reactor.hardware.CpuArchUtil;
import com.suse.manager.reactor.hardware.HardwareMapper;
import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.manager.reactor.utils.ValueMap;
import com.suse.manager.webui.utils.salt.custom.HwProfileUpdateSlsResult;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.utils.Json;

import com.google.gson.JsonElement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * HardwareRefreshAction
 */
@Entity
@DiscriminatorValue("2")
public class HardwareRefreshAction extends Action {
    private static final Logger LOG = LogManager.getLogger(HardwareRefreshAction.class);

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
                            ApplyStatesEventMessage.HARDWARE_PROFILE_UPDATE),
                    Optional.empty()), sshPushMinions);
        }
        if (!regularMinions.isEmpty()) {
            ret.put(State.apply(Arrays.asList(
                            ApplyStatesEventMessage.SYNC_ALL,
                            ApplyStatesEventMessage.HARDWARE_PROFILE_UPDATE),
                    Optional.empty()), regularMinions);
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
                .ifPresent(minionServer -> handleHardwareProfileUpdate(minionServer,
                        Json.GSON.fromJson(jsonResult, HwProfileUpdateSlsResult.class), serverAction));
    }

    /**
     * Update the hardware profile for a minion in the database from incoming
     * event data.
     *
     * @param server the minion server
     * @param result the result of the call as parsed from event data
     * @param serverAction the server action
     */
    private void handleHardwareProfileUpdate(MinionServer server, HwProfileUpdateSlsResult result,
                                             ServerAction serverAction) {
        Instant start = Instant.now();

        HardwareMapper hwMapper = new HardwareMapper(server,
                new ValueMap(result.getGrains()));
        hwMapper.mapCpuInfo(new ValueMap(result.getCpuInfo()));
        server.setRam(hwMapper.getTotalMemory());
        server.setSwap(hwMapper.getTotalSwapMemory());
        if (CpuArchUtil.isDmiCapable(hwMapper.getCpuArch())) {
            hwMapper.mapDmiInfo(
                    result.getSmbiosRecordsBios().orElse(Collections.emptyMap()),
                    result.getSmbiosRecordsSystem().orElse(Collections.emptyMap()),
                    result.getSmbiosRecordsBaseboard().orElse(Collections.emptyMap()),
                    result.getSmbiosRecordsChassis().orElse(Collections.emptyMap()));
        }
        hwMapper.mapDevices(result.getUdevdb());
        if (CpuArchUtil.isS390(hwMapper.getCpuArch())) {
            hwMapper.mapSysinfo(result.getMainframeSysinfo());
        }
        hwMapper.mapVirtualizationInfo(result.getSmbiosRecordsSystem());
        hwMapper.mapNetworkInfo(result.getNetworkInterfaces(), Optional.of(result.getNetworkIPs()),
                result.getNetworkModules(),
                Stream.concat(
                        Stream.concat(
                                result.getFqdns().stream(),
                                result.getDnsFqdns().stream()
                        ),
                        result.getCustomFqdns().stream()
                ).distinct().collect(Collectors.toList())
        );
        server.setPayg(result.getInstanceFlavor().map(o -> o.equals("PAYG")).orElse(false));
        server.setContainerRuntime(result.getContainerRuntime());
        server.setUname(result.getUname());

        var sapWorkloads = result.getSAPWorkloads()
                .map(m -> m.getChanges().getRet())
                .orElse(Collections.emptySet())
                .stream()
                .map(workload -> new SAPWorkload(
                        server, workload.get("system_id"), workload.get("instance_type")
                ))
                .collect(Collectors.toSet());

        server.getSapWorkloads().retainAll(sapWorkloads);
        server.getSapWorkloads().addAll(sapWorkloads);

        // Let the action fail in case there is error messages
        if (!hwMapper.getErrors().isEmpty()) {
            serverAction.setStatusFailed();
            serverAction.setResultMsg("Hardware list could not be refreshed completely:\n" +
                    hwMapper.getErrors().stream().collect(Collectors.joining("\n")));
            serverAction.setResultCode(-1L);
        }

        if (LOG.isDebugEnabled()) {
            long duration = Duration.between(start, Instant.now()).getSeconds();
            LOG.debug("Hardware profile updated for minion: {} ({} seconds)", server.getMinionId(), duration);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean rejectScheduleActionIfByos() {
        // Hardware refresh detect PAYG/BYOS type and refresh it. This should be possible also
        // for BYOS systems in case the former detection failed. On error PAYG is set to false,
        // and we need a way to repeat the detection.
        return false;
    }
}
