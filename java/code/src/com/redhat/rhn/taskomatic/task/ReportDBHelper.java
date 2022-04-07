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
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.db.datasource.WriteMode;

import org.hibernate.Session;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReportDBHelper {

    private ReportDBHelper() {
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
    public static <T> Stream<DataResult<T>> batchStream(SelectMode query, int batchSize, int initialOffset) {
        return Stream.iterate(initialOffset, i -> i + batchSize)
                .map(offset -> (DataResult<T>) query.execute(Map.of("offset", offset, "limit", batchSize)))
                .takeWhile(batch -> !batch.isEmpty());
    }

    /**
     * Generated a query for all local entries of a report db table
     * @param session session the query should use
     * @param table table name
     * @return select mode query
     */
    public static SelectMode generateQuery(Session session, String table) {
        final String sqlStatement = "SELECT * FROM " + table +
                " WHERE mgm_id = 1 ORDER BY ctid OFFSET :offset LIMIT :limit";
        return new GeneratedSelectMode("select." + table, session, sqlStatement, List.of("offset", "limit"));
    }

    /**
     * Generates a delete statement for a report db table that takes mgm_id as parameter
     * @param session session the query should use
     * @param table table name
     * @return write mode query
     */
    public static WriteMode generateDelete(Session session, String table) {
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
    public static WriteMode generateInsert(Session session, String table, long mgmId, Set<String> params) {
        final String sqlStatement = String.format(
                "INSERT INTO %s (mgm_id, %s) VALUES (%s, %s)",
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
    public static WriteMode generateInsertWithDate(Session session, String table, long mgmId, Set<String> params) {
        final String sqlStatement = String.format(
                "INSERT INTO %s (mgm_id, synced_date, %s) VALUES (%s, current_timestamp, %s)",
                table,
                String.join(",", params),
                mgmId,
                params.stream().map(p -> ":" + p).collect(Collectors.joining(","))
        );

        return new GeneratedWriteMode("insert." + table, session, sqlStatement, params);
    }

}
