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

import com.redhat.rhn.domain.user.User;

import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Path;

/**
 * Basic test class with a User
 */
@ExtendWith(UserForTestCaseExtension.class)
@ExtendWith(SaltTestCaseExtension.class)
public abstract class JMockBaseTestCaseWithUser extends MockObjectTestCase {

    @UserForTest
    protected User user;

    @SaltTestRootPath
    protected Path tmpSaltRoot;

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
