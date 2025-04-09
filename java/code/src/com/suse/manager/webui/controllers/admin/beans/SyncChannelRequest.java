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

import java.util.List;

/**
 * Model for unified sync/desync operation for channels
 */
public class SyncChannelRequest {
    private List<ChannelOrgGroup> channelsToAdd;
    private List<String> channelsToRemove;

    public List<ChannelOrgGroup> getChannelsToAdd() {
        return channelsToAdd;
    }

    public void setChannelsToAdd(List<ChannelOrgGroup> channelsToAddIn) {
        channelsToAdd = channelsToAddIn;
    }

    public List<String> getChannelsToRemove() {
        return channelsToRemove;
    }

    public void setChannelsToRemove(List<String> channelsToRemoveIn) {
        channelsToRemove = channelsToRemoveIn;
    }

}
