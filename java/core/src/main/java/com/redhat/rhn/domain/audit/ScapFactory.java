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
import com.redhat.rhn.domain.org.Org;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.type.StandardBasicTypes;

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
     * Search for all tailoring files objects in the database
     * @return list of tailoring files objects
     */
    public static List<TailoringFile> lookupAllTailoringFiles() {
        return getSession().createQuery("FROM TailoringFile").list();
    }
    /**
     * Search for all tailoring files objects in the database
     * @param org the organization
     * @return Returns a list of  tailoring files
     */
    public static List<TailoringFile> listTailoringFiles(Org org) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<TailoringFile> criteria = builder.createQuery(TailoringFile.class);
        Root<TailoringFile> root = criteria.from(TailoringFile.class);
        criteria.where(builder.equal(root.get("org"), org));
        return getSession().createQuery(criteria).getResultList();
    }

    /**
     * Lookup for a tailoring file object based on the id and organization
     * @param id tailoring file ID
     * @param org the organization
     * @return optional of tailoring file object
     */
    public static Optional<TailoringFile> lookupTailoringFileByIdAndOrg(Integer id, Org org) {

        if (Objects.isNull(id)) {
            return Optional.empty();
        }
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<TailoringFile> select = builder.createQuery(TailoringFile.class);
        Root<TailoringFile> root = select.from(TailoringFile.class);
        select.where(builder.and(
                builder.equal(root.get("id"), id),
                builder.equal(root.get("org"), org)));

        return getSession().createQuery(select).uniqueResultOptional();
    }

    /**
     * Lookup for Tailoring files by an id list and organization
     * @param ids image profile id list
     * @param org the organization
     * @return Returns a list of image profiles with the given ids if it exists
     * inside the organization
     */
    public static List<TailoringFile>  lookupTailoringFilesByIds(List<Long> ids, Org org) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<TailoringFile> criteria = builder.createQuery(TailoringFile.class);
        Root<TailoringFile> root = criteria.from(TailoringFile.class);
        criteria.where(builder.and(
                root.get("id").in(ids),
                builder.equal(root.get("org"), org)));
        return getSession().createQuery(criteria).getResultList();
    }

    /**
     * Deletes the Tailoring file object from the database
     * @param tailoringFile TailoringFile object
     */
    public static void deleteTailoringFile(TailoringFile tailoringFile) {
        getSession().delete(tailoringFile);
    }
    /**
     * Save the tailoringFile object to the database
     * @param tailoringFile object
     */
    public static void saveTailoringFile(TailoringFile tailoringFile) {
        tailoringFile.setModified(new Date());
        singleton.saveObject(tailoringFile);
    }

    /**
     * List all SCAP polices objects in the database
     * @param org the organization
     * @return Returns a list of  tailoring files
     */
    public static List<ScapPolicy> listScapPolicies(Org org) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<ScapPolicy> criteria = builder.createQuery(ScapPolicy.class);
        Root<ScapPolicy> root = criteria.from(ScapPolicy.class);
        criteria.where(builder.equal(root.get("org"), org));
        return getSession().createQuery(criteria).getResultList();
    }

    /**
     * Lookup for Scap policies by an id list and organization
     * @param ids image profile id list
     * @param org the organization
     * @return Returns a list of  tailoring files
     * inside the organization
     */
    public static List<ScapPolicy>  lookupScapPoliciesByIds(List<Integer> ids, Org org) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<ScapPolicy> criteria = builder.createQuery(ScapPolicy.class);
        Root<ScapPolicy> root = criteria.from(ScapPolicy.class);
        criteria.where(builder.and(
                root.get("id").in(ids),
                builder.equal(root.get("org"), org)));
        return getSession().createQuery(criteria).getResultList();
    }
    /**
     * Lookup for a tailoring file object based on the id and organization
     * @param id tailoring file ID
     * @param org the organization
     * @return optional of tailoring file object
     */
    public static Optional<ScapPolicy> lookupScapPolicyByIdAndOrg(Integer id, Org org) {

        if (Objects.isNull(id)) {
            return Optional.empty();
        }
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<ScapPolicy> select = builder.createQuery(ScapPolicy.class);
        Root<ScapPolicy> root = select.from(ScapPolicy.class);
        select.where(builder.and(
                builder.equal(root.get("id"), id),
                builder.equal(root.get("org"), org)));
        return getSession().createQuery(select).uniqueResultOptional();
    }
    /**
     * Deletes the Scap Policy object from the database
     * @param scapPolicy ScapPolicy object
     */
    public static void deleteScapPolicy(ScapPolicy scapPolicy) {
        getSession().delete(scapPolicy);
    }
    /**
     * Save the scapPolicy object to the database
     * @param scapPolicy object
     */
    public static void saveScapPolicy(ScapPolicy scapPolicy) {
        scapPolicy.setModified(new Date());
        singleton.saveObject(scapPolicy);
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
