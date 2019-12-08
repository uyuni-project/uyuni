/**
 * Copyright (c) 2019 SUSE LLC
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

package com.redhat.rhn.domain.contentmgmt.test;

import static com.redhat.rhn.domain.role.RoleFactory.ORG_ADMIN;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.contentmgmt.SoftwareEnvironmentTarget;
import com.redhat.rhn.manager.contentmgmt.ContentManager;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;

/**
 * Tests for {@link com.redhat.rhn.domain.contentmgmt.EnvironmentTarget}
 */
public class EnvironmentTargetTest extends JMockBaseTestCaseWithUser {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        user.addPermanentRole(ORG_ADMIN);
    }

    /**
     * Tests the links between {@link SoftwareEnvironmentTarget} {@link Channel}s
     *
     * @throws Exception if anything goes wrong
     */
    public void testSWEnvironmentTargetLinks() throws Exception {
        ContentManager.createProject("testproj1", "testproj1", "", user);
        var devEnv = ContentManager.createEnvironment("testproj1", empty(), "dev", "dev env", "", false, user);
        var testEnv = ContentManager.createEnvironment("testproj1", of("dev"), "test", "test env", "", false, user);

        Channel srcChannel = ChannelFactoryTest.createTestChannel(user);
        srcChannel.setLabel("channel123");
        Channel devChannel = ChannelFactoryTest.createTestClonedChannel(srcChannel, user);
        devChannel.setLabel("testproj1-dev-channel123");
        Channel testChannel = ChannelFactoryTest.createTestClonedChannel(devChannel, user);
        testChannel.setLabel("testproj1-test-channel123");

        var devTgt = new SoftwareEnvironmentTarget(devEnv, devChannel);
        devEnv.addTarget(devTgt);
        var testTgt = new SoftwareEnvironmentTarget(testEnv, testChannel);
        testEnv.addTarget(testTgt);

        // check dev environment
        assertEquals(srcChannel, devTgt.findPredecessorChannel().get());
        assertEquals(testChannel, devTgt.findSuccessorChannel().get());

        // check test environment
        assertEquals(devChannel, testTgt.findPredecessorChannel().get());
        assertTrue(testTgt.findSuccessorChannel().isEmpty());
    }
}