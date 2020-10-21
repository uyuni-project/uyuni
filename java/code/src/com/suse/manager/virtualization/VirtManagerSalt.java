/**
 * Copyright (c) 2018--2020 SUSE LLC
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
package com.suse.manager.virtualization;

import com.redhat.rhn.domain.server.MinionServer;

import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.VirtManager;
import com.suse.manager.webui.services.pillar.MinionPillarFileManager;
import com.suse.manager.webui.services.pillar.MinionVirtualizationPillarGenerator;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.salt.netapi.datatypes.target.MinionList;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service providing utility functions to handle virtual machines using salt.
 */
public class VirtManagerSalt implements VirtManager {

    private final SaltApi saltApi;
    private MinionPillarFileManager minionVirtualizationPillarFileManager =
            new MinionPillarFileManager(new MinionVirtualizationPillarGenerator());

    /**
     * Service providing utility functions to handle virtual machines.
     * @param saltApiIn instance interacting with salt.
     */
    public VirtManagerSalt(SaltApi saltApiIn) {
        this.saltApi = saltApiIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<GuestDefinition> getGuestDefinition(String minionId, String domainName) {
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("vm_", domainName);

        LocalCall<Map<String, VmInfoJson>> vmInfoCall =
                new LocalCall<>("virt.vm_info", Optional.empty(), Optional.of(args),
                        new TypeToken<Map<String, VmInfoJson>>() { });
        Optional<Map<String, VmInfoJson>> vmInfo = saltApi.callSync(vmInfoCall, minionId);

        LocalCall<String> call =
                new LocalCall<>("virt.get_xml", Optional.empty(), Optional.of(args), new TypeToken<String>() { });

        Optional<String> result = saltApi.callSync(call, minionId);
        return result.filter(s -> !s.startsWith("ERROR")).map(xml -> GuestDefinition.parse(xml,
                vmInfo.map(data -> data.get(domainName))));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Map<String, JsonElement>> getCapabilities(String minionId) {
        LocalCall<Map<String, JsonElement>> call =
                new LocalCall<>("virt.all_capabilities", Optional.empty(), Optional.empty(),
                        new TypeToken<Map<String, JsonElement>>() { });

        return saltApi.callSync(call, minionId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<PoolCapabilitiesJson> getPoolCapabilities(String minionId) {
        LocalCall<PoolCapabilitiesJson> call =
                new LocalCall<>("virt.pool_capabilities", Optional.empty(), Optional.empty(),
                        new TypeToken<PoolCapabilitiesJson>() { });

        return saltApi.callSync(call, minionId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<PoolDefinition> getPoolDefinition(String minionId, String poolName) {
        Map<String, JsonObject> infos = getPools(minionId);

        Map<String, Object> args = new LinkedHashMap<>();
        args.put("name", poolName);
        LocalCall<String> call =
                new LocalCall<>("virt.pool_get_xml", Optional.empty(), Optional.of(args), new TypeToken<String>() { });

        Optional<String> result = saltApi.callSync(call, minionId);
        return result.filter(s -> !s.startsWith("ERROR")).map(xml -> {
            PoolDefinition pool = PoolDefinition.parse(xml);
            if (pool != null) {
                pool.setAutostart(infos.get(poolName).get("autostart").getAsInt() == 1);
            }
            return pool;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, JsonObject> getNetworks(String minionId) {
        Map<String, Object> args = new LinkedHashMap<>();
        LocalCall<Map<String, JsonElement>> call =
                new LocalCall<>("virt.network_info", Optional.empty(), Optional.of(args),
                        new TypeToken<Map<String, JsonElement>>() { });

        Optional<Map<String, JsonElement>> nets = saltApi.callSync(call, minionId);
        Map<String, JsonElement> result = nets.orElse(new HashMap<String, JsonElement>());

        // Workaround: Filter out the entries that don't match since we may get a retcode=0 one.
        return result.entrySet().stream()
                .filter(entry -> entry.getValue().isJsonObject())
                .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue().getAsJsonObject()));
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, JsonObject> getPools(String minionId) {
        Map<String, Object> args = new LinkedHashMap<>();
        LocalCall<Map<String, JsonElement>> call =
                new LocalCall<>("virt.pool_info", Optional.empty(), Optional.of(args),
                        new TypeToken<Map<String, JsonElement>>() { });

        Optional<Map<String, JsonElement>> pools = saltApi.callSync(call, minionId);
        Map<String, JsonElement> result = pools.orElse(new HashMap<String, JsonElement>());

        // Workaround: Filter out the entries that don't match since we may get a retcode=0 one.
        return result.entrySet().stream()
                .filter(entry -> entry.getValue().isJsonObject())
                .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue().getAsJsonObject()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Map<String, JsonObject>> getVolumes(String minionId) {
        List<?> args = Arrays.asList(null, null);
        LocalCall<Map<String, Map<String, JsonObject>>> call =
                new LocalCall<>("virt.volume_infos", Optional.of(args), Optional.empty(),
                        new TypeToken<Map<String, Map<String, JsonObject>>>() { });

        Optional<Map<String, Map<String, JsonObject>>> volumes = saltApi.callSync(call, minionId);
        return volumes.orElse(new HashMap<String, Map<String, JsonObject>>());
    }

    /**
     * {@inheritDoc}
     */
    public void updateLibvirtEngine(MinionServer minion) {
        Map<String, Object> pillar = new HashMap<>();
        pillar.put("virt_entitled", minion.hasVirtualizationEntitlement());
        saltApi.callSync(State.apply(Collections.singletonList("virt.engine-events"),
                Optional.of(pillar)), minion.getMinionId());

        if (minion.hasVirtualizationEntitlement()) {
            minionVirtualizationPillarFileManager.updatePillarFile(minion);
        }
        else {
            minionVirtualizationPillarFileManager.removePillarFile(minion.getMinionId());
        }
        saltApi.refreshPillar(new MinionList(minion.getMinionId()));
    }

    /**
     * {@inheritDoc}
     */
    public Optional<String> getHypervisor(String minionId) {
        LocalCall<String> call =
                new LocalCall<>("virt.get_hypervisor", Optional.empty(), Optional.empty(),
                        new TypeToken<String>() { });

        return saltApi.callSync(call, minionId);
    }
}
