/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.common.db.datasource.CallableMode;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.util.RpmVersionComparator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * RpmVersionComparatorTest
 */
public class RpmVersionComparatorTest  {

    private RpmVersionComparator cmp;

    @BeforeEach
    public void setUp() throws Exception {
        cmp = new RpmVersionComparator();
    }

    @AfterEach
    public void tearDown() throws Exception {
        cmp = null;
    }

    @Test
    public void testBasicComparisons() {
        // Some equality
        assertCompareSymm(0, "0", "0");
        assertCompareSymm(0, "1-a.1", "1-a.1");
        assertCompareSymm(0, "1-a.1", "1.a-1");
        assertCompareSymm(0, "", "");

        // all not alphanum signs are treated as the same
        assertCompareSymm(0, "-", ".");
        assertCompareSymm(0, "--", "-");
        assertCompareSymm(0, "1-1-", "1-1.");

        // Some asymmetry .. not really a total ordering
        assertCompareSymm(1, "1.1", "1a");
        assertCompareSymm(-1, "9", "10");
        assertCompareSymm(-1, "00009", "0010");
        assertCompareSymm(-1, "1.1", "1.1.PTF");
    }

    @Test
    public void testBugzilla50977() {
        // From comment #2
        assertCompareSymm(1, "10mdk", "10");
        assertCompareSymm(-1, "10mdk", "10.1mdk");
        assertCompareSymm(1, "9", "ximian.1");

        // From comment #19
        assertCompareSymm(-1, "1.4snap", "1.4.5");
        assertCompareSymm(-1, "4.0x", "4.0.36");
        assertCompareSymm(-1, "p19", "2.0.0");
        assertCompareSymm(0, "2.0e", "2.0e");
        assertCompareSymm(-1, "2.0e", "2.0.11");
    }

    @Test
    public void testBugzilla82639() {
        // Some test cases from that bz. Note that the
        // results being tested for are not necessarily the ones from
        // bz, but whatever rpmvercmp in RHEL3 returns
        assertCompareSymm(1, "1", "asp1.7x.2");
        assertCompareSymm(1, "ipl4mdk", "alt0.8");
        assertCompareSymm(-1, "alt0.8", "ipl4mdk");
        assertCompareSymm(1, "1asp", "alt1");

    }

    /* from official rpm tests */
    @Test
    public void testTildeSorting() {
        assertCompareSymm(0, "1.0~rc1", "1.0~rc1");
        assertCompareSymm(-1, "1.0~rc1", "1.0");
        assertCompareSymm(1, "1.0", "1.0~rc1");
        assertCompareSymm(-1, "1.0~rc1", "1.0~rc2");
        assertCompareSymm(1, "1.0~rc2", "1.0~rc1");
        assertCompareSymm(0, "1.0~rc1~git123", "1.0~rc1~git123");
        assertCompareSymm(-1, "1.0~rc1~git123", "1.0~rc1");
        assertCompareSymm(1, "1.0~rc1", "1.0~rc1~git123");
    }

    /* from official rpm tests */
    @Test
    public void testCaretSorting() {
        assertCompareSymm(1, "1.0^", "1.0");
        assertCompareSymm(1, "1.0^git1", "1.0");
        assertCompareSymm(-1, "1.0^git1", "1.0^git2");
        assertCompareSymm(-1, "1.0^git1", "1.01");
        assertCompareSymm(-1, "1.0^20160101", "1.0.1");
        assertCompareSymm(1, "1.0^20160102", "1.0^20160101^git1");
    }

    /**
     * Test comparing strings of modular package releases.
     */
    @Test
    public void testRhel8ModuleReleaseComparison() {
        // some packages have "module_el8" in the release
        assertCompareSymm(-1, "7.module_el8.2.0+305+5e198a41", "7.module_el8.2.0+458+dab581ed");
        // some packages have "module+el8" in the release
        assertCompareSymm(-1, "10.module+el8.2.0+7749+4a513fb2", "10.module+el8.2.0+7749+5a513fb2");
        assertCompareSymm(-1, "6.module+el8+1645+8d4014a6", "7.module_el8.2.0+458+dab581ed");
    }

    /*
     * Test for https://github.com/uyuni-project/uyuni/issues/2531
     */
    @Test
    public void testUyuniIssue2531() {
        assertCompareSymm(1, "1.27+1.3.9", "1.27.1+1.3.9");
        assertCompareSymm(1, "1.27", "1.3.11");
    }

    /* from official rpm tests */
    @Test
    public void testTildeAndCaretSorting() {
        assertCompareSymm(1, "1.0~rc1^git1", "1.0~rc1");
        assertCompareSymm(1, "1.0^git1", "1.0^git1~pre");
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
        assertEquals(exp, testRPMVersionCompareInDatabase(
                v1, v2));
    }

    private int testRPMVersionCompareInDatabase(String operand1, String operand2) {
        // test the stored function
        CallableMode m = ModeFactory.getCallableMode("test_queries", "rpmstrcmp");
        Map inParams = new HashMap();
        Map outParams = new HashMap();
        outParams.put("compareResult", Types.INTEGER);
        Integer result;
        inParams.put("operand1", operand1);
        inParams.put("operand2", operand2);
        result = (Integer) m.execute(inParams, outParams).get("compareResult");
        return result;
    }
}
