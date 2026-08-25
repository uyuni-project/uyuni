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

package com.suse.manager.autoinstallation.installer.rhel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.kickstart.KickstartInstallType;
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
 * Tests for RhelKernelOptionsBuilder.
 */
public class RhelKernelOptionsBuilderTest {

    private MockConnection connection;
    private Distro distro;
    private Profile profile;
    private SystemRecord system;
    private RhelKernelOptionsBuilder builder;

    @BeforeEach
    public void setUp() {
        connection = new MockConnection("http://localhost", "token");
        distro = new Distro.Builder<String>()
                .setName("test-distro")
                .setKernel("kernel")
                .setInitrd("initrd")
                .setKsmeta(Optional.empty())
                .setBreed("redhat")
                .setOsVersion("rhel8")
                .setArch("x86_64")
                .build(connection);
        profile = Profile.create(connection, "test-profile", distro);
        system = SystemRecord.create(connection, "test-system", profile);
        builder = new RhelKernelOptionsBuilder();
        builder.setServerFqdn("uyuni.example.com");
    }

    @AfterEach
    public void tearDown() {
        MockConnection.clear();
    }

    @Test
    public void testDistroOptionsRhel8OrGreater() {
        KickstartableTree tree = new KickstartableTree() {
            @Override
            public String getLabel() {
                return "rhel8-label";
            }
            @Override
            public KickstartInstallType getInstallType() {
                return new KickstartInstallType() {
                    @Override
                    public boolean isRhel8OrGreater() {
                        return true;
                    }
                };
            }
        };

        KernelOptionsList opts = builder.distroOptions(tree);
        assertEquals("inst.repo=http://uyuni.example.com/ks/dist/rhel8-label", opts.toString());
    }

    @Test
    public void testDistroOptionsRhel6() {
        KickstartableTree tree = new KickstartableTree() {
            @Override
            public String getLabel() {
                return "rhel6-label";
            }
            @Override
            public KickstartInstallType getInstallType() {
                return new KickstartInstallType() {
                    @Override
                    public boolean isRhel8OrGreater() {
                        return false;
                    }
                };
            }
        };

        KernelOptionsList opts = builder.distroOptions(tree);
        assertTrue(opts.isEmpty());
    }

    @Test
    public void testProfileOptions() {
        KernelOptionsList opts = builder.profileOptions(profile);
        String expected = "inst.auto=http://localhost/cblr/svc/op/autoinstall/profile/test-profile " +
                "inst.auto_insecure";
        assertEquals(expected, opts.toString());
    }

    @Test
    public void testSystemOptionsRhel8() {
        KernelOptionsList opts = builder.systemOptions(system);
        String expected = "inst.ks.sendmac ks=http://uyuni.example.com/cblr/svc/op/autoinstall/system/test-system";
        assertEquals(expected, opts.toString());
    }

    @Test
    public void testSystemOptionsRhel6() {
        distro.setOsVersion("rhel6");
        KernelOptionsList opts = builder.systemOptions(system);
        String expected = "kssendmac ks=http://uyuni.example.com/cblr/svc/op/autoinstall/system/test-system";
        assertEquals(expected, opts.toString());
    }

    @Test
    public void testNetworkBoot() {
        KickstartableTree tree = new KickstartableTree() {
            @Override
            public String getLabel() {
                return "rhel8-label";
            }
            @Override
            public KickstartInstallType getInstallType() {
                return new KickstartInstallType() {
                    @Override
                    public boolean isRhel8OrGreater() {
                        return true;
                    }
                };
            }
        };

        KernelOptionsList opts = builder.networkBoot(tree, system);
        String expected = "inst.repo=http://uyuni.example.com/ks/dist/rhel8-label " +
                "inst.auto=http://localhost/cblr/svc/op/autoinstall/profile/test-profile " +
                "inst.auto_insecure " +
                "inst.ks.sendmac ks=http://uyuni.example.com/cblr/svc/op/autoinstall/system/test-system " +
                "info=http://uyuni.example.com/cblr/svc/op/nopxe/system/test-system";
        assertEquals(expected, opts.toString());
    }
}
