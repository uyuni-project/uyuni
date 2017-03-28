package com.redhat.rhn.domain.image.test;

import com.redhat.rhn.domain.credentials.Credentials;
import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.image.ImageStoreFactory;
import com.redhat.rhn.domain.image.ImageStoreType;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import java.util.List;
import java.util.Optional;

import static com.redhat.rhn.testing.ImageTestUtils.createCredentials;
import static com.redhat.rhn.testing.ImageTestUtils.createImageStore;

public class ImageStoreFactoryTest extends BaseTestCaseWithUser {

    public void testLookupStoreType() throws Exception {
        ImageStoreType expected = new ImageStoreType();
        expected.setLabel("registry");
        expected.setName("Registry");

        ImageStoreType stype =
                ImageStoreFactory.lookupStoreTypeByLabel(ImageStore.TYPE_REGISTRY).get();
        assertEquals(expected, stype);

        assertFalse(
                ImageStoreFactory.lookupStoreTypeByLabel("non-existent-label").isPresent());
    }

    public void testListImageStore() throws Exception {
        ImageStore store = createImageStore("mystore", user);
        List<ImageStore> list = ImageStoreFactory.listImageStores(user.getOrg());

        assertEquals(1, list.size());
        assertEquals(store, list.get(0));
        assertNull(list.get(0).getCreds());
        
        Credentials creds = createCredentials();
        ImageStore storeWithCreds = createImageStore("mystorewithcreds", creds, user);
        
        list = ImageStoreFactory.listImageStores(user.getOrg());
        assertEquals(2, list.size());
        ImageStore resultStore =
                list.stream().filter(s -> s.equals(storeWithCreds)).findFirst().get();
        assertEquals(creds, resultStore.getCreds());
    }

    public void testListByTypeLabelAndOrg() throws Exception {
        Credentials creds = createCredentials();
        ImageStore store = createImageStore("mystore", creds, user);

        List<ImageStore> iList = ImageStoreFactory
                .listByTypeLabelAndOrg(ImageStore.TYPE_REGISTRY, user.getOrg());

        assertEquals(1, iList.size());
        assertEquals(store, iList.get(0));
        assertEquals(creds, iList.get(0).getCreds());

        iList = ImageStoreFactory.listByTypeLabelAndOrg("non-existent-type", user.getOrg());
        assertEquals(0, iList.size());

        Org org = OrgFactory.createOrg();
        org.setName("foreign org");
        org = OrgFactory.save(org);

        iList = ImageStoreFactory.listByTypeLabelAndOrg(ImageStore.TYPE_REGISTRY, org);
        assertEquals(0, iList.size());
    }

    public void testLookupById() throws Exception {
        ImageStore store = createImageStore("mystore", user);

        assertNotNull(store.getId());

        Optional<ImageStore> lookup = ImageStoreFactory.lookupById(store.getId());
        assertTrue(lookup.isPresent());
        assertEquals(store, lookup.get());

        lookup = ImageStoreFactory.lookupById(-1);
        assertFalse(lookup.isPresent());
    }

    public void testLookupByIdAndOrg() throws Exception {
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

    public void testLookupImageStore() throws Exception {
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

    public void testDelete() {
        ImageStore store = createImageStore("mystore", user);
        assertTrue(ImageStoreFactory.lookupBylabelAndOrg("mystore", user.getOrg())
                .isPresent());

        ImageStore i =
                ImageStoreFactory.lookupBylabelAndOrg("mystore", user.getOrg()).get();

        ImageStoreFactory.delete(i);
        assertFalse(ImageStoreFactory.lookupBylabelAndOrg("mystore", user.getOrg())
                .isPresent());
    }

}
