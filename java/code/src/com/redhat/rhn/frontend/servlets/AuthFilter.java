/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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

package com.redhat.rhn.frontend.servlets;

import com.redhat.rhn.common.security.CSRFTokenException;
import com.redhat.rhn.common.security.CSRFTokenValidator;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.security.AuthenticationService;
import com.redhat.rhn.frontend.security.AuthenticationServiceFactory;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.manager.session.SessionManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import java.io.IOException;
import java.util.Date;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * AuthFilter - a servlet filter to ensure authenticated user info is put at
 * request scope properly
 */
public class AuthFilter implements Filter {

    private static Logger log = LogManager.getLogger(AuthFilter.class);

    private AuthenticationService authenticationService;

    /**
     * This method is intended for testing purposes only so that a fake, mock, ect.
     * service can be used. This method should <strong>not</strong> be used to change
     * the service implementation used. That is the responsibility of
     * AuthenticationServiceFactory.
     *
     * @param service An AuthenticationService to use for testing.
     * @see AuthenticationServiceFactory
     */
    protected void setAuthenticationService(AuthenticationService service) {
        authenticationService = service;
    }

    /** {@inheritDoc} */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        if (log.isDebugEnabled()) {
            log.debug("ENTER AuthFilter.doFilter: {} [{}] ({})", request.getRemoteAddr(), new Date(),
                    ((HttpServletRequest) (request)).getRequestURI());
        }

        if (authenticationService.validate((HttpServletRequest)request,
                (HttpServletResponse)response)) {

            HttpServletRequest hreq = new
                    RhnHttpServletRequest((HttpServletRequest)request);

            // Prevent read-only API users from using the web UI
            RequestContext requestContext = new RequestContext(hreq);
            User user = requestContext.getCurrentUser();
            if (user != null && user.isReadOnly() &&
                    (!hreq.getServletPath().startsWith("/manager/api") ||
                    !hreq.getMethod().equalsIgnoreCase("GET"))) {
                SessionManager.purgeUserSessions(user);
                authenticationService.redirectToLogin(hreq, (HttpServletResponse) response);
                return;
            }

            if (hreq.getMethod().equals("POST") || hreq.getMethod().equals("PUT") ||
                    hreq.getMethod().equals("DELETE")) {
                // validate security token to prevent CSRF type of attacks
                if (!authenticationService.skipCsfr((HttpServletRequest) request)) {
                    try {
                        CSRFTokenValidator.validate(hreq);
                    }
                    catch (CSRFTokenException e) {
                        // send HTTP 403 if security token validation failed
                        HttpServletResponse hres = (HttpServletResponse) response;
                        hres.sendError(HttpServletResponse.SC_FORBIDDEN,
                                e.getMessage());
                        return;
                    }
                }
            }
            try {
                chain.doFilter(request, response);
            }
            catch (PermissionException e) {
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                //forward to permissions error page
                request.setAttribute("error", e);
                request.getRequestDispatcher("/WEB-INF/pages/common/errors/permission.jsp")
                        .forward(request, response);
            }
            authenticationService.refresh((HttpServletRequest) request,
                    (HttpServletResponse) response);
        }
        else {
            HttpServletRequest servletRequest = (HttpServletRequest) request;
            HttpServletResponse servletResponse = (HttpServletResponse) response;

            // Ignore requests to the download and login endpoints
            if (servletRequest.getServletPath().startsWith("/manager/download/") ||
                    servletRequest.getServletPath().equals("/manager/api/login") ||
                    servletRequest.getServletPath().equals("/manager/api/auth/login")) {
                chain.doFilter(request, response);
            }
            // Send 401 for unauthorized API requests and senna SPA requests, else redirect to login
            else if (servletRequest.getServletPath().startsWith("/manager/api/") ||
                    Boolean.parseBoolean(servletRequest.getHeader("x-pjax"))) {
                servletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
            else {
                authenticationService.redirectToLogin(servletRequest, servletResponse);
            }
        }
    }

    private void addErrorMessage(HttpServletRequest hreq, String msgKey, String[] args) {
        ActionMessages ams = new ActionMessages();
        ams.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(msgKey, args));
        hreq.getSession().setAttribute(Globals.ERROR_KEY, ams);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(FilterConfig arg0) {
        AuthenticationServiceFactory factory = AuthenticationServiceFactory.getInstance();
        authenticationService = factory.getAuthenticationService();
    }
}
