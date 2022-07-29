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
package com.redhat.rhn.taskomatic.task;

import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.task.systems.SystemsOverviewUpdateDriver;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * Taskomatic task updating the suseSystemOverview table
 */
public class SystemOverviewUpdateTask extends RhnJavaJob {

    @Override
    public String getConfigNamespace() {
        return "system_overview_update";
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        // Queue one task for each system to be picked by ServerOverviewUpdateQueue
        SystemManager.listSystemIds().forEach(sid -> {
            WriteMode mode = ModeFactory.getWriteMode(TaskConstants.MODE_NAME, "insert_into_task_queue");
            Map<String, Object> params = new HashMap<>();
            params.put("org_id", 1);
            params.put("task_name", SystemsOverviewUpdateDriver.TASK_NAME);
            params.put("task_data", sid);
            params.put("earliest", new Timestamp(System.currentTimeMillis()));
            mode.executeUpdate(params);
        });
    }
}
