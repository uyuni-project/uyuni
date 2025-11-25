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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.frontend.action.rhnpackage.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageEvrFactory;
import com.redhat.rhn.domain.rhnpackage.PackageType;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerConstants;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.frontend.action.rhnpackage.PackageListSetupAction;
import com.redhat.rhn.frontend.dto.PackageListItem;
import com.redhat.rhn.manager.rhnpackage.test.PackageManagerTest;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.testing.PackageTestUtils;
import com.redhat.rhn.testing.RhnMockStrutsTestCase;
import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

/**
 * PackageListSetupTest
 */
public class PackageListSetupTest extends RhnMockStrutsTestCase {

    @Test
    public void testExecute() throws Exception {
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        Server server = ServerFactoryTest.createTestServer(user, true);
        PackageManagerTest.addPackageToSystemAndChannel(
                "test-package-name" + TestUtils.randomString(), server,
                ChannelFactoryTest.createTestChannel(user));
        server = TestUtils.reload(server);
        //.do?sid=1000010000
        setRequestPathInfo("/systems/details/packages/PackageList");
        addRequestParameter("sid", server.getId().toString());
        actionPerform();
        verifyList(PackageListSetupAction.DATA_SET, PackageListItem.class);
    }

    @ParameterizedTest(name = "{0}, {1} -> {2}")
    @MethodSource("executeWithPtfArguments")
    public void testExecuteWithPtf(String osVersion, String zypperVersion, boolean uninstallationSupported)
        throws Exception {
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        Server server = ServerFactoryTest.createTestServer(user, true);
        Channel channel = ChannelFactoryTest.createTestChannel(user);

        Package standard = PackageTest.createTestPackage(user.getOrg());
        standard.setDescription("Standard package");

        Package ptfMaster = PackageTestUtils.createPtfMaster("123456", "1", user.getOrg());
        Package ptfPackage = PackageTestUtils.createPtfPackage("123456", "1", user.getOrg());

        // Set an OS that does not support PTFs uninstallation
        server.setOs(ServerConstants.SLES);
        server.setRelease(osVersion);

        Package zypperPackage = PackageTest.createTestPackage(user.getOrg(), "zypper");
        PackageEvr zyppEvr = PackageEvrFactory.lookupOrCreatePackageEvr(null, zypperVersion, "0", PackageType.RPM);

        zypperPackage.setPackageEvr(zyppEvr);
        zypperPackage = TestUtils.saveAndReload(zypperPackage);

        channel.getPackages().addAll(List.of(standard, ptfMaster, ptfPackage, zypperPackage));
        channel = TestUtils.saveAndReload(channel);

        SystemManager.subscribeServerToChannel(user, server, channel);
        PackageTestUtils.installPackagesOnServer(List.of(standard, ptfMaster, ptfPackage, zypperPackage), server);

        server = TestUtils.reload(server);

        assertFalse(ptfPackage.isMasterPtfPackage());
        assertTrue(ptfPackage.isPartOfPtf());

        assertTrue(ptfMaster.isMasterPtfPackage());
        assertFalse(ptfMaster.isPartOfPtf());

        //.do?sid=1000010000
        setRequestPathInfo("/systems/details/packages/PackageList");
        addRequestParameter("sid", server.getId().toString());
        actionPerform();

        @SuppressWarnings("unchecked")
        List<PackageListItem> result = (List<PackageListItem>) request.getAttribute(PackageListSetupAction.DATA_SET);
        assertNotNull(result, "The result list is null");
        assertTrue(result.size() > 0, "Your result list is empty");

        // Check the status for the normal package
        String standardPackageName = standard.getPackageName().getName();
        PackageListItem standardItem = result.stream()
                                             .filter(pli -> standardPackageName.equals(pli.getName()))
                                             .findFirst().orElse(null);
        assertNotNull(standardItem, "No entry in the list for the standard package");
        assertTrue(standardItem.isSelectable());

        // Check the status for the ptf package
        String ptfPackageName = ptfPackage.getPackageName().getName();
        PackageListItem ptfItem = result.stream()
                                        .filter(pli -> ptfPackageName.equals(pli.getName()))
                                        .findFirst().orElse(null);
        assertNotNull(ptfItem, "No entry in the list for the ptf package");
        assertFalse(ptfItem.isMasterPtfPackage());
        assertTrue(ptfItem.isPartOfPtf());
        assertFalse(ptfItem.isSelectable());

        // Check the status for the ptf package
        String ptfMasterName = ptfMaster.getPackageName().getName();
        PackageListItem ptfMasterItem = result.stream()
                                        .filter(pli -> ptfMasterName.equals(pli.getName()))
                                        .findFirst().orElse(null);
        assertNotNull(ptfMasterItem, "No entry in the list for the ptf master package");
        assertTrue(ptfMasterItem.isMasterPtfPackage());
        assertFalse(ptfMasterItem.isPartOfPtf());
        assertTrue(ptfMasterItem.isSelectable() == uninstallationSupported);
    }

    private static Stream<Arguments> executeWithPtfArguments() {
        return Stream.of(
            Arguments.of("12", "1.12.76", false),
            Arguments.of("15", "1.14.57", false),
            Arguments.of("15", "1.14.59", true),
            Arguments.of("15", "1.15.27", true)
        );
    }
}
