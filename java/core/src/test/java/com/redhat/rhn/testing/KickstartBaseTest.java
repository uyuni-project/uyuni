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

import com.redhat.rhn.domain.kickstart.KickstartTestUtils;
import com.redhat.rhn.domain.user.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(UserForTestCaseExtension.class)
public class KickstartBaseTest extends BaseTestCase {

    @UserForTest
    protected User user;

    @BeforeEach
    public void setUpKickstartBaseTest() throws Exception {
        KickstartTestUtils.setupTestConfiguration(user);
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
