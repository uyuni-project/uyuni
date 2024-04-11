/*
 * Copyright (c) 2024 SUSE LLC
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
package com.suse.manager.attestation.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.CoCoAttestationAction;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Pillar;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.manager.attestation.AttestationManager;
import com.suse.manager.model.attestation.AttestationFactory;
import com.suse.manager.model.attestation.CoCoAttestationResult;
import com.suse.manager.model.attestation.CoCoAttestationStatus;
import com.suse.manager.model.attestation.CoCoEnvironmentType;
import com.suse.manager.model.attestation.ServerCoCoAttestationConfig;
import com.suse.manager.model.attestation.ServerCoCoAttestationReport;
import com.suse.manager.webui.services.pillar.MinionGeneralPillarGenerator;

import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class AttestationManagerTest extends JMockBaseTestCaseWithUser {

    private User user2;
    private Server server;
    private AttestationManager mgr;
    private static TaskomaticApi taskomaticApi;
    private final SystemManager systemManager = new SystemManager(ServerFactory.SINGLETON,
            ServerGroupFactory.SINGLETON, null);

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        context.setThreadingPolicy(new Synchroniser());
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        user2 = UserTestUtils.createUser("user2", user.getOrg().getId());
        server = ServerFactoryTest.createTestServer(user, true);
        mgr = new AttestationManager(new AttestationFactory(), getTaskomaticApi());
    }

    @Test
    public void testCreateAttestationConfiguration() {
        assertThrows(PermissionException.class,
                () -> mgr.createConfig(user2, server, CoCoEnvironmentType.KVM_AMD_EPYC_GENOA, true));

        ServerCoCoAttestationConfig cnf = mgr.createConfig(user, server, CoCoEnvironmentType.KVM_AMD_EPYC_GENOA, true);
        assertNotNull(cnf);
    }

    @Test
    public void testInitializeAttestationReport() {
        assertThrows(PermissionException.class, () -> mgr.initializeReport(user2, server));
        assertThrows(LookupException.class, () -> mgr.initializeReport(user, server));

        mgr.createConfig(user, server, CoCoEnvironmentType.KVM_AMD_EPYC_GENOA, true);
        ServerCoCoAttestationReport report = mgr.initializeReport(user, server);
        assertNotNull(report);
    }

    @Test
    public void testInitializeAttestationResults() {
        mgr.createConfig(user, server, CoCoEnvironmentType.KVM_AMD_EPYC_GENOA, true);
        ServerCoCoAttestationReport report = mgr.initializeReport(user, server);
        assertThrows(PermissionException.class, () -> mgr.initializeResults(user2, report));

        ServerCoCoAttestationReport brokenReport = new ServerCoCoAttestationReport();
        assertThrows(LookupException.class, () -> mgr.initializeResults(user, brokenReport));

        mgr.initializeResults(user, report);
        List<CoCoAttestationResult> results = report.getResults();
        assertTrue(results.size() > 0);
    }

    @Test
    public void testCreateAttestationAction() throws TaskomaticApiException {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        mgr.createConfig(user, minion, CoCoEnvironmentType.KVM_AMD_EPYC_GENOA, true);
        Date now = new Date();
        CoCoAttestationAction action = mgr.scheduleAttestationAction(user, minion, now);
        assertNotNull(action);

        AttestationFactory f = new AttestationFactory();
        Optional<ServerCoCoAttestationReport> latestReport = f.lookupLatestReportByServer(minion);
        Map<String, Object> inData = latestReport.orElse(new ServerCoCoAttestationReport()).getInData();
        assertNotNull(inData);
        String nonceReport = (String) inData.getOrDefault("nonce", "not in report");
        Pillar pillar = minion.getPillarByCategory(MinionGeneralPillarGenerator.CATEGORY).orElse(new Pillar());
        Map<String, Object> attestationData = (Map<String, Object>) pillar.getPillar()
                .getOrDefault("attestation_data", new HashMap<>());
        String noncePillar = (String) attestationData.getOrDefault("nonce", "not in pillar");
        assertEquals(nonceReport, noncePillar);
        assertEquals("KVM_AMD_EPYC_GENOA",
                attestationData.getOrDefault("environment_type", "environment_type not found"));
    }

    @Test
    public void testListReports() {
        mgr.createConfig(user, server, CoCoEnvironmentType.KVM_AMD_EPYC_GENOA, true);

        ServerCoCoAttestationReport report = mgr.initializeReport(user, server);
        mgr.initializeResults(user, report);
        fakeSuccessfullAttestation(report);

        ServerCoCoAttestationReport report2 = mgr.initializeReport(user, server);
        mgr.initializeResults(user, report2);
        fakeSuccessfullAttestation(report2);
        HibernateFactory.getSession().flush();
        HibernateFactory.commitTransaction();
        HibernateFactory.getSession().clear();

        try {
            List<ServerCoCoAttestationReport> reports = mgr.listCoCoAttestationReports(user, server, new Date(0), 0,
                    Integer.MAX_VALUE);
            assertEquals(2, reports.size());

            ServerCoCoAttestationReport latestReport = mgr.lookupLatestCoCoAttestationReport(user, server);
            assertEquals(CoCoAttestationStatus.SUCCEEDED, latestReport.getStatus());
            assertEquals("Some details", latestReport.getResults().get(0).getDetailsOpt().orElse(""));
        }
        finally {
            // cleanup
            systemManager.deleteServer(user, server.getId());
            HibernateFactory.commitTransaction();
        }
    }

    @Test
    public void testFilterListReports() throws InterruptedException {
        mgr.createConfig(user, server, CoCoEnvironmentType.KVM_AMD_EPYC_GENOA, true);

        long epochStart = (new Date().getTime() / 1000);
        for (int i = 10; i > 0; i--) {
            ServerCoCoAttestationReport report = mgr.initializeReport(user, server);
            mgr.initializeResults(user, report);
            fakeSuccessfullAttestation(report);
            HibernateFactory.getSession().flush();
            HibernateFactory.commitTransaction();
            TimeUnit.SECONDS.sleep(2);
        }
        HibernateFactory.getSession().clear();
        try {
            List<ServerCoCoAttestationReport> reports = mgr.listCoCoAttestationReports(user, server, new Date(0), 0,
                    Integer.MAX_VALUE);
            assertEquals(10, reports.size());

            List<ServerCoCoAttestationReport> reports2 = mgr.listCoCoAttestationReports(user, server,
                    new Date((epochStart + 10) * 1000L), 0, Integer.MAX_VALUE);
            assertTrue(reports2.get(0).getModified().compareTo(new Date((epochStart + 10) * 1000L)) >= 0);
            assertEquals(5, reports2.size());

            reports2 = mgr.listCoCoAttestationReports(user, server, new Date(0), 5, 2);
            assertEquals(2, reports2.size());
            assertEquals(reports.get(6), reports2.get(0));
            assertEquals(reports.get(7), reports2.get(1));
            assertTrue(reports2.get(0).getCreated().after(reports2.get(1).getCreated()),
                    "Report 0 is not created after Report 1");
        }
        finally {
            // cleanup
            systemManager.deleteServer(user, server.getId());
            HibernateFactory.commitTransaction();
        }
    }

    private void fakeSuccessfullAttestation(ServerCoCoAttestationReport reportIn) {
        reportIn.getResults().forEach(res -> {
            res.setStatus(CoCoAttestationStatus.SUCCEEDED);
            res.setDetails("Some details");
            res.setAttested(new Date());
        });
    }

    private TaskomaticApi getTaskomaticApi() throws TaskomaticApiException {
        if (taskomaticApi == null) {
            taskomaticApi = context.mock(TaskomaticApi.class);
            context.checking(new Expectations() {
                {
                    allowing(taskomaticApi).scheduleActionExecution(with(any(Action.class)));
                }
            });
        }

        return taskomaticApi;
    }
}
