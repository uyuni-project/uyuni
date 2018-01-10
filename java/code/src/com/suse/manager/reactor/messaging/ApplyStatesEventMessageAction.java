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
package com.suse.manager.reactor.messaging;

import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.domain.action.salt.ApplyStatesAction;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.frontend.events.AbstractDatabaseAction;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.utils.MinionServerUtils;

import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.Date;

/**
 * Applies states to a server
 */
public class ApplyStatesEventMessageAction extends AbstractDatabaseAction {

    private static final TaskomaticApi TASKOMATIC_API = new TaskomaticApi();
    private static final Logger LOG = Logger.getLogger(ApplyStatesEventMessageAction.class);

    /**
     * Default constructor.
     */
    public ApplyStatesEventMessageAction() {
    }

    @Override
    public void doExecute(EventMessage event) {
        ApplyStatesEventMessage applyStatesEvent = (ApplyStatesEventMessage) event;
        Server server = ServerFactory.lookupById(applyStatesEvent.getServerId());

        // Apply states only for salt systems
        if (server != null && server.hasEntitlement(EntitlementManager.SALT)) {
            LOG.debug("Schedule state.apply for " + server.getName() + ": " +
                    applyStatesEvent.getStateNames());

            // The scheduling user can be null
            User scheduler = event.getUserId() != null ?
                    UserFactory.lookupById(event.getUserId()) : null;

            try {
                // Schedule a "state.apply" action to happen right now
                ApplyStatesAction action = ActionManager.scheduleApplyStates(
                        scheduler,
                        Arrays.asList(server.getId()),
                        applyStatesEvent.getStateNames(),
                        new Date());
                TASKOMATIC_API.scheduleActionExecution(action,
                        applyStatesEvent.isForcePackageListRefresh());

                // For Salt SSH: simply schedule package profile update (no job metadata)
                if (MinionServerUtils.isSshPushMinion(server) &&
                        applyStatesEvent.isForcePackageListRefresh()) {
                    ActionManager.schedulePackageRefresh(server.getOrg(), server);
                }
            }
            catch (TaskomaticApiException e) {
                LOG.error("Could not schedule state application for system: " +
                        server.getId());
                throw new RuntimeException(e);
            }
        }
    }
}
