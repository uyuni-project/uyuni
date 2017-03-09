package com.redhat.rhn.domain.image.test;

import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.credentials.Credentials;
import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.image.ImageStoreFactory;
import com.redhat.rhn.domain.image.ImageStoreType;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import javax.persistence.NoResultException;
import java.util.List;
import java.util.Optional;

public class ImageStoreFactoryTest extends BaseTestCaseWithUser {

    public void testLookupStoreType() throws Exception {
        ImageStoreType expected = new ImageStoreType();
        expected.setLabel("registry");
        expected.setName("Registry");

        ImageStoreType stype =
                ImageStoreFactory.lookupStoreTypeByLabel(ImageStore.TYPE_REGISTRY).get();
        assertEquals(expected, stype);

        if (ImageStoreFactory.lookupStoreTypeByLabel("non-existent-label").isPresent()) {
            fail("Should not be present");
        }
    }

    public void testListImageStore() throws Exception {
        ImageStore iStore = new ImageStore();
        iStore.setLabel("myregistry");
        iStore.setUri("registry.domain.top");
        iStore.setStoreType(
                ImageStoreFactory.lookupStoreTypeByLabel(ImageStore.TYPE_REGISTRY).get());
        iStore.setOrg(user.getOrg());
        ImageStoreFactory.save(iStore);

        List<ImageStore> iList = ImageStoreFactory.listImageStores(user.getOrg());

        assertEquals(1, iList.size());
        assertEquals(iStore, iList.get(0));
        assertNull(iList.get(0).getCreds());
    }

    public void testListByTypeLabelAndOrg() throws Exception {
        ImageStore iStore = new ImageStore();
        iStore.setLabel("myregistry");
        iStore.setUri("registry.domain.top");
        iStore.setStoreType(
                ImageStoreFactory.lookupStoreTypeByLabel(ImageStore.TYPE_REGISTRY).get());
        iStore.setOrg(user.getOrg());
        ImageStoreFactory.save(iStore);

        List<ImageStore> iList =
                ImageStoreFactory.listByTypeLabelAndOrg(ImageStore.TYPE_REGISTRY, user.getOrg());

        assertEquals(1, iList.size());
        assertEquals(iStore, iList.get(0));
        assertNull(iList.get(0).getCreds());

        iList = ImageStoreFactory.listByTypeLabelAndOrg("non-existent-type", user.getOrg());
        assertEquals(0, iList.size());

        Org org = OrgFactory.createOrg();
        org.setName("foreign org");
        org = OrgFactory.save(org);

        iList = ImageStoreFactory.listByTypeLabelAndOrg(ImageStore.TYPE_REGISTRY, org);
        assertEquals(0, iList.size());
    }

    public void testLookupById() throws Exception {
        ImageStore iStore = new ImageStore();
        iStore.setLabel("myregistry");
        iStore.setUri("registry.domain.top");
        iStore.setStoreType(
                ImageStoreFactory.lookupStoreTypeByLabel(ImageStore.TYPE_REGISTRY).get());
        iStore.setOrg(user.getOrg());
        ImageStoreFactory.save(iStore);

        assertNotNull(iStore.getId());

        Optional<ImageStore> lookup = ImageStoreFactory.lookupById(iStore.getId());
        assertTrue(lookup.isPresent());
        assertEquals(iStore, lookup.get());

        lookup = ImageStoreFactory.lookupById(-1);
        assertFalse(lookup.isPresent());
    }

    public void testLookupByIdAndOrg() throws Exception {
        ImageStore iStore = new ImageStore();
        iStore.setLabel("myregistry");
        iStore.setUri("registry.domain.top");
        iStore.setStoreType(
                ImageStoreFactory.lookupStoreTypeByLabel(ImageStore.TYPE_REGISTRY).get());
        iStore.setOrg(user.getOrg());
        ImageStoreFactory.save(iStore);

        assertNotNull(iStore.getId());

        Optional<ImageStore> lookup =
                ImageStoreFactory.lookupByIdAndOrg(iStore.getId(), user.getOrg());
        assertTrue(lookup.isPresent());
        assertEquals(iStore, lookup.get());

        Org org = OrgFactory.createOrg();
        org.setName("foreign org");
        org = OrgFactory.save(org);

        lookup = ImageStoreFactory.lookupByIdAndOrg(iStore.getId(), org);
        assertFalse(lookup.isPresent());
    }

    public void testLookupImageStore() throws Exception {
        ImageStore iStore = new ImageStore();
        iStore.setLabel("myregistry");
        iStore.setUri("registry.domain.top");
        iStore.setStoreType(
                ImageStoreFactory.lookupStoreTypeByLabel(ImageStore.TYPE_REGISTRY).get());
        iStore.setOrg(user.getOrg());
        ImageStoreFactory.save(iStore);

        ImageStore i =
                ImageStoreFactory.lookupBylabelAndOrg("myregistry", user.getOrg()).get();

        assertEquals(iStore, i);
        assertNull(i.getCreds());

        Org org = OrgFactory.createOrg();
        org.setName("foreign org");
        org = OrgFactory.save(org);

        if (ImageStoreFactory.lookupBylabelAndOrg("myregistry", org).isPresent()) {
            fail("Should not be present");
        }
    }

    public void testLookupImageStoreWithCredentials() throws Exception {
        Credentials creds = CredentialsFactory.createRegistryCredentials();
        creds.setUsername("admin");
        creds.setPassword("secret");
        CredentialsFactory.storeCredentials(creds);

        ImageStore iStore = new ImageStore();
        iStore.setLabel("myregistry");
        iStore.setUri("registry.domain.top");
        iStore.setStoreType(
                ImageStoreFactory.lookupStoreTypeByLabel(ImageStore.TYPE_REGISTRY).get());
        iStore.setOrg(user.getOrg());
        iStore.setCreds(creds);
        ImageStoreFactory.save(iStore);

        ImageStore i =
                ImageStoreFactory.lookupBylabelAndOrg("myregistry", user.getOrg()).get();

        assertEquals(iStore, i);
        assertEquals(i.getCreds(), creds);
    }

    public void testDelete() {
        ImageStore store = new ImageStore();

        store.setLabel("myregistry");
        store.setUri("registry.domain.top");
        store.setStoreType(
                ImageStoreFactory.lookupStoreTypeByLabel(ImageStore.TYPE_REGISTRY).get());
        store.setOrg(user.getOrg());
        ImageStoreFactory.save(store);

        ImageStore i =
                ImageStoreFactory.lookupBylabelAndOrg("myregistry", user.getOrg()).get();

        ImageStoreFactory.delete(i);
        if (ImageStoreFactory.lookupBylabelAndOrg("myregistry",
                user.getOrg()).isPresent()) {
            fail("Should not be present anymore");
        }
    }
}
