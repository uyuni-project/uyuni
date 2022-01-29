/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.manager.system.test;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.domain.channel.AccessToken;
import com.redhat.rhn.domain.channel.AccessTokenFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerXMLRPCHelper;
import com.redhat.rhn.manager.kickstart.cobbler.test.MockXMLRPCInvoker;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.services.impl.runner.MgrUtilRunner;

import org.apache.commons.codec.digest.DigestUtils;
import org.cobbler.test.MockConnection;
import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;

import java.util.Collections;
import java.util.Optional;


/**
 * SystemManagerMockTest
 */
public class SystemManagerMockTest extends JMockBaseTestCaseWithUser {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        Config.get().setString(CobblerXMLRPCHelper.class.getName(),
                MockXMLRPCInvoker.class.getName());
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        MockConnection.clear();
    }

    public void testRemovingServerInvalidatesTokens() throws Exception {
        Config.get().setString(
            "server.secret_key",
            DigestUtils.sha256Hex(TestUtils.randomString()));

        User user = UserTestUtils.findNewUser(
            "testUser", "testOrg" + this.getClass().getSimpleName());
        user.addPermanentRole(RoleFactory.ORG_ADMIN);

        MinionServer testMinionServer = MinionServerFactoryTest.createTestMinionServer(user);

        Channel base = ChannelFactoryTest.createBaseChannel(user);
        Channel child = ChannelFactoryTest.createTestChannel(user);
        child.setParentChannel(base);
        testMinionServer.getChannels().add(base);
        testMinionServer.getChannels().add(child);

        AccessToken tokenBase = AccessTokenFactory.generate(
            testMinionServer, Collections.singleton(base)).get();
        AccessToken tokenChild = AccessTokenFactory.generate(
            testMinionServer, Collections.singleton(child)).get();

        MinionServer server = TestUtils.saveAndReload(testMinionServer);

        SaltService saltServiceMock = mock(SaltService.class);

        context().checking(new Expectations() {{
            allowing(saltServiceMock).deleteKey(testMinionServer.getMinionId());
            allowing(saltServiceMock).removeSaltSSHKnownHost(testMinionServer.getHostname());
            will(returnValue(Optional.of(new MgrUtilRunner.RemoveKnowHostResult("removed", ""))));
        }});

        SystemManager systemManager = new SystemManager(ServerFactory.SINGLETON, ServerGroupFactory.SINGLETON,
                saltServiceMock);
        systemManager.deleteServer(server.getOrg().getActiveOrgAdmins().get(0), server.getId());

        assertFalse(tokenBase.getValid());
        assertFalse(tokenChild.getValid());
    }
}
