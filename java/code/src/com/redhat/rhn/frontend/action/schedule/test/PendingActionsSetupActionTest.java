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
package com.redhat.rhn.frontend.action.schedule.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.action.test.ActionFactoryTest;
import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.rhnset.RhnSetManager;
import com.redhat.rhn.testing.RhnPostMockStrutsTestCase;
import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * PendingActionsSetupActionTest
 */
public class PendingActionsSetupActionTest extends RhnPostMockStrutsTestCase {



    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        setRequestPathInfo("/schedule/PendingActions");
    }




    @Test
    public void testPerformExecute() {


        actionPerform();
        verifyForwardPath("/WEB-INF/pages/schedule/pendingactions.jsp");
        Object test = request.getAttribute("dataset");
        assertNotNull(test);

    }

    @Test
    public void testPerformSubmit() throws Exception {


        Server server = ServerFactoryTest.createTestServer(user);

        Action act = ActionFactoryTest.createAction(user, ActionFactory.TYPE_ERRATA);
        ServerAction sAction = ActionFactoryTest.createServerAction(server, act);
        sAction.setStatus(ActionFactory.STATUS_QUEUED);
        TestUtils.saveAndFlush(sAction);


        RhnSet set = RhnSetDecl.ACTIONS_PENDING.get(user);
        set.addElement(act.getId());
        RhnSetManager.store(set);

        request.addParameter(RhnAction.SUBMITTED, "true");
        request.addParameter("dispatch", "Cancel Actions");
        actionPerform();
        verifyForwardPath("/schedule/PendingActionsDeleteConfirm.do");


    }

}
