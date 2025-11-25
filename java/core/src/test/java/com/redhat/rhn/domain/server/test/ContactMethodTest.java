/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.server.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.redhat.rhn.domain.server.ContactMethod;
import com.redhat.rhn.domain.server.ServerFactory;

import org.junit.jupiter.api.Test;

public class ContactMethodTest {

    @Test
    public void testContactMethods()  {
        ContactMethod defaultContactMethod = ServerFactory.findContactMethodById(0L);
        assertEquals(0L, defaultContactMethod.getId());
        assertEquals("default", defaultContactMethod.getLabel());
        //the name has to be the localization service one, and therefore NOT the name in the database field
        assertNotEquals("server.contact-method.default", defaultContactMethod.getName());

        ContactMethod defaultContactMethod2 = ServerFactory.findContactMethodByLabel("default");
        assertEquals(defaultContactMethod, defaultContactMethod2);

        ContactMethod pushContactMethod = ServerFactory.findContactMethodById(1L);
        assertEquals(1L, pushContactMethod.getId());
        assertEquals("ssh-push", pushContactMethod.getLabel());
        //the name has to be the localization service one, and therefore NOT the name in the database field
        assertNotEquals("server.contact-method.ssh-push", pushContactMethod.getName());

        ContactMethod pushContactMethod2 = ServerFactory.findContactMethodByLabel("ssh-push");
        assertEquals(pushContactMethod, pushContactMethod2);

        ContactMethod pushTunnelContactMethod = ServerFactory.findContactMethodById(2L);
        assertEquals(2L, pushTunnelContactMethod.getId());
        assertEquals("ssh-push-tunnel", pushTunnelContactMethod.getLabel());
        //the name has to be the localization service one, and therefore NOT the name in the database field
        assertNotEquals("server.contact-method.ssh-push-tunnel", pushTunnelContactMethod.getName());

        ContactMethod pushTunnelContactMethod2 = ServerFactory.findContactMethodByLabel("ssh-push-tunnel");
        assertEquals(pushTunnelContactMethod, pushTunnelContactMethod2);
    }
}
