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

import static com.suse.proxy.ProxyConfigUtils.USE_CERTS_MODE_REPLACE;

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.common.RhnRuntimeException;
import com.redhat.rhn.common.validator.ValidatorResult;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionSummary;
import com.redhat.rhn.domain.server.Pillar;
import com.redhat.rhn.domain.server.SAPWorkload;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;

import com.suse.manager.reactor.hardware.CpuArchUtil;
import com.suse.manager.reactor.hardware.HardwareMapper;
import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.manager.reactor.utils.ValueMap;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.utils.gson.ProxyConfigUpdateJson;
import com.suse.manager.webui.utils.salt.custom.HwProfileUpdateSlsResult;
import com.suse.proxy.ProxyConfigUtils;
import com.suse.proxy.model.ProxyConfig;
import com.suse.proxy.update.ProxyConfigUpdateFacade;
import com.suse.proxy.update.ProxyConfigUpdateFacadeImpl;
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

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * HardwareRefreshAction
 */
@Entity
@DiscriminatorValue("2")
public class HardwareRefreshAction extends Action {
    private static final Logger LOG = LogManager.getLogger(HardwareRefreshAction.class);
    private static final SaltApi SALT_API = GlobalInstanceHolder.SALT_API;

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

        if (result.missesProxyConfig()) {
            server.getPillarByCategory(ProxyConfigUtils.PROXY_PILLAR_CATEGORY)
                    .ifPresent(pillar -> handleMissingProxyConfig(pillar, server));
        }

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

    private void handleMissingProxyConfig(Pillar pillar, MinionServer proxy) {
        ProxyConfig config = ProxyConfigUtils.proxyConfigFromPillar(pillar);
        ProxyConfigUpdateFacade updater = new ProxyConfigUpdateFacadeImpl();
        SystemManager systemManager = new SystemManager(
                ServerFactory.SINGLETON, ServerGroupFactory.SINGLETON, SALT_API);

        String httpdURL = null, httpdTag = null;
        if (config.getHttpdImage() != null) {
            httpdURL = config.getHttpdImage().getUrl();
            httpdTag = config.getHttpdImage().getTag();
        }

        String saltbrokerURL = null, saltbrokerTag = null;
        if (config.getSaltBrokerImage() != null) {
            saltbrokerURL = config.getSaltBrokerImage().getUrl();
            saltbrokerTag = config.getSaltBrokerImage().getTag();
        }

        String squidURL = null, squidTag = null;
        if (config.getSquidImage() != null) {
            squidURL = config.getSquidImage().getUrl();
            squidTag = config.getSquidImage().getTag();
        }

        String sshURL = null, sshTag = null;
        if (config.getSshImage() != null) {
            sshURL = config.getSshImage().getUrl();
            sshTag = config.getSshImage().getTag();
        }

        String tftpdURL = null, tftpdTag = null;
        if (config.getTftpdImage() != null) {
            tftpdURL = config.getTftpdImage().getUrl();
            tftpdTag = config.getTftpdImage().getTag();
        }

        ProxyConfigUpdateJson request = new ProxyConfigUpdateJson(
                proxy.getId(), config.getParentFqdn(),
                config.getProxyPort(), config.getMaxCache(), config.getEmail(),
                USE_CERTS_MODE_REPLACE,
                config.getRootCA(), config.getIntermediateCAs(), config.getProxyCert(), config.getProxyKey(),
                ProxyConfigUtils.SOURCE_MODE_RPM,
                ProxyConfigUtils.REGISTRY_MODE_SIMPLE,
                null, null,
                httpdURL, httpdTag,
                saltbrokerURL, saltbrokerTag,
                squidURL, squidTag,
                sshURL, sshTag,
                tftpdURL, tftpdTag,
                config.getProxySshPub(), config.getProxySshPriv(), config.getParentSshPub()
        );
        if (!proxy.hasProxyEntitlement()) {
            ValidatorResult result = new SystemEntitler(HardwareRefreshAction.SALT_API)
                    .addEntitlementToServer(proxy, EntitlementManager.PROXY);
            if (result.hasErrors()) {
                throw new RhnRuntimeException(result.getMessage());
            }
        }
        updater.update(request, systemManager, null);
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
