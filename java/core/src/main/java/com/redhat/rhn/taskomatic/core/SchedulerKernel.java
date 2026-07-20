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

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
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
import org.quartz.TriggerListener;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.jdbcjobstore.PostgreSQLDelegate;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.utils.Key;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * Taskomatic Kernel.
 */
public final class SchedulerKernel {

    private static final Logger LOG = LogManager.getLogger(SchedulerKernel.class);

    private static final String DATASOURCE_CONFIG_PATH = "org.quartz.jobStore.dataSource";
    private static final String DATASOURCE_PREFIX = "org.quartz.dataSource";
    private static final String DEFAULT_DATASOURCE = "rhnDs";

    // Singleton instance holder to ensure the initialization is lazy and thread-safe
    private static final class InstanceHolder {
        static final SchedulerKernel INSTANCE = new SchedulerKernel(Config.get());
    }

    private final Scheduler scheduler;

    private final CountDownLatch shutDownSignal;

    private final TaskoXmlRpcServer xmlrpcServer;

    private final ChainedListener chainedTriggerListener;

    /**
     * Kernel main driver behind Taskomatic
     */
    private SchedulerKernel(Config config) {
        try {
            // Build the trigger listener chain and register listeners used by task execution.
            chainedTriggerListener = new ChainedListener();
            chainedTriggerListener.addListener(new TaskEnvironmentListener());

            // Create and configure the Quartz scheduler with the configured trigger listeners.
            scheduler = createQuartScheduler(config, chainedTriggerListener);

            // Initialize the XML-RPC endpoint used to expose Taskomatic operations.
            xmlrpcServer = new TaskoXmlRpcServer(config);

            // Latch used to block until a shutdown signal is received.
            shutDownSignal = new CountDownLatch(1);
        }
        catch (IOException ex) {
            LOG.error("Failed to initialize the TaskoXmlRpcServer", ex);
            throw new IllegalStateException("Failed to initialize the TaskoXmlRpcServer", ex);
        }
    }

    /**
     * Retrieves the singleton instance
     * @return the singleton instance
     */
    public static SchedulerKernel getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * returns scheduler
     * @return scheduler
     */
    public Scheduler getScheduler() {
        return scheduler;
    }

    /**
     * Starts Taskomatic
     * This method does not return until the this.scheduler is shutdown
     * @throws TaskomaticException error occurred during Quartz or Hibernate startup
     */
    public void startup() throws TaskomaticException {
        HibernateFactory.createSessionFactory();
        if (!HibernateFactory.isInitialized()) {
            throw new TaskomaticException("HibernateFactory failed to initialize");
        }

        MessageQueue.startMessaging();
        MessageQueue.configureDefaultActions(GlobalInstanceHolder.SALT_API);

        try {
            TaskoQuartzHelper.cleanInvalidTriggers();
            scheduler.start();
            initializeAllSatSchedules();

            xmlrpcServer.start();

            try {
                shutDownSignal.await();
            }
            catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
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
     * Shutdown the application
     */
    protected void shutdown() {
        try {
            xmlrpcServer.stop();

            scheduler.standby();
            scheduler.shutdown();
        }
        catch (SchedulerException e) {
            LOG.warn("Failed to cleanly stop the scheduler", e);
        }
        finally {
            MessageQueue.stopMessaging();
            HibernateFactory.closeSessionFactory();

            // Wake up thread waiting in startup() so it can exit
            shutDownSignal.countDown();
        }
    }


    /**
     * load DB schedule configuration
     */
    private void initializeAllSatSchedules() {
        try {
            Date now = new Date();
            Set<String> jobNames = scheduler.getJobKeys(GroupMatcher.anyJobGroup()).stream()
                            .map(Key::getName)
                            .collect(Collectors.toSet());

            for (TaskoSchedule schedule : TaskoFactory.listActiveSchedulesByOrg(null)) {
                if (!jobNames.contains(schedule.getJobLabel())) {
                    schedule.sanityCheckForPredefinedSchedules();
                    LOG.info("Initializing {}", schedule.getJobLabel());
                    TaskoQuartzHelper.createJob(schedule);
                }
                else {
                    List<TaskoRun> runList = TaskoFactory.listNewerRunsBySchedule(schedule.getId(), now);
                    if (!runList.isEmpty()) {
                        // there're runs in the future
                        // reinit the schedule
                        LOG.warn("Reinitializing {}, found {} runs in the future.",
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
                    LOG.warn("{} outdated repo-sync schedules detected and removed within org {}",
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
                LOG.warn("Number of interrupted runs: {}", interrupted);
            }
        }
        catch (Exception e) {
            LOG.error("Unexpected error while initializing schedules", e);
        }
        finally {
            HibernateFactory.closeSession();
        }
    }

    private static Scheduler createQuartScheduler(Config config, TriggerListener triggerListener) {
        try {
            Properties props = buildSchedulerProperties(config);
            SchedulerFactory factory = new StdSchedulerFactory(props);
            Scheduler scheduler = factory.getScheduler();

            scheduler.setJobFactory(new RhnJobFactory());
            scheduler.getListenerManager().addTriggerListener(triggerListener);

            PrometheusExporter.INSTANCE.startHttpServer();
            PrometheusExporter.INSTANCE.registerScheduler(scheduler, "taskomatic");

            return scheduler;
        }
        catch (SchedulerException ex) {
            LOG.error("Failed to initialize Quartz scheduler", ex);
            throw new IllegalStateException("Failed to initialize Taskomatic scheduler kernel", ex);
        }
    }

    private static Properties buildSchedulerProperties(Config config) {
        Properties props = config.getNamespaceProperties("org.quartz");

        String dbUser = config.getString(ConfigDefaults.DB_USER);
        String dbPass = config.getString(ConfigDefaults.DB_PASSWORD);
        String dbDriver = config.getString(ConfigDefaults.DB_CLASS, "org.postgresql.Driver");

        String dataSourcePrefix = DATASOURCE_PREFIX + "." + DEFAULT_DATASOURCE;

        props.setProperty(DATASOURCE_CONFIG_PATH, DEFAULT_DATASOURCE);

        props.setProperty(dataSourcePrefix + ".user", dbUser);
        props.setProperty(dataSourcePrefix + ".password", dbPass);
        props.setProperty(dataSourcePrefix + ".driver", dbDriver);
        props.setProperty(dataSourcePrefix + ".URL", ConfigDefaults.get().getJdbcConnectionString());

        props.setProperty("org.quartz.jobStore.driverDelegateClass", PostgreSQLDelegate.class.getName());

        return props;
    }
}
