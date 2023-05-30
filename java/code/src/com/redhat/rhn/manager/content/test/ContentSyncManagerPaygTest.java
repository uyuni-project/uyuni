/*
 * Copyright (c) 2023 SUSE LLC
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
package com.redhat.rhn.manager.content.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.channel.ChannelFamilyFactory;
import com.redhat.rhn.domain.cloudpayg.CloudRmtHostFactory;
import com.redhat.rhn.domain.cloudpayg.PaygSshData;
import com.redhat.rhn.domain.cloudpayg.PaygSshDataFactory;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.notification.UserNotificationFactory;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.scc.SCCCachingFactory;
import com.redhat.rhn.domain.scc.SCCRepository;
import com.redhat.rhn.domain.scc.SCCRepositoryAuth;
import com.redhat.rhn.manager.content.ContentSyncManager;
import com.redhat.rhn.taskomatic.task.payg.PaygAuthDataExtractor;
import com.redhat.rhn.taskomatic.task.payg.PaygUpdateAuthTask;
import com.redhat.rhn.taskomatic.task.payg.beans.PaygInstanceInfo;
import com.redhat.rhn.taskomatic.task.payg.beans.PaygProductInfo;
import com.redhat.rhn.taskomatic.task.payg.test.PaygUpdateAuthTaskTest;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.TestUtils;

import com.suse.cloud.CloudPaygManager;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quartz.JobExecutionException;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ContentSyncManagerPaygTest extends RhnBaseTestCase {
    private static final String JARPATH = "/com/redhat/rhn/manager/content/test/";
    private static final String PRODUCTS_UNSCOPED = JARPATH + "rmtclouddata/organizations_products_unscoped.json";
    private static final String PRODUCT_TREE = JARPATH + "rmtclouddata/product_tree.json";
    private static final String UPGRADE_PATHS = JARPATH + "upgrade_paths.json";
    private static final String CHANNEL_FAMILY = JARPATH + "channel_families.json";
    private static final String ADDITIONAL_PRODUCTS = JARPATH + "additional_products.json";

    private static final PaygUpdateAuthTask PAYG_DATA_TASK = new PaygUpdateAuthTask();

    static {
        PaygAuthDataExtractor paygAuthDataExtractorMock = new PaygAuthDataExtractor() {
            protected PaygInstanceInfo extractAuthDataLocal() {
                List<PaygProductInfo> products = new LinkedList<>();
                products.add(new PaygProductInfo("SUSE-Manager-Server", "4.3", "x86_64"));
                products.add(new PaygProductInfo("sle-module-basesystem", "15.4", "x86_64"));

                Map<String, String> basicAuth = new HashMap<>();
                basicAuth.put("username", "SCC_05c394f");
                basicAuth.put("password", "0e248802");

                String headerAuth = "X-Instance-Data:PGRvY3VtZW50PnsKICAiYWNjb3VudElkIiA6ICI2NDEwODAwN";

                Map<String, String> rmtHost = new HashMap<>();
                rmtHost.put("hostname", "smt-ec2.susecloud.net");
                rmtHost.put("ip", "18.156.40.199");
                rmtHost.put("server_ca", "-----BEGIN CERTIFICATE-----");

                return new PaygInstanceInfo(products, basicAuth, headerAuth, rmtHost);
            }
        };
        PAYG_DATA_TASK.setPaygDataExtractor(paygAuthDataExtractorMock);
    }

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
        clearDb();
    }

    private void clearDb() {
        SCCCachingFactory.clearRepositories();
        SCCCachingFactory.clearSubscriptions();
        SUSEProductFactory.findAllSUSEProducts().forEach(SUSEProductFactory::remove);
        CredentialsFactory.listSCCCredentials().forEach(CredentialsFactory::removeCredentials);
        CloudRmtHostFactory.lookupCloudRmtHosts().forEach(CloudRmtHostFactory::deleteCloudRmtHost);
        PaygSshDataFactory.lookupPaygSshData().forEach(PaygSshDataFactory::deletePaygSshData);
        UserNotificationFactory.deleteNotificationMessagesBefore(Date.from(Instant.now()));
        ChannelFamilyFactory.getAllChannelFamilies()
                .stream()
                .filter(ChannelFamily::isPublic)
                .forEach(ChannelFamilyFactory::remove);
        HibernateFactory.commitTransaction();
    }

    @Test
    public void testRefreshPayg() throws Exception {
        Config.get().setString(ConfigDefaults.PRODUCT_TREE_TAG, "SUMA4.3");
        CloudPaygManager mgr = new CloudPaygManager() {
            @Override
            public boolean isPaygInstance() {
                return true;
            }
        };
        PAYG_DATA_TASK.setCloudPaygManager(mgr);

        WireMockServer wireMockServer = setupWireMockServer();
        Path tmpLogDir = Files.createTempDirectory("scc-data");
        try {
            // first run required to have an Cloud RMT Server to download products from
            runPaygUpdateAuthTaskAndSetHost("http://localhost:8888/repo");

            // download the product data from Cloud RMT
            ContentSyncManager csm = new ContentSyncManager(tmpLogDir, mgr);
            csm.setUpgradePathsJson(new File(TestUtils.findTestData(UPGRADE_PATHS).getPath()));
            csm.setChannelFamiliesJson(new File(TestUtils.findTestData(CHANNEL_FAMILY).getPath()));
            csm.setAdditionalProductsJson(new File(TestUtils.findTestData(ADDITIONAL_PRODUCTS).getPath()));
            csm.updateChannelFamilies(csm.readChannelFamilies());
            csm.updateSUSEProducts(csm.getProducts());
            csm.updateRepositories(null);
            csm.updateSubscriptions();

            // Clear Hibernate cache to drop half loaded objects
            HibernateFactory.commitTransaction();
            HibernateFactory.closeSession();

            // second run required to have the repository auth data for Cloud RMT products
            runPaygUpdateAuthTaskAndSetHost("http://localhost:8888/repo");

            // enable free products
            csm.updateChannelFamilies(csm.readChannelFamilies());
            csm.updateSUSEProducts(csm.getProducts());
            csm.updateRepositories(null);
            csm.updateSubscriptions();

            List<SCCRepositoryAuth> auth = SCCCachingFactory.lookupRepositoryAuth();
            assertFalse(auth.isEmpty());

            Set<String> reponames = auth.stream()
                    .filter(a -> a.cloudRmtAuth().isPresent())
                    .map(SCCRepositoryAuth::getRepo)
                    .map(SCCRepository::getName)
                    .collect(Collectors.toSet());
            assertContains(reponames, "SLE-Product-SUSE-Manager-Server-4.2-Pool");
            assertContains(reponames, "SLE-Product-SUSE-Manager-Server-4.2-Updates");
            assertContains(reponames, "SLE-Product-SUSE-Manager-Server-4.3-Pool");
            assertContains(reponames, "SLE-Product-SUSE-Manager-Server-4.3-Updates");
            assertContains(reponames, "SLE-Module-Basesystem15-SP3-Pool");
            assertContains(reponames, "SLE-Module-Basesystem15-SP4-Pool");
            assertContains(reponames, "SLE-Manager-Tools12-Pool");
            assertContains(reponames, "SLE-Manager-Tools12-Updates");
            assertContains(reponames, "SLE-Manager-Tools15-Pool");
            assertContains(reponames, "SLE-Manager-Tools15-Updates");
            assertContains(reponames, "SLE-Manager-Tools-For-Micro5-Pool");
            assertContains(reponames, "SLE-Manager-Tools-For-Micro5-Updates");
            assertContains(reponames, "SLE-Product-SUSE-Manager-Proxy-4.3-Pool");
            assertContains(reponames, "SLE-Product-SUSE-Manager-Proxy-4.3-Updates");
            assertContains(reponames, "RES-7-SUSE-Manager-Tools");
            assertContains(reponames, "RES8-Manager-Tools-Pool");
            assertContains(reponames, "RES8-Manager-Tools-Updates");
            assertContains(reponames, "EL9-Manager-Tools-Pool");
            assertContains(reponames, "EL9-Manager-Tools-Updates");

            reponames = auth.stream()
                    .filter(a -> a.noAuth().isPresent())
                    .map(SCCRepositoryAuth::getRepo)
                    .map(SCCRepository::getName)
                    .collect(Collectors.toSet());
            assertContains(reponames, "rockylinux-8");
            assertContains(reponames, "rockylinux-9");
            assertContains(reponames, "oraclelinux7");
            assertContains(reponames, "oraclelinux8");
            assertContains(reponames, "oraclelinux9");
            assertContains(reponames, "almalinux8");
            assertContains(reponames, "almalinux9");
            assertContains(reponames, "debian-11-pool");
            assertContains(reponames, "ubuntu-2204-amd64-main");
        }
        finally {
            wireMockServer.stop();
            if (Objects.nonNull(tmpLogDir)) {
                FileUtils.forceDelete(tmpLogDir.toFile());
            }
        }
    }

    private void runPaygUpdateAuthTaskAndSetHost(String url) throws JobExecutionException {
        PAYG_DATA_TASK.execute(null);
        for (PaygSshData outPaygData : PaygSshDataFactory.lookupPaygSshData()) {
            switch (outPaygData.getHost()) {
                case "localhost":
                    //Fake URL for next test
                    outPaygData.getCredentials().setUrl(url);
                    break;
                default:
                    fail("unexpected result");
            }
        }
    }
    private WireMockServer setupWireMockServer() {
        WireMockServer wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(8888));
        wireMockServer.start();

        WireMock.configureFor("localhost", 8888);
        String productsUnscoped = new BufferedReader(new InputStreamReader(
                PaygUpdateAuthTaskTest.class.getResourceAsStream(PRODUCTS_UNSCOPED)))
                .lines().collect(Collectors.joining("\n"));

        String productTree = new BufferedReader(new InputStreamReader(
                PaygUpdateAuthTaskTest.class.getResourceAsStream(PRODUCT_TREE)))
                .lines().collect(Collectors.joining("\n"));

        WireMock.stubFor(
                WireMock.get(WireMock.urlPathEqualTo("/connect/organizations/products/unscoped"))
                        .willReturn(WireMock.aResponse()
                                .withStatus(HttpURLConnection.HTTP_OK)
                                .withHeader("Content-Type", "application/json")
                                .withBody(productsUnscoped)));

        WireMock.stubFor(
                WireMock.get(WireMock.urlPathEqualTo("/suma/product_tree.json"))
                        .willReturn(WireMock.aResponse()
                                .withStatus(HttpURLConnection.HTTP_OK)
                                .withHeader("Content-Type", "application/json")
                                .withBody(productTree)));

        return wireMockServer;
    }
}
