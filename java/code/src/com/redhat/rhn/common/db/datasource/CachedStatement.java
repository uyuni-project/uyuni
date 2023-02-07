/*
 * Copyright (c) 2009--2016 Red Hat, Inc.
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
package com.redhat.rhn.common.db.datasource;

import com.redhat.rhn.common.ObjectCreateWrapperException;
import com.redhat.rhn.common.RhnRuntimeException;
import com.redhat.rhn.common.db.NamedPreparedStatement;
import com.redhat.rhn.common.hibernate.HibernateHelper;
import com.redhat.rhn.common.hibernate.HibernateRuntimeException;
import com.redhat.rhn.common.translation.SqlExceptionTranslator;
import com.redhat.rhn.common.util.MethodUtil;
import com.redhat.rhn.common.util.StringUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.jdbc.ReturningWork;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.FlushModeType;

/**
 * A cached set of query/elaborator strings and the parameterMap hash maps.
 */
public class CachedStatement implements Serializable {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -6256397039512492616L;

    /**
     * Logger for this class
     */
    private static Logger log = LogManager.getLogger(CachedStatement.class);

    /**
     * The size above which queries are split into multiple queries, each of
     * this size.
     */
    public static final int BATCH_SIZE = 500;

    /*
     * This is the original config for this query as specified in the mode query
     * xml file. It is intended to be immutable.
     */
    private ParsedQuery protoQuery;

    /*
     * This is the sql statement that will be executed. This query may get
     * modified up until the time it is executed. Just prior to execution, the
     * only remaining substitution to be made is the optional "in" clause list.
     * This "in" clause list is substituted in the execute() method in a local
     * variable and should never be stored in this variable.
     */
    private String sqlStatement;

    private String name;

    private Map<String, List<Integer>> qMap;

    private List<String> params;

    // This is only set if the current CachedStatement is a duplicate of an
    // existing one with the %s expanded out.
    private CachedStatement parentStatement;
    private RestartData restartData = null;
    private Session session = null;

    // We could (and probably should) cache the ResultSet metadata here as
    // well. There is no reason that the first call to each statement
    // couldn't do the work to determine what is returned.

    /**
     * Create a CachedStatement for a query
     * @param sessionIn hibernate database session.
     * @param parsedQuery This immutable query definition.
     */
    /* package */ CachedStatement(Session sessionIn, ParsedQuery parsedQuery) {
        this.protoQuery = parsedQuery;
        this.name = parsedQuery.getName();
        this.qMap = new HashMap<>();
        this.params = new ArrayList<>(parsedQuery.getParameterList());
        this.sqlStatement = parsedQuery.getSqlStatement();
        this.session = sessionIn;
    }

    /**
     * Create a CachedStatement for a query, this one being an elaborator query.
     * This is only used in executeElaboratorBatch() call below
     * @param sessionIn hibernate database session.
     * @param newName The name for this query.
     * @param parsedQuery This immutable query definition.
     * @param orig The parent query.
     */
    private CachedStatement(Session sessionIn, String newName, ParsedQuery parsedQuery, List<String> paramsIn,
            CachedStatement orig) {
        this(sessionIn, parsedQuery);
        parentStatement = orig;
        this.name = newName;
        this.params = paramsIn;
    }

    /**
     * Returns the arity of the statement
     * @return number of parameters
     */
    public int getArity() {
        return protoQuery.getParameterList() == null ? 0 :
                protoQuery.getParameterList().size();
    }

    /**
     * Get the query's alias
     * @return the querys alias
     */
    public String getAlias() {
        return protoQuery.getAlias();
    }

    /**
     * Get the query's name
     * @return the querys name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the query string
     * @return the query string
     */
    public String getQuery() {
        return sqlStatement;
    }

    /**
     * Get the original query string
     * @return the original query, before the named bind parameters were
     * removed.
     */
    public String getOrigQuery() {
        return protoQuery.getSqlStatement();
    }

    /**
     * Get the column used to relate driving queries and elaborators
     * @return column name.
     */
    public String getColumn() {
        return protoQuery.getElaboratorJoinColumn();
    }

    /**
     * Modify the sql statement by replacing the specified token with the
     * specified list of String values.
     * @param replaceToken The text in the sql statement to be replaced.
     * @param valueList The list of String values to be quoted and concatenated
     * together in a comma-separated list that replaces the replaceToken.
     * @param querySanitizer An optional sanitizer used to check each value.
     */
    public void modifyQuery(String replaceToken, List<String> valueList,
            QuerySanitizer querySanitizer) {
        if (replaceToken == null) {
            throw new IllegalArgumentException(
                    "Bad modify query call - replaceToken required.");
        }
        if (valueList == null || valueList.isEmpty()) {
            throw new IllegalArgumentException("Bad modify query call for token '" +
                    replaceToken +
                    "' - list of replacement values must contain at least one value.");
        }
        StringBuilder sb = new StringBuilder();
        boolean firstValue = true;
        for (String value : valueList) {
            if (!firstValue) {
                sb.append(", ");
            }
            else {
                firstValue = false;
            }
            if (querySanitizer != null && !querySanitizer.isSanitary(value)) {
                throw new IllegalArgumentException(
                        "Attempt to modify query for token '" + replaceToken +
                                "' with value that did not pass sanitary check.  Value: " +
                                value);
            }
            sb.append("'").append(value).append("'");
        }
        this.modifyQuery(replaceToken, sb.toString());
    }

    private void modifyQuery(String replaceToken, String replacementString) {
        sqlStatement = sqlStatement.replace(replaceToken, replacementString);
    }

    int executeUpdate(Map<String, ?> parameters) {
        return executeUpdate(parameters, null);
    }

    int executeUpdate(Map<String, ?> parameters, List<?> inClause) {
        return internalExecuteUpdate(parameters, inClause, null);
    }

    /**
     * Executes multiple updates with one only prepared statement.
     *
     * @param parameterList a list of parameter maps
     * @return a list of affected rows counts
     */
    List<Integer> executeUpdates(List<Map<String, Object>> parameterList) {
        return doWithStolenConnection(connection -> {
            try {
                List<Integer> result = new ArrayList<>(parameterList.size());

                sqlStatement = NamedPreparedStatement.replaceBindParams(sqlStatement, qMap);
                for (Map<String, Object> parameters : parameterList) {
                    result.add((Integer) execute(connection, sqlStatement, qMap, parameters,
                            null, null));
                }
                return result;
            }
            catch (SQLException e) {
                throw SqlExceptionTranslator.sqlException(e);
            }
            catch (HibernateException he) {
                throw new
                    HibernateRuntimeException(
                        "HibernateException executing CachedStatement", he);

            }
            catch (RhnRuntimeException e) {
                log.error("Error while processing cached statement sql: {}", getQuery(), e);
                throw e;
            }
        });
    }


    <T> DataResult<T> execute(Map<String, ?> parameters, Mode mode) {
        return internalExecute(parameters, null, mode);
    }

    <T> DataResult<T> execute(List<?> parameters, Mode mode) {
        return internalExecute(null, parameters, mode);
    }

    <T> DataResult<T> execute(Map<String, ?> parameters, List<?> inClause,
            Mode mode) {
        return internalExecute(parameters, inClause, mode);
    }

    private Integer internalExecuteUpdateNoSubClause(Map<String, ?> parameters, Mode mode) {
        Object resultObj = executeChecking(sqlStatement, qMap, parameters, mode, null);
        if (resultObj instanceof Integer) {
            return (Integer) resultObj;
        }
        return 0;
    }

    private <T> DataResult<T> internalExecuteNoSubClause(Map<String, ?> parameters, Mode mode) {
        storeForRestart(parameters, null, mode);
        this.sqlStatement = NamedPreparedStatement.replaceBindParams(sqlStatement, qMap);
        Object resultObj = executeChecking(sqlStatement, qMap, parameters, mode, null);
        Class<DataResult<T>> drClazz = (Class<DataResult<T>>)(Class<?>) DataResult.class;
        if (drClazz.isAssignableFrom(resultObj.getClass())) {
            return drClazz.cast(executeChecking(sqlStatement, qMap, parameters, mode, null));
        }
        return new DataResult<>(mode);
    }

    private int internalExecuteUpdate(Map<String, ?> parameters, List<?> inClause, Mode mode) {
        storeForRestart(parameters, inClause, mode);
        this.sqlStatement = NamedPreparedStatement.replaceBindParams(sqlStatement, qMap);

        if (sqlStatement.contains("%s")) {
            int returnInt = 0;

            int subStart = 0;
            while (subStart < inClause.size()) {
                int subLength = subStart + BATCH_SIZE >= inClause.size() ? inClause.size() - subStart : BATCH_SIZE;

                List<?> subClause = inClause.subList(subStart, subStart + subLength);
                String finalQuery = sqlStatement.replace("%s", commaSeparatedList(subClause));
                Object resultObj = executeChecking(finalQuery, qMap, parameters, mode, null);
                subStart += subLength;

                if (resultObj instanceof Integer) {
                    returnInt = returnInt + (Integer) resultObj;
                }
            }
            return returnInt;
        }
        else {
            return internalExecuteUpdateNoSubClause(parameters, mode);
        }
    }

    private List<String> queryBatches(List<?> inClause) {
        List<String> queries = new ArrayList<>();

        int subStart = 0;
        while (subStart < inClause.size()) {
            int subLength = subStart + BATCH_SIZE >= inClause.size() ?
                    inClause.size() - subStart : BATCH_SIZE;

            List<?> subClause = inClause.subList(subStart, subStart + subLength);
            queries.add(sqlStatement.replace("%s", commaSeparatedList(subClause)));
            subStart += subLength;
        }
        return queries;
    }

    @SuppressWarnings("unchecked")
    private <T> DataResult<T> internalExecute(Map<String, ?> parameters, List<?> inClause, Mode mode) {

        storeForRestart(parameters, inClause, mode);
        this.sqlStatement = NamedPreparedStatement.replaceBindParams(sqlStatement, qMap);

        if (sqlStatement.contains("%s")) {
            if (inClause == null || inClause.isEmpty()) {
                return new DataResult<>(mode);
            }
            Class<DataResult<T>> drClazz = (Class<DataResult<T>>)(Class<?>) DataResult.class;

            List<String> batches = queryBatches(inClause);
            List<DataResult<T>> results = batches.stream()
                    .map(finalQuery -> executeChecking(finalQuery, qMap, parameters, mode, null))
                    .filter(res -> drClazz.isAssignableFrom(res.getClass()))
                    .map(res -> drClazz.cast(res))
                    .collect(Collectors.toList());
            DataResult<T> result = null;
            if (!results.isEmpty()) {
                result = results.get(0);
                for (DataResult<T> subResult : results.subList(1, results.size())) {
                    result.addDataResult(subResult);
                }
            }
            return result;
        }
        else {
            return internalExecuteNoSubClause(parameters, mode);
        }
    }

    private String commaSeparatedList(List<?> list) {
        StringBuilder sb = new StringBuilder();
        boolean firstValue = true;
        for (Object value : list) {
            if (!firstValue) {
                sb.append(",");
            }
            else {
                firstValue = false;
            }
            if (value instanceof String) {
                sb.append("'").append((String) value).append("'");
            }
            else {
                sb.append(value);
            }
        }
        return sb.toString();
    }

    Collection<Object> executeElaborator(List<Object> resultList, Mode mode,
            Map<String, ?> parametersIn) {
        List<Object> elaborated = new LinkedList<>();
        for (int batch = 0; batch < resultList.size(); batch = batch + BATCH_SIZE) {
            int toIndex = batch + BATCH_SIZE;
            if (toIndex > resultList.size()) {
                toIndex = resultList.size();
            }
            elaborated.addAll(executeElaboratorBatch(resultList.subList(batch, toIndex),
                    mode, parametersIn));
        }
        return elaborated;
    }

    @SuppressWarnings("unchecked")
    private Collection<Object> executeElaboratorBatch(List<Object> resultList, Mode mode,
        Map<String, ?> parametersIn) {

        this.sqlStatement = NamedPreparedStatement.replaceBindParams(sqlStatement, qMap);

        int len = resultList.size();

        Map<String, Object> parameters =
                parametersIn != null ? new HashMap<>(parametersIn) : new HashMap<>();

        if (len == 0) {
            // Nothing to elaborate, just return;
            return resultList;
        }

        // If we aren't actually operating on a list, just elaborate.
        if (!sqlStatement.contains("%s")) {
            return (DataResult<Object>) executeChecking(sqlStatement, qMap, parameters,
                    mode, resultList);
        }

        if (!checkForColumn(resultList.get(0), getColumn())) {
            throw new MapColumnNotFoundException(
                    "Column, " + getColumn() + ", not found in driving query results");
        }
        StringBuilder bindParams = new StringBuilder();
        List<String> newParams = new ArrayList<>(params);
        for (int i = 0; i < len; i++) {
            if (i > 0) { // don't prepend comma before first one
                bindParams.append(", ");
            }
            String newParam = "l" + i;
            bindParams.append(":").append(newParam);
            parameters.put(newParam, getKey(resultList.get(i), getColumn()));
            newParams.add(newParam);
        }

        // This should all be removed and replaced with a copy constructor.
        String newName = "";
        if (!getName().equals("")) {
            newName = getName() + len;
        }
        CachedStatement cs = new CachedStatement(session, newName, protoQuery, newParams, this);
        cs.modifyQuery("%s", bindParams.toString());
        return cs.executeElaboratorBatch(resultList, mode, parameters);
    }

    private Map<String, ?> setupParamMap(Map<String, ?> parameters) {
        if (parameters == null && !params.isEmpty()) {
            throw new IllegalArgumentException(
                    "Query contains named parameter," + " but value map is null");
        }
        // Only pass the parameters from the original query.
        Map<String, Object> intersection = new HashMap<>();
        for (String curr : params) {
            if (!parameters.containsKey(curr)) {
                throw new ParameterValueNotFoundException(
                        "Parameter '" + curr + "' not given for query: " + sqlStatement);
            }
            intersection.put(curr, parameters.get(curr));
        }
        return intersection;
    }

    /**
     * Prepares and executes a SQL statements with parameters, handling
     * Exceptions appropriately.
     * @param sql SQL string to be prepared and executed
     * @param parameterMap The Map returned setup by replaceBindParams
     * @param parameters The Map returned setup by replaceBindParams
     * @param mode Mode for selection queries
     * @param dr Data result list or null
     * @return either an integer count of the number of rows updated, or the
     *         DataResult. Casting to int or DataResult is caller's
     *         responsibility
     */
    private Object executeChecking(String sql, Map<String, List<Integer>> parameterMap,
            Map<String, ?> parameters, Mode mode, List<Object> dr) {
        return doWithStolenConnection(connection -> {
            try {
                return execute(connection, sql, parameterMap, parameters, mode, dr);
            }
            catch (SQLException e) {
                throw SqlExceptionTranslator.sqlException(e);
            }
            catch (HibernateException he) {
                throw new HibernateRuntimeException(
                        "HibernateException executing CachedStatement", he);

            }
            catch (RhnRuntimeException e) {
                // we just add more information for better bug tracking
                log.error("Error while processing cached statement sql: {}", sql, e);
                throw e;
            }
        });
    }

    /**
     * Executes a prepared SQL statement with parameters.
     * @param connection a JDBC Connection object
     * @param sql SQL string to be prepared and executed
     * @param parameterMap The Map returned setup by replaceBindParams
     * @param parameters The Map returned setup by replaceBindParams
     * @param mode Mode for selection queries
     * @param dr Data result list or null
     * @return either an integer count of the number of rows updated, or the
     *         DataResult. Casting to int or DataResult is caller's
     *         responsibility
     * @throws SQLException
     */
    private Object execute(Connection connection, String sql,
            Map<String, List<Integer>> parameterMap, Map<String, ?> parameters, Mode mode,
            List<Object> dr)
        throws SQLException {
        if (log.isDebugEnabled()) {
            log.debug("execute() - Executing: {}", sql);
            log.debug("execute() - With: {}", parameters);
        }

        PreparedStatement ps = null;
        try {
            ps = prepareStatement(connection, sql, mode);
            boolean returnType = NamedPreparedStatement.execute(ps, parameterMap,
                    setupParamMap(parameters));
            if (log.isDebugEnabled()) {
                log.debug("execute() - Return type: {}", returnType);
            }
            if (returnType) {
                return processResultSet(ps.getResultSet(), (SelectMode) mode, dr);
            }
            return ps.getUpdateCount();
        }
        finally {
            HibernateHelper.cleanupDB(ps);
        }
    }

    /**
     * Executes a prepared SQL statement with parameters.
     * @param connection JDBC connection object in which create the statement
     * @param sql SQL string to be prepared and executed
     * @param mode Mode for selection queries
     * @return the prepared statement
     * @throws SQLException
     */
    private PreparedStatement prepareStatement(Connection connection, String sql, Mode mode)
        throws SQLException, HibernateException {
        PreparedStatement ps = connection.prepareStatement(sql);

        // allow limiting the results for better performance.
        if (mode != null && mode instanceof SelectMode) {
            ps.setMaxRows(((SelectMode) mode).getMaxRows());
        }
        return ps;
    }

    private Map<String, Object> processOutputParams(CallableStatement cs,
            Map<String, Integer> outParams)
        throws SQLException {

        Iterator<String> i = outParams.keySet().iterator();
        Map<String, Object> result = new HashMap<>();
        while (i.hasNext()) {
            String param = i.next();
            Iterator<Integer> positions = NamedPreparedStatement.getPositions(param, qMap);
            // For now assume that we only specify each output parameter once,
            // to do otherwise just doesn't make a lot of sense.
            Integer pos = positions.next();
            Object o = cs.getObject(pos);
            if (o instanceof BigDecimal) {
                o = ((BigDecimal) o).longValue();
            }
            result.put(param, o);
        }
        return result;
    }

    Map<String, Object> executeCallable(Map<String, Object> inParams,
            Map<String, Integer> outParams) {

        return doWithStolenConnection(connection -> {
            this.sqlStatement =
                    NamedPreparedStatement.replaceBindParams(sqlStatement, qMap);
            CallableStatement cs = null;
            try {
                cs = connection.prepareCall(this.sqlStatement);
                NamedPreparedStatement.execute(cs, qMap, inParams, outParams);
                return processOutputParams(cs, outParams);
            }
            catch (SQLException e) {
                throw SqlExceptionTranslator.sqlException(e);
            }
            catch (HibernateException he) {
                throw new HibernateRuntimeException(
                        "HibernateException executing CachedStatement", he);
            }
            catch (RuntimeException e) {
                if (e.getCause() instanceof SQLException) {
                    throw SqlExceptionTranslator.sqlException((SQLException) e.getCause());
                }
                throw e;
            }
            finally {
                HibernateHelper.cleanupDB(cs);
            }
        });
    }

    private DataResult<Object> processResultSet(ResultSet rs, SelectMode mode,
            List<Object> currentResults) {

        Map<Object, Integer> pointers = null;
        DataResult<Object> dr;
        if (currentResults != null) {
            pointers = generatePointers(currentResults, getColumn());
            dr = new DataResult<>(currentResults);
        }
        else {
            dr = new DataResult<>(mode);
        }
        String className = mode.getClassString();
        try {
            // Get the column names from the result set.
            List<String> columns = getColumnNames(rs.getMetaData());
            if (currentResults != null && !columns.contains(getColumn().toLowerCase())) {
                // This is ugly, but we check driving query results someplace
                // else, so this is only executed if we are elaborating.
                throw new MapColumnNotFoundException(
                        "Column, " + getColumn() + ", not found in elaborator results");
            }

            // loop through the results, adding them to the displayMap
            while (rs.next()) {
                /*
                 * If no className was specified *or* if the caller wants a Map
                 */
                if (className == null || className.equals("java.util.Map")) {
                    Row resultMap;
                    if (pointers == null) {
                        resultMap = new Row();
                    }
                    else {
                        Integer pos = pointers.get(getObject(rs, getColumn()));
                        /*
                         * TODO: there is a possible bug here. If the elaborator
                         * does not restrict itself to only the current results
                         * (%s thing), then the pos here is null, because the
                         * object might not exist in the map. Decide if this is
                         * a bug here or a bug with the query that allows such
                         * effect. Decide what to do about it.
                         */
                        resultMap = (Row) currentResults.get(pos);
                    }
                    addToMap(columns, rs, resultMap,
                            mode.getElaborators().indexOf(parentStatement));

                    // bug 141664: Don't add to the DataResult if we are
                    // elaborating the data.
                    if (pointers == null) {
                        dr.add(resultMap);
                    }
                }
                /*
                 * Otherwise, try to set the results to the class given.
                 */
                else {
                    Class<?> clazz = Class.forName(className);
                    Object obj;
                    if (pointers == null) {
                        obj = clazz.newInstance();
                    }
                    else {
                        Integer pos = pointers.get(getObject(rs, getColumn()));
                        if (pos == null) {
                            // possible mismatch on elaborator ids
                            throw new IllegalArgumentException("Null elab match for " +
                                    getColumn() + " " + getObject(rs, getColumn()));
                        }
                        obj = currentResults.get(pos);
                    }
                    // if pointers are null, we are doing an elaborator.
                    addToObject(columns, rs, obj, (pointers != null));
                    // bug 141664: Don't add to the DataResult if we are
                    // elaborating the data.
                    if (pointers == null) {
                        dr.add(obj);
                    }
                }
            }
            // TODO: this is the only place that we care that we are
            // returning a DataResult object rather than simply a List.
            // Furthermore, this is entirely because of paging in the
            // user interface which should clearly not be done in the
            // bowels of CachedStatement inside datasource.
            // Remove this pointless coupling once we move the paging
            // logic elsewhere.
            if (!dr.isEmpty()) {
                dr.setStart(1);
                dr.setEnd(dr.size());
                dr.setTotalSize(dr.size());
            }
            return dr;
        }
        catch (SQLException e) {
            throw SqlExceptionTranslator.sqlException(e);
        }
        catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new ObjectCreateWrapperException("Could not create " + className, e);
        }
        finally {
            HibernateHelper.cleanupDB(rs);
        }
    }

    @SuppressWarnings("unchecked")
    private void addToMap(List<String> columns, ResultSet rs, Map<String, Object> resultMap,
            int pos)
        throws SQLException {
        Map<String, Object> newMap = new HashMap<>();
        for (String columnName : columns) {
            newMap.put(columnName.toLowerCase(), getObject(rs, columnName));
        }
        if (resultMap.isEmpty()) {
            resultMap.putAll(newMap);
        }
        else {
            // To find the elaborator name, check if an alias is set, if not
            // check if name is set, if not use elaborator#.
            String stmtName = getAlias();
            if (stmtName.equals("")) {
                if (parentStatement != null) {
                    stmtName = parentStatement.getName();
                }
                else {
                    stmtName = getName();
                }
            }
            if (stmtName.equals("")) {
                stmtName = "elaborator" + pos;
            }
            if (protoQuery.isMultiple()) {
                List<Object> newList = null;
                if (resultMap.containsKey(stmtName)) {
                    newList = (List<Object>) resultMap.get(stmtName);
                }
                else {
                    newList = new ArrayList<>();
                }
                newList.add(newMap);
                resultMap.put(stmtName, newList);
            }
            else {
                resultMap.put(stmtName, newMap);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void addToObject(List<String> columns, ResultSet rs, Object obj,
            boolean elaborator)
        throws SQLException {

        List<String> columnSkip;
        if (elaborator && obj instanceof RowCallback) {
            RowCallback cb = (RowCallback) obj;
            cb.callback(rs);
            columnSkip = cb.getCallBackColumns();
        }
        else {
            columnSkip = new ArrayList<>();
        }

        for (String columnName : columns) {
            if (columnSkip.contains(columnName.toLowerCase())) {
                continue;
            }

            String setName = StringUtil.beanify("set " + columnName.toLowerCase());
            String getName = StringUtil.beanify("get " + columnName.toLowerCase());

            boolean isList = false;
            Method[] methods = obj.getClass().getMethods();
            /*
             * Now loop through the methods and find the set method for this
             * column then decide if it takes a collection Note: This action
             * might not complete correctly if there are two set methods with
             * the same name
             */
            for (Method methodIn : methods) {
                // getName() gets the name of the set method
                // setName is the name of the set method
                if (methodIn.getName().equals(setName)) {
                    Class<?> paramType = methodIn.getParameterTypes()[0];
                    if (Collection.class.isAssignableFrom(paramType)) {
                        isList = true;
                    }
                    break;
                }
            }

            if (isList) { // requires matching get method returning the same
                // list
                Collection<Object> c = (Collection<Object>) MethodUtil.callMethod(obj,
                        getName, new Object[0]);
                if (c == null) {
                    c = new ArrayList<>();
                }
                c.add(getObject(rs, columnName));
                MethodUtil.callMethod(obj, setName, c);
                continue;
            }
            /*
             * Just call the set method. This will call the same set method
             * multiple times. If the result set should be a list, but has a
             * non-Collection set method, the attribute corresponding to this
             * column will ultimately contain the last item found for this
             * column.
             */
            MethodUtil.callMethod(obj, setName, getObject(rs, columnName));
        } // while
    }

    /**
     * Basically a wrapper to rs.getObject, except that it returns a timestamp
     * if the column returned is a date, a Long if the column returned is a
     * BigDecimal OR just the object otherwise. Look at the url blow for more
     * info.
     * http://www.oracle.com/technology/tech/java/sqlj_jdbc/htdocs/jdbc_faq.htm#08_01
     * @param rs the sql result set
     * @param columnName the name of the column to be returned
     * @return the timestamp if rs.getObject is a date, the Long if rs.getObject
     * is a BigDecimal, or just rs.getObject otherwise.
     * @throws SQLException if rs.getObject/rs.getTimestamp raise an exception.
     */
    private Object getObject(ResultSet rs, String columnName) throws SQLException {
        Object columnValue = rs.getObject(columnName);
        if (columnValue == null) {
            return null;
        }

        // Workaround for problem where the JDBC driver returns a
        // java.sql.Date that often times will not deliver time
        // precision beyond 12:00AM Midnight so you get dates like
        // this : August 23, 2005 12:00:00 AM PDT
        // vs the real date: August 23, 2005 1:36:12 PM PDT
        if (columnValue instanceof Date ||
                ("oracle.sql.TIMESTAMPLTZ"
                     .equals(columnValue.getClass().getCanonicalName())) ||
                ("oracle.sql.TIMESTAMP"
                     .equals(columnValue.getClass().getCanonicalName())) ||
                ("oracle.sql.TIMESTAMPTZ"
                     .equals(columnValue.getClass().getCanonicalName()))) {
            return rs.getTimestamp(columnName);
        }
        else if (columnValue instanceof BigDecimal) {
            return rs.getLong(columnName);
        }
        return columnValue;
    }

    private List<String> getColumnNames(ResultSetMetaData rsmd) {
        try {
            ArrayList<String> columns = new ArrayList<>();
            int count = rsmd.getColumnCount();

            for (int i = 1; i <= count; i++) {
                columns.add(rsmd.getColumnName(i).toLowerCase());
            }
            return columns;
        }
        catch (SQLException e) {
            throw SqlExceptionTranslator.sqlException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private boolean checkForColumn(Object obj, String key) {
        if (obj instanceof Map) {
            return ((Map<String, Object>) obj).containsKey(key);
        }
        Class<?> clazz = obj.getClass();
        Method[] methods = clazz.getMethods();
        for (Method methodIn : methods) {
            if (methodIn.getName().equals(StringUtil.beanify("get " + key))) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private Object getKey(Object obj, String key) {
        if (obj instanceof Map) {
            return ((Map<String, Object>) obj).get(key);
        }
        return MethodUtil.callMethod(obj, StringUtil.beanify("get " + key), new Object[0]);
    }

    private Map<Object, Integer> generatePointers(List<Object> dr, String key) {

        Iterator<Object> i = dr.iterator();
        int pos = 0;
        Map<Object, Integer> pointers = new HashMap<>();

        while (i.hasNext()) {
            Object row = i.next();

            if (row instanceof Row) {
                pointers.put(((Row) row).get(key), pos);
            }
            else {
                Object keyData = MethodUtil.callMethod(row,
                        StringUtil.beanify("get " + key), new Object[0]);
                pointers.put(keyData, pos);
            }
            pos++;
        }
        return pointers;
    }

    /**
     * Get the DB connection from Hibernate and run some work on it. Since we
     * will use it to run queries/stored procs, this will also flush the session
     * to ensure that stored procs will see changes made in Hibernate cache
     */
    private <T> T doWithStolenConnection(ReturningWork<T> work) throws HibernateException {
        if (session.getFlushMode().equals(FlushModeType.AUTO)) {
            session.flush();
        }
        return session.doReturningWork(work);
    }

    private void storeForRestart(Map<String, ?> parameters, List<?> inClause, Mode mode) {
        restartData = new RestartData(parameters, inClause, mode);
    }

    /**
     * Restart the latest query
     * @param newSession the new hibernate session to use
     * @param <T> the type of the returned items
     * @return what the previous query returned or null.
     */
    public <T> DataResult<T> restartQuery(Session newSession) {
        session = newSession;

        return restartData == null ? null :
                internalExecute(restartData.getParameters(),
                        restartData.getInClause(), restartData.getMode());
    }

    /**
     * Executes multiple updates with one only prepared statement in batch mode.
     *
     * @param batch a list of parameter maps
     * @return an array of update counts containing one element for each command in the batch
     */
    public int [] executeBatchUpdates(DataResult<Map<String, Object>> batch) {
        return doWithStolenConnection(connection -> {
            try {
                sqlStatement = NamedPreparedStatement.replaceBindParams(sqlStatement, qMap);

                return executeBatch(connection, sqlStatement, qMap, batch);
            }
            catch (SQLException e) {
                throw SqlExceptionTranslator.sqlException(e);
            }
            catch (HibernateException he) {
                throw new
                    HibernateRuntimeException(
                        "HibernateException executing CachedStatement", he);

            }
            catch (RhnRuntimeException e) {
                log.error("Error while processing cached statement sql: {}", getQuery(), e);
                throw e;
            }
        });
    }

    private int [] executeBatch(Connection connection, String sql,
            Map<String, List<Integer>> parameterMap, DataResult<Map<String, Object>> batch) throws SQLException {
        if (log.isDebugEnabled()) {
            log.debug("execute() - Executing: {}", sql);
            log.debug("execute() - With: {}", batch);
        }

        PreparedStatement ps = null;
        try {
            ps = prepareStatement(connection, sql, null);
            return NamedPreparedStatement.executeBatch(ps, parameterMap, batch);
        }
        finally {
            HibernateHelper.cleanupDB(ps);
        }
    }
}
