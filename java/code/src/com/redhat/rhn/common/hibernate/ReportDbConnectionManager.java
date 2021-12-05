/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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
package com.redhat.rhn.common.hibernate;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

import com.redhat.rhn.common.conf.Config;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;


/**
 * Manages the lifecycle of the Hibernate SessionFactory and associated
 * thread-scoped Hibernate sessions.
 */
class ReportDbConnectionManager {

    private static final Logger LOG = Logger.getLogger(ReportDbConnectionManager.class);
    private static final String[] PACKAGE_NAMES = {};

    private final List<Configurator> configurators = new LinkedList<Configurator>();
    private SessionFactory sessionFactoryReportDb;
    private final ThreadLocal<SessionInfo> SESSION_TLS = new ThreadLocal<SessionInfo>() {

        @Override
        public SessionInfo get() {
            return super.get();
        }
    };
    private final Set<String> packageNames = new HashSet<String>(
            Arrays.asList(PACKAGE_NAMES));

    /**
     * enable possibility to load hbm.xml files from different path
     */
    void setAdditionalPackageNames(String[] packageNamesIn) {
        for (String pn : packageNamesIn) {
            packageNames.add(pn);
        }
    }

    /**
     * Register a class with HibernateFactory, to give the registered class a
     * chance to modify the Hibernate configuration before creating the
     * SessionFactory.
     * @param c Configurator to override Hibernate configuration.
     */
    public void addConfigurator(Configurator c) {
        // Yes, this is a race condition, but it will only ever happen at
        // startup, when we really shouldn't have multiple threads running,
        // so it isn't a real race condition.
        configurators.add(c);
    }

    public boolean isTransactionPending() {
        boolean retval = false;
        SessionInfo info = threadSessionInfo();
        if (info != null) {
            retval = info.getTransaction() != null;
        }
        return retval;
    }

    public ClassMetadata getMetadata(Object target) {
        ClassMetadata retval = null;
        if (target != null) {
            if (target instanceof Class) {
                retval = sessionFactoryReportDb.getClassMetadata((Class) target);
            }
            else {
                retval = sessionFactoryReportDb.getClassMetadata(target.getClass());
            }
        }
        return retval;
    }

    /**
     * Close the sessionFactory
     */
    public synchronized void close() {
        try {
            sessionFactoryReportDb.close();
        }
        catch (HibernateException e) {
            LOG.debug("Could not close the SessionFactory", e);
        }
        finally {
            sessionFactoryReportDb = null;
        }
    }

    public boolean isClosed() {
        return sessionFactoryReportDb == null;
    }

    public boolean isInitialized() {
        return sessionFactoryReportDb != null;
    }

    public synchronized void initialize() {
        if (isInitialized()) {
            return;
        }
        createSessionFactory();
    }

    /**
     * Create a SessionFactory, loading the hbm.xml files from the specified
     * location.
     * @param packageNames Package name to be searched.
     */
    private void createSessionFactory() {
        if (sessionFactoryReportDb != null && !sessionFactoryReportDb.isClosed()) {
            return;
        }

        List<String> hbms = new LinkedList<String>();

        try {
            Configuration config = new Configuration();
            /*
             * Let's ask the RHN Config for all properties that begin with
             * hibernate.*
             */
            LOG.info("Adding hibernate properties to hibernate Configuration");
            Properties hibProperties = Config.get().getNamespaceProperties("hibernate");
            hibProperties.put("hibernate.connection.username", Config.get().getString("REPORT_DB_USER"));
            hibProperties.put("hibernate.connection.password", Config.get().getString("REPORT_DB_PASSWORD"));
            hibProperties.put("hibernate.connection.url", Config.get().getString("REPORT_DB_CONNECT"));

            config.addProperties(hibProperties);

            if (configurators != null) {
                for (Iterator<Configurator> i = configurators.iterator(); i
                        .hasNext();) {
                    Configurator c = i.next();
                    c.addConfig(config);
                }
            }

            // add empty varchar warning interceptor
            EmptyVarcharInterceptor interceptor = new EmptyVarcharInterceptor();
            interceptor.setAutoConvert(true);
            config.setInterceptor(interceptor);

            sessionFactoryReportDb = config.buildSessionFactory();
        }
        catch (HibernateException e) {
            LOG.error("FATAL ERROR creating HibernateFactory", e);
        }
    }

    private SessionInfo threadSessionInfo() {
        return SESSION_TLS.get();
    }

    /**
     * Commit the transaction for the current session. This method or
     * {@link #rollbackTransaction}can only be called once per session.
     *
     * @throws HibernateException if the commit fails
     */
    public void commitTransaction() throws HibernateException {
        SessionInfo info = threadSessionInfo();
        if (info == null) {
            return;
        }
        if (info.getSession() == null) {
            // Session was never started
            return;
        }
        Transaction txn = info.getTransaction();
        if (txn != null) {
            txn.commit();
            info.setTransaction(null);
        }
    }

    /**
     * Roll the transaction for the current session back. This method or
     * {@link #commitTransaction}can only be called once per session.
     *
     * @throws HibernateException if the commit fails
     */
    public void rollbackTransaction() throws HibernateException {
        SessionInfo info = threadSessionInfo();
        if (info == null) {
            return;
        }
        if (info.getSession() == null) {
            return;
        }
        Transaction txn = info.getTransaction();
        if (txn != null) {
            txn.rollback();
            info.setTransaction(null);
        }
    }

    /**
     * Returns the Hibernate session stored in ThreadLocal storage. If not
     * present, creates a new one and stores it in ThreadLocal; creating the
     * session also begins a transaction implicitly.
     *
     * @return Session Session asked for
     */
    public Session getSession() {
        if (!isInitialized()) {
            initialize();
        }
        SessionInfo info = threadSessionInfo();
        if (info == null || info.getSession() == null) {
            try {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("YYY Opening Hibernate Session");
                }
                info = new SessionInfo(sessionFactoryReportDb.openSession());
            }
            catch (HibernateException e) {
                throw new HibernateRuntimeException("couldn't open session", e);
            }
            SESSION_TLS.set(info);
        }

        // Automatically start a transaction
        if (info.getTransaction() == null) {
            info.setTransaction(info.getSession().beginTransaction());
        }

        return info.getSession();
    }

    /**
     * Returns the Hibernate session stored in ThreadLocal storage, if it exists
     *
     * @return Session a session
     */
    public Optional<Session> getSessionIfPresent() {
        SessionInfo info = threadSessionInfo();
        if (info == null) {
            return empty();
        }
        return ofNullable(info.getSession());
    }

    /**
     * Closes the Hibernate Session stored in ThreadLocal storage.
     */
    public void closeSession() {
        SessionInfo info = threadSessionInfo();
        if (info == null) {
            return;
        }
        Session session = info.getSession();
        try {
            Transaction txn = info.getTransaction();
            if (txn != null && txn.getStatus().isNotOneOf(
                    TransactionStatus.COMMITTED, TransactionStatus.ROLLED_BACK)) {
                try {
                    txn.commit();
                }
                catch (HibernateException e) {
                    txn.rollback();
                }
            }
        }
        catch (HibernateException e) {
            LOG.error(e);
        }
        finally {
            if (session != null) {
                try {
                    if (session.isOpen()) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("YYY Closing Hibernate Session");
                        }
                        session.close();
                    }
                }
                catch (HibernateException e) {
                    throw new HibernateRuntimeException("couldn't close session");
                }
                finally {
                    SESSION_TLS.set(null);
                }
            }
            else {
                SESSION_TLS.set(null);
            }
        }
    }
}
