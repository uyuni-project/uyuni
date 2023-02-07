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
package com.redhat.rhn.manager.profile.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.action.rhnpackage.PackageAction;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.NoBaseChannelFoundException;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageArch;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.rhnpackage.profile.Profile;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerConstants;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.frontend.dto.PackageListItem;
import com.redhat.rhn.frontend.dto.PackageMetadata;
import com.redhat.rhn.frontend.dto.ProfileDto;
import com.redhat.rhn.manager.profile.ProfileManager;
import com.redhat.rhn.manager.rhnpackage.test.PackageManagerTest;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ChannelTestUtils;
import com.redhat.rhn.testing.ServerTestUtils;
import com.redhat.rhn.testing.TestUtils;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ProfileManagerTest
 */
public class ProfileManagerTest extends BaseTestCaseWithUser {

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        // Give our user ORG_ADMIN
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        UserFactory.save(user);
    }

    @Test
    public void testSyncSystems() throws Exception {
        Channel testChannel = ChannelFactoryTest.createTestChannel(user);

        Package p1 = PackageTest.createTestPackage(user.getOrg());
        Package p2 = PackageTest.createTestPackage(user.getOrg());

        testChannel.addPackage(p1);
        testChannel.addPackage(p2);
        ChannelFactory.save(testChannel);

        Server s1 = ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeEnterpriseEntitled());
        Server s2 = ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeEnterpriseEntitled());

        s1.addChannel(testChannel);
        s2.addChannel(testChannel);

        PackageManagerTest.associateSystemToPackageWithArch(s1, p1);
        PackageManagerTest.associateSystemToPackageWithArch(s2, p2);

        ServerFactory.save(s1);
        ServerFactory.save(s2);

        Set<String> idCombos = new HashSet<>();
        String idCombo = p1.getPackageName().getId() + "|" +
                p1.getPackageEvr().getId() + "|" +
                p1.getPackageArch().getId();
        idCombos.add(idCombo);

        // This call has an embedded transaction in the stored procedure:
        // lookup_transaction_package(:operation, :n, :e, :v, :r, :a)
        // which can cause deadlocks.  We are forced to call commitAndCloseTransaction()
        commitAndCloseSession();
        commitHappened();

        PackageAction action = ProfileManager.syncToSystem(
                user, s1.getId(), s2.getId(), idCombos,
                ProfileManager.OPTION_REMOVE, new Date());
        assertNotNull(action);
        assertNotNull(action.getPrerequisite());
    }


    @Test
    public void testCreateProfileFails() {
        Server server = ServerFactoryTest.createTestServer(user, true);

        try {
            ProfileManager.createProfile(user, server,
                    "Profile test name" + TestUtils.randomString(),
                    "Profile test description");
            fail("Should not be able to create a profile for a server which " +
                 "has no basechannel");
        }
        catch (NoBaseChannelFoundException nbcfe) {
            assertTrue(true);
        }
    }

    @Test
    public void testCreateProfile() throws Exception {
        Server server = ServerFactoryTest.createTestServer(user, true);
        Channel channel = ChannelFactoryTest.createTestChannel(user);
        server.addChannel(channel);
        TestUtils.saveAndFlush(server);

        Profile p = ProfileManager.createProfile(user, server,
                "Profile test name" + TestUtils.randomString(),
                "Profile test description");
        assertNotNull(p, "Profile is null");
        assertNotNull(p.getId(), "Profile has no id");
    }

    @Test
    public void testCopyFrom() throws Exception {
        Server server = ServerFactoryTest.createTestServer(user, true);
        Channel channel = ChannelFactoryTest.createTestChannel(user);
        server.addChannel(channel);
        TestUtils.saveAndFlush(server);

        Profile p = ProfileManager.createProfile(user, server,
                "Profile test name" + TestUtils.randomString(),
                "Profile test description");
        assertNotNull(p, "Profile is null");
        assertNotNull(p.getId(), "Profile has no id");

        ProfileManager.copyFrom(server, p);
    }

    @Test
    public void testCompatibleWithServer() throws Exception {
        Server server = ServerFactoryTest.createTestServer(user, true);
        Channel channel = ChannelFactoryTest.createTestChannel(user);
        server.addChannel(channel);
        TestUtils.saveAndFlush(server);
        Profile p = ProfileManager.createProfile(user, server,
                "Profile test name" + TestUtils.randomString(),
                "Profile test description");
        assertNotNull(p, "Profile is null");
        assertNotNull(p.getId(), "Profile has no id");

        List<Profile> list = ProfileManager.compatibleWithServer(server, user.getOrg());
        assertNotNull(list, "List is null");
        assertFalse(list.isEmpty(), "List is empty");
        for (Object o : list) {
            assertEquals(Profile.class, o.getClass(), "List contains something other than Profiles");
        }
    }

    @Test
    public void testCompareServerToProfile() {
        Long sid = 1005385254L;
        Long prid = 4908L;
        Long orgid = 4116748L;
        DataResult<PackageMetadata> dr = ProfileManager.compareServerToProfile(sid, prid, orgid, null);
        assertNotNull(dr, "DataResult was null");
    }

    @Test
    public void testCompatibleWithChannel() throws Exception {
        Profile p = createProfileWithServer(user);
        DataResult<ProfileDto> dr = ProfileManager.compatibleWithChannel(p.getBaseChannel(),
                user.getOrg(), null);
        assertNotNull(dr);
        assertFalse(dr.isEmpty());
        assertTrue(dr.iterator().next() instanceof ProfileDto);

    }

    public static Profile createProfileWithServer(User userIn) throws Exception {
        Server server = ServerFactoryTest.createTestServer(userIn, true);
        Channel channel = ChannelFactoryTest.createTestChannel(userIn);
        server.addChannel(channel);
        TestUtils.saveAndFlush(server);

        return ProfileManager.createProfile(userIn, server,
                "Profile test name" + TestUtils.randomString(),
        "Profile test description");
    }

    @Test
    public void testTwoVsOneKernelPackages()  {
        /*
         *     public static List comparePackageLists(DataResult profiles,
            DataResult systems, String param) {
         */

        List<PackageListItem> a = new ArrayList<>();
        PackageListItem pli = new PackageListItem();
        pli.setIdCombo("500000341|258204");
        pli.setEvrId(258204L);
        pli.setName("kernel");
        pli.setRelease("27.EL");
        pli.setNameId(500000341L);
        pli.setEvr("kernel-2.4.21-27.EL");
        pli.setVersion("2.4.21");
        pli.setEpoch(null);
        pli.setPackageType("rpm");
        a.add(pli);

        pli = new PackageListItem();
        pli.setIdCombo("500000341|000000");
        pli.setEvrId(000000L);
        pli.setName("kernel");
        pli.setRelease("27.EL-bretm");
        pli.setNameId(500000341L);
        pli.setEvr("kernel-2.4.22-27.EL-bretm");
        pli.setVersion("2.4.22");
        pli.setEpoch(null);
        pli.setPackageType("rpm");
        a.add(pli);

        List<PackageListItem> b = new ArrayList<>();
        pli = new PackageListItem();
        pli.setIdCombo("500000341|258204");
        pli.setEvrId(258204L);
        pli.setName("kernel");
        pli.setRelease("27.EL");
        pli.setNameId(500000341L);
        pli.setEvr("kernel-2.4.21-27.EL");
        pli.setVersion("2.4.21");
        pli.setEpoch(null);
        pli.setPackageType("rpm");
        b.add(pli);

        List<PackageMetadata> diff = ProfileManager.comparePackageLists(new DataResult<>(a),
                new DataResult<>(b), "foo");

        assertEquals(1, diff.size());
        PackageMetadata pm = (PackageMetadata) diff.get(0);
        assertNotNull(pm);
        // assertEquals(PackageMetadata.KEY_OTHER_NEWER, pm.getComparisonAsInt());
        // Changed this to KEY_OTHER_ONLY because for systems with multiple revs of
        // same package we are now
        assertEquals(PackageMetadata.KEY_OTHER_ONLY, pm.getComparisonAsInt());
        assertEquals("kernel-2.4.22-27.EL-bretm", pm.getOther().getEvr());
    }

    @Test
    public void testDifferingVersionsofSamePackage() {
        List<PackageListItem> a = new ArrayList<>();
        PackageListItem pli = new PackageListItem();
        pli.setIdCombo("500000341|000000");
        pli.setEvrId(000000L);
        pli.setName("kernel");
        pli.setRelease("27.EL-bretm");
        pli.setNameId(500000341L);
        pli.setEvr("kernel-2.4.22-27.EL-bretm");
        pli.setVersion("2.4.22");
        pli.setEpoch(null);
        pli.setPackageType("rpm");
        a.add(pli);

        List<PackageListItem> b = new ArrayList<>();
        pli = new PackageListItem();
        pli.setIdCombo("500000341|258204");
        pli.setEvrId(258204L);
        pli.setName("kernel");
        pli.setRelease("27.EL");
        pli.setNameId(500000341L);
        pli.setEvr("kernel-2.4.21-27.EL");
        pli.setVersion("2.4.21");
        pli.setEpoch(null);
        pli.setPackageType("rpm");
        b.add(pli);

        List<PackageMetadata> diff = ProfileManager.comparePackageLists(new DataResult<>(a),
                new DataResult<>(b), "foo");
        assertEquals(1, diff.size());
        PackageMetadata pm = (PackageMetadata) diff.get(0);
        assertNotNull(pm);
        assertEquals(PackageMetadata.KEY_OTHER_NEWER, pm.getComparisonAsInt());
        assertEquals("kernel-2.4.22-27.EL-bretm", pm.getOther().getEvr());
        assertEquals("kernel-2.4.21-27.EL", pm.getSystem().getEvr());
    }

    @Test
    public void testDifferentVersionsOfSamePackageReverseOrder() {
        List<PackageListItem> b = new ArrayList<>();
        PackageListItem pli = new PackageListItem();
        pli.setIdCombo("500000341|000000");
        pli.setEvrId(000000L);
        pli.setName("kernel");
        pli.setRelease("27.EL-bretm");
        pli.setNameId(500000341L);
        pli.setEvr("kernel-2.4.22-27.EL-bretm");
        pli.setVersion("2.4.22");
        pli.setEpoch(null);
        pli.setPackageType("rpm");
        b.add(pli);

        List<PackageListItem> a = new ArrayList<>();
        pli = new PackageListItem();
        pli.setIdCombo("500000341|258204");
        pli.setEvrId(258204L);
        pli.setName("kernel");
        pli.setRelease("27.EL");
        pli.setNameId(500000341L);
        pli.setEvr("kernel-2.4.21-27.EL");
        pli.setVersion("2.4.21");
        pli.setEpoch(null);
        pli.setPackageType("rpm");
        a.add(pli);

        List<PackageMetadata> diff = ProfileManager.comparePackageLists(new DataResult<>(a),
                new DataResult<>(b), "foo");
        assertEquals(1, diff.size());
        PackageMetadata pm = (PackageMetadata) diff.get(0);
        assertNotNull(pm);
        assertEquals(PackageMetadata.KEY_THIS_NEWER, pm.getComparisonAsInt());
        assertEquals("kernel-2.4.22-27.EL-bretm", pm.getSystem().getEvr());
        assertEquals("kernel-2.4.21-27.EL", pm.getOther().getEvr());
    }

    @Test
    public void testDifferingEpochsofSamePackage() {
        // this test will perform a package comparison between 2 packages where
        // the epochs in those packages vary, including null values

        String[] pkg1Epochs = {null, "0", null, "0"};
        String[] pkg2Epochs = {null, null, "0", "0"};

        List<PackageListItem> a = new ArrayList<>();
        PackageListItem pli1 = new PackageListItem();
        pli1.setIdCombo("500000341|000000");
        pli1.setEvrId(000000L);
        pli1.setName("kernel");
        pli1.setRelease("27.EL-bretm");
        pli1.setNameId(500000341L);
        pli1.setEvr("kernel-2.4.22-27.EL-bretm");
        pli1.setVersion("2.4.22");
        pli1.setPackageType("rpm");

        List<PackageListItem> b = new ArrayList<>();
        PackageListItem pli2 = new PackageListItem();
        pli2.setIdCombo("500000341|258204");
        pli2.setEvrId(258204L);
        pli2.setName("kernel");
        pli2.setRelease("27.EL");
        pli2.setNameId(500000341L);
        pli2.setEvr("kernel-2.4.21-27.EL");
        pli2.setVersion("2.4.21");
        pli2.setPackageType("rpm");

        for (int i = 0; i < pkg1Epochs.length; i++) {
            pli1.setEpoch(pkg1Epochs[i]);
            pli2.setEpoch(pkg2Epochs[i]);

            a.clear();
            a.add(pli1);
            b.clear();
            b.add(pli2);

            List<PackageMetadata> diff = ProfileManager.comparePackageLists(
                    new DataResult<>(a), new DataResult<>(b), "foo");
            assertEquals(1, diff.size());
            PackageMetadata pm = (PackageMetadata) diff.get(0);
            assertNotNull(pm);
            assertEquals(PackageMetadata.KEY_OTHER_NEWER, pm.getComparisonAsInt());
            assertEquals("kernel-2.4.22-27.EL-bretm", pm.getOther().getEvr());
            assertEquals(pkg1Epochs[i], pm.getOther().getEpoch());
            assertEquals("kernel-2.4.21-27.EL", pm.getSystem().getEvr());
            assertEquals(pkg2Epochs[i], pm.getSystem().getEpoch());
        }
    }

    public static PackageListItem createItem(String evrString, int nameId) {
        PackageListItem pli = new PackageListItem();
        String[] evr = StringUtils.split(evrString, "-");
        pli.setName(evr[0]);
        pli.setVersion(evr[1]);
        pli.setRelease(evr[2]);
        pli.setEvrId((long) evrString.hashCode());
        pli.setIdCombo(nameId + "|" + evrString.hashCode());
        pli.setEvr(evrString);
        pli.setNameId((long) nameId);
        pli.setPackageType("rpm");
        return pli;
    }

    @Test
    public void testMorePackagesInProfile() {
        List<PackageListItem> profileList = new ArrayList<>();
        profileList.add(createItem("kernel-2.4.21-EL-mmccune", 500341));
        profileList.add(createItem("kernel-2.4.22-EL-mmccune", 500341));
        profileList.add(createItem("kernel-2.4.23-EL-mmccune", 500341));
        profileList.add(createItem("other-2.4.23-EL-mmccune", 500400));

        List<PackageListItem> systemList = new ArrayList<>();
        systemList.add(createItem("kernel-2.4.23-EL-mmccune", 500341));

        List<PackageMetadata> diff = ProfileManager.comparePackageLists(new DataResult<>(profileList),
                new DataResult<>(systemList), "system");
        assertEquals(3, diff.size());

    }

    @Test
    public void testMorePackagesInSystem() {
        List<PackageListItem> profileList = new ArrayList<>();
        profileList.add(createItem("kernel-2.4.23-EL-mmccune", 500341));

        List<PackageListItem> systemList = new ArrayList<>();
        systemList.add(createItem("kernel-2.4.21-EL-mmccune", 500341));
        systemList.add(createItem("kernel-2.4.22-EL-mmccune", 500341));
        systemList.add(createItem("kernel-2.4.23-EL-mmccune", 500341));

        List<PackageMetadata> diff = ProfileManager.comparePackageLists(new DataResult<>(profileList),
                new DataResult<>(systemList), "system");
        assertEquals(2, diff.size());
    }

    public static PackageListItem createPackageListItem(String evrString, int nameId) {
        PackageListItem pli = new PackageListItem();
        String[] evr = StringUtils.split(evrString, "-");
        pli.setName(evr[0]);
        pli.setVersion(evr[1]);
        pli.setRelease(evr[2]);
        pli.setEvrId((long) evrString.hashCode());
        pli.setIdCombo(nameId + "|" + evrString.hashCode());
        pli.setEvr(evrString);
        pli.setNameId((long) nameId);
        PackageArch pa = PackageFactory.lookupPackageArchByLabel("x86_64");
        pli.setArch(pa.getLabel());
        pli.setArchId(pa.getId());
        pli.setPackageType("rpm");
        return pli;
    }

    @Test
     public void testIdenticalPackages() {
        List<PackageListItem> a = new ArrayList<>();
        PackageListItem pli = new PackageListItem();
        pli.setIdCombo("500000341|000000");
        pli.setEvrId(000000L);
        pli.setName("kernel");
        pli.setRelease("27.EL-bretm");
        pli.setNameId(500000341L);
        pli.setEvr("kernel-2.4.22-27.EL-bretm");
        pli.setVersion("2.4.22");
        pli.setEpoch(null);
        pli.setPackageType("rpm");
        a.add(pli);


        List<PackageListItem> b = new ArrayList<>();
        pli = new PackageListItem();
        pli.setIdCombo("500000341|000000");
        pli.setEvrId(000000L);
        pli.setName("kernel");
        pli.setRelease("27.EL-bretm");
        pli.setNameId(500000341L);
        pli.setEvr("kernel-2.4.22-27.EL-bretm");
        pli.setVersion("2.4.22");
        pli.setEpoch(null);
        pli.setPackageType("rpm");
        b.add(pli);

        List<PackageMetadata> diff = ProfileManager.comparePackageLists(new DataResult<>(a),
                new DataResult<>(b), "foo");
        assertEquals(0, diff.size());
    }

    @Test
    public void testVzlatkinTest() {
        List<PackageListItem> a = new ArrayList<>();
        PackageListItem pli = new PackageListItem();
        pli.setIdCombo("390|2069");
        pli.setEvrId(2069L);
        pli.setName("kernel");
        pli.setRelease("5.0.3.EL");
        pli.setNameId(390L);
        pli.setEvr("pkg1");
        pli.setVersion("2.6.9");
        pli.setEpoch(null);
        pli.setArch(null);
        pli.setPackageType("rpm");
        a.add(pli);

        pli = new PackageListItem();
        pli.setIdCombo("390|1628");
        pli.setEvrId(1628L);
        pli.setName("kernel");
        pli.setRelease("5.EL");
        pli.setNameId(390L);
        pli.setEvr("pkg2");
        pli.setVersion("2.6.9");
        pli.setEpoch(null);
        pli.setArch(null);
        pli.setPackageType("rpm");
        a.add(pli);

        pli = new PackageListItem();
        pli.setIdCombo("1620|2069");
        pli.setEvrId(2069L);
        pli.setName("kernel-devel");
        pli.setRelease("5.0.3.EL");
        pli.setNameId(1620L);
        pli.setEvr("pkg3");
        pli.setVersion("2.6.9");
        pli.setEpoch(null);
        pli.setArch(null);
        pli.setPackageType("rpm");
        a.add(pli);

        pli = new PackageListItem();
        pli.setIdCombo("1620|1628");
        pli.setEvrId(1628L);
        pli.setName("kernel-devel");
        pli.setRelease("5.EL");
        pli.setNameId(1620L);
        pli.setEvr("pkg4");
        pli.setVersion("2.6.9");
        pli.setEpoch(null);
        pli.setArch(null);
        pli.setPackageType("rpm");
        a.add(pli);

        pli = new PackageListItem();
        pli.setIdCombo("398|1629");
        pli.setEvrId(1629L);
        pli.setName("kernel-utils");
        pli.setRelease("13.1.48");
        pli.setNameId(398L);
        pli.setEvr("pkg5");
        pli.setVersion("2.4");
        pli.setEpoch("1");
        pli.setArch(null);
        pli.setPackageType("rpm");
        a.add(pli);

        // SETUP B
        List<PackageListItem> b = new ArrayList<>();
        pli = new PackageListItem();
        pli.setIdCombo(null);
        pli.setEvrId(1628L);
        pli.setName("kernel");
        pli.setRelease("5.EL");
        pli.setNameId(390L);
        pli.setEvr("pkg1b");
        pli.setVersion("2.6.9");
        pli.setEpoch(null);
        pli.setArch(null);
        pli.setPackageType("rpm");
        b.add(pli);

        pli = new PackageListItem();
        pli.setIdCombo(null);
        pli.setEvrId(1628L);
        pli.setName("kernel-devel");
        pli.setRelease("5.EL");
        pli.setNameId(1620L);
        pli.setEvr("pkg2b");
        pli.setVersion("2.6.9");
        pli.setEpoch(null);
        pli.setArch(null);
        pli.setPackageType("rpm");
        b.add(pli);

        pli = new PackageListItem();
        pli.setIdCombo(null);
        pli.setEvrId(1629L);
        pli.setName("kernel-utils");
        pli.setRelease("13.1.48");
        pli.setNameId(398L);
        pli.setEvr("pkg3b");
        pli.setVersion("2.4");
        pli.setEpoch("1");
        pli.setArch(null);
        pli.setPackageType("rpm");
        b.add(pli);

        List<PackageMetadata> diff = ProfileManager.comparePackageLists(new DataResult<>(a),
                new DataResult<>(b), "foo");
        // This used to assert: assertEquals(0, diff.size());
        // but we now support showing what older packages exist on a system
        assertEquals(2, diff.size());

    }

    @Test
    public void testBz204345() {
        // kernel-2.6.9-22.EL
        // kernel-2.6.9-42.0.2.EL

        List<PackageListItem> serverList = new ArrayList<>();
        PackageListItem pli3 = new PackageListItem();
        pli3.setIdCombo("500000341|000000");
        pli3.setEvrId(000000L);
        pli3.setName("kernel");
        pli3.setRelease("42.0.2.EL");
        pli3.setNameId(500000341L);
        pli3.setEvr("kernel-2.6.9-42.0.2.EL");
        pli3.setVersion("2.6.9");
        pli3.setEpoch(null);
        pli3.setPackageType("rpm");
        serverList.add(pli3);

        List<PackageListItem> otherServerList = new ArrayList<>();
        PackageListItem pli = new PackageListItem();
        pli.setIdCombo("500000341|000001");
        pli.setEvrId(000000L);
        pli.setName("kernel");
        pli.setRelease("22.EL");
        pli.setNameId(500000341L);
        pli.setEvr("kernel-2.6.9-22.EL");
        pli.setVersion("2.6.9");
        pli.setEpoch(null);
        pli3.setPackageType("rpm");
        otherServerList.add(pli);

        PackageListItem pli2 = new PackageListItem();
        pli2.setIdCombo("500000341|000000");
        pli2.setEvrId(000000L);
        pli2.setName("kernel");
        pli2.setRelease("42.0.2.EL");
        pli2.setNameId(500000341L);
        pli2.setEvr("kernel-2.6.9-42.0.2.EL");
        pli2.setVersion("2.6.9");
        pli2.setEpoch(null);
        pli3.setPackageType("rpm");
        otherServerList.add(pli2);


        List<PackageMetadata> diff = ProfileManager.comparePackageLists(
                new DataResult<>(otherServerList),
                new DataResult<>(serverList), "foo");
        assertEquals(1, diff.size());

        PackageMetadata pm = (PackageMetadata) diff.get(0);
        assertNotNull(pm);
        assertEquals("kernel-2.6.9-22.EL", pm.getOther().getEvr());
        assertEquals(PackageMetadata.KEY_OTHER_ONLY, pm.getComparisonAsInt());
        // assertEquals("kernel-2.4.21-27.EL", pm.getSystem().getEvr());
    }

    @Test
    public void testGetChildChannelsNeededForProfile() throws Exception {
        Server server = ServerTestUtils.createTestSystem(user);
        Channel childChannel1 = ChannelTestUtils.createChildChannel(server.getCreator(),
                server.getBaseChannel());

        PackageManagerTest.addPackageToSystemAndChannel("child1-package1", server,
                childChannel1);
        PackageManagerTest.addPackageToSystemAndChannel("child1-package2", server,
                childChannel1);

        Channel childChannel2 = ChannelTestUtils.createChildChannel(server.getCreator(),
                server.getBaseChannel());
        PackageManagerTest.addPackageToSystemAndChannel("child2-package1", server,
                childChannel2);
        PackageManagerTest.addPackageToSystemAndChannel("child2-package2", server,
                childChannel2);

        Profile p = ProfileManager.createProfile(server.getCreator(), server,
                "Profile test name" + TestUtils.randomString(), "test desc");
        ProfileManager.copyFrom(server, p);

        List<Channel> channels = ProfileManager.getChildChannelsNeededForProfile(
                server.getCreator(),
                server.getBaseChannel(), p);

        assertEquals(2, channels.size());
        assertTrue(channels.contains(childChannel1));
        assertTrue(channels.contains(childChannel2));

        Profile p2 = ProfileManager.createProfile(server.getCreator(), server,
                "Profile test name" + TestUtils.randomString(), "test desc");

        channels = ProfileManager.getChildChannelsNeededForProfile(server.getCreator(),
                server.getBaseChannel(), p2);
        assertEquals(0, channels.size());


    }
}
