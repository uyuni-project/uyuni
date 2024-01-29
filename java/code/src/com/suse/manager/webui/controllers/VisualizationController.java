/*
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

import static com.suse.manager.webui.utils.SparkApplicationHelper.asJson;
import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withOrgAdmin;
import static spark.Spark.get;

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.visualization.VisualizationManager;

import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.jade.JadeTemplateEngine;

/**
 * Controller class providing backend code for visualization pages.
 */
public class VisualizationController {

    private VisualizationController() { }

    /**
     * Invoked from Router. Initialize routes for Systems Views.
     *
     * @param jade the Jade engine to use to render the pages
     */
    public static void initRoutes(JadeTemplateEngine jade) {
        get("/manager/visualization/virtualization-hierarchy",
                withCsrfToken(withOrgAdmin(VisualizationController::showVirtualizationHierarchy)), jade);
        get("/manager/api/visualization/virtualization-hierarchy/data",
                asJson(withOrgAdmin(VisualizationController::virtHierarchyData)));
        get("/manager/visualization/proxy-hierarchy",
                withCsrfToken(withOrgAdmin(VisualizationController::showProxyHierarchy)), jade);
        get("/manager/api/visualization/proxy-hierarchy/data",
                asJson(withOrgAdmin(VisualizationController::proxyHierarchyData)));
        get("/manager/visualization/systems-with-managed-groups",
                withCsrfToken(withOrgAdmin(VisualizationController::systemsWithManagedGroups)), jade);
        get("/manager/api/visualization/systems-with-managed-groups/data",
                asJson(withOrgAdmin(VisualizationController::systemsWithManagedGroupsData)));
    }

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
        return json(response, VisualizationManager.virtualizationHierarchy(user),
                new TypeToken<>() { });
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
        return json(response, VisualizationManager.proxyHierarchy(user),
                new TypeToken<>() { });
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
        return json(response, VisualizationManager.systemsWithManagedGroups(user), new TypeToken<>() { });
    }
}
