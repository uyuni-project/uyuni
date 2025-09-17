/*
 * Copyright (c) 2009--2015 Red Hat, Inc.
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

package com.redhat.rhn.frontend.events.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.messaging.Mail;
import com.redhat.rhn.common.messaging.test.MockMail;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.events.NewUserAction;
import com.redhat.rhn.frontend.events.NewUserEvent;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.UserTestUtils;

import com.mockobjects.servlet.MockHttpServletRequest;
import com.mockobjects.servlet.MockHttpSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

/**
 * Test for NewUserEvent
 */

public class NewUserEventTest extends RhnBaseTestCase {

    private MockMail mailer;

    @Override
    @BeforeEach
    public void setUp() {
        mailer = new MockMail();
    }

    /**
     * test that makes sure we can instantiate the service
     */
    @Test
    public void testToText() {
        NewUserEvent evt = createTestEvent();
        String eventText = evt.toText();
        System.out.println(eventText);
        assertNotNull(eventText);
        assertContains(eventText, "A SUSE Manager login has been created for you");
        assertContains(eventText,
                "SUSE Manager login, in combination with an active SUSE subscription,");
        assertContains(eventText, "e-mail: javaTest@example.com");

    }

    @Test
    public void testAction() {
        NewUserEvent evt = createTestEvent();
        mailer.setExpectedSendCount(2);
        NewUserAction action = new NewUserAction() {
            @Override
            protected Mail getMail() {
                return mailer;
            }
        };
        action.execute(evt);
        mailer.verify();
        assertContains(mailer.getSubject(), "SUSE Manager User Created: testUser");
        assertContains(mailer.getBody(),
                "someserver.rhndev.redhat.com/rhn/users/ActiveList.do");

        assertTrue(mailer.getBody().contains("Your SUSE Manager login:         testUser") ||
                   mailer.getBody().contains("Your RHN login:         testUser"));
        assertTrue(mailer.getBody().contains("Your SUSE Manager email address: " +
                    "javaTest@example.com") ||
                   mailer.getBody().contains("Your RHN email address: " +
                "javaTest@example.com"));
    }

    private NewUserEvent createTestEvent() {
        NewUserEvent evt = new NewUserEvent();
        // In the implementation we use getHeaderNames so we override it with
        // one that returns an empty implementation.
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public Enumeration<String> getHeaderNames() {
                return new Vector<String>().elements();
            }
        };
        request.setSession(new MockHttpSession());
        request.setupGetRequestURI("http://localhost:8080");
        request.setupGetMethod("POST");
        User usr = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());

        evt.setUser(usr);
        evt.setDomain("someserver.rhndev.redhat.com");
        evt.setAdmins(createAdmins());
        evt.setRequest(request);
        return evt;
    }

    private List<User> createAdmins() {
        User adminOne = UserTestUtils.findNewUser("testUserOne", "testOrgOne", true);
        User adminTwo = UserTestUtils.findNewUser("testUserTwo", "testOrgTwo", true);
        List<User> admins = new ArrayList<>();
        admins.add(adminOne);
        admins.add(adminTwo);
        return admins;
    }

}
