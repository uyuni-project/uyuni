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
package com.redhat.rhn.frontend.security.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.frontend.security.AuthenticationService;
import com.redhat.rhn.frontend.security.AuthenticationServiceFactory;
import com.redhat.rhn.frontend.security.PxtAuthenticationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * AuthenticationServiceFactoryTest
 */
public class AuthenticationServiceFactoryTest  {

    private class AuthenticationServiceFactoryStub extends AuthenticationServiceFactory {

        private boolean mgrServer = true;

        public boolean isMgrServer() {
            return mgrServer;
        }

        public void setMgrServer(boolean isMgrServer) {
            mgrServer = isMgrServer;
        }
    }

    private AuthenticationServiceFactoryStub factory;

    @BeforeEach
    public void setUp() throws Exception {
        factory = new AuthenticationServiceFactoryStub();
    }

    @Test
    public final void testGetInstance() {
        assertNotNull(AuthenticationServiceFactory.getInstance());
    }

    @Test
    public final void testGetAuthenticationServiceWhenInSatelliteMode() {
        factory.setMgrServer(true);

        AuthenticationService service = factory.getAuthenticationService();

        assertTrue(service instanceof PxtAuthenticationService);
    }

}
