/**
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
package com.redhat.rhn.frontend.action.user.test;

import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.frontend.action.user.CreateUserAction;
import com.redhat.rhn.frontend.action.user.UserActionHelper;
import com.redhat.rhn.testing.RhnMockDynaActionForm;
import com.redhat.rhn.testing.RhnPostMockStrutsTestCase;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.webapp.RhnServletListener;

/**
 * CreateUserActionTest - Test the CreateUserAction
 *
 * @version $Rev: 1427 $
 */
public class CreateUserActionTest extends RhnPostMockStrutsTestCase {

    private static RhnServletListener rl;

    public void testMessageQueueRegistration() {
        rl = new RhnServletListener();
        rl.contextInitialized(null);
        String[] names = MessageQueue.getRegisteredEventNames();
        boolean found = false;
        for (int i = 0; i < names.length; i++) {
            if (names[i].equals("com.redhat.rhn.frontend.events.NewUserEvent")) {
                found = true;
            }
        }
        assertTrue(found);
        //don't call contextDestroyed here since it stops hibernate and
        //screws everything up ;)
        //rl.contextDestroyed(null);
        MessageQueue.stopMessaging();
    }

    public void testNewUserIntoOrgSatellite() throws Exception {


        setRequestPathInfo("/newlogin/CreateSatelliteSubmit");
        RhnMockDynaActionForm form =
            fillOutForm("createSatelliteForm", CreateUserAction.TYPE_INTO_ORG);
        setActionForm(form);
        actionPerform();
        String forwardPath = getActualForward();
        assertNotNull(forwardPath);
        assertTrue(forwardPath.startsWith("/users/ActiveList.do?uid="));
    }

    public void testCreateFirstSatUser() {
        // ensure no other user exists
        for (Org org : OrgFactory.lookupAllOrgs()) {
            for (User user : UserFactory.getInstance().findAllUsers(org)) {
                UserFactory.deleteUser(user.getId());
            }
        }
        setRequestPathInfo("/newlogin/CreateFirstUserSubmit");
        RhnMockDynaActionForm form =
            fillOutForm("createSatelliteForm", CreateUserAction.TYPE_CREATE_SAT);
        setActionForm(form);
        actionPerform();
        this.verifyForward(CreateUserAction.SUCCESS_SAT);
    }

    /**
     * Ensure this action does not work when one user exists (CVE-2013-4480,
     * bnc#848639)
     */
    public void testCreateMultipleFirstSatUser() {
        UserFactory.createUser();
        setRequestPathInfo("/newlogin/CreateFirstUserSubmit");
        RhnMockDynaActionForm form = fillOutForm("createSatelliteForm",
            CreateUserAction.TYPE_CREATE_SAT);
        setActionForm(form);
        actionPerform();
        this.verifyForwardPath("/errors/Permission.do");
    }

    /**
     * @return Properly filled out user creation form.
     */
    private RhnMockDynaActionForm fillOutForm(String formName, String accountType) {
        RhnMockDynaActionForm f = new RhnMockDynaActionForm(formName);
        f.set("login", "testUser" + TestUtils.randomString());
        f.set("account_type", accountType);
        f.set("address1", "123 somewhere ln");
        f.set("address2", "");
        f.set("city", "Cincinnati");
        f.set("contact_email", new Boolean(true));
        f.set("contact_fax", new Boolean(true));
        f.set("contact_partner", "");
        f.set("company", "Red Hat");
        f.set("country", "US");
        f.set("email", "foobar@redhat.com");
        f.set("fax", "");
        f.set("firstNames", "CreateUserActionTest fname");
        f.set("lastName", "CreateUserActionTest lname");
        f.set(UserActionHelper.DESIRED_PASS, "password");
        f.set(UserActionHelper.DESIRED_PASS_CONFIRM, "password");
        f.set("phone", "123-123-1234");
        f.set("prefix", "Mr.");
        f.set("state", "OH");
        f.set("title", "Heavyweight");
        f.set("zip", "45241");
        f.set("timezone", new Integer(7010));
        f.set("preferredLocale", "en_US");
        return f;
    }
}
