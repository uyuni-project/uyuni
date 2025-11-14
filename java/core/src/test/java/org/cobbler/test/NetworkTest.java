/*
 * Copyright (c) 2022 SUSE LLC
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

import org.cobbler.Network;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class NetworkTest {

    private MockConnection mockConnection;

    @BeforeEach
    public void setUp() {
        mockConnection = new MockConnection("http://localhost", "token");
    }

    @AfterEach
    public void teardown() {
        MockConnection.clear();
    }

    @Test
    public void testNetworkConstructor() {
        // Arrange
        String interfaceName = "eth0";

        // Act
        Network network = new Network(mockConnection, interfaceName);

        // Assert: If no exception is raised this is enough
        Assertions.assertNotNull(network);
    }

    @Test
    public void testName() {
        // Arrange
        String interfaceName = "eth0";
        Network network = new Network(mockConnection, interfaceName);

        // Act
        String result = network.getName();

        // Assert
        Assertions.assertEquals(interfaceName, result);
    }

    @Test
    public void testNetmask() {
        // Arrange
        String interfaceName = "eth0";
        String expectedResult = "not being validated";
        Network network = new Network(mockConnection, interfaceName);

        // Act
        network.setNetmask(expectedResult);
        String result = network.getNetmask();

        // Assert
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void testIpAddress() {
        // Arrange
        String interfaceName = "eth0";
        String expectedResult = "not being validated";
        Network network = new Network(mockConnection, interfaceName);

        // Act
        network.setIpAddress(expectedResult);
        String result = network.getIpAddress();

        // Assert
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void testIpv6Address() {
        // Arrange
        String interfaceName = "eth0";
        String expectedResult = "not being validated";
        Network network = new Network(mockConnection, interfaceName);

        // Act
        network.setIpv6Address(expectedResult);
        String result = network.getIpv6Address();

        // Assert
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void testDnsName() {
        // Arrange
        String interfaceName = "eth0";
        String expectedResult = "not being validated";
        Network network = new Network(mockConnection, interfaceName);

        // Act
        network.setDnsname(expectedResult);
        String result = network.getDnsname();

        // Assert
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void testIpv6Secondaries() {
        // Arrange
        String interfaceName = "eth0";
        List<String> expectedResult = Arrays.asList("not being validated", "second element");
        Network network = new Network(mockConnection, interfaceName);

        // Act
        network.setIpv6Secondaries(expectedResult);
        List<String> result = network.getIpv6Secondaries();

        // Assert
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void testIsStaticNetwork() {
        // Arrange
        String interfaceName = "eth0";
        boolean expectedResult = true;
        Network network = new Network(mockConnection, interfaceName);

        // Act
        network.setStaticNetwork(expectedResult);
        boolean result = network.isStaticNetwork();

        // Assert
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void testMacAddress() {
        // Arrange
        String interfaceName = "eth0";
        String expectedResult = "not being validated";
        Network network = new Network(mockConnection, interfaceName);

        // Act
        network.setMacAddress(expectedResult);
        String result = network.getMacAddress();

        // Assert
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void testBondingMaster() {
        // Arrange
        String interfaceName = "eth0";
        String expectedResult = "not being validated";
        Network network = new Network(mockConnection, interfaceName);

        // Act
        network.setBondingMaster(expectedResult);
        String result = network.getBondingMaster();

                // Assert
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void testBondingOptions() {
        // Arrange
        String interfaceName = "eth0";
        String expectedResult = "not being validated";
        Network network = new Network(mockConnection, interfaceName);

        // Act
        network.setBondingOptions(expectedResult);
        String result = network.getBondingOptions();

        // Assert
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void testMakeBondingMaster() {
        // Arrange
        String interfaceName = "eth0";
        String expectedResult = "bond";
        Network network = new Network(mockConnection, interfaceName);

        // Act
        network.makeBondingMaster();
        String result = network.getBonding();

        // Assert
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void testMakeBondingSlave() {
        // Arrange
        String interfaceName = "eth0";
        String expectedResult = "bond_slave";
        Network network = new Network(mockConnection, interfaceName);

        // Act
        network.makeBondingSlave();
        String result = network.getBonding();

        // Assert
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void testMakeBondingNa() {
        // Arrange
        String interfaceName = "eth0";
        String expectedResult = "na";
        Network network = new Network(mockConnection, interfaceName);

        // Act
        network.makeBondingNA();
        String result = network.getBonding();

        // Assert
        Assertions.assertEquals(expectedResult, result);
    }
}
