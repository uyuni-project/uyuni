/*
 * Copyright (c) 2024 SUSE LLC
 * Copyright (c) 2009--2015 Red Hat, Inc.
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
package com.redhat.rhn.frontend.security;


import com.redhat.rhn.common.util.ServletUtils;
import com.redhat.rhn.frontend.servlets.PxtSessionDelegate;

import com.suse.manager.api.HttpApiRegistry;
import com.suse.manager.webui.utils.LoginHelper;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * PxtAuthenticationService
 */
public class PxtAuthenticationService extends BaseAuthenticationService {

    public static final long MAX_URL_LENGTH = 2048;

    private static final Set<String> UNPROTECTED_URIS;
    private static final Set<String> POST_UNPROTECTED_URIS;
    private static final Set<String> LOGIN_URIS;

    static {
        // Login routes
        Set<String> routes = new HashSet<>();
        routes.add("/rhn/newlogin/");
        routes.add("/rhn/manager/login");
        LOGIN_URIS = Set.copyOf(routes);

        // Unauthenticated routes
        routes.add("/rhn/rpc/api");
        routes.add("/rhn/help/");
        routes.add("/rhn/apidoc");
        routes.add("/rhn/errors");
        routes.add("/rhn/kickstart/DownloadFile");
        routes.add("/rhn/ty/TinyUrl");
        routes.add("/css");
        routes.add("/img");
        routes.add("/img/favicon.ico");
        routes.add("/rhn/common/DownloadFile");
        routes.add("/rhn/manager/sso");
        // password-reset-link destination
        routes.add("/rhn/ResetLink");
        routes.add("/rhn/ResetPasswordSubmit");
        routes.add("/rhn/saltboot");
        routes.add("/rhn/hub");
        // HTTP API public endpoints
        routes.addAll(HttpApiRegistry.getUnautenticatedRoutes());
        UNPROTECTED_URIS = Set.copyOf(routes);

        // CSRF whitelist
        routes.add("/rhn/common/DownloadFile");
        // Search (safe to be unprotected, since it has no modifying side-effects)
        routes.add("/rhn/Search.do");
        routes.add("/rhn/manager/api/");
        routes.add("/rhn/manager/upload/image");
        POST_UNPROTECTED_URIS = Set.copyOf(routes);
    }

    private PxtSessionDelegate pxtDelegate;

    protected PxtAuthenticationService() {
    }

    @Override
    protected Set<String> getLoginURIs() {
        return LOGIN_URIS;
    }

    @Override
    protected Set<String> getUnprotectedURIs() {
        return UNPROTECTED_URIS;
    }

    @Override
    protected Set<String> getPostUnprotectedURIs() {
        return POST_UNPROTECTED_URIS;
    }

    /**
     * "Wires up" the PxtSessionDelegate that this service object will use. Note that this
     * method should be invoked by a factory that creates instances of this class, such as
     * a dependency injection container...should one be used (/me/hopes/).
     *
     * @param delegate The PxtSessionDelegate to be used.
     */
    public void setPxtSessionDelegate(PxtSessionDelegate delegate) {
        pxtDelegate = delegate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean skipCsfr(HttpServletRequest request) {
        return requestURIdoesLogin(request) || requestPostCsfrWhitelist(request);
    }

    @Override
    public boolean validate(HttpServletRequest request, HttpServletResponse response) {
        if (requestURIRequiresAuthentication(request)) {
            if (isAuthenticationRequired(request)) {
                invalidate(request, response);
                return false;
            }
        }
        return true;
    }


    @Override
    public void refresh(HttpServletRequest request, HttpServletResponse response) {
        // If URL requires auth and we are authenticated refresh the session.
        // We don't refresh when the URL doesn't require auth because
        // that may invalidate our old session
        if (requestURIRequiresAuthentication(request)) {
            pxtDelegate.refreshPxtSession(request, response);
        }
    }

    private boolean isAuthenticationRequired(HttpServletRequest request) {
        return (!pxtDelegate.isPxtSessionKeyValid(request) ||
               pxtDelegate.isPxtSessionExpired(request) ||
               pxtDelegate.getWebUserId(request) == null);
    }

    @Override
    public void redirectToLogin(HttpServletRequest request, HttpServletResponse response)
        throws ServletException {

        try {
            StringBuilder redirectURI = new StringBuilder(request.getRequestURI());
            String params = ServletUtils.requestParamsToQueryString(request);
            // don't want to put the ? in the url if there are no params
            if (!StringUtils.isEmpty(params)) {
                redirectURI.append("?");
                redirectURI.append(params);
            }
            String urlBounce = redirectURI.toString();
            if (redirectURI.length() > MAX_URL_LENGTH) {
                urlBounce = LoginHelper.DEFAULT_URL_BOUNCE;
            }

            // in case of logout, let's redirect to Login2.go
            // not to be immediately logged in via Kerberos ticket
            if (urlBounce.equals("/rhn/")) {
                response.sendRedirect("/rhn/Login2.do");
                return;
            }

            URIBuilder uriBuilder = new URIBuilder();
            uriBuilder.setPath("/rhn/manager/login");
            uriBuilder.addParameter("url_bounce", urlBounce);
            uriBuilder.addParameter("request_method", request.getMethod());

            response.sendRedirect(uriBuilder.toString());
        }
        catch (IOException e) {
            throw new ServletException(e);
        }
    }

    @Override
    public void redirectTo(HttpServletRequest request, HttpServletResponse response, String path) {
            response.setHeader("Location", path);
            response.setStatus(HttpServletResponse.SC_SEE_OTHER);
    }

    @Override
    public void invalidate(HttpServletRequest request, HttpServletResponse response) {
        pxtDelegate.invalidatePxtSession(request, response);
    }
}
