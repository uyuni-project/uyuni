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

import static com.redhat.rhn.testing.ImageTestUtils.createCredentials;
import static com.redhat.rhn.testing.ImageTestUtils.createImageStore;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.domain.credentials.RegistryCredentials;
import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.image.ImageStoreFactory;
import com.redhat.rhn.domain.image.ImageStoreType;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.UserTestUtils;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ImageStoreFactoryTest extends BaseTestCaseWithUser {

    @Test
    public void testPermanentOsImageStoreExists() {
        Optional<ImageStore> osImgStoreOpt =
                ImageStoreFactory.lookupBylabelAndOrg("SUSE Manager OS Image Store", user.getOrg());

        assertTrue(osImgStoreOpt.isPresent());
        ImageStore osImgStore = osImgStoreOpt.get();

        assertEquals(ImageStoreFactory.TYPE_OS_IMAGE, osImgStore.getStoreType());
        assertEquals(user.getOrg().getId() + "/", osImgStore.getUri());

        Org newOrg = UserTestUtils.createNewOrgFull("My New Org");

        osImgStoreOpt = ImageStoreFactory.lookupBylabelAndOrg("SUSE Manager OS Image Store", newOrg);

        assertTrue(osImgStoreOpt.isPresent());
        osImgStore = osImgStoreOpt.get();

        assertEquals(ImageStoreFactory.TYPE_OS_IMAGE, osImgStore.getStoreType());
        assertEquals(newOrg.getId() + "/", osImgStore.getUri());

        // Try to update store (not allowed)
        osImgStore.setLabel("Updated Store Label");
        try {
            ImageStoreFactory.save(osImgStore);
            fail("Updating OS Image store should not be allowed.");
        }
        catch (IllegalArgumentException e) {
            assertEquals("Cannot update permanent OS Image store", e.getMessage());
        }
    }

    @Test
    public void testLookupStoreType() {
        ImageStoreType stype = ImageStoreFactory.lookupStoreTypeByLabel("registry").get();
        assertEquals(ImageStoreFactory.TYPE_REGISTRY, stype);

        stype = ImageStoreFactory.lookupStoreTypeByLabel("os_image").get();
        assertEquals(ImageStoreFactory.TYPE_OS_IMAGE, stype);

        assertFalse(ImageStoreFactory.lookupStoreTypeByLabel("non-existent-label").isPresent());
    }

    @Test
    public void testListImageStore() {
        ImageStore store = createImageStore("mystore", user);
        List<ImageStore> list = ImageStoreFactory.listImageStores(user.getOrg());

        assertEquals(2, list.size());
        // Permanent OS Image store should always be present
        assertTrue(list.stream().filter(s -> s.getStoreType().equals(ImageStoreFactory.TYPE_OS_IMAGE)).findFirst()
                .isPresent());
        assertTrue(list.stream().filter(s -> s.equals(store)).findFirst().isPresent());
        assertNull(list.stream().filter(s -> s.equals(store)).findFirst().get().getCreds());

        RegistryCredentials creds = createCredentials();
        ImageStore storeWithCreds = createImageStore("mystorewithcreds", creds, user);

        list = ImageStoreFactory.listImageStores(user.getOrg());
        assertEquals(3, list.size());
        ImageStore resultStore = list.stream().filter(s -> s.equals(storeWithCreds)).findFirst().get();
        assertEquals(creds, resultStore.getCreds());
    }

    @Test
    public void testListByTypeLabelAndOrg() throws Exception {
        RegistryCredentials creds = createCredentials();
        ImageStore store = createImageStore("mystore", creds, user);

        assertEquals(ImageStoreFactory.TYPE_REGISTRY, store.getStoreType());
        List<ImageStore> iList =
                ImageStoreFactory.listByTypeLabelAndOrg(ImageStoreFactory.TYPE_REGISTRY.getLabel(), user.getOrg());

        assertEquals(1, iList.size());
        assertEquals(store, iList.get(0));
        assertEquals(creds, iList.get(0).getCreds());

        iList = ImageStoreFactory.listByTypeLabelAndOrg(ImageStoreFactory.TYPE_OS_IMAGE.getLabel(), user.getOrg());
        assertEquals(1, iList.size());

        iList = ImageStoreFactory.listByTypeLabelAndOrg("non-existent-type", user.getOrg());
        assertEquals(0, iList.size());


        Org org = UserTestUtils.createNewOrgFull("foreign org");

        iList = ImageStoreFactory.listByTypeLabelAndOrg(ImageStoreFactory.TYPE_REGISTRY.getLabel(), org);
        assertEquals(0, iList.size());

        iList = ImageStoreFactory.listByTypeLabelAndOrg(ImageStoreFactory.TYPE_OS_IMAGE.getLabel(), org);
        assertEquals(1, iList.size());
    }

    @Test
    public void testLookupById() {
        ImageStore store = createImageStore("mystore", user);

        assertNotNull(store.getId());

        Optional<ImageStore> lookup = ImageStoreFactory.lookupById(store.getId());
        assertTrue(lookup.isPresent());
        assertEquals(store, lookup.get());

        lookup = ImageStoreFactory.lookupById(-1);
        assertFalse(lookup.isPresent());
    }

    @Test
    public void testLookupByIdAndOrg() {
        ImageStore store = createImageStore("mystore", user);

        assertNotNull(store.getId());

        Optional<ImageStore> lookup =
                ImageStoreFactory.lookupByIdAndOrg(store.getId(), user.getOrg());
        assertTrue(lookup.isPresent());
        assertEquals(store, lookup.get());

        Org org = OrgFactory.createOrg();
        org.setName("foreign org");
        org = OrgFactory.save(org);

        lookup = ImageStoreFactory.lookupByIdAndOrg(store.getId(), org);
        assertFalse(lookup.isPresent());
    }

    @Test
    public void testLookupByIdsAndOrg() {
        ImageStore store1 = createImageStore("mystore1", user);
        ImageStore store2 = createImageStore("mystore2", user);
        ImageStore store3 = createImageStore("mystore3", user);

        List<Long> ids = new ArrayList<>();
        ids.add(store1.getId());
        ids.add(store2.getId());

        List<ImageStore> lookup =
                ImageStoreFactory.lookupByIdsAndOrg(ids, user.getOrg());
        assertEquals(2, lookup.size());
        assertTrue(lookup.stream().filter(store1::equals).findFirst().isPresent());
        assertTrue(lookup.stream().filter(store2::equals).findFirst().isPresent());
        assertFalse(lookup.stream().filter(store3::equals).findFirst().isPresent());

        Org org = OrgFactory.createOrg();
        org.setName("foreign org");
        org = OrgFactory.save(org);

        lookup = ImageStoreFactory.lookupByIdsAndOrg(ids, org);
        assertEquals(0, lookup.size());

        ids.clear();
        ids.add(store1.getId());
        ids.add(100L);
        assertFalse(ImageStoreFactory.lookupById(100L).isPresent());
        lookup = ImageStoreFactory.lookupByIdsAndOrg(ids, user.getOrg());
        assertEquals(1, lookup.size());
        assertEquals(store1, lookup.get(0));
    }

    @Test
    public void testLookupImageStore() {
        ImageStore store = createImageStore("mystore", user);

        ImageStore i =
                ImageStoreFactory.lookupBylabelAndOrg("mystore", user.getOrg()).get();

        assertEquals(store, i);
        assertNull(i.getCreds());

        Org org = OrgFactory.createOrg();
        org.setName("foreign org");
        org = OrgFactory.save(org);

        assertFalse(ImageStoreFactory.lookupBylabelAndOrg("myregistry", org).isPresent());
    }

    @Test
    public void testDelete() {
        ImageStore store = createImageStore("mystore", user);
        assertTrue(ImageStoreFactory.lookupBylabelAndOrg("mystore", user.getOrg())
                .isPresent());

        ImageStore i =
                ImageStoreFactory.lookupBylabelAndOrg("mystore", user.getOrg()).get();

        ImageStoreFactory.delete(i);
        assertFalse(ImageStoreFactory.lookupBylabelAndOrg("mystore", user.getOrg())
                .isPresent());

        // Should not be able to delete the permanent OS Image store
        try {
            ImageStoreFactory
                    .delete(ImageStoreFactory.lookupBylabelAndOrg("SUSE Manager OS Image Store", user.getOrg()).get());
            fail("Deleting OS Image store should not be allowed.");
        }
        catch (IllegalArgumentException e) {
            assertEquals("Cannot delete permanent OS Image store", e.getMessage());
        }
    }

}
