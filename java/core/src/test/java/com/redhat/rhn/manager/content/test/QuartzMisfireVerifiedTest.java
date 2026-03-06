/*
 * Copyright (c) 2026 SUSE LCC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.manager.content.test;

import static org.junit.Assert.fail;

import com.redhat.rhn.common.hibernate.HibernateFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Reproduces issue we see on logs:
 * [DefaultQuartzScheduler_QuartzSchedulerThread] WARN  com.mchange.v2.c3p0.impl.NewPooledConnection - [c3p0] A PooledConnection that has already signalled a Connection error is still in use!
 * [DefaultQuartzScheduler_QuartzSchedulerThread] WARN  com.mchange.v2.c3p0.impl.NewPooledConnection - [c3p0] Another error has occurred [ org.postgresql.util.PSQLException: This connection has been closed. ] which will not be reported to listeners!
 * org.postgresql.util.PSQLException: This connection has been closed.
 * 	at org.postgresql.jdbc.PgConnection.checkClosed(PgConnection.java:880) ~[postgresql.jar:42.2.25]
 * 	...
 * [DefaultQuartzScheduler_QuartzSchedulerThread] ERROR org.quartz.impl.jdbcjobstore.JobStoreTX - Couldn't rollback jdbc connection. This connection has been closed.
 * org.postgresql.util.PSQLException: This connection has been closed.
 * 	at org.postgresql.jdbc.PgConnection.checkClosed(PgConnection.java:880) ~[postgresql.jar:42.2.25]
 * 	...
 *
 * It's been designed to match the exact error scenario:
 * - [QuartzScheduler_..._MisfireHandler] thread
 * - PSQLException: FATAL: terminating connection due to administrator command
 * - c3p0 pool errors
 * - JobStoreTX - MisfireHandler: Error handling misfires
 */
public class QuartzMisfireVerifiedTest {
    private static final Logger LOG = LogManager.getLogger(QuartzMisfireVerifiedTest.class);

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/susemanager";
    private static final String DB_USER = "spacewalk";
    private static final String DB_PASSWORD = "spacewalk";
    private static final String UNIQUE_JOB_ID = "verifiedJob_" + System.currentTimeMillis();
    private static final String VERIFIED_GROUP = "verifiedGroup";

    private Scheduler scheduler;
    private Connection adminConnection;

    @BeforeEach
    public void setUp() throws Exception {
        try {
            adminConnection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (Exception e) {
            fail("Could not create admin connection to db: " + e.getMessage());
        }

        // Configure quartz from java/conf/default/rhn_org_quartz.conf
        Properties props = new Properties();
        props.setProperty("org.quartz.scheduler.instanceName", "VerifiedTestScheduler");
        props.setProperty("org.quartz.scheduler.instanceId", "AUTO");
        
        // Thread pool
        props.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        props.setProperty("org.quartz.threadPool.threadCount", "3");
        
        // Job store
        props.setProperty("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX");
        props.setProperty("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate");
        props.setProperty("org.quartz.jobStore.dataSource", "quartzDS");
        props.setProperty("org.quartz.jobStore.tablePrefix", "QRTZ_");
        props.setProperty("org.quartz.jobStore.isClustered", "false");
        
        // short misfire threshold
        props.setProperty("org.quartz.jobStore.misfireThreshold", "1000");
        
        // Data source
        props.setProperty("org.quartz.dataSource.quartzDS.driver", "org.postgresql.Driver");
        props.setProperty("org.quartz.dataSource.quartzDS.URL", DB_URL);
        props.setProperty("org.quartz.dataSource.quartzDS.user", DB_USER);
        props.setProperty("org.quartz.dataSource.quartzDS.password", DB_PASSWORD);
        props.setProperty("org.quartz.dataSource.quartzDS.maxConnections", "5");

        StdSchedulerFactory factory = new StdSchedulerFactory(props);
        scheduler = factory.getScheduler();
    }

    /**
     * Clean up jobs from this test
     * @throws Exception if an error occurs while cleaning up
     */
    @AfterEach
    public void tearDown() throws Exception {
        if (scheduler != null && !scheduler.isShutdown()) {
            try {
                scheduler.clear();
                scheduler.shutdown(true);
            } catch (Exception e) {
                LOG.error("Unexpected exception while shutting down scheduler: " + e.getMessage());
            }
        }

        adminConnection.close();
    }

    public static class VerifiedJob implements Job {
        @Override
        public void execute(JobExecutionContext context) {
            LOG.debug("Executing job key = " + context.getJobDetail().getKey());
        }
    }

    /**
     * Setup:
     * 1) Start scheduler - MisfireHandler becomes active
     * 2) Schedule a job that fires frequently
     * 3) Let scheduler run normally
     * 4) Terminate database connections while MisfireHandler periodically checks
     * 5) Wait for MisfireHandler to hit the error
     */
    @Test
    public void testMisfireHandlerWithConnectionTermination() {
        try {
            // Starting scheduler
            scheduler.start();
            LOG.info("Scheduler started");

            // Schedule a job with a 1s interval
            JobDetail job = JobBuilder.newJob(VerifiedJob.class)
                    .withIdentity(UNIQUE_JOB_ID, VERIFIED_GROUP)
                    .storeDurably()
                    .requestRecovery()
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(UNIQUE_JOB_ID + "_trigger", VERIFIED_GROUP)
                    .startNow()
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInSeconds(1)
                            .repeatForever()
                            .withMisfireHandlingInstructionFireNow())
                    .build();

            scheduler.scheduleJob(job, trigger);
            LOG.debug("Job scheduled (fires every 1 second)");

            // allow scheduler to run normally
            LOG.debug("Allowing jobs to run normally...");
            Thread.sleep(2100);

            // terminate connections
            LOG.debug("Terminating connections...");
            terminateConnections();

            // wait for jobs to misfire
            LOG.debug("Waiting for jobs to fail...");
            Thread.sleep(2100);

        } catch (Exception e) {
            LOG.error("Test execution error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Terminate active Quartz database connections using pg_terminate_backend
     */
    private void terminateConnections() {
        try {
            Statement stmt = adminConnection.createStatement();
            
            // Find active connections
            String query = 
                "SELECT pid, state, query FROM pg_stat_activity " +
                "WHERE datname = 'susemanager' " +
                "AND usename = '" + DB_USER + "' " +
                "AND pid != pg_backend_pid() " +
                "AND state IS NOT NULL";
            
            ResultSet rs = stmt.executeQuery(query);
            
            int count = 0;
            while (rs.next()) {
                int pid = rs.getInt("pid");
                String state = rs.getString("state");
                
                try {
                    Statement killStmt = adminConnection.createStatement();
                    killStmt.execute("SELECT pg_terminate_backend(" + pid + ")");
                    LOG.debug("Terminated PID " + pid + " (state: " + state + ")");
                    count++;
                    killStmt.close();
                } catch (Exception e) {
                    LOG.error("Failed to terminate PID " + pid + ": " + e.getMessage());
                }
            }
            
            rs.close();
            stmt.close();
            
            if (count > 0) {
                LOG.debug("Terminated " + count + " connections");
            } else {
                LOG.debug("No active connections found to terminate");
                LOG.debug("(MisfireHandler may have finished already)");
            }
            
        } catch (Exception e) {
            LOG.error("Error terminating connections: " + e.getMessage());
        }
    }
}
