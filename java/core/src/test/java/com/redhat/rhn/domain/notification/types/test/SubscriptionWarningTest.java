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

package com.redhat.rhn.domain.notification.types.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.redhat.rhn.domain.notification.types.SubscriptionWarning;
import com.redhat.rhn.testing.RhnBaseTestCase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SubscriptionWarningTest extends RhnBaseTestCase {

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
     public void testGetStrings() {
        SubscriptionWarning sw = new SubscriptionWarning() {
            @Override
            public boolean expiresSoon() {
                return true;
            }
        };
        assertNotNull(sw.getSummary());
        assertNotNull(sw.getDetails());
    }

    @Test
    public void generatedCoverageTestExpiresSoon() {
        // this test has been generated programmatically to test SubscriptionWarning.expiresSoon
        // containing a hibernate query that is not covered by any test so far
        // feel free to modify and/or complete it
        SubscriptionWarning testObject = new SubscriptionWarning();
        testObject.expiresSoon();
    }
}
