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

import com.suse.manager.model.hub.OrgInfoJson;

import java.util.List;
import java.util.Set;

public class ChannelSyncModel {
    private final List<OrgInfoJson> peripheralOrgs;
    private final Set<IssV3ChannelResponse> syncedPeripheralCustomChannels;
    private final Set<IssV3ChannelResponse> syncedPeripheralVendorChannels;
    private final List<IssV3ChannelResponse> availableCustomChannels;
    private final List<IssV3ChannelResponse> availableVendorChannels;

    /**
     * Model for Peripheral Channel Sync get
     *
     * @param peripheralOrgsIn                 a List of Orgs available on the peripheral
     * @param syncedPeripheralCustomChannelsIn the list of synced custom channels
     * @param syncedPeripheralVendorChannelsIn the list of synced vendor channels
     * @param availableCustomChannelsIn        the list of available custom channels to sync
     * @param availableVendorChannelsIn        the list of available vendor channels to sync
     */
    public ChannelSyncModel(
            List<OrgInfoJson> peripheralOrgsIn,
            Set<IssV3ChannelResponse> syncedPeripheralCustomChannelsIn,
            Set<IssV3ChannelResponse> syncedPeripheralVendorChannelsIn,
            List<IssV3ChannelResponse> availableCustomChannelsIn,
            List<IssV3ChannelResponse> availableVendorChannelsIn) {
        this.peripheralOrgs = peripheralOrgsIn;
        // Group synced custom channels by orgId.
        this.syncedPeripheralCustomChannels = syncedPeripheralCustomChannelsIn;
        this.syncedPeripheralVendorChannels = syncedPeripheralVendorChannelsIn;
        this.availableCustomChannels = availableCustomChannelsIn;
        this.availableVendorChannels = availableVendorChannelsIn;
    }

    public List<OrgInfoJson> getPeripheralOrgs() {
        return peripheralOrgs;
    }

    public Set<IssV3ChannelResponse> getSyncedPeripheralCustomChannels() {
        return syncedPeripheralCustomChannels;
    }

    public Set<IssV3ChannelResponse> getSyncedPeripheralVendorChannels() {
        return syncedPeripheralVendorChannels;
    }

    public List<IssV3ChannelResponse> getAvailableCustomChannels() {
        return availableCustomChannels;
    }

    public List<IssV3ChannelResponse> getAvailableVendorChannels() {
        return availableVendorChannels;
    }
}



