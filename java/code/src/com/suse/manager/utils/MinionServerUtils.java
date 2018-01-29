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
package com.suse.manager.utils;

import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.suse.manager.webui.controllers.utils.ContactMethodUtil;

import java.util.List;
import java.util.stream.Stream;

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
    public static Stream<MinionServer> filterSaltMinions(List<Server> servers) {
        return servers.stream()
                .flatMap(server -> server.asMinionServer()
                        .map(Stream::of)
                        .orElse(Stream.empty()));
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
     * TODO: Move this elsewhere and rename as it is not specific for minion servers only.
     *
     * Helper method for finding out whether a server has a ssh-push-like contact method
     * (ssh-push or ssh-push-tunnel).
     * @param server the server
     * @return true if minion is ssh-push or ssh-push-tunnel
     */
    public static boolean isSshPushMinion(Server server) {
        return ContactMethodUtil.isSSHPushContactMethod(server.getContactMethod());
    }

}
