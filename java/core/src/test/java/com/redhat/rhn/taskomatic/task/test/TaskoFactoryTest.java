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

import com.redhat.rhn.taskomatic.NoSuchBunchTaskException;
import com.redhat.rhn.taskomatic.TaskoFactory;
import com.redhat.rhn.taskomatic.domain.TaskoBunch;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.Test;

import java.util.Date;

public class TaskoFactoryTest extends BaseTestCaseWithUser {

    @Test
    public void generatedCoverageTestLookupOrgBunchByName() {
        // this test has been generated programmatically to test TaskoFactory.lookupOrgBunchByName
        // containing a hibernate query that is not covered by any test so far
        // feel free to modify and/or complete it
        TaskoFactory.lookupOrgBunchByName("");
    }


    @Test
    public void generatedCoverageTestListOrgBunches() {
        // this test has been generated programmatically to test TaskoFactory.listOrgBunches
        // containing a hibernate query that is not covered by any test so far
        // feel free to modify and/or complete it
        TaskoFactory.listOrgBunches();
    }

    @Test
    public void generatedCoverageTestListSatBunches() {
        // this test has been generated programmatically to test TaskoFactory.listSatBunches
        // containing a hibernate query that is not covered by any test so far
        // feel free to modify and/or complete it
        TaskoFactory.listSatBunches();
    }

    @Test
    public void generatedCoverageTestListTasks() {
        // this test has been generated programmatically to test TaskoFactory.listTasks
        // containing a hibernate query that is not covered by any test so far
        // feel free to modify and/or complete it
        TaskoFactory.listTasks();
    }

    @Test
    public void generatedCoverageTestListRunsOlderThan() {
        // this test has been generated programmatically to test TaskoFactory.listRunsOlderThan
        // containing a hibernate query that is not covered by any test so far
        // feel free to modify and/or complete it
        TaskoFactory.listRunsOlderThan(new Date(0));
    }


    @Test
    public void generatedCoverageTestListRunsNewerThan() {
        // this test has been generated programmatically to test TaskoFactory.listRunsNewerThan
        // containing a hibernate query that is not covered by any test so far
        // feel free to modify and/or complete it
        TaskoFactory.listRunsNewerThan(new Date(0));
    }

    @Test
    public void generatedCoverageTestListActiveSchedulesByOrg() {
        // this test has been generated programmatically to test TaskoFactory.listActiveSchedulesByOrg
        // containing a hibernate query that is not covered by any test so far
        // feel free to modify and/or complete it
        TaskoFactory.listActiveSchedulesByOrg(null);
        TaskoFactory.listActiveSchedulesByOrg(0);
    }


    @Test
    public void generatedCoverageTestListActiveSchedulesByOrgAndLabel() {
        // this test has been generated programmatically to test TaskoFactory.listActiveSchedulesByOrgAndLabel
        // containing a hibernate query that is not covered by any test so far
        // feel free to modify and/or complete it
        TaskoFactory.listActiveSchedulesByOrgAndLabel(0, "");
        TaskoFactory.listActiveSchedulesByOrgAndLabel(null, "");
    }

    @Test
    public void generatedCoverageTestListActiveSchedulesByOrgAndBunchNullOrg() throws NoSuchBunchTaskException {
        String taskoBunchName = TestUtils.randomString(10);
        TaskoBunch taskoBunch = new TaskoBunch();
        taskoBunch.setName(taskoBunchName);
        TaskoFactory.save(taskoBunch);

        TaskoFactory.listActiveSchedulesByOrgAndBunch(null, taskoBunchName);
    }

    @Test
    public void generatedCoverageTestListActiveSchedulesByOrgAndBunchWithOrg() throws NoSuchBunchTaskException {
        String taskoBunchName = TestUtils.randomString(10);
        TaskoBunch taskoBunch = new TaskoBunch();
        taskoBunch.setName(taskoBunchName);
        taskoBunch.setOrgBunch("Y");
        TaskoFactory.save(taskoBunch);

        TaskoFactory.listActiveSchedulesByOrgAndBunch(user.getOrg().getId().intValue(), taskoBunchName);
    }

    @Test
    public void generatedCoverageTestListFuture() {
        // this test has been generated programmatically to test TaskoFactory.listFuture
        // containing a hibernate query that is not covered by any test so far
        // feel free to modify and/or complete it
        TaskoFactory.listFuture();
    }


    @Test
    public void generatedCoverageTestListNewerRunsBySchedule() {
        // this test has been generated programmatically to test TaskoFactory.listNewerRunsBySchedule
        // containing a hibernate query that is not covered by any test so far
        // feel free to modify and/or complete it
        TaskoFactory.listNewerRunsBySchedule(0L, new Date(0));
    }

    @Test
    public void generatedCoverageTestListSchedulesByOrg() {
        // this test has been generated programmatically to test TaskoFactory.listSchedulesByOrg
        // containing a hibernate query that is not covered by any test so far
        // feel free to modify and/or complete it
        TaskoFactory.listSchedulesByOrg(0);
        TaskoFactory.listSchedulesByOrg(null);
    }


    @Test
    public void generatedCoverageTestListRunsBySchedule() {
        // this test has been generated programmatically to test TaskoFactory.listRunsBySchedule
        // containing a hibernate query that is not covered by any test so far
        // feel free to modify and/or complete it
        TaskoFactory.listRunsBySchedule(0L);
    }


    @Test
    public void generatedCoverageTestListSchedulesOlderThan() {
        // this test has been generated programmatically to test TaskoFactory.listSchedulesOlderThan
        // containing a hibernate query that is not covered by any test so far
        // feel free to modify and/or complete it
        TaskoFactory.listSchedulesOlderThan(new Date(0));
    }


    @Test
    public void generatedCoverageTestListSchedulesByOrgAndLabel() {
        // this test has been generated programmatically to test TaskoFactory.listSchedulesByOrgAndLabel
        // containing a hibernate query that is not covered by any test so far
        // feel free to modify and/or complete it
        TaskoFactory.listSchedulesByOrgAndLabel(0, "");
        TaskoFactory.listSchedulesByOrgAndLabel(null, "");
    }


    @Test
    public void generatedCoverageTestListUnfinishedRuns() {
        // this test has been generated programmatically to test TaskoFactory.listUnfinishedRuns
        // containing a hibernate query that is not covered by any test so far
        // feel free to modify and/or complete it
        TaskoFactory.listUnfinishedRuns();
    }

}
