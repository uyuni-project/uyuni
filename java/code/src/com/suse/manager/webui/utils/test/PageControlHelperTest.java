/*
 * Copyright (c) 2020--2021 SUSE LLC
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
package com.suse.manager.webui.utils.test;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.frontend.listview.PageControl;

import com.suse.manager.webui.utils.PageControlHelper;
import com.suse.manager.webui.utils.SparkTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

public class PageControlHelperTest extends TestCase {

    private static final String REQUEST_URL = "https://pagecontrol.test";

    public void testRequestWithNoParams() {
        PageControlHelper helper = new PageControlHelper(SparkTestUtils.createMockRequest(REQUEST_URL));

        assertEquals(1, helper.getStart());
        assertEquals(0, helper.getPageSize());
        assertNull(helper.getQuery());
        assertNull(helper.getQueryColumn());
        assertNull(helper.getSortDirection());
        assertNull(helper.getSortColumn());
        assertNull(helper.getFunction());
    }

    public void testRequestWithPageParams() {
        Map<String, String> queryParams = new HashMap<>();
        // Start index and page size
        queryParams.put("p", "5");
        queryParams.put("ps", "10");

        PageControlHelper helper = new PageControlHelper(SparkTestUtils.createMockRequestWithParams(REQUEST_URL,
                queryParams), "mydefaultproperty");

        assertEquals(5, helper.getStart());
        assertEquals(10, helper.getPageSize());
        assertNull(helper.getQuery());
        assertNull(helper.getQueryColumn());
        assertNull(helper.getSortDirection());
        assertNull(helper.getSortColumn());
        assertNull(helper.getFunction());
    }

    public void testRequestWithFilterParams() {
        Map<String, String> queryParams = new HashMap<>();
        // Start index and page size
        queryParams.put("p", "5");
        queryParams.put("ps", "10");
        // Filter string and column
        queryParams.put("q", "mystring");
        queryParams.put("qc", "myproperty");

        PageControlHelper helper = new PageControlHelper(SparkTestUtils.createMockRequestWithParams(REQUEST_URL,
                queryParams), "mydefaultproperty");

        assertEquals(5, helper.getStart());
        assertEquals(10, helper.getPageSize());
        assertEquals("mystring", helper.getQuery());
        assertEquals("myproperty", helper.getQueryColumn());
        assertNull(helper.getSortDirection());
        assertNull(helper.getSortColumn());
        assertNull(helper.getFunction());
    }

    public void testRequestWithSortParams() {
        Map<String, String> queryParams = new HashMap<>();
        // Start index and page size
        queryParams.put("p", "5");
        queryParams.put("ps", "10");
        // Sort direction and column
        queryParams.put("s", "1");
        queryParams.put("sc", "mysortproperty");

        PageControlHelper helper = new PageControlHelper(SparkTestUtils.createMockRequestWithParams(REQUEST_URL,
                queryParams), "mydefaultproperty");

        assertEquals(5, helper.getStart());
        assertEquals(10, helper.getPageSize());
        assertNull(helper.getQuery());
        assertNull(helper.getQueryColumn());
        assertEquals("1", helper.getSortDirection());
        assertEquals("mysortproperty", helper.getSortColumn());
        assertNull(helper.getFunction());
    }

    public void testRequestWithFunctionParam() {
        Map<String, String> queryParams = new HashMap<>();
        // Function
        queryParams.put("f", "myfunction");

        PageControlHelper helper = new PageControlHelper(SparkTestUtils.createMockRequestWithParams(REQUEST_URL,
                queryParams), "mydefaultproperty");

        assertEquals(1, helper.getStart());
        assertEquals(0, helper.getPageSize());
        assertNull(helper.getQuery());
        assertNull(helper.getQueryColumn());
        assertNull(helper.getSortDirection());
        assertNull(helper.getSortColumn());
        assertEquals("myfunction", helper.getFunction());
    }

    public void testRequestWithDefaultFilterColumn() {
        Map<String, String> queryParams = new HashMap<>();
        // Start index and page size
        queryParams.put("p", "5");
        queryParams.put("ps", "10");
        // Filter string
        queryParams.put("q", "mystring");

        PageControlHelper helper = new PageControlHelper(SparkTestUtils.createMockRequestWithParams(REQUEST_URL,
                queryParams), "mydefaultproperty");

        assertEquals(5, helper.getStart());
        assertEquals(10, helper.getPageSize());
        assertEquals("mystring", helper.getQuery());
        assertEquals("mydefaultproperty", helper.getQueryColumn());
    }

    public void testPageControlWithNoParams() {
        PageControlHelper helper = new PageControlHelper(SparkTestUtils.createMockRequest(REQUEST_URL));
        PageControl pc = helper.getPageControl();

        assertEquals(1, pc.getStart());
        assertEquals(0, pc.getEnd());
        assertFalse(pc.hasFilter());
        assertNull(pc.getFilterData());
        assertNull(pc.getFilterColumn());
    }

    public void testPageControlWithPageParams() {
        Map<String, String> queryParams = new HashMap<>();
        // Start index and page size
        queryParams.put("p", "5");
        queryParams.put("ps", "10");

        PageControlHelper helper = new PageControlHelper(SparkTestUtils.createMockRequestWithParams(REQUEST_URL,
                queryParams), "mydefaultproperty");
        PageControl pc = helper.getPageControl();

        assertEquals(5, pc.getStart());
        assertEquals(14, pc.getEnd());
        assertFalse(pc.hasFilter());
        assertNull(pc.getFilterData());
        assertNull(pc.getFilterColumn());
    }

    public void testPageControlWithFilterParams() {
        Map<String, String> queryParams = new HashMap<>();
        // Start index and page size
        queryParams.put("p", "5");
        queryParams.put("ps", "10");
        // Filter string and column
        queryParams.put("q", "mystring");
        queryParams.put("qc", "myproperty");

        PageControlHelper helper = new PageControlHelper(SparkTestUtils.createMockRequestWithParams(REQUEST_URL,
                queryParams), "mydefaultproperty");
        PageControl pc = helper.getPageControl();

        assertEquals(5, pc.getStart());
        assertEquals(14, pc.getEnd());
        assertTrue(pc.hasFilter());
        assertEquals("mystring", pc.getFilterData());
        assertEquals("myproperty", pc.getFilterColumn());
    }

    public class PagedDataItem {
        private int firstProperty;
        private String secondProperty;

        PagedDataItem(int firstPropertyIn, String secondPropertyIn) {
            this.firstProperty = firstPropertyIn;
            this.secondProperty = secondPropertyIn;
        }

        public int getFirstProperty() {
            return firstProperty;
        }

        public String getSecondProperty() {
            return secondProperty;
        }
    }

    public void testApplySort() {
        Map<String, String> queryParams = new HashMap<>();
        // Start index and page size
        queryParams.put("p", "5");
        queryParams.put("ps", "10");

        PageControlHelper helper = new PageControlHelper(SparkTestUtils.createMockRequestWithParams(REQUEST_URL,
                queryParams), "mydefaultproperty");

        List<PagedDataItem> unsortedList = Arrays.asList(
                new PagedDataItem(2, "two"),
                new PagedDataItem(1, "one"),
                new PagedDataItem(3, "three"));

        List<PagedDataItem> listToSort = new ArrayList<>(unsortedList);
        helper.applySort(listToSort);

        // No sorting should be performed unless the sort column is specified
        assertEquals(unsortedList, listToSort);

        // Sort column (number)
        queryParams.put("sc", "firstProperty");
        helper = new PageControlHelper(SparkTestUtils.createMockRequestWithParams(REQUEST_URL, queryParams),
                "mydefaultproperty");
        helper.applySort(listToSort);

        // Should be sorted in ascending order by default
        assertEquals(1, listToSort.get(0).firstProperty);
        assertEquals(2, listToSort.get(1).firstProperty);
        assertEquals(3, listToSort.get(2).firstProperty);

        // Sort direction (descending) and column (alpha)
        queryParams.put("s", "-1");
        queryParams.put("sc", "secondProperty");
        helper = new PageControlHelper(SparkTestUtils.createMockRequestWithParams(REQUEST_URL, queryParams),
                "mydefaultproperty");
        listToSort = new ArrayList<>(unsortedList);
        helper.applySort(listToSort);

        assertEquals("two", listToSort.get(0).secondProperty);
        assertEquals("three", listToSort.get(1).secondProperty);
        assertEquals("one", listToSort.get(2).secondProperty);
    }

    public void testProcessPageControl() {
        DataResult<PagedDataItem> testData = new DataResult<>(Arrays.asList(
                new PagedDataItem(1, "angel"),
                new PagedDataItem(2, "apple"),
                new PagedDataItem(3, "orange"),
                new PagedDataItem(4, "ranger")
        ));

        Map<String, String> queryParams = new HashMap<>();
        // Start index and page size
        queryParams.put("p", "2");
        queryParams.put("ps", "2");

        PageControlHelper helper = new PageControlHelper(SparkTestUtils.createMockRequestWithParams(REQUEST_URL,
                queryParams), "mydefaultproperty");

        DataResult<PagedDataItem> result = helper.processPageControl(testData);

        assertEquals(2, result.size());
        assertEquals(4, result.getTotalSize());
        assertEquals(2, result.getStart());
        assertEquals(3, result.getEnd());
        assertEquals("apple", result.get(0).secondProperty);
        assertEquals("orange", result.get(1).secondProperty);
    }

    public void testProcessPageControlFiltered() {
        DataResult<PagedDataItem> testData = new DataResult<>(Arrays.asList(
                new PagedDataItem(1, "angel"),
                new PagedDataItem(2, "apple"),
                new PagedDataItem(3, "orange"),
                new PagedDataItem(4, "ranger")
        ));

        Map<String, String> queryParams = new HashMap<>();
        // Start index and page size
        queryParams.put("p", "2");
        queryParams.put("ps", "2");
        // Filter string and column
        queryParams.put("q", "ang");
        queryParams.put("qc", "secondProperty");

        PageControlHelper helper = new PageControlHelper(SparkTestUtils.createMockRequestWithParams(REQUEST_URL,
                queryParams), "mydefaultproperty");

        DataResult<PagedDataItem> result = helper.processPageControl(testData);

        assertEquals(2, result.size());
        assertEquals(3, result.getTotalSize());
        assertEquals(2, result.getStart());
        assertEquals(3, result.getEnd());
        assertEquals("orange", result.get(0).secondProperty);
        assertEquals("ranger", result.get(1).secondProperty);
    }
}
