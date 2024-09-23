/*
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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.credentials.SCCCredentials;
import com.redhat.rhn.domain.scc.SCCCachingFactory;
import com.redhat.rhn.domain.scc.SCCRegCacheItem;
import com.redhat.rhn.domain.server.CPU;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerInfo;
import com.redhat.rhn.domain.server.VirtualInstanceFactory;
import com.redhat.rhn.domain.server.test.CPUTest;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.formula.FormulaMonitoringManager;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ServerTestUtils;

import com.suse.manager.virtualization.test.TestVirtManager;
import com.suse.manager.webui.services.SaltStateGeneratorService;
import com.suse.manager.webui.services.iface.MonitoringManager;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.VirtManager;
import com.suse.manager.webui.services.test.TestSaltApi;
import com.suse.scc.SCCSystemRegistrationManager;
import com.suse.scc.client.SCCClient;
import com.suse.scc.client.SCCConfig;
import com.suse.scc.client.SCCWebClient;
import com.suse.scc.model.SCCOrganizationSystemsUpdateResponse;
import com.suse.scc.model.SCCRegisterSystemJson;
import com.suse.scc.model.SCCSystemCredentialsJson;
import com.suse.scc.model.SCCUpdateSystemJson;
import com.suse.scc.model.SCCVirtualizationHostJson;
import com.suse.scc.model.SCCVirtualizationHostPropertiesJson;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Tests for {@link SCCClient} methods.
 */
public class SCCSystemRegistrationManagerTest extends BaseTestCaseWithUser {

    /**
     * Value for system uptime data.
     */
    private static final String UPTIME_TEST = "[\"2024-06-26:000000000000000000001111\"," +
                                               "\"2024-06-27:111111111111110000000000\"]";

    @Test
    public void testSCCSystemRegistrationLifecycle() throws Exception {
        Path tmpSaltRoot = Files.createTempDirectory("salt");
        SaltStateGeneratorService.INSTANCE.setSuseManagerStatesFilesRoot(tmpSaltRoot
                .toAbsolutePath());
        SaltStateGeneratorService.INSTANCE.setSkipSetOwner(true);
        Server testSystem = ServerTestUtils.createTestSystem();
        ServerInfo serverInfo = testSystem.getServerInfo();
        serverInfo.setCheckin(new Date(0)); // 1970-01-01 00:00:00 UTC
        serverInfo.setUptimeData(UPTIME_TEST);
        testSystem.setServerInfo(serverInfo);

        SCCWebClient sccWebClient = new SCCWebClient(new SCCConfig(
                new URI("https://localhost"), "username", "password", "uuid")) {
            @Override
            public SCCOrganizationSystemsUpdateResponse createUpdateSystems(
                    List<SCCRegisterSystemJson> systems, String username, String password) {
                assertEquals("username", username);
                assertEquals("password", password);
                assertNotEmpty(systems);
                assertAll(systems.stream().map(system -> () -> assertEquals(new Date(0), system.getLastSeenAt())));
                assertAll(systems.stream().map(system -> () -> assertEquals(UPTIME_TEST, system.getOnlineAt())));

                return new SCCOrganizationSystemsUpdateResponse(
                        systems.stream()
                                .map(system ->
                                        new SCCSystemCredentialsJson(system.getLogin(), system.getPassword(), 12345L)
                                )
                                .collect(Collectors.toList())
                );
            }

            @Override
            public void deleteSystem(long id, String username, String password) {
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
        SCCCredentials credentials = CredentialsFactory.createSCCCredentials("username", "password");
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

    @Test
    public void sccSystemRegistrationLifecycleForPAYGInstance() throws Exception {
        Server testSystem = ServerTestUtils.createTestSystem();
        ServerInfo serverInfo = testSystem.getServerInfo();
        serverInfo.setCheckin(new Date(0)); // 1970-01-01 00:00:00 UTC
        serverInfo.setUptimeData(UPTIME_TEST);
        testSystem.setServerInfo(serverInfo);
        testSystem.setPayg(true);

        SCCWebClient sccWebClient = new SCCWebClient(new SCCConfig(
                new URI("https://localhost"), "username", "password", "uuid"));

        SCCSystemRegistrationManager sccSystemRegistrationManager = new SCCSystemRegistrationManager(sccWebClient);
        SCCCachingFactory.initNewSystemsToForward();

        List<SCCRegCacheItem> allUnregistered = SCCCachingFactory.findSystemsToForwardRegistration();
        List<SCCRegCacheItem> testSystems = allUnregistered.stream()
                .filter(i -> i.getOptServer().get().equals(testSystem))
                .collect(Collectors.toList());

        SCCCredentials credentials = CredentialsFactory.createSCCCredentials("username", "password");
        CredentialsFactory.storeCredentials(credentials);

        sccSystemRegistrationManager.register(testSystems, credentials);
        List<SCCRegCacheItem> afterRegistration = SCCCachingFactory.findSystemsToForwardRegistration();
        assertEquals(allUnregistered.size() - 1, afterRegistration.size());
    }

    @Test
    public void testUpdateSystems() throws Exception {
        Path tmpSaltRoot = Files.createTempDirectory("salt");
        SaltStateGeneratorService.INSTANCE.setSuseManagerStatesFilesRoot(tmpSaltRoot
                .toAbsolutePath());
        SaltStateGeneratorService.INSTANCE.setSkipSetOwner(true);
        Server testSystem = ServerTestUtils.createTestSystem();
        ServerInfo serverInfo = testSystem.getServerInfo();
        serverInfo.setCheckin(new Date(0)); // 1970-01-01 00:00:00 UTC
        serverInfo.setUptimeData(UPTIME_TEST);
        testSystem.setServerInfo(serverInfo);

        SCCWebClient sccWebClient = new SCCWebClient(new SCCConfig(
                new URI("https://localhost"), "username", "password", "uuid")) {
            @Override
            public SCCOrganizationSystemsUpdateResponse createUpdateSystems(
                    List<SCCRegisterSystemJson> systems, String username, String password) {
                assertEquals("username", username);
                assertEquals("password", password);
                assertNotEmpty(systems);
                assertAll(systems.stream().map(system -> () -> assertEquals(new Date(0), system.getLastSeenAt())));
                assertAll(systems.stream().map(system -> () -> assertEquals(UPTIME_TEST, system.getOnlineAt())));

                return new SCCOrganizationSystemsUpdateResponse(
                        systems.stream()
                                .map(system ->
                                        new SCCSystemCredentialsJson(system.getLogin(), system.getPassword(), 12345L)
                                )
                                .collect(Collectors.toList())
                );
            }

            @Override
            public void deleteSystem(long id, String username, String password) {
                assertEquals(12345L, id);
                assertEquals("username", username);
                assertEquals("password", password);
            }

            @Override
            public void updateBulkLastSeen(List<SCCUpdateSystemJson> systems, String username, String password) {
                assertEquals("username", username);
                assertEquals("password", password);
                assertEquals(new Date(0), systems.get(0).getLastSeenAt());
                assertEquals(UPTIME_TEST, systems.get(0).getOnlineAt());
            }
        };

        SCCSystemRegistrationManager sccSystemRegistrationManager = new SCCSystemRegistrationManager(sccWebClient);
        SCCCachingFactory.initNewSystemsToForward();
        List<SCCRegCacheItem> allUnregistered = SCCCachingFactory.findSystemsToForwardRegistration();
        List<SCCRegCacheItem> testSystems = allUnregistered.stream()
                .filter(i -> i.getOptServer().get().equals(testSystem))
                .collect(Collectors.toList());
        SCCCredentials credentials = CredentialsFactory.createSCCCredentials("username", "password");
        credentials.setUrl("https://scc.suse.com");
        CredentialsFactory.storeCredentials(credentials);
        sccSystemRegistrationManager.register(testSystems, credentials);

        sccSystemRegistrationManager.updateLastSeen(credentials);
    }

    @Test
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
            serverInfo.setUptimeData(UPTIME_TEST);
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
            public SCCOrganizationSystemsUpdateResponse createUpdateSystems(
                    List<SCCRegisterSystemJson> systems, String username, String password) {
                return new SCCOrganizationSystemsUpdateResponse(
                        systems.stream()
                                .map(system ->
                                        new SCCSystemCredentialsJson(system.getLogin(), system.getPassword(), 12345L)
                                )
                                .collect(Collectors.toList())
                );
            }

            @Override
            public void updateBulkLastSeen(List<SCCUpdateSystemJson> systems, String username, String password) {
                callCnt += 1;
                assertTrue(callCnt <= 4, "more requests then expected");
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
        SCCCredentials credentials = CredentialsFactory.createSCCCredentials("username", "password");
        credentials.setUrl("https://scc.suse.com");
        CredentialsFactory.storeCredentials(credentials);
        sccSystemRegistrationManager.register(testSystems, credentials);

        sccSystemRegistrationManager.updateLastSeen(credentials);
    }

    @Test
    public void testVirtualInfoLibvirt() throws Exception {
        Path tmpSaltRoot = Files.createTempDirectory("salt");
        SaltStateGeneratorService.INSTANCE.setSuseManagerStatesFilesRoot(tmpSaltRoot
                .toAbsolutePath());
        SaltStateGeneratorService.INSTANCE.setSkipSetOwner(true);

        SaltApi saltApi = new TestSaltApi();
        VirtManager virtManager = new TestVirtManager() {
            @Override
            public void updateLibvirtEngine(MinionServer minionIn) {
            }
        };
        MonitoringManager monitoringManager = new FormulaMonitoringManager(saltApi);
        ServerGroupManager serverGroupManager = new ServerGroupManager(saltApi);
        SystemEntitlementManager sysEntMgr = new SystemEntitlementManager(
                new SystemUnentitler(virtManager, monitoringManager, serverGroupManager),
                new SystemEntitler(saltApi, virtManager, monitoringManager, serverGroupManager)
        );

        Server host = ServerTestUtils.createVirtHostWithGuests(user, 2, true, sysEntMgr);
        host.setHostname("LibVirtHost");
        host.setCpu(CPUTest.createTestCpu(host));
        host.getGuests().stream()
                .forEach(vi -> vi.setType(VirtualInstanceFactory.getInstance().getVirtualInstanceType("qemu")));

        SCCWebClient sccWebClient = new SCCWebClient(new SCCConfig(
                new URI("https://localhost"), "username", "password", "uuid")) {


            @Override
            public SCCOrganizationSystemsUpdateResponse createUpdateSystems(
                    List<SCCRegisterSystemJson> systems, String username, String password) {
                assertEquals("username", username);
                assertEquals("password", password);

                return new SCCOrganizationSystemsUpdateResponse(
                        systems.stream()
                                .map(system -> new SCCSystemCredentialsJson(
                                        system.getLogin(),
                                        system.getPassword(),
                                        new Random().nextLong())
                                )
                                .collect(Collectors.toList())
                );
            }

            @Override
            public void deleteSystem(long id, String username, String password) {
                assertEquals("username", username);
                assertEquals("password", password);
            }

            @Override
            public void updateBulkLastSeen(List<SCCUpdateSystemJson> systems, String username, String password) {
                assertEquals("username", username);
                assertEquals("password", password);
            }

            @Override
            public void setVirtualizationHost(List<SCCVirtualizationHostJson> virtHostInfo,
                                              String username, String password) {
                assertEquals("username", username);
                assertEquals("password", password);
                SCCVirtualizationHostPropertiesJson hostProps = virtHostInfo.get(0).getProperties();
                assertEquals("LibVirtHost", hostProps.getName());
                assertEquals(2, hostProps.getSockets());
                assertEquals(20, hostProps.getCores());
                assertEquals(40, hostProps.getThreads());
                assertEquals("i386", hostProps.getArch());
                assertEquals("KVM", hostProps.getType());
                assertEquals(1024, hostProps.getRamMb());
                assertEquals(2, virtHostInfo.get(0).getSystems().size());
            }
        };

        SCCCredentials credentials = CredentialsFactory.createSCCCredentials("username", "password");
        credentials.setUrl("https://scc.suse.com");
        CredentialsFactory.storeCredentials(credentials);

        SCCSystemRegistrationManager sccSystemRegistrationManager = new SCCSystemRegistrationManager(sccWebClient);
        SCCCachingFactory.initNewSystemsToForward();
        List<SCCRegCacheItem> allUnregistered = SCCCachingFactory.findSystemsToForwardRegistration();
        List<SCCVirtualizationHostJson> virtHostsJson = SCCCachingFactory.listVirtualizationHosts();
        sccSystemRegistrationManager.register(allUnregistered, credentials);
        assertEquals(1, virtHostsJson.size());
        sccSystemRegistrationManager.virtualInfo(virtHostsJson, credentials);
    }

    @Test
    public void testVirtualInfoVMware() throws Exception {
        Path tmpSaltRoot = Files.createTempDirectory("salt");
        SaltStateGeneratorService.INSTANCE.setSuseManagerStatesFilesRoot(tmpSaltRoot
                .toAbsolutePath());
        SaltStateGeneratorService.INSTANCE.setSkipSetOwner(true);

        SaltApi saltApi = new TestSaltApi();
        VirtManager virtManager = new TestVirtManager() {
            @Override
            public void updateLibvirtEngine(MinionServer minionIn) {
            }
        };
        MonitoringManager monitoringManager = new FormulaMonitoringManager(saltApi);
        ServerGroupManager serverGroupManager = new ServerGroupManager(saltApi);
        SystemEntitlementManager sysEntMgr = new SystemEntitlementManager(
                new SystemUnentitler(virtManager, monitoringManager, serverGroupManager),
                new SystemEntitler(saltApi, virtManager, monitoringManager, serverGroupManager)
        );

        Server host = ServerTestUtils.createVirtHostWithGuests(user, 2, true, sysEntMgr);
        sysEntMgr.setBaseEntitlement(host, EntitlementManager.FOREIGN);
        host.setHostname("VMwareHost");
        host.setCpu(CPUTest.createTestCpu(host));
        host.getGuests().stream()
                .forEach(vi -> vi.setType(VirtualInstanceFactory.getInstance().getVirtualInstanceType("vmware")));

        SCCWebClient sccWebClient = new SCCWebClient(new SCCConfig(
                new URI("https://localhost"), "username", "password", "uuid")) {


            @Override
            public SCCOrganizationSystemsUpdateResponse createUpdateSystems(
                    List<SCCRegisterSystemJson> systems, String username, String password) {
                assertEquals("username", username);
                assertEquals("password", password);
                return new SCCOrganizationSystemsUpdateResponse(
                        systems.stream()
                                .map(system -> new SCCSystemCredentialsJson(
                                        system.getLogin(),
                                        system.getPassword(),
                                        new Random().nextLong())
                                )
                                .collect(Collectors.toList())
                );
            }

            @Override
            public void deleteSystem(long id, String username, String password) {
                assertEquals("username", username);
                assertEquals("password", password);
            }

            @Override
            public void updateBulkLastSeen(List<SCCUpdateSystemJson> systems, String username, String password) {
                assertEquals("username", username);
                assertEquals("password", password);
            }

            @Override
            public void setVirtualizationHost(List<SCCVirtualizationHostJson> virtHostInfo,
                                              String username, String password) {
                assertEquals("username", username);
                assertEquals("password", password);
                SCCVirtualizationHostPropertiesJson hostProps = virtHostInfo.get(0).getProperties();
                assertEquals("VMwareHost", hostProps.getName());
                assertEquals(2, hostProps.getSockets());
                assertEquals(20, hostProps.getCores());
                assertEquals(40, hostProps.getThreads());
                assertEquals("i386", hostProps.getArch());
                assertEquals("VMware", hostProps.getType());
                assertEquals(1024, hostProps.getRamMb());
                assertEquals(2, virtHostInfo.get(0).getSystems().size());
            }
        };

        SCCCredentials credentials = CredentialsFactory.createSCCCredentials("username", "password");
        credentials.setUrl("https://scc.suse.com");
        CredentialsFactory.storeCredentials(credentials);

        SCCSystemRegistrationManager sccSystemRegistrationManager = new SCCSystemRegistrationManager(sccWebClient);
        SCCCachingFactory.initNewSystemsToForward();
        List<SCCRegCacheItem> allUnregistered = SCCCachingFactory.findSystemsToForwardRegistration();
        List<SCCVirtualizationHostJson> virtHostsJson = SCCCachingFactory.listVirtualizationHosts();
        sccSystemRegistrationManager.register(allUnregistered, credentials);
        assertEquals(1, virtHostsJson.size());
        sccSystemRegistrationManager.virtualInfo(virtHostsJson, credentials);

        virtHostsJson = SCCCachingFactory.listVirtualizationHosts();
        assertEquals(0, virtHostsJson.size());
        sccSystemRegistrationManager.virtualInfo(virtHostsJson, credentials);

        SCCCachingFactory.lookupCacheItemByServer(host).ifPresent(i -> i.setSccRegistrationRequired(true));
        virtHostsJson = SCCCachingFactory.listVirtualizationHosts();
        assertEquals(1, virtHostsJson.size());
        sccSystemRegistrationManager.virtualInfo(virtHostsJson, credentials);
    }

    @Test
    public void testVirtualInfoCloud() throws Exception {
        Path tmpSaltRoot = Files.createTempDirectory("salt");
        SaltStateGeneratorService.INSTANCE.setSuseManagerStatesFilesRoot(tmpSaltRoot
                .toAbsolutePath());
        SaltStateGeneratorService.INSTANCE.setSkipSetOwner(true);

        SaltApi saltApi = new TestSaltApi();
        VirtManager virtManager = new TestVirtManager() {
            @Override
            public void updateLibvirtEngine(MinionServer minionIn) {
            }
        };
        MonitoringManager monitoringManager = new FormulaMonitoringManager(saltApi);
        ServerGroupManager serverGroupManager = new ServerGroupManager(saltApi);
        SystemEntitlementManager sysEntMgr = new SystemEntitlementManager(
                new SystemUnentitler(virtManager, monitoringManager, serverGroupManager),
                new SystemEntitler(saltApi, virtManager, monitoringManager, serverGroupManager)
        );

        Server host = ServerTestUtils.createVirtHostWithGuests(user, 2, true, sysEntMgr);
        sysEntMgr.setBaseEntitlement(host, EntitlementManager.FOREIGN);
        host.setHostname("CloudHost");
        CPU cpu = CPUTest.createTestCpu(host);
        cpu.setNrCPU(0L);
        cpu.setNrsocket(0L);
        cpu.setNrCore(0L);
        cpu.setNrThread(0L);
        host.setCpu(cpu);
        host.setRam(0L);
        host.getGuests().stream()
                .forEach(vi -> vi.setType(VirtualInstanceFactory.getInstance().getVirtualInstanceType("aws_nitro")));

        SCCWebClient sccWebClient = new SCCWebClient(new SCCConfig(
                new URI("https://localhost"), "username", "password", "uuid")) {


            @Override
            public SCCOrganizationSystemsUpdateResponse createUpdateSystems(
                    List<SCCRegisterSystemJson> systems, String username, String password) {
                assertEquals("username", username);
                assertEquals("password", password);
                return new SCCOrganizationSystemsUpdateResponse(
                        systems.stream()
                                .map(system -> new SCCSystemCredentialsJson(
                                        system.getLogin(),
                                        system.getPassword(),
                                        new Random().nextLong())
                                )
                                .collect(Collectors.toList())
                );
            }

            @Override
            public void deleteSystem(long id, String username, String password) {
                assertEquals("username", username);
                assertEquals("password", password);
            }

            @Override
            public void updateBulkLastSeen(List<SCCUpdateSystemJson> systems, String username, String password) {
                assertEquals("username", username);
                assertEquals("password", password);
            }

            @Override
            public void setVirtualizationHost(List<SCCVirtualizationHostJson> virtHostInfo,
                                              String username, String password) {
                assertEquals("username", username);
                assertEquals("password", password);
                SCCVirtualizationHostPropertiesJson hostProps = virtHostInfo.get(0).getProperties();
                assertEquals("CloudHost", hostProps.getName());
                assertEquals(0, hostProps.getSockets());
                assertEquals(0, hostProps.getCores());
                assertEquals(0, hostProps.getThreads());
                assertEquals("i386", hostProps.getArch());
                assertEquals("Nitro", hostProps.getType());
                assertEquals(0, hostProps.getRamMb());
                assertEquals(2, virtHostInfo.get(0).getSystems().size());
            }
        };

        SCCCredentials credentials = CredentialsFactory.createSCCCredentials("username", "password");
        credentials.setUrl("https://scc.suse.com");
        CredentialsFactory.storeCredentials(credentials);

        SCCSystemRegistrationManager sccSystemRegistrationManager = new SCCSystemRegistrationManager(sccWebClient);
        SCCCachingFactory.initNewSystemsToForward();
        List<SCCRegCacheItem> allUnregistered = SCCCachingFactory.findSystemsToForwardRegistration();
        List<SCCVirtualizationHostJson> virtHostsJson = SCCCachingFactory.listVirtualizationHosts();
        sccSystemRegistrationManager.register(allUnregistered, credentials);
        assertEquals(1, virtHostsJson.size());
        sccSystemRegistrationManager.virtualInfo(virtHostsJson, credentials);

        virtHostsJson = SCCCachingFactory.listVirtualizationHosts();
        assertEquals(0, virtHostsJson.size());
        sccSystemRegistrationManager.virtualInfo(virtHostsJson, credentials);

        SCCCachingFactory.lookupCacheItemByServer(host).ifPresent(i -> i.setSccRegistrationRequired(true));
        virtHostsJson = SCCCachingFactory.listVirtualizationHosts();
        assertEquals(1, virtHostsJson.size());
        sccSystemRegistrationManager.virtualInfo(virtHostsJson, credentials);
    }
}
