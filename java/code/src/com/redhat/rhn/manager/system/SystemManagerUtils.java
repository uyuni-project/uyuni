/*
 * Copyright (c) 2024 SUSE LLC
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

package com.redhat.rhn.manager.system;

import com.redhat.rhn.domain.server.Server;

import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.impl.SaltSSHService;
import com.suse.manager.webui.services.impl.runner.MgrUtilRunner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

/**
 * Utility class for SystemManager
 */
public class SystemManagerUtils {
    private static Logger log = LogManager.getLogger(SystemManagerUtils.class);

    /**
     * Default constructor
     */
    private SystemManagerUtils() {
        throw new IllegalStateException("Cannot initialize utility class SystemManagerUtils");
    }

    /**
     * Create unique id based on given data
     *
     * @param fields list of fields to create unique id
     * @return unique id
     */
    public static String createUniqueId(List<String> fields) {
        String delimiter = "_";
        return delimiter + fields
                .stream()
                .reduce((i1, i2) -> i1 + delimiter + i2)
                .orElseThrow();
    }

    /**
     * Remove Salt SSH known hosts
     * @param saltApi Salt API
     * @param server the server
     */
    public static void removeSaltSSHKnownHosts(SaltApi saltApi, Server server) {
        Integer sshPort = server.getProxyInfo() != null ? server.getProxyInfo().getSshPort() : null;
        int port = sshPort != null ? sshPort : SaltSSHService.SSH_DEFAULT_PORT;
        Optional.ofNullable(server.getHostname()).ifPresentOrElse(
                hostname -> {
                    Optional<MgrUtilRunner.RemoveKnowHostResult> result =
                            saltApi.removeSaltSSHKnownHost(hostname, port);
                    boolean removed = result.map(r -> "removed".equals(r.getStatus())).orElse(false);
                    if (!removed) {
                        log.warn("Hostname {}:{} could not be removed from /var/lib/salt/.ssh/known_hosts: {}",
                                hostname, port, result.map(r -> r.getComment()).orElse(""));
                    }
                },
                () -> log.warn("Unable to remove SSH key for {} from /var/lib/salt/.ssh/known_hosts: unknown hostname",
                        server.getName()));
    }


}
