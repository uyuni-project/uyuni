/*
 * Copyright (c) 2025 SUSE LLC
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

package com.redhat.rhn.testing;


import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class MockFilterChain implements FilterChain {
    /**
     * Simulates the invocation of the next filter in the chain or the target resource.
     * @param request the ServletRequest object
     * @param response the ServletResponse object
     * @throws IOException if an I/O error occurs during processing
     * @throws ServletException if a servlet-specific error occurs during processing
     */
    public void doFilter(ServletRequest request, ServletResponse response)
            throws IOException, ServletException {
    }
}
