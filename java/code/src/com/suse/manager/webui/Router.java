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

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.common.security.CSRFTokenValidator;
import com.suse.manager.webui.utils.RouteWithUser;
import com.suse.manager.webui.controllers.MinionsController;

import spark.Session;
import spark.Spark;
import spark.TemplateViewRoute;
import spark.servlet.SparkApplication;
import spark.template.jade.JadeTemplateEngine;

/**
 * Router class defining the web UI routes.
 */
public class Router implements SparkApplication {

    private final String templateRoot = "com/suse/manager/webui/templates";
    private final JadeTemplateEngine jade = new JadeTemplateEngine(templateRoot);

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

        // List all minions
        Spark.get("/manager/minions", withUser(MinionsController::listMinions), jade);

        // RuntimeException will be passed on (resulting in status code 500)
        Spark.exception(RuntimeException.class, (e, request, response) -> {
            throw (RuntimeException) e;
        });
    }
}
