/**
 * Copyright (c) 2019 SUSE LLC
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
package com.suse.manager.webui.controllers.contentmanagement.handlers;

import com.suse.manager.webui.controllers.contentmanagement.request.ProjectBuildRequest;
import com.suse.utils.Json;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;

import java.util.HashMap;

import spark.Request;
import spark.Spark;

/**
 * Utility class to help the handling of the ProjectActionsApiController
 */
public class ProjectActionsHandler {
    private static final Gson GSON = Json.GSON;

    private ProjectActionsHandler() { }

    /**
     * map request into the project label request bean
     * @param req the http request
     * @return project label request bean
     */
    public static ProjectBuildRequest getProjectBuildRequest(Request req) {
        try {
            return GSON.fromJson(req.body(), ProjectBuildRequest.class);
        }
        catch (JsonParseException e) {
            throw Spark.halt(HttpStatus.SC_BAD_REQUEST);
        }
    }

    /**
     * validate project label request bean
     * @param projectBuild the project label request bean
     * @return validation errors
     */
    public static HashMap<String, String> validateProjectBuildlRequest(ProjectBuildRequest projectBuild) {
        HashMap<String, String> requestErrors = new HashMap<>();

        if (StringUtils.isEmpty(projectBuild.getProjectLabel())) {
            requestErrors.put("projectLabel", "Project label is required");
        }

        // TODO: Validate characters limit for message - Check database

        return requestErrors;
    }

}
