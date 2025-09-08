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

package com.redhat.rhn.frontend.taglibs.test;

import com.mockobjects.servlet.MockHttpServletRequest;
import com.mockobjects.servlet.MockHttpServletResponse;
import com.mockobjects.servlet.MockHttpSession;
import com.mockobjects.servlet.MockRequestDispatcher;
import com.mockobjects.servlet.MockServletConfig;
import com.mockobjects.servlet.MockServletContext;

/**
 * $Revision: 1.1 $
 */
public abstract class AbstractServletTestHelper {
    protected final MockHttpServletRequest request = new MockHttpServletRequest();
    protected final MockHttpServletResponse response = new MockHttpServletResponse();
    protected final MockHttpSession httpSession = new MockHttpSession();
    protected final MockRequestDispatcher requestDispatcher = new MockRequestDispatcher();
    protected final MockServletContext servletContext = new MockServletContext();
    protected final MockServletConfig servletConfig = new MockServletConfig();

    public AbstractServletTestHelper() {
        request.setSession(httpSession);
        servletContext.setupGetRequestDispatcher(requestDispatcher);
        request.setupGetRequestDispatcher(requestDispatcher);
        httpSession.setupServletContext(servletContext);
        servletConfig.setServletContext(servletContext);
    }

    public MockHttpServletRequest getRequest() {
        return request;
    }

    public MockHttpSession getHttpSession() {
        return httpSession;
    }

    public MockRequestDispatcher getRequestDispatcher() {
        return requestDispatcher;
    }

    public MockHttpServletResponse getResponse() {
        return response;
    }

    public MockServletContext getServletContext() {
        return servletContext;
    }

    public MockServletConfig getServletConfig() {
        return servletConfig;
    }
}
