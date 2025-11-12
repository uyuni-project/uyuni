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
 */

package com.suse.manager.webui.controllers.admin.beans;

import com.suse.manager.model.hub.IssHub;
import com.suse.manager.model.hub.IssRole;

import java.util.Optional;

/**
 * The details of a hub
 */
public class HubDetailsData extends IssServerDetailsData {

    private final String gpgKey;

    /**
     * Create an instance from the hub entity.
     * @param hub the hub
     */
    public HubDetailsData(IssHub hub) {
        super(
            hub.getId(),
            IssRole.HUB,
            hub.getFqdn(),
            hub.getRootCa(),
            Optional.ofNullable(hub.getMirrorCredentials()).map(creds -> creds.getUsername()).orElse(null),
            hub.getCreated(),
            hub.getModified()
        );

        this.gpgKey = hub.getGpgKey();
    }

    public String getGpgKey() {
        return gpgKey;
    }
}
