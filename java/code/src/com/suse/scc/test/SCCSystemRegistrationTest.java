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
package com.suse.scc.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.credentials.SCCCredentials;
import com.redhat.rhn.domain.scc.SCCCachingFactory;
import com.redhat.rhn.domain.scc.SCCRegCacheItem;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerInfo;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ServerTestUtils;

import com.suse.manager.webui.services.SaltStateGeneratorService;
import com.suse.scc.SCCSystemRegistrationManager;
import com.suse.scc.client.SCCClientException;
import com.suse.scc.client.SCCConfig;
import com.suse.scc.client.SCCWebClient;
import com.suse.scc.model.SCCOrganizationSystemsUpdateResponse;
import com.suse.scc.model.SCCRegisterSystemJson;
import com.suse.scc.model.SCCSystemCredentialsJson;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tests {@link com.suse.scc.registration.SCCSystemRegistration}.
 */
public class SCCSystemRegistrationTest extends BaseTestCaseWithUser {

    private static final int DEFAULT_SYSTEM_SIZE = 15;
    private static final int DEFAULT_BATCH_SIZE = 3;

    private SCCCredentials credentials;
    private List<Server> servers;
    private List<SCCRegCacheItem> testSystems;

    private Integer systemSize;
    private Integer batchSize;

    private static final String UPTIME_TEST = "[\"2024-06-26:000000000000000000001111\"," +
                                               "\"2024-06-27:111111111111110000000000\"]";

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        this.credentials = CredentialsFactory.createSCCCredentials("username", "password");
        this.credentials.setUrl("https://scc.suse.com");
        CredentialsFactory.storeCredentials(credentials);

        this.systemSize = DEFAULT_SYSTEM_SIZE;
        this.batchSize = DEFAULT_BATCH_SIZE;
    }

    private void setupSystems() throws Exception {
        Path tmpSaltRoot = Files.createTempDirectory("salt");
        SaltStateGeneratorService.INSTANCE.setSuseManagerStatesFilesRoot(tmpSaltRoot.toAbsolutePath());
        SaltStateGeneratorService.INSTANCE.setSkipSetOwner(true);
        Config.get().setString(ConfigDefaults.REG_BATCH_SIZE, String.valueOf(batchSize));

        this.servers = new ArrayList<>();
        for (int i = 0; i < systemSize; i++) {
            Server testSystem = ServerTestUtils.createTestSystem();
            ServerInfo serverInfo = testSystem.getServerInfo();
            serverInfo.setCheckin(new Date(0)); // 1970-01-01 00:00:00 UTC
            serverInfo.setUptimeData(UPTIME_TEST);
            testSystem.setServerInfo(serverInfo);
            this.servers.add(testSystem);
        }

        SCCCachingFactory.initNewSystemsToForward();
        List<SCCRegCacheItem> allUnregistered = SCCCachingFactory.findSystemsToForwardRegistration();
        this.testSystems = allUnregistered.stream()
                .filter(i -> i.getOptServer().get().getServerInfo().getCheckin().equals(new Date(0)))
                .collect(Collectors.toList());
    }


    /**
     * Tests when no systems are provided.
     */
    @Test
    public void testSuccessSystemRegistrationWhenNoSystemsProvided() throws Exception {
        // setup
        this.systemSize = 0;
        this.setupSystems();
        TestSCCWebClient sccWebClient = getDefaultTestSCCWebClient();

        // pre-conditions
        assertPreConditions();

        // execution
        SCCSystemRegistrationManager sccSystemRegistrationManager = new SCCSystemRegistrationManager(sccWebClient);
        sccSystemRegistrationManager.register(testSystems, getCredentials());

        // assertions
        assertPosConditions(sccWebClient, 0, 0, 0, 0);
    }

    /**
     * Tests when all systems are payg.
     */
    @Test
    public void testSuccessSystemRegistrationWhenAllSystemsArePayG() throws Exception {
        // setup
        this.setupSystems();
        this.servers.stream().forEach(i -> i.setPayg(true));
        TestSCCWebClient sccWebClient = getDefaultTestSCCWebClient();

        // pre-conditions
        assertPreConditions();

        // execution
        SCCSystemRegistrationManager sccSystemRegistrationManager = new SCCSystemRegistrationManager(sccWebClient);
        sccSystemRegistrationManager.register(testSystems, getCredentials());

        // assertions
        assertPosConditions(sccWebClient, 0, 0, 0, 15);
    }

    /**
     * In this scenario all systems updated successfully.
     */
    @Test
    public void testSuccessSystemRegistrationWhenAllSystemsAreCreated() throws Exception {
        // setup
        this.setupSystems();
        TestSCCWebClient sccWebClient = getDefaultTestSCCWebClient();

        // pre-conditions
        assertPreConditions();

        // execution
        SCCSystemRegistrationManager sccSystemRegistrationManager = new SCCSystemRegistrationManager(sccWebClient);
        sccSystemRegistrationManager.register(testSystems, getCredentials());

        // assertions
        assertPosConditions(sccWebClient, 5, 15, 0, 15);
    }


    /**
     * In this scenario all SCC requests fail.
     */
    @Test
    public void testSuccessSystemRegistrationWhenAllSccRequestsFail() throws Exception {
        // setup
        this.setupSystems();
        TestSCCWebClient sccWebClient = new TestSCCWebClient(new SCCConfig(
                new URI("https://localhost"), "username", "password", "uuid")) {
            @Override
            public SCCOrganizationSystemsUpdateResponse createUpdateSystems(
                    List<SCCRegisterSystemJson> systems, String username, String password
            ) throws SCCClientException {
                callCnt += 1;
                throw new SCCClientException(400, "Bad Request");
            }
        };

        // pre-conditions
        assertPreConditions();

        // execution
        SCCSystemRegistrationManager sccSystemRegistrationManager = new SCCSystemRegistrationManager(sccWebClient);
        sccSystemRegistrationManager.register(testSystems, getCredentials());

        // assertions
        assertPosConditions(sccWebClient, 5, 0, 15, 0);
    }


    /**
     * Test if every scenario works as expected when occur together.
     * In this case, the logic is as it follows:
     * - 30 systems are pending to be registered;
     * - 5 are payg;
     * - 9 will fail registering as the first SCC api call will fail.
     * This means:
     *  - the remaining 16 systems will be sucessfull registered and will have a scc id;
     *  - the 9 systems that fail and will still require registration and also have a registration error time set;
     *  - 21 systems (the successfull registered systems + payg ones) will have credentials.
     */
    @Test
    public void testSuccessSystemRegistration() throws Exception {
        // setup
        this.systemSize = 30;
        this.batchSize = 9;
        final int skipRegister = 5;

        this.setupSystems();

        for (int i = 0; i < skipRegister; i++) {
            this.servers.get(i).setPayg(true);
        }

        TestSCCWebClient sccWebClient = new TestSCCWebClient(new SCCConfig(
                new URI("https://localhost"), "username", "password", "uuid")) {
            @Override
            public SCCOrganizationSystemsUpdateResponse createUpdateSystems(
                    List<SCCRegisterSystemJson> systems, String username, String password
            ) throws SCCClientException {
                callCnt += 1;
                // allow first call to fail
                if (callCnt == 1) {
                    throw new SCCClientException(400, "Bad Request");
                }
                return new SCCOrganizationSystemsUpdateResponse(
                        systems.stream()
                                .map(system ->
                                        new SCCSystemCredentialsJson(system.getLogin(), system.getPassword(), 12345L)
                                )
                                .collect(Collectors.toList())
                );
            }

        };


        // pre-conditions
        assertPreConditions();

        // execution
        SCCSystemRegistrationManager sccSystemRegistrationManager = new SCCSystemRegistrationManager(sccWebClient);
        sccSystemRegistrationManager.register(testSystems, getCredentials());

        // assertions
        assertPosConditions(sccWebClient, 3, 16, 9, 21);
    }


    /**
     * Asserts the pre-conditions for the tests.
     */
    private void assertPreConditions() {
        assertEquals(
                this.systemSize.intValue(),
                this.testSystems.stream().filter(i -> i.isSccRegistrationRequired()).count(),
                "initially all systems should require registration"
        );
        assertEquals(
                0,
                this.testSystems.stream().filter(i -> i.getOptRegistrationErrorTime().isPresent()).count(),
                "initially no system should have a registration error time"
        );
        assertEquals(
                0,
                this.testSystems.stream().filter(i -> i.getOptCredentials().isPresent()).count(),
                "initially no system should have credentials"
        );
        assertEquals(
                0,
                this.testSystems.stream().filter(i -> i.getOptSccId().isPresent()).count(),
                "initially no system should have a scc id"
        );
    }

    /**
     * Asserts the post-conditions for the tests.
     * @param sccWebClient the SCCWebClient used for the test
     * @param expectedSccRequests expected number of SCC requests
     * @param expectedRegistered expected number of systems successfully registered
     * @param expectedFailed expected number of systems that failed to register
     * @param expectedProcessed expected number of systems that were processed (registered or PAYG)
     */
    private void assertPosConditions(
            TestSCCWebClient sccWebClient,
            int expectedSccRequests,
            int expectedRegistered,
            int expectedFailed,
            int expectedProcessed
    ) {
        assertEquals(expectedSccRequests, sccWebClient.getCallCnt(), "Wrong number of SCC requests");
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

    /**
     * Wrapper for a SCCWebClient that supports a custom counter
     */
    class TestSCCWebClient extends SCCWebClient {
        protected int callCnt;

        TestSCCWebClient(SCCConfig configIn) {
            super(configIn);
            callCnt = 0;
        }

        public int getCallCnt() {
            return callCnt;
        }
    }


    /**
     * Creates a Default {@link TestSCCWebClient} instance for testing purposes
     */
    private TestSCCWebClient getDefaultTestSCCWebClient() throws URISyntaxException {
        TestSCCWebClient sccWebClient = new TestSCCWebClient(new SCCConfig(
                new URI("https://localhost"), "username", "password", "uuid")) {
            @Override
            public SCCOrganizationSystemsUpdateResponse createUpdateSystems(
                    List<SCCRegisterSystemJson> systems, String username, String password
            ) {
                callCnt += 1;
                return new SCCOrganizationSystemsUpdateResponse(
                        systems.stream()
                                .map(system ->
                                        new SCCSystemCredentialsJson(system.getLogin(), system.getPassword(), 12345L)
                                )
                                .collect(Collectors.toList())
                );
            }

        };
        return sccWebClient;
    }

    public SCCCredentials getCredentials() {
        return credentials;
    }


}
