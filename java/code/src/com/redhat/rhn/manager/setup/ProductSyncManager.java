/*
 * Copyright (c) 2014 SUSE LLC
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

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.product.MgrSyncChannelDto;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.SetupWizardProductDto;
import com.redhat.rhn.frontend.dto.SetupWizardProductDto.SyncStatus;
import com.redhat.rhn.frontend.events.ScheduleRepoSyncEvent;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.content.ContentSyncException;
import com.redhat.rhn.manager.content.ContentSyncManager;
import com.redhat.rhn.manager.content.MgrSyncProductDto;
import com.redhat.rhn.taskomatic.TaskoFactory;
import com.redhat.rhn.taskomatic.domain.TaskoRun;
import com.redhat.rhn.taskomatic.domain.TaskoSchedule;
import com.redhat.rhn.taskomatic.task.RepoSyncTask;
import com.redhat.rhn.taskomatic.task.TaskConstants;

import com.suse.manager.model.products.Channel;
import com.suse.manager.model.products.MandatoryChannels;
import com.suse.manager.model.products.OptionalChannels;
import com.suse.mgrsync.MgrSyncStatus;
import com.suse.utils.Exceptions;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Manager class for interacting with SUSE products.
 */
public class ProductSyncManager {

    /** The logger. */
    private static final Logger LOGGER = LogManager.getLogger(ProductSyncManager.class);
    private static final LocalizationService L10NSERVICE = LocalizationService.getInstance();

    /**
     * Returns a list of base products and addons.
     * @return the products list with addons
     */
    public List<SetupWizardProductDto> getBaseAndAddonProducts() {
        ContentSyncManager csm = new ContentSyncManager();
        // Convert the listed products to objects we can display
        Collection<MgrSyncProductDto> products = csm.listProducts();
        List<SetupWizardProductDto> productsWithAddons = convertProducts(products);

        Map<String, com.redhat.rhn.domain.channel.Channel> channelByLabel = ChannelFactory.listVendorChannels()
                .stream().collect(Collectors.toMap(com.redhat.rhn.domain.channel.Channel::getLabel, c -> c));

        // Determine their product sync status separately
        for (SetupWizardProductDto p : productsWithAddons) {
            if (p.isProvided()) {
                p.setSyncStatus(getProductSyncStatus(p, channelByLabel));
            }
            else {
                p.setStatusNotMirrored();
            }
        }
        return productsWithAddons;
    }

    /**
     * Adds multiple products.
     * @param productIdents the product ident list
     * @param user the current user
     * @return a map of added products and an error message if any
     */
    public Map<String, Optional<? extends Exception>> addProducts(List<String> productIdents, User user) {
        List<SetupWizardProductDto> productsWithAddons = getBaseAndAddonProducts();
        // Extract the ids from the identifiers
        final List<Long> productsId = productIdents.stream().map(
            // Ident are in the form 'product_id-label'. If the product id is negative, the ident starts with a minus,
            // so we need to skip the first character when performing the indexOf
            id -> Long.valueOf(id.substring(0, id.indexOf('-', 1)))
        ).collect(Collectors.toList());

        // Retrieve the tree structure
        final Map<Long, Long> productTreeMap =
            HibernateFactory.doWithoutAutoFlushing(() -> getProductTreeMap(productsId));

        // Evaluate possible conflicts with existing custom channels
        final Map<Long, List<String>> conflictsMap =
            HibernateFactory.doWithoutAutoFlushing(() -> verifyChannelConflicts(productsId));

        return productIdents.stream().collect(Collectors.toMap(
            ident -> ident,
            ident -> Exceptions.handleByReturning(() -> {
                SetupWizardProductDto product = findProductByIdent(ident, productsWithAddons);
                if (product == null) {
                    throw new ProductSyncException(L10NSERVICE.getMessage("setup.product.error.notfound", ident));
                }
                addProduct(product, user, productTreeMap, conflictsMap);
            })
            ));
    }

    /**
     * Evaluates if the given product are in conflict with existing custom channels.
     * @param productsId the scc product ids of the product  to check
     * @return a map containing the internal product id and the list of channel that are in conflicting
     */
    public Map<Long, List<String>> verifyChannelConflicts(List<Long> productsId) {
        final SelectMode query = ModeFactory.getMode("Product_queries", "verify_channel_conflicts");
        final DataResult<Map<String, Object>> dataResult = query.execute(productsId);
        if (CollectionUtils.isEmpty(dataResult)) {
            return Collections.emptyMap();
        }

        return dataResult.stream().collect(Collectors.groupingBy(
                row -> (Long) row.get("id"),
                Collectors.mapping(row -> (String) row.get("label"), Collectors.toList())
        ));
    }

    /**
     * Retrieves the tree structure of the products
     * @param productsId the scc product ids of the product to extract.
     *                   Only the base product and its children will be computed.
     * @return a map specifying for each internal product id which is the internal parent id.
     */
    public Map<Long, Long> getProductTreeMap(List<Long> productsId) {
        final SelectMode query = ModeFactory.getMode("Product_queries", "extract_product_tree");
        final DataResult<Map<String, Long>> dataResult = query.execute(productsId);
        if (CollectionUtils.isEmpty(dataResult)) {
            return Collections.emptyMap();
        }

        // It's not possible to use dataResult.stream().collect() since Collectors.toMap() does not support null values
        // See: https://bugs.openjdk.java.net/browse/JDK-8148463
        final Map<Long, Long> resultMap = new HashMap<>();
        dataResult.forEach(row -> resultMap.put(row.get("product_id"), row.get("parent_id")));
        return resultMap;
    }

    /**
     * Adds the product.
     * @param product the SUSE Product as it is shown in the Setup Wizard UI
     * @param user the current user
     * @param productTreeMap the map representing the tree structure of the products
     * @param conflictsMap the map of conflicts
     * @throws ProductSyncException if an error occurred
     */
    private void addProduct(SetupWizardProductDto product, User user, Map<Long, Long> productTreeMap,
                            Map<Long, List<String>> conflictsMap) throws ProductSyncException {

        List<String> conflicts = conflictsMap.get(product.getId());
        if (conflicts != null) {
            throw new ProductSyncException(L10NSERVICE.getMessage("setup.product.error.labelconflict", conflicts));
        }
        else {
            // Evaluate parent conflicts
            Long parentId = productTreeMap.get(product.getId());
            while (parentId != null) {
                if (conflictsMap.containsKey(parentId)) {
                    throw new ProductSyncException(L10NSERVICE.getMessage("setup.product.error.parentconflict"));
                }

                parentId = productTreeMap.get(parentId);
            }
        }

        try {
            List<String> channelsToSync = new LinkedList<>();
            // Add the channels first
            ContentSyncManager csm = new ContentSyncManager();
            for (Channel channel : product.getMandatoryChannels()) {
                LOGGER.debug("Add channel: {}", channel.getLabel());
                csm.addChannel(channel.getLabel(), null);
                channelsToSync.add(channel.getLabel());
            }

            for (Channel iuc : product.getInstallerUpdateChannels()) {
                LOGGER.debug("Add installer update channel: {}", iuc.getLabel());
                csm.addChannel(iuc.getLabel(), null);
                channelsToSync.add(iuc.getLabel());
            }

            ScheduleRepoSyncEvent event = new ScheduleRepoSyncEvent(channelsToSync, user.getId());
            MessageQueue.publish(event);
        }
        catch (ContentSyncException ex) {
            throw new ProductSyncException(L10NSERVICE.getMessage("setup.product.error.unknown"), ex);
        }
    }

    /**
     * Get the synchronization status for a given product.
     * @param product product
     * @param channelByLabel map of all channels, indexed by their label
     * @return sync status as string
     */
    private SyncStatus getProductSyncStatus(SetupWizardProductDto product,
        Map<String, com.redhat.rhn.domain.channel.Channel> channelByLabel) {
        // Compute statistics about channels
        int notMirroredCounter = 0;
        int finishedCounter = 0;
        int failedCounter = 0;
        SyncStatus syncStatus;
        Date maxLastSyncDate = null;

        for (Channel c : product.getMandatoryChannels()) {
            SyncStatus channelStatus = getChannelSyncStatus(c.getLabel(), channelByLabel);

            if (channelStatus.isNotMirrored()) {
                LOGGER.debug("Channel not mirrored: {}", c.getLabel());
                notMirroredCounter++;
            }
            else if (channelStatus.isFinished()) {
                LOGGER.debug("Channel finished: {}", c.getLabel());
                finishedCounter++;
            }
            else if (channelStatus.isFailed()) {
                LOGGER.debug("Channel failed: {}", c.getLabel());
                failedCounter++;
            }
            else {
                LOGGER.debug("Channel in progress: {}", c.getLabel());
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
     * @param channelLabel the channel label
     * @param channelByLabel lookup map for channels by its label
     * @return channel sync status as string
     */
    public SyncStatus getChannelSyncStatus(String channelLabel,
            Map<String, com.redhat.rhn.domain.channel.Channel> channelByLabel) {
        // Check for success: is there metadata for this channel?
        com.redhat.rhn.domain.channel.Channel c = channelByLabel.get(channelLabel);

        if (c == null) {
            return new SyncStatus(SyncStatus.SyncStage.NOT_MIRRORED);
        }
        else if (ChannelManager.getRepoLastBuild(c) != null) {
            SyncStatus status = new SyncStatus(SyncStatus.SyncStage.FINISHED);
            status.setLastSyncDate(c.getLastSynced());
            return status;
        }

        // No success (metadata), check for jobs in taskomatic
        List<TaskoRun> runs = TaskoFactory.listRunsByBunch("repo-sync-bunch");
        boolean repoSyncRunFound = false;
        Date lastRunEndTime = null;
        Optional<SyncStatus> lastFailedStatus = Optional.empty();
        Long channelId = c.getId();

        // Runs are sorted by start time, recent ones first
        for (TaskoRun run : runs) {
            // Get the channel id of that run
            TaskoSchedule schedule = TaskoFactory.lookupScheduleById(run.getScheduleId());
            List<Long> scheduleChannelIds =
                RepoSyncTask.getChannelIds(schedule.getDataMap());

            if (scheduleChannelIds.contains(channelId)) {
                // We found a repo-sync run for this channel
                repoSyncRunFound = true;
                lastRunEndTime = run.getEndTime();

                // Set the status and debug info
                String runStatus = run.getStatus();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Repo sync run found for channel {} ({})", c.getLabel(), runStatus);
                }

                String prefix = "setupwizard.syncstatus.";
                if (runStatus.equals(TaskoRun.STATUS_FAILED) ||
                        runStatus.equals(TaskoRun.STATUS_INTERRUPTED)) {
                    // Reposync has failed or has been interrupted
                    SyncStatus status = new SyncStatus(SyncStatus.SyncStage.FAILED);
                    status.setMessageKey(prefix + "message.reposync.failed");

                    // Don't return from here, there might be a new schedule already
                    lastFailedStatus = Optional.of(status);
                }
                else if (runStatus.equals(TaskoRun.STATUS_READY_TO_RUN) ||
                        runStatus.equals(TaskoRun.STATUS_RUNNING)) {
                    // Reposync is in progress
                    SyncStatus status = new SyncStatus(SyncStatus.SyncStage.IN_PROGRESS);
                    status.setMessageKey(prefix + "message.reposync.progress");
                    return status;
                }

                // We look at the latest run only
                break;
            }
        }

        // Check if there is a schedule that is newer than the last (FAILED) run
        if (!repoSyncRunFound || lastFailedStatus.isPresent()) {
            List<TaskoSchedule> schedules = TaskoFactory.listRepoSyncSchedulesNewerThan(lastRunEndTime);
            for (TaskoSchedule s : schedules) {
                List<Long> scheduleChannelIds = RepoSyncTask.getChannelIds(s.getDataMap());
                if (scheduleChannelIds.contains(channelId)) {
                    // There is a schedule for this channel
                    return new SyncStatus(SyncStatus.SyncStage.IN_PROGRESS);
                }
            }

            // No schedule found, return FAILED
            return lastFailedStatus.orElse(new SyncStatus(SyncStatus.SyncStage.FAILED));
        }

        // Check if channel metadata generation is in progress
        if (ChannelManager.isChannelLabelInProgress(channelLabel)) {
            return new SyncStatus(SyncStatus.SyncStage.IN_PROGRESS);
        }

        // Check for queued items (merge this with the above method?)
        SelectMode selector = ModeFactory.getMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_REPOMD_CANDIDATES_DETAILS_QUERY);
        Map<String, Object> params = new HashMap<>();
        params.put("channel_label", channelLabel);
        if (!selector.execute(params).isEmpty()) {
            return new SyncStatus(SyncStatus.SyncStage.IN_PROGRESS);
        }

        // Otherwise return FAILED
        return new SyncStatus(SyncStatus.SyncStage.FAILED);
    }

    /**
     * Convert a collection of {@link MgrSyncProductDto} to a collection of
     * {@link SetupWizardProductDto} for further display.
     *
     * @param products collection of {@link MgrSyncProductDto}
     * @return List of {@link SetupWizardProductDto} with addons
     */
    private List<SetupWizardProductDto> convertProducts(
            Collection<MgrSyncProductDto> products) {
        List<SetupWizardProductDto> displayProducts =
                new ArrayList<>();
        for (MgrSyncProductDto p : products) {
            if (!p.getStatus().equals(MgrSyncStatus.UNAVAILABLE)) {
                SetupWizardProductDto displayProduct = convertProduct(p);
                displayProducts.add(displayProduct);
                for (SetupWizardProductDto addOnProduct : displayProduct.getAddonProducts()) {
                    displayProducts.add(addOnProduct);
                }
            }
        }
        return displayProducts;
    }

    /**
     * Convert a given {@link MgrSyncProductDto} to a {@link SetupWizardProductDto} for
     * further display.
     *
     * @param productIn instance of {@link MgrSyncProductDto}
     * @return instance of {@link SetupWizardProductDto}
     */
    private SetupWizardProductDto convertProduct(final MgrSyncProductDto productIn) {
        // Sort product channels (mandatory/optional)

        Map<Boolean, List<MgrSyncChannelDto>> collect = productIn.getChannels().stream()
                .collect(Collectors.partitioningBy(MgrSyncChannelDto::isMandatory));

        Function<MgrSyncChannelDto, Channel> mapping = channelIn -> {
            MgrSyncStatus statusIn = channelIn.getStatus();
            String statusOut = statusIn.equals(MgrSyncStatus.INSTALLED) ?
                    Channel.STATUS_PROVIDED : Channel.STATUS_NOT_PROVIDED;
            return new Channel(channelIn.getLabel(), statusOut, channelIn.isInstallerUpdates());
        };

        List<Channel> mandatoryChannelsOut = collect.get(true).stream()
        // Add base channel on top of everything else so it can be added first.
        .sorted((a, b) -> StringUtils.isBlank(a.getParentLabel()) ? -1 :
                StringUtils.isBlank(b.getParentLabel()) ? 1 : 0).map(mapping).collect(Collectors.toList());

        List<Channel> optionalChannelsOut = collect.get(false).stream()
                .map(mapping).collect(Collectors.toList());

        // Setup the product that will be displayed
        SetupWizardProductDto displayProduct = new SetupWizardProductDto(
            productIn.getProductId(), productIn.getId(), productIn.getArch().orElse(null), productIn.getIdent(),
            productIn.getFriendlyName(), "", new MandatoryChannels(mandatoryChannelsOut),
            new OptionalChannels(optionalChannelsOut)
        );

        // Set extensions as addon products
        for (MgrSyncProductDto extension : productIn.getExtensions()) {
            SetupWizardProductDto ext = convertProduct(extension);
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
     * @param productsWithAddons the products list with addons
     * @return the {@link SetupWizardProductDto}
     */
    private SetupWizardProductDto findProductByIdent(String ident, List<SetupWizardProductDto> productsWithAddons) {
        for (SetupWizardProductDto p : productsWithAddons) {
            if (p.getIdent().equals(ident)) {
                return p;
            }
            for (SetupWizardProductDto addon : p.getAddonProducts()) {
                if (addon.getIdent().equals(ident)) {
                    return addon;
                }
            }
        }
        return null;
    }
}
