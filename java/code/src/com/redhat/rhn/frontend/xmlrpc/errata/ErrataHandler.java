/**
 * Copyright (c) 2009--2018 Red Hat, Inc.
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
/*
 * Copyright (c) 2010 SUSE LLC
 */
package com.redhat.rhn.frontend.xmlrpc.errata;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import com.redhat.rhn.FaultException;
import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ClonedChannel;
import com.redhat.rhn.domain.channel.InvalidChannelRoleException;
import com.redhat.rhn.domain.errata.AdvisoryStatus;
import com.redhat.rhn.domain.errata.Bug;
import com.redhat.rhn.domain.errata.Cve;
import com.redhat.rhn.domain.errata.CveFactory;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.ErrataFactory;
import com.redhat.rhn.domain.errata.Keyword;
import com.redhat.rhn.domain.errata.Severity;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.CVE;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.DuplicateErrataException;
import com.redhat.rhn.frontend.xmlrpc.InvalidAdvisoryReleaseException;
import com.redhat.rhn.frontend.xmlrpc.InvalidAdvisoryTypeException;
import com.redhat.rhn.frontend.xmlrpc.InvalidChannelException;
import com.redhat.rhn.frontend.xmlrpc.InvalidChannelLabelException;
import com.redhat.rhn.frontend.xmlrpc.InvalidErrataException;
import com.redhat.rhn.frontend.xmlrpc.InvalidPackageException;
import com.redhat.rhn.frontend.xmlrpc.InvalidParameterException;
import com.redhat.rhn.frontend.xmlrpc.MissingErrataAttributeException;
import com.redhat.rhn.frontend.xmlrpc.NoChannelsSelectedException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchChannelException;
import com.redhat.rhn.frontend.xmlrpc.PermissionCheckFailureException;
import com.redhat.rhn.frontend.xmlrpc.packages.PackageHelper;
import com.redhat.rhn.manager.errata.ErrataManager;
import com.redhat.rhn.manager.errata.cache.ErrataCacheManager;
import com.redhat.rhn.manager.rhnpackage.PackageManager;
import com.redhat.rhn.manager.user.UserManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * ErrataHandler - provides methods to access errata information.
 * @xmlrpc.namespace errata
 * @xmlrpc.doc Provides methods to access and modify errata.
 */
public class ErrataHandler extends BaseHandler {

    private static final List<String> RO_METHODS = Arrays.asList("applicableToChannels");
    private static Logger log = Logger.getLogger(ErrataHandler.class);

    /**
     * Returns an OVAL metadata file for a given errata or CVE
     * @param loggedInUser The current user
     * @param identifier Errata identifier (either id, CVE/CAN, or Advisory name)
     * @return Escaped XML representing the OVAL metadata document
     * @throws IOException error building XML file
     * @throws FaultException general error occurred
     *
     * @xmlrpc.doc Retrieves the OVAL metadata associated with one or more erratas.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "identifier", "Can either be an erratum's ID,
     *              CVE/CAN, or advisory name.  In the case of CVE/CAN, all dashes must be
     *             removed from the name.Numeric advisory IDs and advisory names
     *              (RHSA-2006:011) can be submitted as they are.")
     * @xmlrpc.returntype #param_desc("string", "metadata", "The OVAL metadata document in escaped XML form")
     */
    /**
     * The getOval method is being commented out due to bugzilla 504054.  This bug
     * raises an issue of a null exception being generated on execution.  The method
     * has been updated to address that exception; however, there is a larger issue at
     * hand in that the OVAL functionality is not fully supported by the application.
     * For example, the OVAL meta data is not synced to the database; therefore, there
     * will never be data to return by the method.  So it is better to comment it out
     * than to have the method that cannot return any data. :)  It is, however,
     * desirable to support this in the future, so we don't want to lose the logic.
     *
    public String getOval(User loggedInUser, String identifier) throws IOException,
            FaultException {
        User loggedInUser = getLoggedInUser(sessionKey);

        String retval = "";
        List<Errata> erratas = ErrataManager.lookupErrataByIdentifier(identifier);
        for (Errata errata : erratas) {
            if (errata.getOrg() != null &&
                    !errata.getOrg().equals(loggedInUser.getOrg())) {
                erratas.remove(errata);
            }
        }

        if (erratas == null) {
            throw new FaultException(-1, "errataNotFound",
                    "No patches found for given identifier");
        }
        List files = new LinkedList();
        if (erratas != null) {
            for (Iterator iter = erratas.iterator(); iter.hasNext();) {
                Errata e = (Errata) iter.next();
                List tmp =
                    ErrataFactory.lookupErrataFilesByErrataAndFileType(e.getId(),
                            "oval");
                if ((tmp != null && tmp.size() > 0) &&
                        (e.getOrg() == null || e.getOrg().equals(loggedInUser.getOrg()))) {
                    files.addAll(tmp);
                }
            }
            files = ErrataManager.resolveOvalFiles(files);
            if (files != null) {
                if (files.size() == 0) {
                    throw new FaultException(-1, "ovalNotFound",
                            "No OVAL files found for given patches");
                }
                else if (files.size() == 1) {
                    File f = (File) files.get(0);
                    if (f != null) {
                        InputStream in = null;
                        byte[] buf = new byte[4096];
                        int readsize = 0;
                        ByteArrayOutputStream accum  = new ByteArrayOutputStream();
                        try {
                            in = new FileInputStream(f);
                            while ((readsize = in.read(buf)) > -1) {
                                accum.write(buf, 0, readsize);
                            }
                            retval = new String(accum.toByteArray(), "UTF-8");
                        }
                        finally {
                            if (in != null) {
                                in.close();
                            }
                        }
                    }
                }
                else if (files.size() > 1) {
                    try {
                        OvalFileAggregator agg = new OvalFileAggregator();
                        for (Iterator iter = files.iterator(); iter.hasNext();) {
                            File f = (File) iter.next();
                            if (f != null && !f.getPath().endsWith("test-5.xml")) {
                                agg.add(f);
                            }
                        }
                        retval = StringEscapeUtils.escapeXml(agg.finish(false));
                    }
                    catch (JDOMException e) {
                        throw new FaultException(-1, "err_building_oval", e.getMessage());
                    }
                }
            }
        }
        return retval;
    }
     */

    /**
     * GetDetails - Retrieves the details for a given errata.
     * @param loggedInUser The current user
     * @param advisoryName The advisory name of the errata
     * @return Returns a map containing the details of the errata
     * @throws FaultException A FaultException is thrown if the errata
     * corresponding to advisoryName cannot be found.
     *
     * @xmlrpc.doc Retrieves the details for the erratum matching the given
     * advisory name.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("string", "advisoryName")
     * @xmlrpc.returntype
     *      #struct_begin("erratum")
     *          #prop("int", "id")
     *          #prop("string", "issue_date")
     *          #prop("string", "update_date")
     *          #prop_desc("string", "last_modified_date", "last time the erratum was modified.")
     *          #prop("string", "synopsis")
     *          #prop("int", "release")
     *          #prop("string", "advisory_status")
     *          #prop("string", "type")
     *          #prop("string", "product")
     *          #prop("string", "errataFrom")
     *          #prop("string", "topic")
     *          #prop("string", "description")
     *          #prop("string", "references")
     *          #prop("string", "notes")
     *          #prop("string", "solution")
     *     #struct_end()
     */
    public Map<String, Object> getDetails(User loggedInUser, String advisoryName)
            throws FaultException {
     // Get the logged in user. We don't care what roles this user has, we
        // just want to make sure the caller is logged in.

        Errata errata = lookupAccessibleErratum(advisoryName, empty(), loggedInUser.getOrg());

        Map<String, Object> errataMap = new HashMap<String, Object>();

        errataMap.put("id", errata.getId());
        if (errata.getIssueDate() != null) {
            errataMap.put("issue_date",
                    LocalizationService.getInstance()
                    .formatShortDate(errata.getIssueDate()));
        }
        if (errata.getUpdateDate() != null) {
            errataMap.put("update_date",
                    LocalizationService.getInstance()
                    .formatShortDate(errata.getUpdateDate()));
        }
        if (errata.getLastModified() != null) {
            errataMap.put("last_modified_date", errata.getLastModified().toString());
        }
        if (errata.getAdvisoryRel() != null) {
            errataMap.put("release", errata.getAdvisoryRel());
        }
        if (errata.getAdvisoryStatus() != null) {
            errataMap.put("advisory_status", errata.getAdvisoryStatus().getMetadataValue());
        }
        errataMap.put("product",
                StringUtils.defaultString(errata.getProduct()));
        errataMap.put("errataFrom",
                StringUtils.defaultString(errata.getErrataFrom()));
        errataMap.put("solution",
                StringUtils.defaultString(errata.getSolution()));
        errataMap.put("description",
                StringUtils.defaultString(errata.getDescription()));
        errataMap.put("synopsis",
                StringUtils.defaultString(errata.getSynopsis()));
        errataMap.put("topic",
                StringUtils.defaultString(errata.getTopic()));
        errataMap.put("references",
                StringUtils.defaultString(errata.getRefersTo()));
        errataMap.put("notes",
                StringUtils.defaultString(errata.getNotes()));
        errataMap.put("type",
                StringUtils.defaultString(errata.getAdvisoryType()));
        if (errata.getSeverity() != null) {
            errataMap.put("severity", errata.getSeverity().getLocalizedLabel());
        }


        return errataMap;
    }

    /**
     * Set erratum details.
     *
     * @param loggedInUser The current user
     * @param advisoryName The advisory name of the errata
     * @param details Map of (optional) erratum details to be set.
     * @return 1 on success, exception thrown otherwise.
     *
     * @xmlrpc.doc Set erratum details. All arguments are optional and will only be modified
     * if included in the struct. This method will only allow for modification of custom
     * errata created either through the UI or API.
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("string", "advisoryName")
     * @xmlrpc.param
     *      #struct_begin("errata details")
     *          #prop("string", "synopsis")
     *          #prop("string", "advisory_name")
     *          #prop("int", "advisory_release")
     *          #prop_desc("string", "advisory_type", "Type of advisory (one of the
     *                  following: 'Security Advisory', 'Product Enhancement Advisory',
     *                  or 'Bug Fix Advisory'")
     *          #prop("string", "product")
     *          #prop("dateTime.iso8601", "issue_date")
     *          #prop("dateTime.iso8601", "update_date")
     *          #prop("string", "errataFrom")
     *          #prop("string", "topic")
     *          #prop("string", "description")
     *          #prop("string", "references")
     *          #prop("string", "notes")
     *          #prop("string", "solution")
     *          #prop_desc("string", "severity", "Severity of advisory (one of the
     *                  following: 'Low', 'Moderate', 'Important', 'Critical'
     *                  or 'Unspecified'")
     *          #prop_desc("array", "bugs", "'bugs' is the key into the struct")
     *              #array_begin()
     *                 #struct_begin("bug")
     *                    #prop_desc("int", "id", "Bug Id")
     *                    #prop("string", "summary")
     *                    #prop("string", "url")
     *                 #struct_end()
     *              #array_end()
     *          #prop_desc("array", "keywords", "'keywords' is the key into the struct")
     *              #array_single("string", "keyword - List of keywords to associate
     *                  with the errata.")
     *          #prop_desc("array", "CVEs", "'cves' is the key into the struct")
     *              #array_single("string", "cves - List of CVEs to associate
     *                  with the errata")
     *     #struct_end()
     *
     *  @xmlrpc.returntype #return_int_success()
     */
    public Integer setDetails(User loggedInUser, String advisoryName,
            Map<String, Object> details) {

        Errata errata = lookupErrata(advisoryName, loggedInUser.getOrg());

        // confirm that the user only provided valid keys in the map
        Set<String> validKeys = new HashSet<String>();
        validKeys.add("synopsis");
        validKeys.add("advisory_name");
        validKeys.add("advisory_release");
        validKeys.add("advisory_type");
        validKeys.add("product");
        validKeys.add("issue_date");
        validKeys.add("update_date");
        validKeys.add("errataFrom");
        validKeys.add("topic");
        validKeys.add("description");
        validKeys.add("references");
        validKeys.add("notes");
        validKeys.add("solution");
        validKeys.add("bugs");
        validKeys.add("keywords");
        validKeys.add("severity");
        validKeys.add("cves");
        validateMap(validKeys, details);

        validKeys.clear();
        validKeys.add("id");
        validKeys.add("summary");
        validKeys.add("url");
        if (details.containsKey("bugs")) {
            for (Map<String, Object> bugMap :
                (ArrayList<Map<String, Object>>) details.get("bugs")) {

                validateMap(validKeys, bugMap);
            }
        }

        if (details.containsKey("issue_date")) {
            try {
                errata.setIssueDate((Date)details.get("issue_date"));
            }
            catch (ClassCastException e) {
                throw new InvalidParameterException("Wrong 'issue_date' format.");
            }
        }

        if (details.containsKey("update_date")) {
            try {
                errata.setUpdateDate((Date)details.get("update_date"));
            }
            catch (ClassCastException e) {
                throw new InvalidParameterException("Wrong 'update_date' format.");
            }
        }

        if (details.containsKey("issue_date")) {
            try {
                errata.setIssueDate((Date)details.get("issue_date"));
            }
            catch (ClassCastException e) {
                throw new InvalidParameterException("Wrong 'issue_date' format.");
            }
        }

        if (details.containsKey("update_date")) {
            try {
                errata.setUpdateDate((Date)details.get("update_date"));
            }
            catch (ClassCastException e) {
                throw new InvalidParameterException("Wrong 'update_date' format.");
            }
        }

        if (details.containsKey("synopsis")) {
            if (StringUtils.isBlank((String)details.get("synopsis"))) {
                throw new InvalidParameterException("Synopsis is required.");
            }
            errata.setSynopsis((String)details.get("synopsis"));
        }
        if (details.containsKey("advisory_name")) {
            if (StringUtils.isBlank((String)details.get("advisory_name"))) {
                throw new InvalidParameterException("Advisory name is required.");
            }
            errata.setAdvisoryName((String)details.get("advisory_name"));
        }
        if (details.containsKey("advisory_release")) {
            Long rel = Long.valueOf((Integer)details.get("advisory_release"));
            if (rel.longValue() > ErrataManager.MAX_ADVISORY_RELEASE) {
                throw new InvalidAdvisoryReleaseException(rel.longValue());
            }
            errata.setAdvisoryRel(rel);
        }
        if (details.containsKey("advisory_type")) {
            String pea = "Product Enhancement Advisory"; // hack for checkstyle
            if (!((String)details.get("advisory_type")).equals("Security Advisory") &&
            !((String)details.get("advisory_type")).equals(pea) &&
            !((String)details.get("advisory_type")).equals("Bug Fix Advisory")) {
                throw new InvalidParameterException("Invalid advisory type");
            }
            errata.setAdvisoryType((String)details.get("advisory_type"));
        }
        if (details.containsKey("product")) {
            if (StringUtils.isBlank((String)details.get("product"))) {
                throw new InvalidParameterException("Product name is required.");
            }
            errata.setProduct((String)details.get("product"));
        }
        if (details.containsKey("errataFrom")) {
            errata.setErrataFrom((String)details.get("errataFrom"));
        }
        if (details.containsKey("topic")) {
            if (StringUtils.isBlank((String)details.get("topic"))) {
                throw new InvalidParameterException("Topic is required.");
            }
            errata.setTopic((String)details.get("topic"));
        }
        if (details.containsKey("description")) {
            if (StringUtils.isBlank((String)details.get("description"))) {
                throw new InvalidParameterException("Description is required.");
            }
            errata.setDescription((String)details.get("description"));
        }
        if (details.containsKey("solution")) {
            if (StringUtils.isBlank((String)details.get("solution"))) {
                throw new InvalidParameterException("Solution is required.");
            }
            errata.setSolution((String)details.get("solution"));
        }
        if (details.containsKey("references")) {
            errata.setRefersTo((String)details.get("references"));
        }
        if (details.containsKey("notes")) {
            errata.setNotes((String)details.get("notes"));
        }
        if ("Security Advisory".equals(errata.getAdvisoryType())) {
            if (details.containsKey("severity")) {
                String sevName = (String) details.get("severity");
                errata.setSeverity(Severity.getByName(sevName));
            }
        }
        else if (errata.getSeverity() != null) {
                errata.setSeverity(null);
        }
        if (details.containsKey("bugs")) {

            if (errata.getBugs() != null) {
                errata.getBugs().clear();
                HibernateFactory.getSession().flush();
            }

            for (Map<String, Object> bugMap :
                (ArrayList<Map<String, Object>>) details.get("bugs")) {

                if (bugMap.containsKey("id") && bugMap.containsKey("summary")) {
                    String url = null;
                    if (bugMap.containsKey("url")) {
                        url = (String) bugMap.get("url");
                    }

                    Bug bug = ErrataFactory.createBug(
                            Long.valueOf((Integer) bugMap.get("id")),
                            (String) bugMap.get("summary"), url);

                    errata.addBug(bug);
                }
            }
        }
        if (details.containsKey("keywords")) {
            if (errata.getKeywords() != null) {
                errata.getKeywords().clear();
                HibernateFactory.getSession().flush();
            }
            for (String keyword : (ArrayList<String>) details.get("keywords")) {
                errata.addKeyword(keyword);
            }
        }
        if (details.containsKey("cves")) {
            if (errata.getCves() != null) {
                errata.getCves().clear();
                HibernateFactory.getSession().flush();
            }
            for (String cveName : (ArrayList<String>) details.get("cves")) {
                Cve c = CveFactory.lookupByName(cveName);
                if (c == null) {
                    c = new Cve();
                    c.setName(cveName);
                    CveFactory.save(c);
                }
                errata.getCves().add(c);
            }
        }

        // ALWAYS change the advisory to match, as we do in the UI.
        errata.setAdvisory(errata.getAdvisoryName() + "-" +
                errata.getAdvisoryRel().toString());

        //Save the errata
        ErrataFactory.save(errata);

        return 1;
    }

    /**
     * Return the list of systems affected by the errata with the given advisory name.
     * For those errata that are present in both vendor and user organizations under the same advisory name,
     * this method retrieves the affected systems by both of them.
     * @param loggedInUser The current user
     * @param advisoryName The advisory name of the errata
     * @return Returns an object array containing the system ids and system name
     * @throws FaultException A FaultException is thrown if the errata corresponding to
     * advisoryName cannot be found.
     *
     * @xmlrpc.doc Return the list of systems affected by the errata with the given advisory name.
     * For those errata that are present in both vendor and user organizations under the same advisory name,
     * this method retrieves the affected systems by both of them.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("string", "advisoryName")
     * @xmlrpc.returntype
     *      #array_begin()
     *          $SystemOverviewSerializer
     *      #array_end()
     */
    public Object[] listAffectedSystems(User loggedInUser, String advisoryName)
            throws FaultException {

        // Get the logged in user
        List<Errata> erratas = lookupVendorAndUserErrataByAdvisoryAndOrg(advisoryName, loggedInUser.getOrg());
        List<Long> errataIds = erratas.stream().map(Errata::getId).collect(toList());

        DataResult dr = ErrataManager.systemsAffectedXmlRpc(loggedInUser, errataIds);

        return dr.toArray();
    }

    /**
     * Get the Bugzilla fixes for an erratum matching the given advisoryName.
     * For those errata that are present in both vendor and user organizations under the same advisory name,
     * this method retrieves the list of Bugzilla fixes of both of them.
     * @param loggedInUser The current user
     * @param advisoryName The advisory name of the errata
     * @return Returns a map containing the Bugzilla id and summary for each bug
     * @throws FaultException A FaultException is thrown if the errata
     * corresponding to the given advisoryName cannot be found.
     *
     * @xmlrpc.doc Get the Bugzilla fixes for an erratum matching the given
     * advisoryName. The bugs will be returned in a struct where the bug id is
     * the key.  i.e. 208144="errata.bugzillaFixes Method Returns different
     * results than docs say"
     * For those errata that are present in both vendor and user organizations under the same advisory name,
     * this method retrieves the list of Bugzilla fixes of both of them.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("string", "advisoryName")
     * @xmlrpc.returntype
     *      #struct_begin("Bugzilla info")
     *          #prop_desc("string", "bugzilla_id", "actual bug number is the key into the
     *                      struct")
     *          #prop_desc("string", "bug_summary", "summary who's key is the bug id")
     *      #struct_end()
     */
    public Map<Long, String> bugzillaFixes(User loggedInUser, String advisoryName)
            throws FaultException {

        // Get the logged in user
        List<Errata> erratas = lookupVendorAndUserErrataByAdvisoryAndOrg(advisoryName, loggedInUser.getOrg());

        return (Map<Long, String>) erratas.stream().flatMap(e -> e.getBugs().stream())
                .collect(toMap(Bug::getId, Bug::getSummary));
    }

    /**
     * Get the keywords for a given erratum.
     * For those errata that are present in both vendor and user organizations under the same advisory name,
     * this method retrieves the keywords of both of them.
     * @param loggedInUser The current user
     * @param advisoryName The advisory name of the erratum
     * @return Returns an array of keywords for the erratum
     * @throws FaultException A FaultException is thrown if the errata corresponding to the
     * given advisoryName cannot be fo
     *
     * @xmlrpc.doc Get the keywords associated with an erratum matching the
     * given advisory name.
     * For those errata that are present in both vendor and user organizations under the same advisory name,
     * this method retrieves the keywords of both of them.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("string", "advisoryName")
     * @xmlrpc.returntype #array_single("string", "Keyword associated with erratum.")

     */
    public Object[] listKeywords(User loggedInUser, String advisoryName)
            throws FaultException {
        List<Errata> erratas = lookupVendorAndUserErrataByAdvisoryAndOrg(advisoryName, loggedInUser.getOrg());

        Set<String> keywords = erratas.stream()
                .flatMap(e -> e.getKeywords().stream().map(Keyword::getKeyword))
                .collect(toSet());

        return keywords.toArray();
    }

    /**
     * Returns a list of channels (represented by a map) applicable to the errata
     * with the given advisory name.
     * For those errata that are present in both vendor and user organizations under the same advisory name,
     * this method retrieves the list of channels applicable of both of them.
     * @param loggedInUser The current user
     * @param advisoryName The advisory name of the erratum
     * @return Returns an array of channels for the erratum
     * @throws FaultException A FaultException is thrown if the errata corresponding to the
     * given advisoryName cannot be found
     *
     * @xmlrpc.doc Returns a list of channels applicable to the errata
     * with the given advisory name.
     * For those errata that are present in both vendor and user organizations under the same advisory name,
     * this method retrieves the list of channels applicable of both of them.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("string", "advisoryName")
     * @xmlrpc.returntype
     *      #array_begin()
     *          #struct_begin("channel")
     *              #prop("int", "channel_id")
     *              #prop("string", "label")
     *              #prop("string", "name")
     *              #prop("string", "parent_channel_label")
     *          #struct_end()
     *       #array_end()
     */
    public Object[] applicableToChannels(User loggedInUser, String advisoryName)
            throws FaultException {

        // Get the logged in user
        List<Errata> erratas = lookupVendorAndUserErrataByAdvisoryAndOrg(advisoryName, loggedInUser.getOrg());
        List<Long> errataIds = erratas.stream().map(Errata::getId).collect(toList());

        return ErrataManager.applicableChannels(errataIds,
                loggedInUser.getOrg().getId(), null, Map.class).toArray();
    }

    /**
     * Returns a list of CVEs applicable to the errata with the given advisory name.
     * For those errata that are present in both vendor and user organizations under the same advisory name,
     * this method retrieves the list of CVEs of both of them.
     * @param loggedInUser The current user
     * @param advisoryName The advisory name of the erratum
     * @return Returns a collection of CVEs
     * @throws FaultException A FaultException is thrown if the errata corresponding to the
     * given advisoryName cannot be found
        throws FaultException {
     *
     * @xmlrpc.doc Returns a list of
     * <a href="http://cve.mitre.org/" target="_blank">CVE</a>s
     * applicable to the errata with the given advisory name.
     * For those errata that are present in both vendor and user organizations under the same advisory name,
     * this method retrieves the list of CVEs of both of them.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("string",  "advisoryName")
     * @xmlrpc.returntype
     *      #array_single("string", "cveName")
     *
     */
    public List listCves(User loggedInUser, String advisoryName) throws FaultException {
        // Get the logged in user
        List<Errata> erratas = lookupVendorAndUserErrataByAdvisoryAndOrg(advisoryName, loggedInUser.getOrg());
        List<Long> errataIds = erratas.stream().map(Errata::getId).collect(toList());

        DataResult dr = ErrataManager.errataCVEs(errataIds);

        return (List) dr.stream().map(cve -> ((CVE) cve).getName()).collect(toList());
    }

    /**
     * List the packages for a given errata.
     * For those errata that are present in both vendor and user organizations under the same advisory name,
     * this method retrieves the packages of both of them.
     * @param loggedInUser The current user
     * @param advisoryName The advisory name of the errata
     * @return Returns an Array of maps representing a package
     * @throws FaultException A FaultException is thrown if the errata corresponding to the
     * given advisoryName cannot be found
     *
     * @xmlrpc.doc Returns a list of the packages affected by the errata
     * with the given advisory name.
     * For those errata that are present in both vendor and user organizations under the same advisory name,
     * this method retrieves the packages of both of them.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("string", "advisoryName")
     * @xmlrpc.returntype
     *          #array_begin()
     *              #struct_begin("package")
     *                  #prop("int", "id")
     *                  #prop("string", "name")
     *                  #prop("string", "epoch")
     *                  #prop("string", "version")
     *                  #prop("string", "release")
     *                  #prop("string", "arch_label")
     *                  #prop_array("providing_channels", "string", "- Channel label
     *                              providing this package.")
     *                  #prop("string", "build_host")
     *                  #prop("string", "description")
     *                  #prop("string", "checksum")
     *                  #prop("string", "checksum_type")
     *                  #prop("string", "vendor")
     *                  #prop("string", "summary")
     *                  #prop("string", "cookie")
     *                  #prop("string", "license")
     *                  #prop("string", "path")
     *                  #prop("string", "file")
     *                  #prop("string", "build_date")
     *                  #prop("string", "last_modified_date")
     *                  #prop("string", "size")
     *                  #prop("string", "payload_size")
     *               #struct_end()
     *           #array_end()
     */
    public List<Map> listPackages(User loggedInUser, String advisoryName)
            throws FaultException {
        // Get the logged in user
        List<Errata> erratas = lookupVendorAndUserErrataByAdvisoryAndOrg(advisoryName, loggedInUser.getOrg());

        return erratas.stream().flatMap(
                e -> e.getPackages().stream()
                        .map(pkg -> PackageHelper.packageToMap(pkg, loggedInUser)))
                .collect(toList());
    }

    /**
     * Add a set of packages to an erratum
     * @param loggedInUser The current user
     * @param advisoryName The advisory name of the erratum
     * @param packageIds The ids for packages to remove
     * @return Returns int - representing the number of packages added, exception otherwise
     * @throws FaultException A FaultException is thrown if the errata corresponding to the
     * given advisoryName cannot be found
     *
     * @xmlrpc.doc Add a set of packages to an erratum
     * with the given advisory name. This method will only allow for modification
     * of custom errata created either through the UI or API.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("string", "advisoryName")
     * @xmlrpc.param #array_single("int", "packageId")
     * @xmlrpc.returntype
     *   #param_desc("int", "count", "representing the number of packages added, exception otherwise")
     */
    public int addPackages(User loggedInUser, String advisoryName,
            List<Integer> packageIds) throws FaultException {

        // Get the logged in user
        Errata errata = lookupErrata(advisoryName, loggedInUser.getOrg());

        int packagesAdded = 0;
        for (Integer packageId : packageIds) {

            Package pkg = PackageManager.lookupByIdAndUser(Long.valueOf(packageId),
                    loggedInUser);

            if ((pkg != null) && (!errata.getPackages().contains(pkg))) {
                errata.addPackage(pkg);
                packagesAdded++;
            }
        }

        //Update Errata Cache
        if (packagesAdded > 0 && errata.getChannels() != null) {
            ErrataCacheManager.updateCacheForChannelsAsync(
                    errata.getChannels());
        }

        //Save the errata
        ErrataFactory.save(errata);

        return packagesAdded;
    }

    /**
     * Remove a set of packages from an erratum
     * @param loggedInUser The current user
     * @param advisoryName The advisory name of the erratum
     * @param packageIds The ids for packages to remove
     * @return Returns int - representing the number of packages removed,
     * exception otherwise
     * @throws FaultException A FaultException is thrown if the errata corresponding to the
     * given advisoryName cannot be found
     *
     * @xmlrpc.doc Remove a set of packages from an erratum
     * with the given advisory name.  This method will only allow for modification
     * of custom errata created either through the UI or API.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("string", "advisoryName")
     * @xmlrpc.param #array_single("int", "packageId")
     * @xmlrpc.returntype
     *   #param_desc("int", "count", "representing the number of packages removed, exception otherwise")
     */
    public int removePackages(User loggedInUser, String advisoryName,
            List<Integer> packageIds) throws FaultException {

        // Get the logged in user
        Errata errata = lookupErrata(advisoryName, loggedInUser.getOrg());

        int packagesRemoved = 0;
        for (Integer packageId : packageIds) {

            Package pkg = PackageManager.lookupByIdAndUser(Long.valueOf(packageId),
                    loggedInUser);

            if ((pkg != null) && (errata.getPackages().contains(pkg))) {
                errata.removePackage(pkg);
                packagesRemoved++;
            }
        }

        //Update Errata Cache
        if (packagesRemoved > 0 && errata.getChannels() != null) {
            ErrataCacheManager.updateCacheForChannelsAsync(
                    errata.getChannels());
        }

        //Save the errata
        ErrataFactory.save(errata);

        return packagesRemoved;
    }

    /**
     * Private helper method to lookup an errata and throw a Fault exception if it isn't
     * found
     * @param advisoryName The advisory name for the erratum you're looking for
     * @return Returns the errata or a Fault Exception
     * @throws FaultException Occurs when the erratum is not found
     */
    private Errata lookupErrata(String advisoryName, Org org) throws FaultException {
        Errata errata = ErrataManager.lookupByAdvisoryAndOrg(advisoryName, org);

        /*
         * ErrataManager.lookupByAdvisory() could return null, so we need to check
         * and throw a no_such_errata exception if the errata was not found.
         */
        if (errata == null) {
            throw new FaultException(-208, "no_such_patch",
                                     "The patch " + advisoryName + " cannot be found.");
        }
        return errata;
    }

    /**
     * Retrieves the list of errata that belongs to a vendor or a given organization, given an advisory name.
     * Throw a Fault exception if the errata is not found
     * @param advisoryName The advisory name for the erratum you're looking for
     * @param org the organization
     * @return Returns the errata or a Fault Exception
     * @throws FaultException Occurs when the erratum is not found
     */
    private List<Errata> lookupVendorAndUserErrataByAdvisoryAndOrg(String advisoryName, Org org) throws FaultException {
        List<Errata> erratas = ErrataManager.lookupVendorAndUserErrataByAdvisoryAndOrg(advisoryName, org);

        /*
         * ErrataManager.lookupByAdvisory() could return null, so we need to check
         * and throw a no_such_errata exception if the errata was not found.
         */
        if (erratas.isEmpty()) {
            throw new FaultException(-208, "no_such_patch",
                                     "The patch " + advisoryName + " cannot be found.");
        }
        return erratas;
    }

    /**
     * Clones a list of errata into a specified channel
     *
     * @param loggedInUser The current user
     * @param channelLabel the channel's label that we are cloning into
     * @param advisoryNames an array of String objects containing the advisory name
     *          of every errata you want to clone
     * @throws InvalidChannelRoleException if the user perms are incorrect
     * @return Returns an array of Errata objects, which get serialized into XMLRPC
     *
     * @xmlrpc.doc Clone a list of errata into the specified channel.
     *
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("string", "channel_label")
     * @xmlrpc.param
     *     #array_single("string", " advisory - The advisory name of the errata to clone.")
     * @xmlrpc.returntype
     *          #array_begin()
     *              $ErrataSerializer
     *          #array_end()
     */
    public Object[] clone(User loggedInUser, String channelLabel,
            List advisoryNames) throws InvalidChannelRoleException {
        return clone(loggedInUser, channelLabel, advisoryNames, false, false);
    }

    /**
     * Asynchronously clones a list of errata into a specified channel
     *
     * @param loggedInUser The current user
     * @param channelLabel the channel's label that we are cloning into
     * @param advisoryNames an array of String objects containing the advisory name
     *          of every errata you want to clone
     * @throws InvalidChannelRoleException if the user perms are incorrect
     * @return 1 on success, exception thrown otherwise.
     *
     * @xmlrpc.doc Asynchronously clone a list of errata into the specified channel.
     *
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("string", "channel_label")
     * @xmlrpc.param
     *     #array_single("string", " advisory - The advisory name of the errata to clone.")
     * @xmlrpc.returntype
     *          #return_int_success()
     */
    public int cloneAsync(User loggedInUser, String channelLabel,
            List advisoryNames) throws InvalidChannelRoleException {
        clone(loggedInUser, channelLabel, advisoryNames, false, true);
        return 1;
    }


    private Object[] clone(User loggedInUser, String channelLabel,
            List<String> advisoryNames, boolean inheritPackages,
            boolean asynchronous) {
        Channel channel = ChannelFactory.lookupByLabelAndUser(channelLabel,
                loggedInUser);

        if (channel == null) {
            throw new NoSuchChannelException();
        }

        Channel original = ChannelFactory.lookupOriginalChannel(channel);

        //if calling cloneAsOriginal, do additional checks to verify a clone
        if (inheritPackages) {
            if (!channel.isCloned()) {
                throw new InvalidChannelException("Cloned channel expected: " +
                        channel.getLabel());
            }

            if (original == null) {
                throw new InvalidChannelException("Cannot access original " +
                        "of the channel: " + channel.getLabel());
            }

            // check access to the original
            if (ChannelFactory.lookupByIdAndUser(original.getId(), loggedInUser) == null) {
                throw new LookupException("User " + loggedInUser.getLogin() +
                        " does not have access to channel " + original.getLabel());
            }
        }


        if (!UserManager.verifyChannelAdmin(loggedInUser, channel)) {
            throw new PermissionCheckFailureException();
        }

        List<Errata> errataToClone = new ArrayList<Errata>();
        List<Long> errataIds = new ArrayList<Long>();
        Optional<Org> originalChannelOrg = ofNullable(original).map(c -> c.getOrg());
        //We loop through once, making sure all the errata exist
        for (String advisory : advisoryNames) {
            Errata toClone = lookupAccessibleErratum(advisory, originalChannelOrg, loggedInUser.getOrg());
            errataToClone.add(toClone);
            errataIds.add(toClone.getId());
        }

        if (asynchronous) {
            ErrataManager.cloneErrataApiAsync(channel, errataIds, loggedInUser,
                    inheritPackages);
            return new ArrayList<Errata>().toArray();
        }
        else if (ErrataManager.channelHasPendingAsyncCloneJobs(channel)) {
            throw new InvalidChannelException(
                    "Channel " +
                            channel.getLabel() +
                            " has pending asynchronous errata clone jobs. You must wait" +
                    "until asychronous errata clone jobs are done.");
        }
        else {
            return ErrataManager.cloneErrataApi(channel, errataToClone,
                    loggedInUser, inheritPackages);
        }
    }

    /**
     * Tries to return an {@link Errata} matching given advisory in given organisation (if provided).
     * - If no such {@link Errata} is found, falls back to returning an {@link Errata} matching given advisory in the
     * logged-in user organisation.
     * - If no such {@link Errata} is found, falls back to returning a _vendor_ {@link Errata} matching given advisory.
     * - If no such {@link Errata} is found, throws a {@link FaultException}
     *
     * @param advisory the advisory
     * @param org the optional organization
     * @param loggedInUserOrg the logged-in user organization
     * @return matching errata in given org or logged-in-user org
     * @throws FaultException if no matching errata is found
     */
    private Errata lookupAccessibleErratum(String advisory, Optional<Org> org, Org loggedInUserOrg) {
        return org
                .flatMap(o -> ofNullable(ErrataManager.lookupByAdvisoryAndOrg(advisory, o)))
                .or(() -> ofNullable(ErrataManager.lookupByAdvisoryAndOrg(advisory, loggedInUserOrg)))
                .or(() -> ofNullable(ErrataManager.lookupByAdvisoryAndOrg(advisory, null))) // vendor errata
                .orElseThrow(() -> new FaultException(-208, "no_such_patch",
                        "The patch " + advisory + " cannot be found."));
    }

    /**
     * Clones a list of errata into a specified cloned channel
     * according the original erratas
     *
     * @param loggedInUser The current user
     * @param channelLabel the cloned channel's label that we are cloning into
     * @param advisoryNames an array of String objects containing the advisory name
     *          of every errata you want to clone
     * @throws InvalidChannelRoleException if the user perms are incorrect
     * @return Returns an array of Errata objects, which get serialized into XMLRPC
     *
     * @xmlrpc.doc Clones a list of errata into a specified cloned channel
     * according the original erratas.
     *
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("string", "channel_label")
     * @xmlrpc.param
     *     #array_single("string", " advisory - The advisory name of the errata to clone.")
     * @xmlrpc.returntype
     *          #array_begin()
     *              $ErrataSerializer
     *          #array_end()
     */
    public Object[] cloneAsOriginal(User loggedInUser, String channelLabel,
            List<String> advisoryNames) throws InvalidChannelRoleException {
        return clone(loggedInUser, channelLabel, advisoryNames, true, false);
    }

    /**
     * Asynchronously clones a list of errata into a specified cloned channel
     * according the original erratas
     *
     * @param loggedInUser The current user
     * @param channelLabel the cloned channel's label that we are cloning into
     * @param advisoryNames an array of String objects containing the advisory name
     *          of every errata you want to clone
     * @throws InvalidChannelRoleException if the user perms are incorrect
     * @return 1 on success, exception thrown otherwise.
     *
     * @xmlrpc.doc Asynchronously clones a list of errata into a specified cloned channel
     * according the original erratas
     *
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("string", "channel_label")
     * @xmlrpc.param
     *     #array_single("string", " advisory - The advisory name of the errata to clone.")
     * @xmlrpc.returntype
     *          #return_int_success()
     */
    public int cloneAsOriginalAsync(User loggedInUser, String channelLabel,
            List<String> advisoryNames) throws InvalidChannelRoleException {
        clone(loggedInUser, channelLabel, advisoryNames, true, true);
        return 1;
    }


    private Object getRequiredAttribute(Map map, String attribute) {
        Object value = map.get(attribute);
        if (value == null || StringUtils.isEmpty(value.toString())) {
            throw new MissingErrataAttributeException(attribute);
        }
        return value;
    }

    /**
     * creates an errata
     * @param loggedInUser The current user
     * @param errataInfo map containing the following values:
     *  String "synopsis" short synopsis of the errata
     *  String "advisory_name" advisory name of the errata
     *  Integer "advisory_release" release number of the errata
     *  String "advisory_type" the type of advisory for the errata (Must be one of the
     *          following: "Security Advisory", "Product Enhancement Advisory", or
     *          "Bug Fix Advisory"
     *  String "advirory_status" the status of advisory for the errata
     *         (Must be one of the following: "final", "stable", "testing", "retracted")
     *  String "product" the product the errata affects
     *  String "errataFrom" the author of the errata
     *  String "topic" the topic of the errata
     *  String "description" the description of the errata
     *  String "solution" the solution of the errata
     *  String "references" references of the errata to be created
     *  String "notes" notes on the errata
     *  String "severity" is name of given security advisory severity (Must be one of the
     *          following: 'Low', 'Moderate', 'Important', 'Critical' or 'Unspecified')
     * @param bugs a List of maps consisting of 'id' Integers and 'summary' strings
     * @param keywords a List of keywords for the errata
     * @param packageIds a List of package Id packageId Integers
     * @param channelLabels an array of channel labels to add patches to
     * @throws InvalidChannelRoleException if the user perms are incorrect
     * @return The errata created
     *
     * @xmlrpc.doc Create a custom errata
     * @xmlrpc.param #session_key()
     * @xmlrpc.param
     *      #struct_begin("errata info")
     *          #prop("string", "synopsis")
     *          #prop("string", "advisory_name")
     *          #prop("int", "advisory_release")
     *          #prop_desc("string", "advisory_type", "Type of advisory (one of the
     *                  following: 'Security Advisory', 'Product Enhancement Advisory',
     *                  or 'Bug Fix Advisory'")
     *          #prop_desc("string", "advisory_status", "Status of advisory (one of the
     *                  following: 'final', 'testing', 'stable' or 'retracted'")
     *          #prop("string", "product")
     *          #prop("string", "errataFrom")
     *          #prop("string", "topic")
     *          #prop("string", "description")
     *          #prop("string", "references")
     *          #prop("string", "notes")
     *          #prop("string", "solution")
     *          #prop_desc("string", "severity", "Severity of advisory (one of the
     *                  following: 'Low', 'Moderate', 'Important', 'Critical'
     *                  or 'Unspecified'")
     *       #struct_end()
     *  @xmlrpc.param
     *       #array_begin()
     *              #struct_begin("bug")
     *                  #prop_desc("int", "id", "Bug Id")
     *                  #prop("string", "summary")
     *                  #prop("string", "url")
     *               #struct_end()
     *       #array_end()
     * @xmlrpc.param #array_single("string", "keyword - List of keywords to associate
     *              with the errata.")
     * @xmlrpc.param #array_single("int", "packageId")
     * @xmlrpc.param
     *       #array_single("string", "channelLabel - list of channels the errata should be
     *                  published to")
     * @xmlrpc.returntype
     *      $ErrataSerializer
     */
    public Errata create(User loggedInUser, Map<String, Object> errataInfo,
                         List<Map<String, Object>> bugs, List<String> keywords,
                         List<Integer> packageIds, List<String> channelLabels)
            throws InvalidChannelRoleException {

        // confirm that the user only provided valid keys in the map
        Set<String> validKeys = new HashSet<String>();
        validKeys.add("synopsis");
        validKeys.add("advisory_name");
        validKeys.add("advisory_release");
        validKeys.add("advisory_type");
        validKeys.add("advisory_status");
        validKeys.add("product");
        validKeys.add("errataFrom");
        validKeys.add("topic");
        validKeys.add("description");
        validKeys.add("references");
        validKeys.add("notes");
        validKeys.add("solution");
        validKeys.add("severity");
        validateMap(validKeys, errataInfo);

        validKeys.clear();
        validKeys.add("id");
        validKeys.add("summary");
        validKeys.add("url");
        for (Map<String, Object> bugMap : bugs) {
            validateMap(validKeys, bugMap);
        }

        //Don't want them to add an errata without any channels,
        //so check first before creating anything
        List<String> allowedList = Config.get().getList(ConfigDefaults.ALLOW_ADDING_PATCHES_VIA_API);
        List<Channel> channels = verifyChannelList(channelLabels, loggedInUser, allowedList);

        String synopsis = (String) getRequiredAttribute(errataInfo, "synopsis");
        String advisoryName = (String) getRequiredAttribute(errataInfo, "advisory_name");
        Integer advisoryRelease = (Integer) getRequiredAttribute(errataInfo,
                "advisory_release");
        if (advisoryRelease.longValue() > ErrataManager.MAX_ADVISORY_RELEASE) {
            throw new InvalidAdvisoryReleaseException(advisoryRelease.longValue());
        }
        String advisoryType = (String) getRequiredAttribute(errataInfo, "advisory_type");
        String advisoryStatusString = (String) errataInfo.getOrDefault("advisory_status", "final");
        AdvisoryStatus advisoryStatus = AdvisoryStatus.fromMetadata(advisoryStatusString)
                .filter(a -> a != AdvisoryStatus.RETRACTED)
                .orElseThrow(() -> new InvalidAdvisoryTypeException(advisoryStatusString));
        String product = (String) getRequiredAttribute(errataInfo, "product");
        String errataFrom = (String) errataInfo.get("errataFrom");
        String topic = (String) getRequiredAttribute(errataInfo, "topic");
        String description = (String) getRequiredAttribute(errataInfo, "description");
        String solution = (String) getRequiredAttribute(errataInfo, "solution");
        String references = (String) errataInfo.get("references");
        String notes = (String) errataInfo.get("notes");
        String severity = (String) errataInfo.get("severity");

        Errata newErrata = ErrataManager.lookupByAdvisoryAndOrg(advisoryName,
                loggedInUser.getOrg());
        if (newErrata != null) {
            throw new DuplicateErrataException(advisoryName);
        }
        newErrata = new Errata();
        newErrata.setOrg(loggedInUser.getOrg());

        //all required
        newErrata.setSynopsis(synopsis);
        newErrata.setAdvisory(advisoryName + "-" + advisoryRelease.toString());
        newErrata.setAdvisoryName(advisoryName);
        newErrata.setAdvisoryRel(advisoryRelease.longValue());

        if (advisoryType.equals("Security Advisory") ||
                advisoryType.equals("Product Enhancement Advisory") ||
                advisoryType.equals("Bug Fix Advisory")) {

            newErrata.setAdvisoryType(advisoryType);
        }
        else {
            throw new InvalidAdvisoryTypeException(advisoryType);
        }

        newErrata.setAdvisoryStatus(advisoryStatus);
        newErrata.setProduct(product);
        newErrata.setTopic(topic);
        newErrata.setDescription(description);
        newErrata.setSolution(solution);
        newErrata.setIssueDate(new Date());
        newErrata.setUpdateDate(new Date());

        //not required
        newErrata.setErrataFrom(errataFrom);
        newErrata.setRefersTo(references);
        newErrata.setNotes(notes);
        if (errataInfo.get("severity") != null) {
            newErrata.setSeverity(Severity.getByName(severity));
        }
        for (Iterator<Map<String, Object>> itr = bugs.iterator(); itr.hasNext();) {
            Map<String, Object> bugMap = itr.next();
            String url = null;
            if (bugMap.containsKey("url")) {
                url = (String) bugMap.get("url");
            }

            Bug bug = ErrataFactory.createBug(
                    ((Integer)bugMap.get("id")).longValue(),
                    (String)bugMap.get("summary"), url);
            newErrata.addBug(bug);
        }
        for (Iterator<String> itr = keywords.iterator(); itr.hasNext();) {
            String keyword = itr.next();
            newErrata.addKeyword(keyword);
        }

        newErrata.setPackages(new HashSet());
        for (Iterator<Integer> itr = packageIds.iterator(); itr.hasNext();) {
            Integer pid = itr.next();
            Package pack = PackageFactory.lookupByIdAndOrg(pid.longValue(),
                    loggedInUser.getOrg());
            if (pack != null) {
                newErrata.addPackage(pack);
            }
            else {
                throw new InvalidPackageException(pid.toString());
            }
        }

        ErrataFactory.save(newErrata);
        List<Channel> vendorChannels = channels.stream().filter(c -> c.isVendorChannel()).collect(toList());
        if (!vendorChannels.isEmpty()) {
            log.warn("Errata " + newErrata.getAdvisory() + " added to vendor channels " +
                    vendorChannels.stream().map(Channel::getLabel).collect(Collectors.joining(",")));
        }

        return addToChannels(newErrata, channels, loggedInUser, false);
    }

    /**
     * Delete an erratum.
     * @param loggedInUser The current user
     * @param advisoryName The advisory Name of the erratum to delete
     * @throws FaultException if unknown or invalid erratum is provided.
     * @return 1 on success, exception thrown otherwise.
     *
     * @xmlrpc.doc Delete an erratum.  This method will only allow for deletion
     * of custom errata created either through the UI or API.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("string", "advisoryName")
     * @xmlrpc.returntype #return_int_success()
     */
    public Integer delete(User loggedInUser, String advisoryName)
            throws FaultException {
        Errata errata = lookupErrata(advisoryName, loggedInUser.getOrg());

        ErrataManager.deleteErratum(loggedInUser, errata);
        return 1;
    }

    /**
     * Adds an existing errata to a set of channels
     * @param loggedInUser The current user
     * @param advisory The advisory Name of the errata to add
     * @param channelLabels List of channels to add the errata to
     * @return the added errata
     *
     * @xmlrpc.doc Adds an existing errata to a set of channels.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("string", "advisoryName")
     * @xmlrpc.param
     *      #array_single("string", "channelLabel - list of channel labels to add to")
     * @xmlrpc.returntype
     *          $ErrataSerializer
     */
    public Errata publish(User loggedInUser, String advisory, List<String> channelLabels) {
        List<String> allowedList = Config.get().getList(ConfigDefaults.ALLOW_ADDING_PATCHES_VIA_API);
        List<Channel> channels = verifyChannelList(channelLabels, loggedInUser, allowedList);
        List<Channel> vendorChannels = channels.stream().filter(c -> c.isVendorChannel()).collect(toList());
        Errata toPublish = lookupAccessibleErratum(advisory, empty(), loggedInUser.getOrg());
        if (!vendorChannels.isEmpty()) {
            log.warn("Errata " + toPublish.getAdvisory() + " added to vendor channels " +
                    vendorChannels.stream().map(Channel::getLabel).collect(Collectors.joining(",")));
        }
        return addToChannels(toPublish, channels, loggedInUser, false);
    }

    /**
     * Adds an existing cloned errata to a set of cloned channels
     * according to its original erratum
     * @param loggedInUser The current user
     * @param advisory The advisory Name of the errata to add
     * @param channelLabels List of channels to add the errata to
     * @throws InvalidChannelRoleException if the user perms are incorrect
     * @return the added errata
     *
     * @xmlrpc.doc Adds an existing cloned errata to a set of cloned
     * channels according to its original erratum
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("string", "advisoryName")
     * @xmlrpc.param
     *      #array_single("string", "channelLabel - list of channel labels to add to")
     * @xmlrpc.returntype
     *          $ErrataSerializer
     */
    public Errata publishAsOriginal(User loggedInUser, String advisory,
            List<String> channelLabels) throws InvalidChannelRoleException {
        List<Channel> channels = verifyChannelList(channelLabels, loggedInUser, Collections.emptyList());
        for (Channel c : channels) {
            ClonedChannel cc = null;
            try {
                cc = (ClonedChannel) c;
            }
            catch (ClassCastException e) {
                // just catch, do not do anything
            }
            finally {
                if (cc == null || !cc.isCloned()) {
                    throw new InvalidChannelException("Cloned channel " +
                            "expected: " + c.getLabel());
                }
            }
            Channel original = ChannelFactory.lookupOriginalChannel(c);
            if (original == null) {
                throw new InvalidChannelException("Cannot access original " +
                        "of the channel: " + c.getLabel());
            }
            // check access to the original
            if (ChannelFactory.lookupByIdAndUser(original.getId(), loggedInUser) == null) {
                throw new LookupException("User " + loggedInUser.getLogin() +
                        " does not have access to channel " + original.getLabel());
            }
        }
        Errata toPublish = lookupErrata(advisory, loggedInUser.getOrg());
        if (!toPublish.isCloned()) {
            throw new InvalidErrataException("Cloned errata expected.");
        }
        return addToChannels(toPublish, channels, loggedInUser, true);
    }

    /**
     * Verify a list of channels labels, and populate their corresponding
     * Channel objects into a List.  This is primarily used before adding errata
     * to verify all channels are valid before starting the errata creation
     * @param channelsLabels the List of channel labels to verify
     * @param vendorChannelOverride list of vendor channel labels to allow without permission.
     * @return a List of channel objects
     */
    private List<Channel> verifyChannelList(List<String> channelsLabels, User user,
                                            List<String> vendorChannelOverride) {
        if (channelsLabels.size() == 0) {
            throw new NoChannelsSelectedException();
        }

        List<Channel> resolvedList = new ArrayList<>();
        for (String channelLabel: channelsLabels) {
            Channel channel = ChannelFactory.lookupByLabelAndUser(channelLabel, user);
            if (channel == null) {
                throw new InvalidChannelLabelException();
            }
            if (!UserManager.verifyChannelAdmin(user, channel)) {
                boolean allowed = channel.isVendorChannel() &&
                        vendorChannelOverride.contains(channel.getLabel()) &&
                        user.hasRole(RoleFactory.SAT_ADMIN);
                if (!allowed) {
                    throw new PermissionCheckFailureException();
                }
            }
            resolvedList.add(channel);
        }
        return resolvedList;
    }

    /**
     * private helper method to add the errata to channels
     * @param errata the errata to add
     * @param channels A list of channel objects
     * @return The added Errata
     */
    private Errata addToChannels(Errata errata, List<Channel> channels, User user, boolean inheritPackages) {
        for (Channel chan : channels) {
            errata = ErrataFactory.addToChannel(List.of(errata), chan, user, inheritPackages).get(0);
        }
        return errata;
    }

    /**
     * Lookup the details for errata associated with the given CVE.
     * @param loggedInUser The current user
     * @param cveName name of the CVE
     * @return List of Errata objects
     *
     * @xmlrpc.doc Lookup the details for errata associated with the given CVE
     * (e.g. CVE-2008-3270)
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("string", "cveName")
     * @xmlrpc.returntype
     *          #array_begin()
     *              $ErrataSerializer
     *          #array_end()
     */
    public List<Errata> findByCve(User loggedInUser, String cveName) {
        // Get the logged in user. We don't care what roles this user has, we
        // just want to make sure the caller is logged in.

        List<Errata> erratas = ErrataManager.lookupByCVE(cveName);
        for (Iterator<Errata> iter = erratas.iterator(); iter.hasNext();) {
            Errata errata = iter.next();
            // Remove errata that do not apply to the user's org
            if (errata.getOrg() != null &&
                    !errata.getOrg().equals(loggedInUser.getOrg())) {
                iter.remove();
            }
        }
        return erratas;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<String> getReadonlyMethodNames() {
        return RO_METHODS;
    }
}
