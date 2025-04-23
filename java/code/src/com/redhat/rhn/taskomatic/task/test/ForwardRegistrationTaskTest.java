/*
 * Copyright (c) 2025 SUSE LLC
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

package com.redhat.rhn.taskomatic.task.test;

import static com.suse.scc.SCCEndpoints.SHOULD_BE_REMOVED;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.util.http.HttpClientAdapter;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.credentials.HubSCCCredentials;
import com.redhat.rhn.domain.credentials.SCCCredentials;
import com.redhat.rhn.domain.scc.SCCCachingFactory;
import com.redhat.rhn.domain.scc.SCCRegCacheItem;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerInfo;
import com.redhat.rhn.manager.content.ContentSyncManager;
import com.redhat.rhn.taskomatic.task.ForwardRegistrationTask;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ServerTestUtils;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.hub.test.ControllerTestUtils;
import com.suse.manager.model.hub.HubFactory;
import com.suse.manager.model.hub.IssHub;
import com.suse.manager.model.hub.IssPeripheral;
import com.suse.manager.webui.services.SaltStateGeneratorService;
import com.suse.scc.SCCEndpoints;
import com.suse.scc.SCCSystemRegistrationManager;
import com.suse.scc.client.SCCClientException;
import com.suse.scc.client.SCCConfig;
import com.suse.scc.client.SCCConfigBuilder;
import com.suse.scc.client.SCCWebClient;
import com.suse.scc.model.SCCOrganizationSystemsUpdateResponse;
import com.suse.scc.model.SCCRegisterSystemJson;
import com.suse.scc.model.SCCSystemCredentialsJson;
import com.suse.scc.proxy.SCCProxyFactory;
import com.suse.scc.proxy.SCCProxyManager;
import com.suse.scc.proxy.SCCProxyRecord;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseFactory;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.util.EntityUtils;
import org.hibernate.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import spark.route.HttpMethod;

public class ForwardRegistrationTaskTest extends BaseTestCaseWithUser {

    private SCCCredentials primaryCredentials;
    private List<Server> servers;
    private List<SCCRegCacheItem> testSystems;

    private MockSCCWebClient mockSccWebClient;
    private MockForwardRegistrationTask mockForwardRegistrationTask;
    private SCCProxyFactory testSccProxyFactory;
    private SCCSystemRegistrationManager testSccSystemRegMan;

    private Integer systemSize;
    private Integer batchSize;
    private boolean allowFirstCallToFail;
    private boolean allowAllCallsToFail;

    private static final String HUB_FQDN = "hub.local";
    private static final String PERIPHERAL_FQDN = "peripheral.local";
    private static final String PERIPHERAL_USERNAME = "peripheral-000002";
    private static final String PERIPHERAL_PASSWD = "not so secret";

    static class MockSCCWebClient extends SCCWebClient {
        protected int callCnt;

        MockSCCWebClient(SCCConfig configIn) {
            super(configIn);
            callCnt = 0;
        }

        public int getCallCnt() {
            return callCnt;
        }

        public void resetCallCnt() {
            callCnt = 0;
        }
    }

    static class MockForwardRegistrationTask extends ForwardRegistrationTask {
        @Override
        public void executeSCCTasksCore(SCCSystemRegistrationManager sccRegManager,
                                        SCCProxyFactory sccProxyFactory,
                                        SCCCredentials sccPrimaryOrProxyCredentials) {
            super.executeSCCTasksCore(sccRegManager, sccProxyFactory, sccPrimaryOrProxyCredentials);
        }


        @Override
        public Optional<SCCCredentials> findSccCredentials() {
            return super.findSccCredentials();
        }
    }

    static class MockHttpClientAdapter extends HttpClientAdapter {
        MockHttpClientAdapter(SCCConfig configIn) {
            super(configIn.getAdditionalCerts(), false);
        }

        @Override
        public HttpResponse executeRequest(HttpRequestBase request, String username,
                                           String password) throws IOException {
            HttpResponseFactory factory = new DefaultHttpResponseFactory();
            HttpResponse response = factory.newHttpResponse(
                    new BasicStatusLine(new ProtocolVersion("http", 1, 1),
                            HttpStatus.SC_BAD_REQUEST, null), null);

            if (request.getMethod().compareToIgnoreCase("PUT") == 0) {
                executeRequestCreateSystems(request, username, password, response);
            }
            if (request.getMethod().compareToIgnoreCase("DELETE") == 0) {
                executeRequestDeleteSystems(request, username, password, response);
            }

            return response;
        }

        public void executeRequestCreateSystems(HttpRequestBase request, String username,
                                                String password, HttpResponse response) {
            try {
                HttpEntity entity = ((HttpPut) request).getEntity(); // example
                String requestBody = EntityUtils.toString(entity);

                String result = (String) ControllerTestUtils.simulateApiEndpointCallBasicAuth(
                        SHOULD_BE_REMOVED + "/connect/organizations/systems", HttpMethod.put,
                        username, password, requestBody);

                response.setEntity(new StringEntity(result, APPLICATION_JSON));
                response.setStatusCode(HttpServletResponse.SC_CREATED);
            }
            catch (Exception eIn) {
                throw new IllegalStateException(eIn);
            }
        }

        public void executeRequestDeleteSystems(HttpRequestBase request, String username,
                                                String password, HttpResponse response) {
            try {
                String url = request.getURI().getPath();
                int index = url.lastIndexOf("/");
                String id = url.substring(index + 1);

                Object res = ControllerTestUtils.simulateApiEndpointCallBasicAuth(
                        SHOULD_BE_REMOVED + "/connect/organizations/systems/:id",
                        HttpMethod.delete, username, password, id);

                response.setEntity(new StringEntity(res.toString(), APPLICATION_JSON));
                response.setStatusCode(HttpServletResponse.SC_CREATED);
            }
            catch (Exception eIn) {
                throw new IllegalStateException(eIn);
            }
        }
    }

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        systemSize = 15;
        batchSize = 3;
        allowFirstCallToFail = false;
        allowAllCallsToFail = false;
    }

    private void setupTest() throws Exception {
        setupCreateTestObjects();
        setupCreateTestSCCWebClient();
        testSccSystemRegMan = new SCCSystemRegistrationManager(mockSccWebClient, testSccProxyFactory);
        setupCreateSystems();
        mockSccWebClient.resetCallCnt();
    }

    private void setupCreateTestObjects() {
        primaryCredentials = CredentialsFactory.createSCCCredentials("username", "password");
        primaryCredentials.setUrl("https://scc.suse.com");
        CredentialsFactory.storeCredentials(primaryCredentials);

        mockForwardRegistrationTask = new MockForwardRegistrationTask();
        testSccProxyFactory = new SCCProxyFactory();
    }

    private void setupCreateSystems() throws Exception {
        Path tmpSaltRoot = Files.createTempDirectory("salt");
        SaltStateGeneratorService.INSTANCE.setSuseManagerStatesFilesRoot(tmpSaltRoot.toAbsolutePath());
        SaltStateGeneratorService.INSTANCE.setSkipSetOwner(true);
        Config.get().setString(ConfigDefaults.REG_BATCH_SIZE, String.valueOf(batchSize));

        servers = new ArrayList<>();
        for (int i = 0; i < systemSize; i++) {
            Server testSystem = ServerTestUtils.createTestSystem();
            ServerInfo serverInfo = testSystem.getServerInfo();
            serverInfo.setCheckin(new Date(0)); // 1970-01-01 00:00:00 UTC
            testSystem.setServerInfo(serverInfo);
            servers.add(testSystem);
        }

        SCCCachingFactory.initNewSystemsToForward();
        List<SCCRegCacheItem> allUnregistered = SCCCachingFactory.findSystemsToForwardRegistration();
        testSystems = allUnregistered.stream()
                .filter(i -> i.getOptServer().get().getServerInfo().getCheckin().equals(new Date(0)))
                .collect(Collectors.toList());
    }

    private void setupCreateTestSCCWebClient() throws URISyntaxException {
        String url = "https://localhost";
        SCCConfig sccConfig = new SCCConfigBuilder()
                .setUrl(new URI(url))
                .setUsername("username")
                .setPassword("password")
                .setUuid("uuid")
                .createSCCConfig();
        mockSccWebClient = new MockSCCWebClient(sccConfig) {
            @Override
            public SCCOrganizationSystemsUpdateResponse createUpdateSystems(
                    List<SCCRegisterSystemJson> systems, String username, String password
            ) {
                callCnt += 1;
                if (allowFirstCallToFail) {
                    // allow first call to fail
                    boolean setBadRequest = (callCnt == 1);
                    if (setBadRequest) {
                        throw new SCCClientException(400, "Bad Request");
                    }
                }
                if (allowAllCallsToFail) {
                    throw new SCCClientException(400, "Bad Request");
                }
                return new SCCOrganizationSystemsUpdateResponse(
                        systems.stream()
                                .map(system ->
                                        new SCCSystemCredentialsJson(system.getLogin(),
                                                system.getPassword(), 12345L))
                                .collect(Collectors.toList())
                );
            }
        };
    }

    private void setupAsPeripheral() {
        IssHub hub = new IssHub(HUB_FQDN, "");
        HubFactory hubFactory = new HubFactory();
        hubFactory.save(hub);

        SCCCredentials sccCredentialsTowardsHub =
                CredentialsFactory.createSCCCredentials(PERIPHERAL_USERNAME, PERIPHERAL_PASSWD);
        sccCredentialsTowardsHub.setUrl(HUB_FQDN);

        CredentialsFactory.storeCredentials(sccCredentialsTowardsHub);
    }

    private void setupAsHub() {
        HubSCCCredentials sccCredentialsTowardsPeripheral =
                CredentialsFactory.createHubSCCCredentials(PERIPHERAL_USERNAME, PERIPHERAL_PASSWD, PERIPHERAL_FQDN);
        CredentialsFactory.storeCredentials(sccCredentialsTowardsPeripheral);

        IssPeripheral peripheral = new IssPeripheral(PERIPHERAL_FQDN, "");
        HubFactory hubFactory = new HubFactory();
        hubFactory.save(peripheral);
    }

    private void setupEndpoint() throws URISyntaxException {
        URI url = new URI(HUB_FQDN);
        String uuid = ContentSyncManager.getUUID();
        SCCProxyManager sccProxyManager = new SCCProxyManager();
        SCCEndpoints endpoints = new SCCEndpoints(uuid, url, sccProxyManager);
        endpoints.initRoutes(null);
    }


    private void assertPreConditions() {
        assertEquals(
                systemSize.intValue(),
                testSystems.stream().filter(i -> i.isSccRegistrationRequired()).count(),
                "initially all systems should require registration"
        );
        assertEquals(
                0,
                testSystems.stream().filter(i -> i.getOptRegistrationErrorTime().isPresent()).count(),
                "initially no system should have a registration error time"
        );
        assertEquals(
                0,
                testSystems.stream().filter(i -> i.getOptCredentials().isPresent()).count(),
                "initially no system should have credentials"
        );
        assertEquals(
                0,
                testSystems.stream().filter(i -> i.getOptSccId().isPresent()).count(),
                "initially no system should have a scc id"
        );
    }

    private void assertPostConditionsCount(
            int expectedRegistered,
            int expectedFailed,
            int expectedProcessed
    ) {
        assertEquals(
                expectedRegistered,
                testSystems.stream().filter(i -> i.getOptSccId().isPresent()).count(),
                "Sucessful registered systems mismatch"
        );
        assertEquals(
                expectedFailed,
                testSystems.stream().filter(i -> i.isSccRegistrationRequired()).count(),
                "no system should still be requiring registration"
        );
        assertEquals(
                expectedFailed,
                testSystems.stream().filter(i -> i.getOptRegistrationErrorTime().isPresent()).count(),
                "no system should have a registration error time set"
        );
        assertEquals(
                expectedProcessed,
                testSystems.stream().filter(i -> i.getOptCredentials().isPresent()).count(),
                "no new systems should have credentials"
        );
    }

    @Test
    public void testSuccessSystemRegistrationWhenNoSystemsProvided() throws Exception {
        systemSize = 0;
        setupTest();

        assertPreConditions();
        mockForwardRegistrationTask.executeSCCTasksCore(testSccSystemRegMan, testSccProxyFactory, primaryCredentials);
        assertPostConditionsCount(0, 0, 0);
    }

    /**
     * Tests when all systems are payg.
     */
    @Test
    public void testSuccessSystemRegistrationWhenAllSystemsArePayG() throws Exception {
        setupTest();
        servers.stream().forEach(i -> i.setPayg(true));

        assertPreConditions();
        mockForwardRegistrationTask.executeSCCTasksCore(testSccSystemRegMan, testSccProxyFactory, primaryCredentials);
        assertPostConditionsCount(0, 0, 15);
    }

    @Test
    public void testSuccessSystemRegistrationWhenAllSystemsAreCreated() throws Exception {
        setupTest();

        assertPreConditions();
        mockForwardRegistrationTask.executeSCCTasksCore(testSccSystemRegMan, testSccProxyFactory, primaryCredentials);
        assertPostConditionsCount(15, 0, 15);
    }


    @Test
    public void testSuccessSystemRegistrationWhenAllSccRequestsFail() throws Exception {
        allowAllCallsToFail = true;
        setupTest();

        assertPreConditions();
        mockForwardRegistrationTask.executeSCCTasksCore(testSccSystemRegMan, testSccProxyFactory, primaryCredentials);
        assertPostConditionsCount(0, 15, 0);
    }

    // Test if every scenario works as expected when occur together.
    // In this case, the logic is as it follows:
    // - 30 systems are pending to be registered
    // - 5 are payg
    // - 9 will fail registering as the first SCC api call will fail.
    // This means:
    // - the remaining 16 systems will be successfully registered and will have a scc id
    // - the 9 systems that fail and will still require registration and also have a registration error time set
    // - 21 systems (the successfully registered systems + payg ones) will have credentials.
    @Test
    public void testSuccessSystemRegistration() throws Exception {
        systemSize = 30;
        batchSize = 9;
        allowFirstCallToFail = true;
        final int skipRegister = 5;
        setupTest();

        for (int i = 0; i < skipRegister; i++) {
            this.servers.get(i).setPayg(true);
        }

        assertPreConditions();
        mockForwardRegistrationTask.executeSCCTasksCore(testSccSystemRegMan, testSccProxyFactory, primaryCredentials);
        assertPostConditionsCount(16, 9, 21);
    }


    @Test
    public void testWithHub() throws Exception {
        systemSize = 5;
        setupTest();
        setupAsPeripheral();
        setupAsHub();
        setupEndpoint();

        assertPreConditions();

        URI url = new URI(Config.get().getString(ConfigDefaults.SCC_URL));
        String uuid = ContentSyncManager.getUUID();
        SCCConfig sccConfig = new SCCConfigBuilder()
                .setUrl(url)
                .setUsername("")
                .setPassword("")
                .setUuid(uuid)
                .createSCCConfig();
        MockHttpClientAdapter newAdapter = new MockHttpClientAdapter(sccConfig);
        SCCWebClient sccClient = new SCCWebClient(sccConfig, newAdapter);
        SCCProxyFactory sccProxyFactory = new SCCProxyFactory();

        SCCSystemRegistrationManager sccRegManager = new SCCSystemRegistrationManager(sccClient, sccProxyFactory);

        Optional<SCCCredentials> optCred = mockForwardRegistrationTask.findSccCredentials();
        if (optCred.isEmpty()) {
            throw new IllegalStateException("optCred should have a value");
        }
        mockForwardRegistrationTask.executeSCCTasksCore(sccRegManager, sccProxyFactory, optCred.get());
        HibernateFactory.getSession().flush();

        assertPostConditionsCount(systemSize, 0, systemSize);

        List<SCCProxyRecord> proxyRecords =
                sccProxyFactory.lookupByStatusAndRetry(SCCProxyRecord.Status.SCC_CREATION_PENDING);
        assertEquals(this.systemSize, proxyRecords.size());
        if (this.systemSize == proxyRecords.size()) {
            SCCProxyRecord proxyRecord = proxyRecords.get(0);
            assertTrue(proxyRecord.getOptSccId().isEmpty());
            assertEquals(PERIPHERAL_FQDN, proxyRecord.getPeripheralFqdn());
            assertTrue(proxyRecord.getProxyId() > 0);
        }

        assertEquals(0, sccProxyFactory.lookupByStatusAndRetry(SCCProxyRecord.Status.SCC_CREATED).size());
        assertEquals(0, sccProxyFactory.lookupByStatusAndRetry(SCCProxyRecord.Status.SCC_REMOVAL_PENDING).size());

        List<SCCRegCacheItem> sccRegCacheItems = SCCCachingFactory.testListAllItems();
        assertEquals(this.systemSize, sccRegCacheItems.size());
        if (this.systemSize == sccRegCacheItems.size()) {
            SCCRegCacheItem sccRegCacheItem = sccRegCacheItems.get(0);
            assertFalse(sccRegCacheItem.isSccRegistrationRequired());
            assertTrue(sccRegCacheItem.getOptSccId().isPresent());
            assertTrue(sccRegCacheItem.getOptServer().isPresent());
        }


        //delete
        ServerFactory.delete(servers.get(0));
        ServerFactory.delete(servers.get(1));

        mockForwardRegistrationTask.executeSCCTasksCore(sccRegManager, sccProxyFactory, optCred.get());
        assertEquals(this.systemSize - 2,
                sccProxyFactory.lookupByStatusAndRetry(SCCProxyRecord.Status.SCC_CREATION_PENDING).size());
        assertEquals(0, sccProxyFactory.lookupByStatusAndRetry(SCCProxyRecord.Status.SCC_CREATED).size());
        assertEquals(0, sccProxyFactory.lookupByStatusAndRetry(SCCProxyRecord.Status.SCC_REMOVAL_PENDING).size());
    }

}
