/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.common.BadParameterException;
import com.redhat.rhn.frontend.action.user.UserPrefSetupAction;
import com.redhat.rhn.testing.ActionHelper;
import com.redhat.rhn.testing.RhnBaseTestCase;

import org.apache.struts.action.DynaActionForm;
import org.junit.jupiter.api.Test;

/**
 * UserPrefSetupActionTest - Good **EXAMPLE** of a basic SetupActionTest class.
 */
public class UserPrefSetupActionTest extends RhnBaseTestCase {

    /**
     *
     * @throws Exception on server init failure
     */
    @Test
    public void testPerformExecute() throws Exception {
        UserPrefSetupAction action = new UserPrefSetupAction();
        ActionHelper sah = new ActionHelper();
        sah.setUpAction(action);
        sah.getRequest().setRequestURL("foo");
        sah.executeAction();

        // verify the dyna form got the right values we expected.
        User user = sah.getUser();
        DynaActionForm form = sah.getForm();
        assertEquals(user.getId(), form.get("uid"));
        assertEquals(user.getPageSize(), form.get("pagesize"));
    }

    /**
     *
     * @throws Exception on server init failure
     */
    @Test
    public void testNoParamExecute() throws Exception {
        UserPrefSetupAction action = new UserPrefSetupAction();
        ActionHelper sah = new ActionHelper();
        sah.setUpAction(action);
        sah.getRequest().setRequestURL("rdu.redhat.com/rhn/users/UserPreferences.do");
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
