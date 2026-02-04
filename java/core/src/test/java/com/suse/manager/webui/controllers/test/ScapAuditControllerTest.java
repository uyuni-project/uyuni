/*
 * Copyright (c) 2025 SUSE LLC
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

package com.suse.manager.webui.controllers.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.audit.ScapContent;
import com.redhat.rhn.domain.audit.ScapFactory;
import com.redhat.rhn.domain.audit.ScapPolicy;
import com.redhat.rhn.domain.audit.ScriptType;
import com.redhat.rhn.domain.audit.TailoringFile;
import com.redhat.rhn.domain.audit.XccdfRuleFix;
import com.redhat.rhn.domain.audit.XccdfRuleFixCustom;
import com.redhat.rhn.testing.SparkTestUtils;

import com.suse.manager.webui.controllers.ApplyRemediationJson;
import com.suse.manager.webui.controllers.CustomRemediationJson;
import com.suse.manager.webui.controllers.ScapAuditController;
import com.suse.manager.webui.utils.gson.ResultJson;
import com.suse.manager.webui.utils.gson.ScapPolicyJson;
import com.suse.utils.Json;

import com.google.gson.Gson;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import spark.ModelAndView;
import spark.Request;

/**
 * Test for {@link ScapAuditController}
 */
public class ScapAuditControllerTest extends BaseControllerTestCase {

    private ScapAuditController controller;
    private static final Gson GSON = Json.GSON;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        controller = new ScapAuditController();
    }

    private ScapContent createTestScapContent() {
        ScapContent content = new ScapContent();
        content.setName("Test Content " + System.currentTimeMillis());
        content.setDataStreamFileName("ds-" + System.currentTimeMillis() + ".xml");
        content.setXccdfFileName("xccdf-" + System.currentTimeMillis() + ".xml");
        ScapFactory.saveScapContent(content);
        return content;
    }

    private XccdfRuleFix createTestXccdfRuleFix(String benchmarkId, String ruleId) {
        XccdfRuleFix fix = new XccdfRuleFix(benchmarkId, ruleId, "remediation");
        HibernateFactory.getSession().save(fix);
        HibernateFactory.getSession().flush();
        return fix;
    }

    private TailoringFile createTestTailoringFile() {
        TailoringFile tf = new TailoringFile("Test Tailoring " + System.currentTimeMillis(),
                "tf-" + System.currentTimeMillis() + ".xml");
        tf.setOrg(user.getOrg());
        ScapFactory.saveTailoringFile(tf);
        return tf;
    }

    @Test
    public void testCreateScapPolicy() throws Exception {
        ScapContent content = createTestScapContent();

        ScapPolicyJson json = new ScapPolicyJson();
        json.setPolicyName("Test Policy Create");
        json.setScapContentId(content.getId());
        json.setXccdfProfileId("xccdf_profile_test");
        json.setDescription("Test Description");

        String body = GSON.toJson(json);
        Request request = getPostRequestWithCsrfAndBody("/manager/api/audit/scap/policy/create", body);

        String responseStr = controller.createScapPolicy(request, response, user);
        ResultJson<?> result = GSON.fromJson(responseStr, ResultJson.class);

        assertTrue(result.isSuccess(), "Response should be success");
        assertNotNull(result.getData(), "Response data (ID) should not be null");

        // Verify in DB
        Integer policyId = ((Double) result.getData()).intValue();
        Optional<ScapPolicy> policy = ScapFactory.lookupScapPolicyByIdAndOrg(policyId, user.getOrg());
        assertTrue(policy.isPresent());
        assertEquals("Test Policy Create", policy.get().getPolicyName());
        assertEquals(content.getId(), policy.get().getScapContent().getId());
        assertEquals("xccdf_profile_test", policy.get().getXccdfProfileId());
    }

    @Test
    public void testListTailoringFilesView() throws Exception {
        createTestTailoringFile();
        Request request = getRequestWithCsrf("/manager/audit/scap/tailoring-files");

        ModelAndView mv = controller.listTailoringFilesView(request, response, user);

        assertEquals("templates/audit/list-tailoring-files.jade", mv.getViewName());
        Map<String, Object> model = (Map<String, Object>) mv.getModel();
        assertNotNull(model);
        assertTrue(model.containsKey("tailoringFiles"));
        assertTrue(model.get("tailoringFiles").toString().contains("Test Tailoring"));
    }

    @Test
    public void testCreateTailoringFileView() throws Exception {
        Request request = getRequestWithCsrf("/manager/audit/scap/tailoring-file/create");
        ModelAndView mv = controller.createTailoringFileView(request, response, user);
        assertEquals("templates/audit/create-tailoring-file.jade", mv.getViewName());
    }

    @Test
    public void testUpdateTailoringFileView() throws Exception {
        TailoringFile tf = createTestTailoringFile();
        Request request = getRequestWithCsrf("/manager/audit/scap/tailoring-file/edit/:id", tf.getId());

        ModelAndView mv = controller.updateTailoringFileView(request, response, user);

        assertEquals("templates/audit/create-tailoring-file.jade", mv.getViewName());
        Map<String, Object> model = (Map<String, Object>) mv.getModel();
        assertTrue(model.containsKey("tailoringFileDataJson"));
        assertTrue(model.get("tailoringFileDataJson").toString().contains(tf.getName()));
    }

    @Test
    public void testListScapPoliciesView() throws Exception {
        Request request = getRequestWithCsrf("/manager/audit/scap/policies");
        ModelAndView mv = controller.listScapPoliciesView(request, response, user);
        assertEquals("templates/audit/list-scap-policies.jade", mv.getViewName());
        Map<String, Object> model = (Map<String, Object>) mv.getModel();
        assertNotNull(model.get("scapPolicies"));
    }

    @Test
    public void testCreateScapPolicyView() throws Exception {
        Request request = getRequestWithCsrf("/manager/audit/scap/policy/create");
        ModelAndView mv = controller.createScapPolicyView(request, response, user);
        assertEquals("templates/audit/create-scap-policy.jade", mv.getViewName());
        Map<String, Object> model = (Map<String, Object>) mv.getModel();
        assertNotNull(model.get("scapPolicyPageDataJson"));
    }

    @Test
    public void testEditScapPolicyView() throws Exception {
        ScapContent content = createTestScapContent();
        ScapPolicy policy = new ScapPolicy();
        policy.setPolicyName("Edit Policy");
        policy.setScapContent(content);
        policy.setXccdfProfileId("profile");
        policy.setOrg(user.getOrg());
        ScapFactory.saveScapPolicy(policy);

        Request request = getRequestWithCsrf("/manager/audit/scap/policy/edit/:id", policy.getId());
        ModelAndView mv = controller.editScapPolicyView(request, response, user);

        assertEquals("templates/audit/create-scap-policy.jade", mv.getViewName());
        Map<String, Object> model = (Map<String, Object>) mv.getModel();
        assertTrue(model.get("scapPolicyPageDataJson").toString().contains("Edit Policy"));
    }

    @Test
    public void testDetailScapPolicyView() throws Exception {
        ScapContent content = createTestScapContent();
        ScapPolicy policy = new ScapPolicy();
        policy.setPolicyName("Detail Policy");
        policy.setScapContent(content);
        policy.setXccdfProfileId("profile");
        policy.setOrg(user.getOrg());
        ScapFactory.saveScapPolicy(policy);

        Request request = getRequestWithCsrf("/manager/audit/scap/policy/details/:id", policy.getId());
        ModelAndView mv = controller.detailScapPolicyView(request, response, user);

        assertEquals("templates/scap-policy-details.jade", mv.getViewName());
        Map<String, Object> model = (Map<String, Object>) mv.getModel();
        assertTrue(model.get("scapPolicyPageDataJson").toString().contains("Detail Policy"));
    }

    @Test
    public void testListScapContentView() throws Exception {
        Request request = getRequestWithCsrf("/manager/audit/scap/content");
        ModelAndView mv = controller.listScapContentView(request, response, user);
        assertEquals("templates/audit/list-scap-content.jade", mv.getViewName());
    }

    @Test
    public void testCreateScapContentView() throws Exception {
        Request request = getRequestWithCsrf("/manager/audit/scap/content/create");
        ModelAndView mv = controller.createScapContentView(request, response, user);
        assertEquals("templates/audit/create-scap-content.jade", mv.getViewName());
    }

    @Test
    public void testUpdateScapContentView() throws Exception {
        ScapContent content = createTestScapContent();
        Request request = getRequestWithCsrf("/manager/audit/scap/content/edit/:id", content.getId());
        ModelAndView mv = controller.updateScapContentView(request, response, user);
        assertEquals("templates/audit/create-scap-content.jade", mv.getViewName());
    }

    @Test
    public void testGetProfileList() throws Exception {
        ScapContent content = createTestScapContent();
        Request request = getRequestWithCsrf("/manager/api/audit/profiles/list/:type/:id", "content", content.getId());
        String responseStr = controller.getProfileList(request, response, user);
        ResultJson<?> result = GSON.fromJson(responseStr, ResultJson.class);
        assertTrue(result.isSuccess());
    }

    @Test
    public void testGetCustomRemediationSuccess() throws Exception {
        createTestXccdfRuleFix("test_bench", "test_rule");

        ScapFactory.saveCustomRemediation("test_rule", "test_bench",
                ScriptType.BASH, "#!/bin/bash\necho 'hi'",
                user.getOrg(), user);

        Request request = getRequestWithCsrf("/manager/api/audit/scap/custom-remediation/:identifier/:benchmarkId",
                "test_rule", "test_bench");

        String responseStr = controller.getCustomRemediation(request, response, user);
        ResultJson<?> result = GSON.fromJson(responseStr, ResultJson.class);

        assertTrue(result.isSuccess());
    }

    @Test
    public void testDeleteCustomRemediationSuccess() throws Exception {
        createTestXccdfRuleFix("del_bench", "del_rule");

        ScapFactory.saveCustomRemediation("del_rule", "del_bench",
                ScriptType.BASH, "#!/bin/bash\necho 'del'",
                user.getOrg(), user);

        Request request = getDeleteRequestWithCsrf(
                "/manager/api/audit/scap/custom-remediation/:identifier/:benchmarkId/:scriptType",
                "del_rule", "del_bench", "bash");

        String responseStr = controller.deleteCustomRemediation(request, response, user);
        ResultJson<?> result = GSON.fromJson(responseStr, ResultJson.class);

        assertTrue(result.isSuccess());
        Optional<XccdfRuleFixCustom> deleted = ScapFactory.lookupCustomRemediationByIdentifier(
                "del_rule", "del_bench", user.getOrg());
        assertFalse(deleted.isPresent());
    }

    private Request getDeleteRequestWithCsrf(String uri, Object... vars) throws Exception {
        Request request = SparkTestUtils.createDeleteMockRequestWithBody(uri, Collections.emptyMap(), "", vars);
        request.session(true).attribute("csrf_token", "bleh");
        return request;
    }

    @Test
    public void testScheduleAuditScanSuccess() throws Exception {
        ScapContent content = createTestScapContent();
        Map<String, Object> json = new HashMap<>();
        json.put("ids", Collections.singleton(1L));
        json.put("scapContentId", content.getId());
        json.put("xccdfProfileId", "profile");

        String body = GSON.toJson(json);
        Request request = getPostRequestWithCsrfAndBody("/manager/api/audit/schedule/create", body);

        String responseStr = controller.scheduleAuditScan(request, response, user);
        ResultJson<?> result = GSON.fromJson(responseStr, ResultJson.class);
        assertNotNull(result);
    }

    @Test
    public void testGetPolicyScanHistory() throws Exception {
        ScapContent content = createTestScapContent();
        ScapPolicy policy = new ScapPolicy();
        policy.setPolicyName("History Policy");
        policy.setScapContent(content);
        policy.setXccdfProfileId("profile");
        policy.setOrg(user.getOrg());
        ScapFactory.saveScapPolicy(policy);

        Request request = getRequestWithCsrf("/manager/api/audit/scap/policy/:id/scan-history", policy.getId());
        String responseStr = controller.getPolicyScanHistory(request, response, user);
        ResultJson<?> result = GSON.fromJson(responseStr, ResultJson.class);
        assertTrue(result.isSuccess());
    }

    @Test
    public void testApplyRemediationSuccess() throws Exception {
        ApplyRemediationJson json = new ApplyRemediationJson();
        json.setServerId(1L);
        json.setScriptType("bash");
        json.setRemediationContent("#!/bin/bash\ntrue");

        String body = GSON.toJson(json);
        Request request = getPostRequestWithCsrfAndBody("/manager/api/audit/scap/scan/rule-apply-remediation", body);

        String responseStr = controller.applyRemediation(request, response, user);
        ResultJson<?> result = GSON.fromJson(responseStr, ResultJson.class);
        assertNotNull(result);
    }

    @Test
    public void testUpdateScapPolicy() throws Exception {
        // Setup existing policy
        ScapContent content = createTestScapContent();
        ScapPolicy policy = new ScapPolicy();
        policy.setPolicyName("Original Name");
        policy.setScapContent(content);
        policy.setXccdfProfileId("profile_1");
        policy.setOrg(user.getOrg());
        ScapFactory.saveScapPolicy(policy);

        // Update request
        ScapPolicyJson json = new ScapPolicyJson();
        json.setId(policy.getId());
        json.setPolicyName("Updated Name");
        json.setScapContentId(content.getId());
        json.setXccdfProfileId("profile_2");
        json.setDescription("Updated Desc");

        String body = GSON.toJson(json);
        Request request = getPostRequestWithCsrfAndBody("/manager/api/audit/scap/policy/update", body);

        String responseStr = controller.updateScapPolicy(request, response, user);
        ResultJson<?> result = GSON.fromJson(responseStr, ResultJson.class);

        assertTrue(result.isSuccess());

        // Verify update
        Optional<ScapPolicy> updatedPolicy = ScapFactory.lookupScapPolicyByIdAndOrg(policy.getId(), user.getOrg());
        assertTrue(updatedPolicy.isPresent());
        assertEquals("Updated Name", updatedPolicy.get().getPolicyName());
        assertEquals("profile_2", updatedPolicy.get().getXccdfProfileId());
    }

    @Test
    public void testDeleteScapPolicy() throws Exception {
        // Setup existing policy
        ScapContent content = createTestScapContent();
        ScapPolicy policy = new ScapPolicy();
        policy.setPolicyName("Delete Me");
        policy.setScapContent(content);
        policy.setXccdfProfileId("profile_del");
        policy.setOrg(user.getOrg());
        ScapFactory.saveScapPolicy(policy);
        Integer policyId = policy.getId();

        // Delete request (IDs array)
        Integer[] ids = { policyId };
        String body = GSON.toJson(ids);
        Request request = getPostRequestWithCsrfAndBody("/manager/api/audit/scap/policy/delete", body);

        String responseStr = controller.deleteScapPolicy(request, response, user);
        ResultJson<?> result = GSON.fromJson(responseStr, ResultJson.class);

        assertTrue(result.isSuccess());

        // Verify deletion
        Optional<ScapPolicy> deletedPolicy = ScapFactory.lookupScapPolicyByIdAndOrg(policyId, user.getOrg());
        assertFalse(deletedPolicy.isPresent());
    }

    @Test
    public void testCreateScapPolicyValidationMismatch() throws Exception {
        // Missing Content ID
        ScapPolicyJson json = new ScapPolicyJson();
        json.setPolicyName("Invalid Policy");
        json.setXccdfProfileId("profile");

        String body = GSON.toJson(json);
        Request request = getPostRequestWithCsrfAndBody("/manager/api/audit/scap/policy/create", body);

        String responseStr = controller.createScapPolicy(request, response, user);
        ResultJson<?> result = GSON.fromJson(responseStr, ResultJson.class);

        assertFalse(result.isSuccess());
        // Validation error expected
    }

    @Test
    public void testSaveCustomRemediationSuccess() throws Exception {
        CustomRemediationJson json = new CustomRemediationJson();
        json.setIdentifier("xccdf_rule_save_test");
        json.setBenchmarkId("xccdf_benchmark_save");
        json.setScriptType("bash");
        json.setRemediation("#!/bin/bash\necho 'remediation'");

        String body = GSON.toJson(json);
        Request request = getPostRequestWithCsrfAndBody(
                "/manager/api/audit/scap/custom-remediation", body);

        // Mock lookup to find rule
        createTestXccdfRuleFix("xccdf_benchmark_save", "xccdf_rule_save_test");

        String responseStr = controller.saveCustomRemediation(request, response, user);
        ResultJson<?> result = GSON.fromJson(responseStr, ResultJson.class);

        assertTrue(result.isSuccess());
        assertEquals("Custom remediation saved successfully", result.getData());

        // Verify saved in DB
        Optional<XccdfRuleFixCustom> saved = ScapFactory.lookupCustomRemediationByIdentifier(
                json.getIdentifier(), json.getBenchmarkId(), user.getOrg());
        assertTrue(saved.isPresent());
        assertEquals(json.getRemediation(), saved.get().getCustomRemediationBash());
    }

    @Test
    public void testSaveCustomRemediationMissingIdentifier() throws Exception {
        CustomRemediationJson json = new CustomRemediationJson();
        json.setBenchmarkId("xccdf_benchmark");
        json.setScriptType("bash");
        json.setRemediation("#!/bin/bash\necho 'test'");

        String body = GSON.toJson(json);
        Request request = getPostRequestWithCsrfAndBody(
                "/manager/api/audit/scap/custom-remediation", body);

        String responseStr = controller.saveCustomRemediation(request, response, user);

        assertEquals(HttpStatus.SC_BAD_REQUEST, response.raw().getStatus());
        ResultJson<?> result = GSON.fromJson(responseStr, ResultJson.class);
        assertFalse(result.isSuccess());
        assertTrue(result.getData().toString().contains("Rule identifier is required"));
    }

    @Test
    public void testSaveCustomRemediationMissingBenchmarkId() throws Exception {
        CustomRemediationJson json = new CustomRemediationJson();
        json.setIdentifier("xccdf_rule");
        json.setScriptType("bash");
        json.setRemediation("#!/bin/bash\necho 'test'");

        String body = GSON.toJson(json);
        Request request = getPostRequestWithCsrfAndBody(
                "/manager/api/audit/scap/custom-remediation", body);

        String responseStr = controller.saveCustomRemediation(request, response, user);

        assertEquals(HttpStatus.SC_BAD_REQUEST, response.raw().getStatus());
        ResultJson<?> result = GSON.fromJson(responseStr, ResultJson.class);
        assertFalse(result.isSuccess());
        assertTrue(result.getData().toString().contains("Benchmark ID is required"));
    }

    @Test
    public void testSaveCustomRemediationInvalidScriptType() throws Exception {
        CustomRemediationJson json = new CustomRemediationJson();
        json.setIdentifier("xccdf_rule");
        json.setBenchmarkId("xccdf_benchmark");
        json.setScriptType("invalid_type");
        json.setRemediation("#!/bin/bash\necho 'test'");

        String body = GSON.toJson(json);
        Request request = getPostRequestWithCsrfAndBody(
                "/manager/api/audit/scap/custom-remediation", body);

        String responseStr = controller.saveCustomRemediation(request, response, user);

        assertEquals(HttpStatus.SC_BAD_REQUEST, response.raw().getStatus());
        ResultJson<?> result = GSON.fromJson(responseStr, ResultJson.class);
        assertFalse(result.isSuccess());
        assertTrue(result.getData().toString().contains("Script type must be 'bash' or 'salt'"));
    }

    @Test
    public void testSaveCustomRemediationEmptyContent() throws Exception {
        CustomRemediationJson json = new CustomRemediationJson();
        json.setIdentifier("xccdf_rule");
        json.setBenchmarkId("xccdf_benchmark");
        json.setScriptType("bash");
        json.setRemediation("");

        String body = GSON.toJson(json);
        Request request = getPostRequestWithCsrfAndBody(
                "/manager/api/audit/scap/custom-remediation", body);

        String responseStr = controller.saveCustomRemediation(request, response, user);

        assertEquals(HttpStatus.SC_BAD_REQUEST, response.raw().getStatus());
        ResultJson<?> result = GSON.fromJson(responseStr, ResultJson.class);
        assertFalse(result.isSuccess());
        assertTrue(result.getData().toString().contains("Remediation content is required"));
    }

    // ========== Apply Remediation Helper Method Tests ==========

    @Test
    public void testValidateRemediationBodySuccess() throws Exception {
        ApplyRemediationJson body = new ApplyRemediationJson();
        body.setRemediationContent("#!/bin/bash\necho 'test'");
        body.setScriptType("bash");

        String error = controller.validateRemediationBody(body);
        assertNull(error, "Validation should pass for valid body");
    }

    @Test
    public void testValidateRemediationBodyEmptyContent() throws Exception {
        ApplyRemediationJson body = new ApplyRemediationJson();
        body.setRemediationContent("");
        body.setScriptType("bash");

        String error = controller.validateRemediationBody(body);
        assertNotNull(error);
        assertTrue(error.contains("Remediation content cannot be empty"));
    }

    @Test
    public void testValidateRemediationBodyNullContent() throws Exception {
        ApplyRemediationJson body = new ApplyRemediationJson();
        body.setRemediationContent(null);
        body.setScriptType("bash");

        String error = controller.validateRemediationBody(body);
        assertNotNull(error);
        assertTrue(error.contains("Remediation content cannot be empty"));
    }

    @Test
    public void testValidateRemediationBodyInvalidScriptType() throws Exception {
        ApplyRemediationJson body = new ApplyRemediationJson();
        body.setRemediationContent("#!/bin/bash\necho 'test'");
        body.setScriptType("invalid");

        String error = controller.validateRemediationBody(body);
        assertNotNull(error);
        assertTrue(error.contains("Invalid script type"));
    }

    @Test
    public void testValidateRemediationBodyNullScriptType() throws Exception {
        ApplyRemediationJson body = new ApplyRemediationJson();
        body.setRemediationContent("#!/bin/bash\necho 'test'");
        body.setScriptType(null);

        String error = controller.validateRemediationBody(body);
        assertNotNull(error);
        assertTrue(error.contains("Invalid script type"));
    }
}
