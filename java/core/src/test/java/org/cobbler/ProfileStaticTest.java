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

package org.cobbler;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;

public class ProfileStaticTest {

    private MockConnection connectionMock;

    @BeforeEach
    public void setUp() {
        connectionMock = new MockConnection("http://localhost", "token");
    }

    @AfterEach
    public void teardown() {
        MockConnection.clear();
    }

    @Test
    public void testProfileCreate() {
        // Arrange
        String distroName = "testProfileCreate";
        Distro testDistro = new Distro.Builder<String>()
                .setName(distroName)
                .setKernel("kernel")
                .setInitrd("initrd")
                .setArch("arch")
                .build(connectionMock);

        // Act
        Profile result = Profile.create(connectionMock, "", testDistro);

        // Assert
        Assertions.assertEquals(result.getDistro().getName(), distroName);
    }

    @Test
    public void testProfileLookupByName() {
        // Arrange
        String distroName = "testProfileCreate";
        String profileName = "testProfileCreate";
        Distro testDistro = new Distro.Builder<String>()
                .setName(distroName)
                .setKernel("kernel")
                .setInitrd("initrd")
                .setArch("arch")
                .build(connectionMock);
        Profile.create(connectionMock, profileName, testDistro);

        // Act
        Profile result = Profile.lookupByName(connectionMock, profileName);

        // Assert
        Assertions.assertEquals(profileName, result.getName());
    }

    @Test
    public void testProfileLookupById() {
        // Arrange

        // Act
        Profile result = Profile.lookupById(connectionMock, "asdfasdfa");

        // Assert
        Assertions.assertNull(result);
    }

    @Test
    public void testList() {
        // Arrange

        // Act
        List<Profile> result = Profile.list(connectionMock);

        // Assert
        Assertions.assertEquals(new LinkedList<>(), result);
    }
}
