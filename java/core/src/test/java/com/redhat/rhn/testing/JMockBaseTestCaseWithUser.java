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
public abstract class JMockBaseTestCaseWithUser extends RhnJmockBaseTestCase {

    protected User user;

    /**
     * Called once per test method to set up the test environment.
     *
     * @throws Exception if an error occurs during setup
     */
    @BeforeEach
    public void setUpJMockBaseTestCaseWithUser() throws Exception {
        user = UserTestUtils.createUser();
        KickstartDataTest.setupTestConfiguration(user);
    }

    @Override
    protected void cleanupDatabaseCommits() {
        TestUtils.deleteOrgOfUser(user);
        TestUtils.deleteAllAccessTokens();
    }

    @Override
    protected void afterCleanupDatabaseCommits() {
        user = null;
    }
}
