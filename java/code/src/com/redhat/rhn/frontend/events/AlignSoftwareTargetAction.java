/**
 * Copyright (c) 2019 SUSE LLC
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

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.common.messaging.MessageAction;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.contentmgmt.SoftwareEnvironmentTarget;
import com.redhat.rhn.domain.contentmgmt.SoftwareProjectSource;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.user.UserManager;
import org.apache.log4j.Logger;

import java.time.Duration;
import java.time.Instant;

/**
 * Align Errata and Packages of {@link SoftwareEnvironmentTarget} to its corresponding {@link SoftwareProjectSource}
 *
 */
public class AlignSoftwareTargetAction implements MessageAction {

    private static final Logger LOG = Logger.getLogger(AlignSoftwareTargetAction.class);

    @Override
    public void execute(EventMessage msgIn) {
        AlignSoftwareTargetMsg msg = (AlignSoftwareTargetMsg) msgIn;
        SoftwareProjectSource source = (SoftwareProjectSource) HibernateFactory.reload(msg.getSource());
        SoftwareEnvironmentTarget target = (SoftwareEnvironmentTarget) HibernateFactory.reload(msg.getTarget());

        if (!UserManager.verifyChannelAdmin(msg.getUser(), target.getChannel())) {
            throw new PermissionException("User " + msg.getUser().getLogin() + " has no permission for channel " +
                    target.getChannel().getLabel());
        }

        LOG.info("Asynchronously aligning: " + msg);
        Instant start = Instant.now();
        ChannelManager.alignChannelsSync(source, target, msg.getUser());
        LOG.info("Finished aligning " + msg + " in " + Duration.between(start, Instant.now()));
    }

    @Override
    public boolean canRunConcurrently() {
        return false;
    }

    @Override
    public boolean needsTransactionHandling() {
        return true;
    }
}
