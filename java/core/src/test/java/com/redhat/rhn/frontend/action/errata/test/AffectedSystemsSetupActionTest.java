/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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
package com.redhat.rhn.frontend.action.errata.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.test.ErrataFactoryTest;
import com.redhat.rhn.frontend.dto.SystemOverview;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.testing.RhnMockStrutsTestCase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * AffectedSystemsSetupActionTest
 */
public class AffectedSystemsSetupActionTest extends RhnMockStrutsTestCase {

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        setRequestPathInfo("/errata/details/SystemsAffected");

    }
    @Test
    public void testBadParams() {
        actionPerform();
        assertBadParamException();
    }

    @Test
    public void testInvalidParams() {
        addRequestParameter("eid", "-99999");
        actionPerform();
        assertLookupException();
    }

    @Test
    public void testNormalCase() throws Exception {
        Errata e = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        addRequestParameter("eid", e.getId().toString());
        actionPerform();
        DataResult dr = (DataResult) request.getAttribute(RequestContext.PAGE_LIST);
        assertNotNull(dr);
        for (Object oIn : dr) {
            SystemOverview s = (SystemOverview) oIn;
            assertNotNull(s.getEntitlementLevel());
        }
        assertNotNull(request.getAttribute("errata"));
        assertNotNull(request.getAttribute("set"));
    }

}
