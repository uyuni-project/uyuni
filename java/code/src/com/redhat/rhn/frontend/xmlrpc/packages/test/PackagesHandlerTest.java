/*
 * Copyright (c) 2009--2016 Red Hat, Inc.
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
package com.redhat.rhn.frontend.xmlrpc.packages.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.FaultException;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageSource;
import com.redhat.rhn.domain.rhnpackage.test.PackageFactoryTest;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.domain.rpm.SourceRpm;
import com.redhat.rhn.domain.rpm.test.SourceRpmTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.packages.PackagesHandler;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;
import com.redhat.rhn.testing.UserTestUtils;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PackagesHandlerTest extends BaseHandlerTestCase {

    private final PackagesHandler handler = new PackagesHandler();

    @Test
    public void testGetDetails() {

        Package pkg = PackageTest.createTestPackage(admin.getOrg());
        assertNotNull(pkg.getOrg().getId());

        Map details = handler.getDetails(admin, pkg.getId().intValue());
        assertNotNull(details);
        assertTrue(details.containsKey("name"));

        try {
            handler.getDetails(admin, -213344);
            fail("handler.getDetails didn't throw FaultException for non-existant package");
        }
        catch (FaultException e) {
            //success
        }
    }


    @Test
    public void testListFiles() {
        User user = UserTestUtils.createUser("testUser", admin.getOrg().getId());
        Package pkg = PackageTest.createTestPackage(user.getOrg());

        Object[] files = handler.listFiles(admin,
                pkg.getId().intValue());

        // PackageTest.populateTestPackage populates a test package with 2 associated files
        assertEquals(2, files.length);


        //TODO: Once we work out the mappings between packages -> files -> capabilities
        //we should do some more exhaustive testing of this method.
    }

    @Test
    public void testListProvidingErrata() {
        User user = UserTestUtils.createUser("testUser", admin.getOrg().getId());
        Package pkg = PackageTest.createTestPackage(user.getOrg());

        Object[] result = handler.listProvidingErrata(admin,
                pkg.getId().intValue());
        assertEquals(0, result.length);
    }

    @Test
    public void testListProvidingChannels() {
        User user = UserTestUtils.createUser("testUser", admin.getOrg().getId());
        Package pkg = PackageTest.createTestPackage(user.getOrg());

        Object[] result = handler.listProvidingChannels(admin,
                pkg.getId().intValue());
        //test package shouldn't be "provided" by any channel yet
        assertEquals(0, result.length);
    }

    @Test
    public void testListDependencies() throws Exception {
        User user = UserTestUtils.createUser("testUser", admin.getOrg().getId());
        Package pkg = PackageTest.createTestPackage(user.getOrg());

        PackageFactoryTest.createPackageProperties(pkg);
        Object[] result = handler.listDependencies(admin, pkg.getId().intValue());

        assertEquals(10, result.length);

        Map res = getDependencyByType(result, "requires");
        assertEquals("depRequires", res.get("dependency"));
        assertEquals(" -1.0", res.get("dependency_modifier"));

        res = getDependencyByType(result, "provides");
        assertEquals("depProvides", res.get("dependency"));
        assertEquals(" -1.0", res.get("dependency_modifier"));

        res = getDependencyByType(result, "obsoletes");
        assertEquals("depObsoletes", res.get("dependency"));
        assertEquals(" -1.0", res.get("dependency_modifier"));

        res = getDependencyByType(result, "conflicts");
        assertEquals("depConflicts", res.get("dependency"));
        assertEquals(" -1.0", res.get("dependency_modifier"));

        res = getDependencyByType(result, "recommends");
        assertEquals("depRecommends", res.get("dependency"));
        assertEquals(" -1.0", res.get("dependency_modifier"));

        res = getDependencyByType(result, "suggests");
        assertEquals("depSuggests", res.get("dependency"));
        assertEquals(" -1.0", res.get("dependency_modifier"));

        res = getDependencyByType(result, "supplements");
        assertEquals("depSupplements", res.get("dependency"));
        assertEquals(" -1.0", res.get("dependency_modifier"));

        res = getDependencyByType(result, "enhances");
        assertEquals("depEnhances", res.get("dependency"));
        assertEquals(" -1.0", res.get("dependency_modifier"));

        res = getDependencyByType(result, "predepends");
        assertEquals("depPreDepends", res.get("dependency"));
        assertEquals(" -1.0", res.get("dependency_modifier"));

        res = getDependencyByType(result, "breaks");
        assertEquals("depBreaks", res.get("dependency"));
        assertEquals(" -1.0", res.get("dependency_modifier"));
    }

    private Map getDependencyByType(Object[] result, String type) {
        return Arrays.asList(result).stream()
                .filter(e -> type.equals(((Map)e).get("dependency_type")))
                .map(e -> (Map)e)
                .findFirst().orElseThrow();
    }

    @Test
    public void testRemovePackage() {
        User user = UserTestUtils.createUser("testUser", admin.getOrg().getId());
        Package pkg = PackageTest.createTestPackage(user.getOrg());
        handler.removePackage(admin, pkg.getId().intValue());
    }

    @Test
    public void testRemovePackageSource() {
        User user = UserTestUtils.createUser("testUser", admin.getOrg().getId());
        SourceRpm srpm = SourceRpmTest.createTestSourceRpm();
        PackageSource pkg = PackageTest.createTestPackageSource(srpm, user.getOrg());
        HibernateFactory.getSession().save(pkg);
        handler.removeSourcePackage(admin, pkg.getId().intValue());
    }


    @Test
    public void testFindByNevra() {
        Package p = PackageTest.createTestPackage(admin.getOrg());

        List<Package> newP = handler.findByNvrea(admin, p.getPackageName().getName(),
                p.getPackageEvr().getVersion(), p.getPackageEvr().getRelease(),
                p.getPackageEvr().getEpoch(), p.getPackageArch().getLabel());
        assertEquals(1, newP.size());
        assertEquals(p, newP.get(0));
        newP = handler.findByNvrea(admin, p.getPackageName().getName(),
                p.getPackageEvr().getVersion(), p.getPackageEvr().getRelease(),
                "", p.getPackageArch().getLabel());
        assertEquals(1, newP.size());
        assertEquals(p, newP.get(0));
    }

    @Test
    public void testListSourcePackages() {
        User user = UserTestUtils.createUser("testUser", regular.getOrg().getId());
        Object[] result1 = handler.listSourcePackages(user);
        for (int i = 0; i < 3; i++) {
            SourceRpm srpm = SourceRpmTest.createTestSourceRpm();
            PackageSource pkg = PackageTest.createTestPackageSource(srpm, user.getOrg());
            HibernateFactory.getSession().save(pkg);
        }
        Object[] result2 = handler.listSourcePackages(user);
        assertEquals(3, result2.length - result1.length);
    }

}
