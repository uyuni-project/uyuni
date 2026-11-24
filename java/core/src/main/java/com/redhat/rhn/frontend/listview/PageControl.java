/*
 * Copyright (c) 2025 SUSE LLC
 * Copyright (c) 2009--2010 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.frontend.listview;

import com.redhat.rhn.common.conf.ConfigDefaults;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * PageControl is a means of controlling how much data the user
 * sees in a list at a time. It also provides the filtering and indexing
 * mechanisms of ListControl
 */
public class PageControl extends ListControl {
    private int start;
    // The current user's page size.
    private int pageSize = DEFAULT_PER_PAGE;
    private String sortColumn;
    private boolean sortDescending;

    private static final Logger LOG = LogManager.getLogger(PageControl.class);

    /** static value for default results per page. */
    public static final int DEFAULT_PER_PAGE = 25;
    /** static value for max results per page. */
    public static final int MAX_PER_PAGE = 500;
    /** page sizes offered when the configuration does not set any. */
    public static final List<Integer> DEFAULT_PAGE_SIZES = List.of(5, 10, 25, 50, 100, 250, 500);

    /**
     * Initialize an object with controls starting from 1 and with the default page size
     *
     * See: {@link PageControl#DEFAULT_PER_PAGE}
     */
    public PageControl() {
        this(1);
    }

    /**
     * Initialize an object with the specified controls
     * @param startIn the first result
     * @param pageSizeIn the page size
     * @param sortColumnIn the column used to sort in ascending order
     */
    public PageControl(int startIn, int pageSizeIn, String sortColumnIn) {
        this(startIn, pageSizeIn, sortColumnIn, false, null, null);
    }

    /**
     * Initialize an object with the specified controls
     * @param startIn the first result
     * @param pageSizeIn the page size
     * @param sortColumnIn the column used to sort
     * @param sortDescendingIn true for descending order
     */
    public PageControl(int startIn, int pageSizeIn, String sortColumnIn, boolean sortDescendingIn) {
        this(startIn, pageSizeIn, sortColumnIn, sortDescendingIn, null, null);
    }

        /**
         * Initialize an object with the specified controls
         * @param startIn the first result
         * @param pageSizeIn the page size
         * @param sortColumnIn the column used to sort
         * @param sortDescendingIn true for descending order
         * @param filterColumnIn the filter column
         * @param filterDataIn the filter data
         */
    public PageControl(int startIn, int pageSizeIn, String sortColumnIn, boolean sortDescendingIn,
                       String filterColumnIn, String filterDataIn) {
        this.start = startIn;
        this.pageSize = pageSizeIn;
        this.sortColumn = sortColumnIn;
        this.sortDescending = sortDescendingIn;
        this.setFilter(filterColumnIn != null);
        this.setFilterColumn(filterColumnIn);
        this.setFilterData(filterDataIn);
    }

    /**
     * Initialize an object with controls starting from a specified index and with the default page size
     *
     * See: {@link PageControl#DEFAULT_PER_PAGE}
     *
     * @param startIn the start index of the page that counts from 1
     */
    public PageControl(int startIn) {
        this(startIn, DEFAULT_PER_PAGE);
    }

    /**
     * Initialize an object with the specified start index and and page size
     *
     * @param startIn the start index of the page that counts from 1
     * @param pageSizeIn the page size
     */
    public PageControl(int startIn, int pageSizeIn) {
        this.setStart(startIn);
        this.setPageSize(pageSizeIn);
    }

    /**
     * Get the number of entries desired in this list
     * @return Returns the end.
     */
    public int getEnd() {

        int end = start + pageSize - 1;
        if (end - start > MAX_PER_PAGE) {
            end = start + MAX_PER_PAGE;
        }

        return end;
    }

    /**
     * Set the page size for this list
     * @param ps The current user's desired page size.
     */
    public void setPageSize(int ps) {
        pageSize = ps;
    }

    /**
     * @return the page size
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * Get the first element in the list
     * @return Returns the start.
     */
    public int getStart() {
        return start;
    }

    /**
     * Set the first element in the list must be greater than 0.
     * @param s The start to set greater than 0.
     */
    public void setStart(int s) {
        if (s < 1) {
            throw new IllegalArgumentException("Start must be > 0");
        }
        this.start = s;
    }

    /**
     * @return value of sortColumn
     */
    public String getSortColumn() {
        return sortColumn;
    }

    /**
     * @param sortColumnIn value of sortColumn
     */
    public void setSortColumn(String sortColumnIn) {
        sortColumn = sortColumnIn;
    }

    /**
     * @return value of sortDescending
     */
    public boolean isSortDescending() {
        return sortDescending;
    }

    /**
     * @param sortDescendingIn value of sortDescending
     */
    public void setSortDescending(boolean sortDescendingIn) {
        sortDescending = sortDescendingIn;
    }

    /**
     * Returns the default page size that can be used
     * by the app. This is basically used at the user creation time.
     * When a new user is created, the default page size is set
     * using the value returned by this method..
     * @return the defaut page size.
     */
    public static int getDefaultPageSize() {
        String sizeStr = ConfigDefaults.get().getDefaultPageSize();
        int size = DEFAULT_PER_PAGE;
        try {
            if (!StringUtils.isBlank(sizeStr)) {
                size = Integer.parseInt(sizeStr);
            }
        }
        catch (NumberFormatException nfe) {
            LOG.warn("Number format exception encountered while parsing {}={}",
                    ConfigDefaults.DEFAULT_PAGE_SIZE, sizeStr);
            size = DEFAULT_PER_PAGE;
        }

        int prev = 0;
        for (int sz : getPageSizes()) {
            if (sz == size) {
                return size;
            }
            if (sz > size) {
                if (prev == 0) {
                    return sz;
                }
                return prev;
            }
            prev = sz;
        }

        return prev;
    }

    /**
     * Returns the list of available page sizes.
     * This is used mainly in the PageSize selection
     * drop down when rendering lists...
     * This is also used in the My Preferences page to check
     * the list of page sizes..
     * @return the list of page sizes..
     */
    public static List<Integer> getPageSizes() {
        String pageSizes = ConfigDefaults.get().getPageSizes();
        String [] sizes = pageSizes.split("\\,");
        if (StringUtils.isBlank(pageSizes) || sizes == null || sizes.length == 0) {
            return DEFAULT_PAGE_SIZES;
        }

        try {
            List<Integer> ret = new LinkedList<>();
            for (String size : sizes) {
                ret.add(Integer.valueOf(size.trim()));
            }
            return ret;
        }
        catch (NumberFormatException nfe) {
            LOG.warn("Number format exception encountered while parsing {}={}",
                    ConfigDefaults.PAGE_SIZES, pageSizes);
            return Collections.emptyList();
        }
    }
}
