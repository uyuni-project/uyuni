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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.redhat.rhn.domain.server.MinionFactory;
import com.redhat.rhn.domain.server.MinionServer;
import org.jmock.Mock;

import com.redhat.rhn.domain.server.InstalledPackage;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.testing.RhnJmockBaseTestCase;
import com.redhat.rhn.testing.TestUtils;
import com.suse.manager.reactor.RegisterMinionAction;
import com.suse.manager.reactor.RegisterMinionEvent;
import com.suse.manager.webui.services.SaltService;
import com.suse.saltstack.netapi.calls.modules.Grains;
import com.suse.saltstack.netapi.calls.modules.Pkg;
import com.suse.saltstack.netapi.parser.JsonParser;

/**
 * Tests for {@link RegisterMinionAction}.
 */
public class RegisterMinionActionTest extends RhnJmockBaseTestCase {

    private static final String MINION_ID = "suma3pg.vagrant.local";
	private static final String MACHINE_ID = "003f13081ddd408684503111e066f921";

    /**
     * Test the minion registration.
     *
     * @throws java.io.IOException
     * @throws java.lang.ClassNotFoundException
     */
    public void testDoExecute()
            throws IOException,
            ClassNotFoundException {

        // cleanup
        Mock saltServiceMock = mock(SaltService.class);
        String minionId = MINION_ID;

        MinionFactory.findByMachineId(MACHINE_ID).ifPresent(ServerFactory::delete);

        // Register a minion via RegisterMinionAction and mocked SaltService

        saltServiceMock.stubs().method("getMachineId").with(eq(minionId)).will(
                returnValue(MACHINE_ID));
        saltServiceMock.stubs().method("getGrains").with(eq(minionId)).will(
                returnValue(getGrains(minionId)));
        saltServiceMock.stubs().method("getCpuInfo").with(eq(minionId)).will(
                returnValue(getCpuInfo(minionId)));
        saltServiceMock.stubs().method("getInstalledPackageDetails").with(eq(minionId)).will(
                returnValue(this.getMinionPackages()));
        saltServiceMock.stubs().method("sendEvent").will(returnValue(true));
        saltServiceMock.stubs().method("syncGrains");
        saltServiceMock.stubs().method("syncModules");

        SaltService saltService = (SaltService) saltServiceMock.proxy();


        RegisterMinionAction action = new RegisterMinionAction(saltService) {
        };
        action.doExecute(new RegisterMinionEvent(minionId));

        // Verify the resulting system entry
        String machineId = saltService.getMachineId(minionId);
        Optional<MinionServer> optMinion = MinionFactory.findByMachineId(machineId);
        assertTrue(optMinion.isPresent());
        MinionServer minion = optMinion.get();
        assertEquals(minionId, minion.getName());
        assertEquals(machineId, minion.getDigitalServerId());
        assertEquals("3.12.48-52.27-default", minion.getRunningKernel());
        assertEquals("SLES", minion.getOs());
        assertEquals("12", minion.getRelease());
        assertEquals("N", minion.getAutoUpdate());
        assertEquals(489, minion.getRam());

        assertNotNull(minion.getCpu());
        assertEquals("GenuineIntel", minion.getCpu().getVendor());
        assertEquals("6", minion.getCpu().getFamily());
        assertEquals("2", minion.getCpu().getStepping());
        assertEquals("2593.994", minion.getCpu().getMHz());
        assertEquals("4096 KB", minion.getCpu().getCache());
        assertEquals(new Long(1L), minion.getCpu().getNrsocket());
        // TODO add more assertions

        assertEquals(ServerFactory.lookupServerArchByLabel("x86_64-redhat-linux"),
                minion.getServerArch());
        assertEquals(ServerFactory.findContactMethodByLabel("default"),
                minion.getContactMethod());

        // Verify the entitlement
        assertEquals(EntitlementManager.SALTSTACK, minion.getBaseEntitlement());

        for (InstalledPackage pkg : minion.getPackages()) {
            String release = null;
            String version = null;
            if (pkg.getName().getName().equals("aaa_base")) {
                release = "3.1";
                version = "13.2+git20140911.61c1681";
            }
            else if (pkg.getName().getName().equals("bash")) {
                release = "75.2";
                version = "4.2";
            }

            assertEquals(release, pkg.getEvr().getRelease());
            assertEquals(version, pkg.getEvr().getVersion());
            assertNull(pkg.getEvr().getEpoch());
            assertEquals("x86_64", pkg.getArch().getName());
        }
        assertEquals(2, minion.getPackages().size());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getCpuInfo(String minionId) throws IOException, ClassNotFoundException {
        Map<String, Object> grains = new JsonParser<>(Grains.items(false).getReturnType()).parse(
                readFile("dummy_cpuinfo.json"));
        return (Map<String, Object>)((List<Map<String, Object>>)grains.get("return")).get(0).get(minionId);
    }

    private Map<String, Pkg.Info> getMinionPackages() throws IOException, ClassNotFoundException {
        return new JsonParser<>(Pkg.infoInstalled("").getReturnType()).parse(
                readFile("dummy_package.json"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getGrains(String minionId) throws ClassNotFoundException, IOException {
    	Map<String, Object> grains = new JsonParser<>(Grains.items(false).getReturnType()).parse(
                readFile("dummy_grains.json"));
    	return (Map<String, Object>)((List<Map<String, Object>>)grains.get("return")).get(0).get(minionId);
    }

    private String readFile(String file) throws IOException, ClassNotFoundException {
        return Files.lines(new File(TestUtils.findTestData(
                "/com/suse/manager/reactor/test/" + file).getPath()
        ).toPath()).collect(Collectors.joining("\n"));
    }

}
