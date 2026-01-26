/*
 * Copyright (c) 2026 SUSE LLC
 * Copyright (c) 2009--2017 Red Hat, Inc.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelArch;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.channel.ChannelFamilyFactory;
import com.redhat.rhn.domain.channel.ChannelSyncFlag;
import com.redhat.rhn.domain.channel.ChannelTestUtility;
import com.redhat.rhn.domain.channel.ClonedChannel;
import com.redhat.rhn.domain.channel.ContentSource;
import com.redhat.rhn.domain.channel.ContentSourceType;
import com.redhat.rhn.domain.channel.ProductName;
import com.redhat.rhn.domain.common.ChecksumType;
import com.redhat.rhn.domain.kickstart.KickstartInstallType;
import com.redhat.rhn.domain.kickstart.test.KickstartDataTest;
import com.redhat.rhn.domain.kickstart.test.KickstartableTreeTest;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.rhnpackage.test.PackageManagerTest;
import com.redhat.rhn.manager.user.UserManager;
import com.redhat.rhn.testing.ChannelTestUtils;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * ChannelFactoryTest
 */
public class ChannelFactoryTest extends RhnBaseTestCase {

    @Test
    public void testChannelFactory() {
        User user = UserTestUtils.createUser(this);
        Channel c = ChannelFactoryTest.createTestChannel(user);

        assertNotNull(c.getChannelFamily());

        Channel c2 = ChannelFactory.lookupById(c.getId());
        assertEquals(c.getLabel(), c2.getLabel());

        Channel c3 = ChannelFactoryTest.createTestChannel(user);
        Long id = c3.getId();
        assertNotNull(c.getChannelArch());
        ChannelFactory.remove(c3);
        TestUtils.flushAndEvict(c3);
        assertNull(ChannelFactory.lookupById(id));
    }

    public static ProductName lookupOrCreateProductName(String label) {
        ProductName attempt = ChannelFactory.lookupProductNameByLabel(label);
        if (attempt == null) {
            attempt = new ProductName();
            attempt.setLabel(label);
            attempt.setName(label);
            HibernateFactory.getSession().persist(attempt);
            HibernateFactory.getSession().flush();
        }
        return attempt;
    }

    public static Channel createBaseChannel(User user) {
        Channel c = ChannelFactoryTest.createTestChannel(user);
        c.setOrg(user.getOrg());

        ProductName pn = lookupOrCreateProductName(ChannelManager.RHEL_PRODUCT_NAME);
        c.setProductName(pn);

        return c;
    }

    public static Channel createBaseChannel(User user, String channelArchLabel) {
        Channel c = ChannelFactoryTest.createTestChannel(user, channelArchLabel);
        c.setOrg(user.getOrg());

        ProductName pn = lookupOrCreateProductName(ChannelManager.RHEL_PRODUCT_NAME);
        c.setProductName(pn);

        return c;
    }

    public static Channel createBaseChannel(User user,
                                            ChannelFamily fam) {
        Channel c = createTestChannel(null, fam);
        ProductName pn = lookupOrCreateProductName(ChannelManager.RHEL_PRODUCT_NAME);
        c.setProductName(pn);
        return c;
    }

    public static Channel createTestChannel(User user) {
        Channel c = ChannelFactoryTest.createTestChannel(user.getOrg());
        // assume we want the user to have access to this channel once created
        UserManager.addChannelPerm(user, c.getId(), "subscribe");
        UserManager.addChannelPerm(user, c.getId(), "manage");
        return c;
    }

    public static Channel createTestChannel(User user, List<String> contentSourceUrls) {
        Channel c = ChannelFactoryTest.createTestChannel(user.getOrg());
        // assume we want the user to have access to this channel once created
        UserManager.addChannelPerm(user, c.getId(), "subscribe");
        UserManager.addChannelPerm(user, c.getId(), "manage");

        ContentSourceType type = ChannelManager.findCompatibleContentSourceType(c.getChannelArch());
        contentSourceUrls.stream()
                .map(url -> {
                    ContentSource cs = new ContentSource();
                    cs.setLabel(c.getLabel() + "-CS-" + RandomStringUtils.randomAlphabetic(8));
                    cs.setOrg(user.getOrg());
                    cs.setType(type);
                    cs.setSourceUrl(url);
                    return TestUtils.saveAndReload(cs);
                })
                .forEach(c.getSources()::add);

        return c;
    }

    public static Channel createTestChannel(User user, String channelArch) {
        ChannelArch arch = TestUtils.lookupChannelArchFromCacheByLabel(channelArch);
        Channel c = createTestChannel(user.getOrg(), arch, user.getOrg().getPrivateChannelFamily());
        // assume we want the user to have access to this channel once created
        UserManager.addChannelPerm(user, c.getId(), "subscribe");
        UserManager.addChannelPerm(user, c.getId(), "manage");
        return c;
    }

    public static Channel createTestChannel(Org org) {
        ChannelFamily cfam = org.getPrivateChannelFamily();
        return ChannelFactoryTest.createTestChannel(org, cfam);
    }

    public static Channel createTestChannel(Org org, ChannelFamily cfam) {
        ChannelArch arch = TestUtils.lookupChannelArchFromCacheByLabel("channel-x86_64");
        return createTestChannel(org, arch, cfam);
    }

    /**
     * Create a test channel setting the GPGCheck flag via a parameter.
     *
     * @param user       the user
     * @param gpgCheckIn the GPGCheck flag to set
     * @return the test channel
     */
    public static Channel createTestChannel(User user, boolean gpgCheckIn) {
        Channel c = ChannelFactoryTest.createTestChannel(user.getOrg());
        c.setGPGCheck(gpgCheckIn);
        // assume we want the user to have access to this channel once created
        UserManager.addChannelPerm(user, c.getId(), "subscribe");
        UserManager.addChannelPerm(user, c.getId(), "manage");
        return c;
    }

    public static Channel createTestChannel(Org org, ChannelArch arch, ChannelFamily cfam) {
        String label = "channellabel" + TestUtils.randomString().toLowerCase();
        String name = "ChannelName" + TestUtils.randomString();

        return createTestChannel(name, label, org, arch, cfam);
    }

    public static Channel createTestChannel(String name, String label, Org org, ChannelArch arch, ChannelFamily cfam) {

        String basedir = "TestChannel basedir";
        String summary = "TestChannel summary";
        String description = "TestChannel description";
        Date lastmodified = new Date();
        Date created = new Date();
        Date modified = new Date();
        String gpgurl = "https://gpg.url";
        String gpgid = "B3BCE11D";
        String gpgfp = "AAAA BBBB CCCC DDDD EEEE FFFF 7777 8888 9999 0000";
        Calendar cal = Calendar.getInstance();
        cal.roll(Calendar.DATE, true);
        Date endoflife = new Date(System.currentTimeMillis() + Integer.MAX_VALUE);
        ChecksumType ct = ChannelFactory.findChecksumTypeByLabel("sha256");

        Channel c = new Channel();
        c.setOrg(org);
        c.setLabel(label);
        c.setBaseDir(basedir);
        c.setName(name);
        c.setSummary(summary);
        c.setDescription(description);
        c.setLastModified(lastmodified);
        c.setCreated(created);
        c.setModified(modified);
        c.setGPGKeyUrl(gpgurl);
        c.setGPGKeyId(gpgid);
        c.setGPGKeyFp(gpgfp);
        c.setEndOfLife(endoflife);
        c.setChannelArch(arch);
        c.setChannelFamily(cfam);
        c.setChecksumType(ct);
        Channel save = ChannelFactory.save(c);
        HibernateFactory.getSession().flush();
        return save;
    }

    /**
     * TODO: need to fix this test when we put errata management back in.
     *
     */
    @Test
    public void testChannelsWithClonableErrata() {
        User user = UserTestUtils.createUser(this);
        ChannelManager.
                getChannelsWithClonableErrata(user.getOrg());

        Channel original = ChannelFactoryTest.createTestChannel(user);
        Channel clone = ChannelFactoryTest.createTestClonedChannel(original, user);
        TestUtils.flushAndEvict(original);
        TestUtils.flushAndEvict(clone);

        List<ClonedChannel> channels =
                ChannelFactory.getChannelsWithClonableErrata(
                        user.getOrg());

        assertFalse(channels.isEmpty());
    }

    @Test
    public void testLookupByLabel() {
        User user = UserTestUtils.createUser();
        Channel rh = createTestChannel(user);
        String label = rh.getLabel();
        rh.setOrg(null);
        ChannelFactory.save(rh);
        assertNull(rh.getOrg());

        //Lookup a channel without an org (An RH channel)
        Channel c = ChannelFactory.lookupByLabel(user.getOrg(), label);
        assertEquals(label, c.getLabel());

        //Lookup a channel with an org (user custom channel)
        Channel cust = createTestChannel(user);
        label = cust.getLabel();
        assertNotNull(cust.getOrg());
        c = ChannelFactory.lookupByLabel(user.getOrg(), label);
        assertNotNull(c);
        assertEquals(label, c.getLabel());
        assertEquals(user.getOrg(), c.getOrg());

        //Lookup a channel in a different org
    }

    @Test
    public void testIsGloballySubscribable() {
        User user = UserTestUtils.createUser(this);
        Channel c = createTestChannel(user);
        assertTrue(ChannelFactory.isGloballySubscribable(user.getOrg(), c));
    }

    @Test
    public void testChannelArchByLabel() {
        assertNull(ChannelFactory.findArchByLabel(null), "Arch found for null label");
        assertNull(ChannelFactory.findArchByLabel("some-invalid_arch_label"), "Arch found for invalid label");

        ChannelArch ca = ChannelFactory.findArchByLabel("channel-x86_64");
        assertNotNull(ca);
        assertEquals("channel-x86_64", ca.getLabel());
        assertEquals("x86_64", ca.getName());
    }

    @Test
    public void testVerifyLabel() {
        User user = UserTestUtils.createUser(this);
        Channel c = createTestChannel(user);
        assertFalse(ChannelFactory.doesChannelLabelExist("foo"));
        assertTrue(ChannelFactory.doesChannelLabelExist(c.getLabel()));
    }

    @Test
    public void testVerifyName() {
        User user = UserTestUtils.createUser(this);
        Channel c = createTestChannel(user);
        assertFalse(ChannelFactory.doesChannelNameExist("power house foo channel"));
        assertTrue(ChannelFactory.doesChannelNameExist(c.getName()));
    }

    @Test
    public void testKickstartableTreeChannels() {
        User user = UserTestUtils.createUser(this);

        List<Channel> channels = ChannelFactory.getKickstartableTreeChannels(user.getOrg());
        assertNotNull(channels);
        int originalSize = channels.size();

        createTestChannel(user);

        channels = ChannelFactory.getKickstartableTreeChannels(user.getOrg());
        assertEquals(originalSize + 1, channels.size());
    }

    @Test
    public void testKickstartableChannels() throws Exception {
        User user = UserTestUtils.createUser(this);
        // Setup test config since kickstartable trees are required
        KickstartDataTest.setupTestConfiguration(user);

        List<Channel> channels = ChannelFactory.getKickstartableChannels(user.getOrg());
        assertNotNull(channels);
        int originalSize = channels.size();

        // c1 is kickstartable
        Channel c1 = createTestChannel(user);
        KickstartableTreeTest.createTestKickstartableTree(c1,
                KickstartInstallType.RHEL_7);
        KickstartableTreeTest.createTestKickstartableTree(c1,
                KickstartInstallType.RHEL_7);
        // c2 is kickstartable
        Channel c2 = createTestChannel(user);
        KickstartableTreeTest.createTestKickstartableTree(c2,
                KickstartInstallType.FEDORA_PREFIX + "18");
        // c3 is not kickstartable
        Channel c3 = createTestChannel(user);
        KickstartableTreeTest.createTestKickstartableTree(c3,
                KickstartInstallType.SLES_PREFIX + "11generic");
        // c4 is not kickstartable
        createTestChannel(user);

        channels = ChannelFactory.getKickstartableChannels(user.getOrg());
        assertEquals(originalSize + 2, channels.size());
        assertTrue(channels.contains(c1));
        assertTrue(channels.contains(c2));
    }

    @Test
    public void testPackageCount() {
        User user = UserTestUtils.createUser(this);
        Channel original = ChannelFactoryTest.createTestChannel(user);
        assertEquals(0, ChannelFactory.getPackageCount(original));
        ChannelTestUtility.testAddPackage(original, PackageTest.createTestPackage(user.getOrg()));
        ChannelFactory.save(original);
        TestUtils.flushAndEvict(original);

        original = TestUtils.reload(original);
        assertEquals(1, ChannelFactory.getPackageCount(original));
    }

    /**
     * Create a test cloned channel. NOTE: This function does not copy its
     * original's package list like a real clone would. It is only useful for
     * testing purposes.
     *
     * @param original Channel to be cloned
     * @param user     the user
     * @return a test cloned channel
     */
    public static Channel createTestClonedChannel(Channel original, User user) {
        return createTestClonedChannel(original, user, "clone-", "",
                "Clone of ", "", null);
    }

    public static Channel createTestClonedChannel(Channel original, User user, String labelPrefix, String labelSuffix,
                                                  String namePrefix, String nameSuffix, Channel parent) {
        Org org = user.getOrg();
        ClonedChannel clone = new ClonedChannel();
        ChannelFamily cfam = ChannelFamilyFactory.lookupOrCreatePrivateFamily(org);

        clone.setOrg(org);
        clone.setLabel(labelPrefix + original.getLabel() + labelSuffix);
        clone.setBaseDir(original.getBaseDir());
        clone.setName(namePrefix + original.getName() + nameSuffix);
        clone.setSummary(original.getSummary());
        clone.setDescription(original.getDescription());
        clone.setLastModified(new Date());
        clone.setCreated(new Date());
        clone.setModified(new Date());
        clone.setGPGKeyUrl(original.getGPGKeyUrl());
        clone.setGPGKeyId(original.getGPGKeyId());
        clone.setGPGKeyFp(original.getGPGKeyFp());
        clone.setGPGCheck(original.isGPGCheck());
        clone.setEndOfLife(new Date());
        clone.setChannelFamily(cfam);
        clone.setChannelArch(original.getChannelArch());

        /* clone specific calls */
        clone.setOriginal(original);
        clone.setParentChannel(parent);

        Channel managed = ChannelFactory.save(clone);

        // assume we want the user to have access to this channel once created
        UserManager.addChannelPerm(user, managed.getId(), "subscribe");
        UserManager.addChannelPerm(user, managed.getId(), "manage");

        return managed;
    }

    @Test
    public void testAccessibleChildChannels() {
        User user = UserTestUtils.createUser(this);
        Channel parent = ChannelFactoryTest.createBaseChannel(user);
        Channel child = ChannelFactoryTest.createTestChannel(user);
        child.setParentChannel(parent);
        TestUtils.saveAndFlush(child);
        TestUtils.saveAndFlush(parent);
        TestUtils.flushAndEvict(child);
        List<Channel> dr = parent.getAccessibleChildrenFor(user);

        assertFalse(dr.isEmpty());
        assertEquals(child, dr.get(0));
    }

    public static ProductName createProductName() {
        ProductName pn = new ProductName();
        pn.setLabel("Label - " + TestUtils.randomString());
        pn.setName("Name - " + TestUtils.randomString());
        TestUtils.saveAndFlush(pn);
        return pn;
    }

    @Test
    public void testFindChannelArchesSyncdChannels() {
        // ensure at least one channel is present
        User user = UserTestUtils.createUser();
        ChannelFactoryTest.createTestChannel(user);

        List<String> labels = ChannelFactory.findChannelArchLabelsSyncdChannels();
        assertNotNull(labels);
        assertNotEmpty(labels);
    }

    @Test
    public void testListAllBaseChannels() {
        User user = UserTestUtils.createUser(this);
        // do NOT use createBaseChannel here because that will create a Red Hat
        // base channel NOT a user owned base channel.
        createTestChannel(user);
        List<Channel> channels = ChannelFactory.listAllBaseChannels(user);
        assertNotNull(channels);
        int size = channels.size();
        createTestChannel(user);
        channels = ChannelFactory.listAllBaseChannels(user);
        assertNotNull(channels);
        assertEquals(size + 1, channels.size());
    }

    @Test
    public void testLookupPackageByFileName() throws Exception {
        User user = UserTestUtils.createUser(this);
        Channel channel = ChannelTestUtils.createTestChannel(user);
        TestUtils.saveAndFlush(channel);
        Package p = PackageManagerTest.addPackageToChannel("some-package", channel);
        String fileName = "some-package-2.13.1-6.fc9.x86_64.rpm";
        p.setPath("redhat/1/c7d/some-package/2.13.1-6.fc9/" +
                "x86_64/c7dd5e9b6975bc7f80f2f4657260af53/" +
                fileName);
        TestUtils.saveAndFlush(p);

        Package lookedUp = ChannelFactory.lookupPackageByFilename(channel,
                fileName);
        assertNotNull(lookedUp);
        assertEquals(p.getId(), lookedUp.getId());

        // Test in child channel.
        Channel child = ChannelTestUtils.createChildChannel(user, channel);
        Package cp = PackageManagerTest.addPackageToChannel("some-package-child", child);
        String fileNameChild = "some-package-child-2.13.1-6.fc9.x86_64.rpm";
        cp.setPath("redhat/1/c7d/some-package-child/2.13.1-6.fc9/" +
                "x86_64/c7dd5e9b6975bc7f80f2f4657260af53/" +
                fileNameChild);
        TestUtils.saveAndFlush(child);

        Package lookedUpChild = ChannelFactory.lookupPackageByFilename(channel,
                fileNameChild);
        assertNotNull(lookedUpChild);
        assertEquals(cp.getId(), lookedUpChild.getId());

    }

    @Test
    public void testfindChecksumByLabel() {
        assertNull(ChannelFactory.findChecksumTypeByLabel(null), "Checksum found for null label");
        assertNull(ChannelFactory.findChecksumTypeByLabel("some-invalid_checksum"), "Checksum found for invalid label");

        ChecksumType ct = ChannelFactory.findChecksumTypeByLabel("sha256");
        assertNotNull(ct);
        assertEquals("sha256", ct.getLabel());

        ChecksumType ct2 = ChannelFactory.findChecksumTypeByLabel("sha1");
        assertNotNull(ct2);
        assertEquals("sha1", ct2.getLabel());
    }

    /**
     * Test user channel accessibility
     *
     */
    @Test
    public void testAccessibility() {
        User user1 = UserTestUtils.createUser("testuser1", "testorg1");
        User user2 = UserTestUtils.createUser("testuser2", user1.getOrg().getId());
        User user3 = UserTestUtils.createUser("testuser3", "testorg3");
        Channel c = ChannelFactoryTest.createTestChannel(user1);

        assertTrue(ChannelFactory.isAccessibleByUser(c.getLabel(), user1.getId()));
        assertTrue(ChannelFactory.isAccessibleByUser(c.getLabel(), user2.getId()));
        assertFalse(ChannelFactory.isAccessibleByUser(c.getLabel(), user3.getId()));
    }

    /**
     * Test org channel accessibility
     *
     */
    @Test
    public void testOrgAccessibility() {
        User user1 = UserTestUtils.createUser("testuser1", "testorg1");
        User user2 = UserTestUtils.createUser("testuser2", "testorg2");
        User user3 = UserTestUtils.createUser("testuser3", "testorg3");
        User user4 = UserTestUtils.createUser("testuser4", "testorg4");
        Org org1 = user1.getOrg();
        Org org2 = user2.getOrg();
        Org org3 = user3.getOrg();
        Org org4 = user4.getOrg();


        Channel c1 = ChannelFactoryTest.createTestChannel(user1);
        Channel c2 = ChannelFactoryTest.createTestChannel(user2);

        assertTrue(ChannelFactory.isAccessibleBy(c1.getLabel(), org1.getId()));
        assertFalse(ChannelFactory.isAccessibleBy(c1.getLabel(), org2.getId()));
        assertFalse(ChannelFactory.isAccessibleBy(c1.getLabel(), org3.getId()));
        assertFalse(ChannelFactory.isAccessibleBy(c1.getLabel(), org4.getId()));

        assertFalse(ChannelFactory.isAccessibleBy(c2.getLabel(), org1.getId()));
        assertTrue(ChannelFactory.isAccessibleBy(c2.getLabel(), org2.getId()));
        assertFalse(ChannelFactory.isAccessibleBy(c2.getLabel(), org3.getId()));
        assertFalse(ChannelFactory.isAccessibleBy(c2.getLabel(), org4.getId()));

        ChannelFamily privcfam = ChannelFamilyFactoryTest.createTestChannelFamily(user3, false);
        ChannelFamily pubcfam = ChannelFamilyFactoryTest.createTestChannelFamily(user4, true);

        c1.setChannelFamily(privcfam);
        TestUtils.saveAndFlush(c1);

        c2.setChannelFamily(pubcfam);
        TestUtils.saveAndFlush(c2);

        // c1 belongs to user3 org now
        assertFalse(ChannelFactory.isAccessibleBy(c1.getLabel(), org1.getId()));
        assertFalse(ChannelFactory.isAccessibleBy(c1.getLabel(), org2.getId()));
        assertTrue(ChannelFactory.isAccessibleBy(c1.getLabel(), org3.getId()));
        assertFalse(ChannelFactory.isAccessibleBy(c1.getLabel(), org4.getId()));

        // c2 is public now
        assertTrue(ChannelFactory.isAccessibleBy(c2.getLabel(), org1.getId()));
        assertTrue(ChannelFactory.isAccessibleBy(c2.getLabel(), org2.getId()));
        assertTrue(ChannelFactory.isAccessibleBy(c2.getLabel(), org3.getId()));
        assertTrue(ChannelFactory.isAccessibleBy(c2.getLabel(), org4.getId()));
    }

    /**
     * Test trusted org channel accessibility
     *
     */
    @Test
    public void testTrustedOrgAccessibility() {
        User user1 = UserTestUtils.createUser("testuser1", "testorg1");
        User user2 = UserTestUtils.createUser("testuser2", "testorg2");
        User user3 = UserTestUtils.createUser("testuser3", "testorg3");
        User user4 = UserTestUtils.createUser("testuser4", "testorg4");
        Org org1 = user1.getOrg();
        Org org2 = user2.getOrg();
        Org org3 = user3.getOrg();
        Org org4 = user4.getOrg();

        Channel c1 = ChannelFactoryTest.createTestChannel(user1);
        Channel c2 = ChannelFactoryTest.createTestChannel(user2);

        assertTrue(ChannelFactory.isAccessibleBy(c1.getLabel(), org1.getId()));
        assertFalse(ChannelFactory.isAccessibleBy(c1.getLabel(), org2.getId()));
        assertFalse(ChannelFactory.isAccessibleBy(c1.getLabel(), org3.getId()));
        assertFalse(ChannelFactory.isAccessibleBy(c1.getLabel(), org4.getId()));

        assertFalse(ChannelFactory.isAccessibleBy(c2.getLabel(), org1.getId()));
        assertTrue(ChannelFactory.isAccessibleBy(c2.getLabel(), org2.getId()));
        assertFalse(ChannelFactory.isAccessibleBy(c2.getLabel(), org3.getId()));
        assertFalse(ChannelFactory.isAccessibleBy(c2.getLabel(), org4.getId()));

        // trusted org added to org
        org1.getTrustedOrgs().add(org3);
        c1.setAccess(Channel.PUBLIC);
        TestUtils.clearSession();

        assertTrue(ChannelFactory.isAccessibleBy(c1.getLabel(), org1.getId()));
        assertFalse(ChannelFactory.isAccessibleBy(c1.getLabel(), org2.getId()));
        assertTrue(ChannelFactory.isAccessibleBy(c1.getLabel(), org3.getId()));
        assertFalse(ChannelFactory.isAccessibleBy(c1.getLabel(), org4.getId()));

        assertFalse(ChannelFactory.isAccessibleBy(c2.getLabel(), org1.getId()));
        assertTrue(ChannelFactory.isAccessibleBy(c2.getLabel(), org2.getId()));
        assertFalse(ChannelFactory.isAccessibleBy(c2.getLabel(), org3.getId()));
        assertFalse(ChannelFactory.isAccessibleBy(c2.getLabel(), org4.getId()));

        // trusted org added to channel
        Channel c2Reloaded = ChannelFactory.lookupById(c2.getId());
        Org org4Reloaded = OrgFactory.lookupById(org4.getId());
        c2Reloaded.getTrustedOrgs().add(org4Reloaded);
        c2Reloaded.setAccess(Channel.PROTECTED);
        TestUtils.clearSession();

        assertTrue(ChannelFactory.isAccessibleBy(c1.getLabel(), org1.getId()));
        assertFalse(ChannelFactory.isAccessibleBy(c1.getLabel(), org2.getId()));
        assertTrue(ChannelFactory.isAccessibleBy(c1.getLabel(), org3.getId()));
        assertFalse(ChannelFactory.isAccessibleBy(c1.getLabel(), org4.getId()));

        assertFalse(ChannelFactory.isAccessibleBy(c2.getLabel(), org1.getId()));
        assertTrue(ChannelFactory.isAccessibleBy(c2.getLabel(), org2.getId()));
        assertFalse(ChannelFactory.isAccessibleBy(c2.getLabel(), org3.getId()));
        assertTrue(ChannelFactory.isAccessibleBy(c2.getLabel(), org4.getId()));
    }

    /**
     * Test "ChannelFactory.findAllByUserOrderByChild"
     *
     */
    @Test
    public void testFindAllByUserOrderByChild() {
        User user1 = UserTestUtils.createUser("testuser1", "testorg1");
        User user2 = UserTestUtils.createUser("testuser2", "testorg2");

        Channel parent3 = ChannelFactoryTest.createTestChannel(user1);
        parent3.setLabel("b_parent3");
        ChannelFactory.save(parent3);

        Channel parent2 = ChannelFactoryTest.createTestChannel(user2);
        parent2.setLabel("b_parent2");
        ChannelFactory.save(parent2);

        Channel parent1 = ChannelFactoryTest.createTestChannel(user1);
        parent1.setLabel("b_parent1");
        ChannelFactory.save(parent1);

        Channel child1 = ChannelFactoryTest.createTestChannel(user1);
        child1.setLabel("a_child1");
        child1.setParentChannel(parent1);
        ChannelFactory.save(child1);

        Channel base1 = ChannelFactoryTest.createTestChannel(user1);
        base1.setLabel("z_base1");
        base1.setOrg(null);
        ChannelFactory.save(base1);

        Channel base2 = ChannelFactoryTest.createTestChannel(user2);
        base2.setLabel("z_base2");
        base2.setOrg(null);
        ChannelFactory.save(base2);

        List<Channel> channels = ChannelFactory.findAllByUserOrderByChild(user1);
        assertEquals(4, channels.size());
        assertEquals("z_base1", channels.get(0).getLabel());
        assertEquals("b_parent1", channels.get(1).getLabel());
        assertEquals("a_child1", channels.get(2).getLabel());
        assertEquals("b_parent3", channels.get(3).getLabel());
    }

    @Test
    public void testChannelSyncFlag() {

        User user = UserTestUtils.createUser();
        Channel channel = ChannelFactoryTest.createTestChannel(user);
        ChannelFactory.save(channel);
        long channelId = channel.getId();
        TestUtils.flushAndEvict(channel);

        Channel ch = ChannelFactory.lookupByIdAndUser(channelId, user);
        assertNotNull(ch);
        ChannelSyncFlag csf = ch.getChannelSyncFlag();

        assertNotNull(csf);
        assertFalse(csf.isCreateTree());
        assertFalse(csf.isNoErrata());
        assertFalse(csf.isNoStrict());
        assertFalse(csf.isOnlyLatest());
        assertFalse(csf.isQuitOnError());

        ChannelSyncFlag csf2 = ChannelFactory.lookupChannelReposyncFlag(channel);

        assertNotNull(csf2);
        assertFalse(csf2.isCreateTree());
        assertFalse(csf2.isNoErrata());
        assertFalse(csf2.isNoStrict());
        assertFalse(csf2.isOnlyLatest());
        assertFalse(csf2.isQuitOnError());

        csf.setCreateTree(true);
        csf.setNoErrata(true);
        csf.setNoStrict(true);
        csf.setOnlyLatest(true);
        csf.setQuitOnError(true);

        ChannelFactory.save(csf);
        TestUtils.flushAndEvict(csf);

        assertNotNull(csf);
        assertTrue(csf.isCreateTree());
        assertTrue(csf.isNoErrata());
        assertTrue(csf.isNoStrict());
        assertTrue(csf.isOnlyLatest());
        assertTrue(csf.isQuitOnError());
    }
}
