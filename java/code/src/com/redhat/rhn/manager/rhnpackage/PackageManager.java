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
package com.redhat.rhn.manager.rhnpackage;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.Row;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.common.util.CompressionUtil;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.DistChannelMap;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageArch;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageEvrFactory;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.rhnpackage.PackageName;
import com.redhat.rhn.domain.rhnpackage.PackageSource;
import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.PackageComparison;
import com.redhat.rhn.frontend.dto.PackageFileDto;
import com.redhat.rhn.frontend.dto.PackageListItem;
import com.redhat.rhn.frontend.dto.PackageMergeDto;
import com.redhat.rhn.frontend.dto.PackageOverview;
import com.redhat.rhn.frontend.dto.SsmRemovePackageListItem;
import com.redhat.rhn.frontend.dto.SsmUpgradablePackageListItem;
import com.redhat.rhn.frontend.dto.UpgradablePackageListItem;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.frontend.xmlrpc.PermissionCheckFailureException;
import com.redhat.rhn.manager.BaseManager;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.errata.cache.ErrataCacheManager;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.rhnset.RhnSetManager;
import com.redhat.rhn.manager.satellite.SystemCommandExecutor;
import com.redhat.rhn.manager.system.IncompatibleArchException;

import com.suse.manager.utils.PagedSqlQueryBuilder;
import com.suse.oval.ShallowSystemPackage;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * PackageManager
 */
public class PackageManager extends BaseManager {
    private static final Logger LOG = LogManager.getLogger(PackageManager.class);
    public static final String RHNCFG = "mgr-cfg";
    public static final String RHNCFG_CLIENT = "mgr-cfg-client";
    public static final String RHNCFG_ACTIONS = "mgr-cfg-actions";

    // Valid dependency types
    public static final String[]
        DEPENDENCY_TYPES = {"requires", "conflicts", "obsoletes", "provides",
            "recommends", "suggests", "supplements", "enhances", "predepends", "breaks"};


    private static final String[]
        CLEANUP_QUERIES = {"requires", "provides", "conflicts", "obsoletes",
            "recommends", "suggests", "supplements", "enhances",
            "channels", "files", "caps", "changelogs"};

    /**
     * Runs Package_queries.package_obsoletes query, which returns dependencies of the
     * obsolete type.
     * @param pid The package in question
     * @return Returns dependencies of type obsolete.
     */
    public static DataResult<Row> packageObsoletes(Long pid) {
        SelectMode m = ModeFactory.getMode("Package_queries", "package_obsoletes");
        Map<String, Object> params = new HashMap<>();
        params.put("pid", pid);
        return m.execute(params);
    }

    /**
     * Runs Package_queries.package_conflicts query, which returns dependencies of the
     * conflicts type.
     * @param pid The package in question
     * @return Returns dependencies of type conflicts.
     */
    public static DataResult<Row> packageConflicts(Long pid) {
        SelectMode m = ModeFactory.getMode("Package_queries", "package_conflicts");
        Map<String, Object> params = new HashMap<>();
        params.put("pid", pid);
        return m.execute(params);
    }

    /**
     * Runs Package_queries.package_provides query, which returns dependencies of the
     * provides type.
     * @param pid The package in question
     * @return Returns dependencies of type provides.
     */
    public static DataResult<Row> packageProvides(Long pid) {
        SelectMode m = ModeFactory.getMode("Package_queries", "package_provides");
        Map<String, Object> params = new HashMap<>();
        params.put("pid", pid);
        return m.execute(params);
    }

    /**
     * Runs Package_queries.package_requires query, which returns dependencies of the
     * requires type.
     * @param pid The package in question
     * @return Returns dependencies of type requires.
     */
    public static DataResult<Row> packageRequires(Long pid) {
        SelectMode m = ModeFactory.getMode("Package_queries", "package_requires");
        Map<String, Object> params = new HashMap<>();
        params.put("pid", pid);
        return m.execute(params);
    }

    /**
     * Runs Package_queries.package_recommends query, which returns dependencies of the
     * recommends type.
     * @param pid The package in question
     * @return Returns dependencies of type recommends.
     */
    public static DataResult<Row> packageRecommends(Long pid) {
        SelectMode m = ModeFactory.getMode("Package_queries", "package_recommends");
        Map<String, Object> params = new HashMap<>();
        params.put("pid", pid);
        return m.execute(params);
    }

    /**
     * Runs Package_queries.package_suggests query, which returns dependencies of the
     * suggests type.
     * @param pid The package in question
     * @return Returns dependencies of type suggests.
     */
    public static DataResult<Row> packageSuggests(Long pid) {
        SelectMode m = ModeFactory.getMode("Package_queries", "package_suggests");
        Map<String, Object> params = new HashMap<>();
        params.put("pid", pid);
        return m.execute(params);
    }

    /**
     * Runs Package_queries.package_supplements query, which returns dependencies of the
     * supplements type.
     * @param pid The package in question
     * @return Returns dependencies of type supplements.
     */
    public static DataResult<Row> packageSupplements(Long pid) {
        SelectMode m = ModeFactory.getMode("Package_queries", "package_supplements");
        Map<String, Object> params = new HashMap<>();
        params.put("pid", pid);
        return m.execute(params);
    }

    /**
     * Runs Package_queries.package_enhances query, which returns dependencies of the
     * enhances type.
     * @param pid The package in question
     * @return Returns dependencies of type enhances.
     */
    public static DataResult<Row> packageEnhances(Long pid) {
        SelectMode m = ModeFactory.getMode("Package_queries", "package_enhances");
        Map<String, Object> params = new HashMap<>();
        params.put("pid", pid);
        return m.execute(params);
    }

    /**
     * Runs Package_queries.package_predepends query, which returns dependencies of the
     * pre-depends type.
     * @param pid The package in question
     * @return Returns dependencies of type pre-depends.
     */
    public static DataResult<Row> packagePreDepends(Long pid) {
        SelectMode m = ModeFactory.getMode("Package_queries", "package_predepends");
        Map<String, Object> params = new HashMap<>();
        params.put("pid", pid);
        return m.execute(params);
    }

    /**
     * Runs Package_queries.package_breaks query, which returns dependencies of the
     * breaks type.
     * @param pid The package in question
     * @return Returns dependencies of type breaks.
     */
    public static DataResult<Row> packageBreaks(Long pid) {
        SelectMode m = ModeFactory.getMode("Package_queries", "package_breaks");
        Map<String, Object> params = new HashMap<>();
        params.put("pid", pid);
        return m.execute(params);
    }

    /**
     * List the package in a channel (for the web UI lists)
     * @param cid the channel id
     * @return the list of packages
     */
    public static DataResult listPackagesInChannelForList(Long cid) {
        SelectMode m = ModeFactory.getMode("Package_queries", "packages_in_channel");
        Map<String, Object> params = new HashMap<>();
        params.put("cid", cid);
        return m.execute(params);
    }


    /**
     * Runs Channel_queries.org_pkg_channels query.
     * @param orgId The id of the org for the logged in user
     * @param pid The id of the package in question
     * @return Returns a list of channels that provide the given package
     */
    public static DataResult orgPackageChannels(Long orgId, Long pid) {
        SelectMode m = ModeFactory.getMode("Channel_queries", "org_pkg_channels",
                                           Map.class);
        Map<String, Object> params = new HashMap<>();
        params.put("pid", pid);
        params.put("org_id", orgId);
        return m.execute(params);
    }

    /**
     * Returns the erratas providing a given package
     * @param orgId The id of the org for the logged in user
     * @param pid The package id for the package in question
     * @return Returns a list of errata that provide the given package
     */
    public static DataResult providingErrata(Long orgId, Long pid) {
        SelectMode m = ModeFactory.getMode("Errata_queries", "org_pkg_errata");
        Map<String, Object> params = new HashMap<>();
        params.put("pid", pid);
        params.put("org_id", orgId);
        return m.execute(params);
    }

    /**
     * Returns the files associated with a given package
     * @param pid The package id for the package in question
     * @return Returns a list of files associated with the package
     */
    public static DataResult<PackageFileDto> packageFiles(Long pid) {
        SelectMode m = ModeFactory.getMode("Package_queries", "package_files");
        Map<String, Object> params = new HashMap<>();
        params.put("pid", pid);
        return m.execute(params);
    }

    /**
     * Return information related to packages that are udpates to this one
     * @param user The user in question
     * @param pid The package in question
     * @return A map of updating package information
     */
    public static DataResult<Map<String, Object>> obsoletingPackages(User user, Long pid) {
        SelectMode m = ModeFactory.getMode("Package_queries", "obsoleting_packages");
        Map<String, Object> params = new HashMap<>();
        params.put("pid", pid);
        params.put("org_id", user.getOrg().getId());
        return m.execute(params);
    }

    /**
     * Returns the providing channels for a package that the given user has access to
     * @param user The user requesting the channels
     * @param pid The package in question
     * @return Returns a list of providing channels (id, name, label) for a package
     */
    public static DataResult<Row> providingChannels(User user, Long pid) {
        SelectMode m = ModeFactory.getMode("Package_queries", "providing_channels",
                                           Map.class);
        Map<String, Object> params = new HashMap<>();
        params.put("pid", pid);
        params.put("org_id", user.getOrg().getId());
        return m.execute(params);
    }

    /**
     * Returns list of package for given server
     * @param sid Server Id
     * @param pc PageControl can also be null.
     * @return list of packages for given server
     */
    public static DataResult<PackageListItem> shallowSystemPackageList(Long sid, PageControl pc) {
        return PackageManager.getPackagesPerSystem(sid, "system_package_list", pc);
    }

    public static DataResult<ShallowSystemPackage> shallowSystemPackageList(Long sid) {
        SelectMode m = ModeFactory.getMode("Package_queries", "shallow_system_package_list");

        Map<String, Object> params = new HashMap<>();
        params.put("sid", sid);

        return m.execute(params);
    }

    /**
     * Returns list of package for given server
     * @param sid Server Id
     * @param pc PageControl can also be null.
     * @return list of packages for given server
     */
    public static DataResult<PackageListItem> systemAvailablePackages(Long sid,
            PageControl pc) {
        return PackageManager.getPackagesPerSystem(sid, "system_available_packages", pc);
    }

    /**
     * Returns the list of all installed and also not installed packages,
     * available for the particular system.
     * @param sid Server Id
     * @param pc PageControl can also be null.
     * @return list of packages for given server
     */
    public static DataResult<PackageListItem> systemTotalPackages(Long sid,
            PageControl pc) {
        return PackageManager.getPackagesPerSystem(sid, "system_total_packages", pc);
    }

    /**
     * Returns the list of all installed packages available for locking on a none SUSE system
     * @param sid Server Id
     * @param pc PageControl can also be null.
     * @return list of packages for given server
     */
    public static DataResult<PackageListItem> nonSUSEsystemLockingPackages(Long sid, PageControl pc) {
        return PackageManager.getPackagesPerSystem(sid, "system_installed_pkgs_non_suse_locking", pc);
    }

    /**
     * Call any template from the Package_queries with the system ID.
     *
     * @param sid
     * @param template
     * @param pc
     * @return
     */
    protected static DataResult<PackageListItem> getPackagesPerSystem(Long sid,
                                                     String template,
                                                     PageControl pc) {
        SelectMode m = ModeFactory.getMode("Package_queries", template);
        Map<String, Object> params = new HashMap<>();
        params.put("sid", sid);
        return makeDataResult(params, params, pc, m);
    }

    /**
     * Returns a list of upgradable packages for the given server id.
     * @param sid Server Id
     * @param pc PageControl to limit page size, maybe null for all
     * upgradable packages.
     * @return a list of UpgradablePackageListItems
     */
    public static DataResult<UpgradablePackageListItem> upgradable(Long sid,
                                                                PageControl pc) {
        SelectMode m = ModeFactory.getMode("Package_queries",
                                           "system_upgradable_package_list");

        Map<String, Object> params = new HashMap<>();
        params.put("sid", sid);

        return makeDataResult(params, params, pc, m);
    }

    /**
     * Returns a count of packages that can be upgraded on the given server.
     *
     * @param sid identifies the server
     * @return count of packages that can be upgraded
     */
    public static int countUpgradable(Long sid) {
        SelectMode m = ModeFactory.getMode("Package_queries",
                                           "count_system_upgradable_package_list");
        Map<String, Object> params = new HashMap<>();
        params.put("sid", sid);

        DataResult<Row> dr = makeDataResult(params, null, null, m);
        return ((Long)dr.get(0).get("count")).intValue();
    }

    /**
     * Finds a package by using the id column of rhnPackage
     * @param id The package id
     * @param user The user performing the lookup
     * @return A Package object
     */
    public static Package lookupByIdAndUser(Long id, User user) {
        return PackageFactory.lookupByIdAndUser(id, user);
    }

    /**
     * Find packages by using the id column of rhnPackage
     * @param ids List of package id
     * @param user The user performing the lookup
     * @return List of Package objects
     */
    public static List<Package> lookupByIdAndUser(List<Long> ids, User user) {
        return PackageFactory.lookupByIdAndUser(ids, user);
    }


    /**
     * Returns a dataResult containing all of the packages available to an
     * errata. Picks the right query depending on whether or not the errata
     * is published.
     * @param errata The errata in question
     * @return Returns the list of packages available for this particular errata.
     */
    public static DataResult<PackageOverview> packagesAvailableToErrata(Errata errata) {
        SelectMode m = ModeFactory.getMode("Package_queries", "packages_available_to_errata");
        Map<String, Object> params = new HashMap<>();
        params.put("org_id", errata.getOrg().getId());
        params.put("eid", errata.getId());

        return makeDataResult(params, params, null, m);
    }

    /**
     * Returns a data result containing all of the packages available to an errata
     * in the channel specified by cid.
     * @param errata The errata in question
     * @param cid The channel id, we want packages in this channel
     * @param user The user requesting the list
     * @return Returns the list of packages available for this particular errata in
     * this particular channel.
     */
    public static DataResult<PackageOverview> packagesAvailableToErrataInChannel(Errata errata,
                                                                Long cid,
                                                                User user) {
        SelectMode m = ModeFactory.getMode("Package_queries", "packages_available_to_errata_in_channel");
        Map<String, Object> params = new HashMap<>();
        params.put("target_eid", errata.getId());
        params.put("source_cid", cid);

        Map<String, Object> elabParams = new HashMap<>();
        elabParams.put("org_id", user.getOrg().getId());
        return makeDataResult(params, elabParams, null, m);
    }

    private static DataResult<PackageOverview> getPackageIdsInSet(User user, String label,
                                                 PageControl pc, String query) {
        SelectMode m = ModeFactory.getMode("Package_queries",
                query);
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("set_label", label);

        Map<String, Object> elabs = new HashMap<>();
        elabs.put("org_id", user.getOrg().getId());

        DataResult<PackageOverview> dr;
        if (pc != null) {
            dr = makeDataResult(params, elabs, pc, m);
        }
        else {
            //if page control is null, we don't want to elaborate
            dr = m.execute(params);
            dr.setElaborationParams(elabs);
        }
        return dr;
    }

    /**
     * Returns a DataResult containing PackageOverview dto's representing the
     * package_ids_in_set query
     * @param user The User
     * @param label The label of the set we want
     * @param pc The page control for the user
     * @return Returns the list of packages whose id's are in the given set
     */
    public static DataResult<PackageOverview> packageIdsInSet(User user, String label, PageControl pc) {
        return getPackageIdsInSet(user, label, pc, "package_ids_in_set");
    }

    /**
     * Returns a DataResult containing PackageSourceOverview dto's representing the
     * source_package_ids_in_set query
     * @param user The User
     * @param label The label of the set we want
     * @param pc The page control for the user
     * @return Returns the list of packages whose id's are in the given set
     */
    public static DataResult<PackageOverview> sourcePackageIdsInSet(User user, String label, PageControl pc) {
        return getPackageIdsInSet(user, label, pc, "source_package_ids_in_set");
    }

    /**
     * Returns a DataResult containing PackageOverview dto's representing the
     * package_ids_in_set query
     * @param user The User
     * @param label The label of the set we want
     * @return Returns the list of packages whose id's are in the given set
     */
    public static DataResult<PackageOverview> packageIdsInSet(User user, String label) {

        SelectMode m = ModeFactory.getMode("Package_queries",
                "package_ids_in_set");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("set_label", label);

        Map<String, Object> elabs = new HashMap<>();
        elabs.put("org_id", user.getOrg().getId());

        DataResult<PackageOverview> dr;
        dr = makeDataResult(params, elabs, null, m);
        return dr;

    }

    /**
     * Returns a data result containing PackageOverview dto's representing the
     * packages that are currently associated with this errata.
     * @param errata The errata in question
     * @param pc The page control for the logged in user
     * @return The packages associated with this errata
     */
    public static DataResult<PackageOverview> packagesInErrata(Errata errata, PageControl pc) {
        SelectMode m = ModeFactory.getMode("Package_queries", "packages_in_errata");
        Map<String, Object> params = new HashMap<>();
        params.put("eid", errata.getId());
        params.put("org_id", errata.getOrg().getId());
        if (pc != null) {
            return makeDataResult(params, params, pc, m);
        }
        DataResult<PackageOverview> dr = m.execute(params);
        dr.setTotalSize(dr.size());
        return dr;
    }

    /**
     * Returns true if the package whose name and evr id are passed in exists
     * in the given channel whose id is cid.
     * @param cid Channel id
     * @param evrid package evr id
     * @param nameid package name id
     * @return true if package exists in channel.
     */
    public static boolean isPackageInChannel(Long cid, Long nameid, Long evrid) {
        return PackageFactory.isPackageInChannel(cid, nameid, evrid);
    }

    /**
     * Get the ID of the package that needs updating based on the name.
     *
     * So, if say the server has up2date version 2.8.0 and
     * the latest rev of up2date is 3.1.1 this will return the
     * ID of the package for 3.1.1
     *
     * @param sid of system
     * @param packageName of system - up2date for example
     * @return Long id of package if found.  null if not.
     */
    public static Long getServerNeededUpdatePackageByName(Long sid, String packageName) {
        Map<String, Object> params = new HashMap<>();
        params.put("sid", sid);
        params.put("name", packageName);
        SelectMode m = ModeFactory.getMode("Package_queries",
                "server_packages_needing_update");
        DataResult dr = m.execute(params);
        if (!dr.isEmpty()) {
            Long id = (Long) ((Map) dr.get(0)).get("id");
            return id;
        }
        return null;
    }

    /**
     * Find the most up to date package with the given name accessible to a system with
     * the given system id.
     * @param sid The id of a system to which the package must be accessible.
     * @param name The exact name of the package sought for.
     * @return A map with keys 'name_id' and 'evr_id' containing Long types.
     *         Null if nothing found.
     */
    public static Map<String, Long> lookupEvrIdByPackageName(Long sid, String name) {
        Map<String, Object> params = new HashMap<>();
        params.put("sid", sid);
        params.put("name", name);
        SelectMode m = ModeFactory.getMode("Package_queries", "lookup_id_combo_by_name");
        DataResult<Map<String, Long>> dr = m.execute(params);
        if (!dr.isEmpty()) {
            return dr.get(0);
        }
        return null;
    }

    /**
     * Lookup a package name.
     * @param name Package name to lookup.
     * @return PackageName associated with the given string name.
     */
    public static PackageName lookupPackageName(String name) {
        Session session = null;
        try {
            session = HibernateFactory.getSession();
            return (PackageName)session.getNamedQuery("PackageName.findByName")
                                       .setString("name", name)
                                       .uniqueResult();
        }
        catch (HibernateException e) {
            LOG.error("Hibernate exception: {}", e.toString());
        }
        return null;
    }

    /**
     * Get the list of locked packages, available to the particular system.
     *
     * @param sid System ID.
     * @param pc Page control object.
     * @return  DataResult containing locked packages data.
     */
    public static DataResult<PackageListItem> systemLockedPackages(Long sid, PageControl pc) {
        SelectMode m = ModeFactory.getMode("Package_queries", "system_locked_packages");
        Map<String, Object> params = new HashMap<>();
        params.put("sid", sid);
        return makeDataResult(params, new HashMap<>(), pc, m);
    }

    /**
     * Get the list of packages which should be locked on a particular system.
     *
     * @param sid System ID.
     * @param aid Action ID.
     * @param pc Page control object.
     * @return  DataResult containing locked packages data.
     */
    public static DataResult<PackageListItem> systemSetLockedPackages(
            Long sid, Long aid, PageControl pc) {
        SelectMode m = ModeFactory.getMode("Package_queries", "system_set_locked_packages");
        Map<String, Object> params = new HashMap<>();
        params.put("sid", sid);
        params.put("aid", aid);
        return makeDataResult(params, new HashMap<>(), pc, m);
    }

    /**
     * Lock packages.
     * If the package object has lock pending, then the package will be only half-locked.
     *
     * @param sid Server ID.
     * @param packages List of packages to lock.
     */
    public static void lockPackages(Long sid, Set<Package> packages) {
        for (Package pkg : packages) {
            Map<String, Object> params = new HashMap<>();
            params.put("sid", sid);
            params.put("pkgid", pkg.getId());
            params.put("nid", pkg.getPackageName().getId());
            params.put("eid", pkg.getPackageEvr().getId());
            params.put("aid", pkg.getPackageArch().getId());
            params.put("pending", pkg.isLockPending() ? PackageManager.PKG_PENDING_LOCK : null);
            ModeFactory.getWriteMode("Package_queries", "lock_package").
                    executeUpdate(params);
        }
    }

    /**
     * Unlock packages.
     *
     * @param sid Server ID.
     * @param packages List of packages to unlock.
     */
    public static void unlockPackages(Long sid, Set<Package> packages) {
        for (Package pkg : packages) {
            Map<String, Object> params = new HashMap<>();
            params.put("sid", sid);
            params.put("pkgid", pkg.getId());
            ModeFactory.getWriteMode("Package_queries",
                                     "unlock_package").executeUpdate(params);
        }
    }

    /**
     * Remove orphan locked packages, when action has been canceled.
     *
     * @param sid System ID
     * @param actionId Action ID
     */
    public static void syncLockedPackages(Long sid, Long actionId) {
        Map<String, Object> params = new HashMap<>();
        params.put("sid", sid);
        params.put("action_id", actionId);
        ModeFactory.getWriteMode("Package_queries", "remove_orphan_lock_on_action_cancel").executeUpdate(params);
    }

    /**
     * Update locked packages, when action succeeded.
     *
     * @param sid System ID
     * @param actionId Action ID
     */
    public static void updateLockedPackages(Long sid, Long actionId) {
        Map<String, Object> params = new HashMap<>();
        params.put("sid", sid);
        params.put("action_id", actionId);
        ModeFactory.getWriteMode("Package_queries", "update_pkg_lock_on_action").executeUpdate(params);
    }

    /**
     * Update unlocked packages, when action succeeded.
     *
     * @param sid System ID
     * @param actionId Action ID
     */
    public static void updateUnlockedPackages(Long sid, Long actionId) {
        Map<String, Object> params = new HashMap<>();
        params.put("sid", sid);
        params.put("action_id", actionId);
        ModeFactory.getWriteMode("Package_queries", "update_pkg_unlock_on_action").executeUpdate(params);
    }

    /**
     * Pending status "L" corresponds that the package is going to be locked.
     * At this moment package has different status.
     */
    public static final String PKG_PENDING_LOCK = "L";

    /**
     * Pending status "U" corresponds that the package is going to be unlocked.
     * At this moment package has different status.
     */
    public static final String PKG_PENDING_UNLOCK = "U";

    /**
     * Sets the pending status on locked package. If parameter "pendingStatus" is null,
     * the package is considered locked.
     *
     * @param pkgId the package id
     * @param pendingStatus the pending status
     */
    public static void setPendingStatusOnLockedPackage(Long pkgId, String pendingStatus) {
        if (pendingStatus != null && pendingStatus.isEmpty()) {
            pendingStatus = null;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("pkg_id", pkgId);
        params.put("pending", pendingStatus);
        ModeFactory.getWriteMode("Package_queries",
                                 "set_pending_lock_status").executeUpdate(params);
    }

    /**
     * Sets the pending status on locked packages. If parameter "pendingStatus" is null,
     * the packages in the set are considered locked.
     * @param pkgs Set of packages.
     * @param pendingStatus Status for all of them.
     */
    public static void setPendingStatusOnLockedPackages(Set<Package> pkgs,
                                                        String pendingStatus) {
        pkgs.forEach(x -> PackageManager.setPendingStatusOnLockedPackage(x.getId(), pendingStatus));
    }

    /**
     * Deletes a package from the system
     * @param user calling user
     * @param pkg package to delete
     * @throws PermissionCheckFailureException - caller is not an org admin,
     * the package is in one of the RH owned channels, or is in different org
     */
    public static void schedulePackageRemoval(User user, Package pkg)
        throws PermissionCheckFailureException {
        if (!user.hasRole(RoleFactory.ORG_ADMIN)) {
            throw new PermissionCheckFailureException();
        }
        DataResult channels = PackageManager.orgPackageChannels(
                user.getOrg().getId(), pkg.getId());
        if (pkg.getOrg() == null || user.getOrg() != pkg.getOrg()) {
            throw new PermissionCheckFailureException();
        }
        Session session = HibernateFactory.getSession();
        cleanupFileEntries(pkg.getId());
        StringBuilder packageFileName = new StringBuilder();
        if (pkg.getPath() != null) {
            packageFileName.append(pkg.getPath().trim());
        }
        String pfn = packageFileName.toString().trim();
        if (!pfn.isEmpty()) {
            schedulePackageFileForDeletion(pfn);
        }

        // For every channel the package is in, mark the channel as "changed" in case its
        // metadata needs tto be updated (RHEL5+, mostly)
        for (Object channelIn : channels) {
            Map m = (Map) channelIn;
            String channelLabel = m.get("label").toString();
            Channel channel = ChannelFactory.lookupByLabel(user.getOrg(), channelLabel);
            // force channel save to change last_modified
            // otherwise the repodata won't be generated
            channel.setLastModified(new Date());
            ChannelFactory.save(channel);
            ChannelManager.queueChannelChange(channelLabel,
                    "java::deletePackage",
                    pkg.getPackageName().getName());
        }
        session.delete(pkg);
    }

    /**
     * Deletes a source package from the system
     * @param user calling user
     * @param pkg source package to delete
     * @throws PermissionCheckFailureException - caller is not an org admin,
     * the package is in one of the RH owned channels, or is in different org
     */
    public static void schedulePackageSourceRemoval(User user, PackageSource pkg)
        throws PermissionCheckFailureException {
        if (!user.hasRole(RoleFactory.ORG_ADMIN)) {
            throw new PermissionCheckFailureException();
        }
        if (pkg.getOrg() == null || user.getOrg() != pkg.getOrg()) {
            throw new PermissionCheckFailureException();
        }
        schedulePackageFileForDeletion(pkg.getPath());
        PackageFactory.deletePackageSource(pkg);
    }

    private static void cleanupFileEntries(Long pid) {
        Map<String, Object> params = new HashMap<>();
        params.put("pid", pid);
        for (String cleanupQueryIn : CLEANUP_QUERIES) {
            WriteMode writeMode = ModeFactory.getWriteMode("Package_queries",
                    "cleanup_package_" + cleanupQueryIn);
            writeMode.executeUpdate(params);
        }
    }

    private static void schedulePackageFileForDeletion(String fileName) {
        Map<String, Object> params = new HashMap<>();
        params.put("path", fileName);
        WriteMode wm = ModeFactory.getWriteMode("Package_queries",
                "schedule_pkg_for_delete");
        wm.executeUpdate(params);
    }

    /**
     * Looks at a published errata and a channel and returns a list of PackageComparisons
     * containing the packages that the errata has more recent versions of and may
     * be pushed into the channel by the user
     * @param cid channel id
     * @param eid errata id
     * @param pc PageControl object needed to handle pagination issues.
     * @return DataResult of PackageComparisons
     */
    public static DataResult possiblePackagesForPushingIntoChannel(Long cid, Long eid,
                                                            PageControl pc) {
        Map<String, Object> params = new HashMap<>();
        params.put("cid", cid);
        params.put("eid", eid);

        SelectMode m1 = ModeFactory.getMode("Package_queries",
                                           "possible_packages_for_pushing_into_channel");

        DataResult possiblePackages = m1.execute(params);

        SelectMode m2 = ModeFactory.getMode("Package_queries",
            "packages_in_errata_not_in_channel");

        DataResult notInChannelPackages = m2.execute(params);
        Iterator i = notInChannelPackages.iterator();

        // Remove packages that are in both queries
        while (i.hasNext()) {
            PackageComparison po = (PackageComparison) i.next();
            for (Object possiblePackageIn : possiblePackages) {
                PackageComparison pinner = (PackageComparison) possiblePackageIn;
                if (pinner.getId().equals(po.getId())) {
                    LOG.debug("possiblePackagesForPushingIntoChannel removing: {}", pinner.getId());
                    i.remove();
                }
            }
        }

        // Combine the 2
        possiblePackages.addAll(notInChannelPackages);
        if (LOG.isDebugEnabled()) {
            LOG.debug("All: {}", possiblePackages);
        }
        possiblePackages.setTotalSize(possiblePackages.size());
        return processPageControl(possiblePackages, pc, null);
    }

    /**
     * Given a server this method returns the redhat-release package evr info.
     * This package is a marker package and holds information like
     * @param server the server object who has to be queried
     * @return the redhat release package evr or null if the package can't be found..
     */
    public static PackageEvr lookupReleasePackageEvrFor(Server server) {
        Map<String, Object> params = new HashMap<>();
        params.put("sid", server.getId());
        SelectMode m = ModeFactory.getMode("Package_queries",
                                "lookup_release_package_evr_id");
         DataResult<Map<String, Long>> ret = m.execute(params);
         if (ret.isEmpty()) {
             return null;
         }

         Long evrId = ret.get(0).get("id");
         return PackageEvrFactory.lookupPackageEvrById(evrId);
    }

    /**
     * Given a Server and a PackageEvr, return the OS-version ("6Workstation", "7Server",
     * etc) for that Server
     * @param s Server we care about
     * @param pevr Package-evr of the redhat-release package on that server
     * @return os-version string
     */
    public static String lookupSystemReleaseReleaseVersionFor(Server s, PackageEvr pevr) {
        String vers = pevr.getVersion();

        // RHEL7 broke the protocol Sat5 relies on - special case to find what we need
        if (vers.startsWith("7")) {
            vers = PackageManager.lookupSystemReleaseReleaseVersionFor(s);
        }
        return vers;
    }

    /** Given an assumed-RHEL7-Server, get the OS_version the rest of Sat5 code relies on
     * ("7Server", "7Workstation", etc)
     *
     * @param s Server we care about
     * @return system-release(releasever) of the redhat-release-% on this system
     */
    public static String lookupSystemReleaseReleaseVersionFor(Server s) {
        Map<String, Object> params = new HashMap<>();
        params.put("sid", s.getId());
        SelectMode m = ModeFactory.getMode("Package_queries",
                        "lookup_system_release_releasever");
         DataResult<Map<String, String>> ret = m.execute(params);
         if (ret.isEmpty()) {
             return null;
         }
         else {
             return ret.get(0).get("version");
         }
    }

    /**
     * Returns package metadata for all packages named 'packageName' and exist
     * in the channels whose arch is one of the 'channelArches'.
     * @param org The users Org.
     * @param packageName Name of package being sought.
     * @param channelarches list of valid channel arches. i.e.
     * <ul>
     * <li>channel-ia32</li>
     * <li>channel-ia64</li>
     * <li>channel-sparc</li>
     * <li>channel-alpha</li>
     * <li>channel-s390</li>
     * <li>channel-s390x</li>
     * <li>channel-iSeries</li>
     * <li>channel-pSeries</li>
     * <li>channel-x86_64</li>
     * <li>channel-ppc</li>
     * </ul>
     * @return package metadata for all packages named 'packageName' and exist
     * in the channels whose arch is one of the 'channelArches'.
     */
    public static DataResult lookupPackageNameOverview(
            Org org, String packageName, String[] channelarches) {

        Map<String, Object> params = new HashMap<>();
        params.put("org_id", org.getId());
        params.put("package_name", packageName);

        List<String> inClause = Arrays.asList(channelarches);

        SelectMode m = ModeFactory.getMode("Package_queries", "packages_by_name");
        return m.execute(params, inClause);
    }

    /**
     * Returns package metadata for all packages named 'packageName' and exist
     * in the channels which which the orgId is subscribed.
     * @param org The users Org.
     * @param packageName Name of package being sought.
     * @return package metadata for all packages named 'packageName' and exist
     * in the channels which which the orgId is subscribed.
     */
    public static DataResult lookupPackageNameOverview(Org org, String packageName) {
        Map<String, Object> params = new HashMap<>();
        params.put("org_id", org.getId());
        params.put("package_name", packageName);

        if (OrgFactory.getActiveSystems(org) > 0) {
            SelectMode m = ModeFactory.getMode(
                    "Package_queries", "packages_by_name_smart");
            return m.execute(params);
        }
        SelectMode m = ModeFactory.getMode(
                "Package_queries", "packages_by_name_clabel");
        return m.execute(params);
    }

    /**
     * Returns package metadata for all packages named 'packageName' and exist
     * in passed in channelID if the ORG has access..
     * @param org The users Org.
     * @param packageName Name of package being sought.
     * @param channelID Id of the channel to lookup package in
     *
     * @return package metadata for all packages named 'packageName' and exist
     * in the channels which which the orgId is subscribed.
     */
    public static DataResult lookupPackageNameOverviewInChannel(Org org, String packageName,
            Long channelID) {
        Map<String, Object> params = new HashMap<>();
        params.put("org_id", org.getId());
        params.put("package_name", packageName);
        params.put("channel_id", channelID);

        SelectMode m = ModeFactory.getMode(
                "Package_queries", "packages_by_name_cid");
        return m.execute(params);
    }

    /**
     * Lookup packages contained in fromCid that are eligable to be put in toCid.
     *      Packages are filtered based on channel/package arch, and excluded if
     *      a package with the same nvrea exists in the toCid
     * @param fromCid channel id to pull packages from
     * @param toCid channel id of channel that you will be pushing packges to (later on)
     * @return DataResult of PackageOverview objects
     */
    public static DataResult<PackageOverview> lookupPackageForChannelFromChannel(Long fromCid, Long toCid) {
        Map<String, Object> params = new HashMap<>();
        params.put("cid", toCid);
        params.put("scid", fromCid);

            SelectMode m = ModeFactory.getMode(
                    "Package_queries", "packages_for_channel_from_channel");

            DataResult<PackageOverview> dr = m.execute(params);
            dr.setElaborationParams(new HashMap<>());
            return dr;
    }

    /**
     * Lookup custom packages (packages with org_id of the current user) that can
     *      be pushed into the a channel (cid).
     *      Packages are filtered based on channel/package arch, and excluded if
     *      a package with the same nvrea exists in the toCid
     * @param cid channel id of channel that you will be pushing packges to (later on)
     * @param orgId the org of the custom packages
     * @return DataResult of PackageOverview objects
     */
    public static DataResult<PackageOverview> lookupCustomPackagesForChannel(Long cid, Long orgId) {
        Map<String, Object> params = new HashMap<>();
        params.put("cid", cid);
        params.put("org_id", orgId);

            SelectMode m = ModeFactory.getMode(
                    "Package_queries", "custom_packages_for_channel");

            DataResult<PackageOverview> dr = m.execute(params);
            dr.setElaborationParams(new HashMap<>());
            return dr;
    }

    /**
     * Lookup package differences between thisCid and otherCid channels.
     * @param thisCid channel id we want to update
     * @param otherCid channel id which we compare to
     * @return DataResult of PackageOverview objects
     */
    public static DataResult comparePackagesBetweenChannels(Long thisCid, Long otherCid) {
        Map<String, Object> params = new HashMap<>();
        params.put("this_cid", thisCid);
        params.put("other_cid", otherCid);

            SelectMode m = ModeFactory.getMode(
                    "Package_queries", "compare_managed_channel_packages");

            DataResult dr = m.execute(params);
            dr.setElaborationParams(new HashMap<>());
            return dr;
    }

    /**
     * Lookup which packages add/remove to make thisCid equal to otherCid channel.
     * @param thisCid channel id we want to update
     * @param otherCid channel id which we compare to
     * @param cmpType compare type
     * @return DataResult of PackageMergeDto objects
     */
    public static DataResult comparePackagesBetweenChannelsPreview(Long thisCid,
                         Long otherCid, String cmpType) {
        Map<String, Object> params = new HashMap<>();
        params.put("this_cid", thisCid);
        params.put("other_cid", otherCid);
        params.put("cmp_type", cmpType);

            SelectMode m = ModeFactory.getMode(
                    "Package_queries", "managed_channel_merge_preview");

            DataResult dr = m.execute(params);
            dr.setElaborationParams(new HashMap<>());
            return dr;
    }

    /**
     * Returns a DataResult containing PackageMergeDtos
     * @param user The User
     * @param label The label of the set we want
     * @return Returns the list of packages whose id's are in the given set
     */
    public static DataResult<PackageMergeDto> mergePackagesFromSet(User user, String label) {

        SelectMode m = ModeFactory.getMode("Package_queries",
                "managed_channel_merge_confirm");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("set_label", label);

        Map<String, Object> elabs = new HashMap<>();
        elabs.put("org_id", user.getOrg().getId());

        return makeDataResult(params, elabs, null, m);
    }

    /**
     * Merge packages to channel whos package_ids are in a set
     * @param user the user doing the pushing
     * @param cid the channel to push packages to
     * @param set the set of packages
     */
    public static void mergeChannelPackagesFromSet(User user, Long cid, RhnSet set) {
        Map<String, Object> params = new HashMap<>();
        params.put("uid", user.getId());
        params.put("cid", cid);
        params.put("set_label", set.getLabel());
        WriteMode del = ModeFactory.getWriteMode("Package_queries",
                                                 "merge_delete_packages_from_set");
        del.executeUpdate(params);
        WriteMode ins = ModeFactory.getWriteMode("Package_queries",
                                                 "merge_insert_channel_packages_in_set");
        ins.executeUpdate(params);
        RhnSetManager.store(set);
    }

    /**
     * Add packages to channel whos package_ids are in a set
     * @param user the user doing the pushing
     * @param cid the channel to push packages to
     * @param set the set of packages
     */
    public static void addChannelPackagesFromSet(User user, Long cid, RhnSet set) {
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("cid", cid);
        params.put("set_label", set.getLabel());
        WriteMode writeMode = ModeFactory.getWriteMode("Package_queries",
                "insert_channel_packages_in_set");
        writeMode.executeUpdate(params);
        RhnSetManager.store(set);
    }

    /**
     * List orphaned custom packages for an org
     * @param orgId the org
     * @param source list source packages instead of regular
     * @return list of package overview objects
     */
    public static DataResult<PackageOverview> listOrphanPackages(Long orgId, boolean source) {
        Map<String, Object> params = new HashMap<>();
        params.put("org_id", orgId);

        SelectMode m;
        if (!source) {
            m = ModeFactory.getMode(
                    "Package_queries", "orphan_packages");
        }
        else {
            m = ModeFactory.getMode(
                    "Package_queries", "orphan_source_packages");
        }

            DataResult<PackageOverview> dr = m.execute(params);
            dr.setElaborationParams(new HashMap<>());
            return dr;
    }

    /**
     * List all custom  packages for an org
     * @param orgId the org
     * @param source list source packages instead of regular
     * @return List of custom package (PackageOverview)
     */
    public static DataResult<PackageOverview> listCustomPackages(Long orgId, boolean source) {
        Map<String, Object> params = new HashMap<>();
        params.put("org_id", orgId);

        SelectMode m;
        if (!source) {
            m = ModeFactory.getMode(
                    "Package_queries", "all_custom_packages");
        }
        else {
            m = ModeFactory.getMode(
                    "Package_queries", "all_custom_source_packages");
        }

            DataResult<PackageOverview> dr = m.execute(params);
            Map<String, Object> elabs = new HashMap<>();
            elabs.put("org_id", orgId);
            dr.setElaborationParams(elabs);
            return dr;
    }

    /**
     * List all custom packages for an org
     * @param orgId the org
     * @param source list source packages instead of regular
     * @param pc page controller
     * @return List of custom package (PackageOverview)
     */
    public static DataResult<PackageOverview> listCustomPackages(Long orgId, boolean source, PageControl pc) {
        if (source) {
            if (List.of("provider", "channels").contains(pc.getSortColumn())) {
                pc.setSortColumn(null);
            }
            return new PagedSqlQueryBuilder("PS.id")
                    .select("PS.id AS ID, " +
                            "SRPM.name AS NVREA, " +
                            "PS.path as PATH")
                    .from("rhnPackageSource PS " +
                            "inner join rhnSourceRPM SRPM on PS.source_rpm_id = SRPM.id")
                    .where("PS.org_id = :org_id")
                    .run(Map.of("org_id", orgId), pc, PagedSqlQueryBuilder::parseFilterAsText, PackageOverview.class);
        }

        // We can't use aliases in WHERE clauses and using a subquery would defeat the idea of paged SQL query
        if ("nvrea".equals(pc.getFilterColumn())) {
            pc.setFilterColumn("PN.name");
        }
        return new PagedSqlQueryBuilder("P.id")
                .select("P.id AS ID, " +
                        "PN.name || '-' || evr_t_as_vre_simple(PE.evr) || '.' || PA.label AS NVREA, " +
                        "PP.name as provider, " +
                        "(select string_agg(C.name, ',') " +
                            "FROM rhnChannel C, rhnChannelPackage CP " +
                            "WHERE CP.package_id = P.id AND c.id = CP.channel_id) AS channels")
                .from("rhnPackage P " +
                        "inner join rhnPackageArch PA on P.package_arch_id = PA.id " +
                        "inner join rhnPackageName PN on P.name_id = PN.id " +
                        "inner join rhnPackageEVR PE on P.evr_id = PE.id " +
                        "left join rhnPackageKeyAssociation assoc on assoc.package_id = P.id " +
                        "left join rhnPackageKey KEY on KEY.id = assoc.key_id " +
                        "left join rhnPackageProvider PP on KEY.provider_id = PP.id")
                .countFrom("rhnPackage P")
                .where("P.org_id = :org_id")
                .run(Map.of("org_id", orgId), pc, PagedSqlQueryBuilder::parseFilterAsText, PackageOverview.class);
    }

    /**
     * List orphaned custom packages for an org
     * @param orgId the org
     * @param source list source packages instead of regular
     * @param pc page controller
     * @return list of package overview objects
     */
    public static DataResult<PackageOverview> listOrphanPackages(Long orgId, boolean source, PageControl pc) {
        if ("channels".equals(pc.getSortColumn())) {
            pc.setSortColumn(null);
        }

        if (source) {
            if ("provider".equals(pc.getSortColumn())) {
                pc.setSortColumn(null);
            }
            return new PagedSqlQueryBuilder("PS.id")
                    .select("PS.id AS ID, " +
                            "SRPM.name AS NVREA, " +
                            "PS.path as PATH")
                    .from("rhnPackageSource PS " +
                            "inner join rhnSourceRPM SRPM on PS.source_rpm_id = SRPM.id " +
                            "left join rhnPackage P on SRPM.id = P.source_rpm_id " +
                            "left join rhnChannelPackage CP on CP.package_id = P.id ")
                    .where("PS.org_id = :org_id AND CP.package_id is null")
                    .run(Map.of("org_id", orgId), pc, PagedSqlQueryBuilder::parseFilterAsText, PackageOverview.class);
        }

        // We can't use aliases in WHERE clauses and using a subquery would defeat the idea of paged SQL query
        if ("nvrea".equals(pc.getFilterColumn())) {
            pc.setFilterColumn("PN.name");
        }
        return new PagedSqlQueryBuilder("P.id")
                .select("P.id AS ID, " +
                        "PN.name || '-' || evr_t_as_vre_simple(PE.evr) || '.' || PA.label AS NVREA, " +
                        "PP.name as provider")
                .from("rhnPackage P " +
                        "inner join rhnPackageArch PA on P.package_arch_id = PA.id " +
                        "inner join rhnPackageName PN on P.name_id = PN.id " +
                        "inner join rhnPackageEVR PE on P.evr_id = PE.id " +
                        "left join rhnChannelPackage CP on CP.package_id = P.id " +
                        "left join rhnPackageKeyAssociation assoc on assoc.package_id = P.id " +
                        "left join rhnPackageKey KEY on KEY.id = assoc.key_id " +
                        "left join rhnPackageProvider PP on KEY.provider_id = PP.id")
                .where("P.org_id = :org_id AND CP.package_id is null")
                .run(Map.of("org_id", orgId), pc, PagedSqlQueryBuilder::parseFilterAsText, PackageOverview.class);
    }

    /**
     * list custom packages contained in a channel
     * @param cid the channel id
     * @param orgId the org id
     * @param source list source packages instead of regular
     * @param pc page controller
     * @return the list of custom package (package overview)
     */
    public static DataResult<PackageOverview> listCustomPackageForChannel(Long cid, Long orgId,
                                                                          boolean source, PageControl pc) {
        if (source) {
            if (List.of("provider", "channels").contains(pc.getSortColumn())) {
                pc.setSortColumn(null);
            }
            return new PagedSqlQueryBuilder("PS.id")
                    .select("PS.id AS ID, " +
                            "SRPM.name AS NVREA, " +
                            "PS.path as PATH")
                    .from("rhnPackageSource PS " +
                            "inner join rhnSourceRPM SRPM on PS.source_rpm_id = SRPM.id " +
                            "left join rhnPackage P on SRPM.id = P.source_rpm_id " +
                            "left join rhnChannelPackage CP on CP.package_id = P.id")
                    .where("PS.org_id = :org_id AND CP.channel_id = :cid")
                    .run(Map.of("org_id", orgId, "cid", cid), pc,
                            PagedSqlQueryBuilder::parseFilterAsText, PackageOverview.class);
        }

        // We can't use aliases in WHERE clauses and using a subquery would defeat the idea of paged SQL query
        if ("nvrea".equals(pc.getFilterColumn())) {
            pc.setFilterColumn("PN.name");
        }
        return new PagedSqlQueryBuilder("P.id")
                .select("P.id AS ID, " +
                        "PN.name || '-' || evr_t_as_vre_simple(PE.evr) || '.' || PA.label AS NVREA, " +
                        "PP.name as provider, " +
                        "(select string_agg(C.name, ',') " +
                        "FROM rhnChannel C, rhnChannelPackage CP " +
                        "WHERE CP.package_id = P.id AND c.id = CP.channel_id) AS channels")
                .from("rhnPackage P " +
                        "inner join rhnPackageArch PA on P.package_arch_id = PA.id " +
                        "inner join rhnPackageName PN on P.name_id = PN.id " +
                        "inner join rhnPackageEVR PE on P.evr_id = PE.id " +
                        "left join rhnPackageKeyAssociation assoc on assoc.package_id = P.id " +
                        "left join rhnPackageKey KEY on KEY.id = assoc.key_id " +
                        "left join rhnPackageProvider PP on KEY.provider_id = PP.id " +
                        "left join rhnChannelPackage CP on P.id = CP.package_id")
                .where("P.org_id = :org_id AND CP.channel_id = :cid")
                .run(Map.of("org_id", orgId, "cid", cid), pc,
                        PagedSqlQueryBuilder::parseFilterAsText, PackageOverview.class);
    }

    /**
     * list custom packages contained in a channel
     * @param cid the channel id
     * @param orgId the org id
     * @param source list source packages instead of regular
     * @return the list of custom package (package overview)
     */
    public static DataResult<PackageOverview> listCustomPackageForChannel(Long cid, Long orgId,
                                                         boolean source) {
        Map<String, Object> params = new HashMap<>();
        params.put("org_id", orgId);
        params.put("cid", cid);

        SelectMode m;
        if (!source) {
            m = ModeFactory.getMode(
                    "Package_queries", "custom_package_in_channel");
        }
        else {
            m = ModeFactory.getMode(
                    "Package_queries", "custom_source_package_in_channel");
        }

            DataResult<PackageOverview> dr = m.execute(params);
            Map<String, Object> elabs = new HashMap<>();
            elabs.put("org_id", orgId);
            dr.setElaborationParams(elabs);
            return dr;
    }

    /**
     * This deletes a package completely from the satellite including the
     *      physical rpm on the disk
     * @param ids the set of package ids
     * @param user the user doing the deleting
     */
    public static void deletePackages(Set<Long> ids, User user) {

        if (!user.hasRole(RoleFactory.CHANNEL_ADMIN)) {
            throw new PermissionException(RoleFactory.CHANNEL_ADMIN);
        }

        long start = System.currentTimeMillis();

        // Stuff the package IDs into an RhnSet that the rest of the queries
        // will work on
        RhnSet set = RhnSetDecl.PACKAGES_TO_REMOVE.create(user);

        for (Long id : ids) {
            set.addElement(id);
        }

        RhnSetManager.store(set);

        // Needed for subsequent queries
        WriteMode mode;
        Map<String, Object> params = new HashMap<>();
        params.put("set_label", set.getLabel());
        params.put("uid", user.getId());

        // First, capture all of the channels that have one or more of the packages
        // to delete (we'll need this later)
        SelectMode selectMode = ModeFactory.getMode("Package_queries",
            "determine_channels_for_packages_in_set");

        DataResult dataResult = selectMode.execute(params);
        Set<Map> channelIds = new HashSet<Map>(dataResult);

        // Clear server->package cache for all packages
        mode = ModeFactory.getWriteMode("Package_queries",
            "cleanup_needed_package_cache_from_set");

        mode.executeUpdate(params);


        // Clear server->package cache for all packages
        mode = ModeFactory.getWriteMode("Package_queries",
            "cleanup_package_changelog_from_set");

        mode.executeUpdate(params);

        // Clear server->package cache for all packages
        mode = ModeFactory.getWriteMode("Package_queries",
            "cleanup_package_files_from_set");
        mode.executeUpdate(params);

        // Schedule package files for deletion for all packages
        mode = ModeFactory.getWriteMode("Package_queries",
            "schedule_pkg_for_delete_from_set");

        mode.executeUpdate(params);

        // Delete link between channel and package
        mode = ModeFactory.getWriteMode("Package_queries",
            "cleanup_package_channels_from_set");

        mode.executeUpdate(params);

        // Delete all packages
        mode = ModeFactory.getWriteMode("Package_queries",
            "delete_packages_from_set");

        mode.executeUpdate(params);

        LOG.debug("Time to delete [{}] packages [{}] ms", ids.size(), System.currentTimeMillis() - start);

        start = System.currentTimeMillis();

        // For now, continue to use repeated calls to the managers rather than having the
        // calls take place using the data in the package IDs RhnSet
        List<Long> pList = new ArrayList<>(ids);
        for (Map channelIdData : channelIds) {
            Long channelId = (Long) channelIdData.get("channel_id");
            ChannelManager.refreshWithNewestPackages(channelId, "web.package_delete");
            ErrataCacheManager.deleteCacheEntriesForChannelPackages(channelId, pList);
        }

        LOG.debug("Time to update [{}] channels [{}] ms", channelIds.size(), System.currentTimeMillis() - start);
    }

    /**
     * This deletes a source packages completely including the
     *      physical rpm on the disk
     * @param ids the set of source package ids
     * @param user the user doing the deleting
     */
    public static void deleteSourcePackages(Set<Long> ids, User user) {

        if (!user.hasRole(RoleFactory.CHANNEL_ADMIN)) {
            throw new PermissionException(RoleFactory.CHANNEL_ADMIN);
        }

        long start = System.currentTimeMillis();

        // Stuff the package IDs into an RhnSet that the rest of the queries
        // will work on
        RhnSet set = RhnSetDecl.PACKAGES_TO_REMOVE.create(user);

        for (Long id : ids) {
            set.addElement(id);
        }

        RhnSetManager.store(set);

        // Needed for subsequent queries
        WriteMode mode;
        Map<String, Object> params = new HashMap<>();
        params.put("set_label", set.getLabel());
        params.put("uid", user.getId());

        // Delete RPMS
        mode = ModeFactory.getWriteMode("Package_queries",
                "schedule_source_pkg_for_delete_from_set");
        mode.executeUpdate(params);

        // Delete source packages
        mode = ModeFactory.getWriteMode("Package_queries",
                "delete_package_sources_from_set");
        mode.executeUpdate(params);

        LOG.debug("Time to delete [{}] packages [{}] ms", ids.size(), System.currentTimeMillis() - start);
    }

    /**
     * guestimate a package based on channel id, name and evr
     * @param channelId the channel
     * @param nameId the name
     * @param evrId the evr id
     * @param org the org
     * @return first package object found during the search
     */
    public static Package guestimatePackageByChannel(Long channelId, Long nameId,
            Long evrId, Org org) {
        Map<String, Object> params = new HashMap<>();
        params.put("cid", channelId);
        params.put("nameId", nameId);
        params.put("evrId", evrId);
        SelectMode m = ModeFactory.getMode(
                "Package_queries", "guestimate_package_by_channel");

        DataResult dr = m.execute(params);
        if (dr != null && !dr.isEmpty()) {
            return PackageFactory.lookupByIdAndOrg(
                    (Long) ((Map)dr.get(0)).get("id"), org);
        }

        return null;
    }

    /**
     * guestimate a package based on system id, name and evr
     * @param systemId the channel
     * @param nameId the name
     * @param evrId the evr id
     * @param archId the arch id
     * @param org the org
     * @return first package object found during the search
     */
    public static Package guestimatePackageBySystem(Long systemId, Long nameId,
            Long evrId, Long archId, Org org) {
        SelectMode m;
        Map<String, Object> params = new HashMap<>();
        params.put("sid", systemId);
        params.put("nameId", nameId);
        params.put("evrId", evrId);

        if (archId != null && archId != 0) {
            params.put("archId", archId);
            m = ModeFactory.getMode(
                    "Package_queries", "guestimate_package_by_system_arch");
        }
        else {
            m = ModeFactory.getMode(
                    "Package_queries", "guestimate_package_by_system");
        }

        DataResult dr = m.execute(params);
        if (dr != null && !dr.isEmpty()) {
            return PackageFactory.lookupByIdAndOrg(
                    (Long) ((Map)dr.get(0)).get("id"), org);
        }

        return null;
    }

    /**
     * Returns the list of packages installed on at least one system in the SSM, along with
     * the count of how many systems each package is installed on.
     *
     * @param user user
     * @return list of {@link com.redhat.rhn.frontend.dto.SsmRemovePackageListItem}
     */
    public static DataResult<SsmRemovePackageListItem> packagesFromServerSet(User user) {

        SelectMode m = ModeFactory.getMode("Package_queries", "packages_from_server_set");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("set_label", RhnSetDecl.SYSTEMS.getLabel());

        return m.execute(params);
    }

    /**
     * Returns the list of packages that are on at least one system in the SSM that can
     * be upgraded, along with a count of how many systems each package is installed on.
     *
     * @param user user
     * @return list of {@link com.redhat.rhn.frontend.dto.SsmUpgradablePackageListItem}
     */
    public static DataResult<SsmUpgradablePackageListItem> upgradablePackagesFromServerSet(User user) {

        SelectMode m = ModeFactory.getMode("Package_queries", "ssm_packages_for_upgrade");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());

        return m.execute(params);
    }

    /**
     * Create all repoentries for a channel's packages if needed
     * @param cid the channel id
     */
    public static synchronized void createRepoEntrys(Long cid) {
        Map<String, Object> params = new HashMap<>();
        params.put("cid", cid);
        WriteMode writeMode = ModeFactory.getWriteMode("Package_queries",
            "create_repo_entrys");
        writeMode.executeUpdate(params);
    }

    private static void updateRepoEntry(Long packageId, String xml, String type) {
        if (!ConfigDefaults.get().useDBRepodata()) {
            return;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("pid", packageId);
        byte[] bytes = CompressionUtil.gzipCompress(xml);

        params.put("xml", bytes);
        WriteMode writeMode = ModeFactory.getWriteMode("Package_queries",
                "insert_" + type + "_xml");
        writeMode.executeUpdate(params);
    }

    /**
     * Update the primary XML for a package
     * @param packageId the package id
     * @param primaryXml the raw xml
     */
    public static void updateRepoPrimary(Long packageId, String primaryXml) {
        updateRepoEntry(packageId, primaryXml, "primary");
    }

    /**
     *
     * @param packageId the package id
     * @param filelistXml the raw xml
     */
    public static void updateRepoFileList(Long packageId, String filelistXml) {
        updateRepoEntry(packageId, filelistXml, "filelist");
    }

    /**
     *
     * @param packageId the package id
     * @param otherXml the raw xml
     */
    public static void updateRepoOther(Long packageId, String otherXml) {
        updateRepoEntry(packageId, otherXml, "other");
    }


    /**
     * utility method for getting the repodata for a package
     * @param packageId the package id
     * @return A list of package dto objects
     */
    public static DataResult getRepoData(Long packageId) {
        SelectMode m = ModeFactory.getMode("Package_queries", "lookup_repodata");
        Map<String, Object> params = new HashMap<>();
        params.put("pid", packageId);

        return m.execute(params);
    }

    private static Package findDebugPackage(User user, Package pack, String type) {
        PackageEvr evr = pack.getPackageEvr();
        String name = pack.getPackageName().getName() + "-" + type;
        List<Package> list =  PackageFactory.lookupByNevra(user.getOrg(),
                name, evr.getVersion(), evr.getRelease(), null, pack.getPackageArch());
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    /**
     * Find a debuginfo package for a given package
     * @param user The User doing the search
     * @param pack the package we need a debug info for
     * @return The Package object that is debug info, or null if not found
     */
    public static Package findDebugInfo(User user, Package pack) {
        return findDebugPackage(user, pack, "debuginfo");
    }

    /**
     * Find a debugsource package for a given package
     * @param user The User doing the search
     * @param pack the package we need a debug source for
     * @return The Package object that is debug source, or null if not found
     */
    public static Package findDebugSource(User user, Package pack) {
        return findDebugPackage(user, pack, "debugsource");
    }


    private static String getAssociatedRelease(Package pack) {
        for (Channel chan : pack.getChannels()) {
            for (DistChannelMap map : chan.getDistChannelMaps()) {
                return map.getRelease();
            }
        }
        return null;
    }

    /**
     * Guess the package URL for a debugInfo rpm
     * @param pack the package to guess a debugInfo rpm url for
     * @return the url
     */
    public static String generateFtpDebugPath(Package pack) {

        String release = getAssociatedRelease(pack);
        // generate ftp link only for rhel5 packages
        if (release == null || !release.startsWith("5")) {
            return null;
        }


        PackageEvr evr = pack.getPackageEvr();
        PackageArch arch = pack.getPackageArch();
        String ftpMachine = Config.get().getString("ftp_server", "ftp.redhat.com");
        String dbgFilename = pack.getPackageName().getName() +
            "-debuginfo-" +
            evr.getVersion() + "-" +
            evr.getRelease() + "." +
            arch.getLabel() + "." +
            arch.getArchType().getLabel();

        return "ftp://" + ftpMachine + "/pub/redhat/linux/enterprise/" +
                    release + "/en/os/" +
                    arch.getLabel() + "/Debuginfo/" + dbgFilename;
    }

    /**
     * Gets a package changelog from the file system
     * @param pkg the package to get
     * @return the changelog as a string or null if package isn't readable/doesn't exist
     */
    public static String getPackageChangeLog(Package pkg) {

        String path = pkg.getPath();
        if (path == null) {
            return null;
        }
        File f = new File(Config.get().getString(ConfigDefaults.MOUNT_POINT), path);
        if (!f.canRead()) {
            return null;
        }

        List<String> cmd = new ArrayList<>();
        cmd.add(Config.get().getString("rpm.path", "/bin/rpm"));
        cmd.add("-qp");
        cmd.add("--changelog");
        cmd.add(f.getPath());

        SystemCommandExecutor ce = new SystemCommandExecutor();
        ce.setLogError(false);
        ce.execute(cmd.toArray(new String[0]));
        return ce.getLastCommandOutput();
    }

    /**
     * Throw exception, if any of the packages aren't accessible by user or
     * aren't compatible with provided channel arch
     * @param user user
     * @param channel channel
     * @param packageIds package ids
     * @param checkArchCompat optionally arch compatibility doesn't have to be checked
     * (f.e. when removing packages, only orgt access is important)
     */
    public static void verifyPackagesChannelArchCompatAndOrgAccess(
            User user, Channel channel, List<Long> packageIds, boolean checkArchCompat) {
        Long orgId = user.getOrg().getId();
        DataResult<Row> dr = PackageFactory.getPackagesChannelArchCompatAndOrgAccess(
                orgId, channel.getId(), packageIds);
        List<Long> found = new ArrayList<>();
        List<Long> archNonCompat = new ArrayList<>();
        List<Long> orgNoAccess = new ArrayList<>();
        for (Row m : dr) {
            found.add((Long) m.get("id"));
            if (m.get("package_arch_id") == null) {
                archNonCompat.add((Long) m.get("id"));
            }
            if ((m.get("org_package") == null ||
                    orgId.compareTo((Long) m.get("org_package")) != 0) &&
                    m.get("org_access") == null &&
                    m.get("shared_access") == null) {
                orgNoAccess.add((Long) m.get("id"));
            }
        }
        List<Long> missing = new ArrayList<>(packageIds);
        missing.removeAll(found);
        orgNoAccess.addAll(missing);
        if (!orgNoAccess.isEmpty()) {
            LocalizationService ls = LocalizationService.getInstance();
            String msg = "User: " + user.getLogin() +
                    " does not have access to packages: " +
                    orgNoAccess;
            throw new PermissionException(msg,
                    ls.getMessage("permission.jsp.title.package"),
                    ls.getMessage("permission.jsp.summary.package"));
        }
        if (checkArchCompat && !archNonCompat.isEmpty()) {
            throw new IncompatibleArchException(channel.getChannelArch(), archNonCompat);
        }
    }

    /**
     * build package nevra out of the name, evr, arch identifiers
     * @param nameId name id
     * @param evrId evr id
     * @param archId arch id
     * @return nevra
     */
    public static String buildPackageNevra(Long nameId, Long evrId, Long archId) {
        PackageName pn = null;
        PackageArch pa = null;
        PackageEvr pevr = null;
        if (nameId != null) {
            pn = PackageFactory.lookupPackageName(nameId);
            if (evrId != null) {
                pevr = PackageEvrFactory.lookupPackageEvrById(evrId);
            }
            if (archId != null) {
                pa = PackageFactory.lookupPackageArchById(archId);
            }
        }
        return buildPackageNevra(pn, pevr, pa);
    }

    /**
     * build package nevra out of the name, evr, arch identifiers
     * @param name name
     * @param evr evr
     * @param arch arch
     * @return nevra
     */
    public static String buildPackageNevra(PackageName name, PackageEvr evr,
            PackageArch arch) {
        String nevra = "";
        if (name != null) {
            nevra += name.getName();
            if (evr != null) {
                nevra += "-" + evr.getVersion() + "-" + evr.getRelease();
                if (!StringUtils.isEmpty(evr.getEpoch())) {
                    nevra += ":" + evr.getEpoch();
                }
            }
            if (arch != null) {
                nevra += "." + arch.getLabel();
            }
        }
        return nevra;
    }

    /**
     * Helper method to figure out the dependency modifier. I honestly have no clue
     * why the bitwise ANDs work or what the sense field in the db really means. This was
     * pretty much a line for line port of the perl code.
     * @param sense A number whose number can tell us what kind of modifier is needed
     * @param version The version of the dependency we're investigating
     * @return Returns a string in the form of something like {@literal '>= 4.1-3'}
     */
    public static String getDependencyModifier(Long sense, String version) {
        StringBuilder depmod = new StringBuilder();

        if (sense != null) { //how ironic ;)
            int senseIntVal = sense.intValue();
            //Bitwise AND with 4 --> '>'
            if ((senseIntVal & 4) > 0) {
              depmod.append(">");
            }
            //Bitwise AND with 2 --> '<'
            else if ((senseIntVal & 2) > 0) {
                depmod.append("<");
            }
            //Bitwise AND with 8 tells us whether or not this should have an '=' on it
            if ((senseIntVal & 8) > 0) {
                depmod.append("=");
            }
            //Add the version so we get something like '<= 4.0-1'
            depmod.append(" ");
            depmod.append(version);
        }
        else {
            //Robin thinks that this represents a "anything but this version" scenario.
            depmod.append("-");
            depmod.append(version);
        }

        return depmod.toString();
    }

    /**
     * Util to output package name + evr: krb5-devel-1.3.4-47
     *
     * @param name the package name
     * @param evr  the evr
     * @return String name and evr
     */
    public static String getNevr(PackageName name, PackageEvr evr) {
        return name.getName() + "-" + evr.toString();
    }

    /**
     * Returns the list of installed ptf on the given server
     * @param sid Server Id
     * @param pc package control
     * @return list of packages marked as master ptf installed on given server
     */
    public static DataResult<PackageListItem> systemPtfList(Long sid, PageControl pc) {
        return PackageManager.getPackagesPerSystem(sid, "system_ptfs_list", pc);
    }

    /**
     * Returns available of ptf for given server
     * @param sid Server Id
     * @param pc package control
     * @return list of packages marked as master ptf available for given server
     */
    public static DataResult<PackageListItem> systemAvailablePtf(Long sid, PageControl pc) {
        return PackageManager.getPackagesPerSystem(sid, "system_available_ptfs", pc);
    }
}
