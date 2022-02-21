/*
 * Copyright (c) 2017--2021 SUSE LLC
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
package com.redhat.rhn.manager.audit.test;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.scap.ScapAction;
import com.redhat.rhn.domain.audit.XccdfTestResult;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.audit.ScapManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Test for {@link ScapManager}
 */
public class ScapManagerTest extends JMockBaseTestCaseWithUser {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
    }

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
                "/com/redhat/rhn/manager/audit/test/openscap/minionsles12sp1.test.local/xccdf-resume.xslt.in")
                .getPath());
        InputStream resultsIn = TestUtils.findTestData(
                "/com/redhat/rhn/manager/audit/test/openscap/minionsles12sp1.test.local/results.xml")
                .openStream();
        XccdfTestResult result = ScapManager.xccdfEval(minion, action, 2, "", resultsIn, resumeXsl);

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        result = HibernateFactory.getSession().get(XccdfTestResult.class, result.getId());
        assertNotNull(result);

        assertEquals("Default", result.getProfile().getIdentifier());
        assertEquals("Default vanilla kernel hardening", result.getProfile().getTitle());
    }

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
                "/com/redhat/rhn/manager/audit/test/openscap/suma-ref42-min-sles15/xccdf-resume.xslt.in").getPath());
        InputStream resultsIn = TestUtils.findTestData(
                "/com/redhat/rhn/manager/audit/test/openscap/suma-ref42-min-sles15/results.xml")
                .openStream();
        XccdfTestResult result = ScapManager.xccdfEval(minion, action, 2, "", resultsIn, resumeXsl);

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        result = HibernateFactory.getSession().get(XccdfTestResult.class, result.getId());
        assertNotNull(result);

        assertEquals("xccdf_org.ssgproject.content_profile_cis_suse_test", result.getProfile().getIdentifier());
        assertEquals("Tailored profile", result.getProfile().getTitle());
        assertRuleResults(result, "pass",
                Arrays.asList(
                    "xccdf_org.ssgproject.content_rule_rpm_verify_ownership",
                    "xccdf_org.ssgproject.content_rule_ensure_suse_gpgkey_installed",
                    "CCE-85796-1"
                ));
    }

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
        String resume = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<benchmark-resume xmlns:cdf=\"http://checklists.nist.gov/xccdf/1.1\" " +
                "xmlns:xccdf_12=\"http://checklists.nist.gov/xccdf/1.2\" " +
                "id=\"SUSE-Security-Benchmark-YaST2\" version=\"1\">\n" +
                "  <profile title=\"Default vanilla kernel hardening\" id=\"Default\" description=\"\"/>\n" +
                "  <TestResult id=\"xccdf_org.open-scap_testresult_Default\" start-time=\"2017-02-14T15:22:39\" " +
                "end-time=\"2017-02-14T15:22:39\">\n" +
                "    <pass>\n" +
                "      <rr id=\"rule-sysctl-ipv4-forward\">\n" +
                "          <ident system=\"SYSTEM\">IDENT1</ident>\n" +
                "          <ident system=\"SYSTEM\">IDENT2</ident>\n" +
                "      </rr>\n" +
                "      <rr id=\"rule-sysctl-ipv4-tcpsyncookies\"/>\n" +
                "      <rr id=\"rule-sysctl-ipv6-all-forward\"/>\n" +
                "      <rr id=\"rule-sysctl-ipv6-default-forward\"/>\n" +
                "      <rr id=\"rule-pwd-maxdays\"/>\n" +
                "      <rr id=\"rule-pwd-mindays\"/>\n" +
                "      <rr id=\"rule-pwd-warnage\"/>\n" +
                "      <rr id=\"rule-authc-faildelay\"/>\n" +
                "      <rr id=\"rule-authc-faildelayexist\"/>\n" +
                "      <rr id=\"rule-usermgmt-uidmin\"/>\n" +
                "      <rr id=\"rule-usermgmt-uidmax\"/>\n" +
                "      <rr id=\"rule-usermgmt-gidmin\"/>\n" +
                "      <rr id=\"rule-usermgmt-gidmax\"/>\n" +
                "    </pass>\n" +
                "    <fail>\n" +
                "      <rr id=\"rule-kernel-syncookies\"/>\n" +
                "      <rr id=\"rule-pwd-minlen\"/>\n" +
                "      <rr id=\"rule-pwd-remember\"/>\n" +
                "      <rr id=\"rule-authc-xdmcp-remote\"/>\n" +
                "      <rr id=\"rule-authc-xdmcp-root\"/>\n" +
                "      <rr id=\"rule-misc-sysrq\"/>\n" +
                "      <rr id=\"rule-misc-hashalgo_md5\"/>\n" +
                "      <rr id=\"rule-misc-hashalgo_des\"/>\n" +
                "      <rr id=\"rule-misc-perm-check\"/>\n" +
                "      <rr id=\"rule-misc-sig-check\"/>\n" +
                "      <rr id=\"rule-srvc-dhcpd-chroot\"/>\n" +
                "      <rr id=\"rule-srvc-dhcpd-uid\"/>\n" +
                "      <rr id=\"rule-srvc-dhcpd6-chroot\"/>\n" +
                "      <rr id=\"rule-srvc-dhcpd6-uid\"/>\n" +
                "      <rr id=\"rule-srvc-update-restart\"/>\n" +
                "      <rr id=\"rule-srvc-remove-stop\"/>\n" +
                "    </fail>\n" +
                "    <error>\n" +
                "      <rr id=\"rule-kernel-syncookies\"/>\n" +
                "    </error>\n" +
                "    <unknown>\n" +
                "      <rr id=\"rule-kernel-syncookies\"/>\n" +
                "    </unknown>\n" +
                "    <notapplicable>\n" +
                "      <rr id=\"rule-kernel-syncookies\"/>\n" +
                "    </notapplicable>\n" +
                "    <notchecked>\n" +
                "      <rr id=\"rule-kernel-syncookies\"/>\n" +
                "    </notchecked>\n" +
                "    <notselected>\n" +
                "      <rr id=\"rule-kernel-syncookies\"/>\n" +
                "    </notselected>\n" +
                "    <informational>\n" +
                "      <rr id=\"rule-kernel-syncookies\"/>\n" +
                "    </informational>\n" +
                "    <fixed>\n" +
                "      <rr id=\"rule-kernel-syncookies\"/>\n" +
                "    </fixed>\n" +
                "  </TestResult>\n" +
                "</benchmark-resume>\n";

        XccdfTestResult result = ScapManager.xccdfEvalResume(minion, action, 2, "",
                new ByteArrayInputStream(resume.getBytes("UTF-8")));

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        result = HibernateFactory.getSession().get(XccdfTestResult.class, result.getId());
        assertNotNull(result);

        assertEquals("Default", result.getProfile().getIdentifier());
        assertEquals("Default vanilla kernel hardening", result.getProfile().getTitle());

        assertEquals("SUSE-Security-Benchmark-YaST2", result.getBenchmark().getIdentifier());
        assertEquals("1", result.getBenchmark().getVersion());

        assertEquals("xccdf_eval: oscap tool returned 2\n", result.getErrrosContents());

        assertFalse(result.getResults().isEmpty());

        assertRuleResults(result, "pass",
                Arrays.asList(
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
        ));

        assertRuleResults(result, "fail",
                Arrays.asList(
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
                ));
        assertRuleResults(result, "error",
                Arrays.asList(
                    "rule-kernel-syncookies"
                ));
        assertRuleResults(result, "unknown",
                Arrays.asList(
                        "rule-kernel-syncookies"
                ));
        assertRuleResults(result, "notapplicable",
                Arrays.asList(
                        "rule-kernel-syncookies"
                ));
        assertRuleResults(result, "notchecked",
                Arrays.asList(
                        "rule-kernel-syncookies"
                ));
        assertRuleResults(result, "notselected",
                Arrays.asList(
                        "rule-kernel-syncookies"
                ));
        assertRuleResults(result, "informational",
                Arrays.asList(
                        "rule-kernel-syncookies"
                ));
        assertRuleResults(result, "fixed",
                Arrays.asList(
                        "rule-kernel-syncookies"
                ));
    }

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
                "/com/redhat/rhn/manager/audit/test/openscap/minionsles12sp1.test.local/xccdf-resume.xslt.in")
                .getPath());
        InputStream resultsIn = TestUtils.findTestData(
                "/com/redhat/rhn/manager/audit/test/openscap/rhccp/results.xml")
                .openStream();
        XccdfTestResult result = ScapManager.xccdfEval(minion, action, 2, "", resultsIn, resumeXsl);

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        result = HibernateFactory.getSession().get(XccdfTestResult.class, result.getId());
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
                .map(ident -> ident.getIdentifier())
                .collect(Collectors.toSet());
        assertEquals(ruleIds.size(), resultIds.size());
        assertTrue("Expected but missing rules: " + resultIds.stream()
                     .filter(r -> !ruleIds.contains(r)).collect(Collectors.toList()),
            resultIds.containsAll(ruleIds));
    }

    private void assertRuleResultsCount(XccdfTestResult result, String ruleType, int count) {
        long matchedRulesCount = result.getResults().stream()
                .filter(rr -> rr.getResultType().getLabel().equals(ruleType))
                .count();
        assertEquals(count, matchedRulesCount);
    }

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
                "/com/redhat/rhn/manager/audit/test/openscap/minionsles12sp1.test.local/xccdf-resume.xslt.in")
                .getPath());
        InputStream resultsIn = TestUtils.findTestData(
                "/com/redhat/rhn/manager/audit/test/openscap/minionsles12sp1.test.local/results_malformed.xml")
                .openStream();
        try {
            XccdfTestResult result = ScapManager.xccdfEval(minion, action, 2, "", resultsIn, resumeXsl);
            fail("Expected exception");
        }
        catch (Exception e) {
            assertTrue(e instanceof RuntimeException);
        }
    }

}


