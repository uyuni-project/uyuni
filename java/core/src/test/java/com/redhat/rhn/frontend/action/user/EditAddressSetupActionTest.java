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
package com.redhat.rhn.frontend.action.user;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.redhat.rhn.domain.user.Address;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.testing.ActionHelper;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.RhnMockDynaActionForm;

import org.junit.jupiter.api.Test;

/**
 * EditAddressActionTest
 */
public class EditAddressSetupActionTest extends RhnBaseTestCase {

    @Test
    public void testPerformExecuteWithAddr() throws Exception {
        EditAddressSetupAction action = new EditAddressSetupAction();
        ActionHelper sah = new ActionHelper();
        sah.setUpAction(action);

        User user = sah.getUser();
        Address address = user.getAddress();
        address.setPhone("555-1212");
        address.setFax("555-1212");

        sah.executeAction();

        assertNotNull(sah.getForm().get("uid"));
        RhnMockDynaActionForm form = sah.getForm();
        assertAll(
                "Check if the user information is correct",
                () -> assertEquals(address.getAddress1(), form.get("address1")),
                () -> assertEquals(address.getAddress2(), form.get("address2")),
                () -> assertEquals(address.getPhone(), form.get("phone")),
                () -> assertEquals(address.getFax(), form.get("fax")),
                () -> assertEquals(address.getCity(), form.get("city")),
                () -> assertEquals(address.getState(), form.get("state")),
                () -> assertEquals(address.getCountry(), form.get("country")),
                () -> assertEquals(address.getZip(), form.get("zip"))
        );
    }



}
