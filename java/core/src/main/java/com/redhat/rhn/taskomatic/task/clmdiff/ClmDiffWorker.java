/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.taskomatic.task.clmdiff;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.manager.contentmgmt.ContentManager;
import com.redhat.rhn.taskomatic.TaskoFactory;
import com.redhat.rhn.taskomatic.task.TaskConstants;
import com.redhat.rhn.taskomatic.task.threaded.QueueWorker;
import com.redhat.rhn.taskomatic.task.threaded.TaskQueue;

import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * Generates metadata files for channels.
 */
public class ClmDiffWorker implements QueueWorker {

    private TaskQueue parentQueue;
    private final Logger logger;
    private final String projectLabel;
    private final String environmentLabel;
    private final String channelLabel;

    /**
     *
     * @param workItem work item map
     * @param parentLogger repomd logger
     */
    public ClmDiffWorker(Map<String, Object> workItem, Logger parentLogger) {
        logger = parentLogger;

        projectLabel = (String) workItem.get("project_label");
        environmentLabel = (String) workItem.get("environment_label");
        channelLabel = (String) workItem.get("channel_label");
        logger.debug("Creating ClmDiffWorker for projectLabel({}), environmentLabel({}) and channelLabel ({})",
                projectLabel, environmentLabel, channelLabel);
    }

    /**
     * Sets the parent queue
     * @param queue task queue
     */
    @Override
    public void setParentQueue(TaskQueue queue) {
        parentQueue = queue;
    }

    /**
     * runner method to process the parentQueue
     */
    @Override
    public void run() {
        logger.info("Starting worker for {}/{}/{}", projectLabel, environmentLabel, channelLabel);
        try {
            parentQueue.workerStarting();
            if (!isTaskAlreadyInProcess()) {
                markInProgress(true);

                ContentManager cm = new ContentManager();
                cm.diffClmChannel(projectLabel, environmentLabel, channelLabel);
                dequeueTask();
            }
            else {
                HibernateFactory.commitTransaction();
                logger.warn("NOT processing Task ({}/{}/{}) because another thread is already working on it",
                        projectLabel, environmentLabel, channelLabel);
            }
        }
        catch (Exception e) {
            logger.error(e.getMessage(), e);
            TaskoFactory.markFailed(parentQueue.getQueueRun());
            // unmark channel to be worked on
            markInProgress(false);
            parentQueue.changeRun(null);
        }
        finally {
            logger.info("Worker for Task {}/{}/{} has completed", projectLabel, environmentLabel, channelLabel);
            parentQueue.workerDone();
            HibernateFactory.closeSession();
        }
    }

    private Map<String, Object> getParameterMap() {
        return Map.of(
                "project_label", projectLabel,
                "environment_label", environmentLabel,
                "channel_label", channelLabel);
    }

    /**
     *
     * @return Returns the progress status of the channel
     */
    private boolean isTaskAlreadyInProcess() {
        SelectMode selector = ModeFactory.getMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_CLMDIFF_DETAILS_QUERY);
        DataResult<?> resultSet = selector.execute(getParameterMap());
        return !resultSet.isEmpty();
    }

    /**
     * marks the channel as in progress to avoid conflicts
     */
    private void markInProgress(boolean inProgress) {
        WriteMode inProgressChannel;
        if (inProgress) {
            inProgressChannel = ModeFactory.getWriteMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_CLMDIFF_MARK_IN_PROGRESS);
        }
        else {
            inProgressChannel = ModeFactory.getWriteMode(TaskConstants.MODE_NAME,
                    TaskConstants.TASK_QUERY_CLMDIFF_UNMARK_IN_PROGRESS);
        }
        try {
            int numRows = inProgressChannel.executeUpdate(getParameterMap());
            if (logger.isDebugEnabled()) {
                if (inProgress) {
                    logger.debug("Marked {} rows from the suseClmDiffQueue table in progress by setting " +
                            "next_action to null", numRows);
                }
                else {
                    logger.debug("Cleared {} in progress rows from the suseClmDiffQueue table by " +
                            "setting next_action", numRows);
                }
            }
            HibernateFactory.commitTransaction();
        }
        catch (Exception e) {
            logger.error("Error un/marking in use for {}/{}/{}", projectLabel, environmentLabel, channelLabel, e);
            HibernateFactory.rollbackTransaction();
        }
        finally {
            HibernateFactory.closeSession();
        }
    }

    /**
     * dequeue the queued channel for repomd generation
     */
    private void dequeueTask() {
        WriteMode deqChannel = ModeFactory.getWriteMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_CLMDIFF_DEQUEUE);
        try {
            int eqDeleted = deqChannel.executeUpdate(getParameterMap());
            if (logger.isDebugEnabled()) {
                logger.debug("deleted {} rows from the suseClmDiffQueue table", eqDeleted);
            }
            HibernateFactory.commitTransaction();
        }
        catch (Exception e) {
            logger.error("Error removing Task from queue for {}/{}/{}", projectLabel, environmentLabel,
                    channelLabel, e);
            HibernateFactory.rollbackTransaction();
        }
    }
}
