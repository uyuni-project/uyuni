/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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

import com.redhat.rhn.common.util.StringUtil;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * DumpFilter dumps the current request and response to the log file.
 * Useful for debugging filter and servlet development.
 */
public class DumpFilter implements Filter {
    private static Logger log = LogManager.getLogger(DumpFilter.class);

    /** {@inheritDoc} */
    @Override
    public void doFilter(ServletRequest req,
                         ServletResponse resp,
                         FilterChain chain)
            throws IOException, ServletException {

        if (log.isDebugEnabled()) {
        // handle request
        HttpServletRequest request = (HttpServletRequest) req;
            log.debug("Entered doFilter() ===================================");
            log.debug("AuthType: {}", request.getAuthType());
            log.debug("Method: {}", request.getMethod());
            log.debug("PathInfo: {}", request.getPathInfo());
            log.debug("Translated path: {}", StringUtil.sanitizeLogInput(request.getPathTranslated()));
            log.debug("ContextPath: {}", request.getContextPath());
            log.debug("Query String: {}", StringUtil.sanitizeLogInput(request.getQueryString()));
            log.debug("Remote User: {}", StringUtil.sanitizeLogInput(request.getRemoteUser()));
            log.debug("Remote Host: {}", request.getRemoteHost());
            log.debug("Remote Addr: {}", request.getRemoteAddr());
            log.debug("uri: {}", request.getRequestURI());
            log.debug("url: {}", request.getRequestURL());
            log.debug("Servlet path: {}", request.getServletPath());
            log.debug("Server Name: {}", request.getServerName());
            log.debug("Server Port: {}", request.getServerPort());
            log.debug("RESPONSE encoding: {}", resp.getCharacterEncoding());
            log.debug("REQUEST encoding: {}", StringUtil.sanitizeLogInput(request.getCharacterEncoding()));
            log.debug("JVM encoding: {}", System.getProperty("file.encoding"));
            logSession(request.getSession());
            logHeaders(request);
            logCookies(request.getCookies());
            logParameters(request);
            logAttributes(request);
            log.debug("Calling chain.doFilter() -----------------------------");
        }

        chain.doFilter(req, resp);

        if (log.isDebugEnabled()) {
            log.debug("Returned from chain.doFilter() -----------------------");
            log.debug("Handle Response, not much to print");
            log.debug("Response: {}", resp);
            log.debug("Leaving doFilter() ===================================");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void destroy() {
        // nop
    }

    /** {@inheritDoc} */
    @Override
    public void init(FilterConfig filterConfig) {
        // nop
    }

    private void logCookies(Cookie[] cookies) {
        if (log.isDebugEnabled()) {
            if (cookies == null) {
                log.debug("There are NO cookies to log");
                return;
            }

            for (Cookie cookieIn : cookies) {
                log.debug(StringUtil.sanitizeLogInput(ReflectionToStringBuilder.toString(cookieIn)));
            }
        }
    }

    private void logHeaders(HttpServletRequest req) {
        if (log.isDebugEnabled()) {
            Enumeration<String> items = req.getHeaderNames();
            while (items.hasMoreElements()) {
                String name = items.nextElement();
                Enumeration<String> hdrs = req.getHeaders(name);
                while (hdrs.hasMoreElements()) {
                    log.debug("Header: name [{}] value [{}]",
                            StringUtil.sanitizeLogInput(name), StringUtil.sanitizeLogInput(hdrs.nextElement()));
                }
            }
        }
    }

    private void logSession(HttpSession session) {
        if (log.isDebugEnabled()) {
            log.debug(ReflectionToStringBuilder.toString(session));
        }
    }

    private void logParameters(HttpServletRequest req) {
        if (log.isDebugEnabled()) {
            Enumeration<String> items = req.getParameterNames();
            while (items.hasMoreElements()) {
                String name = items.nextElement();
                String[] values = req.getParameterValues(name);
                for (String valueIn : values) {
                    log.debug("Parameter: name [{}] value [{}]", StringUtil.sanitizeLogInput(name),
                            StringUtil.sanitizeLogInput(valueIn));
                }
            }
        }
    }

    private void logAttributes(HttpServletRequest req) {
        if (log.isDebugEnabled()) {
            Enumeration<String> items = req.getAttributeNames();
            while (items.hasMoreElements()) {
                String name = items.nextElement();
                Object obj = req.getAttribute(name);
                if (obj != null) {
                    log.debug("Attribute: name [{}] value [{}]", StringUtil.sanitizeLogInput(name),
                            StringUtil.sanitizeLogInput(ReflectionToStringBuilder.toString(obj)));
                }
                else {
                    log.debug("Attribute: name [{}] value [null]", StringUtil.sanitizeLogInput(name));
                }
            }
        }
    }
}
