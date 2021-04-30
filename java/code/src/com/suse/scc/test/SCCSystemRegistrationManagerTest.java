/**
 * Copyright (c) 2021 SUSE LLC
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
package com.suse.scc.test;

import com.redhat.rhn.domain.credentials.Credentials;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.scc.SCCCachingFactory;
import com.redhat.rhn.domain.scc.SCCRegCacheItem;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.testing.ServerTestUtils;
import com.suse.manager.webui.services.SaltStateGeneratorService;
import com.suse.scc.SCCSystemRegistrationManager;
import com.suse.scc.client.SCCClient;
import com.suse.scc.client.SCCClientException;
import com.suse.scc.client.SCCConfig;
import com.suse.scc.client.SCCWebClient;
import com.suse.scc.model.SCCRegisterSystemJson;
import com.suse.scc.model.SCCSystemCredentialsJson;
import junit.framework.TestCase;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tests for {@link SCCClient} methods.
 */
public class SCCSystemRegistrationManagerTest extends TestCase {

    public void testSCCSystemRegistrationLifecycle() throws Exception {
        Path tmpSaltRoot = Files.createTempDirectory("salt");
        SaltStateGeneratorService.INSTANCE.setSuseManagerStatesFilesRoot(tmpSaltRoot
                .toAbsolutePath());
        Server testSystem = ServerTestUtils.createTestSystem();
        SCCWebClient sccWebClient = new SCCWebClient(new SCCConfig(
                new URI("https://localhost"), "username", "password", "uuid")) {
            @Override
            public SCCSystemCredentialsJson createSystem(SCCRegisterSystemJson system, String username, String password)
                    throws SCCClientException {
                assertEquals("username", username);
                assertEquals("password", password);
                return new SCCSystemCredentialsJson(system.getLogin(), system.getPassword(), 12345L);
            }

            @Override
            public void deleteSystem(long id, String username, String password) throws SCCClientException {
                assertEquals(12345L, id);
                assertEquals("username", username);
                assertEquals("password", password);
            }
        };

        SCCSystemRegistrationManager sccSystemRegistrationManager = new SCCSystemRegistrationManager(sccWebClient);
        SCCCachingFactory.initNewSystemsToForward();
        List<SCCRegCacheItem> allUnregistered = SCCCachingFactory.findSystemsToForwardRegistration();
        List<SCCRegCacheItem> testSystems = allUnregistered.stream()
                .filter(i -> i.getOptServer().get().equals(testSystem))
                .collect(Collectors.toList());
        Credentials credentials = CredentialsFactory.createSCCCredentials();
        credentials.setUsername("username");
        credentials.setPassword("password");
        CredentialsFactory.storeCredentials(credentials);
        sccSystemRegistrationManager.register(testSystems, credentials);
        List<SCCRegCacheItem> afterRegistration = SCCCachingFactory.findSystemsToForwardRegistration();
        assertEquals(allUnregistered.size() - 1, afterRegistration.size());
        List<SCCRegCacheItem> sccRegCacheItems = SCCCachingFactory.listRegItemsByCredentials(credentials);
        assertEquals(1, sccRegCacheItems.size());

        SCCRegCacheItem sccRegCacheItem = sccRegCacheItems.get(0);
        assertEquals(testSystem, sccRegCacheItem.getOptServer().get());
        assertEquals(12345L, sccRegCacheItem.getOptSccId().get().longValue());
        assertTrue(sccRegCacheItem.getOptSccLogin().isPresent());
        assertTrue(sccRegCacheItem.getOptSccPasswd().isPresent());
        assertTrue(sccRegCacheItem.getOptRegistrationErrorTime().isEmpty());
        assertEquals(credentials, sccRegCacheItem.getOptCredentials().get());


        // Now delete and test deregistration
        List<SCCRegCacheItem> itemsBeforeDelete = SCCCachingFactory.listDeregisterItems();
        assertEquals(0, itemsBeforeDelete.size());
        ServerFactory.delete(testSystem);
        List<SCCRegCacheItem> itemsAfterDelete = SCCCachingFactory.listDeregisterItems();
        assertEquals(1, itemsAfterDelete.size());

        SCCRegCacheItem deregisterItem = itemsAfterDelete.get(0);
        assertTrue(deregisterItem.getOptServer().isEmpty());
        assertEquals(12345L, deregisterItem.getOptSccId().get().longValue());
        assertTrue(sccRegCacheItem.getOptSccLogin().isPresent());
        assertTrue(sccRegCacheItem.getOptSccPasswd().isPresent());
        assertTrue(sccRegCacheItem.getOptRegistrationErrorTime().isEmpty());
        assertEquals(credentials, deregisterItem.getOptCredentials().get());

        //Ensure deregistration list is empty after deregistering elements
        sccSystemRegistrationManager.deregister(itemsAfterDelete, false);
        List<SCCRegCacheItem> itemsAfterDeregistration = SCCCachingFactory.listDeregisterItems();
        assertEquals(0, itemsAfterDeregistration.size());
    }

}
