/*
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
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.manager.system.VirtualInstanceManager;

import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.VirtManager;
import com.suse.manager.webui.utils.salt.custom.GuestProperties;
import com.suse.manager.webui.utils.salt.custom.VmInfo;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.Grains;
import com.suse.salt.netapi.calls.modules.State;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service providing utility functions to handle virtual machines using salt.
 */
public class VirtManagerSalt implements VirtManager {

    private final SaltApi saltApi;

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
    public Optional<GuestDefinition> getGuestDefinition(String minionId, String uuid) {
        String fixedUuid = uuid;
        String uuidPattern = "^([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{12}$)";
        Matcher matcher = Pattern.compile(uuidPattern).matcher(uuid);
        if (matcher.matches()) {
             fixedUuid = String.format("%s-%s-%s-%s-%s", matcher.group(1), matcher.group(2), matcher.group(3),
                    matcher.group(4), matcher.group(5));
        }

        Map<String, Object> args = new LinkedHashMap<>();
        args.put("uuid", fixedUuid);

        LocalCall<Map<String, JsonElement>> vmDefCall = new LocalCall<>("virt_utils.vm_definition",
                Optional.empty(), Optional.of(args),
                new TypeToken<>() {
                });
        Optional<Map<String, JsonElement>> data = saltApi.callSync(vmDefCall, minionId);

        return data.map(vmData -> {
            Optional<VmInfoJson> info = Optional.empty();
            if (vmData.containsKey("info")) {
                info = Optional.ofNullable(new GsonBuilder().create()
                        .fromJson(vmData.get("info"), new TypeToken<VmInfoJson>() { }.getType()));
            }
            if (vmData.containsKey("definition")) {
                String xml = vmData.get("definition").getAsString();
                return GuestDefinition.parse(xml, info);
            }
            return null;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Map<String, JsonElement>> getCapabilities(String minionId) {
        LocalCall<Map<String, JsonElement>> call =
                new LocalCall<>("virt.all_capabilities", Optional.empty(), Optional.empty(),
                        new TypeToken<>() {
                        });

        return saltApi.callSync(call, minionId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<PoolCapabilitiesJson> getPoolCapabilities(String minionId) {
        LocalCall<PoolCapabilitiesJson> call =
                new LocalCall<>("virt.pool_capabilities", Optional.empty(), Optional.empty(),
                        new TypeToken<>() {
                        });

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
                new LocalCall<>("virt.pool_get_xml", Optional.empty(), Optional.of(args), new TypeToken<>() {
                });

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
                        new TypeToken<>() {
                        });

        Optional<Map<String, JsonElement>> nets = saltApi.callSync(call, minionId);
        Map<String, JsonElement> result = nets.orElse(new HashMap<>());

        // Workaround: Filter out the entries that don't match since we may get a retcode=0 one.
        return result.entrySet().stream()
                .filter(entry -> entry.getValue().isJsonObject())
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getAsJsonObject()));
    }

    @Override
    public Optional<NetworkDefinition> getNetworkDefinition(String minionId, String netName) {
        Map<String, JsonObject> infos = getNetworks(minionId);
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("name", netName);
        LocalCall<String> call =
                new LocalCall<>("virt.network_get_xml", Optional.empty(), Optional.of(args),
                        new TypeToken<>() {
                        });

        Optional<String> result = saltApi.callSync(call, minionId);
        return result.filter(s -> !s.startsWith("ERROR")).map(xml -> {
            NetworkDefinition network = NetworkDefinition.parse(xml);
            if (network != null) {
                network.setAutostart(infos.get(netName).get("autostart").getAsInt() == 1);
            }
            return network;
        });
    }

    /**
     * {@inheritDoc}
     */
    public List<JsonObject> getHostDevices(String minionId) {
        LocalCall<List<JsonObject>> call =
                new LocalCall<>("virt.node_devices", Optional.empty(), Optional.empty(),
                        new TypeToken<>() {
                        });
        return saltApi.callSync(call, minionId).orElse(new ArrayList<>());
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, JsonObject> getPools(String minionId) {
        Map<String, Object> args = new LinkedHashMap<>();
        LocalCall<Map<String, JsonElement>> call =
                new LocalCall<>("virt.pool_info", Optional.empty(), Optional.of(args),
                        new TypeToken<>() {
                        });

        Optional<Map<String, JsonElement>> pools = saltApi.callSync(call, minionId);
        Map<String, JsonElement> result = pools.orElse(new HashMap<>());

        // Workaround: Filter out the entries that don't match since we may get a retcode=0 one.
        return result.entrySet().stream()
                .filter(entry -> entry.getValue().isJsonObject())
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getAsJsonObject()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Map<String, JsonObject>> getVolumes(String minionId) {
        List<?> args = Arrays.asList(null, null);
        LocalCall<Map<String, Map<String, JsonObject>>> call =
                new LocalCall<>("virt.volume_infos", Optional.of(args), Optional.empty(),
                        new TypeToken<>() {
                        });

        Optional<Map<String, Map<String, JsonObject>>> volumes = saltApi.callSync(call, minionId);
        return volumes.orElse(new HashMap<>());
    }

    /**
     * {@inheritDoc}
     */
    public void updateLibvirtEngine(MinionServer minion) {
        Map<String, Object> pillar = new HashMap<>();
        pillar.put("virt_entitled", minion.hasVirtualizationEntitlement());
        saltApi.callSync(State.apply(Collections.singletonList("virt.engine-events"),
                Optional.of(pillar)), minion.getMinionId());
    }

    /**
     * {@inheritDoc}
     */
    public Optional<HostInfo> getHostInfo(String minionId) {
        LocalCall<HostInfo> call =
                new LocalCall<>("virt_utils.host_info", Optional.empty(), Optional.empty(),
                        new TypeToken<>() {
                        });

        return saltApi.callSync(call, minionId);
    }

    @Override
    public boolean startGuest(String minionId, String domainName) {
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("name", domainName);
        LocalCall<Boolean> call =
                new LocalCall<>("virt.start", Optional.empty(), Optional.of(args),
                        new TypeToken<>() {
                        });

        return saltApi.callSync(call, minionId).orElse(false);
    }

    /**
     * Get the plan to update the virtual machines list of a minion.
     *
     * @param minionId the virtualization host minionId
     *
     * @return the plan to pass to {@link VirtualInstanceManager#updateGuestsVirtualInstances(Server, List)}
     */
    public Optional<List<VmInfo>> getGuestsUpdatePlan(String minionId) {
        // Get the list of VMs with at least (name, cpu, memory, status) virt.vm_info
        LocalCall<Map<String, Map<String, Object>>> call =
                new LocalCall<>("virt.vm_info", Optional.empty(), Optional.empty(),
                        new TypeToken<>() {
                        });

        Optional<Map<String, Map<String, Object>>> vmInfos = saltApi.callSync(call, minionId);
        return vmInfos.map(
            infos -> {
                List<VmInfo> plan = new ArrayList<>();
                plan.add(new VmInfo(0, VirtualInstanceManager.EVENT_TYPE_FULLREPORT, null, null));

                plan.addAll(
                    infos.entrySet().stream().map(entry -> {
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
                    }).collect(Collectors.toList())
                );
                return plan;
            }
        );
    }

    @Override
    public Optional<Map<String, Boolean>> getFeatures(String minionId) {
        String grainName = "virt_features";
        Optional<Map<String, Map<String, Boolean>>> data = saltApi.callSync(Grains.item(false,
                new TypeToken<Map<String, Map<String, Boolean>>>() { }, grainName), minionId);
        return data.map(features -> features.get(grainName));
    }

    @Override
    public Optional<Map<String, Map<String, JsonElement>>> getVmInfos(String minionId) {
        LocalCall<Map<String, Map<String, JsonElement>>> call =
                new LocalCall<>("virt_utils.vm_info", Optional.empty(), Optional.empty(),
                        new TypeToken<>() {
                        });

        return saltApi.callSync(call, minionId);
    }

    @Override
    public Optional<List<String>> getTuningTemplates(String minionId) {
        LocalCall<List<String>> call =
                new LocalCall<>("virt_utils.virt_tuner_templates", Optional.empty(), Optional.empty(),
                        new TypeToken<>() {
                        });

        return saltApi.callSync(call, minionId);
    }
}
