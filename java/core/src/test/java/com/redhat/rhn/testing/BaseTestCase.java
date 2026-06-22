/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.redhat.rhn.testing;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.hibernate.HibernateFactory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * root class for all test cases
 */
public class BaseTestCase {

    private boolean isCommitted = false;

    @BeforeEach
    void setUpBeforeAllTests() {
        isCommitted = false;
        HibernateFactory.addCommitListener(() -> isCommitted = true);
    }

    @AfterEach
    void tearDownAfterAllTests() {
        HibernateFactory.removeAllCommitListeners();

        TestCaseHelper.tearDownHelper();

        if (isCommitted) {
            cleanupDatabaseCommits();
            TestUtils.commitAndCloseSession();
        }
        isCommitted = false;
        afterCleanupDatabaseCommits();

        cleanupConfiguration();
    }

    /**
     * override in the child classes to clean up committed database items, before closing the session
     */
    protected void cleanupDatabaseCommits() {
        // default does nothing
    }

    /**
     * override in the child classes to add any needed operations after the database cleanup
     */
    protected void afterCleanupDatabaseCommits() {
        // default does nothing
    }

    /**
     * cleans up all configuration items set during the test in the form of Config.get().setString(...)
     */
    private void cleanupConfiguration() {
        Config.clear();
    }
}
