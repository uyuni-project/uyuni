/*
 * Copyright (c) 2025 SUSE LLC
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
import com.redhat.rhn.common.db.datasource.Row;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.hibernate.HibernateRuntimeException;
import com.redhat.rhn.common.util.RpmVersionComparator;
import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.channel.ChannelArch;
import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.credentials.ReportDBCredentials;
import com.redhat.rhn.domain.dto.SystemIDInfo;
import com.redhat.rhn.domain.entitlement.Entitlement;
import com.redhat.rhn.domain.org.CustomDataKey;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.product.Tuple2;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.legacy.UserImpl;
import com.redhat.rhn.frontend.dto.HistoryEvent;
import com.redhat.rhn.frontend.dto.SystemOverview;
import com.redhat.rhn.frontend.xmlrpc.ChannelSubscriptionException;
import com.redhat.rhn.frontend.xmlrpc.ServerNotInGroupException;
import com.redhat.rhn.manager.audit.OsReleasePair;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.UpdateBaseChannelCommand;

import com.suse.manager.model.maintenance.MaintenanceSchedule;
import com.suse.manager.webui.services.pillar.MinionPillarManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.type.StandardBasicTypes;

import java.util.ArrayList;
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

import jakarta.persistence.Tuple;

/**
 * ServerFactory - the singleton class used to fetch and store
 * com.redhat.rhn.domain.server.Server objects from the database.
 */
public class ServerFactory extends HibernateFactory {

    private static final String SYSTEM_QUERIES = "System_queries";
    private static final Logger LOG = LogManager.getLogger(ServerFactory.class);

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
    public static CustomDataValue getCustomDataValue(CustomDataKey key,
            Server server) {
        // Make sure we didn't receive any nulls
        if (key == null || server == null) {
            return null;
        }

        return getSession().createQuery("FROM CustomDataValue AS c WHERE c.server = :server AND c.key = :key",
                        CustomDataValue.class)
                .setParameter("server", server)
                .setParameter("key", key)
                .setCacheable(true)
                .uniqueResult();
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
        Session session = HibernateFactory.getSession();
        return session.createQuery("FROM CustomDataValue AS c WHERE c.key = :key", CustomDataValue.class)
                .setParameter("key", key)
                .list();
    }

    /**
     * Lookup all MinionServers subscribed to the channel.
     * @param cid The channel id
     * @return List of minions.
     */
    public static List<MinionServer> listMinionsByChannel(long cid) {
        return getSession().createQuery("""
                select     ms
                from       com.redhat.rhn.domain.server.MinionServer as ms
                inner join ms.channels as c
                where      c.id = :cid
                """, MinionServer.class)
                .setParameter("cid", cid)
                .list();
    }

    /**
     * Lookup all the systems with the specified CustomDataKey.
     * @param userId The User ID of the user doing the query
     * @param cikid The ID of the Key for the values you would like to lookup
     * @return List of systems
     */
    public static List<Row> lookupServersWithCustomKey(Long userId, Long cikid) {
        SelectMode m = ModeFactory.getMode(SYSTEM_QUERIES, "users_systems_with_value_for_key");
        Map<String, Object> inParams = new HashMap<>();

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
        Session session = HibernateFactory.getSession();
        return session.createQuery(
                "FROM Device AS t WHERE t.server = :server AND t.deviceClass = 'HD' ORDER BY t.device", Device.class)
                .setParameter("server", s)
                .list();
    }

    /**
     * Looks up a proxy server by hostname.
     * @param name the hostname
     * @return the server, if it is found
     */
    public static Optional<Server> lookupProxyServer(String name) {
        boolean nameIsFullyQualified = name.contains(".");
        if (!nameIsFullyQualified) {
            LOG.warn("Specified master name \"{}\" is not fully-qualified,proxy attachment might not be correct", name);
            LOG.warn("Please use a FQDN in /etc/salt/minion.d/susemanager.conf");
        }

        Optional<Server> result = findByFqdn(name);

        if (result.isPresent() && result.get().isProxy()) {
            return result;
        }
        result = getSession().createQuery("""
                SELECT s
                FROM Server s
                JOIN s.proxyInfo pi
                WHERE s.hostname = :hostname
                """, Server.class)
                .setParameter("hostname", name)
                .setMaxResults(1)
                .uniqueResultOptional();

        if (result.isPresent()) {
            return result;
        }

        // precise search did not work, try imprecise
        if (nameIsFullyQualified) {
            String strippedHostname = name.split("\\.")[0];

            return getSession().createQuery("""
                SELECT s
                FROM Server s
                JOIN s.proxyInfo pi
                WHERE s.hostname = :hostname
                """, Server.class)
                    .setParameter("hostname", strippedHostname)
                    .setMaxResults(1)
                    .uniqueResultOptional();
        }
        else {
            return getSession().createQuery("""
                SELECT s
                FROM Server s
                JOIN s.proxyInfo pi
                WHERE s.hostname LIKE :hostname
                """, Server.class)
                    .setParameter("hostname", name + ".%")
                    .setMaxResults(1)
                    .uniqueResultOptional();
        }
    }

    /**
     * List the <b>unique</b> set of pairs of os and release versions used by servers
     *
     * @return the set of unique pairs of os and release version used by servers
     * */
    public static Set<OsReleasePair> listAllServersOsAndRelease() {

        return getSession().createNativeQuery("""
                SELECT DISTINCT s.os, s.release FROM rhnServer s
                """, Tuple.class)
                .addScalar("os", StandardBasicTypes.STRING)
                .addScalar("release", StandardBasicTypes.STRING)
                .stream()
                .map(t -> new OsReleasePair(t.get(0, String.class), t.get(1, String.class)))
                .collect(Collectors.toSet());
    }

    /**
     * Return a map from Salt minion IDs to System IDs.
     * Map entries are limited to systems that are visible by the specified user.
     *
     * @param id a user ID
     * @return the minion ID to system ID map
     */
    public static Map<String, Long> getMinionIdMap(Long id) {
        return getSession().createNativeQuery("""
                SELECT m.minion_id AS minion_id, s.id AS server_id
                FROM   rhnServer s
                JOIN   suseMinionInfo m ON s.id = m.server_id
                WHERE  EXISTS (SELECT 1
                               FROM   rhnUserServerPerms USP
                               WHERE  USP.user_id = :user_id
                               AND    USP.server_id = s.id)
                """, Tuple.class)
                .setParameter("user_id", id)
                .addScalar("minion_id", StandardBasicTypes.STRING)
                .addScalar("server_id", StandardBasicTypes.LONG)
                .stream()
                .collect(toMap(
                                t -> t.get(0, String.class),
                                t -> t.get(1, Long.class)));
    }

    /**
     * Regenerate the package needed cache for the server
     *
     * @param serverId the server ID
     */
    public static void updateServerNeededCache(long serverId) {
        CallableMode m = ModeFactory.getCallableMode(SYSTEM_QUERIES, "update_needed_cache");
        Map<String, Object> inParams = new HashMap<>();
        inParams.put("server_id", serverId);

        m.execute(inParams, new HashMap<>());
    }

    /**
     * Get the Logger for the derived class so log messages show up on the
     * correct class
     */
    @Override
    protected Logger getLogger() {
        return LOG;
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
                Server parentProxyServer = parentPath.getId().getProxyServer();
                ServerPath newPath = new ServerPath();
                newPath.setId(new ServerPathId(server, parentProxyServer));
                newPath.setHostname(parentPath.getHostname());
                return newPath;
            });
            path.setPosition(parentPath.getPosition() + 1);
            ServerFactory.save(path);
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
        ServerFactory.save(path);
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
        return getSession().createQuery("""
                FROM ServerPath sp
                WHERE sp.id.server = :server
                AND sp.id.proxyServer = :proxy
                """, ServerPath.class)
                .setParameter("server", server)
                .setParameter("proxy", proxyServer)
                .uniqueResultOptional();
    }

    /**
     * Adds Servers to a server group.
     * @param servers the servers to add
     * @param serverGroup The group to add the servers to
     */
    public static void addServersToGroup(Collection<Server> servers, ServerGroup serverGroup) {
        List<Long> serverIdsToAdd = servers.stream()
                .filter(s -> s.getOrgId().equals(serverGroup.getOrgId()))
                .map(Server::getId).toList();

        if (serverIdsToAdd.size() != servers.size()) {
            String incompatible = servers.stream()
                    .filter(s -> !s.getOrgId().equals(serverGroup.getOrgId()))
                    .map(Server::getName)
                    .collect(Collectors.joining(", "));

            LOG.error("Unable to set group '{}' for systems in different organization: {}",
                    serverGroup.getName(), incompatible);
        }
        boolean serversUpdated = insertServersToGroup(serverIdsToAdd, serverGroup.getId());

        if (serversUpdated) {
            servers.stream()
                    .filter(s -> s.getOrgId().equals(serverGroup.getOrgId()))
                    .forEach(s -> {
                        HibernateFactory.getSession().refresh(s);
                        s.addGroup(serverGroup);
                        SystemManager.updateSystemOverview(s);
                    });
            if (serverGroup.isManaged()) {
                updatePermissionsForServerGroup(serverGroup.getId());
            }
        }
    }

    private static boolean insertServersToGroup(List<Long> serverIds, Long sgid) {
        WriteMode m = ModeFactory.getWriteMode(SYSTEM_QUERIES, "add_servers_to_server_group");

        Map<String, Object> params = new HashMap<>();
        params.put("sgid", sgid);

        int insertsCount = m.executeUpdate(params, serverIds);

        if (insertsCount > 0) {
            updateCurrentMembersOfServerGroup(sgid, insertsCount);
            return true;
        }
        return false;
    }

    private static void updateCurrentMembersOfServerGroup(Long sgid, int membersCount) {
        WriteMode mode = ModeFactory.getWriteMode(SYSTEM_QUERIES, "update_current_members_of_server_group");

        Map<String, Object> params = new HashMap<>();
        params.put("sgid", sgid);
        params.put("members_count", membersCount);

        mode.executeUpdate(params);
    }

    private static void updatePermissionsForServerGroup(Long sgid) {
        CallableMode m = ModeFactory.getCallableMode(SYSTEM_QUERIES,
                "update_permissions_for_server_group");
        Map<String, Object> params = new HashMap<>();
        params.put("sgid", sgid);

        m.execute(params, new HashMap<>());
    }

    /**
     * Adds a Server to a group.
     * @param serverIn The server to add
     * @param serverGroupIn The group to add the server to
     */
    public static void addServerToGroup(Server serverIn, ServerGroup serverGroupIn) {
        addServersToGroup(Collections.singletonList(serverIn), serverGroupIn);
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

        WriteMode m = ModeFactory.getWriteMode(SYSTEM_QUERIES, "update_server_history_for_entitlement_event");
        m.executeUpdate(in);

        LOG.debug("update_server_history_for_entitlement_event mode query executed.");
    }

    /**
     * Removes Servers from a server group.
     * @param servers The servers to remove
     * @param serverGroup The group to remove the servers from
     */
    public static void removeServersFromGroup(Collection<Server> servers, ServerGroup serverGroup) {
        List<Long> serverIdsToAdd = servers.stream()
                .filter(s -> s.getOrgId().equals(serverGroup.getOrgId()))
                .map(Server::getId).toList();

        boolean serversUpdated = removeServersFromGroup(serverIdsToAdd, serverGroup.getId());

        if (serversUpdated) {
            servers.stream()
                    .filter(s -> s.getOrgId().equals(serverGroup.getOrgId()))
                    .forEach(s -> {
                        s.removeGroup(serverGroup);
                        SystemManager.updateSystemOverview(s);
                    });
            if (serverGroup.isManaged()) {
                updatePermissionsForServerGroup(serverGroup.getId());
            }
        }
        else {
            throw new ServerNotInGroupException();
        }
    }

    private static boolean removeServersFromGroup(List<Long> serverIds, Long sgid) {
        WriteMode m = ModeFactory.getWriteMode(SYSTEM_QUERIES, "delete_from_servergroup");

        Map<String, Object> params = new HashMap<>();
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
        removeServersFromGroup(Collections.singletonList(serverIn), serverGroupIn);
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
        if (serverIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return getSession().createNativeQuery("""
                SELECT SNPC.server_id as serverId, E.id as errataId
                FROM   rhnErrata E, rhnServerNeededErrataCache SNPC
                WHERE  EXISTS (SELECT 1
                               FROM   rhnUserServerPerms USP
                               WHERE  USP.user_id = :user_id
                               AND    USP.server_id = SNPC.server_id)
                AND    SNPC.server_id in (:serverIds)
                AND    SNPC.errata_id = E.id
                AND    NOT EXISTS (SELECT 1
                                   FROM   rhnActionErrataUpdate AEU, rhnServerAction SA, rhnActionStatus AST
                                   WHERE  SA.server_id = SNPC.server_id
                                   AND    SA.status = AST.id
                                   AND    AST.name IN('Queued', 'Picked Up')
                                   AND    AEU.action_id = SA.action_id
                                   AND    AEU.errata_id = E.id )
                ORDER BY E.id
                """, Tuple.class)
                .setParameterList("serverIds", serverIds)
                .setParameter("user_id", user.getId())
                .addScalar("serverId", StandardBasicTypes.LONG)
                .addScalar("errataId", StandardBasicTypes.LONG)
                .stream()
                .collect(
                    Collectors.groupingBy(
                            t -> t.get(0, Long.class),
                            Collectors.mapping(t -> t.get(1, Long.class), Collectors.toList())
                )
        );
    }

    /**
     * Look for servers that have a reboot action scheduled
     * @param systems the systems to check
     * @return list of servers pending reoot action
     */
    public static List<Long> findSystemsPendingRebootActions(List<SystemOverview> systems) {
        List<Long> sids = systems.stream().map(SystemOverview::getId).collect(Collectors.toList());
        return getSession().createQuery("""
                        SELECT distinct(sa.server.id)
                        FROM   ServerAction sa
                        WHERE  sa.server.id IN (:systemIds)
                        AND    sa.parentAction.actionType.label = 'reboot.reboot'
                        AND    status in ( 0, 1 )
                        """, Long.class)
                .setParameter("systemIds", sids)
                .list();
    }

    /**
     * Lookup a list of servers by their ids
     * @param serverIds the server ids to search for
     * @param org the organization who owns the server
     * @return the list of servers
     */
    public static List<Server> lookupByIdsAndOrg(Set<Long> serverIds, Org org) {
        if (serverIds.isEmpty()) {
            return Collections.emptyList();
        }
        return getSession()
                .createQuery("FROM Server AS s WHERE ORG_ID = :orgId AND s.id IN (:serverIds)", Server.class)
                .setParameter("orgId", org.getId())
                .setParameterList("serverIds", serverIds)
                .list();
    }

    /**
     * Retrieves the ids of the non-zypper traditional clients given a set of server ids
     * @param ids the server ids to search for
     * @return the list of non-zypper server ids
     */
    public static List<Long> findNonZypperTradClientsIds(Set<Long> ids) {
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        return getSession().createQuery("""
                SELECT s.id
                FROM   Server s
                WHERE  type(s) != com.redhat.rhn.domain.server.MinionServer
                AND    s.id in (:serverIds)
                AND    NOT EXISTS (SELECT 1
                                   FROM   com.redhat.rhn.domain.server.InstalledPackage as p
                                   JOIN   p.name as n
                                   JOIN   p.server as s2
                                   WHERE  s.id = s2.id
                                   AND    n.name = 'zypper')
                """, Long.class)
                .setParameterList("serverIds", ids)
                .list();
    }


    /**
     * Check if this server currently supports automated ptf uninstallation
     * @param server the server
     * @return <code>true</code> if this server support ptf uninstallation
     */
    public static boolean isPtfUninstallationSupported(Server server) {
        if (!server.doesOsSupportPtf()) {
            return false;
        }

        if (ServerConstants.SLES.equals(server.getOs())) {
            PackageEvr zypperEvr = getSession()
                    .createQuery("""
                            SELECT p.evr
                            FROM   com.redhat.rhn.domain.server.InstalledPackage as p
                            JOIN   p.name as n
                            JOIN   p.server as s
                            WHERE  s.id = :sid
                            AND    n.name = 'zypper'
                            """, PackageEvr.class)
                    .setParameter("sid", server.getId())
                    .uniqueResult();
            if (zypperEvr == null) {
                return false;
            }

            final RpmVersionComparator rpmVersionComparator = new RpmVersionComparator();
            if (server.isSLES15()) {
                return rpmVersionComparator.compare(zypperEvr.getVersion(), "1.14.59") >= 0;
            }
            else if (server.isSLES12()) {
                return rpmVersionComparator.compare(zypperEvr.getVersion(), "1.13.63") >= 0;
            }
            else if (server.isSLES16()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Lookup a Server by their id
     * @param id the id to search for
     * @param orgIn Org who owns the server
     * @return the Server found (null if not or not member if orgIn)
     */
    public static Server lookupByIdAndOrg(Long id, Org orgIn) {
        if (id == null || orgIn == null) {
            return null;
        }
        return getSession().createQuery("FROM Server AS s WHERE s.id = :sid AND ORG_ID = :orgId", Server.class)
                .setParameter("sid", id)
                .setParameter("orgId", orgIn.getId())
                .uniqueResult();
    }

    /**
     * Returns list of the ID information of all systems visible to user and are entitled with the passed entitlement.
     * @param user the logged in user.
     * @param entitlement the entitlement.
     *
     * @return list of system IDs.
     */
    public List<SystemIDInfo> lookupSystemsVisibleToUserWithEntitlement(User user, String entitlement) {
        SelectMode mode = ModeFactory.getMode(SYSTEM_QUERIES, "systems_visible_to_user_with_entitlement", Map.class);

        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("entitlement_label", entitlement);

        DataResult<Map<String, Object>> dr = mode.execute(params);

        return dr.stream().map(m -> new SystemIDInfo((Long) m.get("id"), (String) m.get("name")))
                .collect(Collectors.toList());
    }

    /**
     * Get Systems from SSM for fast channel subscriptions
     *
     * @param user the user to get systems for
     * @return the servers with only limited number of loaded fields
     */
    public static List<Server> getSsmSystemsForSubscribe(User user) {
        List<Tuple> res = getSession().createNativeQuery("""
                SELECT S.id, S.machine_id, SMI.minion_id, {CC.*}
                    FROM rhnServer S
                       LEFT OUTER JOIN suseMinionInfo SMI ON S.id = SMI.server_id
                       INNER JOIN rhnSet ST ON S.id = ST.element
                       LEFT OUTER JOIN rhnServerConfigChannel SCC ON S.id = SCC.server_id
                       LEFT OUTER JOIN rhnConfigChannel CC on CC.id = SCC.config_channel_id
                    WHERE
                       S.id = ST.element
                       AND ST.user_id = :user_id
                       AND ST.label = :system_set_label
                       AND EXISTS(SELECT 1 FROM rhnServerFeaturesView SFV WHERE SFV.server_id = ST.element
                       AND SFV.label = 'ftr_config')
                    ORDER BY S.name, SCC.position""", Tuple.class)
                .addScalar("id", StandardBasicTypes.LONG)
                .addScalar("machine_id", StandardBasicTypes.STRING)
                .addScalar("minion_id", StandardBasicTypes.STRING)
                .addEntity("CC", ConfigChannel.class)
                .setParameter("user_id", user.getId())
                .setParameter("system_set_label", RhnSetDecl.SYSTEMS.getLabel())
                .list();
        return getServersFromTuplesForSubscribe(res);
    }

    private static List<Server> getServersFromTuplesForSubscribe(List<Tuple> tuples) {
        // I know this is not conforming to the new functional programming style,
        // but at least I am sure we won't loop unnecessarily on thousands of rows
        List<Server> data = new ArrayList<>();
        Server current = null;
        List<ConfigChannel> channels = new ArrayList<>();

        for (Tuple tuple : tuples) {
            long sid = tuple.get(0, Long.class);
            if (current == null || current.getId() != sid) {
                if (current != null) {
                    current.setConfigChannels(channels);
                    data.add(current);
                    channels = new ArrayList<>();
                }
                String machineId = tuple.get(1, String.class);
                String minionId = tuple.get(2, String.class);
                if (minionId != null) {
                    current = new MinionServer(sid, machineId);
                }
                else {
                    current = new Server(sid, machineId);
                }
            }
            channels.add(tuple.get(3, ConfigChannel.class));
        }

        if (current != null) {
            current.setConfigChannels(channels);
            data.add(current);
        }

        return data;
    }

    /**
     * Get Systems for fast channel subscriptions
     *
     * @param sids the system ids to look for
     * @param user the user to get systems for
     * @return the servers with only limited number of loaded fields
     */
    public static List<Server> getSystemsForSubscribe(List<Long> sids, User user) {
        List<Tuple> res = getSession().createNativeQuery("""
                        SELECT S.id, S.machine_id, SMI.minion_id, {CC.*}
                                FROM rhnServer S
                                   LEFT OUTER JOIN suseMinionInfo SMI ON S.id = SMI.server_id
                                   LEFT OUTER JOIN rhnServerConfigChannel SCC ON S.id = SCC.server_id
                                   LEFT OUTER JOIN rhnConfigChannel CC on CC.id = SCC.config_channel_id
                                   JOIN rhnUserServerPerms USP ON (S.id = USP.server_id)
                                WHERE
                                   S.id IN (:sids)
                                   AND USP.user_id = :user_id
                                   AND EXISTS(SELECT 1 FROM rhnServerFeaturesView SFV WHERE SFV.server_id = S.id
                                   AND SFV.label = 'ftr_config')
                                ORDER BY S.name, SCC.position""", Tuple.class)
                .addScalar("id", StandardBasicTypes.LONG)
                .addScalar("machine_id", StandardBasicTypes.STRING)
                .addScalar("minion_id", StandardBasicTypes.STRING)
                .addEntity("CC", ConfigChannel.class)
                .setParameter("user_id", user.getId())
                .setParameterList("sids", sids)
                .list();

        return getServersFromTuplesForSubscribe(res);
    }

    /**
     * Lookup a Server by their id
     * @param id the id to search for
     * @return the Server found
     */
    public static Server lookupById(Long id) {
        return HibernateFactory.getSession().find(Server.class, id);
    }

    /**
     * lookup System with specified digital server id which are foreign_entitled
     *
     * @param id the digital server id
     * @return server corresponding to the given id
     */
    public static Server lookupForeignSystemByDigitalServerId(String id) {
        List<Server> servers = getSession().createQuery("FROM Server WHERE digitalServerId = :id", Server.class)
                .setParameter("id", id, StandardBasicTypes.STRING)
                .getResultList();
        for (Server server : servers) {
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
        return lookupByServerIds(Server.class, ids, """
                FROM com.redhat.rhn.domain.server.Server AS s WHERE s.id IN (:serverIds)
                """);
    }

    /**
     * Lookup Servers by their ids
     * @param <T> the type of the returned servers
     * @param resultClass the  result class
     * @param ids the ids to search for
     * @param sqlQuery the SQL query to be executed
     * @return the Servers found
     */
    public static <T extends Server> List<T> lookupByServerIds(Class<T> resultClass, List<Long> ids, String sqlQuery) {
        return findByIds(resultClass, ids, sqlQuery, "serverIds");
    }

    /**
     * Lookup a ServerGroupType by its label
     * @param label The label to search for
     * @return The ServerGroupType
     */
    public static ServerGroupType lookupServerGroupTypeByLabel(String label) {
        return getSession().createQuery("FROM ServerGroupType AS s WHERE s.label = :label", ServerGroupType.class)
                .setParameter("label", label, StandardBasicTypes.STRING)
                .setCacheable(true)
                .uniqueResult();
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
        CallableMode m = ModeFactory.getCallableMode(SYSTEM_QUERIES,
                "delete_server");
        Map<String, Object> in = new HashMap<>();
        in.put("server_id", server.getId());
        m.execute(in, new HashMap<>());
        HibernateFactory.getSession().clear();
    }

    private static void updateServerPerms(Server server) {
        CallableMode m = ModeFactory.getCallableMode(SYSTEM_QUERIES,
                "update_perms_for_server");
        Map<String, Object> inParams = new HashMap<>();
        inParams.put("sid", server.getId());
        m.execute(inParams, new HashMap<>());
    }

    /**
     * Lookup a ServerArch by its label
     * @param label The label to search for
     * @return The ServerArch
     */
    public static ServerArch lookupServerArchByLabel(String label) {
        return getSession().createQuery("FROM ServerArch AS s WHERE s.label = :label", ServerArch.class)
                .setParameter("label", label, StandardBasicTypes.STRING)
                .setCacheable(true)
                .uniqueResult();
    }

    /**
     * Lookup a ServerArch by its name
     * @param name The name to search for
     * @return The first ServerArch found
     */
    public static ServerArch lookupServerArchByName(String name) {
        Session session = HibernateFactory.getSession();
        List<ServerArch> archs = session.createQuery("FROM ServerArch AS s WHERE s.name = :name ORDER BY s.id ASC",
                        ServerArch.class)
                .setParameter("name", name)
                .list();
        if (archs != null && !archs.isEmpty()) {
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
        return getSession().createQuery("FROM CPUArch AS t WHERE LOWER(t.name) = LOWER(:name)", CPUArch.class)
                .setParameter("name", name, StandardBasicTypes.STRING)
                .setCacheable(true).uniqueResult();
    }

    /**
     * Returns a list of Servers which are compatible with the given server.
     * @param user User owner
     * @param server Server whose profiles we want.
     * @return a list of Servers which are compatible with the given server.
     */
    public static List<Row> compatibleWithServer(User user, Server server) {
        SelectMode m = ModeFactory.getMode(SYSTEM_QUERIES, "compatible_with_server");

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
        return getSession().createNativeQuery("""
                select     wc.*
                from       WEB_CONTACT wc
                inner join rhnUserServerPerms usp on wc.id = usp.user_id
                inner join rhnServer S on S.id = usp.server_id
                where      s.id = :sid
                and        s.org_id = :org_id
                """, UserImpl.class)
                .setParameter("sid", server.getId())
                .setParameter("org_id", server.getOrg().getId())
                .stream()
                .map(User.class::cast)
                .collect(Collectors.toList());
    }

    /**
     * For a {@link ServerArch}, find the compatible {@link ChannelArch}.
     * @param serverArch server arch
     * @return channel arch
     */
    public static ChannelArch findCompatibleChannelArch(ServerArch serverArch) {
        Session session = HibernateFactory.getSession();
        return session.createNativeQuery(
                        """
                            SELECT ca.* FROM rhnServerChannelArchCompat sc
                            JOIN rhnChannelArch ca ON sc.channel_arch_id = ca.id
                            WHERE sc.server_arch_id = :server_arch_id
                            """, ChannelArch.class)
                .setParameter("server_arch_id", serverArch.getId())
                //Retrieve from cache if there
                .setCacheable(true)
                .uniqueResult();
    }

    /**
     * gets a server's history sorted by creation time. This includes items from
     * the rhnServerHistory and rhnAction* tables
     * @param server the server who's history you want
     * @return A list of ServerHistoryEvents
     */
    public static List<HistoryEvent> getServerHistory(Server server) {

        SelectMode m = ModeFactory.getMode("Action_queries", "system_events_history");
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

        return getSession().createNativeQuery("""
                select     S.*,
                           mi.*,
                           case when mi.server_id is not null then 1 else 0 end as clazz_
                from       rhnServer S
                inner join rhnUserServerPerms USP on USP.server_id = S.id
                inner join rhnProxyInfo rpi on rpi.server_id = S.id
                left join  suseMinionInfo mi on mi.server_id = S.id
                where      S.org_id = :orgId
                and        USP.user_id = :userId
                """, Server.class)
                .setParameter("userId", user.getId())
                .setParameter("orgId", user.getOrg().getId())
                .list();
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
        return getSession().createNativeQuery("""
                select           S.*,
                                 case when ss.server_id is not null then 2
                                      when proxy.server_id is not null then 3
                                      when S.id is not null then 0 end as clazz_
                from             rhnServer S
                left outer join  suseMgrServerInfo SS on S.id = SS.server_id
                left outer join  rhnProxyInfo proxy on S.id = proxy.server_id
                inner join       rhnSet ST on S.id = st.element
                where            ST.user_id = :userId
                and              ST.label = :label
                and              S.id not in (SELECT server_id FROM rhnProxyInfo)
                """, Server.class)
                .setParameter("userId", user.getId())
                .setParameter("label", RhnSetDecl.SYSTEMS.getLabel())
                .list();
    }

    /**
     * Returns a global multi org spanning list of
     * servers that are config enabled.
     * Basically used by taskomatic.
     * @return a list of config enabled systems
     */
    public static List<Server> listConfigEnabledSystems() {
        return getSession().createQuery("""
                select     s
                from       com.redhat.rhn.domain.server.Server as s
                inner join s.groups as sg
                inner join sg.groupType.features as f
                inner join s.capabilities as c
                where      (sg.groupType is not null)
                and        f.label='ftr_config'
                and        c.id.capability.name like 'configfiles%'
                """, Server.class)
                .list();
    }

    /**
     * Returns a List of FQDNs associated to the system
     * @param sid the server id to check for. Required.
     * @return a list of FQDNs
     */
    public static List<String> listFqdns(Long sid) {
        return getSession().createQuery("""
                select name
                from   com.redhat.rhn.domain.server.ServerFQDN as fqdn
                where  fqdn.server.id = :sid
                """, String.class)
                .setParameter("sid", sid)
                .list();
    }

    /**
     * Find Server by a set of possible FQDNs
     * @param fqdns the set of FQDNs
     * @return return the first Server found if any
     */
    public static Optional<Server> findByAnyFqdn(Set<String> fqdns) {
        for (String fqdn : fqdns) {
            Optional<Server> server = findByFqdn(fqdn);
            if (server.isPresent()) {
                return server;
            }
        }
        return Optional.empty();
    }

    /**
     * Lookup a Server by their FQDN
     * @param name of the FQDN to search for
     * @return the Server found
     */
    public static Optional<Server> findByFqdn(String name) {
        Optional<List<Server>> servers = Optional.ofNullable(name)
                .map(ServerFactory::listByFqdn);
        List<Server> list = servers.orElse(new ArrayList<>());
        if (list.size() == 1) {
            return Optional.of(list.get(0));
        }
        else if (list.size() > 1) {
            throw new HibernateRuntimeException("Executing query findByFqdn" +
                    " with params " + name + " failed. NonUniqueResultException");
        }
        return Optional.empty();
    }

    /**
     * List a Servers by their FQDN
     * @param name of the FQDN to search for
     * @return the Servers found
     */
    public static List<Server> listByFqdn(String name) {
        return getSession().createQuery("""
                select s
                from   com.redhat.rhn.domain.server.Server as s
                join   s.fqdns as fqdn
                where  LOWER(fqdn.name) = LOWER(:name)
                """, Server.class)
                .setParameter("name", name)
                .list();
    }

    /**
     * Returns a global multi org spanning list of
     * servers that are config-diff enabled.
     * Basically used by taskomatic.
     * @return a list of config-diff enabled systems
     */
    public static List<Server> listConfigDiffEnabledSystems() {
        return getSession().createQuery("""
                select     s
                from       com.redhat.rhn.domain.server.Server as s
                inner join s.groups as sg
                inner join sg.groupType.features as f
                inner join s.capabilities as c
                where      (sg.groupType is not null)
                and        f.label='ftr_config'
                and        c.id.capability.name = 'configfiles.diff'
                order by   s.id asc
                """, Server.class)
                .list();
    }

    /**
     * List snapshots associated with a server.
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

        List<ServerSnapshot> snaps;

        Session session = HibernateFactory.getSession();

        if ((startDate != null) && (endDate != null)) {
            snaps = session.createQuery("""
                                    FROM ServerSnapshot AS s
                                    WHERE s.server = :server AND
                                    s.org = :org AND
                                    s.created >= :start_date AND
                                    s.created <= :end_date
                                    ORDER BY s.id DESC""",
                            ServerSnapshot.class)
                    .setParameter("server", server)
                    .setParameter("org", org)
                    .setParameter("start_date", startDate)
                    .setParameter("end_date", endDate)
                    .list();
        }
        else if (startDate != null) {
            snaps = session.createQuery("""
                                    FROM ServerSnapshot AS s
                                    WHERE s.server = :server AND
                                    s.org = :org AND
                                    s.created >= :start_date
                                    ORDER BY s.id DESC""",
                            ServerSnapshot.class)
                    .setParameter("server", server)
                    .setParameter("org", org)
                    .setParameter("start_date", startDate)
                    .list();
        }
        else {
            snaps = session.createQuery("""
                                    FROM ServerSnapshot AS s
                                    WHERE s.server = :server AND
                                    s.org = :org""",
                            ServerSnapshot.class)
                    .setParameter("server", server)
                    .setParameter("org", org)
                    .list();
        }
        return snaps;
    }

    /**
     * Looks up a server snapshot by it's id
     * @param id the snap id
     * @return the server snapshot
     */
    public static ServerSnapshot lookupSnapshotById(Integer id) {
        return SINGLETON.lookupObjectByParam(ServerSnapshot.class, "id", Long.valueOf(id));
    }

    /**
     * Looks up a latest snapshot for a sever
     * @param server server
     * @return the server snapshot
     */
    public static ServerSnapshot lookupLatestForServer(Server server) {
        Session session = HibernateFactory.getSession();
        return session.createQuery("""
                       FROM ServerSnapshot AS s
                       WHERE s.server = :sid AND
                       s.created = (SELECT max(s1.created) FROM ServerSnapshot AS s1 WHERE s1.server = :sid)
                       """, ServerSnapshot.class)
                .setParameter("sid", server)
                .uniqueResult();
    }

    /**
     * Delete a snapshot
     * @param snap the snapshot to delete
     */
    public static void deleteSnapshot(ServerSnapshot snap) {
        getSession().remove(snap);
    }

    /**
     * Delete snapshots across servers in the org.
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
            getSession().createQuery("""
                            DELETE FROM ServerSnapshot AS s
                            WHERE s.org = :org AND
                            s.created >= :start_date AND
                            s.created <= :end_date""")
                    .setParameter("org", org)
                    .setParameter("start_date", startDate)
                    .setParameter("end_date", endDate)
                    .executeUpdate();
        }
        else if (startDate != null) {
            getSession().createQuery("""
                            DELETE FROM ServerSnapshot AS s
                            WHERE s.org = :org AND
                            s.created >= :start_date""")
                    .setParameter("org", org)
                    .setParameter("start_date", startDate)
                    .executeUpdate();
        }
        else {
            getSession().createQuery("""
                            DELETE FROM ServerSnapshot AS s
                            WHERE s.org = :org""")
                    .setParameter("org", org)
                    .executeUpdate();
        }
    }

    /**
     * Delete snapshots associated with a server.
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
            getSession().createQuery("""
                            DELETE FROM ServerSnapshot AS s
                            WHERE s.server = :server AND
                            s.org = :org AND
                            s.created >= :start_date AND
                            s.created <= :end_date""")
                    .setParameter("server", server)
                    .setParameter("org", org)
                    .setParameter("start_date", startDate)
                    .setParameter("end_date", endDate)
                    .executeUpdate();
        }
        else if (startDate != null) {
            getSession().createQuery("""
                            DELETE FROM ServerSnapshot AS s
                            WHERE s.server = :server AND
                            s.org = :org AND
                            s.created >= :start_date""")
                    .setParameter("server", server)
                    .setParameter("org", org)
                    .setParameter("start_date", startDate)
                    .executeUpdate();
        }
        else {
            getSession().createQuery("""
                            DELETE FROM ServerSnapshot AS s
                            WHERE s.server = :server AND
                            s.org = :org""")
                    .setParameter("server", server)
                    .setParameter("org", org)
                    .executeUpdate();
        }
    }

    /**
     * get tags for a given snapshot
     * @param snap the snapshot to get tags for
     * @return list of tags
     */
    public static List<SnapshotTag> getSnapshotTags(ServerSnapshot snap) {
        Session session = HibernateFactory.getSession();
        return session.createQuery("SELECT l.tag FROM ServerSnapshotTagLink AS l WHERE l.snapshot = :snap",
                        SnapshotTag.class)
                .setParameter("snap", snap)
                .list();
    }

    /**
     * Filter out a list of systemIds with ones that are linux systems
     *  (i.e. not solaris systems)
     * @param systemIds list of system ids
     * @return list of system ids that are linux systems
     */
    public static List<Long> listLinuxSystems(Collection<Long> systemIds) {
        if (systemIds.isEmpty()) {
            return Collections.emptyList();
        }
        return getSession().createQuery("""
                SELECT s.id
                FROM   com.redhat.rhn.domain.server.Server AS s
                WHERE  s.id IN (:sids)
                AND    s.serverArch.archType.label != 'sysv-solaris'
                """, Long.class)
                .setParameterList("sids", systemIds)
                .list();
    }

    /**
     * Adds tag to the snapshot
     * @param snpId snapshot id
     * @param orgId org id
     * @param tagName name of the tag
     */
    public static void addTagToSnapshot(Long snpId, Long orgId, String tagName) {
        CallableMode m = ModeFactory.getCallableMode(SYSTEM_QUERIES, "add_tag_to_snapshot");
        Map<String, Object> inParams = new HashMap<>();
        inParams.put("snapshot_id", snpId);
        inParams.put("org_id", orgId);
        inParams.put("tag_name", tagName);
        m.execute(inParams, new HashMap<>());
    }

    /**
     * Adds tag to snapshot for systems in set
     * @param tagName Name of the tag
     * @param setLabel set to find systems in
     * @param user User making the request
     */
    public static void bulkAddTagToSnapshot(String tagName, String setLabel, User user) {
        CallableMode m = ModeFactory.getCallableMode(SYSTEM_QUERIES, "bulk_add_tag_to_snapshot");
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
        CallableMode m = ModeFactory.getCallableMode(SYSTEM_QUERIES, "remove_tag_from_snapshot");
        Map<String, Object> inParams = new HashMap<>();
        inParams.put("server_id", serverId);
        inParams.put("tag_id", tag.getId());
        m.execute(inParams, new HashMap<>());
    }

    /**
     * Looks up snapshot tag by tag name
     * @param tagName name of the tag
     * @return snapshot tag
     */
    public static SnapshotTag lookupSnapshotTagbyName(String tagName) {
        return getSession().createQuery("FROM SnapshotTag AS st WHERE st.name.name = :tag_name", SnapshotTag.class)
                .setParameter("tag_name", tagName, StandardBasicTypes.STRING)
                // Do not use setCacheable(true), as tag deletion will
                // usually end up making this query's output out of date
                .uniqueResult();
    }

    /**
     * @param tagId snapshot tag ID
     * @return snapshot Tag
     */
    public static SnapshotTag lookupSnapshotTagbyId(Long tagId) {
        return getSession().createQuery("FROM SnapshotTag AS st WHERE st.id = :id", SnapshotTag.class)
                .setParameter("id", tagId, StandardBasicTypes.LONG)
                // Do not use setCacheable(true), as tag deletion will
                // usually end up making this query's output out of date
                .uniqueResult();
    }

    /**
     * List all available contact methods.
     * @return available contact methods
     */
    public static List<ContactMethod> listContactMethods() {
        return getSession().createQuery("FROM ContactMethod AS cm ORDER BY cm.id", ContactMethod.class)
                //Retrieve from cache if there
                .setCacheable(true)
                .list();
    }

    /**
     * Find contact method type by given ID.
     * @param id id of the contact method
     * @return contact method
     */
    public static ContactMethod findContactMethodById(Long id) {
        return getSession().createQuery("FROM ContactMethod AS cm WHERE cm.id = :id", ContactMethod.class)
                .setParameter("id", id)
                //Retrieve from cache if there
                .setCacheable(true)
                .uniqueResult();
    }

    /**
     * Find contact method type by a given label.
     * @param label label of the contact method
     * @return contact method with the given label
     */
    public static ContactMethod findContactMethodByLabel(String label) {
        return getSession().createQuery("FROM ContactMethod WHERE label = :label", ContactMethod.class)
                .setParameter("label", label, StandardBasicTypes.STRING)
                .uniqueResult();
    }

    /**
     * @return a list of all systems
     */
    public static List<Server> list() {
        return getSession().createQuery("FROM Server", Server.class).getResultList();

    }

    /**
     * Returns a list of ids of all the servers.
     *
     * @return the list of all the ids of the servers
     */
    public static List<Long> listAllServerIds() {
        return getSession().createQuery("""
                SELECT   s.id
                FROM     com.redhat.rhn.domain.server.Server AS s
                ORDER BY s.id
                """, Long.class).list();
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
        return getSession().createQuery("""
                SELECT s FROM UserImpl user
                JOIN user.servers s
                WHERE user = :user
                AND s.os = 'SLES'
                AND s.name LIKE :nameQuery""", Server.class)
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
        return getSession().createQuery("""
                SELECT DISTINCT pkg.evr FROM InstalledPackage pkg
                WHERE pkg.name.name LIKE 'kernel-default%'
                AND pkg.server = :server""", PackageEvr.class)
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
    public static Map<Long, Map<Long, Set<ErrataInfo>>> listErrataNamesForServers(
            Set<Long> serverIds, Set<Long> errataIds) {
        if (serverIds.isEmpty() || errataIds.isEmpty()) {
            return new HashMap<>();
        }
        List<Tuple> result = getSession().createNativeQuery("""
                SELECT DISTINCT e.id AS errataId,
                                sc.server_id AS serverId,
                                e.advisory_name AS advisoryName,
                                c.update_tag AS updateTag,
                                CASE WHEN (SELECT COUNT(*)
                                          FROM   rhnErrataKeyword k
                                          WHERE  e.id = k.errata_id
                                          AND k.keyword = 'restart_suggested') > 0
                                     THEN TRUE
                                     ELSE FALSE
                                END AS updateStack,
                                CASE WHEN (SELECT COUNT(*)
                                           FROM   rhnErrataPackage k
                                           WHERE  e.id = k.errata_id
                                           AND k.package_id IN (SELECT id
                                                                FROM   rhnPackage p
                                                                WHERE p.name_id IN (SELECT id
                                                                                    FROM   rhnPackageName pn
                                                                                    WHERE  pn.name = 'salt'
                                                                                    OR     pn.name = 'venv-salt-minion')
                                                                )
                                           ) > 0
                                     THEN TRUE
                                     ELSE FALSE
                                END AS includeSalt
                FROM            rhnErrata e
                JOIN            rhnChannelErrata ce ON e.id = ce.errata_id
                JOIN            rhnChannel c ON ce.channel_id = c.id
                JOIN            rhnServerChannel sc ON c.id = sc.channel_id
                WHERE           e.id in (:errataIds)
                AND             sc.server_id in (:serverIds)
                """, Tuple.class)
                .setParameterList("serverIds", serverIds)
                .setParameterList("errataIds", errataIds)
                .addScalar("errataId", StandardBasicTypes.LONG)
                .addScalar("serverId", StandardBasicTypes.LONG)
                .addScalar("advisoryName", StandardBasicTypes.STRING)
                .addScalar("updateTag", StandardBasicTypes.STRING)
                .addScalar("updateStack", StandardBasicTypes.BOOLEAN)
                .addScalar("includeSalt", StandardBasicTypes.BOOLEAN)
                .list();

        return result.stream().collect(
            // Group by server id
            Collectors.groupingBy(t -> t.get(1, Long.class),
                // Group by errata id
                Collectors.groupingBy(t -> t.get(0, Long.class),
                    // Generate names including the update tag
                    Collectors.mapping(t -> {
                        String name = t.get(2, String.class);
                        String tag = t.get(3, String.class);
                        if (StringUtils.isBlank(tag)) {
                            return new ErrataInfo(name, t.get(4, Boolean.class), t.get(5, Boolean.class));
                        }
                        if (name.matches("^([C-Z][A-Z]-)*SUSE-(.*)$")) {
                            return new ErrataInfo(name.replaceFirst("SUSE", "SUSE-" + tag),
                                    t.get(4, Boolean.class), t.get(5, Boolean.class));
                        }
                        else {
                            return new ErrataInfo(tag + "-" + name, t.get(4, Boolean.class), t.get(5, Boolean.class));
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
    public static Map<Long, Map<String, Tuple2<String, String>>> listNewestPkgsForServerErrata(
            Set<Long> serverIds, Set<Long> errataIds) {
        if (serverIds.isEmpty() || errataIds.isEmpty()) {
            return new HashMap<>();
        }
        return getSession().createNativeQuery("""
                WITH NewPackageForServerErrata as (
                 SELECT DISTINCT sc.server_id, pn.name, max(pevr.evr) evr, pa.label as package_arch
                   FROM rhnErrata e
                            INNER JOIN rhnErrataPackage ep ON e.id = ep.errata_id
                            INNER JOIN rhnPackage p ON ep.package_id = p.id
                            INNER JOIN rhnPackageEVR pevr ON p.evr_id = pevr.id
                            INNER JOIN rhnPackageName pn ON p.name_id = pn.id
                            INNER JOIN rhnPackageArch pa ON p.package_arch_id = pa.id

                            INNER JOIN rhnChannelErrata ce ON e.id = ce.errata_id
                            INNER JOIN rhnServerChannel sc ON (sc.channel_id = ce.channel_id
                                                           AND sc.server_id in (:serverIds))
                            INNER JOIN rhnChannelPackage pc ON (pc.channel_id = sc.channel_id AND pc.package_id = p.id)

                            INNER JOIN rhnServerPackage sp ON (sp.server_id = sc.server_id
                                                           AND p.name_id = sp.name_id
                                                           AND p.package_arch_id = sp.package_arch_id)
                            INNER JOIN rhnPackageEVR spevr ON (sp.evr_id = spevr.id
                                                           AND (pevr.evr).type = (spevr.evr).type
                                                           AND pevr.evr > spevr.evr)
                          WHERE e.id in (:errataIds)
                       GROUP BY sc.server_id, pn.name, pa.label
                )
                SELECT pse.server_id AS serverId
                     , pse.name AS packageName
                     , pse.package_arch AS packageArch
                     , CASE WHEN (pse.evr).epoch IS NULL
                         THEN (pse.evr).version || '-' || (pse.evr).release
                         ELSE (pse.evr).epoch || ':' || (pse.evr).version || '-' || (pse.evr).release
                       END AS packageVersion
                FROM NewPackageForServerErrata pse
                """, Tuple.class)
                .setParameterList("serverIds", serverIds)
                .setParameterList("errataIds", errataIds)
                .addScalar("serverId", StandardBasicTypes.LONG)
                .addScalar("packageName", StandardBasicTypes.STRING)
                .addScalar("packageArch", StandardBasicTypes.STRING)
                .addScalar("packageVersion", StandardBasicTypes.STRING)
                .stream()
                .collect(
                        // Group by server id
                        Collectors.groupingBy(t -> t.get(0, Long.class),
                                // Map from package name to version (requires package names
                                // to be unique which is guaranteed by the sql query
                                toMap(t -> t.get(1, String.class),
                                        t -> new Tuple2<>(t.get(2, String.class), t.get(3, String.class)))
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
        getSession().remove(device);
    }

    /**
     * Find a server by machine id.
     * @param machineId the machine id
     * @return the server if any
     */
    public static Optional<Server> findByMachineId(String machineId) {
        return getSession().createQuery("""
                FROM Server WHERE machineId = :machineId
                """, Server.class)
                .setParameter("machineId", machineId, StandardBasicTypes.STRING)
                .uniqueResultOptional();
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
        return getSession().createNativeQuery("""
                SELECT s.id AS serverId
                FROM   rhnServer s
                JOIN   rhnServerChannel sc ON s.id=sc.server_id AND sc.channel_id=:channel_id
                JOIN   rhnSet rset ON rset.element = s.id AND rset.user_id = :user_id AND rset.label = 'system_list'
                """, Tuple.class)
                .setParameter("user_id", user.getId())
                .setParameter("channel_id", channelId)
                .addScalar("serverId", StandardBasicTypes.LONG)
                .stream()
                .map(t -> t.get(0, Long.class))
                .collect(Collectors.toList());
    }

    /**
     * Finds the server ids given a list of minion ids.
     *
     * @param minionIds the list of minion ids
     * @return a map containing the minion id as key and the server id as value
     */
    public static Map<String, Long> findServerIdsByMinionIds(List<String> minionIds) {
        if (minionIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return getSession().createQuery("""
                        SELECT s.minionId, s.id
                        FROM   Server AS s
                        WHERE  type(s) = com.redhat.rhn.domain.server.MinionServer
                        AND    s.minionId IN (:minionIds)
                        """, Tuple.class)
                .setParameterList("minionIds", minionIds)
                .stream()
                .collect(toMap(
                        t -> t.get(0, String.class),
                        t -> t.get(1, Long.class)
                ));
    }

    /**
     * List all Systems with orgId.
     *
     * @param orgId The organization id
     * @return List of servers
     */
    public static List<Server> listOrgSystems(long orgId) {
        return getSession()
                .createQuery("FROM com.redhat.rhn.domain.server.Server AS s WHERE ORG_ID = :orgId", Server.class)
                .setParameter("orgId", orgId)
                .list();
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
        return getSession().createNativeQuery(
                """
                        SELECT sa.* FROM rhnServerAction sa
                        JOIN rhnAction a ON sa.action_id = a.id JOIN rhnActionType at ON a.action_type = at.id
                        WHERE sa.server_id IN (:systemIds) AND at.maintenance_mode_only = 'Y'
                        AND sa.status IN (0, 1)
                    """, ServerAction.class
                )
                .addSynchronizedEntityClass(Action.class)
                .setParameterList("systemIds", systemIds, StandardBasicTypes.LONG)
                .getResultList().stream().map(ServerAction::getServerId).collect(Collectors.toSet());
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
        return getSession().createQuery("""
                UPDATE Server s
                SET    s.maintenanceSchedule = :schedule
                WHERE  s.id IN (:systemIds)
                """)
                .setParameter("schedule", schedule)
                .setParameter("systemIds", systemIds)
                .executeUpdate();
    }

    /**
     * Remove MgrServerInfo from minion
     *
     * @param server the minion
     */
    public static void dropMgrServerInfo(Server server) {
        MgrServerInfo serverInfo = server.getMgrServerInfo();
        if (serverInfo == null) {
            return;
        }
        ReportDBCredentials credentials = serverInfo.getReportDbCredentials();
        if (credentials != null) {
            CredentialsFactory.removeCredentials(credentials);
        }
        SINGLETON.removeObject(serverInfo);
        server.setMgrServerInfo(null);
    }
}
