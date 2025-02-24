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

public class PeripheralResponse {
    private final long id;
    private final String fqdn;
    private final long nChannelsSync;
    private final long nSyncOrgs;
    private final String rootCA;

    /**
     * Response for peripherals list
     * @param idIn the id
     * @param fqdnIn the fully qualified domain name
     * @param nChannelsSyncIn the number of synced channels
     * @param nSyncOrgsIn the number of synced organizations
     * @param rootCAIn the root CA certificate, if present
     */
    public PeripheralResponse(
            long idIn,
            String fqdnIn,
            long nChannelsSyncIn,
            long nSyncOrgsIn,
            String rootCAIn
    ) {
        id = idIn;
        fqdn = fqdnIn;
        nChannelsSync = nChannelsSyncIn;
        nSyncOrgs = nSyncOrgsIn;
        rootCA = rootCAIn;
    }

    public long getId() {
        return id;
    }

    public String getFqdn() {
        return fqdn;
    }

    public long getNChannelsSync() {
        return nChannelsSync;
    }

    public long getNSyncOrgs() {
        return nSyncOrgs;
    }

    public String getRootCA() {
        return rootCA;
    }

    /**
     * Helper converter method from db entity
     * @param peripheralEntity the entity representing the peripheral
     * @return the response to provide to the fronted
     */
    public static PeripheralResponse fromIssEntity(IssPeripheral peripheralEntity) {
        return new PeripheralResponse(
                peripheralEntity.getId(),
                peripheralEntity.getFqdn(),
                peripheralEntity.getPeripheralChannels().size(),
                peripheralEntity.getPeripheralChannels().stream()
                        .map(IssPeripheralChannels::getPeripheralOrgId).distinct().count(),
                peripheralEntity.getRootCa());
    }
}
