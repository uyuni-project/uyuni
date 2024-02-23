/*
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
package com.redhat.rhn.manager.system;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;

import com.redhat.rhn.common.RhnRuntimeException;
import com.redhat.rhn.common.client.ClientCertificate;
import com.redhat.rhn.common.client.InvalidCertificateException;
import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.db.datasource.CallableMode;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.common.validator.ValidatorResult;
import com.redhat.rhn.common.validator.ValidatorWarning;
import com.redhat.rhn.domain.channel.AccessTokenFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.credentials.ReportDBCredentials;
import com.redhat.rhn.domain.dto.SystemGroupID;
import com.redhat.rhn.domain.dto.SystemGroupsDTO;
import com.redhat.rhn.domain.dto.SystemIDInfo;
import com.redhat.rhn.domain.entitlement.Entitlement;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.formula.FormulaFactory;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.rhnpackage.PackageEvrFactory;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.rhnpackage.PackageName;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.CPU;
import com.redhat.rhn.domain.server.MgrServerInfo;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.NetworkInterface;
import com.redhat.rhn.domain.server.Note;
import com.redhat.rhn.domain.server.ProxyInfo;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerConstants;
import com.redhat.rhn.domain.server.ServerFQDN;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.server.ServerHistoryEvent;
import com.redhat.rhn.domain.server.ServerLock;
import com.redhat.rhn.domain.server.VirtualInstance;
import com.redhat.rhn.domain.server.VirtualInstanceFactory;
import com.redhat.rhn.domain.server.VirtualInstanceState;
import com.redhat.rhn.domain.state.PackageState;
import com.redhat.rhn.domain.state.PackageStates;
import com.redhat.rhn.domain.state.ServerStateRevision;
import com.redhat.rhn.domain.state.StateFactory;
import com.redhat.rhn.domain.state.VersionConstraints;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.ActivationKeyDto;
import com.redhat.rhn.frontend.dto.BootstrapSystemOverview;
import com.redhat.rhn.frontend.dto.CustomDataKeyOverview;
import com.redhat.rhn.frontend.dto.EmptySystemProfileOverview;
import com.redhat.rhn.frontend.dto.ErrataOverview;
import com.redhat.rhn.frontend.dto.EssentialServerDto;
import com.redhat.rhn.frontend.dto.HardwareDeviceDto;
import com.redhat.rhn.frontend.dto.NetworkDto;
import com.redhat.rhn.frontend.dto.OrgProxyServer;
import com.redhat.rhn.frontend.dto.PackageListItem;
import com.redhat.rhn.frontend.dto.ServerPath;
import com.redhat.rhn.frontend.dto.ShortSystemInfo;
import com.redhat.rhn.frontend.dto.SnapshotTagDto;
import com.redhat.rhn.frontend.dto.SystemCurrency;
import com.redhat.rhn.frontend.dto.SystemEventDto;
import com.redhat.rhn.frontend.dto.SystemGroupOverview;
import com.redhat.rhn.frontend.dto.SystemOverview;
import com.redhat.rhn.frontend.dto.SystemPendingEventDto;
import com.redhat.rhn.frontend.dto.SystemScheduleDto;
import com.redhat.rhn.frontend.dto.VirtualSystemOverview;
import com.redhat.rhn.frontend.dto.kickstart.KickstartSessionDto;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.frontend.xmlrpc.InvalidProxyVersionException;
import com.redhat.rhn.frontend.xmlrpc.ProxySystemIsSatelliteException;
import com.redhat.rhn.manager.BaseManager;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.formula.FormulaMonitoringManager;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerSystemRemoveCommand;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.manager.user.UserManager;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.model.maintenance.MaintenanceSchedule;
import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.manager.reactor.messaging.ChannelsChangedEventMessage;
import com.suse.manager.reactor.utils.ValueMap;
import com.suse.manager.ssl.SSLCertData;
import com.suse.manager.ssl.SSLCertGenerationException;
import com.suse.manager.ssl.SSLCertManager;
import com.suse.manager.ssl.SSLCertPair;
import com.suse.manager.utils.PagedSqlQueryBuilder;
import com.suse.manager.virtualization.VirtManagerSalt;
import com.suse.manager.webui.controllers.StatesAPI;
import com.suse.manager.webui.services.SaltStateGeneratorService;
import com.suse.manager.webui.services.StateRevisionService;
import com.suse.manager.webui.services.iface.MonitoringManager;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.VirtManager;
import com.suse.manager.webui.services.impl.SaltSSHService;
import com.suse.manager.webui.services.impl.runner.MgrUtilRunner;
import com.suse.manager.webui.utils.YamlHelper;
import com.suse.manager.xmlrpc.dto.SystemEventDetailsDto;
import com.suse.utils.Opt;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.IDN;
import java.security.SecureRandom;
import java.sql.Date;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * SystemManager
 */
public class SystemManager extends BaseManager {

    private static Logger log = LogManager.getLogger(SystemManager.class);

    public static final String CAP_CONFIGFILES_UPLOAD = "configfiles.upload";
    public static final String CAP_CONFIGFILES_DIFF = "configfiles.diff";
    public static final String CAP_CONFIGFILES_MTIME_UPLOAD =
            "configfiles.mtime_upload";
    public static final String CAP_CONFIGFILES_DEPLOY = "configfiles.deploy";
    public static final String CAP_PACKAGES_VERIFY = "packages.verify";
    public static final String CAP_CONFIGFILES_BASE64_ENC =
            "configfiles.base64_enc";
    public static final String CAP_SCRIPT_RUN = "script.run";
    public static final String CAP_SCAP = "scap.xccdf_eval";

    private final SystemEntitlementManager systemEntitlementManager;
    private SaltApi saltApi;
    private ServerFactory serverFactory;
    private ServerGroupFactory serverGroupFactory;

    /**
     * Instantiates a new system manager.
     *
     * @param serverFactoryIn the server factory in
     * @param serverGroupFactoryIn the server group factory in
     * @param saltApiIn the Salt API
     */
    public SystemManager(ServerFactory serverFactoryIn, ServerGroupFactory serverGroupFactoryIn, SaltApi saltApiIn) {
        super();
        this.serverFactory = serverFactoryIn;
        this.serverGroupFactory = serverGroupFactoryIn;
        this.saltApi = saltApiIn;
        ServerGroupManager serverGroupManager = new ServerGroupManager(saltApiIn);
        VirtManager virtManager = new VirtManagerSalt(saltApi);
        MonitoringManager monitoringManager = new FormulaMonitoringManager(saltApi);
        systemEntitlementManager = new SystemEntitlementManager(
                new SystemUnentitler(virtManager, monitoringManager, serverGroupManager),
                new SystemEntitler(saltApiIn, virtManager, monitoringManager, serverGroupManager)
        );
    }

    /**
     * Retrieves the groups information a system is member of, for all the systems
     * visible to the passed user and that are entitled with the passed entitlement
     *
     * @param user the user
     * @param entitlement the entitlement
     * @return the list of SystemGroupsInfo
     */
    public List<SystemGroupsDTO> retrieveSystemGroupsForSystemsWithEntitlementAndUser(User user, String entitlement) {
        List<SystemIDInfo> systemIDInfos =
                this.serverFactory.lookupSystemsVisibleToUserWithEntitlement(user, entitlement);

        List<Long> systemIDs = systemIDInfos.stream().map(SystemIDInfo::getSystemID).collect(Collectors.toList());

        Map<Long, List<SystemGroupID>> managedGroupsPerServer =
                this.serverGroupFactory.lookupManagedSystemGroupsForSystems(systemIDs);

        return systemIDInfos.stream()
                .map(s -> new SystemGroupsDTO(s.getSystemID(),
                        managedGroupsPerServer.getOrDefault(s.getSystemID(), new ArrayList<>())))
                .collect(Collectors.toList());
    }

    /**
     * Takes a snapshot for servers by calling the snapshot_server stored procedure.
     * @param servers The servers to snapshot
     * @param reason The reason for the snapshotting.
     */
    public static void snapshotServers(Collection<Server> servers, String reason) {
        if (!Config.get().getBoolean(ConfigDefaults.TAKE_SNAPSHOTS)) {
            return;
        }
        List<Long> serverIds = servers.stream().map(Server::getId).collect(Collectors.toList());
        List<Long> snapshottableServerIds = filterServerIdsWithFeature(serverIds, "ftr_snapshotting");

        // If the server is null or doesn't have the snapshotting feature, don't bother.
        for (Long serverId : snapshottableServerIds) {
            CallableMode m = ModeFactory.getCallableMode("System_queries", "snapshot_server");
            Map<String, Object> in = new HashMap<>();
            in.put("server_id", serverId);
            in.put("reason", reason);
            m.execute(in, new HashMap<>());
        }
    }

    /**
     * Takes a snapshot for a server by calling the snapshot_server stored procedure.
     * @param server The server to snapshot
     * @param reason The reason for the snapshotting.
     */
    public static void snapshotServer(Server server, String reason) {
        snapshotServers(Arrays.asList(server), reason);
    }

    /**
     * Gets the list of channels that this server could subscribe to given it's base
     * channel.
     * @param sid The id of the server in question
     * @param uid The id of the user asking
     * @param cid The id of the base channel for the server
     * @return Returns a list of subscribable (child) channels for this server.
     */
    public static DataResult<Map<String, Object>> subscribableChannels(Long sid, Long uid,
            Long cid) {
        SelectMode m = ModeFactory.getMode("Channel_queries",
                "subscribable_channels", Map.class);
        Map<String, Object> params = new HashMap<>();
        params.put("server_id", sid);
        params.put("user_id", uid);
        params.put("base_channel_id", cid);

        return m.execute(params);
    }

    /**
     * Gets the list of channel ids that this server could subscribe to
     * according to it's base channel.
     * @param sid The id of the server in question
     * @param uid The id of the user asking
     * @param cid The id of the base channel for the server
     * @return Returns a list of subscribable (child) channel ids for this server.
     */
    public static Set<Long> subscribableChannelIds(Long sid, Long uid, Long cid) {
        Iterator<Map<String, Object>> subscribableChannelIter =
                subscribableChannels(sid, uid, cid).iterator();

        Set<Long> subscribableChannelIdSet = new HashSet<>();
        while (subscribableChannelIter.hasNext()) {
            Map<String, Object> row = subscribableChannelIter.next();
            subscribableChannelIdSet.add((Long) row.get("id"));
        }
        return subscribableChannelIdSet;
    }

    /**
     * Gets the list of channels that this server is subscribed to
     * @param sid The id of the server in question
     * @return Returns a list of subscribed channels for this server.
     */
    public static DataResult<Map<String, Object>> systemChannelSubscriptions(Long sid) {
        SelectMode m = ModeFactory.getMode("System_queries",
                "system_channel_subscriptions");
        Map<String, Object> params = new HashMap<>();
        params.put("sid", sid);
        return m.execute(params);
    }

    /**
     * @param user
     *            Currently logged in user.
     * @param sid
     *            System id
     * @return true if the system requires a reboot i.e: because kernel updates.
     */
    public static boolean requiresReboot(User user, Long sid) {
        return !getSystemsRequiringReboot(user, Optional.of(sid)).isEmpty();
    }

    /**
     * Returns list of systems requiring a reboot i.e: because kernel updates,
     * visible to user, sorted by name.
     *
     * @param user
     *            Currently logged in user.
     * @return list of SystemOverviews.
     */
    public static DataResult<SystemOverview> requiringRebootList(User user) {
        return getSystemsRequiringReboot(user, Optional.empty());
    }

    /**
     * Returns a list of systems that match a serverId and require a reboot (i.e: because kernel updates,
     * visible to user, sorted by name).
     * If the serverId parameter is empty, this query will return all the systems that require a reboot.
     *
     * @param user Currently logged in user.
     * @param serverId the serverId.
     * @return list of SystemOverviews.
     */
    private static DataResult<SystemOverview> getSystemsRequiringReboot(User user, Optional<Long> serverId) {
        SelectMode m = ModeFactory.getMode("System_queries", "systems_requiring_reboot");
        Map<String, Object> params = new HashMap<>();
        params.put("org_id", user.getOrg().getId());
        params.put("user_id", user.getId());
        params.put("sid", serverId.orElse(null));
        Map<String, Object> elabParams = new HashMap<>();
        return makeDataResult(params, elabParams, null, m, SystemOverview.class);
    }

    /**
     * Returns a list of systems with extra packages installed.
     *
     * @param user User to check the systems for
     * @param pc Page control
     *
     * @return list of SystemOverviews.
     */
    public static DataResult<SystemOverview> getExtraPackagesSystems(User user,
            PageControl pc) {
        SelectMode m = ModeFactory.getMode("System_queries",
            "extra_packages_systems_count");
        Map<String, Object> params = new HashMap<>();
        params.put("userid", user.getId());
        params.put("orgid", user.getOrg().getId());

        return makeDataResult(params, new HashMap<String, Object>(), pc, m,
                SystemOverview.class);
    }

    /**
     * Returns the list of extra packages for a system.
     * @param serverId Server ID in question
     * @return List of extra packages
     */
    public static DataResult<PackageListItem> listExtraPackages(Long serverId) {
        SelectMode m = ModeFactory.getMode("Package_queries",
                                           "extra_packages_for_system");
        Map<String, Object> params = new HashMap<>();
        params.put("sid", serverId);

        return makeDataResult(params, params, null, m, PackageListItem.class);
    }

    /**
     * Gets the latest upgradable packages for a system
     * @param sid The id for the system we want packages for
     * @return Returns a list of the latest upgradable packages for a system
     */
    public static DataResult<Map<String, Object>> latestUpgradablePackages(Long sid) {
        SelectMode m = ModeFactory.getMode("Package_queries",
                "system_upgradable_package_list_no_errata_info",
                Map.class);
        Map<String, Object> params = new HashMap<>();
        params.put("sid", sid);
        return m.execute(params);
    }

    /**
     * Get all installable packages for a given system.
     * @param sid The id for the system we want packages for
     * @return Return a list of all installable packages for a system.
     */
    public static DataResult<Map<String, Object>> allInstallablePackages(Long sid) {
        SelectMode m = ModeFactory.getMode("Package_queries",
                "system_all_available_packages",
                Map.class);
        Map<String, Object> params = new HashMap<>();
        params.put("sid", sid);
        return m.execute(params);
    }

    /**
     * Gets the latest installable packages for a system
     * @param sid The id for the system we want packages for
     * @return Returns a list of latest installable packages for a system.
     */
    public static DataResult<Map<String, Object>> latestInstallablePackages(Long sid) {
        SelectMode m = ModeFactory.getMode("Package_queries",
                "system_latest_available_packages",
                Map.class);
        Map<String, Object> params = new HashMap<>();
        params.put("sid", sid);
        return m.execute(params);
    }

    /**
     * Gets the installed packages on a system
     * @param sid The system in question
     * @return Returns a list of packages for a system
     */
    public static DataResult<Map<String, Object>> installedPackages(Long sid) {
        return installedPackages(sid, false);
    }

    /**
     * Gets the installed packages on a system
     * @param sid The system in question
     * @param archAsLabel set to true to return architecture as label, otherwise architecture name is used
     * @return Returns a list of packages for a system
     */
    public static DataResult<Map<String, Object>> installedPackages(Long sid, boolean archAsLabel) {
        String suffix = archAsLabel ? "_arch_as_label" : "";
        String query = "system_installed_packages" + suffix;
        SelectMode m = ModeFactory.getMode("System_queries", query, Map.class);
        Map<String, Object> params = new HashMap<>();
        params.put("sid", sid);
        DataResult<Map<String, Object>> pkgs = m.execute(params);
        for (Map<String, Object> pkg : pkgs) {
            if (pkg.get("arch") == null) {
                pkg.put("arch", LocalizationService.getInstance().getMessage("Unknown"));
            }
            if (pkg.get("installtime") == null) {
                pkg.remove("installtime");
            }
        }
        return pkgs;
    }

    /**
     * Gets packages from a channel for a system
     * @param sid server id
     * @param cid channel id
     * @return list of packages installed on a system from a channel
     */
    public static DataResult<Map<String, Object>> packagesFromChannel(Long sid, Long cid) {
        SelectMode m = ModeFactory.getMode("Package_queries",
                "system_packages_from_channel", Map.class);
        Map<String, Object> params = new HashMap<>();
        params.put("sid", sid);
        params.put("cid", cid);
        return m.execute(params);
    }

    private static String createUniqueId(List<String> fields) {
        // craft unique id based on given data
        String delimiter = "_";
        return delimiter + fields
                .stream()
                .reduce((i1, i2) -> i1 + delimiter + i2)
                .orElseThrow();
    }

    /**
     * Create an empty system profile with required values based on given data.
     *
     * The data must contain at least one of the following fields:
     * - hwAddress - HW address of a NetworkInterface of the profile
     * - hostname - hostname (FQDN) of the profile
     *
     * @param creator the creator user
     * @param systemName the system name
     * @param data the data of the new profile
     * @throws SystemsExistException when a system based on the input data already exists
     * @throws java.lang.IllegalArgumentException when the input data contains insufficient information or
     * if the format of the hardware address is invalid
     * @return the created system
     */
    public MinionServer createSystemProfile(User creator, String systemName, Map<String, Object> data) {
        Optional<String> hwAddress = ofNullable((String) data.get("hwAddress"));
        Optional<String> hostname = ofNullable((String) data.get("hostname"));

        // at least one identifier must be contained
        if (!hwAddress.isPresent() && !hostname.isPresent()) {
            throw new IllegalArgumentException("hwAddress or hostname key must be present.");
        }

        Set<String> hwAddrs = hwAddress.map(Collections::singleton).orElse(emptySet());
        List<MinionServer> matchingProfiles = findMatchingEmptyProfiles(hostname, hwAddrs);
        if (!matchingProfiles.isEmpty()) {
            throw new SystemsExistException(matchingProfiles.stream().map(Server::getId).collect(Collectors.toList()));
        }

        String uniqueId = createUniqueId(
                Arrays.asList(hwAddress, hostname).stream().flatMap(Opt::stream).collect(Collectors.toList()));

        MinionServer server = new MinionServer();
        server.setName(systemName);
        server.setOrg(creator.getOrg());

        // Set network device information to the server so we have something to match with
        server.setCreator(creator);
        hostname.ifPresent(server::setHostname);
        server.setDigitalServerId(uniqueId);
        server.setMachineId(uniqueId);
        server.setMinionId(uniqueId);
        server.setOs("(unknown)");
        server.setOsFamily("(unknown)");
        server.setRelease("(unknown)");
        server.setSecret(RandomStringUtils.random(64, 0, 0, true, true, null, new SecureRandom()));
        server.setAutoUpdate("N");
        server.setContactMethod(ServerFactory.findContactMethodByLabel("default"));
        server.setLastBoot(System.currentTimeMillis() / 1000);
        server.setServerArch(ServerFactory.lookupServerArchByLabel("x86_64-redhat-linux"));
        server.updateServerInfo();
        ServerFactory.save(server);
        systemEntitlementManager.setBaseEntitlement(server, EntitlementManager.BOOTSTRAP);

        hwAddress.ifPresent(addr -> {
            NetworkInterface netInterface = new NetworkInterface();
            netInterface.setHwaddr(addr);
            netInterface.setServer(server);
            netInterface.setName("unknown");
            if (!netInterface.isValid()) {
                throw new IllegalArgumentException("Invalid network interface: " + netInterface);
            }
            ServerFactory.saveNetworkInterface(netInterface);
        });

        return server;
    }

    /**
     * Find matching empty profiles based on hostname and HW addresses (in this order).
     *
     * @param hostname the hostname
     * @param hwAddrs the set of HW addresses
     * @return the List of matching empty profiles
     */
    public static List<MinionServer> findMatchingEmptyProfiles(Optional<String> hostname, Set<String> hwAddrs) {
        List<MinionServer> hostnameMatches = hostname
                .map(MinionServerFactory::findEmptyProfilesByHostName)
                .orElse(emptyList());
        if (!hostnameMatches.isEmpty()) {
            return hostnameMatches;
        }

        List<MinionServer> hwAddrMatches = MinionServerFactory.findEmptyProfilesByHwAddrs(hwAddrs);
        return hwAddrMatches;
    }

    /**
     * Adds a history event to a system.
     *
     * @param server the system
     * @param summary the event summary
     * @param details the event details
     * @return the created event
     */
    public static ServerHistoryEvent addHistoryEvent(Server server, String summary, String details) {
        ServerHistoryEvent historyEvent = new ServerHistoryEvent();
        historyEvent.setCreated(new java.util.Date());
        historyEvent.setServer(server);
        historyEvent.setSummary(summary);
        historyEvent.setDetails(details);
        server.getHistory().add(historyEvent);
        return historyEvent;
    }

    /**
     * Lists empty system profiles (created by createSystemProfile).
     *
     * @param user user viewing the systems
     * @param pc page control
     * @return list of empty system profiles
     */
    public static DataResult<EmptySystemProfileOverview> listEmptySystemProfiles(User user, PageControl pc) {
        SelectMode m = ModeFactory.getMode("System_queries", "xmlrpc_empty_profiles", EmptySystemProfileOverview.class);
        Map<String, Long> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("org_id", user.getOrg().getId());
        Map<String, Long> elabParams = new HashMap<>();

        return makeDataResult(params, elabParams, pc, m, BootstrapSystemOverview.class);
    }

    /**
     * How to cleanup the server on deletion.
      */
    public enum ServerCleanupType {
        /**
         * Fail in case of cleanup error.
         */
        FAIL_ON_CLEANUP_ERR,
        /**
         * Don't cleanup, just delete.
         */
        NO_CLEANUP,
        /**
         * Try cleanup first but delete server
         * anyway in case of error.
         */
        FORCE_DELETE;

        /**
         * Get enum value from string.
         * @param value the string
         * @return an Optional with the enum value or empty if string didn't
         * match any enum value.
         */
        public static Optional<ServerCleanupType> fromString(String value) {
            try {
                return Optional.of(valueOf(value.toUpperCase()));
            }
            catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        }

    }

    /**
     * Checks if a list of systems contains a salt minion.
     * @param systems a list of systems
     * @return true if the list contains at least a salt minion
     */
    public static boolean containsSaltMinion(List<SystemOverview> systems) {
        return systems.stream()
                .map(overview -> ServerFactory.lookupById(overview.getId()))
                .filter(server -> server.asMinionServer().isPresent())
                .count() > 0;
    }

    /**
     * Delete a server and in case of Salt ssh-push minions remove SUSE Manager
     * specific configuration. When removing ssh-push minions the default
     * timeout for the cleanup operation is set to 5 minutes.
     *
     * @param user the user
     * @param sid the server id
     * @param cleanupType cleanup options
     * @return a list of cleanup errors or empty if no errors or no cleanup was done
     */
    public Optional<List<String>> deleteServerAndCleanup(
            User user, long sid, ServerCleanupType cleanupType) {
        return deleteServerAndCleanup(user, sid, cleanupType, 300);
    }

    /**
     * Delete a server and in case of Salt ssh-push minions remove SUSE Manager
     * specific configuration.
     *
     * @param user the user
     * @param sid the server id
     * @param cleanupType cleanup options
     * @param cleanupTimeout timeout for cleanup operation
     * @return a list of cleanup errors or empty if no errors or no cleanup was done
     */
    public Optional<List<String>> deleteServerAndCleanup(
            User user, long sid, ServerCleanupType cleanupType, int cleanupTimeout) {
        if (!ServerCleanupType.NO_CLEANUP.equals(cleanupType)) {
            Server server = lookupByIdAndUser(sid, user);
            if (server.asMinionServer().isPresent()) {
                Optional<List<String>> errs = saltApi
                        .cleanupMinion(server.asMinionServer().get(), cleanupTimeout);
                if (errs.isPresent() &&
                        ServerCleanupType.FAIL_ON_CLEANUP_ERR.equals(cleanupType)) {
                    return errs;
                } // else FORCE_DELETE
            }
        }
        deleteServer(user, sid);
        return Optional.empty();
    }

    /**
     * Deletes a Server and associated VirtualInstances:
     *  - If the server was a virtual guest, remove the VirtualInstance that links it to its
     *  host server.
     *  - If the server was a virtual host, remove all its entitlements and all
     *  VirtualInstances that link it to the guest servers.
     * @param user The user doing the deleting.
     * @param sid The id of the Server to be deleted
     */
    public void deleteServer(User user, Long sid) {
        deleteServer(user, sid, true);
    }

    /**
     * Deletes a Server and associated VirtualInstances:
     *  - If the server was a virtual guest, remove the VirtualInstance that links it to its
     *  host server.
     *  - If the server was a virtual host, remove all its entitlements and all
     *  VirtualInstances that link it to the guest servers.
     * @param user The user doing the deleting.
     * @param sid The id of the Server to be deleted
     * @param deleteSaltKey delete also the salt key when set to true
     */
    public void deleteServer(User user, Long sid, boolean deleteSaltKey) {
        /*
         * Looking up the server here rather than being passed in a Server object, allows
         * us to call lookupByIdAndUser which will ensure the user has access to this
         * server.
         */
        Server server = lookupByIdAndUser(sid, user);

        CobblerSystemRemoveCommand rc = new CobblerSystemRemoveCommand(user, server);
        rc.store();

        // remove associated VirtualInstances
        Set<VirtualInstance> toRemove = new HashSet<>();
        if (server.isVirtualGuest()) {
            // If the host is known, then just remove the system, keep the virtual instance to allow deleting the VM
            if (server.getVirtualInstance().getHostSystem() == null) {
                toRemove.add(server.getVirtualInstance());
            }
        }
        else {
            systemEntitlementManager.removeAllServerEntitlements(server);
            toRemove.addAll(server.getGuests());
        }
        toRemove.stream().forEach(vi ->
            VirtualInstanceFactory.getInstance().deleteVirtualInstanceOnly(vi));

        server.asMinionServer().ifPresent(minion -> minion.getAccessTokens().forEach(token -> {
            token.setValid(false);
            token.setMinion(null);
            AccessTokenFactory.save(token);
        }));


        // clean known_hosts
        if (server.asMinionServer().isPresent()) {
            removeSaltSSHKnownHosts(server);
        }

        server.asMinionServer().ifPresent(minion -> {
            SaltStateGeneratorService.INSTANCE.removeServer(minion);
            if (deleteSaltKey) {
                saltApi.deleteKey(minion.getMinionId());
            }
        });

        // remove server itself
        ServerFactory.delete(server);
    }

    private void removeSaltSSHKnownHosts(Server server) {
        Integer sshPort = server.getProxyInfo() != null ? server.getProxyInfo().getSshPort() : null;
        int port = sshPort != null ? sshPort : SaltSSHService.SSH_DEFAULT_PORT;
        Optional.ofNullable(server.getHostname()).ifPresentOrElse(
                hostname -> {
                    Optional<MgrUtilRunner.RemoveKnowHostResult> result =
                            saltApi.removeSaltSSHKnownHost(hostname, port);
                    boolean removed = result.map(r -> "removed".equals(r.getStatus())).orElse(false);
                    if (!removed) {
                        log.warn("Hostname {}:{} could not be removed from /var/lib/salt/.ssh/known_hosts: {}",
                                hostname, port, result.map(r -> r.getComment()).orElse(""));
                    }
                },
                () -> log.warn("Unable to remove SSH key for {} from /var/lib/salt/.ssh/known_hosts: unknown hostname",
                        server.getName()));
    }

    /**
     * Adds servers to a server group
     * @param servers The servers to add
     * @param serverGroup The group to add the server to
     */
    public void addServersToServerGroup(Collection<Server> servers,
            ServerGroup serverGroup) {
        ServerFactory.addServersToGroup(servers, serverGroup);
        snapshotServers(servers, "Group membership alteration");

        if (FormulaFactory.hasMonitoringDataEnabled(serverGroup)) {
            for (Server server : servers) {
                systemEntitlementManager.grantMonitoringEntitlement(server);
            }
        }
    }

    /**
     * Adds a server to a server group
     * @param server The server to add
     * @param serverGroup The group to add the server to
     */
    public void addServerToServerGroup(Server server, ServerGroup serverGroup) {
        addServersToServerGroup(Arrays.asList(server), serverGroup);
    }

    /**
     * Removes a set of servers from a group
     * @param servers The servers to remove
     * @param serverGroup The group to remove the servers from
     */
    public void removeServersFromServerGroup(Collection<Server> servers, ServerGroup serverGroup) {
        ServerFactory.removeServersFromGroup(servers, serverGroup);
        snapshotServers(servers, "Group membership alteration");
        if (FormulaFactory.hasMonitoringDataEnabled(serverGroup)) {
            for (Server server : servers) {
                systemEntitlementManager.removeServerEntitlement(server, EntitlementManager.MONITORING);
            }
        }
    }

    /**
     * Returns a list of available server groups for a given server
     * @param server The server in question
     * @param user The user requesting the information
     * @return Returns a list of system groups available for this server/user
     */
    public static DataResult<Map<String, Object>> availableSystemGroups(Server server,
            User user) {
        SelectMode m = ModeFactory.getMode("SystemGroup_queries", "visible_to_system",
                Map.class);
        Map<String, Object> params = new HashMap<>();
        params.put("sid", server.getId());
        params.put("org_id", user.getOrg().getId());
        params.put("user_id", user.getId());
        return m.execute(params);
    }

    /**
     * Returns a list of server groups for a given server
     * @param sid The server id in question
     * @return Returns a list of system groups for this server
     */
    public static DataResult<Map<String, Object>> listSystemGroups(Long sid) {
        SelectMode m = ModeFactory.getMode("SystemGroup_queries",
                                           "groups_a_system_is_in_unsafe", Map.class);
        Map<String, Object> params = new HashMap<>();
        params.put("sid", sid);
        return m.execute(params);
    }

    /**
     * Returns list of all notes for a system.
     * @param s The server in question
     * @return list of SystemNotes.
     */
    public static DataResult<Map<String, Object>> systemNotes(Server s) {
        SelectMode m = ModeFactory.getMode("System_queries", "server_notes");
        Map<String, Object> params = new HashMap<>();
        params.put("sid", s.getId());
        return m.execute(params);
    }

    /**
     * Returns list of all systems visible to user.
     * @param user Currently logged in user.
     * @param pc PageControl
     * @return list of SystemOverviews.
     */
    public static DataResult<SystemOverview> systemList(User user, PageControl pc) {
        SelectMode m = ModeFactory.getMode("System_queries", "visible_to_user");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        Map<String, Object> elabParams = new HashMap<>();
        return makeDataResult(params, elabParams, pc, m, SystemOverview.class);
    }

    /**
     * Returns list of all physical systems visible to user.
     * @param user Currently logged in user.
     * @param pc PageControl
     * @return list of SystemOverviews.
     */
    public static DataResult<SystemOverview> physicalList(User user, PageControl pc) {
        SelectMode m = ModeFactory.getMode("System_queries",
                "visible_to_user_physical_list");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        Map<String, Object> elabParams = new HashMap<>();
        return makeDataResult(params, elabParams, pc, m, SystemOverview.class);
    }

    /**
     * Returns list of all systems and their errata type counts
     * @param user Currently logged in user.
     * @param pc PageControl
     * @return list of SystemOverviews.
     */
    public static DataResult<SystemCurrency> systemCurrencyList(User user,
            PageControl pc) {
        SelectMode m = ModeFactory.getMode("System_queries", "system_currency");
        Map<String, Object> params = new HashMap<>();
        params.put("uid", user.getId());

        return m.execute(params, Arrays.asList(
                ServerConstants.getServerGroupTypeForeignEntitled().getId()));
    }

    /**
     * Returns list of all systems visible to user.
     *    This is meant to be fast and only gets the id, name, and last checkin
     * @param user Currently logged in user.
     * @param pc PageControl
     * @return list of SystemOverviews.
     */
    public static DataResult<ShortSystemInfo> systemListShort(User user,
            PageControl pc) {
        SelectMode m = ModeFactory.getMode("System_queries", "xmlrpc_visible_to_user",
                ShortSystemInfo.class);
        Map<String, Long> params = new HashMap<>();
        params.put("user_id", user.getId());
        Map<String, Long> elabParams = new HashMap<>();

        return makeDataResult(params, elabParams, pc, m, ShortSystemInfo.class);
    }

    /**
     * Returns list of all systems visible to user that are inactive.
     *    This is meant to be fast and only gets the id, name, and last checkin
     * @param user Currently logged in user.
     * @param pc PageControl
     * @return list of SystemOverviews.
     */
    public static DataResult<ShortSystemInfo> systemListShortInactive(User user,
            PageControl pc) {
        return systemListShortInactive(user, Config.get().getInt(ConfigDefaults
                .SYSTEM_CHECKIN_THRESHOLD), pc);
    }

    /**
     * Returns list of all systems visible to user that are inactive.
     *    This is meant to be fast and only gets the id, name, and last checkin
     * @param user Currently logged in user.
     * @param inactiveThreshold number of days before we consider systems inactive
     * @param pc PageControl
     * @return list of SystemOverviews.
     */
    public static DataResult<ShortSystemInfo> systemListShortInactive(
            User user, int inactiveThreshold, PageControl pc) {
        SelectMode m = ModeFactory.getMode(
                "System_queries", "xmlrpc_visible_to_user_inactive",
                ShortSystemInfo.class);
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("checkin_threshold", inactiveThreshold);
        Map<String, Object> elabParams = new HashMap<>();

        return makeDataResult(params, elabParams, pc, m, ShortSystemInfo.class);
    }

    /**
     * Returns list of all systems visible to user that are active.
     *    This is meant to be fast and only gets the id, name, and last checkin
     * @param user Currently logged in user.
     * @param pc PageControl
     * @return list of ShortSystemInfos.
     */
    public static DataResult<ShortSystemInfo> systemListShortActive(User user,
            PageControl pc) {
        SelectMode m = ModeFactory.getMode(
                "System_queries", "xmlrpc_visible_to_user_active", ShortSystemInfo.class);
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("checkin_threshold", Config.get().getInt(ConfigDefaults
                .SYSTEM_CHECKIN_THRESHOLD));
        Map<String, Object> elabParams = new HashMap<>();

        return makeDataResult(params, elabParams, pc, m, ShortSystemInfo.class);
    }

    /**
     * Returns list of all systems that are  visible to user
     * but not in the given server group.
     * @param user Currently logged in user.
     * @param sg a ServerGroup
     * @param pc PageControl
     * @return list of SystemOverviews.
     */
    public static DataResult<SystemOverview> systemsNotInGroup(User user,
            ServerGroup sg, PageControl pc) {
        SelectMode m = ModeFactory.getMode("System_queries", "target_systems_for_group");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("sgid", sg.getId());
        Map<String, Object> elabParams = new HashMap<>();
        return makeDataResult(params, elabParams, pc, m, SystemOverview.class);
    }

    /**
     * Returns a list of all systems visible to user with pending errata.
     * @param user Current logged in user.
     * @param pc PageControl
     * @return list of SystemOverviews
     */
    public static DataResult<SystemOverview> mostCriticalSystems(User user,
            PageControl pc) {
        SelectMode m = ModeFactory.getMode("System_queries", "most_critical_systems");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        Map<String, Object> elabParams = new HashMap<>();
        return makeDataResult(params, elabParams, pc, m, SystemOverview.class);
    }

    /**
     * Returns list of all systems visible to user.
     * @param user Currently logged in user.
     * @param feature The String label of the feature we want to get a list of systems for.
     * @param pc PageControl
     * @return list of SystemOverviews.
     */
    public static DataResult<SystemOverview> systemsWithFeature(User user, String feature,
            PageControl pc) {
        SelectMode m = ModeFactory.getMode("System_queries", "systems_with_feature");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("feature", feature);
        Map<String, Object> elabParams = new HashMap<>();
        return makeDataResult(params, elabParams, pc, m, SystemOverview.class);
    }

    /**
     * Returns list of out of date systems visible to user.
     * @param user Currently logged in user.
     * @param pc PageControl
     * @return list of SystemOverviews.
     */
    public static DataResult<SystemOverview> outOfDateList(User user,
            PageControl pc) {
        SelectMode m = ModeFactory.getMode("System_queries", "out_of_date");
        Map<String, Long> params = new HashMap<>();
        params.put("org_id", user.getOrg().getId());
        params.put("user_id", user.getId());
        Map<String, Long> elabParams = new HashMap<>();
        return makeDataResult(params, elabParams, pc, m, SystemOverview.class);
    }

    /**
     * Returns list of unentitled systems visible to user.
     * @param user Currently logged in user.
     * @param pc PageControl
     * @return list of SystemOverviews.
     */
    public static DataResult<SystemOverview> unentitledList(User user, PageControl pc) {
        SelectMode m = ModeFactory.getMode("System_queries", "unentitled");
        Map<String, Object> params = new HashMap<>();
        params.put("org_id", user.getOrg().getId());
        params.put("user_id", user.getId());
        Map<String, Object> elabParams = new HashMap<>();
        return makeDataResult(params, elabParams, pc, m, SystemOverview.class);
    }

    /**
     * Returns list of ungrouped systems visible to user.
     * @param user Currently logged in user.
     * @param pc PageControl
     * @return list of SystemOverviews.
     */
    public static DataResult<SystemOverview> ungroupedList(User user, PageControl pc) {
        SelectMode m = ModeFactory.getMode("System_queries", "ungrouped");
        Map<String, Object> params = new HashMap<>();
        params.put("org_id", user.getOrg().getId());
        params.put("user_id", user.getId());
        Map<String, Object> elabParams = new HashMap<>();
        return makeDataResult(params, elabParams, pc, m, SystemOverview.class);
    }

    /**
     * Returns list of inactive systems visible to user, sorted by name.
     * @param user Currently logged in user.
     * @param pc PageControl
     * @return list of SystemOverviews.
     */
    public static DataResult<SystemOverview> inactiveList(User user, PageControl pc) {
        SelectMode m = ModeFactory.getMode("System_queries", "inactive");
        Map<String, Object> params = new HashMap<>();
        params.put("org_id", user.getOrg().getId());
        params.put("user_id", user.getId());
        params.put("checkin_threshold", Config.get().getInt(ConfigDefaults
                .SYSTEM_CHECKIN_THRESHOLD));
        Map<String, Object> elabParams = new HashMap<>();
        return makeDataResult(params, elabParams, pc, m, SystemOverview.class);
    }

    /**
     * Returns list of inactive systems visible to user, sorted by name.
     * @param user Currently logged in user.
     * @param pc PageControl
     * @param inactiveDays number of days the systems should have been inactive for
     * @return list of SystemOverviews.
     */
    public static DataResult<SystemOverview> inactiveList(User user, PageControl pc,
            int inactiveDays) {
        SelectMode m = ModeFactory.getMode("System_queries", "inactive");
        Map<String, Object> params = new HashMap<>();
        params.put("org_id", user.getOrg().getId());
        params.put("user_id", user.getId());
        params.put("checkin_threshold", inactiveDays);
        Map<String, Object> elabParams = new HashMap<>();
        return makeDataResult(params, elabParams, pc, m, SystemOverview.class);
    }


    /**
     * Returns a list of systems recently registered by the user
     * @param user Currently logged in user.
     * @param pc PageControl
     * @param threshold maximum amount of days ago the system, 0 returns all systems
     * was registered for it to appear in the list
     * @return list of SystemOverviews
     */
    public static DataResult<SystemOverview> registeredList(User user,
            PageControl pc,
            int threshold) {
        SelectMode m;
        Map<String, Object> params = new HashMap<>();

        if (threshold == 0) {
            m = ModeFactory.getMode("System_queries",
                    "all_systems_by_registration");
        }
        else {
            m = ModeFactory.getMode("System_queries",
                    "recently_registered");
            params.put("threshold", threshold);
        }

        params.put("org_id", user.getOrg().getId());
        params.put("user_id", user.getId());
        Map<String, Object> elabParams = new HashMap<>();
        return makeDataResult(params, elabParams, pc, m, SystemOverview.class);
    }

    /**
     * Returns list of inactive systems visible to user, sorted by the systems' last
     * checkin time instead of by name.
     * @param user Currently logged in user.
     * @param pc PageControl
     * @return list of SystemOverviews.
     */
    public static DataResult<SystemOverview> inactiveListSortbyCheckinTime(User user,
            PageControl pc) {
        SelectMode m = ModeFactory.getMode("System_queries",
                "inactive_order_by_checkin_time");
        Map<String, Object> params = new HashMap<>();
        params.put("org_id", user.getOrg().getId());
        params.put("user_id", user.getId());
        params.put("checkin_threshold", Config.get().getInt(ConfigDefaults
                .SYSTEM_CHECKIN_THRESHOLD));
        Map<String, Object> elabParams = new HashMap<>();
        return makeDataResult(params, elabParams, pc, m, SystemOverview.class);
    }

    /**
     * Returns list of proxy systems visible to user.
     * @param user Currently logged in user.
     * @param pc PageControl
     * @return list of SystemOverviews.
     */
    public static DataResult<SystemOverview> proxyList(User user, PageControl pc) {
        SelectMode m = ModeFactory.getMode("System_queries", "proxy_servers");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        Map<String, Object> elabParams = new HashMap<>();
        return makeDataResult(params, elabParams, pc, m, SystemOverview.class);
    }

     /**
     * Returns list of virtual host systems visible to user.
     * @param user Currently logged in user.
     * @param pc PageControl
     * @return list of SystemOverviews.
     */
    public static DataResult<VirtualSystemOverview> virtualSystemsList(User user, PageControl pc) {
        return virtualSystemsListQueryBuilder()
                .run(Map.of("user_id", user.getId()), pc, PagedSqlQueryBuilder::parseFilterAsText,
                        VirtualSystemOverview.class);
    }

    /**
     * @return the Paged SQL query builder used for the virtual systems list.
     */
    public static PagedSqlQueryBuilder virtualSystemsListQueryBuilder() {
        return new PagedSqlQueryBuilder("VI.uuid")
                .select("VI.host_system_id, " +
                        "VI.virtual_system_id, " +
                        "VI.uuid, " +
                        "COALESCE(RS.name, '(none)') AS host_server_name, " +
                        "COALESCE(VII.name, '(none)') AS server_name, " +
                        "COALESCE(VIS.name, '(unknown)') AS STATE_NAME, " +
                        "COALESCE(VIS.label, 'unknown') AS STATE_LABEL, " +
                        "COALESCE(VII.vcpus, 0) AS VCPUS, " +
                        "COALESCE(VII.memory_size, 0) AS MEMORY, " +
                        "rhn_channel.user_role_check((" +
                        "   select channel_id " +
                        "   from rhnServerOverview " +
                        "   where server_id = VI.virtual_system_id), :user_id, 'subscribe') AS subscribable")
                .from("rhnVirtualInstance VI " +
                        "    LEFT OUTER JOIN rhnVirtualInstanceInfo VII ON VI.id = VII.instance_id " +
                        "    LEFT OUTER JOIN rhnVirtualInstanceState VIS ON VII.state = VIS.id " +
                        "    LEFT OUTER JOIN rhnServer RS ON RS.id = VI.host_system_id")
                .where("EXISTS ( " +
                        "   SELECT 1 " +
                        "   FROM rhnUserServerPerms USP " +
                        "   WHERE USP.user_id = :user_id " +
                        "     AND (USP.server_id = VI.host_system_id OR USP.server_id = VI.virtual_system_id) " +
                        ") AND VI.uuid IS NOT NULL");
    }

    /**
     * Returns list of virtual guest systems running 'under' the given system.
     * @param user Currently logged in user.
     * @param sid The id of the system we are looking at
     * @param pc PageControl
     * @return list of SystemOverviews.
     */
    public static DataResult<VirtualSystemOverview> virtualGuestsForHostList(
            User user, Long sid, PageControl pc) {
        SelectMode m = ModeFactory.getMode("System_queries", "virtual_guests_for_host");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("sid", sid);
        Map<String, Object> elabParams = new HashMap<>();
        return makeDataResult(params, elabParams, pc, m, VirtualSystemOverview.class);
    }

    /**
     * Returns list of virtual systems in the given set
     * @param user Currently logged in user.
     * @param setLabel The label of the set of virtual systems
     *        (rhnSet.elem = rhnVirtualInstance.id)
     * @param pc PageControl
     * @return list of SystemOverviews.
     */
    public static DataResult<VirtualSystemOverview> virtualSystemsInSet(
            User user,
            String setLabel,
            PageControl pc) {
        SelectMode m = ModeFactory.getMode("System_queries", "virtual_systems_in_set");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("set_label", setLabel);
        Map<String, Object> elabParams = new HashMap<>();
        return makeDataResult(params, elabParams, pc, m, VirtualSystemOverview.class);
    }

    /**
     * Returns list of system groups visible to user.
     * @param user Currently logged in user.
     * @param pc PageControl
     * @return list of SystemGroupOverviews.
     */
    public static DataResult<SystemGroupOverview> groupList(User user, PageControl pc) {
        SelectMode m = ModeFactory.getMode("SystemGroup_queries", "visible_to_user");
        if (Config.get().getBoolean(ConfigDefaults.WEB_DISABLE_UPDATE_STATUS)) {
            m.removeElaboratorByName("most_severe_errata");
        }
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        Map<String, Object> elabParams = new HashMap<>();
        elabParams.put("org_id", user.getOrg().getId());
        elabParams.put("user_id", user.getId());

        DataResult<SystemGroupOverview> dr = makeDataResult(params, elabParams, pc, m, SystemGroupOverview.class);
        if (Config.get().getBoolean(ConfigDefaults.WEB_DISABLE_UPDATE_STATUS)) {
            dr.stream().forEach(x -> x.setDisabled(true));
        }
        return dr;
    }

    /**
     * Returns list of system groups visible to user, with system-counts.
     * This is marginally faster than the work done when elaborating in
     * groupList(), to allow sorting-by-syscount without having to pay the
     * full overhead of groupList()'s elaborator.
     *
     * @param user Currently logged in user.
     * @param pc PageControl
     * @return list of SystemGroupOverviews.
     */
    public static DataResult<SystemGroupOverview> groupListWithServerCount(
                    User user, PageControl pc) {
        SelectMode m = ModeFactory.getMode("SystemGroup_queries",
                        "visible_to_user_and_counts");
        if (Config.get().getBoolean(ConfigDefaults.WEB_DISABLE_UPDATE_STATUS)) {
            m.removeElaboratorByName("most_severe_errata");
        }
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        Map<String, Object> elabParams = new HashMap<>();

        DataResult<SystemGroupOverview> dr = makeDataResult(params, elabParams, pc, m, SystemGroupOverview.class);
        if (Config.get().getBoolean(ConfigDefaults.WEB_DISABLE_UPDATE_STATUS)) {
            dr.stream().forEach(x -> x.setDisabled(true));
        }
        return dr;
    }

    /**
     * Returns list of systems in the specified group.
     * @param sgid System Group Id
     * @param pc PageControl
     * @return list of SystemOverviews.
     */
    public static DataResult<SystemOverview> systemsInGroup(Long sgid,
            PageControl pc) {
        SelectMode m = ModeFactory.getMode("System_queries", "systems_in_group");
        Map<String, Object> params = new HashMap<>();
        params.put("sgid", sgid);
        Map<String, Object> elabParams = new HashMap<>();
        return makeDataResult(params, elabParams, pc, m, SystemOverview.class);
    }

    /**
     * Returns list of systems in the specified group.
     * This is meant to be fast and only return id, name, and last_checkin
     * @param sgid System Group Id
     * @return list of SystemOverviews.
     */
    public static DataResult<SystemOverview> systemsInGroupShort(Long sgid) {
        SelectMode m = ModeFactory.getMode("System_queries", "xmlrpc_systems_in_group");
        Map<String, Object> params = new HashMap<>();
        params.put("sgid", sgid);
        Map<String, Object> elabParams = new HashMap<>();
        return makeDataResult(params, elabParams, null, m, SystemOverview.class);
    }

    /**
     * Returns the list of systems that are assigned to a specific maintenance schedule
     *
     * @param user currently logged in user
     * @param schedule the maintenance schedule
     * @param pc the {@link PageControl} object
     * @return list of {@link EssentialServerDto} objects
     */
    public static DataResult<EssentialServerDto> systemsInSchedule(User user, MaintenanceSchedule schedule,
            PageControl pc) {
        SelectMode m = ModeFactory.getMode("System_queries", "systems_in_maintenance_schedule");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("schedule_id", schedule.getId());
        return makeDataResult(params, emptyMap(), pc, m, EssentialServerDto.class);
    }

    /**
     * Returns a list of all systems and their assigned schedule details, if any
     *
     * @param user the authorized user
     * @param pc the {@link PageControl} object
     * @return list of {@link SystemScheduleDto} objects
     */
    public static DataResult<SystemScheduleDto> systemListWithSchedules(User user, PageControl pc) {
        SelectMode m = ModeFactory.getMode("System_queries", "visible_to_user_with_schedules");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        return makeDataResult(params, emptyMap(), pc, m, SystemScheduleDto.class);
    }

    /**
     * Returns the number of actions associated with a system
     * @param sid The system's id
     * @return number of actions
     */
    public static int countActions(Long sid) {
        SelectMode m = ModeFactory.getMode("System_queries", "actions_count");
        Map<String, Object> params = new HashMap<>();
        params.put("server_id", sid);
        DataResult<Map<String, Object>> dr = makeDataResult(params, params, null, m);
        return ((Long) dr.get(0).get("count")).intValue();
    }

    /**
     * Returns the number of package actions associated with a system
     * @param sid The system's id
     * @return number of package actions
     */
    public static int countPackageActions(Long sid) {
        SelectMode m = ModeFactory.getMode("System_queries", "package_actions_count");
        Map<String, Object> params = new HashMap<>();
        params.put("server_id", sid);
        DataResult<Map<String, Object>> dr = makeDataResult(params, params, null, m);
        return ((Long) dr.get(0).get("count")).intValue();
    }

    /**
     * Returns a list of unscheduled relevent errata for a system
     * @param user The user
     * @param sid The system's id
     * @param pc PageControl
     * @return a list of ErrataOverviews
     */
    public static DataResult<Errata> unscheduledErrata(User user, Long sid,
            PageControl pc) {
        SelectMode m = ModeFactory.getMode("Errata_queries",
                "unscheduled_relevant_to_system");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("sid", sid);

        Map<String, Object> elabParams = new HashMap<>();
        return makeDataResult(params, elabParams, pc, m, Errata.class);
    }

    /**
     * Returns whether a system has unscheduled relevant errata
     * @param user The user
     * @param sid The system's id
     * @return boolean of if system has unscheduled errata
     */
    public static boolean hasUnscheduledErrata(User user, Long sid) {
        SelectMode m = ModeFactory.getMode("Errata_queries",
                "count_unscheduled_relevant_to_system");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("sid", sid);
        DataResult<Map<String, Object>> dr = makeDataResult(params, null, null, m);
        return ((Long) dr.get(0).get("count")).intValue() > 0;
    }

    /**
     * Returns Kickstart sessions associated with a server
     * @param user The logged in user
     * @param sid The server id
     * @return a list of KickStartSessions
     */
    public static DataResult<KickstartSessionDto>
            lookupKickstartSession(User user, Long sid) {
        SelectMode m = ModeFactory.getMode("System_queries", "lookup_kickstart");

        Map<String, Object> params = new HashMap<>();
        params.put("org_id", user.getOrg().getId());
        params.put("sid", sid);

        return makeDataResult(params, params, null, m, KickstartSessionDto.class);
    }

    /**
     * Returns whether or not a server is kickstarting
     * @param user The logged in user
     * @param sid The server id
     * @return boolean of if a server is kickstarting
     */
    public static boolean isKickstarting(User user, Long sid) {
        for (KickstartSessionDto next : lookupKickstartSession(user, sid)) {
            if (!(next.getState().equals("complete") ||
                    next.getState().equals("failed"))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a list of errata relevant to a system
     * @param user The user
     * @param sid System Id
     * @return a list of ErrataOverviews
     */
    public static DataResult<ErrataOverview> relevantErrata(User user, Long sid) {
        SelectMode m = ModeFactory.getMode("Errata_queries", "relevant_to_system");

        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("sid", sid);

        Map<String, Object> elabParams = new HashMap<>();
        elabParams.put("sid", sid);
        elabParams.put("user_id", user.getId());

        return makeDataResultNoPagination(params, elabParams, m, ErrataOverview.class);
    }

    /**
     * Returns a list of errata relevant to a system
     * @param user The user
     * @param sid System Id
     * @param types of errata types (strings) to include
     * @return a list of ErrataOverviews
     */
    public static DataResult<ErrataOverview> relevantErrata(User user,
            Long sid, List<String> types) {
        SelectMode m = ModeFactory.getMode("Errata_queries", "relevant_to_system_by_types");

        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("sid", sid);

        Map<String, Object> elabParams = new HashMap<>();
        elabParams.put("sid", sid);
        elabParams.put("user_id", user.getId());

        DataResult<ErrataOverview> dr =  m.execute(params, types);
        dr.setElaborationParams(elabParams);
        return dr;
    }

    /**
     * Returns a list of errata relevant to a system
     * @param user The user
     * @param sid System Id
     * @param type of errata to include
     * @param severityLabel to filter by
     * @return a list of ErrataOverviews
     */
    public static DataResult<ErrataOverview> relevantCurrencyErrata(User user,
            Long sid, String type, String severityLabel) {
        SelectMode m = ModeFactory.getMode("Errata_queries",
                "security_relevant_to_system_by_severity");

        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("sid", sid);
        params.put("type", type);
        params.put("severity_label", severityLabel);

        Map<String, Object> elabParams = new HashMap<>();
        elabParams.put("sid", sid);
        elabParams.put("user_id", user.getId());

        DataResult<ErrataOverview> dr =  m.execute(params);
        dr.setElaborationParams(elabParams);
        return dr;
    }

    /**
     * Returns a list of errata relevant to a system by type
     * @param user The user
     * @param sid System Id
     * @param type Type
     * @return a list of ErrataOverviews
     */
    public static DataResult<ErrataOverview> relevantErrataByType(User user, Long sid,
            String type) {
        SelectMode m = ModeFactory.getMode("Errata_queries", "relevant_to_system_by_type");

        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("sid", sid);
        params.put("type", type);

        Map<String, Object> elabParams = new HashMap<>();
        elabParams.put("sid", sid);
        elabParams.put("user_id", user.getId());

        return makeDataResultNoPagination(params, elabParams, m, ErrataOverview.class);
    }

    /**
     * Returns a count of the number of critical errata that are present on the system.
     *
     * @param user user making the request
     * @param sid  identifies the server
     * @return number of critical errata on the system
     */
    public static int countCriticalErrataForSystem(User user, Long sid) {
        SelectMode m = ModeFactory.getMode("Errata_queries",
                "count_critical_errata_for_system");

        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("sid", sid);

        DataResult<Map<String, Object>> dr = makeDataResult(params, null, null, m);
        return ((Long) dr.get(0).get("count")).intValue();
    }

    /**
     * Returns a count of the number of non-critical errata that are present on the system.
     *
     * @param user user making the request
     * @param sid  identifies the server
     * @return number of non-critical errata on the system
     */
    public static int countNoncriticalErrataForSystem(User user, Long sid) {
        SelectMode m = ModeFactory.getMode("Errata_queries",
                "count_noncritical_errata_for_system");

        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("sid", sid);

        DataResult<Map<String, Object>> dr = makeDataResult(params, null, null, m);
        return ((Long) dr.get(0).get("count")).intValue();
    }

    /**
     * Returns a list of errata in a specified set
     * @param user The user
     * @param label The label for the errata set
     * @param pc PageControl
     * @return a list of ErrataOverviews
     */
    public static DataResult errataInSet(User user, String label,
            PageControl pc) {
        SelectMode m = ModeFactory.getMode("Errata_queries", "in_set");

        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("set_label", label);

        Map<String, Object> elabParams = new HashMap<>();
        elabParams.put("user_id", user.getId());

        DataResult dr =  m.execute(params);
        dr.setElaborationParams(elabParams);
        return dr;
    }

    /**
     * Looks up a server by its Id
     * @param sid The server's id
     * @param userIn who wants to lookup the Server
     * @return a server object associated with the given Id
     */
    public static Server lookupByIdAndUser(Long sid, User userIn) {
        Server server = ServerFactory.lookupByIdAndOrg(sid,
                userIn.getOrg());
        ensureAvailableToUser(userIn, sid);
        return server;
    }

    /**
     * Looks up a set of servers by their ids
     * @param serverIds The ids of the servers
     * @param userId the id of the user who wants to lookup the servers
     * @return a list of servers
     */
    public static List<Server> lookupByServerIdsAndUser(List<Long> serverIds, Long userId) {
        List<Server> servers = ServerFactory.lookupByIds(serverIds);

        if (servers.size() == serverIds.size()) {
            if (areSystemsAvailableToUser(userId, serverIds)) {
                return servers;
            }
        }
        throw new LookupException("Could not find servers for user " + userId);
    }

    /**
     * Returns a List of hydrated server objects from server ids.
     * @param serverIds the list of server ids to hyrdrate
     * @param userIn the user who wants to lookup the server
     * @return a List of hydrated server objects.
     */
    public static List<Server> hydrateServerFromIds(Collection<Long> serverIds,
            User userIn) {
        List<Server> servers = new ArrayList<>(serverIds.size());
        for (Long id : serverIds) {
            servers.add(lookupByIdAndUser(id, userIn));
        }
        return servers;
    }

    /**
     * Looks up a server by its Id
     * @param sid The server's id
     * @param org who wants to lookup the Server
     * @return a server object associated with the given Id
     */
    public static Server lookupByIdAndOrg(Long sid, Org org) {
        return ServerFactory.lookupByIdAndOrg(sid, org);
    }

    /**
     * Looks up a Server by it's client certificate.
     * @param cert ClientCertificate of the server.
     * @return the Server which matches the client certificate.
     * @throws InvalidCertificateException thrown if certificate is invalid.
     */
    public static Server lookupByCert(ClientCertificate cert)
            throws InvalidCertificateException {

        return ServerFactory.lookupByCert(cert);
    }

    /**
     * Returns the list of activation keys used when the system was
     * registered.
     * @param serverIn the server to query for
     * @return list of ActivationKeyDto containing the token id and name
     */
    public static DataResult<ActivationKeyDto> getActivationKeys(Server serverIn) {

        SelectMode m = ModeFactory.getMode("General_queries",
                "activation_keys_for_server");
        Map<String, Object> params = new HashMap<>();
        params.put("server_id", serverIn.getId());
        return makeDataResult(params, Collections.EMPTY_MAP, null, m,
                ActivationKeyDto.class);
    }

    /**
     * Returns list of inactive systems visible to user, sorted by name.
     * @param user Currently logged in user.
     * @param pc PageControl
     * @return list of SystemOverviews.
     */
    public static DataResult<SystemOverview>
            getSystemEntitlements(User user, PageControl pc) {
        SelectMode m = ModeFactory.getMode("System_queries", "system_entitlement_list");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        return makeDataResult(params, Collections.EMPTY_MAP, pc, m, SystemOverview.class);
    }



    /**
     * Returns the entitlements for the given server id.
     * @param sid Server id
     * @return entitlements - ArrayList of entitlements
     */
    public static List<Entitlement> getServerEntitlements(Long sid) {
        List<Entitlement> entitlements = new ArrayList<>();

        SelectMode m = ModeFactory.getMode("General_queries", "system_entitlements");

        Map<String, Object> params = new HashMap<>();
        params.put("sid", sid);

        DataResult<Map<String, Object>> dr = makeDataResult(params, null, null, m);

        if (dr.isEmpty()) {
            return null;
        }

        for (Map<String, Object> map : dr) {
            String ent = (String) map.get("label");
            entitlements.add(EntitlementManager.getByName(ent));
        }

        return entitlements;
    }

    /**
     * Used to test if the server has a specific entitlement.
     * We should almost always check for features with serverHasFeature instead.
     * @param sid Server id
     * @param ent Entitlement to look for
     * @return true if the server has the specified entitlement
     */
    public static boolean hasEntitlement(Long sid, Entitlement ent) {
        List<Entitlement> entitlements = getServerEntitlements(sid);

        return entitlements != null && entitlements.contains(ent);
    }

    /**
     * Filter the servers ids of the servers that have the passed feature from the passed server ids
     * @param sids list of Server ids
     * @param feat Feature to look for
     * @return the list of server ids which have the specified feature
     */
    @SuppressWarnings("unchecked")
    public static List<Long> filterServerIdsWithFeature(List<Long> sids, String feat) {
        SelectMode m = ModeFactory.getMode("General_queries", "filter_system_ids_with_feature");

        Map<String, Object> params = new HashMap<>();
        params.put("feature", feat);

        DataResult<Map<String, Long>> result = m.execute(params, sids);
        return result.stream().map(Map::values).flatMap(Collection::stream).collect(Collectors.toList());
    }

    /**
     * Used to test if the server has a specific feature.
     * We should almost always check for features with serverHasFeature instead.
     * @param sid Server id
     * @param feat Feature to look for
     * @return true if the server has the specified feature
     */
    public static boolean serverHasFeature(Long sid, String feat) {
        return !filterServerIdsWithFeature(Arrays.asList(sid), feat).isEmpty();
    }

    /**
     * Return <code>true</code> the given server has virtualization entitlements,
     * <code>false</code> otherwise.

     * @param sid Server ID to lookup.
     * @param org Org id of user performing this query.
     * @return <code>true</code> if the server has virtualization entitlements,
     *      <code>false</code> otherwise.
     */
    public static boolean serverHasVirtuaizationEntitlement(Long sid, Org org) {
        Server s = SystemManager.lookupByIdAndOrg(sid, org);
        return s.hasVirtualizationEntitlement();
    }

    /**
     * Return <code>true</code> the given server has bootstrap entitlement,
     * <code>false</code> otherwise.

     * @param sid Server ID to lookup.
     * @return <code>true</code> if the server has bootstrap entitlement,
     *      <code>false</code> otherwise.
     */
    public static boolean serverHasBootstrapEntitlement(Long sid) {
        Server s = ServerFactory.lookupById(sid);
        return s.hasEntitlement(EntitlementManager.BOOTSTRAP);
    }

    /**
     * Returns a count of systems without
     * a certain set of entitlements in a set of systems.
     *
     * @param user user making the request
     * @param setLabel label of the set
     * @param entitlements list of entitlement labels
     * @return number of systems in the set without those entitlements
     */
    public static int countSystemsInSetWithoutEntitlement(User user, String setLabel,
        List<String> entitlements) {
        SelectMode m = ModeFactory.getMode("System_queries",
                "count_systems_in_set_without_entitlement");

        Map params = new HashMap();
        params.put("user_id", user.getId());
        params.put("set_label", setLabel);
        DataResult dr = m.execute(params, entitlements);
        return ((Long)((HashMap)dr.get(0)).get("count")).intValue();
    }

    /**
     * Returns a count of systems without a certain feature in a set.
     *
     * @param user user making the request
     * @param setLabel label of the set
     * @param featureLabel label of the feature
     * @return number of systems in the set without the feature
     */
    public static int countSystemsInSetWithoutFeature(User user, String setLabel,
        String featureLabel) {
        SelectMode m = ModeFactory.getMode("System_queries",
                "count_systems_in_set_without_feature");

        Map params = new HashMap();
        params.put("user_id", user.getId());
        params.put("set_label", setLabel);
        params.put("feature_label", featureLabel);

        DataResult dr = makeDataResult(params, null, null, m);
        return ((Long)((HashMap)dr.get(0)).get("count")).intValue();
    }

    /**
     * Returns true if server has capability.
     * @param sid Server id
     * @param capability capability
     * @return true if server has capability
     */
    public static boolean clientCapable(Long sid, String capability) {
        SelectMode m = ModeFactory.getMode("System_queries", "lookup_capability");

        Map<String, Object> params = new HashMap<>();
        params.put("sid", sid);
        params.put("name", capability);

        DataResult dr = makeDataResult(params, params, null, m);
        return !dr.isEmpty();
    }

    /**
     * Returns a list of Servers which are compatible with the given server.
     * @param user User owner
     * @param server Server whose profiles we want.
     * @return  a list of Servers which are compatible with the given server.
     */
    public static List<Map<String, Object>> compatibleWithServer(User user, Server server) {
        return ServerFactory.compatibleWithServer(user, server);
    }

    /**
     * Subscribes the given server to the given channel.
     * @param user Current user
     * @param server Server to be subscribed
     * @param channel Channel to subscribe to.
     * @return the modified server if there were
     *           any changes modifications made
     *           to the Server during the call.
     *           Make sure the caller uses the
     *           returned server.
     */
    public static Server subscribeServerToChannel(User user,
            Server server, Channel channel) {
        return subscribeServerToChannel(user, server, channel, false);
    }

    /**
     * Subscribes the given server to the given channel.
     * @param user Current user
     * @param server Server to be subscribed
     * @param channel Channel to subscribe to.
     * @param flush flushes the hibernate session.
     * @return the modified server if there were
     *           any changes modifications made
     *           to the Server during the call.
     *           Make sure the caller uses the
     *           returned server.
     */
    public static Server subscribeServerToChannel(User user,
            Server server,
            Channel channel,
            boolean flush) {

        if (user != null && !ChannelManager.verifyChannelSubscribe(user, channel.getId())) {
            //Throw an exception with a nice error message so the user
            //knows what went wrong.
            LocalizationService ls = LocalizationService.getInstance();
            throw new PermissionException("User does not have permission to subscribe this server to this channel.",
                    ls.getMessage("permission.jsp.title.subscribechannel"),
                    ls.getMessage("permission.jsp.summary.subscribechannel"));
        }

        if (!verifyArchCompatibility(server, channel)) {
            throw new IncompatibleArchException(
                    server.getServerArch(), channel.getChannelArch());
        }

        log.debug("calling subscribe_server_to_channel");
        CallableMode m = ModeFactory.getCallableMode("Channel_queries",
                "subscribe_server_to_channel");

        Map<String, Object> in = new HashMap<>();
        in.put("server_id", server.getId());
        if (user != null) {
            in.put("user_id", user.getId());
        }
        else {
            in.put("user_id", null);
        }
        in.put("channel_id", channel.getId());

        m.execute(in, new HashMap<>());

        /*
         * This is f-ing hokey, but we need to be sure to refresh the
         * server object since
         * we modified it outside of hibernate :-/
         * This will update the server.channels set.
         */
        log.debug("returning with a flush? {}", flush);
        if (flush) {
            return HibernateFactory.reload(server);
        }
        HibernateFactory.getSession().refresh(server);
        return server;
    }

    /**
     * Returns true if the given server has a compatible architecture with the
     * given channel architecture. False if the server or channel is null or
     * they are not compatible.
     * @param server Server architecture to be verified.
     * @param channel Channel to check
     * @return true if compatible; false if null or not compatible.
     */
    public static boolean verifyArchCompatibility(Server server, Channel channel) {
        if (server == null || channel == null) {
            return false;
        }
        return channel.getChannelArch().isCompatible(server.getServerArch());
    }

    /**
     * Unsubscribe given server from the given channel.
     * @param user The user performing the operation
     * @param server The server to be unsubscribed
     * @param channel The channel to unsubscribe from
     */
    public static void unsubscribeServerFromChannel(User user, Server server,
            Channel channel) {
        unsubscribeServerFromChannel(user, server, channel, false);
    }

    /**
     * Unsubscribe given server from the given channel.
     * @param user The user performing the operation
     * @param sid The id of the server to be unsubscribed
     * @param cid The id of the channel from which the server will be unsubscribed
     */
    public static void unsubscribeServerFromChannel(User user, Long sid,
            Long cid) {
        if (ChannelManager.verifyChannelSubscribe(user, cid)) {
            unsubscribeServerFromChannel(sid, cid);
        }
    }

    /**
     * Unsubscribe given server from the given channel.
     * @param user The user performing the operation
     * @param server The server to be unsubscribed
     * @param channel The channel to unsubscribe from
     * @param flush flushes the hibernate session. Make sure you
     *              reload the server and channel after  method call
     *              if you set this to true..
     */
    public static void unsubscribeServerFromChannel(User user, Server server,
            Channel channel, boolean flush) {
        if (!isAvailableToUser(user, server.getId())) {
            //Throw an exception with a nice error message so the user
            //knows what went wrong.
            LocalizationService ls = LocalizationService.getInstance();
            throw new PermissionException("User does not have permission to unsubscribe this server from this channel.",
                    ls.getMessage("permission.jsp.title.subscribechannel"),
                    ls.getMessage("permission.jsp.summary.subscribechannel"));
        }

        unsubscribeServerFromChannel(server, channel, flush);
    }

    /**
     * Unsubscribe given server from the given channel. If you use this method,
     * YOU BETTER KNOW WHAT YOU'RE DOING!!! (Use the version that takes a user as well if
     * you're unsure. better safe than sorry).
     * @param server server to be unsubscribed
     * @param channel the channel to unsubscribe from
     * @return the modified server if there were
     *           any changes modifications made
     *           to the Server during the call.
     *           Make sure the caller uses the
     *           returned server.
     */
    public static Server unsubscribeServerFromChannel(Server server,
            Channel channel) {
        return unsubscribeServerFromChannel(server, channel, false);
    }

    /**
     * Unsubscribe given server from the given channel. If you use this method,
     * YOU BETTER KNOW WHAT YOU'RE DOING!!! (Use the version that takes a user as well if
     * you're unsure. better safe than sorry).
     * @param server server to be unsubscribed
     * @param channel the channel to unsubscribe from
     * @param flush flushes the hibernate session. Make sure you
     *              reload the server and channel after  method call
     *              if you set this to true..
     * @return the modified server if there were
     *           any changes modifications made
     *           to the Server during the call.
     *           Make sure the caller uses the
     *           returned server.
     */
    public static Server unsubscribeServerFromChannel(Server server,
            Channel channel,
            boolean flush) {
        if (channel == null) {
            //nothing to do ;)
            return server;
        }

        unsubscribeServerFromChannel(server.getId(), channel.getId());

        /*
         * This is f-ing hokey, but we need to be sure to refresh the
         * server object since we modified it outside of hibernate :-/
         * This will update the server.channels set.
         */
        if (flush) {
            return HibernateFactory.reload(server);
        }
        HibernateFactory.getSession().refresh(server);
        return server;
    }

    /**
     * Unsubscribes a server from a channel without any check. Please use other
     * overloaded versions of this method if unsure.
     * @param sid the server id
     * @param cid the channel id
     */
    public static void unsubscribeServerFromChannel(Long sid, Long cid) {
        CallableMode m = ModeFactory.getCallableMode("Channel_queries",
            "unsubscribe_server_from_channel");
        Map<String, Object> params = new HashMap<>();
        params.put("server_id", sid);
        params.put("channel_id", cid);
        m.execute(params, new HashMap<>());
    }

    /**
     * Deactivates the given proxy.
     * Make sure you either reload  the server after this call,,
     * or use the returned Server object
     * @param server ProxyServer to be deactivated.
     * @return deproxified server.
     */
    public static Server deactivateProxy(Server server) {
        ServerFactory.deproxify(server);

        // Unsubscribe only if we are configured to automatically re-subscribe again,
        // see the activateProxy() method
        if (Config.get().getBoolean(ConfigDefaults.WEB_SUBSCRIBE_PROXY_CHANNEL)) {
            Set<Channel> channels = server.getChannels();
            for (Channel c : channels) {
                ChannelFamily cf = c.getChannelFamily();
                if (cf.getLabel().equals("SMP")) {
                    SystemManager.unsubscribeServerFromChannel(server, c);
                }
            }
        }

        return server;
    }

    private static int executeWriteMode(String catalog, String mode,
            Map<String, Object> params) {
        WriteMode m = ModeFactory.getWriteMode(catalog, mode);
        return m.executeUpdate(params);
    }

    /**
     * Creates the client certificate (systemid) file for the given Server.
     * @param server Server whose client certificate is sought.
     * @return the client certificate (systemid) file for the given Server.
     * @throws InstantiationException thrown if error occurs creating the
     * client certificate.
     */
    public static ClientCertificate createClientCertificate(Server server)
            throws InstantiationException {

        ClientCertificate cert = new ClientCertificate();
        // add members to this cert
        User user = UserManager.findResponsibleUser(server.getOrg(), RoleFactory.ORG_ADMIN);
        cert.addMember("username", user.getLogin());
        cert.addMember("os_release", server.getRelease());
        cert.addMember("operating_system", server.getOs());
        cert.addMember("architecture",  server.getServerArch().getLabel());
        cert.addMember("system_id", "ID-" + server.getId().toString());
        cert.addMember("type", "REAL");
        String[] fields = {"system_id", "os_release", "operating_system",
                "architecture", "username", "type"};
        cert.addMember("fields", fields);

        try {
            //Could throw InvalidCertificateException in any fields are invalid
            cert.addMember("checksum", cert.genSignature(server.getSecret()));
        }
        catch (InvalidCertificateException e) {
            throw new InstantiationException("Couldn't generate signature");
        }

        return cert;
    }

    /**
     * Store the server back to the db
     * @param serverIn The server to save
     */
    public static void storeServer(Server serverIn) {
        ServerFactory.save(serverIn);
    }

    /**
     * Activates the given proxy for the given version.
     * @param server proxy server to activate.
     * @param version Proxy version.
     * @throws ProxySystemIsSatelliteException thrown if system is a satellite.
     * @throws InvalidProxyVersionException thrown if version is invalid.
     */
    public static void activateProxy(Server server, String version)
            throws ProxySystemIsSatelliteException, InvalidProxyVersionException {

        if (server.isMgrServer()) {
            throw new ProxySystemIsSatelliteException();
        }

        ProxyInfo info = new ProxyInfo();
        info.setServer(server);
        info.setVersion(PackageEvrFactory.lookupOrCreatePackageEvr(
                null, version, "1", server.getPackageType()));
        server.setProxyInfo(info);
        if (Config.get().getBoolean(ConfigDefaults.WEB_SUBSCRIBE_PROXY_CHANNEL)) {
            Channel proxyChannel = ChannelManager.getProxyChannelByVersion(
                    version, server);
            if (proxyChannel != null) {
                subscribeServerToChannel(null, server, proxyChannel);
            }
        }
    }

    private Supplier<RhnRuntimeException> raiseAndLog(String message) {
        log.error(message);
        return () -> new RhnRuntimeException(message);
    }

    private Server getOrCreateProxySystem(User creator, String fqdn, Integer port) {
        Optional<Server> existing = ServerFactory.findByFqdn(fqdn);
        if (existing.isPresent()) {
            Server server = existing.get();
            if (!(server.hasEntitlement(EntitlementManager.FOREIGN) ||
                    server.hasEntitlement(EntitlementManager.SALT))) {
                throw new SystemsExistException(List.of(server.getId()));
            }
            // The SSH key is going to change remove it from the known hosts
            removeSaltSSHKnownHosts(server);
            ProxyInfo info = server.getProxyInfo();
            if (info == null) {
                info = new ProxyInfo();
                info.setServer(server);
                server.setProxyInfo(info);
            }
            info.setSshPort(port);
            return server;
        }
        Server server = ServerFactory.createServer();
        server.setName(fqdn);
        server.setHostname(fqdn);
        server.getFqdns().add(new ServerFQDN(server, fqdn));
        server.setOrg(creator.getOrg());
        server.setCreator(creator);

        String uniqueId = createUniqueId(List.of(fqdn));
        server.setDigitalServerId(uniqueId);
        server.setMachineId(uniqueId);
        server.setOs("(unknown)");
        server.setRelease("(unknown)");
        server.setSecret(RandomStringUtils.randomAlphanumeric(64));
        server.setAutoUpdate("N");
        server.setContactMethod(ServerFactory.findContactMethodByLabel("default"));
        server.setLastBoot(System.currentTimeMillis() / 1000);
        server.setServerArch(ServerFactory.lookupServerArchByLabel("x86_64-redhat-linux"));
        server.updateServerInfo();
        ServerFactory.save(server);

        ProxyInfo info = new ProxyInfo();
        info.setServer(server);
        info.setSshPort(port);
        server.setProxyInfo(info);

        systemEntitlementManager.setBaseEntitlement(server, EntitlementManager.FOREIGN);
        return server;
    }

    /**
     * Create and provide proxy container configuration.
     *
     * @param user the current user
     * @param proxyName  the FQDN of the proxy
     * @param proxyPort  the SSH port the proxy listens on
     * @param server the FQDN of the server the proxy uses
     * @param maxCache the maximum memory cache size
     * @param email the email of proxy admin
     * @param rootCA root CA used to sign the SSL certificate in PEM format
     * @param intermediateCAs intermediate CAs used to sign the SSL certificate in PEM format
     * @param proxyCertKey proxy CRT and key pair
     * @param caPair the CA certificate and key used to sign the certificate to generate.
     *               Can be omitted if proxyCertKey is not provided
     * @param caPassword the CA private key password.
     *               Can be omitted if proxyCertKey is not provided
     * @param certData the data needed to generate the new proxy SSL certificate.
     *               Can be omitted if proxyCertKey is not provided
     * @return the configuration file
     */
    public byte[] createProxyContainerConfig(User user, String proxyName, Integer proxyPort, String server,
                                             Long maxCache, String email,
                                             String rootCA, List<String> intermediateCAs,
                                             SSLCertPair proxyCertKey,
                                             SSLCertPair caPair, String caPassword, SSLCertData certData)
            throws IOException, InstantiationException, SSLCertGenerationException {
        /* Prepare the archive streams where the config files will be stored into */
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        BufferedOutputStream bufOut = new BufferedOutputStream(bytesOut);
        GzipCompressorOutputStream gzOut = new GzipCompressorOutputStream(bufOut);
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(gzOut);

        /**
         * config.yaml
         */
        Map<String, Object> config = new HashMap<>();

        config.put("server", server);
        config.put("max_cache_size_mb", maxCache);
        config.put("email", email);
        config.put("server_version", ConfigDefaults.get().getProductVersion());
        config.put("proxy_fqdn", proxyName);

        SSLCertPair proxyPair = proxyCertKey;
        String rootCaCert = rootCA;
        if (proxyCertKey == null || !proxyCertKey.isComplete()) {
            proxyPair = new SSLCertManager().generateCertificate(caPair, caPassword, certData);
            rootCaCert = caPair.getCertificate();
        }
        config.put("ca_crt", rootCaCert);

        addTarEntry(tarOut, "config.yaml", YamlHelper.INSTANCE.dumpPlain(config).getBytes(), 0644);
        /**
         * config.yaml
         */

        /**
         * httpd.yaml
         */
        Map<String, Object> httpdRootConfig = new HashMap<>();
        Map<String, Object> httpdConfig = new HashMap<>();

        Server proxySystem = getOrCreateProxySystem(user, proxyName, proxyPort);
        ClientCertificate cert = SystemManager.createClientCertificate(proxySystem);
        httpdConfig.put("system_id", cert.asXml());

        // Check the SSL files using mgr-ssl-cert-setup
        try {
            String certificate = saltApi.checkSSLCert(rootCaCert, proxyPair,
                    intermediateCAs != null ? intermediateCAs : Collections.emptyList());
            httpdConfig.put("server_crt", certificate);
            httpdConfig.put("server_key", proxyPair.getKey());
        }
        catch (IllegalArgumentException err) {
            throw new RhnRuntimeException("Certificate check failure: " + err.getMessage());
        }

        httpdRootConfig.put("httpd", httpdConfig);
        addTarEntry(tarOut, "httpd.yaml", YamlHelper.INSTANCE.dumpPlain(httpdRootConfig).getBytes(), 0600);
        /**
         * httpd.yaml
         */

        /**
         * ssh.yaml
         */
        Map<String, Object> sshRootConfig = new HashMap<>();
        Map<String, Object> sshConfig = new HashMap<>();

        MgrUtilRunner.SshKeygenResult result = saltApi.generateSSHKey(SaltSSHService.SSH_KEY_PATH,
                        SaltSSHService.SUMA_SSH_PUB_KEY)
                .orElseThrow(raiseAndLog("Could not generate salt-ssh public key."));
        if (!(result.getReturnCode() == 0 || result.getReturnCode() == -1)) {
            throw raiseAndLog("Generating salt-ssh public key failed: " + result.getStderr()).get();
        }
        sshConfig.put("server_ssh_key_pub", result.getPublicKey());

        // Create the proxy SSH keys
        result = saltApi.generateSSHKey(null, null)
                .orElseThrow(raiseAndLog("Could not generate proxy salt-ssh SSH keys."));
        if (!(result.getReturnCode() == 0 || result.getReturnCode() == -1)) {
            throw raiseAndLog("Generating proxy salt-ssh SSH keys failed: " + result.getStderr()).get();
        }
        sshConfig.put("server_ssh_push", result.getKey());
        sshConfig.put("server_ssh_push_pub", result.getPublicKey());

        sshRootConfig.put("ssh", sshConfig);
        addTarEntry(tarOut, "ssh.yaml", YamlHelper.INSTANCE.dumpPlain(sshRootConfig).getBytes(), 0600);
        /**
         * ssh.yaml
         */

        tarOut.finish();
        tarOut.close();

        return bytesOut.toByteArray();
    }

    private void addTarEntry(TarArchiveOutputStream tarOut, String name, byte[] data, int mode) throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry(name);
        entry.setSize(data.length);
        entry.setMode(mode);
        tarOut.putArchiveEntry(entry);
        tarOut.write(data);
        tarOut.closeArchiveEntry();
    }

    /**
     * Returns a DataResult containing the systems subscribed to a particular channel.
     *      but returns a DataResult of SystemOverview objects instead of maps
     * @param channel The channel in question
     * @param user The user making the call
     * @return Returns a DataResult of maps containing the ids and names of systems
     * subscribed to a channel.
     */
    public static DataResult<Map<String, Object>> systemsSubscribedToChannelDto(
            Channel channel, User user) {
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("cid", channel.getId());
        params.put("org_id", user.getOrg().getId());
        SelectMode m = ModeFactory.getMode("System_queries",
                "systems_subscribed_to_channel", SystemOverview.class);
        return m.execute(params);
    }

    /**
     * Returns the number of systems subscribed to the given channel.
     *
     * @param channelId identifies the channel
     * @param user      user making the request
     * @return number of systems subscribed to the channel
     */
    public static int countSystemsSubscribedToChannel(Long channelId, User user) {
        Map<String, Long> params = new HashMap<>(2);
        params.put("user_id", user.getId());
        params.put("org_id", user.getOrg().getId());
        params.put("cid", channelId);

        SelectMode m = ModeFactory.getMode("System_queries",
                "count_systems_subscribed_to_channel");
        DataResult<Map<String, Object>> dr = makeDataResult(params, params, null, m);

        Map<String, Object> result = dr.get(0);
        Long count = (Long) result.get("count");
        return count.intValue();
    }

    /**
     * Returns a DataResult containing the systems subscribed to a particular channel.
     * @param channel The channel in question
     * @param user The user making the call
     * @return Returns a DataResult of maps containing the ids and names of systems
     * subscribed to a channel.
     */
    public static DataResult<Map<String, Object>> systemsSubscribedToChannel(
            Channel channel, User user) {
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("cid", channel.getId());
        params.put("org_id", user.getOrg().getId());

        SelectMode m = ModeFactory.getMode("System_queries",
                "systems_subscribed_to_channel", Map.class);
        return m.execute(params);
    }

    /**
     * Return the list of systems subscribed to the given channel in the current set.
     * Each entry in the result will be of type EssentialServerDto as per the query.
     *
     * @param cid Channel
     * @param user User requesting the list
     * @param setLabel Set label
     * @return List of systems
     */
    public static DataResult<EssentialServerDto> systemsSubscribedToChannelInSet(
            Long cid, User user, String setLabel) {
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("cid", cid);
        params.put("org_id", user.getOrg().getId());
        params.put("set_label", setLabel);

        SelectMode m = ModeFactory.getMode(
                "System_queries", "systems_subscribed_to_channel_in_set");
        return m.execute(params);
    }

    /**
     * Returns a DataResult containing maps representing the channels a particular system
     * is subscribed to.
     * @param server The server in question.
     * @return Returns a DataResult of maps representing the channels a particular system
     * is subscribed to.
     */
    public static DataResult<Map<String, Object>> channelsForServer(Server server) {
        Map<String, Object> params = new HashMap<>();
        params.put("sid", server.getId());
        SelectMode m = ModeFactory.getMode("Channel_queries", "system_channels", Map.class);
        return m.execute(params);
    }

    /**
     * Unlocks a server if the user has permissions on the server
     * @param user User who is attempting to unlock the server
     * @param server Server that is attempting to be unlocked
     */
    public static void unlockServer(User user, Server server) {
        if (!isAvailableToUser(user, server.getId())) {
           throw getNoServerException(server.getId(), user.getId());
        }
        HibernateFactory.getSession().delete(server.getLock());
        server.setLock(null);
    }

    private static LookupException getNoServerException(Long sid, Long uid) {
        LocalizationService ls = LocalizationService.getInstance();
        return new LookupException("Could not find server " + sid + " for user " + uid,
                ls.getMessage("lookup.jsp.title.system"),
                ls.getMessage("lookup.jsp.reason1.system"),
                ls.getMessage("lookup.jsp.reason2.system"));
    }

    /**
     * Unlocks a server, no permission will be checked here
     * @param server Server that is attempting to be unlocked
     */
    public static void unlockServer(Server server) {
        HibernateFactory.getSession().delete(server.getLock());
        server.setLock(null);
    }

    /**
     * Locks a server if the user has permissions on the server
     * @param locker User who is attempting to lock the server
     * @param server Server that is attempting to be locked
     * @param reason String representing the reason the server was locked
     */
    public static void lockServer(User locker, Server server, String reason) {
        if (!isAvailableToUser(locker, server.getId())) {
            throw getNoServerException(server.getId(), locker.getId());
        }
        ServerLock sl = new ServerLock(locker,
                server,
                reason);

        server.setLock(sl);
    }

    /**
     * Checks if the user has permissions to see a set of Servers, given their server ids
     * @param userId the user id of the user being checked
     * @param serverIds the ids of the Servers being checked
     * @return true if the user can see the servers, false otherwise
     */
    public static boolean areSystemsAvailableToUser(Long userId, List<Long> serverIds) {
        SelectMode m = ModeFactory.getMode("System_queries", "filter_systems_available_to_user");
        Map<String, Object> params = new HashMap<>();
        params.put("uid", userId);
        return m.execute(params, serverIds).size() == serverIds.size();
    }

    /**
     * Checks if the user has permissions to see the Server
     * @param user User being checked
     * @param sid ID of the Server being checked
     * @return true if the user can see the server, false otherwise
     */
    public static boolean isAvailableToUser(User user, Long sid) {
        SelectMode m = ModeFactory.getMode("System_queries", "is_available_to_user");
        Map<String, Object> params = new HashMap<>();
        params.put("uid", user.getId());
        params.put("sid", sid);
        return m.execute(params).size() >= 1;
    }

    /**
     * Checks if the System is a virtual host
     * @param oid id of the Org that the server is in
     * @param sid ID of the Server being checked
     * @return true if the system is a virtual host, false otherwise
     */
    public static boolean isVirtualHost(Long oid, Long sid) {
        SelectMode m = ModeFactory.getMode("System_queries", "is_virtual_host_in_org");
        Map<String, Object> params = new HashMap<>();
        params.put("oid", oid);
        params.put("sid", sid);
        DataResult result = m.execute(params);
        return !result.isEmpty();
    }

    /**
     * Checks if the user has permissions to see the Server
     * @param user User being checked
     * @param sid ID of the Server being checked
     */
    public static void ensureAvailableToUser(User user, Long sid) {
        if (!isAvailableToUser(user, sid)) {
            throw getNoServerException(sid, user.getId());
        }
    }

    /**
     * Return systems in the current set without a base channel.
     * @param user User requesting the query.
     * @return List of systems.
     */
    public static DataResult<EssentialServerDto> systemsWithoutBaseChannelsInSet(
            User user) {
        SelectMode m = ModeFactory.getMode("System_queries",
                "systems_in_set_with_no_base_channel");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        return m.execute(params);
    }


    /**
     * Validates that the proposed number of virtual CPUs is valid for the
     * given virtual instance.
     *
     * @param guestId ID of a virtual instance.
     * @param proposedVcpuSetting Requested number of virtual CPUs for the guest.
     * @return ValidatorResult containing both error and warning messages.
     */
    public static ValidatorResult validateVcpuSetting(Long guestId,
            int proposedVcpuSetting) {
        ValidatorResult result = new ValidatorResult();

        VirtualInstanceFactory viFactory = VirtualInstanceFactory.getInstance();
        VirtualInstance guest = viFactory.lookupById(guestId);
        Server host = guest.getHostSystem();

        // Technically the limit is 32 for 32-bit architectures and 64 for 64-bit,
        // but the kernel is currently set to only accept 32 in either case. This may
        // need to change down the road.
        if (0 > proposedVcpuSetting || proposedVcpuSetting > 32) {
            result.addError(new ValidatorError(
                    "systems.details.virt.vcpu.limit.msg",
                    new Object [] {"32", guest.getName()}));
        }

        if (result.getErrors().isEmpty()) {
            // Warn the user if the proposed vCPUs exceeds the physical CPUs on the
            // host:
            CPU hostCpu = host.getCpu();
            if (hostCpu != null && hostCpu.getNrCPU() != null) {
                if (proposedVcpuSetting > hostCpu.getNrCPU().intValue()) {
                    result.addWarning(new ValidatorWarning(
                            "systems.details.virt.vcpu.exceeds.host.cpus",
                            new Object [] {host.getCpu().getNrCPU(), guest.getName()}));
                }
            }

            // Warn the user if the proposed vCPUs is an increase for this guest.
            // If the new value exceeds the setting the guest was started with, a
            // reboot will be required for the setting to take effect.
            VirtualInstanceState running = VirtualInstanceFactory.getInstance().
                    getRunningState();
            if (guest.getState() != null &&
                    guest.getState().getId().equals(running.getId())) {
                Integer currentGuestCpus = guest.getNumberOfCPUs();
                if (currentGuestCpus != null && proposedVcpuSetting >
                        currentGuestCpus) {
                    result.addWarning(new ValidatorWarning(
                            "systems.details.virt.vcpu.increase.warning",
                            new Object [] {proposedVcpuSetting,
                                guest.getName()}));
                }
            }
        }

        return result;
    }

    /**
     * Validates the amount requested amount of memory can be allocated to each
     * of the guest systems in the list. Assumes all guests are on the same host.
     *
     * @param guestIds List of longs representing IDs of virtual instances.
     * @param proposedMemory Requested amount of memory for each guest. (in Mb)
     * @return ValidatorResult containing both error and warning messages.
     */
    public static ValidatorResult validateGuestMemorySetting(List<Long> guestIds,
            int proposedMemory) {
        ValidatorResult result = new ValidatorResult();
        VirtualInstanceFactory viFactory = VirtualInstanceFactory.getInstance();

        if (guestIds.isEmpty()) {
            return result;
        }

        // Grab the host from the first guest in the list:
        Long firstGuestId = guestIds.get(0);
        Server host = (viFactory.lookupById(firstGuestId)).
                getHostSystem();

        VirtualInstanceState running = VirtualInstanceFactory.getInstance().
                getRunningState();

        log.debug("Adding guest memory:");
        List<ValidatorWarning> warnings = new LinkedList<>();
        for (VirtualInstance guest : host.getGuests()) {
            // if the guest we're examining isn't running, don't count it's memory
            // when determining if the host has enough free:
            if (guest.getState() != null &&
                    guest.getState().getId().equals(running.getId())) {

                if (guest.getTotalMemory() != null) {
                    log.debug("   {} = {}MB", guest.getName(), guest.getTotalMemory() / 1024);

                    if (guestIds.contains(guest.getId())) {
                        // Warn the user that a change to max memory will require a reboot
                        // for the settings to take effect:
                        warnings.add(new ValidatorWarning(
                                "systems.details.virt.memory.warning",
                                new Object[]{guest.getName()}));
                    }
                }
                else {
                    // Not much we can do for calculations if we don't have reliable data,
                    // continue on to other guests:
                    log.warn("No total memory set for guest: {}", guest.getName());
                }
            }
        }

        // Warn the user to verify the system has enough free memory:
        // NOTE: Once upon a time we tried to do this automagically but the
        // code was removed due to uncertainty in terms of rebooting guests
        // if increasing past the allocation they were booted with, missing
        // hardware refreshes for the host, etc.
        warnings.add(new ValidatorWarning("systems.details.virt.memory.check.host"));

        if (!warnings.isEmpty()) {
            for (ValidatorWarning warningIn : warnings) {
                result.addWarning(warningIn);
            }
        }

        return result;
    }

    /**
     * Return the system names and IDs that are selected in the SSM for the given user,
     * which also have been subscribed to the given channel.
     *
     * @param user User.
     * @param channelId Channel ID.
     * @return List of maps containing the system name and ID.
     */
    public static List<Map<String, Object>> getSsmSystemsSubscribedToChannel(User user,
            Long channelId) {
        SelectMode m = ModeFactory.getMode("System_queries",
                "systems_in_set_with_channel");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("channel_id", channelId);
        return m.execute(params);
    }

    /**
     * lists  systems with the given installed NVR
     * @param user the user doing the search
     * @param name the name of the package
     * @param version package version
     * @param release package release
     * @return  list of systemOverview objects
     */
    public static List<SystemOverview> listSystemsWithPackage(User user,
            String name, String version, String release) {
        SelectMode m = ModeFactory.getMode("System_queries",
                "systems_with_package_nvr");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("org_id", user.getOrg().getId());
        params.put("version", version);
        params.put("release", release);
        params.put("name", name);
        DataResult<SystemOverview> toReturn = m.execute(params);
        toReturn.elaborate();
        return toReturn;
    }

    /**
     * lists  systems with the given installed package id
     * @param user the user doing the search
     * @param id the id of the package
     * @return  list of systemOverview objects
     */
    public static DataResult<SystemOverview> listSystemsWithPackage(User user, Long id) {
        SelectMode m = ModeFactory.getMode("System_queries",
                "systems_with_package");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("org_id", user.getOrg().getId());
        params.put("pid", id);
        return m.execute(params);
    }

    /**
     * lists systems that can upgrade to the package id
     * @param user the user doing the search
     * @param id the id of the package
     * @return  list of systemOverview objects
     */
    public static DataResult<SystemOverview> listPotentialSystemsForPackage(User user,
            Long id) {
        SelectMode m = ModeFactory.getMode("System_queries",
                "potential_systems_for_package");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("org_id", user.getOrg().getId());
        params.put("pid", id);
        return m.execute(params);
    }

    /**
     * lists systems with the given needed/upgrade package id
     * @param user the user doing the search
     * @param id the id of the package
     * @return  list of systemOverview objects
     */
    public static List<SystemOverview> listSystemsWithNeededPackage(User user, Long id) {
        SelectMode m = ModeFactory.getMode("System_queries",
                "systems_with_needed_package");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("org_id", user.getOrg().getId());
        params.put("pid", id);
        //toReturn.elaborate();
        return m.execute(params);
    }

    /**
     * List all virtual hosts for a user
     * @param user the user in question
     * @return list of SystemOverview objects
     */
    public static List<SystemOverview> listVirtualHosts(User user) {
        SelectMode m = ModeFactory.getMode("System_queries",
                "virtual_hosts_for_user");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        DataResult<SystemOverview> toReturn = m.execute(params);
        toReturn.elaborate();
        return toReturn;
    }

    /**
     * List all systems with the given entitlement
     *
     * @param user the user doing the search
     * @param entitlement the entitlement to match
     * @return list of SystemOverview objects
     */
    public static List<SystemOverview> listSystemsWithEntitlement(User user, Entitlement entitlement) {
        SelectMode m = ModeFactory.getMode("System_queries",
                "systems_with_entitlement");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("entitlement_label", entitlement.getLabel());
        DataResult<SystemOverview> toReturn = m.execute(params);
        toReturn.elaborate();
        return toReturn;
    }

    /**
     * Returns whether there are traditional systems registered
     *
     * @param user The current user
     * @return true if there is at least one system registered with Enterprise entitlement
     */
    public static boolean hasTraditionalSystems(User user) {
        SelectMode m = ModeFactory.getMode("System_queries", "count_systems_with_entitlement");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("entitlement_label", EntitlementManager.ENTERPRISE_ENTITLED);
        DataResult<Map<String, Object>> dr = makeDataResult(params, null, null, m);
        return ((Long) dr.get(0).get("count")).intValue() > 0;
    }

    /**
     * Returns the number of systems subscribed to the channel that are
     * <strong>not</strong> in the given org.
     *
     * @param orgId identifies the filter org
     * @param cid identifies the channel
     * @return count of systems
     */
    public static int countSubscribedToChannelWithoutOrg(Long orgId, Long cid) {
        SelectMode m = ModeFactory.getMode("System_queries",
                "count_systems_subscribed_to_channel_not_in_org");
        Map<String, Object> params = new HashMap<>();
        params.put("org_id", orgId);
        params.put("cid", cid);

        DataResult<Map<String, Object>> dr = m.execute(params);
        Map<String, Object> result = dr.get(0);
        Long count = (Long) result.get("count");

        return count.intValue();
    }

    /**
     * List of servers subscribed to shared channels via org trust.
     * @param orgA The first org in the trust.
     * @param orgB The second org in the trust.
     * @return (system.id, system.org_id, system.name)
     */
    public static DataResult<Map<String, Object>>
            subscribedInOrgTrust(long orgA, long orgB) {
        SelectMode m =
                ModeFactory.getMode("System_queries",
                        "systems_subscribed_by_orgtrust");
        Map<String, Object> params = new HashMap<>();
        params.put("orgA", orgA);
        params.put("orgB", orgB);
        return m.execute(params);
    }

    /**
     * List of distinct servers subscribed to shared channels via org trust.
     * @param orgA The first org in the trust.
     * @param orgB The second org in the trust.
     * @return (system.id)
     */
    public static DataResult<Map<String, Object>> sidsInOrgTrust(long orgA, long orgB) {
        SelectMode m =
                ModeFactory.getMode("System_queries",
                        "sids_subscribed_by_orgtrust");
        Map<String, Object> params = new HashMap<>();
        params.put("orgA", orgA);
        params.put("orgB", orgB);
        return m.execute(params);
    }

    /**
     * gets the number of systems subscribed to a channel
     * @param user the user checking
     * @param cid the channel id
     * @return list of systems
     */
    public static Long subscribedToChannelSize(User user, Long cid) {
        SelectMode m = ModeFactory.getMode("System_queries",
                "systems_subscribed_to_channel_size");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("org_id", user.getOrg().getId());
        params.put("cid", cid);
        DataResult<Map<String, Object>> toReturn = m.execute(params);
        return (Long) toReturn.get(0).get("count");

    }

    /**
     * List all virtual hosts for a user
     * @param user the user in question
     * @return list of SystemOverview objects
     */
    public static DataResult<CustomDataKeyOverview> listDataKeys(User user) {
        SelectMode m = ModeFactory.getMode("System_queries",
                "custom_vals", CustomDataKeyOverview.class);
        Map<String, Object> params = new HashMap<>();
        params.put("uid", user.getId());
        params.put("org_id", user.getOrg().getId());
        return m.execute(params);
    }
    /**
     * Looks up a hardware device by the hardware device id
     * @param hwId the hardware device id
     * @return the HardwareDeviceDto
     */
    public static HardwareDeviceDto getHardwareDeviceById(Long hwId) {
        HardwareDeviceDto hwDto = null;
        SelectMode m = ModeFactory.getMode("System_queries", "hardware_device_by_id");
        Map<String, Object> params = new HashMap<>();
        params.put("hw_id", hwId);
        DataResult<HardwareDeviceDto> dr = m.execute(params);
        if (dr != null && !dr.isEmpty()) {
            hwDto = dr.get(0);
        }
        return hwDto;
    }

    /**
     * Returns a mapping of servers in the SSM to the user-selected packages to remove
     * that actually exist on those servers.
     *
     * @param user            identifies the user making the request
     * @param packageSetLabel identifies the RhnSet used to store the packages selected
     *                        by the user (this is needed for the query). This must be
     *                        established by the caller prior to calling this method
     * @param shortened       whether or not to include the full elaborator, or a shortened
     *                        one that is much much faster, but doesn't provide a displayed
     *                        string for the package (only the id combo)
     * @return description of server information as well as a list of relevant packages
     */
    public static DataResult<Map<String, Object>> ssmSystemPackagesToRemove(User user,
            String packageSetLabel,
            boolean shortened) {
        SelectMode m;
        if (shortened) {
            m = ModeFactory.getMode("System_queries",
                    "system_set_remove_or_verify_packages_conf_short");
        }
        else {
            m = ModeFactory.getMode("System_queries",
                    "system_set_remove_or_verify_packages_conf");
        }

        Map<String, Object> params = new HashMap<>(3);
        params.put("user_id", user.getId());
        params.put("set_label", RhnSetDecl.SYSTEMS.getLabel());
        params.put("package_set_label", packageSetLabel);

        return makeDataResult(params, params, null, m);
    }

    /**
     * Returns a mapping of servers in the SSM to user-selected packages to upgrade
     * that actually exist on those servers
     *
     * @param user            identifies the user making the request
     * @param packageSetLabel identifies the RhnSet used to store the packages selected
     *                        by the user (this is needed for the query). This must be
     *                        established by the caller prior to calling this method
     * @return description of server information as well as a list of all relevant packages
     */
    public static DataResult ssmSystemPackagesToUpgrade(User user,
            String packageSetLabel) {

        SelectMode m =
                ModeFactory.getMode("System_queries", "ssm_package_upgrades_conf");

        Map<String, Object> params = new HashMap<>(3);
        params.put("user_id", user.getId());
        params.put("set_label", RhnSetDecl.SYSTEMS.getLabel());
        params.put("package_set_label", packageSetLabel);

        return makeDataResult(params, params, null, m);
    }

    /**
     * Deletes the indicates note, assuming the user has the proper permissions to the
     * server.
     *
     * @param user     user making the request
     * @param serverId identifies server the note resides on
     * @param noteId   identifies the note being deleted
     */
    public static void deleteNote(User user, Long serverId, Long noteId) {
        Server server = lookupByIdAndUser(serverId, user);

        Session session = HibernateFactory.getSession();
        Note doomed = session.get(Note.class, noteId);

        boolean deletedOnServer = server.getNotes().remove(doomed);
        if (deletedOnServer) {
            session.delete(doomed);
        }
    }

    /**
     * Deletes all notes on the given server, assuming the user has the proper permissions
     * to the server.
     *
     * @param user     user making the request
     * @param serverId identifies the server on which to delete its notes
     */
    public static void deleteNotes(User user, Long serverId) {
        Server server = lookupByIdAndUser(serverId, user);

        Session session = HibernateFactory.getSession();
        for (Object doomed : server.getNotes()) {
            session.delete(doomed);
        }

        server.getNotes().clear();
    }

    /**
     * Is the package with nameId, archId, and evrId available in the
     *  provided server's subscribed channels
     * @param server the server
     * @param nameId the name id
     * @param archId the arch id
     * @param evrId the evr id
     * @return true if available, false otherwise
     */
    public static boolean hasPackageAvailable(Server server, Long nameId,
            Long archId, Long evrId) {
        Map<String, Object> params = new HashMap<>();
        params.put("server_id", server.getId());
        params.put("eid", evrId);
        params.put("nid", nameId);

        String mode = "has_package_available";
        if (archId == null) {
            mode = "has_package_available_no_arch";
        }
        else {
            params.put("aid", archId);
        }
        SelectMode m =
                ModeFactory.getMode("System_queries", mode);
        DataResult toReturn = m.execute(params);
        return toReturn.size() > 0;
    }

    /**
     * Gets the list of proxies that the given system connects
     * through in order to reach the server.
     * @param sid The id of the server in question
     * @return Returns a list of ServerPath objects.
     */
    public static DataResult<ServerPath> getConnectionPath(Long sid) {
        SelectMode m = ModeFactory.getMode("System_queries", "proxy_path_for_server");
        Map<String, Object> params = new HashMap<>();
        params.put("sid", sid);
        return m.execute(params);
    }

    /**
     * List all of the installed packages with the given name
     * @param packageName the package name
     * @param server the server
     * @return list of maps with name_id, evr_id and arch_id
     */
    public static List<Map<String, Long>> listInstalledPackage(
            String packageName, Server server) {
        SelectMode m = ModeFactory.getMode("System_queries",
                "list_installed_packages_for_name");
        Map<String, Object> params = new HashMap<>();
        params.put("sid", server.getId());
        params.put("name", packageName);
        return m.execute(params);
    }

    /**
     * returns a List proxies available in the given org
     * @param org needed for org information
     * @return list of proxies for org
     */
    public static DataResult<OrgProxyServer> listProxies(Org org) {
        DataResult<OrgProxyServer> retval = null;
        SelectMode mode = ModeFactory.getMode("System_queries",
                "org_proxy_servers");
        Map<String, Object> params = new HashMap<>();
        params.put("org_id", org.getId());
        retval = mode.execute(params);
        return retval;
    }

    /**
     * list systems that can be subscribed to a particular child channel
     * @param user the user
     * @param chan the child channle
     * @return list of SystemOverview objects
     */
    public static List<SystemOverview> listTargetSystemForChannel(User user, Channel chan) {
        DataResult<SystemOverview> retval = null;
        SelectMode mode = ModeFactory.getMode("System_queries",
                "target_systems_for_channel");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("cid", chan.getId());
        params.put("org_id", user.getOrg().getId());
        retval = mode.execute(params);
        retval.setElaborationParams(new HashMap<>());
        return retval;
    }

    /**
     * Get a list of SystemOverview objects for the systems in an rhnset
     * @param user the user doing the lookup
     * @param setLabel the label of the set
     * @return List of SystemOverview objects
     */
    public static List<SystemOverview> inSet(User user, String setLabel) {
        return inSet(user, setLabel, false);
    }


    /**
     * Get a list of SystemOverview objects for the systems in an rhnset with option
     * to elaborate the result
     * @param user the user doing the lookup
     * @param setLabel the label of the set
     * @param elaborate elaborate results
     * @return List of SystemOverview objects
     */
    public static List<SystemOverview> inSet(User user, String setLabel,
            boolean elaborate) {
        DataResult<SystemOverview> retval = null;
        SelectMode mode = ModeFactory.getMode("System_queries",
                "in_set");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("set_label", setLabel);
        retval = mode.execute(params);
        retval.setElaborationParams(new HashMap<>());
        if (elaborate) {
            retval.elaborate();
        }
        return retval;
    }

    /**
     * Find a system by it's name (must be an exact string match)
     * @param user  the user doing the search
     * @param name the name of the system
     * @return the SystemOverview objects with the matching name
     */
    public static List<SystemOverview> listSystemsByName(User user,
            String name) {
        SelectMode mode = ModeFactory.getMode("System_queries", "find_by_name");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("name", name);
        Map<String, Object> elabParams = new HashMap<>();
        DataResult<SystemOverview> result =
                makeDataResult(params, elabParams, null, mode, SystemOverview.class);
        result.elaborate();
        return result;
    }


    private static DataResult<SystemOverview> listDuplicates(User user,
            String query, String key) {
        SelectMode mode = ModeFactory.getMode("System_queries", query);
        Map<String, Object> params = new HashMap<>();
        params.put("uid", user.getId());
        params.put("key", key);
        Map<String, Object> elabParams = new HashMap<>();
        return makeDataResult(params, elabParams, null, mode, SystemOverview.class);
    }

    private static List<DuplicateSystemGrouping> listDuplicates(User user, String query,
            List<String> ignored, Long inactiveHours) {

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, (0 - inactiveHours.intValue()));

        SelectMode ipMode = ModeFactory.getMode("System_queries",
                query);

        Date d = new Date(cal.getTimeInMillis());

        Map<String, Object> params = new HashMap<>();
        params.put("uid", user.getId());
        params.put("inactive_date", d);
        DataResult<NetworkDto> nets;
        if (ignored.isEmpty()) {
            nets = ipMode.execute(params);
        }
        else {
            nets = ipMode.execute(params, ignored);
        }


        List<DuplicateSystemGrouping> nodes = new ArrayList<>();
        for (NetworkDto net : nets) {
            boolean found = false;
            for (DuplicateSystemGrouping node : nodes) {
                if (node.addIfMatch(net)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                nodes.add(new DuplicateSystemGrouping(net));
            }
        }
        return nodes;
    }

    /**
     * List duplicate systems by ip address
     * @param user the user doing the search
     * @param inactiveHours the number of hours a system hasn't checked in
     *          to consider it inactive
     * @return List of DuplicateSystemGrouping objects
     */
    public static List<DuplicateSystemGrouping> listDuplicatesByIP(User user,
            long inactiveHours) {
        List<String> ignoreIps = new ArrayList<>();
        ignoreIps.add("127.0.0.1");
        ignoreIps.add("127.0.0.01");
        ignoreIps.add("127.0.0.2");
        ignoreIps.add("0");
        return listDuplicates(user, "duplicate_system_ids_ip", ignoreIps, inactiveHours);
    }

    /**
     * List duplicate systems by ip address
     * @param user the user doing the search
     * @param ip  ip address of the system
     * @return List of DuplicateSystemGrouping objects
     */
    public static  List<SystemOverview> listDuplicatesByIP(User user, String ip) {
        return listDuplicates(user, "duplicate_system_ids_ip_key", ip);
    }

    /**
     * List duplicate systems by ipv6 address
     * @param user the user doing the search
     * @param inactiveHours the number of hours a system hasn't checked in
     *          to consider it inactive
     * @return List of DuplicateSystemGrouping objects
     */
    public static List<DuplicateSystemGrouping> listDuplicatesByIPv6(User user,
            long inactiveHours) {
        List<String> ignoreIps = new ArrayList<>();
        ignoreIps.add("::1");
        return listDuplicates(user, "duplicate_system_ids_ipv6", ignoreIps, inactiveHours);
    }

    /**
     * List duplicate systems by ipv6 address
     * @param user the user doing the search
     * @param ip  ip address of the system
     * @return List of DuplicateSystemGrouping objects
     */
    public static  List<SystemOverview> listDuplicatesByIPv6(User user, String ip) {
        return listDuplicates(user, "duplicate_system_ids_ipv6_key", ip);
    }

    /**
     * List duplicate systems by mac address
     * @param user the user doing the search
     * @param inactiveHours the number of hours a system hasn't checked in
     *          to consider it inactive
     * @return List of DuplicateSystemGrouping objects
     */
    public static List<DuplicateSystemGrouping> listDuplicatesByMac(User user,
            Long inactiveHours) {
        List<String> ignoreMacs = new ArrayList<>();
        ignoreMacs.add("00:00:00:00:00:00");
        ignoreMacs.add("fe:ff:ff:ff:ff:ff");
        return listDuplicates(user, "duplicate_system_ids_mac", ignoreMacs, inactiveHours);
    }

    /**
     * List duplicate systems by mac address
     * @param user the user doing the search
     * @param mac the mac address of the system
     * @return List of DuplicateSystemGrouping objects
     */
    public static List<SystemOverview> listDuplicatesByMac(User user, String mac) {
        return listDuplicates(user, "duplicate_system_ids_mac_key", mac);
    }

    /**
     * List duplicate systems by hostname
     * @param user the user doing the search
     * @param inactiveHours the number of hours a system hasn't checked in
     *          to consider it inactive
     * @return List of DuplicateSystemBucket objects
     */
    public static List<DuplicateSystemGrouping> listDuplicatesByHostname(User user,
            Long inactiveHours) {
        List<DuplicateSystemGrouping> duplicateSystems = listDuplicates(user,
                "duplicate_system_ids_hostname",
                new ArrayList<>(), inactiveHours);
        for (DuplicateSystemGrouping element : duplicateSystems) {
            element.setKey(IDN.toUnicode(element.getKey()));
        }
        return duplicateSystems;
    }

    /**
     * List duplicate systems by hostName
     * @param user the user doing the search
     * @param hostName host name of the system
     * @return List of DuplicateSystemGrouping objects
     */
    public static List<SystemOverview> listDuplicatesByHostname(User user,
            String hostName) {
        return listDuplicates(user, "duplicate_system_ids_hostname_key",
                hostName);
    }

    /**
     * Return a note by ID and Server ID
     * @param user User to use to do the lookups
     * @param nid note ID
     * @param sid server ID
     * @return Note object
     */
    public static Note lookupNoteByIdAndSystem(User user, Long nid, Long sid) {
        SelectMode m = ModeFactory.getMode("System_queries", "note_by_id_and_server");
        Note n = new Note();
        Map<String, Object> params = new HashMap<>();
        params.put("nid", nid);
        params.put("sid", sid);
        DataResult<Map<String, Object>> dr = m.execute(params);
        for (Map<String, Object> map : dr) {
            n.setCreator(UserManager.lookupUser(user, (Long)map.get("creator")));
            n.setId((Long)map.get("id"));
            n.setServer(lookupByIdAndUser((Long)map.get("server_id"), user));
            n.setSubject((String)map.get("subject"));
            n.setNote((String)map.get("note"));
            n.setModified(Date.valueOf((String)map.get("modified")));
        }
        return n;
    }

    /**
     * Lookup all the custom info keys not assigned to this server
     * @param orgId The org ID that the server belongs to
     * @param sid The ID of the server
     * @return DataResult of keys
     */
    public static DataResult<Map<String, Object>> lookupKeysSansValueForServer(Long orgId,
            Long sid) {
        SelectMode m = ModeFactory.getMode("CustomInfo_queries",
                "custom_info_keys_sans_value_for_system");
        Map<String, Object> inParams = new HashMap<>();

        inParams.put("org_id", orgId);
        inParams.put("sid", sid);

        return m.execute(inParams);
    }

    /**
     * @param sid server id
     * @param oid organization id
     * @param pc pageContext
     * @return Returns history events for a system
     */
    public static DataResult<SystemEventDto> systemEventHistory(Long sid, Long oid, PageControl pc) {
        SelectMode m = ModeFactory.getMode("System_queries", "system_events_history");

        Map<String, Object> params = new HashMap<>();
        params.put("sid", sid);
        params.put("oid", oid);
        params.put("date", "1970-01-01 00:00:00");
        params.put("limit", null);
        params.put("offset", null);

        Map<String, Object> elabParams = new HashMap<>();
        return makeDataResult(params, elabParams, pc, m, SystemEventDto.class);
    }

    /**
     * Returns the list of history events for the specified server
     * @param server the server
     * @param org the organization of the user requesting the list
     * @param earliestDate the earliest completion date of the events returned
     * @param offset the number of results to skip
     * @param limit the maximum number of results returned
     * @return a list of the history event according to the parameters specified
     */
    @SuppressWarnings("unchecked")
    public static DataResult<SystemEventDto> systemEventHistory(Server server, Org org, java.util.Date earliestDate,
                                                                Integer offset, Integer limit) {
        final SelectMode m = ModeFactory.getMode("System_queries", "system_events_history");

        final SimpleDateFormat formatter = new SimpleDateFormat(LocalizationService.RHN_DB_DATEFORMAT);

        final Map<String, Object> params = new HashMap<>();
        params.put("sid", server.getId());
        params.put("oid", org.getId());
        params.put("date", earliestDate != null ? formatter.format(earliestDate) : "1970-01-01 00:00:00");
        params.put("limit", limit);
        params.put("offset", offset);

        return m.execute(params);
    }

    /**
     * Returns the details of a single history event
     *
     * @param sid server id
     * @param oid organization id
     * @param eid event id
     * @return Returns the details of the requested event
     */
    public static SystemEventDetailsDto systemEventDetails(Long sid, Long oid, Long eid) {
        SelectMode m = ModeFactory.getMode("System_queries", "system_event_details");

        Map<String, Object> params = new HashMap<>();
        params.put("sid", sid);
        params.put("oid", oid);
        params.put("eid", eid);

        @SuppressWarnings("unchecked")
        final DataResult<SystemEventDetailsDto> result = m.execute(params);
        return result.isEmpty() ? null : result.get(0);
    }

    /**
     * @param sid server id
     * @param pc pageContext
     * @return Returns system snapshot list
     */
    public static DataResult<Map<String, Object>> systemSnapshots(Long sid,
            PageControl pc) {
        SelectMode m = ModeFactory.getMode("General_queries", "system_snapshots");
        Map<String, Object> params = new HashMap<>();
        params.put("sid", sid);
        Map<String, Object> elabParams = new HashMap<>();
        return makeDataResult(params, elabParams, pc, m);
    }

    /**
     * @param sid server id
     * @param ssid snapshot id
     * @param pc pageContext
     * @return Returns system vs. snapshot packages comparision list
     */
    public static DataResult<Map<String, Object>> systemSnapshotPackages(Long sid,
            Long ssid, PageControl pc) {
        SelectMode m = ModeFactory.getMode("Package_queries",
                                           "compare_packages_to_snapshot");
        Map<String, Object> params = new HashMap<>();
        params.put("sid", sid);
        params.put("ss_id", ssid);
        Map<String, Object> elabParams = new HashMap<>();
        return makeDataResult(params, elabParams, pc, m);
    }

    /**
     * @param sid server id
     * @param ssid snapshot id
     * @param pc pageContext
     * @return Returns system vs. snapshot groups comparision list
     */
    public static DataResult<Map<String, Object>> systemSnapshotGroups(Long sid, Long ssid,
            PageControl pc) {
        SelectMode m = ModeFactory.getMode("SystemGroup_queries",
                                           "snapshot_group_diff");
        Map<String, Object> params = new HashMap<>();
        params.put("sid", sid);
        params.put("ss_id", ssid);
        Map<String, Object> elabParams = new HashMap<>();
        return makeDataResult(params, elabParams, pc, m);
    }

    /**
     * @param sid server id
     * @param ssid snapshot id
     * @param pc pageContext
     * @return Returns system vs. snapshot channels comparision list
     */
    public static DataResult<Map<String, Object>> systemSnapshotChannels(Long sid,
            Long ssid, PageControl pc) {
        SelectMode m = ModeFactory.getMode("Channel_queries",
                                           "snapshot_channel_diff");
        Map<String, Object> params = new HashMap<>();
        params.put("sid", sid);
        params.put("ss_id", ssid);
        Map<String, Object> elabParams = new HashMap<>();
        return makeDataResult(params, elabParams, pc, m);
    }

    /**
     * @param sid server id
     * @return Count of pending actions on system
     */
    public static Long countPendingActions(Long sid) {
        SelectMode m = ModeFactory.getMode("System_queries",
                "system_events_history_count_pending");
        Map<String, Object> params = new HashMap<>();
        params.put("sid", sid);
        DataResult<Map<String, Object>> toReturn = m.execute(params);
        return (Long) toReturn.get(0).get("count");
    }

    /**
     * @param sid server id
     * @param pc pageContext
     * @return Returns pending actions for a system
     */
    public static DataResult<SystemPendingEventDto> systemPendingEvents(Long sid,
            PageControl pc) {
        SelectMode m = ModeFactory.getMode("System_queries", "system_events_pending");
        Map<String, Object> params = new HashMap<>();
        params.put("sid", sid);
        Map<String, Object> elabParams = new HashMap<>();
        return makeDataResult(params, elabParams, pc, m, SystemPendingEventDto.class);
    }

    /**
     * @param sid server id
     * @param pc pageControl
     * @return Returns snapshot tags for a system
     */
    public static DataResult<SnapshotTagDto> snapshotTagsForSystem(Long sid,
            PageControl pc) {
        SelectMode m = ModeFactory.getMode("System_queries", "tags_for_system");
        Map<String, Object> params = new HashMap<>();
        params.put("sid", sid);
        Map<String, Object> elabParams = new HashMap<>();
        return makeDataResult(params, elabParams, pc, m, SnapshotTagDto.class);
    }

    /**
     * @param sid server id
     * @param ssId snapshot ID
     * @param pc pageControl
     * @return Returns snapshot tags for a system
     */
    public static DataResult<SnapshotTagDto> snapshotTagsForSystemAndSnapshot(Long sid,
            Long ssId, PageControl pc) {
        SelectMode m = ModeFactory.getMode("System_queries",
                "tags_for_system_and_snapshot");
        Map<String, Object> params = new HashMap<>();
        params.put("sid", sid);
        params.put("ss_id", ssId);
        Map<String, Object> elabParams = new HashMap<>();
        return makeDataResult(params, elabParams, pc, m, SnapshotTagDto.class);
    }

    /**
     * @param user user
     * @param pc PageControl
     * @param setLabel Set Label
     * @param sid Server ID
     * @return SnapshotTags in RHNSet
     */
    public static DataResult<SnapshotTagDto> snapshotTagsInSet(User user, PageControl pc,
            String setLabel, Long sid) {
        SelectMode m = ModeFactory.getMode("System_queries",
                "snapshot_tags_in_set");
        Map<String, Object> params = new HashMap<>();
        params.put("sid", sid);
        params.put("user_id", user.getId());
        params.put("set_label", setLabel);
        Map<String, Object> elabParams = new HashMap<>();
        return makeDataResult(params, elabParams, pc, m, SnapshotTagDto.class);
    }

    /**
     * @param orgId organization ID
     * @param sid server id
     * @param ssId snapshot ID
     * @param pc pageControl
     * @return Returns unservable packages for a system
     */
    public static DataResult<Map<String, Object>> systemSnapshotUnservablePackages(
            Long orgId, Long sid,
            Long ssId, PageControl pc) {
        SelectMode m = ModeFactory.getMode("Package_queries",
                "snapshot_unservable_package_list");
        Map<String, Object> params = new HashMap<>();
        params.put("org_id", orgId);
        params.put("sid", sid);
        params.put("ss_id", ssId);
        Map<String, Object> elabParams = new HashMap<>();
        return makeDataResult(params, elabParams, pc, m);
    }

    /**
     * @param uid user id
     * @param tid tag id
     * @return ssm systems with tag
     */
    public static DataResult systemsInSetWithTag(Long uid, Long tid) {
        SelectMode m = ModeFactory.getMode("System_queries",
                "systems_in_set_with_tag");
        Map params = new HashMap();
        params.put("user_id", uid);
        params.put("tag_id",  tid);
        return m.execute(params);
    }

    /**
     * Returns ids and names for systems in a given set with at least one of the
     * specified entitlements.
     * @param user the requesting user
     * @param setLabel the set label
     * @param entitlements the entitlement labels
     * @return a list of SystemOverview objects
     */
    @SuppressWarnings("unchecked")
    public static List<SystemOverview> entitledInSet(User user, String setLabel,
        List<String> entitlements) {
        SelectMode mode = ModeFactory.getMode("System_queries", "entitled_systems_in_set");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("set_label", setLabel);
        DataResult<SystemOverview> result = mode.execute(params, entitlements);
        result.setElaborationParams(new HashMap<>());
        return result;
    }

    /**
     * Sets the custom info values for the systems in the set
     * @param user the requesting user
     * @param setLabel the set label
     * @param keyLabel the label of the custom value key
     * @param value the value to set for the custom value
     */
    public static void bulkSetCustomValue(User user, String setLabel, String keyLabel,
            String value) {
        CallableMode mode = ModeFactory.getCallableMode("System_queries",
                "bulk_set_custom_values");

        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("set_label", setLabel);
        params.put("key_label", keyLabel);
        params.put("value", value);

        Map<String, Integer> out = new HashMap<>();
        out.put("retval", Types.INTEGER);

        Map<String, Object> result = mode.execute(params, out);
        log.debug("bulk_set_custom_value returns: {}", result.get("retval"));
    }

    /**
     * Removes the custom info values from the systems in the set
     * @param user the requesting user
     * @param setLabel the set label
     * @param keyId the id of the custom value key
     * @return number of rows deleted
     */
    public static int bulkRemoveCustomValue(User user, String setLabel, Long keyId) {
        WriteMode mode = ModeFactory.getWriteMode("System_queries",
                "bulk_remove_custom_values");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("set_label", setLabel);
        params.put("key_id", keyId);
        return mode.executeUpdate(params);
    }

    /**
     * Get a list of all tags that are applicable to entitled systems in the set
     * @param user The user to check the system set for
     * @return Maps of id, name, tagged_systems, and date_tag_created
     */
    public static DataResult<Map<String, Object>> listTagsForSystemsInSet(User user) {
        SelectMode mode = ModeFactory.getMode("General_queries",
                "tags_for_entitled_in_set");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        return mode.execute(params);
    }

    /**
     * Set the vaules for the user system prefrence for all systems in the system set
     * @param user The user
     * @param preference The name of the preference to set
     * @param value The value to set
     * @param defaultIn The default value for the preference
     */
    public static void setUserSystemPreferenceBulk(User user, String preference,
            Boolean value, Boolean defaultIn) {
        CallableMode mode = ModeFactory.getCallableMode("System_queries",
                "reset_user_system_preference_bulk");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("pref", preference);
        mode.execute(params, new HashMap<>());
        // preference values have a default, only insert if not default
        if (value != defaultIn) {
            mode = ModeFactory.getCallableMode("System_queries",
                    "set_user_system_preference_bulk");
            params = new HashMap<>();
            params.put("user_id", user.getId());
            params.put("pref", preference);
            params.put("value", value ? 1 : 0);
            mode.execute(params, new HashMap<>());
        }
    }

    private static List<Long> errataIdsReleventToSystemSet(User user) {
        SelectMode mode = ModeFactory.getMode("System_queries",
                "unscheduled_relevant_to_system_set");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        List<Map<String, Object>> results = mode.execute(params);
        List<Long> ret = new ArrayList<>();
        for (Map<String, Object> result : results) {
            ret.add((Long) result.get("id"));
        }
        return ret;
    }

    /**
     * Set auto_update for all systems in the system set
     * @param user The user
     * @param value True if the servers should enable auto update
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    public static void setAutoUpdateBulk(User user, Boolean value)
        throws TaskomaticApiException {
        CallableMode mode = ModeFactory.getCallableMode("System_queries",
                "set_auto_update_bulk");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("value", value ? "Y" : "N");
        mode.execute(params, new HashMap<>());
    }

    /**
     * Returns list bare metal systems visible to user.
     * @param user Currently logged in user.
     * @param pc PageControl
     * @return list of SystemOverviews
     */
    public static DataResult<SystemOverview> bootstrapList(User user,
            PageControl pc) {
        SelectMode m = ModeFactory.getMode("System_queries", "bootstrap");
        Map<String, Long> params = new HashMap<>();
        params.put("org_id", user.getOrg().getId());
        params.put("user_id", user.getId());
        Map<String, Long> elabParams = new HashMap<>();
        return makeDataResult(params, elabParams, pc, m);
    }

    /**
     * Returns list of client systems that connect through a proxy
     * @param sid System Id of the proxy to check.
     * @return list of SystemOverviews.
     */
    public static DataResult<SystemOverview> listClientsThroughProxy(Long sid) {
        SelectMode m = ModeFactory.getMode("System_queries", "clients_through_proxy");
        Map<String, Object> params = new HashMap<>();
        params.put("sid", sid);
        Map<String, Object> elabParams = new HashMap<>();
        return makeDataResult(params, elabParams, null, m, SystemOverview.class);
    }

    /**
     * Associate a particular system with a given capability. This is done by the python
     * backend code for traditional RHN clients. For other type of clients (e.g. Salt
     * minions), it can be done using this method.
     *
     * @param sid the server id
     * @param capability the capability to add as a string
     * @param version version number
     */
    public static void giveCapability(Long sid, String capability, Long version) {
        WriteMode m = ModeFactory.getWriteMode("System_queries",
                "add_to_client_capabilities");
        Map<String, Object> params = new HashMap<>();
        params.put("sid", sid);
        params.put("capability", capability);
        params.put("version", version);
        m.executeUpdate(params);
    }

    /**
     * Insert a new record into suseMinionInfo to hold the minion id.
     * @param sid server id
     * @param minionId the Salt minion id
     */
    public static void addMinionInfoToServer(Long sid, String minionId) {
        WriteMode m = ModeFactory.getWriteMode("System_queries",
                "add_minion_info");
        Map<String, Object> params = new HashMap<>();
        params.put("sid", sid);
        params.put("minion_id", minionId);
        m.executeUpdate(params);
    }

    /**
     * Update the the base and child channels of a server. Calls
     * the {@link UpdateBaseChannelCommand} and {@link UpdateChildChannelsCommand}.
     *
     * @param user the user changing the channels
     * @param server the server for which to change channels
     * @param baseChannel the base channel to set
     * @param childChannels the full list of child channels to set. Any channel no provided will be unsubscribed.
     * and will be used when regenerating the Pillar data for Salt minions.
     */
    public static void updateServerChannels(User user,
                                            Server server,
                                            Optional<Channel> baseChannel,
                                            Collection<Channel> childChannels) {
        long baseChannelId =
                baseChannel.map(Channel::getId).orElse(-1L);

        // if there's no base channel present the there are no child channels to set
        List<Long> childChannelIds = baseChannel.isPresent() ?
                childChannels.stream().map(Channel::getId).collect(Collectors.toList()) :
                emptyList();

        UpdateBaseChannelCommand baseChannelCommand =
                new UpdateBaseChannelCommand(
                        user,
                        server,
                        baseChannelId);

        UpdateChildChannelsCommand childChannelsCommand =
                new UpdateChildChannelsCommand(
                        user,
                        server,
                        childChannelIds);

        baseChannelCommand.skipChannelChangedEvent(true);
        baseChannelCommand.store();
        childChannelsCommand.skipChannelChangedEvent(true);
        childChannelsCommand.store();

        MessageQueue.publish(new ChannelsChangedEventMessage(server.getId(), user.getId()));

    }

    /**
     * Update the package state of a given system
     * the {@link UpdateBaseChannelCommand} and {@link UpdateChildChannelsCommand}.
     *
     * @param user the user updating the package state
     * @param minion the server for which to update the package state
     * @param pkgName package Name
     * @param pkgState {@link PackageStates} state of the package (0 = installed, 1= removed, 2 = unmanaged)
     * @param versionConstraint {@link VersionConstraints} (0 = latest, 1= any)
     */
    public static void updatePackageState(User user, MinionServer minion, PackageName pkgName,
                                          PackageStates pkgState, VersionConstraints versionConstraint) {
        //update the state
        ServerStateRevision stateRev = StateRevisionService.INSTANCE.cloneLatest(minion, user, true, true);
        Set<PackageState> pkgStates = stateRev.getPackageStates();
        boolean isAvailable = PackageFactory.hasPackageAvailable(minion, pkgName.getId());

        if (!isAvailable && pkgState == PackageStates.INSTALLED) {
            throw new IllegalArgumentException(pkgName.getName() + " :not available in assigned channels of " +
                    minion.getId());
        }
        else if ((isAvailable && pkgState == PackageStates.INSTALLED) || pkgState == PackageStates.REMOVED) {
            pkgStates.removeIf(ps -> ps.getName().equals(pkgName));
            PackageState packageState = new PackageState();
            packageState.setStateRevision(stateRev);
            packageState.setPackageState(pkgState);
            packageState.setVersionConstraint(versionConstraint);
            packageState.setName(pkgName);
            pkgStates.add(packageState);
        }
        else if (pkgState == PackageStates.PURGED) { // using PURGED flag for unmanaged here for now.
            pkgStates.removeIf(ps -> ps.getName().equals(pkgName));
        }

        StateFactory.save(stateRev);
        StatesAPI.generateServerPackageState(minion);
    }

    /**
     * Update MgrServerInfo with current grains data
     *
     * @param minion the minion which is a Mgr Server
     * @param grains grains from the minion
     */
    public static void updateMgrServerInfo(MinionServer minion, ValueMap grains) {
        // Check for Uyuni Server and create basic info
        if (grains.getOptionalAsBoolean("is_mgr_server").orElse(false)) {
            MgrServerInfo serverInfo = Optional.ofNullable(minion.getMgrServerInfo()).orElse(new MgrServerInfo());
            String oldHost = serverInfo.getReportDbHost();
            String oldName = serverInfo.getReportDbName();

            serverInfo.setVersion(PackageEvrFactory.lookupOrCreatePackageEvr(null,
                    grains.getOptionalAsString("version").orElse("0"),
                    "1", minion.getPackageType()));
            serverInfo.setReportDbName(grains.getValueAsString("report_db_name"));
            serverInfo.setReportDbHost(grains.getValueAsString("report_db_host"));
            serverInfo.setReportDbPort((grains.getValueAsLong("report_db_port").orElse(5432L)).intValue());
            serverInfo.setServer(minion);
            minion.setMgrServerInfo(serverInfo);

            if (!StringUtils.isAnyBlank(oldHost, oldName) &&
                    !(oldHost.equals(serverInfo.getReportDbHost()) &&
                            oldName.equals(serverInfo.getReportDbName()))) {
                // something changed, we better reset the user
                setReportDbUser(minion, false);
            }
        }
        else {
            ServerFactory.dropMgrServerInfo(minion);
            // Should we try to drop the credentials on the reportdb?
        }
    }

    /**
     * Set the User and Password for the report database in MgrServerInfo.
     * It trigger also a state apply to set this user in the report database.
     *
     * @param minion the Mgr Server
     * @param forcePwChange force a password change
     */
    public static void setReportDbUser(MinionServer minion, boolean forcePwChange) {
        // Create a report db user when system is a mgr server
        if (!minion.isMgrServer()) {
            return;
        }
        // create default user with random password
        MgrServerInfo mgrServerInfo = minion.getMgrServerInfo();
        if (StringUtils.isAnyBlank(mgrServerInfo.getReportDbName(), mgrServerInfo.getReportDbHost())) {
            // no reportdb configured
            return;
        }

        String password = RandomStringUtils.random(24, 0, 0, true, true, null, new SecureRandom());
        ReportDBCredentials credentials = Optional.ofNullable(mgrServerInfo.getReportDbCredentials())
            .map(existingCredentials -> {
                if (forcePwChange) {
                    existingCredentials.setPassword(password);
                    CredentialsFactory.storeCredentials(existingCredentials);
                }

                return existingCredentials;
            })
            .orElseGet(() -> {
                String username = "hermes_" + RandomStringUtils.random(8, 0, 0, true, false, null, new SecureRandom());

                ReportDBCredentials reportCredentials = CredentialsFactory.createReportCredentials(username, password);
                CredentialsFactory.storeCredentials(reportCredentials);

                return reportCredentials;
            });

        mgrServerInfo.setReportDbCredentials(credentials);

        Map<String, Object> pillar = new HashMap<>();
        pillar.put("report_db_user", credentials.getUsername());
        pillar.put("report_db_password", credentials.getPassword());

        MessageQueue.publish(new ApplyStatesEventMessage(
                minion.getId(),
                minion.getCreator() != null ? minion.getCreator().getId() : null,
                        false,
                        pillar,
                        ApplyStatesEventMessage.REPORTDB_USER
                ));
    }
}
