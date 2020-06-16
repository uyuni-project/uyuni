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
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import com.suse.manager.webui.services.pillar.MinionPillarFileManager;
import com.suse.manager.webui.services.pillar.MinionVirtualizationPillarGenerator;

import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Tests for {@link MinionVirtualizationPillarGenerator}
 */
public class MinionVirtualizationPillarGeneratorTest extends BaseTestCaseWithUser {

    protected MinionPillarFileManager minionVirtualizationPillarFileManager =
            new MinionPillarFileManager(new MinionVirtualizationPillarGenerator());

    @Override
    public void setUp() throws Exception {
        super.setUp();
        minionVirtualizationPillarFileManager.setPillarDataPath(tmpPillarRoot.toAbsolutePath());
    }

    public void testGenerateVirtualizationPillarData() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);

        this.minionVirtualizationPillarFileManager.generatePillarFile(minion);

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

}