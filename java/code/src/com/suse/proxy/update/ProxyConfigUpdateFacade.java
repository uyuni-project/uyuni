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

package com.suse.proxy.update;

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;

import com.suse.manager.webui.utils.gson.ProxyConfigUpdateJson;

public interface ProxyConfigUpdateFacade {
    /**
     * Update a proxy configuration to a proxy minion.
     * @param request the proxy configuration update JSON with the new values
     * @param systemManager the system manager
     * @param systemEntitlementManager   the systemEntitlementManager
     * @param user the user
     */
    void update(
            ProxyConfigUpdateJson request,
            SystemManager systemManager,
            SystemEntitlementManager systemEntitlementManager,
            User user
    );
}
