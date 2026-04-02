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
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.suse.manager.webui.controllers.users;

import static com.suse.manager.webui.utils.SparkApplicationHelper.asJson;
import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withOrgAdmin;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static spark.Spark.get;
import static spark.Spark.post;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.common.BadParameterException;
import com.redhat.rhn.manager.user.UserManager;

import com.suse.manager.webui.utils.gson.ResultJson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.jade.JadeTemplateEngine;

/**
 * Spark controller for account email management.
 * Handles user email address changes with proper validation.
 */
public class AccountEmailController {

    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .create();

    private static final LocalizationService LS = LocalizationService.getInstance();

    /**
     * Initialize all routes for account email management.
     *
     * @param jade the Jade template engine
     */
    public static void initRoutes(JadeTemplateEngine jade) {
        // Display email change form - user's own email
        get("/manager/account/changeemail",
            withCsrfToken(withUser(AccountEmailController::displayOwnEmailForm)), jade);

        // Display email change form - admin changing user's email
        get("/manager/users/:uid/account/email",
            withCsrfToken(withOrgAdmin(AccountEmailController::displayAdminEmailForm)), jade);

        // Submit email change form (JSON response for AJAX)
        post("/manager/api/account/changeemail",
            asJson(withUser(AccountEmailController::submitForm)));
    }

    /**
     * Handler for user's own email change form.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param user the currently logged-in user
     * @return ModelAndView containing form data and template
     */
    public static ModelAndView displayOwnEmailForm(Request request, Response response, User user) {
        return displayForm(request, user, "own", null);
    }

    /**
     * Handler for admin email change form.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param user the currently logged-in admin user
     * @return ModelAndView containing form data and template
     */
    public static ModelAndView displayAdminEmailForm(Request request, Response response, User user) {
        Long uid = Long.parseLong(request.params(":uid"));
        return displayForm(request, user, "admin", uid);
    }

    /**
     * Display the email change form with current email address.
     * Supports both user's own email change and admin changing user's email.
     *
     * @param request the HTTP request
     * @param user the currently logged-in user
     * @param contextMode either "own" (user changing own email) or "admin" (admin changing user's email)
     * @param targetUid the uid of the target user when in admin mode, null for own mode
     * @return ModelAndView containing form data and template
     */
    public static ModelAndView displayForm(Request request, User user, String contextMode, Long targetUid) {
        Map<String, Object> model = new HashMap<>();
        LocalizationService ls = LS;

        // Determine target user based on context mode
        User targetUser;
        if ("admin".equals(contextMode)) {
            // Admin route: get user by uid
            targetUser = UserManager.lookupUser(user, targetUid);
            if (targetUser == null) {
                throw new BadParameterException("Invalid uid, target user not found");
            }
        } else {
            // Own route: user changing their own email
            targetUser = user;
        }

        // Populate model for React template
        model.put("currentEmail", targetUser.getEmail());
        model.put("targetUserId", targetUser.getId());
        model.put("targetUserName", targetUser.getLogin());
        model.put("contextMode", contextMode);
        model.put("pageInstructions", ls.getMessage("yourchangeemail.instructions"));
        model.put("buttonLabel", ls.getMessage("message.Update"));
        model.put("csrfToken", request.attribute("csrf_token"));

        // Use React template exclusively
        return new ModelAndView(model, "templates/users/account-email.jade");
    }

    /**
     * Submit and process the email change form.
     * Validates email address and updates user record.
     *
     * @param request the HTTP request (expects JSON body with email and optional uid)
     * @param response the HTTP response
     * @param user the currently logged-in user
     * @return JSON response with success or error status
     */
    public static String submitForm(Request request, Response response, User user) {
        // Parse request body
        EmailChangeRequest emailRequest = GSON.fromJson(request.body(), EmailChangeRequest.class);
        try {
            String newEmail = emailRequest.getEmail();

            if (newEmail == null || newEmail.trim().isEmpty()) {
                return json(GSON, response, 
                    ResultJson.error(LS.getMessage("error.email_required")),
                    new TypeToken<>() { });
            }

            newEmail = newEmail.trim();

            // Determine target user (uid in request body indicates admin mode)
            User targetUser;
            Long uid = emailRequest.getUid();
            if (uid != null && uid > 0) {
                // Admin changing user's email
                targetUser = UserManager.lookupUser(user, uid);
                if (targetUser == null) {
                    response.status(400);
                    return json(GSON, response,
                        ResultJson.error("Invalid uid, target user not found"),
                        new TypeToken<>() { });
                }
            } else {
                // User changing own email
                targetUser = user;
            }

            String currentEmail = targetUser.getEmail();

            // Validate email is different from current
            if (newEmail.equals(currentEmail)) {
                return json(GSON, response,
                    ResultJson.error(LS.getMessage("error.same_email")),
                    new TypeToken<>() { });
            }

            // Validate email format using RFC 5321/5322 standard
            validateEmailAddress(newEmail);

            // Update user email
            targetUser.setEmail(newEmail);
            UserManager.storeUser(targetUser);

            // Return success response
            return json(GSON, response,
                ResultJson.success(LS.getMessage("email.verified")),
                new TypeToken<>() { });

        } catch (AddressException e) {
            return json(GSON, response,
                ResultJson.error(LS.getMessage("error.addr_invalid", 
                    emailRequest.getEmail())),
                new TypeToken<>() { });
        } catch (BadParameterException e) {
            response.status(400);
            return json(GSON, response,
                ResultJson.error(e.getMessage()),
                new TypeToken<>() { });
        } catch (Exception e) {
            response.status(500);
            return json(GSON, response,
                ResultJson.error("An unexpected error occurred: " + e.getMessage()),
                new TypeToken<>() { });
        }
    }

    /**
     * Validates an email address using RFC 5321/5322 standard.
     * This matches the validation from the legacy ChangeEmailAction.
     *
     * @param email the email address to validate
     * @throws AddressException if the email format is invalid
     */
    private static void validateEmailAddress(String email) throws AddressException {
        new InternetAddress(email).validate();
    }

    /**
     * Request class for email change JSON payload.
     */
    public static class EmailChangeRequest {
        private String email;
        private Long uid;

        public EmailChangeRequest(String emailIn) {
            this.email = emailIn;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String emailIn) {
            this.email = emailIn;
        }

        public Long getUid() {
            return uid;
        }

        public void setUid(Long uidIn) {
            this.uid = uidIn;
        }
    }
}

