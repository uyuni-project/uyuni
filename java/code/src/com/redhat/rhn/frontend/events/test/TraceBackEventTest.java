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

package com.redhat.rhn.frontend.events.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.messaging.Mail;
import com.redhat.rhn.common.messaging.test.MockMail;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.events.TraceBackAction;
import com.redhat.rhn.frontend.events.TraceBackEvent;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.RhnMockHttpServletRequest;
import com.redhat.rhn.testing.UserTestUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link TraceBackEvent}.
 */

public class TraceBackEventTest extends RhnBaseTestCase {

    private static final String MSG_OUTER_EXC = "outer-exception";
    private static final String MSG_INNER_EXC = "inner-exception";
    private final User user = UserTestUtils.findNewUser("testUser", "testOrg" + this.getClass().getSimpleName());

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
        TraceBackEvent evt = createTestEvent();
        String eventText = evt.toText();
        assertNotNull(eventText);
        assertContains(eventText, MSG_INNER_EXC);
        assertContains(eventText, MSG_OUTER_EXC);
        assertContains(eventText, "Request");
        assertContains(eventText, "User");
        assertContains(eventText, "Exception");
        //with null exception
        evt.setException(null);
        eventText = evt.toText();
        assertContains(eventText, "Request");
        assertContains(eventText, "User");
        assertContains(eventText, "Exception");
    }

    @Test
    public void testProtectPassword() {
        TraceBackEvent evt = createTestEventWithValue("password", "no-secret");
        mailer.setExpectedSendCount(1);
        TraceBackAction action = new TraceBackAction() {
            @Override
            protected Mail getMail() {
                return mailer;
            }
        };
        action.execute(evt);
        mailer.verify();
        String body = mailer.getBody();
        assertTrue(body.indexOf("password") > 0);
        assertTrue(body.indexOf("password: " + evt.getHashMarks()) > 0);
    }

    @Test
    public void testNoPassword() {
        TraceBackEvent evt = createTestEventWithValue("passsword", "no-secret");
        mailer.setExpectedSendCount(1);
        TraceBackAction action = new TraceBackAction() {
            @Override
            protected Mail getMail() {
                return mailer;
            }
        };
        action.execute(evt);
        mailer.verify();
        String body = mailer.getBody();
        assertFalse(body.indexOf("passsword: " + evt.getHashMarks()) > 0);
    }

    @Test
    public void testToTextWithNulls() {
        TraceBackEvent evt = new TraceBackEvent();
        evt.setRequest(null);
        evt.setUser(null);
        evt.setException(new RuntimeException(MSG_OUTER_EXC));
        String eventText = evt.toText();
        assertContains(eventText, MSG_OUTER_EXC);
        assertContains(eventText, "No User logged in");
        assertContains(eventText, "No request information");
    }

    @Test
    public void testToTextWithValues() {
        TraceBackEvent evt = createTestEventWithValue("numbers", "184634294");
        String eventText = evt.toText();
        assertContains(eventText, "numbers: <digits only>");

        evt = createTestEventWithValue("empty", "");
        eventText = evt.toText();
        assertContains(eventText, "empty: <empty>");

        evt = createTestEventWithValue("blank", "  ");
        eventText = evt.toText();
        assertContains(eventText, "blank: <blank>");

        evt = createTestEventWithValue("alpha", "aBczztTToOp");
        eventText = evt.toText();
        assertContains(eventText, "alpha: <alpha only>");

        evt = createTestEventWithValue("alphanumeric", "218hgdsioZttdnll99");
        eventText = evt.toText();
        assertContains(eventText, "alphanumeric: <alphanumeric only>");

        evt = createTestEventWithValue("asciiprint", "2+ 18hgdsioZttdd  hf ! ## nll99");
        eventText = evt.toText();
        assertContains(eventText, "asciiprint: <ascii printable>");

        evt = createTestEventWithValue("chars", "2+ 1ÖäÜ üä § 8hgdsioZttdd  hf ! ## nll99");
        eventText = evt.toText();
        assertContains(eventText, "chars: <characters>");
    }

    @Test
    public void testTraceBackAction() {
        TraceBackEvent evt = createTestEvent();
        mailer.setExpectedSendCount(1);
        TraceBackAction action = new TraceBackAction() {
            @Override
            protected Mail getMail() {
                return mailer;
            }
        };
        action.execute(evt);
        mailer.verify();
        assertEquals(0, mailer.getSubject().indexOf("WEB TRACEBACK from "));
        assertEquals(0, mailer.getBody().indexOf("The following exception occurred"));
        assertTrue(mailer.getBody().indexOf("Request:") > 0);
        assertTrue(mailer.getBody().indexOf("User Information:") > 0);
        assertTrue(mailer.getBody().indexOf("Exception:") > 0);
    }

    private TraceBackEvent createTestEvent() {
        return this.createTestEventWithValue("someparam", "somevalue");
    }

    private TraceBackEvent createTestEventWithValue(String paramIn, String valueIn) {
        TraceBackEvent evt = new TraceBackEvent();
        RhnMockHttpServletRequest request = new RhnMockHttpServletRequest();
        request.setRequestURI("http://localhost:8080");
        request.addParameter(paramIn, valueIn);
        evt.setUser(user);
        evt.setRequest(request);
        Throwable e = new RuntimeException(MSG_OUTER_EXC);
        e.initCause(new RuntimeException(MSG_INNER_EXC));
        evt.setException(e);
        return evt;
    }

}
