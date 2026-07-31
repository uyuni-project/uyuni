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

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.kickstart.cobbler.CobblerSnippet;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerCommand;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerXMLRPCHelper;
import com.redhat.rhn.manager.kickstart.cobbler.MockXMLRPCInvoker;
import com.redhat.rhn.testing.TestUtils;

import org.cobbler.CobblerConnection;
import org.cobbler.Distro;
import org.cobbler.MockConnection;

import java.io.File;

public class KickstartTestUtils {

    protected KickstartTestUtils() {
        //empty
    }

    public static void setupTestConfiguration(User u) throws Exception {
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
    }

    public static void createCobblerObjects(KickstartData k) {
        Distro d = Distro.lookupById(CobblerXMLRPCHelper.getConnection("test"),
                k.getKickstartDefaults().getKstree().getCobblerId());
        org.cobbler.Profile p = org.cobbler.Profile.create(
                CobblerXMLRPCHelper.getConnection("test"),
                CobblerCommand.makeCobblerName(k), d);
        p.setKickstart(k.buildCobblerFileName());
        k.setCobblerId(p.getUid());

    }
}
