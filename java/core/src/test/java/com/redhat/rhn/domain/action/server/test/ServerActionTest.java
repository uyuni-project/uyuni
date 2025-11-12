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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.action.server.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.errata.ActionPackageDetails;
import com.redhat.rhn.domain.action.errata.ErrataAction;
import com.redhat.rhn.domain.action.salt.ApplyStatesAction;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.action.test.ActionFactoryTest;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.UserTestUtils;

import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.function.Consumer;

/**
 * ServerActionTest
 */
public class ServerActionTest extends RhnBaseTestCase {

    @Test
    public void testFail() {
        ServerAction sa1 = new ServerAction();
        sa1.fail(-1L, "Fail_message", new Date());
        assertEquals(sa1.getResultMsg(), "Fail_message");
        assertEquals(sa1.getResultCode(), Long.valueOf(-1));
        assertTrue(sa1.isStatusFailed());

        ServerAction sa2 = new ServerAction();
        sa2.fail(-1L, "Fail_message");
        assertEquals(sa2.getResultMsg(), "Fail_message");
        assertEquals(sa2.getResultCode(), Long.valueOf(-1));
        assertTrue(sa2.isStatusFailed());

        ServerAction sa3 = new ServerAction();
        sa3.fail("Fail_message");
        assertEquals(sa3.getResultMsg(), "Fail_message");
        assertEquals(sa3.getResultCode(), Long.valueOf(-1));
        assertTrue(sa3.isStatusFailed());
    }

    @Test
    public void testEquals() {
        ServerAction sa = new ServerAction();
        ServerAction sa2 = null;
        assertNotEquals(sa, sa2);

        sa2 = new ServerAction();
        assertEquals(sa, sa2);

        Server one = ServerFactory.createServer();
        one.setId(10001L);
        sa.setServerWithCheck(one);
        assertNotEquals(sa, sa2);
        assertNotEquals(sa2, sa);

        Server two = ServerFactory.createServer();
        two.setId(10001L); // same ID
        sa2.setServerWithCheck(two);
        assertEquals(sa, sa2);

        one.setName("foo");
        assertNotEquals(sa, sa2);

        sa2.setServerWithCheck(one);
        assertEquals(sa, sa2);

        Action parent = new ApplyStatesAction();
        parent.setId(243L);
        parent.setActionType(ActionFactory.TYPE_APPLY_STATES);
        sa.setParentActionWithCheck(parent);
        assertNotEquals(sa, sa2);

        sa2.setParentActionWithCheck(parent);
        assertEquals(sa, sa2);
    }

    @Test
    public void testCreate() throws Exception {
        User user = UserTestUtils.createUser(this);
        ErrataAction parent = (ErrataAction) ActionFactoryTest.createAction(user, ActionFactory.TYPE_ERRATA);
        new ActionPackageDetails();
        ServerAction child = createServerAction(ServerFactoryTest
                .createTestServer(user), parent);

        parent.addServerAction(child);
        ActionFactory.save(parent);

        assertNotNull(parent.getId());
        assertEquals(child.getParentAction(), parent);
        assertNotNull(parent.getServerActions());
        assertNotNull(parent.getServerActions().toArray()[0]);
        assertEquals(child, parent.getServerActions().toArray()[0]);
    }

    /**
     * Test fetching a ServerAction
     * @throws Exception something bad happened
     */
    @Test
    public void testLookupServerAction() throws Exception {
        Action newA = ActionFactoryTest.createAction(
                UserTestUtils.createUser(this),
                ActionFactory.TYPE_REBOOT);
        Long id = newA.getId();
        Action a = ActionFactory.lookupById(id);
        assertNotNull(a);
        assertNotNull(a.getServerActions());
        ServerAction sa = (ServerAction) a.getServerActions().toArray()[0];
        assertNotNull(sa);
        assertNotNull(sa.getParentAction());
    }

    /**
     * Create a new ServerAction
     * @param newS new server
     * @param newA new action
     * @return ServerAction created
     */
    public static ServerAction createServerAction(Server newS, Action newA) {
        return createServerAction(newS, newA, ServerAction::setStatusQueued);
    }

    /**
     * Create a new ServerAction
     * @param newS new server
     * @param newA new action
     * @param statusSetter method that set status to a ServerAction
     * @return ServerAction created
     */
    public static ServerAction createServerAction(Server newS, Action newA, Consumer<ServerAction> statusSetter) {
        ServerAction sa = new ServerAction();
        statusSetter.accept(sa);
        sa.setRemainingTries(sa.isStatusFailed() ? 0L : 10L);
        sa.setServerWithCheck(newS);
        sa.setParentActionWithCheck(newA);
        newA.addServerAction(sa);
        return sa;
    }
}
