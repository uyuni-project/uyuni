/**
 * Copyright (c) 2011 SUSE Linux Products GmbH
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

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.struts.Globals;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.logging.AuditLog;

/**
 * AuditLogFilter for generic logging of HTTP requests.
 *
 * @version $Rev$
 */
public class AuditLogFilter implements Filter {

    /** {@inheritDoc} */
    public void init(FilterConfig filterConfig) {
        // nop
    }

    /** {@inheritDoc} */
    public void destroy() {
        // nop
    }

    /** {@inheritDoc} */
    public void doFilter(ServletRequest req, ServletResponse resp,
            FilterChain chain) throws IOException, ServletException {

        // Finished for now
        chain.doFilter(req, resp);

        // Check if audit logging is enabled
        if (ConfigDefaults.get().isAuditEnabled()) {
            HttpServletRequest request = (HttpServletRequest) req;
            // Log POST requests that do not have errors
            if (request.getMethod().equals("POST")
                    && request.getAttribute(Globals.ERROR_KEY) == null) {
                AuditLog.log(null, request, null);
            }
        }
    }
}
