/*
 * Copyright (c) 2009--2013 Red Hat, Inc.
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
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.taskomatic.task.TaskConstants;
import com.redhat.rhn.taskomatic.task.threaded.QueueDriver;
import com.redhat.rhn.taskomatic.task.threaded.QueueWorker;

import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * Driver for the threaded errata queue
 */
public class ErrataQueueDriver implements QueueDriver<Map<String, Long>> {

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
    public List<Map<String, Long>> getCandidates() {
        SelectMode select = ModeFactory.getMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_ERRATA_QUEUE_FIND_CANDIDATES);
        try {
            return select.execute();
        }
        finally {
            HibernateFactory.closeSession();
        }
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
    public Logger getLogger() {
        return logger;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaxWorkers() {
        return Config.get().getInt("taskomatic.errata_queue_workers", 2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueueWorker makeWorker(Map<String, Long> workItem) {
        return new ErrataQueueWorker(workItem, logger);
    }

    /**
    *
    * {@inheritDoc}
    */
    @Override
    public void initialize() {
        // empty
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBlockingTaskQueue() {
        return false;
    }
}
