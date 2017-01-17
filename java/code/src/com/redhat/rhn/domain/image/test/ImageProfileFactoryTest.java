package com.redhat.rhn.domain.image.test;

import com.redhat.rhn.domain.image.DockerfileProfile;
import com.redhat.rhn.domain.image.ImageProfile;
import com.redhat.rhn.domain.image.ImageProfileFactory;
import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.image.ImageStoreFactory;
import com.redhat.rhn.domain.image.ProfileCustomDataValue;
import com.redhat.rhn.domain.org.CustomDataKey;
import com.redhat.rhn.domain.org.test.CustomDataKeyTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import java.util.List;
import java.util.Set;

public class ImageProfileFactoryTest extends BaseTestCaseWithUser {

    public void testLookupByLabel() throws Exception {
        ImageStore iStore = new ImageStore();
        iStore.setLabel("myregistry");
        iStore.setUri("registry.domain.top");
        iStore.setStoreType(ImageStoreFactory.lookupStoreTypeByLabel("dockerreg"));
        iStore.setOrg(user.getOrg());
        ImageStoreFactory.save(iStore);

        DockerfileProfile profile = new DockerfileProfile();
        profile.setLabel("suma-3.1-base");
        profile.setOrg(user.getOrg());
        profile.setPath("http://git.domain.top/dockerimages.git#mybranch:profiles/suma-3.1-base");
        profile.setToken(null);
        profile.setStore(iStore);
        ImageProfileFactory.save(profile);
        profile = TestUtils.saveAndReload(profile);

        ImageProfile prf = ImageProfileFactory.lookupByLabel("suma-3.1-base");
        assertEquals(profile, prf);
    }

    public void testLookupByLabelAndOrg() throws Exception {
        ImageStore iStore = new ImageStore();
        iStore.setLabel("myregistry");
        iStore.setUri("registry.domain.top");
        iStore.setStoreType(ImageStoreFactory.lookupStoreTypeByLabel("dockerreg"));
        iStore.setOrg(user.getOrg());
        ImageStoreFactory.save(iStore);

        DockerfileProfile profile = new DockerfileProfile();
        profile.setLabel("suma-3.1-base");
        profile.setOrg(user.getOrg());
        profile.setPath("http://git.domain.top/dockerimages.git#mybranch:profiles/suma-3.1-base");
        profile.setToken(null);
        profile.setStore(iStore);
        ImageProfileFactory.save(profile);
        profile = TestUtils.saveAndReload(profile);

        ImageProfile prf = ImageProfileFactory.lookupByLabelAndOrg("suma-3.1-base", user.getOrg());
        assertEquals(profile, prf);
    }

    public void testListImageProfiles() throws Exception {
        ImageStore iStore = new ImageStore();
        iStore.setLabel("myregistry");
        iStore.setUri("registry.domain.top");
        iStore.setStoreType(ImageStoreFactory.lookupStoreTypeByLabel("dockerreg"));
        iStore.setOrg(user.getOrg());
        ImageStoreFactory.save(iStore);

        DockerfileProfile profile = new DockerfileProfile();
        profile.setLabel("suma-3.1-base");
        profile.setOrg(user.getOrg());
        profile.setPath("http://git.domain.top/dockerimages.git#mybranch:profiles/suma-3.1-base");
        profile.setToken(null);
        profile.setStore(iStore);
        ImageProfileFactory.save(profile);
        profile = TestUtils.saveAndReload(profile);

        List<ImageProfile> list = ImageProfileFactory.listImageProfiles(user.getOrg());
        assertNotEmpty(list);

        for (ImageProfile p : list) {
            if (p.getLabel().equals("suma-3.1-base")) {
                assertEquals(profile, p);
            }
            else {
                assertTrue("profile not found", false);
            }
        }
    }

    public void testProfileCustomData() throws Exception {
        ImageStore iStore = new ImageStore();
        iStore.setLabel("myregistry");
        iStore.setUri("registry.domain.top");
        iStore.setStoreType(ImageStoreFactory.lookupStoreTypeByLabel("dockerreg"));
        iStore.setOrg(user.getOrg());
        ImageStoreFactory.save(iStore);

        DockerfileProfile profile = new DockerfileProfile();
        profile.setLabel("suma-3.1-base");
        profile.setOrg(user.getOrg());
        profile.setPath("http://git.domain.top/dockerimages.git#mybranch:profiles/suma-3.1-base");
        profile.setToken(null);
        profile.setStore(iStore);
        ImageProfileFactory.save(profile);
        profile = TestUtils.saveAndReload(profile);

        CustomDataKey key = CustomDataKeyTest.createTestCustomDataKey(user);
        ProfileCustomDataValue val = createTestProfileCustomDataValue(user, key, profile);

        Set<ProfileCustomDataValue> values = profile.getCustomDataValues();
        assertNotNull(values);
        for (ProfileCustomDataValue v : values) {
            assertEquals(val, v);
        }
        CustomDataKey key2 = CustomDataKeyTest.createTestCustomDataKey(user);
        ProfileCustomDataValue val2 = createTestProfileCustomDataValue(user, key2, profile);

        profile = TestUtils.saveAndReload(profile);
        Set<ProfileCustomDataValue> values2 = profile.getCustomDataValues();
        assertNotNull(values2);
        for (ProfileCustomDataValue v : values2) {
            if (v.getKey().equals(val.getKey())) {
                assertEquals(val, v);
            }
            else if (v.getKey().equals(val2.getKey())) {
                assertEquals(val2, v);
            }
        }
        assertEquals(2, values2.size());
    }

    public static ProfileCustomDataValue createTestProfileCustomDataValue(User user,
            CustomDataKey key, ImageProfile profile) {
        ProfileCustomDataValue val = new ProfileCustomDataValue();
        val.setCreator(user);
        val.setKey(key);
        val.setProfile(profile);
        val.setValue("Test value");

        TestUtils.saveAndFlush(val);

        return val;
    }
}
