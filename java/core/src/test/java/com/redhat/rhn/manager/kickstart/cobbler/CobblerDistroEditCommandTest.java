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
package com.redhat.rhn.manager.kickstart.cobbler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.redhat.rhn.domain.kickstart.KickstartableTree;
import com.redhat.rhn.testing.TestUtils;

import org.cobbler.CobblerConnection;
import org.cobbler.Distro;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

/**
 * CobblerDistroEditCommand Test.
 */
public class CobblerDistroEditCommandTest extends CobblerCommandTestBase {

    private CobblerConnection connection;
    private KickstartableTree sourceTree;

    /**
     * {@inheritDoc}
     *
     * @throws Exception if anything goes wrong
     */
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        connection = CobblerXMLRPCHelper.getAutomatedConnection();
        sourceTree = ksdata.getTree();
    }

    /**
     * Tests whether cobbler Distro can be successfully edited.
     * After editing the distro, tests whether this distro (and its metadata)
     * can be retrieved using the tree.
     *
     */
    @Test
    public void testDistroEdit() {
        CobblerDistroEditCommand cmd = new
            CobblerDistroEditCommand(sourceTree, user);
        String newName = TestUtils.randomString();
        ksdata.getKickstartDefaults().getKstree().setLabel(newName);
        assertNull(cmd.store());
        assertNotNull(ksdata.getTree().getCobblerObject(user));
        assertNotNull(ksdata.getTree().getCobblerObject(user).getName());
    }

    /**
     * Tests the recreation logic of the CobblerDistroEditCommand.
     * If the tree does paravirtualization, but the cobbler xen distro is missing,
     * CobblerDistroEditCommand recreates it.
     *
     */
    @Test
    public void testParaDistroRecreateXenDistroOnEdit() {
        // remove the distro
        Distro xen = Distro.lookupById(connection, sourceTree.getCobblerXenId());
        xen.remove();

        // verify it's null
        assertNull(Distro.lookupById(connection, sourceTree.getCobblerXenId()));

        // verify it's recreated
        CobblerDistroEditCommand cmd = new
                CobblerDistroEditCommand(sourceTree, user);
        assertNull(cmd.store());
        assertNotNull(Distro.lookupById(connection, sourceTree.getCobblerXenId()));
    }

    /**
     * Tests the removing logic of the CobblerDistroEditCommand.
     * If the tree does NOT paravirtualization, but the cobbler distro exists,
     * CobblerDistroEditCommand removes it.
     *
     */
    @Test
    public void testParaDistroXenDistroRemovedOnEdit() {
        // verify it's there
        assertNotNull(Distro.lookupById(connection, sourceTree.getCobblerXenId()));

        // delete its file
        File xenPath = new File(sourceTree.getKernelXenPath());
        xenPath.delete();

        // verify it's removed
        CobblerDistroEditCommand cmd = new
                CobblerDistroEditCommand(sourceTree, user);
        assertNull(cmd.store());
        assertNull(Distro.lookupById(connection, sourceTree.getCobblerXenId()));
    }

    /**
     * Verify that the cobbler xen id stays same after
     * CobblerDistroEditCommand.store when xen distro already exists.
     *
     */
    @Test
    public void testParaDistroEditIdStaysSameOnEdit() {
        String xenIdBefore = sourceTree.getCobblerXenId();

        CobblerDistroEditCommand cmd = new
                CobblerDistroEditCommand(sourceTree, user);
        cmd.store();

        String xenIdAfter = sourceTree.getCobblerXenId();
        assertEquals(xenIdBefore, xenIdAfter);
    }
}
