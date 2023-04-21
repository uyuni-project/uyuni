/*
 * Copyright (c) 2023 SUSE LLC
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
package org.cobbler.test;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;

import org.cobbler.Distro;
import org.cobbler.Profile;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

public class ProfileTest extends TestCase {
    private Distro testDistro;
    private MockConnection connectionMock;

    /**
     * test setup
     */
    public void setUp() {
        // Config initialization
        Config.get().setString(ConfigDefaults.KICKSTART_COBBLER_DIR,
                "/var/lib/cobbler/templates/");
        Config.get().setString(ConfigDefaults.COBBLER_SNIPPETS_DIR,
                "/var/lib/cobbler/snippets");
        // Object initialization
        String distroName = "testProfileTest";
        connectionMock = new MockConnection("http://localhost", "token");
        testDistro = new Distro.Builder()
                .setName(distroName)
                .setKernel("kernel")
                .setInitrd("initrd")
                .setArch("arch")
                .build(connectionMock);
    }

    /**
     * test cleanup
     */
    public void teardown() {
        testDistro = null;
        MockConnection.clear();
    }

    /**
     * Test Kernel Options parsing
     * @throws Exception
     */
    public void testProfileKopts() throws Exception {
        String profileName = "koptsTest";
        Profile testProfile = Profile.create(connectionMock, profileName, testDistro);
        testProfile.setKernelOptions("ifcfg='eth0=10.99.82.100/24,10.99.82.1,10.99.82.104'");

        Map<String, Object> kopts = testProfile.getKernelOptionsMap();
        assertTrue("Missing key ifcfg", kopts.containsKey("ifcfg"));
        List<String> ifcgOpt = (List)kopts.get("ifcfg");
        assertEquals(1, ifcgOpt.size());
        assertEquals("eth0=10.99.82.100/24,10.99.82.1,10.99.82.104", ifcgOpt.get(0));
    }
}
