/*
 * Copyright (c) 2021 SUSE LLC
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

package com.redhat.rhn.domain.entitlement;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.server.MinionServerFactoryTest;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFQDN;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.testing.ServerTestUtils;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.webui.services.TestSaltApi;
import com.suse.manager.webui.services.iface.SaltApi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Test for {@link com.redhat.rhn.domain.entitlement.PeripheralServerEntitlement}
 */
public class PeripheralServerEntitlementTest extends BaseEntitlementTestCase {
    private final SaltApi saltApi = new TestSaltApi();
    private final SystemEntitlementManager systemEntitlementManager = new SystemEntitlementManager(
            new SystemUnentitler(saltApi), new SystemEntitler(saltApi)
    );

    @Override
    protected void createEntitlement() {
        ent = new PeripheralServerEntitlement();
    }

    @Override
    protected String getLabel() {
        return EntitlementManager.PERIPHERAL_SERVER_ENTITLED;
    }

    private Server exTestForeignServer;
    private Server exTestActualServer;
    private Server exTestOtherMinion;
    private List<Server> exTestServers;

    private void prepareExclusionTest() throws Exception {
        exTestForeignServer = setupServer(ServerTestUtils.createForeignSystem(user, "9999"),
                "foreign", "peripheral.server.com");
        exTestActualServer = setupServer(MinionServerFactoryTest.createTestMinionServer(user),
                "server", "peripheral.server.com");
        exTestOtherMinion = setupServer(MinionServerFactoryTest.createTestMinionServer(user),
                "otherMinion", "otherMinion.com");
        exTestServers = List.of(exTestForeignServer, exTestActualServer, exTestOtherMinion);
    }

    private Server setupServer(Server server, String name, String hostName) {
        server.setName(name);
        server.setHostname(hostName);
        server.setServerArch(ServerFactory.lookupServerArchByLabel("x86_64-redhat-linux"));

        ServerFQDN primaryFqdn = new ServerFQDN(server, server.getName() + ".com");
        primaryFqdn.setPrimary(true);
        server.getFqdns().add(primaryFqdn);

        server.addFqdn("common.dns.com");
        return server;
    }

    private void resetEntitlements() {
        exTestServers.forEach(m ->
                systemEntitlementManager.removeServerEntitlement(m, EntitlementManager.PERIPHERAL_SERVER));
    }

    private void setPeripheralEntitlementTo(Server serverIn) {
        systemEntitlementManager.addEntitlementToServer(serverIn, EntitlementManager.PERIPHERAL_SERVER);
        exTestServers.forEach(TestUtils::saveAndFlush);
    }

    private boolean isAllowedToBePeripheral(Server serverIn) {
        return EntitlementManager.PERIPHERAL_SERVER.isAllowedOnServer(serverIn);
    }

    private List<Server> buildFourMinions() {
        Server minion1 = MinionServerFactoryTest.createTestMinionServer(user);
        minion1.setName("minion1");
        Server minion2 = MinionServerFactoryTest.createTestMinionServer(user);
        minion2.setName("minion2");
        Server minion3 = MinionServerFactoryTest.createTestMinionServer(user);
        minion3.setName("minion3");
        Server minion4 = MinionServerFactoryTest.createTestMinionServer(user);
        minion4.setName("minion4");
        List<Server> allMinions = List.of(minion1, minion2, minion3, minion4);

        allMinions.forEach(m -> m.setServerArch(ServerFactory.lookupServerArchByLabel("x86_64-redhat-linux")));
        allMinions.forEach(m -> {
            ServerFQDN primaryFqdn = new ServerFQDN(m, m.getName() + ".com");
            primaryFqdn.setPrimary(true);
            m.getFqdns().add(primaryFqdn);
        });

        return allMinions;
    }

    private void printMinion(Server minion) {
        String debugString = "%s (%s) (%s): foreign: %s peripheral entitled: %s peripheral isAllowedOnServer: %s"
                .formatted(
                        minion.getName(),
                        minion.getHostname(),
                        minion.getFqdns().stream()
                                .map(fqdn -> "%s/%s".formatted(fqdn.getName(), fqdn.isPrimary())).toList(),
                        minion.isForeign(),
                        minion.hasEntitlement(EntitlementManager.PERIPHERAL_SERVER),
                        EntitlementManager.PERIPHERAL_SERVER.isAllowedOnServer(minion)
                );
        System.out.println(debugString);
    }

    /**
     * Tests that the entitlement is allowed on salt clients.
     */
    @Test
    public void testIsAllowed() throws Exception {
        Server foreign = ServerTestUtils.createForeignSystem(user, "9999");
        Server minion = MinionServerFactoryTest.createTestMinionServer(user);
        foreign.setServerArch(ServerFactory.lookupServerArchByLabel("x86_64-redhat-linux"));
        minion.setServerArch(ServerFactory.lookupServerArchByLabel("x86_64-redhat-linux"));
        foreign.addFqdn("test.com");
        minion.addFqdn("test.com");

        assertTrue(EntitlementManager.PERIPHERAL_SERVER.isAllowedOnServer(foreign));
        assertTrue(EntitlementManager.PERIPHERAL_SERVER.isAllowedOnServer(minion));

        systemEntitlementManager.addEntitlementToServer(foreign, EntitlementManager.PERIPHERAL_SERVER);
        foreign = TestUtils.saveAndFlush(foreign);

        System.out.println("foreign: " + foreign.hasEntitlement(EntitlementManager.PERIPHERAL_SERVER));
        System.out.println("minion: " + minion.hasEntitlement(EntitlementManager.PERIPHERAL_SERVER));
        // the entitlement can't be enabled on 2 servers with the same fqdn
        assertFalse(EntitlementManager.PERIPHERAL_SERVER.isAllowedOnServer(minion));
    }

    @Test
    @DisplayName("non-foreign unrelated minions can all have peripheral server entitlement")
    public void testNonForeignUnrelatedMinions() {
        List<Server> fourMinions = buildFourMinions();
        //check entitlement is allowed
        fourMinions.forEach(m -> assertTrue(EntitlementManager.PERIPHERAL_SERVER.isAllowedOnServer(m)));

        //set entitlement on all minions
        fourMinions.forEach(m ->
                systemEntitlementManager.addEntitlementToServer(m, EntitlementManager.PERIPHERAL_SERVER));
        fourMinions.forEach(TestUtils::saveAndFlush);

        //check entitlement is set and allowed on all minions
        fourMinions.forEach(m -> assertTrue(m.hasEntitlement(EntitlementManager.PERIPHERAL_SERVER)));
        fourMinions.forEach(m -> assertTrue(EntitlementManager.PERIPHERAL_SERVER.isAllowedOnServer(m)));
        fourMinions.forEach(this::printMinion);
    }

    @Test
    @DisplayName("non-foreign minions correlated with the same FQDN can all have peripheral server entitlement")
    public void testNonForeignFqdnCorrelatedMinions() {
        List<Server> fourMinions = buildFourMinions();
        // correlate minions with the same FQDN
        fourMinions.forEach(m -> m.addFqdn("common.dns.com"));
        //check entitlement is allowed
        fourMinions.forEach(m -> assertTrue(EntitlementManager.PERIPHERAL_SERVER.isAllowedOnServer(m)));

        //set entitlement on one minion
        systemEntitlementManager.addEntitlementToServer(fourMinions.get(0), EntitlementManager.PERIPHERAL_SERVER);
        fourMinions.forEach(TestUtils::saveAndFlush);

        //check entitlement is allowed on all minions
        fourMinions.forEach(m -> assertTrue(EntitlementManager.PERIPHERAL_SERVER.isAllowedOnServer(m)));

        //setting entitlement on all minions should be possible
        fourMinions.forEach(m ->
                systemEntitlementManager.addEntitlementToServer(m, EntitlementManager.PERIPHERAL_SERVER));
        fourMinions.forEach(TestUtils::saveAndFlush);

        //check entitlement is set and allowed on all minions
        fourMinions.forEach(m -> assertTrue(m.hasEntitlement(EntitlementManager.PERIPHERAL_SERVER)));
        fourMinions.forEach(m -> assertTrue(EntitlementManager.PERIPHERAL_SERVER.isAllowedOnServer(m)));
        fourMinions.forEach(this::printMinion);
    }


    @Test
    @DisplayName("foreign and actual servers are mutually exclusive")
    public void exclusionTest() throws Exception {
        prepareExclusionTest();
        //check entitlement is allowed for all servers
        exTestServers.forEach(m -> assertTrue(isAllowedToBePeripheral(m)));

        //set foreign as peripheral
        resetEntitlements();
        setPeripheralEntitlementTo(exTestForeignServer);

        // if foreign is peripheral, corresponding actual server should not be allowed to have peripheral entitlement
        // always allowed on other non-corresponding minion
        assertTrue(isAllowedToBePeripheral(exTestForeignServer));
        assertFalse(isAllowedToBePeripheral(exTestActualServer));
        assertTrue(isAllowedToBePeripheral(exTestOtherMinion));

        //set actual server as peripheral
        resetEntitlements();
        setPeripheralEntitlementTo(exTestActualServer);

        // if actual server is peripheral, corresponding foreign should not be allowed to have peripheral entitlement
        // always allowed on other non-corresponding minion
        assertFalse(isAllowedToBePeripheral(exTestForeignServer));
        assertTrue(isAllowedToBePeripheral(exTestActualServer));
        assertTrue(isAllowedToBePeripheral(exTestOtherMinion));

        //set other minion as peripheral
        resetEntitlements();
        setPeripheralEntitlementTo(exTestOtherMinion);

        // if other minion is peripheral, both foreign and corresponding actual server should be allowed
        // to have peripheral entitlement
        // always allowed on other non-corresponding minion
        exTestServers.forEach(this::printMinion);
        assertTrue(isAllowedToBePeripheral(exTestForeignServer));
        assertTrue(isAllowedToBePeripheral(exTestActualServer));
        assertTrue(isAllowedToBePeripheral(exTestOtherMinion));
    }

}
