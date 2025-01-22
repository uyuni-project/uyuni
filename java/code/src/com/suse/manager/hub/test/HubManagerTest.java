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
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.manager.setup.MirrorCredentialsManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.manager.hub.HubManager;
import com.suse.manager.hub.IssClientFactory;
import com.suse.manager.hub.IssExternalClient;
import com.suse.manager.hub.IssInternalClient;
import com.suse.manager.model.hub.HubFactory;
import com.suse.manager.model.hub.IssAccessToken;
import com.suse.manager.model.hub.IssHub;
import com.suse.manager.model.hub.IssPeripheral;
import com.suse.manager.model.hub.IssRole;
import com.suse.manager.model.hub.IssServer;
import com.suse.manager.model.hub.SCCCredentialsJson;
import com.suse.manager.model.hub.TokenType;
import com.suse.manager.webui.utils.token.Token;
import com.suse.manager.webui.utils.token.TokenBuildingException;
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

    private IssClientFactory clientFactoryMock;

    private String originalServerSecret;

    private String originalFqdn;

    static class MockTaskomaticApi extends TaskomaticApi {
        private boolean invokeCalled;
        private String invokeName;
        private String invokeBunch;
        private String invokeRootCaFilename;
        private String invokeRootCaContent;

        MockTaskomaticApi() {
            resetTaskomaticCall();
        }

        public void resetTaskomaticCall() {
            invokeCalled = false;
            invokeName = "";
            invokeBunch = "";
            invokeRootCaFilename = null;
            invokeRootCaContent = null;
        }

        public void verifyTaskoRootCaCertUpdateCall(String expectedRootCaFilename,
                                                    String expectedRootCaContent) {
            assertTrue(invokeCalled);
            assertEquals("tasko.scheduleSingleSatBunchRun", invokeName);
            assertEquals("root-ca-cert-update-bunch", invokeBunch);
            assertEquals(expectedRootCaFilename, invokeRootCaFilename);
            assertEquals(expectedRootCaContent, invokeRootCaContent);
            resetTaskomaticCall();
        }

        @Override
        protected Object invoke(String name, Object... args) throws TaskomaticApiException {
            invokeCalled = true;
            invokeName = name;
            invokeBunch = (String) args[0];
            Map<String, Object> paramList = (Map<String, Object>) args[1];
            Map<String, String> fileToCaCertMap = (Map<String, String>) paramList.get("filename_to_root_ca_cert_map");
            Optional<Map.Entry<String, String>> firstKeyVal = fileToCaCertMap.entrySet().stream().findFirst();
            if (firstKeyVal.isPresent()) {
                invokeRootCaFilename = firstKeyVal.get().getKey();
                invokeRootCaContent = firstKeyVal.get().getValue();
            }
            else {
                invokeRootCaFilename = null;
                invokeRootCaContent = null;
            }
            return null;
        }
    }

    private MockTaskomaticApi mockTaskomaticApi;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        satAdmin = UserTestUtils.createUser("satUser", user.getOrg().getId());
        satAdmin.addPermanentRole(RoleFactory.SAT_ADMIN);
        UserFactory.save(satAdmin);

        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);

        // Setting a fake hostname for the token validation
        originalFqdn = ConfigDefaults.get().getHostname();
        originalServerSecret = Config.get().getString("server.secret_key");

        Config.get().setString(ConfigDefaults.SERVER_HOSTNAME, LOCAL_SERVER_FQDN);
        Config.get().setString("server.secret_key", // my-super-secret-key-for-testing
            "6D792D73757065722D7365637265742D6B65792D666F722D74657374696E670D0A");

        hubFactory = new HubFactory();
        clientFactoryMock = mock(IssClientFactory.class);

        mockTaskomaticApi = new MockTaskomaticApi();
        hubManager = new HubManager(hubFactory, clientFactoryMock, new MirrorCredentialsManager(), mockTaskomaticApi);
    }

    @AfterEach
    @Override
    public void tearDown() throws Exception {
        super.tearDown();

        Config.get().setString(ConfigDefaults.SERVER_HOSTNAME, originalFqdn);
        Config.get().setString("server.secret_key", originalServerSecret);
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
            .filter(method -> !allowedFirstParameters.contains(method.getParameterTypes()[0]))
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
        hubManager.saveNewServer(getValidToken("dummy.hub.fqdn"), IssRole.HUB, "dummy-certificate-data");

        Optional<IssHub> issHub = hubFactory.lookupIssHubByFqdn("dummy.hub.fqdn");
        assertTrue(issHub.isPresent());
        assertEquals("dummy-certificate-data", issHub.get().getRootCa());

        hubManager.saveNewServer(getValidToken("dummy.peripheral.fqdn"), IssRole.PERIPHERAL, null);
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
    public void canSaveRootCa() throws TaskomaticApiException {
        mockTaskomaticApi.resetTaskomaticCall();
        hubManager.saveNewServer(getValidToken("dummy.hub.fqdn"), IssRole.HUB, "dummy-hub-certificate-data");
        mockTaskomaticApi.verifyTaskoRootCaCertUpdateCall("hub_dummy.hub.fqdn_root_ca.pem",
                "dummy-hub-certificate-data");

        mockTaskomaticApi.resetTaskomaticCall();
        hubManager.saveNewServer(getValidToken("dummy2.hub.fqdn"), IssRole.HUB, "");
        mockTaskomaticApi.verifyTaskoRootCaCertUpdateCall("hub_dummy2.hub.fqdn_root_ca.pem", "");

        mockTaskomaticApi.resetTaskomaticCall();
        hubManager.saveNewServer(getValidToken("dummy3.hub.fqdn"), IssRole.HUB, null);
        mockTaskomaticApi.verifyTaskoRootCaCertUpdateCall("hub_dummy3.hub.fqdn_root_ca.pem", "");

        mockTaskomaticApi.resetTaskomaticCall();
        hubManager.saveNewServer(getValidToken("dummy.periph.fqdn"), IssRole.PERIPHERAL,
                "dummy-periph-certificate-data");
        mockTaskomaticApi.verifyTaskoRootCaCertUpdateCall("peripheral_dummy.periph.fqdn_root_ca.pem",
                "dummy-periph-certificate-data");

        mockTaskomaticApi.resetTaskomaticCall();
        hubManager.saveNewServer(getValidToken("dummy2.periph.fqdn"), IssRole.PERIPHERAL, "");
        mockTaskomaticApi.verifyTaskoRootCaCertUpdateCall("peripheral_dummy2.periph.fqdn_root_ca.pem",
                "");

        mockTaskomaticApi.resetTaskomaticCall();
        hubManager.saveNewServer(getValidToken("dummy3.periph.fqdn"), IssRole.PERIPHERAL, null);
        mockTaskomaticApi.verifyTaskoRootCaCertUpdateCall("peripheral_dummy3.periph.fqdn_root_ca.pem",
                "");
    }

    @Test
    public void canGenerateSCCCredentials() throws TaskomaticApiException {
        String peripheralFqdn = "dummy.peripheral.fqdn";

        IssAccessToken peripheralToken = getValidToken(peripheralFqdn);
        var peripheral = (IssPeripheral) hubManager.saveNewServer(peripheralToken, IssRole.PERIPHERAL, null);

        // Ensure no credentials exists
        assertEquals(0, CredentialsFactory.listCredentialsByType(HubSCCCredentials.class).stream()
            .filter(creds -> peripheralFqdn.equals(creds.getPeripheralUrl()))
            .count());

        HubSCCCredentials hubSCCCredentials = hubManager.generateSCCCredentials(peripheralToken);
        assertEquals("peripheral-%06d".formatted(peripheral.getId()), hubSCCCredentials.getUsername());
        assertNotNull(hubSCCCredentials.getPassword());
        assertEquals(peripheralFqdn, hubSCCCredentials.getPeripheralUrl());
    }

    @Test
    public void canStoreSCCCredentials() throws TaskomaticApiException {
        IssAccessToken hubToken = getValidToken("dummy.hub.fqdn");
        var hub = (IssHub) hubManager.saveNewServer(hubToken, IssRole.HUB, null);

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
    public void canRegisterPeripheralWithUserNameAndPassword()
            throws TokenBuildingException, CertificateException, IOException, TokenParsingException,
            TaskomaticApiException {
        IssExternalClient externalClient = mock(IssExternalClient.class);
        IssInternalClient internalClient = mock(IssInternalClient.class);

        // The token generated by REMOTE_SERVER_FQDN for LOCAL_SERVER_FQDN
        String remoteTokenForLocal = """
            eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.ey\
            JqdGkiOiJmdEE2ekpzMWtCZDdlR2JHcWVWS1BRI\
            iwiZXhwIjozMzEzNTY5NjAwLCJpYXQiOjE3MzMz\
            OTM0MjYsIm5iZiI6MTczMzM5MzMwNiwiZnFkbiI\
            6ImxvY2FsLXNlcnZlci51bml0LXRlc3QubG9jYW\
            wifQ.XldOvQJypIkb4E1am0JlBxsHYrx_7J77s1\
            vTrvoNlEU""";

        context().checking(new Expectations() {{
            allowing(clientFactoryMock).newExternalClient(REMOTE_SERVER_FQDN, "admin", "admin", null);
            will(returnValue(externalClient));

            allowing(externalClient).generateAccessToken(LOCAL_SERVER_FQDN);
            will(returnValue(remoteTokenForLocal));

            allowing(externalClient).close();

            allowing(clientFactoryMock).newInternalClient(REMOTE_SERVER_FQDN, remoteTokenForLocal, null);
            will(returnValue(internalClient));

            allowing(internalClient).register(
                with(equal(IssRole.HUB)),
                with(any(String.class)),
                with(aNull(String.class))
            );

            allowing(internalClient).storeCredentials(with(any(String.class)), with(any(String.class)));
        }});

        // Register the remote server as PERIPHERAL for this local server
        hubManager.register(satAdmin, REMOTE_SERVER_FQDN, IssRole.PERIPHERAL, "admin", "admin", null);

        // Verify the remote server is saved as peripheral
        Optional<IssPeripheral> issPeripheral = hubFactory.lookupIssPeripheralByFqdn(REMOTE_SERVER_FQDN);
        assertTrue(issPeripheral.isPresent());

        // Verify we have both tokens for the remote server
        var consumed = hubFactory.lookupAccessTokenByFqdnAndType(REMOTE_SERVER_FQDN, TokenType.CONSUMED);
        assertNotNull(consumed);
        assertEquals(remoteTokenForLocal, consumed.getToken());

        var issued = hubFactory.lookupAccessTokenByFqdnAndType(REMOTE_SERVER_FQDN, TokenType.ISSUED);
        assertNotNull(issued);
    }

    @Test
    public void canRegisterHubWithUserNameAndPassword()
            throws TokenBuildingException, CertificateException, IOException, TokenParsingException,
            TaskomaticApiException {
        IssExternalClient externalClient = mock(IssExternalClient.class);
        IssInternalClient internalClient = mock(IssInternalClient.class);

        // The token generated by REMOTE_SERVER_FQDN for LOCAL_SERVER_FQDN
        String remoteTokenForLocal = """
            eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.ey\
            JqdGkiOiJmdEE2ekpzMWtCZDdlR2JHcWVWS1BRI\
            iwiZXhwIjozMzEzNTY5NjAwLCJpYXQiOjE3MzMz\
            OTM0MjYsIm5iZiI6MTczMzM5MzMwNiwiZnFkbiI\
            6ImxvY2FsLXNlcnZlci51bml0LXRlc3QubG9jYW\
            wifQ.XldOvQJypIkb4E1am0JlBxsHYrx_7J77s1\
            vTrvoNlEU""";

        context().checking(new Expectations() {{
            allowing(clientFactoryMock).newExternalClient(REMOTE_SERVER_FQDN, "admin", "admin", null);
            will(returnValue(externalClient));

            allowing(externalClient).generateAccessToken(LOCAL_SERVER_FQDN);
            will(returnValue(remoteTokenForLocal));

            allowing(externalClient).close();

            allowing(clientFactoryMock).newInternalClient(REMOTE_SERVER_FQDN, remoteTokenForLocal, null);
            will(returnValue(internalClient));

            allowing(internalClient).register(
                with(equal(IssRole.PERIPHERAL)),
                with(any(String.class)),
                with(aNull(String.class))
            );

            allowing(internalClient).generateCredentials();
            will(returnValue(new SCCCredentialsJson("peripheral-000001", "securepassword")));
        }});

        // Register the remote server as HUB for this local server
        hubManager.register(satAdmin, REMOTE_SERVER_FQDN, IssRole.HUB, "admin", "admin", null);

        // Verify the remote server is saved as hub
        Optional<IssHub> issHub = hubFactory.lookupIssHubByFqdn(REMOTE_SERVER_FQDN);
        assertTrue(issHub.isPresent());

        // Verify we have both tokens for the remote server
        var consumed = hubFactory.lookupAccessTokenByFqdnAndType(REMOTE_SERVER_FQDN, TokenType.CONSUMED);
        assertNotNull(consumed);
        assertEquals(remoteTokenForLocal, consumed.getToken());

        var issued = hubFactory.lookupAccessTokenByFqdnAndType(REMOTE_SERVER_FQDN, TokenType.ISSUED);
        assertNotNull(issued);

        // Verify we store the credentials received from the hub
        List<SCCCredentials> sccCredentials = CredentialsFactory.listSCCCredentials().stream()
            .filter(credentials -> "peripheral-000001".equals(credentials.getUsername()))
            .toList();

        assertEquals(1, sccCredentials.size());
        assertEquals("securepassword", sccCredentials.get(0).getPassword());
        assertEquals(
            "https://" + REMOTE_SERVER_FQDN,
            sccCredentials.get(0).getUrl()
        );
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
}
