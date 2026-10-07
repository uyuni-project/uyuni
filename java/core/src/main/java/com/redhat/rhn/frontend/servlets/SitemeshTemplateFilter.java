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
package com.redhat.rhn.frontend.servlets;

import com.opensymphony.module.sitemesh.filter.PageFilter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Simple wrapper on {@link PageFilter} to add a debug logging
 */
public class SitemeshTemplateFilter extends PageFilter {

    private static final Logger LOGGER = LogManager.getLogger(SitemeshTemplateFilter.class);

    @Override
    public void doFilter(ServletRequest rq, ServletResponse rs, FilterChain chain)
            throws IOException, ServletException {
        if (rq instanceof HttpServletRequest request) {
            // Log for download endpoint diagnostics
            if (request.getServletPath().contains("/manager/download/")) {
                LOGGER.warn("[SITEMESH-BEFORE] Download endpoint: {} {} QueryString={} FullURI={}",
                        request.getMethod(), request.getServletPath(), request.getQueryString(),
                        request.getRequestURI());
                dumpFullRequest(request, "[SITEMESH-BEFORE-FULL]");
            }
            LOGGER.warn("Applying templating filter to http servlet request {} {} [{}]",
                    request.getMethod(), request.getServletPath(), request.getDispatcherType());
        }
        else {
            LOGGER.warn("Applying templating filter to generic servlet request {}", rq);
        }

        super.doFilter(rq, rs, chain);

        // Log after filter to diagnose response wrapping
        if (rq instanceof HttpServletRequest request) {
            if (request.getServletPath().contains("/manager/download/")) {
                LOGGER.warn("[SITEMESH-AFTER] Download endpoint completed. Response type: {}",
                        rs.getClass().getName());
            }
        }
    }

    /**
     * Dump full request details for reproduction/debugging
     */
    private void dumpFullRequest(HttpServletRequest request, String prefix) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("\n").append(prefix).append(" REQUEST DUMP\n");
            sb.append("Method: ").append(request.getMethod()).append("\n");
            sb.append("RequestURI: ").append(request.getRequestURI()).append("\n");
            sb.append("RequestURL: ").append(request.getRequestURL()).append("\n");
            sb.append("ContextPath: ").append(request.getContextPath()).append("\n");
            sb.append("ServletPath: ").append(request.getServletPath()).append("\n");
            sb.append("PathInfo: ").append(request.getPathInfo()).append("\n");
            sb.append("QueryString: ").append(request.getQueryString()).append("\n");
            sb.append("RemoteAddr: ").append(request.getRemoteAddr()).append("\n");
            sb.append("RemoteHost: ").append(request.getRemoteHost()).append("\n");
            sb.append("ContentType: ").append(request.getContentType()).append("\n");
            sb.append("ContentLength: ").append(request.getContentLength()).append("\n");

            sb.append("Headers:\n");
            Enumeration<String> headerNames = request.getHeaderNames();
            for (String headerName : Collections.list(headerNames)) {
                Enumeration<String> values = request.getHeaders(headerName);
                for (String value : Collections.list(values)) {
                    sb.append("  ").append(headerName).append(": ").append(value).append("\n");
                }
            }

            sb.append("Query Parameters:\n");
            Enumeration<String> paramNames = request.getParameterNames();
            for (String paramName : Collections.list(paramNames)) {
                String[] values = request.getParameterValues(paramName);
                for (String value : values) {
                    sb.append("  ").append(paramName).append(" = ").append(value).append("\n");
                }
            }

            LOGGER.info(sb.toString());
        }
        catch (Exception e) {
            LOGGER.warn("Error dumping request", e);
        }
    }
}
