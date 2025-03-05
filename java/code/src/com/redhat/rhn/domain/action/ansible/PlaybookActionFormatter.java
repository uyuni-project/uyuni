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
import com.redhat.rhn.domain.server.AnsibleFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ansible.InventoryPath;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.system.SystemManager;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
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
     * @param server the server
     * @param user the current user
     * @return string containing systems
     */
    public String getTargetedSystems(Server server, User user) {
        String inventoryPath = ((PlaybookAction) this.getAction()).getDetails().getInventoryPath();
        if (inventoryPath == null || inventoryPath.isEmpty()) {
            inventoryPath = "/etc/ansible/hosts";
        }
        Optional<InventoryPath> inventory = AnsibleFactory.lookupAnsibleInventoryPath(server.getId(), inventoryPath);
        if (inventory.isPresent()) {
            Set<Server> inventoryServers = inventory.get().getInventoryServers();
            List<String> result = new LinkedList<>();
            result.add(inventoryPath);
            for (Server s : inventoryServers.stream().sorted(Comparator.comparing(Server::getName)).toList()) {
                if (SystemManager.isAvailableToUser(user, s.getId())) {
                    result.add(
                            "<a href=\"/rhn/systems/details/Overview.do?sid=" + s.getId() + "\">" +
                                    StringEscapeUtils.escapeHtml4(s.getName()) + "</a>"
                    );
                }
            }
            return StringUtil.join("</br>", result);
        }
        else {
            return "";
        }

    }
}
