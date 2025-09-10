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

import com.redhat.rhn.common.db.ResetPasswordFactory;
import com.redhat.rhn.domain.common.ResetPassword;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.frontend.action.user.ResetPasswordSubmitAction;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.RhnMockDynaActionForm;
import com.redhat.rhn.testing.RhnMockHttpServletRequest;
import com.redhat.rhn.testing.RhnMockHttpServletResponse;
import com.redhat.rhn.testing.RhnMockHttpSession;
import com.redhat.rhn.testing.UserTestUtils;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * ResetPasswordSubmitActionTest
 */
public class ResetPasswordSubmitActionTest extends BaseTestCaseWithUser {

    private ActionForward mismatch, invalid, badpwd;
    private ActionMapping mapping;
    private DynaActionForm form;
    private RhnMockHttpServletRequest request;
    private RhnMockHttpServletResponse response;
    private ResetPasswordSubmitAction action;
    private User adminUser;

    @Test
    public void testPerformNoToken() {
        form.set("token", null);
        ActionForward rc = action.execute(mapping, form, request, response);
        assertEquals(invalid.getName(), rc.getName(), "No token");
    }

    @Test
    public void testPerformInvalidToken() {
        ResetPassword rp = ResetPasswordFactory.createNewEntryFor(user);
        ResetPasswordFactory.invalidateToken(rp.getToken());
        form.set("token", rp.getToken());
        ActionForward rc = action.execute(mapping, form, request, response);
        assertEquals(invalid.getName(), rc.getName(), "Invalid token");
    }

    @Test
    public void testPerformDisabledUser() {
        ResetPassword rp = ResetPasswordFactory.createNewEntryFor(user);
        UserFactory.getInstance().disable(user, adminUser);
        form.set("token", rp.getToken());
        ActionForward rc = action.execute(mapping, form, request, response);
        assertEquals(invalid.getName(), rc.getName(), "Disabled user");
    }

    @Test
    public void testPerformPasswordMismatch() {
        ResetPassword rp = ResetPasswordFactory.createNewEntryFor(user);
        form.set("token", rp.getToken());
        form.set("password", "foobar");
        form.set("passwordConfirm", "foobarblech");
        ActionForward rc = action.execute(mapping, form, request, response);
        assertEquals(mismatch.getName(), rc.getName());
    }

    @Test
    public void testPerformBadPassword() {
        ResetPassword rp = ResetPasswordFactory.createNewEntryFor(user);
        form.set("token", rp.getToken());
        form.set("password", "a");
        form.set("passwordConfirm", "a");
        ActionForward rc = action.execute(mapping, form, request, response);
        assertEquals(badpwd.getName(), rc.getName(), "too short");

        form.set("password",
        "12345678901234567890123456789012345678901234567890123456789012345678901234567890");
        form.set("passwordConfirm",
        "12345678901234567890123456789012345678901234567890123456789012345678901234567890");
        rc = action.execute(mapping, form, request, response);
        assertEquals(badpwd.getName(), rc.getName(), "too long");

        form.set("password", "123\t\n6");
        form.set("passwordConfirm", "123\t\n6");
        rc = action.execute(mapping, form, request, response);
        assertEquals(badpwd.getName(), rc.getName(), "whitespace");
    }

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        adminUser = UserTestUtils.findNewUser("testAdminUser", "testOrg" +
                        this.getClass().getSimpleName(), true);
        action = new ResetPasswordSubmitAction();

        mapping = new ActionMapping();
        mismatch = new ActionForward("mismatch", "path", false);
        invalid = new ActionForward("invalid", "path", false);
        badpwd = new ActionForward("badpwd", "path", false);
        form = new RhnMockDynaActionForm("resetPasswordForm");
        request = new RhnMockHttpServletRequest();
        response = new RhnMockHttpServletResponse();

        RequestContext requestContext = new RequestContext(request);

        RhnMockHttpSession mockSession = new RhnMockHttpSession();
        mockSession.setAttribute("token", null);
        mockSession.setAttribute("request_method", "GET");
        request.setSession(mockSession);
        request.setServerName("mymachine.rhndev.redhat.com");
        requestContext.getWebSession();

        mapping.addForwardConfig(mismatch);
        mapping.addForwardConfig(invalid);
        mapping.addForwardConfig(badpwd);
    }
}
