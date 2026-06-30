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

import static com.redhat.rhn.domain.action.ActionFactoryTest.addServerAction;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.server.ServerAction;
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

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    /**
     * Test getFailedStateErrors extracts errors correctly from state.apply results
     * Also cover against {@code SaltUtils#updateServerAction}, but you'll need to
     * check to actual test logs
     */
    @Test
    public void testGetFailedStateErrors() {
        String stateSuccess = "module_|-grains_|-grains.items_|-run";
        String stateFail1 = "module_|-pkg_|-pkg.info_installed_|-run";
        String stateFail2 = "module_|-hw_|-hardware.udevdb_|-run";
        String stateFail3 = "module_|-grains_|-grains.items_something_else|-run";

        String stateJson = """
                {
                    "module_|-grains_|-grains.items_|-run": {
                        "result": true,
                        "comment": "Module function grains.items executed",
                        "__run_num__": 1
                    },
                    "module_|-pkg_|-pkg.info_installed_|-run": {
                        "result": false,
                        "comment": "pkg.info_installed threw exception: name 'datetime' is ...",
                        "__run_num__": 2
                    },
                    "module_|-hw_|-hardware.udevdb_|-run": {
                        "result": false,
                        "comment": "",
                        "__run_num__": 3
                    },
                    "module_|-grains_|-grains.items_something_else|-run": {
                        "result": false,
                        "__run_num__": 4
                    }
                }
                """;

        JsonElement jsonResult = JsonParser.parseString(stateJson);
        Map<String, String> errors = SaltUtils.getFailedStateErrors(jsonResult);

        // Expects 3 errors (as success state are ignored)
        assertEquals(3, errors.size());

        assertFalse(errors.containsKey(stateSuccess));

        assertTrue(errors.containsKey(stateFail1));
        assertTrue(errors.containsKey(stateFail2));
        assertTrue(errors.containsKey(stateFail3));

        assertEquals(
                "pkg.info_installed threw exception: name 'datetime' is ...",
                errors.get(stateFail1)
        );


        String stateFailedWithoutErrorMessage = "State failed without error message";
        assertEquals(stateFailedWithoutErrorMessage, errors.get(stateFail2));
        assertEquals(stateFailedWithoutErrorMessage, errors.get(stateFail3));

        // setup and call updateServerAction. No really useful assertions out of it but messages should be logged
        SaltUtils saltUtils = new SaltUtils(null, null);
        Action action = ActionFactory.createAction(ActionFactory.TYPE_SUBSCRIBE_CHANNELS);
        ServerAction serverAction = addServerAction(user, action, ServerAction::setStatusFailed);

        saltUtils.updateServerAction(serverAction, 1L, false, "test-jid-123", jsonResult,
                Optional.of(Xor.right("state.apply")), new Date());

        assertTrue(serverAction.isStatusFailed());
        assertTrue(serverAction.getResultMsg().contains("Failed to apply state"));
    }

    /**
     * Test that when we cannot extract detailed errors from Salt results
     * (eg JSON doesn't match state.apply format), action still fails gracefully
     * expect {@code SaltUtils.getFailedStateErrors} to detect no states
     */
    @Test
    public void testGetFailedStateErrorsWhenNonStateApplyJson() {
        String unparsableJson = """
            {
                "error": "Something went wrong",
                "stderr": "Command execution failed"
            }
            """;

        assertTrue(SaltUtils.getFailedStateErrors(JsonParser.parseString(unparsableJson)).isEmpty());

        // setup and call updateServerAction. No really useful assertions out of it but messages should be logged
        SaltUtils saltUtils = new SaltUtils(null, null);
        Action action = ActionFactory.createAction(ActionFactory.TYPE_SUBSCRIBE_CHANNELS);
        ServerAction serverAction = addServerAction(user, action, ServerAction::setStatusFailed);

        JsonElement jsonResult = JsonParser.parseString(unparsableJson);

        saltUtils.updateServerAction(serverAction, 1L, false, "test-jid-failed",
                jsonResult, Optional.of(Xor.right("state.apply")), new Date());

        assertTrue(serverAction.isStatusFailed());
        assertTrue(serverAction.getResultMsg().contains("Failed to apply state"));
    }
}
