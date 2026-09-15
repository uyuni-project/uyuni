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

package com.suse.proxy.update;

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.system.SystemManager;

import com.suse.manager.webui.utils.gson.ProxyConfigUpdateJson;

public interface ProxyConfigUpdateFacade {
    /**
     * Update a proxy configuration to a proxy minion.
     * @param request the proxy configuration update JSON with the new values
     * @param systemManager the system manager
     * @param user the user
     */
    void update(ProxyConfigUpdateJson request, SystemManager systemManager, User user);
}
