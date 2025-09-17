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
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.action.ansible;

import com.redhat.rhn.common.util.StringUtil;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFormatter;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.server.AnsibleFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ansible.InventoryPath;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.system.SystemManager;

import com.suse.manager.webui.utils.YamlHelper;

import org.yaml.snakeyaml.error.YAMLException;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Formatter for PlaybookActions.
 */
public class PlaybookActionFormatter extends ActionFormatter {

    /**
     * Standard constructor.
     * @param actionIn the action
     */
    public PlaybookActionFormatter(Action actionIn) {
        super(actionIn);
    }

    /**
     * Get a formatted list of inventory systems accessible to the user
     *
     * @param serverAction the server action
     * @param user the current user
     * @return string containing systems
     */
    public String getTargetedSystems(ServerAction serverAction, User user) {
        String inventoryPath = ((PlaybookAction) this.getAction()).getDetails().getInventoryPath();
        Set<Server> inventoryServers = new HashSet<>();
        if (inventoryPath == null || inventoryPath.isEmpty()) {
            inventoryPath = "-";
        }

        // Try to parse servers from playbook return
        try {
            Set<String> hostnames = getHostnamesFromPlaybookReturn(serverAction);
            Set<Server> servers = new HashSet<>();
            hostnames.forEach(name -> ServerFactory.findByFqdn(name).ifPresent(servers::add));
            inventoryServers.addAll(servers);
        }
        // If parsing the playbook return was not successful we try to look up the inventory servers from the db
        catch (YAMLException e) {
            if (inventoryPath.equals("-")) {
                return "";
            }
            Optional<InventoryPath> inventory = AnsibleFactory.lookupAnsibleInventoryPath(
                    serverAction.getServer().getId(), inventoryPath);
            if (inventory.isPresent()) {
                inventoryServers = inventory.get().getInventoryServers();
            }
            else {
                return "";
            }
        }
        List<String> result = new LinkedList<>();
        if (!inventoryPath.equals("-")) {
            result.add("<b>" + inventoryPath + ":</b>");
        }
        for (Server s : inventoryServers.stream().sorted(Comparator.comparing(Server::getName)).toList()) {
            if (SystemManager.isAvailableToUser(user, s.getId())) {
                result.add(
                        "<a href=\"/rhn/systems/details/Overview.do?sid=" + s.getId() + "\">" + s.getName() + "</a>"
                );
            }
        }
        return StringUtil.join("</br>", result);
    }

    private Set<String> getHostnamesFromPlaybookReturn(ServerAction sa) throws YAMLException {
        Set<String> hosts = new HashSet<>();
        String resultMsg = sa.getResultMsg();
        if (resultMsg == null || resultMsg.isEmpty()) {
            throw new YAMLException("resultMsg is empty or null");
        }
        Map<String, Map<String, Object>> yaml = YamlHelper.loadAs(resultMsg, HashMap.class);
        Optional<Map.Entry<String, Map<String, Object>>> entry = yaml.entrySet().stream().findFirst();
        if (entry.isPresent() && entry.get().getKey().contains("ansible.playbooks")) {
            Map<String, Object> result = entry.get().getValue();
            List<Map<String, List<Map<String, Map<String, Object>>>>> plays = getNestedValue(
                    result, "changes", "ret", "plays");
            if (plays != null) {
                List<Map<String, Map<String, Object>>> tasks = plays.get(0).get("tasks");
                tasks.forEach(t -> hosts.addAll(t.get("hosts").keySet()));
            }
        }
        return hosts;
    }

    private <T> T getNestedValue(Map<String, Object> map, String... keys) {
        Object value = map;
        for (String key : keys) {
            value = ((Map<String, Object>) value).get(key);
        }
        return (T) value;
    }
}
