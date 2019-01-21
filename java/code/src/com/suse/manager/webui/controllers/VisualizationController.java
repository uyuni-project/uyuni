/**
 * Copyright (c) 2017 SUSE LLC
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
import com.redhat.rhn.manager.visualization.VisualizationManager;
import spark.ModelAndView;
import spark.Request;
import spark.Response;

import java.util.HashMap;
import java.util.Map;

import static com.suse.manager.webui.utils.SparkApplicationHelper.json;

/**
 * Controller class providing backend code for visualization pages.
 */
public class VisualizationController {

    private VisualizationController() { }

    /**
     * Display the Virtualization Hierarchy visualization page
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView showVirtualizationHierarchy(Request request,
            Response response, User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("title", "Virtualization Hierarchy");
        data.put("endpoint",
                "/rhn/manager/api/visualization/virtualization-hierarchy/data");
        data.put("view", "virtualization-hierarchy");
        request.attribute("legends", "visualization");
        return new ModelAndView(data, "templates/visualization/hierarchy.jade");
    }

    /**
     * Returns JSON data for virtualization-hierarchy view
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return JSON result of the API call
     */
    public static String virtHierarchyData(Request request, Response response, User user) {
        response.type("application/json");
        return json(response, VisualizationManager.virtualizationHierarchy(user));
    }

    /**
     * Display the Proxy Hierarchy visualization page
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView showProxyHierarchy(Request request, Response response,
            User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("title", "Proxy Hierarchy");
        data.put("endpoint", "/rhn/manager/api/visualization/proxy-hierarchy/data");
        data.put("view", "proxy-hierarchy");
        request.attribute("legends", "visualization");
        return new ModelAndView(data, "templates/visualization/hierarchy.jade");
    }

    /**
     * Returns JSON data for proxy-hierarchy view
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return JSON result of the API call
     */
    public static String proxyHierarchyData(Request request, Response response, User user) {
        response.type("application/json");
        return json(response, VisualizationManager.proxyHierarchy(user));
    }

    /**
     * Display the Systems Grouping visualization page
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView systemsWithManagedGroups(Request request, Response response,
            User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("title", "Systems Grouping");
        data.put("endpoint", "/rhn/manager/api/visualization/" +
                "systems-with-managed-groups/data");
        data.put("view", "grouping");
        request.attribute("legends", "visualization");
        return new ModelAndView(data, "templates/visualization/hierarchy.jade");
    }

    /**
     * Returns JSON data for all systems with their managed groups view
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return JSON result of the API call
     */
    public static String systemsWithManagedGroupsData(Request request, Response response,
            User user) {
        response.type("application/json");
        return json(response, VisualizationManager.systemsWithManagedGroups(user));
    }
}
