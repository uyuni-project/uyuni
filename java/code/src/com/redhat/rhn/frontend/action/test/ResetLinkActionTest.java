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
import com.redhat.rhn.testing.MockDynaActionForm;
import com.redhat.rhn.testing.MockHttpServletRequest;
import com.redhat.rhn.testing.MockHttpServletResponse;
import com.redhat.rhn.testing.MockHttpSession;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * ResetLinkActionTest
 */
public class ResetLinkActionTest extends BaseTestCaseWithUser {

    private ActionForward valid, invalid;
    private ActionMapping mapping;
    private MockDynaActionForm form;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private ResetLinkAction action;

    @Test
    public void testPerformNoToken() {
        try {
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
        request.setupAddParameter("token", rp.getToken());
        ActionForward rc = action.execute(mapping, form, request, response);
        assertEquals(invalid, rc);
    }

    @Test
    public void testPerformValidToken() {
        ResetPassword rp = ResetPasswordFactory.createNewEntryFor(user);
        request.setupAddParameter("token", rp.getToken());
        ActionForward rc = action.execute(mapping, form, request, response);
        assertEquals(valid, rc);
    }

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        action = new ResetLinkAction();

        mapping = new ActionMapping();
        valid = new ActionForward("valid", "path", false);
        invalid = new ActionForward("invalid", "path", false);
        form = new MockDynaActionForm("resetPasswordForm");
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        MockHttpSession mockSession = new MockHttpSession();
        mockSession.setAttribute("token", null);
        mockSession.setAttribute("request_method", "GET");
        request.setSession(mockSession);
        request.setupServerName("mymachine.rhndev.redhat.com");

        mapping.addForwardConfig(valid);
        mapping.addForwardConfig(invalid);
    }
}
