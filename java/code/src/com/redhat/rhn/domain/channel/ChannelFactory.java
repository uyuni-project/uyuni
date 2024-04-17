/*
 * Copyright (c) 2009--2017 Red Hat, Inc.
 * Copyright (c) 2011--2021 SUSE LLC
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

import com.redhat.rhn.common.db.datasource.CallableMode;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.Row;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.common.ChecksumType;
import com.redhat.rhn.domain.kickstart.KickstartableTree;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.scc.SCCRepository;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.ssm.SsmChannelDto;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
     * @param id the id to search for
     * @return the Channel found
     */
    public static Channel lookupById(Long id) {
        Session session = HibernateFactory.getSession();
        return session.get(Channel.class, id);
    }

    /**
     * Lookup a Channel by id and User
     * @param id the id to search for
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
     * @param label the label to search for
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
     * @param label the label to lookup
     * @return the ContentSourceType
     */
    public static ContentSourceType lookupContentSourceType(String label) {
        return singleton.lookupObjectByNamedQuery("ContentSourceType.findByLabel", Map.of(LABEL, label));
    }

    /**
     * List all available content source types
     * @return list of ContentSourceType
     */
    public static List<ContentSourceType> listContentSourceTypes() {
        return singleton.listObjectsByNamedQuery("ContentSourceType.listAllTypes", Map.of());
    }

    /**
     * Lookup a content source by org
     * @param org the org to lookup
     * @return the ContentSource(s)
     */
    public static List<ContentSource> lookupContentSources(Org org) {
        return singleton.listObjectsByNamedQuery("ContentSource.findByOrg", Map.of("org", org));
    }

    /**
     * Lookup orphan vendor content source
     * @return the ContentSource(s)
     */
    public static List<ContentSource> lookupOrphanVendorContentSources() {
        return singleton.listObjectsByNamedQuery("ContentSource.findOrphanVendorContentSources", Map.of());
    }

    /**
     * Lookup orphan vendor channels
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
     * @param c the channel
     * @return repository
     */
    public static Optional<SCCRepository> findVendorRepositoryByChannel(Channel c) {
        return Optional.ofNullable(singleton.lookupObjectByNamedQuery("Channel.findVendorRepositoryByChannelId",
                Map.of("cid", c.getId())));
    }

    /**
     * Lookup a content source by org/channel
     * @param org the org to lookup
     * @param c the channel
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
     * @param org the org to lookup
     * @param label repo label
     * @return the ContentSource(s)
     */
    public static ContentSource lookupContentSourceByOrgAndLabel(Org org, String label) {
        return singleton.lookupObjectByNamedQuery("ContentSource.findByOrgAndLabel",
                Map.of("org", org, LABEL, label));
    }

    /**
     * Lookup a Vendor content source (org is NULL) by label
     * @param label repo label
     * @return the ContentSource(s)
     */
    public static ContentSource lookupVendorContentSourceByLabel(String label) {
        return singleton.lookupObjectByNamedQuery("ContentSource.findVendorContentSourceByLabel",
                Map.of(LABEL, label));
    }

    /**
     * Lookup a content source by org and repo
     * @param org the org to lookup
     * @param repoType repo type
     * @param repoUrl repo url
     * @return the ContentSource(s)
     */
    public static List<ContentSource> lookupContentSourceByOrgAndRepo(Org org,
                                                                      ContentSourceType repoType, String repoUrl) {
        return singleton.listObjectsByNamedQuery("ContentSource.findByOrgAndRepo",
                Map.of("org", org, "type_id", repoType.getId(), "url", repoUrl));
    }

    /**
     * lookup content source by id and org
     * @param id id of content source
     * @param orgIn org to check
     * @return content source
     */
    public static ContentSource lookupContentSource(Long id, Org orgIn) {
        return singleton.lookupObjectByNamedQuery("ContentSource.findByIdandOrg", Map.of("id", id, "org", orgIn));
    }

    /**
     * Lookup a content source's filters by id
     * @param id source id
     * @return the ContentSourceFilters
     */
    public static List<ContentSourceFilter> lookupContentSourceFiltersById(Long id) {
        return singleton.listObjectsByNamedQuery("ContentSourceFilter.findBySourceId", Map.of("source_id", id));
    }

    /**
     * Retrieve a list of channel ids associated with the labels provided
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
     * @param c Channel to be stored in database.
     */
    public static void save(Channel c) {
        c.setLastModified(new Date());
        singleton.saveObject(c);
    }

    /**
     * Insert or Update a content source.
     * @param c content source to be stored in database.
     */
    public static void save(ContentSource c) {
        singleton.saveObject(c);
    }

    /**
     * Insert or Update a DistChannelMap.
     * @param dcm DistChannelMap to be stored in database.
     */
    public static void save(DistChannelMap dcm) {
        singleton.saveObject(dcm);
    }

    /**
     * Insert or Update a content source filter.
     * @param f content source filter to be stored in database.
     */
    public static void save(ContentSourceFilter f) {
        singleton.saveObject(f);
    }

    /**
     * Remove a Channel from the DB
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
     * @param dcm Action to be removed from database.
     */
    public static void remove(DistChannelMap dcm) {
        singleton.removeObject(dcm);
    }

    /**
     * Remove a Content Source from the DB
     * @param src to be removed from database
     */
    public static void remove(ContentSource src) {
        singleton.removeObject(src);
    }

    /**
     * Remove a ContentSourceFilter from the DB
     * @param filter to be removed from database
     */
    public static void remove(ContentSourceFilter filter) {
        singleton.removeObject(filter);
    }

    /**
     * Returns the base channel for the given server id.
     * @param sid Server id whose base channel we want.
     * @return Base Channel for the given server id.
     */
    public static Channel getBaseChannel(Long sid) {
        return singleton.lookupObjectByNamedQuery("Channel.findBaseChannel", Map.of("sid", sid));
    }

    /**
     * Returns a list of Channels which have clonable errata.
     * @param org Org.
     * @return List of com.redhat.rhn.domain.Channel objects which have
     * clonable errata.
     */
    public static List<ClonedChannel> getChannelsWithClonableErrata(Org org) {
        return singleton.listObjectsByNamedQuery("Channel.channelsWithClonableErrata", Map.of("org", org), false);
    }

    /**
     * Returns the list of Channel ids which the given orgid has access to.
     * @param orgid Org id
     * @param cid Base Channel id.
     * @return the list of Channel ids which the given orgid has access to.
     */
    public static List<Channel> getUserAcessibleChannels(Long orgid, Long cid) {
        return singleton.listObjectsByNamedQuery("Channel.accessibleChildChannelIds",
                Map.of(ORG_ID, orgid, "cid", cid));
    }

    /**
     * Returns the accessible child channels associated to a base channel.
     * @param baseChannel the base channel who's child channels are needed
     * @param user the user requesting the info.. (has to be globally subscribed etc.)
     * @return the accessible child channels..
     */
    public static List<Channel> getAccessibleChildChannels(Channel baseChannel, User user) {
        return singleton.listObjectsByNamedQuery("Channel.accessibleChildChannels",
                Map.of("userId", user.getId(), "cid", baseChannel.getId()));
    }

    /**
     * Returns the list of Channels accessible by an org
     * Channels are accessible if they are owned by an org or public.
     * @param orgid The id for the org
     * @return A list of Channel Objects.
     */
    public static List<Channel> getAccessibleChannelsByOrg(Long orgid) {
        return singleton.listObjectsByNamedQuery("Org.accessibleChannels", Map.of(ORG_ID, orgid));
    }

    /**
     * Returns list of channel architectures
     * @return list of channel architectures
     */
    public static List<ChannelArch> getChannelArchitectures() {
        Session session = getSession();
        Criteria criteria = session.createCriteria(ChannelArch.class);
        return criteria.list();
    }

    /**
     * Checks if a channel is accessible by an Org.
     * @param channelLabel the channel label
     * @param orgId the Org ID
     * @return true if it is accessible
     */
    public static boolean isAccessibleBy(String channelLabel, Long orgId) {
        return (int)singleton.lookupObjectByNamedQuery("Channel.isAccessibleBy",
                Map.of("channel_label", channelLabel, ORG_ID, orgId)) > 0;
    }

    /**
     * Checks if a channel is accessible by a User.
     *
     * @param channelLabel the channel label
     * @param userId user id
     * @return true if it is accessible
     */
    public static boolean isAccessibleByUser(String channelLabel, Long userId) {
        return singleton.lookupObjectByNamedQuery("Channel.isAccessibleByUser",
                Map.of("channelLabel", channelLabel, "userId", userId)) != null;
    }

    /**
     * returns a ChannelArch by label
     * @param label ChannelArch label
     * @return a ChannelArch by label
     */
    public static ChannelArch findArchByLabel(String label) {
        Session session = getSession();
        Criteria criteria = session.createCriteria(ChannelArch.class);
        criteria.add(Restrictions.eq(LABEL, label));
        return (ChannelArch) criteria.uniqueResult();
    }

    /**
     * Returns the Channel whose label matches the given label.
     * @param org The org of the user looking up the channel
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
     * @param label Channel label sought.
     * @return the Channel whose label matches the given label.
     */
    public static Channel lookupByLabel(String label) {
        Session session = getSession();
        Criteria c = session.createCriteria(Channel.class);
        c.add(Restrictions.eq(LABEL, label));
        return (Channel) c.uniqueResult();
    }

    /**
     * Returns true if the given channel is globally subscribable for the
     * given org.
     * @param org Org
     * @param c Channel to validate.
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
     * @param org The org containing the channel
     * @param channel The channel in question
     * @param value True to make the channel globally subscribable, false to make it not
     * globally subscribable.
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
     * @param org The org in question
     * @param channel The channel in question
     * @param label the label of the setting to remove
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
     * @param org The org in question
     * @param channel The channel in question
     * @param label the label of the setting to add
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
     *
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
     * @param toChannelId cloned channle id
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
     * @param org org
     * @return list of channels
     */
    public static List<Channel> getKickstartableChannels(Org org) {
        return singleton.listObjectsByNamedQuery("Channel.kickstartableChannels",
                Map.of(ORG_ID, org.getId()), false);
    }

    /**
     * Get a list of base channels that have an org associated
     * @param user the logged in user
     * @return List of Channels
     */
    public static List<Channel> listCustomBaseChannels(User user) {
        return singleton.listObjectsByNamedQuery("Channel.findCustomBaseChannels", Map.of("user_id", user.getId()));
    }

    /**
     * Find yum supported checksum types
     * @return List of ChecksumTypes instances
     */
    public static List<ChecksumType> listYumSupportedChecksums() {
        return singleton.listObjectsByNamedQuery("ChecksumType.loadAllForYum", Map.of());
    }

    /**
     * Get a list of modular channels in users org
     * @param user the logged in user
     * @return List of modular channels
     */
    public static List<Channel> listModularChannels(User user) {
        List<Channel> channels = singleton.listObjectsByNamedQuery("Channel.findModularChannels",
                Map.of("org_id", user.getOrg().getId()));
        return channels;
    }

    /**
     * Find checksumtype by label
     * @param checksum checksum label
     * @return ChecksumType instance for given label
     */
    public static ChecksumType findChecksumTypeByLabel(String checksum) {
        if (checksum == null) {
            return null;
        }
        return singleton.lookupObjectByNamedQuery("ChecksumType.findByLabel", Map.of(LABEL, checksum));
    }

    /**
     * Get a list of packages ids that are in a channel and in a list of errata.
     * (The errata do not necessarily have to be associate with the channel)
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
     * Looksup the number of Packages in a channel
     * @param channel the Channel who's package count you are interested in.
     * @return number of packages in this channel.
     */
    public static int getPackageCount(Channel channel) {
        return singleton.lookupObjectByNamedQuery("Channel.getPackageCount", Map.of("cid", channel.getId()));
    }

    /**
     * Get the errata count for a channel
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
     * @param channel The channel object on which the lookup should be performed
     * @return ChannelSyncFlag object containing all flag settings for a specfic channel
     */
    public static ChannelSyncFlag lookupChannelReposyncFlag(Channel channel) {
        return getSession().createQuery("from ChannelSyncFlag where channel = :channel", ChannelSyncFlag.class)
        .setParameter("channel", channel).uniqueResult();
    }

    /**
     * Save a ChannelSyncFlag object for a specfic channel
     * @param flags The ChannelSyncFlag object which should be added to channel
     */
    public static void save(ChannelSyncFlag flags) {
        singleton.saveObject(flags);
    }

    /**
     * List all defined dist channel maps
     *
     * Returns empty array if none is found.
     *
     * @return DistChannelMap[], empty if none is found
     */
    public static List<DistChannelMap> listAllDistChannelMaps() {
        return singleton.listObjectsByNamedQuery("DistChannelMap.listAll", Map.of());
    }

    /**
     * Lists all dist channel maps for an user organization
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
     * @param org organization
     * @param productName Product name.
     * @param release Version.
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
     * @param org organization
     * @param release release
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
     * @param user user
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
     * @param user user
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
     * @param user user
     * @param parentChannelId id of the parent channel
     * @return List of child channel ids.
     */
    public static List<SsmChannelDto> findChildChannelsByParentInSSM(User user, long parentChannelId) {
        List<Object[]> res = singleton.listObjectsByNamedQuery("Channel.findChildChannelsByParentInSSM",
                Map.of("user_id", user.getId(), "parent_id", parentChannelId));
        return res
                .stream()
                .map(Arrays::asList)
                .map(r -> new SsmChannelDto((long)r.get(0), (String)r.get(1), r.get(2) != null))
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
     *
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
     * @return List of Channels
     */
    public static List<Channel> listRedHatBaseChannels() {
        return singleton.listObjectsByNamedQuery("Channel.findRedHatBaseChannels", Map.of());
    }


    /**
     * List all accessible Red Hat base channels for a given user
     * @param user logged in user
     * @return list of Red Hat base channels
     */
    public static List<Channel> listRedHatBaseChannels(User user) {
        return singleton.listObjectsByNamedQuery("Channel.findRedHatBaseChannelsByUserId",
                Map.of("userId", user.getId()));
    }

    /**
     * Lookup the original channel of a cloned channel
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
     * @return a distinct list of ChannelArch labels for all synch'd and custom
     * channels in the satellite.
     */
    public static List<String> findChannelArchLabelsSyncdChannels() {
        return singleton.listObjectsByNamedQuery("Channel.findChannelArchLabelsSyncdChannels", Map.of());
    }

    /**
     * List all accessible base channels for an org
     * @param user logged in user.
     * @return list of custom channels
     */
    public static List<Channel> listSubscribableBaseChannels(User user) {
        return singleton.listObjectsByNamedQuery("Channel.findSubscribableBaseChannels",
                Map.of("user_id", user.getId()));
    }

    /**
     * List all accessible base channels for an org
     * @param user logged in user.
     * @return list of custom channels
     */
    public static List<Channel> listAllBaseChannels(User user) {
        return singleton.listObjectsByNamedQuery("Channel.findAllBaseChannels",
                Map.of(ORG_ID, user.getOrg().getId(), "user_id", user.getId()));
    }

    /**
     * List all accessible base channels for the entire satellite
     * @return list of base channels
     */
    public static List<Channel> listAllBaseChannels() {
        return singleton.listObjectsByNamedQuery("Channel.findAllBaseChannelsOnSatellite", Map.of());
    }


    /**
     * List all child channels of the given parent regardless of the user
     * @param parent the parent channel
     * @return list of children of the parent
     */
    public static List<Channel> listAllChildrenForChannel(Channel parent) {
        return singleton.listObjectsByNamedQuery("Channel.listAllChildren", Map.of("parent", parent));
    }

    /**
     * Lookup a Package based on the channel and package file name
     * @param channel to look in
     * @param fileName to look up
     * @return Package if found
     */
    public static Package lookupPackageByFilename(Channel channel,
            String fileName) {

        List<Package> pkgs = HibernateFactory.getSession()
          .getNamedQuery("Channel.packageByFileName")
          .setString("pathlike", "%/" + fileName)
          .setLong("channel_id", channel.getId())
          .list();
        if (pkgs.isEmpty()) {
            return null;
        }
        return pkgs.get(0);
    }

    /**
     * Lookup a Package based on the channel, package file name and range
     * @param channel to look in
     * @param fileName to look up
     * @param headerStart start of header
     * @param headerEnd end of header
     * @return Package if found
     */
    public static Package lookupPackageByFilenameAndRange(Channel channel,
            String fileName, int headerStart, int headerEnd) {

        List<Package> pkgs = HibernateFactory.getSession()
          .getNamedQuery("Channel.packageByFileNameAndRange")
          .setString("pathlike", "%/" + fileName)
          .setLong("channel_id", channel.getId())
          .setInteger("headerStart", headerStart)
          .setInteger("headerEnd", headerEnd)
          .list();
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
     * @param ch the channel to check distros on
     * @return true of the channels contains any distros
     */
    public static boolean containsDistributions(Channel ch) {
        Criteria criteria = getSession().createCriteria(KickstartableTree.class);
        criteria.setProjection(Projections.rowCount());
        criteria.add(Restrictions.eq("channel", ch));
        return ((Number)criteria.uniqueResult()).intValue() > 0;
    }

    /**
     * Clear a content source's filters
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
     * @param org given organization
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
     * @param org given organization
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
     * @param c Channel to lock
     */
    public static void lock(Channel c) {
        singleton.lockObject(Channel.class, c.getId());
    }

    /**
     * Adds errata to channel mapping. Does nothing else
     * @param eids List of eids to add mappings for
     * @param cid channel id we're cloning into
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
     * List all vendor channels (org is null)
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
     * List all custom channels (org is not null) with at least one repository
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
     * @return list of vendor content sources
     */
    @SuppressWarnings("unchecked")
    public static List<ContentSource> listVendorContentSources() {
        Criteria criteria = getSession().createCriteria(ContentSource.class);
        criteria.add(Restrictions.isNull("org"));
        return criteria.list();
    }

    /**
     * Find a vendor content source (org is null) for a given repo URL.
     * @param repoUrl url to match against
     * @return vendor content source if it exists
     */
    public static ContentSource findVendorContentSourceByRepo(String repoUrl) {
        Criteria criteria = getSession().createCriteria(ContentSource.class);
        criteria.add(Restrictions.isNull("org"));
        if (repoUrl.contains("mirrorlist.centos.org") || repoUrl.contains("mirrors.rockylinux.org")) {
            criteria.add(Restrictions.eq("sourceUrl", repoUrl));
        }
        else {
            String [] parts = repoUrl.split("\\?");
            String repoUrlPrefix = parts[0];
            if (parts.length > 1) {
                criteria.add(Restrictions.like("sourceUrl", repoUrlPrefix + '?',
                        MatchMode.START));
            }
            else {
                criteria.add(Restrictions.eq("sourceUrl", repoUrlPrefix));
            }
        }
        return (ContentSource) criteria.uniqueResult();
    }

    /**
     * Find {@link ContentSource} with source url containing urlPart.
     * Uses SQL wildcard paramter '%'. When urlPart does contain a wildcard parameter, it is passed directly to
     * the query. If not, a wildcard is added and the begining and the end.
     * @param urlPart part of the url
     * @return list of found {@link ContentSource}
     */
    public static List<ContentSource> findContentSourceLikeUrl(String urlPart) {
        String urllike = urlPart;
        if (!urlPart.contains("%")) {
            urllike = String.format("%%%s%%", urlPart);
        }
        return getSession().createNamedQuery("ContentSource.findLikeUrl", ContentSource.class)
                .setParameter("urllike", urllike)
                .list();
    }

    /**
     * Find a {@link ChannelProduct} for given name and version.
     * @param product the product
     * @param version the version
     * @return channel product
     */
    public static ChannelProduct findChannelProduct(String product, String version) {
        Criteria criteria = getSession().createCriteria(ChannelProduct.class);
        criteria.add(Restrictions.eq("product", product));
        criteria.add(Restrictions.eq("version", version));
        return (ChannelProduct) criteria.uniqueResult();
    }

    /**
     * Insert or update a {@link ChannelProduct}.
     * @param channelProduct ChannelProduct to be stored in database.
     */
    public static void save(ChannelProduct channelProduct) {
        singleton.saveObject(channelProduct);
    }

    /**
     * Insert or update a {@link ProductName}.
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
     * @param to  the target Channel
     */
    public static void cloneModulesMetadata(Channel from, Channel to) {
        if (!from.isModular()) {
            if (to.isModular()) {
                HibernateFactory.getSession().delete(to.getModules());
                to.setModules(null);
            }
        }
        else {
            if (!to.isModular()) {
                Modules modules = new Modules();
                modules.setChannel(to);
                to.setModules(modules);
            }
            to.getModules().setRelativeFilename(from.getModules().getRelativeFilename());
        }
    }
}
