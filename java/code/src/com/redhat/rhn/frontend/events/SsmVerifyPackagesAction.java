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

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.common.messaging.MessageAction;
import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.frontend.dto.PackageListItem;
import com.redhat.rhn.manager.action.ActionChainManager;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.ssm.SsmOperationManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Schedules package verifications on systems in the SSM.
 */
public class SsmVerifyPackagesAction implements MessageAction {

    private static final Logger LOG = LogManager.getLogger(SsmVerifyPackagesAction.class);

    /** {@inheritDoc} */
    @Override
    public void execute(EventMessage msg) {
        SsmVerifyPackagesEvent event = (SsmVerifyPackagesEvent) msg;

        User user = UserFactory.lookupById(event.getUserId());

        // Log the action has been created
        long operationId =
            SsmOperationManager.createOperation(user, "ssm.package.verify.operationname",
                RhnSetDecl.SYSTEMS.getLabel());

        try {
            scheduleVerifications(event, user);
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

    private void scheduleVerifications(SsmVerifyPackagesEvent event, User user)
        throws TaskomaticApiException {

        Date earliest = event.getEarliest();
        ActionChain actionChain = ActionChainFactory.getActionChain(
            user, event.getActionChainId());
        DataResult result = event.getResult();

        // Loop over each server that will have packages upgraded
        for (Object valueIn : result) {

            // Add action for each package found in the elaborator
            Map data = (Map) valueIn;

            // Load the server
            Long sid = (Long) data.get("id");
            Server server = SystemManager.lookupByIdAndUser(sid, user);

            // Get the packages out of the elaborator
            List elabList = (List) data.get("elaborator0");

            List<PackageListItem> items = new ArrayList<>(elabList.size());
            for (Object oIn : elabList) {
                Map elabData = (Map) oIn;
                String idCombo = (String) elabData.get("id_combo");
                PackageListItem item = PackageListItem.parse(idCombo);
                items.add(item);
            }

            // Convert to list of maps for the action call
            List<Map<String, Long>> packageListData = PackageListItem.toKeyMaps(items);

            // Create the action(s)
            ActionChainManager.schedulePackageVerify(user, server, packageListData,
                    earliest, actionChain);
        }

    }
}
