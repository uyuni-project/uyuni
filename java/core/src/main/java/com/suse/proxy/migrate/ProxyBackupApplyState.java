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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.suse.proxy.migrate;

import com.redhat.rhn.common.RhnRuntimeException;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.InvalidProxyVersionException;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Set;

public class ProxyBackupApplyState {
    private static final Logger LOG = LogManager.getLogger(ProxyBackupApplyState.class);
    // 15.4 means SLES 15.4 as we do not have SUMA proxy version in the database we rely on this.
    private static final Set<String> ALLOWED_PROXY_VERSION = Set.of("15.4");

    private ProxyBackupApplyState() { }

    /**
     * Schedule the `proxy.backup` state application
     *
     * @param loggedInUser User initiating the backup. Null for system user.
     * @param proxy the proxy minion to back up
     */
    public static void backupProxyConfig(User loggedInUser, MinionServer proxy)
            throws InvalidProxyVersionException, RhnRuntimeException {
        LOG.debug("Initiating proxy backup action");
        // Ensure server is 4.3 proxy server
        if (!(proxy.isProxy() && ALLOWED_PROXY_VERSION.contains(proxy.getRelease()))) {
            throw new InvalidProxyVersionException("Unsupported proxy version");
        }

        try {
            ActionManager.scheduleProxyBackup(loggedInUser, Collections.singletonList(proxy.getId()));
        }
        catch (TaskomaticApiException e) {
            LOG.error("Unable to schedule proxy backup action", e);
            throw new RhnRuntimeException(e);
        }
    }
}
