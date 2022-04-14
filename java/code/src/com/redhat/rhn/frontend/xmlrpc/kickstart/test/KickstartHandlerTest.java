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
package com.redhat.rhn.frontend.xmlrpc.kickstart.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.KickstartInstallType;
import com.redhat.rhn.domain.kickstart.KickstartIpRange;
import com.redhat.rhn.domain.kickstart.KickstartRawData;
import com.redhat.rhn.domain.kickstart.KickstartVirtualizationType;
import com.redhat.rhn.domain.kickstart.KickstartableTree;
import com.redhat.rhn.domain.kickstart.test.KickstartDataTest;
import com.redhat.rhn.domain.kickstart.test.KickstartableTreeTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.kickstart.KickstartDto;
import com.redhat.rhn.frontend.xmlrpc.PermissionCheckFailureException;
import com.redhat.rhn.frontend.xmlrpc.kickstart.InvalidKickstartLabelException;
import com.redhat.rhn.frontend.xmlrpc.kickstart.InvalidVirtualizationTypeException;
import com.redhat.rhn.frontend.xmlrpc.kickstart.KickstartHandler;
import com.redhat.rhn.frontend.xmlrpc.kickstart.NoSuchKickstartTreeException;
import com.redhat.rhn.frontend.xmlrpc.kickstart.tree.KickstartTreeHandler;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;
import com.redhat.rhn.manager.kickstart.IpAddress;
import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * KickstartHandlerTest
 */
public class KickstartHandlerTest extends BaseHandlerTestCase {

    private KickstartHandler handler = new KickstartHandler();

    @Test
    public void testListKickstartableChannels() throws Exception {
        Channel baseChan = ChannelFactoryTest.createTestChannel(admin);
        KickstartableTreeTest.createTestKickstartableTree(baseChan,
                KickstartInstallType.FEDORA_PREFIX + "18");
        List<Channel> ksChannels = handler.listKickstartableChannels(admin);
        assertTrue(ksChannels.size() > 0);
        assertTrue(ksChannels.contains(baseChan));
    }

    @Test
    public void testListAutoinstallableChannels() throws Exception {
        Channel baseChan = ChannelFactoryTest.createTestChannel(admin);
        KickstartableTreeTest.createTestKickstartableTree(baseChan,
                KickstartInstallType.SLES_PREFIX + "12generic");
        List<Channel> ksChannels = handler.listAutoinstallableChannels(admin);
        assertTrue(ksChannels.size() > 0);
        assertTrue(ksChannels.contains(baseChan));
    }

    @Test
    public void testListKickstartableTrees() throws Exception {
        Channel baseChan = ChannelFactoryTest.createTestChannel(admin);
        KickstartableTree testTree = KickstartableTreeTest.
            createTestKickstartableTree(baseChan);
        List ksTrees = new KickstartTreeHandler().list(admin, baseChan.getLabel());
        assertTrue(ksTrees.size() > 0);

        boolean found = false;
        for (Object ksTreeIn : ksTrees) {
            KickstartableTree t = (KickstartableTree) ksTreeIn;
            if (t.getId().equals(testTree.getId())) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    @Test
    public void testListKickstartableTreesByNonExistentChannelLabel() throws Exception {
        try {
            new KickstartTreeHandler().list(admin, "no such label");
            fail();
        }
        catch (Exception e) {
            // expected
        }
    }

    @Test
    public void testFullImport() throws Exception {
        Channel baseChan = ChannelFactoryTest.createTestChannel(admin);
        KickstartableTree testTree = KickstartableTreeTest.
            createTestKickstartableTree(baseChan);
        runImport(admin, testTree.getLabel());
    }

    @Test
    public void testInvalidKickstartLabel() throws Exception {
        Channel baseChan = ChannelFactoryTest.createTestChannel(admin);
        KickstartableTree testTree = KickstartableTreeTest.
            createTestKickstartableTree(baseChan);
        String kickstartFileContents = TestUtils.readAll(TestUtils.findTestData(
                "samplekickstart1.ks"));

        try {
            handler.importFile(admin, "a", KickstartVirtualizationType.XEN_PARAVIRT,
                    testTree.getLabel(), kickstartFileContents);
            fail();
        }
        catch (ValidatorException e) {
            // expected
        }
    }

    public void runImport(User user, String treeLabel) throws Exception {

        String newKsProfileLabel = "test-" + TestUtils.randomString();
        String kickstartFileContents = TestUtils.readAll(TestUtils.findTestData(
                "samplekickstart1.ks"));
        handler.importFile(user, newKsProfileLabel, KickstartVirtualizationType.XEN_PARAVIRT,
                treeLabel, kickstartFileContents);

        KickstartData newKsProfile = KickstartFactory.lookupKickstartDataByLabelAndOrgId(
                newKsProfileLabel, admin.getOrg().getId());
        assertNotNull(newKsProfile);
    }


    @Test
    public void testImportRawFile() throws Exception {

        // Imports should require the same permissions as create, org or config admin.
        Channel baseChan = ChannelFactoryTest.createTestChannel(admin);
        KickstartableTree testTree = KickstartableTreeTest.
            createTestKickstartableTree(baseChan);

        String newKsProfileLabel = "test-" + TestUtils.randomString();
        String kickstartFileContents = TestUtils.readAll(TestUtils.findTestData(
                "samplekickstart1.ks"));
        try {
            handler.importRawFile(regular, newKsProfileLabel,
                    KickstartVirtualizationType.XEN_PARAVIRT,
                    testTree.getLabel(), kickstartFileContents);
            fail("No permission check failure");
        }
        catch (PermissionCheckFailureException pe) {
            //cool!
        }
        handler.importRawFile(admin, newKsProfileLabel,
                KickstartVirtualizationType.XEN_PARAVIRT,
                testTree.getLabel(), kickstartFileContents);

        KickstartRawData newKsProfile = (KickstartRawData)KickstartFactory.
                                                    lookupKickstartDataByLabelAndOrgId(
                newKsProfileLabel, admin.getOrg().getId());
        assertNotNull(newKsProfile);
    }

    @Test
    public void testImportAsRegularUser() throws Exception {
        // Imports should require the same permissions as create, org or config admin.
        Channel baseChan = ChannelFactoryTest.createTestChannel(admin);
        KickstartableTree testTree = KickstartableTreeTest.
            createTestKickstartableTree(baseChan);
        try {
            runImport(regular, testTree.getLabel());
            fail();
        }
        catch (PermissionCheckFailureException e) {
            // expected
        }
    }

    @Test
    public void testNoSuchKickstartTreeLabel() throws Exception {
        try {
            runImport(admin, "nosuchlabel");
            fail();
        }
        catch (NoSuchKickstartTreeException e) {
            // expected
        }
    }

    @Test
    public void testCreate() throws Exception {
        Channel baseChan = ChannelFactoryTest.createTestChannel(admin);
        KickstartableTree testTree = KickstartableTreeTest.
            createTestKickstartableTree(baseChan);

        String profileLabel = "new-ks-profile";
        handler.createProfile(admin, profileLabel,
                KickstartVirtualizationType.XEN_PARAVIRT,
                testTree.getLabel(), "localhost", "rootpw");

        KickstartData newKsProfile = KickstartFactory.lookupKickstartDataByLabelAndOrgId(
                profileLabel, admin.getOrg().getId());
        assertNotNull(newKsProfile);
        assertTrue(newKsProfile.getCommand("url").getArguments().contains("/ks/dist/org/"));
    }

    @Test
    public void testCreateWithInvalidRoles() throws Exception {
        Channel baseChan = ChannelFactoryTest.createTestChannel(admin);
        KickstartableTree testTree = KickstartableTreeTest.
            createTestKickstartableTree(baseChan);

        String profileLabel = "new-ks-profile";
        try {
            handler.createProfileWithCustomUrl(regular, profileLabel,
                    KickstartVirtualizationType.XEN_PARAVIRT,
                    testTree.getLabel(), "default", "rootpw");
            fail();
        }
        catch (PermissionCheckFailureException e) {
            // expected
        }
    }

    @Test
    public void testCreateWithInvalidKickstartLabel() throws Exception {
        String profileLabel = "new-ks-profile";
        try {
            handler.createProfileWithCustomUrl(admin, profileLabel, "none",
                    "nosuchtree", "default", "rootpw");
            fail();
        }
        catch (NoSuchKickstartTreeException e) {
            // expected
        }
    }

    @Test
    public void testCreateWithInvalidLabel() throws Exception {
        Channel baseChan = ChannelFactoryTest.createTestChannel(admin);
        KickstartableTree testTree = KickstartableTreeTest.
            createTestKickstartableTree(baseChan);

        String profileLabel = "short";
        try {
            handler.createProfileWithCustomUrl(admin, profileLabel, "none",
                    testTree.getLabel(), "default", "rootpw");
            fail();
        }
        catch (ValidatorException ve) {
            // expected
        }
    }

    @Test
    public void testCreateWithInvalidVirtType() throws Exception {
        Channel baseChan = ChannelFactoryTest.createTestChannel(admin);
        KickstartableTree testTree = KickstartableTreeTest.
            createTestKickstartableTree(baseChan);

        String profileLabel = "test-ks-profile";
        try {
            handler.createProfileWithCustomUrl(admin, profileLabel,
                    "fakevirttype", testTree.getLabel(), "default", "rootpw");
            fail();
        }
        catch (InvalidVirtualizationTypeException e) {
            // expected
        }
    }

    @Test
    public void testListKickstarts() throws Exception {
        KickstartData ks  = KickstartDataTest.createKickstartWithProfile(admin);
        String label = ks.getLabel();
        KickstartFactory.saveKickstartData(ks);
        ks = (KickstartData) TestUtils.reload(ks);

        List<KickstartDto> list = handler.listKickstarts(admin);
        boolean foundKs = false;
        for (KickstartDto ksDto : list) {
            assertNotNull(ksDto.getTreeLabel());
            if (ksDto.getLabel().equals(label)) {
                foundKs = true;
            }
        }
        assertTrue(foundKs);
    }

    @Test
    public void testRenameProfile() throws Exception {
        KickstartData ks  = KickstartDataTest.createKickstartWithProfile(admin);
        String label = ks.getLabel();
        KickstartFactory.saveKickstartData(ks);
        ks = (KickstartData) TestUtils.reload(ks);

        String newLabel = TestUtils.randomString();
        String oldLabel = ks.getLabel();

        try {
            handler.renameProfile(admin, ks.getLabel(),
                ks.getLabel());
            fail("We should have got a InvalidKickstartLabelException");
        }
        catch (InvalidKickstartLabelException le) {
            // Do nothing
        }

        handler.renameProfile(admin, ks.getLabel(),
                newLabel);

        ks = (KickstartData) reload(ks);
        assertEquals(newLabel, ks.getLabel());
    }

    private KickstartData setupIpRanges() throws Exception {
        KickstartData ks1  = KickstartDataTest.createKickstartWithProfile(admin);
        KickstartIpRange range = new KickstartIpRange();
        range.setMax(new IpAddress("192.168.0.10").getNumber());
        range.setMin(new IpAddress("192.168.0.1").getNumber());
        range.setKsdata(ks1);
        range.setOrg(admin.getOrg());
        ks1.getIps().add(range);
        KickstartFactory.saveKickstartData(ks1);
        return ks1;
    }

    @Test
    public void testListAllIpRanges() throws Exception {
        KickstartData ks1 = setupIpRanges();
        List list = handler.listAllIpRanges(admin);
        assertContains(list, ks1.getIps().iterator().next());
    }

    @Test
    public void testFindKickstartForIp() throws Exception {
        KickstartData ks1 = setupIpRanges();
        String label = handler.findKickstartForIp(admin, "192.168.0.5");
        assertEquals(label, ks1.getLabel());
    }

    @Test
    public void testDeleteProfile() throws Exception {
        KickstartData ksdata =
            KickstartDataTest.createKickstartWithChannel(admin.getOrg());
        Integer i = handler.deleteProfile(admin, ksdata.getLabel());
        assertEquals(Integer.valueOf(1), i);
    }
}
