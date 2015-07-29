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

import com.redhat.rhn.common.security.CSRFTokenValidator;

import javax.servlet.http.HttpSession;

import spark.TemplateViewRoute;
import spark.template.jade.JadeTemplateEngine;

public final class Spark {

    // Define the template root
    private static final String templateRoot = "com/suse/manager/webui/templates";

    /**
     * Responds to a GET with a jade template
     *
     * @param path the path
     * @param route the route
     */
    public static void get(String path, TemplateViewRoute route) {
        spark.Spark.get(path, (request, response) -> {
            HttpSession session = request.raw().getSession(true);
            request.attribute("csrf_token", CSRFTokenValidator.getToken(session));
            response.type("text/html");
            return route.handle(request, response);
        }, new JadeTemplateEngine(templateRoot));
    }
}
