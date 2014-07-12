/**
 * Copyright (c) 2012--2014 Red Hat, Inc.
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
package com.redhat.rhn.manager.audit;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.domain.action.scap.ScapAction;
import com.redhat.rhn.domain.audit.ScapFactory;
import com.redhat.rhn.domain.audit.XccdfTestResult;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.XccdfIdentDto;
import com.redhat.rhn.frontend.dto.XccdfRuleResultDto;
import com.redhat.rhn.frontend.dto.XccdfTestResultDto;
import com.redhat.rhn.manager.BaseManager;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.audit.scap.file.ScapFileManager;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;

/**
 * ScapManager
 * @version $Rev$
 */
public class ScapManager extends BaseManager {

    private static final List<String> SEARCH_TERM_PRECEDENCE = Arrays.asList(
            "slabel", "start", "end", "result");

    /**
     * Returns the given system is scap enabled.
     * @param server The system for which to seach scap capability
     * @param user The user requesting to view the system
     * @return true if the system is scap capable
     */
    public static boolean isScapEnabled(Server server, User user) {
        SelectMode m = ModeFactory.getMode("scap_queries",
                "system_scap_enabled_check");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("user_id", user.getId());
        params.put("sid", server.getId());
        DataResult<Map<String, ? extends Number>> dr = m.execute(params);
        return dr.get(0).get("count").intValue() > 0;
    }

    /**
     * Show brief results of all scans accessible by user.
     * Sorted by date, descending.
     * @param user The user requesting.
     * @return The list of scan results
     */
    public static DataResult latestTestResultByUser(User user) {
        SelectMode m = ModeFactory.getMode("scap_queries",
                "latest_testresults_by_user");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("user_id", user.getId());
        return makeDataResult(params, new HashMap(), null, m);
    }

    /**
     * Show brief results of all scans accessible by user.
     * Sorted by date, descending.
     * @param user The user requesting the data.
     * @param systemId The id of system
     * @return The list of scan results.
     */
    public static List<XccdfTestResultDto> latestTestResultByServerId(
            User user, Long systemId) {
        SelectMode m = ModeFactory.getMode("scap_queries",
                "latest_testresults_by_server");
        Map<String, Long> params = new HashMap<String, Long>();
        params.put("user_id", user.getId());
        params.put("sid", systemId);
        return m.execute(params);
    }

    /**
     * Show brief results of all scans for given system
     * @param server The system for which to search
     * @return The list of scan results in brief
     */
    public static DataResult allScans(Server server) {
        SelectMode m = ModeFactory.getMode("scap_queries",
                "show_system_scans");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("sid", server.getId());
        return makeDataResult(params, new HashMap(), null, m);
    }

    /**
     * Show brief results of scans in given set
     * @param user The user owning the set
     * @param setLabel The label of the set
     * @return The list of scan results in brief
     */
    public static DataResult<XccdfTestResultDto> scansInSet(User user, String setLabel) {
        SelectMode m = ModeFactory.getMode("scap_queries",
                "scans_in_set");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("user_id", user.getId());
        params.put("set_label", setLabel);
        return makeDataResult(params, new HashMap<String, Object>(), null, m);
    }

    /**
     * Show xccdf:rule-result results for given test
     * @param testResultId of XccdfTestResult of the test for which to search
     * @return the list of rule-results
     */
    public static List<XccdfRuleResultDto> ruleResultsPerScan(Long testResultId) {
        SelectMode m = ModeFactory.getMode("scap_queries",
                "show_ruleresults");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("xid", testResultId);
        return m.execute(params);
    }

    /**
     * Get xccdf:rule-result by id
     * @param ruleResultId of the XccdfRuleResult
     * @return the result
     */
    public static XccdfRuleResultDto ruleResultById(Long ruleResultId) {
        SelectMode m = ModeFactory.getMode("scap_queries",
                "ruleresult_by_id");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("rr_id", ruleResultId);
        List<XccdfRuleResultDto> result = m.execute(params);
        return result.isEmpty() ? null : result.get(0);
    }

    /**
     * Get a TestResult with metadata similar to the given.
     * Which has been evaluated on the same machine just before the given.
     * So it makes a sence to compare these two.
     * @param testResultId referential TestResult
     * @return result or null (if not any)
     */
    public static Long previousComparableTestResult(Long testResultId) {
        SelectMode m = ModeFactory.getMode("scap_queries",
                "previous_comparable_tr");
        Map<String, Long> params = new HashMap<String, Long>();
        params.put("xid", testResultId);
        DataResult<Map> toReturn = m.execute(params);
        return (Long) toReturn.get(0).get("xid");
    }

    /**
     * Get xccdf:rule-result-s or xccdf:TestResult-s by ident's ids
     * @param inParams direct parameters for query.
     * user_id is the only compulsory
     * @param identIds list of xccdf:ident ids
     * @param returnTestResults what to return
     * (true - list of testresults, false - list of rule-results)
     * @return the result
     */
    public static DataResult searchByIdentIds(Map inParams,
            List<Long> identIds, boolean returnTestResults) {
        String modeName = (returnTestResults ? "t" : "r") + "r_by_idents";
        for (String term : SEARCH_TERM_PRECEDENCE) {
            if (inParams.containsKey(term)) {
                modeName += "_" + term;
            }
        }
        SelectMode m = ModeFactory.getMode("scap_queries", modeName);
        DataResult dr = m.execute(inParams, identIds);
        if (returnTestResults) {
            dr.setTotalSize(dr.size());
            dr = processPageControl(dr, null, new HashMap());
        }
        return dr;
    }

    /**
     * Show xccdf:ident results for given rule-result
     * @param ruleResultId of XccdfRuleResultDto
     * @return the list of idents
     */
    public static List<XccdfIdentDto> identsPerRuleResult(Long ruleResultId) {
        SelectMode m = ModeFactory.getMode("scap_queries",
                "idents_per_ruleresult");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("rr_id", ruleResultId);
        return m.execute(params);
    }

    /**
     * Show scap capable systems which are currently in SSM
     * @param scheduler user requesting the systems
     * @return the list of systems in SSM
     */
    public static DataResult scapCapableSystemsInSsm(User scheduler) {
        SelectMode m = ModeFactory.getMode("scap_queries",
                "scap_capable_systems_in_set");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("user_id", scheduler.getId());
        params.put("set_label", RhnSetDecl.SYSTEMS.getLabel());
        return m.execute(params);
    }

    /**
     * Show systems in SSM and their true/false scap capability
     * @param scheduler user requesting the systems
     * @return the list of systems in SSM
     */
    public static DataResult systemsInSsmAndScapCapability(User scheduler) {
        SelectMode m = ModeFactory.getMode("scap_queries",
                "systems_in_set_and_scap_capability");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("user_id", scheduler.getId());
        params.put("set_label", RhnSetDecl.SYSTEMS.getLabel());
        return m.execute(params);
    }

    /**
     * Schedule scap.xccdf_eval action for systems in user's SSM.
     * @param scheduler user which commits the schedule
     * @param path path to xccdf document on systems file system
     * @param parameters additional parameters for xccdf scan
     * @param earliest time of earliest action occurence
     * @return the newly created ScapAction
     */
    public static ScapAction scheduleXccdfEvalInSsm(User scheduler, String path,
            String parameters, Date earliest) {
        HashSet<Long> systemIds = idsInDataResultToSet(scapCapableSystemsInSsm(scheduler));
        return ActionManager.scheduleXccdfEval(
                scheduler, systemIds, path, parameters, earliest);
    }

    /**
     * Check if the user has permission to see the XCCDF scan.
     * @param user User being checked.
     * @param testResultId ID of XCCDF scan being checked.
     * @throws LookupException if user cannot access the scan.
     */
    public static void ensureAvailableToUser(User user, Long testResultId) {
        if (!isAvailableToUser(user, testResultId)) {
            throw new LookupException("Could not find XCCDF scan " +
                    testResultId + " for user " + user.getId());
        }
    }

    /**
     * Return list of possible results of xccdf:rule evaluation
     * @return the result
     */
    public static List<Map<String, String>> ruleResultTypeLabels() {
        return ModeFactory.getMode("scap_queries", "result_type_labels").execute();
    }

    /**
     * Checks if the user has permission to see the XCCDF scan.
     * @param user User being checked.
     * @param testResultId ID of the XCCDF scan being checked.
     * @return true - when available
     * @retutn true if the user can access the TestResult, false otherwise.
     */
    public static boolean isAvailableToUser(User user, Long testResultId) {
        SelectMode m = ModeFactory.getMode("scap_queries",
                "is_available_to_user");
        Map<String, Long> params = new HashMap<String, Long>();
        params.put("user_id", user.getId());
        params.put("xid", testResultId);
        return m.execute(params).size() >= 1;
    }

    /**
     * Delete XccdfTestResults specified by set.
     * @param set Set of TestResults to delete
     * @return a number of successfully removed testResult
     */
    public static Long deleteScansInSet(Iterable<XccdfTestResultDto> set) {
        Long deleted = new Long(0);
        for (XccdfTestResultDto dto : set) {
            if (deleteScan(dto.getXid())) {
                deleted++;
            }
        }
        return deleted;
    }

    /**
     * Delete given XccdfTestResult together with stored files
     * @param xid ID of TestResult to delete
     * @return true if the deletion was successfull
     */
    public static Boolean deleteScan(Long xid) {
        XccdfTestResult tr = ScapFactory.lookupTestResultById(xid);
        if (tr.getDeletable()) {
            ScapFileManager.deleteFilesForTestResult(tr);
            ScapFactory.delete(tr);
            return true;
        }
        return false;
    }

    private static HashSet<Long> idsInDataResultToSet(DataResult dataIn) {
        HashSet<Long> result = new HashSet<Long>();
        for (Map<String, Long> map : (List<Map<String, Long>>) dataIn) {
            result.add(map.get("id"));
        }
        return result;
    }

}
