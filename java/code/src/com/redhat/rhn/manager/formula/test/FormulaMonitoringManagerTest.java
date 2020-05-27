/**
 * Copyright (c) 2020 SUSE LLC
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

import com.redhat.rhn.domain.formula.FormulaFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.server.test.ServerGroupTest;
import com.redhat.rhn.manager.formula.FormulaMonitoringManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Test for {@link com.redhat.rhn.manager.formula.FormulaManager}.
 */
public class FormulaMonitoringManagerTest extends BaseTestCaseWithUser {

    static final String TEMP_PATH = "formulas/";
    private FormulaMonitoringManager manager = new FormulaMonitoringManager();
    private Path metadataDir;

    public FormulaMonitoringManagerTest() { }

    @Override
    public void setUp() throws Exception {
        super.setUp();
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
     * Test the conditions in FormulaMonitoringManager.isMonitoringCleanupNeeded().
     * @throws Exception
     */
    public void testIsMonitoringCleanupNeeded() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        FormulaFactory.setDataDir(tmpSaltRoot.resolve(TEMP_PATH).toString());
        FormulaFactory.setMetadataDirOfficial(metadataDir.toString() + File.separator);

        // No group or system level assignment of the `prometheus-exporters` Formula
        assertFalse(manager.isMonitoringCleanupNeeded(minion));

        // Create a group level assignment of the Formula
        ServerGroup group = ServerGroupTest.createTestServerGroup(user.getOrg(), null);
        SystemManager.addServerToServerGroup(minion, group);
        FormulaFactory.saveGroupFormulas(group.getId(), Arrays.asList(PROMETHEUS_EXPORTERS), user.getOrg());

        // Save data that enables monitoring
        Map<String, Object> formulaData = new HashMap<>();
        formulaData.put("node_exporter", Collections.singletonMap("enabled", true));
        formulaData.put("postgres_exporter", Collections.singletonMap("enabled", false));
        formulaData.put("apache_exporter", Collections.singletonMap("enabled", false));
        FormulaFactory.saveGroupFormulaData(formulaData, group.getId(), user.getOrg(), PROMETHEUS_EXPORTERS);
        assertTrue(manager.isMonitoringCleanupNeeded(minion));

        // Save data that disables monitoring
        formulaData.put("node_exporter", Collections.singletonMap("enabled", false));
        formulaData.put("postgres_exporter", Collections.singletonMap("enabled", false));
        formulaData.put("apache_exporter", Collections.singletonMap("enabled", false));
        FormulaFactory.saveGroupFormulaData(formulaData, group.getId(), user.getOrg(), PROMETHEUS_EXPORTERS);
        assertFalse(manager.isMonitoringCleanupNeeded(minion));

        // Create a system level assignment of the Formula
        FormulaFactory.saveServerFormulas(minion.getMinionId(), Arrays.asList(PROMETHEUS_EXPORTERS));
        FormulaFactory.saveServerFormulaData(formulaData, minion.getMinionId(), PROMETHEUS_EXPORTERS);
        assertTrue(manager.isMonitoringCleanupNeeded(minion));
    }

    // Copy the pillar.example file to a temp dir used as metadata directory (in FormulaFactory)
    private void createMetadataFiles() {
        try {
            Path prometheusDir = metadataDir.resolve("prometheus-exporters");
            Path prometheusFile = Paths.get(prometheusDir.toString(),  "form.yml");
            Files.createDirectories(prometheusDir);
            Files.createFile(prometheusFile);

            try (
                InputStream src = this.getClass().getResourceAsStream("prometheus-exporters-pillar.example");
                OutputStream dst = new FileOutputStream(prometheusDir.resolve("pillar.example").toFile())
            ) {
                IOUtils.copy(src, dst);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}
