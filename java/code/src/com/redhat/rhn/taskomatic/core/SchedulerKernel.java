/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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

import static java.util.stream.Collectors.toSet;

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.conf.ConfigException;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.taskomatic.TaskoFactory;
import com.redhat.rhn.taskomatic.TaskoQuartzHelper;
import com.redhat.rhn.taskomatic.TaskoXmlRpcServer;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.domain.TaskoRun;
import com.redhat.rhn.taskomatic.domain.TaskoSchedule;

import com.suse.manager.metrics.PrometheusExporter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.utils.Key;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Taskomatic Kernel.
 */
public class SchedulerKernel {

    private static final String[] TASKOMATIC_PACKAGE_NAMES =
            {"com.redhat.rhn.taskomatic.domain"};
    private static Logger log = LogManager.getLogger(SchedulerKernel.class);
    private byte[] shutdownLock = new byte[0];
    private static SchedulerFactory factory = null;
    private static Scheduler scheduler = null;
    private static TaskoXmlRpcServer xmlrpcServer = null;
    private ChainedListener chainedTriggerListener = null;
    private String dataSourceConfigPath = "org.quartz.jobStore.dataSource";
    private String dataSourcePrefix = "org.quartz.dataSource";
    private String defaultDataSource = "rhnDs";

    /**
     * Kernel main driver behind Taskomatic
     * @throws InstantiationException thrown if this.scheduler can't be initialized.
     * @throws UnknownHostException thrown if xmlrcp host is unknown
     */
    public SchedulerKernel() throws InstantiationException, UnknownHostException {
        Properties props = Config.get().getNamespaceProperties("org.quartz");
        String dbUser = Config.get().getString(ConfigDefaults.DB_USER);
        String dbPass = Config.get().getString(ConfigDefaults.DB_PASSWORD);
        props.setProperty(dataSourceConfigPath, defaultDataSource);
        String ds = dataSourcePrefix + "." + defaultDataSource;
        props.setProperty(ds + ".user", dbUser);
        props.setProperty(ds + ".password", dbPass);
        // props.setProperty(ds + ".maxConnections", 30);

        props.setProperty("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate");

        String driver = Config.get().getString(ConfigDefaults.DB_CLASS, "org.postgresql.Driver");
        props.setProperty(ds + ".driver", driver);
        props.setProperty(ds + ".URL", ConfigDefaults.get().getJdbcConnectionString());

        try {
            SchedulerKernel.factory = new StdSchedulerFactory(props);
            SchedulerKernel.scheduler = SchedulerKernel.factory.getScheduler();
            SchedulerKernel.scheduler.setJobFactory(new RhnJobFactory());

            // Setup TriggerListener chain
            this.chainedTriggerListener = new ChainedListener();
            this.chainedTriggerListener.addListener(new TaskEnvironmentListener());

            try {
                scheduler.getListenerManager()
                        .addTriggerListener(this.chainedTriggerListener);
            }
            catch (SchedulerException e) {
                throw new ConfigException(e.getLocalizedMessage(), e);
            }
            xmlrpcServer = new TaskoXmlRpcServer(Config.get());
            xmlrpcServer.start();

            PrometheusExporter.INSTANCE.startHttpServer();
            PrometheusExporter.INSTANCE.registerScheduler(SchedulerKernel.scheduler, "taskomatic");
        }
        catch (SchedulerException e) {
            throw new InstantiationException("this.scheduler failed");
        }
    }

    /**
     * returns scheduler
     * @return scheduler
     */
    public static Scheduler getScheduler() {
        return SchedulerKernel.scheduler;
    }

    /**
     * Starts Taskomatic
     * This method does not return until the this.scheduler is shutdown
     * @throws TaskomaticException error occurred during Quartz or Hibernate startup
     */
    public void startup() throws TaskomaticException {
        HibernateFactory.registerComponentName("taskomatic");
        HibernateFactory.createSessionFactory(TASKOMATIC_PACKAGE_NAMES);
        if (!HibernateFactory.isInitialized()) {
            throw new TaskomaticException("HibernateFactory failed to initialize");
        }
        MessageQueue.startMessaging();
        MessageQueue.configureDefaultActions(GlobalInstanceHolder.SALT_API);
        try {
            SchedulerKernel.scheduler.start();
            initializeAllSatSchedules();
            synchronized (this.shutdownLock) {
                try {
                    this.shutdownLock.wait();
                }
                catch (InterruptedException ignored) {
                }
            }
        }
        catch (SchedulerException e) {
            throw new TaskomaticException(e.getMessage(), e);
        }
    }

    /**
     * Initiates the shutdown process. Needs to happen in a
     * separate thread to prevent Quartz scheduler errors.
     */
    public void startShutdown() {
        Runnable shutdownTask = () -> shutdown();
        Thread t = new Thread(shutdownTask);
        t.setDaemon(true);
        t.start();
    }

    /**
     * Shutsdown the application
     */
    protected void shutdown() {
        try {
            SchedulerKernel.scheduler.standby();
            SchedulerKernel.scheduler.shutdown();
        }
        catch (SchedulerException e) {
            log.warn("Failed to cleanly stop the scheduler", e);
        }
        finally {
            MessageQueue.stopMessaging();
            HibernateFactory.closeSessionFactory();
            // Wake up thread waiting in startup() so it can exit
            synchronized (this.shutdownLock) {
                this.shutdownLock.notify();
            }
        }
    }


    /**
     * load DB schedule configuration
     */
    public void initializeAllSatSchedules() {
        Set<String> jobNames;
        Date now = new Date();
        try {
            jobNames = SchedulerKernel.scheduler.getJobKeys(GroupMatcher.anyJobGroup())
                    .stream().map(Key::getName).collect(toSet());
            for (TaskoSchedule schedule : TaskoFactory.listActiveSchedulesByOrg(null)) {
                if (!jobNames.contains(schedule.getJobLabel())) {
                    schedule.sanityCheckForPredefinedSchedules();
                    log.info("Initializing {}", schedule.getJobLabel());
                    TaskoQuartzHelper.createJob(schedule);
                }
                else {
                    List<TaskoRun> runList =
                            TaskoFactory.listNewerRunsBySchedule(schedule.getId(), now);
                    if (!runList.isEmpty()) {
                        // there're runs in the future
                        // reinit the schedule
                        log.warn("Reinitializing {}, found {} runs in the future.",
                                schedule.getJobLabel(), runList.size());
                        TaskoFactory.reinitializeScheduleFromNow(schedule, now);
                        for (TaskoRun run : runList) {
                            TaskoFactory.deleteRun(run);
                        }
                    }
                }
            }
            // delete outdated reposync leftovers
            TaskomaticApi tasko = new TaskomaticApi();
            for (Org org : OrgFactory.lookupAllOrgs()) {
                int removed = tasko.unscheduleInvalidRepoSyncSchedules(org);
                if (removed > 0) {
                    log.warn("{} outdated repo-sync schedules detected and removed within org {}",
                            removed, org.getId());
                }
            }
            // close unfinished runs
            int interrupted = 0;
            for (TaskoRun run : TaskoFactory.listUnfinishedRuns()) {
                run.setStatus(TaskoRun.STATUS_INTERRUPTED);
                run.setEndTime(now);
                TaskoFactory.save(run);
                interrupted++;
            }
            if (interrupted > 0) {
                log.warn("Number of interrupted runs: {}", interrupted);
            }
            HibernateFactory.closeSession();
        }
        catch (Exception e) {
            log.error("Unexpected error while initializing schedules", e);
        }
    }
}
