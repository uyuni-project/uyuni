/*
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

import org.apache.commons.collections.set.UnmodifiableSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * PxtAuthenticationService
 */
public class PxtAuthenticationService extends BaseAuthenticationService {

    public static final long MAX_URL_LENGTH = 2048;

    private static final Set UNPROTECTED_URIS;
    private static final Set POST_UNPROTECTED_URIS;
    private static final Set LOGIN_URIS;

    static {
        // Login routes
        TreeSet set = new TreeSet<>();
        set.add("/rhn/newlogin/");
        set.add("/rhn/manager/login");

        LOGIN_URIS = UnmodifiableSet.decorate(set);

        // Unauthenticated routes
        set = new TreeSet<>(set);
        set.add("/rhn/rpc/api");
        set.add("/rhn/help/");
        set.add("/rhn/apidoc");
        set.add("/rhn/errors");
        set.add("/rhn/kickstart/DownloadFile");
        set.add("/rhn/ty/TinyUrl");
        set.add("/css");
        set.add("/img");
        set.add("/img/favicon.ico");
        set.add("/rhn/common/DownloadFile");
        set.add("/rhn/manager/sso");
        // password-reset-link destination
        set.add("/rhn/ResetLink");
        set.add("/rhn/ResetPasswordSubmit");
        set.add("/rhn/saltboot");

        // HTTP API public endpoints
        set.addAll(HttpApiRegistry.getUnautenticatedRoutes());

        UNPROTECTED_URIS = UnmodifiableSet.decorate(set);

        // CSRF whitelist
        set = new TreeSet<>(set);
        set.add("/rhn/common/DownloadFile");
        // search (safe to be unprotected, since it has no modifying side-effects)
        set.add("/rhn/Search.do");
        set.add("/rhn/manager/api/");
        set.add("/rhn/manager/upload/image");

        POST_UNPROTECTED_URIS = UnmodifiableSet.decorate(set);
    }

    private PxtSessionDelegate pxtDelegate;

    protected PxtAuthenticationService() {
    }

    @Override
    protected Set getLoginURIs() {
        return LOGIN_URIS;
    }

    @Override
    protected Set getUnprotectedURIs() {
        return UNPROTECTED_URIS;
    }

    @Override
    protected Set getPostUnprotectedURIs() {
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

    /**
     * {@inheritDoc}
     */
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


    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void redirectTo(HttpServletRequest request, HttpServletResponse response,
                           String path) {
            response.setHeader("Location", path);
            response.setStatus(response.SC_SEE_OTHER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void invalidate(HttpServletRequest request, HttpServletResponse response) {
        pxtDelegate.invalidatePxtSession(request, response);
    }
}
