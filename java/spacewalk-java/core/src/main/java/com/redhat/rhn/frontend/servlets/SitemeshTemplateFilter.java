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

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Simple wrapper on {@link SiteMeshFilter} to add a debug logging
 */
public class SitemeshTemplateFilter extends PageFilter {

    private static final Logger LOGGER = LogManager.getLogger(SitemeshTemplateFilter.class);

    @Override
    public void doFilter(ServletRequest rq, ServletResponse rs, FilterChain chain)
            throws IOException, ServletException {
        if (LOGGER.isDebugEnabled()) {
            if (rq instanceof HttpServletRequest request) {
                LOGGER.debug("Applying templating filter to http servlet request {} {} [{}]",
                        request.getMethod(), request.getServletPath(), request.getDispatcherType());
            }
            else {
                LOGGER.debug("Applying templating filter to generic servlet request {}", rq);
            }
        }

        super.doFilter(rq, rs, chain);
    }

}
