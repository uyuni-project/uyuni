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

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class MockRequestDispatcher implements RequestDispatcher  {
    private ServletRequest request;
    private ServletResponse response;

    /**
     * Forwards a request from a servlet to another resource on the server.
     * In this mock implementation, it simply stores the request and response objects.
     *
     * @param requestIn the servlet request to be forwarded
     * @param responseIn the servlet response to be forwarded
     * @throws ServletException if the target resource throws this exception
     * @throws IOException if the target resource throws this exception
     */
    public void forward(ServletRequest requestIn, ServletResponse responseIn)
            throws ServletException, IOException {
        request = requestIn;
        response = responseIn;
    }

    /**
     * Includes the content of a resource in the response.
     * In this mock implementation, it simply stores the request and response objects.
     *
     * @param requestIn the servlet request to include content from
     * @param responseIn the servlet response to include content to
     * @throws ServletException if the target resource throws this exception
     * @throws IOException if the target resource throws this exception
     */
    public void include(ServletRequest requestIn, ServletResponse responseIn)
            throws ServletException, IOException {
        request = requestIn;
        response = responseIn;
    }
}
