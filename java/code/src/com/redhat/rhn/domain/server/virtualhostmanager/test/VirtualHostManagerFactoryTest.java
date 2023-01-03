/*
 * Copyright (c) 2015 SUSE LLC
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

package com.redhat.rhn.domain.server.virtualhostmanager.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManager;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManagerFactory;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ServerTestUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * VirtualHostManagerFactory Test
 */
public class VirtualHostManagerFactoryTest extends BaseTestCaseWithUser {

    private VirtualHostManagerFactory factory;
    private static final String SUSE_CLOUD = "SUSECloud";

    /**
     * {@inheritDoc}
     */
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        factory = VirtualHostManagerFactory.getInstance();
    }

    /**
     * Tests creating and retrieving a VirtualHostManager.
     */
    @Test
    public void testCreateAndGetVHM() {
        Map<String, String> config = new HashMap<>();
        config.put("username", "FlashGordon");
        config.put("password", "The savior of the universe");

        createAndSaveVirtualHostManager("mylabel", user.getOrg(), SUSE_CLOUD,
                config);
        VirtualHostManager fromDb = factory.lookupByLabel("mylabel");

        assertEquals("mylabel", fromDb.getLabel());
        assertEquals(user.getOrg(), fromDb.getOrg());
        assertEquals(SUSE_CLOUD, fromDb.getGathererModule());
        assertTrue(fromDb.getConfigs().isEmpty());
    }

    /**
     * Tests creating and retrieving a VirtualHostManager with credentials.
     */
    @Test
    public void testCreateAndGetVHManagerWithCreds() {
        Map<String, String> config = new HashMap<>();
        config.put("username", "FlashGordon");
        config.put("password", "The savior of the universe");

        createAndSaveVirtualHostManager("mylabel", user.getOrg(), SUSE_CLOUD, config);
        VirtualHostManager virtualHostManager = factory.lookupByLabel("mylabel");

        assertEquals("FlashGordon", virtualHostManager.getCredentials().getUsername());
        assertEquals("The savior of the universe",
                virtualHostManager.getCredentials().getPassword());
        // user and pass should be deleted from configs
        assertTrue(virtualHostManager.getConfigs().isEmpty());
    }

    @Test
    public void testUpdateVHM() {
        Map<String, String> config = new HashMap<>();
        config.put("username", "FlashGordon");
        config.put("password", "The savior of the universe");
        config.put("param1", "oldparam1");

        createAndSaveVirtualHostManager("mylabel", user.getOrg(), SUSE_CLOUD, config);
        VirtualHostManager vhm = factory.lookupByLabel("mylabel");

        Map<String, String> updConfig = new HashMap<>();
        updConfig.put("username", "TheUpdatedFlashGordon");
        updConfig.put("param1", "newparam1");
        updConfig.put("param2", "newparam2");
        VirtualHostManagerFactory.getInstance().updateVirtualHostManager(vhm, "mynewlabel",
                updConfig);

        vhm = factory.lookupByLabel("mynewlabel");
        assertNotNull(vhm);
        assertEquals("TheUpdatedFlashGordon", vhm.getCredentials().getUsername());

        assertEquals(2, vhm.getConfigs().size());
        assertTrue(vhm.getConfigs().stream()
                .anyMatch(c -> "param1".equals(c.getParameter()) &&
                        "newparam1".equals(c.getValue())));
        assertTrue(vhm.getConfigs().stream()
                .anyMatch(c -> "param2".equals(c.getParameter()) &&
                        "newparam2".equals(c.getValue())));
    }

    /**
     * Tests creating and retrieving a VirtualHostManager with config.
     */
    @Test
    public void testCreateAndGetVHMWithConfigs() {
        Map<String, String> config = new HashMap<>();
        config.put("username", "FlashGordon");
        config.put("password", "The savior of the universe");
        config.put("testkey", "43");

        createAndSaveVirtualHostManager("mylabel", user.getOrg(), SUSE_CLOUD, config);
        VirtualHostManager virtualHostManager = factory.lookupByLabel("mylabel");

        assertEquals(1, virtualHostManager.getConfigs().size());
        assertEquals("43", virtualHostManager.getConfigs().iterator().next().getValue());
    }

    /**
     * Tests that creating a VirtualHostManager with null label will throw an
     * IllegalArgumentException.
     */
    @Test
    public void testCreateAndGetVHMNullLabel() {
        try {
            createAndSaveVirtualHostManager("test", null, SUSE_CLOUD,
                    Collections.emptyMap());
        }
        catch (Exception e) {
            return;
        }
        fail("IllegalArgumentException should have been thrown.");
    }

    /**
     * Tests retrieving non-existing Virtual Host Manager.
     */
    @Test
    public void testCreateAndGetNonExistentVHM() {
        assertNull(factory.lookupByLabel("idontexist"));
    }

    /**
     * Tests deleting an existing Virtual Host Manager.
     */
    @Test
    public void testDeleteVirtualHostManager() {
        Map<String, String> config = new HashMap<>();
        config.put("username", "FlashGordon");
        config.put("password", "The savior of the universe");
        config.put("testkey", "43");
        String myLabel = "myLabel";
        VirtualHostManager vhm = createAndSaveVirtualHostManager(myLabel, user.getOrg(),
                SUSE_CLOUD, config);
        assertNotEmpty(factory.lookupByLabel(myLabel).getConfigs());
        assertNotNull(factory.lookupByLabel(myLabel));

        factory.delete(vhm);
        assertNull(factory.lookupByLabel(myLabel));
    }

    /**
     * Tests that the deleting an existing Virtual Host Manager doesn't cascade to server.
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testDeleteVirtualHostManagerDontCascade() throws Exception {
        Server server = ServerTestUtils.createForeignSystem(user, "server_digital_id");
        Long serverId = server.getId();

        Map<String, String> config = new HashMap<>();
        config.put("username", "FlashGordon");
        config.put("password", "The savior of the universe");

        VirtualHostManager vhm = createAndSaveVirtualHostManager("myLabel", user.getOrg(),
                SUSE_CLOUD, config);

        vhm.addServer(server);
        HibernateFactory.getSession().flush();

        factory.delete(vhm);
        assertNotNull(ServerFactory.lookupById(serverId));
    }

    /**
     * Tests creating and retrieving a list of VirtualHostManager an organization.
     */
    @Test
    public void testCreateAndGetVHMs() {
        Map<String, String> config = new HashMap<>();
        config.put("username", "FlashGordon");
        config.put("password", "The savior of the universe");

        VirtualHostManager manager1 = createAndSaveVirtualHostManager("mylabel",
                user.getOrg(), SUSE_CLOUD, config);
        VirtualHostManager manager2 = createAndSaveVirtualHostManager("mylabel2",
                user.getOrg(), SUSE_CLOUD, config);

        List<VirtualHostManager> managers = factory.listVirtualHostManagers(user.getOrg());

        assertEquals(2, managers.size());
        assertContains(managers, manager1);
        assertContains(managers, manager2);
    }

    /**
     * Tests that after removing VirtualHostManager, its credentials are removed as well.
     */
    @Test
    public void testDeleteVHMAndCredentials() {
        Map<String, String> config = new HashMap<>();
        config.put("username", "foouser");
        config.put("password", "barpass");

        VirtualHostManager manager = createAndSaveVirtualHostManager("label",
                user.getOrg(), SUSE_CLOUD, config);

        Long id = manager.getCredentials().getId();
        assertNotNull(CredentialsFactory.lookupCredentialsById(id));
        factory.delete(manager);
        assertNull(CredentialsFactory.lookupCredentialsById(id));
    }

    /**
     * Tests that createVirtualHostManager throws NullPointerException when
     * passing null params.
     */
    @Test
    public void testFailOnNullParameters() {
        try {
            // should throw a NullPointerException
            createAndSaveVirtualHostManager("mylabel", user.getOrg(), SUSE_CLOUD, null);
        }
        catch (NullPointerException e) {
            return;
        }
        fail();
    }

    /**
     * Creates and saves a Virtual Host Manager.
     *
     * @param label the label
     * @param org the org
     * @param module the module
     * @param parameters the parameters
     * @return the virtual host manager
     */
    private VirtualHostManager createAndSaveVirtualHostManager(String label, Org org,
            String module, Map<String, String> parameters) {
        VirtualHostManager vhm =
                factory.createVirtualHostManager(label, org, module, parameters);
        factory.save(vhm);
        return vhm;
    }
}
