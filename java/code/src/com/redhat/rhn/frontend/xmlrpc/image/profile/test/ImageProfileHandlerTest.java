/**
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
package com.redhat.rhn.frontend.xmlrpc.image.profile.test;

import com.redhat.rhn.domain.image.ImageProfile;
import com.redhat.rhn.domain.image.ImageProfileFactory;
import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.image.ProfileCustomDataValue;
import com.redhat.rhn.domain.org.CustomDataKey;
import com.redhat.rhn.domain.org.test.CustomDataKeyTest;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.InvalidParameterException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchImageProfileException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchImageStoreException;
import com.redhat.rhn.frontend.xmlrpc.image.profile.ImageProfileHandler;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.redhat.rhn.testing.ImageTestUtils.createActivationKey;
import static com.redhat.rhn.testing.ImageTestUtils.createImageProfile;
import static com.redhat.rhn.testing.ImageTestUtils.createImageStore;

public class ImageProfileHandlerTest extends BaseHandlerTestCase {

    private ImageProfileHandler handler = new ImageProfileHandler();

    public final void testListImageProfileTypes() throws Exception {
        List<String> types = handler.listImageProfileTypes(admin);
        assertEquals("Wrong number of image profile types found.", 1, types.size());
        assertEquals(ImageProfile.TYPE_DOCKERFILE, types.get(0));
    }

    public final void testGetDetails() throws Exception {
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
    }

    public final void testListImageProfiles() {
        createImageStore("myregistry", admin);
        int result = handler.create(admin, "newprofile1", ImageProfile.TYPE_DOCKERFILE,
                "myregistry", "/path/to/dockerfile", "");
        assertEquals(1, result);

        result = handler.create(admin, "newprofile2", ImageProfile.TYPE_DOCKERFILE,
                "myregistry", "/path/to/dockerfile", "");
        assertEquals(1, result);

        List<ImageProfile> profiles = handler.listImageProfiles(admin);

        long cnt = profiles.stream().filter(p -> ("newprofile1".equals(p.getLabel()) ||
                "newprofile2".equals(p.getLabel()))).count();

        assertEquals(2, cnt);
    }

    public final void testCreateImageProfile() throws Exception {
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

    public final void testCreateImageProfileFailed() throws Exception {
        ActivationKey key = createActivationKey(admin);
        createImageProfile("existing-profile", createImageStore("myregistry", admin), admin);

        try {
            handler.create(admin, "newprofile", "container", "mystore",
                    "/path/to/dockerfile/", key.getKey());
            fail("Invalid type provided.");
        }
        catch (InvalidParameterException e) {
            assertEquals("Type does not exist.", e.getMessage());
        }

        try {
            handler.create(admin, "newprofile", "dockerfile", "mystore",
                    "/path/to/dockerfile/", key.getKey());
            fail("Invalid store provided.");
        }
        catch (NoSuchImageStoreException ignore) { }

        try {
            handler.create(admin, "newprofile", "dockerfile", "myregistry",
                    "/path/to/dockerfile/", "invalidkey");
            fail("Invalid activation key provided.");
        }
        catch (InvalidParameterException e) {
            assertEquals("Activation key does not exist.", e.getMessage());
        }

        try {
            handler.create(admin, "", ImageProfile.TYPE_DOCKERFILE, "myregistry",
                    "/path/to/dockerfile/", key.getKey());
            fail("No label provided.");
        }
        catch (InvalidParameterException e) {
            assertEquals("Label cannot be empty.", e.getMessage());
        }

        try {
            handler.create(admin, "existing-profile", ImageProfile.TYPE_DOCKERFILE, "myregistry",
                    "/path/to/dockerfile/", key.getKey());
            fail("Existing label provided.");
        }
        catch (InvalidParameterException e) {
            assertEquals("Image already exists.", e.getMessage());
        }

        try {
            handler.create(admin, "invalid:chars", ImageProfile.TYPE_DOCKERFILE, "myregistry",
                    "/path/to/dockerfile/", key.getKey());
            fail("Invalid label with ':' character provided.");
        }
        catch (InvalidParameterException e) {
            assertEquals("Label cannot contain colons (:).", e.getMessage());
        }

        try {
            handler.create(admin, "newprofile", "", "myregistry", "/path/to/dockerfile/",
                    key.getKey());
            fail("No type provided.");
        }
        catch (InvalidParameterException e) {
            assertEquals("Type cannot be empty.", e.getMessage());
        }

        try {
            handler.create(admin, "newprofile", ImageProfile.TYPE_DOCKERFILE, "",
                    "/path/to/dockerfile/", key.getKey());
            fail("No store label provided.");
        }
        catch (InvalidParameterException e) {
            assertEquals("Store label cannot be empty.", e.getMessage());
        }

        try {
            handler.create(admin, "newprofile", ImageProfile.TYPE_DOCKERFILE, "myregistry",
                    "", key.getKey());
            fail("No path provided.");
        }
        catch (InvalidParameterException e) {
            assertEquals("Path cannot be empty.", e.getMessage());
        }
    }

    public final void testDeleteProfile() {
        createImageStore("myregistry", admin);
        int result = handler.create(admin, "myprofile", ImageProfile.TYPE_DOCKERFILE,
                "myregistry", "/path/to/dockerfile", "");
        assertEquals(1, result);

        try {
            handler.delete(admin, "");
            fail("No Label provided.");
        }
        catch (InvalidParameterException e) {
            assertEquals("Label cannot be empty.", e.getMessage());
        }

        try {
            handler.delete(admin, "invalidlabel");
            fail("Invalid Label provided.");
        }
        catch (NoSuchImageProfileException ignore) { }

        result = handler.delete(admin, "myprofile");

        assertEquals(1, result);
        try {
            handler.getDetails(admin, "myprofile");
            fail("Profile should have been deleted.");
        }
        catch (NoSuchImageProfileException ignore) { }
    }

    public final void testSetDetails() throws Exception {
        createImageStore("myregistry", admin);
        ActivationKey key = createActivationKey(admin);
        int result = handler.create(admin, "myprofile", ImageProfile.TYPE_DOCKERFILE,
                "myregistry", "/path/to/dockerfile", key.getKey());

        assertEquals(1, result);

        createImageStore("newstore", admin);
        ActivationKey newKey = createActivationKey(admin);
        assertFalse(key.equals(newKey));

        Map<String, String> details = new HashMap<>();

        // Invalid attempts
        try {
            handler.setDetails(admin, "", details);
            fail("No label provided.");
        }
        catch (InvalidParameterException e) {
            assertEquals("Label cannot be empty.", e.getMessage());
        }

        try {
            handler.setDetails(admin, "invalidlabel", details);
            fail("Invalid label provided.");
        }
        catch (NoSuchImageProfileException ignore) { }

        try {
            details.clear();
            details.put("storeLabel", "invalidstore");
            handler.setDetails(admin, "myprofile", details);
            fail("Invalid store label provided.");
        }
        catch (NoSuchImageStoreException ignore) { }

        try {
            details.clear();
            details.put("storeLabel", "");
            handler.setDetails(admin, "myprofile", details);
            fail("No store label provided.");
        }
        catch (InvalidParameterException e) {
            assertEquals("Store label cannot be empty.", e.getMessage());
        }

        try {
            details.clear();
            details.put("path", "");
            handler.setDetails(admin, "myprofile", details);
            fail("No path provided.");
        }
        catch (InvalidParameterException e) {
            assertEquals("Path cannot be empty.", e.getMessage());
        }

        try {
            details.clear();
            details.put("activationKey", "invalidkey");
            handler.setDetails(admin, "myprofile", details);
            fail("Invalid activation key provided.");
        }
        catch (InvalidParameterException e) {
            assertEquals("Activation key does not exist.", e.getMessage());
        }

        details.clear();
        details.put("storeLabel", "newstore");
        details.put("path", "/new/path");
        details.put("activationKey", newKey.getKey());
        result = handler.setDetails(admin, "myprofile", details);

        assertEquals(1, result);
        ImageProfile profile =
                ImageProfileFactory.lookupByLabelAndOrg("myprofile", admin.getOrg()).get();
        assertEquals("newstore", profile.getTargetStore().getLabel());
        assertEquals("/new/path", profile.asDockerfileProfile().get().getPath());
        assertEquals(newKey.getToken(), profile.getToken());

        // Unset activation key
        details.clear();
        details.put("activationKey", "");
        result = handler.setDetails(admin, "myprofile", details);
        assertEquals(1, result);
        profile = ImageProfileFactory.lookupByLabelAndOrg("myprofile",
                admin.getOrg()).get();
        assertNull(profile.getToken());
    }

    public final void testGetCustomValues() {
        createImageStore("myregistry", admin);
        int result = handler.create(admin, "myprofile", ImageProfile.TYPE_DOCKERFILE,
                "myregistry", "/path/to/dockerfile", "");
        assertEquals(1, result);

        CustomDataKey orgKey1 = CustomDataKeyTest.createTestCustomDataKey(admin);
        CustomDataKey orgKey2 = CustomDataKeyTest.createTestCustomDataKey(admin);
        admin.getOrg().addCustomDataKey(orgKey1);
        admin.getOrg().addCustomDataKey(orgKey2);

        Map <String, String> values = new HashMap<>();
        values.put(orgKey1.getLabel(), "newvalue1");
        values.put(orgKey2.getLabel(), "newvalue2");
        result = handler.setCustomValues(admin, "myprofile", values);
        assertEquals(1, result);

        try {
            handler.getCustomValues(admin, "");
            fail("No Label provided.");
        }
        catch (InvalidParameterException e) {
            assertEquals("Label cannot be empty.", e.getMessage());
        }

        try {
            handler.getCustomValues(admin, "invalidlabel");
            fail("Invalid Label provided.");
        }
        catch (NoSuchImageProfileException ignore) { }

        Map<String, String> results = handler.getCustomValues(admin, "myprofile");
        assertEquals(values, results);
    }

    public final void testSetCustomDataValues() throws Exception {
        createImageStore("myregistry", admin);
        int result = handler.create(admin, "myprofile", ImageProfile.TYPE_DOCKERFILE,
                "myregistry", "/path/to/dockerfile", "");
        assertEquals(1, result);


        // Create additional user
        User anotherAdmin = UserTestUtils.createUserInOrgOne();
        anotherAdmin.addPermanentRole(RoleFactory.ORG_ADMIN);
        TestUtils.saveAndFlush(anotherAdmin);

        // Create custom data keys for the organization
        CustomDataKey orgKey1 = CustomDataKeyTest.createTestCustomDataKey(admin);
        CustomDataKey orgKey2 = CustomDataKeyTest.createTestCustomDataKey(admin);
        admin.getOrg().addCustomDataKey(orgKey1);
        admin.getOrg().addCustomDataKey(orgKey2);

        Map <String, String> values = new HashMap<>();

        values.put(orgKey1.getLabel(), "newvalue");
        try {
            handler.setCustomValues(admin, "", values);
            fail("No Label provided.");
        }
        catch (InvalidParameterException e) {
            assertEquals("Label cannot be empty.", e.getMessage());
        }

        try {
            handler.setCustomValues(admin, "invalidlabel", values);
            fail("Invalid Label provided.");
        }
        catch (NoSuchImageProfileException ignore) { }

        values.put("invalidkey", "newvalue");
        try {
            handler.setCustomValues(admin, "myprofile", values);
            fail("Invalid key provided.");
        }
        catch (InvalidParameterException e) {
            assertEquals("The key 'invalidkey' doesn't exist.", e.getMessage());
        }

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
                assertEquals(cdv.getValue(), "newvalue1");
            }
            else if (cdv.getKey().equals(orgKey2)) {
                assertEquals(cdv.getValue(), "newvalue2");
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
                assertEquals(cdv.getValue(), "newvalue3");
                assertEquals(cdv.getLastModifier(), anotherAdmin);
            }
            else if (cdv.getKey().equals(orgKey2)) {
                assertEquals(cdv.getValue(), "newvalue2");
                assertEquals(cdv.getLastModifier(), admin);
            }
            else {
                fail("Invalid key in profile.");
            }
            assertEquals(cdv.getCreator(), admin);
            assertEquals(cdv.getProfile(), updatedProfile);
        });
    }

    public final void testDeleteCustomValues() {
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
        Map <String, String> values = new HashMap<>();
        values.put(orgKey1.getLabel(), "myvalue1");
        values.put(orgKey2.getLabel(), "myvalue2");
        result = handler.setCustomValues(admin, "myprofile", values);
        assertEquals(1, result);

        ImageProfile profile = handler.getDetails(admin, "myprofile");
        assertEquals(2, profile.getCustomDataValues().size());

        List<String> keysToDelete = new ArrayList<>();
        keysToDelete.add(orgKey1.getLabel());
        try {
            handler.deleteCustomValues(admin, "", keysToDelete);
            fail("No Label provided.");
        }
        catch (InvalidParameterException e) {
            assertEquals("Label cannot be empty.", e.getMessage());
        }

        try {
            handler.deleteCustomValues(admin, "invalidlabel", keysToDelete);
            fail("Invalid Label provided.");
        }
        catch (NoSuchImageProfileException ignore) { }

        keysToDelete.add("invalidkey");
        try {
            handler.deleteCustomValues(admin, "myprofile", keysToDelete);
            fail("Invalid key provided.");
        }
        catch (InvalidParameterException e) {
            assertEquals("The key 'invalidkey' doesn't exist.", e.getMessage());
        }

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
