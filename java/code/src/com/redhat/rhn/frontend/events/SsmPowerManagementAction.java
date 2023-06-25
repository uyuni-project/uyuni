/*
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
package com.redhat.rhn.frontend.events;

import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.common.messaging.MessageAction;
import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.frontend.dto.SystemOverview;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerPowerCommand;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerPowerCommand.Operation;
import com.redhat.rhn.manager.ssm.SsmOperationManager;
import com.redhat.rhn.manager.system.SystemManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cobbler.XmlRpcException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Silvio Moioli {@literal <smoioli@suse.de>}
 */
public class SsmPowerManagementAction implements MessageAction {

    /** Logger instance */
    private static Logger log = LogManager.getLogger(SsmPowerManagementAction.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(EventMessage msgIn) {
        SsmPowerManagementEvent event = (SsmPowerManagementEvent) msgIn;
        Long userId = event.getUserId();
        User user = UserFactory.lookupById(userId);
        List<Long> sids = new ArrayList<>();
        for (SystemOverview systemOverview : event.getSystemOverviews()) {
            sids.add(systemOverview.getId());
        }

        Operation operation = event.getOperation();
        long operationId = SsmOperationManager.createOperation(user,
            "cobbler.powermanagement." + operation.toString().toLowerCase(), null);
        SsmOperationManager.associateServersWithOperation(operationId, userId, sids);

        try {
            for (Long sid : sids) {
                log.debug("Running operation {} on server {}", operation, sid);
                Server server = SystemManager.lookupByIdAndUser(sid, user);

                ValidatorError error = null;
                try {
                    error = new CobblerPowerCommand(user, server, operation).store();
                }
                catch (XmlRpcException e) {
                    log.error(e);
                    error = new ValidatorError(
                        "ssm.provisioning.powermanagement.cobbler_error");
                }
                if (error != null) {
                    log.error(error.getLocalizedMessage());
                    SsmOperationManager.addNoteToOperationOnServer(operationId,
                        server.getId(), error.getKey());
                }
            }
        }
        catch (Exception e) {
            log.error("Error in power management operations {}", event, e);
        }
        finally {
            SsmOperationManager.completeOperation(user, operationId);
        }
    }
}
