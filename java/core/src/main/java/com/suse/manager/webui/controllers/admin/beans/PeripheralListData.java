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

import com.suse.manager.model.hub.IssPeripheral;
import com.suse.manager.model.hub.IssPeripheralChannels;

import java.util.Objects;

/**
 * The information about a peripheral as shown in the list page
 * @param id the id
 * @param fqdn the fully qualified domain name
 * @param rootCA the root certificate authority, if present
 * @param nSyncedChannels the number of currently synced channels
 * @param nSyncedOrgs the number of currently synced organizations
 */
public record PeripheralListData(long id, String fqdn, String rootCA, long nSyncedChannels, long nSyncedOrgs) {

    /**
     * Build an instance from the db entity
     * @param peripheralEntity the entity representing the peripheral
     */
    public PeripheralListData(IssPeripheral peripheralEntity) {
        this(
            peripheralEntity.getId(),
            peripheralEntity.getFqdn(),
            peripheralEntity.getRootCa(),
            peripheralEntity.getPeripheralChannels().size(),
            peripheralEntity.getPeripheralChannels().stream()
                .map(IssPeripheralChannels::getPeripheralOrgId)
                .filter(Objects::nonNull)
                .distinct()
                .count()
        );
    }
}
