/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.webui.controllers;

import com.redhat.rhn.common.RhnRuntimeException;
import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerPath;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Shared helpers for building the list of proxy parent candidates ("id", "name", "path",
 * "primaryFqdn", "additionalFqdns") sent to the frontend by {@link ProxyController} and
 * {@link MinionController}, so both endpoints return the list in the same shape.
 */
final class ProxyListUtils {

    private ProxyListUtils() {
    }

    /**
     * Build the entry representing a direct connection to the Uyuni server itself, conventionally
     * exposed with id 0 and the server's own FQDN.
     *
     * @return the direct connection entry
     * @throws RhnRuntimeException if the server FQDN is not configured
     */
    static Map<String, Object> directConnectionEntry() {
        String localManagerFqdn = Config.get().getString(ConfigDefaults.SERVER_HOSTNAME);
        if (localManagerFqdn == null || localManagerFqdn.isEmpty()) {
            throw new RhnRuntimeException("Could not determine the Server FQDN.");
        }

        Map<String, Object> entry = new HashMap<>();
        entry.put("id", 0L);
        entry.put("name", localManagerFqdn);
        entry.put("path", new ArrayList<>());
        entry.put("primaryFqdn", localManagerFqdn);
        entry.put("additionalFqdns", new ArrayList<>());
        return entry;
    }

    /**
     * Build the entries for the given proxies, sorted by primary FQDN.
     *
     * @param proxies the proxies to build entries for
     * @return the sorted list of proxy entries
     */
    static List<Map<String, Object>> proxyEntries(List<Server> proxies) {
        return proxies.stream()
                .sorted(Comparator.comparing(Server::getPrimaryFqdnName, Comparator.nullsFirst(String::compareTo)))
                .map(ProxyListUtils::proxyEntry)
                .collect(Collectors.toList());
    }

    private static Map<String, Object> proxyEntry(Server proxy) {
        Map<String, Object> entry = new HashMap<>();
        entry.put("id", proxy.getId());
        entry.put("name", proxy.getName());

        List<String> path = proxy.getServerPaths().stream()
                .sorted(Comparator.comparingLong(ServerPath::getPosition))
                .map(ServerPath::getHostname)
                .collect(Collectors.toList());
        entry.put("path", path);

        entry.put("primaryFqdn", proxy.getPrimaryFqdnName());
        entry.put("additionalFqdns", proxy.getAdditionalFqdnNames());
        return entry;
    }
}
