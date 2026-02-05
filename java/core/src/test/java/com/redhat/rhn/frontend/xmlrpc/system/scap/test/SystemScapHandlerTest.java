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
package com.redhat.rhn.frontend.xmlrpc.system.scap.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.scap.ScapAction;
import com.redhat.rhn.domain.audit.ScapContent;
import com.redhat.rhn.domain.audit.ScapFactory;
import com.redhat.rhn.domain.audit.ScapPolicy;
import com.redhat.rhn.domain.audit.TailoringFile;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.frontend.xmlrpc.TaskomaticApiException;
import com.redhat.rhn.frontend.xmlrpc.system.scap.SystemScapHandler;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;
import com.redhat.rhn.manager.action.ActionChainManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.jmock.junit5.JUnit5Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ExtendWith(JUnit5Mockery.class)
public class SystemScapHandlerTest extends BaseHandlerTestCase {
    /**
     * Mock context for testing
     */
    @RegisterExtension
    protected final Mockery mockContext = new JUnit5Mockery() {{
        setThreadingPolicy(new Synchroniser());
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
    }};

    private SystemScapHandler handler;
    private TaskomaticApi taskomaticApi;
    /**
     * Setup the test environment
     * @throws Exception
     */
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        handler = new SystemScapHandler();
        taskomaticApi = mockContext.mock(TaskomaticApi.class);
        ActionChainManager.setTaskomaticApi(taskomaticApi);
        admin.setBetaFeaturesEnabled(true);
    }
    /**
     * Test listing SCAP content
     * @throws Exception
     */
    @Test
    public void testListScapContent() {
        // Create test content
        ScapContent content = new ScapContent();
        content.setName("Test Content");
        content.setDataStreamFileName("ds.xml");
        content.setXccdfFileName("xccdf.xml");
        content.setName("test-content");
        ScapFactory.saveScapContent(content);
        List<ScapContent> result = handler.listScapContent(admin);
        assertNotNull(result);
        assertTrue(result.stream().anyMatch(c -> c.getName().equals("Test Content")));
        ScapFactory.deleteScapContent(content);
    }
    /**
     * Test listing SCAP content when beta features are disabled
     * @throws Exception
     */
    @Test
    public void testListScapContentBetaDisabled() {
        admin.setBetaFeaturesEnabled(false);
        assertThrows(TaskomaticApiException.class, () -> handler.listScapContent(admin));
    }
    /**
     * Test listing tailoring files
     * @throws Exception
     */
    @Test
    public void testListTailoringFiles() {
        // Create test tailoring file
        TailoringFile file = new TailoringFile();
        file.setName("Test Tailoring");
        file.setFileName("tailoring.xml");
        file.setOrg(admin.getOrg());
        ScapFactory.saveTailoringFile(file);
        List<TailoringFile> result = handler.listTailoringFiles(admin);
        assertNotNull(result);
        assertTrue(result.stream().anyMatch(f -> f.getName().equals("Test Tailoring")));
        ScapFactory.deleteTailoringFile(file);
    }
    /**
     * Test listing policies
     * @throws Exception
     */
    @Test
    public void testListPolicies() {
        // Create test policy
        ScapPolicy policy = new ScapPolicy();
        policy.setPolicyName("Test Policy");
        policy.setOrg(admin.getOrg());
        ScapContent content = new ScapContent();
        content.setName("policy-content");
        ScapFactory.saveScapContent(content);
        policy.setScapContent(content);
        ScapFactory.saveScapPolicy(policy);
        List<ScapPolicy> result = handler.listPolicies(admin);
        assertNotNull(result);
        assertTrue(result.stream().anyMatch(p -> p.getPolicyName().equals("Test Policy")));
        ScapFactory.deleteScapPolicy(policy);
        ScapFactory.deleteScapContent(content);
    }
    /**
     * Test scheduling a custom SCAP scan
     * @throws Exception
     */
    @Test
    public void testScheduleBetaXccdfScanWithPolicy() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin);
        ScapContent content = new ScapContent();
        content.setDataStreamFileName("ds.xml");
        content.setName("sched-content");
        ScapFactory.saveScapContent(content);
        ScapPolicy policy = new ScapPolicy();
        policy.setPolicyName("Sched Policy");
        policy.setXccdfProfileId("test_profile");
        policy.setOrg(admin.getOrg());
        policy.setScapContent(content);
        ScapFactory.saveScapPolicy(policy);

        mockContext.checking(new Expectations() {{
            oneOf(taskomaticApi).scheduleActionExecution(with(any(Action.class)));
        }});
        Long actionId = handler.scheduleBetaXccdfScanWithPolicy(admin,
                Collections.singletonList(server.getId().intValue()),
                policy.getId().intValue(),
                new Date());
        assertNotNull(actionId);
        Action action = ActionFactory.lookupById(actionId);
        assertNotNull(action);
        assertTrue(action instanceof ScapAction);
        assertEquals("OpenSCAP xccdf scanning", action.getName());
    }

    /**
     * Test scheduling a custom SCAP scan
     * @throws Exception
     */
    @Test
    public void testScheduleBetaXccdfScanCustom() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin);
        ScapContent content = new ScapContent();
        content.setDataStreamFileName("custom_ds.xml");
        content.setName("custom-content");
        ScapFactory.saveScapContent(content);
        Map<String, Object> params = new HashMap<>();
        params.put("scapContentId", content.getId().intValue()); // Assuming API passes Integers/Longs
        params.put("xccdfProfileId", "custom_profile");
        params.put("fetchRemoteResources", true);
        mockContext.checking(new Expectations() {{
             oneOf(taskomaticApi).scheduleActionExecution(with(any(Action.class)));
        }});

        Long actionId = handler.scheduleBetaXccdfScanCustom(admin,
                Collections.singletonList(server.getId().intValue()),
                params,
                new Date());
        assertNotNull(actionId);
        Action action = ActionFactory.lookupById(actionId);
        assertNotNull(action);
        assertTrue(action instanceof ScapAction);
    }
}
