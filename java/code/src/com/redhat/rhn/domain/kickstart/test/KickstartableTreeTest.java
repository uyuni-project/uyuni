/**
 * Copyright (c) 2009--2015 Red Hat, Inc.
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
package com.redhat.rhn.domain.kickstart.test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.util.FileUtils;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelArch;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.KickstartInstallType;
import com.redhat.rhn.domain.kickstart.KickstartTreeType;
import com.redhat.rhn.domain.kickstart.KickstartableTree;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerXMLRPCHelper;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ChannelTestUtils;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import org.apache.commons.lang3.RandomStringUtils;
import org.cobbler.Distro;
import org.hibernate.Session;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * KickstartableTreeTest
 * @version $Rev$
 */
public class KickstartableTreeTest extends BaseTestCaseWithUser {

    public static final String TEST_BOOT_PATH = "test-boot-image-i186";
    public static final File KICKSTART_TREE_PATH = new File("/tmp/kickstart/images");

    public static void createKickstartTreeItems(User u) throws Exception {
        createKickstartTreeItems(KICKSTART_TREE_PATH, u);
    }

    public static void createKickstartTreeItems(File basePath, User u) throws Exception {
        //Alright setup things we need for trees
        createDirIfNotExists(basePath);
        KickstartableTree tree = new KickstartableTree();
        tree.setChannel(ChannelTestUtils.createBaseChannel(u));
        tree.setInstallType(KickstartFactory.
                lookupKickstartInstallTypeByLabel(KickstartInstallType.RHEL_5));
        tree.setBasePath(basePath.getAbsolutePath());
        tree.setOrg(u.getOrg());
        createKickstartTreeItems(tree);
    }

    public static void createKickstartTreeItems(KickstartableTree tree) throws Exception {
        createDirIfNotExists(new File(tree.getDefaultKernelPaths()[0]).getParentFile());
        createDirIfNotExists(new File(tree.getKernelXenPath()).getParentFile());

        FileUtils.writeStringToFile("kernel", tree.getDefaultKernelPaths()[0]);
        FileUtils.writeStringToFile("kernel-xen", tree.getKernelXenPath());


        createDirIfNotExists(new File(tree.getDefaultInitrdPaths()[0]).getParentFile());
        createDirIfNotExists(new File(tree.getInitrdXenPath()).getParentFile());

        FileUtils.writeStringToFile("initrd-xen", tree.getInitrdXenPath());
        FileUtils.writeStringToFile("initrd", tree.getDefaultInitrdPaths()[0]);
    }

    public void testKickstartableTree() throws Exception {
        KickstartableTree k = createTestKickstartableTree();
        assertNotNull(k);
        assertNotNull(k.getId());

        KickstartableTree k2 = lookupById(k.getId());
        assertEquals(k2.getLabel(), k.getLabel());

        Org o = OrgFactory.lookupById(k2.getOrgId());

        KickstartableTree k3 = KickstartFactory.
            lookupKickstartTreeByLabel(k2.getLabel(), o);
        assertEquals(k3.getLabel(), k2.getLabel());

        List trees = KickstartFactory.
            lookupKickstartTreesByChannelAndOrg(k2.getChannel().getId(), o);

        assertNotNull(trees);
        assertTrue(trees.size() > 0);

        KickstartableTree kwithnullorg = createTestKickstartableTree();
        String label = "treewithnullorg: " + TestUtils.randomString();
        kwithnullorg.setLabel(label);
        kwithnullorg.setOrg(null);
        TestUtils.saveAndFlush(kwithnullorg);
        flushAndEvict(kwithnullorg);
        KickstartableTree lookedUp = KickstartFactory.lookupKickstartTreeByLabel(label, o);
        assertNotNull(lookedUp);
        assertNull(lookedUp.getOrgId());
    }

    public void testIsRhnTree() throws Exception {
        KickstartableTree k = createTestKickstartableTree();
        assertFalse(k.isRhnTree());
        k.setOrg(null);
        assertTrue(k.isRhnTree());
    }

    public void testDownloadLocation() throws Exception {
        KickstartableTree k = createTestKickstartableTree();
        String expected = "/ks/dist/org/" + k.getOrg().getId() + "/" +
                                k.getLabel();
        assertEquals(expected, k.getDefaultDownloadLocation());
    }

    public void testKsDataByTree() throws Exception {
        KickstartableTree k = createTestKickstartableTree(
                ChannelFactoryTest.createTestChannel(user));
        KickstartData ksdata = KickstartDataTest.
            createKickstartWithOptions(user.getOrg());
        ksdata.getKickstartDefaults().setKstree(k);
        KickstartFactory.saveKickstartData(ksdata);
        flushAndEvict(ksdata);

        List profiles = KickstartFactory.lookupKickstartDatasByTree(k);
        assertNotNull(profiles);
        assertTrue(profiles.size() > 0);
    }


    /**
     * Helper method to lookup KickstartableTree by id
     * @param id Id to lookup
     * @return Returns the KickstartableTree
     * @throws Exception something bad happened
     */
    private KickstartableTree lookupById(Long id) throws Exception {
        Session session = HibernateFactory.getSession();
        return (KickstartableTree) session.getNamedQuery("KickstartableTree.findById")
                          .setLong("id", id.longValue())
                          .uniqueResult();
    }

    /**
     * Creates KickstartableTree for testing purposes.
     * @return Returns a committed KickstartableTree
     * @throws Exception something bad happened
     */
    public static KickstartableTree createTestKickstartableTree() throws Exception {
        User u = UserTestUtils.findNewUser("testUser", "testCreateTestKickstartableTree");
        Channel channel = ChannelFactoryTest.createTestChannel(u);
        ChannelTestUtils.addDistMapToChannel(channel);
        return createTestKickstartableTree(channel);
    }

    /**
     * Creates KickstartableTree for testing purposes.
     * @param treeChannel Channel this Tree uses.
     * @return Returns a committed KickstartableTree
     * @throws Exception something bad happened
     */
    public static KickstartableTree
        createTestKickstartableTree(Channel treeChannel) throws Exception {
        Date created = new Date();
        Date modified = new Date();
        Date lastmodified = new Date();

        Long testid = 1L;
        String query = "KickstartInstallType.findById";
        KickstartInstallType installtype = (KickstartInstallType)
                                            TestUtils.lookupFromCacheById(testid, query);

        query = "KickstartTreeType.findById";
        KickstartTreeType treetype = (KickstartTreeType)
                                     TestUtils.lookupFromCacheById(testid, query);

        KickstartableTree k = new KickstartableTree();
        k.setLabel("ks-" + treeChannel.getLabel() +
                RandomStringUtils.randomAlphanumeric(5));

        k.setBasePath(KICKSTART_TREE_PATH.getAbsolutePath());
        k.setCreated(created);
        k.setModified(modified);
        k.setOrg(treeChannel.getOrg());
        k.setLastModified(lastmodified);
        k.setInstallType(installtype);
        k.setTreeType(treetype);
        k.setChannel(treeChannel);
        k.setKernelOptions("");
        k.setKernelOptionsPost("");

        createKickstartTreeItems(k);

        Distro.Builder builder = new Distro.Builder();

        Distro d = builder.setName(k.getLabel())
                .setKernel(k.getDefaultKernelPaths()[0])
                .setInitrd(k.getDefaultInitrdPaths()[0])
                .setKsmeta(new HashMap<String, Object>())
                .setBreed(k.getInstallType().getCobblerBreed())
                .setOsVersion(k.getInstallType().getCobblerOsVersion())
                .setArch(k.getChannel().getChannelArch().cobblerArch())
                .setKernelOptions(k.getKernelOptions())
                .setKernelOptionsPost(k.getKernelOptionsPost())
                .build(CobblerXMLRPCHelper.getConnection("test"));

        Distro xend = builder.setKsmeta(new HashMap<String, Object>())
                .build(CobblerXMLRPCHelper.getConnection("test"));

        k.setCobblerId(d.getUid());
        k.setCobblerXenId(xend.getUid());

        TestUtils.saveAndFlush(k);


        return k;
    }

    /**
     * Create a KickstartableTree for testing purposes using the given install type.
     * @param treeChannel channel to use for this tree.
     * @param installTypeLabel install type to use
     * @return the kickstartable tree
     * @throws Exception something bad happened
     */
    public static KickstartableTree createTestKickstartableTree(
            Channel treeChannel, String installTypeLabel) throws Exception {
        KickstartableTree tree = createTestKickstartableTree(treeChannel);
        String query = "KickstartInstallType.findByLabel";
        KickstartInstallType installtype = (KickstartInstallType)
                TestUtils.lookupFromCacheByLabel(installTypeLabel, query);
        tree.setInstallType(installtype);
        TestUtils.saveAndFlush(tree);
        return tree;
    }

    private KickstartableTree createSUSEKsTreeByArch(Long archId, File ksRoot)
            throws Exception {
        ChannelArch arch = (ChannelArch)
                TestUtils.lookupFromCacheById(archId, "ChannelArch.findById");
        KickstartInstallType suseInstallType = (KickstartInstallType)
                TestUtils.lookupFromCacheById(9L, "KickstartInstallType.findById");
        KickstartTreeType treetype = (KickstartTreeType)
                TestUtils.lookupFromCacheById(1L, "KickstartTreeType.findById");

        User u = UserTestUtils.findNewUser("testUser", "testCreateTestKickstartableTree");
        Channel channel = ChannelFactoryTest.createTestChannel(u);
        ChannelTestUtils.addDistMapToChannel(channel);
        channel.setChannelArch(arch);

        KickstartableTree tree = new KickstartableTree();
        tree.setBasePath(ksRoot.getAbsolutePath());
        tree.setCreated(new Date());
        tree.setModified(new Date());
        tree.setOrg(channel.getOrg());
        tree.setLastModified(new Date());
        tree.setInstallType(suseInstallType);
        tree.setTreeType(treetype);
        tree.setChannel(channel);

        return tree;
    }

    public void testSUSEStartupPaths() throws Exception {
        File ksRoot = new File("/media");

        Map<Long, String[]> archMap = new LinkedHashMap<Long, String[]>();
        archMap.put(500L, new String[]{"i386", "/media/boot/%s/loader/linux",
                                               "/media/boot/%s/loader/initrd"});
        archMap.put(502L, new String[]{"ia64", "/media/boot/%s/image",
                                               "/media/boot/%s/initrd"});
        archMap.put(502L, new String[]{"ia64", "/media/boot/%s/image",
                                               "/media/boot/%s/initdisk.gz"});
        archMap.put(508L, new String[]{"s390", "/media/boot/%s/vmrdr.ikr",
                                               "/media/boot/%s/initrd"});
        archMap.put(508L, new String[]{"s390", "/media/boot/%s/linux",
                                               "/media/boot/%s/initrd"});
        archMap.put(510L, new String[]{"s390x", "/media/boot/%s/vmrdr.ikr",
                                                "/media/boot/%s/initrd"});
        archMap.put(510L, new String[]{"s390x", "/media/boot/%s/linux",
                                                "/media/boot/%s/initrd"});
        archMap.put(513L, new String[]{"x86_64", "/media/boot/%s/loader/linux",
                                                 "/media/boot/%s/loader/initrd"});
        archMap.put(515L, new String[]{"ppc64", "/media/suseboot/inst64",
                                                "/media/suseboot/initrd64"});
        archMap.put(515L, new String[]{"ppc64", "/media/suseboot/linux64",
                                                "/media/suseboot/initrd64"});
        archMap.put(516L, new String[]{"ppc64le", "/media/boot/%s/linux",
                                                  "/media/boot/%s/initrd"});
        archMap.put(520L, new String[]{"aarch64", "/media/boot/%s/linux",
                                                  "/media/boot/%s/initrd"});

        for (Map.Entry<Long, String[]> entry : archMap.entrySet()) {
            KickstartableTree tree = this.createSUSEKsTreeByArch(entry.getKey(), ksRoot);
            assertContains(Arrays.asList(tree.getDefaultKernelPaths()),
                           String.format(entry.getValue()[1], entry.getValue()[0]));
            assertContains(Arrays.asList(tree.getDefaultInitrdPaths()),
                           String.format(entry.getValue()[2], entry.getValue()[0]));
        }
    }

    /**
     * Tests listing candidates for the the cobbler backsync.
     * @throws Exception if anything goes wrong
     */
    public void testListCandidatesForBacksync() throws Exception {
        KickstartableTree k = createTestKickstartableTree(
                ChannelFactoryTest.createTestChannel(user));

        assertEquals(Collections.singletonList(k),
                KickstartFactory.listCandidatesForBacksync());
    }

    /**
     * Tests listing candidates for the the cobbler backsync when there are no candidates.
     * @throws Exception if anything goes wrong
     */
    public void testListCandidatesForBacksyncNoSync() throws Exception {
        KickstartableTree k = createTestKickstartableTree(
                ChannelFactoryTest.createTestChannel(user));
        k.setKernelOptions("option1=val1");
        KickstartFactory.saveKickstartableTree(k);
        KickstartableTree k2 = createTestKickstartableTree(
                ChannelFactoryTest.createTestChannel(user));
        k2.setKernelOptionsPost("option2=val2");
        KickstartFactory.saveKickstartableTree(k2);

        assertTrue(KickstartFactory.listCandidatesForBacksync().isEmpty());
    }
}
