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
    private final Set<ChannelSyncDetail> syncedPeripheralCustomChannels;
    private final Set<ChannelSyncDetail> syncedPeripheralVendorChannels;
    private final List<ChannelSyncDetail> availableCustomChannels;
    private final List<ChannelSyncDetail> availableVendorChannels;

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
            Set<ChannelSyncDetail> syncedPeripheralCustomChannelsIn,
            Set<ChannelSyncDetail> syncedPeripheralVendorChannelsIn,
            List<ChannelSyncDetail> availableCustomChannelsIn,
            List<ChannelSyncDetail> availableVendorChannelsIn) {
        this.peripheralOrgs = peripheralOrgsIn;
        this.syncedPeripheralCustomChannels = syncedPeripheralCustomChannelsIn;
        this.syncedPeripheralVendorChannels = syncedPeripheralVendorChannelsIn;
        this.availableCustomChannels = availableCustomChannelsIn;
        this.availableVendorChannels = availableVendorChannelsIn;
    }

    public List<OrgInfoJson> getPeripheralOrgs() {
        return peripheralOrgs;
    }

    public Set<ChannelSyncDetail> getSyncedPeripheralCustomChannels() {
        return syncedPeripheralCustomChannels;
    }

    public Set<ChannelSyncDetail> getSyncedPeripheralVendorChannels() {
        return syncedPeripheralVendorChannels;
    }

    public List<ChannelSyncDetail> getAvailableCustomChannels() {
        return availableCustomChannels;
    }

    public List<ChannelSyncDetail> getAvailableVendorChannels() {
        return availableVendorChannels;
    }
}



