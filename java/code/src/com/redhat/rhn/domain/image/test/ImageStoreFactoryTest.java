package com.redhat.rhn.domain.image.test;

import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.credentials.DockerCredentials;
import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.image.ImageStoreFactory;
import com.redhat.rhn.domain.image.ImageStoreType;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import javax.persistence.NoResultException;
import java.util.List;


public class ImageStoreFactoryTest extends BaseTestCaseWithUser {

    private static final String STORE_TYPE_DOCKER = "dockerreg";

    public void testLookupStoreType() throws Exception {
        ImageStoreType expected = new ImageStoreType();
        expected.setLabel("dockerreg");
        expected.setName("Docker Registry");

        ImageStoreType stype = ImageStoreFactory.lookupStoreTypeByLabel(STORE_TYPE_DOCKER);
        assertEquals(expected, stype);
    }

    public void testListImageStore() throws Exception {
        ImageStore iStore = new ImageStore();
        iStore.setLabel("myregistry");
        iStore.setUri("registry.domain.top");
        iStore.setStoreType(ImageStoreFactory.lookupStoreTypeByLabel(STORE_TYPE_DOCKER));
        iStore.setOrg(user.getOrg());
        ImageStoreFactory.save(iStore);

        List<ImageStore> iList = ImageStoreFactory.listImageStores(user.getOrg());
        assertNotEmpty(iList);

        for (ImageStore i : iList) {
            if (i.getLabel().equals("myregistry")) {
                assertEquals(iStore, i);
                assertNull(i.getCreds());
            }
            else {
                assertTrue("myregistry not found", false);
            }
        }
    }

    public void testLookupImageStore() throws Exception {
        ImageStore iStore = new ImageStore();
        iStore.setLabel("myregistry");
        iStore.setUri("registry.domain.top");
        iStore.setStoreType(ImageStoreFactory.lookupStoreTypeByLabel(STORE_TYPE_DOCKER));
        iStore.setOrg(user.getOrg());
        ImageStoreFactory.save(iStore);

        ImageStore i = ImageStoreFactory.lookupBylabelAndOrg("myregistry", user.getOrg());

        if (i.getLabel().equals("myregistry")) {
            assertEquals(iStore, i);
            assertNull(i.getCreds());
        }
        else {
            assertTrue("myregistry not found", false);
        }
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

        if (i.getLabel().equals("myregistry")) {
            assertEquals(iStore, i);
            if(i.getCreds().asDockerCredentials().isPresent()) {
                assertEquals(creds, i.getCreds().asDockerCredentials().get());
            }
            else {
                assertTrue("Credentials is not a Docker Credentials object", false);
            }
        }
        else {
            assertTrue("myregistry not found", false);
        }
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
        }
        catch (NoResultException ignored) { }
    }
}
