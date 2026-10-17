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
package com.suse.manager.webui.controllers.admin;

import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static spark.Spark.get;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.common.RhnConfiguration;
import com.redhat.rhn.domain.common.RhnConfigurationFactory;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.User;

import java.util.HashMap;
import java.util.Map;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.jade.JadeTemplateEngine;

/**
 * Controller class providing backend code to enable SCC data forwarding.
 */
public class EnableSCCDataForwardingController {

    private EnableSCCDataForwardingController() { }

    /**
     * Initializes the routes.
     *
     * @param jade the jade engine
     */
    public static void initRoutes(JadeTemplateEngine jade) {
        get("/manager/admin/enable-scc-data-forwarding", withUser(EnableSCCDataForwardingController::enable), jade);
    }

    /**
     * Route to enable data forwarding
     * @param request - the request
     * @param response - the response
     * @param user - the user
     * @return - jade template with confirmation message
     */
    public static ModelAndView enable(Request request, Response response, User user) {
        String message = LocalizationService.getInstance().getMessage("scc-data-forwarding.enabled");

        if (!user.hasRole(RoleFactory.ORG_ADMIN)) {
            throw new PermissionException("Only Org Admins can change SCC data forwarding property.");
        }

        if (ConfigDefaults.get().isForwardRegistrationEnabled()) {
            message = LocalizationService.getInstance().getMessage("scc-data-forwarding.not-changed");
        }
        else {
            RhnConfigurationFactory.getSingleton().setValue(RhnConfiguration.KEYS.FORWARD_REGISTRATION, true);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("message", message);
        return new ModelAndView(data, "templates/admin/enable-scc-data-forwarding-confirm.jade");
    }
}
