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

package com.redhat.rhn.taskomatic.task.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.taskomatic.domain.TaskoBunch;
import com.redhat.rhn.taskomatic.domain.TaskoRun;
import com.redhat.rhn.taskomatic.domain.TaskoTask;
import com.redhat.rhn.taskomatic.domain.TaskoTemplate;
import com.redhat.rhn.taskomatic.task.RhnJavaJob;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.stream.Collectors;


public class RhnJavaJobTest {

    LoggerConfig getLoggerConfig() {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        var config = ctx.getConfiguration();
        return config.getLoggerConfig("com.redhat.rhn.taskomatic.task.test.RhnJavaJobTest$1");
    }

    @Before
    public void setUp() {

    }

    @Test
    public void testEnableLogging() throws JobExecutionException {

        for (int i = 0; i < 10; i++) {
            var job = new RhnJavaJob() {
                @Override
                public void execute(JobExecutionContext jobExecutionContextIn) {
                }

                @Override
                public void execute(JobExecutionContext context, TaskoRun run) {
                    enableLogging(run);
                }

                public RhnJavaJob setPluginName(String name) {
                    defaultPluginName = name;
                    return this;
                }

                @Override
                public String getConfigNamespace() {
                    return null;
                }
            };

            var task = new TaskoTask();
            task.setId(1L);
            task.setName("task_" + i);

            var bunch = new TaskoBunch();
            bunch.setName("bunch_" + i);

            var template = new TaskoTemplate();
            template.setBunch(bunch);
            template.setTask(task);

            var run = new TaskoRun();
            run.setTemplate(template);

            job.setPluginName("Console").execute(null, run);

        }
        var config = getLoggerConfig();

        var appenders = config.getAppenders();
        var appenderRefs = config.getAppenderRefs();

        assertEquals(2, config.getAppenderRefs().size());
        assertEquals(2, config.getAppenders().size());
        assertEquals(
                appenderRefs.stream().map(AppenderRef::getRef).collect(Collectors.toSet()),
                appenders.keySet()
        );

    }

}
