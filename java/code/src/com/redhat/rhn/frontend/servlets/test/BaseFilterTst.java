/*
 * Copyright (c) 2009--2013 Red Hat, Inc.
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
package com.redhat.rhn.frontend.servlets.test;

import com.redhat.rhn.domain.session.WebSession;
import com.redhat.rhn.frontend.servlets.PxtCookieManager;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.testing.MockFilterChain;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.RhnMockHttpServletRequest;
import com.redhat.rhn.testing.RhnMockHttpServletResponse;
import com.redhat.rhn.testing.RhnMockHttpSession;

import org.junit.jupiter.api.BeforeEach;

/**
 * AuthFilterTest
 */
public abstract class BaseFilterTst extends RhnBaseTestCase {

    protected RhnMockHttpServletRequest request;
    protected RhnMockHttpSession session;
    protected RhnMockHttpServletResponse response;
    protected MockFilterChain chain;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        request = new RhnMockHttpServletRequest();
        session = new RhnMockHttpSession();

        PxtCookieManager pcm = new PxtCookieManager();
        RequestContext requestContext = new RequestContext(request);

        request.setServerName("mymachine.rhndev.redhat.com");
        request.setSession(session);
        request.setRequestURI("http://localhost:8080");
        WebSession s = requestContext.getWebSession();
        request.addCookie(pcm.createPxtCookie(s.getId(), request, 10));
        response = new RhnMockHttpServletResponse();
        chain = new MockFilterChain();
    }

}

