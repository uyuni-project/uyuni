/*
 * Copyright (c) 2020 SUSE LLC
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

package com.redhat.rhn.manager.satellite;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.contentmgmt.ContentProjectFactory;
import com.redhat.rhn.manager.BaseTransactionCommand;

import org.apache.log4j.Logger;

/**
 * Tasks to be performed on the application startup
 */
public class StartupTasksCommand extends BaseTransactionCommand {

    private static final Logger LOG = Logger.getLogger(StartupTasksCommand.class);

    /**
     * Constructor
     */
    public StartupTasksCommand() {
        super(LOG);
    }

    /**
     * Execution of application startup tasks
     */
    public void run() {
        try {
            int numOfUpgradedTgts = ContentProjectFactory.failStaleTargets();
            if (numOfUpgradedTgts > 0) {
                LOG.warn(String.format("Set %d stale Content Environment Targets to FAILED state", numOfUpgradedTgts));
            }
        }
        catch (Exception e) {
            LOG.error("Error when executing startup tasks", e);
            HibernateFactory.rollbackTransaction();
        }
        finally {
            handleTransaction();
        }
    }
}
