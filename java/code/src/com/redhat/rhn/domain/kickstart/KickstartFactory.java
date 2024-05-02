/*
 * Copyright (c) 2009--2017 Red Hat, Inc.
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
package com.redhat.rhn.domain.kickstart;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.util.FileUtils;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.kickstart.crypto.CryptoKey;
import com.redhat.rhn.domain.kickstart.crypto.CryptoKeyType;
import com.redhat.rhn.domain.kickstart.crypto.SslCryptoKey;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.rhnpackage.PackageName;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.frontend.action.kickstart.KickstartTreeUpdateType;
import com.redhat.rhn.manager.kickstart.KickstartFormatter;
import com.redhat.rhn.manager.kickstart.KickstartManager;
import com.redhat.rhn.manager.kickstart.KickstartUrlHelper;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerXMLRPCHelper;
import com.redhat.rhn.manager.system.SystemManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cobbler.Profile;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.query.Query;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;


/**
 * KickstartFactory
 */
public class KickstartFactory extends HibernateFactory {

    private static final int IN_CLAUSE_MAX_SIZE = 1000;
    private static KickstartFactory singleton = new KickstartFactory();
    private static Logger log = LogManager.getLogger(KickstartFactory.class);


    public static final CryptoKeyType KEY_TYPE_GPG = lookupKeyType("GPG");
    public static final CryptoKeyType KEY_TYPE_SSL = lookupKeyType("SSL");
    public static final KickstartSessionState SESSION_STATE_FAILED =
            lookupSessionStateByLabel("failed");
    public static final KickstartSessionState SESSION_STATE_CREATED =
            lookupSessionStateByLabel("created");
    public static final KickstartSessionState SESSION_STATE_STARTED =
            lookupSessionStateByLabel("started");
    public static final KickstartSessionState SESSION_STATE_COMPLETE =
            lookupSessionStateByLabel("complete");
    public static final KickstartSessionState SESSION_STATE_CONFIG_ACCESSED =
            lookupSessionStateByLabel("configuration_accessed");

    public static final KickstartVirtualizationType VIRT_TYPE_PV_HOST =
            lookupKickstartVirtualizationTypeByLabel(KickstartVirtualizationType.PARA_HOST);

    public static final KickstartVirtualizationType VIRT_TYPE_XEN_PV =
            lookupKickstartVirtualizationTypeByLabel("xenpv");

    private static final String KICKSTART_CANCELLED_MESSAGE =
            "Kickstart cancelled due to action removal";

    public static final KickstartTreeType TREE_TYPE_EXTERNAL =
            lookupKickstartTreeTypeByLabel("externally-managed");

    private static final String ORG_ID = "org_id";
    private static final String LABEL = "label";

    private KickstartFactory() {
        super();
    }


    /**
     * Get the Logger for the derived class so log messages
     * show up on the correct class
     */
    @Override
    protected Logger getLogger() {
        return log;
    }

    private static CryptoKeyType lookupKeyType(String label) {
        return (CryptoKeyType) HibernateFactory.getSession()
                .getNamedQuery("CryptoKeyType.findByLabel")
                .setParameter(LABEL, label)
                .uniqueResult();
    }

    /**
     * Returns the most recently modified KickstartableTree for that channel
     * @param updateType The type of kickstart tree to allow, all or redhat-only
     * @param channelId The base chanenl
     * @param org The users org to look for cusome kickstart trees in
     * @return most recently modified KickstartableTree
     */
    public static KickstartableTree getNewestTree(
            KickstartTreeUpdateType updateType,
            Long channelId, Org org) {
        List<KickstartableTree> trees = null;
        if (updateType.equals(KickstartTreeUpdateType.ALL)) {
            trees = KickstartManager.getInstance().removeInvalid(
                    lookupKickstartableTrees(channelId, org));
            if (trees.isEmpty()) {
                return null;
            }
            // they are ordered by last_modified, get the last element to get
            // newest
            // tree
            return trees.get(trees.size() - 1);
        }
        else if (updateType.equals(KickstartTreeUpdateType.RED_HAT)) {
            trees = KickstartManager.getInstance().removeInvalid(
                    lookupKickstartTreesByChannelAndNullOrg(channelId));
            // This is harder, if they got synced at the same time they would
            // have
            // the same last_modified date. since they all are for the same base
            // channel then we'll grep the last part of the ks tree label and
            // guess
            // based on that.
            KickstartableTree newest = null;
            int newestUpdate = -1;
            for (KickstartableTree tree : trees) {
                String[] update = tree.getLabel().split("-");
                String major = update[update.length - 2];
                String minor = update[update.length - 1];
                // if major is not actually the major version, this must be an
                // initial release, e.g. "...-server-5".
                if (!major.matches("^\\d*$")) {
                    minor = "0";
                }
                else {
                    if (minor.contains(".")) {
                        String[] parts = minor.split("\\.");
                        minor = parts[parts.length - 1];
                    }
                    else if (minor.contains("u")) {
                        String[] parts = minor.split("u");
                        minor = parts[parts.length - 1];
                    }

                }
                int updateInt = Integer.parseInt(minor);
                if (updateInt > newestUpdate) {
                    newestUpdate = updateInt;
                    newest = tree;
                }

            }
            return newest;
        }
        else {
            return null;
        }
    }

    /**
     * @param orgIn Org associated with Kickstart Data
     * @param ksid Kickstart Data Id to lookup
     * @return Kickstart Data object by ksid
     */
    public static KickstartData lookupKickstartDataByIdAndOrg(Org orgIn, Long ksid) {
        return (KickstartData)  HibernateFactory.getSession()
                .getNamedQuery("KickstartData.findByIdAndOrg")
                .setParameter("id", ksid, LongType.INSTANCE)
                .setParameter(ORG_ID, orgIn.getId(), LongType.INSTANCE)
                .uniqueResult();
    }

    /**
     * @param orgIn Org associated with Kickstart Data
     * @param cobblerId Kickstart Data Cobbler Id Id to lookup
     * @return Kickstart Data object by cobbler id
     */
    public static KickstartData lookupKickstartDataByCobblerIdAndOrg(Org orgIn,
            String cobblerId) {
        return (KickstartData)  HibernateFactory.getSession()
                .getNamedQuery("KickstartData.findByCobblerIdAndOrg")
                .setParameter("id", cobblerId)
                .setParameter(ORG_ID, orgIn.getId(), LongType.INSTANCE)
                .uniqueResult();
    }

    /**
     * Lookup a KickstartData based on a label and orgId
     * @param label to lookup
     * @param orgId who owns KickstartData
     * @return KickstartData if found, null if not
     */
    public static KickstartData lookupKickstartDataByLabelAndOrgId(
            String label, Long orgId) {
        if (StringUtils.isBlank(label)) {
            throw new IllegalArgumentException("kickstartLabel cannot be null");
        }
        return (KickstartData) HibernateFactory.getSession().
                getNamedQuery("KickstartData.findByLabelAndOrg")
                .setParameter(LABEL, label)
                .setParameter(ORG_ID, orgId, LongType.INSTANCE)
                .uniqueResult();
    }

    /**
     * Lookup a KickstartData based on a case insensitive label and orgId
     * This is needed due to the cobbler converts the kickstart profiles to lowecase
     * @param label to lookup
     * @param orgId who owns KickstartData
     * @return KickstartData if found, null if not
     */
    public static KickstartData lookupKickstartDataByCILabelAndOrgId(
            String label, Long orgId) {
        if (StringUtils.isBlank(label)) {
            throw new IllegalArgumentException("kickstartLabel cannot be null");
        }
        return (KickstartData) HibernateFactory.getSession().
                getNamedQuery("KickstartData.findByCILabelAndOrg")
                .setParameter(LABEL, label)
                .setParameter(ORG_ID, orgId, LongType.INSTANCE)
                .uniqueResult();
    }

    /**
     * Lookup a KickstartData based on a label
     * @param label to lookup
     * @return KickstartData if found, null if not
     */
    public static KickstartData lookupKickstartDataByLabel(
            String label) {
        if (StringUtils.isBlank(label)) {
            throw new IllegalArgumentException("kickstartLabel cannot be null");
        }
        return (KickstartData) HibernateFactory.getSession().
                getNamedQuery("KickstartData.findByLabel")
                .setParameter(LABEL, label)
                .uniqueResult();
    }


    /**
     * Returns a list of kickstart data cobbler ids
     * this is useful for cobbler only profiles..
     * @return a list of cobbler ids.
     */
    public static List<String> listKickstartDataCobblerIds() {
        return singleton.listObjectsByNamedQuery("KickstartData.cobblerIds", Collections.emptyMap());

    }

    /**
     * lookup kickstart tree by it's cobbler id
     * @param cobblerId the cobbler id to lookup
     * @return the Kickstartable Tree object
     */
    public static KickstartableTree lookupKickstartTreeByCobblerIdOrXenId(String cobblerId) {
        return singleton.lookupObjectByNamedQuery("KickstartableTree.findByCobblerIdOrXenId",
                Map.of("cid", cobblerId));
    }


    private static List<KickstartCommandName> lookupKickstartCommandNames(boolean onlyAdvancedOptions) {
        String query = "KickstartCommandName.listAllOptions";
        if (onlyAdvancedOptions) {
            query = "KickstartCommandName.listAdvancedOptions";
        }

        Session session = HibernateFactory.getSession();
        List<KickstartCommandName> names = session.getNamedQuery(query).setCacheable(true).list();

        // Filter out the unsupported Commands for the passed in profile
        return names.stream()
                .filter(cn -> !cn.getName().equals("lilocheck") && !cn.getName().equals("langsupport"))
                .collect(Collectors.toList());
    }

    /**
     * Get the list of KickstartCommandName objects that are supportable.
     * Filters out unsupported commands.
     *
     * @return List of advanced KickstartCommandNames. Does not include partitions,
     * logvols, raids, varlogs or includes which is displayed sep in the UI.
     */
    public static List<KickstartCommandName> lookupKickstartCommandNames() {
        return lookupKickstartCommandNames(true);
    }

    /**
     * Get the list of KickstartCommandName objects that are supportable.
     * Filters out unsupported commands.
     *
     * @return List of  KickstartCommandNames.
     */
    public static List<KickstartCommandName> lookupAllKickstartCommandNames() {
        return lookupKickstartCommandNames(false);
    }

    /**
     * Looks up a specific KickstartCommandName
     * @param commandName name of the KickstartCommandName
     * @return found instance, if any
     */
    public static KickstartCommandName lookupKickstartCommandName(String commandName) {
        Session session = HibernateFactory.getSession();
        Query<KickstartCommandName> query = session.getNamedQuery("KickstartCommandName.findByLabel");
        //Retrieve from cache if there
        query.setCacheable(true);
        query.setParameter("name", commandName);
        return query.uniqueResult();

    }

    /**
     * Create a new KickstartCommand object
     * @param ksdata to associate with
     * @param nameIn of KickstartCommand
     * @return KickstartCommand created
     */
    public static KickstartCommand createKickstartCommand(KickstartData ksdata,
            String nameIn) {
        KickstartCommand retval = new KickstartCommand();
        KickstartCommandName name =
                KickstartFactory.lookupKickstartCommandName(nameIn);
        retval.setCommandName(name);
        retval.setKickstartData(ksdata);
        retval.setCreated(new Date());
        retval.setModified(new Date());
        ksdata.addCommand(retval);
        return retval;
    }

    /**
     *
     * @return List of required advanced Kickstart Command Names. Does not include
     * partitions, logvols, raids, varlogs or includes.
     */
    public static List<KickstartCommandName> lookupKickstartRequiredOptions() {
        String query = "KickstartCommandName.requiredOptions";
        Session session = HibernateFactory.getSession();
        return session.getNamedQuery(query)
                //Retrieve from cache if there
                .setCacheable(true).list();
    }

    /**
     * Insert or Update a CryptoKey.
     * @param cryptoKeyIn CryptoKey to be stored in database.
     */
    public static void saveCryptoKey(CryptoKey cryptoKeyIn) {
        singleton.saveObject(cryptoKeyIn);
    }

    /**
     * remove a CryptoKey from the DB.
     * @param cryptoKeyIn CryptoKey to be removed from the database.
     */
    public static void removeCryptoKey(CryptoKey cryptoKeyIn) {
        singleton.removeObject(cryptoKeyIn);
    }

    /**
     * Insert or Update a Command.
     * @param commandIn Command to be stored in database.
     */
    public static void saveCommand(KickstartCommand commandIn) {
        singleton.saveObject(commandIn);
    }


    /**
     * Save a KickstartData to the DB and associate
     * the storage with the KickstartSession passed in.  This is
     * used if you want to save the KickstartData and associate the
     *
     * @param ksdataIn Kickstart Data to be stored in db
     * @param ksession KickstartSession to associate with this save.
     */
    public static void saveKickstartData(KickstartData ksdataIn,
            KickstartSession ksession) {
        log.debug("saveKickstartData: {}", ksdataIn.getLabel());
        singleton.saveObject(ksdataIn);
        String fileData = null;
        if (ksdataIn.isRawData()) {
            log.debug("saveKickstartData is raw, use file");
            KickstartRawData rawData = (KickstartRawData) ksdataIn;
            fileData = rawData.getData();
        }
        else {
            log.debug("saveKickstartData wizard.  use object");
            KickstartFormatter formatter = new KickstartFormatter(
                    KickstartUrlHelper.COBBLER_SERVER_VARIABLE, ksdataIn, ksession);
            fileData = formatter.getFileData();
        }
        Profile p = Profile.lookupById(CobblerXMLRPCHelper.getAutomatedConnection(),
                ksdataIn.getCobblerId());
        if (p != null && p.getKsMeta().isPresent()) {
            Map<String, Object> ksmeta = p.getKsMeta().orElse(new HashMap<>());
            for (String name : ksmeta.keySet()) {
                log.debug("fixing ksmeta: {}", name);
                fileData = StringUtils.replace(fileData, "\\$" + name, "$" + name);
            }
        }
        else {
            log.debug("No ks meta for this profile.");
        }
        String path = ksdataIn.buildCobblerFileName();
        log.debug("writing ks file to : {}", path);
        FileUtils.writeStringToFile(fileData, ConfigDefaults.get().getKickstartConfigDir(), path);
    }

    private static String getKickstartTemplatePath(KickstartData ksdata, Profile p) {
        String path = ksdata.getCobblerFileName();
        if (p != null && p.getKickstart() != null) {
            path = p.getKickstart();
        }
        return path;
    }

    /**
     *
     * @param ksdataIn Kickstart Data to be stored in db
     */
    public static void saveKickstartData(KickstartData ksdataIn) {
        saveKickstartData(ksdataIn, null);
    }

    /**
     * @param ksdataIn Kickstart Data to be removed from the db
     * @return number of tuples affected by delete
     */
    public static int removeKickstartData(KickstartData ksdataIn) {
        removeKickstartTemplatePath(ksdataIn);
        return singleton.removeObject(ksdataIn);
    }

    /**
     * Removes ks cfg template path
     * @param ksdataIn kickstart data
     */
    public static void removeKickstartTemplatePath(KickstartData ksdataIn) {
        Profile p = Profile.lookupById(CobblerXMLRPCHelper.getAutomatedConnection(),
                ksdataIn.getCobblerId());
        String path = getKickstartTemplatePath(ksdataIn, p);
        if (path != null) {
            FileUtils.deleteFile(new File(path).toPath());
        }
    }

    /**
     * Lookup a crypto key by its description and org.
     * @param description to check
     * @param org to lookup in
     * @return CryptoKey if found.
     */
    public static CryptoKey lookupCryptoKey(String description, Org org) {
        Session session = HibernateFactory.getSession();
        Query<CryptoKey> query = null;
        if (org != null) {
            query = session.getNamedQuery("CryptoKey.findByDescAndOrg")
                           .setParameter("description", description)
                           .setParameter(ORG_ID, org.getId(), LongType.INSTANCE);
        }
        else {
            query = session.getNamedQuery("CryptoKey.findByDescAndNullOrg")
                           .setParameter("description", description);
        }
        return query.uniqueResult();
    }

    /**
     * Find all crypto keys for a given org
     * @param org owning org
     * @return list of crypto keys if some found, else empty list
     */
    public static List<CryptoKey> lookupCryptoKeys(Org org) {
        //look for Kickstart data by id
        Session session = HibernateFactory.getSession();
        return session.getNamedQuery("CryptoKey.findByOrg")
                .setParameter(ORG_ID, org.getId(), LongType.INSTANCE)
                .list();
    }

    /**
     * Find all ssl crypto keys for a given org
     * @param org owning org
     * @return list of ssl crypto keys if some found, else empty list
     */
    public static List<SslCryptoKey> lookupSslCryptoKeys(Org org) {
        //look for Kickstart data by id
        Session session = HibernateFactory.getSession();
        return session.getNamedQuery("SslCryptoKey.findByOrg")
                .setParameter(ORG_ID, org.getId(), LongType.INSTANCE)
                .list();
    }

    /**
     * Lookup a crypto key by its id.
     * @param keyId to lookup
     * @param org who owns the key
     * @return CryptoKey if found.  Null if not
     */
    public static CryptoKey lookupCryptoKeyById(Long keyId, Org org) {
        //look for Kickstart data by id
        Session session = HibernateFactory.getSession();
        return (CryptoKey) session.getNamedQuery("CryptoKey.findByIdAndOrg")
                .setParameter("key_id", keyId, LongType.INSTANCE)
                .setParameter(ORG_ID, org.getId(), LongType.INSTANCE)
                .uniqueResult();
    }

    /**
     * Lookup a ssl crypto key by its id.
     * @param keyId to lookup
     * @param org who owns the key
     * @return SslCryptoKey if found.  Null if not
     */
    public static SslCryptoKey lookupSslCryptoKeyById(Long keyId, Org org) {
        //look for Kickstart data by id
        Session session = HibernateFactory.getSession();
        Query<SslCryptoKey> query = session.getNamedQuery("SslCryptoKey.findByIdAndOrg");
        return query.setParameter("key_id", keyId, LongType.INSTANCE)
                .setParameter(ORG_ID, org.getId(), LongType.INSTANCE)
                .uniqueResult();
    }

    /**
     *
     * @param org who owns the Kickstart Range
     * @return List of Kickstart Ip Ranges if found
     */
    public static List<KickstartIpRange> lookupRangeByOrg(Org org) {
        Session session = HibernateFactory.getSession();
        return session.getNamedQuery("KickstartIpRange.lookupByOrg")
                .setParameter("org", org)
                .list();
    }

    /**
     * Lookup a KickstartableTree by its label.  If the Tree isnt owned
     * by the Org it will lookup a BaseChannel with a NULL Org under
     * the same label.
     *
     * @param label to lookup
     * @param org who owns the Tree.  If none found will lookup RHN owned Trees
     * @return KickstartableTree if found.
     */
    public static KickstartableTree lookupKickstartTreeByLabel(String label, Org org) {
        Session session = HibernateFactory.getSession();
        KickstartableTree retval = (KickstartableTree)
                session.getNamedQuery("KickstartableTree.findByLabelAndOrg")
                .setParameter(LABEL, label)
                .setParameter(ORG_ID, org.getId(), LongType.INSTANCE)
                .uniqueResult();
        // If we don't find by label + org then
        // we try by label and NULL org (RHN owned channel)
        if (retval == null) {
            retval = (KickstartableTree)
                    session.getNamedQuery("KickstartableTree.findByLabelAndNullOrg")
                    .setParameter(LABEL, label)
                    .uniqueResult();
        }
        return retval;
    }

    /**
     * Lookup a KickstartableTree by its label.
     *
     * @param label to lookup
     * @return KickstartableTree if found.
     */
    public static KickstartableTree lookupKickstartTreeByLabel(String label) {
        Session session = HibernateFactory.getSession();
        return (KickstartableTree) session.getNamedQuery("KickstartableTree.findByLabel")
                .setParameter(LABEL, label)
                .uniqueResult();
    }


    /**
     * Lookup a list of KickstartableTree objects that use the passed in channelId
     *
     * @param channelId that owns the kickstart trees
     * @param org who owns the trees
     * @return List of KickstartableTree objects
     */
    public static List<KickstartableTree> lookupKickstartTreesByChannelAndOrg(Long channelId, Org org) {
        String query = "KickstartableTree.findByChannelAndOrg";
        Session session = HibernateFactory.getSession();
        return session.getNamedQuery(query).
                setParameter("channel_id", channelId, LongType.INSTANCE).
                setParameter(ORG_ID, org.getId(), LongType.INSTANCE)
                //Retrieve from cache if there
                .setCacheable(true).list();
    }

    /**
     * Lookup a list of KickstartableTree objects that use the passed in channelId
     * @param channelId The base channel of the kickstart trees
     * @return List of KickstartableTree objects
     */
    public static List<KickstartableTree> lookupKickstartTreesByChannelAndNullOrg(
            Long channelId) {
        String query = "KickstartableTree.findByChannelAndNullOrg";
        Session session = HibernateFactory.getSession();
        return session.getNamedQuery(query)
                .setParameter("channel_id", channelId, LongType.INSTANCE)
                // Retrieve from cache if there
                .setCacheable(true).list();
    }

    /**
     * Lookup a list of KickstartableTree objects that use the passed in channelId
     *
     * @param channelId that owns the kickstart trees
     * @param org who owns the trees
     * @return List of KickstartableTree objects
     */
    public static List<KickstartableTree> lookupKickstartableTrees(
            Long channelId, Org org) {

        Session session = null;
        List<KickstartableTree> retval = null;
        String query = null;
        query = "KickstartableTree.findByChannel";
        session = HibernateFactory.getSession();
        retval = session.getNamedQuery(query).
                setParameter("channel_id", channelId, LongType.INSTANCE).
                setParameter(ORG_ID, org.getId(), LongType.INSTANCE).
                list();
        return retval;
    }


    /**
     * Fetch all trees for an org, these include
     * trees where org_id is null or org_id = org.id
     * @param org owning org
     * @return list of KickstartableTrees
     */
    public static List<KickstartableTree> lookupAccessibleTreesByOrg(Org org) {
        Map<String, Object> params = new HashMap<>();
        params.put(ORG_ID, org.getId());
        return singleton.listObjectsByNamedQuery(
                "KickstartableTree.findAccessibleToOrg", params, false);
    }

    /**
     * Return a list of KickstartableTree objects in the Org
     * @param org to lookup by
     * @return List of KickstartableTree objects if found
     */
    public static List<KickstartableTree> listTreesByOrg(Org org) {
        Map<String, Object> params = new HashMap<>();
        params.put(ORG_ID, org.getId());
        return singleton.listObjectsByNamedQuery(
                "KickstartableTree.findByOrg", params, false);
    }

    /**
     * List kickstart data by crypto key
     * @param ckDescription crypto key description
     * @return List of kickstart data with associated crypto key
     */
    public static List<KickstartData> listKickstartDataByCKeyDescription(
            String ckDescription) {
        Map<String, Object> params = new HashMap<>();
        params.put("ck_description", ckDescription);
        return singleton.listObjectsByNamedQuery(
                "KickstartData.findByCKeyDescription", params, true);
    }

    /**
     * list all kickstart trees stored in the satellite
     * @return list of kickstart trees
     */
    public static List<KickstartableTree> lookupKickstartTrees() {
        String query = "KickstartableTree.findBase";
        return singleton.listObjectsByNamedQuery(query, Collections.emptyMap(), false);
    }

    /**
     * Lookup KickstartableTree by tree id and org id
     * @param treeId desired tree
     * @param org owning org
     * @return KickstartableTree if found, otherwise null
     */
    public static KickstartableTree lookupKickstartTreeByIdAndOrg(Long treeId, Org org) {
        String queryName = "KickstartableTree.findByIdAndOrg";
        if (treeId != null && org != null) {
            Session session = HibernateFactory.getSession();
            Query<KickstartableTree> query = session.getNamedQuery(queryName);
            query.setParameter(ORG_ID, org.getId(), LongType.INSTANCE);
            query.setParameter("tree_id", treeId, LongType.INSTANCE);
            //Retrieve from cache if there
            return query.setCacheable(true).uniqueResult();
        }
        return null;
    }

    /**
     * Lookup a KickstartSession for a the passed in Server.  This method
     * finds the *most recent* KickstartSession associated with this Server.
     *
     * We use the serverId instead of the Hibernate object because this method gets
     * called by our ACL layer.
     *
     * @param sidIn id of the Server that you want to lookup the most
     * recent KickstartSession for
     * @return KickstartSession if found.
     */
    public static KickstartSession lookupKickstartSessionByServer(Long sidIn) {
        Session session = HibernateFactory.getSession();
        List<KickstartSession> ksessions = session.getNamedQuery("KickstartSession.findByServer")
                .setParameter("server", sidIn, LongType.INSTANCE)
                .list();
        if (!ksessions.isEmpty()) {
            return ksessions.iterator().next();
        }
        return null;
    }

    /**
     * Lookup most recent KickstartSession for a the passed in KickstartData
     *
     * @param ksdata object you want to get recent KickstartSession for
     * @return KickstartSession if found.
     */
    public static KickstartSession
    lookupDefaultKickstartSessionForKickstartData(KickstartData ksdata) {
        Session session = HibernateFactory.getSession();
        List<KickstartSession> ksessions = session.getNamedQuery(
                "KickstartSession.findDefaultKickstartSessionForKickstartData")
                .setParameter("ksdata", ksdata.getId(), LongType.INSTANCE)
                .setParameter("mode", KickstartSession.MODE_DEFAULT_SESSION)
                .list();
        if (!ksessions.isEmpty()) {
            return ksessions.iterator().next();
        }
        return null;
    }


    /**
     * Helper method to lookup KickstartSessionState by label
     * @param label Label to lookup
     * @return Returns the KickstartSessionState
     */
    public static KickstartSessionState lookupSessionStateByLabel(String label) {
        Session session = HibernateFactory.getSession();
        return (KickstartSessionState) session.getNamedQuery("KickstartSessionState.findByLabel")
                .setParameter(LABEL, label).uniqueResult();
    }

    /**
     * Save a KickstartSession object
     * @param ksession to save.
     */
    public static void saveKickstartSession(KickstartSession ksession) {
        singleton.saveObject(ksession);
        SystemManager.updateSystemOverview(ksession.getOldServer());
        SystemManager.updateSystemOverview(ksession.getNewServer());
    }

    /**
     * Get all the KickstartSessions associated with the passed in server id
     * @param sidIn of Server we want the Sessions for
     * @return List of KickstartSession objects
     */
    public static List<KickstartSession> lookupAllKickstartSessionsByServer(Long sidIn) {
        Session session = HibernateFactory.getSession();
        return session.getNamedQuery("KickstartSession.findByServer")
                .setParameter("server", sidIn, LongType.INSTANCE)
                .list();
    }

    /**
     * Lookup a KickstartSession by its id.
     * @param sessionId to lookup
     * @return KickstartSession if found.
     */
    public static KickstartSession lookupKickstartSessionById(Long sessionId) {
        Session session = HibernateFactory.getSession();
        return session.get(KickstartSession.class, sessionId);
    }

    private static KickstartTreeType lookupKickstartTreeTypeByLabel(String label) {
        Session session = HibernateFactory.getSession();
        return (KickstartTreeType) session.getNamedQuery("KickstartTreeType.findByLabel")
                .setParameter(LABEL, label).uniqueResult();
    }

    /**
     * Verfies that a given kickstart tree can be used based on a channel id
     * and org id
     * @param channelId base channel
     * @param orgId org
     * @param treeId kickstart tree
     * @return true if it can, false otherwise
     */
    public static boolean verifyTreeAssignment(Long channelId, Long orgId, Long treeId) {
        if (channelId != null && orgId != null && treeId != null) {
            Session session = HibernateFactory.getSession();
            Query<KickstartableTree> query = session.
                    getNamedQuery("KickstartableTree.verifyTreeAssignment");
            query.setParameter("channel_id", channelId, LongType.INSTANCE);
            query.setParameter(ORG_ID, orgId, LongType.INSTANCE);
            query.setParameter("tree_id", treeId, LongType.INSTANCE);
            KickstartableTree tree = query.uniqueResult();
            return tree != null;
        }
        return false;
    }

    /**
     * Load a tree based on its id and org id
     * @param treeId kickstart tree id
     * @param orgId org id
     * @return KickstartableTree instance if found, otherwise null
     */
    public static KickstartableTree findTreeById(Long treeId, Long orgId) {
        KickstartableTree retval = HibernateFactory.getSession().load(KickstartableTree.class, treeId);
        if (retval != null) {
            List<Channel> list = OrgFactory.lookupById(orgId).getAccessibleChannels();
            if (!list.contains(retval.getChannel())) {
                retval = null;
            }
        }
        return retval;
    }

    /**
     * Lookup a KickstartInstallType by label
     * @param label to lookup by
     * @return KickstartInstallType if found
     */
    public static KickstartInstallType lookupKickstartInstallTypeByLabel(String label) {
        Session session = HibernateFactory.getSession();
        return (KickstartInstallType) session.getNamedQuery("KickstartInstallType.findByLabel")
                .setParameter(LABEL, label).uniqueResult();
    }

    /**
     * Return a List of KickstartInstallType classes.
     * @return List of KickstartInstallType instances
     */
    public static List<KickstartInstallType> lookupKickstartInstallTypes() {
        String query = "KickstartInstallType.loadAll";
        Session session = HibernateFactory.getSession();

        //Retrieve from cache if there
        return session.getNamedQuery(query).setCacheable(true).list();
    }

    /**
     * Save the KickstartableTree to the DB.
     * @param tree to save
     */
    public static void saveKickstartableTree(KickstartableTree tree) {
        singleton.saveObject(tree);
    }

    /**
     * Remove KickstartableTree from the DB.
     * @param tree to delete
     */
    public static void removeKickstartableTree(KickstartableTree tree) {
        tree.removeSaltFS();
        singleton.removeObject(tree);
    }

    /**
     * Lookup a list of KickstartData objects by the KickstartableTree.
     *
     * Useful for finding KickstartData objects that are using a specified Tree.
     *
     * @param tree to lookup by
     * @return List of KickstartData objects if found
     */
    public static List<KickstartData> lookupKickstartDatasByTree(KickstartableTree tree) {
        String query = "KickstartData.lookupByTreeId";
        Session session = HibernateFactory.getSession();
        return session.getNamedQuery(query)
                .setParameter("kstree_id", tree.getId(), LongType.INSTANCE)
                .list();
    }

    /**
     * Lookup a list of all KickstartData objects located on the Satellite
     *  Should not be used by much.  Ignores org!
     * @return List of KickstartData objects if found
     */
    public static List<KickstartData> listAllKickstartData() {
        Session session = getSession();
        Criteria c = session.createCriteria(KickstartData.class);
        // Hibernate does not filter out duplicate references by default
        c.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        return c.list();
    }

    /**
     * Lookup a list of all KickstartData objects that can automatically
     * update their KS Tree.
     * @return List of KickstartData objects if found
     */
    public static List<KickstartData> lookupKickstartDataByUpdateable() {
        String query = "KickstartData.lookupByUpdateable";
        Session session = HibernateFactory.getSession();
        return session.getNamedQuery(query).list();
    }

    /**
     * Lookup a KickstartData that has its isOrgDefault value set to true
     * This may return null if there aren't any set.
     *
     * @param org who owns the Kickstart.
     * @return KickstartData if found
     */
    public static KickstartData lookupOrgDefault(Org org) {
        Session session = HibernateFactory.getSession();
        return (KickstartData) session
                .getNamedQuery("KickstartData.findOrgDefault")
                .setParameter("org", org)
                .setParameter("isOrgDefault", "Y", StringType.INSTANCE)
                .uniqueResult();
    }

    /**
     * Fetch all virtualization types
     * @return list of VirtualizationTypes
     */
    public static List<KickstartVirtualizationType> lookupVirtualizationTypes() {
        String query = "KickstartVirtualizationType.findAll";
        Session session = HibernateFactory.getSession();
        return session.getNamedQuery(query).setCacheable(true).list();
    }

    /**
     * Lookup a KickstartVirtualizationType by label
     * @param label to lookup by
     * @return KickstartVirtualizationType if found
     */
    public static KickstartVirtualizationType
    lookupKickstartVirtualizationTypeByLabel(String label) {
        Session session = HibernateFactory.getSession();
        return (KickstartVirtualizationType) session.getNamedQuery("KickstartVirtualizationType.findByLabel")
                .setParameter(LABEL, label).uniqueResult();
    }

    /**
     * Fail the kickstart sessions associated with the given actions and servers.
     *
     * @param actionsToDelete Actions associated with the kickstart sessions to fail.
     * @param servers Servers assocaited with the kickstart sessions to fail.
     */
    public static void failKickstartSessions(Set<Action> actionsToDelete, Set<Server> servers) {
        Session session = HibernateFactory.getSession();
        KickstartSessionState failed = KickstartFactory.SESSION_STATE_FAILED;
        Query<KickstartSession> kickstartSessionQuery = session.getNamedQuery(
                "KickstartSession.findPendingForActions");
        kickstartSessionQuery.setParameterList("actions_to_delete", actionsToDelete);
        int subStart = 0;
        List<Server> serverList = new ArrayList<>(servers);
        while (subStart < servers.size()) {
            int subLength = subStart + IN_CLAUSE_MAX_SIZE >= serverList.size() ?
                    serverList.size() - subStart : IN_CLAUSE_MAX_SIZE;
            List<Server> subClause = serverList.subList(subStart, subStart + subLength);
            subStart += subLength;
            kickstartSessionQuery.setParameterList("servers", subClause);
            List<KickstartSession> ksSessions = kickstartSessionQuery.list();
            for (KickstartSession ks : ksSessions) {
                log.debug("Failing kickstart associated with action: {}", ks.getId());
                ks.markFailed(KICKSTART_CANCELLED_MESSAGE);
            }
        }
    }

    /**
     * Set the kickstart session history message.
     *
     * Java version of the stored procedure set_ks_session_history_message. This procedure
     * attempted to iterate all states with the given label, but these are unique and
     * this method will not attempt to do the same.
     */
    private static void setKickstartSessionHistoryMessage(KickstartSession ksSession,
            KickstartSessionState state, String message) {
        Session session = HibernateFactory.getSession();
        Query<KickstartSessionHistory> q = session.getNamedQuery(
                "KickstartSessionHistory.findByKickstartSessionAndState");
        q.setParameter("state", state);
        q.setParameter("kickstartSession", ksSession);
        List<KickstartSessionHistory> results = q.list();
        results.forEach(history -> history.setMessage(message));

        ksSession.addHistory(state, message);
    }

    /**
     * Gets a kickstart script
     * @param org the org doing the request
     * @param id  the id of the script
     * @return the kickstartScript
     */
    public static KickstartScript lookupKickstartScript(Org org, Integer id) {
        KickstartScript script = HibernateFactory.getSession().load(KickstartScript.class, id.longValue());
        if (!org.equals(script.getKsdata().getOrg())) {
            return null;
        }
        return script;
    }

    /**
     * Completely remove a kickstart script from the system
     * @param script the script to remove
     */
    public static void removeKickstartScript(KickstartScript script) {
        singleton.removeObject(script);
    }


    /**
     * Get a list of all trees that have a cobbler id of null
     * @return list of trees
     */
    public static List<KickstartableTree> listUnsyncedKickstartTrees() {
        String query = "KickstartableTree.getUnsyncedKickstartTrees";
        Session session = HibernateFactory.getSession();
        return session.getNamedQuery(query).list();
    }

    /**
     * @param p KickstartPackage to add to DB
     */
    public static void savePackage(KickstartPackage p) {
        singleton.saveObject(p);
    }

    /**
     * @param p KickstartPackage to remove from DB
     */
    public static void removePackage(KickstartPackage p) {
        singleton.removeObject(p);
    }

    /**
     * @param ksData KcikstartData to lookup
     * @param packageName PackageName to lookup
     * @return KickstartPackge list
     */
    public static List<KickstartPackage> lookupKsPackageByKsDataAndPackageName(
            KickstartData ksData, PackageName packageName) {
        return HibernateFactory.getSession()
                .getNamedQuery("KickstartPackage.findByKickstartDataAndPackageName")
                .setParameter("ks_data", ksData.getId(), LongType.INSTANCE)
                .setParameter("package_name", packageName.getId(), LongType.INSTANCE)
                .list();
    }

    /**
     * Lists trees that are candidates for backward synchronization of 'kernel options'
     * and 'kernel options post' parameters.
     *
     * @return list of trees with non-null cobbler id and null kernel options and kernel
     * options post
     */
    public static List<KickstartableTree> listCandidatesForBacksync() {
        CriteriaBuilder builder = HibernateFactory.getSession().getCriteriaBuilder();
        CriteriaQuery<KickstartableTree> query = builder
                .createQuery(KickstartableTree.class);
        Root<KickstartableTree> root = query.from(KickstartableTree.class);
        query.select(root)
                .where(builder.and(
                        builder.isNotNull(root.get("cobblerId")),
                        builder.isNull(root.get("kernelOptions")),
                        builder.isNull(root.get("kernelOptionsPost"))));
        return HibernateFactory.getSession().createQuery(query).getResultList();
    }
}
