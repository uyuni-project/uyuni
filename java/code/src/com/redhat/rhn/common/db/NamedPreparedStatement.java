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
package com.redhat.rhn.common.db;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.translation.SqlExceptionTranslator;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Our own preparedStatement class.  RHN wants to use named bind variables,
 * but jdbc doesn't provide them.  This class takes a SQL query to be
 * prepared, and converts all named variables to "?".  It also keeps a hash
 * so that we can find the correct variable when trying to bind the correct
 * values.
 *
 * This should extend PreparedStatement, but it isn't possible to get
 * OracleConnection to return a subclass of OraclePreparedStatement.
 * Oracle 10g introduces this feature natively.  When we migrate to 10g,
 * we should move to Oracle's version.
 *
 */
public final class NamedPreparedStatement {

    // private constructor, because this is a static class.
    private NamedPreparedStatement() {
    }

    private static int findColon(int start, StringBuilder query) {
        boolean inQuotes = false;
        for (int i = start; i < query.length(); i++) {
            char c = query.charAt(i);
            if (c == '"' || c == '\'') {
                inQuotes = !inQuotes;
            }
            if (inQuotes) {
                continue;
            }
            if (c == ':' && (query.charAt(i + 1) == ':')) {
                i++;
                continue;
            }
            // The Oracle PL/SQL syntax uses := to indicate that a function
            // should return a value.  Since, we do not want to replace := with
            // a ?, just skip this : if the next char is a =.
            if (c == ':' && (query.charAt(i + 1) != '=')) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Given a SQL query with named bind parameters convert it to a format
     * that can be used with JDBC.
     * @param rawSQL the SQL statement to create.
     * @param parameterMap a result map representing named parameters and
     *        their positions in the SQL statement.
     * @return a SQL statement that can be used with JDBC
     */
    public static String replaceBindParams(String rawSQL,
            Map<String, List<Integer>> parameterMap) {
        StringBuilder sql = new StringBuilder(rawSQL);

        int idx = findColon(0, sql);
        int variableNumber = 1;
        while (idx != -1) {
            int end = findEndofVariable(sql, idx);
            String name = sql.substring(idx + 1, end).toLowerCase();
            sql = sql.replace(idx, end, "?");

            List<Integer> lst = parameterMap.get(name);
            if (lst == null) {
                lst = new ArrayList<>();
            }
            lst.add(variableNumber);
            parameterMap.put(name, lst);

            idx = findColon(idx + 1 , sql);
            variableNumber++;
        }
        return sql.toString();
    }

    /**
     * Execute the CallableStatement using the given values for bind parameters.
     * @param cs The CallableStatement to execute
     * @param parameterMap The Map returned setup by replaceBindParams
     * @param inParams A map of parameter name to input value to bind to the
     *                 statement.
     * @param outParams A map of parameter name to Integer object of
     *                  SQL Types representing the type of data to be returned.
     * @return true if CallableStatement executed without error, false otherwise.
     * @throws DatabaseException in case of SQL Exception
     */
    public static boolean execute(CallableStatement cs,
            Map<String, List<Integer>> parameterMap,
            Map<String, ?> inParams, Map<String, Integer> outParams)
            throws DatabaseException {
        try {
            setVars(cs, parameterMap, inParams);
            setOutputVars(cs, parameterMap, outParams);
            return cs.execute();
        }
        catch (SQLException e) {
            throw SqlExceptionTranslator.sqlException(e);
        }
    }

    /**
     * Execute the PreparedStatement using the given values for bind
     * parameters.
     * @param ps The PreparedStatement to execute
     * @param parameterMap The Map returned setup by replaceBindParams
     * @param parameters The values to substitute for the named bind parameters
     * @see java.sql.PreparedStatement#execute()
     * @return true if PreparedStatement received a result set
     *         false if PreparedStatement received an update count
     * @throws DatabaseException in case of SQL Exception
     */
    public static boolean execute(PreparedStatement ps,
            Map<String, List<Integer>> parameterMap,
            Map<String, ?> parameters) {
        try {
            setVars(ps, parameterMap, parameters);
            return ps.execute();
        }
        catch (SQLException e) {
            throw SqlExceptionTranslator.sqlException(e);
        }
    }

    /* Find the index of the end of the bind variable.  For right now, the
     * logic is to find the first character that can't be used in a Java
     * identifier.  This may be wrong, but we'll fix that later.
     */
    private static int findEndofVariable(StringBuilder sql, int idx) {
        int i = idx + 1;
        while (i < sql.length() && Character.isJavaIdentifierPart(sql.charAt(i))) {
            i++;
        }
        return i;
    }

    /** get position for named bind variable.  This method should NOT be
     * public, but I need it for datasource, which is in a subpackage of
     * db, so this must become public.
     * @param name Name of the bind variable
     * @param parameterMap Map of parameters.
     * @return The iterator for the list of data returned.
     * @throws BindVariableNotFoundException couldn't find bind variable
     */
    public static Iterator<Integer> getPositions(String name,
            Map<String, List<Integer>> parameterMap)
            throws BindVariableNotFoundException {

        List<Integer> lst = parameterMap.get(name.toLowerCase());
        if (lst == null) {
            throw new BindVariableNotFoundException("Can't find variable: " +
                    name);
        }
        return lst.iterator();
    }

    private static void setVars(PreparedStatement ps,
            Map<String, List<Integer>> parameterMap, Map<String, ?> map) {

        for (String name : map.keySet()) {
            Iterator<Integer> positions = getPositions(name, parameterMap);
            while (positions.hasNext()) {
                Integer pos = positions.next();
                try {
                    Object value = map.get(name);
                    ps.setObject(pos, value);
                }
                catch (SQLException e) {
                    throw SqlExceptionTranslator.sqlException(e);
                }
            }
        }
    }

    private static void setOutputVars(CallableStatement cs,
            Map<String, List<Integer>> parameterMap, Map<String, Integer> map) {

        for (String name : map.keySet()) {
            Iterator<Integer> positions = getPositions(name, parameterMap);
            while (positions.hasNext()) {
                Integer pos = positions.next();
                try {
                    // JDBC sucks.  It uses static int's instead of Integers
                    // to represent SQL types.  So, we treat the values as
                    // Integers, and the caller is responsible for inserting
                    // the Integer object.
                    Integer type = map.get(name);
                    cs.registerOutParameter(pos, type);
                }
                catch (SQLException e) {
                    throw SqlExceptionTranslator.sqlException(e);
                }
            }
        }
    }

    /**
     * Execute the PreparedStatement using the given values for bind
     * parameters in batch mode.
     * @param ps The PreparedStatement to execute
     * @param parameterMap The Map returned setup by replaceBindParams
     * @param batch The values to substitute for the named bind parameters
     * @see java.sql.PreparedStatement#executeBatch()
     * @return an array of update counts containing one element for each command in the batch
     * @throws DatabaseException in case of SQL Exception
     */
    public static int[] executeBatch(PreparedStatement ps,
            Map<String, List<Integer>> parameterMap, DataResult<Map<String, Object>> batch) {
        try {
            for (Map<String, ?> parameters : batch) {
                setVars(ps, parameterMap, parameters);
                ps.addBatch();
            }
            return ps.executeBatch();
        }
        catch (SQLException e) {
            throw SqlExceptionTranslator.sqlException(e);
        }
    }
}
