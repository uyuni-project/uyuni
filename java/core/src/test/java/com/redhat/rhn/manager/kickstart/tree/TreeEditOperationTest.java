/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactoryTest;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.KickstartableTree;
import com.redhat.rhn.domain.kickstart.KickstartableTreeTest;
import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.Test;

/**
 * TreeEditCommandTest
 */
public class TreeEditOperationTest extends TreeOperationTestBase {

    @Test
    public void testEdit() throws Exception {
        Channel testChannel = ChannelFactoryTest.createTestChannel(user);
        KickstartableTree tree = KickstartableTreeTest.createTestKickstartableTree(testChannel);
        Long tid = tree.getId();

        TreeEditOperation cmd = new TreeEditOperation(tree.getId(), user);
        assertNotNull(cmd.getTree());

        String nlabel = "newlabel" + TestUtils.randomString();
        cmd.setBasePath(KickstartableTreeTest.KICKSTART_TREE_PATH.getAbsolutePath());
        cmd.setLabel(nlabel);
        assertNull(cmd.store());
        TestUtils.flushAndClearSession();

        KickstartableTree lookedUp = KickstartFactory.lookupKickstartTreeByIdAndOrg(tid, user.getOrg());
        assertEquals(nlabel, lookedUp.getLabel());
    }

    @Test
    public void testInvalidEdit() throws Exception {
        Channel testChannel = ChannelFactoryTest.createTestChannel(user);
        KickstartableTree tree = KickstartableTreeTest.createTestKickstartableTree(testChannel);
        Long tid = tree.getId();
        TestUtils.flushAndClearSession();

        TreeEditOperation cmd = new TreeEditOperation(tid, user);
        cmd.setBasePath(KickstartableTreeTest.KICKSTART_TREE_PATH.getAbsolutePath());
        assertNull(cmd.store());
        TestUtils.flushAndClearSession();

        TreeEditOperation newcmd = new TreeEditOperation(tid, user);
        newcmd.setLabel("testInvalidEdit " + TestUtils.randomString());
        assertNotNull(newcmd.store());
        TestUtils.flushAndClearSession();

        KickstartableTree lookedUp = KickstartFactory.lookupKickstartTreeByIdAndOrg(tid, user.getOrg());
        assertFalse(lookedUp.getLabel().startsWith("testInvalidEdit"));
    }

    /**
     * The distro options merge re-runs on edit: computed options are added
     * to the stored ones (the step-1.2 XML-RPC update fix).
     */
    @Test
    public void testEditRecomputesKernelOptions() throws Exception {
        Channel testChannel = ChannelFactoryTest.createTestChannel(user);
        KickstartableTree tree = KickstartableTreeTest.createTestKickstartableTree(testChannel);
        tree.setInstallType(KickstartFactory.
                lookupKickstartInstallTypeByLabel("sles15generic"));
        TestUtils.saveAndFlush(tree);
        Long tid = tree.getId();

        TreeEditOperation cmd = new TreeEditOperation(tid, user);
        cmd.setBasePath(KickstartableTreeTest.KICKSTART_TREE_PATH.getAbsolutePath());
        assertNull(cmd.store());
        TestUtils.flushAndClearSession();

        KickstartableTree lookedUp = KickstartFactory.lookupKickstartTreeByIdAndOrg(tid, user.getOrg());
        String kopts = lookedUp.getKernelOptions();
        TestUtils.assertContains(kopts, "install=");
        TestUtils.assertContains(kopts, "self_update=0");
    }

    /**
     * Re-running the edit does not duplicate the computed options.
     */
    @Test
    public void testEditKernelOptionsIdempotent() throws Exception {
        Channel testChannel = ChannelFactoryTest.createTestChannel(user);
        KickstartableTree tree = KickstartableTreeTest.createTestKickstartableTree(testChannel);
        tree.setInstallType(KickstartFactory.
                lookupKickstartInstallTypeByLabel("sles15generic"));
        TestUtils.saveAndFlush(tree);
        Long tid = tree.getId();

        TreeEditOperation cmd = new TreeEditOperation(tid, user);
        cmd.setBasePath(KickstartableTreeTest.KICKSTART_TREE_PATH.getAbsolutePath());
        assertNull(cmd.store());
        TestUtils.flushAndClearSession();
        String first = KickstartFactory.lookupKickstartTreeByIdAndOrg(tid, user.getOrg())
                .getKernelOptions();

        TreeEditOperation secondEdit = new TreeEditOperation(tid, user);
        secondEdit.setBasePath(KickstartableTreeTest.KICKSTART_TREE_PATH.getAbsolutePath());
        assertNull(secondEdit.store());
        TestUtils.flushAndClearSession();
        String second = KickstartFactory.lookupKickstartTreeByIdAndOrg(tid, user.getOrg())
                .getKernelOptions();

        assertEquals(first, second);
    }

    /**
     * User-typed options persisted on the tree win over the computed ones
     * on edit too: a custom install= is kept and only the missing
     * self_update= is added.
     */
    @Test
    public void testEditUserTypedOptionsWin() throws Exception {
        Channel testChannel = ChannelFactoryTest.createTestChannel(user);
        KickstartableTree tree = KickstartableTreeTest.createTestKickstartableTree(testChannel);
        tree.setInstallType(KickstartFactory.
                lookupKickstartInstallTypeByLabel("sles15generic"));
        tree.setKernelOptions("install=http://mymirror.example.com/sles15 useonlinerepo=1");
        TestUtils.saveAndFlush(tree);
        Long tid = tree.getId();

        TreeEditOperation cmd = new TreeEditOperation(tid, user);
        cmd.setBasePath(KickstartableTreeTest.KICKSTART_TREE_PATH.getAbsolutePath());
        assertNull(cmd.store());
        TestUtils.flushAndClearSession();

        String kopts = KickstartFactory.lookupKickstartTreeByIdAndOrg(tid, user.getOrg())
                .getKernelOptions();
        TestUtils.assertContains(kopts, "install=http://mymirror.example.com/sles15");
        TestUtils.assertContains(kopts, "useonlinerepo=1");
        TestUtils.assertContains(kopts, "self_update=0");
        assertEquals(1, kopts.split("install=", -1).length - 1);
    }

}
