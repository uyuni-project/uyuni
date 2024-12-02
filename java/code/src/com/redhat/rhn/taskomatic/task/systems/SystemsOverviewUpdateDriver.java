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

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.domain.task.Task;
import com.redhat.rhn.domain.task.TaskFactory;
import com.redhat.rhn.taskomatic.task.threaded.AbstractQueueDriver;
import com.redhat.rhn.taskomatic.task.threaded.QueueWorker;

import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Driver for the threaded system overview update queue
 */
public class SystemsOverviewUpdateDriver extends AbstractQueueDriver<Long> {

    public static final String TASK_NAME = "update_system_overview";
    private Logger logger = null;

    @Override
    public void setLogger(Logger loggerIn) {
        logger = loggerIn;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    protected List<Long> getCandidates() {
        // Candidates are system IDs, deduplicated to avoid useless updates
        return TaskFactory.getTaskListByNameLike(TASK_NAME).stream()
            .map(Task::getData)
            .distinct()
            .collect(Collectors.toList());
    }

    @Override
    public int getMaxWorkers() {
        return Config.get().getInt("taskomatic.systems_overview_update_workers", 2);
    }

    @Override
    protected QueueWorker makeWorker(Long sid) {
        return new SystemsOverviewUpdateWorker(sid, logger);
    }
}
