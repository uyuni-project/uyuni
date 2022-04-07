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

package com.redhat.rhn.manager.kickstart.tree.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.KickstartInstallType;
import com.redhat.rhn.manager.kickstart.tree.TreeCreateOperation;

import org.junit.jupiter.api.Test;

/**
 * Tests the {@link com.redhat.rhn.manager.kickstart.tree.TreeCreateOperation} class
 */
public class TreeCreateOperationTest extends TreeOperationTestBase {

    @Test
    public void testCreate() throws Exception {
        TreeCreateOperation cmd = new TreeCreateOperation(user);
        setTestTreeParams(cmd);
        assertNull(cmd.store());
        assertNotNull(cmd.getUser());
        assertNotNull(cmd.getTree());
        assertNotNull(cmd.getTree().getInstallType());
        assertNotNull(cmd.getTree().getBasePath());
        assertNotNull(cmd.getTree().getChannel());
        assertNotNull(cmd.getTree().getLabel());
        assertNotNull(cmd.getTree().getTreeType());
        assertNotNull(cmd.getTree().getOrgId());
    }

    // helper method
    private void testPopulateKernelOptsForSuse(String distroLabel) throws Exception {
        TreeCreateOperation cmd = new TreeCreateOperation(user);
        setTestTreeParams(cmd);
        cmd.setInstallType(KickstartFactory.
                lookupKickstartInstallTypeByLabel(distroLabel));
        cmd.setKernelOptions("");
        cmd.store();
        assertContains(cmd.getKernelOptions(), "install=");
        assertContains(cmd.getKernelOptions(), "self_update=0");
    }

    @Test
    public void testPopulateKernelOptsForSuseBreed() throws Exception {
        testPopulateKernelOptsForSuse("suse");
    }

    @Test
    public void testPopulateKernelOptsForSlesPrefix() throws Exception {
        testPopulateKernelOptsForSuse("sles12generic");
    }

    @Test
    public void testCreateRhelistro() throws Exception {
        TreeCreateOperation cmd = new TreeCreateOperation(user);
        setTestTreeParams(cmd);
        cmd.setKernelOptions("");
        cmd.store();
        assert cmd.getKernelOptions().isEmpty();
    }

    @Test
    public void testPopulateKernelOptsForRhel8() throws Exception {
        TreeCreateOperation cmd = new TreeCreateOperation(user);
        setTestTreeParams(cmd);
        cmd.setInstallType(KickstartFactory.
                lookupKickstartInstallTypeByLabel(KickstartInstallType.RHEL_8));
        cmd.setKernelOptions("");
        cmd.store();
        assertContains(cmd.getKernelOptions(), "inst.repo=");
    }

}

