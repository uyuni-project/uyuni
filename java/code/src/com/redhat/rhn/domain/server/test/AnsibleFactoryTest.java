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

package com.redhat.rhn.domain.server.test;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.server.AnsibleFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ansible.AnsiblePath;
import com.redhat.rhn.domain.server.ansible.InventoryPath;
import com.redhat.rhn.domain.server.ansible.PlaybookPath;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import java.nio.file.Path;

/**
 * Tests for {@link AnsibleFactory}
 */
public class AnsibleFactoryTest extends BaseTestCaseWithUser {

    /**
     * Test basic save - read cycle of AnsiblePath implementations
     * @throws Exception
     */
    public void testSaveAndFindAnsiblePath() throws Exception {
        MinionServer minionServer1 = MinionServerFactoryTest.createTestMinionServer(user);
        MinionServer minionServer2 = MinionServerFactoryTest.createTestMinionServer(user);

        AnsiblePath inventoryPath = new InventoryPath(minionServer1);
        inventoryPath.setPath(Path.of("/tmp/test1"));
        AnsiblePath playbookPath = new PlaybookPath(minionServer2);
        playbookPath.setPath(Path.of("/tmp/test2"));

        inventoryPath = AnsibleFactory.saveAnsiblePath(inventoryPath);
        playbookPath = AnsibleFactory.saveAnsiblePath(playbookPath);
        assertNotNull(inventoryPath.getId());
        assertNotNull(playbookPath.getId());

        HibernateFactory.getSession().flush();
        // let's get those entities out of the session
        HibernateFactory.getSession().evict(inventoryPath);
        HibernateFactory.getSession().evict(playbookPath);

        assertEquals(inventoryPath, AnsibleFactory.lookupAnsiblePathById(inventoryPath.getId()).get());
        assertEquals(inventoryPath, AnsibleFactory.lookupAnsiblePathByPathAndMinion(Path.of("/tmp/test1"),
                minionServer1.getId()).get());
        assertEquals(playbookPath, AnsibleFactory.lookupAnsiblePathById(playbookPath.getId()).get());
        assertEquals(1, AnsibleFactory.listAnsiblePaths(minionServer1.getId()).size());
        assertEquals(inventoryPath, AnsibleFactory.listAnsiblePaths(minionServer1.getId()).iterator().next());
    }

    /**
     * Test removing AnsiblePath
     */
    public void testRemoveAnsiblePath() throws Exception {
        MinionServer minionServer1 = MinionServerFactoryTest.createTestMinionServer(user);
        AnsiblePath inventoryPath = new InventoryPath(minionServer1);
        inventoryPath.setPath(Path.of("/tmp/test1"));
        inventoryPath = AnsibleFactory.saveAnsiblePath(inventoryPath);

        AnsibleFactory.removeAnsiblePath(inventoryPath);
        assertTrue(AnsibleFactory.lookupAnsiblePathById(inventoryPath.getId()).isEmpty());
    }
}
