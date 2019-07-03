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

import com.suse.manager.webui.controllers.contentmanagement.request.FilterRequest;
import com.suse.manager.webui.controllers.contentmanagement.request.ProjectFiltersUpdateRequest;
import com.suse.utils.Json;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import org.apache.http.HttpStatus;

import java.util.HashMap;

import spark.Request;
import spark.Spark;

/**
 * Utility class to help the handling of the FilterApiController
 */
public class FilterHandler {
    private static final Gson GSON = Json.GSON;

    private FilterHandler() { }


    /**
     * map request into the project update filters request bean
     * @param req the http request
     * @return environment request bean
     */
    public static ProjectFiltersUpdateRequest getProjectFiltersRequest(Request req) {
        try {
            return GSON.fromJson(req.body(), ProjectFiltersUpdateRequest.class);
        }
        catch (JsonParseException e) {
            throw Spark.halt(HttpStatus.SC_BAD_REQUEST);
        }
    }

    /**
     * map request into the filter request bean
     * @param req the http request
     * @return environment request bean
     */
    public static FilterRequest getFilterRequest(Request req) {
        try {
            return GSON.fromJson(req.body(), FilterRequest.class);
        }
        catch (JsonParseException e) {
            throw Spark.halt(HttpStatus.SC_BAD_REQUEST);
        }
    }

    /**
     * map validate filter request bean
     * @param envRequest the environment request bean
     * @return validation errors
     */
    public static HashMap<String, String> validateFilterRequest(FilterRequest envRequest) {
        HashMap<String, String> requestErrors = new HashMap<>();

        // TODO: validations

        return requestErrors;
    }
}
