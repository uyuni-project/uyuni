/*
 * Copyright (c) 2017--2021 SUSE LLC
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
package com.redhat.rhn.domain.image.test;

import static com.redhat.rhn.testing.ImageTestUtils.createImageProfile;
import static com.redhat.rhn.testing.ImageTestUtils.createImageStore;
import static com.redhat.rhn.testing.ImageTestUtils.createProfileCustomDataValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ImageProfileFactoryTest extends BaseTestCaseWithUser {

    @Test
    public void testLookupById() {
        ImageProfile profile =
                createImageProfile("myprofile", createImageStore("mystore", user), user);

        Optional<ImageProfile> lookup =
                ImageProfileFactory.lookupById(profile.getProfileId());
        assertTrue(lookup.isPresent());
        assertEquals(profile, lookup.get());

        lookup = ImageProfileFactory.lookupById(-1);
        assertFalse(lookup.isPresent());
    }

    @Test
    public void testLookupByIdAndOrg() {
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

    @Test
    public void testLookupByIdsAndOrg() {
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

    @Test
    public void testLookupByLabelAndOrg() {
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

    @Test
    public void testListImageProfiles() {
        ImageProfile profile =
                createImageProfile("myprofile", createImageStore("mystore", user), user);

        List<ImageProfile> list = ImageProfileFactory.listImageProfiles(user.getOrg());
        assertEquals(1, list.size());
        assertEquals(profile, list.get(0));
    }

    @Test
    public void testProfileCustomData() {
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
