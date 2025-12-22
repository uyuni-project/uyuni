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
package com.redhat.rhn.domain.action.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionFormatter;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.errata.ErrataFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * ActionFormatterTest  - test the formatters associated with the Actions.
 */
public class ActionFormatterTest extends RhnBaseTestCase {

    private User user;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        user = UserTestUtils.createUser(this);
    }

    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test formatting an Action
     * @throws Exception something bad happened
     */
    @Test
    public void testActionFormatter() throws Exception {
        Action a = ActionFactoryTest.createAction(user,
                ActionFactory.TYPE_HARDWARE_REFRESH_LIST);
        a.setSchedulerUser(user);

        ActionFormatter af = a.getFormatter();
        assertNotNull(af);
        assertEquals("RHN-JAVA Test Action", af.getName());
        assertEquals("Hardware List Refresh", af.getActionType());
        assertEquals("(none)", af.getNotes());
        assertEquals(af.getScheduler(), a.getSchedulerUser().getLogin());
        assertNotNull(af.getEarliestDate());

    }

    /**
     * Test formatting an Action
     * @throws Exception something bad happened
     */
    @Test
    public void testActionLinks() throws Exception {
        // We know that TYPE_REBOOT has ServerActions associated with it
        Action areboot = ActionFactoryTest.createAction(user,
                ActionFactory.TYPE_REBOOT);
        ActionFormatter af = areboot.getFormatter();
        ServerAction sa = (ServerAction) areboot.getServerActions().toArray()[0];
        sa.setStatusFailed();
        TestUtils.saveAndReload(sa);
        assertTrue(af.getNotes().startsWith(
                "<a href=\"/rhn/schedule/FailedSystems.do?aid="));
        assertTrue(af.getNotes().endsWith(
                ">1 system</a></strong> failed to complete this action.<br/><br/>"));

        sa.setStatusCompleted();
        TestUtils.saveAndReload(sa);
        assertTrue(af.getNotes().startsWith(
                "<a href=\"/rhn/schedule/CompletedSystems.do?aid="));
        assertTrue(af.getNotes().endsWith(
                ">1 system</a></strong> successfully completed this action.<br/><br/>"));

    }

    /**
     * Test formatting an Action
     * @throws Exception something bad happened
     */
    @Test
    public void testErrataFormatter() throws Exception {

        Action a = ActionFactoryTest.createAction(user, ActionFactory.TYPE_ERRATA);
        ActionFormatter af = a.getFormatter();
        assertNotNull(af);
        assertEquals("Patch Update", af.getActionType());
        String start = "<strong><a href=\"/rhn/errata/details/Details.do?eid=";
        String end = "</a></strong><br/><br/><strong>Test synopsis</strong><br/>" +
            "<br/>" + ErrataFactory.ERRATA_TYPE_BUG +
            "<br/><br/>test topic<br/>Test desc ..<br/><br/>";
        assertTrue(af.getNotes().startsWith(start));
        assertTrue(af.getNotes().endsWith(end));
    }

    /**;
     * Test formatting an Action
     * @throws Exception something bad happened
     */
    @Test
    public void testScriptFormatter() throws Exception {

        Action a = ActionFactoryTest.createAction(user, ActionFactory.TYPE_SCRIPT_RUN);
        a = (Action) reload(a);
        a.setSchedulerUser(user);
        ActionFormatter af = a.getFormatter();
        assertNotNull(af);
        assertEquals("Run an arbitrary script", af.getActionType());
        String start = "Run as: <strong>AFTestTestUser:AFTestTestGroup";
        String end = "</strong><br/><br/><div style=\"padding-left: 1em\">" +
            "<code>#!/bin/csh<br/>ls -al</code></div><br/>";
        assertTrue(af.getNotes().startsWith(start));
        assertTrue(af.getNotes().endsWith(end));

    }

}

