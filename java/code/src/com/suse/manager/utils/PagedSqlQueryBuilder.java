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
package com.suse.manager.utils;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.frontend.dto.BaseTupleDto;
import com.redhat.rhn.frontend.listview.PageControl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.PersistenceException;
import javax.persistence.Tuple;

/**
 * Helps building paged SQL queries
 */
public class PagedSqlQueryBuilder {

    private static final Logger LOG = LogManager.getLogger(PagedSqlQueryBuilder.class);

    private String select;
    private String from;
    private String where;
    private String idColumn = "id";

    /**
     * Create a query builder with count column named 'id'
     */
    public PagedSqlQueryBuilder() {
    }

    /**
     * Create a query builder with customized column name for the count.
     *
     * @param idColumnIn the column to count items with, usually an id
     */
    public PagedSqlQueryBuilder(String idColumnIn) {
        this.idColumn = idColumnIn;
    }


    /**
     * Add select clause to the query builder
     *
     * @param sql the native SQL select part, without the 'select' keyword
     * @return the current object to ease chaining calls
     */
    public PagedSqlQueryBuilder select(String sql) {
        this.select = sql;
        return this;
    }

    /**
     * Add from clause to the query builder
     *
     * @param sql the native SQL from part, without the 'from' keyword
     * @return the current object to ease chaining calls
     */
    public PagedSqlQueryBuilder from(String sql) {
        this.from = sql;
        return this;
    }

    /**
     * Add where clause to the query builder
     *
     * @param sql the native SQL where part, without the 'where' keyword
     * @return the current object to ease chaining calls
     */
    public PagedSqlQueryBuilder where(String sql) {
        this.where = sql;
        return this;
    }

    /**
     * Parse the filter provided in the PageControl by the user into something useful for SQL queries
     *
     * @param pc the page control
     * @return the parse filter and its value
     */
    public static FilterWithValue parseFilter(Optional<PageControl> pc) {
        String filter = "";
        Object filterValue = "";
        String filterColumn = pc.map(PageControl::getFilterColumn).orElse("");

        if (pc.map(PageControl::hasFilter).orElse(false)) {
            String operator = "ILIKE";
            String data = pc.map(PageControl::getFilterData).orElse("").trim();
            filterValue = data;

            // Match everything if we have an operator but no value
            if (Pattern.matches("^([<>!=]{0,2})$", data)) {
                return new FilterWithValue(String.format(" %s ILIKE :filter_value ", filterColumn), "%");
            }

            Matcher matcher = Pattern.compile("^([<>!=]{0,2}) *([^ ]*)$").matcher(data);
            if (matcher.matches()) {
                operator = matcher.group(1);
                if ("".equals(operator)) {
                    operator = "ILIKE";
                }

                if (Stream.of("true", "false").anyMatch(v -> v.equalsIgnoreCase(matcher.group(2)))) {
                    return new FilterWithValue(
                            String.format(" %s = CAST(:filter_value AS BOOLEAN) ", filterColumn),
                            matcher.group(2));
                }

                // Handle Date cast
                try {
                    LocalDate.parse(matcher.group(2));
                    return new FilterWithValue(
                            String.format(" CAST(%s AS DATE) %s CAST(:filter_value AS DATE) ",
                                    filterColumn, operator),
                            matcher.group(2)
                    );
                }
                catch (DateTimeException e) {
                    // That wasn't a date, ignore
                }

                filterValue = parseNumber(matcher.group(2));
            }

            if ("ILIKE".equals(operator)) {
                filterValue = "%" + filterValue + "%";
            }
            filter = String.format(" %s %s :filter_value ", filterColumn, operator);
        }
        return new FilterWithValue(filter, filterValue);
    }

    private static Object parseNumber(String value) {
        try {
            return Long.parseLong(value);
        }
        catch (NumberFormatException e) {
            try {
                return Double.parseDouble(value);
            }
            catch (NumberFormatException e1) {
                return value;
            }
        }
    }

    /**
     * Run the queries and result the page
     *
     * @param parameters the query parameters
     * @param pc the page control
     * @param clazz the class of the row objects
     * @param <T> the type of the returned result
     *
     * @return the DataResult
     */
    public <T extends BaseTupleDto> DataResult<T> run(Map<String, Object> parameters, PageControl pc,
                                                      Class<T> clazz) {
        return run(parameters, pc, clazz, HibernateFactory.getSession());
    }

    /**
     * Run the queries and result the page
     *
     * @param parameters the query parameters
     * @param pc the page control
     * @param clazz the class of the row objects
     * @param session the hibernate session to use
     * @param <T> the type of the returned result
     *
     * @return the DataResult
     */
    public <T extends BaseTupleDto> DataResult<T> run(Map<String, Object> parameters, PageControl pc,
                                                      Class<T> clazz, Session session) {
        Optional<PageControl> pageControl = Optional.ofNullable(pc);
        String whereWithFilter = Optional.ofNullable(where).orElse("");

        FilterWithValue filter = parseFilter(pageControl);
        if (!"".equals(filter.getValue())) {
            whereWithFilter = (where != null) ?
                    String.format("(%s) AND %s", where, filter.getFilter()) :
                    filter.getFilter();
        }

        String sortSql = "";
        if (pageControl.map(PageControl::getSortColumn).orElse(null) != null) {
            String sortDirection = pc.isSortDescending() ? "DESC" : "ASC";
            sortSql = String.format(" ORDER BY %s %s", pc.getSortColumn(), sortDirection);
        }
        String sql = String.format(
                "SELECT %s FROM %s WHERE %s%s",
                select, from, whereWithFilter, sortSql);

        Query<Tuple> query = session.createNativeQuery(sql, Tuple.class);

        if (pageControl.isPresent()) {
            query.setFirstResult(pageControl.map(PageControl::getStart).orElse(1) - 1);
            if (pageControl.map(PageControl::getPageSize).orElse(0) > 0) {
                query.setMaxResults(pc.getPageSize());
            }
        }

        String countSql = String.format("SELECT count(%s) FROM %s WHERE %s", idColumn, from, whereWithFilter);
        Query<Object> countQuery = session.createNativeQuery(countSql);

        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
            countQuery.setParameter(entry.getKey(), entry.getValue());
        }

        if (!"".equals(filter.getValue())) {
            query = query.setParameter("filter_value", filter.getValue());
            countQuery = countQuery.setParameter("filter_value", filter.getValue());
        }

        List<T> rows = null;
        try {
            rows = query.list().stream().map(t -> {
                try {
                    Constructor<T> ctor = clazz.getConstructor(Tuple.class);
                    return ctor.newInstance(t);
                }
                catch (NoSuchMethodException | IllegalAccessException |
                        InstantiationException | InvocationTargetException e) {
                    // Should never happen given the type has to be a BaseTupleDto
                    LOG.error("Failed to create {} from SQL tuple", clazz.getName());
                    return null;
                }
            }).collect(Collectors.toList());
        }
        catch (PersistenceException e) {
            // Log the error here, the user shouldn't see those
            LOG.error("Failed to process paged SQL query", e);
        }

        DataResult<T> dr = new DataResult<>(rows != null ? rows : List.of());
        int count = ((Number)countQuery.uniqueResult()).intValue();
        dr.setTotalSize(count);
        return dr;
    }

    /**
     * Represents the parse filter column and value
     */
    public static class FilterWithValue {
        private final String filter;
        private final Object value;

        protected FilterWithValue(String filterIn, Object valueIn) {
            filter = filterIn;
            value = valueIn;
        }

        /**
         * @return value of filter
         */
        public String getFilter() {
            return filter;
        }

        /**
         * @return value of value
         */
        public Object getValue() {
            return value;
        }
    }
}
