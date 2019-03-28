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

import com.suse.manager.webui.controllers.contentmanagement.request.ProjectPropertiesRequest;
import com.suse.manager.webui.controllers.contentmanagement.request.NewProjectRequest;
import com.suse.utils.Json;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;

import java.util.HashMap;

import spark.Request;
import spark.Spark;

/**
 * Utility class to help the handling of the ProjectApiController
 */
public class ProjectHandler {
    private static final Gson GSON = Json.GSON;

    private ProjectHandler() { }

    /**
     * map request into the project properties  request bean
     * @param req the http request
     * @return project properties request bean
     */
    public static ProjectPropertiesRequest getProjectPropertiesRequest(Request req) {
        try {
            return GSON.fromJson(req.body(), ProjectPropertiesRequest.class);
        }
        catch (JsonParseException e) {
            throw Spark.halt(HttpStatus.SC_BAD_REQUEST);
        }
    }

    /**
     * map  request into the project request bean
     * @param req the http request
     * @return project request bean
     */
    public static NewProjectRequest getProjectRequest(Request req) {
        try {
            return GSON.fromJson(req.body(), NewProjectRequest.class);
        }
        catch (JsonParseException e) {
            throw Spark.halt(HttpStatus.SC_BAD_REQUEST);
        }
    }

    /**
     * map validate project properties request bean
     * @param projPropsRequest the project properties request bean
     * @return validation errors
     */
    public static HashMap<String, String> validateProjectPropertiesRequest(ProjectPropertiesRequest projPropsRequest) {
        HashMap<String, String> requestErrors = new HashMap<>();

        if (StringUtils.isEmpty(projPropsRequest.getLabel())) {
            requestErrors.put("label", "Label is required");
        }

        if (!ValidationUtils.isLabelValid(projPropsRequest.getLabel())) {
            requestErrors.put(
                    "label",
                    "Label must contain only lowercase letters, hyphens ('-'), periods ('.'), " +
                            "underscores ('_'), and numerals."
            );
        }

        if (projPropsRequest.getLabel().length() > 24) {
            requestErrors.put("label", "Label must not exceed 24 characters");
        }

        if (StringUtils.isEmpty(projPropsRequest.getName())) {
            requestErrors.put("name", "Name is required");
        }

        if (projPropsRequest.getName().length() > 128) {
            requestErrors.put("name", "Name must not exceed 128 characters");
        }

        return requestErrors;
    }

    /**
     * map validate project request bean
     * @param projectRequest the project request bean
     * @return validation errors
     */
    public static HashMap<String, String> validateProjectRequest(NewProjectRequest projectRequest) {
        HashMap<String, String> requestErrors = new HashMap<>();

        requestErrors.putAll(validateProjectPropertiesRequest(projectRequest.getProperties()));

        return requestErrors;
    }


}
