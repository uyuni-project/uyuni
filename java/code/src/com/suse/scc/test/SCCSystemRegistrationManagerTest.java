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

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.credentials.Credentials;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.scc.SCCCachingFactory;
import com.redhat.rhn.domain.scc.SCCRegCacheItem;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerInfo;
import com.redhat.rhn.testing.ServerTestUtils;
import com.suse.manager.webui.services.SaltStateGeneratorService;
import com.suse.scc.SCCSystemRegistrationManager;
import com.suse.scc.client.SCCClient;
import com.suse.scc.client.SCCClientException;
import com.suse.scc.client.SCCConfig;
import com.suse.scc.client.SCCWebClient;
import com.suse.scc.model.SCCRegisterSystemJson;
import com.suse.scc.model.SCCSystemCredentialsJson;
import com.suse.scc.model.SCCUpdateSystemJson;
import junit.framework.TestCase;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
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
        SaltStateGeneratorService.INSTANCE.setSkipSetOwner(true);
        Server testSystem = ServerTestUtils.createTestSystem();
        ServerInfo serverInfo = testSystem.getServerInfo();
        serverInfo.setCheckin(new Date(0)); // 1970-01-01 00:00:00 UTC
        testSystem.setServerInfo(serverInfo);

        SCCWebClient sccWebClient = new SCCWebClient(new SCCConfig(
                new URI("https://localhost"), "username", "password", "uuid")) {
            @Override
            public SCCSystemCredentialsJson createSystem(SCCRegisterSystemJson system, String username, String password)
                    throws SCCClientException {
                assertEquals("username", username);
                assertEquals("password", password);
                assertEquals(new Date(0), system.getLastSeenAt());
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
        credentials.setUrl("https://scc.suse.com");
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

    public void testUpdateSystems() throws Exception {
        Path tmpSaltRoot = Files.createTempDirectory("salt");
        SaltStateGeneratorService.INSTANCE.setSuseManagerStatesFilesRoot(tmpSaltRoot
                .toAbsolutePath());
        SaltStateGeneratorService.INSTANCE.setSkipSetOwner(true);
        Server testSystem = ServerTestUtils.createTestSystem();
        ServerInfo serverInfo = testSystem.getServerInfo();
        serverInfo.setCheckin(new Date(0)); // 1970-01-01 00:00:00 UTC
        testSystem.setServerInfo(serverInfo);

        SCCWebClient sccWebClient = new SCCWebClient(new SCCConfig(
                new URI("https://localhost"), "username", "password", "uuid")) {
            @Override
            public SCCSystemCredentialsJson createSystem(SCCRegisterSystemJson system, String username, String password)
                    throws SCCClientException {
                assertEquals("username", username);
                assertEquals("password", password);
                assertEquals(new Date(0), system.getLastSeenAt());
                return new SCCSystemCredentialsJson(system.getLogin(), system.getPassword(), 12345L);
            }

            @Override
            public void deleteSystem(long id, String username, String password) throws SCCClientException {
                assertEquals(12345L, id);
                assertEquals("username", username);
                assertEquals("password", password);
            }

            @Override
            public void updateBulkLastSeen(List<SCCUpdateSystemJson> systems, String username, String password)
                    throws SCCClientException {
                assertEquals("username", username);
                assertEquals("password", password);
                assertEquals(new Date(0), systems.get(0).getLastSeenAt());
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
        credentials.setUrl("https://scc.suse.com");
        CredentialsFactory.storeCredentials(credentials);
        sccSystemRegistrationManager.register(testSystems, credentials);

        sccSystemRegistrationManager.updateLastSeen();
    }

    public void testMassUpdateSystems() throws Exception {
        Path tmpSaltRoot = Files.createTempDirectory("salt");
        SaltStateGeneratorService.INSTANCE.setSuseManagerStatesFilesRoot(tmpSaltRoot
                .toAbsolutePath());
        SaltStateGeneratorService.INSTANCE.setSkipSetOwner(true);

        Config.get().setString(ConfigDefaults.REG_BATCH_SIZE, "5");

        int c = 0;
        while (c < 16) {
            Server testSystem = ServerTestUtils.createTestSystem();
            ServerInfo serverInfo = testSystem.getServerInfo();
            serverInfo.setCheckin(new Date(0)); // 1970-01-01 00:00:00 UTC
            testSystem.setServerInfo(serverInfo);
            c += 1;
        }

        class TestSCCWebClient extends SCCWebClient {

            protected int callCnt;

            TestSCCWebClient(SCCConfig configIn) {
                super(configIn);
                callCnt = 0;
            }

        }

        TestSCCWebClient sccWebClient = new TestSCCWebClient(new SCCConfig(
                new URI("https://localhost"), "username", "password", "uuid")) {
            @Override
            public SCCSystemCredentialsJson createSystem(SCCRegisterSystemJson system, String username, String password)
                    throws SCCClientException {
                return new SCCSystemCredentialsJson(system.getLogin(), system.getPassword(), 12345L);
            }

            @Override
            public void updateBulkLastSeen(List<SCCUpdateSystemJson> systems, String username, String password)
                    throws SCCClientException {
                callCnt += 1;
                assertTrue("more requests then expected", callCnt <= 4);
                if (callCnt < 4) {
                    assertEquals(5, systems.size());
                }
                else {
                    assertEquals(1, systems.size());
                }
            }
        };

        SCCSystemRegistrationManager sccSystemRegistrationManager = new SCCSystemRegistrationManager(sccWebClient);
        SCCCachingFactory.initNewSystemsToForward();
        List<SCCRegCacheItem> allUnregistered = SCCCachingFactory.findSystemsToForwardRegistration();
        List<SCCRegCacheItem> testSystems = allUnregistered.stream()
                .filter(i -> i.getOptServer().get().getServerInfo().getCheckin().equals(new Date(0)))
                .collect(Collectors.toList());
        Credentials credentials = CredentialsFactory.createSCCCredentials();
        credentials.setUsername("username");
        credentials.setPassword("password");
        credentials.setUrl("https://scc.suse.com");
        CredentialsFactory.storeCredentials(credentials);
        sccSystemRegistrationManager.register(testSystems, credentials);

        sccSystemRegistrationManager.updateLastSeen();
    }
}
