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

package com.redhat.rhn.manager.rhnpackage.test;

import static com.redhat.rhn.manager.channel.CloneChannelCommand.CloneBehavior.CURRENT_STATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.errata.AdvisoryStatus;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.ErrataFactory;
import com.redhat.rhn.domain.errata.test.ErrataFactoryTest;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.frontend.dto.PackageDto;
import com.redhat.rhn.frontend.dto.PackageListItem;
import com.redhat.rhn.frontend.dto.PackageOverview;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.channel.CloneChannelCommand;
import com.redhat.rhn.manager.rhnpackage.PackageManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ChannelTestUtils;
import com.redhat.rhn.testing.PackageTestUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Test various logic related to the retracted packages
 */
public class PackageManagerRetractedTest extends BaseTestCaseWithUser {

    private Server server;
    private Package oldPkg;
    private Package newerPkg;
    private Package newestPkg;
    private Channel channel;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        server = ServerFactoryTest.createTestServer(user, true);

        List<Package> generatedPackages = PackageTestUtils.createSubsequentPackages(user.getOrg());
        oldPkg = generatedPackages.get(0);
        newerPkg = generatedPackages.get(1);
        newestPkg = generatedPackages.get(2);

        channel = ChannelTestUtils.createBaseChannel(user);
        channel.setChecksumType(ChannelFactory.findChecksumTypeByLabel("sha256"));
    }

    /**
     * Scenario:
     * - channel with retracted patch and a package
     * - its clone with non-retracted patch and the same package
     *
     * If the system has the package installed and is subscribed to the original,
     * the package list should show the package as "retracted".
     *
     * If the system is subscribed to the cloned channel, the package shouldn't show up as "retracted".
     *
     * @throws Exception
     */
    @Test
    public void testSystemPackageList() throws Exception {
        // create a null-org patch with a package and add it to the channel
        Errata vendorPatch = ErrataFactoryTest.createTestErrata(null);
        vendorPatch.addPackage(oldPkg);
        vendorPatch.addChannel(channel);
        ErrataFactory.save(vendorPatch);

        channel.addPackage(oldPkg);

        // clone the channel
        CloneChannelCommand ccc = new CloneChannelCommand(CURRENT_STATE, channel);
        ccc.setUser(user);
        Channel clonedChannel = ccc.create();

        // set the patch in original to retracted
        vendorPatch.setAdvisoryStatus(AdvisoryStatus.RETRACTED);

        // install the package & subcribe the system to the original cnl (with retracted errata)
        PackageTestUtils.installPackageOnServer(oldPkg, server);
        SystemManager.subscribeServerToChannel(user, server, channel);

        // refresh the newest package cache
        ChannelFactory.refreshNewestPackageCache(channel, "java::test");
        ChannelFactory.refreshNewestPackageCache(clonedChannel, "java::test");

        // verify the package is retracted
        DataResult<PackageListItem> pkgs = PackageManager.systemPackageList(server.getId(), null);
        pkgs.elaborate();
        PackageListItem pkg = assertSingleAndGet(pkgs);
        assertTrue(pkg.isRetracted());

        // now let's subscribe the system to the cloned channel, where the patch is not retracted
        SystemManager.unsubscribeServerFromChannel(server, channel);
        SystemManager.subscribeServerToChannel(user, server, clonedChannel);
        pkgs = PackageManager.systemPackageList(server.getId(), null);
        pkgs.elaborate();
        pkg = assertSingleAndGet(pkgs);
        assertFalse(pkg.isRetracted()); // the package is now NOT retracted for this server!
    }

    @Test
    public void testSystemAvailablePackages() throws Exception {
        // create a null-org patch with a newest package and add it to the channel
        Errata vendorPatch = ErrataFactoryTest.createTestErrata(null);
        vendorPatch.addPackage(newestPkg);
        vendorPatch.addChannel(channel);
        ErrataFactory.save(vendorPatch);

        // channel has all 3 packages
        channel.getPackages().addAll(List.of(oldPkg, newerPkg, newestPkg));

        // clone the channel
        CloneChannelCommand ccc = new CloneChannelCommand(CURRENT_STATE, channel);
        ccc.setUser(user);
        Channel clonedChannel = ccc.create();

        // set the patch in original to retracted
        vendorPatch.setAdvisoryStatus(AdvisoryStatus.RETRACTED);

        // subscribe the system to the channel with the retracted patch
        // the newest installable package should be the "newerPkg", since the "newestPkg" is retracted
        SystemManager.subscribeServerToChannel(user, server, channel);

        // refresh the newest package cache
        ChannelFactory.refreshNewestPackageCache(channel, "java::test");
        ChannelFactory.refreshNewestPackageCache(clonedChannel, "java::test");

        PackageListItem pkg = assertSingleAndGet(PackageManager.systemAvailablePackages(server.getId(), null));
        assertEquals(newerPkg.getId(), pkg.getPackageId());

        // now subscribe to the clone, where the patch is not retracted
        // the newest package should be "newestPkg" now
        SystemManager.unsubscribeServerFromChannel(server, channel);
        SystemManager.subscribeServerToChannel(user, server, clonedChannel);
        pkg = assertSingleAndGet(PackageManager.systemAvailablePackages(server.getId(), null));
        assertEquals(newestPkg.getId(), pkg.getPackageId());
    }

    @Test
    public void testListPackagesInChannelForList() throws Exception {
        Errata vendorPatch = ErrataFactoryTest.createTestErrata(null);
        vendorPatch.addPackage(newerPkg);
        vendorPatch.addChannel(channel);
        ErrataFactory.save(vendorPatch);

        // channel has all 3 packages
        channel.getPackages().addAll(List.of(oldPkg, newerPkg, newestPkg));

        // clone the channel
        CloneChannelCommand ccc = new CloneChannelCommand(CURRENT_STATE, channel);
        ccc.setUser(user);
        Channel clonedChannel = ccc.create();

        // set the patch in original to retracted
        vendorPatch.setAdvisoryStatus(AdvisoryStatus.RETRACTED);

        // refresh the newest package cache
        ChannelFactory.refreshNewestPackageCache(channel, "java::test");
        ChannelFactory.refreshNewestPackageCache(clonedChannel, "java::test");

        // only the "newerPkg" is retracted in the original channel
        DataResult<PackageOverview> pkgsOriginal = PackageManager.listPackagesInChannelForList(channel.getId());
        Map<Long, PackageOverview> pkgsOriginalMap = pkgsOriginal.stream()
                .collect(Collectors.toMap(PackageOverview::getId, p -> p));
        assertFalse(pkgsOriginalMap.get(oldPkg.getId()).getRetracted());
        assertTrue(pkgsOriginalMap.get(newerPkg.getId()).getRetracted());
        assertFalse(pkgsOriginalMap.get(newestPkg.getId()).getRetracted());

        // no package retracted in the cloned channel
        pkgsOriginal = PackageManager.listPackagesInChannelForList(clonedChannel.getId());
        pkgsOriginalMap = pkgsOriginal.stream().collect(Collectors.toMap(PackageOverview::getId, p -> p));
        assertFalse(pkgsOriginalMap.get(oldPkg.getId()).getRetracted());
        assertFalse(pkgsOriginalMap.get(newerPkg.getId()).getRetracted());
        assertFalse(pkgsOriginalMap.get(newestPkg.getId()).getRetracted());
    }

    @Test
    public void testChannelListAllPackages() throws Exception {
        Errata vendorPatch = ErrataFactoryTest.createTestErrata(null);
        vendorPatch.addPackage(newerPkg);
        vendorPatch.addChannel(channel);
        ErrataFactory.save(vendorPatch);

        // channel has all 3 packages
        channel.getPackages().addAll(List.of(oldPkg, newerPkg, newestPkg));

        // clone the channel
        CloneChannelCommand ccc = new CloneChannelCommand(CURRENT_STATE, channel);
        ccc.setUser(user);
        Channel clonedChannel = ccc.create();

        // set the patch in original to retracted
        vendorPatch.setAdvisoryStatus(AdvisoryStatus.RETRACTED);

        // refresh the newest package cache
        ChannelFactory.refreshNewestPackageCache(channel, "java::test");
        ChannelFactory.refreshNewestPackageCache(clonedChannel, "java::test");

        Map<Long, PackageDto> pkgsOriginalMap = ChannelManager.listAllPackages(channel).stream()
                .collect(Collectors.toMap(PackageDto::getId, p -> p));
        assertEquals(3, pkgsOriginalMap.size());
        assertFalse(pkgsOriginalMap.get(oldPkg.getId()).getRetracted());
        assertTrue(pkgsOriginalMap.get(newerPkg.getId()).getRetracted());
        assertFalse(pkgsOriginalMap.get(newestPkg.getId()).getRetracted());


        Map<Long, PackageDto> pkgsCloneMap = ChannelManager.listAllPackages(clonedChannel).stream()
                .collect(Collectors.toMap(PackageDto::getId, p -> p));
        assertEquals(3, pkgsCloneMap.size());
        assertFalse(pkgsCloneMap.get(oldPkg.getId()).getRetracted());
        assertFalse(pkgsCloneMap.get(newerPkg.getId()).getRetracted());
        assertFalse(pkgsCloneMap.get(newestPkg.getId()).getRetracted());
    }

    @Test
    public void testPotentialSystemsForPackage() throws Exception {
        Errata vendorPatch = ErrataFactoryTest.createTestErrata(null);
        vendorPatch.addPackage(newestPkg);
        vendorPatch.addChannel(channel);
        ErrataFactory.save(vendorPatch);

        // channel has all 3 packages
        channel.getPackages().addAll(List.of(oldPkg, newerPkg, newestPkg));

        // clone the channel
        CloneChannelCommand ccc = new CloneChannelCommand(CURRENT_STATE, channel);
        ccc.setUser(user);
        Channel clonedChannel = ccc.create();

        // set the patch in original to retracted
        vendorPatch.setAdvisoryStatus(AdvisoryStatus.RETRACTED);

        PackageTestUtils.installPackageOnServer(oldPkg, server);
        SystemManager.subscribeServerToChannel(user, server, channel);

        // refresh the newest package cache
        ChannelFactory.refreshNewestPackageCache(channel, "java::test");
        ChannelFactory.refreshNewestPackageCache(clonedChannel, "java::test");

        // list the possible package updates when subscribed to the original
        assertSingleAndGet(SystemManager.listPotentialSystemsForPackage(user, newerPkg.getId()));
        // newest is retracted
        assertTrue(SystemManager.listPotentialSystemsForPackage(user, newestPkg.getId()).isEmpty());

        // list the possible package updates when subscribed to the clone
        SystemManager.unsubscribeServerFromChannel(server, channel);
        SystemManager.subscribeServerToChannel(user, server, clonedChannel);
        assertSingleAndGet(SystemManager.listPotentialSystemsForPackage(user, newerPkg.getId()));
        assertSingleAndGet(SystemManager.listPotentialSystemsForPackage(user, newestPkg.getId()));
    }

    private <T> T assertSingleAndGet(Collection<T> items) {
        assertEquals(1, items.size());
        return items.iterator().next();
    }

}
