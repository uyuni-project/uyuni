/*
 * Copyright (c) 2009--2013 Red Hat, Inc.
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
package com.redhat.rhn.frontend.servlets;

import com.redhat.rhn.domain.session.WebSession;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.testing.RhnJmockBaseTestCase;
import com.redhat.rhn.testing.RhnMockHttpServletRequest;
import com.redhat.rhn.testing.RhnMockHttpServletResponse;

import org.junit.jupiter.api.BeforeEach;

import javax.servlet.FilterChain;

/**
 * BaseFilterTest
 */
public abstract class BaseFilterTest extends RhnJmockBaseTestCase {

    protected RhnMockHttpServletRequest request;
    protected RhnMockHttpServletResponse response;
    protected FilterChain chain;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        request = new RhnMockHttpServletRequest();

        PxtCookieManager pcm = new PxtCookieManager();
        RequestContext requestContext = new RequestContext(request);

        WebSession s = requestContext.getWebSession();
        request.addCookie(pcm.createPxtCookie(s.getId(), request, 10));
        response = new RhnMockHttpServletResponse();
        chain = mock(FilterChain.class);
    }

}

