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
package com.suse.manager.webui.utils;

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.RequestContext;

import spark.Route;
import spark.TemplateViewRoute;

/**
 * Utility methods to integrate Spark with SUSE Manager's infrastructure.
 */
public class SparkApplicationUtils {

    /**
     * Private constructor.
     */
    private SparkApplicationUtils() {
    }

    /**
     * Use in routes to automatically get the current user in your controller.
     * Example: <code>Spark.get("/url", withUser(Controller::method), jade);</code>
     * @param route the route
     * @return the template view route
     */
    public static TemplateViewRoute withUser(TemplateViewRouteWithUser route) {
        return (request, response) -> {
            User user = new RequestContext(request.raw()).getCurrentUser();
            return route.handle(request, response, user);
        };
    }

    /**
     * Use in routes to automatically get the current user in your controller.
     * Example: <code>Spark.get("/url", withUser(Controller::method));</code>
     * @param route the route
     * @return the route
     */
    public static Route withUser(RouteWithUser route) {
        return (request, response) -> {
            User user = new RequestContext(request.raw()).getCurrentUser();
            return route.handle(request, response, user);
        };
    }
}
