/*
 * Copyright (c) 2023 SUSE LLC
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

import static org.jmock.AbstractExpectations.returnValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.action.test.ActionFactoryTest;
import com.redhat.rhn.taskomatic.task.MinionActionExecutor;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import com.suse.cloud.test.TestCloudPaygManagerBuilder;
import com.suse.manager.webui.services.SaltServerActionService;

import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quartz.Calendar;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.TriggerKey;
import org.quartz.impl.JobExecutionContextImpl;
import org.quartz.spi.OperableTrigger;
import org.quartz.spi.TriggerFiredBundle;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

public class MinionActionExecutorTest extends JMockBaseTestCaseWithUser {

    private static final LocalizationService LOCALIZATION = LocalizationService.getInstance();

    private Scheduler scheduler;
    private JobDetail jobDetail;
    private Calendar calendar;
    private OperableTrigger trigger;
    private Job job;
    private TriggerFiredBundle firedBundle;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);

        scheduler = mock(Scheduler.class);
        jobDetail = mock(JobDetail.class);
        calendar = mock(Calendar.class);
        trigger = mock(OperableTrigger.class);
        job = mock(Job.class);

        firedBundle = new TriggerFiredBundle(jobDetail, trigger, calendar, false, new Date(), new Date(), null, null);
    }

    @Test
    public void rejectsActionsWithOldEarliestDate() {
        Action a1 = ActionFactoryTest.createEmptyAction(user, ActionFactory.TYPE_REBOOT);
        // Set the earliest action date to one week ago
        a1.setEarliestAction(Date.from(Instant.now().minus(7, ChronoUnit.DAYS)));

        ServerAction sa1 = ActionFactoryTest.addServerAction(user, a1, ActionFactory.STATUS_COMPLETED);
        ServerAction sa2 = ActionFactoryTest.addServerAction(user, a1, ActionFactory.STATUS_QUEUED);

        TestUtils.saveAndReload(a1);

        SaltServerActionService saltServerActionService = mock(SaltServerActionService.class);

        checking(expectations -> {
            expectations.ignoring(jobDetail).getJobDataMap();
            expectations.will(returnValue(new JobDataMap(Map.of(
                "action_id", String.valueOf(a1.getId()),
                "user_id", String.valueOf(user.getId()),
                "staging_job", String.valueOf(false),
                "force_pkg_list_refresh", String.valueOf(false)
            ))));

            expectations.ignoring(jobDetail).getKey();
            expectations.will(returnValue(new JobKey("dummyJob")));

            expectations.ignoring(trigger).getJobDataMap();
            expectations.will(returnValue(new JobDataMap()));

            expectations.ignoring(trigger).getKey();
            expectations.will(returnValue(new TriggerKey("dummyTrigger")));

            expectations.never(saltServerActionService).execute(
                expectations.with(a1),
                expectations.with(false),
                expectations.with(false),
                expectations.with(Optional.empty())
            );
        });

        JobExecutionContext context = new JobExecutionContextImpl(scheduler, firedBundle, job);

        MinionActionExecutor actionExecutor = new MinionActionExecutor(saltServerActionService,
            new TestCloudPaygManagerBuilder().build());
        actionExecutor.execute(context);

        HibernateFactory.getSession().clear();

        sa1 = HibernateFactory.reload(sa1);
        sa2 = HibernateFactory.reload(sa2);

        context().assertIsSatisfied();

        String expectedMessage = LOCALIZATION.getMessage("task.action.rejection.reason",
            MinionActionExecutor.MAXIMUM_TIMEDELTA_FOR_SCHEDULED_ACTIONS);

        assertEquals(ActionFactory.STATUS_COMPLETED, sa1.getStatus());

        assertEquals(ActionFactory.STATUS_FAILED, sa2.getStatus());
        assertEquals(expectedMessage, sa2.getResultMsg());
        assertEquals(-1, sa2.getResultCode());
    }
}
