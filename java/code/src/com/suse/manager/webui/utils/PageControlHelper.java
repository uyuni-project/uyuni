/*
 * Copyright (c) 2020 SUSE LLC
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

package com.suse.manager.webui.utils;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.util.DynamicComparator;
import com.redhat.rhn.frontend.listview.PageControl;

import java.util.List;
import java.util.Map;

import spark.Request;

/**
 * A helper class to parse pagination related query parameters from a Spark request for further use
 *
 * Also provides helper methods to sort/filter data according to the pagination parameters.
 */
public class PageControlHelper {

    private Request request;
    private int start;
    private int pageSize;
    private String query;
    private String queryColumn;
    private String sortDirection;
    private String sortColumn;
    private String function;

    /**
     * Initializes a page control helper with the query parameters from the specified Spark request
     *
     * @param requestIn the spark request which contains the pagination query parameters
     */
    public PageControlHelper(Request requestIn) {
        this(requestIn, null);
    }

    /**
     * Initializes a page control helper with the query parameters from the specified Spark request
     *
     * @param requestIn the spark request which contains the pagination query parameters
     * @param defaultFilterColumn the default filter column to be used if not specified in the request
     */
    public PageControlHelper(Request requestIn, String defaultFilterColumn) {
        request = requestIn;
        parseQueryParams(defaultFilterColumn);
    }

    /**
     * Sorts the specified list in place, using the sort parameters
     *
     * If the sort direction is not specified in the request, the list is sorted in ascending order by default.
     *
     * @param data the list to sort
     * @param <T> the type of the list to sort
     */
    public <T> void applySort(List<T> data) {
        if (isNotEmpty(sortColumn)) {
            // Sort ascending by default
            data.sort(new DynamicComparator(sortColumn, !"-1".equals(sortDirection)));
        }
    }

    /**
     * Parses the request query parameters for pagination
     *
     * @param defaultFilterColumn the default filter column to be used if not specified in the request
     */
    private void parseQueryParams(String defaultFilterColumn) {
        function = request.queryParams("f");

        String filterColumn = request.queryParams("qc");
        if (filterColumn == null) {
            filterColumn = defaultFilterColumn;
        }
        String filterQuery = request.queryParams("q");
        if (isNotEmpty(filterQuery)) {
            query = filterQuery;
            queryColumn = filterColumn;
        }

        sortDirection = request.queryParams("s");
        sortColumn = request.queryParams("sc");

        try {
            start = Integer.parseInt(request.queryParams("p"));
            pageSize = Integer.parseInt(request.queryParams("ps"));
        }
        catch (NumberFormatException e) {
            // No page info available in the request
            start = 1;
        }
    }

    /**
     * Creates a {@link PageControl} object with the page control parameters
     * to be used in legacy code (e.g. mode queries)
     *
     * @return a newly created {@link PageControl} object
     */
    public PageControl getPageControl() {
        PageControl pc = new PageControl();

        pc.setStart(start);
        pc.setPageSize(pageSize);
        if (isNotEmpty(query)) {
            pc.setFilter(true);
            pc.setFilterData(query);
            pc.setFilterColumn(queryColumn);
        }

        pc.setSortColumn(sortColumn);
        pc.setSortDescending("-1".equals(sortDirection));
        return pc;
    }

    /**
     * Process the page controls against a {@link DataResult}
     *
     * A snippet from: {@link com.redhat.rhn.manager.BaseManager#processPageControl}
     *
     * @param dataResult the data result
     * @param <T> the type of the data items
     * @return Data result modified (filtered) by the page control
     */
    public <T> DataResult<T> processPageControl(DataResult<T> dataResult) {
        return this.processPageControl(dataResult, null);
    }

    /**
     * Process the page controls against a {@link DataResult},
     * running any elaborators attached after the data is filtered, sorted and paginated
     *
     * A snippet from: {@link com.redhat.rhn.manager.BaseManager#processPageControl}
     *
     * @param dataResult the data result
     * @param elabParams named parameters for the elaboration query
     * @param <T> the type of the data items
     * @return Data result modified (filtered) by the page control
     */
    public <T> DataResult<T> processPageControl(DataResult<T> dataResult, Map<String, Object> elabParams) {
        if (elabParams != null) {
            dataResult.setElaborationParams(elabParams);
        }

        PageControl pc = this.getPageControl();

        dataResult.setFilter(pc.hasFilter());
        if (pc.hasFilter()) {
            pc.filterData(dataResult);
            //reset the total size because filtering removes some
            dataResult.setTotalSize(dataResult.size());
        }

        this.applySort(dataResult);

        if (pageSize > 0) {
            // Use the PageControl to limit the list to the selected region
            dataResult = dataResult.subList(pc.getStart() - 1, pc.getEnd());
        }

        //elaborate the data result to get the detailed information.
        if (elabParams != null) {
            dataResult.elaborate(elabParams);
        }

        return dataResult;
    }

    public int getStart() {
        return start;
    }

    public int getPageSize() {
        return pageSize;
    }

    public String getQuery() {
        return query;
    }

    public String getQueryColumn() {
        return queryColumn;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public String getSortColumn() {
        return sortColumn;
    }

    public String getFunction() {
        return function;
    }
}
