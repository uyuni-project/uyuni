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

package com.redhat.rhn.taskomatic.task.test;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.taskomatic.task.TaskHelper;
import com.redhat.rhn.testing.RhnBaseTestCase;

import org.junit.jupiter.api.Test;

public class TaskHelperTest extends RhnBaseTestCase {

    @Test
    public void generatedCoverageTestScheduleActionExecution() {
        // this test has been generated programmatically to test TaskHelper.scheduleActionExecution
        // containing a hibernate query that is not covered by any test so far
        // feel free to modify and/or complete it
        Action arg0 = new Action();
        TaskHelper.scheduleActionExecution(arg0);
    }
}
