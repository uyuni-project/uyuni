package com.redhat.rhn.domain.image.test;

import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.credentials.DockerCredentials;
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

    private static final String STORE_TYPE_DOCKER = "dockerreg";

    public void testLookupStoreType() throws Exception {
        ImageStoreType expected = new ImageStoreType();
        expected.setLabel("dockerreg");
        expected.setName("Docker Registry");

        ImageStoreType stype = ImageStoreFactory.lookupStoreTypeByLabel(STORE_TYPE_DOCKER);
        assertEquals(expected, stype);

        try {
            ImageStoreFactory.lookupStoreTypeByLabel("non-existent-label");
            fail("Should throw NoResultException");
        }
        catch (NoResultException ignored) { }
    }

    public void testListImageStore() throws Exception {
        ImageStore iStore = new ImageStore();
        iStore.setLabel("myregistry");
        iStore.setUri("registry.domain.top");
        iStore.setStoreType(ImageStoreFactory.lookupStoreTypeByLabel(STORE_TYPE_DOCKER));
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
        iStore.setStoreType(ImageStoreFactory.lookupStoreTypeByLabel(STORE_TYPE_DOCKER));
        iStore.setOrg(user.getOrg());
        ImageStoreFactory.save(iStore);

        List<ImageStore> iList =
                ImageStoreFactory.listByTypeLabelAndOrg(STORE_TYPE_DOCKER, user.getOrg());

        assertEquals(1, iList.size());
        assertEquals(iStore, iList.get(0));
        assertNull(iList.get(0).getCreds());

        iList = ImageStoreFactory.listByTypeLabelAndOrg("non-existent-type", user.getOrg());
        assertEquals(0, iList.size());

        Org org = OrgFactory.createOrg();
        org.setName("foreign org");
        org = OrgFactory.save(org);

        iList = ImageStoreFactory.listByTypeLabelAndOrg(STORE_TYPE_DOCKER, org);
        assertEquals(0, iList.size());
    }

    public void testLookupById() throws Exception {
        ImageStore iStore = new ImageStore();
        iStore.setLabel("myregistry");
        iStore.setUri("registry.domain.top");
        iStore.setStoreType(ImageStoreFactory.lookupStoreTypeByLabel(STORE_TYPE_DOCKER));
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
        iStore.setStoreType(ImageStoreFactory.lookupStoreTypeByLabel(STORE_TYPE_DOCKER));
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
        iStore.setStoreType(ImageStoreFactory.lookupStoreTypeByLabel(STORE_TYPE_DOCKER));
        iStore.setOrg(user.getOrg());
        ImageStoreFactory.save(iStore);

        ImageStore i = ImageStoreFactory.lookupBylabelAndOrg("myregistry", user.getOrg());

        assertEquals(iStore, i);
        assertNull(i.getCreds());

        Org org = OrgFactory.createOrg();
        org.setName("foreign org");
        org = OrgFactory.save(org);

        try {
            ImageStoreFactory.lookupBylabelAndOrg("myregistry", org);
            fail("Should throw NoResultException");
        }
        catch (NoResultException ignored) { }
    }

    public void testLookupImageStoreWithCredentials() throws Exception {
        DockerCredentials creds = CredentialsFactory.createDockerCredentials();
        creds.setEmail("dockeradmin@example.com");
        creds.setUsername("admin");
        creds.setPassword("secret");

        ImageStore iStore = new ImageStore();
        iStore.setLabel("myregistry");
        iStore.setUri("registry.domain.top");
        iStore.setStoreType(ImageStoreFactory.lookupStoreTypeByLabel(STORE_TYPE_DOCKER));
        iStore.setOrg(user.getOrg());
        iStore.setCreds(creds);
        ImageStoreFactory.save(iStore);

        ImageStore i = ImageStoreFactory.lookupBylabelAndOrg("myregistry", user.getOrg());

        assertEquals(iStore, i);
        assertTrue(i.getCreds().asDockerCredentials().isPresent());
    }

    public void testDelete() {
        ImageStore store = new ImageStore();

        store.setLabel("myregistry");
        store.setUri("registry.domain.top");
        store.setStoreType(ImageStoreFactory.lookupStoreTypeByLabel(STORE_TYPE_DOCKER));
        store.setOrg(user.getOrg());
        ImageStoreFactory.save(store);

        ImageStore i = ImageStoreFactory.lookupBylabelAndOrg("myregistry", user.getOrg());

        ImageStoreFactory.delete(i);
        try {
            ImageStoreFactory.lookupBylabelAndOrg("myregistry", user.getOrg());
            fail("Should throw NoResultException");
        }
        catch (NoResultException ignored) { }
    }
}
