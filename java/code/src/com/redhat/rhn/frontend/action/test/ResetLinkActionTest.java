/*
 * Copyright (c) 2015 Red Hat, Inc.
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
package com.redhat.rhn.frontend.action.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.db.ResetPasswordFactory;
import com.redhat.rhn.domain.common.ResetPassword;
import com.redhat.rhn.frontend.action.common.BadParameterException;
import com.redhat.rhn.frontend.action.user.ResetLinkAction;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * ResetLinkActionTest
 */
public class ResetLinkActionTest extends BaseTestCaseWithUser {

    private ActionForward valid, invalid;
    private ActionMapping mapping;
    private Mockery context;
    private DynaActionForm form;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private ResetLinkAction action;

    @Test
    public void testPerformNoToken() {
        try {
            // simulate missing "token" parameter
            context.checking(new Expectations() {{
                allowing(request).getParameter("token"); will(returnValue(null));
            }});
            action.execute(mapping, form, request, response);
        }
        catch (BadParameterException bpe) {
            assertTrue(true, "Caught BPE");
            return;
        }
        fail("Expected BadParameterException, didn't get one!");
    }

    @Test
    public void testPerformInvalidToken() {
        ResetPassword rp = ResetPasswordFactory.createNewEntryFor(user);
        ResetPasswordFactory.invalidateToken(rp.getToken());

        context.checking(new Expectations() {{
            allowing(request).getParameter("token"); will(returnValue(rp.getToken()));
            allowing(request).getAttribute(org.apache.struts.Globals.ERROR_KEY);
            will(returnValue(null));
            allowing(request).setAttribute(with(any(String.class)), with(any(Object.class)));
        }});
        ActionForward rc = action.execute(mapping, form, request, response);
        assertEquals(invalid, rc);
    }

    @Test
    public void testPerformValidToken() {
        ResetPassword rp = ResetPasswordFactory.createNewEntryFor(user);

        context.checking(new Expectations() {{
            allowing(request).getParameter("token"); will(returnValue(rp.getToken()));
        }});

        ActionForward rc = action.execute(mapping, null, request, response);
        assertEquals(valid, rc);
    }

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        // Initialize jMock context with ByteBuddy for concrete classes if needed
        context = new Mockery();
        context.setImposteriser(ByteBuddyClassImposteriser.INSTANCE);

        form = context.mock(DynaActionForm.class);

        request = context.mock(HttpServletRequest.class);
        response = context.mock(HttpServletResponse.class);
        session = context.mock(HttpSession.class);

        // Setup the ResetLinkAction and mapping
        action = new ResetLinkAction();
        mapping = new ActionMapping();

        valid = new ActionForward("valid", "path", false);
        invalid = new ActionForward("invalid", "path", false);
        mapping.addForwardConfig(valid);
        mapping.addForwardConfig(invalid);

        // Default expectations for request/session
        context.checking(new Expectations() {{
            allowing(request).getSession(); will(returnValue(session));
            allowing(request).getMethod(); will(returnValue("GET"));
            allowing(request).getServerName(); will(returnValue("mymachine.rhndev.redhat.com"));
        }});
    }
}
