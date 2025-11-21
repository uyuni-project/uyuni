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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.type.StandardBasicTypes;

import java.util.List;
import java.util.Optional;

/**
 * ScapFactory - the singleton class used to fetch and store
 * com.redhat.rhn.domain.audit.* objects related to SCAP
 * from the database.
 */
public class ScapFactory extends HibernateFactory {

    private static ScapFactory singleton = new ScapFactory();
    private static Logger log = LogManager.getLogger(ScapFactory.class);

    /**
     * Lookup a XCCDF TestResult by the id
     * @param xid of the XCCDF TestResult to search for
     * @return the XccdfTestResult found
     */
    public static XccdfTestResult lookupTestResultById(Long xid) {
        return singleton.lookupObjectByParam(XccdfTestResult.class, "id", xid);
    }

    /**
     * Lookup an XCCDF TestResult by the id and ensure it is assigned with given system
     * @param xid of the XCCDF TestResult to search for
     * @param sid of the system expected
     * @return the XccdfTestResult found
     */
    public static XccdfTestResult lookupTestResultByIdAndSid(Long xid, Long sid) {
        XccdfTestResult result = lookupTestResultById(xid);
        if (result == null || !result.getServer().getId().equals(sid)) {
            LocalizationService ls = LocalizationService.getInstance();
            throw new LookupException("Could not find XCCDF scan " + xid + " for system " + sid,
                    ls.getMessage("lookup.xccdfscan.title"), null, null);
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
                .createQuery("""
                        SELECT r
                        FROM com.redhat.rhn.domain.audit.XccdfTestResult AS r
                        JOIN r.scapActionDetails d
                        WHERE d.parentAction.id=:actionId
                        AND r.server.id=:serverId""", XccdfTestResult.class)
                .setParameter("serverId", serverId, StandardBasicTypes.LONG)
                .setParameter("actionId", actionId, StandardBasicTypes.LONG)
                .list();
        results.forEach(ScapFactory::delete);
    }

    /**
     * Find a {@link XccdfBenchmark} by id.
     * @param benchmarkId benchmark id
     * @return the {@link XccdfBenchmark} if any
     */
    public static Optional<XccdfBenchmark> lookupBenchmarkById(long benchmarkId) {
        return Optional.ofNullable(getSession().find(XccdfBenchmark.class, benchmarkId));
    }

    /**
     * Find a {@link XccdfIdent} by id.
     * @param identId ident id
     * @return the {@link XccdfIdent} if any
     */
    public static Optional<XccdfIdent> lookupIdentById(long identId) {
        return Optional.ofNullable(getSession().find(XccdfIdent.class, identId));
    }

    /**
     * Find a {@link XccdfProfile} by id.
     * @param profileId profile id
     * @return the {@link XccdfProfile} if any
     */
    public static Optional<XccdfProfile> lookupProfileById(long profileId) {
        return Optional.ofNullable(getSession().find(XccdfProfile.class, profileId));
    }

    /**
     * Queries an XccdfRuleResultType by its label.
     *
     * @param label the label of the XccdfRuleResultType
     * @return optional of XccdfRuleResultType
     */
    public static Optional<XccdfRuleResultType> lookupRuleResultType(String label) {
        String sql = "SELECT * FROM rhnXccdfRuleResultType WHERE label = :label";
        XccdfRuleResultType result =
                getSession().createNativeQuery(sql, XccdfRuleResultType.class)
                        .setParameter("label", label, StandardBasicTypes.STRING)
                        .getResultStream().findFirst().orElse(null);
        return Optional.ofNullable(result);
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
     @Override
     protected Logger getLogger() {
         return log;
     }

}
