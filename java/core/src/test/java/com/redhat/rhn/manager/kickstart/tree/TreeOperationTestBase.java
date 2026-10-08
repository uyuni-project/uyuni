/*
 * Copyright (c) 2016 SUSE LLC
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

package com.redhat.rhn.manager.kickstart.tree;

import com.redhat.rhn.domain.channel.ChannelFactoryTest;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.KickstartTestUtils;
import com.redhat.rhn.domain.kickstart.KickstartableTree;
import com.redhat.rhn.domain.kickstart.KickstartableTreeTest;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.BeforeEach;

/**
 * Base class for Tree operations tests
 */
public abstract class TreeOperationTestBase extends BaseTestCaseWithUser {

    @BeforeEach
    public void setUp() throws Exception {
        KickstartTestUtils.setupTestConfiguration(user);
        createSuseTreeItems();
    }

    /**
     * The generic test setup only creates RHEL-style kernel/initrd files;
     * SUSE install types look for their loader layout, so create those too.
     */
    private void createSuseTreeItems() throws Exception {
        KickstartableTree tree = new KickstartableTree();
        tree.setChannel(ChannelFactoryTest.createTestChannel(user));
        tree.setInstallType(KickstartFactory.
                lookupKickstartInstallTypeByLabel("suse"));
        tree.setBasePath(KickstartableTreeTest.KICKSTART_TREE_PATH.getAbsolutePath());
        tree.setOrg(user.getOrg());
        KickstartableTreeTest.createKickstartTreeItems(tree);
    }

    protected void setTestTreeParams(BaseTreeEditOperation cmd) throws Exception {
        cmd.setInstallType(KickstartFactory.lookupKickstartInstallTypeByLabel("rhel_7"));
        cmd.setBasePath(KickstartableTreeTest.KICKSTART_TREE_PATH.getAbsolutePath());
        cmd.setChannel(ChannelFactoryTest.createTestChannel(user));
        cmd.setLabel("some_label" + TestUtils.randomString());
    }

}
