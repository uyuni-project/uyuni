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
 * SPDX-License-Identifier: GPL-2.0-only
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ProfileTest {

    private Distro testDistro;
    private MockConnection connectionMock;

    @BeforeEach
    public void setUp() {
        // Config initialization
        Config.get().setString(ConfigDefaults.KICKSTART_COBBLER_DIR,
                "/var/lib/cobbler/templates/");
        Config.get().setString(ConfigDefaults.COBBLER_SNIPPETS_DIR,
                "/var/lib/cobbler/snippets");
        // Object initialization
        String distroName = "testProfileTest";
        connectionMock = new MockConnection("http://localhost", "token");
        testDistro = new Distro.Builder<String>()
                .setName(distroName)
                .setKernel("kernel")
                .setInitrd("initrd")
                .setArch("arch")
                .build(connectionMock);
    }

    @AfterEach
    public void teardown() {
        testDistro = null;
        MockConnection.clear();
    }

    @Test
    public void testDistro() {
        // Arrange
        String profileName = "distro";
        Profile testProfile = Profile.create(connectionMock, profileName, testDistro);

        // Act
        testProfile.setDistro(testDistro);
        Distro result = testProfile.getDistro();

        // Assert
        Assertions.assertEquals(testDistro.getUid(), result.getUid());
    }

    @Test
    public void testList() {
        // Arrange
        MockConnection laggingConnection = new MockConnection("http://localhost", "token") {
            @Override
            public Object invokeMethod(String name, Object... args) {
                if (name.equals("get_profile") && "deleted".equals(args[0])) {
                    return "~";
                }
                return super.invokeMethod(name, args);
            }
        };
        Profile testProfile1 = Profile.create(laggingConnection, "profile1", testDistro);
        Profile testProfile2 = Profile.create(laggingConnection, "profile2", testDistro);
        Profile.create(laggingConnection, "deleted", testDistro);

        // Act
        List<Profile> resultExcludes = Profile.list(laggingConnection, Set.of(testProfile2.getId()));
        List<Profile> resultNoExcludes = Profile.list(laggingConnection);

        // Assert
        Assertions.assertEquals(1, resultExcludes.size());
        Assertions.assertEquals(testProfile1.getId(), resultExcludes.get(0).getId());

        Assertions.assertEquals(2, resultNoExcludes.size());
        Assertions.assertEquals(Set.of(testProfile1.getId(), testProfile2.getId()),
                resultNoExcludes.stream().map(p -> p.getId()).collect(Collectors.toSet()));
    }

    @Test
    public void testDhcpTag() {
        // Arrange
        String profileName = "dhcpTag";
        Profile testProfile = Profile.create(connectionMock, profileName, testDistro);
        String expectedResult = "dhcp tag";

        // Act
        testProfile.setDhcpTag(expectedResult);
        String result = testProfile.getDhcpTag();

        // Assert
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void testKickstart() {
        // Arrange
        String profileName = "autoinstall";
        Profile testProfile = Profile.create(connectionMock, profileName, testDistro);
        String expectedResult = "/var/lib/cobbler/templates/dhcp_tag";

        // Act
        testProfile.setKickstart("dhcp_tag");
        String result = testProfile.getKickstart();

        // Assert
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void testVirtBridge() {
        // Arrange
        String profileName = "virtBridge";
        Profile testProfile = Profile.create(connectionMock, profileName, testDistro);
        Optional<String> expectedResult = Optional.of("my_bridge");

        // Act
        testProfile.setVirtBridge(expectedResult);
        Optional<String> result = testProfile.getVirtBridge();

        // Assert
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void testVirtCpus() {
        // Arrange
        String profileName = "virtCpus";
        Profile testProfile = Profile.create(connectionMock, profileName, testDistro);
        Optional<Integer> expectedResult = Optional.of(64);

        // Act
        testProfile.setVirtCpus(expectedResult);
        Optional<Integer> result = testProfile.getVirtCpus();

        // Assert
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void testVirtType() {
        // Arrange
        String profileName = "virtType";
        Profile testProfile = Profile.create(connectionMock, profileName, testDistro);
        Optional<String> expectedResult = Optional.of("test");

        // Act
        testProfile.setVirtType(expectedResult);
        Optional<String> result = testProfile.getVirtType();

        // Assert
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void testGetRepos() {
        // Arrange
        String profileName = "getRepos";
        Profile testProfile = Profile.create(connectionMock, profileName, testDistro);
        List<String> expectedResult = Arrays.asList("test", "test");

        // Act
        testProfile.setRepos(expectedResult);
        List<String> result = testProfile.getRepos();

        // Assert
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void testVirtPath() {
        // Arrange
        String profileName = "virtPath";
        Profile testProfile = Profile.create(connectionMock, profileName, testDistro);
        Optional<String> expectedResult = Optional.of("test");

        // Act
        testProfile.setVirtPath(expectedResult);
        Optional<String> result = testProfile.getVirtPath();

        // Assert
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void testServer() {
        // Arrange
        String profileName = "server";
        Profile testProfile = Profile.create(connectionMock, profileName, testDistro);
        Optional<String> expectedResult = Optional.of("test");

        // Act
        testProfile.setServer(expectedResult);
        Optional<String> result = testProfile.getServer();

        // Assert
        Assertions.assertEquals(expectedResult, result);
    }

    @Disabled("Nameservers don't have a setter at the moment.")
    @Test
    public void testNameServers() {
        // Arrange
        String profileName = "nameServers";
        Profile testProfile = Profile.create(connectionMock, profileName, testDistro);
        Optional<List<String>> expectedResult = Optional.of(Arrays.asList("test", "test2"));

        // Act
        testProfile.setNameServers(expectedResult);
        Optional<List<String>> result = testProfile.getNameServers();

        // Assert
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void testMenuEnabled() {
        // Arrange
        String profileName = "menuEnabled";
        Profile testProfile = Profile.create(connectionMock, profileName, testDistro);
        boolean expectedResult = true;

        // Act
        testProfile.setEnableMenu(expectedResult);
        boolean result = testProfile.menuEnabled();

        // Assert
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void testVirtFileSize() {
        // Arrange
        String profileName = "virtFileSize";
        Profile testProfile = Profile.create(connectionMock, profileName, testDistro);
        Optional<Double> expectedResult = Optional.of(5.0);

        // Act
        testProfile.setVirtFileSize(expectedResult);
        Optional<Double> result = testProfile.getVirtFileSize();

        // Assert
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void testVirtRam() {
        // Arrange
        String profileName = "virtRam";
        Profile testProfile = Profile.create(connectionMock, profileName, testDistro);
        Optional<Integer> expectedResult = Optional.of(5);

        // Act
        testProfile.setVirtRam(expectedResult);
        Optional<Integer> result = testProfile.getVirtRam();

        // Assert
        Assertions.assertEquals(expectedResult, result);
    }

    @Disabled("Method under test is broken by client side implementation.")
    @Test
    public void testGenerateKickstart() {
        // Arrange
        String profileName = "generateKickstart";
        Profile testProfile = Profile.create(connectionMock, profileName, testDistro);
        String expectedResult = "";

        // Act
        String result = testProfile.generateKickstart();

        // Assert
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void testSyncRedHatManagementKeys() {
        // Arrange
        String profileName = "syncRedHatManagementKeys";
        Profile testProfile = Profile.create(connectionMock, profileName, testDistro);
        Optional<String> expectedResult = Optional.of("test2,test1");

        // Act
        testProfile.syncRedHatManagementKeys(null, Arrays.asList("test1", "test2"));
        Optional<String> result = testProfile.getRedHatManagementKey();

        // Assert
        Assertions.assertEquals(expectedResult, result);
    }
}
