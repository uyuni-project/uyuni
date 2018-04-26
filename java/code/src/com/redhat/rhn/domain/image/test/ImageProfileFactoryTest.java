package com.redhat.rhn.domain.image.test;

import com.redhat.rhn.domain.image.ImageProfile;
import com.redhat.rhn.domain.image.ImageProfileFactory;
import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.image.ProfileCustomDataValue;
import com.redhat.rhn.domain.org.CustomDataKey;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.org.test.CustomDataKeyTest;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.redhat.rhn.testing.ImageTestUtils.createImageProfile;
import static com.redhat.rhn.testing.ImageTestUtils.createImageStore;
import static com.redhat.rhn.testing.ImageTestUtils.createProfileCustomDataValue;

public class ImageProfileFactoryTest extends BaseTestCaseWithUser {

    public void testLookupById() throws Exception {
        ImageProfile profile =
                createImageProfile("myprofile", createImageStore("mystore", user), user);

        Optional<ImageProfile> lookup =
                ImageProfileFactory.lookupById(profile.getProfileId());
        assertTrue(lookup.isPresent());
        assertEquals(profile, lookup.get());

        lookup = ImageProfileFactory.lookupById(-1);
        assertFalse(lookup.isPresent());
    }

    public void testLookupByIdAndOrg() throws Exception {
        ImageProfile profile =
                createImageProfile("myprofile", createImageStore("mystore", user), user);

        Optional<ImageProfile> lookup =
                ImageProfileFactory.lookupByIdAndOrg(profile.getProfileId(), user.getOrg());
        assertTrue(lookup.isPresent());
        assertEquals(profile, lookup.get());

        lookup = ImageProfileFactory.lookupByIdAndOrg(-1, user.getOrg());
        assertFalse(lookup.isPresent());

        Org org = OrgFactory.createOrg();
        org.setName("foreign org");
        org = OrgFactory.save(org);

        lookup = ImageProfileFactory.lookupByIdAndOrg(profile.getProfileId(), org);
        assertFalse(lookup.isPresent());
    }

    public void testLookupByIdsAndOrg() throws Exception {
        ImageStore store = createImageStore("mystore", user);
        ImageProfile p1 = createImageProfile("myprofile1", store, user);
        ImageProfile p2 = createImageProfile("myprofile2", store, user);
        ImageProfile p3 = createImageProfile("myprofile3", store, user);

        List<Long> ids = new ArrayList<>();
        ids.add(p1.getProfileId());
        ids.add(p2.getProfileId());

        List<ImageProfile> lookup =
                ImageProfileFactory.lookupByIdsAndOrg(ids, user.getOrg());
        assertEquals(2, lookup.size());
        assertTrue(lookup.stream().filter(p1::equals).findFirst().isPresent());
        assertTrue(lookup.stream().filter(p2::equals).findFirst().isPresent());
        assertFalse(lookup.stream().filter(p3::equals).findFirst().isPresent());

        Org org = OrgFactory.createOrg();
        org.setName("foreign org");
        org = OrgFactory.save(org);

        lookup = ImageProfileFactory.lookupByIdsAndOrg(ids, org);
        assertEquals(0, lookup.size());

        ids.clear();
        ids.add(p1.getProfileId());
        ids.add(100L);
        assertFalse(ImageProfileFactory.lookupById(100L).isPresent());
        lookup = ImageProfileFactory.lookupByIdsAndOrg(ids, user.getOrg());
        assertEquals(1, lookup.size());
        assertEquals(p1, lookup.get(0));
    }

    public void testLookupByLabelAndOrg() throws Exception {
        ImageProfile profile =
                createImageProfile("myprofile", createImageStore("mystore", user), user);

        ImageProfile prf = ImageProfileFactory.lookupByLabelAndOrg("myprofile",
                user.getOrg()).get();
        assertEquals(profile, prf);

        Org org = OrgFactory.createOrg();
        org.setName("foreign org");
        org = OrgFactory.save(org);

        assertFalse(ImageProfileFactory
                .lookupByLabelAndOrg("non-existent-label", user.getOrg()).isPresent());
        assertFalse(ImageProfileFactory.lookupByLabelAndOrg("myprofile", org).isPresent());
    }

    public void testListImageProfiles() throws Exception {
        ImageProfile profile =
                createImageProfile("myprofile", createImageStore("mystore", user), user);

        List<ImageProfile> list = ImageProfileFactory.listImageProfiles(user.getOrg());
        assertEquals(1, list.size());
        assertEquals(profile, list.get(0));
    }

    public void testProfileCustomData() throws Exception {
        ImageProfile profile =
                createImageProfile("myprofile", createImageStore("mystore", user), user);

        CustomDataKey key = CustomDataKeyTest.createTestCustomDataKey(user);
        ProfileCustomDataValue val =
                createProfileCustomDataValue("Test value", key, profile, user);

        Set<ProfileCustomDataValue> values = profile.getCustomDataValues();
        assertNotNull(values);
        for (ProfileCustomDataValue v : values) {
            assertEquals(val, v);
        }
        CustomDataKey key2 = CustomDataKeyTest.createTestCustomDataKey(user);
        ProfileCustomDataValue val2 =
                createProfileCustomDataValue("Test value", key2, profile, user);

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
}
