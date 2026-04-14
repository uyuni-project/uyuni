/*
 * Copyright (c) 2017--2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.manager.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.scap.ScapAction;
import com.redhat.rhn.domain.audit.ScapFactory;
import com.redhat.rhn.domain.audit.XccdfIdent;
import com.redhat.rhn.domain.audit.XccdfTestResult;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactoryTest;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.audit.scap.xml.BenchmarkResume;
import com.redhat.rhn.manager.audit.scap.xml.TestResultRuleResult;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.xml.bind.JAXBContext;

/**
 * Test for {@link ScapManager}
 */
public class ScapManagerTest extends JMockBaseTestCaseWithUser {

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
    }

    @Test
    public void testXccdfEvalTransformXccdf11() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        SystemManager.giveCapability(minion.getId(), SystemManager.CAP_SCAP, 1L);

        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        ActionManager.setTaskomaticApi(taskomaticMock);

        context().checking(new Expectations() { {
            allowing(taskomaticMock).scheduleActionExecution(with(any(Action.class)));
        } });

        ScapAction action = ActionManager.scheduleXccdfEval(user,
                minion, "/usr/share/openscap/scap-yast2sec-xccdf.xml", "--profile Default", new Date());

        File resumeXsl = new File(TestUtils.findTestData(
                "/com/redhat/rhn/manager/audit/openscap/minionsles12sp1.test.local/xccdf-resume.xslt.in")
                .getPath());
        InputStream resultsIn = TestUtils.findTestData(
                "/com/redhat/rhn/manager/audit/openscap/minionsles12sp1.test.local/results.xml")
                .openStream();
        XccdfTestResult result = ScapManager.xccdfEval(minion, action, 2, "", resultsIn, resumeXsl);

        result = HibernateFactory.getSession().find(XccdfTestResult.class, result.getId());
        assertNotNull(result);

        assertEquals("Default", result.getProfile().getIdentifier());
        assertEquals("Default vanilla kernel hardening", result.getProfile().getTitle());
    }

    @Test
    public void testXccdfEvalTransformXccdfWithTailoring() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        SystemManager.giveCapability(minion.getId(), SystemManager.CAP_SCAP, 1L);

        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        ActionManager.setTaskomaticApi(taskomaticMock);

        context().checking(new Expectations() { {
            allowing(taskomaticMock).scheduleActionExecution(with(any(Action.class)));
        } });

        ScapAction action = ActionManager.scheduleXccdfEval(user,
                minion, "/usr/share/xml/scap/ssg/content/ssg-sle15-ds-1.2.xml",
                "--profile suse_test --tailoring-file /root/tailoring.xml", new Date());

        File resumeXsl = new File(TestUtils.findTestData(
                "/com/redhat/rhn/manager/audit/openscap/suma-ref42-min-sles15/xccdf-resume.xslt.in").getPath());
        InputStream resultsIn = TestUtils.findTestData(
                "/com/redhat/rhn/manager/audit/openscap/suma-ref42-min-sles15/results.xml")
                .openStream();
        XccdfTestResult result = ScapManager.xccdfEval(minion, action, 2, "", resultsIn, resumeXsl);

        result = HibernateFactory.getSession().find(XccdfTestResult.class, result.getId());
        assertNotNull(result);

        assertEquals("xccdf_org.ssgproject.content_profile_cis_suse_test", result.getProfile().getIdentifier());
        assertEquals("Tailored profile", result.getProfile().getTitle());
        assertRuleResults(result, "pass",
                List.of(
                    "xccdf_org.ssgproject.content_rule_rpm_verify_ownership",
                    "xccdf_org.ssgproject.content_rule_ensure_suse_gpgkey_installed",
                    "CCE-85796-1"
                )
        );
    }

    @Test
    public void testXccdfEvalResume() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        SystemManager.giveCapability(minion.getId(), SystemManager.CAP_SCAP, 1L);

        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        ActionManager.setTaskomaticApi(taskomaticMock);

        context().checking(new Expectations() { {
            allowing(taskomaticMock).scheduleActionExecution(with(any(Action.class)));
        } });

        ScapAction action = ActionManager.scheduleXccdfEval(user,
                minion, "/usr/share/openscap/scap-yast2sec-xccdf.xml", "--profile Default", new Date());
        String resume = """
                        <?xml version="1.0" encoding="UTF-8"?>
                        <benchmark-resume xmlns:cdf="http://checklists.nist.gov/xccdf/1.1"
                                            xmlns:xccdf_12="http://checklists.nist.gov/xccdf/1.2"
                                            id="SUSE-Security-Benchmark-YaST2" version="1">
                          <profile title="Default vanilla kernel hardening" id="Default" description=""/>
                          <TestResult id="xccdf_org.open-scap_testresult_Default"
                                        start-time="2017-02-14T15:22:39" end-time="2017-02-14T15:22:39">
                            <pass>
                              <rr id="rule-sysctl-ipv4-forward">
                                  <ident system="SYSTEM">IDENT1</ident>
                                  <ident system="SYSTEM">IDENT2</ident>
                              </rr>
                              <rr id="rule-sysctl-ipv4-tcpsyncookies"/>
                              <rr id="rule-sysctl-ipv6-all-forward"/>
                              <rr id="rule-sysctl-ipv6-default-forward"/>
                              <rr id="rule-pwd-maxdays"/>
                              <rr id="rule-pwd-mindays"/>
                              <rr id="rule-pwd-warnage"/>
                              <rr id="rule-authc-faildelay"/>
                              <rr id="rule-authc-faildelayexist"/>
                              <rr id="rule-usermgmt-uidmin"/>
                              <rr id="rule-usermgmt-uidmax"/>
                              <rr id="rule-usermgmt-gidmin"/>
                              <rr id="rule-usermgmt-gidmax"/>
                            </pass>
                            <fail>
                              <rr id="rule-kernel-syncookies"/>
                              <rr id="rule-pwd-minlen"/>
                              <rr id="rule-pwd-remember"/>
                              <rr id="rule-authc-xdmcp-remote"/>
                              <rr id="rule-authc-xdmcp-root"/>
                              <rr id="rule-misc-sysrq"/>
                              <rr id="rule-misc-hashalgo_md5"/>
                              <rr id="rule-misc-hashalgo_des"/>
                              <rr id="rule-misc-perm-check"/>
                              <rr id="rule-misc-sig-check"/>
                              <rr id="rule-srvc-dhcpd-chroot"/>
                              <rr id="rule-srvc-dhcpd-uid"/>
                              <rr id="rule-srvc-dhcpd6-chroot"/>
                              <rr id="rule-srvc-dhcpd6-uid"/>
                              <rr id="rule-srvc-update-restart"/>
                              <rr id="rule-srvc-remove-stop"/>
                            </fail>
                            <error>
                              <rr id="rule-kernel-syncookies"/>
                            </error>
                            <unknown>
                              <rr id="rule-kernel-syncookies"/>
                            </unknown>
                            <notapplicable>
                              <rr id="rule-kernel-syncookies"/>
                            </notapplicable>
                            <notchecked>
                              <rr id="rule-kernel-syncookies"/>
                            </notchecked>
                            <notselected>
                              <rr id="rule-kernel-syncookies"/>
                            </notselected>
                            <informational>
                              <rr id="rule-kernel-syncookies"/>
                            </informational>
                            <fixed>
                              <rr id="rule-kernel-syncookies"/>
                            </fixed>
                          </TestResult>
                        </benchmark-resume>
                        """;

        XccdfTestResult result;

        try (InputStream sourceStream = new ByteArrayInputStream(resume.getBytes(StandardCharsets.UTF_8))) {
            result = ScapManager.xccdfEvalResume(minion, action, 2, "", sourceStream);
        }

        result = HibernateFactory.getSession().find(XccdfTestResult.class, result.getId());
        assertNotNull(result);

        assertEquals("Default", result.getProfile().getIdentifier());
        assertEquals("Default vanilla kernel hardening", result.getProfile().getTitle());

        assertEquals("SUSE-Security-Benchmark-YaST2", result.getBenchmark().getIdentifier());
        assertEquals("1", result.getBenchmark().getVersion());

        assertEquals("xccdf_eval: oscap tool returned 2\n", result.getErrrosContents());

        assertFalse(result.getResults().isEmpty());

        assertRuleResults(result, "pass",
                List.of(
                    "rule-sysctl-ipv4-forward",
                    "IDENT1",
                    "IDENT2",
                    "rule-sysctl-ipv4-tcpsyncookies",
                    "rule-sysctl-ipv6-all-forward",
                    "rule-sysctl-ipv6-default-forward",
                    "rule-pwd-maxdays",
                    "rule-pwd-mindays",
                    "rule-pwd-warnage",
                    "rule-authc-faildelay",
                    "rule-authc-faildelayexist",
                    "rule-usermgmt-uidmin",
                    "rule-usermgmt-uidmax",
                    "rule-usermgmt-gidmin",
                    "rule-usermgmt-gidmax"
                )
        );

        assertRuleResults(result, "fail",
                List.of(
                    "rule-kernel-syncookies",
                    "rule-pwd-minlen",
                    "rule-pwd-remember",
                    "rule-authc-xdmcp-remote",
                    "rule-authc-xdmcp-root",
                    "rule-misc-sysrq",
                    "rule-misc-hashalgo_md5",
                    "rule-misc-hashalgo_des",
                    "rule-misc-perm-check",
                    "rule-misc-sig-check",
                    "rule-srvc-dhcpd-chroot",
                    "rule-srvc-dhcpd-uid",
                    "rule-srvc-dhcpd6-chroot",
                    "rule-srvc-dhcpd6-uid",
                    "rule-srvc-update-restart",
                    "rule-srvc-remove-stop"
                )
        );

        assertRuleResults(result, "error", List.of("rule-kernel-syncookies"));
        assertRuleResults(result, "unknown", List.of("rule-kernel-syncookies"));
        assertRuleResults(result, "notapplicable", List.of("rule-kernel-syncookies"));
        assertRuleResults(result, "notchecked", List.of("rule-kernel-syncookies"));
        assertRuleResults(result, "notselected", List.of("rule-kernel-syncookies"));
        assertRuleResults(result, "informational", List.of("rule-kernel-syncookies"));
        assertRuleResults(result, "fixed", List.of("rule-kernel-syncookies"));
    }

    @Test
    public void testBenchmarkResumeUnmarshallsRuleResultIdents() throws Exception {

        String resume = """
            <?xml version="1.0" encoding="UTF-8"?>
            <benchmark-resume id="benchmark-id" version="1">
              <profile title="Default" id="Default" description=""/>
              <TestResult id="xccdf_org.open-scap_testresult_Default"
                            start-time="2017-02-14T15:22:39" end-time="2017-02-14T15:22:39">
                <pass>
                  <rr id="rule-sysctl-ipv4-forward">
                    <ident system="SYSTEM">IDENT1</ident>
                    <ident system="abc"></ident>
                    <ident system="def" />
                    <ident system="ghi">   </ident>
                  </rr>
                </pass>
                <fail/>
                <error/>
                <unknown/>
                <notapplicable/>
                <notchecked/>
                <notselected/>
                <informational/>
                <fixed/>
              </TestResult>
            </benchmark-resume>
            """;

        BenchmarkResume benchmarkResume;

        try (InputStream sourceStream = new ByteArrayInputStream(resume.getBytes(StandardCharsets.UTF_8))) {
            benchmarkResume = (BenchmarkResume) JAXBContext.newInstance(BenchmarkResume.class)
                    .createUnmarshaller()
                    .unmarshal(sourceStream);
        }

        assertNotNull(benchmarkResume.getTestResult());
        assertNotNull(benchmarkResume.getTestResult().getPass());
        assertEquals(1, benchmarkResume.getTestResult().getPass().size());

        TestResultRuleResult passedRule = benchmarkResume.getTestResult().getPass().get(0);
        assertEquals("rule-sysctl-ipv4-forward", passedRule.getId());
        assertNotNull(passedRule.getIdents());
        assertEquals(4, passedRule.getIdents().size());
        assertEquals("SYSTEM", passedRule.getIdents().get(0).getSystem());
        assertEquals("IDENT1", passedRule.getIdents().get(0).getText());
        assertEquals("abc", passedRule.getIdents().get(1).getSystem());
        assertNull(passedRule.getIdents().get(1).getText());
        assertEquals("def", passedRule.getIdents().get(2).getSystem());
        assertNull(passedRule.getIdents().get(2).getText());
        assertEquals("ghi", passedRule.getIdents().get(3).getSystem());
        assertEquals("   ", passedRule.getIdents().get(3).getText());
    }

    @Test
    public void testXccdfEvalTransformXccdf12() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        SystemManager.giveCapability(minion.getId(), SystemManager.CAP_SCAP, 1L);

        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        ActionManager.setTaskomaticApi(taskomaticMock);

        context().checking(new Expectations() { {
            allowing(taskomaticMock).scheduleActionExecution(with(any(Action.class)));
        } });

        ScapAction action = ActionManager.scheduleXccdfEval(user,
                minion, "/usr/share/openscap/scap-yast2sec-xccdf.xml",
                "--profile xccdf_org.ssgproject.content_profile_rht-ccp", new Date());

        File resumeXsl = new File(TestUtils.findTestData(
                "/com/redhat/rhn/manager/audit/openscap/minionsles12sp1.test.local/xccdf-resume.xslt.in")
                .getPath());
        InputStream resultsIn = TestUtils.findTestData(
                "/com/redhat/rhn/manager/audit/openscap/rhccp/results.xml")
                .openStream();
        XccdfTestResult result = ScapManager.xccdfEval(minion, action, 2, "", resultsIn, resumeXsl);

        result = HibernateFactory.getSession().find(XccdfTestResult.class, result.getId());
        assertNotNull(result);

        assertEquals("xccdf_org.ssgproject.content_profile_rht-ccp", result.getProfile().getIdentifier());
        assertEquals("Red Hat Corporate Profile for Certified Cloud Providers (RH CCP)",
                result.getProfile().getTitle());

        assertEquals(841, result.getResults().size());
        assertRuleResultsCount(result, "pass", 35);
        assertRuleResultsCount(result, "fail", 34);
        assertRuleResultsCount(result, "notchecked", 1);
        assertRuleResultsCount(result, "notselected", 771);

    }

    private void assertRuleResults(XccdfTestResult result, String ruleType, List<String> ruleIds) {
        Set<String> resultIds = result.getResults().stream()
                .filter(rr -> rr.getResultType().getLabel().equals(ruleType))
                .flatMap(rr -> rr.getIdents().stream())
                .map(XccdfIdent::getIdentifier)
                .collect(Collectors.toSet());
        assertEquals(ruleIds.size(), resultIds.size());
        assertTrue(resultIds.containsAll(ruleIds),
                "Expected but missing rules: " + resultIds.stream()
                             .filter(r -> !ruleIds.contains(r)).toList());
    }

    private void assertRuleResultsCount(XccdfTestResult result, String ruleType, int count) {
        long matchedRulesCount = result.getResults().stream()
                .filter(rr -> rr.getResultType().getLabel().equals(ruleType))
                .count();
        assertEquals(count, matchedRulesCount);
    }

    @Test
    public void testXccdfEvalError() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        SystemManager.giveCapability(minion.getId(), SystemManager.CAP_SCAP, 1L);

        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        ActionManager.setTaskomaticApi(taskomaticMock);

        context().checking(new Expectations() { {
            allowing(taskomaticMock).scheduleActionExecution(with(any(Action.class)));
        } });

        ScapAction action = ActionManager.scheduleXccdfEval(user,
                minion, "/usr/share/openscap/scap-yast2sec-xccdf.xml", "--profile Default", new Date());

        File resumeXsl = new File(TestUtils.findTestData(
                "/com/redhat/rhn/manager/audit/openscap/minionsles12sp1.test.local/xccdf-resume.xslt.in")
                .getPath());
        InputStream resultsIn = TestUtils.findTestData(
                "/com/redhat/rhn/manager/audit/openscap/minionsles12sp1.test.local/results_malformed.xml")
                .openStream();

        assertThrows(RuntimeException.class, () -> ScapManager.xccdfEval(minion, action, 2, "", resultsIn, resumeXsl));
    }
    @Test
    public void testRemediationExtraction() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        SystemManager.giveCapability(minion.getId(), SystemManager.CAP_SCAP, 1L);

        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        ActionManager.setTaskomaticApi(taskomaticMock);
        context().checking(new Expectations() { {
            allowing(taskomaticMock).scheduleActionExecution(with(any(Action.class)));
        } });
        ScapAction action = ActionManager.scheduleXccdfEval(user,
                minion, "/usr/share/openscap/scap-yast2sec-xccdf.xml", "--profile Default", new Date());
        File resumeXsl = new File(TestUtils.findTestData(
            "/com/redhat/rhn/manager/audit/openscap/suma-ref42-min-sles15/xccdf-resume.xslt.in")
          .getPath());
        InputStream resultsIn = TestUtils.findTestData(
            "/com/redhat/rhn/manager/audit/openscap/suma-ref42-min-sles15/results.xml")
          .openStream();
        XccdfTestResult result = ScapManager.xccdfEval(minion, action, 2, "", resultsIn, resumeXsl);
        assertNotNull(result);
        assertNotNull(ScapFactory.lookupRuleRemediationsByBenchmark("xccdf_org.ssgproject.content_benchmark_SLE-15"));
        var fix1 = ScapFactory
          .lookupRuleRemediation("xccdf_org.ssgproject.content_benchmark_SLE-15",
            "xccdf_org.ssgproject.content_rule_sshd_set_max_sessions");
        assertTrue(fix1.isPresent());
        //assertTrue(result.);
    }
}


