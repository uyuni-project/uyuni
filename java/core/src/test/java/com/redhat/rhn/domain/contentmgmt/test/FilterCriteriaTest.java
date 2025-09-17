/*
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

import static com.redhat.rhn.domain.contentmgmt.ContentFilter.EntityType.ERRATUM;
import static com.redhat.rhn.domain.contentmgmt.ContentFilter.EntityType.PACKAGE;
import static com.redhat.rhn.domain.contentmgmt.FilterCriteria.Matcher.CONTAINS;
import static com.redhat.rhn.domain.contentmgmt.FilterCriteria.Matcher.EQUALS;
import static com.redhat.rhn.domain.contentmgmt.FilterCriteria.Matcher.GREATER;
import static com.redhat.rhn.domain.contentmgmt.FilterCriteria.Matcher.GREATEREQ;
import static com.redhat.rhn.domain.contentmgmt.FilterCriteria.validate;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.domain.contentmgmt.FilterCriteria;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link FilterCriteria}
 */
public class FilterCriteriaTest  {

    @Test
    public void testLegalValidation() {
        validate(PACKAGE, CONTAINS, "name");
        validate(PACKAGE, EQUALS, "nevr");
        validate(PACKAGE, EQUALS, "nevra");
        validate(ERRATUM, GREATER, "issue_date");
        validate(ERRATUM, GREATEREQ, "issue_date");
    }

    @Test
    public void testIllegalValidation() {
        try {
            validate(PACKAGE, CONTAINS, "nonsense");
            fail("An exception should have been thrown");
        }
        catch (IllegalArgumentException e) {
            // pass
        }

        try {
            validate(PACKAGE, EQUALS, "foo");
            fail("An exception should have been thrown");
        }
        catch (IllegalArgumentException e) {
                // pass
        }
    }
}
