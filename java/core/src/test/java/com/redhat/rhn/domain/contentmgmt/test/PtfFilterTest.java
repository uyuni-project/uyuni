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

package com.redhat.rhn.domain.contentmgmt.test;

import static com.redhat.rhn.domain.contentmgmt.ContentFilter.EntityType.PTF;
import static com.redhat.rhn.domain.contentmgmt.ContentFilter.Rule.ALLOW;
import static com.redhat.rhn.domain.role.RoleFactory.ORG_ADMIN;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.contentmgmt.FilterCriteria;
import com.redhat.rhn.domain.contentmgmt.PtfFilter;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.manager.contentmgmt.ContentManager;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.PackageTestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PtfFilterTest extends BaseTestCaseWithUser {

    private ContentManager contentManager;
    private Package testOnePackage;
    private Package updatedOnePackage;
    private Package ptfOneMasterPackage;
    private Package ptfOnePackage;
    private Package ptfTwoMasterPackage;
    private Package testTwoPackage;
    private Package ptfTwoPackage;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        UserTestUtils.addUserRole(user, ORG_ADMIN);
        contentManager = new ContentManager();

        // Create some packages
        testOnePackage = PackageTest.createTestPackage(user.getOrg(), "vim-data");
        updatedOnePackage = PackageTestUtils.newVersionOfPackage(testOnePackage, null, "2.0.0", null, user.getOrg());

        testTwoPackage = PackageTest.createTestPackage(user.getOrg(), "kernel-default");

        ptfOneMasterPackage = PackageTestUtils.createPtfMaster("123456", "1", user.getOrg());
        ptfOnePackage = PackageTestUtils.createPtfPackage(testOnePackage, "123456", "1", user.getOrg());
        PackageTestUtils.associatePackageToPtf(ptfOneMasterPackage, ptfOnePackage);

        ptfTwoMasterPackage = PackageTestUtils.createPtfMaster("987654", "1", user.getOrg());
        ptfTwoPackage = PackageTestUtils.createPtfPackage(testTwoPackage, "987654", "1", user.getOrg());
        PackageTestUtils.associatePackageToPtf(ptfTwoMasterPackage, ptfTwoPackage);
    }

    @Test
    public void testAllPtfFilter() {

        FilterCriteria criteria = new FilterCriteria(FilterCriteria.Matcher.PTF_ALL, "ptf_all", "ALL");
        PtfFilter filter = (PtfFilter) contentManager.createFilter("all-ptfs", ALLOW, PTF, criteria, user);

        assertFalse(filter.test(testOnePackage));
        assertFalse(filter.test(updatedOnePackage));
        assertTrue(filter.test(ptfOneMasterPackage));
        assertTrue(filter.test(ptfOnePackage));
    }

    @Test
    public void testPtfNumberLowerFilter() {
        FilterCriteria criteria = new FilterCriteria(FilterCriteria.Matcher.LOWER, "ptf_number", "567890");
        PtfFilter filter = (PtfFilter) contentManager.createFilter("ptf-number-1", ALLOW, PTF, criteria, user);

        assertFalse(filter.test(testOnePackage));
        assertTrue(filter.test(ptfOneMasterPackage));
        assertTrue(filter.test(ptfOnePackage));
        assertFalse(filter.test(ptfTwoMasterPackage));
        assertFalse(filter.test(ptfTwoPackage));
    }

    @Test
    public void testPtfNumberLowerEqualsFilter() {
        FilterCriteria criteria = new FilterCriteria(FilterCriteria.Matcher.LOWEREQ, "ptf_number", "987654");
        PtfFilter filter = (PtfFilter) contentManager.createFilter("ptf-number-2", ALLOW, PTF, criteria, user);

        assertFalse(filter.test(testOnePackage));
        assertTrue(filter.test(ptfOneMasterPackage));
        assertTrue(filter.test(ptfOnePackage));
        assertTrue(filter.test(ptfTwoMasterPackage));
        assertTrue(filter.test(ptfTwoPackage));
    }

    @Test
    public void testPtfNumberEqualsFilter() {
        FilterCriteria criteria = new FilterCriteria(FilterCriteria.Matcher.EQUALS, "ptf_number", "123456");
        PtfFilter filter = (PtfFilter) contentManager.createFilter("ptf-number-3", ALLOW, PTF, criteria, user);

        assertFalse(filter.test(testOnePackage));
        assertTrue(filter.test(ptfOneMasterPackage));
        assertTrue(filter.test(ptfOnePackage));
        assertFalse(filter.test(ptfTwoMasterPackage));
        assertFalse(filter.test(ptfTwoPackage));
    }

    @Test
    public void testPtfNumberGreaterFilter() {
        FilterCriteria criteria = new FilterCriteria(FilterCriteria.Matcher.GREATER, "ptf_number", "567890");
        PtfFilter filter = (PtfFilter) contentManager.createFilter("ptf-number-4", ALLOW, PTF, criteria, user);

        assertFalse(filter.test(testOnePackage));
        assertFalse(filter.test(ptfOneMasterPackage));
        assertFalse(filter.test(ptfOnePackage));
        assertTrue(filter.test(ptfTwoMasterPackage));
        assertTrue(filter.test(ptfTwoPackage));
    }

    @Test
    public void testPtfNumberGreaterEqualsFilter() {
        FilterCriteria criteria = new FilterCriteria(FilterCriteria.Matcher.GREATEREQ, "ptf_number", "987654");
        PtfFilter filter = (PtfFilter) contentManager.createFilter("ptf-number-5", ALLOW, PTF, criteria, user);

        assertFalse(filter.test(testOnePackage));
        assertFalse(filter.test(ptfOneMasterPackage));
        assertFalse(filter.test(ptfOnePackage));
        assertTrue(filter.test(ptfTwoMasterPackage));
        assertTrue(filter.test(ptfTwoPackage));
    }

    @Test
    public void testFixedPackageEquals() {
        FilterCriteria criteria = new FilterCriteria(FilterCriteria.Matcher.EQUALS, "ptf_package", "vim-data");
        PtfFilter filter = (PtfFilter) contentManager.createFilter("ptf-package-1", ALLOW, PTF, criteria, user);

        assertFalse(filter.test(testOnePackage));
        assertFalse(filter.test(testTwoPackage));
        assertTrue(filter.test(ptfOneMasterPackage));
        assertTrue(filter.test(ptfOnePackage));
        assertFalse(filter.test(ptfTwoMasterPackage));
        assertFalse(filter.test(ptfTwoPackage));
    }

    @Test
    public void testFixedPackageMatches() {
        FilterCriteria criteria = new FilterCriteria(FilterCriteria.Matcher.MATCHES, "ptf_package", "[a-z]+-[a-z]+");
        PtfFilter filter = (PtfFilter) contentManager.createFilter("ptf-package-2", ALLOW, PTF, criteria, user);

        assertFalse(filter.test(testOnePackage));
        assertFalse(filter.test(testTwoPackage));
        assertTrue(filter.test(ptfOneMasterPackage));
        assertTrue(filter.test(ptfOnePackage));
        assertTrue(filter.test(ptfTwoMasterPackage));
        assertTrue(filter.test(ptfTwoPackage));
    }

    @Test
    public void testFixedPackageContains() {
        FilterCriteria criteria = new FilterCriteria(FilterCriteria.Matcher.CONTAINS, "ptf_package", "default");
        PtfFilter filter = (PtfFilter) contentManager.createFilter("ptf-package-3", ALLOW, PTF, criteria, user);

        assertFalse(filter.test(testOnePackage));
        assertFalse(filter.test(testTwoPackage));
        assertFalse(filter.test(ptfOneMasterPackage));
        assertFalse(filter.test(ptfOnePackage));
        assertTrue(filter.test(ptfTwoMasterPackage));
        assertTrue(filter.test(ptfTwoPackage));
    }
}
