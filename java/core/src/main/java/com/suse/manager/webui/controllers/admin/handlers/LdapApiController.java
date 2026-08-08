/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.webui.controllers.admin.handlers;

import static com.redhat.rhn.domain.role.RoleFactory.SAT_ADMIN;
import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.put;

import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.EntityExistsException;

import com.suse.manager.admin.LdapAdminManager;
import com.suse.manager.ldap.LdapServiceException;
import com.suse.manager.model.ldap.LdapAuthServer;
import com.suse.manager.webui.controllers.admin.beans.LdapProperties;
import com.suse.manager.webui.controllers.admin.mappers.LdapResponseMappers;
import com.suse.manager.webui.controllers.contentmanagement.handlers.ValidationUtils;
import com.suse.manager.webui.utils.FlashScopeHelper;
import com.suse.manager.webui.utils.gson.ResultJson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

import spark.Request;
import spark.Response;

/**
 * Spark API controller for LDAP directory server administration (SAT_ADMIN only).
 */
public class LdapApiController {

    private static final LocalizationService LOC = LocalizationService.getInstance();
    private static final Logger LOG = LogManager.getLogger(LdapApiController.class);
    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .create();

    private final LdapAdminManager ldapAdminManager;

    /**
     * Default constructor.
     */
    public LdapApiController() {
        this(new LdapAdminManager());
    }

    /**
     * @param ldapAdminManagerIn manager used by the handlers
     */
    public LdapApiController(LdapAdminManager ldapAdminManagerIn) {
        this.ldapAdminManager = ldapAdminManagerIn;
    }

    /**
     * Registers the LDAP admin API routes.
     */
    public void initRoutes() {
        get("/manager/api/admin/config/ldap", withUser(this::listLdap));
        post("/manager/api/admin/config/ldap", withUser(this::createLdap));
        get("/manager/api/admin/config/ldap/:id", withUser(this::getLdap));
        put("/manager/api/admin/config/ldap/:id", withUser(this::updateLdap));
        delete("/manager/api/admin/config/ldap/:id", withUser(this::deleteLdap));
        post("/manager/api/admin/config/ldap/:id/test-connection", withUser(this::testConnection));
        post("/manager/api/admin/config/ldap/:id/test-user-lookup", withUser(this::testUserLookup));
        post("/manager/api/admin/config/ldap/:id/test-group-resolution", withUser(this::testGroupResolution));
    }

    /**
     * Lists all configured LDAP servers.
     *
     * @param request the request
     * @param response the response
     * @param user the current user
     * @return JSON list payload
     */
    public String listLdap(Request request, Response response, User user) {
        requireSatAdmin(user);
        return json(GSON, response,
                ResultJson.success(LdapResponseMappers.mapResumeFromDB(ldapAdminManager.list())),
                new TypeToken<>() { });
    }

    /**
     * Creates a new LDAP server.
     *
     * @param request the request
     * @param response the response
     * @param user the current user
     * @return JSON with the new server id
     */
    public String createLdap(Request request, Response response, User user) {
        requireSatAdmin(user);
        LdapProperties properties = GSON.fromJson(request.body(), LdapProperties.class);
        try {
            LdapAuthServer server = ldapAdminManager.create(properties);
            FlashScopeHelper.flash(request, LOC.getMessage("ldap.server_created", server.getLabel()));
            return json(GSON, response, ResultJson.success(server.getId()), new TypeToken<>() { });
        }
        catch (EntityExistsException error) {
            return json(GSON, response, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error(LOC.getMessage("ldap.label_exists")), new TypeToken<>() { });
        }
        catch (ValidatorException e) {
            return validationError(response, e);
        }
        catch (Exception e) {
            LOG.error("Failed to create LDAP server", e);
            return json(GSON, response, HttpStatus.SC_BAD_REQUEST, ResultJson.error(e.getMessage()),
                    new TypeToken<>() { });
        }
    }

    /**
     * Returns one LDAP server in full detail (without bind password).
     *
     * @param request the request
     * @param response the response
     * @param user the current user
     * @return JSON detail payload
     */
    public String getLdap(Request request, Response response, User user) {
        requireSatAdmin(user);
        try {
            long id = Long.parseLong(request.params("id"));
            LdapAuthServer server = ldapAdminManager.get(id);
            return json(GSON, response,
                    ResultJson.success(LdapResponseMappers.mapFullFromDB(server)),
                    new TypeToken<>() { });
        }
        catch (LookupException | NumberFormatException e) {
            return json(GSON, response, HttpStatus.SC_NOT_FOUND, ResultJson.error(), new TypeToken<>() { });
        }
    }

    /**
     * Updates an LDAP server.
     *
     * @param request the request
     * @param response the response
     * @param user the current user
     * @return JSON full detail of the updated server
     */
    public String updateLdap(Request request, Response response, User user) {
        requireSatAdmin(user);
        try {
            long id = Long.parseLong(request.params("id"));
            LdapProperties properties = GSON.fromJson(request.body(), LdapProperties.class);
            LdapAuthServer server = ldapAdminManager.update(id, properties);
            return json(GSON, response,
                    ResultJson.success(LdapResponseMappers.mapFullFromDB(server)),
                    new TypeToken<>() { });
        }
        catch (EntityExistsException error) {
            return json(GSON, response, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error(LOC.getMessage("ldap.label_exists")), new TypeToken<>() { });
        }
        catch (LookupException | NumberFormatException e) {
            return json(GSON, response, HttpStatus.SC_NOT_FOUND, ResultJson.error(), new TypeToken<>() { });
        }
        catch (ValidatorException e) {
            return validationError(response, e);
        }
        catch (Exception e) {
            LOG.error("Failed to update LDAP server", e);
            return json(GSON, response, HttpStatus.SC_BAD_REQUEST, ResultJson.error(e.getMessage()),
                    new TypeToken<>() { });
        }
    }

    /**
     * Deletes an LDAP server.
     *
     * @param request the request
     * @param response the response
     * @param user the current user
     * @return JSON success message
     */
    public String deleteLdap(Request request, Response response, User user) {
        requireSatAdmin(user);
        try {
            long id = Long.parseLong(request.params("id"));
            LdapAuthServer server = ldapAdminManager.get(id);
            String label = server.getLabel();
            boolean removed = ldapAdminManager.delete(id);
            if (removed) {
                String successMessage = LOC.getMessage("ldap.server_deleted", label);
                FlashScopeHelper.flash(request, successMessage);
                return json(GSON, response, ResultJson.successMessage(successMessage), new TypeToken<>() { });
            }
            return json(GSON, response, ResultJson.error(), new TypeToken<>() { });
        }
        catch (LookupException | NumberFormatException e) {
            return json(GSON, response, HttpStatus.SC_NOT_FOUND, ResultJson.error(), new TypeToken<>() { });
        }
    }

    /**
     * Tests connectivity (and optional bind) for a stored LDAP server.
     *
     * @param request the request
     * @param response the response
     * @param user the current user
     * @return JSON success or generic error
     */
    public String testConnection(Request request, Response response, User user) {
        requireSatAdmin(user);
        try {
            long id = Long.parseLong(request.params("id"));
            ldapAdminManager.testConnection(id);
            return json(GSON, response,
                    ResultJson.successMessage(LOC.getMessage("ldap.connection_test_success")),
                    new TypeToken<>() { });
        }
        catch (LookupException | NumberFormatException e) {
            return json(GSON, response, HttpStatus.SC_NOT_FOUND, ResultJson.error(), new TypeToken<>() { });
        }
        catch (LdapServiceException | IllegalStateException | IllegalArgumentException e) {
            LOG.warn("LDAP connection test failed: {}", e.getMessage());
            LOG.debug("LDAP connection test failure details", e);
            return json(GSON, response, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error(LOC.getMessage("ldap.connection_test_failed")), new TypeToken<>() { });
        }
    }

    /**
     * Looks up a directory user by login (no password) for admin troubleshooting.
     *
     * @param request the request
     * @param response the response
     * @param user the current user
     * @return JSON with user summary or generic error
     */
    public String testUserLookup(Request request, Response response, User user) {
        requireSatAdmin(user);
        try {
            long id = Long.parseLong(request.params("id"));
            String login = loginFromBody(request);
            Map<String, Object> result = ldapAdminManager.testUserLookup(id, login);
            return json(GSON, response,
                    ResultJson.success(result, LOC.getMessage("ldap.user_lookup_success")),
                    new TypeToken<>() { });
        }
        catch (LookupException | NumberFormatException e) {
            return json(GSON, response, HttpStatus.SC_NOT_FOUND, ResultJson.error(), new TypeToken<>() { });
        }
        catch (ValidatorException e) {
            return validationError(response, e);
        }
        catch (LdapServiceException | IllegalStateException | IllegalArgumentException e) {
            LOG.warn("LDAP user lookup test failed: {}", e.getMessage());
            LOG.debug("LDAP user lookup test failure details", e);
            return json(GSON, response, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error(LOC.getMessage("ldap.user_lookup_failed")), new TypeToken<>() { });
        }
    }

    /**
     * Looks up a directory user and resolves groups (no password) for admin troubleshooting.
     *
     * @param request the request
     * @param response the response
     * @param user the current user
     * @return JSON with user and group summary or generic error
     */
    public String testGroupResolution(Request request, Response response, User user) {
        requireSatAdmin(user);
        try {
            long id = Long.parseLong(request.params("id"));
            String login = loginFromBody(request);
            Map<String, Object> result = ldapAdminManager.testGroupResolution(id, login);
            return json(GSON, response,
                    ResultJson.success(result, LOC.getMessage("ldap.group_resolution_success")),
                    new TypeToken<>() { });
        }
        catch (LookupException | NumberFormatException e) {
            return json(GSON, response, HttpStatus.SC_NOT_FOUND, ResultJson.error(), new TypeToken<>() { });
        }
        catch (ValidatorException e) {
            return validationError(response, e);
        }
        catch (LdapServiceException | IllegalStateException | IllegalArgumentException e) {
            LOG.warn("LDAP group resolution test failed: {}", e.getMessage());
            LOG.debug("LDAP group resolution test failure details", e);
            return json(GSON, response, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error(LOC.getMessage("ldap.group_resolution_failed")), new TypeToken<>() { });
        }
    }

    private static String loginFromBody(Request request) {
        Map<String, String> body = GSON.fromJson(request.body(), new TypeToken<Map<String, String>>() { }.getType());
        return body == null ? null : body.get("login");
    }

    private static void requireSatAdmin(User user) {
        if (!user.hasRole(SAT_ADMIN)) {
            throw new PermissionException(SAT_ADMIN);
        }
    }

    private static String validationError(Response response, ValidatorException e) {
        return json(GSON, response, HttpStatus.SC_BAD_REQUEST,
                ResultJson.error(ValidationUtils.convertValidationErrors(e),
                        ValidationUtils.convertFieldValidationErrors(e)), new TypeToken<>() { });
    }
}
