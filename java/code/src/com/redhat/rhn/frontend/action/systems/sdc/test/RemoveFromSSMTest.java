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
package com.redhat.rhn.frontend.action.systems.sdc.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.rhnset.RhnSetManager;
import com.redhat.rhn.testing.RhnMockStrutsTestCase;
import com.redhat.rhn.testing.ServerTestUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * SystemChannelsActionTest
 */
public class RemoveFromSSMTest extends RhnMockStrutsTestCase {

    private Server server;

    /**
     * {@inheritDoc}
     */
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        server = ServerTestUtils.createTestSystem(user);

        addRequestParameter(RequestContext.SID, server.getId().toString());
        setRequestPathInfo("/systems/details/RemoveFromSSM");

        RhnSet set = RhnSetDecl.SYSTEMS.get(user);
        set.clear();
        set.addElement(server.getId());
        RhnSetManager.store(set);

    }


    @Test
    public void testExecute() {

        actionPerform();
        assertEquals(request.getParameter("sid"), server.getId().toString());
        RhnSet set = RhnSetDecl.SYSTEMS.get(user);
        assertFalse(set.contains(server.getId()));

    }

}
