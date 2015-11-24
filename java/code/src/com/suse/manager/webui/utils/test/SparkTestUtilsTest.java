/**
 * Copyright (c) 2015 SUSE LLC
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

package com.suse.manager.webui.utils.test;

import com.suse.manager.webui.utils.SparkTestUtils;
import junit.framework.TestCase;

/**
 * Test for SparkTestUtils.
 */
public class SparkTestUtilsTest extends TestCase {

    /**
     * Test substituting variables in a URI.
     */
    public void testSubstituteVariables() {
        String uri = "http://localhost:8080/:vhm/delete/:vhmlabel/";
        String expected = "http://localhost:8080/myVHM/delete/MyLabel/";

        assertEquals(expected, SparkTestUtils.substituteVariables(uri, "myVHM", "MyLabel"));
    }

    /**
     * Test substitute variables method with no substitutions.
     */
    public void testSubstituteVariablesNoSubs() {
        String uri = "http://localhost:8080/delete/";
        String expected = "http://localhost:8080/delete/";

        assertEquals(expected, SparkTestUtils.substituteVariables(uri));
    }

}