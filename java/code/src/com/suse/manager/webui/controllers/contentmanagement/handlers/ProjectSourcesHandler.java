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

import com.suse.manager.webui.controllers.contentmanagement.request.ProjectSourcesRequest;
import com.suse.utils.Json;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import org.apache.http.HttpStatus;

import spark.Request;
import spark.Spark;

/**
 * Utility class to help the handling of the SourceApiController
 */
public class ProjectSourcesHandler {
    private static final Gson GSON = Json.GSON;

    private ProjectSourcesHandler() { }

    /**
     * map request into the sources request bean
     * @param req the http request
     * @return environment request bean
     */
    public static ProjectSourcesRequest getSourcesRequest(Request req) {
        try {
            return GSON.fromJson(req.body(), ProjectSourcesRequest.class);
        }
        catch (JsonParseException e) {
            throw Spark.halt(HttpStatus.SC_BAD_REQUEST);
        }
    }

}
