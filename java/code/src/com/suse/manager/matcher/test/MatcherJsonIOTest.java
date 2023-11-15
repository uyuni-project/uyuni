/*
 * Copyright (c) 2015--2021 SUSE LLC
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
package com.suse.manager.matcher.test;

import static java.util.Collections.singleton;
import static org.hamcrest.CoreMatchers.any;
import static org.jmock.AbstractExpectations.returnValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.db.datasource.CallableMode;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.product.test.SUSEProductTestUtils;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.server.CPU;
import com.redhat.rhn.domain.server.InstalledProduct;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.PinnedSubscription;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.VirtualInstance;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManager;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManagerFactory;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.content.ContentSyncManager;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.formula.FormulaMonitoringManager;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.ServerTestUtils;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.maintenance.BaseProductManager;
import com.suse.manager.matcher.MatcherJsonIO;
import com.suse.manager.virtualization.test.TestVirtManager;
import com.suse.manager.webui.services.iface.MonitoringManager;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.VirtManager;
import com.suse.manager.webui.services.test.TestSaltApi;
import com.suse.matcher.json.MatchJson;
import com.suse.matcher.json.ProductJson;
import com.suse.matcher.json.SubscriptionJson;
import com.suse.matcher.json.SystemJson;
import com.suse.matcher.json.VirtualizationGroupJson;
import com.suse.scc.model.SCCSubscriptionJson;

import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class MatcherJsonIOTest extends JMockBaseTestCaseWithUser {
    private static final String JARPATH = "/com/redhat/rhn/manager/content/test/sccdata/";
    private static final String SUBSCRIPTIONS_JSON = "organizations_subscriptions.json";
    private static final String ORDERS_JSON = "organizations_orders.json";

    // SCC Product IDs:
    private static final long MGMT_SINGLE_PROD_ID = 1076L;
    private static final long MGMT_UNLIMITED_VIRT_PROD_ID = 1078L;
    private static final long PROV_UNLIMITED_VIRT_PROD_ID = 1204L;
    private static final long MONITORING_SINGLE_PROD_ID = 1201L;
    private static final long MONITORING_UNLIMITED_VIRT_PROD_ID = 1203L;
    private static final long SUMA_X8664_PROD_ID = 2378L;
    private static final long SUMA_S390_PROD_ID = 2377L;
    private static final long SUMA_PPC64LE_PROD_ID = 2376L;

    private static final String AMD64_ARCH = "amd64";
    private static final String S390_ARCH = "s390";
    private static final String PPC64LE_ARCH = "ppc64le";
    private SystemEntitlementManager systemEntitlementManager;
    private BaseProductManager baseProductManagerMock;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);

        VirtManager virtManager = new TestVirtManager() {
            @Override
            public void updateLibvirtEngine(MinionServer minion) {
            }
        };
        SaltApi saltApi = new TestSaltApi();
        MonitoringManager monitoringManager = new FormulaMonitoringManager(saltApi);
        ServerGroupManager serverGroupManager = new ServerGroupManager(saltApi);
        systemEntitlementManager = new SystemEntitlementManager(
                new SystemUnentitler(virtManager, monitoringManager, serverGroupManager),
                new SystemEntitler(saltApi, virtManager, monitoringManager, serverGroupManager)
        );

        baseProductManagerMock = mock(BaseProductManager.class);

        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        ActionManager.setTaskomaticApi(taskomaticMock);

        checking(builder -> {
            builder.allowing(baseProductManagerMock).getName();
            builder.will(returnValue("suse-manager-server"));

            builder.allowing(baseProductManagerMock).getVersion();
            builder.will(returnValue("4.3"));

            builder.allowing(taskomaticMock).scheduleActionExecution(builder.with(any(Action.class)));
        });
    }

    @Test
    public void testSystemsToJson() throws Exception {
        SUSEProductTestUtils.clearAllProducts();
        SUSEProductTestUtils.createVendorSUSEProducts();
        SUSEProductTestUtils.createVendorEntitlementProducts();

        Server h1 = ServerTestUtils.createTestSystem();
        h1.setName("host1.example.com");
        h1.setCpu(createCPU(h1, 8L, 1 , 1));

        Set<InstalledProduct> installedProducts = new HashSet<>();
        InstalledProduct instPrd = createInstalledProduct("SLES", "12.1", "0", "x86_64", true);
        installedProducts.add(instPrd);
        instPrd = createInstalledProduct("sle-ha", "12.1", "0", "x86_64", false);
        installedProducts.add(instPrd);

        InstalledProduct instPrdMicro = createInstalledProduct("SLE-Micro", "5.4", "0", "x86_64", true);

        Server g1 = ServerTestUtils.createTestSystem();
        g1.setCpu(createCPU(g1, 2L, 1, 1));
        g1.setName("guest1.example.com");
        g1.setInstalledProducts(installedProducts);
        String uuid1 = TestUtils.randomString();

        VirtualInstance refGuest1 = createVirtualInstance(h1, g1, uuid1);
        h1.addGuest(refGuest1);

        Server g2 = ServerTestUtils.createTestSystem();
        g2.setName("guest2.example.com");
        g2.setCpu(createCPU(g2, 4L, 1 , 1));
        g2.setInstalledProducts(installedProducts);
        String uuid2 = TestUtils.randomString();

        VirtualInstance refGuest2 = createVirtualInstance(h1, g2, uuid2);
        h1.addGuest(refGuest2);

        Server g3 = ServerTestUtils.createTestSystem();
        g3.setName("guest3.example.com");
        g3.setCpu(createCPU(g3, 4L, 1 , 1));
        g3.setInstalledProducts(installedProducts);
        g3.setPayg(true);

        Server m1 = ServerTestUtils.createTestSystem();
        m1.setName("sle-micro1.example.com");
        m1.setInstalledProducts(Set.of(instPrdMicro));
        m1.setCpu(createCPU(m1, 2L, 8, 2));

        Server m2hyp = ServerTestUtils.createTestSystem();
        m2hyp.setName("sle-micro2.example.com");
        m2hyp.setInstalledProducts(Set.of(instPrdMicro));
        m2hyp.setCpu(createCPU(m2hyp, 2L, 8, 2));

        Server g4 = ServerTestUtils.createTestSystem();
        g4.setName("guest4.example.com");
        g4.setCpu(createCPU(g4, 4L, 1 , 1));
        g4.setInstalledProducts(Set.of(instPrdMicro));
        String uuid4 = TestUtils.randomString();

        VirtualInstance refGuest4 = createVirtualInstance(m2hyp, g4, uuid4);
        m2hyp.addGuest(refGuest4);

        HibernateFactory.getSession().flush();

        // tell MatcherJsonIO to include self system in the JSON output, which would happen
        // if the running SUMA is an ISS Master
        List<SystemJson> result = getMatcherJsonIO().getJsonSystems(AMD64_ARCH, true, false, true);
        assertNotNull(result);

        SystemJson resultH1 = findSystem(h1.getId(), result);
        assertNotNull(resultH1);
        assertEquals("host1.example.com", resultH1.getName());
        assertEquals(0, resultH1.getProductIds().size());

        SystemJson resultG1 = findSystem(g1.getId(), result);
        assertNotNull(resultG1);
        assertEquals("guest1.example.com", resultG1.getName());
        assertEquals(3, resultG1.getProductIds().size());
        assertTrue(resultG1.getProductIds().contains(1322L));
        assertTrue(resultG1.getProductIds().contains(MGMT_SINGLE_PROD_ID));
        assertTrue(resultG1.getProductIds().contains(1324L));

        SystemJson resultG2 = findSystem(g2.getId(), result);
        assertNotNull(resultG2);
        assertEquals("guest2.example.com", resultG2.getName());
        assertEquals(3, resultG2.getProductIds().size());
        assertTrue(resultG2.getProductIds().contains(1322L));
        assertTrue(resultG2.getProductIds().contains(MGMT_SINGLE_PROD_ID));
        assertTrue(resultG2.getProductIds().contains(1324L));

        // PAYG systems must only have entitlements
        SystemJson resultG3 = findSystem(g3.getId(), result);
        assertNotNull(resultG3);
        assertEquals("guest3.example.com", resultG3.getName());
        assertEquals(1, resultG3.getProductIds().size());
        assertTrue(resultG3.getProductIds().contains(MGMT_SINGLE_PROD_ID));

        // ISS Master should add itself
        SystemJson sumaItself = findSystem(MatcherJsonIO.SELF_SYSTEM_ID, result);
        assertNotNull(sumaItself);
        assertEquals(1L, sumaItself.getCpus().longValue());
        assertEquals("SUSE Manager Server system", sumaItself.getName());
        assertTrue(sumaItself.getPhysical());
        assertTrue(sumaItself.getProductIds().contains(SUMA_X8664_PROD_ID));

        // SLE-Micro System
        SystemJson resultM1 = findSystem(m1.getId(), result);
        assertNotNull(resultM1);
        assertEquals(32L, resultM1.getCpus().longValue());
        assertEquals("sle-micro1.example.com", resultM1.getName());
        assertTrue(resultM1.getPhysical());
        assertTrue(resultM1.getProductIds().contains(2574L));

        // SLE-Micro System Hypervisor
        SystemJson resultM2 = findSystem(m2hyp.getId(), result);
        assertNotNull(resultM2);
        assertEquals(2L, resultM2.getCpus().longValue());
        assertEquals("sle-micro2.example.com", resultM2.getName());
        assertTrue(resultM2.getPhysical());
        assertTrue(resultM2.getProductIds().contains(2574L));

        SystemJson resultG4 = findSystem(g4.getId(), result);
        assertNotNull(resultG4);
        assertEquals("guest4.example.com", resultG4.getName());
        // SLE-Micro + Mgm Single
        assertEquals(2, resultG4.getProductIds().size());
        assertTrue(resultG4.getProductIds().contains(2574L));
        assertFalse(resultG4.getPhysical());
        assertEquals(4L, resultG4.getCpus().longValue());
    }

    @Test
    public void testSystemsToJsonIssSlave() {
        List<SystemJson> result = getMatcherJsonIO().getJsonSystems(AMD64_ARCH, false, false, true);
        assertTrue(result.stream().noneMatch(
                s -> s.getId().equals(MatcherJsonIO.SELF_SYSTEM_ID)));
    }

    @Test
    public void testSystemsToJsonMonitoringEnabled() {
        // x86_64
        List<SystemJson> result = getMatcherJsonIO().getJsonSystems(AMD64_ARCH, false, true, true);
        SystemJson sumaItself = findSystem(MatcherJsonIO.SELF_SYSTEM_ID, result);
        assertEquals(1, sumaItself.getProductIds().size());
        assertEquals(MONITORING_SINGLE_PROD_ID, sumaItself.getProductIds().iterator().next().longValue());

        // s390
        result = getMatcherJsonIO().getJsonSystems(S390_ARCH, false, true, true);
        sumaItself = findSystem(MatcherJsonIO.SELF_SYSTEM_ID, result);
        assertEquals(1, sumaItself.getProductIds().size());
        assertEquals(MONITORING_UNLIMITED_VIRT_PROD_ID, sumaItself.getProductIds().iterator().next().longValue());

        // ppc64le
        result = getMatcherJsonIO().getJsonSystems(PPC64LE_ARCH, false, true, true);
        sumaItself = findSystem(MatcherJsonIO.SELF_SYSTEM_ID, result);
        assertEquals(1, sumaItself.getProductIds().size());
        assertEquals(MONITORING_SINGLE_PROD_ID, sumaItself.getProductIds().iterator().next().longValue());
    }

    @Test
    public void testSystemsToJsonIssMasterWithMonitoring() {
        // x86_64
        List<SystemJson> result = getMatcherJsonIO().getJsonSystems(AMD64_ARCH, true, true, true);
        SystemJson sumaItself = findSystem(MatcherJsonIO.SELF_SYSTEM_ID, result);
        assertEquals(2, sumaItself.getProductIds().size());
        assertEquals(Set.of(SUMA_X8664_PROD_ID, MONITORING_SINGLE_PROD_ID), sumaItself.getProductIds());

        // s390
        result = getMatcherJsonIO().getJsonSystems(S390_ARCH, true, true, true);
        sumaItself = findSystem(MatcherJsonIO.SELF_SYSTEM_ID, result);
        assertEquals(2, sumaItself.getProductIds().size());
        assertEquals(Set.of(SUMA_S390_PROD_ID, MONITORING_UNLIMITED_VIRT_PROD_ID), sumaItself.getProductIds());

        // ppc64le
        result = getMatcherJsonIO().getJsonSystems(PPC64LE_ARCH, true, true, true);
        sumaItself = findSystem(MatcherJsonIO.SELF_SYSTEM_ID, result);
        assertEquals(2, sumaItself.getProductIds().size());
        assertEquals(Set.of(SUMA_PPC64LE_PROD_ID, MONITORING_SINGLE_PROD_ID), sumaItself.getProductIds());
    }

    @Test
    public void testSystemsToJsonWithPayg() throws Exception {
        SUSEProductTestUtils.clearAllProducts();
        SUSEProductTestUtils.createVendorSUSEProducts();
        SUSEProductTestUtils.createVendorEntitlementProducts();

        Set<InstalledProduct> installedProducts = Set.of(createInstalledProduct("SLES", "15.1", "0", "x86_64", true));

        Server h1 = ServerTestUtils.createTestSystem();
        h1.setName("byos.example.com");
        h1.setCpu(createCPU(h1, 8L, 1, 1));
        h1.setPayg(false);
        h1.setInstalledProducts(installedProducts);

        Server h2 = ServerTestUtils.createTestSystem();
        h2.setName("payg.example.com");
        h2.setCpu(createCPU(h2, 8L, 1, 1));
        h2.setPayg(true);
        h2.setInstalledProducts(installedProducts);

        // Test on PAYG SUMA
        List<SystemJson> result = getMatcherJsonIO().getJsonSystems(AMD64_ARCH, false, false, false);
        // No SUMA should be added for payg
        assertTrue(result.stream().noneMatch(system -> MatcherJsonIO.SELF_SYSTEM_ID == system.getId()));

        // System is not payg, we collect product id, but no management entitlement
        SystemJson byosSystem = findSystem(h1.getId(), result);
        assertEquals(Set.of(1326L), byosSystem.getProductIds());

        // System is payg, no product nor entitlement needed
        SystemJson paygSystem = findSystem(h2.getId(), result);
        assertEquals(Collections.emptySet(), paygSystem.getProductIds());


        // Test on BYOS SUMA
        result = getMatcherJsonIO().getJsonSystems(AMD64_ARCH, true, true, true);
        // SUMA must be added for byos
        SystemJson sumaItself = findSystem(MatcherJsonIO.SELF_SYSTEM_ID, result);
        assertEquals(2, sumaItself.getProductIds().size());
        assertEquals(Set.of(SUMA_X8664_PROD_ID, MONITORING_SINGLE_PROD_ID), sumaItself.getProductIds());

        // System is not payg, we need both the product id and the entitlement
        byosSystem = findSystem(h1.getId(), result);
        assertEquals(Set.of(1326L, MGMT_SINGLE_PROD_ID), byosSystem.getProductIds());

        // System is payg, only the entitlement for suma is needed
        paygSystem = findSystem(h2.getId(), result);
        assertEquals(Set.of(MGMT_SINGLE_PROD_ID), paygSystem.getProductIds());
    }

    @Test
    public void testServerPaygSlesSapShouldNotRequireAnEntitlement() throws Exception {
        SUSEProductTestUtils.clearAllProducts();
        SUSEProductTestUtils.createVendorSUSEProducts();
        SUSEProductTestUtils.createVendorEntitlementProducts();

        Set<InstalledProduct> sleSap = Set.of(createInstalledProduct("SLES_SAP", "15.5", "0", "x86_64", true));
        Set<InstalledProduct> sles = Set.of(createInstalledProduct("SLES", "15.1", "0", "x86_64", true));

        Server h1 = ServerTestUtils.createTestSystem();
        h1.setName("byos-sles.example.com");
        h1.setCpu(createCPU(h1, 8L, 1, 1));
        h1.setPayg(false);
        h1.setInstalledProducts(sles);

        Server h2 = ServerTestUtils.createTestSystem();
        h2.setName("payg-sles.example.com");
        h2.setCpu(createCPU(h2, 8L, 1, 1));
        h2.setPayg(true);
        h2.setInstalledProducts(sles);

        Server h3 = ServerTestUtils.createTestSystem();
        h3.setName("byos-sles4sap.example.com");
        h3.setCpu(createCPU(h3, 8L, 1, 1));
        h3.setPayg(false);
        h3.setInstalledProducts(sleSap);

        Server h4 = ServerTestUtils.createTestSystem();
        h4.setName("payg-sles4sap.example.com");
        h4.setCpu(createCPU(h4, 8L, 1, 1));
        h4.setPayg(true);
        h4.setInstalledProducts(sleSap);

        // Test on BYOS SUMA
        List<SystemJson> result = getMatcherJsonIO().getJsonSystems(AMD64_ARCH, true, true, true);
        SystemJson sumaItself = findSystem(MatcherJsonIO.SELF_SYSTEM_ID, result);
        assertEquals(Set.of(SUMA_X8664_PROD_ID, MONITORING_SINGLE_PROD_ID), sumaItself.getProductIds());

        // System is BYOS sles, we need both the product id and the entitlement
        SystemJson byosSlesSystem = findSystem(h1.getId(), result);
        assertEquals(Set.of(1326L, MGMT_SINGLE_PROD_ID), byosSlesSystem.getProductIds());

        // System is PAYG SLES, only entitlement needed
        SystemJson paygSlesSystem = findSystem(h2.getId(), result);
        assertEquals(Set.of(MGMT_SINGLE_PROD_ID), paygSlesSystem.getProductIds());

        // System is BYOS sles_sap, we need both the product id and the entitlement
        SystemJson byosSlesSapSystem = findSystem(h3.getId(), result);
        assertEquals(Set.of(2467L, MGMT_SINGLE_PROD_ID), byosSlesSapSystem.getProductIds());

        // System is PAYG SLES, no product id and no entitlement
        SystemJson paygSlesSapSystem = findSystem(h4.getId(), result);
        assertEquals(Set.of(), paygSlesSapSystem.getProductIds());
    }

    @Test
    public void testProductsToJson() {
        SUSEProductTestUtils.clearAllProducts();
        SUSEProductTestUtils.createVendorSUSEProducts();

        List<ProductJson> result = getMatcherJsonIO().getJsonProducts();
        assertNotNull(result);

        assertEquals("SUSE Linux Enterprise Server 12 SP1",
                result.stream().filter(p -> p.getId().equals(1322L))
                    .findFirst().get().getName());

        assertEquals("SUSE Linux Enterprise High Availability Extension 12 SP1",
                result.stream().filter(p -> p.getId().equals(1324L))
                    .findFirst().get().getName());
    }

    /**
     * Tests that lifecycle products of the systems are present in the input for the matcher (non s390x scenario).
     * For virtual hosts, we should report the same lifecycle products as for other systems.
     *
     * @throws Exception - if anything goes wrong
     */
    @Test
    public void testLifecycleProductsReporting() throws Exception {
        SUSEProductTestUtils.clearAllProducts();
        SUSEProductTestUtils.createVendorSUSEProducts();
        SUSEProductTestUtils.createVendorEntitlementProducts();

        Server hostServer = ServerTestUtils.createVirtHostWithGuests(1, systemEntitlementManager);
        // let's set some base product to our systems (otherwise lifecycle subscriptions aren't reported)
        InstalledProduct instProd = createInstalledProduct("SLES", "12.1", "0", "x86_64", true);
        Set<InstalledProduct> installedProducts = singleton(instProd);
        hostServer.setInstalledProducts(installedProducts);
        Server guestServer = hostServer.getGuests().iterator().next().getGuestSystem();
        guestServer.setInstalledProducts(installedProducts);

        MatcherJsonIO matcherInput = getMatcherJsonIO();
        List<SystemJson> systems = matcherInput.getJsonSystems(AMD64_ARCH, false, false, true);
        assertEquals(1, systems.stream().filter(s -> s.getId().equals(hostServer.getId())).count());
        assertEquals(1, systems.stream().filter(s -> s.getId().equals(guestServer.getId())).count());
        SystemJson host = findSystem(hostServer.getId(), systems);
        SystemJson guest = findSystem(guestServer.getId(), systems);

        assertTrue(host.getProductIds().contains(MGMT_SINGLE_PROD_ID));
        assertFalse(host.getProductIds().contains(MGMT_UNLIMITED_VIRT_PROD_ID));
        assertFalse(host.getProductIds().contains(PROV_UNLIMITED_VIRT_PROD_ID));
        assertTrue(guest.getProductIds().contains(MGMT_SINGLE_PROD_ID));
        assertFalse(guest.getProductIds().contains(MGMT_UNLIMITED_VIRT_PROD_ID));
        assertFalse(guest.getProductIds().contains(PROV_UNLIMITED_VIRT_PROD_ID));
    }

    /**
     * Tests that monitoring products of the systems are present in the input for the matcher (non s390x scenario).
     * For virtual hosts, we should report the same monitoring products as for guest systems.
     *
     * @throws Exception - if anything goes wrong
     */
    @Test
    public void testMonitoringProductsReporting() throws Exception {
        SUSEProductTestUtils.clearAllProducts();
        SUSEProductTestUtils.createVendorSUSEProducts();
        SUSEProductTestUtils.createVendorEntitlementProducts();

        Server hostServer = ServerTestUtils.createVirtHostWithGuests(user, 1, true, systemEntitlementManager);
        // monitoring is only compatible with certain architectures. make sure we use one of them:
        hostServer.setServerArch(ServerFactory.lookupServerArchByLabel("x86_64-redhat-linux"));
        // let's set some base product to our systems (otherwise lifecycle subscriptions aren't reported)
        InstalledProduct instProd = createInstalledProduct("SLES", "12.1", "0", "x86_64", true);
        Set<InstalledProduct> installedProducts = singleton(instProd);
        hostServer.setInstalledProducts(installedProducts);
        Server guestServer = hostServer.getGuests().iterator().next().getGuestSystem();
        // monitoring is only compatible with certain architectures. make sure we use one of them:
        guestServer.setServerArch(ServerFactory.lookupServerArchByLabel("x86_64-redhat-linux"));
        guestServer.setInstalledProducts(installedProducts);

        MatcherJsonIO matcherInput = getMatcherJsonIO();
        boolean selfMonitoringEnabled = false;
        List<SystemJson> systems = matcherInput.getJsonSystems(AMD64_ARCH, false, selfMonitoringEnabled, true);
        assertEquals(1, systems.stream().filter(s -> s.getId().equals(hostServer.getId())).count());
        assertEquals(1, systems.stream().filter(s -> s.getId().equals(guestServer.getId())).count());
        SystemJson host = findSystem(hostServer.getId(), systems);
        SystemJson guest = findSystem(guestServer.getId(), systems);

        // let's check that monitoring products are NOT reported when the servers are not Monitoring-entitled
        assertFalse(host.getProductIds().contains(MONITORING_SINGLE_PROD_ID));
        assertFalse(host.getProductIds().contains(MONITORING_UNLIMITED_VIRT_PROD_ID));
        assertFalse(guest.getProductIds().contains(MONITORING_SINGLE_PROD_ID));
        assertFalse(guest.getProductIds().contains(MONITORING_UNLIMITED_VIRT_PROD_ID));

        // let's entitle the servers and check again
        entitleServerMonitoring(hostServer);
        entitleServerMonitoring(guestServer);
        HibernateFactory.getSession().clear();

        systems = matcherInput.getJsonSystems(AMD64_ARCH, false, selfMonitoringEnabled, true);
        host = findSystem(hostServer.getId(), systems);
        guest = findSystem(guestServer.getId(), systems);
        assertTrue(host.getProductIds().contains(MONITORING_SINGLE_PROD_ID));
        assertFalse(host.getProductIds().contains(MONITORING_UNLIMITED_VIRT_PROD_ID));
        assertTrue(guest.getProductIds().contains(MONITORING_SINGLE_PROD_ID));
        assertFalse(guest.getProductIds().contains(MONITORING_UNLIMITED_VIRT_PROD_ID));
    }

    // directly entitles server to EntitlementManager.MONITORING entitlement. This bypasses the formula-assignment logic
    // in the SystemManager.entitleServer, which we don't want to kick-in in these tests.
    private void entitleServerMonitoring(Server hostServer) {
        CallableMode m = ModeFactory.getCallableMode("System_queries", "entitle_server");
        Map<String, Object> in = new HashMap<>();
        in.put("sid", hostServer.getId());
        in.put("entitlement", EntitlementManager.MONITORING.getLabel());
        m.execute(in, new HashMap<>());
    }

    /**
     * Tests that the SUSE Manager Tools proudct is not reported to the matcher.
     *
     * @throws java.lang.Exception if anything goes wrong
     */
    @Test
    public void testFilteringToolsProducts() throws Exception {
        SUSEProductTestUtils.clearAllProducts();
        SUSEProductTestUtils.createVendorSUSEProducts();
        SUSEProductTestUtils.createSUMAToolsProduct();
        SUSEProductTestUtils.createVendorEntitlementProducts();

        Set<InstalledProduct> installedProducts = new HashSet<>();
        InstalledProduct instPrd = createInstalledProduct("SLES", "12.1", "0", "x86_64", true);
        installedProducts.add(instPrd);
        instPrd = createInstalledProduct("sle-manager-tools", "12", "0", "x86_64", false);
        installedProducts.add(instPrd);

        Server testSystem = ServerTestUtils.createTestSystem(user);
        testSystem.setInstalledProducts(installedProducts);

        MatcherJsonIO matcherInput = getMatcherJsonIO();
        SystemJson system = findSystem(testSystem.getId(), matcherInput.getJsonSystems(AMD64_ARCH, false, false, true));

        assertFalse(system.getProductIds().contains(instPrd.getSUSEProduct().getProductId()));
    }

    @Test
    public void testSubscriptionsToJson() throws Exception {
        withSetupContentSyncManager(JARPATH, () -> {
            List<SubscriptionJson> result = getMatcherJsonIO().getJsonSubscriptions();

            SubscriptionJson resultSubscription1 = result.stream()
                    .filter(rs -> rs.getId().equals(9998L))
                    .findFirst().get();

            assertEquals("662644474670", resultSubscription1.getPartNumber());
            assertEquals(Integer.valueOf(10), resultSubscription1.getQuantity());
            assertTrue(resultSubscription1.getProductIds().contains(1322L));
            assertTrue(resultSubscription1.getProductIds().contains(1324L));
            assertEquals("extFile", resultSubscription1.getSccUsername());

            SubscriptionJson resultSubscription2 = result.stream()
                    .filter(rs -> rs.getId().equals(9999L))
                    .findFirst().get();

            assertEquals("874-005117", resultSubscription2.getPartNumber());
            assertEquals(Integer.valueOf(100), resultSubscription2.getQuantity());
            assertTrue(resultSubscription2.getProductIds().contains(1322L));
            assertTrue(resultSubscription2.getProductIds().contains(1324L));
            assertEquals("extFile", resultSubscription2.getSccUsername());
        });
    }

    @Test
    public void testLifecycleProductsInSubscriptions() throws Exception {
        withSetupContentSyncManager("/com/redhat/rhn/manager/content/test/sccdata_lifecycle_products", () -> {
            List<SubscriptionJson> subscriptions = getMatcherJsonIO().getJsonSubscriptions();

            assertEquals(4, subscriptions.size());

            subscriptions.stream()
                    .filter(s -> s.getId() == 11 || s.getId() == 12) // Management
                    .forEach(s -> {
                        assertTrue(s.getProductIds().contains(MGMT_SINGLE_PROD_ID));
                        assertFalse(s.getProductIds().contains(MGMT_UNLIMITED_VIRT_PROD_ID));
                    });
            subscriptions.stream()
                    .filter(s -> s.getId() == 13 || s.getId() == 14) // Monitoring
                    .forEach(s -> {
                        assertTrue(s.getProductIds().contains(MONITORING_SINGLE_PROD_ID));
                        assertFalse(s.getProductIds().contains(MONITORING_UNLIMITED_VIRT_PROD_ID));
                    });
        });
    }

    /**
     * Helper method for handling the ContentSyncManager mocking boilerplate.
     *
     * @param workDir the working directory of the ContentSyncManager
     * @param body the Runnable with the test body
     * @throws Exception if anything goes wrong
     */
    private static void withSetupContentSyncManager(String workDir, Runnable body) throws Exception {
        File subJson = new File(TestUtils.findTestData(
                new File(workDir, SUBSCRIPTIONS_JSON).getAbsolutePath()).getPath());
        File orderJson = new File(TestUtils.findTestData(
                new File(workDir, ORDERS_JSON).getAbsolutePath()).getPath());

        Path fromdir = Files.createTempDirectory("sumatest");
        File subtempFile = new File(fromdir.toString(), SUBSCRIPTIONS_JSON);
        File ordertempFile = new File(fromdir.toString(), ORDERS_JSON);
        Files.copy(subJson.toPath(), subtempFile.toPath());
        Files.copy(orderJson.toPath(), ordertempFile.toPath());
        Config.get().setString(ContentSyncManager.RESOURCE_PATH, fromdir.toString());
        try {
            SUSEProductTestUtils.clearAllProducts();
            SUSEProductTestUtils.createVendorSUSEProducts();
            SUSEProductTestUtils.createVendorEntitlementProducts();

            ContentSyncManager cm = new ContentSyncManager();

            // this will also refresh the DB cache of subscriptions
            Collection<SCCSubscriptionJson> s;
            s = cm.updateSubscriptions();
            HibernateFactory.getSession().flush();
            assertNotNull(s);

            body.run();
        }
        finally {
            Config.get().remove(ContentSyncManager.RESOURCE_PATH);
            SUSEProductTestUtils.deleteIfTempFile(subJson);
            SUSEProductTestUtils.deleteIfTempFile(orderJson);
            subtempFile.delete();
            ordertempFile.delete();
            fromdir.toFile().delete();
        }
    }

    /**
     * Tests creating a nontrivial JSON Virtualization Group (based on single
     * {@link VirtualHostManager}.
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testVirtualHostManagersToJson() throws Exception {
        Server virtualHost1 = ServerTestUtils.createVirtHostWithGuests(2, systemEntitlementManager);
        Server virtualHost2 = ServerTestUtils.createVirtHostWithGuest(systemEntitlementManager);
        VirtualHostManager vhm = VirtualHostManagerFactory.getInstance()
                .createVirtualHostManager("ESXi", user.getOrg(), "file", new HashMap<>());
        vhm.addServer(virtualHost1);
        vhm.addServer(virtualHost2);
        VirtualHostManagerFactory.getInstance().save(vhm);

        List<VirtualizationGroupJson> jsonVirtualizationGroups = getMatcherJsonIO()
                .getJsonVirtualizationGroups();
        assertEquals(1, jsonVirtualizationGroups.size());

        VirtualizationGroupJson virtualizationGroup =
                jsonVirtualizationGroups.iterator().next();
        assertEquals(vhm.getId(), virtualizationGroup.getId());
        assertEquals(vhm.getLabel(), virtualizationGroup.getName());
        assertEquals("virtual_host_manager_file", virtualizationGroup.getType());
        assertEquals(3, virtualizationGroup.getVirtualGuestIds().size());

        Set<Long> guestIds = Arrays.asList(virtualHost1, virtualHost2).stream()
                .flatMap(s -> s.getGuests().stream())
                .map(VirtualInstance::getGuestSystem)
                .map(Server::getId)
                .collect(Collectors.toSet());
        assertTrue(virtualizationGroup.getVirtualGuestIds().containsAll(guestIds));
    }

    private MatcherJsonIO getMatcherJsonIO() {
        return new MatcherJsonIO(baseProductManagerMock);
    }

    @Test
    public void testPinsToJson() throws Exception {
        File subJson = new File(TestUtils.findTestData(
                new File(JARPATH, SUBSCRIPTIONS_JSON).getAbsolutePath()).getPath());
        File orderJson = new File(TestUtils.findTestData(
                new File(JARPATH, ORDERS_JSON).getAbsolutePath()).getPath());

        Path fromdir = Files.createTempDirectory("sumatest");
        File subtempFile = new File(fromdir.toString(), SUBSCRIPTIONS_JSON);
        File ordertempFile = new File(fromdir.toString(), ORDERS_JSON);
        Files.copy(subJson.toPath(), subtempFile.toPath());
        Files.copy(orderJson.toPath(), ordertempFile.toPath());
        try {
            Config.get().setString(ContentSyncManager.RESOURCE_PATH, fromdir.toString());

            SUSEProductTestUtils.clearAllProducts();
            SUSEProductTestUtils.createVendorSUSEProducts();
            SUSEProductTestUtils.createVendorEntitlementProducts();

            Server h1 = ServerTestUtils.createTestSystem();
            h1.setName("host1.example.com");
            h1.setCpu(createCPU(h1, 8L, 1, 1));
            ServerFactory.save(h1);

            Set<InstalledProduct> installedProducts = new HashSet<>();
            InstalledProduct instPrd = createInstalledProduct("SLES", "12.1", "0", "x86_64", true);
            installedProducts.add(instPrd);
            instPrd = createInstalledProduct("sle-ha", "12.1", "0", "x86_64", false);
            installedProducts.add(instPrd);

            h1.setInstalledProducts(installedProducts);

            ContentSyncManager cm = new ContentSyncManager();
            Collection<SCCSubscriptionJson> s = cm.updateSubscriptions();
            HibernateFactory.getSession().flush();

            PinnedSubscription pin = new PinnedSubscription();
            pin.setSystemId(h1.getId());
            pin.setSubscriptionId(9999L);
            TestUtils.saveAndFlush(pin);
            HibernateFactory.getSession().clear();

            List<MatchJson> result = getMatcherJsonIO()
                    .getJsonMatches();
            Optional<MatchJson> resultPin = result.stream()
                .filter(p -> p.getSystemId().equals(h1.getId()) &&
                    p.getSubscriptionId().equals(9999L))
                .findFirst();

            assertTrue(resultPin.isPresent());
        }
        finally {
            Config.get().remove(ContentSyncManager.RESOURCE_PATH);
            SUSEProductTestUtils.deleteIfTempFile(subJson);
            SUSEProductTestUtils.deleteIfTempFile(orderJson);
            subtempFile.delete();
            ordertempFile.delete();
            fromdir.toFile().delete();
        }
    }
    private VirtualInstance createVirtualInstance(Server host, Server guest, String uuid) {
        VirtualInstance virtualInstance = new VirtualInstance();
        virtualInstance.setHostSystem(host);
        virtualInstance.setGuestSystem(guest);
        virtualInstance.setUuid(uuid);
        virtualInstance.setConfirmed(1L);
        return virtualInstance;
    }

    private CPU createCPU(Server s, long sockets, long cores, long threads) {
        if (threads < 1) {
            threads = 1;
        }
        if (cores < 1) {
            cores = 1;
        }
        if (sockets < 1) {
            sockets = 1;
        }
        CPU cpu = new CPU();
        cpu.setNrsocket(sockets);
        cpu.setNrCore(cores);
        cpu.setNrThread(threads);
        cpu.setNrCPU(sockets * cores * threads);
        cpu.setServer(s);
        cpu.setArch(ServerFactory.lookupCPUArchByName("x86_64"));
        return cpu;
    }

    private InstalledProduct createInstalledProduct(String name, String version,
                                                    String release, String archLabel, boolean base) {
        InstalledProduct instProd = new InstalledProduct();
        instProd.setName(name);
        instProd.setVersion(version);
        instProd.setRelease(release);
        instProd.setArch(PackageFactory.lookupPackageArchByLabel(archLabel));
        instProd.setBaseproduct(base);
        return instProd;
    }

    // helper method for finding system in a list
    private SystemJson findSystem(long systemId, List<SystemJson> result) {
        return result.stream().filter(
                s -> s.getId().equals(systemId))
                .findFirst().get();
    }
}
