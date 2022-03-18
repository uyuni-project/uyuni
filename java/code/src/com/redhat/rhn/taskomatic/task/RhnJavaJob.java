/*
 * Copyright (c) 2010--2013 Red Hat, Inc.
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
package com.redhat.rhn.taskomatic.task;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.manager.satellite.SystemCommandThreadedExecutor;
import com.redhat.rhn.taskomatic.domain.TaskoRun;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;



/**
 * RhnJavaJob
 */
public abstract class RhnJavaJob implements RhnJob {

    protected Logger log = LogManager.getLogger(getClass());

    protected Logger getLogger() {
        return log;
    }

    void enableLogging(TaskoRun run) {

        var builder = ConfigurationBuilderFactory.newConfigurationBuilder();

        var layoutBuilder = builder
                .newLayout("PatternLayout")
                .addAttribute("pattern", DEFAULT_LOGGING_LAYOUT);
        var appenderName = this.getClass().getName()+"fileAppender";
        var appenderBuilder = builder
                .newAppender(appenderName, "File")
                .addAttribute("fileName", run.buildStdOutputLogPath())
                .add(layoutBuilder);
        builder.add(appenderBuilder);

        var out = builder.newLogger(this.getClass().getName(), Level.INFO);
        var err = builder.newLogger(this.getClass().getName(), Level.ERROR);
        builder.add(out.add(builder.newAppenderRef(appenderName)));
        builder.add(err.add(builder.newAppenderRef(appenderName)));
        Configurator.initialize(builder.build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void appendExceptionToLogError(Exception e) {
        log.error("Executing a task threw an exception: " + e.getClass().getName());
        log.error("Message: " + e.getMessage());
        log.error("Cause: " + e.getCause());

        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
        log.error("Stack trace:" + errors.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(JobExecutionContext context, TaskoRun run)
        throws JobExecutionException {
        run.start();
        enableLogging(run);
        HibernateFactory.commitTransaction();
        HibernateFactory.closeSession();
        execute(context);
        run.saveStatus(TaskoRun.STATUS_FINISHED);
        run.finished();
        HibernateFactory.commitTransaction();
        HibernateFactory.closeSession();
        finishJob();
        HibernateFactory.commitTransaction();
        HibernateFactory.closeSession();
    }

    /**
     * Finish the job after the main DB transaction has been committed.
     */
    protected void finishJob() { }

    protected void executeExtCmd(String[] args)
        throws JobExecutionException {

        SystemCommandThreadedExecutor ce = new SystemCommandThreadedExecutor(getLogger());
        int exitCode = ce.execute(args);

        if (exitCode != 0) {
            String msg = ce.getLastCommandErrorMessage();
            if (msg.isBlank()) {
                msg = ce.getLastCommandOutput();
            }
            if (msg.length() > 2300) {
                msg = "... " + msg.substring(msg.length() - 2300);
            }
            throw new JobExecutionException(
                    "Command '" + Arrays.asList(args) +
                    "' exited with error code " + exitCode +
                    (msg.isBlank() ? "" : ": " + msg));
        }
    }
}
