package com.redhat.rhn.manager.audit.test;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.scap.ScapAction;
import com.redhat.rhn.domain.audit.XccdfTestResult;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.audit.ScapManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.suse.utils.Opt;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.redhat.rhn.testing.ErrataTestUtils.createTestServer;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestUser;

/**
 * Created by matei on 2/14/17.
 */
public class ScapManagerTest extends RhnBaseTestCase {

    public void testXccdfEval() throws Exception {
        User user = createTestUser();
        Server server1 = createTestServer(user);
        SystemManager.giveCapability(server1.getId(), "scap.xccdf_eval", 1L);
        ScapAction action = ActionManager.scheduleXccdfEval(user,
                server1, "/usr/share/openscap/scap-yast2sec-xccdf.xml", "--profile Default", new Date());
        String errors = "xccdf_eval: oscap tool returned 2\n";
        String resume = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<benchmark-resume xmlns:cdf=\"http://checklists.nist.gov/xccdf/1.1\" xmlns:xccdf_12=\"http://checklists.nist.gov/xccdf/1.2\" id=\"SUSE-Security-Benchmark-YaST2\" version=\"1\">\n" +
                "  <profile title=\"Default vanilla kernel hardening\" id=\"Default\" description=\"\"/>\n" +
                "  <TestResult id=\"xccdf_org.open-scap_testresult_Default\" start-time=\"2017-02-14T15:22:39\" end-time=\"2017-02-14T15:22:39\">\n" +
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
                "    <error/>\n" +
                "    <unknown/>\n" +
                "    <notapplicable/>\n" +
                "    <notchecked/>\n" +
                "    <notselected/>\n" +
                "    <informational/>\n" +
                "    <fixed/>\n" +
                "  </TestResult>\n" +
                "</benchmark-resume>\n";
        XccdfTestResult result = ScapManager.xccdfEval(server1, action, errors, resume);

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        result = (XccdfTestResult)HibernateFactory.getSession().get(XccdfTestResult.class, result.getId());
        assertNotNull(result);

        assertEquals("Default", result.getProfile().getIdentifier());
        assertEquals("Default vanilla kernel hardening", result.getProfile().getTitle());

        assertEquals("SUSE-Security-Benchmark-YaST2", result.getBenchmark().getIdentifier());
        assertEquals("1", result.getBenchmark().getVersion());

        assertEquals(errors, result.getErrrosContents());

        assertFalse(result.getResults().isEmpty());
        Set<String> pass = result.getResults().stream()
                .filter(rr -> rr.getResultType().getLabel().equals("pass"))
                .flatMap(rr -> rr.getIdents().stream())
                .map(ident -> ident.getIdentifier())
                .collect(Collectors.toSet());
        List<String> passIds = Arrays.asList(
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
        );
        assertTrue(pass.containsAll(passIds));
        assertEquals(passIds.size(), pass.size());

    }

}


