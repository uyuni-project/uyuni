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
package com.redhat.rhn.taskomatic.task.repomd;

import static com.redhat.rhn.domain.contentmgmt.EnvironmentTarget.Status.BUILT;
import static com.redhat.rhn.domain.contentmgmt.EnvironmentTarget.Status.FAILED;
import static com.redhat.rhn.domain.contentmgmt.EnvironmentTarget.Status.GENERATING_REPODATA;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.contentmgmt.ContentProjectFactory;
import com.redhat.rhn.domain.contentmgmt.EnvironmentTarget.Status;
import com.redhat.rhn.domain.contentmgmt.SoftwareEnvironmentTarget;
import com.redhat.rhn.taskomatic.task.TaskConstants;
import com.redhat.rhn.taskomatic.task.threaded.QueueWorker;
import com.redhat.rhn.taskomatic.task.threaded.TaskQueue;

import org.apache.logging.log4j.Logger;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * Generates metadata files for channels.
 */
public class ChannelRepodataWorker implements QueueWorker {

    private final RepositoryWriter repoWriter;
    private TaskQueue parentQueue;
    private final Logger logger;
    private final String channelLabelToProcess;

    private List<Map<String, String>> queueEntries;

    /**
     *
     * @param workItem work item map
     * @param parentLogger repomd logger
     */
    public ChannelRepodataWorker(Map<String, Object> workItem, Logger parentLogger) {
        logger = parentLogger;

        String prefixPath = Config.get().getString(ConfigDefaults.REPOMD_PATH_PREFIX, "rhn/repodata");
        String mountPoint = Config.get().getString(ConfigDefaults.REPOMD_CACHE_MOUNT_POINT, "/pub");

        channelLabelToProcess = (String) workItem.get("channel_label");

        // We need to find out whether to use Rpm or Debian repository
        Channel channelToProcess = ChannelFactory.lookupByLabel(channelLabelToProcess);
        // if the channelExists in the db still
        if (channelToProcess != null &&
            channelToProcess.getChannelArch().getArchType().getLabel().equalsIgnoreCase("deb")) {
            repoWriter = new DebRepositoryWriter(prefixPath, mountPoint);
        }
        else {
            repoWriter = new RpmRepositoryWriter(prefixPath, mountPoint);
        }
        logger.debug("Creating ChannelRepodataWorker with prefixPath({}), mountPoint({}) for channel_label ({})",
                prefixPath, mountPoint, channelLabelToProcess);
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
        // if a channel has a EnvironmentTarget associated, we update it too
        Optional<SoftwareEnvironmentTarget> envTarget = ContentProjectFactory
                .lookupEnvironmentTargetByChannelLabel(channelLabelToProcess);
        try {
            parentQueue.workerStarting();
            if (!isChannelLabelAlreadyInProcess()) {
                markInProgress(true);
                populateQueueEntryDetails();
                Channel channelToProcess = ChannelFactory.lookupByLabel(channelLabelToProcess);
                // if the channelExists in the db still
                if (channelToProcess != null) {
                    // see if the channel is stale, or one of the entries has
                    // force='Y'
                    if (queueContainsBypass("force") ||
                            repoWriter.isChannelRepodataStale(channelToProcess)) {
                        if (queueContainsBypass("bypass_filters") ||
                                channelToProcess.isChannelRepodataRequired()) {
                            repoWriter.writeRepomdFiles(channelToProcess);
                        }
                    }
                    else {
                        logger.debug("Not processing channel({}) because the request isn't forced AND the channel " +
                                "repodata isn't stale", channelLabelToProcess);
                    }
                }
                else {
                    repoWriter.deleteRepomdFiles(channelLabelToProcess, true);
                }

                setEnvironmentTargetStatus(envTarget, BUILT);

                dequeueChannel();
            }
            else {
                HibernateFactory.commitTransaction();
                logger.warn("NOT processing channel({}) because another thread is already working on run",
                        channelLabelToProcess);
            }
        }
        catch (Exception e) {
            logger.error(e);
            parentQueue.getQueueRun().failed();
            // unmark channel to be worked on
            markInProgress(false);
            setEnvironmentTargetStatus(envTarget, FAILED);
            parentQueue.changeRun(null);
        }
        finally {
            parentQueue.workerDone();
            HibernateFactory.closeSession();
        }
    }

    // helper method for setting state of environment target
    private void setEnvironmentTargetStatus(Optional<SoftwareEnvironmentTarget> environmentTarget, Status built) {
        environmentTarget
                .filter(t -> t.getStatus() == GENERATING_REPODATA)
                .ifPresent(t -> {
                    t.setStatus(built);
                    t.setBuiltTime(new Date());
                    ContentProjectFactory.save(t);
                });
    }

    /**
     * populates the queue details for repomd event
     */
    private void populateQueueEntryDetails() {
        SelectMode selector = ModeFactory.getMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_REPOMD_DETAILS_QUERY);
        queueEntries = selector.execute(Map.of("channel_label", channelLabelToProcess));
    }

    /**
     *
     * @return Returns the progress status of the channel
     */
    private boolean isChannelLabelAlreadyInProcess() {
        SelectMode selector = ModeFactory.getMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_REPOMD_DETAILS_QUERY);
        DataResult<?> resultSet = selector.execute(Map.of("channel_label", channelLabelToProcess));
        return !resultSet.isEmpty();
    }

    /**
     *
     * @param entryToCheck the name of the entry to verify
     * @return Returns a boolean to force or not
     */
    private boolean queueContainsBypass(String entryToCheck) {
        boolean shouldForce = false;

        for (Map<String, String> currentEntry : queueEntries) {
            String forceFlag = currentEntry.get(entryToCheck);
            if ("Y".equalsIgnoreCase(forceFlag)) {
                shouldForce = true;
            }
        }
        return shouldForce;
    }

    /**
     * marks the channel as in progress to avoid conflicts
     */
    private void markInProgress(boolean inProgress) {
        WriteMode inProgressChannel;
        if (inProgress) {
            inProgressChannel = ModeFactory.getWriteMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_REPOMD_MARK_IN_PROGRESS);
        }
        else {
            inProgressChannel = ModeFactory.getWriteMode(TaskConstants.MODE_NAME,
                    TaskConstants.TASK_QUERY_REPOMD_UNMARK_IN_PROGRESS);
        }
        Map<String, String> dqeParams = new HashMap<>();
        dqeParams.put("channel_label", channelLabelToProcess);
        try {
            int channelLabels = inProgressChannel.executeUpdate(dqeParams);
            if (logger.isDebugEnabled()) {
                if (inProgress) {
                    logger.debug("Marked {} rows from the rhnRepoRegenQueue table in progress by setting " +
                            "next_action to null", channelLabels);
                }
                else {
                    logger.debug("Cleared {} in progress rows from the rhnRepoRegenQueue table by " +
                            "setting next_action", channelLabels);
                }
            }
            HibernateFactory.commitTransaction();
        }
        catch (Exception e) {
            logger.error("Error un/marking in use for channel_label: {}", channelLabelToProcess, e);
            HibernateFactory.rollbackTransaction();
        }
        finally {
            HibernateFactory.closeSession();
        }
    }

    /**
     * dequeue the queued channel for repomd generation
     */
    private void dequeueChannel() {
        WriteMode deqChannel = ModeFactory.getWriteMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_REPOMD_DEQUEUE);
        Map<String, String> dqeParams = new HashMap<>();
        dqeParams.put("channel_label", channelLabelToProcess);
        try {
            int eqDeleted = deqChannel.executeUpdate(dqeParams);
            if (logger.isDebugEnabled()) {
                logger.debug("deleted {} rows from the rhnRepoRegenQueue table", eqDeleted);
            }
            HibernateFactory.commitTransaction();
        }
        catch (Exception e) {
            logger.error("Error removing Channel from queue for Channel: {}", channelLabelToProcess, e);
            HibernateFactory.rollbackTransaction();
        }
    }
}
