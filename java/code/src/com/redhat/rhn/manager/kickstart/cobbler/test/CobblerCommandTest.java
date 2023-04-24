/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.manager.kickstart.cobbler.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.KickstartableTree;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.NetworkInterface;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.NetworkInterfaceTest;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerDistroDeleteCommand;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerDistroSyncCommand;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerLoginCommand;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerProfileCreateCommand;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerProfileDeleteCommand;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerProfileEditCommand;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerSystemCreateCommand;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerXMLRPCHelper;
import com.redhat.rhn.testing.ServerTestUtils;
import com.redhat.rhn.testing.TestUtils;

import org.cobbler.CobblerConnection;
import org.cobbler.Distro;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * CobblerCommandTest
 */
public class CobblerCommandTest extends CobblerCommandTestBase {

    /*public void testDupSystems() throws Exception {
        Server s = ServerFactory.lookupById(Long.valueOf(1000010339));
        CobblerSystemCreateCommand cmd = new CobblerSystemCreateCommand(user, s, ksdata,
                "http://localhost/test/path", TestUtils.randomString());
        cmd.store();
    }*/

    @Test
    public void testSystemCreate() throws Exception {

        Server s = ServerTestUtils.createTestSystem(user);
        NetworkInterface device = NetworkInterfaceTest.createTestNetworkInterface(s);
        s.addNetworkInterface(device);

        CobblerSystemCreateCommand cmd = new CobblerSystemCreateCommand(user, s, ksdata,
                    "http://localhost/test/path", TestUtils.randomString());
        cmd.store();
        assertNotNull(s.getCobblerId());

        // Ensure we can call it twice.
        cmd = new CobblerSystemCreateCommand(user, s, ksdata,
                "http://localhost/test/path", TestUtils.randomString());
        cmd.store();
        assertNotNull(s.getCobblerId());
    }

    @Test
    public void testProfileCreate() {
        CobblerProfileCreateCommand cmd = new CobblerProfileCreateCommand(
                ksdata, user);
        assertNull(cmd.store());
        assertNotNull(ksdata.getCobblerObject(user));
        assertNotNull(ksdata.getCobblerObject(user).getName());
    }

    @Test
    public void testProfileEdit() {
        // create one first
        CobblerProfileCreateCommand cmd = new CobblerProfileCreateCommand(
                ksdata, user);
        assertNull(cmd.store());

        // Now test edit
        CobblerProfileEditCommand pec = new
            CobblerProfileEditCommand(ksdata, user);
        String newName = "some-new-name-" + System.currentTimeMillis();
        ksdata.setLabel(newName);
        assertNull(pec.store());
        assertNotNull(ksdata.getCobblerObject(user).getName());
    }

    @Test
    public void testProfileDelete() {
        CobblerProfileCreateCommand createCmd = new CobblerProfileCreateCommand(
                ksdata, user);
        assertNull(createCmd.store());

        CobblerProfileDeleteCommand cmd = new CobblerProfileDeleteCommand(ksdata, user);
        assertNull(cmd.store());
        assertNull(ksdata.getCobblerObject(user));
    }

    /**
     * Tests that CobblerDistroSyncCommand recreates missing cobbler entries.
     */
    @Test
    public void testDistroSync() {
        CobblerConnection con = CobblerXMLRPCHelper.getAutomatedConnection();

        // delete all cobbler distros
        for (Distro distro : Distro.list(con)) {
            distro.remove();
        }

        // verify the distros corresponding to our tree aren't there
        for (KickstartableTree kickstartableTree : KickstartFactory.lookupKickstartTrees()) {
            assertNull(Distro.lookupById(con, kickstartableTree.getCobblerId()));
            assertNull(Distro.lookupById(con, kickstartableTree.getCobblerXenId()));
        }

        CobblerDistroSyncCommand cmd = new CobblerDistroSyncCommand();
        cmd.store();

        // verify they got resynced
        for (KickstartableTree kickstartableTree : KickstartFactory.lookupKickstartTrees()) {
            assertNotNull(Distro.lookupById(con, kickstartableTree.getCobblerId()));
            assertNotNull(Distro.lookupById(con, kickstartableTree.getCobblerXenId()));
        }
    }

    /**
     * Tests that 'kernel options' and 'kernel options post' are back-synced from cobbler
     * in case they are null.
     */
    @Test
    public void testKernelOptionsBacksync() {
        // re-use the KS tree created in setUp
        KickstartableTree tree = KickstartFactory.lookupKickstartTrees().get(0);
        String cobblerId = tree.getCobblerId();
        // make sure they have null kernel options and kernel options post
        tree.setKernelOptions(null);
        tree.setKernelOptionsPost(null);
        KickstartFactory.saveKickstartableTree(tree);

        // modify the kernel options of the cobbler distro
        CobblerConnection con = CobblerXMLRPCHelper.getAutomatedConnection();
        Distro distro = Distro.lookupById(con, cobblerId);
        Map<String, Object> kernelOptions = new HashMap<>();
        kernelOptions.put("option1", "val1");
        distro.setKernelOptions(Optional.of(kernelOptions));
        Map<String, Object> kernelOptionsPost = new HashMap<>();
        kernelOptionsPost.put("otherOption", "val2 with space");
        distro.setKernelOptionsPost(Optional.of(kernelOptionsPost));

        // backsync
        CobblerDistroSyncCommand syncCommand = new CobblerDistroSyncCommand();
        syncCommand.backsyncKernelOptions();

        KickstartableTree fromDb = KickstartFactory
                .lookupKickstartTreeByCobblerIdOrXenId(cobblerId);
        // after the backsync command, the kickstartable tree in the db should have
        // the kernel options updated
        assertEquals("option1=val1", fromDb.getKernelOptions().trim());
        assertEquals("otherOption=\"val2 with space\"", fromDb.getKernelOptionsPost().trim());
    }

    /**
     * Tests that non-null 'kernel options' field of the kickstartable tree
     * remains untouched on the backsync.
     */
    @Test
    public void testKernelOptionsDontBacksync() {
        // re-use the KS tree created in setUp
        KickstartableTree tree = KickstartFactory.lookupKickstartTrees().get(0);
        String cobblerId = tree.getCobblerId();
        // make sure some kernel options are not null
        tree.setKernelOptions("option1=value1");
        tree.setKernelOptionsPost(null);
        KickstartFactory.saveKickstartableTree(tree);

        // backsync
        CobblerDistroSyncCommand syncCommand = new CobblerDistroSyncCommand();
        syncCommand.backsyncKernelOptions();

        KickstartableTree fromDb = KickstartFactory
                .lookupKickstartTreeByCobblerIdOrXenId(cobblerId);
        // after the backsync command, the kernel options of the kickstartable tree in the
        // db should remain untouched
        assertEquals("option1=value1", fromDb.getKernelOptions().trim());
        // the get function convert null to empty string
        assert fromDb.getKernelOptionsPost().isEmpty();
    }

    @Test
    public void testDistroDelete() {
        CobblerDistroDeleteCommand cmd = new
            CobblerDistroDeleteCommand(ksdata.getTree(), user);
        assertNull(cmd.store());
    }

    @Test
    public void testLogin() {
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        UserFactory.save(user);
        user = reload(user);
        CobblerLoginCommand cmd = new CobblerLoginCommand();
        String cobblertoken = cmd.login(user.getLogin(), "password");
        assertNotNull(cobblertoken);
        assertTrue(cmd.checkToken(cobblertoken));
    }

}
