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

import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static spark.Spark.get;
import static spark.Spark.post;

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.conf.sso.SSOConfig;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.security.AuthenticationServiceFactory;
import com.redhat.rhn.manager.acl.AclManager;
import com.redhat.rhn.manager.user.UserManager;

import com.suse.manager.webui.utils.LoginHelper;
import com.suse.manager.webui.utils.SparkApplicationHelper;
import com.suse.utils.Json;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.onelogin.saml2.Auth;
import com.onelogin.saml2.exception.SettingsException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletResponse;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.template.jade.JadeTemplateEngine;

/**
 * Spark controller class to perform the login.
 */
public class LoginController {

    private static Logger log = LogManager.getLogger(LoginController.class);
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
        // TODO: Use this endpoint with the "Logout" button
        get("/manager/api/logout", withUser(LoginController::logout));
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
                log.debug("Already authenticated, redirecting to: {}", urlBounce);
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

        // Pay as you go code
        boolean sccForwardWarning = GlobalInstanceHolder.PAYG_MANAGER.isPaygInstance() &&
                GlobalInstanceHolder.PAYG_MANAGER.hasSCCCredentials() &&
                !ConfigDefaults.get().isForwardRegistrationEnabled();

        model.put("sccForwardWarning", sccForwardWarning);

        return new ModelAndView(model, "controllers/login/templates/login.jade");
    }

    /**
     * Perform the webUI login.
     *
     * @param request the request object
     * @param response the response object
     * @return the JSON result of the login operation
     */
    public static String login(Request request, Response response) {
        return performLogin(request, response, false);
    }

    /**
     * Perform the http api login.
     *
     * @param request the request object
     * @param response the response object
     * @return the JSON result of the login operation
     */
    public static String apiLogin(Request request, Response response) {
        return performLogin(request, response, true);
    }

    /**
     * Perform the login.
     *
     * @param request the request object
     * @param response the response object
     * @return the JSON result of the login operation
     */
    private static String performLogin(Request request, Response response, boolean allowReadOnly) {
        Optional<String> errorMsg = Optional.empty();
        LoginCredentials creds = GSON.fromJson(request.body(), LoginCredentials.class);
        User user = LoginHelper.checkExternalAuthentication(request.raw(), new ArrayList<>(), new ArrayList<>());

        // External-auth didn't return a user - try local-auth
        if (user == null) {
            if (creds == null) {
                Spark.halt(HttpServletResponse.SC_BAD_REQUEST);
            }
            else {
                try {
                    if (allowReadOnly) {
                        user = UserManager.loginReadOnlyUser(creds.getLogin(), creds.getPassword());
                    }
                    else {
                        user = UserManager.loginUser(creds.getLogin(), creds.getPassword());
                    }
                    log.info("LOCAL AUTH SUCCESS: [{}]", user.getLogin());
                }
                catch (LoginException e) {
                    log.error("LOCAL AUTH FAILURE: [{}]", creds.getLogin());
                    errorMsg = Optional.of(LocalizationService.getInstance().getMessage(e.getMessage()));
                }
            }
        }
        // External-auth returned a user and no errors
        else {
            log.info("EXTERNAL AUTH SUCCESS: [{}]", user.getLogin());
        }

        if (errorMsg.isEmpty()) {
            LoginHelper.successfulLogin(request.raw(), response.raw(), user);
            return SparkApplicationHelper.json(response, new LoginResult(true, null), new TypeToken<>() { });
        }
        else {
            log.error("LOCAL AUTH FAILURE: [{}]", creds.getLogin());
            response.status(HttpServletResponse.SC_UNAUTHORIZED);
            return SparkApplicationHelper.json(response, new LoginResult(false, errorMsg.get()), new TypeToken<>() { });
        }
    }

    /**
     * Logs out the current user
     * @param request the request
     * @param response the response
     * @param user the user
     * @return the JSON result of the logout operation
     */
    public static String logout(Request request, Response response, User user) {
        AuthenticationServiceFactory.getInstance().getAuthenticationService().invalidate(request.raw(), response.raw());
        log.info("WEB LOGOUT: [{}]", user.getLogin());
        return SparkApplicationHelper.json(response, new LoginResult(true, null), new TypeToken<>() { });
    }

    /**
     * Class to hold the login credentials.
     */
    public static class LoginCredentials {
        private String login;

        // You can choose to use either login and username, both should work equally.
        private String username;
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
            this.username = loginIn;
            this.password = passwordIn;
        }

        /**
         * @return the login
         */
        public String getLogin() {
            return login != null ? login : username;
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
        private final String message;

        /**
         * @param successIn success
         * @param messageIn message
         */
        public LoginResult(boolean successIn, String messageIn) {
            this.success = successIn;
            this.message = messageIn;
        }

        /**
         * @return success
         */
        public boolean isSuccess() {
            return success;
        }

        /**
         * @return message
         */
        public String getMessage() {
            return message;
        }
    }
}
