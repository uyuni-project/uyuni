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
package com.redhat.rhn.taskomatic.core.test;

import com.redhat.rhn.taskomatic.core.TaskomaticDaemon;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class TestDaemon extends TaskomaticDaemon {

    @Override
    protected int onShutdown(boolean breakFromUser) {
        return TaskomaticDaemon.SUCCESS;
    }

    @Override
    protected int onStartup(CommandLine commandLine) {
        new Thread(new DaemonLogic()).start();
        return TaskomaticDaemon.SUCCESS;
    }

    public static void main(String[] argv) {
        TestDaemon td = new TestDaemon();
        td.registerImplementation(argv);

        optionListPseudoTest(td);
    }

    public static void optionListPseudoTest(TestDaemon td) {
        Options options = td.buildOptionsList();
        System.out.println(options.getOptions().size() == 9);
        Option opt = options.getOption("debug");
        System.out.println(opt.toString()
                .equals("[ option: debug debug  :: turn on debug mode :: class java.lang.String ]"));
        opt = options.getOption("daemon");
        System.out.println(opt.toString()
                .equals("[ option: daemon daemon  :: turn on daemon mode :: class java.lang.String ]"));
        opt = options.getOption("single");
        System.out.println(opt.toString()
                .equals("[ option: single single  :: run a single task and exit :: class java.lang.String ]"));
        opt = options.getOption("help");
        System.out.println(opt.toString()
                .equals("[ option: help help  :: prints out help screen :: class java.lang.String ]"));
        opt = options.getOption("pidfile");
        System.out.println(opt.toString()
                .equals("[ option: pidfile pidfile  [ARG] :: use PID file <pidfile> :: class java.lang.String ]"));
        opt = options.getOption("task");
        System.out.println(opt.toString()
                .equals("[ option: task task  [ARG] " +
                        ":: run this task (may be specified multiple times) :: class java.lang.String ]"));
        opt = options.getOption("dburl");
        System.out.println(opt.toString()
                .equals("[ option: dburl dburl  [ARG] :: jdbcurl :: class java.lang.String ]"));
        opt = options.getOption("dbuser");
        System.out.println(opt.toString()
                .equals("[ option: dbuser dbuser  [ARG] :: database username :: class java.lang.String ]"));
        opt = options.getOption("dbpassword");
        System.out.println(opt.toString()
                .equals("[ option: dbpassword dbpassword  [ARG] :: database password :: class java.lang.String ]"));
    }

    class DaemonLogic implements Runnable {
        @Override
        public void run() {
            System.out.println("Hello, world!");
            try {
                Thread.sleep(10000);
            }
            catch (InterruptedException e) {
            }
        }
    }
}
