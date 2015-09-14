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

import com.suse.manager.webui.services.SaltService;
import com.suse.saltstack.netapi.datatypes.Keys;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import spark.ModelAndView;
import spark.Request;
import spark.Response;

/**
 * Controller class providing backend code for the minions page.
 */
public class MinionsController {

    // Reference to the SaltService instance
    private static final SaltService SALT_SERVICE = SaltService.INSTANCE;

    private MinionsController() { }

    /**
     * Handler for the minions page.
     *
     * @param request the request object
     * @param response the response object
     * @param user the user associated with this request
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView listMinions(Request request, Response response) {
        Keys keys = SALT_SERVICE.getKeys();
        List<String> present = SALT_SERVICE.present();
        Map<String, Object> data = new HashMap<>();
        data.put("minions", keys.getMinions());
        data.put("unaccepted_minions", keys.getUnacceptedMinions());
        data.put("rejected_minions", keys.getRejectedMinions());
        data.put("present", present);
        return new ModelAndView(data, "minions.jade");
    }

    /**
     * Handler for the minion details page.
     *
     * @param request the request object
     * @param response the response object
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView minionDetails(Request request, Response response) {
        String key = request.params("minion");
        Map<String, Object> grains = SALT_SERVICE.getGrains(key);
        Map<String, List<String>> packages = SALT_SERVICE.getPackages(key);
        Map<String, Object> data = new HashMap<>();
        data.put("key", key);
        data.put("grains", grains);
        data.put("packages", packages);
        return new ModelAndView(data, "minion.jade");
    }

    /**
     * Handler for accept minion url.
     *
     * @param request the request object
     * @param response the response object
     * @return dummy string to satisfy spark
     */
    public static Object acceptMinion(Request request, Response response) {
        SALT_SERVICE.acceptKey(request.params("minion"));
        response.redirect("/rhn/manager/minions");
        return "";
    }

    /**
     * Handler for delete minion url.
     *
     * @param request the request object
     * @param response the response object
     * @return dummy string to satisfy spark
     */
    public static Object deleteMinion(Request request, Response response) {
        SALT_SERVICE.deleteKey(request.params("minion"));
        response.redirect("/rhn/manager/minions");
        return "";
    }

    /**
     * Handler for reject minion url.
     *
     * @param request the request object
     * @param response the response object
     * @return dummy string to satisfy spark
     */
    public static Object rejectMinion(Request request, Response response) {
        SALT_SERVICE.rejectKey(request.params("minion"));
        response.redirect("/rhn/manager/minions");
        return "";
    }
}
