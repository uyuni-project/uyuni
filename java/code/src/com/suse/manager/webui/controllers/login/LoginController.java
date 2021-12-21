/*
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
package com.suse.manager.webui.controllers.login;

import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static spark.Spark.get;
import static spark.Spark.post;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.conf.sso.SSOConfig;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.acl.AclManager;
import com.redhat.rhn.manager.user.UserManager;

import com.suse.manager.webui.utils.LoginHelper;
import com.suse.utils.Json;

import com.google.gson.Gson;
import com.onelogin.saml2.Auth;
import com.onelogin.saml2.exception.SettingsException;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.jade.JadeTemplateEngine;

/**
 * Spark controller class to perform the login.
 */
public class LoginController {

    private static Logger log = Logger.getLogger(LoginController.class);
    private static final Gson GSON = Json.GSON;
    private static final String URL_CREATE_FIRST_USER = "/rhn/newlogin/CreateFirstUser.do";

    private LoginController() { }

    /**
     * Init all the routes used by LoginController
     * @param jade the used jade template engine
     */
    public static void initRoutes(JadeTemplateEngine jade) {
        get("/manager/login", withCsrfToken(LoginController::loginView), jade);
        post("/manager/api/login", LoginController::login);
    }

    /**
     * Return the login page.
     *
     * @param request the request object
     * @param response the response object
     * @return the model and view
     */
    public static ModelAndView loginView(Request request, Response response) {
        Map<String, Object> model = new HashMap<>();

        if (ConfigDefaults.get().isSingleSignOnEnabled() && SSOConfig.getSSOSettings().isPresent()) {
            /* Single Sign-On is enabled */
            try {
                Auth auth = new Auth(SSOConfig.getSSOSettings().get(), request.raw(), response.raw());
                auth.login(LoginHelper.DEFAULT_URL_BOUNCE);
            }
            catch (SettingsException | IOException e) {
                log.error(e.getMessage());
            }
            /*
                The return at the end of the method is dummy: the login page will not be displayed as we will be
                redirected to IdP's login page.
             */
        }
        else {
            if (!UserManager.satelliteHasUsers()) { // Redirect to user creation if needed
                response.redirect(URL_CREATE_FIRST_USER);
            }

            // Handle "url_bounce" parameters
            String urlBounce = request.queryParams("url_bounce");
            String reqMethod = request.queryParams("request_method");
            urlBounce = LoginHelper.updateUrlBounce(urlBounce, reqMethod);

            // In case we are authenticated go directly to redirect target
            if (AclManager.hasAcl("user_authenticated()", request.raw(), null)) {
                log.debug("Already authenticated, redirecting to: " + urlBounce);
                response.redirect(urlBounce);
            }

            model.put("url_bounce", urlBounce);
            model.put("request_method", reqMethod);
        }
        model.put("isUyuni", ConfigDefaults.get().isUyuni());
        model.put("title", Config.get().getString(ConfigDefaults.PRODUCT_NAME) + " - Sign In");
        model.put("validationErrors", Json.GSON.toJson(LoginHelper.validateDBVersion()));
        model.put("schemaUpgradeRequired", Json.GSON.toJson(LoginHelper.isSchemaUpgradeRequired()));
        model.put("webVersion", ConfigDefaults.get().getProductVersion());
        model.put("productName", Config.get().getString(ConfigDefaults.PRODUCT_NAME));
        model.put("customHeader", Config.get().getString("java.custom_header"));
        model.put("customFooter", Config.get().getString("java.custom_footer"));
        model.put("legalNote", Config.get().getString("java.legal_note"));
        model.put("loginLength", Config.get().getString("max_user_len"));
        model.put("passwordLength", Config.get().getString("max_passwd_len"));
        model.put("preferredLocale", ConfigDefaults.get().getDefaultLocale());
        model.put("docsLocale", ConfigDefaults.get().getDefaultDocsLocale());
        model.put("webTheme", ConfigDefaults.get().getDefaultWebTheme());
        model.put("diskspaceSeverity", LoginHelper.validateDiskSpaceAvailability());

        return new ModelAndView(model, "controllers/login/templates/login.jade");
    }

    /**
     * Perform the login.
     *
     * @param request the request object
     * @param response the response object
     * @return the model and view
     */
    public static String login(Request request, Response response) {
        List<String> errors = new ArrayList<>();
        LoginCredentials creds = GSON.fromJson(request.body(), LoginCredentials.class);
        User user = LoginHelper.checkExternalAuthentication(request.raw(), new ArrayList<>(), new ArrayList<>());

        // External-auth didn't return a user - try local-auth
        if (user == null) {
            user = LoginHelper.loginUser(creds.getLogin(), creds.getPassword(), errors);
            if (errors.isEmpty()) {
                // No errors, log success
                log.info("LOCAL AUTH SUCCESS: [" + user.getLogin() + "]");
            }
            else {
                // Errors, log failure
                log.error("LOCAL AUTH FAILURE: [" + creds.getLogin() + "]");
            }
        }
        // External-auth returned a user and no errors
        else if (errors.isEmpty()) {
            log.info("EXTERNAL AUTH SUCCESS: [" + user.getLogin() + "]");
        }

        if (errors.isEmpty()) {
            LoginHelper.successfulLogin(request.raw(), response.raw(), user);
            return json(response, new LoginResult(true, new String[0]));
        }
        else {
            log.error("LOCAL AUTH FAILURE: [" + creds.getLogin() + "]");
            return json(response, new LoginResult(false, errors.toArray(new String[errors.size()])));
        }
    }

    /**
     * Class to hold the login credentials.
     */
    public static class LoginCredentials {
        private String login;
        private String password;

        /**
         * Default constructor.
         */
        public LoginCredentials() {
        }

        /**
         * Default constructor.
         * @param loginIn the login the user has inputted in
         * @param passwordIn the password the user has inputted it
         */
        public LoginCredentials(String loginIn, String passwordIn) {
            this.login = loginIn;
            this.password = passwordIn;
        }

        /**
         * @return the login
         */
        public String getLogin() {
            return login;
        }

        /**
         * @return the password
         */
        public String getPassword() {
            return password;
        }
    }

    /**
     * Class to hold the login return results.
     */
    public static class LoginResult {

        private final boolean success;
        private final String[] messages;

        /**
         * @param successIn success
         * @param messagesIn messages
         */
        public LoginResult(boolean successIn, String... messagesIn) {
            this.success = successIn;
            this.messages = messagesIn;
        }

        /**
         * @return success
         */
        public boolean isSuccess() {
            return success;
        }

        /**
         * @return messages
         */
        public String[] getMessages() {
            return messages;
        }
    }
}
