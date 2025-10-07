/*
 * Copyright (c) 2009--2017 Red Hat, Inc.
 * Copyright (c) 2011--2025 SUSE LLC
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
package com.redhat.rhn.domain.channel;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.db.datasource.CallableMode;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.Row;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.common.ChecksumType;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.scc.SCCRepository;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.appstreams.AppStreamsManager;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.content.ContentSyncManager;
import com.redhat.rhn.manager.content.MgrSyncUtils;
import com.redhat.rhn.manager.ssm.SsmChannelDto;

import com.suse.manager.model.hub.ChannelInfoDetailsJson;
import com.suse.manager.model.hub.HubFactory;
import com.suse.scc.SCCEndpoints;
import com.suse.scc.model.SCCRepositoryJson;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.hibernate.type.StandardBasicTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * ChannelFactory
 */
public class ChannelFactory extends HibernateFactory {

    private static final String CHANNEL_QUERIES = "Channel_queries";
    private static final String LABEL = "label";
    private static final String ORG_ID = "org_id";

    private static ChannelFactory singleton = new ChannelFactory();
    private static Logger log = LogManager.getLogger(ChannelFactory.class);

    private ChannelFactory() {
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

    /**
     * Lookup a Channel by its id
     *
     * @param id the id to search for
     * @return the Channel found
     */
    public static Channel lookupById(Long id) {
        Session session = HibernateFactory.getSession();
        return session.get(Channel.class, id);
    }

    /**
     * Lookup a Channel by id and User
     *
     * @param id     the id to search for
     * @param userIn User who is doing the looking
     * @return the Server found (null if not or not member if userIn)
     */
    public static Channel lookupByIdAndUser(Long id, User userIn) {
        if (id == null || userIn == null) {
            return null;
        }
        return singleton.lookupObjectByNamedQuery("Channel.findByIdAndUserId",
                Map.of("cid", id, "userId", userIn.getId()));
    }

    /**
     * Lookup a Channel by label and User
     *
     * @param label  the label to search for
     * @param userIn User who is doing the looking
     * @return the Server found (null if not or not member if userIn)
     */
    public static Channel lookupByLabelAndUser(String label, User userIn) {
        if (label == null || userIn == null) {
            return null;
        }
        return singleton.lookupObjectByNamedQuery("Channel.findByLabelAndUserId",
                Map.of(LABEL, label, "userId", userIn.getId()));
    }

    /**
     * Lookup a content source type by label
     *
     * @param label the label to lookup
     * @return the ContentSourceType
     */
    public static ContentSourceType lookupContentSourceType(String label) {
        Session session = HibernateFactory.getSession();
        return session.createQuery("FROM ContentSourceType AS c WHERE c.label = :label", ContentSourceType.class)
                .setParameter(LABEL, label)
                .uniqueResult();
    }

    /**
     * List all available content source types
     *
     * @return list of ContentSourceType
     */
    public static List<ContentSourceType> listContentSourceTypes() {
        Session session = HibernateFactory.getSession();
        return session.createQuery("FROM ContentSourceType", ContentSourceType.class)
                .list();
    }

    /**
     * Lookup a content source by org
     *
     * @param org the org to lookup
     * @return the ContentSource(s)
     */
    public static List<ContentSource> lookupContentSources(Org org) {
        return singleton.listObjectsByNamedQuery("ContentSource.findByOrg", Map.of("org", org));
    }

    /**
     * Lookup orphan vendor content source
     *
     * @return the ContentSource(s)
     */
    public static List<ContentSource> lookupOrphanVendorContentSources() {
        return singleton.listObjectsByNamedQuery("ContentSource.findOrphanVendorContentSources", Map.of());
    }

    /**
     * Lookup orphan vendor channels
     *
     * @return the Channels(s)
     */
    public static List<Channel> lookupOrphanVendorChannels() {
        return singleton.listObjectsByNamedQuery("Channel.findOrphanVendorChannels", Map.of());
    }

    /**
     * Remove all Vendor ContentSources which are not bound to a channel
     */
    public static void cleanupOrphanVendorContentSource() {
        List<ContentSource> unused = singleton.listObjectsByNamedQuery(
                "ContentSource.findUnusedVendorContentSources", Map.of());
        unused.forEach(ChannelFactory::remove);
    }

    /**
     * Lookup repository for given channel
     *
     * @param c the channel
     * @return repository
     */
    public static Optional<SCCRepository> findVendorRepositoryByChannel(Channel c) {
        return Optional.ofNullable(singleton.lookupObjectByNamedQuery("Channel.findVendorRepositoryByChannelId",
                Map.of("cid", c.getId())));
    }

    /**
     * List all available Vendor Channels created from a given SCC repository ID
     *
     * @param sccId the SCC Repository ID
     * @return return a list of available {@link Channel}s for this SCC repository ID
     */
    public static List<Channel> findVendorChannelBySccId(Long sccId) {
        return getSession().createNativeQuery("""
                        SELECT c.*, cl.original_id,
                        CASE WHEN cl.original_id IS NULL THEN 0 ELSE 1 END AS clazz_
                        FROM suseSCCRepository r
                        JOIN suseChannelTemplate ct ON r.id = ct.repo_id
                        JOIN rhnChannel c on c.label = ct.channel_label
                        LEFT JOIN rhnChannelCloned cl ON c.id = cl.id
                        WHERE r.scc_id = :sccId
                        ORDER BY c.label
                        """, Channel.class)
                .setParameter("sccId", sccId)
                .list();
    }

    /**
     * Lookup a content source by org/channel
     *
     * @param org the org to lookup
     * @param c   the channel
     * @return the ContentSource(s)
     */
    public static List<ContentSource> lookupContentSources(Org org, Channel c) {
        Map<String, Object> params = new HashMap<>();
        params.put("channel", c);
        if (org != null) {
            params.put("org", org);
            return singleton.listObjectsByNamedQuery("ContentSource.findByOrgandChannel", params);
        }
        else {
            return singleton.listObjectsByNamedQuery("ContentSource.findVendorContentSourceByChannel", params);
        }
    }

    /**
     * Lookup a content source by org and label
     *
     * @param org   the org to lookup
     * @param label repo label
     * @return the ContentSource(s)
     */
    public static ContentSource lookupContentSourceByOrgAndLabel(Org org, String label) {
        return singleton.lookupObjectByNamedQuery("ContentSource.findByOrgAndLabel",
                Map.of("org", org, LABEL, label));
    }

    /**
     * Lookup a Vendor content source (org is NULL) by label
     *
     * @param label repo label
     * @return the ContentSource(s)
     */
    public static ContentSource lookupVendorContentSourceByLabel(String label) {
        return singleton.lookupObjectByNamedQuery("ContentSource.findVendorContentSourceByLabel",
                Map.of(LABEL, label));
    }

    /**
     * Lookup a content source by org and repo
     *
     * @param org      the org to lookup
     * @param repoType repo type
     * @param repoUrl  repo url
     * @return the ContentSource(s)
     */
    public static List<ContentSource> lookupContentSourceByOrgAndRepo(Org org,
                                                                      ContentSourceType repoType, String repoUrl) {
        return singleton.listObjectsByNamedQuery("ContentSource.findByOrgAndRepo",
                Map.of("org", org, "type_id", repoType.getId(), "url", repoUrl));
    }

    /**
     * lookup content source by id and org
     *
     * @param id    id of content source
     * @param orgIn org to check
     * @return content source
     */
    public static ContentSource lookupContentSource(Long id, Org orgIn) {
        return singleton.lookupObjectByNamedQuery("ContentSource.findByIdandOrg", Map.of("id", id, "org", orgIn));
    }

    /**
     * Lookup a content source's filters by id
     *
     * @param id source id
     * @return the ContentSourceFilters
     */
    public static List<ContentSourceFilter> lookupContentSourceFiltersById(Long id) {
        return singleton.listObjectsByNamedQuery("ContentSourceFilter.findBySourceId", Map.of("source_id", id));
    }

    /**
     * Retrieve a list of channel ids associated with the labels provided
     *
     * @param labelsIn the labels to search for
     * @return list of channel ids
     */
    public static List<Long> getChannelIds(List<String> labelsIn) {
        List<Long> list = singleton.listObjectsByNamedQuery("Channel.findChannelIdsByLabels",
                Map.of("labels", labelsIn));
        if (list != null) {
            return list;
        }
        return new ArrayList<>();
    }

    /**
     * Insert or Update a Channel.
     *
     * @param c Channel to be stored in database.
     */
    public static void save(Channel c) {
        c.setLastModified(new Date());
        singleton.saveObject(c);
    }

    /**
     * Insert or Update a content source.
     *
     * @param c content source to be stored in database.
     */
    public static void save(ContentSource c) {
        singleton.saveObject(c);
    }

    /**
     * Insert or Update a DistChannelMap.
     *
     * @param dcm DistChannelMap to be stored in database.
     */
    public static void save(DistChannelMap dcm) {
        singleton.saveObject(dcm);
    }

    /**
     * Insert or Update a content source filter.
     *
     * @param f content source filter to be stored in database.
     */
    public static void save(ContentSourceFilter f) {
        singleton.saveObject(f);
    }

    /**
     * Remove a Channel from the DB
     *
     * @param c Action to be removed from database.
     */
    public static void remove(Channel c) {
        // When we change delete_channel to return the number of rows
        // affected, we can delete all of the CallableMode code below
        // and simply use singleton.removeObject(c); Until then I'm
        // using DataSource.  I must say that working with existing
        // schema, while a reality in most software projects, SUCKS!

        CallableMode m = ModeFactory.getCallableMode(
                CHANNEL_QUERIES, "delete_channel");
        Map<String, Object> inParams = new HashMap<>();
        inParams.put("cid", c.getId());

        m.execute(inParams, new HashMap<>());
    }

    /**
     * Remove a DistChannelMap from the DB
     *
     * @param dcm Action to be removed from database.
     */
    public static void remove(DistChannelMap dcm) {
        singleton.removeObject(dcm);
    }

    /**
     * Remove a Content Source from the DB
     *
     * @param src to be removed from database
     */
    public static void remove(ContentSource src) {
        singleton.removeObject(src);
    }

    /**
     * Remove a ContentSourceFilter from the DB
     *
     * @param filter to be removed from database
     */
    public static void remove(ContentSourceFilter filter) {
        singleton.removeObject(filter);
    }

    /**
     * Returns the base channel for the given server id.
     *
     * @param sid Server id whose base channel we want.
     * @return Base Channel for the given server id.
     */
    public static Channel getBaseChannel(Long sid) {
        return singleton.lookupObjectByNamedQuery("Channel.findBaseChannel", Map.of("sid", sid));
    }

    /**
     * Returns a list of Channels which have clonable errata.
     *
     * @param org Org.
     * @return List of com.redhat.rhn.domain.Channel objects which have
     * clonable errata.
     */
    public static List<ClonedChannel> getChannelsWithClonableErrata(Org org) {
        return singleton.listObjectsByNamedQuery("Channel.channelsWithClonableErrata", Map.of("org", org), false);
    }

    /**
     * Returns the list of Channel ids which the given orgid has access to.
     *
     * @param orgid Org id
     * @param cid   Base Channel id.
     * @return the list of Channel ids which the given orgid has access to.
     */
    public static List<Channel> getUserAcessibleChannels(Long orgid, Long cid) {
        return singleton.listObjectsByNamedQuery("Channel.accessibleChildChannelIds",
                Map.of(ORG_ID, orgid, "cid", cid));
    }

    /**
     * Returns the accessible child channels associated to a base channel.
     *
     * @param baseChannel the base channel who's child channels are needed
     * @param user        the user requesting the info.. (has to be globally subscribed etc.)
     * @return the accessible child channels..
     */
    public static List<Channel> getAccessibleChildChannels(Channel baseChannel, User user) {
        return singleton.listObjectsByNamedQuery("Channel.accessibleChildChannels",
                Map.of("userId", user.getId(), "cid", baseChannel.getId()));
    }

    /**
     * Returns the list of Channels accessible by an org
     * Channels are accessible if they are owned by an org or public.
     *
     * @param orgid The id for the org
     * @return A list of Channel Objects.
     */
    public static List<Channel> getAccessibleChannelsByOrg(Long orgid) {
        return singleton.listObjectsByNamedQuery("Org.accessibleChannels", Map.of(ORG_ID, orgid));
    }

    /**
     * Returns list of channel architectures
     *
     * @return list of channel architectures
     */
    public static List<ChannelArch> getChannelArchitectures() {
        return getSession().createNativeQuery("SELECT * FROM rhnChannelArch", ChannelArch.class).getResultList();
    }

    /**
     * Checks if a channel is accessible by an Org.
     *
     * @param channelLabel the channel label
     * @param orgId        the Org ID
     * @return true if it is accessible
     */
    public static boolean isAccessibleBy(String channelLabel, Long orgId) {
        return (int) singleton.lookupObjectByNamedQuery("Channel.isAccessibleBy",
                Map.of("channel_label", channelLabel, ORG_ID, orgId)) > 0;
    }

    /**
     * Checks if a channel is accessible by a User.
     *
     * @param channelLabel the channel label
     * @param userId       user id
     * @return true if it is accessible
     */
    public static boolean isAccessibleByUser(String channelLabel, Long userId) {
        return singleton.lookupObjectByNamedQuery("Channel.isAccessibleByUser",
                Map.of("channelLabel", channelLabel, "userId", userId)) != null;
    }

    /**
     * returns a ChannelArch by label
     *
     * @param label ChannelArch label
     * @return a ChannelArch by label
     */
    public static ChannelArch findArchByLabel(String label) {
        Session session = getSession();
        String sql = "SELECT * FROM rhnChannelArch WHERE label = :label";
        return session.createNativeQuery(sql, ChannelArch.class)
                .setParameter(LABEL, label, StandardBasicTypes.STRING)
                .uniqueResult();
    }

    /**
     * Returns the Channel whose label matches the given label.
     *
     * @param org   The org of the user looking up the channel
     * @param label Channel label sought.
     * @return the Channel whose label matches the given label.
     */
    public static Channel lookupByLabel(Org org, String label) {
        return singleton.lookupObjectByNamedQuery("Channel.findByLabelAndOrgId",
                Map.of(LABEL, label, "orgId", org.getId()));
    }

    /**
     * Returns the Channel whose label matches the given label.
     * This was added to allow taskomatic to lookup channels by label,
     * and should NOT be used from the webui.
     *
     * @param label Channel label sought.
     * @return the Channel whose label matches the given label.
     */
    public static Channel lookupByLabel(String label) {
        Session session = getSession();
        String sql = """
                SELECT c.*,
                cl.original_id,
                CASE
                WHEN cl.original_id IS NULL THEN 0
                ELSE 1
                END AS clazz_
                FROM rhnChannel c
                LEFT JOIN rhnChannelCloned cl ON c.id = cl.id
                WHERE c.label = :label""";
        return session.createNativeQuery(sql, Channel.class)
                .setParameter(LABEL, label, StandardBasicTypes.STRING)
                .uniqueResult();
    }

    /**
     * Returns true if the given channel is globally subscribable for the
     * given org.
     *
     * @param org Org
     * @param c   Channel to validate.
     * @return true if the given channel is globally subscribable for the
     */
    public static boolean isGloballySubscribable(Org org, Channel c) {
        SelectMode mode = ModeFactory.getMode(
                CHANNEL_QUERIES, "is_not_globally_subscribable");
        Map<String, Object> params = new HashMap<>();
        params.put(ORG_ID, org.getId());
        params.put("cid", c.getId());
        params.put(LABEL, "not_globally_subscribable");

        DataResult<Row> dr = mode.execute(params);
        // if the query returns something that means that this channel
        // is NOT globally subscribable by the org.  Which means the DataResult
        // will have a value in it.  If the channel IS globally subscribable
        // the DataResult will be empty (true)
        return dr.isEmpty();
    }

    /**
     * Set the globally subscribable attribute for a given channel
     *
     * @param org     The org containing the channel
     * @param channel The channel in question
     * @param value   True to make the channel globally subscribable, false to make it not
     *                globally subscribable.
     */
    public static void setGloballySubscribable(Org org, Channel channel, boolean value) {
        //we need to check here, otherwise if we try to remove and it's already removed
        //  the db throws a violation
        if (value == channel.isGloballySubscribable(org)) {
            return;
        }

        /*
         *  this is some bass-ackwards logic...
         *  if value == true, remove the 'not_globally_subscribable' setting
         *  if value == false, add the 'not_globally_subscribable' setting
         */
        if (value) {
            removeOrgChannelSetting(org, channel, "not_globally_subscribable");
        }
        else {
            addOrgChannelSetting(org, channel, "not_globally_subscribable");
        }
    }

    /**
     * Remove an org-channel setting
     *
     * @param org     The org in question
     * @param channel The channel in question
     * @param label   the label of the setting to remove
     */
    private static void removeOrgChannelSetting(Org org, Channel channel, String label) {
        WriteMode m = ModeFactory.getWriteMode(CHANNEL_QUERIES,
                "remove_org_channel_setting");
        Map<String, Object> params = new HashMap<>();
        params.put(ORG_ID, org.getId());
        params.put("cid", channel.getId());
        params.put(LABEL, label);
        m.executeUpdate(params);
    }

    /**
     * Adds an org-channel setting
     *
     * @param org     The org in question
     * @param channel The channel in question
     * @param label   the label of the setting to add
     */
    private static void addOrgChannelSetting(Org org, Channel channel, String label) {
        WriteMode m = ModeFactory.getWriteMode(CHANNEL_QUERIES,
                "add_org_channel_setting");
        Map<String, Object> params = new HashMap<>();
        params.put(ORG_ID, org.getId());
        params.put("cid", channel.getId());
        params.put(LABEL, label);
        m.executeUpdate(params);
    }

    /**
     * @param cid Channel package is being added to
     * @param pid Package id from rhnPackage
     */
    public static void addChannelPackage(Long cid, Long pid) {
        WriteMode m = ModeFactory.getWriteMode(CHANNEL_QUERIES,
                "add_channel_package");
        Map<String, Object> params = new HashMap<>();
        params.put("cid", cid);
        params.put("pid", pid);
        m.executeUpdate(params);
    }

    /**
     * Creates empty SSL set for repository
     *
     * @return empty SSL set
     */
    public static SslContentSource createRepoSslSet() {
        return new SslContentSource();
    }

    /**
     * Utility to call {@link #refreshNewestPackageCache(Long, String)} given a channel.
     *
     * @param c     channel to be refreshed
     * @param label the label
     */
    public static void refreshNewestPackageCache(Channel c, String label) {
        refreshNewestPackageCache(c.getId(), label);
    }

    /**
     * Refreshes the channel with the "newest" packages.  Newest isn't just
     * the latest versions, an errata could have obsoleted a package in which
     * case this would have removed said package from the channel.
     *
     * @param channelId identifies the channel to be refreshed
     * @param label     the label
     */
    public static void refreshNewestPackageCache(Long channelId, String label) {
        CallableMode m = ModeFactory.getCallableMode(CHANNEL_QUERIES,
                "refresh_newest_package");
        Map<String, Object> inParams = new HashMap<>();
        inParams.put("cid", channelId);
        inParams.put(LABEL, label);

        m.execute(inParams, new HashMap<>());
    }

    /**
     * Clones the "newest" channel packages according to clone.
     *
     * @param fromChannelId original channel id
     * @param toChannelId   cloned channle id
     */
    public static void cloneNewestPackageCache(Long fromChannelId, Long toChannelId) {
        WriteMode m = ModeFactory.getWriteMode(CHANNEL_QUERIES,
                "clone_newest_package");
        Map<String, Object> params = new HashMap<>();
        params.put("from_cid", fromChannelId);
        params.put("to_cid", toChannelId);
        m.executeUpdate(params);
    }

    /**
     * Returns true if the given label is in use.
     *
     * @param label Label
     * @return true if the given label is in use.
     */
    public static boolean doesChannelLabelExist(String label) {
        if (label == null) {
            return false;
        }
        Object o = singleton.lookupObjectByNamedQuery("Channel.verifyLabel", Map.of(LABEL, label), false);
        return (o != null);
    }

    /**
     * Returns true if the given name is in use.
     *
     * @param name name
     * @return true if the given name is in use.
     */
    public static boolean doesChannelNameExist(String name) {
        if (name == null) {
            return false;
        }
        Object o = singleton.lookupObjectByNamedQuery("Channel.verifyName", Map.of("name", name), false);
        return (o != null);
    }

    /**
     * Return a list of kickstartable tree channels, i.e. channels that can
     * be used for creating kickstartable trees (distributions).
     *
     * @param org org
     * @return list of channels
     */
    public static List<Channel> getKickstartableTreeChannels(Org org) {
        return singleton.listObjectsByNamedQuery("Channel.kickstartableTreeChannels",
                Map.of(ORG_ID, org.getId()), false);
    }

    /**
     * Return a list of channels that are kickstartable to the Org passed in,
     * i.e. channels that can be used for creating kickstart profiles.
     *
     * @param org org
     * @return list of channels
     */
    public static List<Channel> getKickstartableChannels(Org org) {
        return singleton.listObjectsByNamedQuery("Channel.kickstartableChannels",
                Map.of(ORG_ID, org.getId()), false);
    }

    /**
     * Get a list of base channels that have an org associated
     *
     * @param user the logged in user
     * @return List of Channels
     */
    public static List<Channel> listCustomBaseChannels(User user) {
        return singleton.listObjectsByNamedQuery("Channel.findCustomBaseChannels", Map.of("user_id", user.getId()));
    }

    /**
     * Find yum supported checksum types
     *
     * @return List of ChecksumTypes instances
     */
    public static List<ChecksumType> listYumSupportedChecksums() {
        return getSession().createQuery("""
                 FROM com.redhat.rhn.domain.common.ChecksumType as t
                 WHERE t.label LIKE 'sha%'""", ChecksumType.class)
                .setCacheable(true)
                .list();
    }

    /**
     * Get a list of modular channels in users org
     *
     * @param user the logged in user
     * @return List of modular channels
     */
    public static List<Channel> listModularChannels(User user) {
        return singleton.listObjectsByNamedQuery("Channel.findModularChannels",
                Map.of(ORG_ID, user.getOrg().getId()));
    }

    /**
     * Find checksumtype by label
     *
     * @param checksum checksum label
     * @return ChecksumType instance for given label
     */
    public static ChecksumType findChecksumTypeByLabel(String checksum) {
        if (checksum == null) {
            return null;
        }
        return getSession().createQuery("""
                 FROM com.redhat.rhn.domain.common.ChecksumType AS t
                 WHERE t.label = :label""", ChecksumType.class)
                .setParameter(LABEL, checksum)
                .setCacheable(true)
                .uniqueResult();
    }

    /**
     * Get a list of packages ids that are in a channel and in a list of errata.
     * (The errata do not necessarily have to be associate with the channel)
     *
     * @param chan the channel
     * @param eids the errata ids
     * @return list of package ids
     */
    public static List<Long> getChannelPackageWithErrata(Channel chan, Collection<Long> eids) {
        return singleton.listObjectsByNamedQuery("Channel.packageInChannelAndErrata",
                Map.of("cid", chan.getId()), eids, "eids");

    }

    /**
     * Lookup a ChannelArch based on its name
     *
     * @param name arch name
     * @return ChannelArch if found, otherwise null
     */
    public static ChannelArch lookupArchByName(String name) {
        if (name == null) {
            return null;
        }
        return singleton.lookupObjectByNamedQuery("ChannelArch.findByName", Map.of("name", name));
    }

    /**
     * Lookup a ChannelArch based on its label
     *
     * @param label arch label
     * @return ChannelArch if found, otherwise null
     */
    public static ChannelArch lookupArchByLabel(String label) {
        if (label == null) {
            return null;
        }
        return singleton.lookupObjectByNamedQuery("ChannelArch.findByLabel", Map.of(LABEL, label));
    }

    /**
     * Get package ids for a channel
     *
     * @param cid the channel id
     * @return List of package ids
     */
    public static List<Long> getPackageIds(Long cid) {
        if (cid == null) {
            return new ArrayList<>();
        }
        return singleton.listObjectsByNamedQuery("Channel.getPackageIdList", Map.of("cid", cid));
    }

    /**
     * Get cloned errata ids for a channel
     *
     * @param cid the channel id
     * @return List of errata ids
     */
    public static List<Long> getClonedErrataIds(Long cid) {
        if (cid == null) {
            return new ArrayList<>();
        }
        return singleton.listObjectsByNamedQuery("Channel.getClonedErrataOriginalIdList", Map.of("cid", cid));
    }

    /**
     * Looksup the number of Packages in a channel
     *
     * @param channel the Channel who's package count you are interested in.
     * @return number of packages in this channel.
     */
    public static int getPackageCount(Channel channel) {
        return singleton.lookupObjectByNamedQuery("Channel.getPackageCount", Map.of("cid", channel.getId()));
    }

    /**
     * Get the errata count for a channel
     *
     * @param channel the channel
     * @return the errata count as an int
     */
    public static int getErrataCount(Channel channel) {
        return singleton.lookupObjectByNamedQuery("Channel.getErrataCount", Map.of("cid", channel.getId()));
    }

    /**
     * Lookup the default release channel map for the given channel. Returns null if no
     * default is found.
     *
     * @param channel Channel to lookup mapping for
     * @return Default ReleaseChannelMap
     */
    public static ReleaseChannelMap lookupDefaultReleaseChannelMapForChannel(Channel channel) {
        if (channel == null) {
            return null;
        }
        List<ReleaseChannelMap> list = singleton.listObjectsByNamedQuery("ReleaseChannelMap.findDefaultForChannel",
                Map.of("channel", channel));
        if (list.isEmpty()) {
            return null;
        }
        Collections.sort(list);
        return list.get(0);
    }

    /**
     * Lookup ChannelSyncFlag object for a specfic channel
     *
     * @param channel The channel object on which the lookup should be performed
     * @return ChannelSyncFlag object containing all flag settings for a specfic channel
     */
    public static ChannelSyncFlag lookupChannelReposyncFlag(Channel channel) {
        return getSession()
                .createNativeQuery(
                        "SELECT * FROM rhnChannelSyncFlag WHERE channel_id = :channel", ChannelSyncFlag.class)
                .setParameter("channel", channel.getId(), StandardBasicTypes.LONG)
                .getSingleResult();
    }

    /**
     * Save a ChannelSyncFlag object for a specfic channel
     *
     * @param flags The ChannelSyncFlag object which should be added to channel
     */
    public static void save(ChannelSyncFlag flags) {
        singleton.saveObject(flags);
    }

    /**
     * List all defined dist channel maps
     * <p>
     * Returns empty array if none is found.
     *
     * @return DistChannelMap[], empty if none is found
     */
    public static List<DistChannelMap> listAllDistChannelMaps() {
        return singleton.listObjectsByNamedQuery("DistChannelMap.listAll", Map.of());
    }

    /**
     * Lists all dist channel maps for an user organization
     *
     * @param org organization
     * @return list of dist channel maps
     */
    public static List<DistChannelMap> listAllDistChannelMapsByOrg(Org org) {
        return singleton.listObjectsByNamedQuery("DistChannelMap.listAllByOrg", Map.of(ORG_ID, org.getId()));
    }

    /**
     * Lookup the dist channel map by id
     *
     * @param id dist channel map id
     * @return DistChannelMap, null if none is found
     */
    public static DistChannelMap lookupDistChannelMapById(Long id) {
        return singleton.lookupObjectByNamedQuery("DistChannelMap.lookupById", Map.of("id", id));
    }

    /**
     * Lookup the dist channel map for the given product name, release, and channel arch.
     * Returns null if none is found.
     *
     * @param org         organization
     * @param productName Product name.
     * @param release     Version.
     * @param channelArch Channel arch.
     * @return DistChannelMap, null if none is found
     */
    public static DistChannelMap lookupDistChannelMapByPnReleaseArch(
            Org org, String productName, String release, ChannelArch channelArch) {
        return singleton.lookupObjectByNamedQuery("DistChannelMap.findByProductNameReleaseAndChannelArch",
                Map.of("for_org_id", org.getId(), "product_name", productName,
                        "release", release, "channel_arch_id", channelArch.getId()));
    }

    /**
     * Lookup the dist channel map for the given organization according to release and channel arch.
     * Returns null if none is found.
     *
     * @param org         organization
     * @param release     release
     * @param channelArch Channel arch.
     * @return DistChannelMap, null if none is found
     */
    public static DistChannelMap lookupDistChannelMapByOrgReleaseArch(Org org, String release,
                                                                      ChannelArch channelArch) {
        return singleton.lookupObjectByNamedQuery("DistChannelMap.findByOrgReleaseArch",
                Map.of(ORG_ID, org.getId(), "release", release, "channel_arch_id", channelArch.getId()));
    }

    /**
     * Lists compatible dist channel mappings for a server available within an organization
     * Returns empty list if none is found.
     *
     * @param server server
     * @return list of dist channel mappings, empty list if none is found
     */
    public static List<DistChannelMap> listCompatibleDcmByServerInNullOrg(Server server) {
        return singleton.listObjectsByNamedQuery("DistChannelMap.findCompatibleByServerInNullOrg",
                Map.of("release", server.getRelease(), "server_arch_id", server.getServerArch().getId()));
    }

    /**
     * Lists *common* compatible channels for all SSM systems subscribed to a common base
     * Returns empty list if none is found.
     *
     * @param user    user
     * @param channel channel
     * @return list of compatible channels, empty list if none is found
     */
    public static List<Channel> listCompatibleDcmForChannelSSMInNullOrg(User user, Channel channel) {
        return singleton.listObjectsByNamedQuery("Channel.findCompatibleForChannelSSMInNullOrg",
                Map.of("user_id", user.getId(), "channel_id", channel.getId()));
    }

    /**
     * Lists *common* compatible channels for all SSM systems subscribed to a common base
     * Returns empty list if none is found.
     *
     * @param user user
     * @return list of compatible channels, empty list if none is found
     */
    public static List<Channel> listCompatibleBasesForSSMNoBaseInNullOrg(User user) {
        return singleton.listObjectsByNamedQuery("Channel.findCompatibleSSMNoBaseInNullOrg",
                Map.of("user_id", user.getId()));
    }

    /**
     * Lists *common* custom compatible channels
     * for all SSM systems subscribed to a common base
     *
     * @param user    user
     * @param channel channel
     * @return List of channels.
     */
    public static List<Channel> listCustomBaseChannelsForSSM(User user, Channel channel) {
        return singleton.listObjectsByNamedQuery("Channel.findCompatCustomBaseChsSSM",
                Map.of("user_id", user.getId(), ORG_ID, user.getOrg().getId(), "channel_id", channel.getId()));
    }

    /**
     * Lists *common* custom compatible channels
     * for all SSM systems without base channel
     *
     * @param user user
     * @return List of channels.
     */
    public static List<Channel> listCustomBaseChannelsForSSMNoBase(User user) {
        return singleton.listObjectsByNamedQuery("Channel.findCompatCustomBaseChsSSMNoBase",
                Map.of("user_id", user.getId(), ORG_ID, user.getOrg().getId()));
    }

    /**
     * Find child channels that can be subscribed by the user and have the arch compatible
     * with the servers in the SSM.
     *
     * @param user            user
     * @param parentChannelId id of the parent channel
     * @return List of child channel ids.
     */
    public static List<SsmChannelDto> findChildChannelsByParentInSSM(User user, long parentChannelId) {
        List<Object[]> res = singleton.listObjectsByNamedQuery("Channel.findChildChannelsByParentInSSM",
                Map.of("user_id", user.getId(), "parent_id", parentChannelId));
        return res
                .stream()
                .map(Arrays::asList)
                .map(r -> new SsmChannelDto((long) r.get(0), (String) r.get(1), r.get(2) != null))
                .collect(Collectors.toList());
    }

    /**
     * Lookup dist channel mappings for the given channel.
     * Returns empty list if none is found.
     *
     * @param c Channel to lookup mapping for
     * @return list of dist channel mappings, empty list if none is found
     */
    public static List<DistChannelMap> listDistChannelMaps(Channel c) {
        if (c == null) {
            return List.of();
        }
        return singleton.listObjectsByNamedQuery("DistChannelMap.findByChannel", Map.of("channel", c));
    }

    /**
     * All channels (including children) based on the following rules
     * <p>
     * 1) Base channels are listed first
     * 2) Parent channels are ordered by label
     * 3) Child channels are listed right after the corresponding parent, and ordered by label
     * 4) Channels are included only if user has access to them
     * 5) Child channels are included only if the user has access to the corresponding parent channel
     *
     * @param user The user to check channel access
     * @return List of channels (including children) accessible for the provided user
     */
    public static List<Channel> findAllByUserOrderByChild(User user) {
        return singleton.listObjectsByNamedQuery("Channel.findAllByUserOrderByChild", Map.of("userId", user.getId()));
    }

    /**
     * Get a list of channels with no org that are not a child
     *
     * @return List of Channels
     */
    public static List<Channel> listRedHatBaseChannels() {
        return singleton.listObjectsByNamedQuery("Channel.findRedHatBaseChannels", Map.of());
    }


    /**
     * List all accessible Red Hat base channels for a given user
     *
     * @param user logged in user
     * @return list of Red Hat base channels
     */
    public static List<Channel> listRedHatBaseChannels(User user) {
        return singleton.listObjectsByNamedQuery("Channel.findRedHatBaseChannelsByUserId",
                Map.of("userId", user.getId()));
    }

    /**
     * Lookup the original channel of a cloned channel
     *
     * @param chan the channel to find the original of
     * @return The channel that was cloned, null if none
     */
    public static Channel lookupOriginalChannel(Channel chan) {
        return singleton.lookupObjectByNamedQuery("Channel.lookupOriginal", Map.of("clone", chan));
    }

    /**
     * Lookup a product name by label.
     *
     * @param label Product name label to search for.
     * @return Product name if found, null otherwise.
     */
    public static ProductName lookupProductNameByLabel(String label) {
        if (label == null) {
            return null;
        }
        return singleton.lookupObjectByNamedQuery("ProductName.findByLabel", Map.of(LABEL, label));
    }

    /**
     * Returns a distinct list of ChannelArch labels for all synch'd and custom
     * channels in the satellite.
     *
     * @return a distinct list of ChannelArch labels for all synch'd and custom
     * channels in the satellite.
     */
    public static List<String> findChannelArchLabelsSyncdChannels() {
        return singleton.listObjectsByNamedQuery("Channel.findChannelArchLabelsSyncdChannels", Map.of());
    }

    /**
     * List all accessible base channels for an org
     *
     * @param user logged in user.
     * @return list of custom channels
     */
    public static List<Channel> listSubscribableBaseChannels(User user) {
        return singleton.listObjectsByNamedQuery("Channel.findSubscribableBaseChannels",
                Map.of("user_id", user.getId()));
    }

    /**
     * List all accessible base channels for an org
     *
     * @param user logged in user.
     * @return list of custom channels
     */
    public static List<Channel> listAllBaseChannels(User user) {
        return singleton.listObjectsByNamedQuery("Channel.findAllBaseChannels",
                Map.of(ORG_ID, user.getOrg().getId(), "user_id", user.getId()));
    }

    /**
     * List all accessible base channels for the entire satellite
     *
     * @return list of base channels
     */
    public static List<Channel> listAllBaseChannels() {
        return singleton.listObjectsByNamedQuery("Channel.findAllBaseChannelsOnSatellite", Map.of());
    }


    /**
     * List all child channels of the given parent regardless of the user
     *
     * @param parent the parent channel
     * @return list of children of the parent
     */
    public static List<Channel> listAllChildrenForChannel(Channel parent) {
        return singleton.listObjectsByNamedQuery("Channel.listAllChildren", Map.of("parent", parent));
    }

    /**
     * Lookup a Package based on the channel and package file name
     *
     * @param channel  to look in
     * @param fileName to look up
     * @return Package if found
     */
    public static Package lookupPackageByFilename(Channel channel,
                                                  String fileName) {

        List<Package> pkgs = HibernateFactory.getSession()
                .getNamedQuery("Channel.packageByFileName")
                .setParameter("pathlike", "%/" + fileName, StandardBasicTypes.STRING)
                .setParameter("channel_id", channel.getId(), StandardBasicTypes.LONG)
                .list();
        if (pkgs.isEmpty()) {
            return null;
        }
        return pkgs.get(0);
    }

    /**
     * Lookup a Package based on the channel, package file name and range
     *
     * @param channel     to look in
     * @param fileName    to look up
     * @param headerStart start of header
     * @param headerEnd   end of header
     * @return Package if found
     */
    public static Package lookupPackageByFilenameAndRange(Channel channel,
                                                          String fileName, int headerStart, int headerEnd) {

        List<Package> pkgs = HibernateFactory.getSession()
                .getNamedQuery("Channel.packageByFileNameAndRange")
                .setParameter("pathlike", "%/" + fileName, StandardBasicTypes.STRING)
                .setParameter("channel_id", channel.getId(), StandardBasicTypes.LONG)
                .setParameter("headerStart", headerStart, StandardBasicTypes.INTEGER)
                .setParameter("headerEnd", headerEnd, StandardBasicTypes.INTEGER).list();
        if (pkgs.isEmpty()) {
            return null;
        }
        else {
            return pkgs.get(0);
        }
    }

    /**
     * Method to check if the channel contains any kickstart distributions
     * associated to it.
     *
     * @param ch the channel to check distros on
     * @return true of the channels contains any distros
     */
    public static boolean containsDistributions(Channel ch) {
        Session session = getSession();
        String sql
                = "SELECT COUNT(*) FROM rhnKickstartableTree WHERE channel_id = :channelId";
        Number count = (Number) session.createNativeQuery(sql)
                .setParameter("channelId", ch.getId(), StandardBasicTypes.LONG).getSingleResult();
        return count.intValue() > 0;
    }

    /**
     * Clear a content source's filters
     *
     * @param id source id
     */
    public static void clearContentSourceFilters(Long id) {
        List<ContentSourceFilter> filters = lookupContentSourceFiltersById(id);

        for (ContentSourceFilter filter : filters) {
            remove(filter);
        }

        // flush so that if we're creating new filters we don't get constraint
        // violations for rhn_csf_sid_so_uq
        HibernateFactory.getSession().flush();
    }

    /**
     * returns channel manager id for given channel
     *
     * @param org       given organization
     * @param channelId channel id
     * @return list of channel managers
     */
    public static List<Long> listManagerIdsForChannel(Org org, Long channelId) {
        SelectMode m = ModeFactory.getMode(CHANNEL_QUERIES,
                "managers_for_channel_in_org");
        Map<String, Object> params = new HashMap<>();
        params.put(ORG_ID, org.getId());
        params.put("channel_id", channelId);
        DataResult<Map<String, Long>> dr = m.execute(params);
        List<Long> ids = new ArrayList<>();
        for (Map<String, Long> row : dr) {
            ids.add(row.get("id"));
        }
        return ids;
    }

    /**
     * returns channel subscriber id for given channel
     *
     * @param org       given organization
     * @param channelId channel id
     * @return list of channel subscribers
     */
    public static List<Long> listSubscriberIdsForChannel(Org org, Long channelId) {
        SelectMode m = ModeFactory.getMode(CHANNEL_QUERIES,
                "subscribers_for_channel_in_org");
        Map<String, Object> params = new HashMap<>();
        params.put(ORG_ID, org.getId());
        params.put("channel_id", channelId);
        DataResult<Map<String, Long>> dr = m.execute(params);
        List<Long> ids = new ArrayList<>();
        for (Map<String, Long> row : dr) {
            ids.add(row.get("id"));
        }
        return ids;
    }

    /**
     * Locks the given Channel for update on a database level
     *
     * @param c Channel to lock
     */
    public static void lock(Channel c) {
        singleton.lockObject(Channel.class, c.getId());
    }

    /**
     * Adds errata to channel mapping. Does nothing else
     *
     * @param eids List of eids to add mappings for
     * @param cid  channel id we're cloning into
     */
    public static void addErrataToChannel(Set<Long> eids, Long cid) {
        WriteMode m = ModeFactory.getWriteMode(CHANNEL_QUERIES,
                "add_cloned_erratum_to_channel");
        Map<String, Object> params = new HashMap<>();
        params.put("cid", cid);
        for (Long eid : eids) {
            params.put("eid", eid);
            m.executeUpdate(params);
        }
    }

    /**
     * List all channels
     *
     * @return list of all channels
     */
    public static List<Channel> listAllChannels() {
        return getSession().createQuery("FROM Channel c", Channel.class).getResultList();
    }

    /**
     * List all vendor channels (org is null)
     *
     * @return list of vendor channels
     */
    public static List<Channel> listVendorChannels() {
        List<Channel> result = singleton.listObjectsByNamedQuery("Channel.findVendorChannels", Map.of());
        if (result != null) {
            return result;
        }
        return new ArrayList<>();
    }

    /**
     * Return a list of all custom channels (org is not null)
     * @return the list of custom channels
     */
    public static List<Channel> listCustomChannels() {
        return getSession()
                .createQuery("FROM Channel c WHERE c.org IS NOT NULL", Channel.class)
                .getResultList();
    }

    /**
     * List all custom channels (org is not null) with at least one repository
     *
     * @return list of vendor channels
     */
    public static List<Channel> listCustomChannelsWithRepositories() {
        List<Channel> result =
                singleton.listObjectsByNamedQuery("Channel.findCustomChannelsWithRepositories", Map.of());
        if (result != null) {
            return result;
        }
        return new ArrayList<>();
    }

    /**
     * List all vendor content sources (org is null)
     *
     * @return list of vendor content sources
     */
    @SuppressWarnings("unchecked")
    public static List<ContentSource> listVendorContentSources() {
        return getSession().createNativeQuery("SELECT * FROM rhnContentSource WHERE org_id IS NULL",
                ContentSource.class).getResultList();
    }

    /**
     * List all custom channels which URL access a specific FQDN
     * Mainly used for finding repos pointing to a Hub
     * @param fqdn the FQDN
     * @return a list of {@link ContentSource}
     */
    public static List<ContentSource> findCustomContentSourcesForHubFqdn(String fqdn) {
        return getSession().createNativeQuery("""
                SELECT * FROM rhnContentSource
                 WHERE org_id IS NOT NULL
                 AND source_url like :urlstart
                """, ContentSource.class)
                .setParameter("urlstart", "https://%s/%%".formatted(fqdn))
                .list();
    }

    /**
     * Find a vendor content source (org is null) for a given repo URL.
     *
     * @param repoUrl url to match against
     * @return vendor content source if it exists
     */
    public static ContentSource findVendorContentSourceByRepo(String repoUrl) {
        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<ContentSource> cq = cb.createQuery(ContentSource.class);
        Root<ContentSource> root = cq.from(ContentSource.class);

        // Create predicates for the query
        Predicate isOrgNull = cb.isNull(root.get("org"));
        Predicate sourceUrlPredicate;

        if (repoUrl.contains("mirrorlist.centos.org") || repoUrl.contains("mirrors.rockylinux.org")) {
            sourceUrlPredicate = cb.equal(root.get("sourceUrl"), repoUrl);
        }
        else {
            String[] parts = repoUrl.split("\\?");
            String repoUrlPrefix = parts[0];
            if (parts.length > 1) {
                sourceUrlPredicate = cb.like(root.get("sourceUrl"), repoUrlPrefix + '%');
            }
            else {
                sourceUrlPredicate = cb.equal(root.get("sourceUrl"), repoUrlPrefix);
            }
        }

        // Combine predicates
        cq.where(cb.and(isOrgNull, sourceUrlPredicate));

        // Create and execute the query
        TypedQuery<ContentSource> query = getSession().createQuery(cq);
        ContentSource contentSource;
        try {
            contentSource = query.getSingleResult();
        }
        catch (NoResultException e) {
            contentSource = null;
        }

        return contentSource;
    }

    /**
     * Find a {@link ChannelProduct} for given name and version.
     *
     * @param product the product
     * @param version the version
     * @return channel product
     */
    public static ChannelProduct findChannelProduct(String product, String version) {
        Session session = getSession();
        String sql
                = "SELECT * FROM rhnChannelProduct WHERE product = :product AND version = :version";
        Query<ChannelProduct> query = session.createNativeQuery(sql, ChannelProduct.class);
        query.setParameter("product", product, StandardBasicTypes.STRING);
        query.setParameter("version", version, StandardBasicTypes.STRING);
        try {
            return query.getSingleResult();
        }
        catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Insert or update a {@link ChannelProduct}.
     *
     * @param channelProduct ChannelProduct to be stored in database.
     */
    public static void save(ChannelProduct channelProduct) {
        singleton.saveObject(channelProduct);
    }

    /**
     * Insert or update a {@link ProductName}.
     *
     * @param productName ProductName to be stored in database.
     */
    public static void save(ProductName productName) {
        singleton.saveObject(productName);
    }

    /**
     * Analyzes the rhnChannelPackage table, useful to update statistics after massive changes.
     */
    public static void analyzeChannelPackages() {
        var m = ModeFactory.getCallableMode(CHANNEL_QUERIES, "analyze_channel_packages");
        m.execute(new HashMap<>(), new HashMap<>());
    }

    /**
     * Analyzes the rhnErrataPackage table, useful to update statistics after massive changes.
     */
    public static void analyzeErrataPackages() {
        var m = ModeFactory.getCallableMode(CHANNEL_QUERIES, "analyze_errata_packages");
        m.execute(new HashMap<>(), new HashMap<>());
    }

    /**
     * Analyzes the rhnChannelErrata table, useful to update statistics after massive changes.
     */
    public static void analyzeChannelErrata() {
        var m = ModeFactory.getCallableMode(CHANNEL_QUERIES, "analyze_channel_errata");
        m.execute(new HashMap<>(), new HashMap<>());
    }

    /**
     * Analyzes the rhnErrataCloned table, useful to update statistics after massive changes.
     */
    public static void analyzeErrataCloned() {
        var m = ModeFactory.getCallableMode(CHANNEL_QUERIES, "analyze_errata_cloned");
        m.execute(new HashMap<>(), new HashMap<>());
    }

    /**
     * Analyzes the rhnErrata table, useful to update statistics after massive changes.
     */
    public static void analyzeErrata() {
        var m = ModeFactory.getCallableMode(CHANNEL_QUERIES, "analyze_errata");
        m.execute(new HashMap<>(), new HashMap<>());
    }

    /**
     * Analyzes the rhnServerNeededCache table, useful to update statistics after massive changes.
     */
    public static void analyzeServerNeededCache() {
        var m = ModeFactory.getCallableMode(CHANNEL_QUERIES, "analyze_serverNeededCache");
        m.execute(new HashMap<>(), new HashMap<>());
    }

    /**
     * Sets channel modules data from given channel.
     *
     * @param from the source Channel
     * @param to   the target Channel
     */
    public static void cloneModulesMetadata(Channel from, Channel to) {
        if (!from.isModular()) {
            if (to.isModular()) {
                HibernateFactory.getSession().delete(to.getModules());
                to.setModules(null);
            }
        }
        else {
            // clone appstreams
            if (AppStreamsManager.listChannelAppStreams(to.getId()).isEmpty()) {
                AppStreamsManager.cloneAppStreams(to, from);
            }
            if (!to.isModular()) {
                Modules modules = new Modules();
                modules.setChannel(to);
                to.setModules(modules);
            }
            to.getModules().setRelativeFilename(from.getModules().getRelativeFilename());
        }
    }

    /**
     * Converts a channel to a channel info structure
     *
     * @param channel                    the channel to be converted
     * @param peripheralOrgId            the peripheral org id this channel will be assigned to in the peripheral, if
     *                                   channel is a custom channel. Vendor channels stay on Org NULL on the peripheral
     * @param forcedOriginalChannelLabel an optional string setting the original of a cloned channel,
     *                                   instead of the pristine one
     * @return CreateChannelInfoJson the converted info of the channel
     */
    public static ChannelInfoDetailsJson toChannelInfo(Channel channel, Long peripheralOrgId,
                                                       Optional<String> forcedOriginalChannelLabel) {

        ChannelInfoDetailsJson channelInfo = new ChannelInfoDetailsJson(channel.getLabel());

        if (channel.getOrg() == null) {
            channelInfo.setPeripheralOrgId(null);
        }
        else {
            channelInfo.setPeripheralOrgId(peripheralOrgId);
        }

        String parentChannelLabel = (null == channel.getParentChannel()) ? null :
                channel.getParentChannel().getLabel();
        channelInfo.setParentChannelLabel(parentChannelLabel);

        String channelArchLabel = (null == channel.getChannelArch()) ? null :
                channel.getChannelArch().getLabel();
        channelInfo.setChannelArchLabel(channelArchLabel);

        channelInfo.setBaseDir(channel.getBaseDir());
        channelInfo.setName(channel.getName());
        channelInfo.setSummary(channel.getSummary());
        channelInfo.setDescription(channel.getDescription());

        String productNameLabel = (null == channel.getProductName()) ? null :
                channel.getProductName().getLabel();
        channelInfo.setProductNameLabel(productNameLabel);

        channelInfo.setGpgCheck(channel.isGPGCheck());
        channelInfo.setGpgKeyUrl(channel.getGPGKeyUrl());
        channelInfo.setGpgKeyId(channel.getGPGKeyId());
        channelInfo.setGpgKeyFp(channel.getGPGKeyFp());

        channelInfo.setEndOfLifeDate(channel.getEndOfLife());
        channelInfo.setChecksumTypeLabel(channel.getChecksumTypeLabel());

        String channelProductProduct = (null == channel.getProduct()) ? null :
                channel.getProduct().getProduct();
        channelInfo.setChannelProductProduct(channelProductProduct);
        String channelProductVersion = (null == channel.getProduct()) ? null :
                channel.getProduct().getVersion();
        channelInfo.setChannelProductVersion(channelProductVersion);

        channelInfo.setMaintainerName(channel.getMaintainerName());
        channelInfo.setMaintainerEmail(channel.getMaintainerEmail());
        channelInfo.setMaintainerPhone(channel.getMaintainerPhone());
        channelInfo.setSupportPolicy(channel.getSupportPolicy());
        channelInfo.setUpdateTag(channel.getUpdateTag());
        channelInfo.setInstallerUpdates(channel.isInstallerUpdates());

        String originalChannelLabel = channel.asCloned()
                .flatMap(clonedChannel -> forcedOriginalChannelLabel)
                .orElse(null);
        channelInfo.setOriginalChannelLabel(originalChannelLabel);

        // obtain repository info
        String hostname = ConfigDefaults.get().getJavaHostname();
        String channelLabel = channel.getLabel();

        Optional<String> tokenString = SCCEndpoints.buildHubRepositoryToken(channel);
        if (tokenString.isPresent()) {
            SCCRepositoryJson repositoryInfo;
            if (channel.getOrg() != null) {
                repositoryInfo = SCCEndpoints.buildCustomRepoJson(channelLabel, hostname, tokenString.get());
            }
            else {
                repositoryInfo = SUSEProductFactory.lookupByChannelLabelFirst(channelLabel)
                        .map(ct -> SCCEndpoints.buildVendorRepoJson(ct, hostname, tokenString.get()))
                        .orElse(SCCEndpoints.buildCustomRepoJson(channelLabel, hostname, tokenString.get()));
            }
            channelInfo.setRepositoryInfo(repositoryInfo);
        }

        return channelInfo;
    }

    /**
     * Synchronize a channel. Either create or update from the given the info
     * @param info the info about the channel
     * @param channelInfoByLabel all channel info by label
     * @param syncFinished list of finished channel labels
     */
    public static void syncChannel(ChannelInfoDetailsJson info, Map<String, ChannelInfoDetailsJson> channelInfoByLabel,
                                   Set<String> syncFinished) {

        String parentChannelLabel = info.getParentChannelLabel();
        if (StringUtils.isNotEmpty(parentChannelLabel) && !syncFinished.contains(parentChannelLabel)) {
            ChannelInfoDetailsJson parentInfo = channelInfoByLabel.get(parentChannelLabel);
            if (parentInfo != null) {
                syncChannel(parentInfo, channelInfoByLabel, syncFinished);
            }
            else {
                throw new IllegalArgumentException("Information about the parent channel '%1$s' missing"
                        .formatted(parentChannelLabel));
            }
        }
        String originalChannelLabel = info.getOriginalChannelLabel();
        if (StringUtils.isNotEmpty(originalChannelLabel) && !syncFinished.contains(originalChannelLabel)) {
            ChannelInfoDetailsJson originalChannelInfo = channelInfoByLabel.get(originalChannelLabel);
            if (null != originalChannelInfo) {
                syncChannel(originalChannelInfo, channelInfoByLabel, syncFinished);
            }
            else {
                throw new IllegalArgumentException("Information about the original channel '%1$s' missing"
                        .formatted(originalChannelLabel));
            }
        }
        if (lookupByLabel(info.getLabel()) == null) {
            Channel channel = toChannel(info);
            ChannelFactory.save(channel);
        }
        else {
            Channel channel = modifyChannel(info);
            ChannelFactory.save(channel);
        }
        syncFinished.add(info.getLabel());
    }

    /**
     * Converts a channel info structure to a channel
     *
     * @param channelInfo the channel info to be converted
     * @return {@link Channel} converted from channel info
     */
    public static Channel toChannel(ChannelInfoDetailsJson channelInfo) {
        Org org = Optional.ofNullable(channelInfo.getPeripheralOrgId())
                .map(OrgFactory::lookupById)
                .orElse(null);
        Channel parentChannel = ChannelFactory.lookupByLabel(channelInfo.getParentChannelLabel());
        ChannelArch channelArch = ChannelFactory.findArchByLabel(channelInfo.getChannelArchLabel());
        ChecksumType checksumType = ChannelFactory.findChecksumTypeByLabel(channelInfo.getChecksumTypeLabel());

        Channel channel = new Channel();
        if (StringUtils.isNotEmpty(channelInfo.getOriginalChannelLabel())) {
            Channel originalChannel = ChannelFactory.lookupByLabel(channelInfo.getOriginalChannelLabel());

            ClonedChannel temp = new ClonedChannel();
            temp.setOriginal(originalChannel);
            channel = temp;
        }

        channel.setOrg(org);
        channel.setParentChannel(parentChannel);
        channel.setChannelArch(channelArch);

        channel.setLabel(channelInfo.getLabel());
        channel.setBaseDir(channelInfo.getBaseDir());
        channel.setName(channelInfo.getName());
        channel.setSummary(channelInfo.getSummary());
        channel.setDescription(channelInfo.getDescription());

        if (StringUtils.isNotEmpty(channelInfo.getProductNameLabel())) {
            channel.setProductName(MgrSyncUtils.findOrCreateProductName(channelInfo.getProductNameLabel()));
        }
        else {
            channel.setProductName(null);
        }

        channel.setGPGCheck(channelInfo.isGpgCheck());
        channel.setGPGKeyUrl(channelInfo.getGpgKeyUrl());
        channel.setGPGKeyId(channelInfo.getGpgKeyId());
        channel.setGPGKeyFp(channelInfo.getGpgKeyFp());

        channel.setEndOfLife(channelInfo.getEndOfLifeDate());
        channel.setChecksumType(checksumType);

        if (StringUtils.isNotEmpty(channelInfo.getChannelProductProduct()) &&
                StringUtils.isNotEmpty(channelInfo.getChannelProductVersion())) {
            channel.setProduct(MgrSyncUtils.findOrCreateChannelProduct(
                    channelInfo.getChannelProductProduct(), channelInfo.getChannelProductVersion()));
        }

        channel.setMaintainerName(channelInfo.getMaintainerName());
        channel.setMaintainerEmail(channelInfo.getMaintainerEmail());
        channel.setMaintainerPhone(channelInfo.getMaintainerPhone());
        channel.setSupportPolicy(channelInfo.getSupportPolicy());
        channel.setUpdateTag(channelInfo.getUpdateTag());
        channel.setInstallerUpdates(channelInfo.isInstallerUpdates());
        if (org != null) {
            channel.addChannelFamily(org.getPrivateChannelFamily());
        }

        // need to save before calling stored proc below
        ChannelFactory.save(channel);

        if (org != null) {
            // this ends up being a mode query call, must have saved the channel to get an id
            channel.setGloballySubscribable(false, org);
        }

        // rebuild repository
        ContentSyncManager csm = new ContentSyncManager();
        HubFactory hubFactory = new HubFactory();
        csm.refreshOrCreateRepository(channelInfo.getRepositoryInfo(), channel,
                hubFactory.lookupIssHub().orElse(null));

        return channel;
    }

    /**
     * Ensures that the channels json info structure is valid, as well as all consequent data
     *
     * @param channelInfoByLabel list of channel info structures to be checked
     * @throws IllegalArgumentException if something is wrong
     */
    public static void ensureValidChannelInfo(List<ChannelInfoDetailsJson> channelInfoByLabel) {
        ensureValidOrgIds(channelInfoByLabel);

        ensureExistingOrAboutToCreate(channelInfoByLabel, "channel arch",
                ChannelInfoDetailsJson::getChannelArchLabel, ChannelFactory::findArchByLabel);

        ensureExistingOrAboutToCreate(channelInfoByLabel, "checksum type",
                ChannelInfoDetailsJson::getChecksumTypeLabel, ChannelFactory::findChecksumTypeByLabel);
    }

    private static <T extends ChannelInfoDetailsJson>
    void ensureExistingOrAboutToCreate(List<T> channelInfoList,
                                       String informationTypeString,
                                       Function<T, String> searchLabelMethod,
                                       Function<String, Object> lookupLabelMethod) {
        Set<String> accumulationSet = new HashSet<>();

        for (T channelInfo : channelInfoList) {
            String searchLabel = searchLabelMethod.apply(channelInfo);
            boolean isEmptySearchLabel = StringUtils.isEmpty(searchLabel);

            if (isEmptySearchLabel) {
                throw new IllegalArgumentException(String.format("Channel searchLabel [%s] must have valid %s",
                        channelInfo.getLabel(), informationTypeString));
            }

            if (!accumulationSet.contains(searchLabel)) {
                if (null == lookupLabelMethod.apply(searchLabel)) {
                    throw new IllegalArgumentException(String.format("No %s named [%s] for channel [%s]",
                            informationTypeString, searchLabel, channelInfo.getLabel()));
                }
                accumulationSet.add(searchLabel);
            }
        }
    }

    private static <T extends ChannelInfoDetailsJson> void ensureValidOrgIds(List<T> channelInfoList) {
        Set<Long> orgSet = new HashSet<>();

        for (T channelInfo : channelInfoList) {
            Long orgId = channelInfo.getPeripheralOrgId();

            if (null == orgId) {
                // orgId NULL is a vendor channel
                continue;
            }

            if (!orgSet.contains(orgId)) {
                Org org = OrgFactory.lookupById(orgId);
                if (null == org) {
                    throw new IllegalArgumentException("No org id found [" + orgId +
                            "] for channel [" + channelInfo.getLabel() + "]");
                }
                orgSet.add(orgId);
            }

            Channel channel = ChannelFactory.lookupByLabel(channelInfo.getLabel());
            if ((null != channel) && (channel.isCustom())) {
                if (!orgId.equals(channel.getOrg().getId())) {
                    throw new IllegalArgumentException("Unable to modify org from [" + channel.getOrg().getId() +
                            "] to [" + orgId +
                            "] for channel [" + channelInfo.getLabel() + "]");
                }
            }
        }
    }

    private static <T> void setValueIfNotNull(Channel channelIn, T valueIn,
                                              BiConsumer<Channel, T> channelSetValueMethod) {
        if (null != valueIn) {
            channelSetValueMethod.accept(channelIn, valueIn);
        }
    }

    /**
     * Modifies a channel according to the info structure
     *
     * @param modifyChannelInfo the channel info with the info on how to modify the channel
     * @return the modified channel
     */
    public static Channel modifyChannel(ChannelInfoDetailsJson modifyChannelInfo) {

        Channel channel = ChannelFactory.lookupByLabel(modifyChannelInfo.getLabel());
        if (null == channel) {
            throw new IllegalArgumentException("No existing channel to modify with label [" +
                    modifyChannelInfo.getLabel() + "]");
        }

        if (null != modifyChannelInfo.getPeripheralOrgId()) {
            Org org = OrgFactory.lookupById(modifyChannelInfo.getPeripheralOrgId());
            if (null == org) {
                throw new IllegalArgumentException("No org id to modify [" +
                        modifyChannelInfo.getPeripheralOrgId() +
                        "] for channel [" + modifyChannelInfo.getLabel() + "]");
            }
            String channelOrgName = (null == channel.getOrg()) ? "vendor" : channel.getOrg().getName();
            if (!org.equals(channel.getOrg())) {
                throw new IllegalArgumentException("Unable to modify org from [" + channelOrgName +
                        "] to [" + org.getName() +
                        "] for channel [" + modifyChannelInfo.getLabel() + "]");
            }
        }

        if (StringUtils.isNotEmpty(modifyChannelInfo.getOriginalChannelLabel())) {
            Channel originalChannel =
                    ChannelFactory.lookupByLabel(modifyChannelInfo.getOriginalChannelLabel());

            if (null == originalChannel) {
                throw new IllegalArgumentException("No original channel to modify as original [" +
                        modifyChannelInfo.getOriginalChannelLabel() +
                        "] for channel [" + modifyChannelInfo.getLabel() + "]");
            }

            ChannelManager.forceBecomingCloneOf(channel, originalChannel);
        }

        setValueIfNotNull(channel, modifyChannelInfo.getBaseDir(), Channel::setBaseDir);
        setValueIfNotNull(channel, modifyChannelInfo.getName(), Channel::setName);
        setValueIfNotNull(channel, modifyChannelInfo.getSummary(), Channel::setSummary);
        setValueIfNotNull(channel, modifyChannelInfo.getDescription(), Channel::setDescription);

        if (null != modifyChannelInfo.getProductNameLabel()) {
            channel.setProductName(
                    MgrSyncUtils.findOrCreateProductName(modifyChannelInfo.getProductNameLabel()));
        }

        setValueIfNotNull(channel, modifyChannelInfo.isGpgCheck(), Channel::setGPGCheck);
        setValueIfNotNull(channel, modifyChannelInfo.getGpgKeyUrl(), Channel::setGPGKeyUrl);
        setValueIfNotNull(channel, modifyChannelInfo.getGpgKeyId(), Channel::setGPGKeyId);
        setValueIfNotNull(channel, modifyChannelInfo.getGpgKeyFp(), Channel::setGPGKeyFp);
        setValueIfNotNull(channel, modifyChannelInfo.getEndOfLifeDate(), Channel::setEndOfLife);

        if ((null != modifyChannelInfo.getChannelProductProduct()) &&
                (null != modifyChannelInfo.getChannelProductVersion())) {
            channel.setProduct(MgrSyncUtils.findOrCreateChannelProduct(
                    modifyChannelInfo.getChannelProductProduct(),
                    modifyChannelInfo.getChannelProductVersion()));
        }

        setValueIfNotNull(channel, modifyChannelInfo.getMaintainerName(), Channel::setMaintainerName);
        setValueIfNotNull(channel, modifyChannelInfo.getMaintainerEmail(), Channel::setMaintainerEmail);
        setValueIfNotNull(channel, modifyChannelInfo.getMaintainerPhone(), Channel::setMaintainerPhone);
        setValueIfNotNull(channel, modifyChannelInfo.getSupportPolicy(), Channel::setSupportPolicy);
        setValueIfNotNull(channel, modifyChannelInfo.getUpdateTag(), Channel::setUpdateTag);
        setValueIfNotNull(channel, modifyChannelInfo.isInstallerUpdates(), Channel::setInstallerUpdates);

        if (null != modifyChannelInfo.getRepositoryInfo()) {
            ContentSyncManager csm = new ContentSyncManager();
            HubFactory hubFactory = new HubFactory();
            csm.refreshOrCreateRepository(modifyChannelInfo.getRepositoryInfo(), channel,
                    hubFactory.lookupIssHub().orElse(null));
        }
        return channel;
    }
}
