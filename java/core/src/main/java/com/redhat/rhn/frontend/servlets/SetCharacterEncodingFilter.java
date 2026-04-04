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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

/**
 * <p>Example filter that sets the character encoding to be used in parsing the
 * incoming request, either unconditionally or only if the client did not
 * specify a character encoding.  Configuration of this filter is based on
 * the following initialization parameters:</p>
 * <ul>
 * <li><strong>encoding</strong> - The character encoding to be configured
 *     for this request, either conditionally or unconditionally based on
 *     the <code>ignore</code> initialization parameter.  This parameter
 *     is required, so there is no default.</li>
 * <li><strong>ignore</strong> - If set to "true", any character encoding
 *     specified by the client is ignored, and the value returned by the
 *     <code>selectEncoding()</code> method is set.  If set to "false,
 *     <code>selectEncoding()</code> is called <strong>only</strong> if the
 *     client has not already specified an encoding.  By default, this
 *     parameter is set to "true".</li>
 * </ul>
 *
 * @author shughes, modified from tomcat examples
 */

public class SetCharacterEncodingFilter implements Filter {

    private static final Logger LOG = LogManager.getLogger(SetCharacterEncodingFilter.class);

    /**
     * The default character encoding to set for requests that pass through
     * this filter.
     */
    protected String encoding = "UTF-8";

    /**
     * The filter configuration object we are associated with.  If this value
     * is null, this filter instance is not currently configured.
     */
    protected FilterConfig filterConfig = null;

    /**
     * Take this filter out of service.
     */
    @Override
    public void destroy() {
        this.encoding = null;
        this.filterConfig = null;
    }

    /**
     * Select and set (if specified) the character encoding to be used to
     * interpret request parameters for this request.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param chain The filter chain we are processing
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain)
           throws IOException, ServletException {

        // Conditionally select and set the character encoding to be used
        if ((request.getCharacterEncoding() == null)) {
            String encodingIn = selectEncoding(request);
            if (encodingIn != null) {
                request.setCharacterEncoding(encodingIn);
                response.setContentType("text/html; charset=" + encodingIn);
                response.setCharacterEncoding(encodingIn);
            }
            else {
                HttpServletRequest httpRequest = (HttpServletRequest) request;
                LOG.error("No character encoding defined for request: {}", httpRequest.getRequestURI());
            }
        }

        // Pass control on to the next filter
        chain.doFilter(request, response);

    }

    /** {@inheritDoc} */
    @Override
    public void init(FilterConfig filterConfigIn) {

        this.filterConfig = filterConfigIn;
        this.encoding = filterConfig.getInitParameter("encoding");
    }

    /**
     * Select an appropriate character encoding to be used, based on the
     * characteristics of the current request and/or filter initialization
     * parameters.  If no character encoding should be set, return
     * <code>null</code>.
     * <p>
     * The default implementation unconditionally returns the value configured
     * by the <strong>encoding</strong> initialization parameter for this
     * filter.
     *
     * @param request The servlet request we are processing
     * @return character encoding to use
     */
    protected String selectEncoding(ServletRequest request) {
        return (this.encoding);
    }

}
