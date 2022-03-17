/*
 * Copyright (c) 2009--2016 Red Hat, Inc.
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
package com.redhat.rhn.domain.server;

import static java.util.stream.Collectors.toMap;

import com.redhat.rhn.common.client.ClientCertificate;
import com.redhat.rhn.common.client.InvalidCertificateException;
import com.redhat.rhn.common.db.datasource.CallableMode;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.channel.ChannelArch;
import com.redhat.rhn.domain.credentials.Credentials;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.dto.SystemIDInfo;
import com.redhat.rhn.domain.entitlement.Entitlement;
import com.redhat.rhn.domain.org.CustomDataKey;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.product.Tuple2;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.HistoryEvent;
import com.redhat.rhn.frontend.xmlrpc.ChannelSubscriptionException;
import com.redhat.rhn.frontend.xmlrpc.ServerNotInGroupException;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.system.UpdateBaseChannelCommand;

import com.suse.manager.model.maintenance.MaintenanceSchedule;
import com.suse.manager.webui.services.pillar.MinionPillarManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;

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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

/**
 * ServerFactory - the singleton class used to fetch and store
 * com.redhat.rhn.domain.server.Server objects from the database.
 */
public class ServerFactory extends HibernateFactory {

    private static Logger log = Logger.getLogger(ServerFactory.class);

    public static final ServerFactory SINGLETON = new ServerFactory();

    private ServerFactory() {
        super();
    }

    /**
     * Looks up the CustomDataValue given CustomDataKey and Server objects.
     * @param key The Key for the value you would like to lookup
     * @param server The Server in question
     * @return Returns the CustomDataValue object if found, null if not.
     */
    protected static CustomDataValue getCustomDataValue(CustomDataKey key,
            Server server) {
        // Make sure we didn't recieve any nulls
        if (key == null || server == null) {
            return null;
        }

        Session session = HibernateFactory.getSession();
        return (CustomDataValue) session.getNamedQuery(
                "CustomDataValue.findByServerAndKey").setParameter("server",
                        server).setParameter("key", key)
                        // Retrieve from cache if there
                        .setCacheable(true).uniqueResult();
    }

    /**
     * Remove the custom data values associated with a server.
     * @param server The server
     */
    public static void removeCustomDataValues(Server server) {
        for (Object value : server.getCustomDataValues()) {
            SINGLETON.removeObject(value);
        }
        server.getCustomDataValues().clear();
        server.asMinionServer().ifPresent(minion -> MinionPillarManager.INSTANCE.generatePillar(minion, false,
            MinionPillarManager.PillarSubset.CUSTOM_INFO));
    }

    /**
     * Remove the custom data value associated with the custom data key
     * provided for the given server.
     * @param server The server
     * @param key The custom data key
     */
    public static void removeCustomDataValue(Server server, CustomDataKey key) {
        CustomDataValue value = server.getCustomDataValue(key);
        server.getCustomDataValues().remove(value);
        if (value != null) {
            SINGLETON.removeObject(value);
        }
        server.asMinionServer().ifPresent(minion -> MinionPillarManager.INSTANCE.generatePillar(minion, false,
            MinionPillarManager.PillarSubset.CUSTOM_INFO));
    }

    /**
     * Lookup all CustomDataValues associated with the CustomDataKey.
     * @param key The Key for the values you would like to lookup
     * @return List of custom data values.
     */
    public static List<CustomDataValue> lookupCustomDataValues(CustomDataKey key) {
        Map<String, Object> params = new HashMap<>();
        params.put("key", key);
        return SINGLETON.listObjectsByNamedQuery(
                "CustomDataValue.findByKey", params);
    }

    /**
     * Lookup all MinionServers subscribed to the channel.
     * @param cid The channel id
     * @return List of minions.
     */
    public static List<MinionServer> listMinionsByChannel(long cid) {
        Map<String, Object> params = new HashMap<>();
        params.put("cid", cid);
        return SINGLETON.listObjectsByNamedQuery(
                "Server.listMinionsByChannel", params);
    }

    /**
     * Lookup all the systems with the specified CustomDataKey.
     * @param userId The User ID of the user doing the query
     * @param cikid The ID of the Key for the values you would like to lookup
     * @return List of systems
     */
    public static List lookupServersWithCustomKey(Long userId, Long cikid) {
        SelectMode m = ModeFactory.getMode("System_queries",
                "users_systems_with_value_for_key");
        Map inParams = new HashMap();

        inParams.put("user_id", userId);
        inParams.put("cikid", cikid);

        return m.execute(inParams);
    }

    /**
     * Lookup all storage Devices associated with the server.
     * @param s The server for the values you would like to lookup
     * @return List of devices
     */
    public static List<Device> lookupStorageDevicesByServer(Server s) {
        Map<String, Object> params = new HashMap<>();
        params.put("server", s);
        return SINGLETON.listObjectsByNamedQuery(
                "Device.findStorageByServer", params);
    }

    /**
     * Looks up a proxy server by hostname.
     * @param name the hostname
     * @return the server, if it is found
     */
    @SuppressWarnings("unchecked")
    public static Optional<Server> lookupProxyServer(String name) {
        boolean nameIsFullyQualified = name.contains(".");
        if (!nameIsFullyQualified) {
            log.warn("Specified master name \"" + name + "\" is not fully-qualified," +
                    "proxy attachment might not be correct");
            log.warn("Please use a FQDN in /etc/salt/minion.d/susemanager.conf");
        }

        DetachedCriteria proxyIds = DetachedCriteria.forClass(ProxyInfo.class)
                .setProjection(Projections.property("server.id"));

        Optional<Server> result = findByFqdn(name);

        if (result.isPresent()) {
            return result;
        }

        result = HibernateFactory.getSession()
                .createCriteria(Server.class)
                .add(Subqueries.propertyIn("id", proxyIds))
                .add(Restrictions.eq("hostname", name))
                .list()
                .stream()
                .findFirst();

        if (result.isPresent()) {
            return result;
        }

        // precise search did not work, try imprecise
        if (nameIsFullyQualified) {
            String srippedHostname = name.split("\\.")[0];

            return HibernateFactory.getSession()
                    .createCriteria(Server.class)
                    .add(Subqueries.propertyIn("id", proxyIds))
                    .add(Restrictions.eq("hostname", srippedHostname))
                    .list()
                    .stream()
                    .findFirst();
        }
        else {
            return HibernateFactory.getSession()
                    .createCriteria(Server.class)
                    .add(Subqueries.propertyIn("id", proxyIds))
                    .add(Restrictions.like("hostname", name + ".", MatchMode.START))
                    .list()
                    .stream()
                    .findFirst();
        }
    }

    /**
     * Return a map from Salt minion IDs to System IDs.
     * Map entries are limited to systems that are visible by the specified user.
     *
     * @param id a user ID
     * @return the minion ID to system ID map
     */
    public static Map<String, Long> getMinionIdMap(Long id) {
        var params = new HashMap<String, Object>();
        params.put("user_id", id);
        List<Object[]> result = SINGLETON.listObjectsByNamedQuery("Server.listMinionIdMappings", params);
        return result.stream().collect(toMap(
                row -> (String)(row[0]),
                row -> (Long)(row[1]))
        );
    }

    /**
     * Regenerate the package needed cache for the server
     *
     * @param serverId the server ID
     */
    public static void updateServerNeededCache(long serverId) {
        CallableMode m = ModeFactory.getCallableMode("System_queries", "update_needed_cache");
        Map inParams = new HashMap();
        inParams.put("server_id", serverId);

        m.execute(inParams, new HashMap());
    }

    /**
     * Get the Logger for the derived class so log messages show up on the
     * correct class
     */
    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Create a new Server from scratch
     * @return the Server created
     */
    public static Server createServer() {
        return new Server();
    }

    /**
     * Creates a set of new {@link ServerPath} objects using the <code>proxyServer</code>'s
     * server path (if any).
     *
     * @param server the server
     * @param proxyServer the proxy server to which <code>server</code> connects directly
     * @param proxyHostname the proxy hostname
     * @return a set of {@link ServerPath} objects where the proxy to which the
     * <code>server</code> connects directly has position 0, the second proxy
     * has position 1, etc
     */
    public static Set<ServerPath> createServerPaths(Server server, Server proxyServer,
                                              String proxyHostname) {
        Set<ServerPath> paths = new HashSet<>();
        for (ServerPath parentPath : proxyServer.getServerPaths()) {
            ServerPath path = findServerPath(server, parentPath.getId().getProxyServer()).orElseGet(() -> {
                ServerPath newPath = new ServerPath();
                newPath.setId(new ServerPathId(server, parentPath.getId().getProxyServer()));
                newPath.setHostname(parentPath.getHostname());
                return newPath;
            });
            path.setPosition(parentPath.getPosition() + 1);
            paths.add(path);

        }
        ServerPath path = findServerPath(server, proxyServer).orElseGet(() -> {
            ServerPath newPath = new ServerPath();
            newPath.setId(new ServerPathId(server, proxyServer));
            // the first proxy is the one to which
            // the server connects directly
            newPath.setHostname(proxyHostname);
            return newPath;
        });
        path.setPosition(0L);
        paths.add(path);
        return paths;
    }

    /**
     * Finds a Server Path identified by proxyServer
     * @param server the server
     * @param proxyServer the proxy server to which <code>server</code> connects directly
     * @return the ServerPath if found
     */
    public static Optional<ServerPath> findServerPath(Server server, Server proxyServer) {
        if (server.getId() == null) {
            // not yet persisted, return empty to avoid Hibernate error
            // on query with a transient object
            return Optional.empty();
        }
        CriteriaBuilder builder = HibernateFactory.getSession().getCriteriaBuilder();
        CriteriaQuery<ServerPath> criteria = builder.createQuery(ServerPath.class);
        Root<ServerPath> root = criteria.from(ServerPath.class);
        Path<ServerPathId> id = root.get("id");
        criteria.where(builder.and(
                builder.equal(id.get("server"), server),
                builder.equal(id.get("proxyServer"), proxyServer)));
        return getSession().createQuery(criteria).uniqueResultOptional();
    }

    /**
     * Adds Servers to a server group.
     * @param servers the servers to add
     * @param serverGroup The group to add the servers to
     */
    public static void addServersToGroup(Collection<Server> servers, ServerGroup serverGroup) {
        List<Long> serverIdsToAdd = servers.stream().filter(s -> s.getOrgId().equals(serverGroup.getOrgId()))
                .map(Server::getId).collect(Collectors.toList());

        boolean serversUpdated = insertServersToGroup(serverIdsToAdd, serverGroup.getId());

        if (serversUpdated) {
            servers.stream().forEach(s -> s.addGroup(serverGroup));
            if (serverGroup.isManaged()) {
                updatePermissionsForServerGroup(serverGroup.getId());
            }
        }
    }

    private static boolean insertServersToGroup(List<Long> serverIds, Long sgid) {
        WriteMode m = ModeFactory.getWriteMode("System_queries", "add_servers_to_server_group");

        Map params = new HashMap();
        params.put("sgid", sgid);

        int insertsCount = m.executeUpdate(params, serverIds);

        if (insertsCount > 0) {
            updateCurrentMembersOfServerGroup(sgid, insertsCount);
            return true;
        }
        return false;
    }

    private static void updateCurrentMembersOfServerGroup(Long sgid, int membersCount) {
        WriteMode mode = ModeFactory.getWriteMode("System_queries", "update_current_members_of_server_group");

        Map params = new HashMap();
        params.put("sgid", sgid);
        params.put("members_count", membersCount);

        mode.executeUpdate(params);
    }

    private static void updatePermissionsForServerGroup(Long sgid) {
        CallableMode m = ModeFactory.getCallableMode("System_queries",
                "update_permissions_for_server_group");
        Map params = new HashMap();
        params.put("sgid", sgid);

        m.execute(params, new HashMap());
    }

    /**
     * Adds a Server to a group.
     * @param serverIn The server to add
     * @param serverGroupIn The group to add the server to
     */
    public static void addServerToGroup(Server serverIn, ServerGroup serverGroupIn) {
        addServersToGroup(Arrays.asList(serverIn), serverGroupIn);
    }

    /**
     * Adds a server history event after an entitlement event occurred
     * @param server the server
     * @param ent the entitlement
     * @param summary the summary of the event
     */
    public static void addServerHistoryWithEntitlementEvent(Server server, Entitlement ent, String summary) {
        Map<String, Object> in = new HashMap<>();
        in.put("sid", server.getId());
        in.put("entitlement_label", ent.getLabel());
        in.put("summary", summary);

        WriteMode m = ModeFactory.getWriteMode("System_queries", "update_server_history_for_entitlement_event");
        m.executeUpdate(in);

        log.debug("update_server_history_for_entitlement_event mode query executed.");
    }

    /**
     * Removes Servers from a server group.
     * @param servers The servers to remove
     * @param serverGroup The group to remove the servers from
     */
    public static void removeServersFromGroup(Collection<Server> servers, ServerGroup serverGroup) {
        List<Long> serverIdsToAdd = servers.stream().filter(s -> s.getOrgId().equals(serverGroup.getOrgId()))
                .map(Server::getId).collect(Collectors.toList());

        boolean serversUpdated = removeServersFromGroup(serverIdsToAdd, serverGroup.getId());

        if (serversUpdated) {
            servers.stream().forEach(s -> s.removeGroup(serverGroup));
            if (serverGroup.isManaged()) {
                updatePermissionsForServerGroup(serverGroup.getId());
            }
        }
        else {
            throw new ServerNotInGroupException();
        }
    }

    private static boolean removeServersFromGroup(List<Long> serverIds, Long sgid) {
        WriteMode m = ModeFactory.getWriteMode("System_queries", "delete_from_servergroup");

        Map params = new HashMap();
        params.put("sgid", sgid);

        int removesCount = m.executeUpdate(params, serverIds);

        if (removesCount > 0) {
            updateCurrentMembersOfServerGroup(sgid, -removesCount);
            return true;
        }
        return false;
    }

    /**
     * Removes a Server from a group.
     * @param serverIn The server to remove
     * @param serverGroupIn The group to remove the server from
     */
    public static void removeServerFromGroup(Server serverIn, ServerGroup serverGroupIn) {
        removeServersFromGroup(Arrays.asList(serverIn), serverGroupIn);
    }

    /**
     * Lookup a Server with the ClientCertificate.
     * @param clientcert ClientCertificate for the server wanted.
     * @return the Server found if certificate is valid, null otherwise.
     * @throws InvalidCertificateException thrown if certificate is invalid.
     */
    public static Server lookupByCert(ClientCertificate clientcert)
            throws InvalidCertificateException {

        String idstr = clientcert.getValueByName(ClientCertificate.SYSTEM_ID);
        String[] parts = StringUtils.split(idstr, '-');
        if (parts != null && parts.length > 0) {
            Long sid = Long.valueOf(parts[1]);
            Server s = ServerFactory.lookupById(sid);
            if (s != null) {
                clientcert.validate(s.getSecret());
                return s;
            }
        }

        return null;
    }

    /**
     * Retrieves the ids of the unscheduled erratas for a given set of servers
     * @param user the ids of the servers
     * @param serverIds the ids of the servers
     * @return the ids of the erratas grouped by server id
     */
    public static Map<Long, List<Long>> findUnscheduledErrataByServerIds(User user, List<Long> serverIds) {
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());

        List<Object[]> result = (SINGLETON.listObjectsByNamedQuery("Server.findUnscheduledErrataByServerIds",
                params, serverIds, "serverIds"));

        return result.stream().collect(
                Collectors.groupingBy(
                        row -> Long.valueOf(row[0].toString()),
                        Collectors.mapping(row -> Long.valueOf(row[1].toString()), Collectors.toList())
                        )
                );
    }

    /**
     * Lookup a list of servers by their ids
     * @param serverIds the server ids to search for
     * @param org the organization who owns the server
     * @return the list of servers
     */
    public static List<Server> lookupByIdsAndOrg(Set<Long> serverIds, Org org) {
        Map<String, Object> params = new HashMap<>();
        params.put("orgId", org.getId());
        return SINGLETON.listObjectsByNamedQuery("Server.findByIdsAndOrgId", params, serverIds, "serverIds");
    }

    /**
     * Retrieves the ids of the non-zypper traditional clients given a set of server ids
     * @param ids the server ids to search for
     * @return the list of non-zypper server ids
     */
    public static List<Long> findNonZypperTradClientsIds(Set<Long> ids) {
        Map<String, Object> params = new HashMap<>();
        return SINGLETON.listObjectsByNamedQuery("Server.findNonZypperTradClientsIds", params, ids,
                "serverIds");
    }

    /**
     * Lookup a Server by their id
     * @param id the id to search for
     * @param orgIn Org who owns the server
     * @return the Server found (null if not or not member if orgIn)
     */
    public static Server lookupByIdAndOrg(Long id, Org orgIn) {
        Map<String, Object> params = new HashMap<>();
        params.put("sid", id);
        params.put("orgId", orgIn.getId());
        return (Server) SINGLETON.lookupObjectByNamedQuery(
                "Server.findByIdandOrgId", params);
    }

    /**
     * Looks up server objects from the given list of server IDs.
     *
     * @param serverIds List of server IDs.
     * @param user who wants to lookup the Server
     * @return list of server objects
     */
    public static List<Server> lookupByIdsAndUser(List<Long> serverIds, User user) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("orgId", user.getOrg().getId());

        return findByIds(serverIds, "Server.findByIdsAndOrgId", "serverIds", parameters);
    }

    /**
     * Returns list of the ID information of all systems visible to user and are entitled with the passed entitlement.
     * @param user the logged in user.
     * @param entitlement the entitlement.
     *
     * @return list of system IDs.
     */
    public List<SystemIDInfo> lookupSystemsVisibleToUserWithEntitlement(User user, String entitlement) {
        SelectMode mode = ModeFactory.getMode("System_queries", "systems_visible_to_user_with_entitlement", Map.class);

        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("entitlement_label", entitlement);

        DataResult<Map<String, Object>> dr = mode.execute(params);

        return dr.stream().map(m -> new SystemIDInfo((Long) m.get("id"), (String) m.get("name")))
                .collect(Collectors.toList());
    }

    /**
     * Lookup a Server by their id
     * @param id the id to search for
     * @return the Server found
     */
    public static Server lookupById(Long id) {
        return HibernateFactory.getSession().get(Server.class, id);
    }

    /**
     * lookup System with specified digital server id which are foreign_entitled
     *
     * @param id the digital server id
     * @return server corresponding to the given id
     */
    @SuppressWarnings("unchecked")
    public static Server lookupForeignSystemByDigitalServerId(String id) {
        Criteria criteria = getSession().createCriteria(Server.class);
        criteria.add(Restrictions.eq("digitalServerId", id));
        for (Server server : (List<Server>) criteria.list()) {
            if (server.hasEntitlement(EntitlementManager.getByName("foreign_entitled"))) {
                return server;
            }
        }
        return null;
    }

    /**
     * Lookup Servers by their ids
     * @param ids the ids to search for
     * @return the Servers found
     */
    public static List<Server> lookupByIds(List<Long> ids) {
        return lookupByServerIds(ids, "Server.findByIds");
    }

    /**
     * Lookup Servers by their ids
     * @param <T> the type of the returned servers
     * @param ids the ids to search for
     * @param queryName the name of the query to be executed
     * @return the Servers found
     */
    public static <T extends Server> List<T> lookupByServerIds(List<Long> ids, String queryName) {
        return findByIds(ids, queryName, "serverIds");
    }

    /**
     * Lookup a ServerGroupType by its label
     * @param label The label to search for
     * @return The ServerGroupType
     */
    public static ServerGroupType lookupServerGroupTypeByLabel(String label) {
        return (ServerGroupType) HibernateFactory.getSession().getNamedQuery("ServerGroupType.findByLabel")
                .setString("label", label)
                // Retrieve from cache if there
                .setCacheable(true).uniqueResult();

    }

    /**
     * Insert or Update a Server.
     * @param serverIn Server to be stored in database.
     */
    public static void save(Server serverIn) {
        SINGLETON.saveObject(serverIn);
        updateServerPerms(serverIn);
    }

    /**
     * Insert or Update a ServerPath.
     * @param pathIn ServerPath to be stored in database.
     */
    public static void save(ServerPath pathIn) {
        SINGLETON.saveObject(pathIn);
    }

    /**
     * Insert or Update InstalledProduct
     * @param productIn product to store in database
     */
    public static void save(InstalledProduct productIn) {
        SINGLETON.saveObject(productIn);
    }

    /**
     * Save a custom data key
     * @param keyIn the key to save
     */
    public static void saveCustomKey(CustomDataKey keyIn) {
        SINGLETON.saveObject(keyIn);
    }

    /**
     * Remove a custom data key. This will also remove all systems'
     * values for the key.
     * @param keyIn the key to remove
     */
    public static void removeCustomKey(CustomDataKey keyIn) {

        // If the CustomKey is being removed, any system that has a
        // "Custom Info" value associated with it must have that
        // value removed...
        List<CustomDataValue> values = lookupCustomDataValues(keyIn);
        for (CustomDataValue value : values) {
            Server server = value.getServer();
            server.getCustomDataValues().remove(value);
            SINGLETON.removeObject(value);
            server.asMinionServer().ifPresent(minion -> MinionPillarManager.INSTANCE.generatePillar(minion, false,
                    MinionPillarManager.PillarSubset.CUSTOM_INFO));
        }

        SINGLETON.removeObject(keyIn);
    }

    /**
     * Remove proxy info object associated to the server.
     * @param server the server to deProxify
     */
    public static void deproxify(Server server) {
        if (server.getProxyInfo() != null) {
            ProxyInfo info = server.getProxyInfo();
            SINGLETON.removeObject(info);
            server.setProxyInfo(null);
        }
    }

    /**
     * Deletes a server
     *
     * @param server The server to delete
     */
    public static void delete(Server server) {
        HibernateFactory.getSession().evict(server);
        CallableMode m = ModeFactory.getCallableMode("System_queries",
                "delete_server");
        Map in = new HashMap();
        in.put("server_id", server.getId());
        m.execute(in, new HashMap());
        HibernateFactory.getSession().clear();
    }

    private static void updateServerPerms(Server server) {
        CallableMode m = ModeFactory.getCallableMode("System_queries",
                "update_perms_for_server");
        Map inParams = new HashMap();
        inParams.put("sid", server.getId());
        m.execute(inParams, new HashMap());
    }

    /**
     * Lookup a ServerArch by its label
     * @param label The label to search for
     * @return The ServerArch
     */
    public static ServerArch lookupServerArchByLabel(String label) {
        Session session = HibernateFactory.getSession();
        return (ServerArch) session.getNamedQuery("ServerArch.findByLabel")
                .setString("label", label)
                // Retrieve from cache if there
                .setCacheable(true).uniqueResult();
    }

    /**
     * Lookup a ServerArch by its name
     * @param name The name to search for
     * @return The first ServerArch found
     */
    @SuppressWarnings("unchecked")
    public static ServerArch lookupServerArchByName(String name) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        List<ServerArch> archs = SINGLETON.listObjectsByNamedQuery(
                "ServerArch.findByName",
                params);
        if (archs != null && archs.size() > 0) {
            return archs.get(0);
        }
        return null;
    }

    /**
     * Lookup a CPUArch by its name
     * @param name The name to search for
     * @return The CPUArch
     */
    public static CPUArch lookupCPUArchByName(String name) {
        Session session = HibernateFactory.getSession();
        return (CPUArch) session.getNamedQuery("CPUArch.findByName")
                .setString("name", name)
                // Retrieve from cache if there
                .setCacheable(true).uniqueResult();
    }

    /**
     * Returns a list of Servers which are compatible with the given server.
     * @param user User owner
     * @param server Server whose profiles we want.
     * @return a list of Servers which are compatible with the given server.
     */
    public static List<Map<String, Object>> compatibleWithServer(User user, Server server) {
        SelectMode m = ModeFactory.getMode("System_queries",
                "compatible_with_server");

        Map<String, Object> params = new HashMap<>();
        params.put("sid", server.getId());
        params.put("user_id", user.getId());
        return m.execute(params);
    }

    /**
     * Returns the admins of a given server This includes
     * @param server the server to find the admins of
     * @return list of User objects that can administer the system
     */
    public static List<User> listAdministrators(Server server) {
        Map<String, Object> params = new HashMap<>();
        params.put("sid", server.getId());
        params.put("org_id", server.getOrg().getId());
        return SINGLETON.listObjectsByNamedQuery("Server.lookupAdministrators",
                params);
    }

    /**
     * For a {@link ServerArch}, find the compatible {@link ChannelArch}.
     * @param serverArch server arch
     * @return channel arch
     */
    public static ChannelArch findCompatibleChannelArch(ServerArch serverArch) {
        Map<String, Object> params = new HashMap<>();
        params.put("server_arch_id", serverArch.getId());
        return (ChannelArch) SINGLETON.lookupObjectByNamedQuery("ServerArch.findCompatibleChannelArch",
                params, true);
    }

    /**
     * gets a server's history sorted by creation time. This includes items from
     * the rhnServerHistory and rhnAction* tables
     * @param server the server who's history you want
     * @return A list of ServerHistoryEvents
     */
    public static List<HistoryEvent> getServerHistory(Server server) {

        SelectMode m = ModeFactory.getMode("Action_queries",
                "system_events_history");
        Map<String, Long> params = new HashMap<>();
        params.put("sid", server.getId());

        return m.execute(params);
    }

    /**
     * List all proxies for a given org
     * @param user the user, who's accessible proxies will be returned.
     * @return a list of Proxy Server objects
     */
    public static List<Server> lookupProxiesByOrg(User user) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", user.getId());
        params.put("orgId", user.getOrg().getId());
        List<Number> ids = SINGLETON.listObjectsByNamedQuery(
                "Server.listProxies", params);
        List<Server> servers = new ArrayList(ids.size());
        for (Number id : ids) {
            servers.add(lookupById(id.longValue()));
        }
        return servers;
    }

    /**
     * Clear out all subscriptions for a particular server
     * @param user User doing the un-subscription
     * @param server Server that is unsubscribing
     * @return new Server object showing all the new channel info.
     */
    public static Server unsubscribeFromAllChannels(User user, Server server) {
        UpdateBaseChannelCommand command = new UpdateBaseChannelCommand(user,
                server, -1L);
        ValidatorError error = command.store();
        if (error != null) {
            throw new ChannelSubscriptionException(error.getKey());
        }
        return HibernateFactory.reload(server);
    }

    /**
     * Returns a list of Server objects currently selected in the System Set
     * Manager. This list will <strong>not</strong> contain servers that are
     * proxies.
     *
     * @param user User requesting.
     * @return List of servers.
     */
    public static List<Server> listSystemsInSsm(User user) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", user.getId());
        params.put("label", RhnSetDecl.SYSTEMS.getLabel());
        return SINGLETON.listObjectsByNamedQuery("Server.findInSet", params);
    }

    /**
     * Returns a global multi org spanning list of
     * servers that are config enabled.
     * Basically used by taskomatic.
     * @return a list of config enabled systems
     */
    public static List<Server> listConfigEnabledSystems() {
        return SINGLETON.listObjectsByNamedQuery(
                "Server.listConfigEnabledSystems", Collections.EMPTY_MAP);
    }

    /**
     * Returns a List of FQDNs associated to the system
     * @param sid the server id to check for. Required.
     * @return a list of FQDNs
     */
    public static List<String> listFqdns(Long sid) {
        Map<String, Object> params = new HashMap<>();
        params.put("sid", sid);
        return SINGLETON.listObjectsByNamedQuery("Server.listFqdns", params);
    }

    /**
     * Lookup a Server by their FQDN
     * @param name of the FQDN to search for
     * @return the Server found
     */
    public static Optional<Server> findByFqdn(String name) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        return Optional.ofNullable((Server) SINGLETON
                .lookupObjectByNamedQuery("Server.findByFqdn", params));
    }

    /**
     * Returns a global multi org spanning list of
     * servers that are config-diff enabled.
     * Basically used by taskomatic.
     * @return a list of config-diff enabled systems
     */
    public static List<Server> listConfigDiffEnabledSystems() {
        return SINGLETON.listObjectsByNamedQuery(
                "Server.listConfigDiffEnabledSystems", Collections.EMPTY_MAP);
    }

    /**
     * List snapshots associated with a server.
     *
     * A user may optionally provide a start and end date to narrow the snapshots that
     * will be listed.  For example,
     * - If user provides startDate only, all snapshots created either on or after the
     *   date provided will be returned
     * - If user provides startDate and endDate, all snapshots created on or between the
     *   dates provided will be returned
     * - If the user doesn't provide a startDate and endDate, all snapshots associated with
     *   the server will be returned.
     *
     * @param org the org doing the request. Required.
     * @param server the server to check for. Required.
     * @param startDate the start date.  Optional, unless endDate is provided.
     * @param endDate the end date. Optional.
     * @return list of ServerSnapshot objects
     */
    public static List<ServerSnapshot> listSnapshots(Org org, Server server,
            Date startDate, Date endDate) {

        Map<String, Object> params = new HashMap<>();
        params.put("org", org);
        params.put("server", server);

        List<ServerSnapshot> snaps = null;

        if ((startDate != null) && (endDate != null)) {
            params.put("start_date", startDate);
            params.put("end_date", endDate);
            snaps = SINGLETON.listObjectsByNamedQuery(
                    "ServerSnapshot.findBetweenDates", params);
        }
        else if (startDate != null) {
            params.put("start_date", startDate);
            snaps = SINGLETON.listObjectsByNamedQuery(
                    "ServerSnapshot.findAfterDate", params);
        }
        else {
            snaps = SINGLETON.listObjectsByNamedQuery(
                    "ServerSnapshot.findForServer", params);
        }
        return snaps;
    }

    /**
     * Looks up a server snapshot by it's id
     * @param id the snap id
     * @return the server snapshot
     */
    public static ServerSnapshot lookupSnapshotById(Integer id) {
        Map<String, Object> params = new HashMap<>();
        params.put("snapId", Long.valueOf(id));
        return (ServerSnapshot) SINGLETON.lookupObjectByNamedQuery(
                "ServerSnapshot.findById", params);
    }

    /**
     * Looks up a latest snapshot for a sever
     * @param server server
     * @return the server snapshot
     */
    public static ServerSnapshot lookupLatestForServer(Server server) {
        Map params = new HashMap();
        params.put("sid", server);
        return (ServerSnapshot) SINGLETON.lookupObjectByNamedQuery(
                "ServerSnapshot.findLatestForServer", params);
    }

    /**
     * Save a server snapshot
     * @param snapshotIn snapshot to save
     */
    public static void saveSnapshot(ServerSnapshot snapshotIn) {
        SINGLETON.saveObject(snapshotIn);
    }

    /**
     * Delete a snapshot
     * @param snap the snapshot to delete
     */
    public static void deleteSnapshot(ServerSnapshot snap) {
        ServerFactory.getSession().delete(snap);
    }

    /**
     * Delete a server path
     * @param path the server path to delete
     */
    public static void deleteServerPath(ServerPath path) {
        ServerFactory.getSession().delete(path);
    }

    /**
     * Delete snapshots across servers in the org.
     *
     * A user may optionally provide a start and end date to narrow the snapshots that
     * will be removed.  For example,
     * - If user provides startDate only, all snapshots created either on or after the
     *   date provided will be removed
     * - If user provides startDate and endDate, all snapshots created on or between the
     *   dates provided will be removed
     * - If the user doesn't provide a startDate and endDate, all snapshots associated with
     *   the server will be removed.
     *
     * @param org the org doing the request. Required.
     * @param startDate the start date.  Optional, unless endDate is provided.
     * @param endDate the end date. Optional.
     */
    public static void deleteSnapshots(Org org, Date startDate, Date endDate) {

        if ((startDate != null) && (endDate != null)) {
            HibernateFactory.getSession()
            .getNamedQuery("ServerSnapshot.deleteBetweenDates")
            .setParameter("org", org)
            .setParameter("start_date", startDate)
            .setParameter("end_date", endDate)
            .executeUpdate();
        }
        else if (startDate != null) {
            HibernateFactory.getSession()
            .getNamedQuery("ServerSnapshot.deleteAfterDate")
            .setParameter("org", org)
            .setParameter("start_date", startDate)
            .executeUpdate();
        }
        else {
            HibernateFactory.getSession()
            .getNamedQuery("ServerSnapshot.delete")
            .setParameter("org", org)
            .executeUpdate();
        }
    }

    /**
     * Delete snapshots associated with a server.
     *
     * A user may optionally provide a start and end date to narrow the snapshots that
     * will be removed.  For example,
     * - If user provides startDate only, all snapshots created either on or after the
     *   date provided will be removed
     * - If user provides startDate and endDate, all snapshots created on or between the
     *   dates provided will be removed
     * - If the user doesn't provide a startDate and endDate, all snapshots associated with
     *   the server will be removed.
     *
     * @param org the org doing the request. Required.
     * @param server the server to check for. Required.
     * @param startDate the start date.  Optional, unless endDate is provided.
     * @param endDate the end date. Optional.
     */
    public static void deleteSnapshots(Org org, Server server,
            Date startDate, Date endDate) {

        if ((startDate != null) && (endDate != null)) {
            HibernateFactory.getSession()
            .getNamedQuery("ServerSnapshot.deleteForServerBetweenDates")
            .setParameter("org", org)
            .setParameter("server", server)
            .setParameter("start_date", startDate)
            .setParameter("end_date", endDate)
            .executeUpdate();
        }
        else if (startDate != null) {
            HibernateFactory.getSession()
            .getNamedQuery("ServerSnapshot.deleteForServerAfterDate")
            .setParameter("org", org)
            .setParameter("server", server)
            .setParameter("start_date", startDate)
            .executeUpdate();
        }
        else {
            HibernateFactory.getSession()
            .getNamedQuery("ServerSnapshot.deleteForServer")
            .setParameter("org", org)
            .setParameter("server", server)
            .executeUpdate();
        }
    }

    /**
     * get tags for a given snapshot
     * @param snap the snapshot to get tags for
     * @return list of tags
     */
    public static List<SnapshotTag> getSnapshotTags(ServerSnapshot snap) {
        Map<String, Object> params = new HashMap<>();
        params.put("snap", snap);
        return SINGLETON.listObjectsByNamedQuery("ServerSnapshot.findTags", params);
    }

    /**
     * Filter out a list of systemIds with ones that are linux systems
     *  (i.e. not solaris systems)
     * @param systemIds list of system ids
     * @return list of system ids that are linux systems
     */
    public static List<Long> listLinuxSystems(Collection<Long> systemIds) {
        return SINGLETON.listObjectsByNamedQuery("Server.listRedHatSystems",
                new HashMap(), systemIds, "sids");
    }

    /**
     * Adds tag to the snapshot
     * @param snpId snapshot id
     * @param orgId org id
     * @param tagName name of the tag
     */
    public static void addTagToSnapshot(Long snpId, Long orgId, String tagName) {
        CallableMode m = ModeFactory.getCallableMode("System_queries",
                "add_tag_to_snapshot");
        Map inParams = new HashMap();
        inParams.put("snapshot_id", snpId);
        inParams.put("org_id", orgId);
        inParams.put("tag_name", tagName);
        m.execute(inParams, new HashMap());
    }

    /**
     * Adds tag to snapshot for systems in set
     * @param tagName Name of the tag
     * @param setLabel set to find systems in
     * @param user User making the request
     */
    public static void bulkAddTagToSnapshot(String tagName, String setLabel, User user) {
        CallableMode m = ModeFactory.getCallableMode("System_queries",
                "bulk_add_tag_to_snapshot");
        Map<String, Object> params = new HashMap<>();
        params.put("set_label", setLabel);
        params.put("org_id", user.getOrg().getId());
        params.put("tag_name", tagName);
        params.put("user_id", user.getId());
        m.execute(params, new HashMap<>());
    }

    /**
     * Removes tag from system snapshot
     * @param serverId server id
     * @param tag snapshot tag
     */
    public static void removeTagFromSnapshot(Long serverId, SnapshotTag tag) {
        CallableMode m = ModeFactory.getCallableMode("System_queries",
                "remove_tag_from_snapshot");
        Map inParams = new HashMap();
        inParams.put("server_id", serverId);
        inParams.put("tag_id", tag.getId());
        m.execute(inParams, new HashMap());
    }

    /**
     * Looks up snapshot tag by tag name
     * @param tagName name of the tag
     * @return snapshot tag
     */
    public static SnapshotTag lookupSnapshotTagbyName(String tagName) {
        return (SnapshotTag) HibernateFactory.getSession().getNamedQuery("SnapshotTag.lookupByTagName")
                .setString("tag_name", tagName)
                // Do not use setCacheable(true), as tag deletion will
                // usually end up making this query's output out of date
                .uniqueResult();
    }

    /**
     * @param tagId snapshot tag ID
     * @return snapshot Tag
     */
    public static SnapshotTag lookupSnapshotTagbyId(Long tagId) {
        return (SnapshotTag) HibernateFactory.getSession().getNamedQuery("SnapshotTag.lookupById")
                .setLong("id", tagId)
                // Do not use setCacheable(true), as tag deletion will
                // usually end up making this query's output out of date
                .uniqueResult();
    }

    /**
     * List all available contact methods.
     * @return available contact methods
     */
    public static List<ContactMethod> listContactMethods() {
        return SINGLETON.listObjectsByNamedQuery("ContactMethod.findAll", null, true);
    }

    /**
     * Find contact method type by given ID.
     * @param id id of the contact method
     * @return contact method
     */
    public static ContactMethod findContactMethodById(Long id) {
        Map params = new HashMap();
        params.put("id", id);
        return (ContactMethod) SINGLETON.lookupObjectByNamedQuery(
                "ContactMethod.findById", params, true);
    }

    /**
     * Find contact method type by a given label.
     * @param label label of the contact method
     * @return contact method with the given label
     */
    public static ContactMethod findContactMethodByLabel(String label) {
        Session session = getSession();
        Criteria criteria = session.createCriteria(ContactMethod.class);
        criteria.add(Restrictions.eq("label", label));
        return (ContactMethod) criteria.uniqueResult();
    }

    /**
     * @param fetchingVirtualGuests eagerly load virtual guests
     * @param fetchingGroups eagerly load server groups
     * @return a list of all systems
     */
    public static List<Server> list(boolean fetchingVirtualGuests, boolean fetchingGroups) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<Server> criteria = builder.createQuery(Server.class);
        Root r = criteria.from(Server.class);
        if (fetchingVirtualGuests) {
            r.fetch("virtualGuests", JoinType.LEFT);
        }
        if (fetchingGroups) {
            r.fetch("groups", JoinType.LEFT);
        }
        criteria.distinct(true);
        return getSession().createQuery(criteria).getResultList().stream()
                .collect(Collectors.toList());

    }

    /**
     * Get all SLES systems containing the name query string
     *
     * @param nameQuery the partial system name to query
     * @param limit the limit of the result stream
     * @param user the requesting user
     * @return a stream of SLES systems
     */
    public static Stream<Server> querySlesSystems(String nameQuery, int limit, User user) {
        return HibernateFactory.getSession().createQuery(
                "SELECT s FROM UserImpl user " +
                "JOIN user.servers s " +
                "WHERE user = :user " +
                "AND s.os = 'SLES' " +
                "AND s.name LIKE :nameQuery ", Server.class)
                .setParameter("user", user)
                .setParameter("nameQuery", '%' + StringUtils.defaultString(nameQuery) + '%')
                .setMaxResults(limit)
                .getResultStream();
    }

    /**
     * Get all kernel versions installed in a server
     *
     * @param server the server
     * @return the stream of EVRs of every installed kernel on the system
     */
    public static Stream<PackageEvr> getInstalledKernelVersions(Server server) {
        return HibernateFactory.getSession().createQuery(
                "SELECT DISTINCT pkg.evr FROM InstalledPackage pkg " +
                "WHERE pkg.name.name LIKE 'kernel-default%' " +
                "AND pkg.server = :server", PackageEvr.class)
                .setParameter("server", server)
                .getResultStream();
    }

    /**
     * List errata names for a given set of servers and errata.
     *
     * @param serverIds set of server ids
     * @param errataIds set of errata ids
     * @return map from server id to map from errata id to patch name
     */
    @SuppressWarnings("unchecked")
    public static Map<Long, Map<Long, Set<ErrataInfo>>> listErrataNamesForServers(
            Set<Long> serverIds, Set<Long> errataIds) {
        if (serverIds.isEmpty() || errataIds.isEmpty()) {
            return new HashMap<>();
        }
        Map<String, Object> params = new HashMap<>();
        params.put("serverIds", serverIds);
        params.put("errataIds", errataIds);
        List<Object[]> result = SINGLETON.listObjectsByNamedQuery(
                "Server.listErrataNamesForServers", params);
        return result.stream().collect(
            // Group by server id
            Collectors.groupingBy(row -> (Long) row[1],
                // Group by errata id
                Collectors.groupingBy(row -> (Long) row[0],
                    // Generate names including the update tag
                    Collectors.mapping(row -> {
                        String name = (String) row[2];
                        String tag = (String) row[3];
                        if (StringUtils.isBlank(tag)) {
                            return new ErrataInfo(name, (boolean)row[4], (boolean)row[5]);
                        }
                        if (name.matches("^([C-Z][A-Z]-)*SUSE-(.*)$")) {
                            return new ErrataInfo(name.replaceFirst("SUSE", "SUSE-" + tag),
                                    (boolean)row[4], (boolean)row[5]);
                        }
                        else {
                            return new ErrataInfo(tag + "-" + name, (boolean)row[4], (boolean)row[5]);
                        }
                    }, Collectors.toSet())
                )
            )
        );
    }

    /**
     * list the newest packages required by the given errata ids
     * for each of the given servers.
     *
     * @param serverIds set of server ids
     * @param errataIds set of errata ids
     * @return map from server id to map of package name to package version in evr format
     */
    @SuppressWarnings("unchecked")
    public static Map<Long, Map<String, Tuple2<String, String>>> listNewestPkgsForServerErrata(
            Set<Long> serverIds, Set<Long> errataIds) {
        if (serverIds.isEmpty() || errataIds.isEmpty()) {
            return new HashMap<>();
        }
        Map<String, Object> params = new HashMap<>();
        params.put("serverIds", serverIds);
        params.put("errataIds", errataIds);
        List<Object[]> result = SINGLETON.listObjectsByNamedQuery(
                "Server.listNewestPkgsForServerErrata", params);

        return result.stream().collect(
                // Group by server id
                Collectors.groupingBy(row -> (Long) row[0],
                        // Map from package name to version (requires package names
                        // to be unique which is guaranteed by the sql query
                        toMap(row -> (String)row[1], row -> new Tuple2<>((String)row[2], (String)row[3]))
                )
        );
    }

    /**
     * Save NetworkInterface
     * @param networkInterfaceIn the interface to save
     */
    public static void saveNetworkInterface(NetworkInterface networkInterfaceIn) {
        SINGLETON.saveObject(networkInterfaceIn);
    }


    /**
     * Delete a device
     * @param device the device to delete
     */
    public static void delete(Device device) {
        HibernateFactory.getSession().delete(device);
    }

    /**
     * Find a server by machine id.
     * @param machineId the machine id
     * @return the server if any
     */
    public static Optional<Server> findByMachineId(String machineId) {
        Session session = getSession();
        Criteria criteria = session.createCriteria(Server.class);
        criteria.add(Restrictions.eq("machineId", machineId));
        return Optional.ofNullable((Server) criteria.uniqueResult());
    }

    /**
     * Find {@link Capability} by name
     * @param name the name of the capability
     * @return a {@link Capability} with the given name
     */
    public static Optional<Capability> findCapability(String name) {
        Criteria criteria = getSession().createCriteria(Capability.class);
        criteria.add(Restrictions.eq("name", name));
        return Optional.ofNullable((Capability) criteria.uniqueResult());
    }

    /**
     * Find servers in the user's system set that are subscribed to the given channel.
     *
     * @param user user
     * @param channelId channel id
     * @return a list of Servers ids in the user's SSM subscribed to the
     * given channel
     */
    public static List<Long> findServersInSetByChannel(User user, long channelId) {
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("channel_id", channelId);
        return SINGLETON.listObjectsByNamedQuery(
                "Server.findServerInSSMByChannel", params);

    }

    /**
     * Finds the server ids given a list of minion ids.
     *
     * @param minionIds the list of minion ids
     * @return a map containing the minion id as key and the server id as value
     */
    public static Map<String, Long> findServerIdsByMinionIds(List<String> minionIds) {
        List<Object[]> results = findByIds(minionIds, "Server.findServerIdsByMinionIds", "minionIds");
        return results.stream().collect(toMap(row -> row[0].toString(), row -> (Long)row[1]));
    }

    /**
     * List all Systems with orgId.
     *
     * @param orgId The organization id
     * @return List of servers
     */
    public static List<Server> listOrgSystems(long orgId) {
        Map<String, Object> params = new HashMap<>();
        params.put("orgId", orgId);
        return SINGLETON.listObjectsByNamedQuery(
                "Server.listOrgSystems", params);
    }

    /**
     * Filter systems with pending maintenance-only actions
     *
     * @param systemIds the system IDs
     * @return only the IDs if systems with pending maintenance-only actions
     */
    public static Set<Long> filterSystemsWithPendingMaintOnlyActions(Set<Long> systemIds) {
        if (systemIds.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(HibernateFactory.getSession()
                .getNamedQuery("Server.filterSystemsWithPendingMaintenanceOnlyActions")
                .setParameter("systemIds", systemIds)
                .list());
    }

    /**
     * Assign the given {@link MaintenanceSchedule} to set of {@link Server}s
     *
     * @param schedule the {@link MaintenanceSchedule}
     * @param systemIds the set of {@link Server} IDs
     * @return number of involved {@link Server}s
     */
    public static int setMaintenanceScheduleToSystems(MaintenanceSchedule schedule, Set<Long> systemIds) {
        if (systemIds.isEmpty()) {
            return 0;
        }
        return HibernateFactory.getSession()
                .getNamedQuery("Server.setMaintenanceScheduleToSystems")
                .setParameter("schedule", schedule)
                .setParameter("systemIds", systemIds)
                .executeUpdate();
    }

    /**
     * Remove MgrServerInfo from minion
     *
     * @param minion the minion
     */
    public static void dropMgrServerInfo(MinionServer minion) {
        MgrServerInfo serverInfo = minion.getMgrServerInfo();
        if (serverInfo == null) {
            return;
        }
        Credentials credentials = serverInfo.getReportDbCredentials();
        CredentialsFactory.removeCredentials(credentials);
        SINGLETON.removeObject(serverInfo);
        minion.setMgrServerInfo(null);
    }
}
