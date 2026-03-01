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

import com.redhat.rhn.domain.kickstart.test.KickstartDataTest;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.user.User;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * Basic test class class with a User
 */
public abstract class BaseTestCaseWithUser extends RhnBaseTestCase {

    protected User user;
    private boolean committed = false;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        user = UserTestUtils.createUser(this);
        KickstartDataTest.setupTestConfiguration(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();

        // If at some point we created a user and committed the transaction, we need
        // clean up our mess
        if (committed) {
           OrgFactory.deleteOrg(user.getOrg().getId(), user);
           TestUtils.commitAndCloseSession();
        }
        committed = false;
        user = null;
    }

    // If we have to commit in mid-test, set up the next transaction correctly
    protected void commitHappened() {
        committed = true;
    }
}
