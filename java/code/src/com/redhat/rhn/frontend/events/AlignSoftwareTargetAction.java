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

import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.common.messaging.MessageAction;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.contentmgmt.ContentFilter;
import com.redhat.rhn.domain.contentmgmt.ContentProjectFactory;
import com.redhat.rhn.domain.contentmgmt.EnvironmentTarget.Status;
import com.redhat.rhn.domain.contentmgmt.SoftwareEnvironmentTarget;
import com.redhat.rhn.manager.EntityNotExistsException;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.user.UserManager;
import org.apache.log4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;

/**
 * Align Errata and Packages of {@link Channel} to given {@link Channel}
 *
 */
public class AlignSoftwareTargetAction implements MessageAction {

    private static final Logger LOG = Logger.getLogger(AlignSoftwareTargetAction.class);

    @Override
    public void execute(EventMessage msgIn) {
        AlignSoftwareTargetMsg msg = (AlignSoftwareTargetMsg) msgIn;
        Channel sourceChannel = ChannelFactory.lookupById(msg.getSource().getId());
        Long targetId = msg.getTarget().getId();
        SoftwareEnvironmentTarget target = ContentProjectFactory
                .lookupSwEnvironmentTargetById(targetId)
                .orElseThrow(() -> new EntityNotExistsException(targetId));
        Channel targetChannel = target.getChannel();

        try {
            if (!UserManager.verifyChannelAdmin(msg.getUser(), targetChannel)) {
                throw new PermissionException("User " + msg.getUser().getLogin() + " has no permission for channel " +
                        targetChannel.getLabel());
            }

            LOG.info("Asynchronously aligning: " + msg);
            Instant start = Instant.now();
            // todo explicit passing of the filters!
            List<ContentFilter> filters = target.getContentEnvironment().getContentProject().getActiveFilters();
            ChannelManager.alignEnvironmentTargetSync(filters, sourceChannel, targetChannel, msg.getUser());
            target.setStatus(Status.GENERATING_REPODATA);
            LOG.info("Finished aligning " + msg + " in " + Duration.between(start, Instant.now()));
        }
        catch (Throwable t) {
            throw new AlignSoftwareTargetException(target, t);
        }
    }

    @Override
    public boolean canRunConcurrently() {
        return false;
    }

    @Override
    public Consumer<Exception> getExceptionHandler() {
        return (e) -> {
            if (e instanceof AlignSoftwareTargetException) {
                LOG.error("Error aligning channel", e);
                AlignSoftwareTargetException exc = ((AlignSoftwareTargetException) e);
                exc.getTarget().setStatus(Status.FAILED);
                ContentProjectFactory.save(exc.getTarget());
            }
        };
    }

    @Override
    public boolean needsTransactionHandling() {
        return true;
    }

    private class AlignSoftwareTargetException extends RuntimeException {

        private SoftwareEnvironmentTarget target;

        AlignSoftwareTargetException(SoftwareEnvironmentTarget targetIn, Throwable cause) {
            super(cause);
            this.target = targetIn;
        }

        /**
         * Gets the target.
         *
         * @return target
         */
        public SoftwareEnvironmentTarget getTarget() {
            return target;
        }
    }
}
