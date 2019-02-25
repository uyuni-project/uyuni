/**
 * Copyright (c) 2015 SUSE LLC
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
package com.suse.manager.webui;

import static com.suse.manager.webui.utils.SparkApplicationHelper.setup;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withImageAdmin;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withOrgAdmin;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withProductAdmin;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUserPreferences;
import static spark.Spark.delete;
import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.head;
import static spark.Spark.notFound;
import static spark.Spark.post;

import com.suse.manager.webui.controllers.ActivationKeysController;
import com.suse.manager.webui.controllers.CVEAuditController;
import com.suse.manager.webui.controllers.DownloadController;
import com.suse.manager.webui.controllers.FormulaCatalogController;
import com.suse.manager.webui.controllers.FormulaController;
import com.suse.manager.webui.controllers.FrontendLogController;
import com.suse.manager.webui.controllers.ImageBuildController;
import com.suse.manager.webui.controllers.ImageProfileController;
import com.suse.manager.webui.controllers.ImageStoreController;
import com.suse.manager.webui.controllers.MinionController;
import com.suse.manager.webui.controllers.MinionsAPI;
import com.suse.manager.webui.controllers.NotificationMessageController;
import com.suse.manager.webui.controllers.ProductsController;
import com.suse.manager.webui.controllers.SaltSSHController;
import com.suse.manager.webui.controllers.SsmController;
import com.suse.manager.webui.controllers.StatesAPI;
import com.suse.manager.webui.controllers.SubscriptionMatchingController;
import com.suse.manager.webui.controllers.SystemsController;
import com.suse.manager.webui.controllers.TaskoTop;
import com.suse.manager.webui.controllers.VirtualGuestsController;
import com.suse.manager.webui.controllers.VirtualHostManagerController;
import com.suse.manager.webui.controllers.VirtualNetsController;
import com.suse.manager.webui.controllers.VirtualPoolsController;
import com.suse.manager.webui.controllers.VisualizationController;
import com.suse.manager.webui.controllers.contentmanagement.ContentManagementApiController;
import com.suse.manager.webui.controllers.contentmanagement.ContentManagementViewsController;
import com.suse.manager.webui.errors.NotFoundException;

import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

import spark.ModelAndView;
import spark.servlet.SparkApplication;
import spark.template.jade.JadeTemplateEngine;

/**
 * Router class defining the web UI routes.
 */
public class Router implements SparkApplication {

    /**
     * Invoked from the SparkFilter. Add routes here.
     */
    @Override
    public void init() {
        JadeTemplateEngine jade = setup();

        initNotFoundRoutes(jade);

        post("/manager/frontend-log", withUser(FrontendLogController::log));

        //CVEAudit

        get("/manager/audit/cve",
                withUserPreferences(withCsrfToken(withUser(CVEAuditController::cveAuditView))), jade);

        post("/manager/api/audit/cve", withUser(CVEAuditController::cveAudit));
        get("/manager/api/audit/cve.csv", withUser(CVEAuditController::cveAuditCSV));

        initContentManagementRoutes(jade);

        // Virtualization Routes
        initVirtualizationRoutes(jade);

        // Content Management Routes
        ContentManagementViewsController.initRoutes(jade);
        ContentManagementApiController.initRoutes();

        // Minions
        get("/manager/systems/keys",
                withUserPreferences(withCsrfToken(withUser(MinionController::list))),
                jade);
        get("/manager/systems/bootstrap",
                withCsrfToken(withOrgAdmin(MinionController::bootstrap)),
                jade);
        get("/manager/systems/cmd",
                withCsrfToken(MinionController::cmd),
                jade);
        get("/manager/systems/:id",
                MinionController::show);

        // Minions API
        post("/manager/api/systems/bootstrap", withOrgAdmin(MinionsAPI::bootstrap));
        post("/manager/api/systems/bootstrap-ssh", withOrgAdmin(MinionsAPI::bootstrapSSH));
        get("/manager/api/systems/keys", withUser(MinionsAPI::listKeys));
        post("/manager/api/systems/keys/:target/accept", withOrgAdmin(MinionsAPI::accept));
        post("/manager/api/systems/keys/:target/reject", withOrgAdmin(MinionsAPI::reject));
        post("/manager/api/systems/keys/:target/delete", withOrgAdmin(MinionsAPI::delete));

        // States
        get("/manager/systems/details/packages",
                withCsrfToken(MinionController::packageStates),
                jade);
        get("/manager/systems/details/custom",
                withCsrfToken(MinionController::minionCustomStates),
                jade);
        get("/manager/systems/details/highstate",
                withCsrfToken(withUser(MinionController::highstate)),
                jade);
        get("/manager/multiorg/details/custom",
                withCsrfToken(MinionController::orgCustomStates),
                jade);
        get("/manager/yourorg/custom",
                withCsrfToken(withUser(MinionController::yourOrgConfigChannels)),
                jade);
        get("/manager/groups/details/custom",
                withCsrfToken(withUser(MinionController::serverGroupConfigChannels)),
                jade);
        get("/manager/groups/details/highstate",
                withCsrfToken(withUser(MinionController::serverGroupHighstate)), jade);

        // SSM API
        get("/manager/systems/ssm/highstate",
                withCsrfToken(withUser(MinionController::ssmHighstate)), jade);
        get("/manager/systems/ssm/channels/bases",
                withUser(SsmController::getBaseChannels));
        post("/manager/systems/ssm/channels/allowed-changes",
                withUser(SsmController::computeAllowedChannelChanges));
        post("/manager/systems/ssm/channels",
                withUser(SsmController::changeChannels));

        // Systems API
        post("/manager/api/systems/:sid/delete", withUser(SystemsController::delete));
        get("/manager/api/systems/:sid/channels", withUser(SystemsController::getChannels));
        get("/manager/api/systems/:sid/channels-available-base",
                withUser(SystemsController::getAvailableBaseChannels));
        post("/manager/api/systems/:sid/channels", withUser(SystemsController::subscribeChannels));
        get("/manager/api/systems/:sid/channels/:channelId/accessible-children",
                withUser(SystemsController::getAccessibleChannelChildren));

        // Activation Keys API
        get("/manager/api/activation-keys/:tid/channels", withUser(ActivationKeysController::getChannels));
        get("/manager/api/activation-keys/base-channels",
                withUser(ActivationKeysController::getAccessibleBaseChannels));
        get("/manager/api/activation-keys/base-channels/:cid/child-channels",
                withUser(ActivationKeysController::getChildChannelsByBaseId));

        // States API
        post("/manager/api/states/apply", withUser(StatesAPI::apply));
        post("/manager/api/states/applyall", withUser(StatesAPI::applyHighstate));
        get("/manager/api/states/match", withUser(StatesAPI::matchStates));
        post("/manager/api/states/save", withUser(StatesAPI::saveConfigChannels));
        get("/manager/api/states/packages", StatesAPI::packages);
        post("/manager/api/states/packages/save", withUser(StatesAPI::savePackages));
        get("/manager/api/states/packages/match", StatesAPI::matchPackages);
        get("/manager/api/states/highstate", StatesAPI::showHighstate);
        get("/manager/api/states/:channelId/content", withUser(StatesAPI::stateContent));

        // Virtual Host Managers
        get("/manager/vhms",
                withUserPreferences(withCsrfToken(withOrgAdmin(VirtualHostManagerController::list))),
                jade);
        post("/manager/api/vhms/kubeconfig/validate",
                withOrgAdmin(VirtualHostManagerController::validateKubeconfig));
        post("/manager/api/vhms/create/kubernetes",
                withUser(VirtualHostManagerController::createKubernetes));
        post("/manager/api/vhms/update/kubernetes",
                withUser(VirtualHostManagerController::updateKubernetes));
        get("/manager/api/vhms/kubeconfig/:id/contexts",
                withOrgAdmin(VirtualHostManagerController::getKubeconfigContexts));
        post("/manager/api/vhms/:id/refresh",
                withOrgAdmin(VirtualHostManagerController::refresh));
        get("/manager/api/vhms/:id/nodes",
                withOrgAdmin(VirtualHostManagerController::getNodes));
        get("/manager/api/vhms/modules",
                withOrgAdmin(VirtualHostManagerController::getModules));
        get("/manager/api/vhms/module/:name/params",
                withOrgAdmin(VirtualHostManagerController::getModuleParams));
        get("/manager/api/vhms",
                withOrgAdmin(VirtualHostManagerController::get));
        get("/manager/api/vhms/:id",
                withOrgAdmin(VirtualHostManagerController::getSingle));
        post("/manager/api/vhms/create",
                withOrgAdmin(VirtualHostManagerController::create));
        post("/manager/api/vhms/update/:id",
                withOrgAdmin(VirtualHostManagerController::update));
        delete("/manager/api/vhms/delete/:id",
                withOrgAdmin(VirtualHostManagerController::delete));

        // Subscription Matching
        get("/manager/subscription-matching",
                withUserPreferences(withCsrfToken(withProductAdmin(SubscriptionMatchingController::show))),
                jade);
        get("/manager/subscription-matching/:filename",
                withProductAdmin(SubscriptionMatchingController::csv));

        // Subscription Matching API
        get("/manager/api/subscription-matching/data",
                withProductAdmin(SubscriptionMatchingController::data));
        post("/manager/api/subscription-matching/schedule-matcher-run",
                withProductAdmin(SubscriptionMatchingController::scheduleMatcherRun));
        post("/manager/api/subscription-matching/pins",
                withProductAdmin(SubscriptionMatchingController::createPin));
        post("/manager/api/subscription-matching/pins/:id/delete",
                withProductAdmin(SubscriptionMatchingController::deletePin));

        // TaskoTop
        get("/manager/admin/runtime-status",
                withUserPreferences(withCsrfToken(withOrgAdmin(TaskoTop::show))), jade);
        get("/manager/api/admin/runtime-status/data",
                withOrgAdmin(TaskoTop::data));

        // Download endpoint
        get("/manager/download/:channel/getPackage/:file",
                DownloadController::downloadPackage);
        get("/manager/download/:channel/repodata/:file",
                DownloadController::downloadMetadata);
        head("/manager/download/:channel/getPackage/:file",
                DownloadController::downloadPackage);
        head("/manager/download/:channel/repodata/:file",
                DownloadController::downloadMetadata);

        // Formula catalog
        get("/manager/formula-catalog",
                withUserPreferences(withOrgAdmin(FormulaCatalogController::list)), jade);
        get("/manager/formula-catalog/formula/:name",
                withCsrfToken(withOrgAdmin(FormulaCatalogController::details)), jade);

        // Formula catalog API
        get("/manager/api/formula-catalog/data",
                withOrgAdmin(FormulaCatalogController::data));
        get("/manager/api/formula-catalog/formula/:name/data",
                withOrgAdmin(FormulaCatalogController::detailsData));

        // Formulas
        get("/manager/groups/details/formulas",
                withCsrfToken(withUser(FormulaController::serverGroupFormulas)),
                jade);
        get("/manager/systems/details/formulas",
                withCsrfToken(withUser(FormulaController::minionFormulas)),
                jade);
        get("/manager/groups/details/formula/:formula_id",
                withCsrfToken(withUser(FormulaController::serverGroupFormula)),
                jade);
        get("/manager/systems/details/formula/:formula_id",
                withCsrfToken(FormulaController::minionFormula),
                jade);

        // Formula API
        get("/manager/api/formulas/list/:targetType/:id",
                withUser(FormulaController::listSelectedFormulas));
        get("/manager/api/formulas/form/:targetType/:id/:formula_id",
                withUser(FormulaController::formulaData));
        post("/manager/api/formulas/select",
                withUser(FormulaController::saveSelectedFormulas));
        post("/manager/api/formulas/save",
                withUser(FormulaController::saveFormula));

        // Visualization
        get("/manager/visualization/virtualization-hierarchy",
                withCsrfToken(withOrgAdmin(VisualizationController::showVirtualizationHierarchy)), jade);
        get("/manager/api/visualization/virtualization-hierarchy/data",
                withOrgAdmin(VisualizationController::virtHierarchyData));
        get("/manager/visualization/proxy-hierarchy",
                withCsrfToken(withOrgAdmin(VisualizationController::showProxyHierarchy)), jade);
        get("/manager/api/visualization/proxy-hierarchy/data",
                withOrgAdmin(VisualizationController::proxyHierarchyData));
        get("/manager/visualization/systems-with-managed-groups",
                withCsrfToken(withOrgAdmin(VisualizationController::systemsWithManagedGroups)), jade);
        get("/manager/api/visualization/systems-with-managed-groups/data",
                withOrgAdmin(VisualizationController::systemsWithManagedGroupsData));

        get("/manager/download/saltssh/pubkey", SaltSSHController::getPubKey);


        // NotificationMessages
        get("/manager/notification-messages",
                withUserPreferences(withCsrfToken(withUser(NotificationMessageController::getList))), jade);
        get("/manager/notification-messages/data-unread", withUser(NotificationMessageController::dataUnread));
        get("/manager/notification-messages/data-all", withUser(NotificationMessageController::dataAll));
        post("/manager/notification-messages/update-messages-status",
                withUser(NotificationMessageController::updateMessagesStatus));
        post("/manager/notification-messages/delete",
                withUser(NotificationMessageController::delete));
        post("/manager/notification-messages/retry-onboarding/:minionId",
                withUser(NotificationMessageController::retryOnboarding));
        post("/manager/notification-messages/retry-reposync/:channelId",
                withUser(NotificationMessageController::retryReposync));

        // SUSE Products
        get("/manager/admin/setup/products",
                withUserPreferences(withCsrfToken(withOrgAdmin(ProductsController::show))), jade);
        get("/manager/api/admin/products", withUser(ProductsController::data));
        post("/manager/api/admin/mandatoryChannels", withUser(ProductsController::getMandatoryChannels));
        post("/manager/admin/setup/products",
                withProductAdmin(ProductsController::addProduct));
        post("/manager/admin/setup/sync/products",
                withProductAdmin(ProductsController::synchronizeProducts));
        post("/manager/admin/setup/sync/channelfamilies",
                withProductAdmin(ProductsController::synchronizeChannelFamilies));
        post("/manager/admin/setup/sync/subscriptions",
                withProductAdmin(ProductsController::synchronizeSubscriptions));
        post("/manager/admin/setup/sync/repositories",
                withProductAdmin(ProductsController::synchronizeRepositories));
    }

    private void  initNotFoundRoutes(JadeTemplateEngine jade) {
        notFound((request, response) -> {
            Map<String, Object> data = new HashMap<>();
            data.put("currentUrl", request.pathInfo());
            return jade.render(new ModelAndView(data, "templates/errors/404.jade"));
        });

        exception(NotFoundException.class, (exception, request, response) -> {
            response.status(HttpStatus.SC_NOT_FOUND);
            Map<String, Object> data = new HashMap<>();
            data.put("currentUrl", request.pathInfo());
            response.body(jade.render(new ModelAndView(data, "templates/errors/404.jade")));
        });
    }

    private void initVirtualizationRoutes(JadeTemplateEngine jade) {
        VirtualGuestsController.initRoutes(jade);
        VirtualNetsController.initRoutes(jade);
        VirtualPoolsController.initRoutes(jade);
    }

    private void initContentManagementRoutes(JadeTemplateEngine jade) {
        get("/manager/cm/imagestores",
                withUserPreferences(withCsrfToken(withUser(ImageStoreController::listView))), jade);
        get("/manager/cm/imagestores/create",
                withCsrfToken(withImageAdmin(ImageStoreController::createView)), jade);
        get("/manager/cm/imagestores/edit/:id",
                withCsrfToken(withImageAdmin(ImageStoreController::updateView)), jade);

        get("/manager/api/cm/imagestores", withUser(ImageStoreController::list));
        get("/manager/api/cm/imagestores/type/:type",
                withUser(ImageStoreController::listAllWithType));
        get("/manager/api/cm/imagestores/:id", withUser(ImageStoreController::getSingle));
        get("/manager/api/cm/imagestores/find/:label",
                withUser(ImageStoreController::getSingleByLabel));
        post("/manager/api/cm/imagestores/create",
                withImageAdmin(ImageStoreController::create));
        post("/manager/api/cm/imagestores/update/:id",
                withImageAdmin(ImageStoreController::update));
        post("/manager/api/cm/imagestores/delete",
                withImageAdmin(ImageStoreController::delete));

        get("/manager/cm/imageprofiles",
                withUserPreferences(withCsrfToken(withUser(ImageProfileController::listView))), jade);
        get("/manager/cm/imageprofiles/create",
                withCsrfToken(withImageAdmin(ImageProfileController::createView)), jade);
        get("/manager/cm/imageprofiles/edit/:id",
                withCsrfToken(withImageAdmin(ImageProfileController::updateView)), jade);

        get("/manager/api/cm/imageprofiles", withUser(ImageProfileController::list));
        get("/manager/api/cm/imageprofiles/:id",
                withUser(ImageProfileController::getSingle));
        get("/manager/api/cm/imageprofiles/find/:label",
                withUser(ImageProfileController::getSingleByLabel));
        get("/manager/api/cm/imageprofiles/channels/:token",
                withUser(ImageProfileController::getChannels));
        post("/manager/api/cm/imageprofiles/create",
                withImageAdmin(ImageProfileController::create));
        post("/manager/api/cm/imageprofiles/update/:id",
                withImageAdmin(ImageProfileController::update));
        post("/manager/api/cm/imageprofiles/delete",
                withImageAdmin(ImageProfileController::delete));

        get("/manager/cm/build", withCsrfToken(withUser(ImageBuildController::buildView)),
                jade);
        get("/manager/cm/import", withCsrfToken(withUser(ImageBuildController::importView)),
                jade);
        get("/manager/cm/rebuild/:id",
                withCsrfToken(withUser(ImageBuildController::rebuild)), jade);

        get("/manager/api/cm/build/hosts/:type", withUser(ImageBuildController::getBuildHosts));
        post("/manager/api/cm/build/:id", withImageAdmin(ImageBuildController::build));

        get("/manager/cm/images", withUserPreferences(withCsrfToken(withUser(ImageBuildController::listView))),
                jade);

        get("/manager/api/cm/images", withUser(ImageBuildController::list));
        get("/manager/api/cm/images/:id", withUser(ImageBuildController::get));
        get("/manager/api/cm/clusters", withUser(ImageBuildController::getClusterList));
        get("/manager/api/cm/runtime/:clusterId",
                withUser(ImageBuildController::getRuntimeSummaryAll));
        get("/manager/api/cm/runtime/:clusterId/:id",
                withUser(ImageBuildController::getRuntimeSummary));
        get("/manager/api/cm/runtime/details/:clusterId/:id",
                withUser(ImageBuildController::getRuntimeDetails));
        get("/manager/api/cm/images/patches/:id",
                withUser(ImageBuildController::getPatches));
        get("/manager/api/cm/images/packages/:id",
                withUser(ImageBuildController::getPackages));
        post("/manager/api/cm/images/inspect/:id",
                withImageAdmin(ImageBuildController::inspect));
        post("/manager/api/cm/images/delete", withImageAdmin(ImageBuildController::delete));
        post("/manager/api/cm/images/import",
                withImageAdmin(ImageBuildController::importImage));
        get("/manager/api/cm/activationkeys",
                withUser(ImageProfileController::getActivationKeys));
    }

}
