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
package com.redhat.rhn.frontend.action.systems.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.test.ErrataFactoryTest;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.manager.errata.cache.ErrataCacheManager;
import com.redhat.rhn.testing.RhnMockStrutsTestCase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * ErrataSetupActionTest
 */
public class ErrataSetupActionTest extends RhnMockStrutsTestCase {
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        setRequestPathInfo("/systems/details/ErrataList");
    }
    @Test
    public void testInvalidParamCase() {
        addRequestParameter(RequestContext.SID, "-9999");
        actionPerform();
        assertPermissionException();
    }

    @Test
    public void testNormalCase() throws Exception {
        Server server = ServerFactoryTest.createTestServer(user, true);
        addRequestParameter("allowVendorChange", "false");
        addRequestParameter("sid", server.getId().toString());
        Errata e = ErrataFactoryTest.createTestErrata(user.getOrg().getId());

        for (Package pkg : e.getPackages()) {
            ErrataCacheManager.insertNeededErrataCache(server.getId(),
                    e.getId(), pkg.getId());
        }

        actionPerform();
        assertNotNull(request.getAttribute("set"));
        assertNotNull(request.getAttribute("system"));

        //trying show bttn logic
        assertEquals(Boolean.TRUE.toString(),
                    request.getAttribute("showApplyErrata"));
        assertTrue(getActualForward().contains("errata.jsp"));

        assertEquals("true", request.getAttribute("showApplyErrata"));
        clearRequestParameters();
        addRequestParameter("sid", server.getId().toString());
        addRequestParameter("allowVendorChange", new String[]{ "false" });
        for (Package pkg : e.getPackages()) {
            ErrataCacheManager.deleteNeededCache(server.getId(),
                    e.getId(), pkg.getId());
        }
        actionPerform();
        assertEquals(Boolean.FALSE.toString(),
                request.getAttribute("showApplyErrata"));
    }

}
