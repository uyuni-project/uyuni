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

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.common.messaging.MessageAction;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.manager.ssm.SsmOperationManager;
import com.redhat.rhn.manager.system.SystemManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Handles performing subscription changes for servers in the SSM.
 *
 * @see com.redhat.rhn.frontend.events.SsmDeleteServersEvent
 */
public class SsmDeleteServersAction implements MessageAction {
    public static final String OPERATION_NAME = "ssm.server.delete.operationname";

    /** Logger instance. */
    private static final Logger LOG = LogManager.getLogger(SsmDeleteServersAction.class);

    private final SystemManager systemManager;

    /**
     * Constructor
     *
     * @param systemManagerIn the system manager
     */
    public SsmDeleteServersAction(SystemManager systemManagerIn) {
        systemManager = systemManagerIn;
    }

    /** {@inheritDoc} */
    @Override
    public void execute(EventMessage msg) {
        SsmDeleteServersEvent event = (SsmDeleteServersEvent) msg;
        User user = UserFactory.lookupById(event.getUserId());
        List<Long> sids = event.getSids();

        long operationId = SsmOperationManager.createOperation(user,
                        OPERATION_NAME, null);

        SsmOperationManager.associateServersWithOperation(operationId,
                                                        user.getId(), sids);
        HibernateFactory.commitTransaction();
        try {
            for (Long sid : sids) {
                try {
                    systemManager.deleteServerAndCleanup(user,
                            sid,
                            event.getServerCleanupType()
                    );
                    // commit after each deletion to prevent deadlocks with
                    // system registration
                    HibernateFactory.commitTransaction();
                }
                catch (Exception e) {
                    // Should never happen, but let's roll back the transaction
                    // and continue
                    HibernateFactory.rollbackTransaction();
                }

                HibernateFactory.closeSession();
                HibernateFactory.getSession();
            }
        }
        catch (Exception e) {
            LOG.error("Error deleting servers {}", event, e);
        }
        finally {
            // Complete the action
            SsmOperationManager.completeOperation(user, operationId);
        }

    }
}
