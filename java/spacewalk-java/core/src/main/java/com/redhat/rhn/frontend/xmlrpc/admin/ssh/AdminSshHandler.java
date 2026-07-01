/*
 * Copyright (c) 2026 SUSE LLC
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

package com.redhat.rhn.frontend.xmlrpc.admin.ssh;

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;

import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.impl.runner.MgrUtilRunner;

import java.util.Optional;

/**
 * AdminSshHandler
 * @apidoc.namespace admin.ssh
 * @apidoc.doc Provides methods to manage SSH data.
 */
public class AdminSshHandler extends BaseHandler {

    private final SaltApi saltApi;

    /**
     * @param saltApiIn SaltApi
     */
    public AdminSshHandler(SaltApi saltApiIn) {
        saltApi = saltApiIn;
    }

    /**
     * Remove a host from the list of known hosts.
     * @param loggedInUser the current user
     * @param hostname the hostname or IP of the host to remove
     * @param port the port of the host to remove
     * @return 1 on success
     *
     * @apidoc.doc Remove a host from the list of known hosts.
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "hostname")
     * @apidoc.param #param("int", "port")
     * @apidoc.returntype #return_int_success()
     */
    public int removeKnownHost(User loggedInUser, String hostname, Integer port) {
        ensureSatAdmin(loggedInUser);

        Optional<MgrUtilRunner.RemoveKnowHostResult> result =
                saltApi.removeSaltSSHKnownHost(hostname, port);
        boolean removed = result.map(r -> "removed".equals(r.getStatus())).orElse(false);

        if (removed) {
            return 1;
        }

        return 0;
    }
}
