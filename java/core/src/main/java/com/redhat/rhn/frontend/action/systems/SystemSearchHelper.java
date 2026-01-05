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
package com.redhat.rhn.frontend.action.systems;


import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.session.WebSession;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.SystemOverview;
import com.redhat.rhn.frontend.dto.SystemSearchResult;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.session.SessionManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.user.UserManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

import redstone.xmlrpc.XmlRpcClient;
import redstone.xmlrpc.XmlRpcFault;

/**
 * SystemSearchHelper
 * This will make calls to the XMLRPC Search Server
 */
public class SystemSearchHelper {
    private static Logger log = LogManager.getLogger(SystemSearchHelper.class);

    protected SystemSearchHelper() { }

    private static final String SCORE = "score";
    private static final String MATCHING_FIELD = "matchingField";
    private static final String MATCHING_FIELD_VALUE = "matchingFieldValue";


    /**
     * Returns a DataResult of SystemSearchResults which are based on the user's search
     * criteria
     * @param ctx request context
     * @param searchString string to search on
     * @param viewMode what field to search
     * @param invertResults whether the results should be inverted
     * @param whereToSearch whether to search through all user visible systems or the
     *        systems selected in the SSM
     * @param isFineGrained fine grained search
     * @return DataResult of SystemSearchResults based on user's search criteria
     * @throws XmlRpcFault on xmlrpc error
     * @throws MalformedURLException on bad search server address
     */
    public static DataResult<SystemSearchResult> systemSearch(RequestContext ctx,
                                          String searchString,
                                          String viewMode,
                                          Boolean invertResults,
                                          String whereToSearch, Boolean isFineGrained)
        throws XmlRpcFault, MalformedURLException {
        WebSession session = ctx.getWebSession();
        String key = session.getKey();
        return systemSearch(key, searchString, viewMode, invertResults, whereToSearch,
                isFineGrained);
    }

    /**
     * Returns a DataResult of SystemSearchResults which are based on the user's search
     * criteria
     * @param sessionKey key for this session
     * @param searchString string to search on
     * @param viewMode what field to search
     * @param invertResults whether the results should be inverted
     * @param whereToSearch whether to search through all user visible systems or the
     *        systems selected in the SSM
     * @param isFineGrained fine grained search
     * @return DataResult of SystemSearchResults based on user's search criteria
     * @throws XmlRpcFault on xmlrpc error
     * @throws MalformedURLException on bad search server address
     */
    public static DataResult<SystemSearchResult> systemSearch(String sessionKey,
            String searchString,
            String viewMode,
            boolean invertResults,
            String whereToSearch, Boolean isFineGrained)
        throws XmlRpcFault, MalformedURLException {

        WebSession session = SessionManager.loadSession(sessionKey);
        Long sessionId = session.getId();
        User user = session.getUser();

        //Make sure there was a valid user in the session. If not, the session is invalid.
        if (user == null) {
            throw new LookupException("Could not find a valid user for session with key: " +
                                      sessionKey);
        }

        /*
         * Determine what index to search and form the query
         */
        Map<String, String> params = preprocessSearchString(searchString, viewMode);
        String query = params.get("query");
        String index = params.get("index");
        // Contact the XMLRPC search server and get back the results
        List<Map<String, Object>> results = performSearch(sessionId, index, query, isFineGrained);

        // We need to translate these results into a fleshed out DTO object which can be displayed.by the JSP
        Map<Long, Map<String, Object>> serverIds;
        if (SystemSearchHelperIndex.PACKAGES_INDEX.equalsIndex(index)) {
            serverIds = getResultMapFromPackagesIndex(user, results, viewMode);
        }
        else if (SystemSearchHelperIndex.SERVER_INDEX.equalsIndex(index)) {
            serverIds = getResultMapFromServerIndex(results);
        }
        else if (SystemSearchHelperIndex.HARDWARE_DEVICE_INDEX.equalsIndex(index)) {
            serverIds = getResultMapFromHardwareDeviceIndex(results);
        }
        else if (SystemSearchHelperIndex.SNAPSHOT_TAG_INDEX.equalsIndex(index)) {
            serverIds = getResultMapFromSnapshotTagIndex(results);
        }
        else if (SystemSearchHelperIndex.SERVER_CUSTOM_INFO_INDEX.equalsIndex(index)) {
            serverIds = getResultMapFromServerCustomInfoIndex(results);
        }
        else {
            log.warn("Unknown index: {}", index);
            log.warn("Defaulting to treating this as a {} index", SystemSearchHelperIndex.SERVER_INDEX.getLabel());
            serverIds = getResultMapFromServerIndex(results);
        }
        if (invertResults) {
            serverIds = invertResults(user, serverIds);
        }
        // Assuming we search all systems by default, unless whereToSearch states
        // to use the System Set Manager systems only.  In that case we simply do a
        // filter of returned search results to only return IDs which are in SSM
        if ("system_list".equals(whereToSearch)) {
            filterOutIdsNotInSSM(user, serverIds);
        }
        return processResultMap(user, serverIds, viewMode);
    }

    protected static List<Map<String, Object>> performSearch(Long sessionId, String index, String query,
                Boolean isFineGrained) throws XmlRpcFault, MalformedURLException {

        log.info("Performing system search: index = {}, query = {}", index, query);
        XmlRpcClient client = new XmlRpcClient(
                ConfigDefaults.get().getSearchServerUrl(), true);
        List<Object> args = new ArrayList<>();
        args.add(sessionId.toString());
        args.add(index);
        args.add(query);
        args.add(isFineGrained);
        List<Map<String, Object>> results = (List<Map<String, Object>>)client.invoke("index.search", args);
        if (log.isDebugEnabled()) {
            log.debug("results = [{}]", results);
        }
        if (results.isEmpty()) {
            return Collections.emptyList();
        }
        return results;
    }

    protected static Map<String, String> preprocessSearchString(String searchstring,
                       String mode) {
        StringBuilder buf = new StringBuilder(searchstring.length());
        String[] tokens = searchstring.split(" ");
        for (String s : tokens) {
            if (s.trim().equalsIgnoreCase("AND") ||
                    s.trim().equalsIgnoreCase("OR") ||
                    s.trim().equalsIgnoreCase("NOT")) {
                s = s.toUpperCase();
            }
            else {
                // Escape colons in IPv6 address automatically.
                // (colon is a special lucene character)
                if (SystemSearchHelperType.IP6.equalsMode(mode)) {
                    s = s.replace(":", "\\:");
                }
            }
            buf.append(s);
            buf.append(" ");
        }
        String terms = buf.toString().trim();

        Optional<SystemSearchHelperType> type = SystemSearchHelperType.find(mode);
        String query, index;
        if (type.isPresent()) {
            query = type.get().getQuery(terms);
            index = type.get().getIndexLabel();
        }
        else {
            throw new ValidatorException("Mode: " + mode + " not supported.");
        }
        Map<String, String> retval = new HashMap<>();
        retval.put("query", query);
        retval.put("index", index);
        return retval;
    }

    /**
     * We did a normal package search and got back a List of results for
     * the package name(s), now we correlate that to what systems have those
     * installed, or need them to be updated.
     * @param user The User object
     * @param searchResults The list of results to process
     * @param viewMode the view mode
     * @return server map
     * TODO:  Look into a quicker/more efficient implementation.  This appears to
     * work....but I think it can be become quicker.
     */
    protected static Map<Long, Map<String, Object>> getResultMapFromPackagesIndex(User user,
                List<Map<String, Object>> searchResults, String viewMode) {
        // this is our main result Map which we will return, it's keys
        // represent the list of server Ids this search yielded
        Map<Long, Map<String, Object>> serverMaps = new HashMap<>();
        log.info("Entering getResultMapFromPackagesIndex() searchResults.size() = {}", searchResults.size());

        for (Map<String, Object> result : searchResults) {
            Long pkgId = Long.valueOf((String)result.get("id"));
            List<Long> serverIds = null;
            if (SystemSearchHelperType.INSTALLED_PACKAGES.equalsMode(viewMode)) {
                serverIds = getSystemsByInstalledPackageId(user, pkgId);
            }
            else if (SystemSearchHelperType.NEEDED_PACKAGES.equalsMode(viewMode)) {
                serverIds = getSystemsByNeededPackageId(user, pkgId);
            }
            if (serverIds == null || serverIds.isEmpty()) {
                continue;
            }
            Double currentScore = (Double)result.get(SCORE);
            log.info("Name = {}, Score = {}", result.get("name"), currentScore);

            int countServerIds = serverIds.size();
            serverMaps.putAll(serverIds.stream().distinct().collect(Collectors.toMap(s -> s, s -> {
                    // Create the serverInfo which we will be returning back
                    Package pkg = PackageFactory.lookupByIdAndUser(pkgId, user);
                    if (pkg == null) {
                        log.warn("SystemSearchHelper.getResultMapFromPackagesIndex() problem when looking " +
                                "up package id <{} PackageFactory.lookupByIdAndUser returned null.", pkgId);
                        return null;
                    }
                    log.info("Package {}, id = {}, score = {}, serverIds associated with package = {}",
                            pkg.getNameEvra(), pkgId, currentScore, countServerIds);
                    Map<String, Object> serverInfo = new HashMap<>();
                    serverInfo.put(SCORE, result.get(SCORE));
                    serverInfo.put(MATCHING_FIELD, "packageName");
                    serverInfo.put(MATCHING_FIELD_VALUE, pkg.getNvrea());
                    serverInfo.put("packageName", pkg.getNameEvra());
                    log.debug("created new map for server id: {}, searched with packageName: {} score = {}",
                            s, pkg.getNameEvra(), serverInfo.get(SCORE));
                    return serverInfo;
                })));
        } // end looping over packageId
        return serverMaps;
    }

    protected static Map<Long, Map<String, Object>> getResultMapFromServerIndex(
            List<Map<String, Object>> searchResults) {
        if (log.isDebugEnabled()) {
            log.debug("forming results for: {}", searchResults);
        }
        Map<Long, Map<String, Object>> serverIds = new HashMap<>();
        for (Map<String, Object> result : searchResults) {
            Map<String, Object> serverItem = new HashMap<>();
            serverItem.put("rank", result.get("rank"));
            serverItem.put(SCORE, result.get(SCORE));
            serverItem.put("name", result.get("name"));
            serverItem.put("uuid", result.get("uuid"));
            String matchingField = (String)result.get(MATCHING_FIELD);
            if (matchingField.isEmpty()) {
                matchingField = (String)result.get("name");
            }
            else if ("system_id".compareTo(matchingField) == 0) {
                //system_id was used to allow tokenized searches on id
                //we want to treat it as if 'id' was used for all lookups
                matchingField = "id";
            }
            serverItem.put(MATCHING_FIELD, matchingField);
            serverItem.put(MATCHING_FIELD_VALUE, result.get(MATCHING_FIELD_VALUE));
            if (log.isDebugEnabled()) {
                log.debug("creating new map for system id: {} new map = {}", result.get("id"), serverItem);
            }
            serverIds.put(Long.valueOf((String)result.get("id")), serverItem);
        }
        return serverIds;
    }

    protected static Map<Long, Map<String, Object>> getResultMapFromHardwareDeviceIndex(
            List<Map<String, Object>> searchResults) {
        if (log.isDebugEnabled()) {
            log.debug("forming results for: {}", searchResults);
        }
        Map<Long, Map<String, Object>> serverIds = new HashMap<>();
        for (Map<String, Object> result : searchResults) {
            Long sysId = Long.valueOf((String)result.get("serverId"));
            if (serverIds.containsKey(sysId)) {
                Map<String, Object> priorResult = serverIds.get(sysId);
                Double priorScore = (Double)priorResult.get(SCORE);
                Double thisScore = (Double)result.get(SCORE);
                if (priorScore >= thisScore) {
                    // We only want to capture the best match of a hwdevice for each system
                    continue;
                }
            }

            Map<String, Object> serverItem = new HashMap<>();
            serverItem.put("rank", result.get("rank"));
            serverItem.put(SCORE, result.get(SCORE));
            serverItem.put("name", result.get("name"));
            serverItem.put("hwdeviceId", result.get("id"));
            String matchingField = (String)result.get(MATCHING_FIELD);
            if (matchingField.isEmpty()) {
                matchingField = (String)result.get("name");
            }
            serverItem.put(MATCHING_FIELD, matchingField);
            serverItem.put(MATCHING_FIELD_VALUE, result.get(MATCHING_FIELD_VALUE));
            if (log.isDebugEnabled()) {
                log.debug("creating new map for serverId = {}, hwdevice id: {} new map = {}",
                        result.get("serverId"), result.get("id"), serverItem);
            }
            serverIds.put(sysId, serverItem);
        }
        return serverIds;
    }

    protected static Map<Long, Map<String, Object>> getResultMapFromSnapshotTagIndex(
            List<Map<String, Object>> searchResults) {
        if (log.isDebugEnabled()) {
            log.debug("forming results for: {}", searchResults);
        }
        Map<Long, Map<String, Object>> serverIds = new HashMap<>();
        for (Map<String, Object> result : searchResults) {
            Map<String, Object> serverItem = new HashMap<>();
            serverItem.put("rank", result.get("rank"));
            serverItem.put(SCORE, result.get(SCORE));
            serverItem.put("name", result.get("name"));
            serverItem.put("snapshotId", result.get("snapshotId"));
            String matchingField = (String)result.get(MATCHING_FIELD);
            if (matchingField.isEmpty()) {
                matchingField = (String)result.get("name");
            }
            serverItem.put(MATCHING_FIELD, matchingField);
            serverItem.put(MATCHING_FIELD_VALUE, result.get(MATCHING_FIELD_VALUE));
            if (log.isDebugEnabled()) {
                log.debug("creating new map for serverId = {}, snapshotID: {} new map = {}",
                        result.get("serverId"), result.get("snapshotId"), serverItem);
            }
            serverIds.put(Long.valueOf((String)result.get("serverId")), serverItem);
        }
        return serverIds;
    }

    protected static Map<Long, Map<String, Object>> getResultMapFromServerCustomInfoIndex(
            List<Map<String, Object>> searchResults) {
        if (log.isDebugEnabled()) {
            log.debug("forming results for: {}", searchResults);
        }
        Map<Long, Map<String, Object>> serverIds = new HashMap<>();
        for (Map<String, Object> result : searchResults) {
            Map<String, Object> serverItem = new HashMap<>();
            serverItem.put("rank", result.get("rank"));
            serverItem.put(SCORE, result.get(SCORE));
            serverItem.put("name", result.get("value"));
            serverItem.put("snapshotId", result.get("snapshotId"));
            String matchingField = (String)result.get(MATCHING_FIELD);
            if (matchingField.isEmpty()) {
                matchingField = (String)result.get("value");
            }
            serverItem.put(MATCHING_FIELD, matchingField);
            String matchingFieldValue = (String)result.get(MATCHING_FIELD_VALUE);
            if (matchingFieldValue.isEmpty()) {
                matchingFieldValue = (String)result.get("value");
            }
            serverItem.put(MATCHING_FIELD_VALUE, matchingFieldValue);
            if (log.isDebugEnabled()) {
                log.debug("creating new map for serverId = {}, customValueID: {} new map = {}",
                        result.get("serverId"), result.get("id"), serverItem);
            }
            serverIds.put(Long.valueOf((String)result.get("serverId")), serverItem);
        }
        return serverIds;
    }

    private static void fillSystemSearchResult(Map<String, Object> details, SystemSearchResult sr) {
        String field = (String)details.get(MATCHING_FIELD);
        sr.setMatchingField(field);
        if (details.containsKey("packageName")) {
            sr.setPackageName((String)details.get("packageName"));
        }
        if (details.containsKey("hwdeviceId")) {
            Long hwId = Long.parseLong((String)details.get("hwdeviceId"));
            sr.setHw(SystemManager.getHardwareDeviceById(hwId));
            // we want the matching field to call into the HardwareDeviceDto
            // to return back the value of what matched
            sr.setMatchingField("hw." + field);
        }
        if (details.containsKey("uuid")) {
            sr.setUuid((String)details.get("uuid"));
        }
        if (details.containsKey("rank")) {
            sr.setRank((Integer)details.get("rank"));
        }
        if (details.containsKey(SCORE)) {
            sr.setScore((Double)details.get(SCORE));
        }
        if (details.containsKey(MATCHING_FIELD_VALUE)) {
            sr.setMatchingFieldValue((String)details.get(MATCHING_FIELD_VALUE));
        }
    }

    protected static DataResult<SystemSearchResult> processResultMap(User userIn,
                                                                     Map<Long, Map<String, Object>> serverIds,
                                                                     String viewMode) {
        DataResult<SystemSearchResult> serverList = UserManager.visibleSystemsAsDtoFromList(userIn,
                    new ArrayList<>(serverIds.keySet()));
        if (serverList == null) {
            return null;
        }

        serverList.forEach(sr -> fillSystemSearchResult(serverIds.get(sr.getId()), sr));
        log.debug("sorting server data based on score from lucene search");

        /* RangeQueries return a constant score of 1.0 for anything that matches.
         * Therefore we need to do more work to understand how to best sort results.
         * Sorting will be done based on value for 'matchingFieldValue', this is a best
         * guess from the search server of what field in the document most influenced
         * the result.
         */
        if (SystemSearchHelperType.isSortLowToHighMode(viewMode)) {
            // We want to sort Low to High
            SearchResultMatchedFieldComparator comparator =
                new SearchResultMatchedFieldComparator(serverIds);
            serverList.sort(comparator);
        }
        else if (SystemSearchHelperType.isSortHighToLowMode(viewMode)) {
            // We want to sort High to Low
            SearchResultMatchedFieldComparator comparator =
                new SearchResultMatchedFieldComparator(serverIds, false);
            serverList.sort(comparator);
        }
        else {
            SearchResultScoreComparator scoreComparator =
                new SearchResultScoreComparator(serverIds);
            serverList.sort(scoreComparator);
        }
        log.debug("sorted server data = {}", serverList);
        return serverList;
    }

    protected static List<Long> getSystemsByInstalledPackageId(User user, Long pkgId) {
        List<Long> serverIds = new ArrayList<>();
        List<SystemOverview> data = SystemManager.listSystemsWithPackage(user, pkgId);
        if (data == null) {
            log.info("SystemSearchHelper.getSystemsByInstalledPackageId({}) got back null.", pkgId);
            return null;
        }
        for (SystemOverview so : data) {
            serverIds.add(so.getId());
        }
        return serverIds;
    }

    protected static List<Long> getSystemsByNeededPackageId(User user, Long pkgId) {
        List<Long> serverIds = new ArrayList<>();
        List<SystemOverview> data = SystemManager.listSystemsWithNeededPackage(user, pkgId);
        if (data == null) {
            log.info("SystemSearchHelper.getSystemsByNeededPackageId({}) got back null.", pkgId);
            return null;
        }
        for (SystemOverview so : data) {
            serverIds.add(so.getId());
        }
        return serverIds;
    }

    protected static void filterOutIdsNotInSSM(
            User user, Map<Long, Map<String, Object>> ids) {
        RhnSet systems = RhnSetDecl.SYSTEMS.get(user);
        ids.keySet().removeIf(id -> {
            boolean shouldRemove = !systems.contains(id);
            if (shouldRemove) {
                log.debug("SystemSearchHelper.filterOutIdsNotInSSM() removing system id {}, because it is not" +
                        " in the SystemSetManager list of ids", id);
            }
            return shouldRemove;
        });
    }

    protected static Map<Long, Map<String, Object>> invertResults(User user, Map<Long, Map<String, Object>> ids) {
        // Hack to guess at what the matchingField should be, use the MATCHING_FIELD from
        // the first item in the passed in Map of ids
        String matchingField = ids.values().stream()
                .findFirst()
                .map(firstItem -> (String) firstItem.get(MATCHING_FIELD))
                .orElse("");
        log.info("Will use <{}> as the value to supply for matchingField in all of these invertMatches", matchingField);
        // Get list of all SystemIds and save to new Map
        Map<Long, Map<String, Object>> invertedIds = new HashMap<>();
        DataResult<SystemOverview> dr = SystemManager.systemList(user, null);
        log.info("{} systems came back as the total number of visible systems to this user", dr.size());
        for (SystemOverview so : dr) {
            log.debug("Adding system id: {} to allIds map", so.getId());
            Map<String, Object> info = new HashMap<>();
            info.put(MATCHING_FIELD, matchingField);
            invertedIds.put(so.getId(), info);
        }
        // Remove each entry which matches passed in ids
        ids.keySet().stream()
                .filter(id -> invertedIds.containsKey(id))
                .forEach(id -> {
                    invertedIds.remove(id);
                    log.debug("removed {} from allIds", id);
                });
        log.info("returning {} system ids as the inverted results", invertedIds.size());
        return invertedIds;
    }

    protected static String formatDateString(Date d) {
        String dateFormat = "yyyyMMddHHmm";
        java.text.SimpleDateFormat sdf =
              new java.text.SimpleDateFormat(dateFormat);
        // Lucene uses GMT for indexing
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(d);
    }

    /**
     * Will compare two SystemOverview objects based on their score from a
     * lucene search Creates a list ordered from highest score to lowest
     */
    public static class SearchResultScoreComparator implements Comparator<SystemOverview> {
        private static final int S1_FIRST = -1;
        private static final int S2_FIRST = 1;

        protected Map<Long, Map<String, Object>> results;

        /**
         * @param resultsIn
         *            map of server related info to use for comparisons
         */
        public SearchResultScoreComparator(Map<Long, Map<String, Object>> resultsIn) {
            this.results = resultsIn;
        }

        /**
         * @param sys1 systemOverview11
         * @param sys2 systemOverview2
         * @return comparison info based on lucene score
         */
        @Override
        public int compare(SystemOverview sys1, SystemOverview sys2) {
            Long serverId1 = sys1.getId();
            Long serverId2 = sys2.getId();
            if (results == null) {
                return compareByNameAndSID(sys1, sys2);
            }

            Map<String, Object> sMap1 = results.get(serverId1);
            Map<String, Object> sMap2 = results.get(serverId2);
            if ((sMap1 == null) && (sMap2 == null)) {
                return compareByNameAndSID(sys1, sys2);
            }

            if (sMap1 == null) {
                return S2_FIRST;
            }

            if (sMap2 == null) {
                return S1_FIRST;
            }

            Double score1 = (sMap1.containsKey(SCORE) ? (Double) sMap1.get(SCORE) : null);
            Double score2 = (sMap2.containsKey(SCORE) ? (Double) sMap2.get(SCORE) : null);
            if ((score1 == null) && (score2 == null)) {
                return compareByNameAndSID(sys1, sys2);
            }
            if (score1 == null) {
                return S2_FIRST;
            }
            if (score2 == null) {
                return S1_FIRST;
            }

            /*
             * Note: We want a list which goes from highest score to lowest
             * score, so we are reversing the order of comparison.
             */
            score1 *= -1.0d;
            score2 *= -1.0d;

            /*
             * 2/19/09 Adding to this for bz# 483177 Customer request that we
             * also order by systemid, they request that when the same hostname
             * has been registered many times and shows up in search, we sort by
             * sysid with the highest systemid at the top.
             */
            if (Double.compare(score1, score2) == 0) {
                return compareByNameAndSID(sys1, sys2);
            }

            return score1.compareTo(score2);
        }

        // Sort by profile-name if there is one, then by reverse-sid-order
        private int compareByNameAndSID(SystemOverview sys1, SystemOverview sys2) {

            if ((sys1.getName() == null) && (sys2.getName() == null)) {
                return sys2.getId().compareTo(sys1.getId());
            }

            if (sys1.getName() == null) {
                return S2_FIRST;
            }

            if (sys2.getName() == null) {
                return S1_FIRST;
            }

            if (sys1.getName().equals(sys2.getName())) {
                // We want highest id to be on top
                return sys2.getId().compareTo(sys1.getId());
            }
            else {
                return sys1.getName().compareTo(sys2.getName());
            }
        }
    }

    /**
     *
     * Compares search results by 'matchingFieldValue'
     *
     */
    public static class SearchResultMatchedFieldComparator implements Comparator<SystemOverview> {
        protected Map<Long, Map<String, Object>> results;
        protected boolean sortLowToHigh;

        /**
         * @param resultsIn map of server related info to use for comparisons
         */
        public SearchResultMatchedFieldComparator(Map<Long, Map<String, Object>> resultsIn) {
            this.results = resultsIn;
            this.sortLowToHigh = true;
        }
        /**
         * @param resultsIn map of server related info to use for comparisons
         * @param sortLowToHighIn sort order boolean
         */
        public SearchResultMatchedFieldComparator(Map<Long, Map<String, Object>> resultsIn, boolean sortLowToHighIn) {
            this.results = resultsIn;
            this.sortLowToHigh = sortLowToHighIn;
        }
        /**
         * @param sys1 systemOverview11
         * @param sys2 systemOverview2
         * @return comparison info based on matchingFieldValue
         */
        @Override
        public int compare(SystemOverview sys1, SystemOverview sys2) {
            Long serverId1 = sys1.getId();
            Long serverId2 = sys2.getId();
            if (results == null) {
                return 0;
            }
            Map<String, Object> sMap1 = results.get(serverId1);
            Map<String, Object> sMap2 = results.get(serverId2);
            if ((sMap1 == null) && (sMap2 == null)) {
                return 0;
            }
            if (sMap1 == null) {
                return -1;
            }
            if (sMap2 == null) {
                return 1;
            }
            String val1 = (String)sMap1.get(MATCHING_FIELD_VALUE);
            String val2 = (String)sMap2.get(MATCHING_FIELD_VALUE);
            if ((val1 == null) && (val2 == null)) {
                return 0;
            }
            if (val1 == null) {
                return -1;
            }
            if (val2 == null) {
                return 1;
            }
            try {
                Long lng1 = Long.parseLong(val1);
                Long lng2 = Long.parseLong(val2);
                if (sortLowToHigh) {
                    return lng1.compareTo(lng2);
                }
                return lng2.compareTo(lng1);
            }
            catch (NumberFormatException e) {
                // String isn't a Long so continue
            }
            try {
                Double doub1 = Double.parseDouble(val1);
                Double doub2 = Double.parseDouble(val2);
                if (sortLowToHigh) {
                    return doub1.compareTo(doub2);
                }
                return doub2.compareTo(doub1);
            }
            catch (NumberFormatException e) {
                // String isn't a Double so continue
            }
            // Fallback to standard string sort
            if (sortLowToHigh) {
                return val1.compareTo(val2);
            }
            return val2.compareTo(val1);
        }
    }
}
