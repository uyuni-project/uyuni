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

import static com.suse.manager.webui.Spark.get;
import static spark.Spark.exception;

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.RequestContext;

import com.suse.manager.webui.controllers.MinionsController;
import com.suse.saltstack.netapi.datatypes.Keys;

import org.apache.log4j.Logger;

import spark.ModelAndView;
import spark.servlet.SparkApplication;

import java.util.HashMap;
import java.util.Map;

/**
 * Router class defining the web UI routes.
 */
public class Router implements SparkApplication {

    private static Logger logger = Logger.getLogger(Router.class);

    @Override
    public void init() {

        // List all minions
        get("/manager/minions", (request, response) -> {
            User user = new RequestContext(request.raw()).getCurrentUser();
            Keys keys = new MinionsController().getKeys(user);
            Map<String, Object> data = new HashMap<String, Object>() {{
                put("minions", keys.getMinions());
                put("unaccepted_minions", keys.getUnacceptedMinions());
                put("rejected_minions", keys.getRejectedMinions());
                put("localize", new Localizer());
            }};
            return new ModelAndView(data, "minions.jade");
        });

        exception(RuntimeException.class, (e, request, response) -> {
            logger.error(e.getMessage(), e);
            response.status(500);
            response.body("<h1>Internal Server Error</h1>");
        });
    }
}
