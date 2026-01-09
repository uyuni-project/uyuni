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

import com.redhat.rhn.common.db.DatabaseException;
import com.redhat.rhn.common.db.datasource.CallableMode;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.engine.jdbc.LobCreator;
import org.hibernate.query.Query;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import jakarta.persistence.FlushModeType;

/**
 * HibernateFactory - Helper superclass that contains methods for fetching and
 * storing Objects from the DB using Hibernate.
 * <p>
 * Abstract methods define what the subclass must implement to determine what is
 * specific to that Factory's instance.
 */
public class ReportDbHibernateFactory {

    private final ConnectionManager connectionManager;
    private static final Logger LOG = LogManager.getLogger(ReportDbHibernateFactory.class);
    private static final int LIST_BATCH_MAX_SIZE = 1000;

    /**
     * Create a new hibernate factory for reporting with the specified {@link ReportDbConnectionManager}
     *
     * @param conMgr the configuration manager
     */
    public ReportDbHibernateFactory(ConnectionManager conMgr) {
        connectionManager = conMgr;
    }

    /**
     * Register a class with HibernateFactory, to give the registered class a
     * chance to modify the Hibernate configuration before creating the
     * SessionFactory.
     * @param c Configurator to override Hibernate configuration.
     */
    public void addConfigurator(Configurator c) {
        connectionManager.addConfigurator(c);
    }

    /**
     * Close the sessionFactory
     */
    public void closeSessionFactory() {
        connectionManager.close();
    }

    /**
     * Is the factory closed
     * @return boolean
     */
    public boolean isClosed() {
        return connectionManager.isClosed();
    }

    /**
     * Create a SessionFactory
     */
    public void createSessionFactory() {
        connectionManager.initialize();
    }

    /**
     * Get the Logger for the derived class so log messages show up on the
     * correct class
     * @return Logger for this class.
     */
    protected Logger getLogger() {
        return LOG;
    }

    /**
     * Returns the Hibernate session stored in ThreadLocal storage. If not
     * present, creates a new one and stores it in ThreadLocal; creating the
     * session also begins a transaction implicitly.
     *
     * @return Session Session asked for
     */
    public Session getSession() {
        return connectionManager.getSession();
    }

    /**
     * Returns the Hibernate session stored in ThreadLocal storage, if it exists
     *
     * @return Session a session
     */
    public Optional<Session> getSessionIfPresent() {
        return connectionManager.getSessionIfPresent();
    }

    /**
     * Commit the transaction for the current session. This method or
     * {@link #rollbackTransaction}can only be called once per session.
     *
     * @throws HibernateException if the commit fails
     */
    public void commitTransaction() throws HibernateException {
        connectionManager.commitTransaction();
    }

    /**
     * Roll the transaction for the current session back. This method or
     * {@link #commitTransaction}can only be called once per session.
     *
     * @throws HibernateException if the commit fails
     */
    public void rollbackTransaction() throws HibernateException {
        connectionManager.rollbackTransaction();
    }

    /**
     * Is transaction pending for thread?
     * @return boolean
     */
    public boolean inTransaction() {
        return connectionManager.isTransactionPending();
    }

    /**
     * Closes the Hibernate Session stored in ThreadLocal storage.
     */
    public void closeSession() {
        connectionManager.closeSession();
    }

    /**
     * utility to convert blob to byte array
     * @param fromBlob blob to convert
     * @return byte array converted from blob
     */
    public byte[] blobToByteArray(Blob fromBlob) {

        if (fromBlob == null) {
            return new byte[0];
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); InputStream is = fromBlob.getBinaryStream()) {
            IOUtils.copy(is, baos, 4000);
            return baos.toByteArray();
        }
        catch (SQLException e) {
            LOG.error("SQL Error converting blob to byte array", e);
            throw new DatabaseException(e.toString(), e);
        }
        catch (IOException e) {
            LOG.error("I/O Error converting blob to byte array", e);
            throw new DatabaseException(e.toString(), e);
        }
    }

    /**
     * Get the String version of the byte array contents
     * used to return the string representation of byte arrays constructed from blobs
     * @param barr byte array to convert to String
     * @return String version of the byte array contents
     */
    public String getByteArrayContents(byte[] barr) {

        String retval = "";

        if (barr != null) {
            retval = new String(barr, StandardCharsets.UTF_8);
        }
        return retval;
    }

    /**
     * Get the String version of an object corresponding to a BLOB column
     * Handles both the byte[] and the Blob cases
     * @param blob the blob to handle
     * @return String version of the blob contents, null if the blob was null
     * or if the specified object is not actually a Blob
     */
    public String getBlobContents(Object blob) {
        // Returned by Hibernate, and also returned by mode queries
        // from an Oracle database
        if (blob instanceof byte[] byt) {
            return getByteArrayContents(byt);
        }
        // Returned only by mode queries from a Postgres database
        if (blob instanceof Blob blb) {
            return getByteArrayContents(blobToByteArray(blb));
        }
        return null;
    }

    /**
     * Convert a byte[] array to a Blob object.  Guards against
     * null arrays and 0 length arrays.
     * @param data array to convert to a Blob
     * @return Blob if data[] is non-null and {@literal length > 0}, null otherwise
     */
    public Blob byteArrayToBlob(byte[] data) {
        if (data == null) {
            return null;
        }
        if (data.length == 0) {
            return null;
        }

        return Hibernate.getLobHelper().createBlob(data);
    }

    /**
     * Convert a String to a byte[] object.  Guards against
     * null arrays and 0 length arrays.
     * @param data string to convert to a Blob
     * @return Blob if data[] is non-null and {@literal length > 0}, null otherwise
     */
    public byte[] stringToByteArray(String data) {
        if (StringUtils.isEmpty(data)) {
            return null;
        }

        return data.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Initialize the underlying db layer
     *
     */
    public void initialize() {
        connectionManager.initialize();
    }

    /**
     * Disables Hibernate's automatic flushing, runs <code>body</code>, and then
     * enables it again. Returns the result from <code>body</code>.
     *
     * You might want this in order to improve performance by skipping automatic
     * flushes, which might be costly if the number of objects in the Hibernate
     * cache is high. As of hibernate 5.1 the algorithm is quadratic in the
     * number of objects.
     *
     * WARNING: this might result in queries returning stale data -
     * modifications to Hibernate objects before or in this call will not be
     * seen by queries called from <code>body</code>!
     *
     * Only use with code that does not make assumptions about Hibernate cache
     * modifications being reflected in the database. Also it is recommended to
     * make sure via profiling that your method is spending too much CPU time in
     * automatic flushing before attempting to use this method.
     *
     * @param body code to run in FlushModeType.COMMIT
     * @param <T> return type
     * @return the value of supplier
     */
    public <T> T doWithoutAutoFlushing(Supplier<T> body) {
        return doWithoutAutoFlushing(body, true);
    }

    /**
     * Disables Hibernate's automatic flushing, runs <code>body</code>, and then
     * enables it again. Returns the result from <code>body</code>.
     *
     * You might want this in order to improve performance by skipping automatic
     * flushes, which might be costly if the number of objects in the Hibernate
     * cache is high. As of hibernate 5.1 the algorithm is quadratic in the
     * number of objects.
     *
     * WARNING: this might result in queries returning stale data -
     * modifications to Hibernate objects before or in this call will not be
     * seen by queries called from <code>body</code>!
     *
     * Only use with code that does not make assumptions about Hibernate cache
     * modifications being reflected in the database. Also it is recommended to
     * make sure via profiling that your method is spending too much CPU time in
     * automatic flushing before attempting to use this method.
     *
     * Optionally do not open a session if one does not exist.
     *
     * @param <T> return type
     * @param body code to run in FlushModeType.COMMIT
     * @param createSession whether to create a session if one does not exist
     * @return the value of supplier
     */
    public <T> T doWithoutAutoFlushing(Supplier<T> body, boolean createSession) {
        Optional<Session> session = getSessionIfPresent();
        if (!session.isPresent() && !createSession) {
            return body.get();
        }

        FlushModeType old = getSession().getFlushMode();
        getSession().setFlushMode(FlushModeType.COMMIT);
        try {
            return body.get();
        }
        finally {
            getSession().setFlushMode(old);
        }
    }

    /**
     * Disables Hibernate's automatic flushing, runs <code>body</code>, and then
     * enables it again.
     *
     * You might want this in order to improve performance by skipping automatic
     * flushes, which might be costly if the number of objects in the Hibernate
     * cache is high. As of Hibernate 5.1 the algorithm is quadratic in the
     * number of objects.
     *
     * WARNING: this might result in queries returning stale data -
     * modifications to Hibernate objects before or in this call will not be
     * seen by queries called from <code>body</code>!
     *
     * Only use with code that does not make assumptions about Hibernate cache
     * modifications being reflected in the database. Also it is recommended to
     * make sure via profiling that your method is spending too much CPU time in
     * automatic flushing before attempting to use this method.
     *
     * @param body code to run in FlushModeType.COMMIT
     */
    public void doWithoutAutoFlushing(Runnable body) {
        doWithoutAutoFlushing(body, true);
    }

    /**
     * Disables Hibernate's automatic flushing, runs <code>body</code>, and then
     * enables it again.
     *
     * You might want this in order to improve performance by skipping automatic
     * flushes, which might be costly if the number of objects in the Hibernate
     * cache is high. As of Hibernate 5.1 the algorithm is quadratic in the
     * number of objects.
     *
     * WARNING: this might result in queries returning stale data -
     * modifications to Hibernate objects before or in this call will not be
     * seen by queries called from <code>body</code>!
     *
     * Only use with code that does not make assumptions about Hibernate cache
     * modifications being reflected in the database. Also it is recommended to
     * make sure via profiling that your method is spending too much CPU time in
     * automatic flushing before attempting to use this method.
     *
     * Optionally do not open a session if one does not exist.
     *
     * @param body code to run in FlushModeType.COMMIT
     * @param createSession whether to create a session if one does not exist
     */
    public void doWithoutAutoFlushing(Runnable body, boolean createSession) {
        doWithoutAutoFlushing(() -> {
            body.run();
            return 0;
        }, createSession);
    }

    /**
     * Returns the current initialization status
     * @return boolean current status
     */
    public boolean isInitialized() {
        return connectionManager.isInitialized();
    }

    protected <T> DataResult<T> executeSelectMode(String name, String mode, Map<String, ?> params) {
        SelectMode m = ModeFactory.getMode(name, mode);
        return m.execute(params);
    }

    protected void executeCallableMode(String name, String mode, Map<String, Object> params) {
        CallableMode m = ModeFactory.getCallableMode(name, mode);
        m.execute(params, new HashMap<>());
    }

    /**
     * Executes a 'lookup' query to retrieve data from the database given a list of ids.
     * The query will be execute in batches of LIST_BATCH_MAX_SIZE ids each.
     * @param <T> the type of the returned objects
     * @param <ID>
     * @param ids the ids to search for
     * @param queryName the name of the query to be executed
     * @param idsParameterName the name of the parameter to match the ids
     * @return a list of the objects found
     */
    protected <T, ID> List<T> findByIds(List<ID> ids, String queryName, String idsParameterName) {
        return findByIds(ids, queryName, idsParameterName, new HashMap<>());
    }

    /**
     * Executes an 'update' query to the database given a list of parameters.
     * The query will be executed in batches of LIST_BATCH_MAX_SIZE parameters each.
     * @param <E> the type of the list parameters
     * @param list the list of parameters to search for
     * @param queryName the name of the query to be executed
     * @param parameterName the name of the parameter to match the parameters in the list
     * @return the count of affected rows
     */
    @SuppressWarnings("unchecked")
    protected <E> int udpateByIds(List<E> list, String queryName, String parameterName,
            Map<String, Object> parameters) {
        Query<Integer> query = getSession().getNamedQuery(queryName);

        parameters.entrySet().stream().forEach(entry -> query.setParameter(entry.getKey(), entry.getValue()));

        return splitAndExecuteQuery(list, parameterName, query, query::executeUpdate, 0, Integer::sum);
    }

    /**
     * Executes a 'lookup' query to retrieve data from the database given a list of ids.
     * The query will be execute in batches of LIST_BATCH_MAX_SIZE ids each.
     * @param <T> the type of the returned objects
     * @param <ID> the type of the ids
     * @param ids the ids to search for
     * @param queryName the name of the query to be executed
     * @param idsParameterName the name of the parameter to match the ids
     * @param parameters extra parameters to include in the query
     * @return a list of the objects found
     */
    @SuppressWarnings("unchecked")
    protected <T, ID> List<T> findByIds(List<ID> ids, String queryName,
            String idsParameterName, Map<String, Object> parameters) {
        Query<T> query = getSession().getNamedQuery(queryName);

        parameters.entrySet().stream().forEach(entry -> query.setParameter(entry.getKey(), entry.getValue()));

        return splitAndExecuteQuery(ids, idsParameterName, query, query::getResultList,
                new ArrayList<T>(), ListUtils::union);
    }

    /**
     * Splits a list of elements in batches of LIST_BATCH_MAX_SIZE and execute a query for each batch.
     * Results from each query are reduced via `accumulator` using the provided `identity`.
     * @param <T> the return type
     * @param <E> the type of the elements in the list parameter
     * @param <R> the type of the returned objects by the query
     * @param list the list of parameters to search for
     * @param parameterName the name of the parameter to match the parameters in the list
     * @param query the query to be executed
     * @param queryFunction the function to be call on the query
     * @param identity the identity for the accumulator function
     * @param accumulator the operation for the result accumulator
     * @return an accumulated result of executing the query
     */
    private <E, T, R> T splitAndExecuteQuery(List<E> list, String parameterName,
            Query<R> query, Supplier<T> queryFunction, T identity, BinaryOperator<T> accumulator) {
        int size = list.size();

        List<List<E>> batches = IntStream.iterate(0, i -> i < size, i -> i + LIST_BATCH_MAX_SIZE)
                .mapToObj(i -> list.subList(i, Math.min(i + LIST_BATCH_MAX_SIZE, size)))
                .toList();
        return batches.stream()
                .map(b -> {
                    query.setParameterList(parameterName, b);
                    return queryFunction.get();
                })
                .reduce(identity, accumulator::apply);
    }

}
