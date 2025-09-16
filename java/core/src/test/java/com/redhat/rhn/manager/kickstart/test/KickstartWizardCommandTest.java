/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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
package com.redhat.rhn.manager.kickstart.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.KickstartSession;
import com.redhat.rhn.domain.kickstart.KickstartableTree;
import com.redhat.rhn.domain.kickstart.test.KickstartDataTest;
import com.redhat.rhn.domain.kickstart.test.KickstartableTreeTest;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.token.ActivationKeyFactory;
import com.redhat.rhn.manager.kickstart.KickstartWizardHelper;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * KickstartWizardCommandTest
 */
public class KickstartWizardCommandTest extends BaseTestCaseWithUser {

    @Test
    public void testWizTrees() throws Exception {

        Channel c = ChannelFactoryTest.createBaseChannel(user);
        assertNull(c.getParentChannel());
        assertNotNull(c.getOrg());
        KickstartableTree tree  = KickstartableTreeTest.createTestKickstartableTree(c);
        tree.setChannel(c);
        TestUtils.saveAndFlush(tree);
        TestUtils.saveAndFlush(c);

        KickstartWizardHelper cmd = new KickstartWizardHelper(user);
        List trees = cmd.getKickstartableTrees();
        assertNotNull(trees);
        assertFalse(trees.isEmpty());
        boolean foundBaseTree = false;
        for (Object treeIn : trees) {
            KickstartableTree t = (KickstartableTree) treeIn;
            if (t.getChannel().getParentChannel() == null) {
                foundBaseTree = true;
            }
        }
        assertTrue(foundBaseTree, "Didnt find any trees that are from a basechannel.");


        assertNotNull(cmd.getKickstartableTree(tree.getId()));
    }


    // This tests a critical bit of functionality
    // to ensure that when we create a KickstartData we also
    // create a default KickstartSession that is used for
    // bare metal/PXE installs and that there is a default key
    // associated with it.
    @Test
    public void testStore() throws Exception {

        KickstartData ksdata = KickstartDataTest.createKickstartWithOptions(user.getOrg());
        KickstartWizardHelper cmd = new KickstartWizardHelper(user);
        cmd.store(ksdata);
        KickstartSession ksession =
            KickstartFactory.lookupDefaultKickstartSessionForKickstartData(ksdata);
        assertNotNull(ksession);
        ActivationKey key = ActivationKeyFactory.lookupByKickstartSession(ksession);
        assertNotNull(key);
        // Make sure its unlimited
        assertNull(key.getUsageLimit());
    }


}
