package com.redhat.rhn.domain.server.virtualhostmanager;

import com.redhat.rhn.testing.BaseTestCaseWithUser;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.PropertyValueException;

import java.util.HashMap;
import java.util.Map;

/**
 * VirtualHostManagerFactory Test
 */
public class VirtualHostManagerFactoryTest extends BaseTestCaseWithUser {

    private VirtualHostManagerFactory factory;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        factory = VirtualHostManagerFactory.getInstance();
    }

    /**
     * Tests creating and retrieving a VirtualHostManager.
     * @throws Exception if anything goes wrong
     */
    public void testCreateAndGetVHM() throws Exception {
        factory.createVirtualHostManager("mylabel", user.getOrg(), "SUSECloud", null);
        VirtualHostManager fromDb = factory.lookupByLabel("mylabel");

        assertEquals("mylabel", fromDb.getLabel());
        assertEquals(user.getOrg(), fromDb.getOrg());
        assertEquals("SUSECloud", fromDb.getGathererModule());
        assertNull(fromDb.getConfigs());
    }

    /**
     * Tests creating and retrieving a VirtualHostManager with credentials.
     * @throws Exception if anything goes wrong
     */
    public void testCreateAndGetVHManagerWithCreds() throws Exception {
        Map<String, String> config = new HashMap<>();
        config.put("user", "FlashGordon");
        config.put("pass", "The savior of the universe");

        factory.createVirtualHostManager("mylabel", user.getOrg(), "SUSECloud", config);
        VirtualHostManager virtualHostManager = factory.lookupByLabel("mylabel");

        assertEquals("FlashGordon", virtualHostManager.getCredentials().getUsername());
        assertEquals("The savior of the universe", virtualHostManager.getCredentials().getPassword());
        // user and pass should be deleted from configs
        assertTrue(virtualHostManager.getConfigs().isEmpty());
    }

    /**
     * Tests creating and retrieving a VirtualHostManager with config.
     * @throws Exception if anything goes wrong
     */
    public void testCreateAndGetVHMWithConfigs() throws Exception {
        Map<String, String> config = new HashMap<>();
        config.put("testkey", "43");

        factory.createVirtualHostManager("mylabel", user.getOrg(), "SUSECloud", config);
        VirtualHostManager virtualHostManager = factory.lookupByLabel("mylabel");

        assertEquals(1, virtualHostManager.getConfigs().size());
        assertEquals("43", virtualHostManager.getConfigs().iterator().next().getValue());
    }

    /**
     * Tests creating and retrieving a VirtualHostManager with null label.
     * @throws Exception if anything goes wrong
     */
    public void testCreateAndGetVHMNullLabel() {
        try {
            factory.createVirtualHostManager(null, user.getOrg(), "SUSECloud", null);
        } catch (PropertyValueException e) {
            return; // we've caught exception about violating not-null constraint
        }
        fail("PQLException should have been thrown.");
    }

    /**
     * Tests creating and retrieving a VirtualHostManager with null organization.
     * @throws Exception if anything goes wrong
     */
    public void testCreateAndGetVHMNullOrg() {
        try {
            factory.createVirtualHostManager("mylabel", null, "SUSECloud", null);
        } catch (PropertyValueException e) {
            return; // we've caught exception about violating not-null constraint
        }
        fail("PQLException should have been thrown.");
    }

    /**
     * Tests retrieving non-existing Virtual Host Manager.
     * @throws Exception if anything goes wrong
     */
    public void testCreateAndGetNonExistentVHM() {
        try {
            factory.lookupByLabel("idontexist");
        } catch (ObjectNotFoundException e) {
            return; // we've caught exception about non existing object
        }
        fail("ObjectNotFoundException should have been thrown.");
    }

    public void testDeleteVirtualHostManager() {
        Map<String, String> config = new HashMap<>();
        config.put("testkey", "43");
        String myLabel = "myLabel";
        VirtualHostManager vhm = factory
                .createVirtualHostManager(myLabel, user.getOrg(), "SUSECloud", config);
        assertNotEmpty(factory.lookupByLabel(myLabel).getConfigs());
        assertNotNull(factory.lookupByLabel(myLabel));

        factory.delete(vhm);

        try {
            factory.lookupByLabel(myLabel);
        } catch (ObjectNotFoundException e) {
            return; // we've caught exception about non existing object
        }
        fail("ObjectNotFoundException should have been thrown.");
    }
}
