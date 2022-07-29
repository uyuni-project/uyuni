/*
 * Copyright (c) 2022 SUSE LLC
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
package com.redhat.rhn.taskomatic.task.systems;

import com.redhat.rhn.common.db.datasource.CallableMode;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.taskomatic.task.threaded.QueueWorker;
import com.redhat.rhn.taskomatic.task.threaded.TaskQueue;

import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Performs overview table refresh for a given server
 */
public class SystemsOverviewUpdateWorker implements QueueWorker {

    private final Logger logger;
    private final Long sid;
    private TaskQueue parentQueue;


    /**
     * Constructor
     *
     * @param sidIn The ID of server to update the overview from
     * @param loggerIn the logger
     */
    public SystemsOverviewUpdateWorker(Long sidIn, Logger loggerIn) {
        sid = sidIn;
        logger = loggerIn;
    }

    @Override
    public void setParentQueue(TaskQueue queue) {
        parentQueue = queue;
    }

    @Override
    public void run() {
        try {
            removeTask();
            parentQueue.workerStarting();
            doUpdate();
            HibernateFactory.commitTransaction();
        }
        catch (Exception e) {
            logger.error(e);
            HibernateFactory.rollbackTransaction();
        }
        finally {
            parentQueue.workerDone();
            HibernateFactory.closeSession();
        }

    }

    private void doUpdate() {
        CallableMode mode = ModeFactory.getCallableMode("System_queries", "update_system_overview");
        Map<String, Object> params = Map.of("sid", sid);
        mode.execute(params, new HashMap<>());
    }

    private void removeTask() {
        WriteMode mode = ModeFactory.getWriteMode("Task_queries", "delete_task");
        Map<String, Object> params = new HashMap<>();
        params.put("org_id", 1);
        params.put("name", SystemsOverviewUpdateDriver.TASK_NAME);
        params.put("task_data", sid);
        params.put("priority", 0);
        mode.executeUpdate(params);
    }
}
