/*
 * Copyright (c) 2017 SUSE LLC
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
package com.redhat.rhn.frontend.xmlrpc.image.profile;

import static com.redhat.rhn.testing.ImageTestUtils.createActivationKey;
import static com.redhat.rhn.testing.ImageTestUtils.createImageProfile;
import static com.redhat.rhn.testing.ImageTestUtils.createImageStore;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.image.DockerfileProfile;
import com.redhat.rhn.domain.image.ImageProfile;
import com.redhat.rhn.domain.image.ImageProfileFactory;
import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.image.ImageStoreFactory;
import com.redhat.rhn.domain.image.KiwiProfile;
import com.redhat.rhn.domain.image.ProfileCustomDataValue;
import com.redhat.rhn.domain.org.CustomDataKey;
import com.redhat.rhn.domain.org.CustomDataKeyTest;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandlerTestCase;
import com.redhat.rhn.frontend.xmlrpc.InvalidParameterException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchImageProfileException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchImageStoreException;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageProfileHandlerTest extends BaseHandlerTestCase {

    private ImageProfileHandler handler = new ImageProfileHandler();

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        Config.get().setBoolean(ConfigDefaults.KIWI_OS_IMAGE_BUILDING_ENABLED, "true");
    }

    @Test
    void testListImageProfileTypes() {
        List<String> types = handler.listImageProfileTypes(admin);
        assertEquals(2, types.size(), "Wrong number of image profile types found.");
        assertTrue(types.stream().anyMatch(ImageProfile.TYPE_DOCKERFILE::equals));
        assertTrue(types.stream().anyMatch(ImageProfile.TYPE_KIWI::equals));
    }

    @Test
    void testGetDetailsDockerfile() throws Exception {
        ImageStore store = createImageStore("myregistry", admin);
        ActivationKey key = createActivationKey(admin);
        int result = handler.create(admin, "myprofile", ImageProfile.TYPE_DOCKERFILE,
                "myregistry", "/path/to/dockerfile", key.getKey());

        assertEquals(1, result);

        // Try with no label
        try {
            handler.getDetails(admin, "");
        }
        catch (InvalidParameterException e) {
            assertEquals("Label cannot be empty.", e.getMessage());
        }

        ImageProfile profile = handler.getDetails(admin, "myprofile");
        assertEquals("myprofile", profile.getLabel());
        assertEquals(ImageProfile.TYPE_DOCKERFILE, profile.getImageType());
        assertEquals(key.getToken(), profile.getToken());
        assertEquals(store, profile.getTargetStore());
        assertEquals("/path/to/dockerfile", profile.asDockerfileProfile().get().getPath());
    }

    @Test
    void testGetDetailsKiwi() throws Exception {
        ImageStore store = createImageStore("mystore", admin, ImageStoreFactory.TYPE_OS_IMAGE);
        ActivationKey key = createActivationKey(admin);
        int result = handler.create(admin, "myprofile", ImageProfile.TYPE_KIWI,
                "mystore", "/path/to/kiwiconfig", key.getKey(), "--profile test1");

        assertEquals(1, result);

        ImageProfile profile = handler.getDetails(admin, "myprofile");
        assertEquals("myprofile", profile.getLabel());
        assertEquals(ImageProfile.TYPE_KIWI, profile.getImageType());
        assertEquals(key.getToken(), profile.getToken());
        assertEquals(store, profile.getTargetStore());
        assertEquals("/path/to/kiwiconfig", profile.asKiwiProfile().get().getPath());
        assertEquals("--profile test1", profile.asKiwiProfile().get().getKiwiOptions());
    }

    @Test
    void testListImageProfiles() throws Exception {
        createImageStore("myregistry", admin, ImageStoreFactory.TYPE_REGISTRY);
        createImageStore("myosimagestore", admin, ImageStoreFactory.TYPE_OS_IMAGE);

        int result = handler.create(admin, "newprofile1", ImageProfile.TYPE_DOCKERFILE,
                "myregistry", "/path/to/dockerfile", "");
        assertEquals(1, result);

        ActivationKey key = createActivationKey(admin);
        result = handler.create(admin, "newprofile2", ImageProfile.TYPE_KIWI,
                "myosimagestore", "/path/to/kiwiconfig", key.getKey());
        assertEquals(1, result);

        List<ImageProfile> profiles = handler.listImageProfiles(admin);

        assertEquals(2, profiles.size());

        DockerfileProfile p1 = profiles.stream().filter(p -> "newprofile1".equals(p.getLabel())).findFirst().get()
                .asDockerfileProfile().get();
        assertEquals(ImageProfile.TYPE_DOCKERFILE, p1.getImageType());
        assertEquals("myregistry", p1.getTargetStore().getLabel());
        assertEquals("/path/to/dockerfile", p1.getPath());

        KiwiProfile p2 = profiles.stream().filter(p -> "newprofile2".equals(p.getLabel())).findFirst().get()
                .asKiwiProfile().get();
        assertEquals(ImageProfile.TYPE_KIWI, p2.getImageType());
        assertEquals("myosimagestore", p2.getTargetStore().getLabel());
        assertEquals("/path/to/kiwiconfig", p2.getPath());
        assertEquals("", p2.getKiwiOptions());
    }

    @Test
    void testCreateImageProfile() throws Exception {
        ImageStore store = createImageStore("myregistry", admin);
        int result = handler.create(admin, "newprofile", ImageProfile.TYPE_DOCKERFILE,
                "myregistry", "/path/to/dockerfile", "");

        assertEquals(1, result);

        ImageProfile profile = ImageProfileFactory.lookupByLabelAndOrg(
                "newprofile", admin.getOrg()).get();

        assertEquals("newprofile", profile.getLabel());
        assertEquals(ImageProfile.TYPE_DOCKERFILE, profile.getImageType());
        assertEquals(store, profile.getTargetStore());
        assertEquals("/path/to/dockerfile", profile.asDockerfileProfile().get().getPath());
        assertNull(profile.getToken());

        // Create an activation key
        ActivationKey key = createActivationKey(admin);

        result = handler.create(admin, "newprofile2", ImageProfile.TYPE_DOCKERFILE,
                "myregistry", "/path/to/dockerfile", key.getKey());

        assertEquals(1, result);
        profile = ImageProfileFactory.lookupByLabelAndOrg("newprofile2",
                admin.getOrg()).get();
        assertEquals(key.getToken(), profile.getToken());
    }

    @Test
    void testCreateImageProfileFailedPart1() throws Exception {
        ActivationKey key = createActivationKey(admin);
        createImageProfile("existing-profile", createImageStore("myregistry", admin), admin);

        String activationKey = key.getKey();
        InvalidParameterException ex1 = assertThrows(InvalidParameterException.class,
                () -> handler.create(admin, "newprofile", "container", "mystore",
                        "/path/to/dockerfile/", activationKey),
                "Invalid type provided.");
        assertEquals("Type does not exist.", ex1.getMessage());

        ActivationKey tmp = createActivationKey(admin);
        tmp.setBaseChannel(null);
        String activationKey2 = tmp.getKey();
        InvalidParameterException ex2 = assertThrows(InvalidParameterException.class,
                () -> handler.create(admin, "newprofile", "dockerfile", "mystore",
                        "/path/to/dockerfile/", activationKey2),
                "Activation Key with no base channel provided for Kiwi profile");
        assertEquals("Activation key does not have any base channel associated " +
                "(do not use SUSE Multi-Linux Manager default).", ex2.getMessage());


        String activationKey3 = key.getKey();
        assertThrows(NoSuchImageStoreException.class,
                () -> handler.create(admin, "newprofile", "dockerfile", "mystore",
                        "/path/to/dockerfile/", activationKey3),
                "Invalid store provided.");

        InvalidParameterException ex3 = assertThrows(InvalidParameterException.class,
                () -> handler.create(admin, "newprofile", "dockerfile", "myregistry",
                        "/path/to/dockerfile/", "invalidkey"),
                "Invalid activation key provided.");
        assertEquals("Activation key does not exist.", ex3.getMessage());


        createImageStore("myosimagestore", admin, ImageStoreFactory.TYPE_OS_IMAGE);

        InvalidParameterException ex4 = assertThrows(InvalidParameterException.class,
                () -> handler.create(admin, "newprofile", "kiwi", "myosimagestore",
                        "/path/to/dockerfile/", ""),
                "No activation key provided for Kiwi profile.");
        assertEquals("Activation key cannot be empty for Kiwi profiles.", ex4.getMessage());

        ActivationKey tmp5 = createActivationKey(admin);
        tmp5.setBaseChannel(null);
        String activationKey5 = tmp5.getKey();
        InvalidParameterException ex5 = assertThrows(InvalidParameterException.class,
                () -> handler.create(admin, "newprofile", "kiwi", "myosimagestore",
                        "/path/to/kiwiconfig", activationKey5),
                "Activation Key with no base channel provided for Kiwi profile");
        assertEquals("Activation key does not have any base channel associated " +
                "(do not use SUSE Multi-Linux Manager default).", ex5.getMessage());

        String activationKey6 = key.getKey();
        InvalidParameterException ex6 = assertThrows(InvalidParameterException.class,
                () -> handler.create(admin, "newprofile", "dockerfile", "myosimagestore",
                        "/path/to/dockerfile", activationKey6),
                "os_image store provided for dockerfile profile.");
        assertEquals("Invalid store for profile type: 'dockerfile'", ex6.getMessage());

        String activationKey7 = key.getKey();
        InvalidParameterException ex7 = assertThrows(InvalidParameterException.class,
                () -> handler.create(admin, "newprofile", "kiwi", "myregistry",
                        "/path/to/kiwiconfig", activationKey7),
                "registry store provided for kiwi profile.");
        assertEquals("Invalid store for profile type: 'kiwi'", ex7.getMessage());
    }

    @Test
    void testCreateImageProfileFailedPart2() throws Exception {
        ActivationKey key = createActivationKey(admin);
        createImageProfile("existing-profile", createImageStore("myregistry", admin), admin);

        String activationKey8 = key.getKey();
        InvalidParameterException ex8 = assertThrows(InvalidParameterException.class,
                () -> handler.create(admin, "newprofile", "invalidtype", "myosimagestore",
                        "/path/to/dockerfile", activationKey8),
                "Invalid profile type provided.");
        assertEquals("Type does not exist.", ex8.getMessage());

        String activationKey9 = key.getKey();
        InvalidParameterException ex9 = assertThrows(InvalidParameterException.class,
                () -> handler.create(admin, "", ImageProfile.TYPE_DOCKERFILE, "myregistry",
                        "/path/to/dockerfile/", activationKey9),
                "No label provided.");
        assertEquals("Label cannot be empty.", ex9.getMessage());

        String activationKey10 = key.getKey();
        InvalidParameterException ex10 = assertThrows(InvalidParameterException.class,
                () -> handler.create(admin, "existing-profile", ImageProfile.TYPE_DOCKERFILE,
                        "myregistry", "/path/to/dockerfile/", activationKey10),
                "Existing label provided.");
        assertEquals("Image already exists.", ex10.getMessage());

        String activationKey11 = key.getKey();
        InvalidParameterException ex11 = assertThrows(InvalidParameterException.class,
                () -> handler.create(admin, "invalid:chars", ImageProfile.TYPE_DOCKERFILE, "myregistry",
                        "/path/to/dockerfile/", activationKey11),
                "Invalid label with ':' character provided.");
        assertEquals("Label cannot contain colons (:).", ex11.getMessage());

        String activationKey12 = key.getKey();
        InvalidParameterException ex12 = assertThrows(InvalidParameterException.class,
                () -> handler.create(admin, "newprofile", "", "myregistry",
                        "/path/to/dockerfile/", activationKey12),
                "No type provided.");
        assertEquals("Type cannot be empty.", ex12.getMessage());

        String activationKey13 = key.getKey();
        InvalidParameterException ex13 = assertThrows(InvalidParameterException.class,
                () -> handler.create(admin, "newprofile", ImageProfile.TYPE_DOCKERFILE, "",
                        "/path/to/dockerfile/", activationKey13),
                "No store label provided.");
        assertEquals("Store label cannot be empty.", ex13.getMessage());

        String activationKey14 = key.getKey();
        InvalidParameterException ex14 = assertThrows(InvalidParameterException.class,
                () -> handler.create(admin, "newprofile", ImageProfile.TYPE_DOCKERFILE, "myregistry",
                        "", activationKey14),
                "No path provided.");
        assertEquals("Path cannot be empty.", ex14.getMessage());
    }

    @Test
    void testDeleteProfile() {
        createImageStore("myregistry", admin);
        int result = handler.create(admin, "myprofile", ImageProfile.TYPE_DOCKERFILE,
                "myregistry", "/path/to/dockerfile", "");
        assertEquals(1, result);

        InvalidParameterException ex1 = assertThrows(InvalidParameterException.class,
                () -> handler.delete(admin, ""),
                "No Label provided.");
        assertEquals("Label cannot be empty.", ex1.getMessage());

        assertThrows(NoSuchImageProfileException.class,
                () -> handler.delete(admin, "invalidlabel"),
                "Invalid Label provided.");

        result = handler.delete(admin, "myprofile");

        assertEquals(1, result);
        assertThrows(NoSuchImageProfileException.class,
                () -> handler.getDetails(admin, "myprofile"),
                "Profile should have been deleted.");
    }

    @Test
    void testSetDetails() throws Exception {
        createImageStore("myregistry", admin);
        createImageStore("myosimagestore", admin, ImageStoreFactory.TYPE_OS_IMAGE);

        ActivationKey key = createActivationKey(admin);

        handler.create(admin, "mydockerfileprofile", ImageProfile.TYPE_DOCKERFILE,
                "myregistry", "/path/to/dockerfile", key.getKey());
        handler.create(admin, "mykiwiprofile", ImageProfile.TYPE_KIWI,
                "myosimagestore", "/path/to/kiwiconfig", key.getKey());

        createImageStore("newstore", admin);
        createImageStore("newosimagestore", admin, ImageStoreFactory.TYPE_OS_IMAGE);
        ActivationKey newKey = createActivationKey(admin);
        assertNotEquals(key, newKey);

        Map<String, String> details = new HashMap<>();

        // Invalid attempts
        InvalidParameterException ex1 = assertThrows(InvalidParameterException.class,
                () -> handler.setDetails(admin, "", details),
                "No label provided.");
        assertEquals("Label cannot be empty.", ex1.getMessage());

        assertThrows(NoSuchImageProfileException.class,
                () -> handler.setDetails(admin, "invalidlabel", details),
                "Invalid label provided.");

        details.clear();
        details.put("storeLabel", "invalidstore");
        assertThrows(NoSuchImageStoreException.class,
                () -> handler.setDetails(admin, "mydockerfileprofile", details),
                "Invalid store label provided.");

        details.clear();
        details.put("storeLabel", "");
        InvalidParameterException ex2 = assertThrows(InvalidParameterException.class,
                () -> handler.setDetails(admin, "mydockerfileprofile", details),
                "No store label provided.");
        assertEquals("Store label cannot be empty.", ex2.getMessage());

        details.clear();
        details.put("storeLabel", "newosimagestore");
        InvalidParameterException ex3 = assertThrows(InvalidParameterException.class,
                () -> handler.setDetails(admin, "mydockerfileprofile", details),
                "Store label of invalid type provided.");
        assertEquals("Invalid store for profile type: 'dockerfile'", ex3.getMessage());

        details.clear();
        details.put("path", "");
        InvalidParameterException ex4 = assertThrows(InvalidParameterException.class,
                () -> handler.setDetails(admin, "mydockerfileprofile", details),
                "No path provided.");
        assertEquals("Path cannot be empty.", ex4.getMessage());

        details.clear();
        details.put("activationKey", "invalidkey");
        InvalidParameterException ex5 = assertThrows(InvalidParameterException.class,
                () -> handler.setDetails(admin, "mydockerfileprofile", details),
                "Invalid activation key provided.");
        assertEquals("Activation key does not exist.", ex5.getMessage());

        details.clear();
        details.put("activationKey", "");
        InvalidParameterException ex6 = assertThrows(InvalidParameterException.class,
                () -> handler.setDetails(admin, "mykiwiprofile", details),
                "Empty activation key provided for Kiwi profile.");
        assertEquals("Activation key cannot be empty for Kiwi profiles.", ex6.getMessage());

        details.clear();
        details.put("storeLabel", "newstore");
        details.put("path", "/new/path");
        details.put("activationKey", newKey.getKey());
        int result = handler.setDetails(admin, "mydockerfileprofile", details);

        assertEquals(1, result);
        ImageProfile profile =
                ImageProfileFactory.lookupByLabelAndOrg("mydockerfileprofile", admin.getOrg()).get();
        assertEquals("newstore", profile.getTargetStore().getLabel());
        assertEquals("/new/path", profile.asDockerfileProfile().get().getPath());
        assertEquals(newKey.getToken(), profile.getToken());

        // Unset activation key
        details.clear();
        details.put("activationKey", "");
        result = handler.setDetails(admin, "mydockerfileprofile", details);
        assertEquals(1, result);
        profile = ImageProfileFactory.lookupByLabelAndOrg("mydockerfileprofile",
                admin.getOrg()).get();
        assertNull(profile.getToken());
    }

    @Test
    void testGetCustomValues() {
        createImageStore("myregistry", admin);
        int result = handler.create(admin, "myprofile", ImageProfile.TYPE_DOCKERFILE,
                "myregistry", "/path/to/dockerfile", "");
        assertEquals(1, result);

        CustomDataKey orgKey1 = CustomDataKeyTest.createTestCustomDataKey(admin);
        CustomDataKey orgKey2 = CustomDataKeyTest.createTestCustomDataKey(admin);
        admin.getOrg().addCustomDataKey(orgKey1);
        admin.getOrg().addCustomDataKey(orgKey2);

        Map<String, String> values = new HashMap<>();
        values.put(orgKey1.getLabel(), "newvalue1");
        values.put(orgKey2.getLabel(), "newvalue2");
        result = handler.setCustomValues(admin, "myprofile", values);
        assertEquals(1, result);

        InvalidParameterException ex1 = assertThrows(InvalidParameterException.class,
                () -> handler.getCustomValues(admin, ""),
                "No Label provided.");
        assertEquals("Label cannot be empty.", ex1.getMessage());

        assertThrows(NoSuchImageProfileException.class,
                () -> handler.getCustomValues(admin, "invalidlabel"),
                "Invalid Label provided.");

        Map<String, String> results = handler.getCustomValues(admin, "myprofile");
        assertEquals(values, results);
    }

    @Test
    void testSetCustomDataValues() throws Exception {
        createImageStore("myregistry", admin);
        int result = handler.create(admin, "myprofile", ImageProfile.TYPE_DOCKERFILE,
                "myregistry", "/path/to/dockerfile", "");
        assertEquals(1, result);


        // Create additional user
        User anotherAdmin = UserTestUtils.createUser("anotherAdmin", admin.getOrg().getId());
        anotherAdmin.addPermanentRole(RoleFactory.ORG_ADMIN);
        TestUtils.saveAndFlush(anotherAdmin);

        // Create custom data keys for the organization
        CustomDataKey orgKey1 = CustomDataKeyTest.createTestCustomDataKey(admin);
        CustomDataKey orgKey2 = CustomDataKeyTest.createTestCustomDataKey(admin);
        admin.getOrg().addCustomDataKey(orgKey1);
        admin.getOrg().addCustomDataKey(orgKey2);

        Map<String, String> values = new HashMap<>();

        values.put(orgKey1.getLabel(), "newvalue");
        InvalidParameterException ex1 = assertThrows(InvalidParameterException.class,
                () -> handler.setCustomValues(admin, "", values),
                "No Label provided.");
        assertEquals("Label cannot be empty.", ex1.getMessage());

        assertThrows(NoSuchImageProfileException.class,
                () -> handler.setCustomValues(admin, "invalidlabel", values),
                "Invalid Label provided.");

        values.put("invalidkey", "newvalue");
        InvalidParameterException ex2 = assertThrows(InvalidParameterException.class,
                () -> handler.setCustomValues(admin, "myprofile", values),
                "Invalid key provided.");
        assertEquals("The key 'invalidkey' doesn't exist.", ex2.getMessage());

        // Add values
        values.clear();
        values.put(orgKey1.getLabel(), "newvalue1");
        values.put(orgKey2.getLabel(), "newvalue2");
        result = handler.setCustomValues(admin, "myprofile", values);
        assertEquals(1, result);

        ImageProfile profile = handler.getDetails(admin, "myprofile");
        assertEquals(2, profile.getCustomDataValues().size());
        profile.getCustomDataValues().forEach(cdv -> {
            if (cdv.getKey().equals(orgKey1)) {
                assertEquals("newvalue1", cdv.getValue());
            }
            else if (cdv.getKey().equals(orgKey2)) {
                assertEquals("newvalue2", cdv.getValue());
            }
            else {
                fail("Invalid key in profile.");
            }

            assertEquals(cdv.getCreator(), admin);
            assertEquals(cdv.getLastModifier(), admin);
            assertEquals(cdv.getProfile(), profile);
        });

        // Update values with another user
        values.clear();
        values.put(orgKey1.getLabel(), "newvalue3");
        result = handler.setCustomValues(anotherAdmin, "myprofile", values);
        assertEquals(1, result);

        ImageProfile updatedProfile = handler.getDetails(admin, "myprofile");
        assertEquals(2, updatedProfile.getCustomDataValues().size());
        updatedProfile.getCustomDataValues().forEach(cdv -> {
            if (cdv.getKey().equals(orgKey1)) {
                assertEquals("newvalue3", cdv.getValue());
                assertEquals(cdv.getLastModifier(), anotherAdmin);
            }
            else if (cdv.getKey().equals(orgKey2)) {
                assertEquals("newvalue2", cdv.getValue());
                assertEquals(cdv.getLastModifier(), admin);
            }
            else {
                fail("Invalid key in profile.");
            }
            assertEquals(cdv.getCreator(), admin);
            assertEquals(cdv.getProfile(), updatedProfile);
        });
    }

    @Test
    void testDeleteCustomValues() {
        createImageStore("myregistry", admin);
        int result = handler.create(admin, "myprofile", ImageProfile.TYPE_DOCKERFILE,
                "myregistry", "/path/to/dockerfile", "");
        assertEquals(1, result);

        // Create custom data keys for the organization
        CustomDataKey orgKey1 = CustomDataKeyTest.createTestCustomDataKey(admin);
        CustomDataKey orgKey2 = CustomDataKeyTest.createTestCustomDataKey(admin);
        admin.getOrg().addCustomDataKey(orgKey1);
        admin.getOrg().addCustomDataKey(orgKey2);

        // Create values for the profile
        Map<String, String> values = new HashMap<>();
        values.put(orgKey1.getLabel(), "myvalue1");
        values.put(orgKey2.getLabel(), "myvalue2");
        result = handler.setCustomValues(admin, "myprofile", values);
        assertEquals(1, result);

        ImageProfile profile = handler.getDetails(admin, "myprofile");
        assertEquals(2, profile.getCustomDataValues().size());

        List<String> keysToDelete = new ArrayList<>();
        keysToDelete.add(orgKey1.getLabel());
        InvalidParameterException ex1 = assertThrows(InvalidParameterException.class,
                () -> handler.deleteCustomValues(admin, "", keysToDelete),
                "No Label provided.");
        assertEquals("Label cannot be empty.", ex1.getMessage());

        assertThrows(NoSuchImageProfileException.class,
                () -> handler.deleteCustomValues(admin, "invalidlabel", keysToDelete),
                "Invalid Label provided.");

        keysToDelete.add("invalidkey");
        InvalidParameterException ex2 = assertThrows(InvalidParameterException.class,
                () -> handler.deleteCustomValues(admin, "myprofile", keysToDelete),
                "Invalid key provided.");
        assertEquals("The key 'invalidkey' doesn't exist.", ex2.getMessage());

        keysToDelete.clear();
        keysToDelete.add(orgKey1.getLabel());
        result = handler.deleteCustomValues(admin, "myprofile", keysToDelete);

        // Assert results
        assertEquals(1, result);
        profile = handler.getDetails(admin, "myprofile");
        assertEquals(1, profile.getCustomDataValues().size());

        ProfileCustomDataValue val = profile.getCustomDataValues().iterator().next();
        assertEquals(orgKey2.getLabel(), val.getKey().getLabel());
        assertEquals("myvalue2", val.getValue());
    }
}
