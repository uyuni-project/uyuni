/*
 * Copyright (c) 2024 SUSE LLC
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
package com.suse.manager.webui.controllers.activationkeys;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ActivationKeysAppStreamsChanges {
    private Map<Long, ChannelAppStreamsChanges> changes;

    /**
     * Obtains the list of appStreams to remove
     * @param channelId the id of the channel
     * @return the list of appStreams to remove
     */
    public List<String> getToRemove(Long channelId) {
        return changes.get(channelId).toRemove;
    }

    /**
     * Obtains the list of appStreams to include
     * @param channelId the id of the channel
     * @return the list of appStreams to include
     */
    public List<String> getToInclude(Long channelId) {
        return changes.get(channelId).toInclude;
    }

    public Set<Long> getChannelIds() {
        return changes.keySet();
    }

    static class ChannelAppStreamsChanges {
        private List<String> toInclude;
        private List<String> toRemove;
    }
}
