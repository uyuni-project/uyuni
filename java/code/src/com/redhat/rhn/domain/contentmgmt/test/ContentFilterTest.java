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
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.manager.contentmgmt.ContentManager;
import com.redhat.rhn.testing.ErrataTestUtils;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;

import static com.redhat.rhn.domain.contentmgmt.ContentFilter.EntityType.ERRATUM;
import static com.redhat.rhn.domain.contentmgmt.ContentFilter.EntityType.PACKAGE;
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

    /**
     * Test basic Errata filtering based on advisory name
     *
     * @throws Exception if anything goes wrong
     */
    public void testErrataAdvisoryFilter() throws Exception {
        String cveName = TestUtils.randomString().substring(0, 13);
        Errata erratum = ErrataTestUtils.createTestErrata(user, Collections.singleton(ErrataTestUtils.createTestCve(cveName)));

        FilterCriteria criteria = new FilterCriteria(FilterCriteria.Matcher.EQUALS, "advisory_name", erratum.getAdvisoryName());
        ContentFilter filter = ContentManager.createFilter(cveName + "-filter", DENY, ERRATUM, criteria, user);
        assertFalse(filter.test(erratum));

        criteria = new FilterCriteria(FilterCriteria.Matcher.EQUALS, "advisory_name", "idontexist");
        filter = ContentManager.createFilter(cveName + "-filter-2", DENY, ERRATUM, criteria, user);
        assertTrue(filter.test(erratum));
    }

    /**
     * Test basic Errata filtering based on issue_date
     *
     * @throws Exception if anything goes wrong
     */
    public void testErrataByDateFilter() throws Exception {
        String cveName1 = TestUtils.randomString().substring(0, 13);
        Errata erratum1 = ErrataTestUtils.createTestErrata(user, Collections.singleton(ErrataTestUtils.createTestCve(cveName1)));
        erratum1.setIssueDate(new Date(1556604000000L));
        String cveName2 = TestUtils.randomString().substring(0, 13);
        Errata erratum2 = ErrataTestUtils.createTestErrata(user, Collections.singleton(ErrataTestUtils.createTestCve(cveName2)));
        erratum2.setIssueDate(new Date(1556694000000L));
        String cveName3 = TestUtils.randomString().substring(0, 13);
        Errata erratum3 = ErrataTestUtils.createTestErrata(user, Collections.singleton(ErrataTestUtils.createTestCve(cveName3)));
        erratum3.setIssueDate(new Date(1556668800000L)); // "2019-05-01 00:00:00 +0000"

        FilterCriteria criteria = new FilterCriteria(FilterCriteria.Matcher.GREATER, "issue_date", "2019-05-01T00:00:00+00:00");
        ContentFilter filter = ContentManager.createFilter("bydate-filter", DENY, ERRATUM, criteria, user);
        assertTrue(filter.test(erratum1));
        assertFalse(filter.test(erratum2));
        assertTrue(filter.test(erratum3));
    }

    /**
     * Test basic Errata filtering based on issue_date
     *
     * @throws Exception if anything goes wrong
     */
    public void testErrataByDate2Filter() throws Exception {
        String cveName1 = TestUtils.randomString().substring(0, 13);
        Errata erratum1 = ErrataTestUtils.createTestErrata(user, Collections.singleton(ErrataTestUtils.createTestCve(cveName1)));
        erratum1.setIssueDate(new Date(1556604000000L));
        String cveName2 = TestUtils.randomString().substring(0, 13);
        Errata erratum2 = ErrataTestUtils.createTestErrata(user, Collections.singleton(ErrataTestUtils.createTestCve(cveName2)));
        erratum2.setIssueDate(new Date(1556694000000L));
        String cveName3 = TestUtils.randomString().substring(0, 13);
        Errata erratum3 = ErrataTestUtils.createTestErrata(user, Collections.singleton(ErrataTestUtils.createTestCve(cveName3)));
        erratum3.setIssueDate(new Date(1556668800000L)); // "2019-05-01 00:00:00 +0000"

        FilterCriteria criteria = new FilterCriteria(FilterCriteria.Matcher.GREATEREQ, "issue_date", "2019-05-01T00:00:00+00:00");
        ContentFilter filter = ContentManager.createFilter("bydate-filter", DENY, ERRATUM, criteria, user);
        assertTrue(filter.test(erratum1));
        assertFalse(filter.test(erratum2));

        ZonedDateTime criteriaDate = ZonedDateTime.parse("2019-05-01T00:00:00+00:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        assertFalse(erratum3.getIssueDate().toInstant().atZone(ZoneId.systemDefault()).toString()
                + " should be equal " +  criteriaDate.toString(), filter.test(erratum3));
    }
}
