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
package com.redhat.rhn.taskomatic;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.filter.ThresholdFilter;
import org.apache.logging.log4j.core.layout.PatternLayout;

/**
 * Utilities to setup loggers for taskomatic tasks
 */
public class LogUtils {

    /**
     * Setup the logger to output errors in an err file and everything else in an out file
     *
     * @param clazz the class to configure the logger for
     * @param outPath the path for non-error logs
     * @param errPath the path for error logs
     * @return the configured logger
     */
    public static Logger configureLogger(Class<?> clazz, String outPath, String errPath) {

        String loggerName = clazz.getName();
        cleanLogging(loggerName);

        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        final var config = context.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(loggerName);

        PatternLayout layout = PatternLayout.newBuilder()
                .withPattern("%d [%t] %-5p %c - %m%n")
                .withConfiguration(config)
                .build();

        // Carry over the log level changes from the config file to allow getting finer logs than INFO
        Level level = loggerConfig.getLevel().isLessSpecificThan(Level.INFO) ? loggerConfig.getLevel() : Level.INFO;
        config.removeLogger(loggerName);
        Filter outFilter = ThresholdFilter.createFilter(Level.ERROR, Filter.Result.DENY, Filter.Result.ACCEPT);
        Appender outAppender = FileAppender.newBuilder().withFileName(outPath)
                .setName(loggerName + "fileAppender_out")
                .setLayout(layout)
                .setFilter(outFilter)
                .build();
        outAppender.start();

        Filter errFilter = ThresholdFilter.createFilter(Level.ERROR, Filter.Result.ACCEPT, Filter.Result.DENY);
        Appender errAppender = FileAppender.newBuilder().withFileName(errPath)
                .setName(loggerName + "fileAppender_err")
                .setLayout(layout)
                .setFilter(errFilter)
                .build();
        errAppender.start();

        AppenderRef[] refs = new AppenderRef[]{
                AppenderRef.createAppenderRef(outAppender.getName(), null, null),
                AppenderRef.createAppenderRef(errAppender.getName(), null, null)
        };
        config.addAppender(outAppender);
        config.addAppender(errAppender);
        loggerConfig = LoggerConfig.createLogger(false, level, loggerName, "false",
                refs, null, config, null);
        loggerConfig.addAppender(outAppender, level, outFilter);
        loggerConfig.addAppender(errAppender, Level.ERROR, errFilter);
        config.addLogger(loggerName, loggerConfig);

        context.updateLoggers();
        return context.getLogger(clazz);
    }

    /**
     * Remove the logger appenders
     *
     * @param loggerName the name of logger for which to stop and remove the appenders
     */
    public static void cleanLogging(String loggerName) {
        final var config = ((LoggerContext) LogManager.getContext(false)).getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(loggerName);
        if (loggerConfig.getName().equals(loggerName)) {
            for (var appender : config.getLoggerConfig(loggerName).getAppenders().values()) {
                appender.stop();
                config.getLoggerConfig(loggerName).removeAppender(appender.getName());
                config.getAppenders().remove(appender.getName());
            }
        }
    }

    private LogUtils() {
    }
}
