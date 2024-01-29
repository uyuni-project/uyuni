/*
 * Copyright (c) 2018 SUSE LLC
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

package com.suse.manager.webui.controllers;

import static com.redhat.rhn.common.hibernate.HibernateFactory.doWithoutAutoFlushing;
import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.result;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withOrgAdmin;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withProductAdmin;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUserPreferences;
import static spark.Spark.get;
import static spark.Spark.post;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.util.FileLocks;
import com.redhat.rhn.common.util.TimeUtils;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.iss.IssFactory;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.content.ContentSyncException;
import com.redhat.rhn.manager.content.ContentSyncManager;
import com.redhat.rhn.manager.content.MgrSyncProductDto;
import com.redhat.rhn.manager.setup.ProductSyncManager;
import com.redhat.rhn.taskomatic.TaskoFactory;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;
import com.redhat.rhn.taskomatic.domain.TaskoRun;

import com.suse.manager.model.products.ChannelJson;
import com.suse.manager.model.products.Extension;
import com.suse.manager.model.products.Product;
import com.suse.manager.webui.utils.gson.ProductsPageMetadataJson;
import com.suse.manager.webui.utils.gson.ResultJson;
import com.suse.manager.webui.utils.gson.SUSEProductsJson;
import com.suse.utils.Json;

import com.google.gson.reflect.TypeToken;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.jade.JadeTemplateEngine;

/**
 * Controller class providing backend code for the Products
 */
public class ProductsController {

    private static Logger log = LogManager.getLogger(ProductsController.class);

    private ProductsController() { }

    /**
     * Invoked from Router. Initialize routes for Systems Views.
     *
     * @param jade the Jade engine to use to render the pages
     */
    public static void initRoutes(JadeTemplateEngine jade) {
        get("/manager/admin/setup/products",
                withUserPreferences(withCsrfToken(withOrgAdmin(ProductsController::show))), jade);
        get("/manager/api/admin/products", withUser(ProductsController::data));
        get("/manager/api/admin/products/metadata", withUser(ProductsController::getMetadata));
        post("/manager/api/admin/mandatoryChannels", withUser(ProductsController::getMandatoryChannels));
        post("/manager/admin/setup/products",
                withProductAdmin(ProductsController::addProduct));
        post("/manager/admin/setup/channels/optional",
                withProductAdmin(ProductsController::addOptionalChannels));
        post("/manager/admin/setup/sync/products",
                withProductAdmin(ProductsController::synchronizeProducts));
        post("/manager/admin/setup/sync/channelfamilies",
                withProductAdmin(ProductsController::synchronizeChannelFamilies));
        post("/manager/admin/setup/sync/subscriptions",
                withProductAdmin(ProductsController::synchronizeSubscriptions));
        post("/manager/admin/setup/sync/repositories",
                withProductAdmin(ProductsController::synchronizeRepositories));
    }

    /**
     * Displays the Products page
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView show(Request request, Response response, User user) {
        return new ModelAndView(getMetadataJson().toMap(), "templates/products/show.jade");
    }

    /**
     * Retrieves the metadata of the products page
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return a JSON object containing the metadata
     */
    public static String getMetadata(Request request, Response response, User user) {
        return json(response, getMetadataJson(), new TypeToken<>() { });
    }

    private static ProductsPageMetadataJson getMetadataJson() {
        TaskoRun latestRun = TaskoFactory.getLatestRun("mgr-sync-refresh-bunch");
        ContentSyncManager csm = new ContentSyncManager();

        return new ProductsPageMetadataJson(
                IssFactory.getCurrentMaster() == null,
                csm.isRefreshNeeded(null),
                latestRun != null && latestRun.getEndTime() == null,
                FileLocks.SCC_REFRESH_LOCK.isLocked(),
                !(ConfigDefaults.get().isUyuni() ||
                csm.hasToolsChannelSubscription() || csm.canSyncToolsChannelViaCloudRMT())
        );
    }

    /**
     * Trigger a synchronization of Products
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return a JSON flag of the success/failed result
     */
    public static String synchronizeProducts(Request request, Response response, User user) {
        return FileLocks.SCC_REFRESH_LOCK.withFileLock(() -> {
            try {
                ContentSyncManager csm = new ContentSyncManager();
                csm.updateSUSEProducts(csm.getProducts());
                return json(response, true);
            }
            catch (Exception e) {
                log.fatal(e.getMessage(), e);
                return json(response, false);
            }
        });
    }

    /**
     * Trigger a synchronization of Channel Families
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return a JSON flag of the success/failed result
     */
    public static String synchronizeChannelFamilies(Request request, Response response, User user) {
        return FileLocks.SCC_REFRESH_LOCK.withFileLock(() -> {
            try {
                ContentSyncManager csm = new ContentSyncManager();
                csm.updateChannelFamilies(csm.readChannelFamilies());
                return json(response, true);
            }
            catch (Exception e) {
                log.fatal(e.getMessage(), e);
                return json(response, false);
            }
        });
    }

    /**
     * Trigger a synchronization of Repositories
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return a JSON flag of the success/failed result
     */
    public static String synchronizeRepositories(Request request, Response response, User user) {
        return FileLocks.SCC_REFRESH_LOCK.withFileLock(() -> {
            try {
                ContentSyncManager csm = new ContentSyncManager();
                csm.updateRepositories(null);
                return json(response, true);
            }
            catch (Exception e) {
                log.fatal(e.getMessage(), e);
                return json(response, false);
            }
        });
    }

    /**
     * Trigger a synchronization of Subscriptions
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return a JSON flag of the success/failed result
     */
    public static String synchronizeSubscriptions(Request request, Response response, User user) {
        return FileLocks.SCC_REFRESH_LOCK.withFileLock(() -> {
            try {
                ContentSyncManager csm = new ContentSyncManager();
                csm.updateSubscriptions();
                return json(response, true);
            }
            catch (Exception e) {
                log.fatal(e.getMessage(), e);
                return json(response, false);
            }
        });
    }

    /**
     * Add products to be mirrored in the SUSE Manager Server
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return the map of requested products with a success/failed flag in a JSON format
     */
    public static String addProduct(Request request, Response response, User user) {
        List<String> identifiers = Json.GSON.fromJson(request.body(), new TypeToken<List<String>>() { }.getType());
        ContentSyncManager csm = new ContentSyncManager();
        if (csm.isRefreshNeeded(null)) {
            log.fatal("addProduct failed: Product Data refresh needed");
            return json(response, identifiers.stream().collect(Collectors.toMap(
                Function.identity(),
                ident -> LocalizationService.getInstance().getMessage("setup.product.error.dataneedsrefresh")
            )), new TypeToken<>() { });
        }

        log.debug("Add/Sync products: {}", identifiers);

        ProductSyncManager psm = new ProductSyncManager();
        Map<String, Optional<? extends Exception>> productStatusMap = psm.addProducts(identifiers, user);

        // Convert to a map specifying operation result for each product while logging the errors that have happened
        Map<String, String> resultMap = new HashMap<>();
        productStatusMap.forEach((product, error) -> {
            error.ifPresent(ex -> log.fatal("addProduct() failed for {}", product, ex));
            resultMap.put(product, error.map(Throwable::getMessage).orElse(null));
        });

        return json(response, resultMap, new TypeToken<>() { });
    }

    /**
     * Add optional channels to be synced in the SUSE Manager Server
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return the map of requested channels with a success/failed flag in a JSON format
     */
    public static String addOptionalChannels(Request request, Response response, User user) {
        List<String> channels = Json.GSON.fromJson(request.body(), new TypeToken<List<String>>() { }.getType());
        ContentSyncManager csm = new ContentSyncManager();
        if (csm.isRefreshNeeded(null)) {
            log.fatal("addOptionalChannels failed: Product Data refresh needed");
            return json(response, channels.stream().collect(Collectors.toMap(
                    Function.identity(),
                    ident -> LocalizationService.getInstance().getMessage("setup.product.error.dataneedsrefresh")
            )), new TypeToken<>() { });
        }

        log.debug("Add/Sync channels: {}", channels);

        List<Channel> channelsToSync = new ArrayList<>();
        Map<String, String> resultMap = new HashMap<>();
        for (String channel : channels) {
            try {
                csm.addChannel(channel, null);
                channelsToSync.add(ChannelManager.lookupByLabelAndUser(channel, user));
            }
            catch (ContentSyncException | LookupException e) {
                log.error("addChannel() failed for {}", channel, e);
                resultMap.put(channel, e.getMessage());
            }
        }

        try {
            TaskomaticApi tapi = new TaskomaticApi();
            tapi.scheduleSingleRepoSync(channelsToSync);
        }
        catch (TaskomaticApiException e) {
            log.error(e.getMessage());
        }

        return json(response, resultMap, new TypeToken<>() { });
    }

    /**
     * Return Mandatory Channels
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return the collection of Mandatory Channels in a JSON format
     */
    public static String getMandatoryChannels(Request request, Response response, User user) {
        return doWithoutAutoFlushing(() -> {
            List<Long> channelIdList = Json.GSON.fromJson(request.body(), new TypeToken<List<Long>>() { }.getType());
            Map<Long, List<Long>> result = channelIdList.stream().collect(Collectors.toMap(
                    channelId -> channelId,
                    channelId -> {
                        Stream<Channel> channels = Stream.empty();
                        try {
                            channels = SUSEProductFactory.findSyncedMandatoryChannels(
                                    ChannelFactory.lookupById(channelId).getLabel());
                        }
                        catch (NoSuchElementException e) {
                            log.error("Fail to load mandatory channels for channel {}", channelId, e);
                        }
                        return channels.map(Channel::getId).collect(Collectors.toList());
                    }
            ));
            return result(response, ResultJson.success(result), new TypeToken<>() { });
        });
    }

    /**
     * Returns JSON data for the SUSE products
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return JSON result of the API call
     */
    public static String data(Request request, Response response, User user) {
        try {
            ContentSyncManager csm = new ContentSyncManager();
            ProductSyncManager psm = new ProductSyncManager();
            Collection<MgrSyncProductDto> products = csm.listProducts();

            Map<String, Channel> channelByLabel = ChannelFactory.listVendorChannels()
                    .stream().collect(Collectors.toMap(Channel::getLabel, c -> c));


            List<Product> jsonProducts = TimeUtils.logTime(log, "build ui tree",
                    () -> products.stream().map(syncProduct -> {
                SUSEProduct rootProduct = doWithoutAutoFlushing(
                        () -> SUSEProductFactory.lookupByProductId(syncProduct.getProductId()));
                HashSet<Extension> rootExtensions = new HashSet<>();

                Map<Long, Extension> extensionByProductId =
                            syncProduct.getExtensions().stream()
                                    .collect(Collectors.toMap(MgrSyncProductDto::getProductId, s -> new Extension(
                                            s.getProductId(),
                                            s.getIdent(),
                                            s.getFriendlyName(),
                                            s.getArch().orElse(null),
                                            s.isRecommended(),
                                            s.getStatus(),
                                            s.getChannels().stream().map(c ->
                                                    new ChannelJson(
                                                            Optional.ofNullable(channelByLabel.get(c.getLabel()))
                                                                    .map(Channel::getId).orElse(-1L),
                                                            c.getName(),
                                                            c.getLabel(),
                                                            c.getSummary(),
                                                            !c.isMandatory(),
                                                            psm.getChannelSyncStatus(c.getLabel(), channelByLabel)
                                                                    .getStage()
                                                    )
                                            ).collect(Collectors.toSet()),
                                            new HashSet<>()
                                    )));

                    // recreate the extension tree from our flat representation
                    for (MgrSyncProductDto ext : syncProduct.getExtensions()) {
                        long extProductId = ext.getProductId();
                        SUSEProduct extProduct =
                                        doWithoutAutoFlushing(() -> SUSEProductFactory
                                                .lookupByProductId(extProductId));
                        List<SUSEProduct> allBaseProductsOf =
                                doWithoutAutoFlushing(() -> SUSEProductFactory
                                        .findAllBaseProductsOf(extProduct, rootProduct));
                        if (allBaseProductsOf.isEmpty()) {
                            rootExtensions.add(extensionByProductId.get(extProductId));
                        }
                            for (SUSEProduct baseProduct : allBaseProductsOf) {
                                if (baseProduct.getProductId() == rootProduct.getProductId()) {
                                    rootExtensions.add(extensionByProductId.get(extProductId));
                                }
                                else {
                                    Optional.ofNullable(extensionByProductId.get(baseProduct.getProductId()))
                                        .ifPresent(e -> e.getExtensions().add(extensionByProductId.get(extProductId)));
                                }
                            }
                    }

                return new Product(
                        syncProduct.getProductId(),
                        syncProduct.getIdent(),
                        syncProduct.getFriendlyName(),
                        syncProduct.getArch().orElse(null),
                        syncProduct.isRecommended(),
                        syncProduct.getStatus(),
                        rootExtensions,
                        syncProduct.getChannels().stream().map(c ->
                                new ChannelJson(
                                        Optional.ofNullable(channelByLabel.get(c.getLabel()))
                                                .map(Channel::getId).orElse(-1L),
                                        c.getName(),
                                        c.getLabel(),
                                        c.getSummary(),
                                        !c.isMandatory(),
                                        psm.getChannelSyncStatus(c.getLabel(), channelByLabel).getStage()
                                )
                        ).collect(Collectors.toSet()));

            }).collect(Collectors.toList()));
            return json(response, new SUSEProductsJson(jsonProducts, null), new TypeToken<>() { });
        }
        catch (Exception e) {
            log.error("Exception while rendering products: {}", e.getMessage());
            return json(response, new SUSEProductsJson(null, "Exception while fetching products: " + e.getMessage()),
                    new TypeToken<>() { });
        }

    }

}
