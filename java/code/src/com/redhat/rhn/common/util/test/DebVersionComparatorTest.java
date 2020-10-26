/**
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
package com.redhat.rhn.common.util.test;

import com.redhat.rhn.common.util.DebVersionComparator;

import junit.framework.TestCase;

/**
 * DebVersionComparatorTest
 */
public class DebVersionComparatorTest extends TestCase {

    private DebVersionComparator cmp;

    protected void setUp() throws Exception {
        super.setUp();
        cmp = new DebVersionComparator();
    }

    protected void tearDown() throws Exception {
        cmp = null;
        super.tearDown();
    }

    public void testDpkgTestCases() {
        // Debian dpkg test cases follow [ https://git.dpkg.org/cgit/dpkg/dpkg.git/tree/scripts/t/Dpkg_Version.t ]
        assertCompareSymm(-1, "1.0-1", "2.0-2");
        assertCompareSymm(-1, "2.2~rc-4", "2.2-1");
        assertCompareSymm(1, "2.2-1", "2.2~rc-4");
        assertCompareSymm(0, "1.0000-1", "1.0-1");
        assertCompareSymm(0, "0foo", "0foo");
        assertCompareSymm(0, "0foo-0", "0foo");
        assertCompareSymm(0, "0foo", "0foo-0");
        assertCompareSymm(1, "0foo", "0fo");
        assertCompareSymm(-1, "0foo-0", "0foo+");
        assertCompareSymm(-1, "0foo~1", "0foo");
        assertCompareSymm(-1, "0foo~foo+Bar", "0foo~foo+bar");
        assertCompareSymm(-1, "0foo~~", "0foo~");
        assertCompareSymm(-1, "1~", "1");
        assertCompareSymm(-1, "12345+that-really-is-some-ver-0", "12345+that-really-is-some-ver-10");
        assertCompareSymm(-1, "0foo-0", "0foo-01");
        assertCompareSymm(1, "0foo.bar", "0foobar");
        assertCompareSymm(1, "0foo.bar", "0foo1bar");
        assertCompareSymm(1, "0foo.bar", "0foo0bar");
        assertCompareSymm(-1, "0foo1bar-1", "0foobar-1");
        assertCompareSymm(1, "0foo2.0", "0foo2");
        assertCompareSymm(-1, "0foo2.0.0", "0foo2.10.0");
        assertCompareSymm(-1, "0foo2.0", "0foo2.0.0");
        assertCompareSymm(-1, "0foo2.0", "0foo2.10");
        assertCompareSymm(-1, "0foo2.1", "0foo2.10");
        assertCompareSymm(0, "1.09", "1.9");
        assertCompareSymm(1, "1.0.8+nmu1", "1.0.8");
        assertCompareSymm(1, "3.11", "3.10+nmu1");
        assertCompareSymm(1, "0.9j-20080306-4", "0.9i-20070324-2");
        assertCompareSymm(1, "1.2.0~b7-1", "1.2.0~b6-1");
        assertCompareSymm(1, "1.011-1", "1.06-2");
        assertCompareSymm(1, "0.0.9+dfsg1-1", "0.0.8+dfsg1-3");
        assertCompareSymm(1, "4.6.99+svn6582-1", "4.6.99+svn6496-1");
        assertCompareSymm(1, "53", "52");
        assertCompareSymm(1, "0.9.9~pre122-1", "0.9.9~pre111-1");
        assertCompareSymm(1, "1.0.1+gpl-1", "1.0.1-2");
        assertCompareSymm(-1, "1a", "1000a");
    }

    public void testBugzillaCases() {
        // bsc#1150113
        // bsc#1173201
        assertCompareSymm(0, "0.2017-01-15.gdad1bbc69", "0.2017-01-15.gdad1bbc69");
        assertCompareSymm(1, "0.2017-01-15.gdad1bbc69", "0.2016-08-15.cafecafe");
        assertCompareSymm(-1, "0.2017-01-15.gdad1bbc69", "1.0");
        assertCompareSymm(1, "1.0.0~alpha+201804191824-24b36a9", "0.99");
        assertCompareSymm(-1, "1.20.4", "14.1");
        assertCompareSymm(1, "1.27", "1.3.11");
        assertCompareSymm(-1, "1.27+1.3.9", "1.27.1+1.3.9");
        assertCompareSymm(-1, "1.27+1.3.9", "1.27.1ubuntu1+1.3.9");
        assertCompareSymm(-1, "1.27+1.3.9", "1.27.1ubuntu2+1.3.11");
        assertCompareSymm(1, "1.27+1.3.9", "1.3.11");
        assertCompareSymm(1, "1.27+1.3.9", "1.3.9");
        assertCompareSymm(-1, "1.27+1.3.9", "5.18.4.1");
        assertCompareSymm(1, "2.27+1.3.9", "1.10~ubuntu18.04.4+1.2.10");
        assertCompareSymm(-1, "2.7.15~rc1", "2.7.15");
        assertCompareSymm(1, "3.1-20170329", "3.1-20150325");
        assertCompareSymm(1, "3.27", "1.10~ubuntu18.04.4+1.2.10");
        assertCompareSymm(1, "4.27~test", "1.10~ubuntu18.04.4+1.2.10");
        assertCompareSymm(-1, "8.0.9.22-abcd", "8.0.9.22-abcd-expr1");
        assertCompareSymm(-1, "8.0.9", "8.0.9.22");
        assertCompareSymm(-1, "8.0.9", "a.8.0.9-22");
        assertCompareSymm(1, "8-20180414", "8");
        assertCompareSymm(-1, "8-20180414", "8.3.0");
    }

    private void assertCompareAsym(int exp, String v1, String v2) {
        assertCompare(exp, v1, v2);
        assertCompare(exp, v2, v1);
    }

    private void assertCompareSymm(int exp, String v1, String v2) {
        assertCompare(exp, v1, v2);
        assertCompare(-exp, v2, v1);
    }

    private void assertCompare(int exp, String v1, String v2) {
        assertEquals(exp, cmp.compare(v1, v2));
        assertEquals(0, cmp.compare(v1, v1));
        assertEquals(0, cmp.compare(v2, v2));
    }
}
