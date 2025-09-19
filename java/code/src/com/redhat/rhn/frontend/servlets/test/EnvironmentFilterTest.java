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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.redhat.rhn.frontend.servlets.EnvironmentFilter;

import org.apache.struts.Globals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * EnvironmentFilterTest
 */
public class EnvironmentFilterTest extends BaseFilterTst {

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        this.request.setRequestURL("https://rhn.webdev.redhat.com/rhn/manager/login");
    }

    @Test
    public void testNonSSLUrls() throws Exception {

        EnvironmentFilter filter = new EnvironmentFilter();
        request.addParameter("message", "some.key.to.localize");
        request.addParameter("messagep1", "param value");
        request.addParameter("messagep2", "param value");
        request.addParameter("messagep3", "param value");
        filter.init(null);

        filter.doFilter(request, response, chain);

        // Check that we got the expected redirect.
        String expectedRedir = "https://mymachine.rhndev.redhat.com/rhn/manager/login";
        assertEquals(expectedRedir, response.getRedirect());

        request.setRequestURI("/rhn/kickstart/DownloadFile");
        response.clearRedirect();
        request.addParameter("message", "some.key.to.localize");
        request.addParameter("messagep1", "param value");
        request.addParameter("messagep2", "param value");
        request.addParameter("messagep3", "param value");
        filter.doFilter(request, response, chain);
        assertNull(response.getRedirect());
        assertNotEquals(expectedRedir, response.getRedirect());

        request.setRequestURI("/rhn/rpc/api");
        response.clearRedirect();
        filter.doFilter(request, response, chain);
        assertNull(response.getRedirect());
    }

    @Test
    public void testAddAMessage() throws Exception {
        EnvironmentFilter filter = new EnvironmentFilter();
        filter.init(null);
        this.request.setIsSecure(true);
        request.addParameter("message", "some.key.to.localize");
        request.addParameter("messagep1", "param value");
        request.addParameter("messagep2", "param value");
        request.addParameter("messagep3", "param value");

        filter.doFilter(request, response, chain);

        assertNotNull(request.getAttribute(Globals.MESSAGE_KEY));
        assertNotNull(session.getAttribute(Globals.MESSAGE_KEY));
    }
}
