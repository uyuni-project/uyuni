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

import com.suse.manager.webui.controllers.contentmanagement.request.EnvironmentRequest;
import com.suse.utils.Json;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;

import java.util.HashMap;

import spark.Request;
import spark.Spark;

/**
 * Utility class to help the handling of the EnvironmentApiController
 */
public class EnvironmentHandler {
    private static final Gson GSON = Json.GSON;

    private EnvironmentHandler() { }

    /**
     * map request into the environment request bean
     * @param req the http request
     * @return environment request bean
     */
    public static EnvironmentRequest getEnvironmentRequest(Request req) {
        try {
            return GSON.fromJson(req.body(), EnvironmentRequest.class);
        }
        catch (JsonParseException e) {
            throw Spark.halt(HttpStatus.SC_BAD_REQUEST);
        }
    }

    /**
     * map validate environment request bean
     * @param envRequest the environment request bean
     * @return validation errors
     */
    public static HashMap<String, String> validateEnvironmentRequest(EnvironmentRequest envRequest) {
        HashMap<String, String> requestErrors = new HashMap<>();

        if (StringUtils.isEmpty(envRequest.getName()) || StringUtils.isEmpty(envRequest.getLabel())) {
            requestErrors.put("name", "Name is required");
        }

        if (!ValidationUtils.isLabelValid(envRequest.getLabel())) {
            requestErrors.put(
                    "label",
                    "Label must contain only lowercase letters, hyphens ('-'), periods ('.'), " +
                            "underscores ('_'), and numerals."
            );
        }

        if (envRequest.getLabel().length() > 16) {
            requestErrors.put("label", "Label must not exceed 16 characters");
        }
        return requestErrors;
    }
}
