/**
 * Copyright (c) 2014 SUSE LLC
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
package com.redhat.rhn.frontend.events;

import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.common.messaging.MessageAction;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import org.apache.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ScheduleRepoSyncAction
 */
public class ScheduleRepoSyncAction implements MessageAction {

    /** The logger. */
    protected static Logger logger = Logger.getLogger(ScheduleRepoSyncAction.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(EventMessage msg) {
        ScheduleRepoSyncEvent event = (ScheduleRepoSyncEvent) msg;
        if (logger.isDebugEnabled()) {
            logger.debug("Scheduling repo sync for channels: " + event.getChannelLabels());
        }
        scheduleRepoSync(event.getChannelLabels(), event.getUserId());
    }

    /**
     * Schedule an immediate reposync via the Taskomatic API.
     *
     * @param channelLabels labels of the channel to sync
     * @param userId id of user requesting the sync
     */
    private void scheduleRepoSync(List<String> channelLabels, Long userId) {
        User user = UserFactory.lookupById(userId);
        if (user != null && !channelLabels.isEmpty()) {
            Org org = user.getOrg();

            List<Channel> channels = channelLabels.stream()
                .map(label -> ChannelManager.lookupByLabel(org, label))
                .collect(Collectors.toList());

            try {
                new TaskomaticApi().scheduleSingleRepoSync(channels);
            }
            catch (TaskomaticApiException e) {
                logger.error("Could not schedule repository synchronization for: " +
                        channels.toString());
                logger.error(e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean needsTransactionHandling() {
        return false;
    }
}
