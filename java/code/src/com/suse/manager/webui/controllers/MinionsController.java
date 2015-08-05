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
package com.suse.manager.webui.controllers;

import com.redhat.rhn.domain.user.User;

import com.suse.manager.webui.models.MinionsModel;
import com.suse.saltstack.netapi.datatypes.Keys;
import spark.ModelAndView;
import spark.Request;
import spark.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller class providing backend code for the minions page.
 */
public class MinionsController {

    private MinionsController() { }

    /**
     * Handler for the minions page
     * @param request  The request object providing information about the HTTP request
     * @param response The response object providing functionality for modifying the response
     * @param user The user associated with this request
     * @return The ModelAndView to render the page.
     */
    public static ModelAndView listMinions(Request request, Response response, User user) {
        Keys keys = MinionsModel.getKeys(user);
        Map<String, Object> data = new HashMap<>();
        data.put("minions", keys.getMinions());
        data.put("unaccepted_minions", keys.getUnacceptedMinions());
        data.put("rejected_minions", keys.getRejectedMinions());
        return new ModelAndView(data, "minions.jade");
    }
}
