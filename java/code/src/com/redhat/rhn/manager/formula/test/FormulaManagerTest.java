/*
 * Copyright (c) 2018--2021 SUSE LLC
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
package com.redhat.rhn.manager.formula.test;

import static com.redhat.rhn.domain.formula.FormulaFactory.PROMETHEUS_EXPORTERS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.dto.EndpointInfo;
import com.redhat.rhn.domain.dto.FormulaData;
import com.redhat.rhn.domain.formula.FormulaFactory;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.server.test.ServerGroupTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.formula.FormulaManager;
import com.redhat.rhn.manager.formula.InvalidFormulaException;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.ServerGroupTestUtils;
import com.redhat.rhn.testing.TestStatics;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.manager.webui.services.impl.SaltService;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.utils.Json;

import org.apache.commons.io.FileUtils;
import org.cobbler.test.MockConnection;
import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Test for {@link com.redhat.rhn.manager.formula.FormulaManager}.
 */
public class FormulaManagerTest extends JMockBaseTestCaseWithUser {

    static final String FORMULA_DATA = "dhcpd-formula-data.json";
    static final String FORMULA_DEFINITION = "dhcpd-formula-form.json";
    static final String PROMETHEUS_EXPORTERS_FORMULA_DATA = "prometheus-exporters-formula-data.json";

    static final String TEMP_PATH = "formulas/";
    static final String FORMULA_NAME = "dhcpd";
    private SaltService saltServiceMock;
    private FormulaManager manager;
    private Path metadataDir;

    public FormulaManagerTest() { }

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        MockConnection.clear();
        saltServiceMock = mock(SaltService.class);
        metadataDir = Files.createTempDirectory("metadata");
        manager = new FormulaManager(saltServiceMock);
        FormulaFactory.setMetadataDirOfficial(metadataDir.toString());
        createMetadataFiles();
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
        FileUtils.deleteDirectory(metadataDir.toFile());
    }

    /**
     * Validate the input data(valid) with the definition of formula
     *
     * @throws Exception - if anything goes wrong
     */

    @Test
    public void testValidContents() throws Exception {

        String contentsData = TestUtils.readAll(TestUtils.findTestData(FORMULA_DATA));
        String layoutData = TestUtils.readAll(TestUtils.findTestData(FORMULA_DEFINITION));
        Map<String, Object> contents = Json.GSON.fromJson(contentsData, Map.class);

        Map<String, Object> layout = Json.GSON.fromJson(layoutData, Map.class);
        FormulaManager formulaManager = new FormulaManager(saltServiceMock);
        formulaManager.validateContents(contents, layout);

    }

    /**
     * Validate the input data(invalid) with the definition of formula
     * @throws Exception
     */
    @Test
    public void testInValidContents() throws Exception {

        String contentsData = TestUtils.readAll(TestUtils.findTestData(FORMULA_DATA));
        String layoutData = TestUtils.readAll(TestUtils.findTestData(FORMULA_DEFINITION));
        Map<String, Object> contents = Json.GSON.fromJson(contentsData, Map.class);
        Map<String, Object> layout = Json.GSON.fromJson(layoutData, Map.class);

        contents.put("test", "dummy"); // add a random field

        FormulaManager formulaManager = new FormulaManager(saltServiceMock);
        try {
            formulaManager.validateContents(contents, layout);
            fail("Exception expected but didn't throw");
        }
        catch (InvalidFormulaException ex) {

        }
    }

    /**
     * Test the saved group formula data
     * @throws Exception
     */
    @Test
    public void testSaveGroupFormulaData() throws Exception {
        String contentsData = TestUtils.readAll(TestUtils.findTestData(FORMULA_DATA));
        Map<String, Object> contents = Json.GSON.fromJson(contentsData, Map.class);
        ManagedServerGroup managed = ServerGroupTestUtils.createManaged(user);

        context().checking(new Expectations() {{
            allowing(saltServiceMock).refreshPillar(with(any(MinionList.class)));
        }});
        manager.saveGroupFormulaData(user, managed.getId(), FORMULA_NAME, contents);
        Map<String, Object> savedFormulaData =
                FormulaFactory.getGroupFormulaValuesByNameAndGroup(FORMULA_NAME, managed)
                        .orElseGet(Collections::emptyMap);
        assertNotNull(savedFormulaData);
        assertEquals(contents, savedFormulaData);
    }

    /**
     * Test the enable formula method
     * @throws Exception if the formula cannot be enabled
     */
    @Test
    public void testEnableFormula() throws Exception {
        String contentsData = TestUtils.readAll(TestUtils.findTestData(FORMULA_DATA));
        Map<String, Object> contents = Json.GSON.fromJson(contentsData, Map.class);
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        FormulaFactory.setMetadataDirOfficial(metadataDir.toString());

        context().checking(new Expectations() {{
            allowing(saltServiceMock).refreshPillar(with(any(MinionList.class)));
        }});
        manager.enableFormula(minion, FORMULA_NAME);
        List<String> enabledFormulas = FormulaFactory.getFormulasByMinion(minion);
        assertNotNull(enabledFormulas);
        assertTrue(enabledFormulas.contains(FORMULA_NAME));
    }

    /**
     * Test the saved server formula data
     * @throws Exception
     */
    @Test
    public void testSaveServerFormulaData() throws Exception {

        String contentsData = TestUtils.readAll(TestUtils.findTestData(FORMULA_DATA));
        Map<String, Object> contents = Json.GSON.fromJson(contentsData, Map.class);
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        context().checking(new Expectations() {{
            allowing(saltServiceMock).refreshPillar(with(any(MinionList.class)));
        }});
        manager.saveServerFormulaData(user, minion.getId(), FORMULA_NAME, contents);
        Map<String, Object> savedFormulaData =
                FormulaFactory.getFormulaValuesByNameAndMinion(FORMULA_NAME, minion)
                        .orElseGet(Collections::emptyMap);
        assertNotNull(savedFormulaData);
        assertEquals(contents, savedFormulaData);
        assertTrue(savedFormulaData.equals(contents));
    }

    /**
     * Test the saving of server formula data when group formula is already assigned
     * When saving server data to group assigned formula, make sure server formula is assigned
     * @throws Exception
     */
    @Test
    public void testSaveServerFormulaDataForGroupFormula() throws Exception {
        String contentsData = TestUtils.readAll(TestUtils.findTestData(FORMULA_DATA));
        Map<String, Object> contents = Json.GSON.fromJson(contentsData, Map.class);
        ManagedServerGroup group = ServerGroupTestUtils.createManaged(user);
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        minion.addGroup(group);
        context().checking(new Expectations() {{
            allowing(saltServiceMock).refreshPillar(with(any(MinionList.class)));
        }});
        manager.saveGroupFormulaData(user, group.getId(), FORMULA_NAME, contents);
        Map<String, Object> savedFormulaData =
                FormulaFactory.getGroupFormulaValuesByNameAndGroup(FORMULA_NAME, group)
                        .orElseGet(Collections::emptyMap);
        assertNotNull(savedFormulaData);
        assertEquals(contents, savedFormulaData);

        List<String> formulasServer = FormulaFactory.getFormulasByMinion(minion);
        assertTrue(formulasServer.isEmpty());

        Map<String, Object> contentsServer = Json.GSON.fromJson(contentsData, Map.class);
        ((Map<String, Object>)contentsServer.get(FORMULA_NAME)).replace("domain_name", "server_domain_test");
        manager.saveServerFormulaData(user, minion.getId(), FORMULA_NAME, contentsServer);
        Map<String, Object> savedFormulaSystemData =
                FormulaFactory.getFormulaValuesByNameAndMinion(FORMULA_NAME, minion)
                        .orElseGet(Collections::emptyMap);
        assertNotNull(savedFormulaData);
        assertEquals(contentsServer, savedFormulaSystemData);

        List<String> formulasServerNew = FormulaFactory.getFormulasByMinion(minion);
        assertTrue(formulasServerNew.contains(FORMULA_NAME));
    }

    /**
     * Test if unauthorized user can save formula data
     * @throws Exception
     */
    @Test
    public void testSaveServerFormulaDataForUnAuthorized() throws Exception {
        String contentsData = TestUtils.readAll(TestUtils.findTestData(FORMULA_DATA));
        Map<String, Object> contents = Json.GSON.fromJson(contentsData, Map.class);
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        FormulaManager formulaManager = new FormulaManager(saltServiceMock);
        User testUser = UserTestUtils.createUser("test-user", user.getOrg().getId());
        try {
            formulaManager.saveServerFormulaData(testUser, minion.getId(), FORMULA_NAME, contents);
            fail("Exception expected but didn't throw");
        }
        catch (PermissionException ex) {
            //expected exception
        }
    }

    @Test
    public void testGetCombinedFormulaDataForSystems() throws Exception {
        // minion with only group formulas
        User user = UserTestUtils.findNewUser(TestStatics.TESTUSER, TestStatics.TESTORG);
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        minion.setServerArch(ServerFactory.lookupServerArchByLabel("x86_64-redhat-linux"));
        assertFalse(SystemManager.hasEntitlement(minion.getId(), EntitlementManager.MONITORING));

        ServerGroup group = ServerGroupTest.createTestServerGroup(user.getOrg(), null);
        FormulaFactory.saveGroupFormulas(group, Arrays.asList(PROMETHEUS_EXPORTERS));

        Map<String, Object> formulaData = new HashMap<>();
        Map<String, Object> exportersData = new HashMap<>();
        exportersData.put("node_exporter", Collections.singletonMap("enabled", true));
        exportersData.put("apache_exporter", Collections.singletonMap("enabled", false));
        exportersData.put("postgres_exporter", Collections.singletonMap("enabled", false));
        formulaData.put("exporters", exportersData);

        FormulaFactory.saveGroupFormulaData(formulaData, group, PROMETHEUS_EXPORTERS);

        // Server should have a monitoring entitlement after being added to the group
        context().checking(new Expectations() {{
            allowing(saltServiceMock).refreshPillar(with(any(MinionList.class)));
        }});
        SystemManager systemManager = new SystemManager(ServerFactory.SINGLETON, ServerGroupFactory.SINGLETON,
                saltServiceMock);
        systemManager.addServerToServerGroup(minion, group);
        assertTrue(SystemManager.hasEntitlement(minion.getId(), EntitlementManager.MONITORING));

        List<FormulaData> combinedPrometheusExportersFormulas = this.manager
                .getCombinedFormulaDataForSystems(user, Arrays.asList(minion.getId()), PROMETHEUS_EXPORTERS);

        assertNotNull(combinedPrometheusExportersFormulas);
        assertEquals(combinedPrometheusExportersFormulas.size(), 1);

        FormulaData combinedFormulaData = combinedPrometheusExportersFormulas.get(0);

        assertEquals(combinedFormulaData.getSystemID(), minion.getId());
        assertEquals(combinedFormulaData.getMinionID(), minion.getMinionId());
        assertNotNull(combinedFormulaData.getFormulaValues().get("exporters"));
        exportersData = (Map<String, Object>) combinedFormulaData.getFormulaValues().get("exporters");
        assertNotNull(exportersData.get("postgres_exporter"));
        assertNotNull(exportersData.get("apache_exporter"));
        assertNotNull(exportersData.get("node_exporter"));

        // minion with only system formulas with no group formula
        minion = MinionServerFactoryTest.createTestMinionServer(user);

        String contentsData = TestUtils.readAll(TestUtils.findTestData(FORMULA_DATA));
        Map<String, Object> contents = Json.GSON.fromJson(contentsData, Map.class);

        manager.saveServerFormulaData(user, minion.getId(), FORMULA_NAME, contents);

        combinedPrometheusExportersFormulas = this.manager
                .getCombinedFormulaDataForSystems(user, Arrays.asList(minion.getId()), FORMULA_NAME);

        assertNotNull(combinedPrometheusExportersFormulas);
        assertEquals(combinedPrometheusExportersFormulas.size(), 1);

        combinedFormulaData = combinedPrometheusExportersFormulas.get(0);

        assertEquals(combinedFormulaData.getSystemID(), minion.getId());
        assertEquals(combinedFormulaData.getMinionID(), minion.getMinionId());
        assertNotNull(combinedFormulaData.getFormulaValues().get(FORMULA_NAME));
    }

    @Test
    public void testListEndpoints() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        String formulaValues = TestUtils.readAll(TestUtils.findTestData(PROMETHEUS_EXPORTERS_FORMULA_DATA));
        Map<String, Object> formulaValuesMap = Json.GSON.fromJson(formulaValues, Map.class);
        context().checking(new Expectations() {{
            allowing(saltServiceMock).refreshPillar(with(any(MinionList.class)));
        }});
        FormulaFactory.saveServerFormulas(minion, Collections.singletonList(PROMETHEUS_EXPORTERS));
        manager.saveServerFormulaData(user, minion.getId(), PROMETHEUS_EXPORTERS, formulaValuesMap);

        List<EndpointInfo> endpoints = manager.listEndpoints(Collections.singletonList(minion.getId()));

        assertNotNull(endpoints);
        assertEquals(endpoints.size(), 2);
        EndpointInfo nodeExporter = endpoints.stream()
                .filter(e -> e.getExporterName().get().equals("node_exporter")).findFirst().get();
        EndpointInfo apacheExporter = endpoints.stream()
                .filter(e -> e.getExporterName().get().equals("apache_exporter")).findFirst().get();
        assertEquals(nodeExporter.getSystemID(), minion.getId());
        assertEquals(nodeExporter.getEndpointName(), "node_exporter");
        assertNull(nodeExporter.getPath());
        assertNull(nodeExporter.getModule());
        assertFalse(nodeExporter.isTlsEnabled());
        assertEquals(nodeExporter.getPort(), Integer.valueOf(9100));
        assertEquals(apacheExporter.getPort(), Integer.valueOf(9117));
    }

    // Copy the pillar.example file to a temp dir used as metadata directory (in FormulaFactory)
    private void createMetadataFiles() {
        try {
            Path prometheusDir = metadataDir.resolve("prometheus-exporters");
            Files.createDirectories(prometheusDir);
            Path prometheusFile = Paths.get(prometheusDir.toString(),  "form.yml");
            Files.createFile(prometheusFile);

            Path testFormulaDir = metadataDir.resolve("dhcpd");
            Files.createDirectories(testFormulaDir);
            Path testFormulaFile = Paths.get(testFormulaDir.toString(), "form.yml");
            Files.createFile(testFormulaFile);
        }
        catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}
