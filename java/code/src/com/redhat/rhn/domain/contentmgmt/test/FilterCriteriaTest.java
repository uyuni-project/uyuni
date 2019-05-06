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

import com.redhat.rhn.domain.contentmgmt.FilterCriteria;
import junit.framework.TestCase;

import static com.redhat.rhn.domain.contentmgmt.FilterCriteria.validate;

/**
 * Tests for {@link FilterCriteria}
 */
public class FilterCriteriaTest extends TestCase {

    public void testLegalValidation() {
        validate(FilterCriteria.Matcher.CONTAINS, "name");
        validate(FilterCriteria.Matcher.EQUALS, "nevr");
        validate(FilterCriteria.Matcher.EQUALS, "nevra");
    }

    public void testIllegalValidation() {
        try {
            validate(FilterCriteria.Matcher.CONTAINS, "nonsense");
            fail("An exception should have been thrown");
        }
        catch (IllegalArgumentException e) {
            // pass
        }

        try {
            validate(FilterCriteria.Matcher.EQUALS, "foo");
            fail("An exception should have been thrown");
        }
        catch (IllegalArgumentException e) {
                // pass
        }
    }
}
