/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.autoinstallation.installer.debian;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.kickstart.KickstartableTree;

import com.suse.manager.autoinstallation.KernelOptionsList;

import org.cobbler.Distro;
import org.cobbler.MockConnection;
import org.cobbler.Profile;
import org.cobbler.SystemRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

/**
 * Tests for DebianKernelOptionsBuilder.
 */
public class DebianKernelOptionsBuilderTest {

    private org.cobbler.CobblerConnection connection;
    private final boolean useRealCobbler = true;
    private Distro distro;
    private Profile profile;
    private SystemRecord system;
    private DebianKernelOptionsBuilder builder;

    @BeforeEach
    public void setUp() {
        if (useRealCobbler) {
            connection = com.redhat.rhn.manager.kickstart.cobbler.CobblerXMLRPCHelper.getUncachedAutomatedConnection();
        }
        else {
            connection = new MockConnection("http://localhost", "token");
        }
        distro = new Distro.Builder<String>()
                .setName("test-distro")
                .setKernel("kernel")
                .setInitrd("initrd")
                .setKsmeta(Optional.empty())
                .setBreed("debian")
                .setArch("x86_64")
                .build(connection);
        profile = Profile.create(connection, "test-profile", distro);
        system = SystemRecord.create(connection, "test-system", profile);
        builder = new DebianKernelOptionsBuilder();
        builder.setServerFqdn("uyuni.example.com");
    }

    @AfterEach
    public void tearDown() {
        if (useRealCobbler) {
            SystemRecord.list(connection).forEach(org.cobbler.CobblerObject::remove);
            Profile.list(connection).forEach(org.cobbler.CobblerObject::remove);
            Distro.list(connection).forEach(org.cobbler.CobblerObject::remove);
        }
        else {
            MockConnection.clear();
        }
    }

    @Test
    public void testDistroOptions() {
        KickstartableTree tree = new KickstartableTree();
        KernelOptionsList opts = builder.distroOptions(tree);
        assertTrue(opts.isEmpty());
    }

    @Test
    public void testProfileOptions() {
        KernelOptionsList opts = builder.profileOptions(profile);
        assertTrue(opts.isEmpty());
    }

    @Test
    public void testSystemOptions() {
        KernelOptionsList opts = builder.systemOptions(system);
        String expected = "auto-install/enable=true priority=critical netcfg/choose_interface=auto " +
                "url=http://uyuni.example.com/cblr/svc/op/autoinstall/system/test-system";
        assertEquals(expected, opts.toString());
    }
}
