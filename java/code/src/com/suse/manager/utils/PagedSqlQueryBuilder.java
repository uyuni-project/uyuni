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
import java.util.function.BiFunction;
import java.util.function.Function;
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

    private static final Pattern EQUAL_OPERATOR_ONLY_REGEX = Pattern.compile("^([!=]{0,2})$");
    private static final Pattern EQUAL_FILTER_REGEX = Pattern.compile("^([!=]{0,2}) *([^ ]*)$");

    private static final Pattern NUMBER_OPERATOR_ONLY_REGEX = Pattern.compile("^([<>!=]{0,2})$");

    private static final Pattern NUMBER_FILTER_REGEX = Pattern.compile("^([<>!=]{0,2}) *([^ ]*)$");

    private static final Logger LOG = LogManager.getLogger(PagedSqlQueryBuilder.class);

    private String select;
    private String from;
    private String where;
    private String idColumn = "id";
    private String countFrom;

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
     * Add from clause to count the items to the query builder
     * If not set, use the from value. This may be useful to avoid joining tables for nothing in the count query.
     *
     * @param sql the native SQL from part, without the 'from' keyword
     * @return the current object to ease chaining calls
     */
    public PagedSqlQueryBuilder countFrom(String sql) {
        this.countFrom = sql;
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
     * Transform the PageControl filter into an SQL text query.
     * Operators:
     *   ! converted to NOT ILIKE
     *   = and != left as is
     *   no operator converted to ILIKE
     *
     * @param pc the PageControl to parse
     * @return the created filter query and the value
     */
    public static FilterWithValue parseFilterAsText(Optional<PageControl> pc) {
        String filter = "";
        Object filterValue = "";
        String filterColumn = pc.map(PageControl::getFilterColumn).orElse("");

        if (pc.map(PageControl::hasFilter).orElse(false)) {
            String operator = "ILIKE";
            String data = pc.map(PageControl::getFilterData).orElse("").trim();
            filterValue = data;

            // Match everything if we have an operator but no value
            if (EQUAL_OPERATOR_ONLY_REGEX.matcher(data).matches()) {
                return FilterWithValue.NO_FILTER;
            }

            Matcher matcher = EQUAL_FILTER_REGEX.matcher(data);
            if (matcher.matches()) {
                operator = matcher.group(1);
                if (operator.isEmpty()) {
                    operator = "ILIKE";
                }
                filterValue = matcher.group(2);
            }

            if ("!".equals(operator)) {
                operator = "NOT ILIKE";
            }

            if (operator.contains("ILIKE")) {
                filterValue = "%" + filterValue + "%";
            }
            filter = String.format(" %s %s :filter_value ", filterColumn, operator);
        }
        return new FilterWithValue(filter, filterValue);
    }

    private static FilterWithValue parseFilterAsComparable(Optional<PageControl> pc, Pattern operatorOnlyRegex,
                                                           Pattern filterRegex,
                                                           BiFunction<String, String, FilterWithValue> converter) {
        if (pc.map(PageControl::hasFilter).orElse(false)) {
            String data = pc.map(PageControl::getFilterData).orElse("").trim();

            // Match everything if we have an operator but no value
            if (operatorOnlyRegex.matcher(data).matches()) {
                return FilterWithValue.NO_FILTER;
            }

            Matcher matcher = filterRegex.matcher(data);
            if (matcher.matches()) {
                String operator = matcher.group(1);
                if (operator.isEmpty()) {
                    operator = "=";
                }
                return converter.apply(operator, matcher.group(2));
            }
        }
        return FilterWithValue.NO_FILTER;
    }

    /**
     * Transform the PageControl filter into an SQL boolean query.
     * Supported operators: = and !=. Default is =.
     *
     * @param pc the page control
     * @return the parse filter and its value
     */
    public static FilterWithValue parseFilterAsBoolean(Optional<PageControl> pc) {
        String filterColumn = pc.map(PageControl::getFilterColumn).orElse("");
        return parseFilterAsComparable(pc, EQUAL_OPERATOR_ONLY_REGEX, EQUAL_FILTER_REGEX, (operator, value) -> {
            if (Stream.of("true", "false").anyMatch(v -> v.equalsIgnoreCase(value))) {
                return new FilterWithValue(
                        String.format(" %s %s CAST(:filter_value AS BOOLEAN) ", filterColumn, operator), value);
            }
            return FilterWithValue.NO_FILTER;
        });
    }

    /**
     * Transform the PageControl filter into an SQL DATE query.
     * Supported operators: <, <=, >, >=, =, !=. Default is =
     *
     * @param pc the page control
     * @return the parse filter and its value
     */
    public static FilterWithValue parseFilterAsDate(Optional<PageControl> pc) {
        String filterColumn = pc.map(PageControl::getFilterColumn).orElse("");
        return parseFilterAsComparable(pc, NUMBER_OPERATOR_ONLY_REGEX, NUMBER_FILTER_REGEX, (operator, value) -> {
            try {
                LocalDate.parse(value);
                return new FilterWithValue(
                        String.format(" CAST(%s AS DATE) %s CAST(:filter_value AS DATE) ",
                                filterColumn, operator),
                       value
                );
            }
            catch (DateTimeException e) {
                // That wasn't a date, ignore
            }
            return FilterWithValue.NO_FILTER;
        });
    }

    /**
     * Transform the PageControl filter into an SQL NUMBER query.
     * Supported operators: <, <=, >, >=, =, !=. Default is =
     *
     * @param pc the page control
     * @return the parse filter and its value
     */
    public static FilterWithValue parseFilterAsNumber(Optional<PageControl> pc) {
        String filterColumn = pc.map(PageControl::getFilterColumn).orElse("");
        return parseFilterAsComparable(pc, NUMBER_OPERATOR_ONLY_REGEX, NUMBER_FILTER_REGEX, (operator, value) -> {
            Number filterValue;
            try {
                filterValue = Long.parseLong(value);
            }
            catch (NumberFormatException e) {
                try {
                    filterValue = Double.parseDouble(value);
                }
                catch (NumberFormatException e1) {
                    return FilterWithValue.NO_FILTER;
                }
            }
            return new FilterWithValue(String.format(" %s %s :filter_value ", filterColumn, operator), filterValue);
        });
    }

    /**
     * Run the queries and result the page
     *
     * @param parameters the query parameters
     * @param pc the page control
     * @param filterParser function converting the PageControl filter into a FilterWithValue
     * @param clazz the class of the row objects
     * @param <T> the type of the returned result
     *
     * @return the DataResult
     */
    public <T extends BaseTupleDto> DataResult<T> run(Map<String, Object> parameters, PageControl pc,
                                                      Function<Optional<PageControl>, FilterWithValue> filterParser,
                                                      Class<T> clazz) {
        return run(parameters, pc, filterParser, clazz, HibernateFactory.getSession());
    }

    /**
     * Run the queries and result the page
     *
     * @param parameters the query parameters
     * @param pc the page control
     * @param filterParser function converting the PageControl filter into a FilterWithValue
     * @param clazz the class of the row objects
     * @param session the hibernate session to use
     * @param <T> the type of the returned result
     *
     * @return the DataResult
     */
    public <T extends BaseTupleDto> DataResult<T> run(Map<String, Object> parameters, PageControl pc,
                                                      Function<Optional<PageControl>, FilterWithValue> filterParser,
                                                      Class<T> clazz, Session session) {
        Optional<PageControl> pageControl = Optional.ofNullable(pc);
        String whereWithFilter = Optional.ofNullable(where).orElse("");

        FilterWithValue filter = Optional.ofNullable(filterParser).map(parser -> parser.apply(pageControl)).
                orElse(FilterWithValue.NO_FILTER);
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

        String countSql = String.format("SELECT count(%s) FROM %s WHERE %s", idColumn,
                countFrom != null ? countFrom : from, whereWithFilter);
        Query<Tuple> countQuery = session.createNativeQuery(countSql, Tuple.class);

        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
            countQuery.setParameter(entry.getKey(), entry.getValue());
        }

        if (!"".equals(filter.getValue())) {
            query = query.setParameter("filter_value", filter.getValue());
            countQuery = countQuery.setParameter("filter_value", filter.getValue());
        }

        List<T> rows = runQuery(query, clazz);

        DataResult<T> dr = new DataResult<>(rows != null ? rows : List.of());
        try {
            int count = countQuery.uniqueResult().get(0, Number.class).intValue();
            dr.setTotalSize(count);
        }
        catch (PersistenceException e) {
            // Ignore since it would already be reported in the previous error
            LOG.debug("Failed to get total count", e);
        }
        return dr;
    }

    private <T> List<T> runQuery(Query<Tuple> query, Class<T> clazz) {
        try {
            LOG.debug("Running pages query");
            List<Tuple> results = query.list();
            LOG.debug("Creating DTOs for {} results", results.size());
            List<T> dtos = createDTOs(results, clazz);
            LOG.debug("Finished creating DTOs");
            return dtos;
        }
        catch (PersistenceException e) {
            // Log the error here, the user shouldn't see those
            LOG.error("Failed to process paged SQL query: {}", query.getQueryString(), e);
        }
        return List.of();
    }

    private <T> List<T> createDTOs(List<Tuple> data, Class<T> clazz) {
        try {
            final Constructor<T> ctor = clazz.getConstructor(Tuple.class);

            return data.stream().map(t -> {
                try {
                    return ctor.newInstance(t);
                }
                catch (IllegalAccessException |
                       InstantiationException | InvocationTargetException e) {
                    // Should never happen given the type has to be a BaseTupleDto
                    LOG.error("Failed to create {} from SQL tuple", clazz.getName(), e);
                    return null;
                }
            }).collect(Collectors.toList());
        }
        catch (NoSuchMethodException e) {
            LOG.error("Cannot create {} objects from Tuple", clazz.getName());
            return List.of();
        }
    }

    /**
     * Represents the parse filter column and value
     */
    public static class FilterWithValue {

        private static final FilterWithValue NO_FILTER = new FilterWithValue("", "");

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
