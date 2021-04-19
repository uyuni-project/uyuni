/**
 * Copyright (c) 2021 SUSE LLC
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

import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withDocsLocale;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static spark.Spark.get;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.jade.JadeTemplateEngine;

/**
 * Spark controller class the CVE Audit page.
 */
public class AnsibleController {

    private static final Gson GSON = new GsonBuilder().create();

    private static Logger log = Logger.getLogger(AnsibleController.class);

    private AnsibleController() { }

    /**
     * Invoked from Router. Initialize routes for Systems Views.
     *
     * @param jade the Jade engine to use to render the pages
     */
    public static void initRoutes(JadeTemplateEngine jade) {
        get("/manager/systems/details/ansible",
                withCsrfToken(withDocsLocale(withUser(AnsibleController::view))), jade);
    }

    /**
     * Returns the ansible control node page
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the model and view
     */
    public static ModelAndView view(Request req, Response res, User user) {
        String serverId = req.queryParams("sid");
        Map<String, Object> data = new HashMap<>();
        Server server = ServerFactory.lookupById(Long.valueOf(serverId));
        data.put("server", server);

        return new ModelAndView(data, "templates/ansible/view.jade");
    }
}
