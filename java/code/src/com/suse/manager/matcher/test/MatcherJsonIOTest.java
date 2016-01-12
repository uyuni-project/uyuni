package com.suse.manager.matcher.test;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.common.LoggingFactory;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.product.test.SUSEProductTestUtils;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.server.CPU;
import com.redhat.rhn.domain.server.InstalledProduct;
import com.redhat.rhn.domain.server.PinnedSubscription;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.VirtualInstance;
import com.redhat.rhn.manager.content.ContentSyncManager;
import com.redhat.rhn.testing.ServerTestUtils;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.matcher.JsonPinnedMatch;
import com.suse.manager.matcher.JsonProduct;
import com.suse.manager.matcher.JsonSubscription;
import com.suse.manager.matcher.JsonSystem;
import com.suse.manager.matcher.MatcherJsonIO;
import com.suse.scc.model.SCCSubscription;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import junit.framework.TestCase;

public class MatcherJsonIOTest extends TestCase {
    private static final String JARPATH = "/com/redhat/rhn/manager/content/test/sccdata/";
    private static final String SUBSCRIPTIONS_JSON = "organizations_subscriptions.json";
    private static final String ORDERS_JSON = "organizations_orders.json";

    private static final long SUMA_SRV_ID = Long.MAX_VALUE;
    private static final String AMD64_ARCH = "amd64";

    public void testSystemsToJson() throws Exception {
        SUSEProductFactory.clearAllProducts();
        SUSEProductTestUtils.createVendorSUSEProducts();
        SUSEProductTestUtils.createVendorEntitlementProducts();
        LoggingFactory.clearLogId();

        Server h1 = ServerTestUtils.createTestSystem();
        h1.setName("host1.example.com");
        h1.setCpu(createCPU(h1, 8L));

        Set<InstalledProduct> installedProducts = new HashSet<>();
        InstalledProduct instPrd = new InstalledProduct();
        instPrd.setName("SLES");
        instPrd.setVersion("12.1");
        instPrd.setRelease("0");
        instPrd.setArch(PackageFactory.lookupPackageArchByLabel("x86_64"));
        instPrd.setBaseproduct(true);
        installedProducts.add(instPrd);

        instPrd = new InstalledProduct();
        instPrd.setName("sle-ha");
        instPrd.setVersion("12.1");
        instPrd.setRelease("0");
        instPrd.setArch(PackageFactory.lookupPackageArchByLabel("x86_64"));
        instPrd.setBaseproduct(false);
        installedProducts.add(instPrd);

        Server g1 = ServerTestUtils.createTestSystem();
        g1.setCpu(createCPU(g1, 2L));
        g1.setName("guest1.example.com");
        g1.setInstalledProducts(installedProducts);
        String uuid1 = TestUtils.randomString();

        VirtualInstance refGuest1 = createVirtualInstance(h1, g1, uuid1);
        h1.addGuest(refGuest1);

        Server g2 = ServerTestUtils.createTestSystem();
        g2.setName("guest2.example.com");
        g2.setCpu(createCPU(g2, 4L));
        g2.setInstalledProducts(installedProducts);
        String uuid2 = TestUtils.randomString();

        VirtualInstance refGuest2 = createVirtualInstance(h1, g2, uuid2);
        h1.addGuest(refGuest2);

        // tell MatcherJsonIO to include self system in the JSON output, which would happen
        // if the running SUMA is an ISS Master
        List<JsonSystem> result = new MatcherJsonIO(true, AMD64_ARCH).getJsonSystems();
        assertNotNull(result);

        JsonSystem resultH1 =
                result.stream().filter(s -> s.getId() == h1.getId()).findFirst().get();
        assertNotNull(resultH1);
        assertEquals("host1.example.com", resultH1.getName());
        assertEquals(0, resultH1.getProductIds().size());

        JsonSystem resultG1 =
                result.stream().filter(s -> s.getId() == g1.getId()).findFirst().get();
        assertNotNull(resultG1);
        assertEquals("guest1.example.com", resultG1.getName());
        assertEquals(4, resultG1.getProductIds().size());
        assertTrue(resultG1.getProductIds().contains(1322L));
        assertTrue(resultG1.getProductIds().contains(1076L));
        assertTrue(resultG1.getProductIds().contains(1097L));
        assertTrue(resultG1.getProductIds().contains(1324L));

        JsonSystem resultG2 =
                result.stream().filter(s -> s.getId() == g2.getId()).findFirst().get();
        assertNotNull(resultG2);
        assertEquals("guest2.example.com", resultG2.getName());
        assertEquals(4, resultG2.getProductIds().size());
        assertTrue(resultG2.getProductIds().contains(1322L));
        assertTrue(resultG2.getProductIds().contains(1076L));
        assertTrue(resultG2.getProductIds().contains(1097L));
        assertTrue(resultG2.getProductIds().contains(1324L));

        // ISS Master should add itself
        JsonSystem sumaItself = result.stream().filter(s -> s.getId().equals(SUMA_SRV_ID))
                .findFirst().get();
        assertNotNull(sumaItself);
        assertEquals(1L, sumaItself.getCpus().longValue());
        assertEquals("SUSE Manager Server system", sumaItself.getName());
        assertTrue(sumaItself.getPhysical());
        assertTrue(sumaItself.getProductIds().contains(1349L));
        assertTrue(sumaItself.getProductIds().contains(1322L));
    }

    public void testSystemsToJsonIssSlave() {
        List<JsonSystem> result = new MatcherJsonIO(false, AMD64_ARCH).getJsonSystems();
        assertTrue(result.stream().noneMatch(s -> s.getId().equals(SUMA_SRV_ID)));
    }

    public void testProductsToJson() throws Exception {
        SUSEProductFactory.clearAllProducts();
        SUSEProductTestUtils.createVendorSUSEProducts();
        LoggingFactory.clearLogId();

        List<JsonProduct> result = new MatcherJsonIO(true, AMD64_ARCH).getJsonProducts();
        assertNotNull(result);

        assertEquals("SUSE Linux Enterprise Server 12 SP1",
                result.stream().filter(p -> p.getId() == 1322L)
                    .findFirst().get().getName());

        assertEquals("SUSE Linux Enterprise High Availability Extension 12 SP1",
                result.stream().filter(p -> p.getId() == 1324L)
                    .findFirst().get().getName());
    }

    public void testSubscriptionsToJson() throws Exception {
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
            SUSEProductFactory.clearAllProducts();
            SUSEProductTestUtils.createVendorSUSEProducts();

            ContentSyncManager cm = new ContentSyncManager();

            // this will also refresh the DB cache of subscriptions
            Collection<SCCSubscription> s = cm.getSubscriptions();
            HibernateFactory.getSession().flush();
            assertNotNull(s);
            List<JsonSubscription> result = new MatcherJsonIO(true, AMD64_ARCH)
                    .getJsonSubscriptions();

            JsonSubscription resultSubscription1 = result.stream()
                    .filter(rs -> rs.getId() == 9998L)
                    .findFirst().get();

            assertEquals("662644474670", resultSubscription1.getPartNumber());
            assertEquals(new Long(10), resultSubscription1.getQuantity());
            assertEquals("55REGCODE180", resultSubscription1.getRegcode());
            assertTrue(resultSubscription1.getProductIds().contains(1322L));
            assertTrue(resultSubscription1.getProductIds().contains(1324L));
            assertEquals("extFile", resultSubscription1.getSccUsername());

            JsonSubscription resultSubscription2 = result.stream()
                    .filter(rs -> rs.getId() == 9999L)
                    .findFirst().get();

            assertEquals("874-005117", resultSubscription2.getPartNumber());
            assertEquals(new Long(100), resultSubscription2.getQuantity());
            assertEquals("55REGCODE180", resultSubscription2.getRegcode());
            assertTrue(resultSubscription2.getProductIds().contains(1322L));
            assertTrue(resultSubscription2.getProductIds().contains(1324L));
            assertEquals("extFile", resultSubscription2.getSccUsername());
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

            SUSEProductFactory.clearAllProducts();
            SUSEProductTestUtils.createVendorSUSEProducts();
            SUSEProductTestUtils.createVendorEntitlementProducts();
            LoggingFactory.clearLogId();

            Server h1 = ServerTestUtils.createTestSystem();
            h1.setName("host1.example.com");
            h1.setCpu(createCPU(h1, 8L));
            ServerFactory.save(h1);

            Set<InstalledProduct> installedProducts = new HashSet<>();
            InstalledProduct instPrd = new InstalledProduct();
            instPrd.setName("SLES");
            instPrd.setVersion("12.1");
            instPrd.setRelease("0");
            instPrd.setArch(PackageFactory.lookupPackageArchByLabel("x86_64"));
            instPrd.setBaseproduct(true);
            installedProducts.add(instPrd);

            instPrd = new InstalledProduct();
            instPrd.setName("sle-ha");
            instPrd.setVersion("12.1");
            instPrd.setRelease("0");
            instPrd.setArch(PackageFactory.lookupPackageArchByLabel("x86_64"));
            instPrd.setBaseproduct(false);
            installedProducts.add(instPrd);

            h1.setInstalledProducts(installedProducts);

            ContentSyncManager cm = new ContentSyncManager();
            Collection<SCCSubscription> s = cm.getSubscriptions();
            HibernateFactory.getSession().flush();

            PinnedSubscription pin = new PinnedSubscription();
            pin.setServer(h1);
            pin.setOrderitemId(9999L);
            TestUtils.saveAndFlush(pin);
            HibernateFactory.getSession().clear();

            List<JsonPinnedMatch> result = new MatcherJsonIO(true, AMD64_ARCH)
                    .getJsonPinnedMatches();
            Optional<JsonPinnedMatch> resultPin = result.stream()
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

    private CPU createCPU(Server s, Long sockets) {
        CPU cpu = new CPU();
        cpu.setNrsocket(sockets);
        cpu.setServer(s);
        cpu.setArch(ServerFactory.lookupCPUArchByName("x86_64"));
        return cpu;
    }
}
