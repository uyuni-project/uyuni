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
        // Display email change form
        get("/rhn/account/changeemail",
            withCsrfToken(withUser(AccountEmailController::displayForm)), jade);

        // Submit email change form (JSON response for AJAX)
        post("/rhn/account/changeemail",
            asJson(withUser(AccountEmailController::submitForm)));
    }

    /**
     * Display the email change form with current email address.
     * Supports optional uid parameter for admin user lookups.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param user the currently logged-in user
     * @return ModelAndView containing form data and template
     */
    public static ModelAndView displayForm(Request request, Response response, User user) {
        Map<String, Object> model = new HashMap<>();
        LocalizationService ls = LS;

        // Determine target user (admin can edit other users via uid parameter)
        User targetUser;
        try {
            String uidParam = request.queryParams("uid");
            if (uidParam != null && !uidParam.isEmpty()) {
                Long uid = Long.parseLong(uidParam);
                targetUser = UserManager.lookupUser(user, uid);
                if (targetUser == null) {
                    throw new BadParameterException("Invalid uid, target user not found");
                }
            } else {
                targetUser = user; // Editing own email
            }
        } catch (NumberFormatException e) {
            throw new BadParameterException("Invalid uid parameter format");
        }

        // Populate model for React template
        model.put("currentEmail", targetUser.getEmail());
        model.put("targetUserId", targetUser.getId());
        model.put("pageInstructions", ls.getMessage("yourchangeemail.instructions"));
        model.put("buttonLabel", ls.getMessage("message.Update"));
        model.put("csrfToken", request.attribute("csrf_token"));

        // Use React template exclusively
        return new ModelAndView(model, "users/account-email.jade");
    }

    /**
     * Submit and process the email change form.
     * Validates email address and updates user record.
     *
     * @param request the HTTP request (expects JSON body with email field)
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

            // Determine target user (admin can change other users' emails)
            User targetUser;
            String uidParam = request.queryParams("uid");
            if (uidParam != null && !uidParam.isEmpty()) {
                try {
                    Long uid = Long.parseLong(uidParam);
                    targetUser = UserManager.lookupUser(user, uid);
                    if (targetUser == null) {
                        throw new BadParameterException("Invalid uid, target user not found");
                    }
                } catch (NumberFormatException e) {
                    throw new BadParameterException("Invalid uid parameter format");
                }
            } else {
                targetUser = user; // Editing own email
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


        public EmailChangeRequest(String emailIn) {
            this.email = emailIn;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String emailIn) {
            this.email = emailIn;
        }
    }
}

