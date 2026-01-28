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

package com.suse.manager.xmlrpc.iss.test;

import static org.jmock.AbstractExpectations.returnValue;
import static org.jmock.AbstractExpectations.throwException;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.FaultException;
import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.frontend.xmlrpc.InvalidParameterException;
import com.redhat.rhn.frontend.xmlrpc.InvalidTokenException;
import com.redhat.rhn.frontend.xmlrpc.PermissionCheckFailureException;
import com.redhat.rhn.frontend.xmlrpc.TokenCreationException;
import com.redhat.rhn.frontend.xmlrpc.TokenExchangeFailedException;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;
import com.redhat.rhn.manager.setup.MirrorCredentialsManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.hub.DefaultHubInternalClient;
import com.suse.manager.hub.HubClientFactory;
import com.suse.manager.hub.HubManager;
import com.suse.manager.hub.migration.IssMigrator;
import com.suse.manager.hub.migration.IssMigratorFactory;
import com.suse.manager.model.hub.HubFactory;
import com.suse.manager.model.hub.IssHub;
import com.suse.manager.model.hub.IssPeripheral;
import com.suse.manager.model.hub.ServerInfoJson;
import com.suse.manager.model.hub.migration.MigrationResult;
import com.suse.manager.model.hub.migration.MigrationResultCode;
import com.suse.manager.model.hub.migration.SlaveMigrationData;
import com.suse.manager.webui.utils.token.TokenBuildingException;
import com.suse.manager.webui.utils.token.TokenException;
import com.suse.manager.webui.utils.token.TokenParsingException;
import com.suse.manager.xmlrpc.InvalidCertificateException;
import com.suse.manager.xmlrpc.iss.HubHandler;

import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.jmock.junit5.JUnit5Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.SSLHandshakeException;

@ExtendWith(JUnit5Mockery.class)
public class HubHandlerTest extends BaseHandlerTestCase {
    private static final String LOCAL_SERVER_FQDN = "uyuni-server.dev.local";
    private static final String DUMMY_TOKEN = "token";
    private static final String DUMMY_ROOT_CA = "dummy";
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_DUMMY_PASS = "admin";

    static class HubHandlerMock extends HubHandler {
        @Override
        public String logAndGetErrorMessage(Throwable ex, String message, Object... args) {
            return super.logAndGetErrorMessage(ex, message, args);
        }

        @Override
        public String logAndGetErrorMessage(String message, Object... args) {
            return super.logAndGetErrorMessage(message, args);
        }
    }

    @RegisterExtension
    protected final JUnit5Mockery context = new JUnit5Mockery() {{
        setThreadingPolicy(new Synchroniser());
    }};

    private IssMigratorFactory migratorFactoryMock;

    private HubManager hubManagerMock;

    private HubHandler hubHandler;

    @BeforeEach
    public void setupTest() {
        context.setThreadingPolicy(new Synchroniser());
        context.setImposteriser((ByteBuddyClassImposteriser.INSTANCE));

        hubManagerMock = context.mock(HubManager.class);
        migratorFactoryMock = context.mock(IssMigratorFactory.class);
        hubHandler = new HubHandler(hubManagerMock, migratorFactoryMock, null);
    }

    @Test
    public void ensureOnlySatAdminCanAccessToTokenGeneration() throws TokenBuildingException, TokenParsingException {
        Expectations expectations = new Expectations();
        expectations.allowing(hubManagerMock).issueAccessToken(satAdmin, LOCAL_SERVER_FQDN);
        expectations.will(returnValue("dummy-token"));
        context.checking(expectations);

        assertThrows(
            PermissionCheckFailureException.class,
            () -> hubHandler.generateAccessToken(regular, LOCAL_SERVER_FQDN)
        );

        assertThrows(
            PermissionCheckFailureException.class,
            () -> hubHandler.generateAccessToken(admin, LOCAL_SERVER_FQDN)
        );

        assertDoesNotThrow(
            () -> hubHandler.generateAccessToken(satAdmin, LOCAL_SERVER_FQDN)
        );
    }

    @Test
    public void throwsCorrectExceptionWhenIssuingFails() throws TokenException {
        Expectations expectations = new Expectations();
        expectations.allowing(hubManagerMock).issueAccessToken(satAdmin, LOCAL_SERVER_FQDN);
        expectations.will(throwException(new TokenBuildingException("unexpected error")));
        context.checking(expectations);

        assertThrows(TokenCreationException.class,
            () -> hubHandler.generateAccessToken(satAdmin, LOCAL_SERVER_FQDN));
    }

    @Test
    public void ensureOnlySatAdminCanAccessToTokenStorage() throws TokenParsingException {
        Expectations expectations = new Expectations();
        expectations.allowing(hubManagerMock).storeAccessToken(satAdmin, LOCAL_SERVER_FQDN, "dummy-token");
        context.checking(expectations);

        assertThrows(
            PermissionCheckFailureException.class,
            () -> hubHandler.storeAccessToken(regular, LOCAL_SERVER_FQDN, "dummy-token")
        );

        assertThrows(
            PermissionCheckFailureException.class,
            () -> hubHandler.storeAccessToken(admin, LOCAL_SERVER_FQDN, "dummy-token")
        );

        assertDoesNotThrow(
            () -> hubHandler.storeAccessToken(satAdmin, LOCAL_SERVER_FQDN, "dummy-token")
        );
    }

    @Test
    public void throwsCorrectExceptionWhenStoringFails() throws TokenParsingException {
        Expectations expectations = new Expectations();
        expectations.allowing(hubManagerMock).storeAccessToken(satAdmin, LOCAL_SERVER_FQDN, "dummy-token");
        expectations.will(throwException(new TokenParsingException("Cannot parse")));
        context.checking(expectations);

        assertThrows(InvalidTokenException.class,
            () -> hubHandler.storeAccessToken(satAdmin, LOCAL_SERVER_FQDN, "dummy-token"));
    }

    @Test
    public void ensureOnlySatAdminCanRegister() throws TokenBuildingException, TaskomaticApiException,
            CertificateException, IOException, TokenParsingException {
        Expectations expectations = new Expectations();
        expectations.allowing(hubManagerMock)
                .register(satAdmin, "remote-server.dev.local", ADMIN_USERNAME, ADMIN_DUMMY_PASS, null);
        context.checking(expectations);

        assertThrows(
            PermissionCheckFailureException.class,
            () -> hubHandler.registerPeripheral(regular, "remote-server.dev.local", ADMIN_USERNAME, ADMIN_DUMMY_PASS)
        );

        assertThrows(
            PermissionCheckFailureException.class,
            () -> hubHandler.registerPeripheral(admin, "remote-server.dev.local", ADMIN_USERNAME, ADMIN_DUMMY_PASS)
        );

        assertDoesNotThrow(
            () -> hubHandler.registerPeripheral(satAdmin, "remote-server.dev.local", ADMIN_USERNAME, ADMIN_DUMMY_PASS)
        );
    }

    @Test
    public void throwsCorrectExceptionsWhenRegisteringFails() throws TokenBuildingException, TaskomaticApiException,
            CertificateException, IOException, TokenParsingException {
        Expectations expectations = new Expectations();
        expectations.allowing(hubManagerMock)
            .register(satAdmin, "fails-certificate.dev.local", ADMIN_USERNAME, ADMIN_DUMMY_PASS, DUMMY_ROOT_CA);
        expectations.will(throwException(new CertificateException("Unable to parse")));

        expectations.allowing(hubManagerMock)
            .register(satAdmin, "fails-parsing.dev.local", ADMIN_USERNAME, ADMIN_DUMMY_PASS, DUMMY_ROOT_CA);
        expectations.will(throwException(new TokenParsingException("Unable to parse")));

        expectations.allowing(hubManagerMock)
            .register(satAdmin, "fails-building.dev.local", ADMIN_USERNAME, ADMIN_DUMMY_PASS, DUMMY_ROOT_CA);
        expectations.will(throwException(new TokenBuildingException("Unable to build")));

        expectations.allowing(hubManagerMock)
            .register(satAdmin, "fails-connecting.dev.local", ADMIN_USERNAME, ADMIN_DUMMY_PASS, DUMMY_ROOT_CA);
        expectations.will(throwException(new IOException("Unable to connect")));

        context.checking(expectations);

        assertThrows(
            InvalidCertificateException.class,
            () -> hubHandler.registerPeripheral(satAdmin, "fails-certificate.dev.local",
                    ADMIN_USERNAME, ADMIN_DUMMY_PASS, DUMMY_ROOT_CA)
        );

        assertThrows(
            TokenExchangeFailedException.class,
            () -> hubHandler.registerPeripheral(satAdmin, "fails-parsing.dev.local",
                    ADMIN_USERNAME, ADMIN_DUMMY_PASS, DUMMY_ROOT_CA)
        );

        assertThrows(
            TokenExchangeFailedException.class,
            () -> hubHandler.registerPeripheral(satAdmin, "fails-building.dev.local",
                    ADMIN_USERNAME, ADMIN_DUMMY_PASS, DUMMY_ROOT_CA)
        );

        assertThrows(
            TokenExchangeFailedException.class,
            () -> hubHandler.registerPeripheral(satAdmin, "fails-connecting.dev.local",
                    ADMIN_USERNAME, ADMIN_DUMMY_PASS, DUMMY_ROOT_CA)
        );
    }

    @Test
    public void ensureOnlySatAdminCanRegisterWithToken() throws TokenBuildingException, TaskomaticApiException,
            CertificateException, IOException, TokenParsingException {
        Expectations expectations = new Expectations();
        expectations.allowing(hubManagerMock)
            .register(satAdmin, "remote-server.dev.local", DUMMY_TOKEN, null);
        context.checking(expectations);

        assertThrows(
            PermissionCheckFailureException.class,
            () -> hubHandler.registerPeripheralWithToken(regular, "remote-server.dev.local", DUMMY_TOKEN)
        );

        assertThrows(
            PermissionCheckFailureException.class,
            () -> hubHandler.registerPeripheralWithToken(admin, "remote-server.dev.local", DUMMY_TOKEN)
        );

        assertDoesNotThrow(
            () -> hubHandler.registerPeripheralWithToken(satAdmin, "remote-server.dev.local", DUMMY_TOKEN)
        );
    }

    @Test
    public void throwsCorrectExceptionsWhenRegisteringWithTokenFails() throws TokenBuildingException,
            TaskomaticApiException, CertificateException, IOException, TokenParsingException {
        Expectations expectations = new Expectations();
        expectations.allowing(hubManagerMock)
            .register(satAdmin, "fails-certificate.dev.local", DUMMY_TOKEN, DUMMY_ROOT_CA);
        expectations.will(throwException(new CertificateException("Unable to parse")));

        expectations.allowing(hubManagerMock)
            .register(satAdmin, "fails-parsing.dev.local", DUMMY_TOKEN, DUMMY_ROOT_CA);
        expectations.will(throwException(new TokenParsingException("Unable to parse")));

        expectations.allowing(hubManagerMock)
            .register(satAdmin, "fails-building.dev.local", DUMMY_TOKEN, DUMMY_ROOT_CA);
        expectations.will(throwException(new TokenBuildingException("Unable to build")));

        expectations.allowing(hubManagerMock)
            .register(satAdmin, "fails-connecting.dev.local", DUMMY_TOKEN, DUMMY_ROOT_CA);
        expectations.will(throwException(new IOException("Unable to connect")));

        context.checking(expectations);

        assertThrows(
            InvalidCertificateException.class,
            () -> hubHandler.registerPeripheralWithToken(satAdmin, "fails-certificate.dev.local",
                    DUMMY_TOKEN, DUMMY_ROOT_CA)
        );

        assertThrows(
            TokenExchangeFailedException.class,
            () -> hubHandler.registerPeripheralWithToken(satAdmin, "fails-parsing.dev.local",
                    DUMMY_TOKEN, DUMMY_ROOT_CA)
        );

        assertThrows(
            TokenExchangeFailedException.class,
            () -> hubHandler.registerPeripheralWithToken(satAdmin, "fails-building.dev.local",
                    DUMMY_TOKEN, DUMMY_ROOT_CA)
        );

        assertThrows(
            TokenExchangeFailedException.class,
            () -> hubHandler.registerPeripheralWithToken(satAdmin, "fails-connecting.dev.local",
                    DUMMY_TOKEN, DUMMY_ROOT_CA)
        );
    }

    @Test
    public void throwsCorrectExceptionWhenReplaceTokensFailsCausedByIllegalStateException() {
        HubHandler newHubHandler = new HubHandler();
        assertThrows(FaultException.class,
                () -> newHubHandler.replaceTokens(satAdmin, LOCAL_SERVER_FQDN));
    }

    @Test
    public void throwsCorrectExceptionWhenRegisterPeripheralWithTokenFailsCausedByIllegalStateException() {
        String dummyFqdn = "dummy-server.dev.local";
        HubHandler newHubHandler = new HubHandler();

        HubFactory newHubFactory = new HubFactory();
        IssPeripheral peripheral = new IssPeripheral(dummyFqdn, "");
        newHubFactory.save(peripheral);

        assertThrows(FaultException.class,
                () -> newHubHandler.registerPeripheralWithToken(satAdmin, dummyFqdn, DUMMY_TOKEN, null));
    }

    @Test
    public void yetThrowsCorrectExceptionWhenRegisterPeripheralWithTokenFailsCausedByIllegalStateException() {
        String dummyFqdn = "dummy-server.dev.local";
        HubHandler newHubHandler = new HubHandler();

        HubFactory newHubFactory = new HubFactory();
        IssHub hub = new IssHub(dummyFqdn, "");
        newHubFactory.save(hub);

        assertThrows(FaultException.class,
                () -> newHubHandler.registerPeripheralWithToken(satAdmin, dummyFqdn, DUMMY_TOKEN, null));
    }

    @Test
    public void throwsCorrectExceptionWhenSetDetailsFailsCausedByIllegalStateException() {
        HubHandler newHubHandler = new HubHandler();

        //server not found
        assertThrows(FaultException.class,
                () -> newHubHandler.setDetails(satAdmin, "dummy-server.dev.local", "HUB", null));

        Map<String, String> dataMap = new HashMap<>();
        assertThrows(FaultException.class,
                () -> newHubHandler.setDetails(satAdmin, "dummy-server.dev.local", "HUB", dataMap));
    }

    @Test
    public void registerPeripheralWithTokenCorrectlyThrows()
            throws TaskomaticApiException, IOException, CertificateException {
        TaskomaticApi mockTaskomaticApi = context.mock(TaskomaticApi.class);
        HubClientFactory mockHubClientFactory = context.mock(HubClientFactory.class);
        DefaultHubInternalClient mockDefaultHubInternalClient = context.mock(DefaultHubInternalClient.class);
        HubFactory hubFactory = new HubFactory();
        HubManager newHubManager = new HubManager(hubFactory, mockHubClientFactory,
                new MirrorCredentialsManager(), mockTaskomaticApi, GlobalInstanceHolder.SYSTEM_ENTITLEMENT_MANAGER);
        HubHandler newHubHandler = new HubHandler(newHubManager);

        String sshFailureErrorString = "unable to find valid certification path to target";

        context.checking(new Expectations() {{
            allowing(mockTaskomaticApi).scheduleSingleRootCaCertUpdate(
                    with(any(String.class)), with(any(String.class)));

            allowing(mockHubClientFactory).newInternalClient(
                    with(any(String.class)), with(any(String.class)), with(any(String.class)));
            will(returnValue(mockDefaultHubInternalClient));

            allowing(mockDefaultHubInternalClient).registerHub(
                    with(any(String.class)), with(aNull(String.class)), with(aNull(String.class)));
            will(throwException(new SSLHandshakeException(sshFailureErrorString)));

            allowing(mockDefaultHubInternalClient).deregister();
            allowing(mockDefaultHubInternalClient).getServerInfo();
            will(returnValue(new ServerInfoJson()));
        }});

        String dummyToken = newHubHandler.generateAccessToken(satAdmin, ConfigDefaults.get().getHostname());
        String dummyFqdn = "dummy-server.dev.local";

        Throwable exFirstCall = assertThrows(TokenExchangeFailedException.class,
                () -> newHubHandler.registerPeripheralWithToken(satAdmin, dummyFqdn,
                        dummyToken, ""));
        assertTrue(exFirstCall.getMessage().endsWith(sshFailureErrorString));
    }

    @Test
    public void canMigrateIssV1Servers() {
        context.checking(new Expectations() {{
            IssMigrator migrator = context.mock(IssMigrator.class);

            allowing(migratorFactoryMock).createFor(satAdmin);
            will(returnValue(migrator));

            allowing(migrator).migrateFromV1(Map.of(
                "first-slave.dev.local", new SlaveMigrationData("first-slave.dev.local", "one", "dummy"),
                "second-slave.dev.local", new SlaveMigrationData("second-slave.dev.local", "two", null)
            ));
            will(returnValue(new MigrationResult()));
        }});

        MigrationResult migrationResult = hubHandler.migrateFromISSv1(satAdmin, List.of(
            Map.of("fqdn", "first-slave.dev.local", "token", "one", "root_ca", "dummy"),
            mapWithNull("fqdn", "second-slave.dev.local", "token", "two", "root_ca", null)
        ));
        assertEquals(MigrationResultCode.SUCCESS, migrationResult.getResultCode());
        assertEquals(Set.of(), migrationResult.getMessages());
    }

    @Test
    public void migrateIssV1CorrectlyThrows() {
        var illegalParameter = assertThrows(InvalidParameterException.class,
            () -> hubHandler.migrateFromISSv1(satAdmin, List.of()));
        assertEquals("migration data must not be empty", illegalParameter.getMessage());

        illegalParameter = assertThrows(InvalidParameterException.class,
            () -> hubHandler.migrateFromISSv1(satAdmin, null));
        assertEquals("migration data must not be empty", illegalParameter.getMessage());

        illegalParameter = assertThrows(InvalidParameterException.class,
            () -> hubHandler.migrateFromISSv1(satAdmin, List.of(Map.of("fqdn", "slave.dev.local"))));
        assertEquals("Migration data is not valid: Missing access token for slave.dev.local",
            illegalParameter.getMessage());
    }

    @Test
    public void canMigrateIssV2Servers() {
        context.checking(new Expectations() {{
            IssMigrator migrator = context.mock(IssMigrator.class);

            allowing(migratorFactoryMock).createFor(satAdmin);
            will(returnValue(migrator));

            allowing(migrator).migrateFromV2(List.of(
                new SlaveMigrationData("first-slave.dev.local", "one", "dummy"),
                new SlaveMigrationData("second-slave.dev.local", "two", null)
            ));
            will(returnValue(new MigrationResult()));
        }});

        MigrationResult migrationResult = hubHandler.migrateFromISSv2(satAdmin, List.of(
            Map.of("fqdn", "first-slave.dev.local", "token", "one", "root_ca", "dummy"),
            mapWithNull("fqdn", "second-slave.dev.local", "token", "two", "root_ca", null)
        ));
        assertEquals(MigrationResultCode.SUCCESS, migrationResult.getResultCode());
        assertEquals(Set.of(), migrationResult.getMessages());
    }

    @Test
    public void migrateIssV2CorrectlyThrows() {
        var illegalParameter = assertThrows(InvalidParameterException.class,
            () -> hubHandler.migrateFromISSv2(satAdmin, List.of()));
        assertEquals("migration data must not be empty", illegalParameter.getMessage());

        illegalParameter = assertThrows(InvalidParameterException.class,
            () -> hubHandler.migrateFromISSv2(satAdmin, null));
        assertEquals("migration data must not be empty", illegalParameter.getMessage());

        illegalParameter = assertThrows(InvalidParameterException.class,
            () -> hubHandler.migrateFromISSv2(satAdmin, List.of(Map.of("fqdn", "slave.dev.local"))));
        assertEquals("Migration data is not valid: Missing access token for slave.dev.local",
            illegalParameter.getMessage());
    }

    @Test
    public void ensureDeregisterPeripheralWithNullMirrorCredentialsDoesNotThrow() {
        String dummyFqdn = "dummy-server.dev.local";
        HubHandler newHubHandler = new HubHandler();

        HubFactory newHubFactory = new HubFactory();
        IssPeripheral peripheral = new IssPeripheral(dummyFqdn, "");
        newHubFactory.save(peripheral);

        assertNull(peripheral.getMirrorCredentials());
        assertDoesNotThrow(() -> newHubHandler.deregister(satAdmin, dummyFqdn, true));
    }

    @Test
    public void testFormattingErrorMessages() {
        HubHandlerMock hubHandlerMock = new HubHandlerMock();
        TokenCreationException ex = new TokenCreationException("ex_error_message");

        assertEquals("main_error_message: ex_error_message",
                hubHandlerMock.logAndGetErrorMessage(ex, "main_error_message"));
        assertEquals("main_error_message argument1: ex_error_message",
                hubHandlerMock.logAndGetErrorMessage(ex, "main_error_message {}", "argument1"));
        assertEquals("main_error_message argument1 argument2: ex_error_message",
                hubHandlerMock.logAndGetErrorMessage(ex, "main_error_message {} {}", "argument1", "argument2"));

        TokenCreationException ex2 = new TokenCreationException("");
        assertEquals("main_error_message",
                hubHandlerMock.logAndGetErrorMessage(ex2, "main_error_message"));
        assertEquals("main_error_message argument1",
                hubHandlerMock.logAndGetErrorMessage(ex2, "main_error_message {}", "argument1"));

        assertEquals("",
                hubHandlerMock.logAndGetErrorMessage(""));
        assertEquals("secondary_msg",
                hubHandlerMock.logAndGetErrorMessage("secondary_msg"));
        assertEquals("secondary_msg argument1",
                hubHandlerMock.logAndGetErrorMessage("secondary_msg {}", "argument1"));
        assertEquals("secondary_msg argument1 argument2",
                hubHandlerMock.logAndGetErrorMessage("secondary_msg {} {}", "argument1", "argument2"));
        assertEquals("secondary_msg argument1 argument2 argument3",
                hubHandlerMock.logAndGetErrorMessage("secondary_msg {} {} {}",
                        "argument1", "argument2", "argument3"));
    }

    private static Map<String, String> mapWithNull(String... keyValuePairs) {
        if (keyValuePairs == null || (keyValuePairs.length % 2) != 0) {
            throw new IllegalArgumentException();
        }

        Map<String, String> resultMap = new HashMap<>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            resultMap.put(keyValuePairs[i], keyValuePairs[i + 1]);
        }
        return resultMap;
    }
}
