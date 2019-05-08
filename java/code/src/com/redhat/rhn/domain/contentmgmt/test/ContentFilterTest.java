/**
 * Copyright (c) 2019 SUSE LLC
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

import com.redhat.rhn.domain.contentmgmt.ContentFilter;
import com.redhat.rhn.domain.contentmgmt.FilterCriteria;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.manager.contentmgmt.ContentManager;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.UserTestUtils;

import static com.redhat.rhn.domain.contentmgmt.ContentFilter.EntityType.PACKAGE;
import static com.redhat.rhn.domain.contentmgmt.ContentFilter.Rule.ALLOW;
import static com.redhat.rhn.domain.contentmgmt.ContentFilter.Rule.DENY;
import static com.redhat.rhn.domain.role.RoleFactory.ORG_ADMIN;

/**
 * Tests for {@link ContentFilter}
 */
public class ContentFilterTest extends JMockBaseTestCaseWithUser {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        UserTestUtils.addUserRole(user, ORG_ADMIN);
    }

    public void testPackageDenyFilter() throws Exception {
        Package pack = PackageTest.createTestPackage(user.getOrg());
        String packageName = pack.getPackageName().getName();

        FilterCriteria criteria = new FilterCriteria(FilterCriteria.Matcher.CONTAINS, "name", packageName);
        ContentFilter filter = ContentManager.createFilter(packageName + "-filter", DENY, PACKAGE, criteria, user);
        assertFalse(filter.test(pack));
    }

    public void testPackageNevrFilter() throws Exception {
        Package pack = PackageTest.createTestPackage(user.getOrg());
        String packageName = pack.getPackageName().getName();

        FilterCriteria criteria = new FilterCriteria(FilterCriteria.Matcher.EQUALS, "nevr", pack.getNameEvr());
        ContentFilter filter = ContentManager.createFilter(packageName + "-nevr-filter", DENY, PACKAGE, criteria, user);
        assertFalse(filter.test(pack));

        criteria = new FilterCriteria(FilterCriteria.Matcher.EQUALS, "nevr", pack.getNameEvra());
        filter = ContentManager.createFilter(packageName + "nevr2-filter", DENY, PACKAGE, criteria, user);
        assertTrue(filter.test(pack));

        criteria = new FilterCriteria(FilterCriteria.Matcher.EQUALS, "nevr", packageName);
        filter = ContentManager.createFilter(packageName + "nevr3-filter", DENY, PACKAGE, criteria, user);
        assertTrue(filter.test(pack));
    }

    public void testPackageNevraFilter() throws Exception {
        Package pack = PackageTest.createTestPackage(user.getOrg());
        String packageName = pack.getPackageName().getName();

        FilterCriteria criteria = new FilterCriteria(FilterCriteria.Matcher.EQUALS, "nevra", pack.getNameEvra());
        ContentFilter filter = ContentManager.createFilter(packageName + "-nevra-filter", DENY, PACKAGE, criteria, user);
        assertFalse(filter.test(pack));

        criteria = new FilterCriteria(FilterCriteria.Matcher.EQUALS, "nevra", pack.getNameEvr());
        filter = ContentManager.createFilter(packageName + "nevra2-filter", DENY, PACKAGE, criteria, user);
        assertTrue(filter.test(pack));

        criteria = new FilterCriteria(FilterCriteria.Matcher.EQUALS, "nevra", packageName);
        filter = ContentManager.createFilter(packageName + "nevra3-filter", DENY, PACKAGE, criteria, user);
        assertTrue(filter.test(pack));
    }
}