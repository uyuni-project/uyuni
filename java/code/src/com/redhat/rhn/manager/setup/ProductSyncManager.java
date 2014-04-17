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
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.satellite.Executor;
import com.redhat.rhn.manager.satellite.SystemCommandExecutor;
import com.redhat.rhn.taskomatic.TaskoFactory;
import com.redhat.rhn.taskomatic.TaskoRun;
import com.redhat.rhn.taskomatic.TaskoSchedule;
import com.redhat.rhn.taskomatic.task.TaskConstants;

import com.suse.manager.model.products.Channel;
import com.suse.manager.model.products.Product;
import com.suse.manager.model.products.Product.SyncStatus;
import com.suse.manager.model.products.ProductList;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.simpleframework.xml.core.Persister;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Simple command class for interacting (listing/adding) SUSE products.
 */
public class ProductSyncManager {

    /** Product sync command command line. */
    public static final String[] PRODUCT_SYNC_COMMAND = {
        "/usr/bin/sudo",
        "/usr/sbin/mgr-ncc-sync"
    };

    /** Product sync command switch to obtain a list of products. */
    public static final String LIST_PRODUCT_SWITCH = "--list-products-xml";

    /** Product sync command switch add a product. */
    public static final String ADD_PRODUCT_SWITCH = "--add-product-by-ident";

    /** String returned by the sync command if there is any invalid mirror credential. */
    private static final String INVALID_MIRROR_CREDENTIAL_ERROR = "HTTP error code 401";

    /**
     * Product sync command switch to refresh product, channel and subscription
     * information without triggering any reposync.
     */
    public static final String REFRESH_SWITCH = "--refresh";

    /** The logger. */
    private static Logger logger = Logger.getLogger(ProductSyncManager.class);

    /** The executor. */
    private Executor executor;

    /**
     * Default constructor.
     */
    public ProductSyncManager() {
        this(new SystemCommandExecutor());
    }

    /**
     * Executor constructor, use directly for tests.
     * @param executorIn the executor in
     */
    public ProductSyncManager(Executor executorIn) {
        executor = executorIn;
    }

    /**
     * Returns a list of base products.
     * @return the products list
     * @throws ProductSyncManagerCommandException if external commands or parsing fail
     * @throws ProductSyncManagerParseException if a parsing problem shows up
     */
    public List<Product> getBaseProducts()
        throws ProductSyncManagerCommandException, ProductSyncManagerParseException {
        return parseBaseProducts(readProducts());
    }

    /**
     * Invoke external commands which list all the available SUSE products.
     * @return a String containing the XML description of the SUSE products
     * @throws ProductSyncManagerCommandException if external commands fail
     */
    public String readProducts() throws ProductSyncManagerCommandException {
        return runProductSyncCommand(LIST_PRODUCT_SWITCH);
    }

    /**
     * Run product sync command.
     * @param arguments the arguments
     * @return the string
     * @throws ProductSyncManagerCommandException the product sync manager exception
     */
    public String runProductSyncCommand(String... arguments)
        throws ProductSyncManagerCommandException {
        String[] commandLine =
                (String[]) ArrayUtils.addAll(PRODUCT_SYNC_COMMAND, arguments);
        int exitCode = executor.execute(commandLine);
        String output = executor.getLastCommandOutput();
        String errorMessage = executor.getLastCommandErrorMessage();
        if (exitCode != 0) {
            String message = "Error while running product sync command: " +
                    ArrayUtils.toString(commandLine);
            throw new ProductSyncManagerCommandException(message, exitCode, output,
                    errorMessage);
        }
        if (logger.isTraceEnabled()) {
            logger.trace("This the output of product sync command:");
            logger.trace(output);
        }

        return output;
    }

    /**
     * Returns a list of base products from an XML string.
     * @param xml a String containing an XML description of SUSE products
     * @return list of parsed base products
     * @throws ProductSyncManagerParseException if the xml cannot be parsed
     */
    public List<Product> parseBaseProducts(String xml)
        throws ProductSyncManagerParseException {
        List<Product> result = new LinkedList<Product>();
        Set<Product> products = parsePlainProducts(xml);

        // associates ident codes to parsed product objects
        Map<String, Product> identProductMap = new HashMap<String, Product>();
        for (Product product : products) {
            identProductMap.put(product.getIdent(), product);
        }

        for (Product product : products) {
            if (product.isBase()) {
                result.add(product);
            }
            else {
                Product parent = identProductMap.get(product.getBaseProductIdent());
                product.setBaseProduct(parent);
                parent.getAddonProducts().add(product);
            }

            // If status is "P", get a more detailed status
            if (product.isProvided()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Product is provided: " + product.getName());
                }
                product.setSyncStatus(getProductSyncStatus(product));
            }
            else {
                product.setSyncStatus(SyncStatus.NOT_MIRRORED);
            }
        }

        return result;
    }

    /**
     * Get the synchronization status for a given product.
     * @param product product
     * @return sync status as string
     */
    private SyncStatus getProductSyncStatus(Product product) {
        // Fall back to in progress (mgr-ncc-sync ".")
        SyncStatus productSyncStatus = SyncStatus.IN_PROGRESS;

        // Count failed and finished channels
        int failedCounter = 0;
        int finishedCounter = 0;

        // Aggregate status of the single channels
        for (Channel c : product.getMandatoryChannels()) {
            SyncStatus channelStatus = getChannelSyncStatus(c);
            if (channelStatus.equals(SyncStatus.FAILED)) {
                logger.debug("Channel failed: " + c.getLabel());
                failedCounter++;
            }
            else if (channelStatus.equals(SyncStatus.FINISHED)) {
                logger.debug("Channel finished: " + c.getLabel());
                finishedCounter++;
            }
            else {
                logger.debug("Channel in progress: " + c.getLabel());
            }
        }

        // Failed if at least one channel failed
        if (failedCounter > 0) {
            productSyncStatus = SyncStatus.FAILED;
        }
        // Finished if all mandatory channels have metadata
        else if (finishedCounter == product.getMandatoryChannels().size()) {
            productSyncStatus = SyncStatus.FINISHED;
        }

        return productSyncStatus;
    }

    /**
     * Get sync status for a given Channel.
     * @param channel the channel
     * @return channel sync status as string
     */
    private SyncStatus getChannelSyncStatus(Channel channel) {
        // Fall back to FAILED if no progress can be detected
        SyncStatus channelSyncStatus = SyncStatus.FAILED;

        // Check if there is metadata for this channel
        com.redhat.rhn.domain.channel.Channel c =
                ChannelFactory.lookupByLabel(channel.getLabel());
        if (ChannelManager.getRepoLastBuild(c) != null) {
            return SyncStatus.FINISHED;
        }

        // No metadata, check for failed download jobs in taskomatic
        List<TaskoRun> runningRuns = TaskoFactory.listRunsByBunch("repo-sync-bunch");

        // They are sorted so that by start time, recent ones first
        for (TaskoRun run : runningRuns) {
            TaskoSchedule schedule = TaskoFactory.lookupScheduleById(run.getScheduleId());
            @SuppressWarnings("unchecked")
            Map<String, Object> dataMap = schedule.getDataMap();

            // Convert channel id to a long
            String channelIdString = (String) dataMap.get("channel_id");
            Long channelId;
            try {
                channelId = Long.parseLong(channelIdString);
            } catch (NumberFormatException e) {
                // If we can't get the id, continue, may be there is an older job
                // with good metadata
                continue;
            }

            if (channelId.equals(c.getId())) {
                // We found the latest run, get debug information
                String log = run.getTailOfStdError(1024);
                if (log.isEmpty()) {
                    log = run.getTailOfStdOutput(1024);
                }

                // Set the correct status and additional info
                String runStatus = run.getStatus();
                String prefix = "setupwizard.syncstatus.";
                if (runStatus.equals(TaskoRun.STATUS_FAILED) ||
                        runStatus.equals(TaskoRun.STATUS_INTERRUPTED)) {
                    // Reposync failed or interrupted
                    channelSyncStatus = SyncStatus.FAILED;
                    channelSyncStatus.setMessageKey(prefix + "message.reposync.failed");
                    channelSyncStatus.setDetails(log);
                    return channelSyncStatus;
                }
                else if (runStatus.equals(TaskoRun.STATUS_READY_TO_RUN) ||
                        runStatus.equals(TaskoRun.STATUS_RUNNING)) {
                    // Reposync is in progress
                    if (logger.isDebugEnabled()) {
                        logger.debug("Repo sync run found for channel " + c +
                                ", status is: " + run.getStatus());
                    }
                    channelSyncStatus = SyncStatus.IN_PROGRESS;
                    channelSyncStatus.setMessageKey(prefix + "message.reposync.progress");
                    channelSyncStatus.setDetails(log);
                    return channelSyncStatus;
                }
                // Might have been successful and now waiting for metadata generation
                break;
            }
        }

        // Check if the channel is in the metadata generation queue as in-progress
        if (ChannelManager.isChannelLabelInProgress(channel.getLabel())) {
            return SyncStatus.IN_PROGRESS;
        }

        // Check for queued items, merge this with the above method?
        SelectMode selector = ModeFactory.getMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_REPOMD_CANDIDATES_DETAILS_QUERY);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("channel_label", channel.getLabel());
        if (selector.execute(params).size() > 0) {
            return SyncStatus.IN_PROGRESS;
        }

        // Otherwise return FAILED
        return channelSyncStatus;
    }

    /**
     * Parses an XML string into an ordered Set of products, does not handle
     * base/addon relationships.
     * @param xml the xml
     * @return the product set
     * @throws ProductSyncManagerParseException the product sync manager exception
     */
    private Set<Product> parsePlainProducts(String xml)
        throws ProductSyncManagerParseException {
        try {
            ProductList result =
                    new Persister().read(ProductList.class, IOUtils.toInputStream(xml));
            TreeSet<Product> products = new TreeSet<Product>(result.getProducts());
            return products;
        }
        catch (Exception e) {
            throw new ProductSyncManagerParseException(e);
        }
    }

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
    public void addProduct(String productIdent) throws ProductSyncManagerCommandException {
        runProductSyncCommand(ADD_PRODUCT_SWITCH, productIdent);
    }

    /**
     * Refresh product, channel and subscription information without triggering
     * any reposysnc.
     * @throws ProductSyncManagerCommandException if the refresh failed
     * @throws InvalidMirrorCredentialException if mirror credentials are not valid
     */
    public void refreshProducts()
        throws ProductSyncManagerCommandException, InvalidMirrorCredentialException {
        try {
            runProductSyncCommand(REFRESH_SWITCH);
        }
        catch (ProductSyncManagerCommandException e) {
            if (e.getErrorCode() == 1 &&
                e.getCommandErrorMessage().contains(INVALID_MIRROR_CREDENTIAL_ERROR)) {
                throw new InvalidMirrorCredentialException();
            }
            else {
                throw e;
            }
        }
    }
}
