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

import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.frontend.events.ScheduleRepoSyncEvent;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.content.ContentSyncException;
import com.redhat.rhn.manager.content.ContentSyncManager;
import com.redhat.rhn.manager.content.ListedProduct;
import com.redhat.rhn.taskomatic.TaskoFactory;
import com.redhat.rhn.taskomatic.TaskoRun;
import com.redhat.rhn.taskomatic.TaskoSchedule;
import com.redhat.rhn.taskomatic.task.TaskConstants;

import com.suse.manager.model.products.Channel;
import com.suse.manager.model.products.MandatoryChannels;
import com.suse.manager.model.products.OptionalChannels;
import com.suse.manager.model.products.Product;
import com.suse.manager.model.products.Product.SyncStatus;
import com.suse.mgrsync.MgrSyncChannel;
import com.suse.mgrsync.MgrSyncStatus;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manager class for interacting with SUSE products.
 */
public class ProductSyncManager {

    /** The logger. */
    protected static Logger logger = Logger.getLogger(ProductSyncManager.class);

    /**
     * Returns a list of base products.
     * @return the products list
     * @throws ProductSyncException if an error occurred
     */
    public List<Product> getBaseProducts() throws ProductSyncException {
        ContentSyncManager csm = new ContentSyncManager();
        try {
            // Convert the listed products to objects we can display
            Collection<ListedProduct> products = csm.listProducts(csm.listChannels());
            List<Product> result = convertProducts(products);

            // Determine their product sync status separately
            for (Product p : result) {
                if (p.isProvided()) {
                    p.setSyncStatus(getProductSyncStatus(p));
                }
                else {
                    p.setStatusNotMirrored();
                }
                for (Product addon : p.getAddonProducts()) {
                    if (addon.isProvided()) {
                        addon.setSyncStatus(getProductSyncStatus(addon));
                    }
                    else {
                        addon.setStatusNotMirrored();
                    }
                }
            }
            return result;
        }
        catch (ContentSyncException e) {
            throw new ProductSyncException(e);
        }
    }

    /**
     * Adds multiple products.
     * @param productIdents the product ident list
     * @throws ProductSyncException if an error occurred
     */
    public void addProducts(List<String> productIdents) throws ProductSyncException {
        for (String productIdent : productIdents) {
            addProduct(productIdent);
        }
    }

    /**
     * Adds the product.
     * @param productIdent the product ident
     * @throws ProductSyncException if an error occurred
     */
    public void addProduct(String productIdent) throws ProductSyncException {
        Product product = findProductByIdent(productIdent);
        if (product != null) {
            try {
                // Add the channels first
                ContentSyncManager csm = new ContentSyncManager();
                for (Channel channel : product.getMandatoryChannels()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Add channel: " + channel.getLabel());
                    }
                    csm.addChannel(channel.getLabel(), null);
                }

                // Trigger sync of those channels
                for (Channel channel : product.getMandatoryChannels()) {
                    ScheduleRepoSyncEvent event =
                            new ScheduleRepoSyncEvent(channel.getLabel());
                    MessageQueue.publish(event);
                }
            }
            catch (ContentSyncException ex) {
                throw new ProductSyncException(ex.getMessage());
            }
        }
        else {
            String msg = String.format("Product %s cannot be found.", productIdent);
            throw new ProductSyncException(msg);
        }
    }

    /**
     * Get the synchronization status for a given product.
     * @param product product
     * @return sync status as string
     */
    public SyncStatus getProductSyncStatus(Product product) {
        // Compute statistics about channels
        int notMirroredCounter = 0;
        int finishedCounter = 0;
        int failedCounter = 0;
        SyncStatus syncStatus;
        Date maxLastSyncDate = null;
        StringBuilder debugDetails = new StringBuilder();

        for (Channel c : product.getMandatoryChannels()) {
            SyncStatus channelStatus = getChannelSyncStatus(c);

            if (StringUtils.isNotBlank(channelStatus.getDetails())) {
                debugDetails.append(channelStatus.getDetails());
            }

            if (channelStatus.isNotMirrored()) {
                logger.debug("Channel not mirrored: " + c.getLabel());
                notMirroredCounter++;
            }
            else if (channelStatus.isFinished()) {
                logger.debug("Channel finished: " + c.getLabel());
                finishedCounter++;
            }
            else if (channelStatus.isFailed()) {
                logger.debug("Channel failed: " + c.getLabel());
                failedCounter++;
            }
            else {
                logger.debug("Channel in progress: " + c.getLabel());
            }

            Date lastSyncDate = channelStatus.getLastSyncDate();
            if (maxLastSyncDate == null ||
                    (lastSyncDate != null && lastSyncDate.after(maxLastSyncDate))) {
                maxLastSyncDate = lastSyncDate;
            }
        }

        // Return NOT_MIRRORED if at least one mandatory channel is not mirrored
        if (notMirroredCounter > 0) {
            syncStatus = new SyncStatus(SyncStatus.SyncStage.NOT_MIRRORED);
        }
        // Set FINISHED if all mandatory channels have metadata
        else if (finishedCounter == product.getMandatoryChannels().size()) {
            syncStatus = new SyncStatus(SyncStatus.SyncStage.FINISHED);
            syncStatus.setLastSyncDate(maxLastSyncDate);
        }
        // Status is FAILED if at least one channel has failed
        else if (failedCounter > 0) {
            syncStatus = new SyncStatus(SyncStatus.SyncStage.FAILED);
            syncStatus.setDetails(debugDetails.toString());
        }
        // Otherwise return IN_PROGRESS
        else {
            syncStatus = new SyncStatus(SyncStatus.SyncStage.IN_PROGRESS);
            int totalChannels = product.getMandatoryChannels().size();
            syncStatus.setSyncProgress((finishedCounter * 100) / totalChannels);
        }

        return syncStatus;
    }

    /**
     * Get the synchronization status for a given channel.
     * @param channel the channel
     * @return channel sync status as string
     */
    private SyncStatus getChannelSyncStatus(Channel channel) {
        // Fall back to FAILED if no progress or success is detected
        SyncStatus channelSyncStatus = new SyncStatus(SyncStatus.SyncStage.FAILED);

        // Check for success: is there metadata for this channel?
        com.redhat.rhn.domain.channel.Channel c =
                ChannelFactory.lookupByLabel(channel.getLabel());

        if (c == null) {
            return new SyncStatus(SyncStatus.SyncStage.NOT_MIRRORED);
        }
        else if (ChannelManager.getRepoLastBuild(c) != null) {
            channelSyncStatus = new SyncStatus(SyncStatus.SyncStage.FINISHED);
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
                    logger.debug("Repo sync run found for channel " + c.getLabel() +
                            " (" + runStatus + ")");
                }

                String prefix = "setupwizard.syncstatus.";
                if (runStatus.equals(TaskoRun.STATUS_FAILED) ||
                        runStatus.equals(TaskoRun.STATUS_INTERRUPTED)) {
                    // Reposync has failed or has been interrupted
                    channelSyncStatus = new SyncStatus(SyncStatus.SyncStage.FAILED);
                    channelSyncStatus.setMessageKey(prefix + "message.reposync.failed");
                    channelSyncStatus.setDetails(debugInfo);
                    // Don't return from here, there might be a new schedule already
                }
                else if (runStatus.equals(TaskoRun.STATUS_READY_TO_RUN) ||
                        runStatus.equals(TaskoRun.STATUS_RUNNING)) {
                    // Reposync is in progress
                    channelSyncStatus = new SyncStatus(SyncStatus.SyncStage.IN_PROGRESS);
                    channelSyncStatus.setMessageKey(prefix + "message.reposync.progress");
                    channelSyncStatus.setDetails(debugInfo);
                    return channelSyncStatus;
                }

                // We look at the latest run only
                break;
            }
        }

        // Check if there is a schedule that is newer than the last (FAILED) run
        if (!repoSyncRunFound || channelSyncStatus.isFailed()) {
            List<TaskoSchedule> schedules =
                    TaskoFactory.listRepoSyncSchedulesNewerThan(lastRunEndTime);
            for (TaskoSchedule s : schedules) {
                Long scheduleChannelId = getChannelIdForSchedule(s);
                if (channelId.equals(scheduleChannelId)) {
                    // There is a schedule for this channel
                    channelSyncStatus = new SyncStatus(SyncStatus.SyncStage.IN_PROGRESS);
                    return channelSyncStatus;
                }
            }

            // No schedule found, return FAILED
            return channelSyncStatus;
        }

        // Check if channel metadata generation is in progress
        if (ChannelManager.isChannelLabelInProgress(channel.getLabel())) {
            channelSyncStatus = new SyncStatus(SyncStatus.SyncStage.IN_PROGRESS);
            return channelSyncStatus;
        }

        // Check for queued items (merge this with the above method?)
        SelectMode selector = ModeFactory.getMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_REPOMD_CANDIDATES_DETAILS_QUERY);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("channel_label", channel.getLabel());
        if (selector.execute(params).size() > 0) {
            channelSyncStatus = new SyncStatus(SyncStatus.SyncStage.IN_PROGRESS);
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
     * Convert a collection of {@link ListedProduct} to a collection of {@link Product}
     * for further display.
     *
     * @param products collection of {@link ListedProduct}
     * @return List of {@link Product}
     */
    private List<Product> convertProducts(Collection<ListedProduct> products) {
        List<Product> displayProducts = new ArrayList<Product>();
        for (ListedProduct p : products) {
            if (!p.getStatus().equals(MgrSyncStatus.UNAVAILABLE)) {
                Product displayProduct = convertProduct(p);
                displayProducts.add(displayProduct);
            }
        }
        return displayProducts;
    }

    /**
     * Convert a given {@link ListedProduct} to a {@link Product} for further display.
     *
     * @param productIn instance of {@link ListedProduct}
     * @return instance of {@link Product}
     */
    private Product convertProduct(final ListedProduct productIn) {
        // Sort product channels (mandatory/optional)
        List<Channel> mandatoryChannelsOut = new ArrayList<Channel>();
        List<Channel> optionalChannelsOut = new ArrayList<Channel>();
        for (MgrSyncChannel channelIn : productIn.getChannels()) {
            MgrSyncStatus statusIn = channelIn.getStatus();
            String statusOut = statusIn.equals(MgrSyncStatus.INSTALLED) ?
                    Channel.STATUS_PROVIDED : Channel.STATUS_NOT_PROVIDED;
            Channel channelOut = new Channel(channelIn.getLabel(), statusOut);
            if (channelIn.isOptional()) {
                optionalChannelsOut.add(channelOut);
            }
            else {
                mandatoryChannelsOut.add(channelOut);
            }
        }

        // Add base channel on top of everything else so it can be added first.
        Collections.sort(mandatoryChannelsOut, new Comparator<Channel>() {
            public int compare(Channel a, Channel b) {
                return a.getLabel().equals(productIn.getBaseChannel().getLabel()) ? -1 :
                        b.getLabel().equals(productIn.getBaseChannel().getLabel()) ? 1 : 0;
            }
        });

        // Setup the product that will be displayed
        Product displayProduct = new Product(productIn.getArch(), productIn.getIdent(),
                productIn.getFriendlyName(), "",
                new MandatoryChannels(mandatoryChannelsOut),
                new OptionalChannels(optionalChannelsOut));

        // Set extensions as addon products
        for (ListedProduct extension : productIn.getExtensions()) {
            Product ext = convertProduct(extension);
            ext.setBaseProduct(displayProduct);
            displayProduct.getAddonProducts().add(ext);
            ext.setBaseProductIdent(displayProduct.getIdent());
        }

        return displayProduct;
    }

    /**
     * Find a product for any given ident by looking through base and their addons.
     *
     * @param ident ident of a product
     * @return the {@link Product}
     * @throws ProductSyncException in case of an error
     */
    private Product findProductByIdent(String ident) throws ProductSyncException {
        for (Product p : getBaseProducts()) {
            if (p.getIdent().equals(ident)) {
                return p;
            }
            for (Product addon : p.getAddonProducts()) {
                if (addon.getIdent().equals(ident)) {
                    return addon;
                }
            }
        }
        return null;
    }
}
