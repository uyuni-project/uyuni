/*
 * Copyright (c) 2020 SUSE LLC
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
package com.suse.manager.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.domain.rhnpackage.PackageArch;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageName;
import com.redhat.rhn.domain.rhnpackage.PackageType;
import com.redhat.rhn.domain.server.InstalledPackage;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactoryTest;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import com.suse.salt.netapi.calls.modules.Pkg;
import com.suse.salt.netapi.results.Change;
import com.suse.salt.netapi.utils.Xor;
import com.suse.utils.Json;

import com.google.gson.reflect.TypeToken;

import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

public class SaltUtilsTest extends BaseTestCaseWithUser {

    @Test
    public void testPackageToKey() {
        // atom package, openSUSE style, from database
        var atomName = new PackageName();
        atomName.setName("atom");

        var atomEvr = new PackageEvr(null, "1.42.0", "0.1", PackageType.RPM);

        var x86Arch = new PackageArch();
        x86Arch.setName("x86_64");
        x86Arch.setLabel("x86_64");

        var atomIp = new InstalledPackage();
        atomIp.setName(atomName);
        atomIp.setEvr(atomEvr);
        atomIp.setArch(x86Arch);
        assertEquals("atom-1.42.0-0.1.x86_64", SaltUtils.packageToKey(atomIp));

        // atom package, openSUSE style, from Salt
        var atomJson = "{" +
            "\"install_date_time_t\": 1498636553," +
            "\"version\": \"1.42.0\"," +
            "\"release\": \"0.1\"," +
            "\"arch\": \"x86_64\"" +
        "}";
        Pkg.Info atomInfo = Json.GSON.fromJson(atomJson, new TypeToken<Pkg.Info>() { }.getType());
        assertEquals("atom-1.42.0-0.1.x86_64", SaltUtils.packageToKey("atom", atomInfo));


        // initramfs-tools package, Debian style, from database
        var initramfsToolsName = new PackageName();
        initramfsToolsName.setName("initramfs-tools");

        var initramfsToolsEvr = new PackageEvr(null, "0.130ubuntu3.8", "X", PackageType.DEB);

        var allDebArch = new PackageArch();
        allDebArch.setName("all-deb");
        allDebArch.setLabel("all-deb");

        var initramfsToolsIp = new InstalledPackage();
        initramfsToolsIp.setName(initramfsToolsName);
        initramfsToolsIp.setEvr(initramfsToolsEvr);
        initramfsToolsIp.setArch(allDebArch);
        assertEquals("initramfs-tools-0.130ubuntu3.8.all", SaltUtils.packageToKey(initramfsToolsIp));

        // initramfs-tools package, Debian style, from Salt
        var initramfsToolsJson = "{" +
            "\"install_date_time_t\": 1498636553," +
            "\"version\": \"0.130ubuntu3.8\"," +
            "\"arch\": \"all\"" +
        "}";
        Pkg.Info initramfsToolsInfo = Json.GSON.fromJson(initramfsToolsJson, new TypeToken<Pkg.Info>() { }.getType());
        assertEquals("initramfs-tools-0.130ubuntu3.8.all",
                SaltUtils.packageToKey("initramfs-tools", initramfsToolsInfo));
    }

    /**
     * Test if the package change outcome is reported as "needs refreshing"
     * after installation of a new live patch package
     */
    @Test
    public void testPackageChangeOutcomeWithLivePatchPackages() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);

        Map<String, Change<Xor<String, List<Pkg.Info>>>> installLivePatch =
                Json.GSON.fromJson(new InputStreamReader(getClass().getResourceAsStream(
                                "/com/suse/manager/reactor/messaging/pkg_install.live_patch.json")),
                        new TypeToken<Map<String, Change<Xor<String, List<Pkg.Info>>>>>() { }.getType());

        Map<String, Change<Xor<String, List<Pkg.Info>>>> installOther =
                Json.GSON.fromJson(new InputStreamReader(getClass().getResourceAsStream(
                                "/com/suse/manager/reactor/messaging/pkg_install.new_format.json")),
                        new TypeToken<Map<String, Change<Xor<String, List<Pkg.Info>>>>>() { }.getType());

        // Other packages mustn't trigger refresh
        SaltUtils.PackageChangeOutcome outcome = SaltUtils.applyChangesFromStateModule(installOther, minion);
        assertEquals(SaltUtils.PackageChangeOutcome.DONE, outcome);

        // Live patch packages must trigger refresh
        outcome = SaltUtils.applyChangesFromStateModule(installLivePatch, minion);
        assertEquals(SaltUtils.PackageChangeOutcome.NEEDS_REFRESHING, outcome);
    }
}
