/*
 * Copyright (c) 2026 SUSE LCC
 * Copyright (c) 2023--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.scc.registration.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;

import com.suse.scc.client.SCCClientException;
import com.suse.scc.client.SCCConfig;
import com.suse.scc.client.SCCConfigBuilder;
import com.suse.scc.client.SCCWebClient;
import com.suse.scc.model.SCCOrganizationSystemsUpdateResponse;
import com.suse.scc.model.SCCRegisterSystemItem;
import com.suse.scc.model.SCCSystemCredentialsJson;
import com.suse.scc.registration.SCCSystemRegistrationContext;
import com.suse.scc.registration.SCCSystemRegistrationCreateUpdateSystems;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tests {@link com.suse.scc.registration.SCCSystemRegistration}.
 */
public class SCCSystemRegistrationCreateUpdateSystemsTest extends AbstractSCCSystemRegistrationTest {

    /**
     * This tests covers the scenario when no systems are provided.
     * In this case no SCC Rest calls are executed.
     */
    @Test
    public void testSuccessSCCSystemRegistrationBatchSystemUpdateWhenNoSystemsProvided() throws URISyntaxException {
        // setup
        TestSCCWebClient sccWebClient = getDefaultTestSCCWebClient();
        final SCCSystemRegistrationContext context =
                new SCCSystemRegistrationContext(sccWebClient, null, getCredentials());

        // pre-conditions
        assertEquals(0, sccWebClient.getCallCnt());

        // execution
        new SCCSystemRegistrationCreateUpdateSystems().handle(context);

        // assertions
        assertEquals(0, sccWebClient.getCallCnt());
    }


    /**
     * In this scenario all SCC Rest calls are successful and all systems were updated.
     * In this case there are 3 calls resulting in all 101 systems being added to context.getRegisteredSystems().
     */
    @Test
    public void testSuccessSCCSystemRegistrationCreateUpdateSystemsWhenAllSystemRegistrationAreSuccessful()
            throws URISyntaxException {
        final int systemSize = 101;
        final int batchSize = 50;
        Config.get().setString(ConfigDefaults.REG_BATCH_SIZE, String.valueOf(batchSize));

        // setup
        final TestSCCWebClient sccWebClient = getDefaultTestSCCWebClient();
        final SCCSystemRegistrationContext context =
                setupSCCSystemRegistrationContextWithPendingRegistrationSystems(sccWebClient, systemSize);

        // pre-conditions
        assertEquals(0, sccWebClient.getCallCnt());
        assertEquals(0, context.getRegisteredSystems().size());

        // execution
        new SCCSystemRegistrationCreateUpdateSystems().handle(context);

        // assertions
        assertEquals(3, sccWebClient.getCallCnt());
        assertEquals(systemSize, context.getRegisteredSystems().size());
    }

    /**
     * In this scenario all SCC Rest calls fail/don't return a valid response.
     * In this case there are 3 calls but no systems are added to context.getRegisteredSystems().
     */
    @Test
    public void testSuccessSCCSystemRegistrationCreateUpdateSystemsWhenRestCallFail() {
        final int systemSize = 101;
        final int batchSize = 50;
        Config.get().setString(ConfigDefaults.REG_BATCH_SIZE, String.valueOf(batchSize));

        // setup
        TestSCCWebClient sccWebClient = new TestSCCWebClient(null) {
            @Override
            public SCCOrganizationSystemsUpdateResponse createUpdateSystems(
                    List<SCCRegisterSystemItem> systems,
                    String username,
                    String password
            ) throws SCCClientException {
                callCnt += 1;
                throw new SCCClientException(400, "Bad Request");
            }
        };

        final SCCSystemRegistrationContext context =
                setupSCCSystemRegistrationContextWithPendingRegistrationSystems(sccWebClient, systemSize);

        // pre-conditions
        assertEquals(0, sccWebClient.getCallCnt());
        assertEquals(0, context.getRegisteredSystems().size());

        // execution
        new SCCSystemRegistrationCreateUpdateSystems().handle(context);

        // assertions
        assertEquals(3, sccWebClient.getCallCnt());
        assertEquals(0, context.getRegisteredSystems().size());
    }

    /**
     * In this scenario all SCC Rest calls trigger a RuntimeException.
     * In this case there are 3 calls but no systems are added to context.getRegisteredSystems().
     */
    @Test
    public void testSuccessSCCSystemRegistrationCreateUpdateSystemsWhenRTE() {
        final int systemSize = 101;
        final int batchSize = 50;
        Config.get().setString(ConfigDefaults.REG_BATCH_SIZE, String.valueOf(batchSize));

        // setup
        TestSCCWebClient sccWebClient = new TestSCCWebClient(null) {
            @Override
            public SCCOrganizationSystemsUpdateResponse createUpdateSystems(
                    List<SCCRegisterSystemItem> systems,
                    String username,
                    String password
            ) {
                callCnt += 1;
                throw new RuntimeException("Something went wrong while executing REST call");
            }
        };

        final SCCSystemRegistrationContext context =
                setupSCCSystemRegistrationContextWithPendingRegistrationSystems(sccWebClient, systemSize);

        // pre-conditions
        assertEquals(0, sccWebClient.getCallCnt());
        assertEquals(0, context.getRegisteredSystems().size());

        // execution
        new SCCSystemRegistrationCreateUpdateSystems().handle(context);

        // assertions
        assertEquals(3, sccWebClient.getCallCnt());
        assertEquals(0, context.getRegisteredSystems().size());
    }


    /**
     * Creates a {@link SCCSystemRegistrationContext} with a custom {@link TestSCCWebClient} for a given number of
     * systems.
     *
     * @param sccWebClient the {@link TestSCCWebClient} to use
     * @param systemSize   the number os systems to add
     * @return the {@link SCCSystemRegistrationContext} instance
     */
    public SCCSystemRegistrationContext setupSCCSystemRegistrationContextWithPendingRegistrationSystems(
            TestSCCWebClient sccWebClient,
            int systemSize
    ) {
        final SCCSystemRegistrationContext context =
                new SCCSystemRegistrationContext(sccWebClient, null, getCredentials());
        for (int i = 0; i < systemSize; i++) {
            context.getPendingRegistrationSystemsByLogin().put(
                    "SCCSystemId.login" + i,
                    new SCCRegisterSystemItem(
                            "SCCRegisterSystemItem.login" + i, "SCCRegisterSystemItem.pwd" + i,
                            null, null, null, null)
            );
        }
        return context;
    }


    /**
     * Wrapper for a SCCWebClient that supports a custom counter
     */
    public class TestSCCWebClient extends SCCWebClient {
        protected int callCnt;

        public TestSCCWebClient(SCCConfig configIn) {
            super(configIn);
            callCnt = 0;
        }

        public int getCallCnt() {
            return callCnt;
        }
    }


    /**
     * Creates a Default {@link TestSCCWebClient} instance for testing purposes.
     * All systems are updated successfully.
     * @return a default SccWebClient instance for testing purposes
     * @throws URISyntaxException
     */
    public TestSCCWebClient getDefaultTestSCCWebClient() throws URISyntaxException {
        SCCConfig sccConfig = new SCCConfigBuilder()
                .setUrl(new URI("https://localhost"))
                .setUsername("username")
                .setPassword("password")
                .setUuid("uuid")
                .createSCCConfig();
        return new TestSCCWebClient(sccConfig) {
            @Override
            public SCCOrganizationSystemsUpdateResponse createUpdateSystems(
                    List<SCCRegisterSystemItem> systems, String username, String password
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
    }

}
