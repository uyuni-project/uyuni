/**
 * Copyright (c) 2013 SUSE LLC
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
/**
 * Copyright (c) 2014 Red Hat, Inc.
 */

package com.redhat.rhn.frontend.events;

import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.common.messaging.MessageAction;
import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.manager.action.ActionChainManager;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.ssm.SsmOperationManager;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import org.apache.log4j.Logger;

/**
 * System Reboot action in the SSM/Miscellaneous.
 * @author Bo Maryniuk {@literal <bo@suse.de>}
 */
public class SsmSystemRebootAction implements MessageAction {
    private static Logger log = Logger.getLogger(SsmSystemRebootAction.class);

    @Override
    public void execute(EventMessage msg) {
        SsmSystemRebootAction.log.debug("Scheduling systems reboot in SSM.");
        SsmSystemRebootEvent event = (SsmSystemRebootEvent) msg;
        User user = UserFactory.lookupById(event.getUserId());
        ActionChain actionChain = ActionChainFactory.getActionChain(user, event
            .getActionChainId());

        try {
            ActionChainManager.scheduleRebootActions(user, event.getServerIds(),
                event.getEarliest(), actionChain);
        }
        catch (TaskomaticApiException e) {
            log.error("Could not schedule reboot:");
            log.error(e);
            throw new RuntimeException(e);
        }
        SsmOperationManager.completeOperation(user,
            SsmOperationManager.createOperation(user, "ssm.misc.reboot.operationname",
                RhnSetDecl.SSM_SYSTEMS_REBOOT.getLabel()));
    }
}
