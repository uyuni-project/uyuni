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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.frontend.action.common.BadParameterException;
import com.redhat.rhn.frontend.action.user.AddressesAction;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.testing.ActionHelper;
import com.redhat.rhn.testing.RhnBaseTestCase;

import org.junit.jupiter.api.Test;

/**
 * AddressesActionTest
 */
public class AddressesActionTest extends RhnBaseTestCase {

    @Test
    public void testPerformExecute() throws Exception {
        AddressesAction action =
            new AddressesAction();
        ActionHelper sah = new ActionHelper();
        sah.setUpAction(action);
        sah.getRequest().setRequestURL("foo");
        sah.executeAction();

        // verify the dyna form got the right values we expected.
        assertNotNull(sah.getRequest().getAttribute(RhnHelper.TARGET_ADDRESS_MARKETING));
        assertNull(sah.getRequest().getAttribute(RhnHelper.TARGET_ADDRESS_BILLING));
        assertNull(sah.getRequest().getAttribute(RhnHelper.TARGET_ADDRESS_SHIPPING));
        assertNotNull(sah.getRequest().getAttribute(RhnHelper.TARGET_USER));
    }

    @Test
    public void testNoParamExecute() throws Exception {
        AddressesAction action = new AddressesAction();
        ActionHelper sah = new ActionHelper();
        sah.setUpAction(action);
        sah.getRequest().setRequestURL("rdu.redhat.com/rhn/users/Adresses.do");

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

    @Test
    public void testExecuteWithLoggedInUserNoAddress() throws Exception {
        AddressesAction action =
            new AddressesAction();
        ActionHelper sah = new ActionHelper();
        sah.setUpAction(action);
        sah.getRequest().setRequestURL("foo");
        sah.getRequest().getParameter("uid");
        sah.getRequest().addParameter("uid", (String) null);

        sah.executeAction();

        // verify request has a User
        assertNotNull(sah.getRequest().getAttribute(RhnHelper.TARGET_USER));
    }
}
