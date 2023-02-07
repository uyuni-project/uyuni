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
package com.redhat.rhn.taskomatic.task.errata;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.task.Task;
import com.redhat.rhn.domain.task.TaskFactory;
import com.redhat.rhn.manager.errata.cache.UpdateErrataCacheCommand;
import com.redhat.rhn.taskomatic.task.threaded.QueueWorker;
import com.redhat.rhn.taskomatic.task.threaded.TaskQueue;

import org.apache.logging.log4j.Logger;

/**
 * Performs errata cache recalc for a given server or channel
 */
public class ErrataCacheWorker implements QueueWorker {

    public static final String BY_CHANNEL = "update_errata_cache_by_channel";
    public static final String FOR_SERVER = "update_server_errata_cache";
    public static final String FOR_IMAGE  = "update_image_errata_cache";

    private Task task;
    private Logger logger;
    private TaskQueue parentQueue;

    /**
     * Constructor
     * @param taskIn the task to work on
     * @param parentLogger logger to use
     */
    public ErrataCacheWorker(Task taskIn, Logger parentLogger) {
        task = taskIn;
        logger = parentLogger;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        try {
            removeTask();
            parentQueue.workerStarting();
            UpdateErrataCacheCommand uecc = new UpdateErrataCacheCommand();
            if (ErrataCacheWorker.FOR_SERVER.equals(task.getName())) {
                Long sid = task.getData();
                if (logger.isDebugEnabled()) {
                    logger.debug("Updating errata cache for sid [{}]", sid);
                }
                uecc.updateErrataCacheForServer(sid, false);
                if (logger.isDebugEnabled()) {
                    logger.debug("Finished errata cache for sid [{}]", sid);
                }
            }
            else if (ErrataCacheWorker.FOR_IMAGE.equals(task.getName())) {
                Long iid = task.getData();
                if (logger.isDebugEnabled()) {
                    logger.debug("Updating errata cache for iid [{}]", iid);
                }
                uecc.updateErrataCacheForImage(iid, false);
                if (logger.isDebugEnabled()) {
                    logger.debug("Finished errata cache for iid [{}]", iid);
                }
            }
            else if (ErrataCacheWorker.BY_CHANNEL.equals(task.getName())) {
                Long cid = task.getData();
                if (logger.isDebugEnabled()) {
                    logger.debug("Updating errata cache for cid [{}]", cid);
                }
                uecc.updateErrataCacheForChannel(cid);
                if (logger.isDebugEnabled()) {
                    logger.debug("Finished errata cache for cid [{}]", cid);
                }
            }
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

    /**
     * Set the parent so we can tell it when we're done
     * @param queue the parent queue
     */
    @Override
    public void setParentQueue(TaskQueue queue) {
        parentQueue = queue;
    }

    /**
     * Remove the task related to this worker from the DB via mode query.
     */
    private void removeTask() {
        TaskFactory.deleteByOrgNameDataPriority(task.getOrg(), task.getName(), task.getData(), task.getPriority());
    }
}
