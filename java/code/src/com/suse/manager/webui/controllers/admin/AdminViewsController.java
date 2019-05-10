/**
 * Copyright (c) 2019 SUSE LLC
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

package com.suse.manager.webui.controllers.admin;

import com.redhat.rhn.domain.user.User;
import org.apache.log4j.Logger;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.jade.JadeTemplateEngine;

import java.util.HashMap;
import java.util.Map;

import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withOrgAdmin;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUserPreferences;
import static spark.Spark.get;

/**
 * Spark controller class for admin pages.
 */
public class AdminViewsController {

    private static Logger log = Logger.getLogger(AdminViewsController.class);

    private AdminViewsController() { }

    /**
     * @param jade JadeTemplateEngine
     * Invoked from Router. Init routes for Admin Views.
     */
    public static void initRoutes(JadeTemplateEngine jade) {
        get("/manager/admin/config/monitoring",
                withUserPreferences(withCsrfToken(withOrgAdmin(AdminViewsController::showMonitoring))), jade);
    }

    /**
     * Show monitoring tab.
     * @param request http request
     * @param response http response
     * @param user current user
     * @return the view to show
     */
    public static ModelAndView showMonitoring(Request request, Response response, User user) {
        Map<String, Object> data = new HashMap<>();
        return new ModelAndView(data, "controllers/admin/templates/monitoring.jade");
    }

}
