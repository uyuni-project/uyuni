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
package com.suse.manager.webui.services.pillar.test;

import static com.suse.manager.webui.services.SaltConstants.PILLAR_DATA_FILE_EXT;
import static com.suse.manager.webui.services.SaltConstants.PILLAR_DATA_FILE_PREFIX;

import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.formula.FormulaMonitoringManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import com.suse.manager.virtualization.GuestDefinition;
import com.suse.manager.virtualization.PoolCapabilitiesJson;
import com.suse.manager.virtualization.PoolDefinition;
import com.suse.manager.webui.services.iface.VirtManager;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.services.pillar.MinionPillarFileManager;
import com.suse.manager.webui.services.pillar.MinionVirtualizationPillarGenerator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

/**
 * Tests for {@link MinionVirtualizationPillarGenerator}
 */
public class MinionVirtualizationPillarGeneratorTest extends BaseTestCaseWithUser {

    private SystemEntitlementManager systemEntitlementManager;

    protected MinionPillarFileManager minionVirtualizationPillarFileManager =
            new MinionPillarFileManager(new MinionVirtualizationPillarGenerator());

    @Override
    public void setUp() throws Exception {
        super.setUp();
        minionVirtualizationPillarFileManager.setPillarDataPath(tmpPillarRoot.toAbsolutePath());

        VirtManager virtManager = new VirtManager() {
            @Override
            public Optional<GuestDefinition> getGuestDefinition(String minionId, String domainName) {
                return Optional.empty();
            }

            @Override
            public Optional<Map<String, JsonElement>> getCapabilities(String minionId) {
                return Optional.empty();
            }

            @Override
            public Optional<PoolCapabilitiesJson> getPoolCapabilities(String minionId) {
                return Optional.empty();
            }

            @Override
            public Optional<PoolDefinition> getPoolDefinition(String minionId, String poolName) {
                return Optional.empty();
            }

            @Override
            public Map<String, JsonObject> getNetworks(String minionId) {
                return null;
            }

            @Override
            public Map<String, JsonObject> getPools(String minionId) {
                return null;
            }

            @Override
            public Map<String, Map<String, JsonObject>> getVolumes(String minionId) {
                return null;
            }

            @Override
            public void updateLibvirtEngine(MinionServer minion) {
            }
        };

        systemEntitlementManager = new SystemEntitlementManager(
                new SystemUnentitler(virtManager, new FormulaMonitoringManager()),
                new SystemEntitler(new SaltService(), virtManager, new FormulaMonitoringManager())
        );
    }

    public void testGenerateVirtualizationPillarDataVirt() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        systemEntitlementManager.addEntitlementToServer(minion, EntitlementManager.VIRTUALIZATION);

        this.minionVirtualizationPillarFileManager.updatePillarFile(minion);

        Path filePath = tmpPillarRoot.resolve(PILLAR_DATA_FILE_PREFIX + "_" +
                minion.getMinionId() + "_" + "virtualization" + "." +
                PILLAR_DATA_FILE_EXT);

        assertTrue(Files.exists(filePath));

        Map<String, Object> map;
        try (FileInputStream fi = new FileInputStream(filePath.toFile())) {
            map = new Yaml().loadAs(fi, Map.class);
        }

        assertTrue(map.containsKey("beacons"));
        Map<String, Object> beacons = (Map<String, Object>) map.get("beacons");

        assertTrue(beacons.containsKey("virtpoller"));
        Map<String, Object> virtpoller = (Map<String, Object>)beacons.get("virtpoller");

        assertTrue(virtpoller.containsKey("cache_file"));
        assertTrue(virtpoller.containsKey("expire_time"));
        assertTrue(virtpoller.containsKey("interval"));
    }


    public void testGenerateVirtualizationPillarDataNoVirt() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);

        this.minionVirtualizationPillarFileManager.updatePillarFile(minion);

        Path filePath = tmpPillarRoot.resolve(PILLAR_DATA_FILE_PREFIX + "_" +
                minion.getMinionId() + "_" + "virtualization" + "." +
                PILLAR_DATA_FILE_EXT);

        assertFalse(Files.exists(filePath));
    }
}