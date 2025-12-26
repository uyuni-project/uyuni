/*
 * Copyright (c) 2015--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.webui;

import static com.suse.manager.webui.utils.SparkApplicationHelper.isApiRequest;
import static com.suse.manager.webui.utils.SparkApplicationHelper.isJson;
import static com.suse.manager.webui.utils.SparkApplicationHelper.message;
import static com.suse.manager.webui.utils.SparkApplicationHelper.setup;
import static com.suse.manager.webui.utils.SparkApplicationHelper.setupHibernateSessionFilter;
import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.notFound;

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.common.RhnRuntimeException;
import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.manager.content.ContentSyncManager;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;

import com.suse.cloud.CloudPaygManager;
import com.suse.manager.api.HttpApiRegistry;
import com.suse.manager.attestation.AttestationManager;
import com.suse.manager.hub.HubController;
import com.suse.manager.hub.HubManager;
import com.suse.manager.hub.migration.IssMigratorFactory;
import com.suse.manager.kubernetes.KubernetesManager;
import com.suse.manager.model.products.migration.MigrationDataFactory;
import com.suse.manager.utils.SaltKeyUtils;
import com.suse.manager.webui.controllers.AccessGroupController;
import com.suse.manager.webui.controllers.AnsibleController;
import com.suse.manager.webui.controllers.CSVDownloadController;
import com.suse.manager.webui.controllers.CVEAuditController;
import com.suse.manager.webui.controllers.ConfidentialComputingController;
import com.suse.manager.webui.controllers.DownloadController;
import com.suse.manager.webui.controllers.FormulaCatalogController;
import com.suse.manager.webui.controllers.FormulaController;
import com.suse.manager.webui.controllers.FrontendLogController;
import com.suse.manager.webui.controllers.ImageBuildController;
import com.suse.manager.webui.controllers.ImageProfileController;
import com.suse.manager.webui.controllers.ImageStoreController;
import com.suse.manager.webui.controllers.ImageUploadController;
import com.suse.manager.webui.controllers.MinionController;
import com.suse.manager.webui.controllers.MinionsAPI;
import com.suse.manager.webui.controllers.NotificationMessageController;
import com.suse.manager.webui.controllers.PackageController;
import com.suse.manager.webui.controllers.ProductsController;
import com.suse.manager.webui.controllers.ProxyConfigurationController;
import com.suse.manager.webui.controllers.ProxyController;
import com.suse.manager.webui.controllers.RecurringActionController;
import com.suse.manager.webui.controllers.SSOController;
import com.suse.manager.webui.controllers.SaltSSHController;
import com.suse.manager.webui.controllers.SaltbootController;
import com.suse.manager.webui.controllers.SetController;
import com.suse.manager.webui.controllers.SsmController;
import com.suse.manager.webui.controllers.StatesAPI;
import com.suse.manager.webui.controllers.StorybookController;
import com.suse.manager.webui.controllers.SubscriptionMatchingController;
import com.suse.manager.webui.controllers.SystemsController;
import com.suse.manager.webui.controllers.TaskoTop;
import com.suse.manager.webui.controllers.VirtualHostManagerController;
import com.suse.manager.webui.controllers.activationkeys.ActivationKeysController;
import com.suse.manager.webui.controllers.activationkeys.ActivationKeysViewsController;
import com.suse.manager.webui.controllers.admin.AdminApiController;
import com.suse.manager.webui.controllers.admin.AdminViewsController;
import com.suse.manager.webui.controllers.admin.EnableSCCDataForwardingController;
import com.suse.manager.webui.controllers.admin.handlers.HubApiController;
import com.suse.manager.webui.controllers.appstreams.AppStreamsController;
import com.suse.manager.webui.controllers.bootstrap.RegularMinionBootstrapper;
import com.suse.manager.webui.controllers.bootstrap.SSHMinionBootstrapper;
import com.suse.manager.webui.controllers.channels.ChannelsApiController;
import com.suse.manager.webui.controllers.contentmanagement.ContentManagementApiController;
import com.suse.manager.webui.controllers.contentmanagement.ContentManagementViewsController;
import com.suse.manager.webui.controllers.login.LoginController;
import com.suse.manager.webui.controllers.maintenance.MaintenanceCalendarController;
import com.suse.manager.webui.controllers.maintenance.MaintenanceController;
import com.suse.manager.webui.controllers.maintenance.MaintenanceScheduleController;
import com.suse.manager.webui.errors.NotFoundException;
import com.suse.manager.webui.services.RbacRouteValidator;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.scc.SCCEndpoints;

import org.apache.http.HttpStatus;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

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
    @SuppressWarnings("checkstyle:methodlength")
    @Override
    public void init() {
        JadeTemplateEngine jade = setup();
        setupHibernateSessionFilter();

        initNotFoundRoutes(jade);

        TaskomaticApi taskomaticApi = new TaskomaticApi();
        SystemQuery systemQuery = GlobalInstanceHolder.SYSTEM_QUERY;
        SaltApi saltApi = GlobalInstanceHolder.SALT_API;
        KubernetesManager kubernetesManager = GlobalInstanceHolder.KUBERNETES_MANAGER;
        RegularMinionBootstrapper regularMinionBootstrapper = GlobalInstanceHolder.REGULAR_MINION_BOOTSTRAPPER;
        SSHMinionBootstrapper sshMinionBootstrapper = GlobalInstanceHolder.SSH_MINION_BOOTSTRAPPER;
        SaltKeyUtils saltKeyUtils = GlobalInstanceHolder.SALT_KEY_UTILS;
        ServerGroupManager serverGroupManager = GlobalInstanceHolder.SERVER_GROUP_MANAGER;
        SystemManager systemManager = GlobalInstanceHolder.SYSTEM_MANAGER;
        CloudPaygManager paygManager = GlobalInstanceHolder.PAYG_MANAGER;
        AttestationManager attestationManager = GlobalInstanceHolder.ATTESTATION_MANAGER;
        MigrationDataFactory migrationDataFactory = new MigrationDataFactory();

        SystemsController systemsController = new SystemsController(saltApi);
        ProxyController proxyController = new ProxyController(systemManager);
        SaltSSHController saltSSHController = new SaltSSHController(saltApi);
        NotificationMessageController notificationMessageController =
                new NotificationMessageController(systemQuery, saltApi, paygManager, attestationManager);
        MinionsAPI minionsAPI = new MinionsAPI(saltApi, sshMinionBootstrapper, regularMinionBootstrapper,
                saltKeyUtils, attestationManager, taskomaticApi, paygManager, migrationDataFactory);
        StatesAPI statesAPI = new StatesAPI(saltApi, taskomaticApi, serverGroupManager);
        FormulaController formulaController = new FormulaController(saltApi);
        HttpApiRegistry httpApiRegistry = new HttpApiRegistry();
        FrontendLogController frontendLogController = new FrontendLogController();
        DownloadController downloadController = new DownloadController(paygManager);
        ConfidentialComputingController confidentialComputingController =
                new ConfidentialComputingController(attestationManager);
        ProxyConfigurationController proxyConfigurationController =
                new ProxyConfigurationController(systemManager);

        try {
            URI url = new URI(Config.get().getString(ConfigDefaults.SCC_URL));
            SCCEndpoints sccEndpoints = new SCCEndpoints(ContentSyncManager.getUUID(), url);
            sccEndpoints.initRoutes(jade);
        }
        catch (URISyntaxException e) {
            throw new RhnRuntimeException();
        }

        // Login
        LoginController.initRoutes(jade);

        //CVEAudit
        CVEAuditController.initRoutes(jade);

        // Confidential Computing
        confidentialComputingController.initRoutes(jade);

        initContentManagementRoutes(jade, kubernetesManager);

        // Virtual Host Managers
        VirtualHostManagerController.initRoutes(jade);

        // Content Management Routes
        ContentManagementViewsController.initRoutes(jade);
        ContentManagementApiController.initRoutes();

        // Channels
        ChannelsApiController.initRoutes();

        // Admin Router
        AdminViewsController.initRoutes(jade);
        AdminApiController.initRoutes(taskomaticApi);

        // Minions
        MinionController.initRoutes(jade);

        // Minions API
        minionsAPI.initRoutes();

        // Systems API
        systemsController.initRoutes(jade);

        // Packages
        PackageController.initRoutes(jade);

        AppStreamsController.initRoutes(jade);

        // Proxy
        proxyController.initRoutes(proxyController, jade);

        // Proxy Configuration
        proxyConfigurationController.initRoutes(proxyConfigurationController, jade);

        //CSV API
        CSVDownloadController.initRoutes();

        // Activation Keys API
        ActivationKeysController.initRoutes();
        ActivationKeysViewsController.initRoutes(jade);

        SsmController.initRoutes();

        // States API
        statesAPI.initRoutes();

        // Recurring Action
        RecurringActionController.initRoutes(jade);

        EnableSCCDataForwardingController.initRoutes(jade);

        // Subscription Matching
        SubscriptionMatchingController.initRoutes(jade);

        // TaskoTop
        TaskoTop.initRoutes(jade);

        // Download endpoint
        downloadController.initRoutes();

        // Formula catalog
        FormulaCatalogController.initRoutes(jade);

        // Formulas
        formulaController.initRoutes(jade);

        get("/manager/download/saltssh/pubkey", saltSSHController::getPubKey);


        // NotificationMessages
        NotificationMessageController.initRoutes(jade, notificationMessageController);

        // Products
        ProductsController.initRoutes(jade);

        // Single Sign-On (SSO) via SAML
        SSOController.initRoutes();

        // Maintenance windows
        MaintenanceController.initRoutes();
        MaintenanceScheduleController.initRoutes(jade);
        MaintenanceCalendarController.initRoutes(jade);

        // Ansible Control Node
        AnsibleController.initRoutes(jade);

        // Rhn Set API
        SetController.initRoutes();

        // Image Upload
        ImageUploadController.initRoutes();

        // Frontend Logging
        frontendLogController.initRoutes();

        // HTTP API
        httpApiRegistry.initRoutes();

        // Saltboot
        SaltbootController.initRoutes();

        // Storybook
        StorybookController.initRoutes(jade);

        // ISSv3 Sync
        initISSv3Routes(taskomaticApi);

        // RBAC
        AccessGroupController.initRoutes();

        // Validate RBAC endpoints
        RbacRouteValidator.validateEndpoints();

        // if the calls above opened Hibernate session, close it now
        HibernateFactory.closeSession();
    }

    private void initNotFoundRoutes(JadeTemplateEngine jade) {
        notFound((request, response) -> {
            if (isJson(response) || isApiRequest(request)) {
                return message(response, "404 Not found");
            }
            var data = Collections.singletonMap("currentUrl",
                    URLEncoder.encode(request.pathInfo(), StandardCharsets.UTF_8));
            return jade.render(new ModelAndView(data, "templates/errors/404.jade"));
        });

        exception(NotFoundException.class, (exception, request, response) -> {
            response.status(HttpStatus.SC_NOT_FOUND);
            if (isJson(response) || isApiRequest(request)) {
                response.body(message(response, "404 Not found"));
            }
            else {
                var data = Collections.singletonMap("currentUrl",
                        URLEncoder.encode(request.pathInfo(), StandardCharsets.UTF_8));
                response.body(jade.render(new ModelAndView(data, "templates/errors/404.jade")));
            }
        });
    }


    private static void initContentManagementRoutes(JadeTemplateEngine jade, KubernetesManager kubernetesManager) {
        ImageBuildController imageBuildController = new ImageBuildController(kubernetesManager);
        ImageStoreController.initRoutes(jade);
        ImageProfileController.initRoutes(jade);
        ImageBuildController.initRoutes(jade, imageBuildController);
    }

    private static void initISSv3Routes(TaskomaticApi taskomaticApiIn) {
        HubManager hubManager = new HubManager();

        // ISS v3 Internal APIs
        HubController hubController = new HubController(hubManager, taskomaticApiIn);
        hubController.initRoutes();

        // API for the web interface
        HubApiController hubApiController = new HubApiController(hubManager, new IssMigratorFactory(), taskomaticApiIn);
        hubApiController.initRoutes();
    }
}
