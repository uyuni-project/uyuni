/*
 * Copyright (c) 2023 SUSE LLC
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

package com.suse.manager.webui.controllers.image;

import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withImageAdmin;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUserPreferences;
import static spark.Spark.get;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.redhat.rhn.domain.role.Role;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.User;
import com.suse.manager.webui.errors.NotFoundException;
import com.suse.manager.webui.utils.gson.ResultJson;
import com.suse.utils.Json;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.template.jade.JadeTemplateEngine;

/**
 * Spark controller class for image sync pages.
 */
public class ImageManagementViewsController {

    private static final Gson GSON = Json.GSON;
    private static final Role ADMIN_ROLE = RoleFactory.IMAGE_ADMIN;

    private ImageManagementViewsController() { }

    /**
     * Invoked from Router. Initialize routes for image sync pages.
     *
     * @param jade the Jade engine to use to render the pages
     */
    public static void initRoutes(JadeTemplateEngine jade) {
        get("/manager/cm/imagesync",
                withUserPreferences(withCsrfToken(withUser(ImageManagementViewsController::listView))), jade);
        get("/manager/cm/imagesync/create",
                withCsrfToken(withImageAdmin(ImageManagementViewsController::createView)), jade);
        get("/manager/cm/imagesync/edit/:id",
                withCsrfToken(withImageAdmin(ImageManagementViewsController::updateView)), jade);
    }

    /**
     * Returns a view to list image sync projects
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the model and view
     */
    public static ModelAndView listView(Request req, Response res, User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("isAdmin", user.hasRole(ADMIN_ROLE));
        return new ModelAndView(data, "controllers/image/templates/list-image-sync.jade");
    }

    /**
     * Returns a view to display create form
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the model and view
     */
    public static ModelAndView createView(Request req, Response res, User user) {
        Map<String, Object> data = new HashMap<>();
        return new ModelAndView(data, "controllers/image/templates/edit-image-sync.jade");
    }

    /**
     * Returns a view to display update form
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the model and view
     */
    public static ModelAndView updateView(Request req, Response res, User user) {
        Long projectId;
        try {
            projectId = Long.parseLong(req.params("id"));
        }
        catch (NumberFormatException e) {
            throw new NotFoundException();
        }

        // TODO: Wait for corresponding Manager class to be implemented
        // Optional<ImageSyncProject> project =
        //         ImageSyncFactory.lookupByIdAndOrg(projectId, user.getOrg());
        // if (!project.isPresent()) {
        //     res.redirect("/rhn/manager/cm/imagesync/create");
        // }

        Map<String, Object> data = new HashMap<>();
        data.put("projectId", projectId);
        return new ModelAndView(data, "controllers/image/templates/edit-image-sync.jade");
    }

    /**
     * TODO: Move to API controller?
     * Processes a DELETE request
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static Object delete(Request req, Response res, User user) {
        List<Long> ids;
        try {
            ids = Arrays.asList(GSON.fromJson(req.body(), Long[].class));
        }
        catch (JsonParseException e) {
            Spark.halt(HttpStatus.SC_BAD_REQUEST);
            return null;
        }

        // TODO: Call delete on Manager class (when available)

        return json(res, ResultJson.success());
    }
}
