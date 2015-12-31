/**
 * Copyright (c) 2015 SUSE LLC
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
package com.suse.manager.reactor;

import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.common.messaging.MessageAction;

import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.domain.server.MinionFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.suse.manager.webui.services.SaltService;
import com.suse.manager.webui.services.impl.SaltAPIService;
import com.suse.manager.webui.utils.salt.JobReturnEvent;

import com.suse.saltstack.netapi.calls.modules.Grains;
import com.suse.saltstack.netapi.datatypes.target.Glob;
import org.apache.log4j.Logger;

/**
 * Handler class for {@link JobReturnEventMessage}.
 */
public class JobReturnEventMessageAction implements MessageAction {

    /* Logger for this class */
    private static final Logger LOG = Logger.getLogger(JobReturnEventMessageAction.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(EventMessage msg) {
        JobReturnEventMessage jobReturnEventMessage = (JobReturnEventMessage) msg;
        JobReturnEvent jobReturnEvent = jobReturnEventMessage.getJobReturnEvent();

        // React according to the function the minion ran
        String function = (String) jobReturnEvent.getData().get("fun");

        if (LOG.isDebugEnabled()) {
            LOG.debug("Job return event for minion: " +
                    jobReturnEvent.getMinionId() + "/" + jobReturnEvent.getJobId() +
                    " (" + function + ")");
        }

        if (packagesChanged(jobReturnEvent)) {
            MinionFactory.findByMinionId(jobReturnEvent.getMinionId()).ifPresent(minionServer -> {
                MessageQueue.publish(new UpdatePackageProfileEventMessage(minionServer.getId()));
            });
        }
    }

    private boolean packagesChanged(JobReturnEvent event) {
        String function = (String) event.getData().get("fun");
        //TODO: add more events that change packages
        //TODO: this can be further optimized by inspecting the event content
        switch (function) {
            case "pkg.install": return true;
            case "pkg.remove": return true;
            case "state.apply": return true;
            default: return false;
        }
    }
}
