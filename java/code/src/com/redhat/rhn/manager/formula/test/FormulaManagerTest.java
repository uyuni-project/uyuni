/**
 * Copyright (c) 2018 SUSE LLC
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

import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.dto.FormulaData;
import com.redhat.rhn.domain.formula.FormulaFactory;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroup;
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

    static final String TEMP_PATH = "formulas/";
    static final String formulaName = "dhcpd";
    private SaltService saltServiceMock;
    private FormulaManager manager = FormulaManager.getInstance();
    private Path metadataDir;

    public FormulaManagerTest() { }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        MockConnection.clear();
        saltServiceMock = mock(SaltService.class);
        manager.setSystemQuery(saltServiceMock);
        manager.setSaltApi(saltServiceMock);
        metadataDir = Files.createTempDirectory("metadata");
        FormulaFactory.setDataDir(tmpSaltRoot.toString());
        FormulaFactory.setMetadataDirOfficial(metadataDir.toString());
        createMetadataFiles();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        FileUtils.deleteDirectory(metadataDir.toFile());
    }

    /**
     * Validate the input data(valid) with the definition of formula
     *
     * @throws Exception - if anything goes wrong
     */

    public void testValidContents() throws Exception {

        String contentsData = TestUtils.readAll(TestUtils.findTestData(FORMULA_DATA));
        String layoutData = TestUtils.readAll(TestUtils.findTestData(FORMULA_DEFINITION));
        Map<String, Object> contents = Json.GSON.fromJson(contentsData, Map.class);

        Map<String, Object> layout = Json.GSON.fromJson(layoutData, Map.class);
        FormulaManager manager = FormulaManager.getInstance();
        manager.validateContents(contents,layout);

    }

    /**
     * Validate the input data(invalid) with the definition of formula
     * @throws Exception
     */
    public void testInValidContents() throws Exception {

        String contentsData = TestUtils.readAll(TestUtils.findTestData(FORMULA_DATA));
        String layoutData = TestUtils.readAll(TestUtils.findTestData(FORMULA_DEFINITION));
        Map<String, Object> contents = Json.GSON.fromJson(contentsData, Map.class);
        Map<String, Object> layout = Json.GSON.fromJson(layoutData, Map.class);

        contents.put("test","dummy"); // add a random field

        FormulaManager manager = FormulaManager.getInstance();
        try {
            manager.validateContents(contents,layout);
            fail( "Exception expected but didn't throw" );
        } catch (InvalidFormulaException ex) {

        }
    }

    /**
     * Test the saved group formula data
     * @throws Exception
     */
    public void testSaveGroupFormulaData() throws Exception {
        String contentsData = TestUtils.readAll(TestUtils.findTestData(FORMULA_DATA));
        Map<String, Object> contents = Json.GSON.fromJson(contentsData, Map.class);
        ManagedServerGroup managed = ServerGroupTestUtils.createManaged(user);
        FormulaFactory.setDataDir(tmpSaltRoot.resolve(TEMP_PATH).toString());

        context().checking(new Expectations(){{
            allowing(saltServiceMock).refreshPillar(with(any(MinionList.class)));
        }});
        manager.saveGroupFormulaData(user,managed.getId(), formulaName, contents);
        Map<String, Object> savedFormulaData =
                FormulaFactory.getGroupFormulaValuesByNameAndGroupId(formulaName, managed.getId())
                        .orElseGet(Collections::emptyMap);
        assertNotNull(savedFormulaData);
        assertEquals(contents,savedFormulaData);
    }

    /**
     * Test the enable formula method
     * @throws Exception if the formula cannot be enabled
     */
    public void testEnableFormula() throws Exception {
        String contentsData = TestUtils.readAll(TestUtils.findTestData(FORMULA_DATA));
        Map<String, Object> contents = Json.GSON.fromJson(contentsData, Map.class);
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        FormulaFactory.setMetadataDirOfficial(metadataDir.toString());

        context().checking(new Expectations() {{
            allowing(saltServiceMock).refreshPillar(with(any(MinionList.class)));
        }});
        manager.enableFormula(minion.getMinionId(), formulaName);
        List<String> enabledFormulas = FormulaFactory.getFormulasByMinionId(minion.getMinionId());
        assertNotNull(enabledFormulas);
        assertEquals(true, enabledFormulas.contains(formulaName));
    }

    /**
     * Test the saved server formula data
     * @throws Exception
     */
    public void testSaveServerFormulaData() throws Exception {

        String contentsData = TestUtils.readAll(TestUtils.findTestData(FORMULA_DATA));
        Map<String, Object> contents = Json.GSON.fromJson(contentsData, Map.class);
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        FormulaFactory.setDataDir(tmpSaltRoot.resolve(TEMP_PATH).toString());
        context().checking(new Expectations() {{
            allowing(saltServiceMock).refreshPillar(with(any(MinionList.class)));
        }});
        manager.saveServerFormulaData(user,minion.getId(), formulaName, contents);
        Map<String, Object> savedFormulaData =
                FormulaFactory.getFormulaValuesByNameAndMinionId(formulaName, minion.getMinionId())
                        .orElseGet(Collections::emptyMap);
        assertNotNull(savedFormulaData);
        assertEquals(contents,savedFormulaData);
        assertEquals( true, savedFormulaData.equals(contents));
    }

    /**
     * Test if unauthorized user can save formula data
     * @throws Exception
     */
    public void testSaveServerFormulaDataForUnAuthorized() throws Exception {
        String contentsData = TestUtils.readAll(TestUtils.findTestData(FORMULA_DATA));
        Map<String, Object> contents = Json.GSON.fromJson(contentsData, Map.class);
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        FormulaFactory.setDataDir(tmpSaltRoot.resolve(TEMP_PATH).toString());
        FormulaManager manager = FormulaManager.getInstance();
        User testUser = UserTestUtils.createUser("test-user", user.getOrg().getId());
        try {
            manager.saveServerFormulaData(testUser,minion.getId(), formulaName, contents);
            fail( "Exception expected but didn't throw" );
        } catch (PermissionException ex) {
            //expected exception
        }
    }

    public void testGetCombinedFormulaDataForSystems() throws Exception {
        // minion with only group formulas
        User user = UserTestUtils.findNewUser(TestStatics.TESTUSER, TestStatics.TESTORG);
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        minion.setServerArch(ServerFactory.lookupServerArchByLabel("x86_64-redhat-linux"));
        assertFalse(SystemManager.hasEntitlement(minion.getId(), EntitlementManager.MONITORING));

        ServerGroup group = ServerGroupTest.createTestServerGroup(user.getOrg(), null);
        FormulaFactory.saveGroupFormulas(group.getId(), Arrays.asList(PROMETHEUS_EXPORTERS), user.getOrg());

        Map<String, Object> formulaData = new HashMap<>();
        Map<String, Object> exportersData = new HashMap<>();
        exportersData.put("node_exporter", Collections.singletonMap("enabled", true));
        exportersData.put("apache_exporter", Collections.singletonMap("enabled", false));
        exportersData.put("postgres_exporter", Collections.singletonMap("enabled", false));
        formulaData.put("exporters", exportersData);

        FormulaFactory.saveGroupFormulaData(formulaData, group.getId(), user.getOrg(), PROMETHEUS_EXPORTERS);

        // Server should have a monitoring entitlement after being added to the group
        SystemManager.addServerToServerGroup(minion, group);
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

        FormulaFactory.setDataDir(tmpSaltRoot.resolve(TEMP_PATH).toString());
        context().checking(new Expectations() {{
            allowing(saltServiceMock).refreshPillar(with(any(MinionList.class)));
        }});
        manager.saveServerFormulaData(user,minion.getId(), formulaName, contents);

        combinedPrometheusExportersFormulas = this.manager
                .getCombinedFormulaDataForSystems(user, Arrays.asList(minion.getId()), formulaName);

        assertNotNull(combinedPrometheusExportersFormulas);
        assertEquals(combinedPrometheusExportersFormulas.size(), 1);

        combinedFormulaData = combinedPrometheusExportersFormulas.get(0);

        assertEquals(combinedFormulaData.getSystemID(), minion.getId());
        assertEquals(combinedFormulaData.getMinionID(), minion.getMinionId());
        assertNotNull(combinedFormulaData.getFormulaValues().get(formulaName));
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
