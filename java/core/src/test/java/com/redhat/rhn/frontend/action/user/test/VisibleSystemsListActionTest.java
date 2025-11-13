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
package com.redhat.rhn.frontend.action.user.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.server.ServerConstants;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.user.VisibleSystemsListAction;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.testing.ActionHelper;
import com.redhat.rhn.testing.RhnBaseTestCase;

import org.apache.struts.action.Action;
import org.junit.jupiter.api.Test;

/**
 * VisibleSystemsListActionTest
 */
public class VisibleSystemsListActionTest extends RhnBaseTestCase {

    @Test
    public void testSelectAll() throws Exception {
        Action action = new VisibleSystemsListAction();
        ActionHelper ah = new ActionHelper();
        ah.setUpAction(action);
        ah.setupClampListBounds();


        User user = ah.getUser();
        ServerFactoryTest.createTestServer(user, true,
                        ServerConstants.getServerGroupTypeEnterpriseEntitled());

        ah.getRequest().addParameter("newset", (String[]) null);
        ah.getRequest().addParameter("items_on_page", (String[]) null);
        ah.getRequest().addParameter("items_selected", (String[]) null);
        ah.getRequest().addParameter("uid", user.getId().toString());

        RhnSetDecl.SYSTEMS.clear(user);
        assertTrue(RhnSetDecl.SYSTEMS.get(user).isEmpty());
        ah.executeAction("selectall");
        assertFalse(RhnSetDecl.SYSTEMS.get(user).isEmpty());
    }
}
