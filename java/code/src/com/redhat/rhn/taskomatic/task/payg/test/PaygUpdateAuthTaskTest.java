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

package com.redhat.rhn.taskomatic.task.payg.test;

import static com.redhat.rhn.testing.RhnBaseTestCase.assertContains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ContentSource;
import com.redhat.rhn.domain.channel.SslContentSource;
import com.redhat.rhn.domain.cloudpayg.CloudRmtHostFactory;
import com.redhat.rhn.domain.cloudpayg.PaygSshData;
import com.redhat.rhn.domain.cloudpayg.PaygSshDataFactory;
import com.redhat.rhn.domain.credentials.CloudCredentials;
import com.redhat.rhn.domain.credentials.CloudRMTCredentials;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.credentials.RHUICredentials;
import com.redhat.rhn.domain.notification.UserNotificationFactory;
import com.redhat.rhn.domain.notification.types.NotificationType;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.scc.SCCCachingFactory;
import com.redhat.rhn.manager.content.ContentSyncManager;
import com.redhat.rhn.taskomatic.task.payg.PaygAuthDataExtractor;
import com.redhat.rhn.taskomatic.task.payg.PaygDataExtractException;
import com.redhat.rhn.taskomatic.task.payg.PaygUpdateAuthTask;
import com.redhat.rhn.taskomatic.task.payg.beans.PaygInstanceInfo;
import com.redhat.rhn.taskomatic.task.payg.beans.PaygProductInfo;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.MockFileLocks;

import com.suse.cloud.CloudPaygManager;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jcraft.jsch.JSchException;

import org.apache.commons.io.FileUtils;
import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quartz.JobExecutionException;

import java.io.BufferedReader;
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
import java.util.Optional;
import java.util.stream.Collectors;

public class PaygUpdateAuthTaskTest extends JMockBaseTestCaseWithUser {

    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .create();

    private static final String PRODUCTS_UNSCOPED =
            "/com/redhat/rhn/manager/content/test/smallBase/productsUnscoped.json";
    private static final String PRODUCT_TREE = "/com/redhat/rhn/manager/content/test/smallBase/product_tree.json";

    private PaygAuthDataExtractor paygAuthDataExtractorMock;

    private PaygUpdateAuthTask paygUpdateAuthTask;

    private PaygSshData paygData;
    private PaygInstanceInfo paygInstanceInfo;
    private ContentSyncManager contentSyncManagerMock;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        clearDb();

        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);

        paygAuthDataExtractorMock = mock(PaygAuthDataExtractor.class);
        contentSyncManagerMock = mock(ContentSyncManager.class);

        paygUpdateAuthTask = new PaygUpdateAuthTask();

        paygUpdateAuthTask.setCloudPaygManager(new CloudPaygManager());
        paygUpdateAuthTask.setPaygDataExtractor(paygAuthDataExtractorMock);
        paygUpdateAuthTask.setContentSyncManager(contentSyncManagerMock);
        paygUpdateAuthTask.setSccRefreshLock(new MockFileLocks());

        paygData = createPaygSshData();
        PaygSshDataFactory.savePaygSshData(paygData);
        paygInstanceInfo = createPaygInstanceInfo();
        HibernateFactory.commitTransaction();
        HibernateFactory.closeSession();
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
        CredentialsFactory.listCredentials().forEach(CredentialsFactory::removeCredentials);
        CloudRmtHostFactory.lookupCloudRmtHosts().forEach(CloudRmtHostFactory::deleteCloudRmtHost);
        PaygSshDataFactory.lookupPaygSshData().forEach(PaygSshDataFactory::deletePaygSshData);
        UserNotificationFactory.deleteNotificationMessagesBefore(Date.from(Instant.now()));
        HibernateFactory.commitTransaction();
    }

    @Test
    public void testLocalhostPaygConnection() throws Exception {
        checking(new Expectations() {
            {
                allowing(contentSyncManagerMock).updateRepositoriesPayg();
                oneOf(paygAuthDataExtractorMock).extractAuthData(with(any(PaygSshData.class)));
                will(returnValue(paygInstanceInfo));
                oneOf(paygAuthDataExtractorMock).extractAuthData(with(any(PaygSshData.class)));
                will(returnValue(paygInstanceInfo));
                oneOf(paygAuthDataExtractorMock).extractAuthData(with(any(PaygSshData.class)));
                will(returnValue(paygInstanceInfo));
        }});
        CloudPaygManager mgr = new CloudPaygManager() {
            @Override
            public boolean isPaygInstance() {
                return true;
            }
        };
        paygUpdateAuthTask.setCloudPaygManager(mgr);
        paygUpdateAuthTask.execute(null);
        for (PaygSshData outPaygData : PaygSshDataFactory.lookupPaygSshData()) {
            switch (outPaygData.getHost()) {
                case "localhost":
                    assertEquals("SUSE Manager PAYG", outPaygData.getDescription());
                    assertEquals("root", outPaygData.getUsername());
                    Optional<CloudRMTCredentials> creds = outPaygData.getCredentials()
                        .castAs(CloudRMTCredentials.class);
                    assertTrue(creds.isPresent());
                    assertEquals("https://smt-ec2.susecloud.net/repo", creds.get().getUrl());
                    //Fake URL for next test
                    creds.get().setUrl("http://localhost:8888/repo");
                    break;
                case "my-instance":
                    assertEquals("username", outPaygData.getUsername());
                    assertEquals("password", outPaygData.getPassword());
                    assertEquals(21, outPaygData.getPort());
                    break;
                default:
                    assertTrue(false, "unexpected result");
            }
        }

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

        Path tmpLogDir = Files.createTempDirectory("scc-data");
        try {
            ContentSyncManager csm = new ContentSyncManager(tmpLogDir, mgr);
            csm.updateSUSEProducts(csm.getProducts());

            WireMock.verify(WireMock.getRequestedFor(
                WireMock.urlPathEqualTo("/connect/organizations/products/unscoped")));
            WireMock.verify(WireMock.getRequestedFor(
                WireMock.urlPathEqualTo("/suma/product_tree.json")));
            wireMockServer.stop();
        }
        finally {
            if (Objects.nonNull(tmpLogDir)) {
                FileUtils.forceDelete(tmpLogDir.toFile());
            }
        }

        mgr = new CloudPaygManager() {
            @Override
            public boolean isPaygInstance() {
                return false;
            }
        };
        paygUpdateAuthTask.setCloudPaygManager(mgr);
        paygUpdateAuthTask.execute(null);
        for (PaygSshData outPaygData : PaygSshDataFactory.lookupPaygSshData()) {
            switch (outPaygData.getHost()) {
                case "my-instance":
                    assertEquals("username", outPaygData.getUsername());
                    assertEquals("password", outPaygData.getPassword());
                    assertEquals(21, outPaygData.getPort());
                    break;
                default:
                    assertTrue(false, "unexpected result");
            }
        }
    }

    @Test
    public void testRHUIConnection() throws Exception {
        PaygInstanceInfo rhuiInstanceInfo = createRHUIInstanceInfo();
        checking(new Expectations() {
            {
                allowing(contentSyncManagerMock).updateRepositoriesPayg();
                oneOf(paygAuthDataExtractorMock).extractAuthData(with(any(PaygSshData.class)));
                will(returnValue(rhuiInstanceInfo));
            }});
        paygUpdateAuthTask.execute(null);
        for (PaygSshData outPaygData : PaygSshDataFactory.lookupPaygSshData()) {
            CloudCredentials creds = outPaygData.getCredentials();
            assertInstanceOf(RHUICredentials.class, creds);
            Map<String, String> extraAuthData = GSON.fromJson(new String(creds.getExtraAuthData()), Map.class);
            assertEquals(extraAuthData.size(), 2);
            assertEquals("PGRvY3VtZW50PnsKICAiYWNjb3VudElkIiA6ICI2NDEwODAwN", extraAuthData.get("X-RHUI-ID"));
            assertEquals("WStaWWtsN0dNY1FJeHNLK3BPYlcyZ3JqeHBFR3g4TkRPejBtRmdEakJW",
                    extraAuthData.get("X-RHUI-SIGNATURE"));


            for (ContentSource cs : ChannelFactory.lookupContentSources(OrgFactory.lookupById(1L))) {
                long cid = creds.getId();
                String dCertFmt = String.format("RHUI %s %s (C%d)", "Client Certificate", "%s", cid);
                String dKeyFmt = String.format("RHUI %s %s (C%d)", "Private Key", "%s", cid);
                String dCa = String.format("RHUI %s %s (C%d)", "CA Certificate", "ca-cert.crt", cid);

                if (cs.getLabel().equals("repo-label-1-c" + cid)) {
                    String dCert = String.format(dCertFmt, "repo-1.crt");
                    String dKey = String.format(dKeyFmt, "repo-1.key");

                    assertEquals("http://example.domain.top/path/to/repository_1?credentials=mirrcred_" + cid,
                            cs.getSourceUrl());
                    assertEquals(1, cs.getSslSets().size());
                    SslContentSource sslcerts = cs.getSslSets().stream().findFirst().orElseThrow();

                    assertEquals(dCert, sslcerts.getClientCert().getDescription());
                    assertEquals("CLIENT CERTIFICATE 1", sslcerts.getClientCert().getKeyString());

                    assertEquals(dKey, sslcerts.getClientKey().getDescription());
                    assertEquals("CLIENT PRIVATE KEY 1", sslcerts.getClientKey().getKeyString());

                    assertEquals(dCa, sslcerts.getCaCert().getDescription());
                    assertEquals("CA CERTIFICATE", sslcerts.getCaCert().getKeyString());
                }
                else if (cs.getLabel().equals("repo-label-2-c" + cid)) {
                    String dCert = String.format(dCertFmt, "repo-2.crt");
                    String dKey = String.format(dKeyFmt, "repo-2.key");

                    assertEquals("http://example.domain.top/path/to/repository_2?credentials=mirrcred_" + cid,
                            cs.getSourceUrl());
                    assertEquals(1, cs.getSslSets().size());
                    SslContentSource sslcerts = cs.getSslSets().stream().findFirst().orElseThrow();

                    assertEquals(dCert, sslcerts.getClientCert().getDescription());
                    assertEquals("CLIENT CERTIFICATE 2", sslcerts.getClientCert().getKeyString());

                    assertEquals(dKey, sslcerts.getClientKey().getDescription());
                    assertEquals("CLIENT PRIVATE KEY 2", sslcerts.getClientKey().getKeyString());

                    assertEquals(dCa, sslcerts.getCaCert().getDescription());
                    assertEquals("CA CERTIFICATE", sslcerts.getCaCert().getKeyString());
                }
            }
        }
    }

    @Test
    public void testJschException() throws Exception {
        checking(new Expectations() {
            {
                allowing(contentSyncManagerMock).updateRepositoriesPayg();
                oneOf(paygAuthDataExtractorMock).extractAuthData(with(any(PaygSshData.class)));
                will(throwException(new JSchException("My JSchException exception")));
            }});
        paygUpdateAuthTask.execute(null);
        paygData = HibernateFactory.reload(paygData);
        assertContains(paygData.getErrorMessage(), "My JSchException exception");
        assertEquals(paygData.getStatus(), PaygSshData.Status.E);
        assertEquals(1, UserNotificationFactory.listAllNotificationMessages().size());
        assertEquals(NotificationType.PaygAuthenticationUpdateFailed,
                UserNotificationFactory.listAllNotificationMessages().get(0).getType());
    }

    @Test
    public void testPaygDataExtractException() throws Exception {
        checking(new Expectations() {
            {
                allowing(contentSyncManagerMock).updateRepositoriesPayg();
                oneOf(paygAuthDataExtractorMock).extractAuthData(with(any(PaygSshData.class)));
                will(throwException(new PaygDataExtractException("My PaygDataExtractException")));
            }});
        paygUpdateAuthTask.execute(null);
        paygData = HibernateFactory.reload(paygData);
        assertContains(paygData.getErrorMessage(), "My PaygDataExtractException");
        assertEquals(paygData.getStatus(), PaygSshData.Status.E);
        assertEquals(1, UserNotificationFactory.listAllNotificationMessages().size());
        assertEquals(NotificationType.PaygAuthenticationUpdateFailed,
                UserNotificationFactory.listAllNotificationMessages().get(0).getType());
    }

    /**
     * Test PAYG Instance shutdown. On first Error the credentials should not yet been invalidated.
     * This should happen on the second try which failed. When the connection is restored,
     * the credentials should be restored as well.
     * @throws Exception
     */
    @Test
    public void testPaygDataExtractExceptionInvalidateCredentials() throws Exception {
        checking(new Expectations() {
            {
                allowing(contentSyncManagerMock).updateRepositoriesPayg();
                oneOf(paygAuthDataExtractorMock).extractAuthData(with(any(PaygSshData.class)));
                will(returnValue(paygInstanceInfo));
                oneOf(paygAuthDataExtractorMock).extractAuthData(with(any(PaygSshData.class)));
                will(throwException(new PaygDataExtractException("My PaygDataExtractException")));
                oneOf(paygAuthDataExtractorMock).extractAuthData(with(any(PaygSshData.class)));
                will(throwException(new PaygDataExtractException("My PaygDataExtractException")));
                oneOf(paygAuthDataExtractorMock).extractAuthData(with(any(PaygSshData.class)));
                will(returnValue(paygInstanceInfo));
            }});
        // first call successfull - set credentials and a header
        paygUpdateAuthTask.execute(null);
        paygData = HibernateFactory.reload(paygData);
        assertEquals(paygData.getStatus(), PaygSshData.Status.S);
        Optional<CloudRMTCredentials> creds = paygData.getCredentials().castAs(CloudRMTCredentials.class);
        assertTrue(creds.isPresent());
        assertEquals("0e248802", creds.get().getPassword());
        assertEquals("{\"X-Instance-Data\":\"PGRvY3VtZW50PnsKICAiYWNjb3VudElkIiA6ICI2NDEwODAwN\"}",
                new String(creds.get().getExtraAuthData()));

        // second call failed - set status to Error, but keep credentials
        paygUpdateAuthTask.execute(null);
        paygData = HibernateFactory.reload(paygData);

        assertContains(paygData.getErrorMessage(), "My PaygDataExtractException");
        assertEquals(paygData.getStatus(), PaygSshData.Status.E);
        assertEquals(1, UserNotificationFactory.listAllNotificationMessages().size());
        assertEquals(NotificationType.PaygAuthenticationUpdateFailed,
                UserNotificationFactory.listAllNotificationMessages().get(0).getType());
        creds = paygData.getCredentials().castAs(CloudRMTCredentials.class);
        assertTrue(creds.isPresent());
        assertEquals("0e248802", creds.get().getPassword());
        assertEquals("{\"X-Instance-Data\":\"PGRvY3VtZW50PnsKICAiYWNjb3VudElkIiA6ICI2NDEwODAwN\"}",
                new String(creds.get().getExtraAuthData()));

        // third call failed - invalidate credentials
        paygUpdateAuthTask.execute(null);
        paygData = HibernateFactory.reload(paygData);

        assertContains(paygData.getErrorMessage(), "My PaygDataExtractException");
        assertEquals(paygData.getStatus(), PaygSshData.Status.E);
        creds = paygData.getCredentials().castAs(CloudRMTCredentials.class);
        assertTrue(creds.isPresent());
        assertEquals("invalidated", creds.get().getPassword());
        assertEquals("{}", new String(creds.get().getExtraAuthData()));

        // forth call successfull - restore credentials and a header again
        paygUpdateAuthTask.execute(null);
        paygData = HibernateFactory.reload(paygData);
        assertEquals(paygData.getStatus(), PaygSshData.Status.S);
        creds = paygData.getCredentials().castAs(CloudRMTCredentials.class);
        assertTrue(creds.isPresent());
        assertEquals("0e248802", creds.get().getPassword());
        assertEquals("{\"X-Instance-Data\":\"PGRvY3VtZW50PnsKICAiYWNjb3VudElkIiA6ICI2NDEwODAwN\"}",
                new String(creds.get().getExtraAuthData()));
    }

    @Test
    public void testGenericException() throws Exception {
        checking(new Expectations() {
            {
                allowing(contentSyncManagerMock).updateRepositoriesPayg();
                oneOf(paygAuthDataExtractorMock).extractAuthData(with(any(PaygSshData.class)));
                will(throwException(new Exception("My Exception")));
            }});
        paygUpdateAuthTask.execute(null);
        paygData = HibernateFactory.reload(paygData);
        assertNull(paygData.getErrorMessage());
        assertEquals(paygData.getStatus(), PaygSshData.Status.E);
        assertEquals(1, UserNotificationFactory.listAllNotificationMessages().size());
        assertEquals(NotificationType.PaygAuthenticationUpdateFailed,
                UserNotificationFactory.listAllNotificationMessages().get(0).getType());
    }

    @Test
    public void testSuccessClearStatus() throws Exception {
        checking(new Expectations() {
            {
                allowing(contentSyncManagerMock).updateRepositoriesPayg();
                oneOf(paygAuthDataExtractorMock).extractAuthData(with(any(PaygSshData.class)));
                will(returnValue(paygInstanceInfo));
            }});
        paygUpdateAuthTask.execute(null);
        paygData = HibernateFactory.reload(paygData);
        assertNull(paygData.getErrorMessage());
        assertEquals(paygData.getStatus(), PaygSshData.Status.S);
        assertEquals(0, UserNotificationFactory.listAllNotificationMessages().size());
    }

    @Test
    public void doNotCallContentSyncManagerIfNoSshDataConnectionIsDefined() throws JobExecutionException {
        PaygSshDataFactory.lookupPaygSshData().forEach(PaygSshDataFactory::deletePaygSshData);
        commitAndCloseSession();

        checking(new Expectations() {{
            never(contentSyncManagerMock).updateRepositoriesPayg();
        }});

        paygUpdateAuthTask.execute(null);
    }

    private PaygSshData createPaygSshData() {
        PaygSshData paygSshData = PaygSshDataFactory.createPaygSshData();
        paygSshData.setDescription("My special instance");
        paygSshData.setHost("my-instance");
        paygSshData.setPort(21);
        paygSshData.setUsername("username");
        paygSshData.setPassword("password");
        paygSshData.setKey("key");
        paygSshData.setKeyPassword("keyPassword");
        paygSshData.setErrorMessage("My status");
        return paygSshData;
    }

    private PaygInstanceInfo createPaygInstanceInfo() {
        List<PaygProductInfo> products = new LinkedList<>();
        products.add(new PaygProductInfo("SLES", "15.2", "x86_64"));
        products.add(new PaygProductInfo("sle-module-basesystem", "15.2", "x86_64"));

        Map<String, String> basicAuth = new HashMap<>();
        basicAuth.put("username", "SCC_05c394f");
        basicAuth.put("password", "0e248802");

        Map<String, String> headerAuth = new HashMap<>();
        headerAuth.put("X-Instance-Data", "PGRvY3VtZW50PnsKICAiYWNjb3VudElkIiA6ICI2NDEwODAwN");

        Map<String, String> rmtHost = new HashMap<>();
        rmtHost.put("hostname", "smt-ec2.susecloud.net");
        rmtHost.put("ip", "18.156.40.199");
        rmtHost.put("server_ca", "-----BEGIN CERTIFICATE-----");

        return new PaygInstanceInfo(products, basicAuth, headerAuth, rmtHost);
    }

    private PaygInstanceInfo createRHUIInstanceInfo() {
        Map<String, String> headerAuth = new HashMap<>();
        headerAuth.put("X-RHUI-ID", "PGRvY3VtZW50PnsKICAiYWNjb3VudElkIiA6ICI2NDEwODAwN");
        headerAuth.put("X-RHUI-SIGNATURE", "WStaWWtsN0dNY1FJeHNLK3BPYlcyZ3JqeHBFR3g4TkRPejBtRmdEakJW");

        Map<String, Map<String, String>> repositories = new HashMap<>();
        Map<String, String> repodata = new HashMap<>();
        repodata.put("url", "http://example.domain.top/path/to/repository_1");
        repodata.put("sslclientcert", "/etc/pki/rhui/product/repo-1.crt");
        repodata.put("sslclientkey", "/etc/pki/rhui/repo-1.key");
        repodata.put("sslcacert", "/etc/pki/rhui/ca-cert.crt");
        repositories.put("repo-label-1", repodata);

        repodata = new HashMap<>();
        repodata.put("url", "http://example.domain.top/path/to/repository_2");
        repodata.put("sslclientcert", "/etc/pki/rhui/product/repo-2.crt");
        repodata.put("sslclientkey", "/etc/pki/rhui/repo-2.key");
        repodata.put("sslcacert", "/etc/pki/rhui/ca-cert.crt");
        repositories.put("repo-label-2", repodata);

        Map<String, String> certs = new HashMap<>();
        certs.put("/etc/pki/rhui/product/repo-1.crt", "CLIENT CERTIFICATE 1");
        certs.put("/etc/pki/rhui/repo-1.key", "CLIENT PRIVATE KEY 1");
        certs.put("/etc/pki/rhui/ca-cert.crt", "CA CERTIFICATE");
        certs.put("/etc/pki/rhui/product/repo-2.crt", "CLIENT CERTIFICATE 2");
        certs.put("/etc/pki/rhui/repo-2.key", "CLIENT PRIVATE KEY 2");

        PaygInstanceInfo info = new PaygInstanceInfo(headerAuth, certs, repositories);
        return info;
    }
}
