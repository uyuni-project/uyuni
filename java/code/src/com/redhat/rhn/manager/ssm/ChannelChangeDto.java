/*
 * Copyright (c) 2018 SUSE LLC
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

package com.redhat.rhn.manager.ssm;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Object holding the information about a channel change to be applied.
 */
public class ChannelChangeDto {

    /**
     * What to do with a channel.
     */
    public enum ChannelAction {
        SUBSCRIBE,
        UNSUBSCRIBE,
        NO_CHANGE
    }

    private Optional<Long> oldBaseId;
    private Optional<Long> newBaseId;
    private boolean newBaseDefault;
    private Map<Long, ChannelAction> childChannelActions = new HashMap<>();

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
    public Map<Long, ChannelAction> getChildChannelActions() {
        return childChannelActions;
    }

    /**
     * @param childChannelsIn to set
     */
    public void setChildChannelActions(Map<Long, ChannelAction> childChannelsIn) {
        this.childChannelActions = childChannelsIn;
    }
}
