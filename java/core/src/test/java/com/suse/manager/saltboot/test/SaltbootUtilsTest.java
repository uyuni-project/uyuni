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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.formula.FormulaFactory;
import com.redhat.rhn.domain.image.ImageInfo;
import com.redhat.rhn.domain.image.ImageProfile;
import com.redhat.rhn.domain.image.ImageProfileFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.Pillar;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.testing.ImageTestUtils;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.ServerGroupTestUtils;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.saltboot.SaltbootException;
import com.suse.manager.saltboot.SaltbootUtils;

import org.cobbler.CobblerConnection;
import org.cobbler.Distro;
import org.cobbler.Network;
import org.cobbler.Profile;
import org.cobbler.SystemRecord;
import org.cobbler.test.MockConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test for {@link SaltbootUtils}.
 */
public class SaltbootUtilsTest extends JMockBaseTestCaseWithUser {

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

    static ImageInfo createImageHelper(User user, String label, String version, int revision) throws Exception {
        MinionServer server = MinionServerFactory.findByMinionId("minion.local").orElseGet(
                () -> {
                    MinionServer s = MinionServerFactoryTest.createTestMinionServer(user);
                    s.setMinionId("minion.local");
                    s.setServerArch(ServerFactory.lookupServerArchByLabel("x86_64-redhat-linux"));
                    ServerFactory.save(s);
                    return s;
                }
        );
        ActivationKey key = ImageTestUtils.createActivationKey(user);
        ImageProfile profile = ImageProfileFactory.lookupByLabelAndOrg(label, user.getOrg()).orElseGet(
                () -> ImageTestUtils.createKiwiImageProfile(label, key, user)
        );
        ImageInfo image = ImageTestUtils.createImageInfo(profile, server, version, user);
        image.setRevisionNumber(revision);
        image.setImageType(ImageProfile.TYPE_KIWI);
        image.setBuilt(true);
        ImageTestUtils.createImageFile(image, "kernel", "kernel");
        ImageTestUtils.createImageFile(image, "initrd", "initrd");
        ImageTestUtils.createImageFile(image, "image", "image");
        return image;
    }

    static ServerGroup createSaltbootGroupHelper(User user, String label) {
        return createSaltbootGroupHelper(user, label, null, null);
    }

    static ServerGroup createSaltbootGroupHelper(User user, String label, String image, String version) {
        ServerGroup group = ServerGroupTestUtils.createManaged(user);
        group.setName(label);

        Map<String, Object> saltboot = new HashMap<>();
        saltboot.put("download_server", "mybranch.example.com");
        saltboot.put("disable_id_prefix", true);
        saltboot.put("disable_unique_suffix", false);
        saltboot.put("minion_id_naming", "Hostname");
        saltboot.put("default_kernel_parameters", "");
        if (image != null && !image.isEmpty()) {
            saltboot.put("default_boot_image", image);
            if (version != null && !version.isEmpty()) {
                saltboot.put("default_boot_image_version", version);
            }
        }

        Map<String, Object> pillar = new HashMap<>();
        pillar.put("saltboot", saltboot);

        group.getPillars().add(new Pillar(FormulaFactory.SALTBOOT_PILLAR, pillar, group));
        TestUtils.saveAndFlush(group);
        return group;
    }

    @Test
    public void testCobblerName() {
        String orgName = user.getOrg().getName();
        Long orgId = user.getOrg().getId();

        // Test with a simple label
        String label = "my-test-label";
        String expectedName = String.format("%s:S:%d:%s", label, orgId, orgName);
        assertEquals(expectedName, SaltbootUtils.makeCobblerName(user.getOrg(), label));

        // Test with a label containing spaces
        String labelWithSpaces = "my test label";
        String expectedNameWithSpaces = String.format("my_test_label:S:%d:%s", orgId, orgName);
        assertEquals(expectedNameWithSpaces, SaltbootUtils.makeCobblerName(user.getOrg(), labelWithSpaces));

        // Test with a label containing spaces and special chars
        String labelWithSpacesAndChars = "my test %label%2";
        String expectedNameWithSpacesAndChars = String.format("my_test_label2:S:%d:%s", orgId, orgName);
        assertEquals(expectedNameWithSpacesAndChars, SaltbootUtils.makeCobblerName(
                user.getOrg(), labelWithSpacesAndChars));
    }

    @Test
    public void testMakeCobblerNameVR() throws Exception {
        String imageName = "my-image";
        String imageVersion = "1.2.3";
        int imageRevision = 4;
        ImageInfo imageInfo = createImageHelper(user, imageName, imageVersion, imageRevision);

        String orgName = user.getOrg().getName();
        Long orgId = user.getOrg().getId();

        String expectedName = String.format("%s-%s-%d:S:%d:%s",
                imageName, imageVersion, imageRevision, orgId, orgName);
        assertEquals(expectedName, SaltbootUtils.makeCobblerNameVR(imageInfo));

        // Test with spaces in name
        imageInfo.setName("my image");
        String expectedNameWithSpaces = String.format("my_image-%s-%d:S:%d:%s",
                imageVersion, imageRevision, orgId, orgName);
        assertEquals(expectedNameWithSpaces, SaltbootUtils.makeCobblerNameVR(imageInfo));
    }

    @Test
    public void testCreateSaltbootDistro() throws Exception {
        ImageInfo imageInfo = createImageHelper(user, "my-image", "1.2.3", 4);
        SaltbootUtils.createSaltbootDistro(imageInfo, Distro.list(client), client);

        String nameVR = SaltbootUtils.makeCobblerNameVR(imageInfo);
        String nameV = SaltbootUtils.makeCobblerName(user.getOrg(), "my-image-1.2.3");
        String name = SaltbootUtils.makeCobblerName(user.getOrg(), "my-image");
        String defaultName = SaltbootUtils.makeCobblerName(user.getOrg(), SaltbootUtils.DEFAULT_BOOT_IMAGE);

        // Check that the distro was created
        Distro distro = Distro.lookupByName(client, nameVR);
        assertNotNull(distro);
        assertEquals(nameVR, distro.getName());

        // Check that all profiles were created and point to the new distro
        Profile profileVR = Profile.lookupByName(client, nameVR);
        assertNotNull(profileVR);
        assertEquals(distro.getName(), profileVR.getDistro().getName());

        Profile profileV = Profile.lookupByName(client, nameV);
        assertNotNull(profileV);
        assertEquals(distro.getName(), profileV.getDistro().getName());

        Profile profile = Profile.lookupByName(client, name);
        assertNotNull(profile);
        assertEquals(distro.getName(), profile.getDistro().getName());

        Profile defaultProfile = Profile.lookupByName(client, defaultName);
        assertNotNull(defaultProfile);
        assertEquals(distro.getName(), defaultProfile.getDistro().getName());
    }

    @Test
    public void testCreateSaltbootDistroIdempotent() throws Exception {
        ImageInfo imageInfo = createImageHelper(user, "my-image", "1.2.3", 4);

        // First call
        SaltbootUtils.createSaltbootDistro(imageInfo, Distro.list(client), client);

        // Check that the distro was created
        String nameVR = SaltbootUtils.makeCobblerNameVR(imageInfo);
        Distro distro = Distro.lookupByName(client, nameVR);
        assertNotNull(distro);
        int distroCount = Distro.list(client).size();
        int profileCount = Profile.list(client).size();

        // Second call should be a no-op
        SaltbootUtils.createSaltbootDistro(imageInfo, Distro.list(client), client);

        assertEquals(distroCount, Distro.list(client).size());
        assertEquals(profileCount, Profile.list(client).size());
    }

    @Test
    public void testDeleteSaltbootDistro() throws Exception {
        // Create two versions of an image
        ImageInfo imageV1 = createImageHelper(user, "my-image", "1.0.0", 1);
        ImageInfo imageV2 = createImageHelper(user, "my-image", "1.0.0", 2);

        SaltbootUtils.createSaltbootDistro(imageV1, Distro.list(client), client);
        SaltbootUtils.createSaltbootDistro(imageV2, Distro.list(client), client);

        String nameV1VR = SaltbootUtils.makeCobblerNameVR(imageV1);
        String nameV2VR = SaltbootUtils.makeCobblerNameVR(imageV2);
        String nameV = SaltbootUtils.makeCobblerName(user.getOrg(), "my-image-1.0.0");
        String name = SaltbootUtils.makeCobblerName(user.getOrg(), "my-image");
        String defaultName = SaltbootUtils.makeCobblerName(user.getOrg(), SaltbootUtils.DEFAULT_BOOT_IMAGE);

        // Profiles should point to the newest version (v2)
        assertEquals(nameV2VR, Profile.lookupByName(client, nameV).getDistro().getName());
        assertEquals(nameV2VR, Profile.lookupByName(client, name).getDistro().getName());
        assertEquals(nameV2VR, Profile.lookupByName(client, defaultName).getDistro().getName());

        // Delete the newer version
        SaltbootUtils.deleteSaltbootDistro(imageV2, client);

        // Check that distro and its private profile are gone
        assertNull(Distro.lookupByName(client, nameV2VR));
        assertNull(Profile.lookupByName(client, nameV2VR));

        // Check that other profiles now point to the older version (v1)
        assertEquals(nameV1VR, Profile.lookupByName(client, nameV).getDistro().getName());
        assertEquals(nameV1VR, Profile.lookupByName(client, name).getDistro().getName());
        assertEquals(nameV1VR, Profile.lookupByName(client, defaultName).getDistro().getName());

        // Delete the last version
        SaltbootUtils.deleteSaltbootDistro(imageV1, client);

        // Check that distro and its private profile are gone
        assertNull(Distro.lookupByName(client, nameV1VR));
        assertNull(Profile.lookupByName(client, nameV1VR));

        // The other profiles should still exist, but point to a now-deleted distro.
        // The delete logic doesn't clean them up if there's no replacement.
        assertNotNull(Profile.lookupByName(client, nameV));
        assertNotNull(Profile.lookupByName(client, name));
        assertNotNull(Profile.lookupByName(client, defaultName));
    }

    @Test
    public void testCreateSaltbootProfile() throws Exception {
        // Create a server group and an image
        ServerGroup group = createSaltbootGroupHelper(user, "my-saltboot-group");
        ImageInfo image = createImageHelper(user, "my-image", "1.0.0", 1);
        SaltbootUtils.createSaltbootDistro(image, Distro.list(client), client);

        // Call the method to be tested
        String imageProfileName = SaltbootUtils.makeCobblerName(user.getOrg(), "my-image-1.0.0");
        SaltbootUtils.createSaltbootProfile(group, imageProfileName, false, client);

        // Verify the profile was created correctly
        String groupProfileName = SaltbootUtils.makeCobblerName(user.getOrg(), "my-saltboot-group");
        Profile groupProfile = Profile.lookupByName(client, groupProfileName);
        assertNotNull(groupProfile);

        // Verify parent and kernel options
        assertEquals(imageProfileName, groupProfile.getParent());
        Map<String, Object> kernelOptions = groupProfile.getKernelOptions().get();
        assertEquals("my-saltboot-group", kernelOptions.get("MINION_ID_PREFIX"));
        assertEquals("mybranch.example.com", kernelOptions.get("MASTER"));
        assertEquals("1", kernelOptions.get("DISABLE_ID_PREFIX"));
    }

    @Test
    public void testCreateSaltbootProfileWithDefault() throws Exception {
        // Create a server group and an image
        ImageInfo image = createImageHelper(user, "my-image", "1.0.0", 1);
        SaltbootUtils.createSaltbootDistro(image, Distro.list(client), client);

        // Create Saltboot Profile for group
        ServerGroup group = createSaltbootGroupHelper(user, "my-saltboot-group-2");
        SaltbootUtils.createSaltbootProfile(group, client);

        // Verify the profile was created correctly
        String groupProfileName = SaltbootUtils.makeCobblerName(user.getOrg(), "my-saltboot-group-2");
        Profile groupProfile = Profile.lookupByName(client, groupProfileName);
        assertNotNull(groupProfile);

        // Verify parent and kernel options
        String imageProfileName = SaltbootUtils.makeCobblerName(user.getOrg(), SaltbootUtils.DEFAULT_BOOT_IMAGE);
        assertEquals(imageProfileName, groupProfile.getParent());
        Map<String, Object> kernelOptions = groupProfile.getKernelOptions().get();
        assertEquals("my-saltboot-group-2", kernelOptions.get("MINION_ID_PREFIX"));
        assertEquals("mybranch.example.com", kernelOptions.get("MASTER"));
        assertEquals("1", kernelOptions.get("DISABLE_ID_PREFIX"));
    }

    @Test
    public void testCreateSaltbootProfileWithImage() throws Exception {
        // Create a server group and an image
        ImageInfo image = createImageHelper(user, "my-branch-image", "1.0.0", 1);
        SaltbootUtils.createSaltbootDistro(image, Distro.list(client), client);

        // Create Saltboot Profile for group
        ServerGroup group = createSaltbootGroupHelper(user, "my-saltboot-group-3", "my-branch-image", null);
        SaltbootUtils.createSaltbootProfile(group, client);

        // Verify the profile was created correctly
        String groupProfileName = SaltbootUtils.makeCobblerName(user.getOrg(), "my-saltboot-group-3");
        Profile groupProfile = Profile.lookupByName(client, groupProfileName);
        assertNotNull(groupProfile);

        // Verify parent and kernel options
        String imageProfileName = SaltbootUtils.makeCobblerName(user.getOrg(), "my-branch-image");
        assertEquals(imageProfileName, groupProfile.getParent());
        Map<String, Object> kernelOptions = groupProfile.getKernelOptions().get();
        assertEquals("my-saltboot-group-3", kernelOptions.get("MINION_ID_PREFIX"));
        assertEquals("mybranch.example.com", kernelOptions.get("MASTER"));
        assertEquals("1", kernelOptions.get("DISABLE_ID_PREFIX"));
    }

    @Test
    public void testCreateSaltbootProfileWithImageVersion() throws Exception {
        // Create a server group and an image
        ImageInfo image = createImageHelper(user, "my-branch-image", "1.0.0", 1);
        SaltbootUtils.createSaltbootDistro(image, Distro.list(client), client);

        // Create Saltboot Profile for group
        ServerGroup group = createSaltbootGroupHelper(user, "my-saltboot-group-4", "my-branch-image", "1.0.0");
        SaltbootUtils.createSaltbootProfile(group, client);

        // Verify the profile was created correctly
        String groupProfileName = SaltbootUtils.makeCobblerName(user.getOrg(), "my-saltboot-group-4");
        Profile groupProfile = Profile.lookupByName(client, groupProfileName);
        assertNotNull(groupProfile);

        // Verify parent and kernel options
        String imageProfileName = SaltbootUtils.makeCobblerName(user.getOrg(), "my-branch-image-1.0.0");
        assertEquals(imageProfileName, groupProfile.getParent());
        Map<String, Object> kernelOptions = groupProfile.getKernelOptions().get();
        assertEquals("my-saltboot-group-4", kernelOptions.get("MINION_ID_PREFIX"));
        assertEquals("mybranch.example.com", kernelOptions.get("MASTER"));
        assertEquals("1", kernelOptions.get("DISABLE_ID_PREFIX"));
    }

    @Test
    public void testDeleteSaltbootProfile() throws Exception {
        // 1. Test successful deletion
        ServerGroup group = createSaltbootGroupHelper(user, "my-group-to-delete");
        ImageInfo image = createImageHelper(user, "my-image", "1.0.0", 1);
        SaltbootUtils.createSaltbootDistro(image, Distro.list(client), client);
        String imageProfileName = SaltbootUtils.makeCobblerName(user.getOrg(), "my-image-1.0.0");
        SaltbootUtils.createSaltbootProfile(group, imageProfileName, false, client);

        String groupProfileName = SaltbootUtils.makeCobblerName(user.getOrg(), "my-group-to-delete");
        assertNotNull(Profile.lookupByName(client, groupProfileName));

        SaltbootUtils.deleteSaltbootProfile(groupProfileName, client);
        assertNull(Profile.lookupByName(client, groupProfileName));

        // 2. Test deletion of non-existent profile (should not throw)
        SaltbootUtils.deleteSaltbootProfile("non-existent-profile", client);

        // 3. Test deletion of a profile with associated systems (should throw)
        ServerGroup groupWithSystems = createSaltbootGroupHelper(user, "my-group-with-systems");
        SaltbootUtils.createSaltbootProfile(groupWithSystems, imageProfileName, false, client);
        String groupWithSystemsProfileName = SaltbootUtils.makeCobblerName(user.getOrg(), "my-group-with-systems");
        Profile profileWithSystems = Profile.lookupByName(client, groupWithSystemsProfileName);

        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        minion.setMinionId("test-minion");
        String systemName = SaltbootUtils.makeCobblerName(user.getOrg(), minion.getMinionId());
        SystemRecord.create(client, systemName, profileWithSystems);

        assertThrows(SaltbootException.class, () ->
                SaltbootUtils.deleteSaltbootProfile(groupWithSystemsProfileName, client));
    }

    @Test
    public void testCreateSaltbootSystem() throws Exception {
        // 1. Setup
        ImageInfo image = createImageHelper(user, "my-image", "1.0.0", 1);
        SaltbootUtils.createSaltbootDistro(image, Distro.list(client), client);
        String imageProfileName = SaltbootUtils.makeCobblerNameVR(image);

        ServerGroup group = createSaltbootGroupHelper(user, "my-saltboot-group");
        SaltbootUtils.createSaltbootProfile(group, imageProfileName, false, client);
        String groupName = group.getName();

        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        minion.setMinionId("test-minion-for-system");

        List<String> hwAddresses = List.of("AA:BB:CC:DD:EE:FF");
        String kernelParams = "custom_param=value otherparam=\"quoted value\"";

        // 2. Test system creation
        SaltbootUtils.createSaltbootSystem(minion, "my-image-1.0.0-1", groupName, hwAddresses, kernelParams, client);

        String systemName = SaltbootUtils.makeCobblerName(user.getOrg(), minion.getMinionId());
        SystemRecord system = SystemRecord.lookupByName(client, systemName);
        assertNotNull(system);
        assertEquals(imageProfileName, system.getProfile().getName());
        // Test system has one network interface by default, we need to look for our entry
        assertEquals(2, system.getNetworkInterfaces().size());
        assertTrue(system.getNetworkInterfaces().stream().anyMatch(
                nic -> nic.getMacAddress().equals("AA:BB:CC:DD:EE:FF")));
        assertTrue(system.isNetbootEnabled());

        String[] expectedOpts =
            ("custom_param=value otherparam=\"quoted value\" MINION_ID_PREFIX=my-saltboot-group " +
             "MASTER=mybranch.example.com DISABLE_ID_PREFIX=1")
            .split(" ");
        String[] actualOpts = system.getKernelOptions().get().entrySet().stream().map(
                entry -> entry.getKey() + "=" + entry.getValue()).toArray(String[]::new);
        Arrays.sort(expectedOpts);
        Arrays.sort(actualOpts);
        assertEquals(String.join(" ", expectedOpts), String.join(" ", actualOpts));

        // 3. Test system update
        ImageInfo newImage = createImageHelper(user, "my-new-image", "2.0.0", 1);
        SaltbootUtils.createSaltbootDistro(newImage, Distro.list(client), client);
        String newImageProfileName = SaltbootUtils.makeCobblerNameVR(newImage);
        SaltbootUtils.createSaltbootSystem(minion, "my-new-image-2.0.0-1",
                groupName, hwAddresses, kernelParams, client);

        system = SystemRecord.lookupByName(client, systemName);
        assertNotNull(system);
        assertEquals(newImageProfileName, system.getProfile().getName());

        // 4. Test error conditions
        assertThrows(SaltbootException.class, () -> SaltbootUtils.createSaltbootSystem(
                minion, "non-existent-image", groupName, hwAddresses, kernelParams, client),
                "Should throw when image profile doesn't exist");
        assertThrows(SaltbootException.class, () -> SaltbootUtils.createSaltbootSystem(
                minion, imageProfileName, "non-existent-group", hwAddresses, kernelParams, client),
                "Should throw when group profile doesn't exist");

        // 5. Test MAC conflict resolution
        MinionServer conflictingMinion = MinionServerFactoryTest.createTestMinionServer(user);
        conflictingMinion.setMinionId("conflicting-minion");
        String conflictingSystemName = SaltbootUtils.makeCobblerName(user.getOrg(), "some-other-system");
        SystemRecord conflictingSystem = SystemRecord.create(client, conflictingSystemName,
                Profile.lookupByName(client, imageProfileName));
        Network net = new Network(client, "00:11:22:33:44:55");
        net.setMacAddress("00:11:22:33:44:55");
        conflictingSystem.setNetworkInterfaces(List.of(net));
        conflictingSystem.save();

        SaltbootUtils.createSaltbootSystem(conflictingMinion, "my-image-1.0.0-1", groupName,
                List.of("00:11:22:33:44:55"), "", client);

        assertNull(SystemRecord.lookupByName(client,
                conflictingSystemName), "Conflicting system should be deleted");
        assertNotNull(SystemRecord.lookupByName(client,
                SaltbootUtils.makeCobblerName(user.getOrg(), "conflicting-minion")));
    }
}
