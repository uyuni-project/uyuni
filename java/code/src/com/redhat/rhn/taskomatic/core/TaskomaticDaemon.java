/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.taskomatic.core;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of the Taskomatic schedule task execution system.
 * This class serves merely as an interface between the native daemon
 * library and the actual scheduler setup and running logic implemented
 * in SchedulerKernel.
 * @see SchedulerKernel
 */
public class TaskomaticDaemon {

    public static final int ERR_SCHED_CREATE = -5;
    public static final int SUCCESS = Integer.MIN_VALUE;
    public static final Logger LOG = LogManager.getLogger(TaskomaticDaemon.class);

    private Map<String, Option> masterOptionsMap = new HashMap<>();
    private SchedulerKernel kernel;

    /**
     * Main entry point for the native daemon
     *
     * @param argv "Command-line" parameters
     */
    public static void main(String[] argv) {
        TaskomaticDaemon daemon = new TaskomaticDaemon();
        daemon.registerImplementation(argv);
    }

    /**
     * Starts TaskomaticDaemon
     *
     * @param argv Arguments configured in the daemon's config file
     * @return Integer indicating status (null indicates success, else value indicates
     * error code)
     */
    public Integer start(String[] argv) {
        Integer retval = null;
        Options options = buildOptionsList();
        int status = SUCCESS;
        if (options != null) {
            status = startupWithOptions(options, argv);
            if (status != SUCCESS) {
                retval = status;
            }
        }
        else {
            status = startupWithoutOptions();
            if (status != SUCCESS) {
                retval = status;
            }
        }
        return retval;
    }

    /**
     * Parse startup options using jakarta-commons-cli and start
     * the daemon implementation
     *
     * @param options Master list of options built by the daemon implementation
     * @param argv    Startup arguments
     * @return int indicates status where <code>TaskomaticDaemon.SUCCESS</code>
     * indicates success and any other number indicates failure
     */
    private int startupWithOptions(Options options, String[] argv) {
        int retval = SUCCESS;
        CommandLineParser parser = null;
        try {
            parser = new DefaultParser();
            CommandLine cl = parser.parse(options, argv);
            retval = onStartup(cl);
        }
        catch (ParseException e) {
            retval = onOptionsParseError(e);
            if (retval == SUCCESS) {
                retval = onStartup(null);
            }
        }
        return retval;
    }

    /**
     * Start the daemon implementation with no startup parameters
     *
     * @return int indicates status where <code>TaskomaticDaemon.SUCCESS</code>
     * indicates success and any other number indicates failure
     */
    private int startupWithoutOptions() {
        return onStartup(null);
    }

    protected Options buildOptionsList() {
        Options accum = new Options();
        createOption(accum, TaskomaticConstants.CLI_DEBUG,
                false, null, "turn on debug mode");
        createOption(accum, TaskomaticConstants.CLI_DAEMON,
                false, null, "turn on daemon mode");
        createOption(accum, TaskomaticConstants.CLI_SINGLE,
                false, null, "run a single task and exit");
        createOption(accum, TaskomaticConstants.CLI_HELP,
                false, null, "prints out help screen");
        createOption(accum, TaskomaticConstants.CLI_PIDFILE,
                true, "<pidfile>", "use PID file <pidfile>");
        createOption(accum, TaskomaticConstants.CLI_TASK,
                true, "taskname", "run this task (may be specified multiple times)");
        createOption(accum, TaskomaticConstants.CLI_DBURL,
                true, "url", "jdbcurl");
        createOption(accum, TaskomaticConstants.CLI_DBUSER,
                true, "username", "database username");
        createOption(accum, TaskomaticConstants.CLI_DBPASSWORD,
                true, "password", "database password");
        return accum;
    }

    protected int onStartup(CommandLine commandLine) {
        Map<String, Object> overrides;
        int retval = SUCCESS;

        if (commandLine != null) {
            overrides = parseOverrides(commandLine);
        }
        try {
            this.kernel = new SchedulerKernel();
            Runnable r = () -> {
                try {
                    kernel.startup();
                }
                catch (Throwable e) {
                    LOG.fatal(e.getMessage());
                    LOG.fatal(ExceptionUtils.getStackTrace(e));
                    System.exit(-1);
                }
            };
            Thread t = new Thread(r);
            t.start();
        }
        catch (Throwable t) {
            LOG.fatal("Fatal error starting Taskomatic", t);
            System.exit(-1);
        }
        return retval;
    }

    protected int onShutdown(boolean breakFromUser) {
        return 0;
    }

    private Map<String, Object> parseOverrides(CommandLine commandLine) {
        Map<String, Object> configOverrides = new HashMap<>();
        // Loop thru all possible options and let's see what we get
        for (String optionName : this.masterOptionsMap.keySet()) {
            if (commandLine.hasOption(optionName)) {

                // All of these options are single-value options so they're
                // grouped together here
                if (optionName.equals(TaskomaticConstants.CLI_PIDFILE) ||
                        optionName.equals(TaskomaticConstants.CLI_DBURL) ||
                        optionName.equals(TaskomaticConstants.CLI_DBUSER) ||
                        optionName.equals(TaskomaticConstants.CLI_DBPASSWORD)) {
                    configOverrides.put(optionName,
                            commandLine.getOptionValue(optionName));
                }

                // The presence of these options toggle them on, hence the use of
                // Boolean.TRUE
                else if (optionName.equals(TaskomaticConstants.CLI_DEBUG) ||
                        optionName.equals(TaskomaticConstants.CLI_DAEMON) ||
                        optionName.equals(TaskomaticConstants.CLI_SINGLE)) {
                    configOverrides.put(optionName, Boolean.TRUE);
                }

                // Possibly multi-value list of task implementations to schedule
                else if (optionName.equals(TaskomaticConstants.CLI_TASK)) {
                    String[] taskImpls = commandLine.getOptionValues(optionName);
                    if (taskImpls != null && taskImpls.length > 0) {
                        configOverrides.put(optionName, Arrays.asList(taskImpls));
                    }
                }
            }
        }
        return configOverrides;
    }

    private void createOption(Options accum, String longopt, boolean arg,
                              String argName, String description) {
        Option option = Option.builder(longopt)
                .argName(argName)
                .longOpt(longopt)
                .hasArg(arg)
                .desc(description)
                .build();
        accum.addOption(option);
        this.masterOptionsMap.putIfAbsent(longopt, option);
    }

    /**
     * Registers the daemon implementation with the scheduler
     *
     * @param argv startup parameters (if any)
     */
    protected void registerImplementation(String[] argv) {
        start(argv);
    }

    /**
     * Lifecycle method called when startup parameters cannot be parsed. This gives
     * the daemon implementation an opportunity to do something about the error such
     * as display a usage message.
     *
     * @param e the ParseException
     * @return int indicates error code. If TaskomaticDaemon.SUCCESS is returned, then
     * the framework will still try to start the daemon implementation _without_ parameters.
     */
    protected int onOptionsParseError(ParseException e) {
        return SUCCESS;
    }
}
