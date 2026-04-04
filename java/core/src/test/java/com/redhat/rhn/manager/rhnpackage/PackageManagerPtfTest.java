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

package com.redhat.rhn.manager.rhnpackage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.rhnpackage.PackageTest;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerFactoryTest;
import com.redhat.rhn.frontend.dto.PackageListItem;
import com.redhat.rhn.frontend.dto.UpgradablePackageListItem;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ChannelTestUtils;
import com.redhat.rhn.testing.PackageTestUtils;
import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

public class PackageManagerPtfTest extends BaseTestCaseWithUser {
    private Server server;

    private Package pkg;

    private Package ptfPackage;

    private Package updatedPtfPackage;

    private Package ptfMaster;

    private Package updatedPtfMaster;

    private Package standard;

    private Package updatedStandard;

    private Channel channel;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        server = ServerFactoryTest.createTestServer(user);

        // The original package with a bug
        pkg = PackageTest.createTestPackage(user.getOrg());

        // The first version ptf addressing the issue
        ptfMaster = PackageTestUtils.createPtfMaster("12987", "1", user.getOrg());
        ptfPackage = PackageTestUtils.createPtfPackage(pkg, "12987", "1", user.getOrg());

        // The second version of the same ptf
        updatedPtfMaster = PackageTestUtils.createPtfMaster("12987", "2", user.getOrg());
        updatedPtfPackage = PackageTestUtils.createPtfPackage(pkg, "12987", "2", user.getOrg());

        // A normal package unrelatad to the ptf
        standard = PackageTest.createTestPackage(user.getOrg());
        updatedStandard = PackageTestUtils.newVersionOfPackage(standard, null, "3.0.0", null, user.getOrg());

        channel = ChannelTestUtils.createBaseChannel(user);
        channel.setChecksumType(ChannelFactory.findChecksumTypeByLabel("sha256"));

        SystemManager.subscribeServerToChannel(user, server, channel);
        server = TestUtils.saveAndReload(server);
    }

    @Test
    public void testSystemAvailablePackageList() {
        // Add the packages to the channel
        channel.getPackages().addAll(List.of(pkg, ptfPackage, ptfMaster));
        channel = TestUtils.saveAndReload(channel);
        ChannelFactory.refreshNewestPackageCache(channel, "java::test");

        DataResult<PackageListItem> installablePackages = PackageManager.systemAvailablePackages(server.getId(), null);


        assertNotNull(installablePackages);
        // There should be two installable package. The standard one and the PTF.
        assertEquals(2, installablePackages.size());
        // No packages should be part of a PTF
        assertEquals(0L, installablePackages.stream()
                                            .map(e -> PackageFactory.lookupByIdAndUser(e.getPackageId(), user))
                                            .filter(Package::isPartOfPtf)
                                            .count());
    }

    @Test
    public void testUpgradable() {
        channel.getPackages().addAll(
            List.of(pkg, standard, ptfPackage, ptfMaster, updatedPtfPackage, updatedPtfMaster, updatedStandard)
        );
        channel = TestUtils.saveAndReload(channel);

        PackageTestUtils.installPackagesOnServer(List.of(ptfPackage, ptfMaster, standard), server);

        ChannelFactory.refreshNewestPackageCache(channel, "java::test");
        ServerFactory.updateServerNeededCache(server.getId());

        DataResult<UpgradablePackageListItem> upgradablePackages = PackageManager.upgradable(server.getId(), null);

        assertNotNull(upgradablePackages);
        // The ptf and the unrelated package should be both upgradable
        assertEquals(2, upgradablePackages.size());

        // Convert to database packages
        List<Package> packages = upgradablePackages.stream()
                                                   .flatMap(e -> PackageFactory.lookupByNevraIds(user.getOrg(),
                                                       e.getNameId(), e.getEvrId(), e.getArchId()).stream())
                                                   .filter(Objects::nonNull)
                                                   .toList();

        // There should be still two packages
        assertEquals(2, packages.size());

        // Only one should be a PTF
        assertEquals(1L, packages.stream().filter(Package::isMasterPtfPackage).count());
        // No packages should be part of a PTF
        assertEquals(0L, packages.stream().filter(Package::isPartOfPtf).count());
    }

    @Test
    public void testSystemInstalledPtfList() {
        // Add the packages to the channel
        channel.getPackages().addAll(List.of(pkg, ptfPackage, ptfMaster));
        channel = TestUtils.saveAndReload(channel);
        ChannelFactory.refreshNewestPackageCache(channel, "java::test");

        PackageTestUtils.installPackagesOnServer(List.of(ptfPackage, ptfMaster, standard), server);

        DataResult<PackageListItem> installedPtf = PackageManager.systemPtfList(server.getId(), null);

        assertNotNull(installedPtf);
        // There should be one installed ptf
        assertEquals(1, installedPtf.size());
        // The package retrieved must be a master ptf package
        assertEquals(0L, installedPtf.stream()
                                            .map(e -> PackageFactory.lookupByIdAndUser(e.getPackageId(), user))
                                            .filter(p -> !p.isMasterPtfPackage())
                                            .count());
    }
    @Test
    public void testSystemAvailablePtfList() {
        // Add the packages to the channel
        channel.getPackages().addAll(List.of(pkg, ptfPackage, ptfMaster));
        channel = TestUtils.saveAndReload(channel);
        ChannelFactory.refreshNewestPackageCache(channel, "java::test");

        DataResult<PackageListItem> installablePackages = PackageManager.systemAvailablePtf(server.getId(), null);


        assertNotNull(installablePackages);
        // There should be one installable ptf
        assertEquals(1, installablePackages.size());
        // The package retrieved must be a master ptf package
        assertEquals(0L, installablePackages.stream()
                                            .map(e -> PackageFactory.lookupByIdAndUser(e.getPackageId(), user))
                                            .filter(p -> !p.isMasterPtfPackage())
                                            .count());
    }

}

