/*
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
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.formula.FormulaMonitoringManager;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import com.suse.manager.virtualization.test.TestVirtManager;
import com.suse.manager.webui.services.iface.MonitoringManager;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.VirtManager;
import com.suse.manager.webui.services.pillar.MinionPillarFileManager;
import com.suse.manager.webui.services.pillar.MinionVirtualizationPillarGenerator;
import com.suse.manager.webui.services.test.TestSaltApi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Tests for {@link MinionVirtualizationPillarGenerator}
 */
public class MinionVirtualizationPillarGeneratorTest extends BaseTestCaseWithUser {

    private SystemEntitlementManager systemEntitlementManager;

    protected MinionPillarFileManager minionVirtualizationPillarFileManager =
            new MinionPillarFileManager(new MinionVirtualizationPillarGenerator());

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        minionVirtualizationPillarFileManager.setPillarDataPath(tmpPillarRoot.toAbsolutePath());

        VirtManager virtManager = new TestVirtManager() {
            @Override
            public void updateLibvirtEngine(MinionServer minion) {
            }
        };

        SaltApi saltApi = new TestSaltApi();
        MonitoringManager monitoringManager = new FormulaMonitoringManager(saltApi);
        ServerGroupManager serverGroupManager =  new ServerGroupManager(saltApi);
        systemEntitlementManager = new SystemEntitlementManager(
                new SystemUnentitler(virtManager, monitoringManager, serverGroupManager),
                new SystemEntitler(new TestSaltApi(), virtManager, monitoringManager, serverGroupManager)
        );
    }

    @Test
    public void testGenerateVirtualizationPillarDataVirt() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        systemEntitlementManager.addEntitlementToServer(minion, EntitlementManager.VIRTUALIZATION);

        this.minionVirtualizationPillarFileManager.updatePillarFile(minion);

        Path filePath = tmpPillarRoot.resolve(PILLAR_DATA_FILE_PREFIX + "_" +
                minion.getMinionId() + "_" + "virtualization" + "." +
                PILLAR_DATA_FILE_EXT);

        assertFalse(Files.exists(filePath));
    }


    @Test
    public void testGenerateVirtualizationPillarDataNoVirt() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);

        this.minionVirtualizationPillarFileManager.updatePillarFile(minion);

        Path filePath = tmpPillarRoot.resolve(PILLAR_DATA_FILE_PREFIX + "_" +
                minion.getMinionId() + "_" + "virtualization" + "." +
                PILLAR_DATA_FILE_EXT);

        assertFalse(Files.exists(filePath));
    }
}
