/*
 * Copyright (c) 2024--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.hub.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.credentials.HubSCCCredentials;
import com.redhat.rhn.domain.credentials.SCCCredentials;
import com.redhat.rhn.domain.iss.IssFactory;
import com.redhat.rhn.domain.iss.IssMaster;
import com.redhat.rhn.domain.iss.IssSlave;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.MgrServerInfo;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.setup.MirrorCredentialsManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.TestStatics;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.manager.hub.HubClientFactory;
import com.suse.manager.hub.HubExternalClient;
import com.suse.manager.hub.HubInternalClient;
import com.suse.manager.hub.HubManager;
import com.suse.manager.model.hub.HubFactory;
import com.suse.manager.model.hub.IssAccessToken;
import com.suse.manager.model.hub.IssHub;
import com.suse.manager.model.hub.IssPeripheral;
import com.suse.manager.model.hub.IssRole;
import com.suse.manager.model.hub.IssServer;
import com.suse.manager.model.hub.ManagerInfoJson;
import com.suse.manager.model.hub.ServerInfoJson;
import com.suse.manager.model.hub.TokenType;
import com.suse.manager.model.hub.UpdatableServerData;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.test.TestSaltApi;
import com.suse.manager.webui.utils.token.IssTokenBuilder;
import com.suse.manager.webui.utils.token.Token;
import com.suse.manager.webui.utils.token.TokenBuildingException;
import com.suse.manager.webui.utils.token.TokenException;
import com.suse.manager.webui.utils.token.TokenParser;
import com.suse.manager.webui.utils.token.TokenParsingException;

import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.jose4j.jwt.consumer.ErrorCodes;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.cert.CertificateException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class HubManagerTest extends JMockBaseTestCaseWithUser {

    private static final String LOCAL_SERVER_FQDN = "local-server.unit-test.local";

    private static final String REMOTE_SERVER_FQDN = "remote-server.unit-test.local";

    private User satAdmin;

    private HubFactory hubFactory;

    private HubManager hubManager;

    private HubClientFactory clientFactoryMock;

    private String originalServerSecret;

    private String originalFqdn;

    private String originalReportDb;

    private String originalProductVersion;

    static class MockTaskomaticApi extends TaskomaticApi {
        private int invoked;
        private List<String> invokeNames;
        private List<String> invokeBunches;
        private String invokeRootCaFilename;
        private String invokeRootCaContent;
        private String invokeGpgKeyContent;
        private int expectedInvocations;
        private List<String> expectedInvocationNames;
        private List<String> expectedInvocationBunches;
        private String expectedRootCaFilename;
        private String expectedRootCaContent;
        private String expectedGpgKeyContent;

        MockTaskomaticApi() {
            resetTaskomaticCall();
        }

        public void resetTaskomaticCall() {
            invoked = 0;
            invokeNames = new ArrayList<>();
            invokeBunches = new ArrayList<>();
            invokeRootCaFilename = null;
            invokeRootCaContent = null;
            invokeGpgKeyContent = null;
        }

        public void setExpectations(int expectedInvocationsIn,
                                    List<String> expectedInvocationNamesIn,
                                    List<String> expectedInvocationBunchesIn,
                                    String expectedRootCaFilenameIn,
                                    String expectedRootCaContentIn,
                                    String expectedGpgKeyContentIn) {
            expectedInvocations = expectedInvocationsIn;
            expectedInvocationNames = expectedInvocationNamesIn;
            expectedInvocationBunches = expectedInvocationBunchesIn;
            expectedRootCaFilename = expectedRootCaFilenameIn;
            expectedRootCaContent = expectedRootCaContentIn;
            expectedGpgKeyContent = expectedGpgKeyContentIn;
        }

        public void verifyTaskoCall() {
            assertEquals(expectedInvocations, invoked);
            assertEquals(expectedInvocationNames, invokeNames);
            assertEquals(expectedInvocationBunches, invokeBunches);
            for (String bunch : invokeBunches) {
                if (bunch.equals("root-ca-cert-update-bunch")) {
                    assertEquals(expectedRootCaFilename, invokeRootCaFilename);
                    assertEquals(expectedRootCaContent, invokeRootCaContent);
                }
                else if (bunch.equals("custom-gpg-key-import-bunch")) {
                    assertEquals(expectedGpgKeyContent, invokeGpgKeyContent);
                }
                else {
                    fail("Unexpected bunch called: " + bunch);
                }
            }
        }

        @Override
        protected Object invoke(String name, Object... args) throws TaskomaticApiException {
            invoked += 1;
            invokeNames.add(name);
            String bunch = (String) args[0];
            invokeBunches.add(bunch);
            Map<String, Object> paramList = (Map<String, Object>) args[1];
            if (bunch.equals("root-ca-cert-update-bunch")) {
                Map<String, String> fileToCaCertMap =
                        (Map<String, String>) paramList.get("filename_to_root_ca_cert_map");
                Optional<Map.Entry<String, String>> firstKeyVal = fileToCaCertMap.entrySet().stream().findFirst();
                if (firstKeyVal.isPresent()) {
                    invokeRootCaFilename = firstKeyVal.get().getKey();
                    invokeRootCaContent = firstKeyVal.get().getValue();
                }
                else {
                    invokeRootCaFilename = null;
                    invokeRootCaContent = null;
                }
            }
            else if (bunch.equals("custom-gpg-key-import-bunch")) {
                invokeGpgKeyContent = (String) paramList.get("gpg-key");
            }
            return null;
        }
    }

    private MockTaskomaticApi mockTaskomaticApi;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        satAdmin = UserTestUtils.createUser(TestStatics.TEST_SAT_USER, user.getOrg().getId());
        satAdmin.addPermanentRole(RoleFactory.SAT_ADMIN);
        UserFactory.save(satAdmin);

        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);

        // Setting a fake hostname for the token validation
        originalFqdn = ConfigDefaults.get().getHostname();
        originalServerSecret = Config.get().getString("server.secret_key");
        originalReportDb =  Config.get().getString(ConfigDefaults.REPORT_DB_NAME);
        originalProductVersion = Config.get().getString(ConfigDefaults.PRODUCT_VERSION_MGR);

        Config.get().setString(ConfigDefaults.SERVER_HOSTNAME, LOCAL_SERVER_FQDN);
        Config.get().setString("server.secret_key", // my-super-secret-key-for-testing
            "6D792D73757065722D7365637265742D6B65792D666F722D74657374696E670D0A");

        Config.get().setString(ConfigDefaults.REPORT_DB_NAME, "reportdb");
        Config.get().setString(ConfigDefaults.PRODUCT_VERSION_MGR, "5.1.0");

        hubFactory = new HubFactory();
        clientFactoryMock = mock(HubClientFactory.class);

        mockTaskomaticApi = new MockTaskomaticApi();
        SaltApi saltApi = new TestSaltApi();
        SystemEntitlementManager sysEntMgr = new SystemEntitlementManager(
                new SystemUnentitler(saltApi), new SystemEntitler(saltApi)
        );

        hubManager = new HubManager(hubFactory, clientFactoryMock, new MirrorCredentialsManager(), mockTaskomaticApi,
                sysEntMgr);
    }

    @AfterEach
    @Override
    public void tearDown() throws Exception {
        super.tearDown();

        Config.get().setString(ConfigDefaults.SERVER_HOSTNAME, originalFqdn);
        Config.get().setString("server.secret_key", originalServerSecret);
        Config.get().setString(ConfigDefaults.REPORT_DB_NAME, originalReportDb);
        Config.get().setString(ConfigDefaults.PRODUCT_VERSION_MGR, originalProductVersion);
    }

    /**
     * This test ensure the implementation does not have any public method that are not requiring either a user
     * or a token to enforce authorization.
     */
    @Test
    public void implementationDoesNotHaveAnyPublicUnprotectedMethods() {
        List<Method> publicClassMethods = Arrays.stream(hubManager.getClass().getMethods())
            // Exclude methods declared by Object
            .filter(method -> !Object.class.equals(method.getDeclaringClass()))
            // Exclude non-public methods
            .filter(method -> Modifier.isPublic(method.getModifiers()))
            .toList();

        // First parameter of the method must be either a User or an IssAccessToken
        List<Class<?>> allowedFirstParameters = List.of(User.class, IssAccessToken.class);

        // Extract all methods that don't have a valid first parameter
        List<String> unprotectedMethods = publicClassMethods.stream()
            .filter(method -> method.getParameterTypes().length == 0 ||
                    !allowedFirstParameters.contains(method.getParameterTypes()[0]))
            .map(Method::toGenericString)
            .toList();

        assertTrue(unprotectedMethods.isEmpty(),
            "These methods seem to not enforce authorization, as the first parameter is not any of %s:%n\t%s"
                .formatted(allowedFirstParameters, String.join("\n\t", unprotectedMethods))
        );

        for (Method method : publicClassMethods) {
            // Generate default values for all parameters except the first one. Since the method should fail due to
            // validation the other parameters should be irrelevant
            List<Object> params = Arrays.stream(method.getParameterTypes())
                .skip(1)
                .map(paramType -> getDefaultValue(paramType))
                .collect(Collectors.toList());

            Class<?> firstParameterClass = method.getParameterTypes()[0];
            String expectedMessage = "You do not have permissions to perform this action. ";

            IssAccessToken expiredToken = new IssAccessToken(
                TokenType.ISSUED,
                "dummy",
                "my.remote.server",
                Instant.now().minus(30, ChronoUnit.DAYS)
            );

            if (User.class.equals(firstParameterClass)) {
                params.add(0, user);
                expectedMessage += "You need to have at least a SUSE Manager Administrator role to perform this action";
            }
            else if (IssAccessToken.class.equals(firstParameterClass)) {
                params.add(0, expiredToken);
                expectedMessage += "Invalid token provided";
            }
            else {
                fail("Unable to identify a value for the first parameter type " + firstParameterClass);
                // appeasing the compiler: this line will never be executed.
                return;
            }

            // Try to invoke the method. It should fail with a permission exception. Since we are using reflection
            // it will be wrapped into an InvocationTargetException
            InvocationTargetException wrapperException = assertThrows(InvocationTargetException.class,
                () -> method.invoke(hubManager, params.toArray()));

            // Verify the actual exception is correct
            assertInstanceOf(PermissionException.class, wrapperException.getCause(),
                "Method " + method.toGenericString() + " is throwing an unexpected Exception");
            assertEquals(expectedMessage, wrapperException.getCause().getMessage(),
                "Method " + method.toGenericString() + " is throwing an unexpected exception message");

        }
    }

    @Test
    public void canIssueANewToken() throws Exception {
        Instant expectedExpiration = Instant.now().truncatedTo(ChronoUnit.SECONDS).plus(525_600L, ChronoUnit.MINUTES);
        String token = hubManager.issueAccessToken(satAdmin, REMOTE_SERVER_FQDN);

        // Ensure we get a token
        assertNotNull(token);

        // Ensure the token is correctly stored in the database
        IssAccessToken issAccessToken = hubFactory.lookupIssuedToken(token);
        assertNotNull(issAccessToken);
        assertEquals(REMOTE_SERVER_FQDN, issAccessToken.getServerFqdn());
        assertEquals(TokenType.ISSUED, issAccessToken.getType());
        assertFalse(issAccessToken.isExpired());
        assertTrue(issAccessToken.isValid());
        assertEquals(
            expectedExpiration,
            issAccessToken.getExpirationDate().toInstant().truncatedTo(ChronoUnit.SECONDS)
        );

        // Ensure we can decode the token correctly
        Token parsedJwtToken = new TokenParser()
            .usingServerSecret()
            .parse(token);

        assertEquals(expectedExpiration, parsedJwtToken.getExpirationTime());
        assertEquals(REMOTE_SERVER_FQDN, parsedJwtToken.getClaim("fqdn", String.class));
    }

    @Test
    public void canStoreThirdPartyToken() throws Exception {
        // This token has the following jwt payload:
        // {
        //     "jti" : "ftA6zJs1kBd7eGbGqeVKPQ",
        //     "exp" : 3313569600,
        //     "iat" : 1733393426,
        //     "nbf" : 1733393306,
        //     "fqdn" : "local-server.unit-test.local" (aka LOCAL_SERVER_FQDN)
        // }
        String token = """
            eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.ey\
            JqdGkiOiJmdEE2ekpzMWtCZDdlR2JHcWVWS1BRI\
            iwiZXhwIjozMzEzNTY5NjAwLCJpYXQiOjE3MzMz\
            OTM0MjYsIm5iZiI6MTczMzM5MzMwNiwiZnFkbiI\
            6ImxvY2FsLXNlcnZlci51bml0LXRlc3QubG9jYW\
            wifQ.6M9MOQvsiFr4EeyeAo36_2jUbV1Ju9ceCD\
            kI-mykFms""";

        hubManager.storeAccessToken(satAdmin, REMOTE_SERVER_FQDN, token);

        // Ensure the token is correctly stored in the database
        IssAccessToken issAccessToken = hubFactory.lookupAccessTokenFor(REMOTE_SERVER_FQDN);
        assertNotNull(issAccessToken);
        assertEquals(token, issAccessToken.getToken());
        assertEquals(TokenType.CONSUMED, issAccessToken.getType());
        assertNotNull(issAccessToken.getExpirationDate());
    }

    @Test
    public void rejectsTokenIfFqdnIsNotMatching() {
        // This token has the following jwt payload:
        // {
        //     "jti" : "ftA6zJs1kBd7eGbGqeVKPQ",
        //     "exp" : 3313569600,
        //     "iat" : 1733393426,
        //     "nbf" : 1733393306,
        //     "fqdn" : "different-server.unit-test.local"
        // }

        String token = """
            eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.ey\
            JqdGkiOiJmdEE2ekpzMWtCZDdlR2JHcWVWS1BRI\
            iwiZXhwIjozMzEzNTY5NjAwLCJpYXQiOjE3MzMz\
            OTM0MjYsIm5iZiI6MTczMzM5MzMwNiwiZnFkbiI\
            6ImRpZmZlcmVudC1zZXJ2ZXIudW5pdC10ZXN0Lm\
            xvY2FsIn0.GaW8A54CrfUI43MiCDw_7TPCUeL8l\
            TU76L5Yn9bXHGs""";

        var exception = assertThrows(
            TokenParsingException.class,
            () -> hubManager.storeAccessToken(satAdmin, REMOTE_SERVER_FQDN, token)
        );

        assertEquals(
            "FQDN do not match. Expected " + LOCAL_SERVER_FQDN + " got different-server.unit-test.local",
            exception.getMessage()
        );
    }

    @Test
    public void rejectsTokenIfAlreadyExpired() {
        // This token has the following jwt payload:
        // {
        //     "jti" : "ftA6zJs1kBd7eGbGqeVKPQ",
        //     "exp": 1607249426,
        //     "iat": 1575713426,
        //     "nbf": 1575713306,
        //     "fqdn" : "local-server.unit-test.local" (aka LOCAL_SERVER_FQDN)
        // }

        String token = """
            eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.ey\
            JqdGkiOiJmdEE2ekpzMWtCZDdlR2JHcWVWS1BRI\
            iwiZXhwIjoxNjA3MjQ5NDI2LCJpYXQiOjE1NzU3\
            MTM0MjYsIm5iZiI6MTU3NTcxMzMwNiwiZnFkbiI\
            6ImxvY2FsLXNlcnZlci51bml0LXRlc3QubG9jYW\
            wifQ.CWuFZX8KKFQ3Le7Qn0kGE_16ZCNs7QrneL\
            ajQABZuAw""";

        TokenParsingException exception = assertThrows(
            TokenParsingException.class,
            () -> hubManager.storeAccessToken(satAdmin, "external-server.dev.local", token)
        );

        InvalidJwtException cause = assertInstanceOf(InvalidJwtException.class, exception.getCause());
        assertEquals(1, cause.getErrorDetails().size());
        assertEquals(ErrorCodes.EXPIRED, cause.getErrorDetails().get(0).getErrorCode());
    }

    @Test
    public void canSaveHubAndPeripheralServers() throws TaskomaticApiException {
        hubManager.saveNewServer(getValidToken("dummy.hub.fqdn"), IssRole.HUB, "dummy-certificate-data",
                "dummy-gpg-key");

        Optional<IssHub> issHub = hubFactory.lookupIssHubByFqdn("dummy.hub.fqdn");
        assertTrue(issHub.isPresent());
        assertEquals("dummy-certificate-data", issHub.get().getRootCa());
        assertEquals("dummy-gpg-key", issHub.get().getGpgKey());

        hubManager.saveNewServer(getValidToken("dummy.peripheral.fqdn"), IssRole.PERIPHERAL, null, null);
        Optional<IssPeripheral> issPeripheral = hubFactory.lookupIssPeripheralByFqdn("dummy.peripheral.fqdn");
        assertTrue(issPeripheral.isPresent());
        assertNull(issPeripheral.get().getRootCa());
    }

    @Test
    public void canRetrieveHubAndPeripheralServers() {
        hubFactory.save(new IssHub("dummy.hub.fqdn", null));
        hubFactory.save(new IssPeripheral("dummy.peripheral.fqdn", null));

        IssServer result = hubManager.findServer(getValidToken("dummy.peripheral.fqdn"), IssRole.PERIPHERAL);
        assertNotNull(result);
        assertInstanceOf(IssPeripheral.class, result);

        result = hubManager.findServer(getValidToken("dummy.hub.fqdn"), IssRole.HUB);
        assertNotNull(result);
        assertInstanceOf(IssHub.class, result);

        result = hubManager.findServer(getValidToken("dummy.unknown.fqdn"), IssRole.HUB);
        assertNull(result);
    }

    @Test
    public void canUpdateServer() {
        hubFactory.save(new IssHub("dummy.hub.fqdn", null));
        hubFactory.save(new IssPeripheral("dummy.peripheral.fqdn", null));

        IssHub hub = (IssHub) hubManager.findServer(getValidToken("dummy.hub.fqdn"), IssRole.HUB);
        assertNull(hub.getRootCa());
        assertNull(hub.getMirrorCredentials());

        SCCCredentials sccCredentials = CredentialsFactory.createSCCCredentials("user", "password");
        CredentialsFactory.storeCredentials(sccCredentials);

        hub.setMirrorCredentials(sccCredentials);
        hub.setRootCa("--DUMMY--");

        Optional<IssHub> issHub = hubFactory.lookupIssHubByFqdn("dummy.hub.fqdn");
        assertTrue(issHub.isPresent());
        assertEquals("--DUMMY--", issHub.get().getRootCa());
        assertNotNull(issHub.get().getMirrorCredentials());
        assertEquals("user", issHub.get().getMirrorCredentials().getUsername());
        assertEquals("password", issHub.get().getMirrorCredentials().getPassword());
    }

    @Test
    public void canSaveRootCaAndGpg() throws TaskomaticApiException {
        mockTaskomaticApi.resetTaskomaticCall();
        mockTaskomaticApi.setExpectations(2,
                List.of("tasko.scheduleSingleSatBunchRun", "tasko.scheduleSingleSatBunchRun"),
                List.of("root-ca-cert-update-bunch", "custom-gpg-key-import-bunch"),
                "hub_dummy.hub.fqdn_root_ca.pem", "dummy-hub-certificate-data",
                "dummy-gpg-key");
        hubManager.saveNewServer(getValidToken("dummy.hub.fqdn"), IssRole.HUB, "dummy-hub-certificate-data",
                "dummy-gpg-key");
        mockTaskomaticApi.verifyTaskoCall();

        mockTaskomaticApi.resetTaskomaticCall();
        mockTaskomaticApi.setExpectations(0,
                List.of(),
                List.of(),
                "hub_dummy2.hub.fqdn_root_ca.pem", "", "");
        hubManager.saveNewServer(getValidToken("dummy2.hub.fqdn"), IssRole.HUB, "", "");
        mockTaskomaticApi.verifyTaskoCall();

        mockTaskomaticApi.resetTaskomaticCall();
        mockTaskomaticApi.setExpectations(0,
                List.of(),
                List.of(),
                "hub_dummy3.hub.fqdn_root_ca.pem", "", "");
        hubManager.saveNewServer(getValidToken("dummy3.hub.fqdn"), IssRole.HUB, null, null);
        mockTaskomaticApi.verifyTaskoCall();

        mockTaskomaticApi.resetTaskomaticCall();
        mockTaskomaticApi.setExpectations(1,
                List.of("tasko.scheduleSingleSatBunchRun"),
                List.of("root-ca-cert-update-bunch"),
                "peripheral_dummy.periph.fqdn_root_ca.pem",
                "dummy-periph-certificate-data", null);
        hubManager.saveNewServer(getValidToken("dummy.periph.fqdn"), IssRole.PERIPHERAL,
                "dummy-periph-certificate-data", null);
        mockTaskomaticApi.verifyTaskoCall();

        mockTaskomaticApi.resetTaskomaticCall();
        mockTaskomaticApi.setExpectations(0,
                List.of(),
                List.of(),
                "peripheral_dummy2.periph.fqdn_root_ca.pem", "", "");
        hubManager.saveNewServer(getValidToken("dummy2.periph.fqdn"), IssRole.PERIPHERAL, "", "");
        mockTaskomaticApi.verifyTaskoCall();

        mockTaskomaticApi.resetTaskomaticCall();
        mockTaskomaticApi.setExpectations(0,
                List.of(),
                List.of(),
                "peripheral_dummy3.periph.fqdn_root_ca.pem", "", "");
        hubManager.saveNewServer(getValidToken("dummy3.periph.fqdn"), IssRole.PERIPHERAL, null, null);
        mockTaskomaticApi.verifyTaskoCall();
    }

    @Test
    public void canStoreSCCCredentials() throws TaskomaticApiException {
        IssAccessToken hubToken = getValidToken("dummy.hub.fqdn");
        hubManager.saveNewServer(hubToken, IssRole.HUB, null, null);

        // Ensure no credentials exists
        assertEquals(0, CredentialsFactory.listSCCCredentials().stream()
            .filter(creds -> "https://dummy.hub.fqdn".equals(creds.getUrl()))
            .count());

        SCCCredentials sccCredentials = hubManager.storeSCCCredentials(hubToken, "dummy-username", "dummy-password");
        assertEquals("dummy-username", sccCredentials.getUsername());
        assertEquals("dummy-password", sccCredentials.getPassword());
        assertEquals("https://dummy.hub.fqdn", sccCredentials.getUrl());
    }

    @Test
    public void canStoreSCCCredentialsWhenTheyAlreadyExist() throws TaskomaticApiException {
        IssAccessToken hubToken = getValidToken("dummy.hub.fqdn");
        IssHub hub = (IssHub) hubManager.saveNewServer(hubToken, IssRole.HUB, null, null);

        SCCCredentials sccCredentials = CredentialsFactory.createSCCCredentials("user", "password");
        CredentialsFactory.storeCredentials(sccCredentials);

        long id = sccCredentials.getId();
        hub.setMirrorCredentials(sccCredentials);
        hubFactory.save(hub);

        TestUtils.flushAndClearSession();

        sccCredentials = hubManager.storeSCCCredentials(hubToken, "dummy-username", "dummy-password");
        assertEquals(id, sccCredentials.getId());
        assertEquals("dummy-username", sccCredentials.getUsername());
        assertEquals("dummy-password", sccCredentials.getPassword());
        assertEquals("https://dummy.hub.fqdn", sccCredentials.getUrl());
    }

    @Test
    public void canRegenerateSCCCredentialsForAPeripheral()
        throws TokenException, TaskomaticApiException, CertificateException, IOException {
        String fqdn = LOCAL_SERVER_FQDN;
        IssAccessToken token = createPeripheralRegistration(fqdn, null);

        IssPeripheral peripheral = hubFactory.lookupIssPeripheralByFqdn(fqdn)
            .orElseGet(() -> fail("Peripheral Server not found"));

        long peripheralId = peripheral.getId();
        long credentialsId = peripheral.getMirrorCredentials().getId();
        String expectedUsername = "peripheral-%06d".formatted(peripheralId);
        String previousPassword = peripheral.getMirrorCredentials().getPassword();

        HubInternalClient internalClient = mock(HubInternalClient.class);

        context().checking(new Expectations() {{
            allowing(clientFactoryMock).newInternalClient(fqdn, token.getToken(), null);
            will(returnValue(internalClient));

            allowing(internalClient)
                .storeCredentials(with(equal(expectedUsername)), with(any(String.class)));
        }});

        TestUtils.flushAndClearSession();

        HubSCCCredentials newCredentials = hubManager.regenerateCredentials(satAdmin, peripheralId);

        assertEquals(credentialsId, newCredentials.getId());
        assertEquals(expectedUsername, newCredentials.getUsername());
        assertNotEquals(previousPassword, newCredentials.getPassword());
        assertEquals(fqdn, newCredentials.getPeripheralUrl());

        peripheral = hubFactory.findPeripheralById(peripheralId);
        assertNotNull(peripheral);
        assertEquals(newCredentials, peripheral.getMirrorCredentials());
    }

    @Test
    public void canDeregisterHub() throws Exception {
        String fqdn = LOCAL_SERVER_FQDN;
        IssAccessToken token = createHubRegistration(fqdn, null, null);
        HubInternalClient internalClient = mock(HubInternalClient.class);

        context().checking(new Expectations() {{
            allowing(clientFactoryMock).newInternalClient(fqdn, token.getToken(), null);
            will(returnValue(internalClient));

            allowing(internalClient).deregister();
        }});

        hubManager.deregister(satAdmin, fqdn, IssRole.HUB, false);

        assertNull(hubFactory.lookupAccessTokenFor(fqdn));
        assertNull(hubFactory.lookupIssuedToken(fqdn));
        assertTrue(hubFactory.lookupIssHub().isEmpty(), "Failed to remove Hub");
        assertEquals(0, CredentialsFactory.listSCCCredentials().size());
    }

    @Test
    public void canDeregisterHubLocalOnly() throws Exception {
        String fqdn = LOCAL_SERVER_FQDN;
        hubManager.deregister(satAdmin, fqdn, IssRole.HUB, true);

        assertNull(hubFactory.lookupAccessTokenFor(fqdn));
        assertNull(hubFactory.lookupIssuedToken(fqdn));
        assertTrue(hubFactory.lookupIssHub().isEmpty(), "Failed to remove Hub");
        assertEquals(0, CredentialsFactory.listSCCCredentials().size());
    }

    @Test
    public void canDeregisterPeripheral() throws Exception {
        String fqdn = LOCAL_SERVER_FQDN;
        IssAccessToken token = createPeripheralRegistration(fqdn, null);
        HubInternalClient internalClient = mock(HubInternalClient.class);

        context().checking(new Expectations() {{
            allowing(clientFactoryMock).newInternalClient(fqdn, token.getToken(), null);
            will(returnValue(internalClient));

            allowing(internalClient).deregister();
        }});

        hubManager.deregister(satAdmin, fqdn, IssRole.PERIPHERAL, false);

        assertNull(hubFactory.lookupAccessTokenFor(fqdn));
        assertNull(hubFactory.lookupIssuedToken(fqdn));
        assertTrue(hubFactory.lookupIssPeripheralByFqdn(fqdn).isEmpty(), "Failed to remove Peripheral");
        assertEquals(0, CredentialsFactory.listCredentialsByType(HubSCCCredentials.class).size());
    }

    @Test
    public void canDeregisterPeripheralLocalOnly() throws Exception {
        String fqdn = LOCAL_SERVER_FQDN;

        hubManager.deregister(satAdmin, fqdn, IssRole.PERIPHERAL, true);

        assertNull(hubFactory.lookupAccessTokenFor(fqdn));
        assertNull(hubFactory.lookupIssuedToken(fqdn));
        assertTrue(hubFactory.lookupIssPeripheralByFqdn(fqdn).isEmpty(), "Failed to remove Peripheral");
        assertEquals(0, CredentialsFactory.listCredentialsByType(HubSCCCredentials.class).size());
    }

    @Test
    public void canDeregisterHubWithToken() throws TokenBuildingException, TaskomaticApiException,
            TokenParsingException {
        String fqdn = LOCAL_SERVER_FQDN;
        IssAccessToken token = createHubRegistration(fqdn, null, null);
        IssRole remoteRole = hubManager.deleteIssServerLocal(token, fqdn);

        assertEquals(IssRole.HUB, remoteRole);
        assertNull(hubFactory.lookupAccessTokenFor(fqdn));
        assertNull(hubFactory.lookupIssuedToken(fqdn));
        assertTrue(hubFactory.lookupIssHub().isEmpty(), "Failed to remove Hub");
        assertEquals(0, CredentialsFactory.listSCCCredentials().size());
    }

    @Test
    public void canDeregisterPeripheralWithToken() throws TokenBuildingException, TaskomaticApiException,
            TokenParsingException {
        String fqdn = LOCAL_SERVER_FQDN;
        IssAccessToken token = createPeripheralRegistration(fqdn, null);
        IssRole remoteRole = hubManager.deleteIssServerLocal(token, fqdn);

        assertEquals(IssRole.PERIPHERAL, remoteRole);
        assertNull(hubFactory.lookupAccessTokenFor(fqdn));
        assertNull(hubFactory.lookupIssuedToken(fqdn));
        assertTrue(hubFactory.lookupIssPeripheralByFqdn(fqdn).isEmpty(), "Failed to remove Peripheral");
        assertEquals(0, CredentialsFactory.listCredentialsByType(HubSCCCredentials.class).size());
    }

    @Test
    public void canRegisterPeripheralWithUserNameAndPassword()
            throws TokenBuildingException, CertificateException, IOException, TokenParsingException,
            TaskomaticApiException {
        HubExternalClient externalClient = mock(HubExternalClient.class);
        HubInternalClient internalClient = mock(HubInternalClient.class);

        // The token generated by REMOTE_SERVER_FQDN for LOCAL_SERVER_FQDN
        String remoteTokenForLocal = """
            eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.ey\
            JqdGkiOiJmdEE2ekpzMWtCZDdlR2JHcWVWS1BRI\
            iwiZXhwIjozMzEzNTY5NjAwLCJpYXQiOjE3MzMz\
            OTM0MjYsIm5iZiI6MTczMzM5MzMwNiwiZnFkbiI\
            6ImxvY2FsLXNlcnZlci51bml0LXRlc3QubG9jYW\
            wifQ.XldOvQJypIkb4E1am0JlBxsHYrx_7J77s1\
            vTrvoNlEU""";

        ManagerInfoJson mgrInfo = new ManagerInfoJson("5.1.0", true, "reportdb", REMOTE_SERVER_FQDN, 5432);

        context().checking(new Expectations() {{
            allowing(clientFactoryMock).newExternalClient(REMOTE_SERVER_FQDN, "admin", "admin", null);
            will(returnValue(externalClient));

            allowing(externalClient).generateAccessToken(LOCAL_SERVER_FQDN);
            will(returnValue(remoteTokenForLocal));

            allowing(externalClient).close();

            allowing(clientFactoryMock).newInternalClient(REMOTE_SERVER_FQDN, remoteTokenForLocal, null);
            will(returnValue(internalClient));

            allowing(internalClient).registerHub(
                with(any(String.class)),
                with(aNull(String.class)),
                with(aNull(String.class))
            );

            allowing(internalClient).storeCredentials(with(any(String.class)), with(any(String.class)));

            allowing(internalClient).getManagerInfo();
            will(returnValue(mgrInfo));

            allowing(internalClient).storeReportDbCredentials(with(any(String.class)), with(any(String.class)));

            allowing(internalClient).scheduleProductRefresh();

            allowing(internalClient).getServerInfo();
            will(returnValue(new ServerInfoJson()));
        }});

        // Register the remote server as PERIPHERAL for this local server
        hubManager.register(satAdmin, REMOTE_SERVER_FQDN, "admin", "admin", null);

        // Verify the remote server is saved as peripheral
        Optional<IssPeripheral> issPeripheral = hubFactory.lookupIssPeripheralByFqdn(REMOTE_SERVER_FQDN);
        assertTrue(issPeripheral.isPresent());

        // Verify we have both tokens for the remote server
        var consumed = hubFactory.lookupAccessTokenByFqdnAndType(REMOTE_SERVER_FQDN, TokenType.CONSUMED);
        assertNotNull(consumed);
        assertEquals(remoteTokenForLocal, consumed.getToken());

        var issued = hubFactory.lookupAccessTokenByFqdnAndType(REMOTE_SERVER_FQDN, TokenType.ISSUED);
        assertNotNull(issued);

        Optional<Server> optServer = ServerFactory.findByFqdn(REMOTE_SERVER_FQDN);
        if (optServer.isPresent()) {
            Server srv = optServer.get();
            MgrServerInfo mgrServerInfo = srv.getMgrServerInfo();
            assertEquals(mgrInfo.getReportDbName(), mgrServerInfo.getReportDbName());
            assertEquals(mgrInfo.getVersion(), mgrServerInfo.getVersion().getVersion());
            assertTrue(srv.hasEntitlement(EntitlementManager.FOREIGN));
        }
        else {
            fail("Server not found");
        }
    }

     @Test
     public void canReplaceTokensLocal() throws TokenBuildingException, TaskomaticApiException, TokenParsingException {
         String hubFqdn = "hub.domain.top";

         IssAccessToken currentToken = createHubRegistration(hubFqdn, null, null);
         List<String> tokenList = hubFactory.listAccessTokensByFqdn(hubFqdn)
                 .stream().map(IssAccessToken::getToken).toList();
         assertEquals(2, tokenList.size());
         assertTrue(tokenList.contains(currentToken.getToken()), "Expected token not found");

         Token token = new IssTokenBuilder(hubFqdn)
                 .usingServerSecret()
                 .build();
         IssAccessToken newHubToken = new IssAccessToken(TokenType.ISSUED, token.getSerializedForm(), hubFqdn,
                 token.getExpirationTime());
         assertNotEquals(currentToken.getToken(), newHubToken.getToken());

         String newRemoteToken = hubManager.replaceTokens(currentToken, newHubToken.getToken());
         TestUtils.flushAndClearSession();

         assertNotNull(newRemoteToken);
         assertTrue(tokenList.stream().noneMatch(t -> t.equals(newRemoteToken)));
         assertNotEquals(currentToken.getToken(), newRemoteToken);

         List<String> newTokenList = hubFactory.listAccessTokensByFqdn(hubFqdn)
                 .stream().map(IssAccessToken::getToken).toList();
         assertEquals(2, newTokenList.size());
         assertNotEquals(tokenList, newTokenList);
     }

    @Test
    public void canReplaceTokensOnHub() throws TokenBuildingException, TaskomaticApiException, TokenParsingException,
            CertificateException, IOException {
        String peripherlaFqdn = "peripheral.domain.top";
        HubInternalClient internalClient = mock(HubInternalClient.class);

        IssAccessToken currentToken = createPeripheralRegistration(peripherlaFqdn, null);
        List<String> tokenList = hubFactory.listAccessTokensByFqdn(peripherlaFqdn)
                .stream().map(IssAccessToken::getToken).toList();
        assertEquals(2, tokenList.size());
        assertTrue(tokenList.contains(currentToken.getToken()), "Expected token not found");

        Token token = new IssTokenBuilder(peripherlaFqdn)
                .usingServerSecret()
                .build();
        IssAccessToken newHubToken = new IssAccessToken(TokenType.ISSUED, token.getSerializedForm(), peripherlaFqdn,
                token.getExpirationTime());
        assertNotEquals(currentToken.getToken(), newHubToken.getToken());

        context().checking(new Expectations() {{
            allowing(clientFactoryMock).newInternalClient(peripherlaFqdn, currentToken.getToken(), null);
            will(returnValue(internalClient));

            allowing(internalClient).replaceTokens(with(any(String.class)));
            will(returnValue(newHubToken.getToken()));
        }});

        hubManager.replaceTokensHub(satAdmin, peripherlaFqdn);
        TestUtils.flushAndClearSession();

        List<String> newTokenList = hubFactory.listAccessTokensByFqdn(peripherlaFqdn)
                .stream().map(IssAccessToken::getToken).toList();
        assertEquals(2, newTokenList.size());
        assertNotEquals(tokenList, newTokenList);
        assertTrue(newTokenList.contains(newHubToken.getToken()), "Expected new token not found");
    }

    @Test
    public void canUpdateServerDetails() throws TokenBuildingException, TaskomaticApiException, TokenParsingException {
        createHubRegistration("hub.domain.com", "---- BEGIN ROOT CA ----", "---- BEGIN GPG PUB KEY -----");
        IssHub hub = hubFactory.lookupIssHubByFqdn("hub.domain.com").orElseGet(() -> fail("Hub Server not found"));
        assertEquals("---- BEGIN ROOT CA ----", hub.getRootCa());
        assertEquals("---- BEGIN GPG PUB KEY -----", hub.getGpgKey());

        UpdatableServerData data = new UpdatableServerData(Map.of(
            "gpg_key", "---- BEGIN NEW GPG PUB KEY -----",
            "root_ca", "---- BEGIN NEW ROOT CA ----")
        );

        hubManager.updateServerData(satAdmin, "hub.domain.com", IssRole.valueOf("HUB"), data);
        TestUtils.flushAndClearSession();

        hub = hubFactory.lookupIssHubByFqdn("hub.domain.com").orElseGet(() -> fail("Hub Server not found"));
        assertEquals("---- BEGIN NEW ROOT CA ----", hub.getRootCa());
        assertEquals("---- BEGIN NEW GPG PUB KEY -----", hub.getGpgKey());
        assertThrows(IllegalArgumentException.class,
                () -> hubManager.updateServerData(satAdmin, "hub1.domain.com", IssRole.valueOf("HUB"), data));

        // PERIPHERAL

        createPeripheralRegistration("peripheral.domain.com", "---- BEGIN ROOT CA ----");
        IssPeripheral peripheral = hubFactory.lookupIssPeripheralByFqdn("peripheral.domain.com")
                .orElseGet(() -> fail("Peripheral Server not found"));
        assertEquals("---- BEGIN ROOT CA ----", peripheral.getRootCa());

        hubManager.updateServerData(satAdmin, "peripheral.domain.com", IssRole.valueOf("PERIPHERAL"), data);
        TestUtils.flushAndClearSession();

        peripheral = hubFactory.lookupIssPeripheralByFqdn("peripheral.domain.com")
                .orElseGet(() -> fail("Peripheral Server not found"));
        assertEquals("---- BEGIN NEW ROOT CA ----", peripheral.getRootCa());
        assertThrows(IllegalArgumentException.class, () -> hubManager.updateServerData(satAdmin,
                "peripheral1.domain.com", IssRole.valueOf("PERIPHERAL"), data));
    }

    @Test
    public void canDeleteIssV1Slave() throws Exception {
        IssSlave slave = new IssSlave();
        slave.setSlave(REMOTE_SERVER_FQDN);
        slave.setEnabled("Y");
        slave.setAllowAllOrgs("Y");
        IssFactory.save(slave);

        IssAccessToken token = createPeripheralRegistration(REMOTE_SERVER_FQDN, null);
        HubInternalClient internalClient = mock(HubInternalClient.class);

        context().checking(new Expectations() {{
            allowing(clientFactoryMock).newInternalClient(REMOTE_SERVER_FQDN, token.getToken(), null);
            will(returnValue(internalClient));

            allowing(internalClient).deleteIssV1Master();
        }});

        hubManager.deleteIssV1Slave(satAdmin, REMOTE_SERVER_FQDN, false);

        assertNull(IssFactory.lookupSlaveByName(REMOTE_SERVER_FQDN));
    }

    @Test
    public void deleteIssV1SlaveDoesNotCallSlaveWhenSettingAsOnlyLocal() throws Exception {
        IssSlave slave = new IssSlave();
        slave.setSlave(REMOTE_SERVER_FQDN);
        slave.setEnabled("Y");
        slave.setAllowAllOrgs("Y");
        IssFactory.save(slave);

        createPeripheralRegistration(REMOTE_SERVER_FQDN, null);

        hubManager.deleteIssV1Slave(satAdmin, REMOTE_SERVER_FQDN, true);

        assertNull(IssFactory.lookupSlaveByName(REMOTE_SERVER_FQDN));
    }

    @Test
    public void deleteIssV1SlaveDoesNotDeleteIfTheSlaveIsMissing() {
        IssSlave slave = new IssSlave();
        slave.setSlave(REMOTE_SERVER_FQDN);
        slave.setEnabled("Y");
        slave.setAllowAllOrgs("Y");
        IssFactory.save(slave);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> hubManager.deleteIssV1Slave(satAdmin, REMOTE_SERVER_FQDN, true));

        assertEquals(REMOTE_SERVER_FQDN + " is not registered as an ISS v3 peripheral", exception.getMessage());
    }

    @Test
    public void deleteIssV1SlaveDoesNotDeleteIfThePeripheralIsMissing() throws Exception {
        createPeripheralRegistration(REMOTE_SERVER_FQDN, null);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> hubManager.deleteIssV1Slave(satAdmin, REMOTE_SERVER_FQDN, true));

        assertEquals(REMOTE_SERVER_FQDN + " is not registered as an ISS v1 slave", exception.getMessage());
    }

    @Test
    public void canLocallyDeleteIssV1Master() throws Exception {
        IssMaster hub = new IssMaster();
        hub.setLabel(REMOTE_SERVER_FQDN);
        hub.makeDefaultMaster();
        IssFactory.save(hub);

        IssAccessToken token = createHubRegistration(REMOTE_SERVER_FQDN, null, null);

        hubManager.deleteIssV1Master(token);

        assertNull(IssFactory.lookupMasterByLabel(REMOTE_SERVER_FQDN));
        assertNull(IssFactory.getCurrentMaster());
    }

    @Test
    public void deleteIssV1MasterDoesNotDeleteIfMasterIsMissing() throws Exception {
        IssAccessToken token = createHubRegistration(REMOTE_SERVER_FQDN, null, null);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> hubManager.deleteIssV1Master(token));
        assertEquals(REMOTE_SERVER_FQDN + " is not registered as an ISS v1 master", exception.getMessage());
    }

    @Test
    public void deleteIssV1MasterDoesNotDeleteIfHubIsMissing() {
        IssMaster hub = new IssMaster();
        hub.setLabel(REMOTE_SERVER_FQDN);
        hub.makeDefaultMaster();
        IssFactory.save(hub);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> hubManager.deleteIssV1Master(getValidToken(REMOTE_SERVER_FQDN)));
        assertEquals(REMOTE_SERVER_FQDN + " is not registered as an ISS v3 hub", exception.getMessage());
    }

    private static IssAccessToken getValidToken(String fdqn) {
        return new IssAccessToken(TokenType.ISSUED, "dummy-token", fdqn);
    }

    private static <T> Object getDefaultValue(Class<T> clazz) {
        if (int.class.equals(clazz)) {
            return 0;
        }
        else if (boolean.class.equals(clazz)) {
            return false;
        }
        else if (double.class.equals(clazz)) {
            return 0.0;
        }
        else if (float.class.equals(clazz)) {
            return 0.0f;
        }
        else if (long.class.equals(clazz)) {
            return 0L;
        }
        else if (short.class.equals(clazz)) {
            return (short) 0;
        }
        else if (byte.class.equals(clazz)) {
            return (byte) 0;
        }
        else if (char.class.equals(clazz)) {
            return '\u0000';
        }

        return null;
    }

    private IssAccessToken createPeripheralRegistration(String fqdn, String rootCA) throws TaskomaticApiException,
            TokenBuildingException, TokenParsingException {
        Config.get().setString(ConfigDefaults.SERVER_HOSTNAME, fqdn);
        String peripheralTokenStr = hubManager.issueAccessToken(satAdmin, fqdn);
        hubManager.storeAccessToken(satAdmin, fqdn, peripheralTokenStr);
        IssAccessToken peripheralToken = hubFactory.lookupIssuedToken(peripheralTokenStr);
        var peripheral = (IssPeripheral) hubManager.saveNewServer(peripheralToken, IssRole.PERIPHERAL, rootCA, null);
        var hubSCCCredentials = CredentialsFactory.createHubSCCCredentials("peripheral-dummy-username",
                "peripheral-dummy-password", peripheral.getFqdn());
        CredentialsFactory.storeCredentials(hubSCCCredentials);

        peripheral.setMirrorCredentials(hubSCCCredentials);
        hubFactory.save(peripheral);
        TestUtils.flushAndClearSession();

        assertEquals(TokenType.CONSUMED, hubFactory.lookupAccessTokenFor(fqdn).getType());
        assertTrue(hubFactory.lookupIssPeripheralByFqdn(fqdn).isPresent(), "Failed to create Peripheral");
        assertEquals(1, CredentialsFactory.listCredentialsByType(HubSCCCredentials.class).size());

        return peripheralToken;
    }

    private IssAccessToken createHubRegistration(String fqdn, String rootCA, String gpgKey)
            throws TaskomaticApiException, TokenBuildingException, TokenParsingException {
        Config.get().setString(ConfigDefaults.SERVER_HOSTNAME, fqdn);
        String hubTokenStr = hubManager.issueAccessToken(satAdmin, fqdn);
        hubManager.storeAccessToken(satAdmin, fqdn, hubTokenStr);
        IssAccessToken hubToken = hubFactory.lookupIssuedToken(hubTokenStr);
        hubManager.saveNewServer(hubToken, IssRole.HUB, rootCA, gpgKey);
        hubManager.storeSCCCredentials(hubToken, "dummy-username", "dummy-password");
        TestUtils.flushAndClearSession();

        assertEquals(TokenType.CONSUMED, hubFactory.lookupAccessTokenFor(fqdn).getType());
        assertTrue(hubFactory.lookupIssHub().isPresent(), "Failed to create Hub");
        assertEquals(1, CredentialsFactory.listSCCCredentials().size());

        return hubToken;
    }
}
