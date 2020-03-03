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
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static spark.Spark.exception;
import static spark.Spark.get;
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
import com.suse.manager.webui.controllers.RecurringActionController;
import com.suse.manager.webui.controllers.SSOController;
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
import com.suse.manager.webui.controllers.admin.AdminApiController;
import com.suse.manager.webui.controllers.admin.AdminViewsController;
import com.suse.manager.webui.controllers.channels.ChannelsApiController;
import com.suse.manager.webui.controllers.contentmanagement.ContentManagementApiController;
import com.suse.manager.webui.controllers.contentmanagement.ContentManagementViewsController;
import com.suse.manager.webui.controllers.login.LoginController;
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
    @SuppressWarnings("checkstyle:methodlength")
    @Override
    public void init() {
        JadeTemplateEngine jade = setup();

        initNotFoundRoutes(jade);

        post("/manager/frontend-log", withUser(FrontendLogController::log));

        // Login
        LoginController.initRoutes(jade);

        //CVEAudit
        CVEAuditController.initRoutes(jade);

        initContentManagementRoutes(jade);

        // Virtual Host Managers
        VirtualHostManagerController.initRoutes(jade);

        // Virtualization Routes
        initVirtualizationRoutes(jade);

        // Content Management Routes
        ContentManagementViewsController.initRoutes(jade);
        ContentManagementApiController.initRoutes();

        // Channels
        ChannelsApiController.initRoutes();

        // Admin Router
        AdminViewsController.initRoutes(jade);
        AdminApiController.initRoutes();

        // Minions
        MinionController.initRoutes(jade);

        // Minions API
        MinionsAPI.initRoutes();

        // Systems API
        SystemsController.initRoutes();

        // Activation Keys API
        ActivationKeysController.initRoutes();

        SsmController.initRoutes();

        // States API
        StatesAPI.initRoutes();

        // Recurring Action
        RecurringActionController.initRoutes(jade);

        // Subscription Matching
        SubscriptionMatchingController.initRoutes(jade);

        // TaskoTop
        TaskoTop.initRoutes(jade);

        // Download endpoint
        DownloadController.initRoutes();

        // Formula catalog
        FormulaCatalogController.initRoutes(jade);

        // Formulas
        FormulaController.initRoutes(jade);

        // Visualization
        VisualizationController.initRoutes(jade);

        get("/manager/download/saltssh/pubkey", SaltSSHController::getPubKey);


        // NotificationMessages
        NotificationMessageController.initRoutes(jade);

        // Products
        ProductsController.initRoutes(jade);

        // Single Sign-On (SSO) via SAML
        SSOController.initRoutes();
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
        ImageStoreController.initRoutes(jade);
        ImageProfileController.initRoutes(jade);
        ImageBuildController.initRoutes(jade);
    }

}
