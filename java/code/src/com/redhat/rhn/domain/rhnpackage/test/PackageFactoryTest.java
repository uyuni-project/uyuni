/**
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
package com.redhat.rhn.domain.rhnpackage.test;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageCapability;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.rhnpackage.PackageProvides;
import com.redhat.rhn.domain.rhnpackage.PackageSource;
import com.redhat.rhn.domain.server.InstalledPackage;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.channel.PackageSearchAction;
import com.redhat.rhn.frontend.dto.PackageOverview;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ErrataTestUtils;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * PackageFactoryTest
 * @version $Rev$
 */
public class PackageFactoryTest extends BaseTestCaseWithUser {

    /**
     * Test fetching a Package with the logged in User
     * @throws Exception something bad happened
     */
    public void testLookupWithUser() throws Exception {
        Package pkg = PackageTest.createTestPackage(user.getOrg());
        assertNotNull(pkg.getOrg().getId());

        User usr = UserTestUtils.createUser("testUser", pkg.getOrg().getId());
        Package pkg2 = PackageFactory.lookupByIdAndUser(pkg.getId(), usr);
        assertNotNull(pkg2);
        // Check to make sure it returns NULL
        // if we lookup with a User who isnt part of the
        // Org that owns that Action.  Ignore for
        // Sat mode since there is only one Org.
    }

    public void testLookupPackageArchByLabel() {
        assertNull(PackageFactory.lookupPackageArchByLabel("biteme-arch"));
        assertNotNull(PackageFactory.lookupPackageArchByLabel("i386"));
    }

    public void testLookupByNameAndServer() throws Exception {
        Server testServer = ServerFactoryTest.createTestServer(user, true);

        Channel channel = ChannelFactoryTest.createBaseChannel(user);
        testServer.addChannel(channel);

        Package testPackage = PackageTest.createTestPackage(user.getOrg());

        //Test a package the satellite knows about
        InstalledPackage testInstPack = new InstalledPackage();
        testInstPack.setArch(testPackage.getPackageArch());
        testInstPack.setEvr(testPackage.getPackageEvr());
        testInstPack.setName(testPackage.getPackageName());
        testInstPack.setServer(testServer);
        Set<InstalledPackage> serverPackages = testServer.getPackages();
        serverPackages.add(testInstPack);

        ServerFactory.save(testServer);
        testServer = (Server) reload(testServer);

        InstalledPackage pack = PackageFactory.lookupByNameAndServer(
                testInstPack.getName().getName(), testServer);

        assertEquals(testInstPack, pack);
    }

    public void testPackageSearch() {
        List<Long> pids = new ArrayList<Long>();
        pids.add(2125L);
        pids.add(2915L);
        List<String> arches = new ArrayList<String>();
        arches.add("channel-ia32");
        arches.add("channel-ia64");

        List<PackageOverview> results =
                PackageFactory.packageSearch(pids, arches, user.getId(), null,
                        PackageSearchAction.ARCHITECTURE);
        assertNotNull(results);
    }

    public void testPackageDelete() throws Exception {
        Package pkg = PackageTest.createTestPackage(user.getOrg());
        Long id = pkg.getId();
        Org org = pkg.getOrg();
        com.redhat.rhn.testing.TestUtils.flushAndEvict(pkg);
        pkg = PackageFactory.lookupByIdAndOrg(id, org);
        PackageFactory.deletePackage(pkg);

        HibernateFactory.getSession().flush();

    }


   public void testPackageSourceLookup() throws Exception {
       Package pack = PackageTest.createTestPackage(user.getOrg());

       List<PackageSource> list = PackageFactory.lookupPackageSources(pack);
       assertTrue(list.size() > 0);

   }

   public void testFindMissingProductPackageOnServer() throws Exception {
       Server testServer = ServerFactoryTest.createTestServer(user, true);

       Channel channel = ChannelFactoryTest.createBaseChannel(user);
       testServer.addChannel(channel);

       // Create an installed package so the Server package list won't be empty
       ErrataTestUtils.createTestInstalledPackage(ErrataTestUtils.createTestPackage(user, channel, "x86_64"),
               testServer);

       Package testPackage = ErrataTestUtils.createTestPackage(user, channel, "x86_64");
       PackageCapability productCap = PackageCapabilityTest.createTestCapability("product()");
       PackageProvides provideProduct = new PackageProvides();
       provideProduct.setCapability(productCap);
       provideProduct.setPack(testPackage);
       provideProduct.setSense(0L);
       TestUtils.saveAndFlush(provideProduct);

       ServerFactory.save(testServer);
       testServer = (Server) reload(testServer);

       List<Package> missing = PackageFactory.
               findMissingProductPackagesOnServer(testServer.getId());

       assertEquals(1, missing.size());
       assertEquals(testPackage.getNameEvra() ,missing.get(0).getNameEvra());
   }

    public void testFindMissingProductPackageNoPackageProfile() throws Exception {
        Server testServer = ServerFactoryTest.createTestServer(user, true);

        Channel channel = ChannelFactoryTest.createBaseChannel(user);
        testServer.addChannel(channel);

        Package testPackage = ErrataTestUtils.createTestPackage(user, channel, "x86_64");
        PackageCapability productCap = PackageCapabilityTest.createTestCapability("product()");
        PackageProvides provideProduct = new PackageProvides();
        provideProduct.setCapability(productCap);
        provideProduct.setPack(testPackage);
        provideProduct.setSense(0L);
        TestUtils.saveAndFlush(provideProduct);

        ServerFactory.save(testServer);
        testServer = (Server) reload(testServer);

        List<Package> missing = PackageFactory.
                findMissingProductPackagesOnServer(testServer.getId());

        // Since there is no packages in the server (no package profile data available),
        // no missing products will be reported
        assertEquals(0, missing.size());
    }

   public void testInstalledProductPackage() throws Exception {
       Server testServer = ServerFactoryTest.createTestServer(user, true);

       Channel channel = ChannelFactoryTest.createBaseChannel(user);
       testServer.addChannel(channel);

       Package testPackage = ErrataTestUtils.createTestPackage(user, channel, "x86_64");
       PackageCapability productCap = PackageCapabilityTest.createTestCapability("product()");
       PackageProvides provideProduct = new PackageProvides();
       provideProduct.setCapability(productCap);
       provideProduct.setPack(testPackage);
       provideProduct.setSense(0L);
       TestUtils.saveAndFlush(provideProduct);

       InstalledPackage testInstPack = new InstalledPackage();
       testInstPack.setArch(testPackage.getPackageArch());
       testInstPack.setEvr(testPackage.getPackageEvr());
       testInstPack.setName(testPackage.getPackageName());
       testInstPack.setServer(testServer);
       Set<InstalledPackage> serverPackages = testServer.getPackages();
       serverPackages.add(testInstPack);

       ServerFactory.save(testServer);
       testServer = (Server) reload(testServer);

       List<Package> missing = PackageFactory.
               findMissingProductPackagesOnServer(testServer.getId());

       assertEquals(0, missing.size());
   }

   public void testInstalledProductPackageProvidesOtherProduct() throws Exception {
       Server testServer = ServerFactoryTest.createTestServer(user, true);

       Channel channel = ChannelFactoryTest.createBaseChannel(user);
       testServer.addChannel(channel);


       Package testPackage1 = ErrataTestUtils.createTestPackage(user, channel, "x86_64");
       PackageCapability productCap = PackageCapabilityTest.createTestCapability("product()");

       PackageProvides provideProduct = new PackageProvides();
       provideProduct.setCapability(productCap);
       provideProduct.setPack(testPackage1);
       provideProduct.setSense(0L);

       TestUtils.saveAndFlush(provideProduct);

       Package testPackage2 = ErrataTestUtils.createTestPackage(user, channel, "x86_64");

       PackageProvides provideProduct2 = new PackageProvides();
       provideProduct2.setCapability(productCap);
       provideProduct2.setPack(testPackage2);
       provideProduct2.setSense(0L);

       PackageCapability pkg1Cap = PackageCapabilityTest.createTestCapability(
               testPackage1.getPackageName().getName());

       PackageProvides provideProduct3 = new PackageProvides();
       provideProduct3.setCapability(pkg1Cap);
       provideProduct3.setPack(testPackage2);
       provideProduct3.setSense(0L);

       TestUtils.saveAndFlush(provideProduct3);

       InstalledPackage testInstPack = new InstalledPackage();
       testInstPack.setArch(testPackage2.getPackageArch());
       testInstPack.setEvr(testPackage2.getPackageEvr());
       testInstPack.setName(testPackage2.getPackageName());
       testInstPack.setServer(testServer);
       Set<InstalledPackage> serverPackages = testServer.getPackages();
       serverPackages.add(testInstPack);

       ServerFactory.save(testServer);
       testServer = (Server) reload(testServer);

       List<Package> missing = PackageFactory.
               findMissingProductPackagesOnServer(testServer.getId());

       assertEquals(0, missing.size());
   }

   public void testInstalledProductPackageProvidesOtherProduct2() throws Exception {
       Server testServer = ServerFactoryTest.createTestServer(user, true);

       Channel channel = ChannelFactoryTest.createBaseChannel(user);
       testServer.addChannel(channel);


       Package testPackage1 = ErrataTestUtils.createTestPackage(user, channel, "x86_64");
       PackageCapability productCap = PackageCapabilityTest.createTestCapability("product()");

       PackageProvides provideProduct = new PackageProvides();
       provideProduct.setCapability(productCap);
       provideProduct.setPack(testPackage1);
       provideProduct.setSense(0L);
       TestUtils.saveAndFlush(provideProduct);

       Package testPackage2 = ErrataTestUtils.createTestPackage(user, channel, "x86_64");

       PackageProvides provideProduct2 = new PackageProvides();
       provideProduct2.setCapability(productCap);
       provideProduct2.setPack(testPackage2);
       provideProduct2.setSense(0L);
       TestUtils.saveAndFlush(provideProduct2);

       PackageCapability pkg1Cap = PackageCapabilityTest.createTestCapability(
               testPackage1.getPackageName().getName());

       PackageProvides provideProduct3 = new PackageProvides();
       provideProduct3.setCapability(pkg1Cap);
       provideProduct3.setPack(testPackage2);
       provideProduct3.setSense(0L);
       TestUtils.saveAndFlush(provideProduct3);

       InstalledPackage testInstPack = new InstalledPackage();
       testInstPack.setArch(testPackage1.getPackageArch());
       testInstPack.setEvr(testPackage1.getPackageEvr());
       testInstPack.setName(testPackage1.getPackageName());
       testInstPack.setServer(testServer);
       Set<InstalledPackage> serverPackages = testServer.getPackages();
       serverPackages.add(testInstPack);

       ServerFactory.save(testServer);
       testServer = (Server) reload(testServer);

       List<Package> missing = PackageFactory.
               findMissingProductPackagesOnServer(testServer.getId());

       assertEquals(1, missing.size());
       assertEquals(testPackage2.getNameEvra() ,missing.get(0).getNameEvra());
   }
}

