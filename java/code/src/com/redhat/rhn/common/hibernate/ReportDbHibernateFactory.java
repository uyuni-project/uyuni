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
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.persistence.FlushModeType;

/**
 * HibernateFactory - Helper superclass that contains methods for fetching and
 * storing Objects from the DB using Hibernate.
 * <p>
 * Abstract methods define what the subclass must implement to determine what is
 * specific to that Factory's instance.
 */
public class ReportDbHibernateFactory {

    private final ConnectionManager connectionManager;
    private static final Logger LOG = Logger.getLogger(ReportDbHibernateFactory.class);
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
     * Create a SessionFactory, loading the hbm.xml files from the default
     * location (com.redhat.rhn.domain).
     */
    public void createSessionFactory() {
        connectionManager.initialize();
    }

    /**
     * Create a SessionFactory, loading the hbm.xml files from alternate
     * location
     * @param additionalLocation Alternate location for hbm.xml files
     */
    public void createSessionFactory(String[] additionalLocation) {
        connectionManager.setAdditionalPackageNames(additionalLocation);
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
     * Binds the values of the map to a named query parameter, whose value
     * matches the key in the given Map, guessing the Hibernate type from the
     * class of the given object.
     * @param query Query to be modified.
     * @param parameters named query parameters to be bound.
     * @return Modified Query.
     * @throws HibernateException if there is a problem with updating the Query.
     * @throws ClassCastException if the key in the given Map is NOT a String.
     */
    /*
    private Query bindParameters(Query query, Map parameters)
        throws HibernateException {
        if (parameters == null) {
            return query;
        }

        Set entrySet = parameters.entrySet();
        for (Iterator itr = entrySet.iterator(); itr.hasNext();) {
            Map.Entry entry = (Map.Entry) itr.next();
            if (entry.getValue() instanceof Collection) {
                Collection c = (Collection) entry.getValue();
                if (c.size() > 1000) {
                    LOG.error("Query executed with Collection larger than 1000");
                }
                query.setParameterList((String) entry.getKey(), c);
            }
            else {
                query.setParameter((String) entry.getKey(), entry.getValue());
            }
        }

        return query;
    }*/

    /**
     * Finds a single instance of a persistent object given a named query.
     * @param qryName The name of the query used to find the persistent object.
     * It should be formulated to ensure a single object is returned or an error
     * will occur.
     * @param qryParams Map of named bind parameters whose keys are Strings. The
     * map can also be null.
     * @return Object found by named query or null if nothing found.
     */
    /*
    protected Object lookupObjectByNamedQuery(String qryName, Map qryParams) {
        return lookupObjectByNamedQuery(qryName, qryParams, false);
    }*/

    /**
     * Finds a single instance of a persistent object given a named query.
     * @param qryName The name of the query used to find the persistent object.
     * It should be formulated to ensure a single object is returned or an error
     * will occur.
     * @param qryParams Map of named bind parameters whose keys are Strings. The
     * map can also be null.
     * @param cacheable if we should cache the results of this object
     * @return Object found by named query or null if nothing found.
     */
    /*
    protected Object lookupObjectByNamedQuery(String qryName, Map qryParams,
            boolean cacheable) {
        Object retval = null;
        Session session = null;

        try {
            session = getSession();

            Query query = session.getNamedQuery(qryName)
                    .setCacheable(cacheable);
            bindParameters(query, qryParams);
            retval = query.uniqueResult();
        }
        catch (MappingException me) {
            throw new HibernateRuntimeException("Mapping not found for " + qryName, me);
        }
        catch (HibernateException he) {
            throw new HibernateRuntimeException("Executing query " + qryName +
                    " with params " + qryParams + " failed", he);
        }

        return retval;
    }*/

    /**
     * Using a named query, find all the objects matching the criteria within.
     * Warning: This can be very expensive if the returned list is large. Use
     * only for small tables with static data
     * @param qryName Named query to use to find a list of objects.
     * @param qryParams Map of named bind parameters whose keys are Strings. The
     * map can also be null.
     * @return List of objects returned by named query, or null if nothing
     * found.
     */
    /*
    protected List listObjectsByNamedQuery(String qryName, Map qryParams) {
        return listObjectsByNamedQuery(qryName, qryParams, false);
    }*/

    /**
     * Using a named query, find all the objects matching the criteria within.
     * Warning: This can be very expensive if the returned list is large. Use
     * only for small tables with static data
     * @param qryName Named query to use to find a list of objects.
     * @param qryParams Map of named bind parameters whose keys are Strings. The
     * map can also be null.
     * @param col the collection to use as an inclause
     * @param colLabel the label the collection will have
     * @return List of objects returned by named query, or null if nothing
     * found.
     */
    /*
    protected List listObjectsByNamedQuery(String qryName, Map qryParams,
                                        Collection col, String colLabel) {

        if (col.isEmpty()) {
            return Collections.EMPTY_LIST;
        }

        ArrayList<Long> tmpList = new ArrayList<Long>();
        List<Long> toRet = new ArrayList<Long>();
        tmpList.addAll(col);

        for (int i = 0; i < col.size();) {
            int initial = i;
            int fin = i + 500 < col.size() ? i + 500 : col.size();
            List<Long> sublist = tmpList.subList(i, fin);

            qryParams.put(colLabel, sublist);
            toRet.addAll(listObjectsByNamedQuery(qryName, qryParams, false));
            i = fin;
        }
        return toRet;
    }*/



    /**
     * Using a named query, find all the objects matching the criteria within.
     * Warning: This can be very expensive if the returned list is large. Use
     * only for small tables with static data
     * @param qryName Named query to use to find a list of objects.
     * @param qryParams Map of named bind parameters whose keys are Strings. The
     * map can also be null.
     * @param cacheable if we should cache the results of this query
     * @return List of objects returned by named query, or null if nothing
     * found.
     */
    /*
    protected List listObjectsByNamedQuery(String qryName, Map qryParams,
            boolean cacheable) {
        Session session = null;
        List retval = null;
        session = getSession();
        Query query = session.getNamedQuery(qryName);
        query.setCacheable(cacheable);
        bindParameters(query, qryParams);
        retval = query.list();
        return retval;
    }*/

    /**
     * Saves the given object to the database using Hibernate.
     * @param toSave Object to be persisted.
     * @param saveOrUpdate true if saveOrUpdate should be called, false if
     * save() is to be called directly.
     */
    /*
    protected void saveObject(Object toSave, boolean saveOrUpdate) {
        Session session = null;
        session = getSession();
        if (saveOrUpdate) {
            session.saveOrUpdate(toSave);
        }
        else {
            session.save(toSave);
        }
    }*/

    /**
     * Saves the given object to the database using Hibernate.
     * @param toSave Object to be persisted.
     */
    /*
    protected void saveObject(Object toSave) {
        saveObject(toSave, true);
    }*/

    /**
     * Remove a Session from the DB
     * @param toRemove Object to be removed.
     * @return int number of objects affected.
     */
    /*
    protected int removeObject(Object toRemove) {
        Session session = null;
        int numDeleted = 0;
        session = getSession();

        session.delete(toRemove);
        numDeleted++;

        return numDeleted;
    }*/

    /**
     * Deletes rows corresponding to multiple objects (as in DELETE FROM... IN ...).
     *
     * @param objects the objects to delete
     * @param clazz class of the objects to delete
     * @param <T> type of the objects to delete
     * @return the number of deleted objects
     */
    /*
    public <T> int delete(Collection<T> objects, Class<T> clazz) {
        // both T and clazz are needed because type erasure
        if (objects.isEmpty()) {
            return 0;
        }
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaDelete<T> delete = builder.createCriteriaDelete(clazz);
        Root<T> root = delete.from(clazz);
        delete.where(root.in(objects));
        return getSession().createQuery(delete).executeUpdate();
    }*/

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
     * Return the persistent instance of the given entity class with the given
     * identifier, or null if there is no such persistent instance. (If the
     * instance, or a proxy for the instance, is already associated with the
     * session, return that instance or proxy.)
     * @param clazz a persistent class
     * @param id an identifier
     * @return Object persistent instance or null
     */
    /*
    public Object getObject(Class clazz, Serializable id) {
        Object retval = null;
        Session session = null;

        try {
            session = getSession();

            retval = session.get(clazz, id);
        }
        catch (MappingException me) {
            getLogger().error("Mapping not found for " + clazz.getName(), me);

        }
        catch (HibernateException he) {
            getLogger().error("Hibernate exception: " + he.toString());
        }

        return retval;
    }*/

    /**
     * Return a locked persistent instance of the given entity class with
     * the given identifier, or null if there is no such persistent instance.
     * (If the instance, or a proxy for the instance, is already associated
     * with the session, return that instance or proxy.)
     * @param clazz a persistent class
     * @param id an identifier
     * @return Object persistent instance or null
     */
    /*
    protected Object lockObject(Class clazz, Serializable id) {
        Object retval = null;
        Session session = null;

        try {
            session = getSession();

            retval = session.get(clazz, id, LockMode.UPGRADE);
        }
        catch (MappingException me) {
            getLogger().error("Mapping not found for " + clazz.getName(), me);

        }
        catch (HibernateException he) {
            getLogger().error("Hibernate exception: " + he.toString());
        }

        return retval;
    }*/

    /**
     * Util to reload an object using Hibernate
     * @param obj to be reloaded
     * @return Object found if not, null
     * @throws HibernateException if something bad happens.
     * @param <T> the entity type
     */
    /*
    public <T> T reload(T obj) throws HibernateException {
        // assertNotNull(obj);
        ClassMetadata cmd = connectionManager.getMetadata(obj);
        Serializable id = cmd.getIdentifier(obj, (SessionImplementor) getSession());
        Session session = getSession();
        session.flush();
        session.evict(obj);
        *
         * In hibernate 3, the following doesn't work:
         * session.load(obj.getClass(), id);
         * load returns the proxy class instead of the persisted class, ie,
         * Filter$$EnhancerByCGLIB$$9bcc734d_2 instead of Filter.
         * session.get is set to not return the proxy class, so that is what we'll use.
         *
        // assertNotSame(obj, result);
        return (T) session.get(obj.getClass(), id);
    }*/

    /**
     * utility to convert blob to byte array
     * @param fromBlob blob to convert
     * @return byte array converted from blob
     */
    public byte[] blobToByteArray(Blob fromBlob) {

        if (fromBlob == null) {
            return new byte[0];
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            return toByteArrayImpl(fromBlob, baos);
        }
        catch (SQLException e) {
            LOG.error("SQL Error converting blob to byte array", e);
            throw new DatabaseException(e.toString());
        }
        catch (IOException e) {
            LOG.error("I/O Error converting blob to byte array", e);
            throw new DatabaseException(e.toString());
        }
        finally {
            try {
                baos.close();
            }
            catch (IOException ex) {
                throw new DatabaseException(ex.toString());
            }
        }
    }

    /**
     * helper utility to convert blob to byte array
     * @param fromBlob blob to convert
     * @param baos byte array output stream
     * @return String version of the byte array contents
     */
    private byte[] toByteArrayImpl(Blob fromBlob, ByteArrayOutputStream baos)
        throws SQLException, IOException {

        byte[] buf = new byte[4000];
        InputStream is = fromBlob.getBinaryStream();
        try {
            for (;;) {
                int dataSize = is.read(buf);
                if (dataSize == -1) {
                    break;
                }
                baos.write(buf, 0, dataSize);
            }
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                }
                catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        return baos.toByteArray();
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
            try {
                retval = new String(barr, "UTF-8");
            }
            catch (UnsupportedEncodingException uee) {
                throw new RuntimeException("Illegal Argument: " +
              "This VM or environment doesn't support UTF-8: Data - " +
                                                 barr, uee);
            }
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
        if (blob instanceof byte[]) {
            return getByteArrayContents((byte[]) blob);
        }
        // Returned only by mode queries from a Postgres database
        if (blob instanceof Blob) {
            return getByteArrayContents(blobToByteArray((Blob) blob));
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
        return Hibernate.getLobCreator(getSession()).createBlob(data);

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

        try {
            return data.getBytes("UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Illegal Argument: " +
            "This VM or environment doesn't support UTF-8 - Data - " +
                                             data, e);
        }
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

    protected DataResult executeSelectMode(String name, String mode, Map params) {
        SelectMode m = ModeFactory.getMode(name, mode);
        return m.execute(params);
    }

    protected void executeCallableMode(String name, String mode, Map params) {
        CallableMode m = ModeFactory.getCallableMode(name, mode);
        m.execute(params, new HashMap());
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
                .collect(Collectors.toList());
        return batches.stream()
                .map(b -> {
                    query.setParameterList(parameterName, b);
                    return queryFunction.get();
                })
                .reduce(identity, accumulator::apply);
    }

}
