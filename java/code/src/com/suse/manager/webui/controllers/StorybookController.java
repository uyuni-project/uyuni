/*
 * Copyright (c) 2024 SUSE LLC
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
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUserPreferences;
import static spark.Spark.get;

import com.redhat.rhn.domain.user.User;

import java.util.HashMap;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.jade.JadeTemplateEngine;

public class StorybookController {
    private StorybookController() { }

    /**
     * Initialize routes
     *
     * @param jade the Jade engine to use to render the pages
     */
    public static void initRoutes(JadeTemplateEngine jade) {
        get("/manager/storybook",
                withUserPreferences(withCsrfToken(withUser(StorybookController::view))), jade);
    }

    /**
     * Returns the ui debug page
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the model and view
     */
    public static ModelAndView view(Request req, Response res, User user) {
        return new ModelAndView(new HashMap<String, Object>(), "templates/storybook/storybook.jade");
    }
}
