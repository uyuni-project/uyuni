/*
 * Copyright (c) 2009--2015 Red Hat, Inc.
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
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.taskomatic.domain.TaskoRun;
import com.redhat.rhn.taskomatic.task.threaded.QueueDriver;
import com.redhat.rhn.taskomatic.task.threaded.TaskQueue;
import com.redhat.rhn.taskomatic.task.threaded.TaskQueueFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Custom Quartz Job implementation which only allows one thread to
 * run at a time. All other threads return without performing any work.
 * This policy was chosen instead of blocking to reduce threading
 * problems inside Quartz itself.
 *
 * @param <T> The {@link QueueDriver} class
 *
 */
public abstract class RhnQueueJob<T extends QueueDriver> implements RhnJob {

    private TaskoRun jobRun = null;
    protected Logger log = LogManager.getLogger(getClass().getName());
    protected Logger getLogger() {
        return log;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void appendExceptionToLogError(Exception e) {
        getLogger().error(e.getMessage(), e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(JobExecutionContext ctx, TaskoRun runIn)
            throws JobExecutionException {
        setJobRun(runIn);
        execute(ctx);
        HibernateFactory.commitTransaction();
        HibernateFactory.closeSession();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(JobExecutionContext ctx) {
        TaskQueueFactory factory = TaskQueueFactory.get();
        String queueName = getQueueName();
        TaskQueue queue = factory.getQueue(queueName);
        if (queue == null) {
            try {
                queue = factory.createQueue(queueName, getDriverClass(), getLogger());
            }
            catch (Exception e) {
                getLogger().error(e.getMessage(), e);
                return;
            }
        }
        if (queue.changeRun(jobRun)) {
            jobRun.start();
            HibernateFactory.commitTransaction();
            HibernateFactory.closeSession();
            getLogger().debug("Starting run {}", jobRun.getId());
        }
        else {
            // close current run
            TaskoRun run = HibernateFactory.reload(jobRun);
            log.debug("Run with id {} handles the whole task queue.", queue.getQueueRun().getId());
            run.skipped();
            HibernateFactory.commitTransaction();
            HibernateFactory.closeSession();
        }
        int defaultItems = 3;
        if (queueName.equals("channel_repodata")) {
            defaultItems = 1;
        }
        int maxWorkItems = Config.get().getInt("taskomatic." + queueName + "_max_work_items", defaultItems);
        int queueSize = queue.getQueueSize();
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Queue size (before run): {}", queueSize);
        }
        if (queueSize < maxWorkItems) {
            queue.run();
        }
        else {
            getLogger().warn("Maximum number of workers already put ... skipping.");
        }
    }

    /**
     * @return Returns the run.
     */
    public TaskoRun getRun() {
        return jobRun;
    }

    /**
     * @param runIn The run to set.
     */
    public void setJobRun(TaskoRun runIn) {
        jobRun = runIn;
    }

    protected abstract Class<T> getDriverClass();

    protected abstract String getQueueName();
}
