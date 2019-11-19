/**
 * Copyright (c) 2018 SUSE LLC
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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.salt.netapi.calls.LocalCall;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service providing utility functions to handle virtual machines.
 */
public class VirtManager {

    private static SaltService saltService = SaltService.INSTANCE;

    /**
     * Query virtual machine definition
     *
     * @param minionId the host minion ID
     * @param domainName the domain name to look for
     * @return the XML definition or an empty Optional
     */
    public static Optional<GuestDefinition> getGuestDefinition(String minionId, String domainName) {
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("vm_", domainName);
        LocalCall<String> call =
                new LocalCall<>("virt.get_xml", Optional.empty(), Optional.of(args), new TypeToken<String>() { });

        Optional<String> result = saltService.callSync(call, minionId);
        return result.filter(s -> !s.startsWith("ERROR")).map(xml -> GuestDefinition.parse(xml));
    }

    /**
     * Query virtual host and domains capabilities.
     *
     * @param minionId the salt minion virtual host to ask about
     * @return the output of the salt virt.all_capabilities call in JSON
     */
    public static Optional<Map<String, JsonElement>> getCapabilities(String minionId) {
        LocalCall<Map<String, JsonElement>> call =
                new LocalCall<>("virt.all_capabilities", Optional.empty(), Optional.empty(),
                        new TypeToken<Map<String, JsonElement>>() { });

        return saltService.callSync(call, minionId);
    }

    /**
     * Query virtual storage pool capabilities
     *
     * @param minionId the salt minion virtual host to ask about
     * @return the output of the salt virt.pool_capabilities call
     */
    public static Optional<PoolCapabilitiesJson> getPoolCapabilities(String minionId) {
        LocalCall<PoolCapabilitiesJson> call =
                new LocalCall<>("virt.pool_capabilities", Optional.empty(), Optional.empty(),
                        new TypeToken<PoolCapabilitiesJson>() { });

        return saltService.callSync(call, minionId);
    }

    /**
     * Query the list of virtual networks defined on a salt minion.
     *
     * @param minionId the minion to ask about
     * @return a list of the network names
     */
    public static Map<String, JsonObject> getNetworks(String minionId) {
        Map<String, Object> args = new LinkedHashMap<>();
        LocalCall<Map<String, JsonElement>> call =
                new LocalCall<>("virt.network_info", Optional.empty(), Optional.of(args),
                        new TypeToken<Map<String, JsonElement>>() { });

        Optional<Map<String, JsonElement>> nets = saltService.callSync(call, minionId);
        Map<String, JsonElement> result = nets.orElse(new HashMap<String, JsonElement>());

        // Workaround: Filter out the entries that don't match since we may get a retcode=0 one.
        return result.entrySet().stream()
                    .filter(entry -> entry.getValue().isJsonObject())
                    .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue().getAsJsonObject()));
    }

    /**
     * Query the list of virtual storage pools defined on a salt minion.
     *
     * @param minionId the minion to ask about
     * @return a map associating pool names with their informations as Json elements
     */
    public static Map<String, JsonObject> getPools(String minionId) {
        Map<String, Object> args = new LinkedHashMap<>();
        LocalCall<Map<String, JsonElement>> call =
                new LocalCall<>("virt.pool_info", Optional.empty(), Optional.of(args),
                        new TypeToken<Map<String, JsonElement>>() { });

        Optional<Map<String, JsonElement>> pools = saltService.callSync(call, minionId);
        Map<String, JsonElement> result = pools.orElse(new HashMap<String, JsonElement>());

        // Workaround: Filter out the entries that don't match since we may get a retcode=0 one.
        return result.entrySet().stream()
                    .filter(entry -> entry.getValue().isJsonObject())
                    .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue().getAsJsonObject()));
    }

    /**
     * Query the list of virtual storage volumes defined on a salt minion.
     *
     * @param minionId the minion to ask about
     * @return a map associating pool names with the list of volumes it contains mapped by their names
     */
    public static Map<String, Map<String, JsonObject>> getVolumes(String minionId) {
        List<?> args = Arrays.asList(null, null);
        LocalCall<Map<String, Map<String, JsonObject>>> call =
                new LocalCall<>("virt.volume_infos", Optional.of(args), Optional.empty(),
                        new TypeToken<Map<String, Map<String, JsonObject>>>() { });

        Optional<Map<String, Map<String, JsonObject>>> volumes = saltService.callSync(call, minionId);
        return volumes.orElse(new HashMap<String, Map<String, JsonObject>>());
    }

    /**
     * @param saltServiceIn to set for tests
     */
    public static void setSaltService(SaltService saltServiceIn) {
        saltService = saltServiceIn;
    }

    private VirtManager() {
    }
}
