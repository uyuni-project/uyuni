/**
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

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.virtualhostmanager.InvalidGathererConfigException;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManager;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManagerFactory;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ServerTestUtils;

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

    @Override
    public void setUp() throws Exception {
        super.setUp();
        factory = new VirtualHostManagerFactory() {
            @Override
            protected void validateGathererConfiguration(String moduleName,
                    Map<String, String> parameters) {
                // no op
            }
        };
    }

    /**
     * Tests creating and retrieving a VirtualHostManager.
     * @throws Exception if anything goes wrong
     */
    public void testCreateAndGetVHM() throws Exception {
        factory.createVirtualHostManager("mylabel", user.getOrg(), SUSE_CLOUD,
                Collections.emptyMap());
        VirtualHostManager fromDb = factory.lookupByLabel("mylabel");

        assertEquals("mylabel", fromDb.getLabel());
        assertEquals(user.getOrg(), fromDb.getOrg());
        assertEquals(SUSE_CLOUD, fromDb.getGathererModule());
        assertTrue(fromDb.getConfigs().isEmpty());
    }

    /**
     * Tests creating and retrieving a VirtualHostManager with credentials.
     * @throws Exception if anything goes wrong
     */
    public void testCreateAndGetVHManagerWithCreds() throws Exception {
        Map<String, String> config = new HashMap<>();
        config.put("user", "FlashGordon");
        config.put("pass", "The savior of the universe");

        factory.createVirtualHostManager("mylabel", user.getOrg(), SUSE_CLOUD, config);
        VirtualHostManager virtualHostManager = factory.lookupByLabel("mylabel");

        assertEquals("FlashGordon", virtualHostManager.getCredentials().getUsername());
        assertEquals("The savior of the universe",
                virtualHostManager.getCredentials().getPassword());
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

        factory.createVirtualHostManager("mylabel", user.getOrg(), SUSE_CLOUD, config);
        VirtualHostManager virtualHostManager = factory.lookupByLabel("mylabel");

        assertEquals(1, virtualHostManager.getConfigs().size());
        assertEquals("43", virtualHostManager.getConfigs().iterator().next().getValue());
    }

    /**
     * Tests that creating a VirtualHostManager with null label will throw an
     * IllegalArgumentException.
     * @throws Exception if anything goes wrong
     */
    public void testCreateAndGetVHMNullLabel() throws Exception {
        try {
            factory.createVirtualHostManager("test", null, SUSE_CLOUD,
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
    public void testCreateAndGetNonExistentVHM() {
        assertNull(factory.lookupByLabel("idontexist"));
    }

    /**
     * Tests deleting an existing Virtual Host Manager.
     * @throws Exception if anything goes wrong
     */
    public void testDeleteVirtualHostManager() throws Exception {
        Map<String, String> config = new HashMap<>();
        config.put("testkey", "43");
        String myLabel = "myLabel";
        VirtualHostManager vhm = factory.createVirtualHostManager(myLabel, user.getOrg(),
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
    public void testDeleteVirtualHostManagerDontCascade() throws Exception {
        Server server = ServerTestUtils.createForeignSystem(user, "server");
        Long serverId = server.getId();

        VirtualHostManager vhm = factory.createVirtualHostManager("myLabel", user.getOrg(),
                SUSE_CLOUD, Collections.emptyMap());

        vhm.addServer(server);
        HibernateFactory.getSession().flush();

        factory.delete(vhm);
        assertNotNull(ServerFactory.lookupById(serverId));
    }

    /**
     * Tests throwing the IllegalArgumentException creating Virtual Host Manager
     * with an invalid gatherer module
     * @throws Exception if anything goes wrong
     */
    public void testCreateVHMInvalidGathererConfig() {
        VirtualHostManagerFactory customFactory = new VirtualHostManagerFactory() {
            @Override
            protected void validateGathererConfiguration(String moduleName,
                    Map<String, String> parameters)
                    throws InvalidGathererConfigException {
                throw new InvalidGathererConfigException("Module 'foobar' not available");
            }
        };

        try {
            customFactory.createVirtualHostManager("myLabel", user.getOrg(), SUSE_CLOUD,
                    Collections.emptyMap());
        }
        catch (InvalidGathererConfigException e) {
            return; // exception must be thrown
        }
        fail("IllegalArgumentException should have been thrown.");
    }

    /**
     * Tests creating and retrieving a list of VirtualHostManager an organization.
     * @throws Exception if anything goes wrong
     */
    public void testCreateAndGetVHMs() throws Exception {
        VirtualHostManager manager1 = factory.createVirtualHostManager("mylabel",
                user.getOrg(), SUSE_CLOUD, Collections.emptyMap());
        VirtualHostManager manager2 = factory.createVirtualHostManager("mylabel2",
                user.getOrg(), SUSE_CLOUD, Collections.emptyMap());

        List<VirtualHostManager> managers = factory.listVirtualHostManagers(user.getOrg());

        assertEquals(2, managers.size());
        assertContains(managers, manager1);
        assertContains(managers, manager2);
    }

    /**
     * Tests that after removing VirtualHostManager, its credentials are removed as well.
     * @throws Exception if anything goes wrong
     */
    public void testDeleteVHMAndCredentials() throws Exception {
        Map<String, String> config = new HashMap<>();
        config.put("user", "foouser");
        config.put("pass", "barpass");

        VirtualHostManager manager = factory.createVirtualHostManager("label",
                user.getOrg(), SUSE_CLOUD, config);

        Long id = manager.getCredentials().getId();
        assertNotNull(CredentialsFactory.lookupCredentialsById(id));
        factory.delete(manager);
        assertNull(CredentialsFactory.lookupCredentialsById(id));
    }

    /**
     * Tests that createVirtualHostManager throws NullPointerException when
     * passing null params.
     * @throws Exception - if anything goes wrong
     */
    public void testFailOnNullParameters() throws Exception {
        try {
            // should throw a NullPointerException
            factory.createVirtualHostManager("mylabel", user.getOrg(), SUSE_CLOUD, null);
        }
        catch (NullPointerException e) {
            return;
        }
        fail();
    }
}
