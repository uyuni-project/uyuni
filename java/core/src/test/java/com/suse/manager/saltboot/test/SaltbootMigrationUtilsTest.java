/*
 * Copyright (c) 2025 SUSE LLC
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

package com.suse.manager.saltboot.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.redhat.rhn.domain.image.ImageInfo;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;

import com.suse.manager.saltboot.SaltbootMigrationUtils;
import com.suse.manager.saltboot.SaltbootUtils;

import org.cobbler.CobblerConnection;
import org.cobbler.Distro;
import org.cobbler.Profile;
import org.cobbler.SystemRecord;
import org.cobbler.test.MockConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Test for {@link SaltbootMigrationUtils}.
 */
public class SaltbootMigrationUtilsTest extends JMockBaseTestCaseWithUser {

    private CobblerConnection client;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        MockConnection.clear();
        client = new MockConnection("http://localhost", "token");
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        MockConnection.clear();
        super.tearDown();
    }

    @Test
    public void testMigrateSaltbootWhenThereIsNothing() {
        List<Distro> distros = Distro.list(client);
        List<Profile> profiles = Profile.list(client);
        List<SystemRecord> systems = SystemRecord.list(client);

        assertEquals(0, distros.size());
        assertEquals(0, profiles.size());
        assertEquals(0, systems.size());

        SaltbootMigrationUtils.migrateSaltboot();

        distros = Distro.list(client);
        profiles = Profile.list(client);
        systems = SystemRecord.list(client);

        assertEquals(0, distros.size());
        assertEquals(0, profiles.size());
        assertEquals(0, systems.size());
    }

    @Test
    public void testMigrateSaltbootDistro() throws Exception {
        String orgName = user.getOrg().getName();
        Long orgId = user.getOrg().getId();

        // create ImageInfo objects for the distro
        String image = "POS_Image_JeOS7";
        String imageVersion = "7.1.0";
        int imageRevision = 3;
        String label = String.format("%s-%s-%s", image, imageVersion, imageRevision);
        SaltbootUtilsTest.createImageHelper(user, image, imageVersion, imageRevision);

        // create old entries
        String oldDistroName = String.format("%s-%s", orgId, label);
        Distro oldDistro = new Distro.Builder<String>()
                .setName(oldDistroName)
                .setKernel("kernel")
                .setInitrd("initrd")
                .setArch("architecture")
                .build(client);
        Profile.create(client, oldDistroName, oldDistro);

        SaltbootMigrationUtils.migrateSaltboot(client);

        // check that old entries were removed and new ones created
        String newDistroName = String.format("%s-%s-%s:S:%s:%s", image, imageVersion, imageRevision, orgId, orgName);
        assertEquals(newDistroName, SaltbootUtils.makeCobblerName(user.getOrg(), label));
        assertNull(Distro.lookupByName(client, oldDistroName));
        assertNotNull(Distro.lookupByName(client, newDistroName));
        assertNull(Profile.lookupByName(client, oldDistroName));
        assertNotNull(Profile.lookupByName(client, newDistroName));
        assertNotNull(Profile.lookupByName(client, SaltbootUtils.makeCobblerName(
                user.getOrg(), SaltbootUtils.DEFAULT_BOOT_IMAGE)));
    }

    @Test
    public void testMigrateSaltboot() throws Exception {
        String orgName = user.getOrg().getName();
        Long orgId = user.getOrg().getId();
        assertEquals(0, Distro.list(client).size());

        // create ImageInfo objects for the distro
        SaltbootUtilsTest.createImageHelper(user, "POS_Image_JeOS7", "7.1.0", 3);

        // create old entries
        String oldDistroName = orgId + "-POS_Image_JeOS7-7.1.0-3";
        Distro oldDistro = new Distro.Builder<String>()
                .setName(oldDistroName)
                .setKernel("kernel")
                .setInitrd("initrd")
                .setArch("architecture")
                .build(client);
        Profile oldDistroProfile = Profile.create(client, oldDistroName, oldDistro);

        // Old branch profiles
        String groupName = "my-old-saltboot-group";
        SaltbootUtilsTest.createSaltbootGroupHelper(user, groupName);
        String oldProfileName = orgId + "-" + groupName;
        Profile.create(client, oldProfileName, oldDistro);

        // Old system record
        SystemRecord.create(client, orgId + "-my-system", oldDistroProfile);

        SaltbootMigrationUtils.migrateSaltboot(client);

        // check that old entries were removed and new ones created
        String newDistroName = String.format("POS_Image_JeOS7-7.1.0-3:S:%s:%s", orgId, orgName);
        String newProfileName = String.format("%s:S:%s:%s", groupName, orgId, orgName);
        // System is correctly moved over to new distro profile
        SystemRecord system = SystemRecord.lookupByName(client, orgId + "-my-system");
        assertNotNull(system);
        assertEquals(newDistroName, system.getProfile().getName());
        // Old distro is removed, new distro is present
        assertNull(Distro.lookupByName(client, oldDistroName));
        assertNotNull(Distro.lookupByName(client, newDistroName));
        // Old distro profile is removed, new distro profile is present
        assertNull(Profile.lookupByName(client, oldDistroName));
        assertNotNull(Profile.lookupByName(client, newDistroName));
        // Old branch profile is removed, new branch profile is present
        assertNull(Profile.lookupByName(client, oldProfileName));
        assertNotNull(Profile.lookupByName(client, newProfileName));
        // check default boot image was also created
        assertNotNull(Profile.lookupByName(client, SaltbootUtils.makeCobblerName(
                user.getOrg(), SaltbootUtils.DEFAULT_BOOT_IMAGE)));
    }

    @Test
    public void testMigrationSaltbootGroup() throws Exception {
        Long orgId = user.getOrg().getId();
        assertEquals(0, Distro.list(client).size());

        // create ImageInfo objects for the distro
        ImageInfo image = SaltbootUtilsTest.createImageHelper(user, "POS_Image_JeOS7", "7.1.0", 3);

        // create old entries
        String oldDistroName = orgId + "-POS_Image_JeOS7-7.1.0-3";
        Distro oldDistro = new Distro.Builder<String>()
                .setName(oldDistroName)
                .setKernel("kernel")
                .setInitrd("initrd")
                .setArch("architecture")
                .build(client);
        Profile.create(client, oldDistroName, oldDistro);

        // Old branch profiles
        String groupName = "my-old-saltboot-group";
        SaltbootUtilsTest.createSaltbootGroupHelper(user, groupName);
        String oldProfileName = orgId + "-" + groupName;
        Profile.create(client, oldProfileName, oldDistro);

        SaltbootMigrationUtils.migrateSaltboot(client);

        // check that old entries were removed and new ones created
        String newDistroName = SaltbootUtils.makeCobblerNameVR(image);
        String newProfileName = SaltbootUtils.makeCobblerName(user.getOrg(), groupName);

        // Old distro is removed, new distro is present
        assertNull(Distro.lookupByName(client, oldDistroName));
        assertNotNull(Distro.lookupByName(client, newDistroName));
        // Old distro profile is removed, new distro profile is present
        assertNull(Profile.lookupByName(client, oldDistroName));
        assertNotNull(Profile.lookupByName(client, newDistroName));
        // Old branch profile is removed, new branch profile is present
        assertNull(Profile.lookupByName(client, oldProfileName));
        assertNotNull(Profile.lookupByName(client, newProfileName));
        // Check default boot image was also created
        String defaultImage = SaltbootUtils.makeCobblerName(user.getOrg(), SaltbootUtils.DEFAULT_BOOT_IMAGE);
        assertNotNull(Profile.lookupByName(client, defaultImage));
        // New branch has default boot image assigned
        assertEquals(defaultImage, Profile.lookupByName(client, newProfileName).getParent());
    }

    @Test
    public void testMigrationIdempotence() throws Exception {
        // Database entries
        ImageInfo image = SaltbootUtilsTest.createImageHelper(user, "POS_Image_JeOS7", "7.1.0", 3);
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        minion.setMinionId("my-saltboot-system");
        ServerGroup group = SaltbootUtilsTest.createSaltbootGroupHelper(user, "my-saltboot-group");

        // Saltboot entries
        SaltbootUtils.createSaltbootDistro(image, List.of(), client);
        SaltbootUtils.createSaltbootProfile(group, image.getName(), false, client);
        SaltbootUtils.createSaltbootSystem(minion, "POS_Image_JeOS7-7.1.0-3", group.getName(), List.of(), "", client);

        // Expected names
        String orgName = user.getOrg().getName();
        Long orgId = user.getOrg().getId();
        String distroName = String.format("POS_Image_JeOS7-7.1.0-3:S:%s:%s", orgId, orgName);
        String distroProfileNameVR = String.format("POS_Image_JeOS7-7.1.0-3:S:%s:%s", orgId, orgName);
        String distroProfileNameV = String.format("POS_Image_JeOS7-7.1.0:S:%s:%s", orgId, orgName);
        String distroProfileName = String.format("POS_Image_JeOS7:S:%s:%s", orgId, orgName);
        String profileName = String.format("%s:S:%s:%s", group.getName(), orgId, orgName);
        String systemName = String.format("%s:S:%s:%s", minion.getMinionId(), orgId, orgName);

        // System is correctly set to the distro profile with version and revision
        SystemRecord system = SystemRecord.lookupByName(client, systemName);
        assertNotNull(system);
        assertEquals(distroProfileNameVR, system.getProfile().getName());
        // Distro
        assertNotNull(Distro.lookupByName(client, distroName));
        // All distro profiles are present
        assertNotNull(Profile.lookupByName(client, distroProfileNameVR));
        assertNotNull(Profile.lookupByName(client, distroProfileNameV));
        assertNotNull(Profile.lookupByName(client, distroProfileName));
        // Branch profile
        assertNotNull(Profile.lookupByName(client, profileName));
        // Check default boot image was also created
        assertNotNull(Profile.lookupByName(client, SaltbootUtils.makeCobblerName(
                user.getOrg(), SaltbootUtils.DEFAULT_BOOT_IMAGE)));

        SaltbootMigrationUtils.migrateSaltboot(client);

        // System is correctly set to the distro profile with version and revision
        system = SystemRecord.lookupByName(client, systemName);
        assertNotNull(system);
        assertEquals(distroProfileNameVR, system.getProfile().getName());
        // Distro
        assertNotNull(Distro.lookupByName(client, distroName));
        // All distro profiles are present
        assertNotNull(Profile.lookupByName(client, distroProfileNameVR));
        assertNotNull(Profile.lookupByName(client, distroProfileNameV));
        assertNotNull(Profile.lookupByName(client, distroProfileName));
        // Branch profile
        assertNotNull(Profile.lookupByName(client, profileName));
        // check default boot image was also created
        assertNotNull(Profile.lookupByName(client, SaltbootUtils.makeCobblerName(
                user.getOrg(), SaltbootUtils.DEFAULT_BOOT_IMAGE)));
    }

    @Test
    public void testMigrationOfPartiallyMigratedData() throws Exception {
        Long orgId = user.getOrg().getId();

        // An image that needs migration
        ImageInfo imageToMigrate = SaltbootUtilsTest.createImageHelper(user, "Image-To-Migrate", "1.0.0", 1);
        String oldDistroName = String.format("%d-%s-%s-%d", orgId, "Image-To-Migrate", "1.0.0", 1);
        Distro oldDistro = new Distro.Builder<String>()
                .setName(oldDistroName)
                .setKernel("kernel")
                .setInitrd("initrd")
                .setArch("architecture")
                .build(client);
        Profile.create(client, oldDistroName, oldDistro);

        // An image that is already migrated
        ImageInfo imageMigrated = SaltbootUtilsTest.createImageHelper(user, "Image-Migrated", "2.0.0", 2);
        SaltbootUtils.createSaltbootDistro(imageMigrated, List.of(), client);

        // A group that needs migration
        ServerGroup groupToMigrate = SaltbootUtilsTest.createSaltbootGroupHelper(user, "group-to-migrate");
        String oldProfileName = orgId + "-" + groupToMigrate.getName();
        Profile.create(client, oldProfileName, oldDistro);

        // A group that is already migrated
        ServerGroup groupMigrated = SaltbootUtilsTest.createSaltbootGroupHelper(user, "group-migrated");
        SaltbootUtils.createSaltbootProfile(groupMigrated, imageMigrated.getName(), false, client);

        // Run migration
        SaltbootMigrationUtils.migrateSaltboot(client);

        // Verify imageToMigrate was migrated
        String newDistroNameToMigrate = SaltbootUtils.makeCobblerNameVR(imageToMigrate);
        assertNull(Distro.lookupByName(client, oldDistroName), "Old distro should be removed");
        assertNotNull(Distro.lookupByName(client, newDistroNameToMigrate), "New distro should be created");
        assertNull(Profile.lookupByName(client, oldDistroName), "Old distro profile should be removed");
        assertNotNull(Profile.lookupByName(client, newDistroNameToMigrate), "New distro profile should be created");

        // Verify imageMigrated is untouched
        String newDistroNameMigrated = SaltbootUtils.makeCobblerNameVR(imageMigrated);
        assertNotNull(Distro.lookupByName(client, newDistroNameMigrated), "Already migrated distro should exist");
        assertNotNull(Profile.lookupByName(client, newDistroNameMigrated),
                "Already migrated distro profile should exist");

        // Verify groupToMigrate was migrated
        String newProfileNameToMigrate = SaltbootUtils.makeCobblerName(groupToMigrate.getOrg(),
                groupToMigrate.getName());
        assertNull(Profile.lookupByName(client, oldProfileName), "Old group profile should be removed");
        assertNotNull(Profile.lookupByName(client, newProfileNameToMigrate), "New group profile should be created");

        // Verify groupMigrated is untouched
        String newProfileNameMigrated = SaltbootUtils.makeCobblerName(groupMigrated.getOrg(),
                groupMigrated.getName());
        assertNotNull(Profile.lookupByName(client, newProfileNameMigrated),
                "Already migrated group profile should exist");
    }

    @Test
    public void testMigrateMultipleDistrosAndSystems() throws Exception {
        Long orgId = user.getOrg().getId();

        ImageInfo image1 = SaltbootUtilsTest.createImageHelper(user, "Image-One", "1.1.0", 1);
        String oldDistroName1 = String.format("%d-%s-%s-%d", orgId, "Image-One", "1.1.0", 1);
        Distro oldDistro1 = new Distro.Builder<String>()
                .setName(oldDistroName1)
                .setKernel("kernel1")
                .setInitrd("initrd1")
                .setArch("x86_64")
                .build(client);
        Profile oldProfile1 = Profile.create(client, oldDistroName1, oldDistro1);
        String systemName1 = orgId + "-system-one";
        SystemRecord.create(client, systemName1, oldProfile1);

        ImageInfo image2 = SaltbootUtilsTest.createImageHelper(user, "Image-Two", "2.2.0", 2);
        String oldDistroName2 = String.format("%d-%s-%s-%d", orgId, "Image-Two", "2.2.0", 2);
        Distro oldDistro2 = new Distro.Builder<String>()
                .setName(oldDistroName2)
                .setKernel("kernel2")
                .setInitrd("initrd2")
                .setArch("x86_64")
                .build(client);
        Profile oldProfile2 = Profile.create(client, oldDistroName2, oldDistro2);
        String systemName2 = orgId + "-system-two";
        String systemName3 = orgId + "-system-three";
        SystemRecord.create(client, systemName2, oldProfile2);
        SystemRecord.create(client, systemName3, oldProfile2);

        SaltbootMigrationUtils.migrateSaltboot(client);

        String newDistroName1 = SaltbootUtils.makeCobblerNameVR(image1);
        String newDistroName2 = SaltbootUtils.makeCobblerNameVR(image2);

        // Old distros and profiles are removed
        assertNull(Distro.lookupByName(client, oldDistroName1));
        assertNull(Profile.lookupByName(client, oldDistroName1));
        assertNull(Distro.lookupByName(client, oldDistroName2));
        assertNull(Profile.lookupByName(client, oldDistroName2));

        // Verify systems are reassigned to the correct new profiles
        assertEquals(newDistroName1, SystemRecord.lookupByName(client, systemName1).getProfile().getName());
        assertEquals(newDistroName2, SystemRecord.lookupByName(client, systemName2).getProfile().getName());
        assertEquals(newDistroName2, SystemRecord.lookupByName(client, systemName3).getProfile().getName());
    }
}
