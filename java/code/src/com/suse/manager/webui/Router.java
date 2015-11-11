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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.security.CSRFTokenValidator;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.RequestContext;

import com.suse.manager.webui.controllers.DownloadController;
import com.suse.manager.webui.controllers.MinionsAPI;
import com.suse.manager.webui.controllers.MinionsController;
import com.suse.manager.webui.utils.RouteWithUser;

import de.neuland.jade4j.JadeConfiguration;
import spark.ModelAndView;
import spark.Route;
import spark.Session;
import spark.Spark;
import spark.TemplateViewRoute;
import spark.servlet.SparkApplication;
import spark.template.jade.JadeTemplateEngine;

import java.util.HashMap;
import java.util.Map;

/**
 * Router class defining the web UI routes.
 */
public class Router implements SparkApplication {

    private final String templateRoot = "com/suse/manager/webui/templates";
    private final Gson GSON = new GsonBuilder().create();
    private final static int HTTP_ERROR_500 = 500;

    @SuppressWarnings("unused")
    private TemplateViewRoute templatedWithUser(RouteWithUser<ModelAndView> route) {
        return (request, response) -> {
            User user = new RequestContext(request.raw()).getCurrentUser();
            return route.handle(request, response, user);
        };
    }

    @SuppressWarnings("unused")
    private Route withUser(RouteWithUser<Object> route) {
        return (request, response) -> {
            User user = new RequestContext(request.raw()).getCurrentUser();
            return route.handle(request, response, user);
        };
    }

    /**
     * Invoked from the SparkFilter. Add routes here.
     */
    @Override
    public void init() {
        // handler for crosscutting concerns relevant to all pages
        Spark.before((request, response) -> {
            Session session = request.session(true);
            CSRFTokenValidator.getToken(session.raw());
            response.type("text/html");
        });

        // Exhibit localization service in templates
        JadeTemplateEngine jade = new JadeTemplateEngine(templateRoot);
        Map<String, Object> sharedVariables = new HashMap<>();
        sharedVariables.put("l10n", LocalizationService.getInstance());
        sharedVariables.put("isDevMode",
                Config.get().getBoolean("java.development_environment"));
        JadeConfiguration config = jade.configuration();
        config.setSharedVariables(sharedVariables);

        // Setup routes
        Spark.get("/manager/minions", MinionsController::listMinions, jade);
        Spark.get("/manager/minions/overview/:minion", MinionsController::systemOverview);
        Spark.get("/manager/minions/accept/:minion", MinionsController::acceptMinion);
        Spark.get("/manager/minions/delete/:minion", MinionsController::deleteMinion);
        Spark.get("/manager/minions/reject/:minion", MinionsController::rejectMinion);

        // Remote commands
        Spark.get("/manager/minions/cmd", MinionsController::remoteCommands, jade);

        //Setup API routes
        Spark.get("/manager/api/minions/cmd", MinionsAPI::run);
        Spark.get("/manager/api/minions/match", MinionsAPI::match);

        // download endpoint
        Spark.get("/manager/download/:channel/getPackage/:file", DownloadController::downloadPackage);
        Spark.get("/manager/download/:channel/repodata/:file", DownloadController::downloadMetadata);
        Spark.head("/manager/download/:channel/getPackage/:file", DownloadController::downloadPackage);
        Spark.head("/manager/download/:channel/repodata/:file", DownloadController::downloadMetadata);


        // RuntimeException will be passed on (resulting in status code 500)
        Spark.exception(RuntimeException.class, (e, request, response) -> {
            if (request.headers("accept").contains("json")) {
                Map<String, Object> exc = new HashMap<>();
                exc.put("message", e.getMessage());
                response.type("application/json");
                response.body(this.GSON.toJson(exc));
                response.status(Router.HTTP_ERROR_500);
            } else {
                throw (RuntimeException) e;
            }
        });
    }
}
