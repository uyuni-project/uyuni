/**
 * Copyright (c) 2009--2013 Red Hat, Inc.
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
package com.redhat.rhn.webapp;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.manager.satellite.StartupTasksCommand;
import com.redhat.rhn.manager.satellite.UpgradeCommand;

import com.suse.manager.reactor.SaltReactor;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/* Long term, if we end up with a lot of code in here, we will want to
 * move this code out of a single listener and into multiple classes
 * that each do one and only one thing, but for two startup/shutdown
 * actions, a single class is ok.
 */

/**
 * ServletContextListener for RHN Applications.  Initializes hibernate and
 * the messaging system.
 *
 * @version $Rev$
 */
public class RhnServletListener implements ServletContextListener {

    private static Logger log = Logger.getLogger(RhnServletListener.class);

    private boolean hibernateStarted = false;
    private boolean loggingStarted = false;

    // Salt event reactor instance
    private SaltReactor saltReactor = new SaltReactor();

    private void startMessaging() {
        // Start the MessageQueue thread listening for
        // Events
        MessageQueue.startMessaging();
        MessageQueue.configureDefaultActions();
    }

    private void stopMessaging() {
        MessageQueue.stopMessaging();
    }

    /**
     * Check to see if Messaging is started
     * @return boolean if or not messaging is running
     */
    public boolean messagingStarted() {
        return MessageQueue.isMessaging();
    }

    private void logStart(String system) {
        if (log.isDebugEnabled()) {
            log.debug(system + " started");
        }
        loggingStarted = true;
    }

    private void logStop(String system) {
        if (log.isDebugEnabled()) {
            log.debug(system + "Starting ");
        }
        loggingStarted = false;
    }

    /**
     * Check to see if we have started logging
     * @return boolean if or not logging is running
     */
    public boolean loggingStarted() {
        return loggingStarted;
    }

    private void startHibernate() {
        HibernateFactory.createSessionFactory();
        hibernateStarted = true;
    }

    private void stopHibernate() {
        HibernateFactory.closeSessionFactory();
        hibernateStarted = false;
    }

    /**
     * Have we started Hibernate
     * @return boolean value if we started hibernate
     */
    public boolean hibernateStarted() {
        return hibernateStarted;
    }

    /** {@inheritDoc} */
    public void contextInitialized(ServletContextEvent sce) {
        startMessaging();
        logStart("Messaging");

        startHibernate();
        logStart("Hibernate");

        // the following is not safe to run in the testsuite
        // and will be excluded from test runs
        if (sce != null) {
            saltReactor.start();
            logStart("Salt reactor");
        }

        log.debug("Starting upgrade check");
        executeUpgradeStep();

        log.debug("Executing startup tasks");
        executeStartupTasks();
    }

    private void executeUpgradeStep() {
        log.debug("calling UpgradeCommand.");
        UpgradeCommand cmd = new UpgradeCommand();
        cmd.store();
        log.debug("UpgradeCommand done.");
    }

    /**
     * Tasks that should be run on application startup.
     */
    private void executeStartupTasks() {
        var startupTasksCommand = new StartupTasksCommand();
        startupTasksCommand.run();
    }

    /** {@inheritDoc} */
    public void contextDestroyed(ServletContextEvent sce) {
        saltReactor.stop();
        logStop("Salt reactor");

        stopMessaging();
        logStop("Messaging");

        stopHibernate();
        logStop("Hibernate");

        if (sce == null) {
            // this has been called from the testsuite, next steps would
            // break subsequent tests
            return;
        }

        // This manually deregisters JDBC driver,
        // which prevents Tomcat from complaining about memory leaks
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
                log.info("deregistering jdbc driver: " + driver);
            }
            catch (SQLException e) {
                log.warn("Error deregistering driver " + driver);
            }
        }


        // shutdown the logger to avoid ThreadDeath exception during
        // webapp reload.
        LogManager.shutdown();
    }
}
