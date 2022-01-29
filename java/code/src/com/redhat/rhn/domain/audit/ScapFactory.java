/*
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
package com.redhat.rhn.domain.audit;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.localization.LocalizationService;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ScapFactory - the singleton class used to fetch and store
 * com.redhat.rhn.domain.audit.* objects related to SCAP
 * from the database.
 */
public class ScapFactory extends HibernateFactory {

    private static ScapFactory singleton = new ScapFactory();
    private static Logger log = Logger.getLogger(ScapFactory.class);

    /**
     * Lookup a XCCDF TestResult by the id
     * @param xid of the XCCDF TestResult to search for
     * @return the XccdfTestResult found
     */
    public static XccdfTestResult lookupTestResultById(Long xid) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("xid", xid);
        return (XccdfTestResult)singleton.lookupObjectByNamedQuery(
                "XccdfTestResult.findById", params);
    }

    /**
     * Lookup an XCCDF TestResult by the id and ensure it is assigned with given system
     * @param xid of the XCCDF TestResult to search for
     * @param sid of the system expected
     * @return the XccdfTestResult found
     */
    public static XccdfTestResult lookupTestResultByIdAndSid(Long xid, Long sid) {
        XccdfTestResult result = lookupTestResultById(xid);
        if (result == null || result.getServer().getId() != sid) {
            LocalizationService ls = LocalizationService.getInstance();
            LookupException e = new LookupException("Could not find XCCDF scan " +
                    xid + " for system " + sid);

            e.setLocalizedTitle(ls.getMessage("lookup.xccdfscan.title"));
            throw e;
        }
        return result;
    }

    /**
     * Delete XCCDF TestResult
     * @param tr XCCDF TestResult to delete
     */
    public static void delete(XccdfTestResult tr) {
        singleton.removeObject(tr);
    }

    /**
     * Delete XCCDF TestResults for the given server and action.
     * @param serverId the server id
     * @param actionId the action id
     */
    public static void clearTestResult(long serverId, long actionId) {
        List<XccdfTestResult> results = getSession()
                .getNamedQuery("XccdfTestResult.findByActionId")
                .setLong("serverId", serverId)
                .setLong("actionId", actionId)
                .list();
        results.forEach(tr -> delete(tr));
    }

    /**
     * Find a {@link XccdfBenchmark} by id.
     * @param benchmarkId benchmark id
     * @return the {@link XccdfBenchmark} if any
     */
    public static Optional<XccdfBenchmark> lookupBenchmarkById(long benchmarkId) {
        return Optional.ofNullable(
                (XccdfBenchmark)getSession().get(XccdfBenchmark.class, benchmarkId));
    }

    /**
     * Find a {@link XccdfIdent} by id.
     * @param identId ident id
     * @return the {@link XccdfIdent} if any
     */
    public static Optional<XccdfIdent> lookupIdentById(long identId) {
        return Optional.ofNullable((XccdfIdent)getSession().get(XccdfIdent.class, identId));
    }

    /**
     * Find a {@link XccdfProfile} by id.
     * @param profileId profile id
     * @return the {@link XccdfProfile} if any
     */
    public static Optional<XccdfProfile> lookupProfileById(long profileId) {
        return Optional.ofNullable(
                (XccdfProfile)getSession().get(XccdfProfile.class, profileId));
    }

    /**
     * Find a {@link XccdfRuleResultType} by id.
     * @param label label id
     * @return the {@link XccdfRuleResultType} if any
     */
    public static Optional<XccdfRuleResultType> lookupRuleResultType(String label) {
        return getSession().createCriteria(XccdfRuleResultType.class)
                .add(Restrictions.eq("label", label))
                .list()
                .stream().findFirst();
    }

    /**
     * Persist {@link XccdfTestResult} to db.
     * @param result entity to persist
     */
    public static void save(XccdfTestResult result) {
        getSession().persist(result);
    }

    /**
     * Persist {@link XccdfRuleResult} to db.
     * @param ruleResult entity to persist
     */
    public static void save(XccdfRuleResult ruleResult) {
        getSession().persist(ruleResult);
    }

    /**
     * Get the Logger for the derived class so log messages
     * show up on the correct class.
     * @return Logger
     */
     protected Logger getLogger() {
         return log;
     }

}
