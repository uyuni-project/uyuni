/**
 * Copyright (c) 2015 SUSE LLC
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
package com.suse.manager.reactor.test;

import com.redhat.rhn.domain.server.InstalledPackage;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.testing.RhnJmockBaseTestCase;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.reactor.RegisterMinionAction;
import com.suse.manager.reactor.RegisterMinionEvent;
import com.suse.manager.webui.services.SaltService;
import com.suse.saltstack.netapi.calls.modules.Pkg;
import com.suse.saltstack.netapi.parser.JsonParser;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.jmock.Mock;

import java.util.HashMap;
import java.util.Map;

/**
 * Tests for {@link RegisterMinionAction}.
 */
public class RegisterMinionActionTest extends RhnJmockBaseTestCase {

    private Map<String, String> machineIds = new HashMap<>();

    /**
     * Test the minion registration.
     * @throws java.io.IOException
     * @throws java.lang.ClassNotFoundException
     */
    public void testDoExecute()
            throws IOException,
                   ClassNotFoundException {
        // Register a minion via RegisterMinionAction and mocked SaltService
        Mock saltServiceMock = mock(SaltService.class);
        String minionId = TestUtils.randomString();
        saltServiceMock.stubs().method("getMachineId").with(eq(minionId)).will(
                returnValue(getMachineId(minionId)));
        saltServiceMock.stubs().method("getGrains").with(eq(minionId)).will(
                returnValue(getGrains(minionId)));
        saltServiceMock.stubs().method("getInstalledPackageDetails").with(eq(minionId)).will(
                returnValue(this.getMinionPackages()));
        SaltService saltService = (SaltService) saltServiceMock.proxy();
        RegisterMinionAction action = new RegisterMinionAction(saltService) {};
        action.doExecute(new RegisterMinionEvent(minionId));

        // Verify the resulting system entry
        String machineId = saltService.getMachineId(minionId);
        Server minion = ServerFactory.findRegisteredMinion(machineId);
        assertNotNull(minion);
        assertEquals(minionId, minion.getName());
        assertEquals(machineId, minion.getDigitalServerId());
        assertEquals("3.12.47-2-default", minion.getRunningKernel());
        assertEquals("SLES", minion.getOs());
        assertEquals("12", minion.getRelease());
        assertEquals("N", minion.getAutoUpdate());
        assertEquals(477L, minion.getRam());
        assertEquals(ServerFactory.lookupServerArchByLabel("x86_64-redhat-linux"),
                minion.getServerArch());
        assertEquals(ServerFactory.findContactMethodByLabel("default"),
                minion.getContactMethod());

        // Verify the entitlement
        assertEquals(EntitlementManager.SALTSTACK, minion.getBaseEntitlement());

        int pkgs = 0;
        for (InstalledPackage pkg : minion.getPackages()) {
            String release = null;
            String version = null;
            if (pkg.getName().getName().equals("aaa_base")) {
                release = "3.1";
                version = "13.2+git20140911.61c1681";
            } else if (pkg.getName().getName().equals("bash")) {
                release = "75.2";
                version = "4.2";
            }

            assertEquals(pkg.getEvr().getRelease(), release);
            assertEquals(pkg.getEvr().getVersion(), version);
            assertNull(pkg.getEvr().getEpoch());
            assertEquals(pkg.getArch().getName(), "x86_64");
            pkgs++;
        }
        assertEquals(pkgs, 2);
    }

    private Map<String, Pkg.Info> getMinionPackages()
            throws IOException,
                   ClassNotFoundException {
        String jsonData = "{}";
        File channelsXML = new File(TestUtils.findTestData(
                "/com/suse/manager/reactor/test/dummy_package.json").getPath());
        StringBuilder jdata = new StringBuilder();
        for (String line : Files.readAllLines(channelsXML.toPath())) {
            jdata.append(line).append("\n");
        }

        jsonData = jdata.toString();

        return new JsonParser<>(Pkg.infoInstalled("").getReturnType()).parse(jsonData);
    }

    private String getMachineId(String minionId) {
        if (!machineIds.containsKey(minionId)) {
            machineIds.put(minionId, TestUtils.randomString());
        }
        return machineIds.get(minionId);
    }

    private Map<String, Object> getGrains(String minionId) {
        Map<String, Object> grains = new HashMap<>();
        grains.put("kernelrelease", "3.12.47-2-default");
        grains.put("machine_id", getMachineId(minionId));
        grains.put("mem_total", 477.0);
        grains.put("os", "SLES");
        grains.put("osfullname", "SLES");
        grains.put("osrelease", "12");
        return grains;
    }
}
