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


public abstract class AbstractServletTestHelper {
    protected final RhnMockHttpServletRequest request = new RhnMockHttpServletRequest();
    protected final RhnMockHttpServletResponse response = new RhnMockHttpServletResponse();
    protected final RhnMockHttpSession httpSession = new RhnMockHttpSession();
    protected final MockRequestDispatcher requestDispatcher = new MockRequestDispatcher();
    protected final MockServletContext servletContext = new MockServletContext();

    /**
     * Constructor that initializes the mock servlet components and sets up their relationships.
     * Configures the request with a session, servlet context with request dispatcher,
     * and establishes proper cross-references between the mock objects.
     */
    public AbstractServletTestHelper() {
        request.setSession(httpSession);
        servletContext.setRequestDispatcher(requestDispatcher);
        request.setRequestDispatcher(requestDispatcher);
        httpSession.setServletContext(servletContext);
    }

    public RhnMockHttpServletRequest getRequest() {
        return request;
    }

    public RhnMockHttpSession getHttpSession() {
        return httpSession;
    }

    public MockRequestDispatcher getRequestDispatcher() {
        return requestDispatcher;
    }

    public RhnMockHttpServletResponse getResponse() {
        return response;
    }

    public MockServletContext getServletContext() {
        return servletContext;
    }

}
