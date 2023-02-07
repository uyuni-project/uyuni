/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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

import org.cobbler.CobblerConnection;
import org.cobbler.Distro;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * @author paji
 */
public class DistroTest {
    private CobblerConnection client;
    private Distro testDistro;

    @BeforeEach
    public void setUp() {
        MockConnection.clear();
        client = new MockConnection("http://localhost", "token");
        String distroName = "testDistro";
        testDistro = new Distro.Builder<String>()
                .setName(distroName)
                .setKernel("kernel")
                .setInitrd("initrd")
                .setArch("architecture")
                .build(client);
    }

    @AfterEach
    public void teardown() {
        testDistro = null;
        MockConnection.clear();
    }

    @Test
    public void testDistroBuilder() {
        // Arrange
        String name = "Partha-Test";
        String kernel =
                "/var/satellite/rhn/kickstart/ks-rhel-i386-as-4-u2//images/pxeboot/vmlinuz";
        String initrd =
                "/var/satellite/rhn/kickstart/ks-rhel-i386-as-4-u2//images/pxeboot/initrd.img";
        String breed = "redhat";
        String osVersion = "rhel4";
        String arch = "i386";

        // Act
        Distro newDistro = new Distro.Builder<String>()
                .setName(name)
                .setKernel(kernel)
                .setInitrd(initrd)
                .setKsmeta(Optional.empty())
                .setBreed(breed)
                .setOsVersion(osVersion)
                .setArch(arch)
                .build(client);

        // Assert
        Assertions.assertEquals(name, newDistro.getName());
        Assertions.assertEquals(kernel, newDistro.getKernel());
        Assertions.assertEquals(initrd, newDistro.getInitrd());
    }

    @Test
    public void testOwnersRaw() {
        // Arrange
        Optional<List<String>> expectedRaw = Optional.of(Arrays.asList("test1", "test2"));

        // Act
        testDistro.setOwners(expectedRaw);
        Optional<List<String>> resultRaw = testDistro.getOwners();

        // Assert
        Assertions.assertEquals(expectedRaw, resultRaw);
    }

    @Test
    public void testOwnersResolved() {
        // Arrange
        List<String> expectedResolved = Arrays.asList("test1", "test2");

        // Act
        testDistro.setResolvedOwners(expectedResolved);
        List<String> resultResolved = testDistro.getResolvedOwners();

        // Assert
        Assertions.assertEquals(expectedResolved, resultResolved);
    }

    @Test
    public void testKernelOptions() {
        // Arrange
        Optional<Map<String, Object>> expectedResult = Optional.of(new HashMap<>());

        // Act
        testDistro.setKernelOptions(expectedResult);
        Optional<Map<String, Object>> result = testDistro.getKernelOptions();

        // Assert
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void testKernelOptionsPost() {
        // Arrange
        Optional<Map<String, Object>> expectedResult = Optional.of(new HashMap<>());

        // Act
        testDistro.setKernelOptionsPost(expectedResult);
        Optional<Map<String, Object>> result = testDistro.getKernelOptionsPost();

        // Assert
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void testAutoinstallMeta() {
        // Arrange
        Optional<Map<String, Object>> expectedResult = Optional.empty();

        // Act
        testDistro.setKsMeta(expectedResult);
        Optional<Map<String, Object>> result = testDistro.getKsMeta();

        // Assert
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void testRedhatManagementKey() {
        // Arrange
        Optional<String> expectedResult = Optional.of("");

        // Act
        Optional<String> result = testDistro.getRedHatManagementKey();

        // Assert
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void testComment() {
        // Arrange
        String expectedResult = "Testcomment";

        // Act
        testDistro.setComment(expectedResult);
        String result = testDistro.getComment();

        // Assert
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void testManagementClasses() {
        // Arrange
        Optional<List<String>> expectedResult = Optional.of(new ArrayList<>());

        // Act
        testDistro.setManagementClasses(expectedResult);
        Optional<List<String>> result = testDistro.getManagementClasses();

        // Assert
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void testUid() {
        // Arrange
        String expectedResult = "";
        // Act
        testDistro.setUid("");
        String result = testDistro.getUid();

        // Assert
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void testName() {
        // Arrange
        String expectedResult = "testname";

        // Act
        testDistro.setName(expectedResult);
        String result = testDistro.getName();

        // Assert
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void testCreated() {
        // Arrange
        Date expectedResult = new Calendar.Builder().setDate(2022, Calendar.JULY, 22).build().getTime();

        // Act
        testDistro.setCreated(expectedResult);
        Date result = testDistro.getCreated();

        // Assert
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void testModified() {
        // Arrange
        Date expectedResult = new Calendar.Builder().setDate(2022, Calendar.JULY, 22).build().getTime();

        // Act
        testDistro.setModified(expectedResult);
        Date result = testDistro.getModified();

        // Assert
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void testDepth() {
        // Arrange
        int expectedResult = 5;

        // Act
        testDistro.setDepth(5);
        int result = testDistro.getDepth();

        // Assert
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void testParent() {
        // Arrange
        String expectedResult = "";

        // Act
        testDistro.setParent("");
        String result = testDistro.getParent();

        // Assert
        Assertions.assertEquals(expectedResult, result);
    }
}
