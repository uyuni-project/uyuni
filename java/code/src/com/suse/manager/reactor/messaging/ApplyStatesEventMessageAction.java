/*
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
import com.redhat.rhn.common.messaging.MessageAction;
import com.redhat.rhn.domain.action.salt.ApplyStatesAction;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

/**
 * Applies states to a server
 */
public class ApplyStatesEventMessageAction implements MessageAction {

    private static final TaskomaticApi TASKOMATIC_API = new TaskomaticApi();
    private static final Logger LOG = LogManager.getLogger(ApplyStatesEventMessageAction.class);

    /**
     * Default constructor.
     */
    public ApplyStatesEventMessageAction() {
    }

    @Override
    public void execute(EventMessage event) {
        ApplyStatesEventMessage applyStatesEvent = (ApplyStatesEventMessage) event;
        Server server = ServerFactory.lookupById(applyStatesEvent.getServerId());

        // Apply states only for salt systems
        if (server != null && server.hasEntitlement(EntitlementManager.SALT)) {
            LOG.debug("Schedule state.apply for {}: {}", server.getName(), applyStatesEvent.getStateNames());

            // The scheduling user can be null
            User scheduler = event.getUserId() != null ?
                    UserFactory.lookupById(event.getUserId()) : null;

            try {
                // Schedule a "state.apply" action to happen right now
                ApplyStatesAction action = ActionManager.scheduleApplyStates(
                        scheduler,
                        Arrays.asList(server.getId()),
                        applyStatesEvent.getStateNames(),
                        applyStatesEvent.getPillar(),
                        new Date(), Optional.of(false));
                TASKOMATIC_API.scheduleActionExecution(action,
                        applyStatesEvent.isForcePackageListRefresh());
            }
            catch (TaskomaticApiException e) {
                LOG.error("Could not schedule state application for system: {}", server.getId());
                throw new RuntimeException(e);
            }
        }
    }
}
