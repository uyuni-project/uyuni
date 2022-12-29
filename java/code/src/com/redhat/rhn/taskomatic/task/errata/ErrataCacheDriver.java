/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.taskomatic.task.errata;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.domain.task.Task;
import com.redhat.rhn.domain.task.TaskFactory;
import com.redhat.rhn.taskomatic.task.threaded.QueueDriver;
import com.redhat.rhn.taskomatic.task.threaded.QueueWorker;

import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Driver for the threaded errata cache update queue
 */
public class ErrataCacheDriver implements QueueDriver<Task> {

    private Logger logger = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canContinue() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Task> getCandidates() {
        List<Task> tasks = TaskFactory.getTaskListByNameLike(ErrataCacheWorker.BY_CHANNEL);
        tasks.addAll(consolidateTasks(
                TaskFactory.getTaskListByNameLike(ErrataCacheWorker.FOR_SERVER)));
        tasks.addAll(consolidateTasks(
                TaskFactory.getTaskListByNameLike(ErrataCacheWorker.FOR_IMAGE)));
        return tasks;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Logger getLogger() {
        return logger;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLogger(Logger loggerIn) {
        logger = loggerIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaxWorkers() {
        return Config.get().getInt("taskomatic.errata_cache_workers", 2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueueWorker makeWorker(Task task) {
        return new ErrataCacheWorker(task, logger);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize() {
        // empty
    }

    /**
     * Reduce a given list of tasks to a list with unique data fields. Data is either
     * a system id or a channel id depending on the type of tasks given in.
     *
     * @param tasks list of {@link Task} objects
     * @return consolidated list of tasks
     */
    private List<Task> consolidateTasks(List<Task> tasks) {
        Set<Long> uniqueTaskData = new HashSet<>();
        List<Task> consolidated = new ArrayList<>();
        for (Task task : tasks) {
            if (uniqueTaskData.add(task.getData())) {
                consolidated.add(task);
            }
        }
        return consolidated;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBlockingTaskQueue() {
        return false;
    }
}
