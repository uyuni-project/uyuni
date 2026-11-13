/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.suse.manager.webui.controllers.admin.handlers;

import static com.redhat.rhn.domain.role.RoleFactory.SAT_ADMIN;
import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static spark.Spark.get;
import static spark.Spark.post;

import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.webui.controllers.admin.beans.ServerSettingsEntry;
import com.suse.manager.webui.controllers.admin.service.ServerSettingsService;
import com.suse.manager.webui.controllers.contentmanagement.handlers.ValidationUtils;
import com.suse.manager.webui.utils.gson.ResultJson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpStatus;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import spark.Request;
import spark.Response;

/**
 * API for the server settings page
 */
public class ServerSettingsController {

    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .create();

    private static final Type STRING_MAP = new TypeToken<Map<String, String>>() { }.getType();

    private final ServerSettingsService service;

    /**
     * Constructor for the server settings controller
     * @param serviceIn the server settings service
     */
    public ServerSettingsController(ServerSettingsService serviceIn) {
        service = serviceIn;
    }

    /**
     * initialize all the API Routes for the server settings
     */
    public void initRoutes() {
        get("/manager/api/admin/config/server-settings", withUser(this::listSettings));
        post("/manager/api/admin/config/server-settings", withUser(this::updateSettings));
        post("/manager/api/admin/config/server-settings/reset", withUser(this::resetSetting));
    }

    /**
     * list the server settings
     *
     * @param request  the request
     * @param response the response
     * @param user     the user
     * @return the settings
     */
    public String listSettings(Request request, Response response, User user) {
        ensureSatAdmin(user);
        return settingsResult(response);
    }

    /**
     * change the values of the server settings in the request body, a JSON object with the key names and values
     *
     * @param request  the request
     * @param response the response
     * @param user     the user
     * @return the changed settings or validation errors
     */
    public String updateSettings(Request request, Response response, User user) {
        ensureSatAdmin(user);
        try {
            Map<String, String> values = GSON.fromJson(request.body(), STRING_MAP);
            if (values == null) {
                return badRequest(response, "Missing values");
            }
            service.update(values);
            return settingsResult(response);
        }
        catch (JsonParseException e) {
            return badRequest(response, "Invalid request body");
        }
        catch (ValidatorException e) {
            return validationErrors(response, e);
        }
    }

    /**
     * reset the server setting in the request body ({"key": "NAME"}) to the default value
     *
     * @param request  the request
     * @param response the response
     * @param user     the user
     * @return the changed settings or validation errors
     */
    public String resetSetting(Request request, Response response, User user) {
        ensureSatAdmin(user);
        try {
            Map<String, String> body = GSON.fromJson(request.body(), STRING_MAP);
            if (body == null || body.get("key") == null) {
                return badRequest(response, "Missing key");
            }
            service.reset(body.get("key"));
            return settingsResult(response);
        }
        catch (JsonParseException e) {
            return badRequest(response, "Invalid request body");
        }
        catch (ValidatorException e) {
            return validationErrors(response, e);
        }
    }

    private static void ensureSatAdmin(User user) {
        if (!user.hasRole(SAT_ADMIN)) {
            throw new PermissionException(SAT_ADMIN);
        }
    }

    private String settingsResult(Response response) {
        return json(GSON, response, ResultJson.success(service.list()),
                new TypeToken<ResultJson<List<ServerSettingsEntry>>>() { });
    }

    private static String validationErrors(Response response, ValidatorException e) {
        return json(GSON, response, HttpStatus.SC_BAD_REQUEST,
                ResultJson.error(ValidationUtils.convertValidationErrors(e),
                        ValidationUtils.convertFieldValidationErrors(e)), new TypeToken<>() { });
    }

    private static String badRequest(Response response, String message) {
        return json(GSON, response, HttpStatus.SC_BAD_REQUEST, ResultJson.error(message), new TypeToken<>() { });
    }
}
