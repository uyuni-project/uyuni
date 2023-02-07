/*
 * Copyright (c) 2015--2021 SUSE LLC
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.redhat.rhn.domain.kickstart.KickstartableTree;
import com.redhat.rhn.manager.kickstart.KickstartUrlHelper;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerDistroCreateCommand;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerXMLRPCHelper;

import org.cobbler.CobblerConnection;
import org.cobbler.Distro;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

/**
 * CobblerDistroCreateCommand Test.
 */
public class CobblerDistroCreateCommandTest extends CobblerCommandTestBase {

    private KickstartableTree tree;

    /**
     * {@inheritDoc}
     *
     * @throws Exception if anything goes wrong
     */
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        tree = ksdata.getTree();
    }

    /**
     * Tests whether cobbler Distro can be successfully created using given
     * data. After creating the distro, tests whether this distro (and its
     * metadata) can be retrieved using the tree.
     */
    @Test
    public void testDistroCreate() {
        CobblerDistroCreateCommand cmd = new
            CobblerDistroCreateCommand(tree, user);
        assertNull(cmd.store());
        assertNotNull(tree.getCobblerObject(user));
        assertNotNull(
                tree.getCobblerObject(user)
                        .getKsMeta()
                        .get()
                        .get(KickstartUrlHelper.COBBLER_MEDIA_VARIABLE)
        );
    }

    /**
     * Tests whether the xen distro is created for a tree with paravirtualization.
     */
    @Test
    public void testDistroCreateXenCreated() {
        CobblerConnection con = CobblerXMLRPCHelper.getAutomatedConnection();

        Distro distro = Distro.lookupById(con, tree.getCobblerXenId());
        distro.remove();
        assertNull(Distro.lookupById(con, tree.getCobblerXenId()));

        CobblerDistroCreateCommand cmd = new CobblerDistroCreateCommand(tree, user);
        cmd.store();
        assertNotNull(Distro.lookupById(con, tree.getCobblerXenId()));
    }

    /**
     * Tests that the xen distro is NOT created for a tree without paravirtualization.
     */
    @Test
    public void testDistroCreateXenNotCreated() {
        CobblerConnection con = CobblerXMLRPCHelper.getAutomatedConnection();

        Distro.lookupById(con, tree.getCobblerXenId()).remove();
        assertNull(Distro.lookupById(con, tree.getCobblerXenId()));

        File xenPath = new File(tree.getKernelXenPath());
        xenPath.delete();

        CobblerDistroCreateCommand cmd = new
            CobblerDistroCreateCommand(tree, user);
        cmd.store();
        assertNull(Distro.lookupById(con, tree.getCobblerXenId()));
    }
}
