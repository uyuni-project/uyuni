/*
 * Copyright (c) 2018--2025 SUSE LLC
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

package com.redhat.rhn.manager.ssm;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Object holding the information about a channel change to be applied.
 */
public class ChannelChangeDto {

    private Optional<Long> oldBaseId;
    private Optional<Long> newBaseId;
    private boolean newBaseDefault;
    private Map<Long, ChannelChangeAction> childChannelActions = new HashMap<>();

    /**
     * @return oldBaseId to get
     */
    public Optional<Long> getOldBaseId() {
        return oldBaseId;
    }

    /**
     * @param oldBaseIdIn to set
     */
    public void setOldBaseId(Optional<Long> oldBaseIdIn) {
        this.oldBaseId = oldBaseIdIn;
    }

    /**
     * @return newBaseId to get
     */
    public Optional<Long> getNewBaseId() {
        return newBaseId;
    }

    /**
     * @param newBaseIdIn to set
     */
    public void setNewBaseId(Optional<Long> newBaseIdIn) {
        this.newBaseId = newBaseIdIn;
    }

    /**
     * @return newBaseDefault to get
     */
    public boolean isNewBaseDefault() {
        return newBaseDefault;
    }

    /**
     * @param newBaseDefaultIn to set
     */
    public void setNewBaseDefault(boolean newBaseDefaultIn) {
        this.newBaseDefault = newBaseDefaultIn;
    }

    /**
     * @return childChannels to get
     */
    public Map<Long, ChannelChangeAction> getChildChannelActions() {
        return childChannelActions;
    }

    /**
     * @param childChannelsIn to set
     */
    public void setChildChannelActions(Map<Long, ChannelChangeAction> childChannelsIn) {
        this.childChannelActions = childChannelsIn;
    }
}
