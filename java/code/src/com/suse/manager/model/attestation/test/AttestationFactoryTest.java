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
package com.suse.manager.model.attestation.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.test.ActionFactoryTest;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.model.attestation.AttestationFactory;
import com.suse.manager.model.attestation.CoCoAttestationResult;
import com.suse.manager.model.attestation.CoCoAttestationStatus;
import com.suse.manager.model.attestation.CoCoEnvironmentType;
import com.suse.manager.model.attestation.CoCoResultType;
import com.suse.manager.model.attestation.ServerCoCoAttestationConfig;
import com.suse.manager.model.attestation.ServerCoCoAttestationReport;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class AttestationFactoryTest extends BaseTestCaseWithUser {

    private Server server;
    private AttestationFactory attestationFactory;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        server = ServerFactoryTest.createTestServer(user);
        attestationFactory = new AttestationFactory();
    }

    @Test
    public void testCreateAttestationConfiguration() {
        ServerCoCoAttestationConfig cnf = attestationFactory.createConfigForServer(server,
                CoCoEnvironmentType.KVM_AMD_EPYC_MILAN, true);
        HibernateFactory.getSession().flush();
        assertEquals(server, cnf.getServer());
        assertEquals(CoCoEnvironmentType.KVM_AMD_EPYC_MILAN, cnf.getEnvironmentType());
        assertTrue(cnf.isEnabled(), "Config is not enabled");
    }

    @Test
    public void testAttestationConfigurationLookup() {
        ServerCoCoAttestationConfig cnf = attestationFactory.createConfigForServer(server,
                CoCoEnvironmentType.KVM_AMD_EPYC_MILAN, true);
        HibernateFactory.getSession().flush();
        Server srv = ServerFactory.lookupByIdAndOrg(server.getId(), user.getOrg());
        Optional<ServerCoCoAttestationConfig> cocoAttCnf = srv.getOptCocoAttestationConfig();
        assertNotNull(cocoAttCnf);
        if (cocoAttCnf.isPresent()) {
            assertEquals(cnf.getId(), cocoAttCnf.get().getId());
            assertEquals(cnf.getEnvironmentType(), cocoAttCnf.get().getEnvironmentType());
            assertEquals(cnf.isEnabled(), cocoAttCnf.get().isEnabled());
            assertEquals(CoCoEnvironmentType.KVM_AMD_EPYC_MILAN, cocoAttCnf.get().getEnvironmentType());
            assertTrue(cocoAttCnf.get().isEnabled());
        }
        else {
            fail("config not initialzed");
        }

        Optional<ServerCoCoAttestationConfig> optConfig = attestationFactory.lookupConfigByServerId(srv.getId());
        if (optConfig.isPresent()) {
            assertEquals(cnf, optConfig.get());
        }
        else {
            fail("Configuration not initialized");
        }
    }

    @Test
    public void testCreateAttestationReport() throws Exception {
        ServerCoCoAttestationConfig cnf = attestationFactory.createConfigForServer(server,
                CoCoEnvironmentType.KVM_AMD_EPYC_MILAN, true);
        ServerCoCoAttestationReport report = attestationFactory.createReportForServer(server);
        HibernateFactory.getSession().flush();
        assertNotNull(report);
        assertEquals(CoCoAttestationStatus.PENDING, report.getStatus());
        assertEquals(cnf.getEnvironmentType(), report.getEnvironmentType());
        assertNull(report.getAction());

        Action action = ActionFactoryTest.createAction(user, ActionFactory.TYPE_COCO_ATTESTATION);
        report.setAction(action);
        action.addCocoAttestationReport(report);

        Long actionId = action.getId();
        TestUtils.flushAndEvict(action);

        Action a1 = ActionFactory.lookupByUserAndId(user, actionId);
        assertNotNull(a1);
        Set<ServerCoCoAttestationReport> reports = a1.getCocoAttestationReports();
        assertNotNull(reports);
        assertNotEmpty(reports);
        reports.forEach(r -> assertEquals(report, r));
    }

    @Test
    public void testInitAttestationResults() {
        attestationFactory.createConfigForServer(server, CoCoEnvironmentType.KVM_AMD_EPYC_MILAN, true);
        ServerCoCoAttestationReport report = attestationFactory.createReportForServer(server);
        Long reportId = report.getId();
        attestationFactory.initResultsForReport(report);
        TestUtils.flushAndEvict(report);

        Optional<ServerCoCoAttestationReport> optReport = attestationFactory.lookupReportById(reportId);
        List<CoCoAttestationResult> results = optReport.orElseThrow().getResults();
        assertNotEmpty(results);
        List<CoCoResultType> rTypeList = results.stream()
                .map(CoCoAttestationResult::getResultType)
                .collect(Collectors.toList());
        assertContains(rTypeList, CoCoResultType.SEV_SNP);
        assertContains(rTypeList, CoCoResultType.SECURE_BOOT);
        assertNotContains(rTypeList, CoCoResultType.AZURE_SEV_SNP);

        assertTrue(results.stream()
                .filter(r -> r.getResultType().equals(CoCoResultType.SEV_SNP))
                .anyMatch(r -> r.getDescription().equals(CoCoResultType.SEV_SNP.getTypeDescription())));
    }
}
