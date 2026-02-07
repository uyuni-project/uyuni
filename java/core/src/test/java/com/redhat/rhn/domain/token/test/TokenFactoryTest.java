/*
 * Copyright (c) 2026 SUSE LLC
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

package com.redhat.rhn.domain.token.test;

import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.token.TokenFactory;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import org.junit.jupiter.api.Test;

public class TokenFactoryTest extends BaseTestCaseWithUser {

    @Test
    public void generatedCoverageTestLookup() {
        // this test has been generated programmatically to test TokenFactory.lookup
        // containing a hibernate query that is not covered by any test so far
        // feel free to modify and/or complete it
        Org arg1 = user.getOrg();
        try {
            TokenFactory.lookup(0L, arg1);
        }
        catch (LookupException eIn) {
            //do nothing
        }
    }
}
