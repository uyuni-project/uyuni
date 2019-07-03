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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Types;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.redhat.rhn.common.db.datasource.CallableMode;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.util.DateFormatTransformer;
import com.redhat.rhn.domain.action.scap.ScapAction;
import com.redhat.rhn.domain.audit.ScapFactory;
import com.redhat.rhn.domain.audit.XccdfBenchmark;
import com.redhat.rhn.domain.audit.XccdfIdent;
import com.redhat.rhn.domain.audit.XccdfProfile;
import com.redhat.rhn.domain.audit.XccdfRuleResult;
import com.redhat.rhn.domain.audit.XccdfRuleResultType;
import com.redhat.rhn.domain.audit.XccdfTestResult;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.XccdfIdentDto;
import com.redhat.rhn.frontend.dto.XccdfRuleResultDto;
import com.redhat.rhn.frontend.dto.XccdfTestResultDto;
import com.redhat.rhn.manager.BaseManager;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.audit.scap.file.ScapFileManager;
import com.redhat.rhn.manager.audit.scap.xml.BenchmarkResume;
import com.redhat.rhn.manager.audit.scap.xml.Profile;
import com.redhat.rhn.manager.audit.scap.xml.TestResult;
import com.redhat.rhn.manager.audit.scap.xml.TestResultRuleResult;
import com.redhat.rhn.manager.audit.scap.xml.TestResultRuleResultIdent;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.log4j.Logger;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.transform.RegistryMatcher;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * ScapManager
 * @version $Rev$
 */
public class ScapManager extends BaseManager {

    private static Logger log = Logger.getLogger(ScapManager.class);

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
     * Calls the db function {@code lookup_xccdf_ident} to get or create
     * a new entry in {@code rhnXccdfIdent} with the given identifier.
     * Also creates the {@code system} if it's not yet in the db.
     * @param xccdfSystem the XCCDF system
     * @param xccdfIdentifier the XCCDF ident
     * @return the id of the existing or newly created XCCDF ident
     */
    public static long lookupIdent(String xccdfSystem, String xccdfIdentifier) {
        CallableMode m = ModeFactory.getCallableMode("scap_queries",
                "lookup_xccdf_ident");
        Map<String, Object> inParams = new HashMap<>();
        inParams.put("system_in", xccdfSystem);
        inParams.put("identifier_in", xccdfIdentifier);
        Map<String, Integer> outParams = new HashMap<>();
        outParams.put("ident_id", Types.NUMERIC);
        Map out = m.execute(inParams, outParams);
        return (Long)out.get("ident_id");
    }

    /**
     * Calls the db function {@code lookup_xccdf_benchmark} to get or create
     * a new entry in {@code rhnXccdfBenchmark} with the given identifier
     * and version.
     * @param identifier benchmark identifier
     * @param version benchmark version
     * @return the id of the existing or newly created XCCDF benchmark
     */
    public static long lookupBenchmark(String identifier, String version) {
        CallableMode m = ModeFactory.getCallableMode("scap_queries",
                "lookup_xccdf_benchmark");
        Map<String, Object> inParams = new HashMap<>();
        inParams.put("identifier_in", identifier);
        inParams.put("version_in", version);
        Map<String, Integer> outParams = new HashMap<>();
        outParams.put("benchmark_id", Types.NUMERIC);
        Map out = m.execute(inParams, outParams);
        return (Long)out.get("benchmark_id");
    }

    /**
     * Calls the db function {@code lookup_xccdf_profile} to get or create
     * a new entry in {@code rhnXccdfProfile} with the given identifier
     * and title.
     * @param identifier profile identifier
     * @param tile profile title
     * @return the id of the existing or newly created XCCDF profile
     */
    public static long lookupProfile(String identifier, String tile) {
        CallableMode m = ModeFactory.getCallableMode("scap_queries",
                "lookup_xccdf_profile");
        Map<String, Object> inParams = new HashMap<>();
        inParams.put("identifier_in", identifier);
        inParams.put("title_in", tile);
        Map<String, Integer> outParams = new HashMap<>();
        outParams.put("profile_id", Types.NUMERIC);
        Map out = m.execute(inParams, outParams);
        return (Long)out.get("profile_id");
    }

    /**
     * Schedule scap.xccdf_eval action for systems in user's SSM.
     * @param scheduler user which commits the schedule
     * @param path path to xccdf document on systems file system
     * @param parameters additional parameters for xccdf scan
     * @param earliest time of earliest action occurence
     * @return the newly created ScapAction
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    public static ScapAction scheduleXccdfEvalInSsm(User scheduler, String path,
            String parameters, Date earliest) throws TaskomaticApiException {
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
     * @return true if the user can access the TestResult, false otherwise.
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
        Long deleted = 0L;
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

    /**
     * Evaluate the XCCDF results report and store the results in the db.
     * @param server the server
     * @param action the action
     * @param returnCode openscap return code
     * @param errors openscap errors output
     * @param resultsXml the XCCDF file
     * @param resumeXsl the XSL used to generate the intermediary XML
     * @return the {@link XccdfTestResult} that was saved in the db
     * @throws IOException if the input files could not be read
     */
    public static XccdfTestResult xccdfEval(Server server, ScapAction action,
                                            int returnCode, String errors,
                                            InputStream resultsXml, File resumeXsl)
            throws IOException {
        // Transform XML
        File output = File.createTempFile("scap-resume-" + action.getId(), ".xml");
        output.deleteOnExit();
        StreamSource xslStream = new StreamSource(resumeXsl);
        StreamSource in = new StreamSource(resultsXml);
        try (OutputStream resumeOut = new FileOutputStream(output)) {
            StreamResult out = new StreamResult(resumeOut);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer(xslStream);
            transformer.transform(in, out);
        }
        catch (javax.xml.transform.TransformerException e) {
            throw new RuntimeException("XSL transform failed", e);
        }
        try (InputStream resumeIn = new FileInputStream(output)) {
            return xccdfEvalResume(server, action, returnCode, errors, resumeIn);
        }
        finally {
            output.delete();
        }
    }

    /**
     * Evaluate the SCAP results report and store the results in the db.
     * @param server the server
     * @param action the action
     * @param returnCode openscap return code
     * @param errors openscap errors output
     * @param resumeXml the SCAP report in intermediary XML format
     * @return the {@link XccdfTestResult} that was saved in the db
     */
    public static XccdfTestResult xccdfEvalResume(Server server, ScapAction action,
                                                  int returnCode, String errors,
                                                  InputStream resumeXml) {
        ScapFactory.clearTestResult(server.getId(), action.getId());
        try {
            BenchmarkResume resume = createXmlPersister()
                    .read(BenchmarkResume.class, resumeXml);
            Profile profile = Optional.ofNullable(resume.getProfile()).orElse(
                    new Profile("None",
                            "No profile selected. Using defaults.",
                            ""));
            TestResult testResults = resume.getTestResult();
            if (testResults == null) {
                log.error("Scap report misses profile or testresult element");
                throw new RuntimeException(
                        "Scap report misses profile or testresult element");
            }
            MutableBoolean truncated = new MutableBoolean();
            XccdfTestResult result = new XccdfTestResult();
            result.setServer(server);
            result.setIdentifier(testResults.getId());
            result.setScapActionDetails(action.getScapActionDetails());

            XccdfBenchmark xccdfBenchmark =
                    getOrCreateBenchmark(resume, truncated);
            result.setBenchmark(xccdfBenchmark);
            XccdfProfile xccdfProfile =
                    getOrCreateProfile(profile, truncated);
            result.setProfile(xccdfProfile);
            result.setStartTime(testResults.getStartTime());
            result.setEndTime(testResults.getEndTime());

            processRuleResult(result, testResults.getPass(), "pass", truncated);
            processRuleResult(result, testResults.getFail(), "fail", truncated);
            processRuleResult(result, testResults.getError(), "error", truncated);
            processRuleResult(result, testResults.getUnknown(), "unknown", truncated);
            processRuleResult(result, testResults.getNotapplicable(),
                    "notapplicable", truncated);
            processRuleResult(result, testResults.getNotchecked(),
                    "notchecked", truncated);
            processRuleResult(result, testResults.getNotselected(),
                    "notselected", truncated);
            processRuleResult(result, testResults.getInformational(),
                    "informational", truncated);
            processRuleResult(result, testResults.getFixed(), "fixed", truncated);

            String errs = errors;
            if (returnCode != 0) {
                errs += String.format("xccdf_eval: oscap tool returned %d\n", returnCode);
            }
            if (truncated.isTrue()) {
                errs = errors +
                    "\nSome text strings were truncated when saving to the database.";
            }
            result.setErrors(HibernateFactory.stringToByteArray(errs));
            ScapFactory.save(result);

            return result;
        }
        catch (Exception e) {
            log.error("Scap xccdf eval failed", e);
            throw new RuntimeException("Scap xccdf eval failed", e);
        }
    }

    private static void processRuleResult(XccdfTestResult testResult,
                                          List<TestResultRuleResult> ruleResults,
                                          String label,
                                          MutableBoolean truncated) {
        for (TestResultRuleResult rr : ruleResults) {
            XccdfRuleResult ruleResult = new XccdfRuleResult();
            ruleResult.setTestResult(testResult);
            testResult.getResults().add(ruleResult);
            Optional<XccdfRuleResultType> resultType
                    = ScapFactory.lookupRuleResultType(label);
            ruleResult.setResultType(
                    resultType.orElseThrow(() ->
                            new RuntimeException("no xccdf result type found for label=" +
                                    label)));
            ruleResult.getIdents().add(
                    getOrCreateIdent("#IDREF#", truncate(rr.getId(), 255, truncated)));
            if (rr.getIdents() != null) {
                for (TestResultRuleResultIdent rrIdent : rr.getIdents()) {
                    String text = truncate(rrIdent.getText(), 255, truncated);
                    if (StringUtils.isEmpty(text)) {
                        continue;
                    }
                    ruleResult.getIdents().add(
                            getOrCreateIdent(
                                    rrIdent.getSystem(),
                                    text));
                }

            }
        }
    }

    private static XccdfProfile getOrCreateProfile(Profile profile,
                                                   MutableBoolean truncated) {
        long profileId = lookupProfile(profile.getId(),
                truncate(profile.getTitle(),
                        120, truncated));
        return ScapFactory.lookupProfileById(profileId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Xccdf benchmark not found in db identifier=" +
                                        profile.getId() +
                                        ", version=" +
                                        profile.getTitle()));
    }

    private static XccdfBenchmark getOrCreateBenchmark(BenchmarkResume resume,
                                                       MutableBoolean truncated) {
        long benchId = lookupBenchmark(truncate(resume.getId(), 120, truncated),
                truncate(resume.getVersion(), 80, truncated));
        return ScapFactory.lookupBenchmarkById(benchId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Xccdf benchmark not found in db identifier=" +
                                        resume.getId() +
                                        ", version=" +
                                        resume.getVersion()));
    }

    private static XccdfIdent getOrCreateIdent(String system, String ruleIdentifier) {
        long identId = lookupIdent(system, ruleIdentifier);
        return ScapFactory.lookupIdentById(identId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Xccdf ident not found in db identifier=" +
                                        ruleIdentifier +
                                        ", system=" +
                                        system));
    }

    private static Persister createXmlPersister() {
        RegistryMatcher registryMatcher = new RegistryMatcher();
        registryMatcher.bind(Date.class, DateFormatTransformer.createXmlDateTransformer());
        return new Persister(registryMatcher);
    }

    private static String truncate(String string, int maxLen, MutableBoolean truncated) {
        if (string != null && string.length() > maxLen) {
            truncated.setValue(true);
            return string.substring(0, maxLen - 3) + "...";
        }
        return string;
    }

}
