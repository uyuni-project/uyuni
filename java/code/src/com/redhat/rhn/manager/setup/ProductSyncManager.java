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
 * Manager class for interacting with SUSE products.
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

    /** FIXME: Product sync command switch to sync a product. */
    public static final String SYNC_PRODUCT_SWITCH = "";

    /** String returned by the sync command if there is any invalid mirror credential. */
    private static final String INVALID_MIRROR_CREDENTIAL_ERROR = "HTTP error code 401";

    /** String returned by the sync command if there is any invalid mirror credential. */
    private static final String COULD_NOT_CONNECT_ERROR = "connection failed.";

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
        // Count finished and failed channels
        int finishedCounter = 0;
        int failedCounter = 0;

        // Aggregate sync status of all mandatory product channels
        for (Channel c : product.getMandatoryChannels()) {
            SyncStatus channelStatus = getChannelSyncStatus(c);
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
        }

        // Set FINISHED if all mandatory channels have metadata
        if (finishedCounter == product.getMandatoryChannels().size()) {
            return SyncStatus.FINISHED;
        }
        // Status is FAILED if at least one channel has failed
        else if (failedCounter > 0) {
            return SyncStatus.FAILED;
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
            return channelSyncStatus;
        }

        // No success (metadata), check for failed jobs in taskomatic
        List<TaskoRun> runs = TaskoFactory.listRunsByBunch("repo-sync-bunch");
        boolean repoSyncRunFound = false;

        // Runs are sorted by start time, recent ones first
        for (TaskoRun run : runs) {
            TaskoSchedule schedule = TaskoFactory.lookupScheduleById(run.getScheduleId());
            @SuppressWarnings("unchecked")
            Map<String, Object> dataMap = schedule.getDataMap();

            // Convert channel id to Long
            String channelIdString = (String) dataMap.get("channel_id");
            Long channelId;
            try {
                channelId = Long.parseLong(channelIdString);
            }
            catch (NumberFormatException e) {
                // Continue if we can't get the id, maybe there is an older job
                continue;
            }

            if (channelId.equals(c.getId())) {
                // We found a repo-sync run for this channel
                repoSyncRunFound = true;

                // Get debug information
                String debugInfo = run.getTailOfStdError(1024);
                if (debugInfo.isEmpty()) {
                    debugInfo = run.getTailOfStdOutput(1024);
                }

                // Set the correct status and additional info
                String runStatus = run.getStatus();
                String prefix = "setupwizard.syncstatus.";
                if (runStatus.equals(TaskoRun.STATUS_FAILED) ||
                        runStatus.equals(TaskoRun.STATUS_INTERRUPTED)) {
                    // Reposync has failed or has been interrupted
                    channelSyncStatus = SyncStatus.FAILED;
                    channelSyncStatus.setMessageKey(prefix + "message.reposync.failed");
                    channelSyncStatus.setDetails(debugInfo);
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
                    channelSyncStatus.setDetails(debugInfo);
                    return channelSyncStatus;
                }

                // We look only at the latest run
                break;
            }
        }

        // Assume this is a new product if there was never a repo-sync before
        if (!repoSyncRunFound) {
            channelSyncStatus = SyncStatus.IN_PROGRESS;
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
     * Schedule synchronization for a given list of products.
     * @param productIdents list of products to synchronize
     * @throws ProductSyncManagerCommandException if the product sync failed
     */
    public void syncProducts(List<String> productIdents)
            throws ProductSyncManagerCommandException {
        // FIXME: This currently just calls 'mgr-ncc-sync' without parameters!
        runProductSyncCommand(SYNC_PRODUCT_SWITCH);
    }

    /**
     * Refresh product, channel and subscription information without triggering
     * any reposysnc.
     * @throws ProductSyncManagerCommandException if the refresh failed
     * @throws InvalidMirrorCredentialException if mirror credentials are not valid
     * @throws ConnectionException if a connection to NCC was not possible
     */
    public void refreshProducts()
        throws ProductSyncManagerCommandException, InvalidMirrorCredentialException,
        ConnectionException {
        try {
            runProductSyncCommand(REFRESH_SWITCH);
        }
        catch (ProductSyncManagerCommandException e) {
            if (e.getErrorCode() == 1) {
                if (e.getCommandErrorMessage().contains(INVALID_MIRROR_CREDENTIAL_ERROR)) {
                    throw new InvalidMirrorCredentialException();
                }
                if (e.getCommandErrorMessage().contains(COULD_NOT_CONNECT_ERROR)) {
                    throw new ConnectionException();
                }
            }
            throw e;
        }
    }
}
