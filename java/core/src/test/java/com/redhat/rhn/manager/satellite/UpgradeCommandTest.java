/*
 * Copyright (c) 2009--2013 Red Hat, Inc.
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
package com.redhat.rhn.manager.satellite;

import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.MinionServerFactoryTest;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;

import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.pillar.MinionPillarManager;
import com.suse.salt.netapi.datatypes.target.MinionList;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Expectations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class UpgradeCommandTest extends JMockBaseTestCaseWithUser {

    private static final int MIN_NUM_MINIONS = 2;

    @Override
    @BeforeEach
    public void setUpJMockBaseTestCaseWithUser() throws Exception {
        super.setUpJMockBaseTestCaseWithUser();
        while (MinionServerFactory.listMinions().size() < MIN_NUM_MINIONS) {
            MinionServerFactoryTest.createTestMinionServer(user);
        }
    }

    /**
     * Tests that refreshAllSystemsPillar calls SaltApi.refreshPillar with correct MinionList
     */
    @Test
    public void testRefreshAllSystemsPillarWithMinion()  {
        // Calculate expected data
        List<MinionServer> expectedMinions = MinionServerFactory.listMinions();
        List<String> expectedMinionIds = expectedMinions.stream().map(MinionServer::getMinionId).toList();

        // Mock SaltApi
        SaltApi mockSaltApi = context.mock(SaltApi.class);

        // Expect refreshPillar to be called with the correct MinionList
        context.checking(new Expectations() {{
            oneOf(mockSaltApi).refreshPillar(with(minionListWith(expectedMinionIds)));
            will(returnValue(null));
        }});

        // Execute
        new UpgradeCommand(mockSaltApi, MinionPillarManager.INSTANCE).refreshAllSystemsPillar();
    }

    /**
     * Tests allSystemsSyncAll invokes SaltApi.syncAllAsync with correct MinionList
     */
    @Test
    public void testAllSystemsSyncAllWithMinion() {
        // Get expected minion IDs from database (same as the method does)
        List<String> expectedMinionIds = MinionServerFactory.listMinions()
                .stream()
                .map(MinionServer::getMinionId)
                .toList();

        // Mock SaltApi
        SaltApi mockSaltApi = context.mock(SaltApi.class);

        // Expect syncAllAsync to be called with the expected MinionList
        context.checking(new Expectations() {{
            oneOf(mockSaltApi).syncAllAsync(with(minionListWith(expectedMinionIds)));
            will(returnValue(null));
        }});

        // Create UpgradeCommand with mock
        UpgradeCommand cmd = new UpgradeCommand(mockSaltApi, MinionPillarManager.INSTANCE);

        // Call the actual method
        cmd.allSystemsSyncAll();
    }

    /**
     * Matcher to compare MinionList objects by their minion IDs
     */
    private static class MinionListMatcher extends TypeSafeMatcher<MinionList> {
        private final List<String> expectedIds;

        MinionListMatcher(List<String> expectedIdsIn) {
            this.expectedIds = expectedIdsIn;
        }

        @Override
        protected boolean matchesSafely(MinionList minionList) {
            return expectedIds.equals(minionList.getTarget());
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("MinionList with IDs ").appendValue(expectedIds);
        }
    }

    private static MinionListMatcher minionListWith(List<String> expectedIds) {
        return new MinionListMatcher(expectedIds);
    }
}
