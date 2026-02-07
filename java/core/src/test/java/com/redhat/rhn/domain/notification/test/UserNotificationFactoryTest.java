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

package com.redhat.rhn.domain.notification.test;

import com.redhat.rhn.domain.notification.UserNotificationFactory;
import com.redhat.rhn.domain.notification.types.NotificationType;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.UserTestUtils;

import org.junit.jupiter.api.Test;

public class UserNotificationFactoryTest extends RhnBaseTestCase {

    @Test
    public void generatedCoverageTestGetLastNotificationMessageByType() {
        // this test has been generated programmatically to test
        // UserNotificationFactory.getLastNotificationMessageByType
        // containing a hibernate query that is not covered by any test so far
        // feel free to modify and/or complete it
        NotificationType arg0 = NotificationType.SUBSCRIPTION_WARNING;
        UserNotificationFactory.getLastNotificationMessageByType(arg0);
    }

    @Test
    public void generatedCoverageTestLookupByUserAndMessageId() {
        // this test has been generated programmatically to test UserNotificationFactory.lookupByUserAndMessageId
        // containing a hibernate query that is not covered by any test so far
        // feel free to modify and/or complete it
        User arg1 = UserTestUtils.createUser(this);
        UserNotificationFactory.lookupByUserAndMessageId(0L, arg1);
    }
}
