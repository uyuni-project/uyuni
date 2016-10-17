/**
 * Copyright (c) 2016 SUSE LLC
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
package com.redhat.rhn.taskomatic.task.sshpush;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.task.threaded.QueueWorker;
import com.redhat.rhn.taskomatic.task.threaded.TaskQueue;

import org.apache.log4j.Logger;

import java.util.Optional;

/**
 * SSH push worker executing actions via salt-ssh.
 */
public class SSHPushWorkerSalt implements QueueWorker {

    private Logger log;
    private SSHPushSystem system;
    private TaskQueue parentQueue;

    /**
     * Constructor.
     * @param logger Logger for this instance
     * @param systemIn the system to work with
     */
    public SSHPushWorkerSalt(Logger logger, SSHPushSystem systemIn) {
        log = logger;
        system = systemIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setParentQueue(TaskQueue queue) {
        parentQueue = queue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        try {
            parentQueue.workerStarting();

            // Lookup the minion server object
            Optional<MinionServer> minion = MinionServerFactory.lookupById(system.getId());
            minion.ifPresent(m -> {
                log.info("Executing actions for " + m.getMinionId());

                // TODO
                // 1. Get the current list of pending actions
                // 2. Execute those in schedule date order (consider prerequisites!)
                log.info("Number of pending events: " +
                        SystemManager.systemPendingEvents(m.getId(), null).size());
            });
        }
        catch (Exception e) {
            log.error(e.getMessage());
            HibernateFactory.rollbackTransaction();
        }
        finally {
            parentQueue.workerDone();
            HibernateFactory.closeSession();

            // Finished talking to this system
            SSHPushDriver.getCurrentSystems().remove(system);
        }
    }
}
