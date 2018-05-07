/**
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.domain.channel.test;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.channel.AccessToken;
import com.redhat.rhn.domain.channel.AccessTokenFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;
import org.apache.commons.codec.digest.DigestUtils;

import java.sql.Date;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;


public class AccessTokenFactoryTest extends BaseTestCaseWithUser {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        Config.get().setString("server.secret_key",
                DigestUtils.sha256Hex(TestUtils.randomString()));
    }

    public void testCleanupNotDeletingChannels() throws Exception {
        int initialChannelCount = ChannelFactory.listAllBaseChannels().size();
        Channel base1 = ChannelFactoryTest.createBaseChannel(user);

        assertEquals(initialChannelCount + 1, ChannelFactory.listAllBaseChannels().size());

        AccessToken expired = new AccessToken();
        expired.setChannels(Collections.singleton(base1));
        expired.setExpiration(Date.from(Instant.now().minus(Duration.ofDays(1))));
        expired.setStart(Date.from(Instant.now().minus(Duration.ofDays(3))));
        expired.setToken("expired");
        AccessTokenFactory.save(expired);

        assertEquals(1, AccessTokenFactory.all().size());
        AccessTokenFactory.cleanupUnusedExpired();
        List<AccessToken> all = AccessTokenFactory.all();
        assertEquals(initialChannelCount + 1, ChannelFactory.listAllBaseChannels().size());
    }

    public void testCleanupExpired() {
        AccessToken valid = new AccessToken();
        valid.setExpiration(Date.from(Instant.now().plus(Duration.ofDays(1))));
        valid.setStart(Date.from(Instant.now()));
        valid.setToken("valid");
        AccessTokenFactory.save(valid);

        AccessToken expired = new AccessToken();
        expired.setExpiration(Date.from(Instant.now().minus(Duration.ofDays(1))));
        expired.setStart(Date.from(Instant.now().minus(Duration.ofDays(3))));
        valid.setToken("valid");
        expired.setToken("expired");
        AccessTokenFactory.save(expired);

        assertEquals(2, AccessTokenFactory.all().size());
        AccessTokenFactory.cleanupUnusedExpired();
        List<AccessToken> all = AccessTokenFactory.all();
        assertEquals(1, all.size());
        assertEquals("valid", all.get(0).getToken());
    }

    public void testValidWhenCreated() {
        AccessToken token = new AccessToken();
        assertTrue(token.getValid());
    }

    public void testGenerate() throws Exception {
        MinionServer testMinionServer = MinionServerFactoryTest.createTestMinionServer(user);
        Channel base = ChannelFactoryTest.createBaseChannel(user);
        Channel child = ChannelFactoryTest.createTestChannel(user);
        assertTrue(AccessTokenFactory.generate(testMinionServer, Collections.singleton(base)).isPresent());
        assertTrue(AccessTokenFactory.generate(testMinionServer, Collections.singleton(child)).isPresent());
        MinionServer minionServer = TestUtils.saveAndReload(testMinionServer);
        assertEquals(2, minionServer.getAccessTokens().size());
    }

    public void testUnneeded() throws Exception {
        MinionServer testMinionServer = MinionServerFactoryTest.createTestMinionServer(user);
        Channel base = ChannelFactoryTest.createBaseChannel(user);
        Channel child = ChannelFactoryTest.createTestChannel(user);
        child.setParentChannel(base);
        testMinionServer.getChannels().add(base);
        testMinionServer.getChannels().add(child);

        assertEquals(0, AccessTokenFactory.unneededTokens(testMinionServer).size());

        assertTrue(AccessTokenFactory.generate(testMinionServer, Collections.singleton(base)).isPresent());
        assertTrue(AccessTokenFactory.generate(testMinionServer, Collections.singleton(child)).isPresent());

        assertEquals(0, AccessTokenFactory.unneededTokens(testMinionServer).size());

        testMinionServer.getChannels().remove(child);
        MinionServer minionServer = TestUtils.saveAndReload(testMinionServer);
        assertEquals(1, minionServer.getChannels().size());
        assertEquals(1, AccessTokenFactory.unneededTokens(minionServer).size());

    }

    public void testUnnededTokensInvalidatedOnRefresh() throws Exception {
        MinionServer testMinionServer = MinionServerFactoryTest.createTestMinionServer(user);
        Channel base = ChannelFactoryTest.createBaseChannel(user);
        Channel child = ChannelFactoryTest.createTestChannel(user);
        child.setParentChannel(base);
        testMinionServer.getChannels().add(base);
        testMinionServer.getChannels().add(child);
        AccessToken token = AccessTokenFactory.generate(testMinionServer, Collections.singleton(child)).get();
        testMinionServer.getChannels().remove(child);
        MinionServer minionServer = TestUtils.saveAndReload(testMinionServer);
        assertTrue(AccessTokenFactory.refreshTokens(minionServer));
        assertFalse(token.getValid());
    }

    public void testRegenerate() throws Exception {
        MinionServer testMinionServer = MinionServerFactoryTest.createTestMinionServer(user);
        AccessToken valid = new AccessToken();
        valid.setStart(Date.from(Instant.now()));
        valid.setExpiration(Date.from(Instant.now().plus(Duration.ofDays(1))));
        valid.setToken("valid");
        valid.setMinion(testMinionServer);
        valid.setChannels(new HashSet<>());
        AccessTokenFactory.save(valid);

        assertEquals(1, AccessTokenFactory.all().size());

        AccessTokenFactory.regenerate(valid);

        assertEquals(2, AccessTokenFactory.all().size());
    }

    public void testRefresh() throws Exception {
        MinionServer testMinionServer = MinionServerFactoryTest.createTestMinionServer(user);
        assertFalse(AccessTokenFactory.refreshTokens(testMinionServer));
        assertEquals(0, testMinionServer.getAccessTokens().size());

        Channel base = ChannelFactoryTest.createBaseChannel(user);
        testMinionServer.getChannels().add(base);
        assertTrue(AccessTokenFactory.refreshTokens(testMinionServer));

        Channel child = ChannelFactoryTest.createTestChannel(user);
        child.setParentChannel(base);
        testMinionServer.getChannels().add(child);
        assertTrue(AccessTokenFactory.refreshTokens(testMinionServer));

        testMinionServer.getChannels().remove(child);
        assertTrue(AccessTokenFactory.refreshTokens(testMinionServer));
    }

}
