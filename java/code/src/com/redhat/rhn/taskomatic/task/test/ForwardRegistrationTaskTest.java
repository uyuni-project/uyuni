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

import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


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
import com.redhat.rhn.domain.server.VirtualInstance;
import com.redhat.rhn.manager.content.ContentSyncManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.taskomatic.task.ForwardRegistrationTask;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ServerTestUtils;

import com.suse.manager.hub.test.ControllerTestUtils;
import com.suse.manager.model.hub.HubFactory;
import com.suse.manager.model.hub.IssHub;
import com.suse.manager.model.hub.IssPeripheral;
import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;
import com.suse.manager.webui.services.SaltStateGeneratorService;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.test.TestSaltApi;
import com.suse.scc.SCCEndpoints;
import com.suse.scc.SCCSystemRegistrationManager;
import com.suse.scc.client.SCCClient;
import com.suse.scc.client.SCCClientException;
import com.suse.scc.client.SCCConfig;
import com.suse.scc.client.SCCConfigBuilder;
import com.suse.scc.client.SCCWebClient;
import com.suse.scc.model.SCCOrganizationSystemsUpdateResponse;
import com.suse.scc.model.SCCRegisterSystemJson;
import com.suse.scc.model.SCCSystemCredentialsJson;
import com.suse.scc.model.SCCUpdateSystemJson;
import com.suse.scc.model.SCCVirtualizationHostJson;
import com.suse.scc.proxy.SCCProxyFactory;
import com.suse.scc.proxy.SCCProxyManager;
import com.suse.scc.proxy.SCCProxyRecord;
import com.suse.scc.proxy.SccProxyStatus;
import com.suse.utils.Json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import spark.route.HttpMethod;

public class ForwardRegistrationTaskTest extends BaseTestCaseWithUser {

    private SCCCredentials primaryCredentials;
    private List<Server> servers;
    private static List<SCCRegCacheItem> testSystems;

    private SystemEntitlementManager systemEntitlementManager;

    private MockHttpClientAdapter testHttpClientAdapter;
    private MockSCCWebClient testSCCWebClient;
    private MockForwardRegistrationTask testForwardRegistrationTask;
    private static SCCProxyFactory testSccProxyFactory;
    private MockSCCSystemRegistrationManager testSCCSystemRegistrationManager;

    private MockSCCWebClientWithCount mockWebClientWithCount;
    private MockSCCSystemRegistrationManager mockSystemRegistrationManagerWithCount;

    private static Verifier testVerifier = new Verifier();

    private static Integer systemSize;
    private Integer batchSize;
    private static int virtualHostsSize = 0;
    private static boolean allowFirstCallToFail;
    private static boolean allowAllCallsToFail;

    private static final String HUB_FQDN = "hub.local";
    private static final String PERIPHERAL_FQDN = "peripheral.local";
    private static final String PERIPHERAL_USERNAME = "peripheral-000002";
    private static final String PERIPHERAL_PASSWD = "not so secret";

    static class MockSCCWebClient extends SCCWebClient {
        MockSCCWebClient(SCCConfig configIn, HttpClientAdapter httpClientIn) {
            super(configIn, httpClientIn);
        }

        @Override
        public void updateBulkLastSeen(List<SCCUpdateSystemJson> systems, String username, String password)
                throws SCCClientException {
            //do nothing
        }
    }

    static class MockSCCWebClientWithCount extends SCCWebClient {
        protected int callCnt;
        protected int callVirtHostCnt;

        MockSCCWebClientWithCount() {
            super(new SCCConfigBuilder().createSCCConfig());
            resetCallCnt();
        }

        public int getCallCnt() {
            return callCnt;
        }

        public int getCallVirtHostCnt() {
            return callVirtHostCnt;
        }

        public void resetCallCnt() {
            callCnt = 0;
            callVirtHostCnt = 0;
        }

        @Override
        public void updateBulkLastSeen(List<SCCUpdateSystemJson> systems, String username, String password)
                throws SCCClientException {
            //do nothing
        }

        @Override
        public void setVirtualizationHost(List<SCCVirtualizationHostJson> virtHostInfo,
                                          String username, String password) throws SCCClientException {
            callVirtHostCnt += 1;
            assertEquals(virtualHostsSize, virtHostInfo.size());
        }

        @Override
        public SCCOrganizationSystemsUpdateResponse createUpdateSystems(List<SCCRegisterSystemJson> systems,
                                                                        String username, String password) {
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
    }

    static class MockSCCSystemRegistrationManager extends SCCSystemRegistrationManager {
        MockSCCSystemRegistrationManager(SCCClient sccClientIn, SCCProxyFactory sccProxyFactoryIn) {
            super(sccClientIn, sccProxyFactoryIn);
        }

        @Override
        public void updateLastSeen(SCCCredentials primaryCredential) {
            //do nothing
        }
    }

    static class MockForwardRegistrationTask extends ForwardRegistrationTask {
        @Override
        public void executeSCCTasksAsServer(SCCSystemRegistrationManager sccRegManager,
                                            SCCCredentials sccPrimaryOrProxyCredentials) {
            super.executeSCCTasksAsServer(sccRegManager, sccPrimaryOrProxyCredentials);
        }

        @Override
        public void executeSCCTasksAsProxy(SCCSystemRegistrationManager sccRegManager,
                                           SCCCredentials sccPrimaryOrProxyCredentials) {
            super.executeSCCTasksAsProxy(sccRegManager, sccPrimaryOrProxyCredentials);
        }

        public Optional<SCCCredentials> setupTaskGetSccCredentials() {
            setupTaskConfiguration();
            return taskConfigSccCredentials;
        }
    }

    static class MockHttpClientAdapter extends HttpClientAdapter {
        private boolean simulateProxy = true;
        private String username;
        private String password;
        private long sccid;
        private int virtualhostcnt;
        private int deletedcnt;

        private final Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX")
                .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
                .create();

        MockHttpClientAdapter() {
            super(null, false);
            setSimulateProxy();
            username = "";
            password = "";
            sccid = 0;
            virtualhostcnt = 0;
            deletedcnt = 0;
        }

        public void setSimulateProxy() {
            simulateProxy = true;
        }

        public void setSimulateScc() {
            simulateProxy = false;
        }

        public long getRegisteredSystemsCnt() {
            return sccid;
        }

        public int getVirtualHostsForwaredCnt() {
            return virtualhostcnt;
        }

        public int getDeletedCnt() {
            return deletedcnt;
        }

        @Override
        public HttpResponse executeRequest(HttpRequestBase request, String usernameIn, String passwordIn) {
            try {
                username = usernameIn;
                password = passwordIn;

                HttpResponseFactory factory = new DefaultHttpResponseFactory();
                HttpResponse response = factory.newHttpResponse(
                        new BasicStatusLine(new ProtocolVersion("http", 1, 1),
                                HttpStatus.SC_BAD_REQUEST, null), null);

                boolean isPutRequest = (request.getMethod().compareToIgnoreCase("PUT") == 0);
                boolean isDeleteRequest = (request.getMethod().compareToIgnoreCase("DELETE") == 0);
                if (isPutRequest &&
                        request.getURI().getPath().contains("/connect/organizations/virtualization_hosts")) {
                    if (simulateProxy) {
                        simulateProxyRequestSetVirtualizationHosts(request, response);
                    }
                    else {
                        simulateSccRequestSetVirtualizationHosts(request, response);
                    }
                }
                else if (isPutRequest) {
                    if (simulateProxy) {
                        simulateProxyRequestCreateSystems(request, response);
                    }
                    else {
                        simulateSccRequestCreateSystems(request, response);
                    }
                }
                else if (isDeleteRequest) {
                    if (simulateProxy) {
                        simulateProxyRequestDeleteSystems(request, response);
                    }
                    else {
                        simulateSccRequestDeleteSystems(request, response);
                    }
                }

                return response;
            }
            catch (Exception eIn) {
                throw new IllegalStateException(eIn);
            }
        }

        public void simulateProxyRequestSetVirtualizationHosts(HttpRequestBase request, HttpResponse response)
                throws Exception {
            HttpEntity entity = ((HttpPut) request).getEntity(); // example
            String requestBody = EntityUtils.toString(entity);

            ControllerTestUtils.simulateApiEndpointCallBasicAuth(
                    "/hub/scc/connect/organizations/virtualization_hosts", HttpMethod.put,
                    username, password, requestBody);

            response.setStatusCode(HttpServletResponse.SC_OK);
        }

        public void simulateProxyRequestCreateSystems(HttpRequestBase request, HttpResponse response)
                throws Exception {
            HttpEntity entity = ((HttpPut) request).getEntity(); // example
            String requestBody = EntityUtils.toString(entity);

            String result = (String) ControllerTestUtils.simulateApiEndpointCallBasicAuth(
                    "/hub/scc/connect/organizations/systems", HttpMethod.put,
                    username, password, requestBody);

            response.setEntity(new StringEntity(result, APPLICATION_JSON));
            response.setStatusCode(HttpServletResponse.SC_CREATED);
        }

        public void simulateProxyRequestDeleteSystems(HttpRequestBase request, HttpResponse response)
                throws Exception {
            String url = request.getURI().getPath();
            int index = url.lastIndexOf("/");
            String id = url.substring(index + 1);

            Object res = ControllerTestUtils.simulateApiEndpointCallBasicAuth(
                    "/hub/scc/connect/organizations/systems/:id",
                    HttpMethod.delete, username, password, id);

            response.setEntity(new StringEntity(res.toString(), APPLICATION_JSON));
            response.setStatusCode(HttpServletResponse.SC_NO_CONTENT);
        }

        public void simulateSccRequestSetVirtualizationHosts(HttpRequestBase request, HttpResponse response)
                throws IOException {
            HttpEntity entity = ((HttpPut) request).getEntity(); // example
            String requestBody = EntityUtils.toString(entity);

            TypeToken<Map<String, List<SCCVirtualizationHostJson>>> typeToken = new TypeToken<>() {
            };
            Map<String, List<SCCVirtualizationHostJson>> payload = gson.fromJson(requestBody, typeToken.getType());
            if (!payload.containsKey("virtualization_hosts")) {
                fail("wrong json input: missing virtualization_hosts key");
            }
            List<SCCVirtualizationHostJson> virtHostsList = payload.get("virtualization_hosts");
            virtualhostcnt += virtHostsList.size();

            response.setStatusCode(HttpServletResponse.SC_OK);
        }

        public void simulateSccRequestCreateSystems(HttpRequestBase request, HttpResponse response)
                throws IOException {
            HttpEntity entity = ((HttpPut) request).getEntity(); // example
            String requestBody = EntityUtils.toString(entity);

            assertContains(request.getURI().getPath(), "connect/organizations/systems");

            TypeToken<Map<String, List<SCCRegisterSystemJson>>> typeToken = new TypeToken<>() {
            };
            Map<String, List<SCCRegisterSystemJson>> payload = gson.fromJson(requestBody, typeToken.getType());

            if (!payload.containsKey("systems")) {
                fail("wrong json input: missing systems key");
            }
            List<SCCRegisterSystemJson> systemsList = payload.get("systems");

            List<SCCSystemCredentialsJson> systemsResponse = new ArrayList<>();
            for (SCCRegisterSystemJson sj : systemsList) {
                sccid++;
                systemsResponse.add(new SCCSystemCredentialsJson(
                        sj.getLogin(),
                        sj.getPassword(),
                        sccid,
                        new Date(0)));
            }

            response.setStatusCode(HttpStatus.SC_CREATED);
            response.setEntity(new StringEntity(
                    gson.toJson(new SCCOrganizationSystemsUpdateResponse(systemsResponse)), APPLICATION_JSON));
        }

        public void simulateSccRequestDeleteSystems(HttpRequestBase request, HttpResponse response) {
            deletedcnt++;
            response.setEntity(new StringEntity("", APPLICATION_JSON));
            response.setStatusCode(HttpServletResponse.SC_NO_CONTENT);
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
        virtualHostsSize = 0;

        SaltApi saltApi = new TestSaltApi();
        systemEntitlementManager = new SystemEntitlementManager(
                new SystemUnentitler(saltApi), new SystemEntitler(saltApi)
        );
    }

    private void setupTest() throws Exception {
        setupCreatePrimaryCredentials();
        setupCreateTestObjects();
        setupCreateSystems();
    }

    private void setupCreatePrimaryCredentials() {
        primaryCredentials = CredentialsFactory.createSCCCredentials("username", "password");
        primaryCredentials.setUrl("https://scc.suse.com");
        CredentialsFactory.storeCredentials(primaryCredentials);
    }

    private void setupCreateTestObjects() throws URISyntaxException {
        testHttpClientAdapter = new MockHttpClientAdapter();
        testHttpClientAdapter.setSimulateProxy();
        testSCCWebClient = new MockSCCWebClient(new SCCConfigBuilder().createSCCConfig(), testHttpClientAdapter);
        testForwardRegistrationTask = new MockForwardRegistrationTask();
        testSccProxyFactory = new SCCProxyFactory();
        testSCCSystemRegistrationManager = new MockSCCSystemRegistrationManager(testSCCWebClient, testSccProxyFactory);

        mockWebClientWithCount = new MockSCCWebClientWithCount();
        mockSystemRegistrationManagerWithCount =
                new MockSCCSystemRegistrationManager(mockWebClientWithCount, testSccProxyFactory);
    }

    private void setupCreateSystems() throws Exception {
        Path tmpSaltRoot = Files.createTempDirectory("salt");
        SaltStateGeneratorService.INSTANCE.setSuseManagerStatesFilesRoot(tmpSaltRoot.toAbsolutePath());
        SaltStateGeneratorService.INSTANCE.setSkipSetOwner(true);
        Config.get().setString(ConfigDefaults.REG_BATCH_SIZE, String.valueOf(batchSize));

        for (SCCRegCacheItem item : getAllSCCRegCacheItems()) {
            SCCCachingFactory.deleteRegCacheItem(item);
        }

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

    private void setupVirtualHostWithGuest() throws Exception {
        Server host = ServerTestUtils.createVirtHostWithGuests(user, 1, true, systemEntitlementManager);
        host.getServerInfo().setCheckin(new Date(0));
        servers.add(host);
        systemSize += 1;
        virtualHostsSize += 1;
        host.getVirtualGuests().stream().map(VirtualInstance::getGuestSystem).forEach(guest -> {
            guest.getServerInfo().setCheckin(new Date(0));
            servers.add(guest);
            systemSize += 1;
        });
        HibernateFactory.getSession().flush();

        SCCCachingFactory.initNewSystemsToForward();
        List<SCCRegCacheItem> allUnregistered = SCCCachingFactory.findSystemsToForwardRegistration();
        testSystems = allUnregistered.stream()
                .filter(i -> i.getOptServer().get().getServerInfo().getCheckin().equals(new Date(0)))
                .collect(Collectors.toList());
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

    private List<SCCRegCacheItem> getAllSCCRegCacheItems() {
        return HibernateFactory.getSession()
                .createNativeQuery("SELECT * FROM suseSCCRegCache", SCCRegCacheItem.class)
                .getResultList();
    }

    static class Verifier {
        public Verifier verifyProxyCreationPending(int expected) {
            assertEquals(expected,
                    testSccProxyFactory.lookupByStatusAndRetry(SccProxyStatus.SCC_CREATION_PENDING).size());
            return this;
        }
        public Verifier verifyProxyCreated(int expected) {
            assertEquals(expected,
                    testSccProxyFactory.lookupByStatusAndRetry(SccProxyStatus.SCC_CREATED).size());
            return this;
        }
        public Verifier verifyProxyRemovalPending(int expected) {
            assertEquals(expected,
                    testSccProxyFactory.lookupByStatusAndRetry(SccProxyStatus.SCC_REMOVAL_PENDING).size());
            return this;
        }
        public Verifier verifyProxyVirtHostPending(int expected) {
            assertEquals(expected,
                    testSccProxyFactory.lookupByStatusAndRetry(SccProxyStatus.SCC_VIRTHOST_PENDING).size());
            return this;
        }

        public Verifier verifySystemsToRegister(int expected) {
            assertEquals(expected, SCCCachingFactory.findSystemsToForwardRegistration().size());
            return this;
        }
        public Verifier verifySystemsByCredentials(int expected, Optional<SCCCredentials> optCred) {
            assertEquals(expected, SCCCachingFactory.listRegItemsByCredentials(optCred.get()).size());
            return this;
        }
        public Verifier verifySystemsToDeregister(int expected) {
            assertEquals(expected, SCCCachingFactory.listDeregisterItems().size());
            return this;
        }
        public Verifier verifySystemsVirtHost(int expected) {
            assertEquals(expected, SCCCachingFactory.listVirtualizationHosts().size());
            return this;
        }

        public Verifier assertPreConditions() {
            assertEquals(systemSize.intValue(),
                    testSystems.stream().filter(i -> i.isSccRegistrationRequired()).count(),
                    "initially all systems should require registration");
            assertEquals(0,
                    testSystems.stream().filter(i -> i.getOptRegistrationErrorTime().isPresent()).count(),
                    "initially no system should have a registration error time");
            assertEquals(0,
                    testSystems.stream().filter(i -> i.getOptCredentials().isPresent()).count(),
                    "initially no system should have credentials");
            assertEquals(0,
                    testSystems.stream().filter(i -> i.getOptSccId().isPresent()).count(),
                    "initially no system should have a scc id");
            return this;
        }

        public Verifier assertPostConditionsRegisteredCount(int expected) {
            assertEquals(expected,
                    testSystems.stream().filter(i -> i.getOptSccId().isPresent()).count(),
                    "Sucessful registered systems mismatch");
            return this;
        }
        public Verifier assertPostConditionsFailedCount(int expected) {
            assertEquals(expected,
                    testSystems.stream().filter(i -> i.isSccRegistrationRequired()).count(),
                    "no system should still be requiring registration");
            assertEquals(expected,
                    testSystems.stream().filter(i -> i.getOptRegistrationErrorTime().isPresent()).count(),
                    "no system should have a registration error time set");
            return this;
        }
        public Verifier assertPostConditionsProcessedCount(int expected) {
            assertEquals(expected,
                    testSystems.stream().filter(i -> i.getOptCredentials().isPresent()).count(),
                    "no new systems should have credentials");
            return this;
        }
    }

    @Test
    public void testSuccessSystemRegistrationWhenNoSystemsProvided() throws Exception {
        systemSize = 0;
        setupTest();

        testVerifier.assertPreConditions();
        testForwardRegistrationTask.executeSCCTasksAsServer(mockSystemRegistrationManagerWithCount, primaryCredentials);
        testForwardRegistrationTask.executeSCCTasksAsProxy(mockSystemRegistrationManagerWithCount, primaryCredentials);
        testVerifier
                .assertPostConditionsRegisteredCount(0)
                .assertPostConditionsFailedCount(0)
                .assertPostConditionsProcessedCount(0);
    }

    @Test
    public void testSuccessSystemRegistrationWhenAllSystemsArePayG() throws Exception {
        setupTest();
        servers.stream().forEach(i -> i.setPayg(true));

        testVerifier.assertPreConditions();
        testForwardRegistrationTask.executeSCCTasksAsServer(mockSystemRegistrationManagerWithCount, primaryCredentials);
        testForwardRegistrationTask.executeSCCTasksAsProxy(mockSystemRegistrationManagerWithCount, primaryCredentials);
        testVerifier
                .assertPostConditionsRegisteredCount(0)
                .assertPostConditionsFailedCount(0)
                .assertPostConditionsProcessedCount(15);
    }

    @Test
    public void testSuccessSystemRegistrationWhenAllSystemsAreCreated() throws Exception {
        setupTest();

        testVerifier.assertPreConditions();
        testForwardRegistrationTask.executeSCCTasksAsServer(mockSystemRegistrationManagerWithCount, primaryCredentials);
        testForwardRegistrationTask.executeSCCTasksAsProxy(mockSystemRegistrationManagerWithCount, primaryCredentials);
        testVerifier
                .assertPostConditionsRegisteredCount(15)
                .assertPostConditionsFailedCount(0)
                .assertPostConditionsProcessedCount(15);
    }

    @Test
    public void testSuccessSystemRegistrationWhenAllSccRequestsFail() throws Exception {
        allowAllCallsToFail = true;
        setupTest();

        testVerifier.assertPreConditions();
        testForwardRegistrationTask.executeSCCTasksAsServer(mockSystemRegistrationManagerWithCount, primaryCredentials);
        testForwardRegistrationTask.executeSCCTasksAsProxy(mockSystemRegistrationManagerWithCount, primaryCredentials);
        testVerifier
                .assertPostConditionsRegisteredCount(0)
                .assertPostConditionsFailedCount(15)
                .assertPostConditionsProcessedCount(0);
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

        testVerifier.assertPreConditions();
        testForwardRegistrationTask.executeSCCTasksAsServer(mockSystemRegistrationManagerWithCount, primaryCredentials);
        testForwardRegistrationTask.executeSCCTasksAsProxy(mockSystemRegistrationManagerWithCount, primaryCredentials);
        testVerifier
                .assertPostConditionsRegisteredCount(16)
                .assertPostConditionsFailedCount(9)
                .assertPostConditionsProcessedCount(21);
    }

    @Test
    public void testSuccessVirtHost() throws Exception {
        systemSize = 0;
        setupTest();
        setupVirtualHostWithGuest();

        testVerifier.assertPreConditions();
        testForwardRegistrationTask.executeSCCTasksAsServer(mockSystemRegistrationManagerWithCount, primaryCredentials);
        testForwardRegistrationTask.executeSCCTasksAsProxy(mockSystemRegistrationManagerWithCount, primaryCredentials);
        testVerifier
                .assertPostConditionsRegisteredCount(2)
                .assertPostConditionsFailedCount(0)
                .assertPostConditionsProcessedCount(2);
        assertEquals(virtualHostsSize, mockWebClientWithCount.getCallVirtHostCnt());
    }

    @Test
    public void testForwardVirtualHostsFromPeripheralToHub() throws Exception {
        // Cleanup all systems which might exist from previous tests
        ServerFactory.list().forEach(ServerFactory::delete);
        systemSize = 0;
        setupTest();
        setupAsPeripheral();
        setupAsHub();
        setupEndpoint();
        setupVirtualHostWithGuest();

        testVerifier.assertPreConditions();

        Optional<SCCCredentials> optCred = testForwardRegistrationTask.setupTaskGetSccCredentials();
        if (optCred.isEmpty()) {
            throw new IllegalStateException("optCred should have a value");
        }

        assertEquals(systemSize, testSystems.size());
        assertEquals(systemSize, servers.size());

        // verify on the Peripheral:
        // - 2 systems pending registration against the hub
        // - 1 virtual hosts pending forward
        testVerifier
                .verifySystemsToRegister(2)
                .verifySystemsByCredentials(0, optCred)
                .verifySystemsToDeregister(0)
                .verifySystemsVirtHost(1);

        // verify on the Hub:
        // - 0 systems pending to be registered against SCC
        // - 0 virtual host pending to be set in SCC
        testVerifier
                .verifyProxyCreationPending(0)
                .verifyProxyCreated(0)
                .verifyProxyRemovalPending(0)
                .verifyProxyVirtHostPending(0);

        testForwardRegistrationTask.executeSCCTasksAsServer(testSCCSystemRegistrationManager, optCred.get());

        testVerifier
                .assertPostConditionsRegisteredCount(systemSize)
                .assertPostConditionsFailedCount(0)
                .assertPostConditionsProcessedCount(systemSize);

        // verify on the Peripheral:
        // - 2 systems registered against the hub
        // - no virtual host needs to be forwarded
        testVerifier
                .verifySystemsToRegister(0)
                .verifySystemsByCredentials(2, optCred)
                .verifySystemsToDeregister(0)
                .verifySystemsVirtHost(0);

        // verify on the Hub:
        // - 2 systems pending to be registered against SCC
        // - 1 virtual host pending to be set in SCC
        testVerifier
                .verifyProxyCreationPending(2)
                .verifyProxyCreated(0)
                .verifyProxyRemovalPending(0)
                .verifyProxyVirtHostPending(1);

        // delete systems to perform later a clean data forwarding against SCC
        testSystems.stream()
                .flatMap(c -> c.getOptServer().stream())
                .forEach(ServerFactory::delete);

        // verify on the Peripheral:
        // - 2 systems are registered at the Hub
        // - 2 systems requires de-registration from the hub
        testVerifier
                .verifySystemsToRegister(0)
                .verifySystemsByCredentials(2, optCred)
                .verifySystemsToDeregister(2)
                .verifySystemsVirtHost(0);

        SCCCachingFactory.listDeregisterItems().forEach(SCCCachingFactory::deleteRegCacheItem);

        testHttpClientAdapter.setSimulateScc();
        testForwardRegistrationTask.executeSCCTasksAsProxy(testSCCSystemRegistrationManager, optCred.get());

        // verify on the Hub:
        // - 2 proxy systems registered against SCC
        // - 0 virtual host pending to be set in SCC
        testVerifier
                .verifyProxyCreationPending(0)
                .verifyProxyCreated(2)
                .verifyProxyRemovalPending(0)
                .verifyProxyVirtHostPending(0);
        assertEquals(1, testHttpClientAdapter.getVirtualHostsForwaredCnt());
    }

    @Test
    public void testWithHub() throws Exception {
        // Cleanup all systems which might exist from previous tests
        ServerFactory.list().forEach(ServerFactory::delete);
        systemSize = 5;
        setupTest();
        setupAsPeripheral();
        setupAsHub();
        setupEndpoint();

        testVerifier.assertPreConditions();

        Optional<SCCCredentials> optCred = testForwardRegistrationTask.setupTaskGetSccCredentials();
        if (optCred.isEmpty()) {
            throw new IllegalStateException("optCred should have a value");
        }

        assertEquals(systemSize, testSystems.size());
        assertEquals(systemSize, servers.size());

        // verify on the Peripheral:
        // - (systemSize) systems pending registration against the hub
        testVerifier
                .verifySystemsToRegister(systemSize)
                .verifySystemsByCredentials(0, optCred)
                .verifySystemsToDeregister(0)
                .verifySystemsVirtHost(0);

        // verify on the Hub:
        // - NO systems pending to be registered against SCC, created, to be removed or virtual hosts
        testVerifier
                .verifyProxyCreationPending(0)
                .verifyProxyCreated(0)
                .verifyProxyRemovalPending(0)
                .verifyProxyVirtHostPending(0);

        testForwardRegistrationTask.executeSCCTasksAsServer(testSCCSystemRegistrationManager, optCred.get());

        testVerifier
                .assertPostConditionsRegisteredCount(systemSize)
                .assertPostConditionsFailedCount(0)
                .assertPostConditionsProcessedCount(systemSize);

        // verify on the Peripheral:
        // - (systemSize) systems registered against the hub
        testVerifier
                .verifySystemsToRegister(0)
                .verifySystemsByCredentials(systemSize, optCred)
                .verifySystemsToDeregister(0)
                .verifySystemsVirtHost(0);

        // verify on the Hub:
        // - (systemSize) systems pending to be registered against SCC
        testVerifier
                .verifyProxyCreationPending(systemSize)
                .verifyProxyCreated(0)
                .verifyProxyRemovalPending(0)
                .verifyProxyVirtHostPending(0);


        List<SCCProxyRecord> proxyRecords =
                testSccProxyFactory.lookupByStatusAndRetry(SccProxyStatus.SCC_CREATION_PENDING);
        for (SCCProxyRecord proxyRecord : proxyRecords) {
            assertTrue(proxyRecord.getProxyId() > 0);
            assertEquals(PERIPHERAL_FQDN, proxyRecord.getPeripheralFqdn());
            assertFalse(proxyRecord.getSccLogin().isEmpty());
            assertFalse(proxyRecord.getSccPasswd().isEmpty());

            assertFalse(proxyRecord.getSccCreationJson().isEmpty());
            SCCRegisterSystemJson creationJson =
                    Json.GSON.fromJson(proxyRecord.getSccCreationJson(), SCCRegisterSystemJson.class);
            assertEquals(creationJson.getLogin(), proxyRecord.getSccLogin());
            assertEquals(creationJson.getPassword(), proxyRecord.getSccPasswd());

            assertTrue(proxyRecord.getOptSccId().isEmpty());
            assertNull(proxyRecord.getSccRegistrationErrorTime());
            assertEquals(SccProxyStatus.SCC_CREATION_PENDING, proxyRecord.getStatus());
        }

        //delete first 2 servers, to verify server deletion behaviour before SCC is seen
        ServerFactory.delete(servers.get(0));
        ServerFactory.delete(servers.get(1));
        int newSystemSize = systemSize - 2;

        testForwardRegistrationTask.executeSCCTasksAsServer(testSCCSystemRegistrationManager, optCred.get());

        // verify on the Hub:
        // - two less systems pending to be registered against SCC
        testVerifier
                .verifyProxyCreationPending(newSystemSize)
                .verifyProxyCreated(0)
                .verifyProxyRemovalPending(2)
                .verifyProxyVirtHostPending(0);

        // delete systems to perform later a clean data forwarding against SCC
        testSystems.stream()
                .flatMap(c -> c.getOptServer().stream())
                .forEach(ServerFactory::delete);

        // verify on the Peripheral:
        // - systemSize systems are registered at the Hub
        // - systemSize systems requires de-registration from the hub
        testVerifier
                .verifySystemsToRegister(0)
                .verifySystemsByCredentials(newSystemSize, optCred)
                .verifySystemsToDeregister(newSystemSize)
                .verifySystemsVirtHost(0);

        SCCCachingFactory.listDeregisterItems().forEach(SCCCachingFactory::deleteRegCacheItem);

        testHttpClientAdapter.setSimulateScc();
        testForwardRegistrationTask.executeSCCTasksAsProxy(testSCCSystemRegistrationManager, optCred.get());

        // verify on the Hub:
        // - newSystemSize proxy systems registered against SCC
        // - 0 virtual host pending to be set in SCC
        testVerifier
                .verifyProxyCreationPending(0)
                .verifyProxyCreated(newSystemSize)
                .verifyProxyRemovalPending(0)
                .verifyProxyVirtHostPending(0);
    }
}
