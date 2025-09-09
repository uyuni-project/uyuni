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
package com.redhat.rhn.frontend.servlets.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.redhat.rhn.frontend.servlets.RhnHttpServletRequest;
import com.redhat.rhn.testing.MockObjectTestCase;
import com.redhat.rhn.testing.RhnMockHttpServletRequest;
import com.redhat.rhn.testing.RhnMockHttpSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * RhnHttpServletRequestTest
 */
public class RhnHttpServletRequestTest extends MockObjectTestCase {
    private RhnMockHttpServletRequest mockRequest;
    private RhnHttpServletRequest request;

    @BeforeEach
    public void setUp() {
        mockRequest = new RhnMockHttpServletRequest();
        mockRequest.setSession(new RhnMockHttpSession());
        request = new RhnHttpServletRequest(mockRequest);
    }

    /**
     *
     */
    @Test
    public void testNoHeaders() {
        mockRequest.setServerName("localhost");
        mockRequest.setServerPort(8080);
        assertEquals("localhost", request.getServerName());
        assertEquals(8080, request.getServerPort());
    }

    /**
     *
     */
    @Test
    public void testOverrideServerName() {
        mockRequest.setServerName("localhost");
        mockRequest.setServerPort(8080);
        mockRequest.setHeader("X-Server-Hostname", "testServer.redhat.com");
        assertEquals("testServer.redhat.com", request.getServerName());
        assertEquals(8080, request.getServerPort());
    }

    /**
     *
     */
    @Test
    public void testNoOverrideSecure() {
        mockRequest.setIsSecure(false);
        assertFalse(request.isSecure());
    }

    /**
     *
     */
    @Test
    public void testOverrideSecureHosted() {

        mockRequest.setIsSecure(false);
        mockRequest.setHeader("X-ENV-HTTPS", "on");

        // We expect this to be false, because this isn't a satellite.
        assertFalse(request.isSecure());
    }

    /**
     *
     */
    @Test
    public void testOverrideSecureSat() {
    }
}
