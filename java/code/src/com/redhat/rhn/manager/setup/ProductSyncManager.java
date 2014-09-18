/**
 * Copyright (c) 2014 SUSE
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
package com.redhat.rhn.manager.setup;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.content.ContentSyncManager;
import com.redhat.rhn.manager.satellite.Executor;
import com.redhat.rhn.taskomatic.TaskoFactory;
import com.redhat.rhn.taskomatic.TaskoRun;
import com.redhat.rhn.taskomatic.TaskoSchedule;
import com.redhat.rhn.taskomatic.TaskomaticApiException;
import com.redhat.rhn.taskomatic.task.TaskConstants;

import com.suse.manager.model.products.Channel;
import com.suse.manager.model.products.Product;
import com.suse.manager.model.products.Product.SyncStatus;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import redstone.xmlrpc.XmlRpcClient;
import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcFault;

/**
 * Manager class for interacting with SUSE products.
 */
public abstract class ProductSyncManager {

    /** The logger. */
    protected static Logger logger = Logger.getLogger(ProductSyncManager.class);

    /**
     * Create {@link ProductSyncManager} instance for a given {@link Executor}.
     * @param executorIn instance of {@link Executor}
     * @return instance of {@link ProductSyncManager}
     */
    public static ProductSyncManager createInstance(Executor executorIn) {
        return isMigratedToSCC() ? new SCCProductSyncManager() :
                new NCCProductSyncManager(executorIn);
    }

    /**
     * Create {@link ProductSyncManager} instance.
     * @return instance of {@link ProductSyncManager}
     */
    public static ProductSyncManager createInstance() {
        return isMigratedToSCC() ? new SCCProductSyncManager() : new NCCProductSyncManager();
    }

    /**
     * Returns a list of base products.
     * @return the products list
     * @throws ProductSyncManagerCommandException if external commands or parsing fail
     * @throws ProductSyncManagerParseException if a parsing problem shows up
     */
    public abstract List<Product> getBaseProducts()
            throws ProductSyncManagerCommandException,
                   ProductSyncManagerParseException;

    /**
     * Adds multiple products.
     * @param productIdents the product ident list
     * @throws ProductSyncManagerCommandException if a product addition failed
     */
    public void addProducts(List<String> productIdents)
            throws ProductSyncManagerCommandException {
        for (String productIdent : productIdents) {
            addProduct(productIdent);
        }
    }

    /**
     * Adds the product.
     * @param productIdent the product ident
     * @throws ProductSyncManagerCommandException if the product addition failed
     */
    public abstract void addProduct(String productIdent)
            throws ProductSyncManagerCommandException;

    /**
     * Refresh product, channel and subscription information without triggering
     * any reposysnc.
     * @throws ProductSyncManagerCommandException if the refresh failed
     * @throws InvalidMirrorCredentialException if mirror credentials are not valid
     * @throws ConnectionException if a connection to NCC was not possible
     */
    public abstract void refreshProducts() throws ProductSyncManagerCommandException,
            InvalidMirrorCredentialException, ConnectionException;

    /**
     * Check if SCC provider is in use.
     * @return true if provider is migrated from the NCC to SCC.
     */
    private static boolean isMigratedToSCC() {
        return new File(ContentSyncManager.SCC_MIGRATED).exists();
    }

    /**
     * Get the synchronization status for a given product.
     * @param product product
     * @return sync status as string
     */
    protected SyncStatus getProductSyncStatus(Product product) {
        // Compute statistics about channels
        int finishedCounter = 0;
        int failedCounter = 0;
        Date maxLastSyncDate = null;
        StringBuilder debugDetails = new StringBuilder();

        for (Channel c : product.getMandatoryChannels()) {
            SyncStatus channelStatus = getChannelSyncStatus(c);

            if (StringUtils.isNotBlank(channelStatus.getDetails())) {
                debugDetails.append(channelStatus.getDetails());
            }

            if (channelStatus.equals(SyncStatus.FINISHED)) {
                logger.debug("Channel finished: " + c.getLabel());
                finishedCounter++;
            }
            else if (channelStatus.equals(SyncStatus.FAILED)) {
                logger.debug("Channel failed: " + c.getLabel());
                failedCounter++;
            }
            else {
                logger.debug("Channel in progress: " + c.getLabel());
            }

            Date lastSyncDate = channelStatus.getLastSyncDate();
            if (maxLastSyncDate == null
                    || (lastSyncDate != null && lastSyncDate.after(maxLastSyncDate))) {
                maxLastSyncDate = lastSyncDate;
            }
        }

        // Set FINISHED if all mandatory channels have metadata
        if (finishedCounter == product.getMandatoryChannels().size()) {
            SyncStatus result = SyncStatus.FINISHED;
            result.setLastSyncDate(maxLastSyncDate);
            return result;
        }
        // Status is FAILED if at least one channel has failed
        else if (failedCounter > 0) {
            SyncStatus failedResult = SyncStatus.FAILED;
            failedResult.setDetails(debugDetails.toString());
            return failedResult;
        }
        // Otherwise return IN_PROGRESS
        else {
            SyncStatus status = SyncStatus.IN_PROGRESS;
            int totalChannels = product.getMandatoryChannels().size();
            status.setSyncProgress((finishedCounter * 100) / totalChannels);
            return status;
        }
    }

    /**
     * Get the synchronization status for a given channel.
     * @param channel the channel
     * @return channel sync status as string
     */
    private SyncStatus getChannelSyncStatus(Channel channel) {
        // Fall back to FAILED if no progress or success is detected
        SyncStatus channelSyncStatus = SyncStatus.FAILED;

        // Check for success: is there metadata for this channel?
        com.redhat.rhn.domain.channel.Channel c =
                ChannelFactory.lookupByLabel(channel.getLabel());

        // the XML data may say P, but if the channel is not in the database
        // we assume the XML data is wrong
        if (c == null) {
            return SyncStatus.NOT_MIRRORED;
        }

        if (ChannelManager.getRepoLastBuild(c) != null) {
            channelSyncStatus = SyncStatus.FINISHED;
            channelSyncStatus.setLastSyncDate(c.getLastSynced());
            return channelSyncStatus;
        }

        // No success (metadata), check for jobs in taskomatic
        List<TaskoRun> runs = TaskoFactory.listRunsByBunch("repo-sync-bunch");
        boolean repoSyncRunFound = false;
        Date lastRunEndTime = null;
        Long channelId = c.getId();

        // Runs are sorted by start time, recent ones first
        for (TaskoRun run : runs) {
            // Get the channel id of that run
            TaskoSchedule schedule = TaskoFactory.lookupScheduleById(run.getScheduleId());
            Long scheduleChannelId = getChannelIdForSchedule(schedule);

            if (channelId.equals(scheduleChannelId)) {
                // We found a repo-sync run for this channel
                repoSyncRunFound = true;
                lastRunEndTime = run.getEndTime();

                // Get debug information
                String debugInfo = run.getTailOfStdError(1024);
                if (debugInfo.isEmpty()) {
                    debugInfo = run.getTailOfStdOutput(1024);
                }

                // Set the status and debug info
                String runStatus = run.getStatus();
                if (logger.isDebugEnabled()) {
                    logger.debug("Repo sync run found for channel " + c +
                            ", status is: " + runStatus);
                }

                String prefix = "setupwizard.syncstatus.";
                if (runStatus.equals(TaskoRun.STATUS_FAILED) ||
                        runStatus.equals(TaskoRun.STATUS_INTERRUPTED)) {
                    // Reposync has failed or has been interrupted
                    channelSyncStatus = SyncStatus.FAILED;
                    channelSyncStatus.setMessageKey(prefix + "message.reposync.failed");
                    channelSyncStatus.setDetails(debugInfo);
                    // Don't return from here, there might be a new schedule already
                }
                else if (runStatus.equals(TaskoRun.STATUS_READY_TO_RUN) ||
                        runStatus.equals(TaskoRun.STATUS_RUNNING)) {
                    // Reposync is in progress
                    channelSyncStatus = SyncStatus.IN_PROGRESS;
                    channelSyncStatus.setMessageKey(prefix + "message.reposync.progress");
                    channelSyncStatus.setDetails(debugInfo);
                    return channelSyncStatus;
                }

                // We look at the latest run only
                break;
            }
        }

        // Check if there is a schedule that is newer than the last (FAILED) run
        if (!repoSyncRunFound || channelSyncStatus == SyncStatus.FAILED) {
            List<TaskoSchedule> schedules =
                    TaskoFactory.listRepoSyncSchedulesNewerThan(lastRunEndTime);
            for (TaskoSchedule s : schedules) {
                Long scheduleChannelId = getChannelIdForSchedule(s);
                if (channelId.equals(scheduleChannelId)) {
                    // There is a schedule for this channel
                    channelSyncStatus = SyncStatus.IN_PROGRESS;
                    return channelSyncStatus;
                }
            }

            // No schedule found, return FAILED
            return channelSyncStatus;
        }

        // Check if channel metadata generation is in progress
        if (ChannelManager.isChannelLabelInProgress(channel.getLabel())) {
            channelSyncStatus = SyncStatus.IN_PROGRESS;
            return channelSyncStatus;
        }

        // Check for queued items (merge this with the above method?)
        SelectMode selector = ModeFactory.getMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_REPOMD_CANDIDATES_DETAILS_QUERY);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("channel_label", channel.getLabel());
        if (selector.execute(params).size() > 0) {
            channelSyncStatus = SyncStatus.IN_PROGRESS;
            return channelSyncStatus;
        }

        // Otherwise return FAILED
        return channelSyncStatus;
    }

    /**
     * For a given {@link TaskoSchedule} return the id of the associated channel.
     * @param schedule a taskomatic schedule
     * @return channel ID as {@link Long} or null in case of an error
     */
    @SuppressWarnings("unchecked")
    private Long getChannelIdForSchedule(TaskoSchedule schedule) {
        Long ret = null;
        Map<String, Object> dataMap = schedule.getDataMap();
        String channelIdString = (String) dataMap.get("channel_id");
        try {
            ret = Long.parseLong(channelIdString);
        }
        catch (NumberFormatException e) {
            logger.error(e.getMessage());
        }
        return ret;
    }

    /**
     * Schedule single sat reposync.
     *
     * @param channel
     */
    public void scheduleSingleSatRepoSync(Channel channel) {
        List<String> labels = new ArrayList<String>();
        labels.add(channel.getLabel());
        @SuppressWarnings("unchecked")
        List<Long> channelIds = ChannelFactory.getChannelIds(labels);
        if (!channelIds.isEmpty()) {
            this.rpcInvoke("tasko.scheduleSingleSatRepoSync", channelIds.get(0));
        }
    }

    /**
     * Invoke an XMLRPC method from the client.
     * @param name
     * @param args
     * @return
     * @throws MalformedURLException
     */
    private Object rpcInvoke(String name, Object...args) {
        try {
            return new XmlRpcClient(ConfigDefaults.get()
                    .getTaskoServerUrl(), false)
                    .invoke(name, args);
        }
        catch (XmlRpcException e) {
            throw new TaskomaticApiException(e);
        }
        catch (MalformedURLException e) {
            throw new TaskomaticApiException(e);
        }
        catch (XmlRpcFault e) {
            throw new TaskomaticApiException(e);
        }
    }
}
