/*
 * Copyright (c) 2021 SUSE LLC
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

package com.redhat.rhn.domain.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.server.ansible.AnsiblePath;
import com.redhat.rhn.domain.server.ansible.InventoryPath;
import com.redhat.rhn.domain.server.ansible.PlaybookPath;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.ServerTestUtils;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.webui.services.iface.SaltApi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

class AnsibleFactoryTest extends JMockBaseTestCaseWithUser {

    private SaltApi saltApi;

    @BeforeEach
    void setup() {
        saltApi = context.mock(SaltApi.class);
    }

    @Test
    void testSaveAndFindAnsiblePath() {
        MinionServer minionServer1 = MinionServerFactoryTest.createTestMinionServer(user);
        MinionServer minionServer2 = MinionServerFactoryTest.createTestMinionServer(user);

        AnsiblePath inventoryPath = new InventoryPath(minionServer1, Path.of("/tmp/test1"));
        AnsiblePath playbookPath = new PlaybookPath(minionServer2, Path.of("/tmp/test2"));

        inventoryPath = AnsibleFactory.saveAnsiblePath(inventoryPath);
        playbookPath = AnsibleFactory.saveAnsiblePath(playbookPath);
        assertNotNull(inventoryPath.getId());
        assertNotNull(playbookPath.getId());

        TestUtils.flushSession();
        // let's get those entities out of the session
        TestUtils.evict(inventoryPath);
        TestUtils.evict(playbookPath);

        assertEquals(inventoryPath, AnsibleFactory.lookupAnsiblePathById(inventoryPath.getId()).get());
        assertEquals(inventoryPath, AnsibleFactory.lookupAnsiblePathByPathAndMinion(Path.of("/tmp/test1"),
                minionServer1.getId()).get());
        assertEquals(playbookPath, AnsibleFactory.lookupAnsiblePathById(playbookPath.getId()).get());
        assertEquals(1, AnsibleFactory.listAnsiblePaths(minionServer1.getId()).size());
        assertEquals(inventoryPath, AnsibleFactory.listAnsiblePaths(minionServer1.getId()).iterator().next());
    }

    @Test
    void testRemoveAnsiblePath() {
        MinionServer minionServer1 = MinionServerFactoryTest.createTestMinionServer(user);
        AnsiblePath inventoryPath = new InventoryPath(minionServer1, Path.of("/tmp/test1"));
        inventoryPath = AnsibleFactory.saveAnsiblePath(inventoryPath);

        AnsibleFactory.removeAnsiblePath(inventoryPath);
        assertTrue(AnsibleFactory.lookupAnsiblePathById(inventoryPath.getId()).isEmpty());
    }

    @Test
    void correctlyListsAnsibleInventoryServersByControlNode() {
        MinionServer controlMinion = ServerTestUtils.createAnsibleControlNode(user, saltApi, context);

        MinionServer one = MinionServerFactoryTest.createTestMinionServer(user);
        MinionServer two = MinionServerFactoryTest.createTestMinionServer(user);
        MinionServer three = MinionServerFactoryTest.createTestMinionServer(user);
        MinionServer four = MinionServerFactoryTest.createTestMinionServer(user);

        InventoryPath firstInventory = new InventoryPath(controlMinion, Path.of("/tmp/test/one"), Set.of(one, two));
        AnsibleFactory.saveAnsiblePath(firstInventory);

        InventoryPath secondInventory = new InventoryPath(controlMinion, Path.of("/tmp/test/two"), Set.of(three, four));
        AnsibleFactory.saveAnsiblePath(secondInventory);

        TestUtils.flushAndClearSession();

        List<Server> allServers = AnsibleFactory.listAnsibleInventoryServersByControlNode(controlMinion.getId());
        assertNotNull(allServers);
        assertEquals(
            Stream.of(one, two, three, four).sorted(Comparator.comparing(Server::getId)).toList(),
            allServers.stream().sorted(Comparator.comparing(Server::getId)).toList()
        );
    }

    @Test
    void correctlyListsAnsibleInventoryServersExcludingControlNode() {
        MinionServer controlOne = ServerTestUtils.createAnsibleControlNode(user, saltApi, context);
        MinionServer controlTwo = ServerTestUtils.createAnsibleControlNode(user, saltApi, context);
        MinionServer controlThree = ServerTestUtils.createAnsibleControlNode(user, saltApi, context);

        MinionServer one = MinionServerFactoryTest.createTestMinionServer(user);
        MinionServer two = MinionServerFactoryTest.createTestMinionServer(user);
        MinionServer three = MinionServerFactoryTest.createTestMinionServer(user);
        MinionServer four = MinionServerFactoryTest.createTestMinionServer(user);
        MinionServer five = MinionServerFactoryTest.createTestMinionServer(user);
        MinionServer six = MinionServerFactoryTest.createTestMinionServer(user);

        InventoryPath firstInventory = new InventoryPath(controlOne, Path.of("/tmp/test/one"), Set.of(one, two));
        AnsibleFactory.saveAnsiblePath(firstInventory);

        InventoryPath secondInventory = new InventoryPath(controlTwo, Path.of("/tmp/test/two"), Set.of(three, four));
        AnsibleFactory.saveAnsiblePath(secondInventory);

        InventoryPath thirdInventory = new InventoryPath(controlThree, Path.of("/tmp/test/three"), Set.of(five, six));
        AnsibleFactory.saveAnsiblePath(thirdInventory);

        TestUtils.flushAndClearSession();

        List<Server> allServers = AnsibleFactory.listAnsibleInventoryServersExcludingControlNode(controlTwo.getId());
        assertNotNull(allServers);
        assertEquals(
            Stream.of(one, two, five, six).sorted(Comparator.comparing(Server::getId)).toList(),
            allServers.stream().sorted(Comparator.comparing(Server::getId)).toList()
        );
    }

    @Test
    void generatedCoverageTestLookupAnsibleInventoryPath() {
        // this test has been generated programmatically to test AnsibleFactory.lookupAnsibleInventoryPath
        // containing a hibernate query that is not covered by any test so far
        // feel free to modify and/or complete it
        AnsibleFactory.lookupAnsibleInventoryPath(0L, "");
    }

    @Test
    void generatedCoverageTestListAnsiblePlaybookPaths() {
        // this test has been generated programmatically to test AnsibleFactory.listAnsiblePlaybookPaths
        // containing a hibernate query that is not covered by any test so far
        // feel free to modify and/or complete it
        AnsibleFactory.listAnsiblePlaybookPaths(0L);
    }
}
