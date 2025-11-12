/*
 * Copyright (c) 2024 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.redhat.rhn.taskomatic.task.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.taskomatic.TaskoFactory;
import com.redhat.rhn.taskomatic.domain.TaskoRun;
import com.redhat.rhn.taskomatic.domain.TaskoSchedule;
import com.redhat.rhn.taskomatic.domain.TaskoTemplate;
import com.redhat.rhn.taskomatic.task.ChannelRepodata;
import com.redhat.rhn.taskomatic.task.RhnJob;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;

import org.hibernate.type.StandardBasicTypes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quartz.JobExecutionContext;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

import javax.persistence.Tuple;

public class ChannelRepodataTest extends JMockBaseTestCaseWithUser {

    private JobExecutionContext jobContext;

    private ChannelRepodata channelRepodata;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        channelRepodata = new ChannelRepodata();
        jobContext = mock(JobExecutionContext.class);
    }

    @AfterEach
    @Override
    public void tearDown() throws Exception {
        super.tearDown();

        // Delete all the runs we created
        TaskoFactory.listRunsByBunch("channel-repodata-bunch")
            .forEach(TaskoFactory::deleteRun);
    }

    @Test
    public void canProcessRepositoriesDataForChannel() throws Exception {
        // A commit will happen in taskomatic, so we need to clean up everything in the end
        commitHappened();

        // Fill in the rhnRepoRegenQueue table to request the repodata generation execution
        Channel testChannel = ChannelFactoryTest.createTestChannel(user);
        ChannelManager.queueChannelChange(testChannel.getLabel(), "createchannel", "createchannel");

        // Create the run for the task
        TaskoRun firstRun = createTaskomaticTaskRun("channel-repodata-default", ChannelRepodata.class);
        channelRepodata.execute(jobContext, firstRun);

        // Wait for the worker to process the channel
        assertConditionWithRetries("Channel has been processed", () -> isChannelProcessed(testChannel), 500, 10);

        // Still the run we created should be in status RUNNING, as the driver is not blocking the task queue
        firstRun = HibernateFactory.reload(firstRun);
        assertEquals(TaskoRun.STATUS_RUNNING, firstRun.getStatus());
        assertNull(firstRun.getEndTime());

        // Subsequent run should mark the first run as FINISHED
        TaskoRun secondRun = createTaskomaticTaskRun("channel-repodata-default", ChannelRepodata.class);
        channelRepodata.execute(jobContext, secondRun);

        firstRun = HibernateFactory.reload(firstRun);
        secondRun = HibernateFactory.reload(secondRun);

        assertEquals(TaskoRun.STATUS_SKIPPED, secondRun.getStatus());
        assertEquals(TaskoRun.STATUS_FINISHED, firstRun.getStatus());
        assertNotNull(firstRun.getEndTime());
    }

    private static boolean isChannelProcessed(Channel channel) {
        Integer items = HibernateFactory.getSession()
            .createNativeQuery("SELECT COUNT(*) AS count FROM rhnRepoRegenQueue WHERE channel_label = :label",
                    Tuple.class)
            .addScalar("count", StandardBasicTypes.INTEGER)
            .setParameter("label", channel.getLabel())
            .getSingleResult()
                .get(0, Integer.class);

        return items == 0;
    }

    private static TaskoRun createTaskomaticTaskRun(String scheduleLabel, Class<? extends RhnJob> taskClass) {
        List<TaskoSchedule> schedules = TaskoFactory.listScheduleByLabel(scheduleLabel);
        assertEquals(1, schedules.size(), "Expected exactly one schedule with label " + scheduleLabel);

        List<TaskoTemplate> templates = schedules.get(0).getBunch().getTemplates()
            .stream()
            .filter(template -> taskClass.getName().equals(template.getTask().getTaskClass()))
            .collect(Collectors.toList());
        assertEquals(1, templates.size(), "Expected exactly one template with task " + taskClass.getName());

        TaskoRun taskoRun = new TaskoRun(null, templates.get(0), schedules.get(0).getId());
        TaskoFactory.save(taskoRun);
        return taskoRun;
    }

    private static void assertConditionWithRetries(String name, BooleanSupplier condition, long waitTime, int retries)
        throws InterruptedException {
        for (int i = 0; i < retries; i++) {
            boolean result = condition.getAsBoolean();
            if (result) {
                return;
            }

            Thread.sleep(waitTime);
        }

        fail("Unable to verify condition '" + name + "' after " + retries + " attempt");
    }

}
