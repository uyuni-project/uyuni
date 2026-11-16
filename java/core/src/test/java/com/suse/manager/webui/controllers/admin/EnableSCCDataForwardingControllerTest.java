/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.suse.manager.webui.controllers.admin;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.common.RhnConfiguration;
import com.redhat.rhn.domain.common.RhnConfigurationFactory;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.testing.TestStatics;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.manager.webui.controllers.BaseControllerTestCase;

import org.junit.jupiter.api.Test;

public class EnableSCCDataForwardingControllerTest extends BaseControllerTestCase {

    private static final String URL = "/manager/admin/enable-scc-data-forwarding";

    private final RhnConfigurationFactory factory = RhnConfigurationFactory.getSingleton();

    @Test
    public void testOrgAdminCannotEnableForwarding() {
        factory.setValue(RhnConfiguration.KEYS.SCC_FORWARD_REGISTRATION, false);

        assertThrows(PermissionException.class, () ->
                EnableSCCDataForwardingController.enable(getRequestWithCsrf(URL), response, user));
        assertFalse(factory.getBoolean(RhnConfiguration.KEYS.SCC_FORWARD_REGISTRATION));
    }

    @Test
    public void testSatAdminCanEnableForwarding() {
        factory.setValue(RhnConfiguration.KEYS.SCC_FORWARD_REGISTRATION, false);
        User satAdmin = UserTestUtils.createUser(TestStatics.TEST_SAT_USER, user.getOrg().getId());
        satAdmin.addPermanentRole(RoleFactory.SAT_ADMIN);

        EnableSCCDataForwardingController.enable(getRequestWithCsrf(URL), response, satAdmin);

        assertTrue(factory.getBoolean(RhnConfiguration.KEYS.SCC_FORWARD_REGISTRATION));
    }
}
