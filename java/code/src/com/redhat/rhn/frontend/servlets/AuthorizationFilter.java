/*
 * TODO: Copyright (c) 2009--2014 Red Hat, Inc.
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

import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.access.WebEndpoint;
import com.redhat.rhn.domain.access.WebEndpointFactory;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.security.AuthenticationService;
import com.redhat.rhn.frontend.security.AuthenticationServiceFactory;
import com.redhat.rhn.frontend.struts.RequestContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import spark.route.HttpMethod;
import spark.route.ServletRoutes;
import spark.routematch.RouteMatch;

/**
 * A servlet filter to enforce access control rules
 */
public class AuthorizationFilter implements Filter {

    private static Logger log = LogManager.getLogger(AuthorizationFilter.class);
    private AuthenticationService authenticationService;

    @Override
    public void init(FilterConfig config) {
        AuthenticationServiceFactory factory = AuthenticationServiceFactory.getInstance();
        authenticationService = factory.getAuthenticationService();
    }

    /** {@inheritDoc} */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {

        if (log.isDebugEnabled()) {
            log.debug("ENTER AuthorizationFilter.doFilter: {} [{}] ({})", request.getRemoteAddr(), new Date(),
                    ((HttpServletRequest) (request)).getRequestURI());
        }

        HttpServletRequest hreq = new RhnHttpServletRequest((HttpServletRequest) request);
        HttpServletResponse hres = (HttpServletResponse) response;
        Set<String> noAuthEndpoints = WebEndpointFactory.getUnauthorizedWebEndpoints();

        try {
            if (hreq.getServletPath().startsWith("/ajax")) {
                handleAjaxAccess(hreq, noAuthEndpoints);
            }
            else if (hreq.getServletPath().endsWith(".do") || hreq.getServletPath().endsWith(".jsp")) {
                handleStrutsAccess(hreq, noAuthEndpoints);
            }
            else {
                handleSparkAccess(hreq, noAuthEndpoints);
            }
        }
        catch (PermissionException e) {
            // TODO: Handle PermissionExceptions properly depending on the "Content-Type" and "Accept" headers
            // TODO: possibly pass it down another filter
            // TODO: Review PageFilter
            hres.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
            return;
        }

        // Pass control on to the next filter
        chain.doFilter(request, response);
    }

    private void handleAjaxAccess(HttpServletRequest hreq, Set<String> noAuthEndpoints) {
        String path = hreq.getServletPath() + hreq.getPathInfo();
        if (!authenticationService.requestURIRequiresAuthentication(hreq) || noAuthEndpoints.contains(path)) {
            return;
        }

        User user = new RequestContext(hreq).getCurrentUser();
        if (user == null ) {
            if (!noAuthEndpoints.contains(path)){
                throw new PermissionException("The " + hreq.getRequestURI() +
                        " is not available for unauthenticated users.");
            }
        } else {
            if (!user.hasRole(RoleFactory.SAT_ADMIN)) {
                Optional<WebEndpoint> endpointOpts = WebEndpointFactory.lookupByUserIdEndpointScope(user.getId(),
                        path,
                        hreq.getMethod(),
                        WebEndpoint.Scope.W);
                if (endpointOpts.isEmpty()) {
                    throw new PermissionException("The " + hreq.getRequestURI() +
                            " API is not available to user " + user.getLogin());
                }
            }
        }
    }

    private void handleStrutsAccess(HttpServletRequest hreq, Set<String> noAuthEndpoints) {
        // Check if the request URI needs authentication. If so, also check if the URI needs authorization as well.
        if (!authenticationService.requestURIRequiresAuthentication(hreq) ||
                noAuthEndpoints.contains(hreq.getServletPath()) ){
            return;
        }

        User user = new RequestContext(hreq).getCurrentUser();
        if (user == null ) {
            if (!noAuthEndpoints.contains(hreq.getServletPath())){
                throw new PermissionException("The " + hreq.getRequestURI() +
                        " is not available for unauthenticated users.");
            }
        } else {
            if (!user.hasRole(RoleFactory.SAT_ADMIN)) {
                Optional<WebEndpoint> endpointOpts = WebEndpointFactory.lookupByUserIdEndpointScope(user.getId(),
                        hreq.getServletPath(),
                        hreq.getMethod(),
                        WebEndpoint.Scope.W);
                if (endpointOpts.isEmpty()) {
                    throw new PermissionException("The " + hreq.getRequestURI() +
                            " API is not available to user " + user.getLogin());
                }
            }
        }
    }

    private void handleSparkAccess(HttpServletRequest hreq, Set<String> noAuthEndpoints) {
        RouteMatch route = ServletRoutes.get().find(
                HttpMethod.get(hreq.getMethod().toLowerCase()),
                hreq.getServletPath(), hreq.getContentType());
        if (route == null)  {
            // TODO: This needs to be 404
            throw new PermissionException("Route not found to verify authorization for: " + hreq.getRequestURI());
        }

        // Check if the request URI needs authentication. If so, also check if the URI needs authorization as well.
        if (!authenticationService.requestURIRequiresAuthentication(hreq) ||
                noAuthEndpoints.contains(route.getMatchUri())) {
            return;
        }

        RequestContext requestContext = new RequestContext(hreq);
        User user = requestContext.getCurrentUser();
        if (user == null ) {
            if (!noAuthEndpoints.contains(route.getMatchUri())){
                throw new PermissionException("The " + route.getRequestURI() +
                        " is not available for unauthenticated users.");
            }
        } else {
            if (!user.hasRole(RoleFactory.SAT_ADMIN)) {
                Optional<WebEndpoint> endpoinOpts = WebEndpointFactory.lookupByUserIdEndpoint(user.getId(),
                        route.getMatchUri(),
                        // TODO: use AcceptType to check for 'scope'?
                        hreq.getMethod());
                if (endpoinOpts.isEmpty()) {
                    throw new PermissionException("The " + route.getRequestURI() +
                            " API is not available to user " + user.getLogin());
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
    }
}
