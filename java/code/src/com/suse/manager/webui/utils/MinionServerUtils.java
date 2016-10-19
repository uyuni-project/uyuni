/**
 * Copyright (c) 2015 SUSE LLC
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
package com.suse.manager.webui.utils;

import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility for working with minion servers.
 */
public class MinionServerUtils {

    private MinionServerUtils() { }

    /**
     * Filter only the minion servers.
     * @param servers a list of servers
     * @return a list containing the results of the function
     */
    public static List<MinionServer> filterSaltMinions(List<Server> servers) {
        return servers.stream()
                .filter(s -> isMinionServer(s))
                .map(s -> s.asMinionServer().get())
                .collect(Collectors.toList());
    }

    /**
     * Check if a server is a minion server.
     * @param server the server
     * @return true if the server is a minion, false otherwise
     */
    public static boolean isMinionServer(Server server) {
        return server.asMinionServer().isPresent();
    }

    /**
     * Helper method for finding out whether minion has a ssh-push-like contact method
     * (ssh-push or ssh-push-tunnel).
     * @param minion the minion
     * @return true if minion is ssh-push or ssh-push-tunnel
     */
    public static boolean isSshPushMinion(MinionServer minion) {
        List<String> contactMethods = Arrays.asList("ssh-push", "ssh-push-tunnel");
        return contactMethods.contains(minion.getContactMethod().getLabel());
    }

}
