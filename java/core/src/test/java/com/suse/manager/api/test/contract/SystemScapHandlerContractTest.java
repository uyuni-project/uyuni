/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.api.test.contract;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.scap.ScapActionDetails;
import com.redhat.rhn.domain.audit.ScapContent;
import com.redhat.rhn.domain.audit.ScapPolicy;
import com.redhat.rhn.domain.audit.TailoringFile;
import com.redhat.rhn.domain.audit.XccdfBenchmark;
import com.redhat.rhn.domain.audit.XccdfProfile;
import com.redhat.rhn.domain.audit.XccdfTestResult;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.XccdfIdentDto;
import com.redhat.rhn.frontend.dto.XccdfRuleResultDto;
import com.redhat.rhn.frontend.dto.XccdfTestResultDto;
import com.redhat.rhn.frontend.xmlrpc.system.scap.SystemScapHandler;

import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SystemScapHandlerContractTest extends BaseOpenApiTest {

    private static final Integer SID = 1000010000;
    private static final Integer XID = 10;
    private static final String XCCDF_PATH = "/usr/share/openscap/scap-yast2sec-xccdf.xml";
    private static final String OSCAP_PARAMS = "--profile Default";
    private static final String OVAL_FILES = "/usr/share/openscap/oval.xml";
    private static final String SCAN_DATE = "2026-06-01T10:00:00Z";
    private static final List<Integer> SIDS = List.of(1000010000, 1000010001);

    @Override
    protected String getApiNamespace() {
        return "system.scap";
    }

    @Override
    protected Class<SystemScapHandler> getHandlerClass() {
        return SystemScapHandler.class;
    }

    private SystemScapHandler handler() {
        return (SystemScapHandler) handlerMock;
    }

    /**
     * Assigns a field that the class fills in from the database and exposes no setter for.
     *
     * @param entity the entity to set the field on
     * @param declaringClass the class declaring the field
     * @param name the field name
     * @param value the value to assign
     * @throws Exception if the field cannot be assigned
     */
    private void setField(Object entity, Class<?> declaringClass, String name, Object value) throws Exception {
        Field field = declaringClass.getDeclaredField(name);
        field.setAccessible(true);
        field.set(entity, value);
    }

    private XccdfTestResultDto scanSummary() {
        XccdfTestResultDto scan = new XccdfTestResultDto();
        scan.setXid(XID.longValue());
        scan.setSid(SID.longValue());
        scan.setServerName("test-system.example.com");
        scan.setProfile("Default");
        scan.setPath(XCCDF_PATH);
        scan.setOvalfiles(OVAL_FILES);
        scan.setCompleted(new Date());
        return scan;
    }

    /**
     * The scan details are mocked: the real class reads whether it may be deleted through the
     * organization configuration and the action history, neither of which a contract test reaches.
     *
     * @return the details of a single scan
     */
    private XccdfTestResult scanDetails() {
        Server server = new Server();
        server.setId(SID.longValue());

        Action parentAction = context.mock(Action.class);

        ScapActionDetails actionDetails = new ScapActionDetails(XCCDF_PATH, OSCAP_PARAMS, OVAL_FILES);
        actionDetails.setParentAction(parentAction);

        XccdfBenchmark benchmark = new XccdfBenchmark();
        benchmark.setIdentifier("xccdf_org.ssgproject.content_benchmark_SLES-15");
        benchmark.setVersion("0.1.60");

        XccdfProfile profile = new XccdfProfile();
        profile.setIdentifier("xccdf_org.ssgproject.content_profile_standard");
        profile.setTitle("Standard System Security Profile");

        XccdfTestResult details = context.mock(XccdfTestResult.class);
        context.checking(new Expectations() {{
            allowing(parentAction).getId();
            will(returnValue(99L));
            allowing(details).getId();
            will(returnValue(XID.longValue()));
            allowing(details).getServer();
            will(returnValue(server));
            allowing(details).getScapActionDetails();
            will(returnValue(actionDetails));
            allowing(details).getBenchmark();
            will(returnValue(benchmark));
            allowing(details).getProfile();
            will(returnValue(profile));
            allowing(details).getIdentifier();
            will(returnValue("xccdf_org.open-scap_testresult_standard"));
            allowing(details).getStartTime();
            will(returnValue(new Date()));
            allowing(details).getEndTime();
            will(returnValue(new Date()));
            allowing(details).getErrrosContents();
            will(returnValue(""));
            allowing(details).getDeletable();
            will(returnValue(true));
        }});
        return details;
    }

    /**
     * The idents of a rule result are read from the database on first use, so the fixture assigns
     * them up front.
     *
     * @return a single rule result
     * @throws Exception if the idents cannot be assigned
     */
    private XccdfRuleResultDto ruleResult() throws Exception {
        XccdfIdentDto idref = new XccdfIdentDto();
        idref.setIdentifier("xccdf_org.ssgproject.content_rule_no_empty_passwords");
        idref.setSystem("#IDREF#");

        XccdfIdentDto cce = new XccdfIdentDto();
        cce.setIdentifier("CCE-85673-4");
        cce.setSystem("http://cce.mitre.org");

        XccdfRuleResultDto result = new XccdfRuleResultDto();
        result.setId(1L);
        result.setLabel("pass");
        result.setTestResultId(XID.longValue());
        setField(result, XccdfRuleResultDto.class, "idents", List.of(idref, cce));
        return result;
    }

    private ScapContent scapContent() {
        ScapContent content = new ScapContent();
        content.setId(1L);
        content.setName("SLES 15 SP6 content");
        content.setDescription("SCAP security guide for SLES 15 SP6");
        content.setDataStreamFileName("ssg-sle15-ds.xml");
        content.setXccdfFileName("ssg-sle15-xccdf.xml");
        return content;
    }

    private TailoringFile tailoringFile() {
        TailoringFile file = new TailoringFile("custom tailoring", "ssg-sle15-tailoring.xml");
        file.setId(2L);
        file.setOrg(fakeOrg);
        return file;
    }

    private ScapPolicy scapPolicy() {
        ScapPolicy policy = new ScapPolicy();
        policy.setId(3);
        policy.setPolicyName("Standard hardening");
        policy.setDescription("Standard hardening policy for SLES 15");
        policy.setScapContent(scapContent());
        policy.setXccdfProfileId("xccdf_org.ssgproject.content_profile_standard");
        policy.setOvalFiles(OVAL_FILES);
        policy.setAdvancedArgs("--fetch-remote-resources");
        policy.setFetchRemoteResources(true);
        policy.setOrg(fakeOrg);
        return policy;
    }

    @Test
    public void testListXccdfScans() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listXccdfScans(with(mockUser), with(SID));
            will(returnValue(List.of(scanSummary())));
        }});

        validateApiContract("/system.scap/listXccdfScans", "GET")
                .withParams(Map.of("sid", new String[] {SID.toString()}))
                .onHandlerMethod("listXccdfScans", User.class, Integer.class);
    }

    @Test
    public void testGetXccdfScanDetails() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getXccdfScanDetails(with(mockUser), with(XID));
            will(returnValue(scanDetails()));
        }});

        validateApiContract("/system.scap/getXccdfScanDetails", "GET")
                .withParams(Map.of("xid", new String[] {XID.toString()}))
                .onHandlerMethod("getXccdfScanDetails", User.class, Integer.class);
    }

    @Test
    public void testGetXccdfScanRuleResults() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getXccdfScanRuleResults(with(mockUser), with(XID));
            will(returnValue(List.of(ruleResult())));
        }});

        validateApiContract("/system.scap/getXccdfScanRuleResults", "GET")
                .withParams(Map.of("xid", new String[] {XID.toString()}))
                .onHandlerMethod("getXccdfScanRuleResults", User.class, Integer.class);
    }

    @Test
    public void testDeleteXccdfScan() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).deleteXccdfScan(with(mockUser), with(XID));
            will(returnValue(true));
        }});

        validateApiContract("/system.scap/deleteXccdfScan", "POST")
                .withBody(Map.of("xid", XID))
                .onHandlerMethod("deleteXccdfScan", User.class, Integer.class);
    }

    @Test
    public void testListScapContent() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listScapContent(with(mockUser));
            will(returnValue(List.of(scapContent())));
        }});

        validateApiContract("/system.scap/listScapContent", "GET")
                .onHandlerMethod("listScapContent", User.class);
    }

    @Test
    public void testListTailoringFiles() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listTailoringFiles(with(mockUser));
            will(returnValue(List.of(tailoringFile())));
        }});

        validateApiContract("/system.scap/listTailoringFiles", "GET")
                .onHandlerMethod("listTailoringFiles", User.class);
    }

    /**
     * A policy without a tailoring file leaves the tailoring properties out, which the documented
     * schema marks as optional.
     */
    @Test
    public void testListPolicies() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listPolicies(with(mockUser));
            will(returnValue(List.of(scapPolicy())));
        }});

        validateApiContract("/system.scap/listPolicies", "GET")
                .onHandlerMethod("listPolicies", User.class);
    }

    @Test
    public void testScheduleXccdfScanBySids() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sids", SIDS);
        body.put("xccdfPath", XCCDF_PATH);
        body.put("oscapParams", OSCAP_PARAMS);

        context.checking(new Expectations() {{
            oneOf(handler()).scheduleXccdfScan(with(mockUser), with(any(List.class)), with(XCCDF_PATH),
                    with(OSCAP_PARAMS));
            will(returnValue(1));
        }});

        validateApiContract("/system.scap/scheduleXccdfScan", "POST")
                .withBody(body)
                .onHandlerMethod("scheduleXccdfScan", User.class, List.class, String.class, String.class);
    }

    @Test
    public void testScheduleXccdfScanBySidsAtDate() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sids", SIDS);
        body.put("xccdfPath", XCCDF_PATH);
        body.put("oscapParams", OSCAP_PARAMS);
        body.put("date", SCAN_DATE);

        context.checking(new Expectations() {{
            oneOf(handler()).scheduleXccdfScan(with(mockUser), with(any(List.class)), with(XCCDF_PATH),
                    with(OSCAP_PARAMS), with(any(Date.class)));
            will(returnValue(1));
        }});

        validateApiContract("/system.scap/scheduleXccdfScan", "POST")
                .withBody(body)
                .onHandlerMethod("scheduleXccdfScan", User.class, List.class, String.class, String.class,
                        Date.class);
    }

    @Test
    public void testScheduleXccdfScanBySidsWithOvalFiles() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sids", SIDS);
        body.put("xccdfPath", XCCDF_PATH);
        body.put("oscapParams", OSCAP_PARAMS);
        body.put("ovalFiles", OVAL_FILES);
        body.put("date", SCAN_DATE);

        context.checking(new Expectations() {{
            oneOf(handler()).scheduleXccdfScan(with(mockUser), with(any(List.class)), with(XCCDF_PATH),
                    with(OSCAP_PARAMS), with(OVAL_FILES), with(any(Date.class)));
            will(returnValue(1));
        }});

        validateApiContract("/system.scap/scheduleXccdfScan", "POST")
                .withBody(body)
                .onHandlerMethod("scheduleXccdfScan", User.class, List.class, String.class, String.class,
                        String.class, Date.class);
    }

    @Test
    public void testScheduleXccdfScanBySid() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sid", SID);
        body.put("xccdfPath", XCCDF_PATH);
        body.put("oscapParams", OSCAP_PARAMS);

        context.checking(new Expectations() {{
            oneOf(handler()).scheduleXccdfScan(with(mockUser), with(SID), with(XCCDF_PATH), with(OSCAP_PARAMS));
            will(returnValue(1));
        }});

        validateApiContract("/system.scap/scheduleXccdfScan", "POST")
                .withBody(body)
                .onHandlerMethod("scheduleXccdfScan", User.class, Integer.class, String.class, String.class);
    }

    @Test
    public void testScheduleXccdfScanBySidAtDate() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sid", SID);
        body.put("xccdfPath", XCCDF_PATH);
        body.put("oscapParams", OSCAP_PARAMS);
        body.put("date", SCAN_DATE);

        context.checking(new Expectations() {{
            oneOf(handler()).scheduleXccdfScan(with(mockUser), with(SID), with(XCCDF_PATH), with(OSCAP_PARAMS),
                    with(any(Date.class)));
            will(returnValue(1));
        }});

        validateApiContract("/system.scap/scheduleXccdfScan", "POST")
                .withBody(body)
                .onHandlerMethod("scheduleXccdfScan", User.class, Integer.class, String.class, String.class,
                        Date.class);
    }

    @Test
    public void testScheduleBetaXccdfScanWithPolicy() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sids", SIDS);
        body.put("policyId", 3);
        body.put("date", SCAN_DATE);

        context.checking(new Expectations() {{
            oneOf(handler()).scheduleBetaXccdfScanWithPolicy(with(mockUser), with(SIDS), with(3),
                    with(any(Date.class)));
            will(returnValue(1L));
        }});

        validateApiContract("/system.scap/scheduleBetaXccdfScanWithPolicy", "POST")
                .withBody(body)
                .onHandlerMethod("scheduleBetaXccdfScanWithPolicy", User.class, List.class, Integer.class,
                        Date.class);
    }

    @Test
    public void testScheduleBetaXccdfScanCustom() throws Exception {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("scapContentId", 1);
        params.put("xccdfProfileId", "xccdf_org.ssgproject.content_profile_standard");
        params.put("ovalFiles", OVAL_FILES);
        params.put("advancedArgs", "--fetch-remote-resources");
        params.put("fetchRemoteResources", true);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sids", SIDS);
        body.put("params", params);
        body.put("date", SCAN_DATE);

        context.checking(new Expectations() {{
            oneOf(handler()).scheduleBetaXccdfScanCustom(with(mockUser), with(SIDS), with(any(Map.class)),
                    with(any(Date.class)));
            will(returnValue(1L));
        }});

        validateApiContract("/system.scap/scheduleBetaXccdfScanCustom", "POST")
                .withBody(body)
                .onHandlerMethod("scheduleBetaXccdfScanCustom", User.class, List.class, Map.class, Date.class);
    }
}
