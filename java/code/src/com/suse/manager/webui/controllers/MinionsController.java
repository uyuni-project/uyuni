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

import java.util.HashMap;
import java.util.Map;

import spark.ModelAndView;
import spark.Request;
import spark.Response;

/**
 * Controller class providing backend code for the minions page.
 */
public class MinionsController {

    private MinionsController() { }

    /**
     * Handler for the minions page.
     *
     * @param request the request object
     * @param response the response object
     * @param user the user associated with this request
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView listMinions(Request request, Response response, User user) {
        Keys keys = MinionsModel.getKeys(user);
        Map<String, Object> data = new HashMap<>();
        data.put("minions", keys.getMinions());
        data.put("unaccepted_minions", keys.getUnacceptedMinions());
        data.put("rejected_minions", keys.getRejectedMinions());
        return new ModelAndView(data, "minions.jade");
    }

    public static ModelAndView minionDetails(Request request, Response response) {
        String key = request.params("key");
        Map<String, Object> grains = MinionsModel.grains(key);
        Map<String, Object> data = new HashMap<>();
        data.put("key", key);
        data.put("grains", grains);
        return new ModelAndView(data, "minion.jade");
    }

    public static Object acceptMinion(Request request, Response response) {
        MinionsModel.accept(request.params("key"));
        response.redirect("/rhn/manager/minions");
        return "";
    }

    public static Object deleteMinion(Request request, Response response) {
        MinionsModel.delete(request.params("key"));
        response.redirect("/rhn/manager/minions");
        return "";
    }

    public static Object rejectMinion(Request request, Response response) {
        MinionsModel.reject(request.params("key"));
        response.redirect("/rhn/manager/minions");
        return "";
    }
}
