/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.manager;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.frontend.listview.ListControl;
import com.redhat.rhn.frontend.listview.PageControl;

import java.util.Map;

/**
 *  BaseManager - Class that contains common functionality for our Lister/Manager
 *  classes.  The most often used method is makeDataResult() that
 *  executes a DataSource (our XML sourced SQL query classes) query
 *  and formats it into a DataResult object used by our UI layer for
 *  viewing.
 */
public abstract class BaseManager {

    protected BaseManager() {
        // putting this in to appease checkstyle.
        // if we choose to make the Managers actual
        // singletons, we should probably make this class
        // abstract and add a protected constructor.
    }

    /**
     * Returns a DataResult for the given SelectMode bounded
     * by the values of the PageControl.
     * @param <T> the DataResults type
     * @param queryParams Named parameters for the driving query.
     * @param elabParams Named parameters for the elaboration query.
     * @param m datasource SelectMode.
     * @return resulting DataResult bounded by the values of the
     * PageControl.
     */
    protected static <T> DataResult<T> makeDataResult(Map<String, Object> queryParams,
                                                      Map<String, Object> elabParams, SelectMode m) {
        // execute the driving query to get the initial data set.
        DataResult<T> dr = m.execute(queryParams);
        dr.setTotalSize(dr.size());
        return processPageControl(dr, null, elabParams);
    }

    /**
     * Returns a DataResult for the given SelectMode bounded
     * by the values of the PageControl.
     * @param <T> the DataResults type
     * @param queryParams Named parameters for the driving query.
     * @param elabParams Named parameters for the elaboration query.
     * @param pc Page Control boundary definition.
     * @param m datasource SelectMode.
     * @return resulting DataResult bounded by the values of the
     * PageControl.
     */
    protected static <T> DataResult<T> makeDataResult(Map<String, Object> queryParams,
                                                      Map<String, Object> elabParams, PageControl pc, SelectMode m) {
        // execute the driving query to get the initial data set.
        DataResult<T> dr = m.execute(queryParams);
        dr.setTotalSize(dr.size());
        dr = processPageControl(dr, pc, elabParams);
        return dr;
    }

    /**
     * Returns a DataResult for the given SelectMode bounded
     * by the values of the ListControl.
     * @param <T> the DataResults type
     * @param queryParams Named parameters for the driving query.
     * @param elabParams Named parameters for the elaboration query.
     * @param lc ListControl filtering definition
     * @param m datasource SelectMode.
     * @return resulting DataResult bounded by the values of the PageControl.
     */
    protected static <T> DataResult<T> makeDataResult(Map<String, Object> queryParams,
                                                      Map<String, Object> elabParams, ListControl lc,
                                                      SelectMode m) {
        // execute the driving query to get the initial data set.
        DataResult<T> dr = m.execute(queryParams);
        dr.setTotalSize(dr.size());
        return processListControl(dr, lc, elabParams);
    }

    /**
     * Returns a DataResult for the given SelectMode with no bounds.  This
     * can be usefull if you want a list without pagination controls.
     *
     * @param <T> the DataResults type
     * @param queryParams Named parameters for the driving query.
     * @param elabParams Named parameters for the elaboration query.
     * @param m datasource SelectMode.
     * @return resulting DataResult bounded by the values of the
     * PageControl.
     */
    protected static <T> DataResult<T> makeDataResultNoPagination(
            Map<String, Object> queryParams,
            Map<String, Object> elabParams, SelectMode m) {
        // execute the driving query to get the initial data set.
        DataResult<T> dr = makeDataResult(queryParams, elabParams, null, m);
        dr.setStart(1);
        dr.setEnd(dr.getTotalSize());
        return dr;
    }

    /**
     * Process the PageControl against the DataResult. Returns an
     * <strong>unelaborated</strong> list if PageControl is null.
     * @param dr the DataResult
     * @param elabParams Named parameters for the elaboration query.
     * @param pc Page Control boundary definition.
     * @return DataResult modified (filtered) by the PageControl
     */
    protected static <T> DataResult<T> processPageControl(DataResult<T> dr,
                                            PageControl pc,
                                            Map<String, Object> elabParams) {
        if (elabParams != null) {
            dr.setElaborationParams(elabParams);
        }

        if (pc != null) {
            dr.setFilter(pc.hasFilter());
            if (pc.hasFilter()) {
                pc.filterData(dr);
                //reset the total size because filtering removes some
                dr.setTotalSize(dr.size());
            }

            // If we are filtering the content, _don't_ show the alphabar.
            // This matches what the perl code does.  If we want to show a
            // smaller alphabar, just remove the if statement.
            if (pc.getFilterData() == null || pc.getFilterData().equals("")) {
                if (pc.hasIndex()) {
                    dr.setIndex(pc.createIndex(dr));
                }
            }

            // now use the PageControl to limit the list to the
            // selected region.
            dr = dr.subList(pc.getStart() - 1, pc.getEnd());

            //elaborate the data result to get the detailed information.
            if (elabParams != null) {
                dr.elaborate(elabParams);
            }
        }

        return dr;

    }

    /**
     * Process the ListControl against the DataResult. The method
     * does not limit the number of results, unlike PageControl, and
     * simply provides filtering. Returns an <strong>unelaborated</strong>
     * list if ListControl is null.
     * @param dr the DataResult
     * @param elabParams Named parameters for the elaboration query.
     * @param lc ListControl filtering definition.
     * @return DataResult modified (filtered) by the PageControl
     */
    protected static <T> DataResult<T> processListControl(DataResult<T> dr,
                                            ListControl lc,
                                            Map<String, Object> elabParams) {
        if (elabParams != null) {
            dr.setElaborationParams(elabParams);
        }

        if (lc != null) {
            dr.setFilter(lc.hasFilter());
            if (lc.hasFilter()) {
                lc.filterData(dr);
                //reset the total size because filtering removes some
                dr.setTotalSize(dr.size());
            }

            // If we are filtering the content, _don't_ show the alphabar.
            // This matches what the perl code does.  If we want to show a
            // smaller alphabar, just remove the if statement.
            if ((lc.getFilterData() == null || lc.getFilterData().equals("")) && lc.hasIndex()) {
                dr.setIndex(lc.createIndex(dr));
            }

            //elaborate the data result to get the detailed information.
            dr.elaborate(elabParams);
        }
        return dr;
    }
}
