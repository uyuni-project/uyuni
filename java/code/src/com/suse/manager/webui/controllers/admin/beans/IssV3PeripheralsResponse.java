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
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.suse.manager.webui.controllers.admin.beans;

import com.suse.manager.model.hub.IssPeripheral;
import com.suse.manager.model.hub.IssPeripheralChannels;

public class IssV3PeripheralsResponse {
    private final long id;
    private final String fqdn;
    private final long nChannelsSync;
    private final long nSyncOrgs;
    private final String rootCA;

    /**
     * Response for peripherals list
     * @param idIn
     * @param fqdnIn
     * @param nChannelsSyncIn
     * @param nSyncOrgsIn
     * @param rootCAIn
     */
    public IssV3PeripheralsResponse(
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
     * @param peripheralEntity
     * @return
     */
    public static IssV3PeripheralsResponse fromIssEntity(IssPeripheral peripheralEntity) {
        return new IssV3PeripheralsResponse(
                peripheralEntity.getId(),
                peripheralEntity.getFqdn(),
                peripheralEntity.getPeripheralChannels().size(),
                peripheralEntity.getPeripheralChannels().stream()
                        .map(IssPeripheralChannels::getPeripheralOrgId).distinct().count(),
                peripheralEntity.getRootCa());
    }
}
