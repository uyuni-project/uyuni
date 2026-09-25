/*
 * Copyright (c) 2024 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.suse.manager.webui.controllers.admin.handlers;

import static com.redhat.rhn.domain.role.RoleFactory.SAT_ADMIN;
import static com.suse.manager.webui.utils.SparkApplicationHelper.internalServerError;
import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static spark.Spark.get;
import static spark.Spark.post;

import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.common.util.validation.password.PasswordPolicy;
import com.redhat.rhn.common.util.validation.password.PasswordValidationUtils;
import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.webui.controllers.admin.service.PasswordPolicyService;
import com.suse.manager.webui.controllers.contentmanagement.handlers.ValidationUtils;
import com.suse.manager.webui.utils.gson.ResultJson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpStatus;

import spark.Request;
import spark.Response;

public class PasswordPolicyController {

    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .create();

    private final PasswordPolicyService service;

    /**
     * Constructor for the password policy controller
     * @param serviceIn the password policy service
     */
    public PasswordPolicyController(PasswordPolicyService serviceIn) {
        service = serviceIn;
    }

    /**
     * initialize all the API Routes for the password policy
     */
    public void initRoutes() {
        post("/manager/api/admin/config/password-policy", withUser(this::postPasswordPolicy));
        get("/manager/api/admin/config/password-policy/default", withUser(this::defaultPasswordPolicy));
        get("/manager/api/admin/config/password-policy", withUser(this::getPasswordPolicy));
        post("/manager/api/admin/config/password-policy/validate-password", withUser(this::validatePassword));
    }

    /**
     * edit the current password policy settings
     *
     * @param request  the request
     * @param response the response
     * @param user     the user
     * @return an OK string or Validation Errors
     */
    public String postPasswordPolicy(Request request, Response response, User user) {
        if (!user.hasRole(SAT_ADMIN)) {
            throw new PermissionException(SAT_ADMIN);
        }
        PasswordPolicy passwordPolicyProperties = GSON.fromJson(request.body(),
                PasswordPolicy.class);
        try {
            service.validatePasswordPolicy(passwordPolicyProperties);
            service.savePasswordPolicy(passwordPolicyProperties);
            return json(GSON, response, ResultJson.success(), new TypeToken<>() {
            });
        }
        catch (ValidatorException e) {
            return json(GSON, response, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error(ValidationUtils.convertValidationErrors(e),
                            ValidationUtils.convertFieldValidationErrors(e)), new TypeToken<>() {
                    });
        }
        catch (Exception e) {
            return json(GSON, response, HttpStatus.SC_INTERNAL_SERVER_ERROR, ResultJson.error(e.getMessage()),
                    new TypeToken<>() {
                    });
        }
    }

    /**
     * get the default password policy
     *
     * @param request  the request
     * @param response the response
     * @param user     the user
     * @return an OK string or Validation Errors
     */
    public String defaultPasswordPolicy(Request request, Response response, User user) {
        if (!user.hasRole(SAT_ADMIN)) {
            throw new PermissionException(SAT_ADMIN);
        }
        try {
            PasswordPolicy passwordPolicyProperties = PasswordPolicy.buildFromDefaults();
            return json(GSON, response, ResultJson.success(
                    GSON.toJson(passwordPolicyProperties)
            ), new TypeToken<>() { });
        }
        catch (Exception e) {
            return internalServerError(response, e.getMessage());
        }
    }

    /**
     * get the current password policy
     *
     * @param request  the request
     * @param response the response
     * @param user     the user
     * @return an OK string or Validation Errors
     */
    public String getPasswordPolicy(Request request, Response response, User user) {
        if (!user.hasRole(SAT_ADMIN)) {
            throw new PermissionException(SAT_ADMIN);
        }
        try {
            PasswordPolicy passwordPolicyProperties = PasswordPolicy.buildFromFactory();
            return json(GSON, response, ResultJson.success(
                    GSON.toJson(passwordPolicyProperties)
            ), new TypeToken<>() { });
        }
        catch (Exception e) {
            return json(GSON, response, HttpStatus.SC_BAD_REQUEST, ResultJson.error(e.getMessage()),
                    new TypeToken<>() {
                    });
        }
    }

    /**
     * validate a password against the current policy
     *
     * @param request  the request
     * @param response the response
     * @param user     the user
     * @return an OK string or Validation Errors
     */
    public String validatePassword(Request request, Response response, User user) {
        if (!user.hasRole(SAT_ADMIN)) {
            throw new PermissionException(SAT_ADMIN);
        }
        try {
            PasswordPolicy passwordPolicyProperties = PasswordPolicy.buildFromFactory();
            //throw away local class declaration
            class PasswordRequest {
                private String password;
                public String getPassword() {
                    return password;
                }
                public void setPassword(String passwordIn) {
                    password = passwordIn;
                }
            }
            PasswordRequest passwordRequest = GSON.fromJson(request.body(), PasswordRequest.class);
            PasswordValidationUtils.validatePasswordFromConfiguration(passwordRequest.password);
            return json(GSON, response, ResultJson.success(GSON.toJson(passwordPolicyProperties)),
                    new TypeToken<>() { });
        }
        catch (Exception e) {
            return json(GSON, response, HttpStatus.SC_BAD_REQUEST, ResultJson.error(e.getMessage()),
                    new TypeToken<>() {
                    });
        }
    }

}
