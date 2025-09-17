/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.common.security.acl.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.security.acl.SystemAclHandler;
import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.KickstartSession;
import com.redhat.rhn.domain.kickstart.test.KickstartDataTest;
import com.redhat.rhn.domain.kickstart.test.KickstartSessionTest;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.test.SystemManagerTest;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * SystemAclHandlerTest
 */
public class SystemAclHandlerTest extends BaseTestCaseWithUser {
    private Server srvr;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        srvr = ServerFactoryTest.createTestServer(user);
        Long version = 1L;
        SystemManagerTest.giveCapability(srvr.getId(),
                SystemManager.CAP_CONFIGFILES_BASE64_ENC, version);
    }

    @Test
    public void testClientCapable() {
        SystemAclHandler sah = new SystemAclHandler();

        Map<String, Object> ctx = new HashMap<>();
        ctx.put("sid", srvr.getId());

        String[] params = { SystemManager.CAP_CONFIGFILES_BASE64_ENC };
        boolean rc = sah.aclClientCapable(ctx, params);
        assertTrue(rc);

        String[] params1 = { "" };
        rc = sah.aclClientCapable(ctx, params1);
        assertFalse(rc);

        rc = sah.aclClientCapable(ctx, null);
        assertFalse(rc);
    }

    @Test
    public void testSystemHasKickstartSession() throws Exception {
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        SystemAclHandler sah = new SystemAclHandler();
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("sid", srvr.getId());
        assertFalse(sah.aclSystemKickstartSessionExists(ctx, null));

        // Test positive
        KickstartData k = KickstartDataTest.createTestKickstartData(user.getOrg());
        KickstartSession sess = KickstartSessionTest.createKickstartSession(k, user);
        ctx.put("sid", sess.getOldServer().getId());
        KickstartFactory.saveKickstartSession(sess);
        flushAndEvict(sess);
        assertTrue(sah.aclSystemKickstartSessionExists(ctx, null));
    }

}
