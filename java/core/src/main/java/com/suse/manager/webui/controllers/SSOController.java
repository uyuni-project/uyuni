/*
 * Copyright (c) 2019--2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.webui.controllers;

import static spark.Spark.get;
import static spark.Spark.post;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.frontend.security.AuthenticationServiceFactory;
import com.redhat.rhn.frontend.servlets.PxtSessionDelegateFactory;
import com.redhat.rhn.manager.user.UserManager;

import com.suse.manager.webui.utils.SparkApplicationHelper;

import com.onelogin.saml2.Auth;
import com.onelogin.saml2.exception.Error;
import com.onelogin.saml2.exception.SettingsException;
import com.onelogin.saml2.exception.XMLEntityException;
import com.onelogin.saml2.servlet.ServletUtils;
import com.onelogin.saml2.settings.Saml2Settings;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.servlet.ServletException;
import spark.Request;
import spark.Response;

/**
 * A controller class to deal with SAML protocol when using Single Sign-On (SSO)
 */
public final class SSOController {

    private static final Logger LOG = LogManager.getLogger(SSOController.class);

    private final Optional<Saml2Settings> ssoConfig;

    /**
     * Default constructor.
     * @param ssoConfigIn the SSO configuration
     */
    public SSOController(Optional<Saml2Settings> ssoConfigIn) {
        this.ssoConfig = ssoConfigIn;
    }

    /**
     * Method used to init routes in Spark
     */
    public void initRoutes() {
        get("/manager/sso/metadata", this::getMetadata);
        post("/manager/sso/acs", this::getACS);
        get("/manager/sso/logout", this::logout);
        get("/manager/sso/sls", this::sls);
    }

    /**
     * Returns the Assertion Consumer Service (ACS) for the our server (called by IdP)
     * @param request the Spark Request instance used in the current request scope
     * @param response the Spark Response instance used in the current response scope
     * @return the response object
     */
    public Object getACS(Request request, Response response) {
        if (!ConfigDefaults.get().isSingleSignOnEnabled() || ssoConfig.isEmpty()) {
            return null;
        }

        try {
            final Auth auth = new Auth(ssoConfig.get(), request.raw(), response.raw());
            auth.processResponse();

            final List<String> errors = auth.getErrors();

            if (errors.isEmpty()) {
                final Map<String, List<String>> attributes = auth.getAttributes();
                final String nameId = auth.getNameId();

                request.raw().getSession().setAttribute("attributes", attributes);
                request.raw().getSession().setAttribute("nameId", nameId);

                final String relayState = request.raw().getParameter("RelayState");

                final Collection<String> keys = attributes.keySet();

                if (keys.contains("uid") && !attributes.get("uid").isEmpty()) {
                    final Optional uidOpt = Optional.ofNullable(attributes.get("uid").get(0));
                    if (uidOpt.isPresent()) {
                        final User user = UserFactory.lookupByLogin(String.valueOf(uidOpt.get()));
                        user.setLastLoggedIn(new Date());
                        UserManager.storeUser(user);
                        PxtSessionDelegateFactory.getInstance().newPxtSessionDelegate().updateWebUserId(
                                request.raw(), response.raw(), user.getId());
                        if (relayState != null && !relayState.isEmpty() &&
                                !relayState.equals(ServletUtils.getSelfRoutedURLNoQuery(request.raw()))) {
                            // If the execution is at this point of the code, it means that the request successfully
                            // passed Auth.processResponse(), meaning that it containes a "SAMLResponse" parameter
                            // as an encoded64 XML file. This XML file has been in turn validated (signature)
                            // and it has a correct timestamp, so has not been forged by an attacker.
                            // The other parameter sent with the http request is named "RelayState", which usually
                            // has always a value of "/rhn/YourRhn.do" (sent by SSO, pointing to the main web page).
                            // As a precautionary measure, we can allow redirection only if RelayState parameter
                            // points to "/rhn/" pages, hence validating against redirection to external urls
                            if (relayState.startsWith("/rhn/")) {
                                response.redirect(relayState);
                                return response;
                            }
                        }
                    }
                }
                else {
                    LOG.error("SAML attribute named 'uid' not found in SAML attributes. Cannot log the user in. " +
                            "Please check with your Identity Provider (IdP) the presence of this attribute.");
                }
            }
            else {
                String allErrors = StringUtils.join(errors, ", ");
                LOG.error(allErrors);
                final String errorReason = auth.getLastErrorReason();
                if (errorReason != null && !errorReason.isEmpty()) {
                    LOG.error(auth.getLastErrorReason());
                }
            }
            response.redirect("/");
            return response;
        }
        catch (LookupException e) {
            LOG.error("Unable to find user: {}", e.getMessage(), e);
            return "Internal error during SSO authentication phase. Have you created the corresponding user in " +
                    "SUSE Manager? See product documentation";
        }
        catch (SettingsException e) {
            LOG.error("Unable to parse settings for SSO: {}", e.getMessage(), e);
            return "Internal error during SSO authentication phase - please check the logs " + e.getMessage();
        }
        catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

        return null;
    }

    /**
     * Returns the metadata associated with our server
     * @param request the Spark Request instance used in the current request scope
     * @param response the Spark Response instance used in the current response scope
     * @return the response object
     */
    public Object getMetadata(Request request, Response response) {
        if (ssoConfig.isEmpty() || !ConfigDefaults.get().isSingleSignOnEnabled()) {
            return null;
        }

        try {
            final Auth auth = new Auth(ssoConfig.get(), request.raw(), response.raw());
            final Saml2Settings settings = auth.getSettings();
            settings.setSPValidationOnly(true);
            final String metadata = settings.getSPMetadata();
            final List<String> errors = Saml2Settings.validateMetadata(metadata);
            if (errors.isEmpty()) {
                response.type("text/xml; charset=UTF-8");
                return metadata;
            }
            else {
                for (final String error : errors) {
                    LOG.error(error);
                }
                return SparkApplicationHelper.internalServerError(response, errors.toArray(new String[0]));
            }
        }
        catch (IOException | SettingsException | Error | CertificateEncodingException e) {
            LOG.error("Unable to parse settings for SSO and/or certificate error: {}", e.getMessage(), e);
        }
        catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

        return response;

    }

    /**
     * The service provider-initiated single logout (SLO)
     * @param request the Spark Request instance used in the current request scope
     * @param response the Spark Response instance used in the current response scope
     * @return the response object
     */
    public Object logout(Request request, Response response) {
        if (ssoConfig.isEmpty() || !ConfigDefaults.get().isSingleSignOnEnabled()) {
            return null;
        }

        try {
            AuthenticationServiceFactory.getInstance()
                    .getAuthenticationService()
                    .invalidate(request.raw(), response.raw());
            final Auth auth = new Auth(ssoConfig.get(), request.raw(), response.raw());
            auth.logout();
            return response;
        }
        catch (SettingsException | IOException | XMLEntityException e) {
            LOG.error("Unable to parse settings for SSO and/or XML parsing: {}", e.getMessage(), e);
        }

        return null;
    }

    /**
     * The Identity service provider initiated single logout service (SLS)
     * @param request the Spark Request instance used in the current request scope
     * @param response the Spark Response instance used in the current response scope
     * @return the response object
     */
    public Object sls(Request request, Response response) {
        if (ssoConfig.isEmpty() || !ConfigDefaults.get().isSingleSignOnEnabled()) {
            return null;
        }

        try {
            AuthenticationServiceFactory.getInstance()
                    .getAuthenticationService()
                    .invalidate(request.raw(), response.raw());
            final Auth auth = new Auth(ssoConfig.get(), request.raw(), response.raw());
            auth.processSLO();
            return "You have been logged out";
        }
        catch (ServletException | SettingsException e) {
            LOG.error("Unable to parse settings for SSO: {}", e.getMessage(), e);
        }
        catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

        return null;
    }
}
