/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.hub.migration.test;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.iss.IssFactory;
import com.redhat.rhn.domain.iss.IssSlave;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.formula.FormulaMonitoringManager;
import com.redhat.rhn.manager.setup.MirrorCredentialsManager;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.manager.hub.HubClientFactory;
import com.suse.manager.hub.HubInternalClient;
import com.suse.manager.hub.HubManager;
import com.suse.manager.hub.migration.IssMigrator;
import com.suse.manager.model.hub.HubFactory;
import com.suse.manager.model.hub.IssAccessToken;
import com.suse.manager.model.hub.IssPeripheral;
import com.suse.manager.model.hub.migration.MigrationMessage;
import com.suse.manager.model.hub.migration.MigrationResult;
import com.suse.manager.model.hub.migration.MigrationResultCode;
import com.suse.manager.model.hub.migration.SlaveMigrationData;
import com.suse.manager.webui.services.test.TestSaltApi;
import com.suse.manager.webui.utils.token.IssTokenBuilder;
import com.suse.manager.webui.utils.token.Token;
import com.suse.utils.Exceptions;

import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IssMigratorTest extends JMockBaseTestCaseWithUser {

    private static final String LOCAL_SERVER_FQDN = "local-server.unit-test.local";

    public static final String ALPHA = "alpha.unit-test.local";
    public static final String BETA = "beta.unit-test.local";
    public static final String GAMMA = "gamma.unit-test.local";

    private final String originalFqdn;

    private HubFactory hubFactory;

    private HubClientFactory hubClientFactory;

    private TaskomaticApi taskomaticApi;

    private IssMigrator migrator;

    public IssMigratorTest() {
        originalFqdn = ConfigDefaults.get().getHostname();
    }

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        Config.get().setString(ConfigDefaults.SERVER_HOSTNAME, LOCAL_SERVER_FQDN);

        User satAdmin = UserTestUtils.createUser("satUser", user.getOrg().getId());
        satAdmin.addPermanentRole(RoleFactory.SAT_ADMIN);
        UserFactory.save(satAdmin);

        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);

        hubFactory = new HubFactory();

        hubClientFactory = mock(HubClientFactory.class);
        taskomaticApi = mock(TaskomaticApi.class);

        var credentialsManager = mock(MirrorCredentialsManager.class);
        var saltApi = new TestSaltApi();
        var monitoringManager = new FormulaMonitoringManager(saltApi);
        var serverGroupManager = new ServerGroupManager(saltApi);
        var sysEntMgr = new SystemEntitlementManager(
            new SystemUnentitler(monitoringManager, serverGroupManager),
            new SystemEntitler(saltApi, monitoringManager, serverGroupManager)
        );

        var hubManager = new HubManager(hubFactory, hubClientFactory, credentialsManager, taskomaticApi, sysEntMgr);

        migrator = new IssMigrator(hubManager, satAdmin);
    }

    @AfterEach
    @Override
    public void tearDown() throws Exception {
        super.tearDown();

        Config.get().setString(ConfigDefaults.SERVER_HOSTNAME, originalFqdn);
    }

    @Test
    @DisplayName("Migrate successfully existing slaves with correct data")
    public void migrateSuccessfullyExistingSlaves() throws Exception {
        // Setup some slaves and create the migration data
        Map<String, SlaveMigrationData> migrationData = Stream.of(
                createSlave(ALPHA),
                createSlave(BETA),
                createSlave(GAMMA)
            ).collect(Collectors.toMap(IssSlave::getSlave, slave -> createMigrationData(slave.getSlave())));

        // Expect a successful migration for all the slaves and set up the mock accordingly
        checking(new MigrationExpectations() {{
            expectMigrated(migrationData.get(ALPHA));
            expectMigrated(migrationData.get(BETA));
            expectMigrated(migrationData.get(GAMMA));
        }});

        MigrationResult migrationResult = migrator.migrateFromV1(migrationData);

        // Verify everything succeeded
        assertEquals(MigrationResultCode.SUCCESS, migrationResult.getResultCode());
        assertEquals(Set.of(), migrationResult.getMessages());

        // Ensure the slaves has been removed
        List<IssSlave> issSlaves = IssFactory.listAllIssSlaves();
        assertTrue(issSlaves.isEmpty());

        // Ensure the peripheral have been created
        List<IssPeripheral> peripherals = hubFactory.listPeripherals();
        assertEquals(3, peripherals.size());

        // Verify if everything was configured successfully
        for (var data : migrationData.values()) {
            IssPeripheral issPeripheral = hubFactory.lookupIssPeripheralByFqdn(data.fqdn())
                .orElseThrow(() -> new AssertionFailedError("Cannot find ISS Peripheral with fqdn " + data.fqdn()));
            assertEquals(data.rootCA(), issPeripheral.getRootCa());

            IssAccessToken issAccessToken = hubFactory.lookupAccessTokenFor(data.fqdn());
            assertNotNull(issAccessToken);
            assertEquals(data.token(), issAccessToken.getToken());

            Server server = ServerFactory.findByFqdn(data.fqdn())
                .orElseThrow(() -> new AssertionFailedError("Cannot find server with fqdn " + data.fqdn()));

            assertEquals(Set.of(EntitlementManager.FOREIGN), server.getEntitlements());
        }
    }

    @Test
    @DisplayName("Skip slaves if they are disabled or missing migration data")
    public void skipSlavesWhenDisabledOrMissingMigrationData() throws Exception {
        // Setup some slaves and create the migration data
        createSlave(ALPHA);
        createSlave(BETA);
        createSlave(GAMMA, false); // Disabled

        // Omit the data for alpha
        Map<String, SlaveMigrationData> migrationData = Stream.of(
                createMigrationData(BETA),
                createMigrationData(GAMMA)
            )
            .collect(Collectors.toMap(SlaveMigrationData::fqdn, Function.identity()));

        // Expect a successful migration for all the slaves and set up the mock accordingly
        context().checking(new MigrationExpectations() {{
            expectSkipped(migrationData.get(ALPHA));
            expectMigrated(migrationData.get(BETA));
            expectSkipped(migrationData.get(GAMMA));
        }});

        MigrationResult migrationResult = migrator.migrateFromV1(migrationData);

        // Verify everything succeeded
        assertEquals(MigrationResultCode.PARTIAL, migrationResult.getResultCode());
        assertEquals(Set.of(
            MigrationMessage.info("Slave alpha.unit-test.local has no migration data and will not be migrated"),
            MigrationMessage.warn("Slave gamma.unit-test.local is currently disabled and will not be migrated")
        ), migrationResult.getMessages());

        // Ensure only one slave has been removed
        List<IssSlave> issSlaves = IssFactory.listAllIssSlaves();
        assertEquals(2, issSlaves.size());

        // Ensure the peripheral have been created
        List<IssPeripheral> peripherals = hubFactory.listPeripherals();
        assertEquals(1, peripherals.size());

        // Verify if everything was configured successfully for beta.unit-test.local
        SlaveMigrationData data = migrationData.get(BETA);

        IssPeripheral issPeripheral = hubFactory.lookupIssPeripheralByFqdn(data.fqdn())
            .orElseThrow(() -> new AssertionFailedError("Cannot find ISS Peripheral with fqdn " + data.fqdn()));
        assertEquals(data.rootCA(), issPeripheral.getRootCa());

        IssAccessToken issAccessToken = hubFactory.lookupAccessTokenFor(data.fqdn());
        assertNotNull(issAccessToken);
        assertEquals(data.token(), issAccessToken.getToken());

        Server server = ServerFactory.findByFqdn(data.fqdn())
            .orElseThrow(() -> new AssertionFailedError("Cannot find server with fqdn " + data.fqdn()));

        assertEquals(Set.of(EntitlementManager.FOREIGN), server.getEntitlements());
        assertNull(IssFactory.lookupSlaveByName(data.fqdn()));
    }

    @Test
    @DisplayName("Warn when invalid data is present in migration data")
    public void warnsAboutInvalidDataInTheMigrationData() throws Exception {
        // Setup some slaves and create the migration data
        createSlave(ALPHA);

        // Omit the data for alpha
        Map<String, SlaveMigrationData> migrationData = Stream.of(
                createMigrationData(ALPHA),
                createMigrationData(BETA)
            )
            .collect(Collectors.toMap(SlaveMigrationData::fqdn, Function.identity()));

        // Expect a successful migration for all the slaves and set up the mock accordingly
        context().checking(new MigrationExpectations() {{
            expectMigrated(migrationData.get(ALPHA));
        }});

        MigrationResult migrationResult = migrator.migrateFromV1(migrationData);

        // Verify everything succeeded
        assertEquals(MigrationResultCode.SUCCESS, migrationResult.getResultCode());
        assertEquals(Set.of(
            MigrationMessage.warn("Slave beta.unit-test.local does not exist")
        ), migrationResult.getMessages());

        // Ensure only one slave has been removed
        List<IssSlave> issSlaves = IssFactory.listAllIssSlaves();
        assertEquals(0, issSlaves.size());

        // Ensure the peripheral have been created
        List<IssPeripheral> peripherals = hubFactory.listPeripherals();
        assertEquals(1, peripherals.size());

        // Verify if everything was configured successfully for beta.unit-test.local
        SlaveMigrationData data = migrationData.get(ALPHA);

        IssPeripheral issPeripheral = hubFactory.lookupIssPeripheralByFqdn(data.fqdn())
            .orElseThrow(() -> new AssertionFailedError("Cannot find ISS Peripheral with fqdn " + data.fqdn()));
        assertEquals(data.rootCA(), issPeripheral.getRootCa());

        IssAccessToken issAccessToken = hubFactory.lookupAccessTokenFor(data.fqdn());
        assertNotNull(issAccessToken);
        assertEquals(data.token(), issAccessToken.getToken());

        Server server = ServerFactory.findByFqdn(data.fqdn())
            .orElseThrow(() -> new AssertionFailedError("Cannot find server with fqdn " + data.fqdn()));

        assertEquals(Set.of(EntitlementManager.FOREIGN), server.getEntitlements());
        assertNull(IssFactory.lookupSlaveByName(data.fqdn()));
    }

    @Test
    @DisplayName("Fails when there are no slaves to migrate")
    public void failsWhenThereAreNoSlavesToMigrate() {
        MigrationResult migrationResult = migrator.migrateFromV1(Map.of());
        assertEquals(MigrationResultCode.FAILURE, migrationResult.getResultCode());
        assertNotNull(migrationResult.getMessages());
        assertEquals(Set.of(
            MigrationMessage.error("This server does not have any ISSv1 slave")
        ), migrationResult.getMessages());
    }


    @Test
    @DisplayName("Fails when all slave fail to migrate")
    public void failsWhenAllSlavesFailToMigrate() throws Exception {
        // Setup some slaves and create the migration data
        createSlave(ALPHA);
        createSlave(BETA);

        Map<String, SlaveMigrationData> migrationData = Stream.of(
                createMigrationData(ALPHA),
                createMigrationData(BETA)
            )
            .collect(Collectors.toMap(SlaveMigrationData::fqdn, Function.identity()));

        // Expect a successful migration for all the slaves and set up the mock accordingly
        context().checking(new MigrationExpectations() {{
            expectFailureAtRegistration(migrationData.get(ALPHA));
            expectFailureAtRegistration(migrationData.get(BETA));
        }});

        MigrationResult migrationResult = migrator.migrateFromV1(migrationData);
        assertEquals(MigrationResultCode.FAILURE, migrationResult.getResultCode());
        assertNotNull(migrationResult.getMessages());
        assertEquals(Set.of(
            MigrationMessage.error("Unable to migrate alpha.unit-test.local: Remote failure"),
            MigrationMessage.error("Unable to migrate beta.unit-test.local: Remote failure")
        ), migrationResult.getMessages());
    }

    private static IssSlave createSlave(String slaveName) {
        return createSlave(slaveName, true);
    }

    private static IssSlave createSlave(String slaveName, boolean enabled) {
        IssSlave slave = new IssSlave();
        slave.setSlave(slaveName);
        slave.setEnabled(enabled ? "Y" : "N");
        // Migration does not take into account organizations
        slave.setAllowAllOrgs("Y");

        IssFactory.save(slave);
        return slave;
    }

    private static SlaveMigrationData createMigrationData(String slaveFqdn) {
        Token accessToken = Exceptions.handleByWrapping(
            () -> new IssTokenBuilder(LOCAL_SERVER_FQDN).usingServerSecret().build(),
            ex -> new IllegalStateException("Unable to create a fake token issued by " + slaveFqdn, ex)
        );

        return new SlaveMigrationData(slaveFqdn, accessToken.getSerializedForm(), TestUtils.randomString(16));
    }

    private class MigrationExpectations extends Expectations {

        protected void expectMigrated(SlaveMigrationData data)
            throws TaskomaticApiException, CertificateException, IOException {

            HubInternalClient internalClientMock = mock(HubInternalClient.class, "internalClient_" + data.fqdn());

            allowRegistration(data, internalClientMock);
            allowIssV1Removal(internalClientMock);
        }

        protected void expectSkipped(SlaveMigrationData data) {
            // Nothing to do
        }

        protected void expectFailureAtRegistration(SlaveMigrationData data)
            throws TaskomaticApiException, CertificateException, IOException {

            HubInternalClient internalClientMock = mock(HubInternalClient.class, "internalClient_" + data.fqdn());

            failingRegistration(data, internalClientMock);

            allowingRollback(internalClientMock);
        }

        private void allowRegistration(SlaveMigrationData data, HubInternalClient internalClientMock)
            throws TaskomaticApiException, CertificateException, IOException {
            allowing(taskomaticApi)
                .scheduleSingleRootCaCertUpdate("peripheral_" + data.fqdn() + "_root_ca.pem", data.rootCA());

            allowing(hubClientFactory).newInternalClient(data.fqdn(), data.token(), data.rootCA());
            will(returnValue(internalClientMock));

            allowing(internalClientMock).registerHub(
                with(any(String.class)),
                with(anyOf(nullValue(String.class), any(String.class))),
                with(anyOf(nullValue(String.class), any(String.class)))
            );

            allowing(internalClientMock)
                .storeCredentials(with(any(String.class)), with(any(String.class)));

            allowing(internalClientMock).getManagerInfo();
            will(returnValue(null));

            allowing(internalClientMock).scheduleProductRefresh();
        }

        private void failingRegistration(SlaveMigrationData data, HubInternalClient internalClientMock)
            throws TaskomaticApiException, CertificateException, IOException {
            allowing(taskomaticApi)
                .scheduleSingleRootCaCertUpdate("peripheral_" + data.fqdn() + "_root_ca.pem", data.rootCA());

            allowing(hubClientFactory).newInternalClient(data.fqdn(), data.token(), data.rootCA());
            will(returnValue(internalClientMock));

            allowing(internalClientMock).registerHub(
                with(any(String.class)),
                with(anyOf(nullValue(String.class), any(String.class))),
                with(anyOf(nullValue(String.class), any(String.class)))
            );
            will(throwException(new IOException("Remote failure")));
        }

        private void allowIssV1Removal(HubInternalClient internalClientMock) throws IOException {
            allowing(internalClientMock).deleteIssV1Master();
        }

        private void allowingRollback(HubInternalClient internalClientMock) throws IOException {
            allowing(internalClientMock).deregister();
        }
    }
}
