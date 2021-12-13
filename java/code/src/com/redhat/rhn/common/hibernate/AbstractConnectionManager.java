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

import static org.hibernate.resource.transaction.spi.TransactionStatus.COMMITTED;
import static org.hibernate.resource.transaction.spi.TransactionStatus.ROLLED_BACK;

import com.redhat.rhn.common.finder.FinderFactory;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.metadata.ClassMetadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;


/**
 * Manages the lifecycle of Hibernate SessionFactory and associated
 * thread-scoped Hibernate sessions.
 */
abstract class AbstractConnectionManager {

    protected final Logger LOG;

    private final List<Configurator> configurators;
    private final ThreadLocal<SessionInfo> sessionInfoThreadLocal;
    private final Set<String> packageNames;

    private SessionFactory sessionFactory;


    /**
     * Set up the connection manager.
     *
     * @param packageNamesSet set of packages that will be scanned for hbm.xml files on initialization.
     */
    protected AbstractConnectionManager(Set<String> packageNamesSet) {
        this.LOG =  Logger.getLogger(getClass());
        this.configurators = new ArrayList<>();
        this.sessionInfoThreadLocal = new ThreadLocal<>();
        this.packageNames = new HashSet<>(packageNamesSet);
    }

    /**
     * enable possibility to load hbm.xml files from different path
     *
     * @param packageNamesIn the array of package names to be added to the scan.
     */
    protected void setAdditionalPackageNames(String[] packageNamesIn) {
        packageNames.addAll(Arrays.asList(packageNamesIn));
    }

    /**
     * Register a class with HibernateFactory, to give the registered class a
     * chance to modify Hibernate configuration before creating the
     * SessionFactory.
     *
     * @param configurator Configurator to override Hibernate configuration.
     */
    public void addConfigurator(Configurator configurator) {
        // Yes, this is a race condition, but it will only ever happen at
        // startup, when we really shouldn't have multiple threads running,
        // so it isn't a real race condition.
        configurators.add(configurator);
    }

    /**
     * Verify if a transaction is pending
     *
     * @return true if a transaction is currently active.
     */
    public boolean isTransactionPending() {
        final SessionInfo info = threadSessionInfo();
        if (info == null) {
            return false;
        }

        return info.getTransaction() != null;
    }

    /**
     * Returns the metadata for the given object.
     * @param target an object instance or a class to retrieve the metadata for
     * @return the {@link ClassMetadata} for the given object.
     */
    public ClassMetadata getMetadata(Object target) {
        if (target == null) {
            return null;
        }

        if (target instanceof Class) {
            return sessionFactory.getClassMetadata((Class<?>) target);
        }

        return sessionFactory.getClassMetadata(target.getClass());
    }

    /**
     * Close the sessionFactory
     */
    public synchronized void close() {
        try {
            sessionFactory.close();
        }
        catch (HibernateException e) {
            LOG.debug("Could not close the SessionFactory", e);
        }
        finally {
            sessionFactory = null;
        }
    }

    /**
     * Check if this connection manager is closed
     *
     * @return true if it's close and the session factory is not initialized
     */
    public boolean isClosed() {
        return sessionFactory == null;
    }

    /**
     * Check if this connection manager has already been initialized.

     * @return true if {@link #initialize()} has been called and the session factory is available.
     */
    public boolean isInitialized() {
        return sessionFactory != null;
    }

    /**
     * Initializes the connection manager by creating the session factory.
     */
    public synchronized void initialize() {
        if (isInitialized()) {
            return;
        }

        createSessionFactory();
    }

    /**
     * Create a SessionFactory, loading the hbm.xml files from the specified
     * location.
     */
    protected void createSessionFactory() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            return;
        }

        try {
            final Configuration config = new Configuration();

            /*
             * Let's ask the RHN Config for all properties that begin with
             * hibernate.*
             */
            LOG.info("Adding hibernate properties to hibernate Configuration");
            config.addProperties(getConfigurationProperties());

            // Collect all the hbm files available in the specified packages
            packageNames.stream()
                        .map(FinderFactory::getFinder)
                        .flatMap(finder -> finder.find("hbm.xml").stream())
                        .peek(hbmFile -> LogMF.debug(LOG, "Adding resource {0}", hbmFile))
                        .forEach(config::addResource);

            // Invoke each configurator to add additional entries to Hibernate config
            configurators.forEach(configurator -> configurator.addConfig(config));

            // TODO: Fix auto-discovery (see commit: e92b062)
            getAnnotatedClasses().forEach(config::addAnnotatedClass);

            // add empty varchar interceptor to automatically convert empty to null
            config.setInterceptor(new EmptyVarcharInterceptor(true));

            sessionFactory = config.buildSessionFactory();
        }
        catch (HibernateException e) {
            LOG.error("FATAL ERROR creating HibernateFactory", e);
        }
    }

    protected abstract List<Class<?>> getAnnotatedClasses();

    protected abstract Properties getConfigurationProperties();

    private SessionInfo threadSessionInfo() {
        return sessionInfoThreadLocal.get();
    }

    /**
     * Commit the transaction for the current session. This method or
     * {@link #rollbackTransaction} can only be called once per session.
     * @throws HibernateException if the commit fails
     */
    public void commitTransaction() throws HibernateException {
        final SessionInfo info = threadSessionInfo();
        if (info == null || info.getSession() == null) {
            return;
        }

        final Transaction txn = info.getTransaction();
        if (txn != null) {
            txn.commit();
            info.setTransaction(null);
        }
    }

    /**
     * Roll the transaction for the current session back. This method or
     * {@link #commitTransaction} can only be called once per session.
     * @throws HibernateException if the commit fails
     */
    public void rollbackTransaction() throws HibernateException {
        final SessionInfo info = threadSessionInfo();
        if (info == null || info.getSession() == null) {
            return;
        }

        final Transaction txn = info.getTransaction();
        if (txn != null) {
            txn.rollback();
            info.setTransaction(null);
        }
    }

    /**
     * Returns Hibernate session stored in ThreadLocal storage. If not
     * present, creates a new one and stores it in ThreadLocal; creating the
     * session also begins a transaction implicitly.
     * @return Session asked for
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
                info = new SessionInfo(sessionFactory.openSession());
            }
            catch (HibernateException e) {
                throw new HibernateRuntimeException("couldn't open session", e);
            }
            sessionInfoThreadLocal.set(info);
        }

        // Automatically start a transaction
        if (info.getTransaction() == null) {
            info.setTransaction(info.getSession().beginTransaction());
        }

        return info.getSession();
    }

    /**
     * Returns Hibernate session stored in ThreadLocal storage, if it exists
     * @return Session a session
     */
    public Optional<Session> getSessionIfPresent() {
        return Optional.ofNullable(threadSessionInfo()).map(SessionInfo::getSession);
    }

    /**
     * Closes Hibernate Session stored in ThreadLocal storage.
     */
    public void closeSession() {
        SessionInfo info = threadSessionInfo();
        if (info == null) {
            return;
        }

        Session session = info.getSession();
        try {
            Transaction txn = info.getTransaction();
            if (txn != null && txn.getStatus().isNotOneOf(COMMITTED, ROLLED_BACK)) {
                try {
                    txn.commit();
                }
                catch (HibernateException e) {
                    LOG.warn("Unable to commit transaction", e);
                    txn.rollback();
                }
            }
        }
        catch (HibernateException e) {
            LOG.error(e);
        }
        finally {
            try {
                if (session != null && session.isOpen()) {
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
                sessionInfoThreadLocal.set(null);
            }
        }
    }
}
