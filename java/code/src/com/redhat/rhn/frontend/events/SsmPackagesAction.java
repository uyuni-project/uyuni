/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.ssm.SsmOperationManager;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;
import java.util.List;

/**
 * Base functionality for responding to SSM package install/update/remove.
 * Handles ordering if a remote-cmd has been specified.  Subclasses are responsible
 * for describing the affected servers, and the actual work of scheduling action(s)
 *
 * @author ggainey
 *
 */
public abstract class SsmPackagesAction implements MessageAction {

    private static final Logger LOG = LogManager.getLogger(SsmPackagesAction.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(EventMessage msg) {
        SsmPackageEvent event = (SsmPackageEvent) msg;

        User user = UserFactory.lookupById(event.getUserId());

        /*
         * Comment below applies to more than just SsmPackageUpgrade. Moving here into the
         * (new) base-class for all actions, since it applies generally - there are often
         * systems in RhnSet.SYSTEM to which an Ssm action WILL NOT apply, since SSM
         * allows for heterogeneous sets of systems -- GRG 2013-APR-3
         */
        /*
         * The following isn't 100% accurate. All systems in the SSM are associated with
         * the operation, however only systems on which the package already exists (since
         * this is an upgrade) will actually have events scheduled.
         *
         * The problem is that the list of servers to which the package upgrades apply is
         * never stored in an RhnSet, which is used to make the impact of this call
         * minimal. The correct list is showed to the user before selecting confirm, so
         * the only potential issue is in viewing the SSM task log after the user has
         * confirmed the operation. Again, the events themselves are correctly scheduled
         * on only the systems to which they apply.
         *
         * For now, this small potential for logging inaccuracy is acceptable given the
         * proxmity of this fix to the Satellite 5.3 release (as opposed to omitting the
         * server association to the task entirely).
         *
         * jdobies, Aug 12, 2009
         */

        long operationId = SsmOperationManager.createOperation(user,
                getOperationName(), RhnSetDecl.SYSTEMS.getLabel());

        // Explicitly call handle transactions here so the operation creation above
        // is persisted before the potentially long running logic below
        //handleTransactions(true);

        try {
            long actionStart = System.currentTimeMillis();

            scheduleAction(event, user);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Time to schedule all actions: {}", System.currentTimeMillis() - actionStart);
            }
        }
        catch (TaskomaticApiException e) {
            LOG.error("Could not schedule package action:");
            LOG.error(e);
            throw new RuntimeException(e);
        }
        catch (Exception e) {
            LOG.error("Error scheduling package installations for event {}", event, e);
        }
        finally {
            // This should stay in the finally block so the operation is
            // not perpetually left in an in progress state
            SsmOperationManager.completeOperation(user, operationId);
        }
    }

    protected void scheduleAction(SsmPackageEvent event, User user)
        throws TaskomaticApiException {

        LOG.debug("Scheduling package actions.");
        Date earliest = event.getEarliest();
        ActionChain actionChain = ActionChainFactory.getActionChain(user, event
            .getActionChainId());

        List<Long> sids = getAffectedServers(event, user);


        LOG.debug("Scheduling actions.");

        doSchedule(event, user, sids, earliest, actionChain);

        LOG.debug("Done scheduling package actions.");

    }

    protected abstract String getOperationName();

    protected abstract List<Long> getAffectedServers(SsmPackageEvent event, User u);


    protected abstract List<Action> doSchedule(SsmPackageEvent event, User user,
            List<Long> sid, Date earliest, ActionChain actionChain)
        throws TaskomaticApiException;
}
