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

package com.redhat.rhn.domain.kickstart;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.kickstart.cobbler.CobblerSnippet;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerCommand;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerXMLRPCHelper;
import com.redhat.rhn.manager.kickstart.cobbler.MockXMLRPCInvoker;
import com.redhat.rhn.testing.TestUtils;

import org.cobbler.CobblerConnection;
import org.cobbler.CobblerObject;
import org.cobbler.Distro;
import org.cobbler.MockConnection;
import org.cobbler.Profile;
import org.cobbler.SystemRecord;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.nio.file.Files;

public class KickstartTestUtils {

    /**
     * System property switching the cobbler-touching test suites from the
     * mocked cobbler connection to a real one (started inside the test
     * container via java/buildconf/test/cobbler/setup-cobbler-test.sh).
     */
    public static final String USE_REAL_COBBLER_PROPERTY = "use.real.cobbler";

    protected KickstartTestUtils() {
        //empty
    }

    /**
     * @return true if the tests should run against a real cobbler instead of
     *         the mock
     */
    public static boolean useRealCobbler() {
        return Boolean.getBoolean(USE_REAL_COBBLER_PROPERTY);
    }

    public static void setupTestConfiguration(User u) throws Exception {
        setupTestConfiguration(u, useRealCobbler());
    }

    /**
     * Sets up the test configuration for mocked or real cobbler. In real
     * mode the connection classes are re-pointed from the mocks to the real
     * implementations (the same override CobblerCommandTestBase does for its
     * connectToCobbler flag); tests are skipped when cobbler is unreachable.
     *
     * @param u the user to create test tree items for
     * @param realCobbler true to run against a real cobbler
     * @throws Exception if anything goes wrong
     */
    public static void setupTestConfiguration(User u, boolean realCobbler) throws Exception {
        Config.get().setString(CobblerXMLRPCHelper.class.getName(),
                MockXMLRPCInvoker.class.getName());
        Config.get().setString(ConfigDefaults.KICKSTART_COBBLER_DIR,
                "/tmp/kickstart/");
        Config.get().setString(ConfigDefaults.COBBLER_SNIPPETS_DIR,
                "/tmp/kickstart/snippets");
        Config.get().setString(ConfigDefaults.MOUNT_POINT,
                "/tmp/kickstart/mount_point");
        TestUtils.createDirIfNotExists(new File("/tmp/kickstart/mount_point"));

        Config.get().setString(ConfigDefaults.KICKSTART_MOUNT_POINT,
                "/tmp/kickstart/kickstart_mount_point");
        TestUtils.createDirIfNotExists(new File("/tmp/kickstart/kickstart_mount_point"));

        Config.get().setString(CobblerConnection.class.getName(),
                MockConnection.class.getName());

        TestUtils.createDirIfNotExists(new File(ConfigDefaults.get()
                .getKickstartConfigDir() + File.separator + KickstartData.WIZARD_DIR));
        TestUtils.createDirIfNotExists(new File(ConfigDefaults.get()
                .getKickstartConfigDir() + File.separator + KickstartData.RAW_DIR));
        TestUtils.createDirIfNotExists(CobblerSnippet.getSpacewalkSnippetsDir());

        KickstartableTreeTest.createKickstartTreeItems(u);

        MockConnection.clear();

        if (realCobbler) {
            assumeTrue(isCobblerReachable(),
                    "Cobbler is not reachable, start it with "
                            + "java/buildconf/test/cobbler/setup-cobbler-test.sh");
            Config.get().setString(CobblerXMLRPCHelper.class.getName(),
                    CobblerXMLRPCHelper.class.getName());
            Config.get().setString(CobblerConnection.class.getName(),
                    CobblerConnection.class.getName());
        }
    }

    /**
     * Removes everything the test created on a real cobbler (system records,
     * profiles and then distros - dependency order) or resets the mock.
     * Must run after every test: real cobbler rejects duplicate MACs, and
     * the test fixtures reuse one fixed MAC.
     */
    public static void cleanUpCobbler() {
        if (useRealCobbler()) {
            CobblerConnection con = CobblerXMLRPCHelper.getUncachedAutomatedConnection();
            SystemRecord.list(con).forEach(CobblerObject::remove);
            Profile.list(con).forEach(CobblerObject::remove);
            Distro.list(con).forEach(CobblerObject::remove);
        }
        else {
            MockConnection.clear();
        }
    }

    /**
     * Probes whether the real cobbler under test knows the opensuse16.1
     * signature (cobbler PR #177). SLE16 fixtures create distros with that
     * os_version; a cobbler without the Agama support rejects them, so the
     * SLE16-specific tests are skipped in that case.
     *
     * @return true if the cobbler accepts opensuse16.1 distros
     */
    public static synchronized boolean cobblerSupportsSles16() {
        if (sles16Support == null) {
            Distro probe = null;
            try {
                probe = new Distro.Builder<String>()
                        .setName("sles16-support-probe")
                        .setKernel("/tmp/kickstart/images/images/pxeboot/vmlinuz")
                        .setInitrd("/tmp/kickstart/images/images/pxeboot/initrd.img")
                        .setBreed("suse")
                        .setOsVersion("opensuse16.1")
                        .build(CobblerXMLRPCHelper.getUncachedAutomatedConnection());
                sles16Support = true;
            }
            catch (RuntimeException e) {
                sles16Support = false;
            }
            finally {
                if (probe != null) {
                    probe.remove();
                }
            }
        }
        return sles16Support;
    }

    private static Boolean sles16Support;

    private static boolean isCobblerReachable() {
        URI uri = URI.create(ConfigDefaults.get().getCobblerServerUrl());
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(uri.getHost(), uri.getPort()), 3000);
            return true;
        }
        catch (IOException e) {
            return false;
        }
    }

    public static void createCobblerObjects(KickstartData k) {
        Distro d = Distro.lookupById(CobblerXMLRPCHelper.getConnection("test"),
                k.getKickstartDefaults().getKstree().getCobblerId());
        org.cobbler.Profile p = org.cobbler.Profile.create(
                CobblerXMLRPCHelper.getConnection("test"),
                CobblerCommand.makeCobblerName(k), d);
        // Real cobbler validates the profile's template path on modify; the
        // fixture has no rendered content yet, so create the (empty) file.
        File ksFile = new File(k.buildCobblerFileName());
        ksFile.getParentFile().mkdirs();
        if (!ksFile.exists()) {
            try {
                Files.createFile(ksFile.toPath());
            }
            catch (IOException e) {
                throw new RuntimeException("Cannot create " + ksFile, e);
            }
        }
        p.setKickstart(k.buildCobblerFileName());
        k.setCobblerId(p.getUid());

    }
}
