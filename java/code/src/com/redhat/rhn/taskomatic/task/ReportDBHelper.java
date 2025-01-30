/*
 * Copyright (c) 2022 SUSE LLC
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
package com.redhat.rhn.taskomatic.task;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.GeneratedSelectMode;
import com.redhat.rhn.common.db.datasource.GeneratedWriteMode;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.db.datasource.WriteMode;

import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReportDBHelper {

    /** The mgm_id used in the reporting database to indicate the data local which belong to the local server. */
    public static final long LOCAL_MGM_ID = 1;

    public static final ReportDBHelper INSTANCE = new ReportDBHelper();

    /**
     * Default constructor to allow unit test sub-classes
     */
    protected ReportDBHelper() {
    }

    /**
     * Update the query parameters map getting the value of the filter field from the last entry of the data batch.
     *
     * @param parametersMap the parameters map to update
     * @param dataBatch the data batch
     * @param fieldSet the set of fields to update
     */
    public void updateParameters(Map<String, Object> parametersMap, DataResult<Map<String, Object>> dataBatch,
                                 Set<String> fieldSet) {
        // Update each filters of the parametersMap based on the last entry of the batch so that
        // we can filter all the rows we have already extracted
        for (String filterField : fieldSet) {
            parametersMap.put(filterField, dataBatch.get(dataBatch.size() - 1).get(filterField));
        }
    }

    /**
     * Returns the result of a query in a stream of batches.
     * @param query select query
     * @param batchSize max size of a batch
     * @param initialOffset initial offset
     * @param <T> type of the query result
     * @return stream of batched results
     */
    @SuppressWarnings("unchecked")
    public <T> Stream<DataResult<T>> batchStream(SelectMode query, int batchSize, int initialOffset) {
        return Stream.iterate(initialOffset, i -> i + batchSize)
                .map(offset -> (DataResult<T>) query.execute(Map.of("offset", offset, "limit", batchSize)))
                .takeWhile(batch -> !batch.isEmpty());
    }

    private String getOrderColumns(Session session, String table, Logger log) {
        String orderSQL =
                "SELECT string_agg(a.attname, ', ') AS order " +
                "  FROM pg_constraint AS c " +
                "    CROSS JOIN LATERAL UNNEST(c.conkey) AS cols(colnum) " +
                "    INNER JOIN pg_attribute AS a ON a.attrelid = c.conrelid AND cols.colnum = a.attnum " +
                " WHERE c.contype = 'p' " +
                "   AND c.conrelid = '" + table + "'::REGCLASS " +
                "UNION " +
                "SELECT string_agg(a.attname, ', ') AS order " +
                "  FROM pg_index ix " +
                "  JOIN pg_class t on t.oid = ix.indrelid " +
                "  JOIN pg_class i on i.oid = ix.indexrelid " +
                "  JOIN pg_attribute a on a.attrelid = t.oid and a.attnum = ANY(ix.indkey) " +
                " WHERE t.relkind = 'r' " +
                "   AND t.relname = '" + table.toLowerCase() + "' " +
                "   AND i.relname = '" + table.toLowerCase() + "_order_idx'";

        GeneratedSelectMode orderQuery = new GeneratedSelectMode("orderquery." + table, session, orderSQL , List.of());

        DataResult<Map<String, String>> order = orderQuery.execute();
        String orderColumns = order.stream().findFirst().map(o -> o.getOrDefault("order", "ctid")).orElse("ctid");

        log.debug("Order Columns of {} by: {}", table, orderColumns);
        return orderColumns;
    }

    /**
     * Generated a query for checking if a table exists
     * @param session session the query should use
     * @param tables tables list name
     * @return select mode query
     */
    public SelectMode generateExistingTables(Session session, List<String> tables) {
        List<String> selectContent =
                tables.stream().map(t -> "to_regclass('" + t + "') AS " + t).collect(Collectors.toList());
        final String sqlStatement = "SELECT " + String.join(",", selectContent);
        return new GeneratedSelectMode("exists.reportdbtables" , session, sqlStatement, Collections.emptyList());
    }

    /**
     * Generated a query for all local entries of a report db table
     * @param session session the query should use
     * @param table table name
     * @param log the logger
     * @return select mode query
     */
    public SelectMode generateQuery(Session session, String table, Logger log) {
        String orderColumns = getOrderColumns(session, table, log);

        final String sqlStatement = "SELECT * FROM " + table +
                " WHERE mgm_id = " + LOCAL_MGM_ID +  " ORDER BY " + orderColumns + " OFFSET :offset LIMIT :limit";
        return new GeneratedSelectMode("select." + table, session, sqlStatement, List.of("offset", "limit"));
    }

    /**
     * Generates a delete statement for a report db table that takes mgm_id as parameter
     * @param session session the query should use
     * @param table table name
     * @return write mode query
     */
    public WriteMode generateDelete(Session session, String table) {
        final String sqlStatement = "DELETE FROM " + table + " WHERE mgm_id = :mgm_id";
        final List<String> params = List.of("mgm_id");

        return new GeneratedWriteMode("delete." + table, session, sqlStatement, params);
    }

    /**
     * Generates an insert statement for a report db table
     * @param session session the query should use
     * @param table table name
     * @param mgmId mgmId to insert
     * @param params table column names (excluding mgm_id)
     * @return write mode query
     */
    public WriteMode generateInsert(Session session, String table, long mgmId, Set<String> params) {
        final String sqlStatement = String.format(
                "INSERT INTO %s (mgm_id, %s) " +
                "     VALUES (%s, %s) " +
                "ON CONFLICT DO NOTHING",
                table,
                String.join(",", params),
                mgmId,
                params.stream().map(p -> ":" + p).collect(Collectors.joining(","))
        );

        return new GeneratedWriteMode("insert." + table, session, sqlStatement, params);
    }

    /**
     * Generates an insert statement for a report db table that automatically sets synced_date to current_timestamp
     * @param session session the query should use
     * @param table table name
     * @param mgmId mgmId to insert
     * @param params table column names (excluding mgm_id)
     * @return write mode query
     */
    public WriteMode generateInsertWithDate(Session session, String table, long mgmId, Set<String> params) {
        final String sqlStatement = String.format(
                "INSERT INTO %s (mgm_id, synced_date, %s) " +
                "     VALUES (%s, current_timestamp, %s) " +
                "ON CONFLICT DO NOTHING",
                table,
                String.join(",", params),
                mgmId,
                params.stream().map(p -> ":" + p).collect(Collectors.joining(","))
        );

        return new GeneratedWriteMode("insert." + table, session, sqlStatement, params);
    }

    /**
     * Analyzes the report database tables after massive inserts
     * @param session session the query should use
     */
    public void analyzeReportDb(Session session) {
        var m = ModeFactory.getCallableMode(session, "GeneralReport_queries", "analyze_reportdb");
        m.execute(new HashMap<>(), new HashMap<>());
    }
}
