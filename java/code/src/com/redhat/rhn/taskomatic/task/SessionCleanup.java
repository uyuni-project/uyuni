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
package com.redhat.rhn.taskomatic.task;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.WriteMode;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.HashMap;
import java.util.Map;

/**
 * SessionCleanup
 * Deletes expired rows from the PXTSessions table to keep it from
 * growing too large.
 */
public class SessionCleanup extends RhnJavaJob {

    @Override
    public String getConfigNamespace() {
        return "session_cleanup";
    }

    /**
     * {@inheritDoc}
     */
    public void execute(JobExecutionContext context)
            throws JobExecutionException {
        Config c = Config.get();
        Map inParams = new HashMap();

        //retrieves info from user preferences
        long window = c.getInt("web.session_database_lifetime");

        long bound = (System.currentTimeMillis() / 1000) - (2 * window);

        log.debug("session_cleanup: starting delete of stale sessions");
        if (log.isDebugEnabled()) {
            log.debug("Session expiry threshold is " + bound);
        }

        //input parameters of the query
        inParams.put("bound", bound);

        WriteMode m = ModeFactory.getWriteMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_SESSION_CLEANUP);
        if (log.isDebugEnabled()) {
            log.debug("Executing WriteMode " + TaskConstants.MODE_NAME + "::" +
                    TaskConstants.TASK_QUERY_SESSION_CLEANUP);
        }
        int sessionsDeleted = m.executeUpdate(inParams);
        if (log.isDebugEnabled()) {
            log.debug("WriteMode " + TaskConstants.MODE_NAME + "::" +
                    TaskConstants.TASK_QUERY_SESSION_CLEANUP + " returned");
        }
        //logs number of sessions deleted
        if (sessionsDeleted > 0) {
            log.info(sessionsDeleted + " stale session(s) deleted");
        }
        else {
            log.debug("No stale sessions deleted");
        }
    }

}
