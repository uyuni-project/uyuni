/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
 * Copyright (c) 2025 SUSE LLC
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
package com.redhat.rhn.frontend.xmlrpc;

import com.redhat.rhn.domain.access.AccessGroup;
import com.redhat.rhn.domain.kickstart.KickstartTestUtils;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.testing.BaseTestCase;
import com.redhat.rhn.testing.SaltTestCaseExtension;
import com.redhat.rhn.testing.TestStatics;
import com.redhat.rhn.testing.UserForTest;
import com.redhat.rhn.testing.UserForTest.UserRole;
import com.redhat.rhn.testing.UserForTestCaseExtension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({SaltTestCaseExtension.class, UserForTestCaseExtension.class})
public class BaseHandlerTestCase extends BaseTestCase {

    @UserForTest
    protected User regular;

    @UserForTest(userName = TestStatics.TEST_ADMIN_USER, role = UserRole.ORG_ADMIN)
    protected User admin;

    @UserForTest(userName = TestStatics.TEST_SAT_USER, role = UserRole.SAT_ADMIN)
    protected User satAdmin;

    protected String adminKey;

    protected String regularKey;

    protected String satAdminKey;

    @BeforeEach
    public void setUpBaseHandlerTestCase() throws Exception {
        //setup session keys
        adminKey = XmlRpcTestUtils.getSessionKey(admin);
        regularKey = XmlRpcTestUtils.getSessionKey(regular);
        satAdminKey = XmlRpcTestUtils.getSessionKey(satAdmin);

        // Setup configuration for kickstart tests (mock cobbler etc.)
        KickstartTestUtils.setupTestConfiguration(admin);
    }

    protected void addAccessGroup(User user, AccessGroup group) {
        user.addToGroup(group);
    }
}
