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
package com.redhat.rhn.domain.token.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.config.ConfigChannelListProcessor;
import com.redhat.rhn.domain.entitlement.Entitlement;
import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.KickstartSession;
import com.redhat.rhn.domain.kickstart.test.KickstartDataTest;
import com.redhat.rhn.domain.kickstart.test.KickstartSessionTest;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerConstants;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerGroupType;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.token.ActivationKeyFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.token.ActivationKeyManager;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ConfigTestUtils;
import com.redhat.rhn.testing.ServerGroupTestUtils;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;

/**
 * ActivationKeyTest
 */
public class ActivationKeyTest extends BaseTestCaseWithUser {
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
    }
    @Test
    public void testKeyGeneration() throws Exception {

        ActivationKey k = createTestActivationKey(user);
        String note = k.getNote();
        String key = k.getKey();

        TestUtils.saveAndFlush(k);

        ActivationKey k2 = ActivationKeyFactory.lookupByKey(key);
        assertEquals(key, k2.getKey());
        assertEquals(note, k2.getNote());

        ActivationKey k3 = ActivationKeyFactory.lookupByKey(TestUtils.randomString());
        assertNull(k3);

        // Make sure we got the entitlements correct
        Server server = k2.getServer();
        assertEquals(1, server.getEntitlements().size());
        assertEquals(1, k2.getEntitlements().size());

        Entitlement e = server.getEntitlements().iterator().next();
        ServerGroupType t2 = k2.getEntitlements().iterator().next();
        assertEquals(e.getLabel(), t2.getLabel());

        // test out ActivationKeyManager.findByServer while we're here...
        ActivationKey k4 = ActivationKeyManager.
            getInstance().findByServer(server, user).iterator().next();
        assertNotNull(k4);
        assertEquals(key, k4.getKey());


        try {
            ActivationKeyManager.getInstance().
                findByServer(null, user).iterator().next();
            String msg = "Permission check failed :(.." +
                            " Activation key should not have existed" +
                            " for a server of 'null' id. An exception " +
                             "should have been raised for this.";
            fail(msg);
        }
        catch (Exception ie) {
         // great!.. Exception for passing in invalid keys always welcome
        }

        User user1 = UserTestUtils.createUser();
        Server server2 = ServerFactoryTest.createTestServer(user1);
        try {
            ActivationKeyManager.getInstance().
                findByServer(server2, user1).iterator().next();
            String msg = "Permission check failed :(.." +
                            " Activation key should not have existed" +
                                " for a server of the associated id. An exception " +
                                "should have been raised for this.";
            fail(msg);
        }
        catch (Exception ie) {
            // great!.. Exception for passing in invalid keys always welcome
        }
     }
    @Test
    public void testBadKeys() {
        ActivationKeyManager manager = ActivationKeyManager.getInstance();
        try {
            manager.createNewActivationKey(user, "A,B", "Cool", null, null, false);
            fail("Validator exception Not raised for an invalid name");
        }
        catch (ValidatorException ve) {
            //success . Name had invalid chars
        }
    }

    @Test
    public void testKeyTrimming() {
        ActivationKeyManager manager = ActivationKeyManager.getInstance();
        String keyName = " Test Space  ";
        ActivationKey k = manager.createNewActivationKey
            (user, keyName, "Cool Duplicate", null, null, false);
        assertEquals(ActivationKey.makePrefix(user.getOrg()) +
                keyName.trim().replace(" ", ""), k.getKey());
        String newKey = keyName + " FOO  ";
        manager.changeKey(newKey , k, user);
        assertNotNull(ActivationKey.makePrefix(user.getOrg()) + newKey.trim());
    }

    @Test
    public void testLookupBySession() throws Exception {
        // Still have that weird error creating a test server
        // sometimes in hosted.
        ActivationKey k = createTestActivationKey(user);
        KickstartData ksdata = KickstartDataTest.
            createKickstartWithOptions(k.getOrg());
        KickstartFactory.saveKickstartData(ksdata);
        KickstartSession sess = KickstartSessionTest.createKickstartSession(ksdata,
                                                    k.getCreator());
        KickstartFactory.saveKickstartSession(sess);
        k.setKickstartSession(sess);
        ActivationKeyFactory.save(k);
        reload(k);

        ActivationKey lookedUp = ActivationKeyFactory.lookupByKickstartSession(sess);
        assertNotNull(lookedUp);
    }

    @Test
    public void testNullServer() {
        ActivationKey key = ActivationKeyFactory.createNewKey(user,
                TestUtils.randomString());
        assertNotNull(key.getEntitlements());
        assertEquals(1, key.getEntitlements().size());
    }

    // See BZ: 191007
    @Test
    public void testCreateWithCustomGroups() {
        Server s = ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeEnterpriseEntitled());
        ServerGroup testGroup = ServerGroupTestUtils.createManaged(user);
        s.getManagedGroups().add((ManagedServerGroup)testGroup);

        //Three, one for the server entitlement, one for the user permission to the
        //server, one as the testGroup.
        assertEquals(1, s.getManagedGroups().size());
        ActivationKey key = createTestActivationKey(user, s);
        assertNotNull(key);
        key = (ActivationKey) reload(key);
        assertNotNull(key.getId());
    }

    @Test
    public void testAddGetKeys() throws Exception {

        ActivationKey k = createTestActivationKey(user);

        for (int i = 0; i < 5; i++) {
            Channel c = ChannelFactoryTest.
                createTestChannel(user);
            k.addChannel(c);
        }
        assertEquals(5, k.getChannels().size());
    }

    @Test
    public void testLookupByServer() throws Exception {
        ActivationKey k = createTestActivationKey(user);
        Server s = k.getServer();
        createTestActivationKey(user, s);
        createTestActivationKey(user, s);
        createTestActivationKey(user, s);
        List keys = ActivationKeyFactory.lookupByServer(s);
        assertEquals(4, keys.size());
    }

    @Test
    public void testCreateNewKeys() throws Exception {
        ActivationKey k = createTestActivationKey(user);
        Server s = k.getServer();
        for (int i = 0; i < 10; i++) {
            ActivationKey tk = createTestActivationKey(s.getCreator(), s);
            System.out.println("tk: " + tk.getKey());
        }
    }

    public static ActivationKey createTestActivationKey(User user) {
        Server server = ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeEnterpriseEntitled());

        return createTestActivationKey(user, server);
    }

    public static ActivationKey createTestActivationKey(User u, Server s) {

        String note = "" + TestUtils.randomString() +
                      " -- Java unit test activation key.";

        ActivationKey key = ActivationKeyManager.getInstance().
                                            createNewReActivationKey(u, s, note);
        ActivationKeyFactory.save(key);
        return key;
    }

    @Test
    public void testDuplicateKeyCreation() {
        String keyName = "Hey!";
        ActivationKeyManager.getInstance().createNewActivationKey
                (user, keyName, null, null, null, false);
        try {
            ActivationKeyManager.getInstance().createNewActivationKey
                                (user, keyName, "Cool Duplicate", null, null, false);
            String msg = "Duplicate Key exception not raised..";
            fail(msg);
        }
        catch (ValidatorException e) {
            for (ValidatorError er : e.getResult().getErrors()) {
                    if (er.getKey().equals("activation-key.java.exists")) {
                        // sweet duplicate object exception
                        return;
                    }
            }
            throw e;
        }
    }

    // Allowed values: numbers, upper and lower case letters, '-', '_' and '.'
    @Test
    public void testActivationKeyValidation() {
        List<String> validKeys = new LinkedList<>();
        validKeys.add("1234567890");
        validKeys.add("ABCdefGHI");
        validKeys.add(".........");
        validKeys.add("---------");
        validKeys.add("_________");
        validKeys.add("a-1-b-2-C_D_E");
        validKeys.add("-_");
        validKeys.add("--11__22--aa__BB..Cd34");
        validKeys.add("a-1-b-2-C_D_E.fgh.ijk");
        System.out.println("VALID KEYS:");
        for (String key: validKeys) {
            System.out.println("test-key: " + key +
                    "; validation: " + ActivationKey.isValid(key));
            assertTrue(ActivationKey.isValid(key));
        }

        List<String> invalidKeys = new LinkedList<>();
        invalidKeys.add("123,456.7-8_9");
        invalidKeys.add("@#!~!$%*(%&^");
        invalidKeys.add("%%%");
        invalidKeys.add("++++");
        invalidKeys.add("abcde+fgh");
        invalidKeys.add("q=w=e;r");
        invalidKeys.add("1#2:a:b");
        invalidKeys.add("1$2:a-b.c");
        invalidKeys.add("1@2!a)*(b");
        invalidKeys.add("<key>");
        System.out.println("INVALID KEYS:");
        for (String key: invalidKeys) {
            System.out.println("test-key: " + key +
                    "; validation: " + ActivationKey.isValid(key));
            assertFalse(ActivationKey.isValid(key));
        }
    }

    @Test
    public void testChangeConfigChannelsRankingOrder() {
        //create activation key and subscribe configuration channels in order 1,2,3
        ActivationKey key = ActivationKeyManager.getInstance().createNewActivationKey
                (user, "anyKeyName", null, null, null, false);
        ConfigChannel channel1 = ConfigTestUtils.createConfigChannel(user.getOrg(), "Ch 1", "cfg-channel-1");
        ConfigChannel channel2 = ConfigTestUtils.createConfigChannel(user.getOrg(), "Ch 2", "cfg-channel-2");
        ConfigChannel channel3 = ConfigTestUtils.createConfigChannel(user.getOrg(), "Ch 3", "cfg-channel-3");

        ConfigChannelListProcessor proc = new ConfigChannelListProcessor();
        proc.add(key.getConfigChannelsFor(user), channel1);
        proc.add(key.getConfigChannelsFor(user), channel2);
        proc.add(key.getConfigChannelsFor(user), channel3);

        //save and reload key, check configuration channels order
        TestUtils.saveAndFlush(key);
        ActivationKey key1 = ActivationKeyFactory.lookupByKey(key.getKey());
        assertNotNull(key1);
        List<ConfigChannel> configChannels1 = key1.getConfigChannelsFor(user);
        assertEquals(3, configChannels1.size());
        assertEquals("cfg-channel-1", configChannels1.get(0).getLabel());
        assertEquals("cfg-channel-2", configChannels1.get(1).getLabel());
        assertEquals("cfg-channel-3", configChannels1.get(2).getLabel());


        //change configuration channels ranking order to 3,1,2
        ConfigChannelListProcessor proc1 = new ConfigChannelListProcessor();
        proc1.add(key1.getConfigChannelsFor(user), channel2);
        proc1.add(key1.getConfigChannelsFor(user), channel3);
        proc1.add(key1.getConfigChannelsFor(user), channel1);

        //save and reload key, check configuration channels order
        TestUtils.saveAndFlush(key1);
        ActivationKey key2 = ActivationKeyFactory.lookupByKey(key.getKey());
        assertNotNull(key2);
        List<ConfigChannel> configChannels2 = key2.getConfigChannelsFor(user);
        assertEquals(3, configChannels2.size());
        assertEquals("cfg-channel-2", configChannels2.get(0).getLabel());
        assertEquals("cfg-channel-3", configChannels2.get(1).getLabel());
        assertEquals("cfg-channel-1", configChannels2.get(2).getLabel());
    }

}
