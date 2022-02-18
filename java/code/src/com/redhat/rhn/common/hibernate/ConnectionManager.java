/*
 * Copyright (c) 2021 SUSE LLC
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

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.metadata.ClassMetadata;

import java.util.Optional;

/**
 * Manages the lifecycle of Hibernate SessionFactory and associated
 * thread-scoped Hibernate sessions.
 */
public interface ConnectionManager {

    /**
     * Register a class with HibernateFactory, to give the registered class a
     * chance to modify Hibernate configuration before creating the
     * SessionFactory.
     *
     * @param configurator Configurator to override Hibernate configuration.
     **/
    void addConfigurator(Configurator configurator);

    /**
     * Closes the session factory.
     */
    void close();

    /**
     * Check if this connection manager is closed.
     *
     * @return true if it's close and the session factory is not initialized.
     */
    boolean isClosed();

    /**
     * Initializes the connection manager by creating the session factory.
     */
    void initialize();

    /**
     * Check if this connection manager has already been initialized.

     * @return true if {@link #initialize()} has been called and the session factory is available.
     */
    boolean isInitialized();

    /**
     * Allow loading hbm.xml files from additional locations.
     *
     * @param additionalLocation the array of package names to be added to the scan.
     */
    void setAdditionalPackageNames(String[] additionalLocation);

    /**
     * Returns Hibernate session stored in ThreadLocal storage. If not
     * present, creates a new one and stores it in ThreadLocal; creating the
     * session also begins a transaction implicitly.
     * @return Session asked for
     */
    Session getSession();

    /**
     * Returns Hibernate session stored in ThreadLocal storage, if it exists
     * @return Session a session
     */
    Optional<Session> getSessionIfPresent();

    /**
     * Commit the transaction for the current session. This method or
     * {@link #rollbackTransaction} can only be called once per session.
     * @throws HibernateException if the commit fails
     **/
    void commitTransaction() throws HibernateException;

    /**
     * Roll the transaction for the current session back. This method or
     * {@link #commitTransaction} can only be called once per session.
     * @throws HibernateException if the commit fails
     */
    void rollbackTransaction();

    /**
     * Verify if a transaction is pending
     *
     * @return true if a transaction is currently active.
     */
    boolean isTransactionPending();

    /**
     * Returns the metadata for the given object.
     * @param target an object instance or a class to retrieve the metadata for
     * @return the {@link ClassMetadata} for the given object.
     */
    ClassMetadata getMetadata(Object target);

    /**
     * Closes Hibernate Session stored in ThreadLocal storage.
     */
    void closeSession();

}
