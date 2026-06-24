/*
 * Copyright (c) 2009--2013 Red Hat, Inc.
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
package com.redhat.rhn.testing;

import com.redhat.rhn.domain.kickstart.KickstartDataTest;
import com.redhat.rhn.domain.user.User;

import org.junit.jupiter.api.BeforeEach;

/**
 * Basic test class with a User
 */
public abstract class BaseTestCaseWithUser extends RhnBaseTestCase {

    protected User user;

    @BeforeEach
    public void setUpBaseTestCaseWithUser() throws Exception {
        user = UserTestUtils.createUser(this);
        KickstartDataTest.setupTestConfiguration(user);
    }

     @Override
    protected void cleanupDatabaseCommits() {
        TestUtils.deleteOrgOfUser(user);
    }

    @Override
    protected void afterCleanupDatabaseCommits() {
        user = null;
    }
}
