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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.access.AccessGroupFactory;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.common.BadParameterException;
import com.redhat.rhn.frontend.action.user.UserEditSetupAction;
import com.redhat.rhn.frontend.action.user.UserRoleStatusBean;
import com.redhat.rhn.testing.ActionHelper;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.RhnMockDynaActionForm;
import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * UserEditSetupActionTest
 */
public class UserEditSetupActionTest extends RhnBaseTestCase {

    @Test
    public void testPerformExecute() throws Exception {
        UserEditSetupAction action = new UserEditSetupAction();
        ActionHelper sah = new ActionHelper();
        sah.setUpAction(action);
        sah.getRequest().setRequestURL("foo");

        User user = sah.getUser();
        user.setTitle("Test title");
        // Lets add some roles
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        user.getAccessGroups().addAll(AccessGroupFactory.DEFAULT_GROUPS);

        // Below we test to make sure that some of
        // the strings in the form are localized
        TestUtils.enableLocalizationDebugMode();
        try {
            sah.executeAction();

            // verify the dyna form got the right values we expected.
            RhnMockDynaActionForm form = sah.getForm();
            assertEquals(user.getId(), form.get("uid"));
            assertEquals(user.getFirstNames(), form.get("firstNames"));
            assertEquals(user.getLastName(), form.get("lastName"));
            assertEquals(user.getTitle(), form.get("title"));
            assertEquals(user.getPrefix(), form.get("prefix"));

            assertEquals(sah.getUser().getLastLoggedIn(),
                    sah.getRequest().getAttribute("lastLoggedIn"));
            // Verify some more intensive stuff
            assertNotNull(sah.getRequest().getAttribute("adminRoles"));
            assertNotNull(sah.getRequest().getAttribute("rbacRoles"));
            List<UserRoleStatusBean> rbacRoles = (List<UserRoleStatusBean>)
                sah.getRequest().getAttribute("rbacRoles");
            assertEquals(6, rbacRoles.size());
            UserRoleStatusBean lv = rbacRoles.get(0);
            assertTrue(TestUtils.isLocalized(lv.getName()));
            assertNotNull(sah.getRequest().getAttribute("disabledRoles"));
            assertInstanceOf(User.class, sah.getRequest().getAttribute("user"));

            //If we have pam setup where we're testing, make sure displaypam was set
            String pamAuthService = Config.get().getString(
                    ConfigDefaults.WEB_PAM_AUTH_SERVICE);
            if (pamAuthService != null && !pamAuthService.trim().isEmpty()) {
                assertNotNull(sah.getRequest().getAttribute("displaypam"));
            }
        }
        finally {
            TestUtils.disableLocalizationDebugMode();
        }
    }

    @Test
    public void testNoParamExecute() throws Exception {
        UserEditSetupAction action = new UserEditSetupAction();
        ActionHelper sah = new ActionHelper();
        sah.setUpAction(action);
        sah.getRequest().setRequestURL("rdu.redhat.com/rhn/users/UserDetails.do");

        sah.getRequest().addParameter("uid", (String)null);
        sah.getRequest().getParameterValues("uid"); //now uid = null

        try {
            sah.executeAction();
            fail(); //should never get this far
        }
        catch (BadParameterException e) {
            //no op
        }
    }

}
