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

import com.suse.manager.matcher.MatcherJsonIO;
import com.suse.scc.model.SCCSubscription;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

public class MatcherJsonIOTest extends TestCase {
    private static final String JARPATH = "/com/redhat/rhn/manager/content/test/sccdata/";
    private static final String SUBSCRIPTIONS_JSON = "organizations_subscriptions.json";
    private static final String ORDERS_JSON = "organizations_orders.json";

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

        String jsonString = new MatcherJsonIO().getJsonSystems();
        assertNotNull(jsonString);
        assertTrue(jsonString.contains("\"name\": \"host1.example.com\""));
        assertTrue(jsonString.contains("\"name\": \"guest1.example.com\""));
        assertTrue(jsonString.contains("\"name\": \"guest2.example.com\""));
        assertTrue(jsonString.contains("      " + g1.getId()));
        assertTrue(jsonString.contains("      " + g2.getId()));
        assertTrue(jsonString.contains("1322"));
        assertTrue(jsonString.contains("1324"));
        assertTrue(jsonString.contains("1076"));
        assertTrue(jsonString.contains("1097"));
    }

    public void testSubscriptionsToJson() throws Exception {
        File subJson = new File(TestUtils.findTestData(
                new File(JARPATH, SUBSCRIPTIONS_JSON).getAbsolutePath()).getPath());
        File orderJson = new File(TestUtils.findTestData(
                new File(JARPATH, ORDERS_JSON).getAbsolutePath()).getPath());
        //String fromdir = subJson.getParent();
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
            String jsonString = new MatcherJsonIO().getJsonSubscriptions();
            assertNotNull(jsonString);
            assertTrue(jsonString.contains("\"part_number\": \"662644474670\""));
            assertTrue(jsonString.contains("\"quantity\": 10,"));
            assertTrue(jsonString.contains("\"regcode\": \"55REGCODE180\""));
            assertTrue(jsonString.contains("1324"));
            assertTrue(jsonString.contains("1322"));
            assertTrue(jsonString.contains("\"scc_username\": \"extFile\""));
            assertTrue(jsonString.contains("\"id\": 9998"));
            assertTrue(jsonString.contains("\"id\": 9999"));
            assertTrue(jsonString.contains("\"quantity\": 100,"));
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
        String fromdir = subJson.getParent();
        try {
            Config.get().setString(ContentSyncManager.RESOURCE_PATH, fromdir);

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

            String jsonString = new MatcherJsonIO().getJsonPinnedMatches();
            assertNotNull(jsonString);
            assertTrue(jsonString.contains("\"system_id\": " + h1.getId()));
            assertTrue(jsonString.contains("\"subscription_id\": 9999"));
        }
        finally {
            Config.get().remove(ContentSyncManager.RESOURCE_PATH);
            SUSEProductTestUtils.deleteIfTempFile(subJson);
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
