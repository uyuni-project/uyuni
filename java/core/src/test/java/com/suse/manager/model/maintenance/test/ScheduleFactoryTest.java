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

package com.suse.manager.model.maintenance.test;

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.manager.model.maintenance.MaintenanceCalendar;
import com.suse.manager.model.maintenance.ScheduleFactory;

import org.junit.jupiter.api.Test;

public class ScheduleFactoryTest extends RhnBaseTestCase {

    @Test
    public void generatedCoverageTestListByCalendar() {
        // this test has been generated programmatically to test ScheduleFactory.listByCalendar
        // containing a hibernate query that is not covered by any test so far
        // feel free to modify and/or complete it
        ScheduleFactory testObject = new ScheduleFactory();
        User arg0 = UserTestUtils.createUser();
        MaintenanceCalendar arg1 = new MaintenanceCalendar();
        arg1.setOrg(arg0.getOrg());
        TestUtils.save(arg1);
        testObject.listByCalendar(arg0, arg1);

        TestUtils.clearSession();
    }
}
