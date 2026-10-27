/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.redhat.rhn.taskomatic.task;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.taskomatic.TaskoFactory;
import com.redhat.rhn.taskomatic.domain.TaskoBunch;
import com.redhat.rhn.taskomatic.domain.TaskoRun;
import com.redhat.rhn.taskomatic.domain.TaskoSchedule;
import com.redhat.rhn.taskomatic.domain.TaskoTemplate;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;

import org.jmock.Expectations;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class ClearLogHistoryTest extends JMockBaseTestCaseWithUser {
    private static final String TEST_BUNCH_NAME = "minion-action-executor-bunch";
    private JobExecutionContext jobContext;
    private final TaskoTemplate testTaskoTemplate = testLookupTaskoTemplateByBunchName(TEST_BUNCH_NAME);
    private final TaskoBunch testTaskoBunch = TaskoFactory.lookupSatBunchByName(TEST_BUNCH_NAME);
    private Instant testCurrentTime = Instant.now();

    @Nested
    class TestsWithSchedulesAndRuns {

        @BeforeEach()
        void beforeEach() {
            //setup test
            cleanupTestSchedulesAndRuns();
            testCurrentTime = Instant.now();
            createTestScenario();
            HibernateFactory.commitTransaction();
        }

        @AfterEach()
        void afterEach() {
            //clean all commited records
            cleanupTestSchedulesAndRuns();
        }

        @Test
        @DisplayName("taskomatic task cleans up old schedules and runs")
        public void testClearLogHistoryTask() throws JobExecutionException {
            //expect all runs and schedules are present
            expectAllRuns();
            expectAllSchedules();

            //execute the taskomatic task
            mockJobExecution();
            ClearLogHistory task = new ClearLogHistory();
            task.execute(jobContext);

            //expect old runs and schedules are cleaned up
            expectOnlyNewRuns();
            expectOnlyNewSchedules();
        }

        @Test
        @DisplayName("tests TaskoFactory deleteRunsOlderThan and deleteSchedulesWithNoRunsOlderThan methods")
        public void testTaskoFactoryDeleteRunsAndSchedules() {
            //expect all runs and schedules are present
            expectAllRuns();
            expectAllSchedules();

            //delete old schedules: all of them have correspondent runs, so none is cleared
            TaskoFactory.deleteSchedulesWithNoRunsOlderThan(Date.from(testCurrentTime));

            //expect all runs and schedules are present
            expectAllRuns();
            expectAllSchedules();

            //now delete old runs
            TaskoFactory.deleteRunsOlderThan(Date.from(testCurrentTime));

            //old runs are cleaned, but schedules are still there
            expectOnlyNewRuns();
            expectAllSchedules();

            //delete old schedules: now all of them have no correspondent runs
            TaskoFactory.deleteSchedulesWithNoRunsOlderThan(Date.from(testCurrentTime));

            //expect only new runs and schedules
            expectOnlyNewRuns();
            expectOnlyNewSchedules();
        }
    }

    @Test
    @DisplayName("test getting the limit time from job execution context")
    public void testGetLimitTimeWithDays() throws JobExecutionException {
        int days = 42;
        Instant currentTime = Instant.now();
        Date expectedDate = Date.from(currentTime.minus(days, ChronoUnit.DAYS));

        mockJobExecution(days);
        ClearLogHistory task = new ClearLogHistory();
        Date limitTime = task.getLimitTime(jobContext, currentTime);

        assertEquals(expectedDate, limitTime);
    }

    @Test
    @DisplayName("test getting the default limit time")
    public void testGetDefaultLimitTime() throws JobExecutionException {
        int clearLogHistoryDefaultDaysValue = 7;
        Instant currentTime = Instant.now();
        Date expectedDate = Date.from(currentTime.minus(clearLogHistoryDefaultDaysValue, ChronoUnit.DAYS));

        mockJobExecution();
        ClearLogHistory task = new ClearLogHistory();
        Date limitTime = task.getLimitTime(jobContext, currentTime);

        assertEquals(expectedDate, limitTime);
    }

    private TaskoTemplate testLookupTaskoTemplateByBunchName(String bunchName) {
        return HibernateFactory.getSession()
                .createQuery("""
                        FROM TaskoTemplate t
                        JOIN TaskoBunch b
                        ON t.bunch.id = b.id
                        WHERE b.name = :name""", TaskoTemplate.class)
                .setParameter("name", bunchName)
                .uniqueResult();
    }

    private List<TaskoRun> testListRunsOlderThan(Date limitTime) {
        return HibernateFactory.getSession()
                .createQuery("FROM TaskoRun WHERE endTime < :limit_time",
                        TaskoRun.class)
                .setParameter("limit_time", limitTime)
                .list();
    }

    private List<TaskoSchedule> testListSchedulesOlderThan(Date limitTime) {
        return HibernateFactory.getSession()
                .createQuery("FROM TaskoSchedule WHERE activeTill < :limit_time",
                        TaskoSchedule.class)
                .setParameter("limit_time", limitTime)
                .list();
    }

    private String getTestJobLabel(int index) {
        return TEST_BUNCH_NAME + "-" + index;
    }

    private void createTestScheduleAndRun(int numDays, int index) {
        Date atDate = Date.from(testCurrentTime.plus(Duration.ofDays(numDays)));

        TaskoSchedule taskoSchedule = new TaskoSchedule();
        taskoSchedule.setJobLabel(getTestJobLabel(index));
        taskoSchedule.setBunch(testTaskoBunch);
        taskoSchedule.setOrgId(null);
        taskoSchedule.setActiveFrom(atDate);
        taskoSchedule.setActiveTill(atDate);
        TaskoFactory.save(taskoSchedule);

        TaskoRun taskoRun = new TaskoRun();
        taskoRun.setOrgId(null);
        taskoRun.setTemplate(testTaskoTemplate);
        taskoRun.setScheduleId(taskoSchedule.getId());
        taskoRun.setStartTime(atDate);
        taskoRun.setEndTime(atDate);
        taskoRun.setStatus(TaskoRun.STATUS_FINISHED);
        TaskoFactory.save(taskoRun);
    }


    private void mockJobExecution() {
        mockJobExecution(null);
    }

    private void mockJobExecution(Integer days) {
        jobContext = mock(JobExecutionContext.class);
        JobDetail jobDetail = mock(JobDetail.class);

        context().checking(new Expectations() {
            {

                allowing(jobContext).getJobDetail();
                will(returnValue(jobDetail));

                JobDataMap dataMap = new JobDataMap();
                if(days != null) {
                    //dataMap.putAsString("days", days);
                    dataMap.put("days", days);
                }

                allowing(jobDetail).getJobDataMap();
                will(returnValue(dataMap));
            }
        });
    }

    private void cleanupTestSchedulesAndRuns() {
        HibernateFactory.getSession()
                .createMutationQuery("DELETE FROM TaskoRun r WHERE r.template = :template")
                .setParameter("template", testTaskoTemplate)
                .executeUpdate();
        HibernateFactory.getSession()
                .createMutationQuery("DELETE FROM TaskoSchedule s WHERE s.bunch = :bunch")
                .setParameter("bunch", testTaskoBunch)
                .executeUpdate();

        HibernateFactory.commitTransaction();
    }

    private void createTestScenario() {
        // 3 jobs 15 days ago
        createTestScheduleAndRun(-15, 1);
        createTestScheduleAndRun(-15, 2);
        createTestScheduleAndRun(-15, 3);

        // 2 jobs in 10 days
        createTestScheduleAndRun(10, 4);
        createTestScheduleAndRun(10, 5);
    }

    private boolean isTestSchedule(TaskoSchedule taskoSchedule) {
        return Objects.equals(taskoSchedule.getBunch().getId(), testTaskoBunch.getId());
    }

    private boolean isTestRun(TaskoRun taskoRun) {
        return Objects.equals(taskoRun.getTemplate().getId(), testTaskoTemplate.getId());
    }

    private void expectAllRuns() {
        Date date = Date.from(testCurrentTime);
        assertEquals(3, testListRunsOlderThan(date).stream().filter(this::isTestRun).count());
        assertEquals(2, TaskoFactory.listRunsNewerThan(date).stream().filter(this::isTestRun).count());
    }

    private void expectOnlyNewRuns() {
        Date date = Date.from(testCurrentTime);
        assertEquals(0, testListRunsOlderThan(date).stream().filter(this::isTestRun).count());
        assertEquals(2, TaskoFactory.listRunsNewerThan(date).stream().filter(this::isTestRun).count());
    }

    private void expectAllSchedules() {
        Date date = Date.from(testCurrentTime);
        assertEquals(1, TaskoFactory.listScheduleByLabel(getTestJobLabel(1)).size());
        assertEquals(1, TaskoFactory.listScheduleByLabel(getTestJobLabel(2)).size());
        assertEquals(1, TaskoFactory.listScheduleByLabel(getTestJobLabel(3)).size());
        assertEquals(1, TaskoFactory.listScheduleByLabel(getTestJobLabel(4)).size());
        assertEquals(1, TaskoFactory.listScheduleByLabel(getTestJobLabel(5)).size());
        assertEquals(3, testListSchedulesOlderThan(date).stream()
                .filter(this::isTestSchedule).count());
    }

    private void expectOnlyNewSchedules() {
        Date date = Date.from(testCurrentTime);
        assertEquals(0, TaskoFactory.listScheduleByLabel(getTestJobLabel(1)).size());
        assertEquals(0, TaskoFactory.listScheduleByLabel(getTestJobLabel(2)).size());
        assertEquals(0, TaskoFactory.listScheduleByLabel(getTestJobLabel(3)).size());
        assertEquals(1, TaskoFactory.listScheduleByLabel(getTestJobLabel(4)).size());
        assertEquals(1, TaskoFactory.listScheduleByLabel(getTestJobLabel(5)).size());
        assertEquals(0, testListSchedulesOlderThan(date).stream()
                .filter(this::isTestSchedule).count());
    }
}
