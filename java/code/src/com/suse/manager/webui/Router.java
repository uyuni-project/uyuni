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

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.security.CSRFTokenValidator;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.RequestContext;

import com.suse.manager.webui.controllers.MinionsController;
import com.suse.manager.webui.utils.JadeTemplateEngine;
import com.suse.manager.webui.utils.RouteWithUser;

import de.neuland.jade4j.JadeConfiguration;
import spark.Session;
import spark.Spark;
import spark.TemplateViewRoute;
import spark.servlet.SparkApplication;

import java.util.HashMap;
import java.util.Map;

/**
 * Router class defining the web UI routes.
 */
public class Router implements SparkApplication {

    private final String templateRoot = "com/suse/manager/webui/templates";

    @SuppressWarnings("unused")
    private TemplateViewRoute withUser(RouteWithUser route) {
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
        JadeConfiguration config = jade.getConfiguration();
        config.setSharedVariables(sharedVariables);

        // Setup routes
        Spark.get("/manager/minions", MinionsController::listMinions, jade);
        Spark.get("/manager/minions/accept/:minion", MinionsController::acceptMinion);
        Spark.get("/manager/minions/delete/:minion", MinionsController::deleteMinion);
        Spark.get("/manager/minions/reject/:minion", MinionsController::rejectMinion);
        Spark.get("/manager/minions/:minion", MinionsController::minionDetails, jade);

        // RuntimeException will be passed on (resulting in status code 500)
        Spark.exception(RuntimeException.class, (e, request, response) -> {
            throw (RuntimeException) e;
        });
    }
}
