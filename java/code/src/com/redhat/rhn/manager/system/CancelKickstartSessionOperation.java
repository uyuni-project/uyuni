/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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
package com.redhat.rhn.manager.system;

import com.redhat.rhn.common.RhnRuntimeException;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.KickstartSession;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.SystemPendingEventDto;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerSystemRemoveCommand;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * DeleteSystemFromActionOperation - deletes a system from an action
 */
public class CancelKickstartSessionOperation
    extends BaseSystemOperation {

    /**
     * Construct the Operation
     * @param userIn who is performing this operation
     * @param sid id of System to lookup
     */
    public CancelKickstartSessionOperation(User userIn, Long sid) {
        super(sid);
        this.user = userIn;
    }

    /**
     * {@inheritDoc}
     */
    public ValidatorError store() {

        KickstartSession ksession =
            KickstartFactory.lookupKickstartSessionByServer(server.getId());
        String failedMessage = LocalizationService.getInstance().
        getMessage("kickstart.session.user_canceled", this.user.getLogin());
        ksession.markFailed(failedMessage);
        KickstartFactory.saveKickstartSession(ksession);

        // Create and schedule a CobblerSystemRemoveCommand
        CobblerSystemRemoveCommand cobblerRemove = new CobblerSystemRemoveCommand(user, server);
        cobblerRemove.store();

        // Remove any possible autoinstallation initiate actions from the server
        DataResult<SystemPendingEventDto> pendingActions = SystemManager.systemPendingEvents(server.getId(), null);

        String rebootName = ActionFactory.TYPE_KICKSTART_INITIATE.getName();
        List<Action> actions = pendingActions.stream().filter(action -> action.getActionName().equals(rebootName))
                .map(action -> ActionManager.lookupAction(user, action.getId()))
                .collect(Collectors.toList());
        try {
            ActionManager.cancelActions(user, actions, List.of(server.getId()));
        }
        catch (TaskomaticApiException eIn) {
            throw new RhnRuntimeException(eIn);
        }
        return null;
    }

}
