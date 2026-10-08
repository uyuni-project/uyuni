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

package com.redhat.rhn.domain.action.kickstart;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.kickstart.KickstartInstallType;
import com.redhat.rhn.domain.kickstart.KickstartableTree;

import org.cobbler.Distro;
import org.cobbler.MockConnection;
import org.cobbler.Profile;
import org.cobbler.SystemRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Unit tests for KickstartInitiateAction.
 */
public class KickstartInitiateActionTest {

    private org.cobbler.CobblerConnection connection;
    private final boolean useRealCobbler = true;

    private static class MyMockConnection extends MockConnection {
        private Map<String, Object> resolvedKernelOptions = new HashMap<>();

        MyMockConnection(String url, String token) {
            super(url, token);
        }

        public void setResolvedKernelOptions(Map<String, Object> opts) {
            this.resolvedKernelOptions = opts;
        }

        @Override
        public Object invokeMethod(String name, Object... args) {
            if ("get_item_resolved_value".equals(name) && args.length == 2 && "kernel_options".equals(args[1])) {
                return resolvedKernelOptions;
            }
            return super.invokeMethod(name, args);
        }
    }

    @BeforeEach
    public void setUp() {
        if (useRealCobbler) {
            connection = com.redhat.rhn.manager.kickstart.cobbler.CobblerXMLRPCHelper.getUncachedAutomatedConnection();
        }
        else {
            connection = new MyMockConnection("http://localhost", "token");
        }
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
    public void testBuildKernelOptionsSuse() {
        Distro distro = new Distro.Builder<String>()
                .setName("test-suse-distro")
                .setKernel("kernel")
                .setInitrd("initrd")
                .setKsmeta(Optional.empty())
                .setBreed("suse")
                .setOsVersion("sles15")
                .setArch("x86_64")
                .build(connection);
        Profile profile = Profile.create(connection, "test-suse-profile", distro);
        SystemRecord system = SystemRecord.create(connection, "test-suse-system", profile);

        // Put some starting kernel options including text
        Map<String, Object> resolvedOpts = new HashMap<>();
        resolvedOpts.put("text", "");
        resolvedOpts.put("foo", "bar");
        resolvedOpts.put("initrd", "some_initrd");

        if (useRealCobbler) {
            system.setKernelOptions(Optional.of(resolvedOpts));
        }
        else {
            ((MyMockConnection) connection).setResolvedKernelOptions(resolvedOpts);
        }

        // Let's create a mocked tree for SUSE
        KickstartableTree tree = new KickstartableTree() {
            @Override
            public KickstartInstallType getInstallType() {
                KickstartInstallType it = new KickstartInstallType();
                it.setLabel("sles15generic");
                it.setName("SLES15 Generic");
                return it;
            }
        };

        String kopts = KickstartInitiateAction.buildKernelOptions(system, tree, "uyuni.example.com", true);

        // Expect textmode=1 (since text was present), foo=bar, autoyast=..., info=...
        // and initrd should be removed.
        assertTrue(kopts.contains("textmode=1"));
        assertTrue(kopts.contains("foo=bar"));
        assertTrue(kopts.contains("autoyast=http://uyuni.example.com/cblr/svc/op/autoinstall/system/test-suse-system"));
        assertTrue(kopts.contains("info=http://uyuni.example.com/cblr/svc/op/nopxe/system/test-suse-system"));
        assertTrue(!kopts.contains("initrd"));
    }

    @Test
    public void testBuildKernelOptionsRhel() {
        Distro distro = new Distro.Builder<String>()
                .setName("test-rhel-distro")
                .setKernel("kernel")
                .setInitrd("initrd")
                .setKsmeta(Optional.empty())
                .setBreed("redhat")
                .setOsVersion("rhel8")
                .setArch("x86_64")
                .build(connection);
        Profile profile = Profile.create(connection, "test-rhel-profile", distro);
        SystemRecord system = SystemRecord.create(connection, "test-rhel-system", profile);

        Map<String, Object> resolvedOpts = new HashMap<>();
        resolvedOpts.put("foo", "bar");

        if (useRealCobbler) {
            system.setKernelOptions(Optional.of(resolvedOpts));
        }
        else {
            ((MyMockConnection) connection).setResolvedKernelOptions(resolvedOpts);
        }

        KickstartableTree tree = new KickstartableTree() {
            @Override
            public KickstartInstallType getInstallType() {
                KickstartInstallType it = new KickstartInstallType();
                it.setLabel("rhel_8");
                it.setName("Red Hat Enterprise Linux 8");
                return it;
            }
        };

        String kopts = KickstartInitiateAction.buildKernelOptions(system, tree, "uyuni.example.com", true);

        assertTrue(kopts.contains("foo=bar"));
        assertTrue(kopts.contains("inst.ks.sendmac"));
        assertTrue(kopts.contains("ks=http://uyuni.example.com/cblr/svc/op/autoinstall/system/test-rhel-system"));
        assertTrue(kopts.contains("info=http://uyuni.example.com/cblr/svc/op/nopxe/system/test-rhel-system"));
    }

    @Test
    public void testBuildKernelOptionsDebian() {
        Distro distro = new Distro.Builder<String>()
                .setName("test-debian-distro")
                .setKernel("kernel")
                .setInitrd("initrd")
                .setKsmeta(Optional.empty())
                .setBreed("debian")
                .setOsVersion("debian11")
                .setArch("x86_64")
                .build(connection);
        Profile profile = Profile.create(connection, "test-debian-profile", distro);
        SystemRecord system = SystemRecord.create(connection, "test-debian-system", profile);

        Map<String, Object> resolvedOpts = new HashMap<>();
        resolvedOpts.put("foo", "bar");

        if (useRealCobbler) {
            system.setKernelOptions(Optional.of(resolvedOpts));
        }
        else {
            ((MyMockConnection) connection).setResolvedKernelOptions(resolvedOpts);
        }

        // Debian has no tree install type, so we pass null tree to fall back to breed
        String kopts = KickstartInitiateAction.buildKernelOptions(system, null, "uyuni.example.com", true);

        assertTrue(kopts.contains("foo=bar"));
        assertTrue(kopts.contains("auto-install/enable=true"));
        assertTrue(kopts.contains("priority=critical"));
        assertTrue(kopts.contains("netcfg/choose_interface=auto"));
        assertTrue(kopts.contains("url=http://uyuni.example.com/cblr/svc/op/autoinstall/system/test-debian-system"));
        assertTrue(kopts.contains("info=http://uyuni.example.com/cblr/svc/op/nopxe/system/test-debian-system"));
    }
}
