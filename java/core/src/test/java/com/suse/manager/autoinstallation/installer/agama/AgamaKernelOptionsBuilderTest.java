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

package com.suse.manager.autoinstallation.installer.agama;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.kickstart.KickstartableTree;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.legacy.UserImpl;

import com.suse.manager.autoinstallation.KernelOptionsList;

import org.cobbler.Distro;
import org.cobbler.MockConnection;
import org.cobbler.Profile;
import org.cobbler.SystemRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Tests for AgamaKernelOptionsBuilder.
 */
public class AgamaKernelOptionsBuilderTest {

    private MockConnection connection;
    private Distro distro;
    private Profile profile;
    private SystemRecord system;
    private AgamaKernelOptionsBuilder builder;
    private User dummyUser;

    @BeforeEach
    public void setUp() {
        connection = new MockConnection("http://localhost", "token");
        distro = new Distro.Builder<String>()
                .setName("test-distro")
                .setKernel("kernel")
                .setInitrd("initrd")
                .setKsmeta(Optional.empty())
                .setBreed("generic")
                .setOsVersion("sles16generic")
                .setArch("x86_64")
                .build(connection);
        profile = Profile.create(connection, "test-profile", distro);
        system = SystemRecord.create(connection, "test-system", profile);
        builder = new AgamaKernelOptionsBuilder();
        builder.setServerFqdn("uyuni.example.com");
        dummyUser = new UserImpl();
        builder.setUser(dummyUser);
    }

    @AfterEach
    public void tearDown() {
        MockConnection.clear();
    }

    @Test
    public void testDistroOptionsWithSelfUpdate() {
        Channel childChannel = new Channel() {
            @Override
            public String getLabel() {
                return "child-label";
            }
            @Override
            public boolean isInstallerUpdates() {
                return true;
            }
        };

        Channel parentChannel = new Channel() {
            @Override
            public List<Channel> getAccessibleChildrenFor(User u) {
                return Arrays.asList(childChannel);
            }
        };

        KickstartableTree tree = new KickstartableTree() {
            @Override
            public String getLabel() {
                return "sles16-label";
            }
            @Override
            public Channel getChannel() {
                return parentChannel;
            }
        };

        KernelOptionsList opts = builder.distroOptions(tree);
        String expected = "self_update=https://uyuni.example.com/ks/dist/child/child-label/" +
                "sles16-label?ssl_verify=no " +
                "root=live:https://uyuni.example.com/ks/dist/sles16-label/LiveOS/squashfs.img rd.noverifyssl";
        assertEquals(expected, opts.toString());
    }

    @Test
    public void testDistroOptionsNoSelfUpdate() {
        Channel parentChannel = new Channel() {
            @Override
            public List<Channel> getAccessibleChildrenFor(User u) {
                return Collections.emptyList();
            }
        };

        KickstartableTree tree = new KickstartableTree() {
            @Override
            public String getLabel() {
                return "sles16-label";
            }
            @Override
            public Channel getChannel() {
                return parentChannel;
            }
        };

        KernelOptionsList opts = builder.distroOptions(tree);
        String expected = "root=live:https://uyuni.example.com/ks/dist/sles16-label/LiveOS/squashfs.img rd.noverifyssl";
        assertEquals(expected, opts.toString());
    }

    @Test
    public void testProfileOptions() {
        KernelOptionsList opts = builder.profileOptions(profile);
        String expected = "inst.auto=http://localhost/cblr/svc/op/autoinstall/profile/test-profile " +
                "inst.auto_insecure";
        assertEquals(expected, opts.toString());
    }

    @Test
    public void testSystemOptions() {
        KernelOptionsList opts = builder.systemOptions(system);
        assertTrue(opts.isEmpty());
    }
}
