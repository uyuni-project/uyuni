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

package com.suse.manager.webui.controllers.admin.beans;

import com.suse.manager.model.hub.IssHub;

import java.util.Date;

/**
 * The details of a hub
 * @param id the id
 * @param fqdn the fully qualified domain name
 * @param rootCA the root certificate authority, if present
 * @param gpgKey the gpg key used to sign channel metadata, if present
 * @param sccUsername the username for syncing scc data
 * @param created when this hub configuration was registered
 * @param modified when this hub configuration was modified
 */
public record HubDetailsData(long id, String fqdn, String rootCA, String gpgKey, String sccUsername, Date created,
                             Date modified) {

    /**
     * Create an instance from the hub entity.
     * @param hub the hub
     */
    public HubDetailsData(IssHub hub) {
        this(
            hub.getId(),
            hub.getFqdn(),
            hub.getRootCa(),
            hub.getGpgKey(),
            hub.getMirrorCredentials().getUsername(),
            hub.getCreated(),
            hub.getModified()
        );
    }
}
