/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.manager.profile;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.action.rhnpackage.PackageAction;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.NoBaseChannelFoundException;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.rhnpackage.MissingPackagesException;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.profile.DuplicateProfileNameException;
import com.redhat.rhn.domain.rhnpackage.profile.Profile;
import com.redhat.rhn.domain.rhnpackage.profile.ProfileFactory;
import com.redhat.rhn.domain.rhnpackage.profile.ProfileType;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.PackageListItem;
import com.redhat.rhn.frontend.dto.PackageMetadata;
import com.redhat.rhn.frontend.dto.ProfileDto;
import com.redhat.rhn.frontend.dto.ProfileOverviewDto;
import com.redhat.rhn.frontend.dto.ProfilePackageOverviewDto;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.manager.BaseManager;
import com.redhat.rhn.manager.MissingEntitlementException;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.rhnpackage.PackageManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ProfileManager
 */
public class ProfileManager extends BaseManager {

    private static Logger log = LogManager.getLogger(ProfileManager.class);
    public static final String OPTION_REMOVE = "remove";
    public static final String OPTION_SUBSCRIBE = "subscribe";

    /**
     * Removes the given profile.
     * @param profile Profile to delete
     * @return number of profiles affected (should be 1 or 0)
     */
    public static int deleteProfile(Profile profile) {
        return ProfileFactory.remove(profile);
    }

    /**
     * Creates and persists a Server Package Profile for the given Server
     * with the name and description.
     * @param type ProfileType we want to create
     * @param user Logged in User
     * @param channel Channel that this Profile is created from
     * @param name Name of profile
     * @param description Profile description
     * @return Profile for the given Server.
     */
    public static Profile createProfile(ProfileType type, User user, Channel channel,
            String name, String description) {
        if (isNameInUse(name, user.getOrg().getId())) {
            throw new DuplicateProfileNameException(name);
        }
        if (channel == null) {
            throw new NoBaseChannelFoundException("Channel is null when trying to create " +
                    "a profile.");
        }

        Profile p = ProfileFactory.createProfile(type);
        p.setName(name);
        p.setDescription(description);
        p.setOrg(user.getOrg());
        p.setBaseChannel(channel);
        ProfileFactory.save(p);
        return p;
    }

    /**
     * Creates and persists a Server Package Profile for the given Server
     * with the name and description.
     * @param user Logged in User
     * @param server Server which profile should be associated with.
     * @param name Name of profile
     * @param description Profile description
     * @return Profile for the given Server.
     */
    public static Profile createProfile(User user, Server server,
            String name, String description) {
        Channel baseChannel = ChannelFactory.getBaseChannel(server.getId());
        return createProfile(ProfileFactory.TYPE_NORMAL, user,
                baseChannel, name, description);
    }

    private static boolean isNameInUse(String name, Long orgid) {
        return (ProfileFactory.findByNameAndOrgId(name, orgid) != null);
    }

    /**
     * Copies the packages from a given Server to the given Profile.
     * @param server Server whose packages are to be copied.
     * @param profile Profile where packages are copied to.
     */
    public static void copyFrom(Server server, Profile profile) {
        WriteMode m = ModeFactory.getWriteMode("profile_queries",
                "delete_package_profile");
        Map<String, Object> params = new HashMap<>();
        params.put("sid", server.getId());
        params.put("prid", profile.getId());
        m.executeUpdate(params);

        m = ModeFactory.getWriteMode("profile_queries",
                "insert_package_profile");
        params = new HashMap<>();
        params.put("sid", server.getId());
        params.put("prid", profile.getId());
        m.executeUpdate(params);
    }

    /**
     * Returns a list of Profiles which are compatible with the given server.
     * @param server Server whose profiles we want.
     * @param org Org owner
     * @return  a list of Profiles which are compatible with the given server.
     */
    public static List<Profile> compatibleWithServer(Server server, Org org) {
        return ProfileFactory.compatibleWithServer(server, org);
    }

    private static DataResult<PackageListItem> canonicalProfilePackages(Long prid, Long orgid,
            PageControl pc) {

        SelectMode m = ModeFactory.getMode("Package_queries",
                "profile_canonical_package_list");
        Map<String, Object> params = new HashMap<>();
        params.put("prid", prid);
        params.put("org_id", orgid);
        Map<String, Object> elabParams = new HashMap<>();
        return makeDataResult(params, elabParams, pc, m);
    }

    private static DataResult<PackageListItem> canonicalSystemsPackages(Long sid, Long orgid,
            PageControl pc) {

        SelectMode m = ModeFactory.getMode("Package_queries",
                "system_canonical_package_list");
        Map<String, Object> params = new HashMap<>();
        params.put("sid", sid);
        params.put("org_id", orgid);
        Map<String, Object> elabParams = new HashMap<>();
        return makeDataResult(params, elabParams, pc, m);
    }

    /**
     * compares the given lists of Packages.
     *
     * NOTE: For lists that contain entries with the same package with
     * multiple versions we show the entries as "OTHER_ONLY" when the version
     * isn't on one of the other lists.
     *
     * @param profiles Packages to compare
     * @param systems Packages to compare
     * @param param comparison parameter
     * @return List of differences
     */
    public static List<PackageMetadata> comparePackageLists(DataResult<PackageListItem> profiles,
            DataResult<PackageListItem> systems, String param) {
        List<PackageMetadata> result = new LinkedList<>();
        Map<String, List<PackageListItem>> profilesNameIdMap = buildPackagesMap(profiles);
        Map<String, List<PackageListItem>> systemsNameIdMap = buildPackagesMap(systems);

        if (log.isDebugEnabled()) {
            log.debug("profilesIdComboMap: {}", profilesNameIdMap);
            log.debug("systemsIdComboMap: {}", systemsNameIdMap);
        }

        // Here is the real work.  Iterate over the list of packages in the
        // system and see what matches we get against the profile list.

        // skipPkg is used to store the names of packages once they are
        // identified as either having a matching package in both lists (sys & prof)
        // or they have been identified as being valid difference.  This purpose
        // of having this set is to avoid processing the same package multiple times.
        Set<String> skipPkg = new HashSet<>();
        for (Object key : systemsNameIdMap.keySet()) {
            List<PackageListItem> syslist = systemsNameIdMap.get(key);
            List<PackageListItem> plist = profilesNameIdMap.get(key);
            if (plist == null) {
                // No packages in profile with same name.  We know its only in the System
                for (PackageListItem packageListItemIn : syslist) {
                    PackageListItem syspkgitem = (PackageListItem) packageListItemIn;
                    PackageMetadata pm = createPackageMetadata(syspkgitem,
                            null, PackageMetadata.KEY_THIS_ONLY, param);
                    log.debug("plist is null - adding KEY_THIS_ONLY: {}", pm.getSystem().getVersion());
                    skipPkg.add(syspkgitem.getNevra());
                    result.add(pm);
                }
            }
            else {
                // We have packages on the system that are also in the Profile.  If either
                // the system or the profile list has more than one version of a package
                // installed we need to run a different algorithm.
                log.debug("syslist.size: {} plist.size: {}", syslist.size(), plist.size());
                if (syslist.size() > 1 || plist.size() > 1) {
                    Map<String, PackageMetadata> compareMap = new HashMap<>();
                    for (PackageListItem packageListItemIn : syslist) {
                        PackageListItem syspkgitem = (PackageListItem) packageListItemIn;
                        for (int j = 0; j < plist.size(); j++) {

                            PackageListItem profpkgitem = (PackageListItem) plist.get(j);

                            if (skipPkg.contains(profpkgitem.getNevra())) {
                                // this package was evaluated on a previous pass through
                                // the plist and identified as a match on the syslist;
                                // therefore, it may be skipped
                                continue;
                            }
                            log.debug("Checking on : {}", profpkgitem.getEvr());

                            if (compareArch(syspkgitem.getArch(),
                                    profpkgitem.getArch()) != 0) {
                                // if the arch of the packages doesn't match, we don't
                                // need to compare the EVR; therefore, if at end of the
                                // list, add both packages to the result
                                if ((j + 1) == plist.size()) {
                                    PackageMetadata pm = createPackageMetadata(
                                            syspkgitem, null,
                                            PackageMetadata.KEY_THIS_ONLY, param);
                                    skipPkg.add(syspkgitem.getNevra());
                                    result.add(pm);

                                    pm = createPackageMetadata(null, profpkgitem,
                                            PackageMetadata.KEY_OTHER_ONLY, param);
                                    skipPkg.add(profpkgitem.getNevra());
                                    result.add(pm);
                                }
                            }
                            else {
                                PackageMetadata pm = compareAndCreatePackageMetaData(
                                        syspkgitem, profpkgitem, param);
                                String evrKey = pm.getSystemEvr() + "|" +
                                        pm.getOtherEvr();
                                // If the package exists on one but not the other we
                                // need to add it to the compare map
                                if (pm.getComparisonAsInt() !=
                                        PackageMetadata.KEY_NO_DIFF) {

                                    if ((j + 1) == plist.size()) {
                                        // this is the last entry in plist; therefore,
                                        // this must be a difference between pkgs
                                        log.debug("Adding to cm: {} comp: {}", evrKey, pm.getComparison());
                                        pm.setComparison(
                                                PackageMetadata.KEY_OTHER_ONLY);
                                        compareMap.put(evrKey, pm);
                                        skipPkg.add(syspkgitem.getNevra());
                                        skipPkg.add(profpkgitem.getNevra());
                                    }
                                }
                                else {
                                    log.debug("Removing from cm: {}", evrKey);
                                    compareMap.remove(evrKey);
                                    skipPkg.add(profpkgitem.getNevra());
                                    // pkg found in both plist & syslist, skip to next
                                    // syslist entry
                                    break;
                                }
                            }
                        }
                        if (!skipPkg.contains(syspkgitem.getNevra())) {

                            // reached end of plist w/o finding match in syslist
                            // or recording a difference; therefore, add one now
                            log.debug("Checking on : {}", syspkgitem.getEvr());

                            PackageMetadata pm = createPackageMetadata(syspkgitem,
                                    null, PackageMetadata.KEY_THIS_ONLY, param);

                            log.debug("*** adding a PM(1): {}", pm.hashCode());
                            skipPkg.add(syspkgitem.getNevra());
                            result.add(pm);
                        }
                    }
                    // Copy into the result map
                    for (PackageMetadata pm : compareMap.values()) {
                        log.debug("*** adding a PM(2): {}", pm.hashCode());
                        result.add(pm);
                    }
                }
                // Else the system and profile list just have one rev so we
                // can do a standard compare
                else {
                    PackageListItem syspkgitem = (PackageListItem) syslist.get(0);
                    PackageListItem profpkgitem = (PackageListItem) plist.get(0);

                    if (compareArch(syspkgitem.getArch(), profpkgitem.getArch()) != 0) {

                        // pkg arches do not match; therefore, no need to check evr
                        PackageMetadata pm = createPackageMetadata(syspkgitem, null,
                                PackageMetadata.KEY_THIS_ONLY, param);
                        skipPkg.add(syspkgitem.getNevra());
                        result.add(pm);

                        pm = createPackageMetadata(null, profpkgitem,
                                PackageMetadata.KEY_OTHER_ONLY, param);
                        skipPkg.add(profpkgitem.getNevra());
                        result.add(pm);
                    }
                    else {
                        PackageMetadata pm = compareAndCreatePackageMetaData(syspkgitem,
                                profpkgitem, param);
                        if (pm != null && pm.getComparisonAsInt() !=
                                PackageMetadata.KEY_NO_DIFF) {
                            log.debug("*** adding a PM(3): {}", pm.hashCode());
                            result.add(pm);
                        }
                    }
                    skipPkg.add(profpkgitem.getNevra());
                    skipPkg.add(syspkgitem.getNevra());
                }
            }
        }

        // Reverse of above so we can check for pkgs that are *only* in the profile
        for (String key : profilesNameIdMap.keySet()) {
            List<PackageListItem> syslist = systemsNameIdMap.get(key);
            List<PackageListItem> plist = profilesNameIdMap.get(key);

            if (syslist == null) {
                // No packages in system with same name.  We know its only in the Profile
                for (PackageListItem packageListItemIn : plist) {
                    PackageMetadata pm = createPackageMetadata(null,
                            (PackageListItem) packageListItemIn, PackageMetadata.KEY_OTHER_ONLY,
                            param);

                    result.add(pm);
                }
            }
            else {
                for (PackageListItem packageListItemIn : plist) {
                    PackageListItem profpkgitem = (PackageListItem) packageListItemIn;

                    if (!skipPkg.contains(profpkgitem.getNevra())) {

                        PackageMetadata pm = createPackageMetadata(
                                null, profpkgitem, PackageMetadata.KEY_OTHER_ONLY, param);
                        log.debug("*** adding a PM(4): {}", pm.hashCode());
                        result.add(pm);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Build a map of packages based on the list of PackageListItems provided.
     * @param packageListItems - set of PackageListItems to be processed
     * @return Map where the key is the package name Id and the value is a list
     * of evr values for each of the packages associated with that name.
     * E.g. for kernel, the nameid might be 23 and there may be multiple
     * versions of that package in the list 2.1, 2.2, 2.3...etc
     */
    private static Map<String, List<PackageListItem>> buildPackagesMap(DataResult<PackageListItem> packageListItems) {
        Map<String, List<PackageListItem>> packages = new HashMap<>();

        for (PackageListItem item : packageListItems) {
            // We actually put *each* package in a sub-list in the map.
            // This is so we can have a List containing each package
            // by name.  Used for when we have multiple revs of the same
            // package, kernel-2.1, kernel-2.2, kernel-2.3 etc..
            String mapId = item.getMapHash();
            List<PackageListItem> list = packages.get(mapId);
            if (list == null) {
                list = new LinkedList<>();
            }
            list.add(item);
            packages.put(mapId, list);
        }
        return packages;
    }

    /*
     * Compare the arches provided.
     * @param a1 - arch value
     * @param a2 - arch value
     * @return returns 0 if arch are equal; otherwise, return 1
     */
    private static int compareArch(String a1, String a2) {
        if (((a1 == null) && (a2 != null)) ||
                ((a1 != null) && (a2 == null))) {
            return 1;
        }

        if (((a1 == null) && (a2 == null)) || (a1.equals(a2))) {
            return 0;
        }
        return 1;
    }

    private static PackageMetadata compareAndCreatePackageMetaData(
            PackageListItem syspkgitem, PackageListItem profpkgitem, String param) {

        log.debug("    Sys: {} get: {}", syspkgitem.getName(), syspkgitem.getVersion());
        log.debug("    Pro: {} get: {}", profpkgitem.getName(), profpkgitem.getVersion());

        PackageMetadata retval = null;
        PackageEvr evr1 = new PackageEvr(
                syspkgitem.getEpoch() == null ? "0" : syspkgitem.getEpoch(),
                syspkgitem.getVersion(),
                syspkgitem.getRelease(),
                syspkgitem.getPackageType()
        );

        PackageEvr evr2 = new PackageEvr(
                profpkgitem.getEpoch() == null ? "0" : profpkgitem.getEpoch(),
                profpkgitem.getVersion(),
                profpkgitem.getRelease(),
                syspkgitem.getPackageType()
        );
        int rc = evr1.compareTo(evr2);
        log.debug("    rc: {}", rc);

        // do nothing if they are equal
        if (rc < 0) {
            retval = createPackageMetadata(
                    syspkgitem,
                    profpkgitem,
                    PackageMetadata.KEY_OTHER_NEWER,
                    param);

        }
        else if (rc > 0) {
            retval = createPackageMetadata(
                    syspkgitem,
                    profpkgitem,
                    PackageMetadata.KEY_THIS_NEWER,
                    param);

        }
        else if (rc == 0) {
            retval = createPackageMetadata(
                    syspkgitem,
                    profpkgitem,
                    PackageMetadata.KEY_NO_DIFF,
                    param);
        }
        return retval;
    }

    /**
     * Returns a DataResult with a diff of the server's packages and those
     * in the profile.
     * @param sid Server whose packages are to be compared.
     * @param sid1 Server whose packages should be used in the comparison.
     * @param orgid Org owner
     * @param pc PageControl
     * @return a DataResult with a diff of the server's packages and those
     * in the profile.
     */
    public static DataResult<PackageMetadata> compareServerToServer(Long sid,
            Long sid1, Long orgid, PageControl pc) {

        Server source = ServerFactory.lookupById(sid1);

        if (!isComparable(sid) || !isComparable(sid1)) {
            throw new MissingEntitlementException();
        }

        // passing in null PageControls since we want ALL of the records
        // so we can reconcile them here.
        DataResult<PackageListItem> othersystems = canonicalSystemsPackages(sid1, orgid, null);
        DataResult<PackageListItem> systems = canonicalSystemsPackages(sid, orgid, null);

        List<PackageMetadata> result = comparePackageLists(othersystems, systems, source.getName());

        // this has to return a DataResult full of PackageMetadata
        Collections.sort(result);
        return prepareList(result, pc);
    }

    /**
     * Checks if the given sid corresponds to a system whose package list can be
     * compared to others.
     *
     * @param sid the sid
     * @return true, if is comparable
     */
    private static boolean isComparable(Long sid) {
        return SystemManager.hasEntitlement(sid, EntitlementManager.MANAGEMENT) ||
               SystemManager.hasEntitlement(sid, EntitlementManager.SALT);
    }

    /**
     * Returns a DataResult with a diff of the server's packages and those
     * in the profile.
     * @param sid Server whose packages are to be compared.
     * @param prid Profile whose packages should be used in the comparison.
     * @param orgid Org owner
     * @param pc PageControl
     * @return a DataResult with a diff of the server's packages and those
     * in the profile.
     */
    public static DataResult<PackageMetadata> compareServerToProfile(Long sid,
            Long prid, Long orgid, PageControl pc) {

        // passing in null PageControls since we want ALL of the records
        // so we can reconcile them here.
        DataResult<PackageListItem> profiles = canonicalProfilePackages(prid, orgid, null);
        DataResult<PackageListItem> systems = canonicalSystemsPackages(sid, orgid, null);
        List<PackageMetadata> result = comparePackageLists(profiles, systems, null);

        // this has to return a DataResult full of PackageMetadata
        Collections.sort(result);
        return prepareList(result, pc);
    }

    /**
     * Prepares the list of packages to be synced for comfirmation.
     * @param sid Server involved in sync.
     * @param prid Profile we're syncing with.
     * @param orgid Org id
     * @param pc PageControl
     * @param pkgIdCombos Set of packages selected.
     * @return DataResult of PackageMetadata's suitable for listview display.
     */
    public static DataResult<PackageMetadata> prepareSyncToProfile(Long sid, Long prid,
            Long orgid, PageControl pc, Set<String> pkgIdCombos) {
        Map<String, PackageMetadata> profilesMap = new HashMap<>();
        List<PackageMetadata> packagesToSync = new ArrayList<>();
        // seems like a waste, but that's how it works
        DataResult<PackageMetadata> profiles = compareServerToProfile(sid, prid, orgid, null);

        // in order to search the list by combo id (name_id|evr_id|arch_id),
        // it's easiest to create a map instead of looping through n times where n
        // is the size of the RhnSet.
        for (PackageMetadata pm : profiles) {
            profilesMap.put(pm.getIdCombo(), pm);
        }

        // find all of the items in profiles which are in RhnSet
        for (String pkgIdCombo : pkgIdCombos) {
            PackageMetadata pm = profilesMap.get(pkgIdCombo);
            pm.updateActionStatus();
            packagesToSync.add(pm);
        }

        Collections.sort(packagesToSync);
        return prepareList(packagesToSync, pc);
    }

    /**
     * Prepares the list of packages to be synced for comfirmation.
     * @param sid Server involved in sync.
     * @param sid1 Profile we're syncing with.
     * @param orgid Org id
     * @param pc PageControl
     * @param pkgIdCombos Set of packages selected.
     * @return DataResult of PackageMetadata's suitable for listview display.
     */
    public static DataResult<PackageMetadata> prepareSyncToServer(Long sid, Long sid1,
            Long orgid, PageControl pc, Set<String> pkgIdCombos) {

        Map<String, PackageMetadata> profilesMap = new HashMap<>();
        List<PackageMetadata> packagesToSync = new ArrayList<>();
        // seems like a waste, but that's how it works
        DataResult<PackageMetadata> profiles = compareServerToServer(sid, sid1, orgid, null);
        if (log.isDebugEnabled()) {
            log.debug("  profiles .. {}", profiles);
        }

        // in order to search the list by combo id (name_id|evr_id|arch_id),
        // it's easiest to create a map instead of looping through n times where n
        // is the size of the RhnSet.
        for (PackageMetadata pm : profiles) {

            if (log.isDebugEnabled()) {
                log.debug("  pm, putting: {}", pm.getIdCombo());
            }

            profilesMap.put(pm.getIdCombo(), pm);
        }

        // find all of the items in profiles which are in RhnSet
        for (String pkgIdCombo : pkgIdCombos) {
            if (log.isDebugEnabled()) {
                log.debug("  rse, fetching: {}", pkgIdCombo);
            }
            PackageMetadata pm = (PackageMetadata) profilesMap.get(pkgIdCombo);
            if (pm != null) {
                pm.updateActionStatus();
                packagesToSync.add(pm);
            }
        }

        Collections.sort(packagesToSync);
        return prepareList(packagesToSync, pc);
    }

    /**
     * Syncs the given server id to the given profile id.
     * @param user Current user
     * @param sid Server id to be affected.
     * @param sid1 Profile id to be used to sync.
     * @param pkgIdCombos Set of packages which will be synced.
     * @param missingoption Defines what do to if packages go missing.  null means
     * ask the user.
     * @param earliest The earliest Date to perform this action
     * @return The PackageAction which was scheduled containing the sync information.
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    public static PackageAction syncToSystem(User user, Long sid, Long sid1,
            Set<String> pkgIdCombos, String missingoption, Date earliest)
        throws TaskomaticApiException {

        if (log.isDebugEnabled()) {
            log.debug("in syncToSystem: {}", missingoption);
        }

        if (!SystemManager.hasEntitlement(sid, EntitlementManager.MANAGEMENT) ||
                !SystemManager.hasEntitlement(sid1, EntitlementManager.MANAGEMENT)) {
            throw new MissingEntitlementException(
                    EntitlementManager.MANAGEMENT.getHumanReadableLabel());
        }

        DataResult<PackageMetadata> dr = prepareSyncToServer(sid, sid1, user.getOrg().getId(),
                null, pkgIdCombos);

        if (log.isDebugEnabled()) {
            log.debug("prepareTosyncServer results: {}", dr);
        }

        // dr should be the list of packages and actions that need to be taken.
        // Now get the channels of the victim (victim being the server).
        Server server = ServerFactory.lookupById(sid);
        Set<Channel> channels = server.getChannels();
        List<PackageMetadata> missingPackages = findMissingPackages(dr, channels);
        PackageAction action = null;

        log.debug("is missingpackages empty: {}", missingPackages.isEmpty());
        if (missingPackages.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("Schedule sync, no missing packages, so we're good");
            }
            action = ActionManager.schedulePackageRunTransaction(user, server, dr,
                    earliest);
            if (log.isDebugEnabled()) {
                log.debug("created an action: {}", action);
            }
        }
        else if (OPTION_REMOVE.equals(missingoption)) {
            // User chose to have missing packages removed.  So we will remove
            // any package that exists in the missing packages list and is NOT
            // on the server. This means that if the PackageMetadata has a
            // comparison value of KEY_OTHER_ONLY we remove it, otherwise,
            // the server has a version of the package and we don't want to
            // touch it.
            if (log.isDebugEnabled()) {
                log.debug("Missingoption set to remove.  DataResult size [{}]", dr.size());
            }

            for (PackageMetadata pm : missingPackages) {
                int compare = pm.getComparisonAsInt();
                if (compare == PackageMetadata.KEY_OTHER_ONLY ||
                        compare == PackageMetadata.KEY_OTHER_NEWER) {
                    dr.remove(pm);
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("DataResult size after removals [{}]", dr.size());
            }

            action = ActionManager.schedulePackageRunTransaction(user, server, dr,
                    earliest);
            if (log.isDebugEnabled()) {
                log.debug("Action: {}", action);
            }
        }
        else if (OPTION_SUBSCRIBE.equals(missingoption)) {
            // subscribe to channels and continue
            if (log.isDebugEnabled()) {
                log.debug("Missingoption set to subscribe");
            }

            // get list of accessible channels for the current user
            // for each accessible channel found, see if any of the
            // missing packages are in that channel.  If so,
            // add the channel to the "needed channels list" and
            // remove package from missingpackages list.
            Channel baseChannel = ChannelFactory.getBaseChannel(server.getId());
            List<Channel> validChannels = ChannelManager.userAccessibleChildChannels(
                    user.getOrg().getId(), baseChannel.getId());
            List<Channel> neededChannels = new ArrayList<>();

            for (Channel validChannel : validChannels) {
                for (Iterator<PackageMetadata> innerItr = missingPackages.iterator(); innerItr.hasNext();) {
                    PackageMetadata pm = innerItr.next();
                    if (PackageManager.isPackageInChannel(
                            validChannel.getId(), pm.getNameId(),
                            pm.getEvrId())) {

                        if (log.isDebugEnabled()) {
                            log.debug("Package [{}] is in Channel [{}]", pm.getName(), validChannel.getId());
                        }

                        neededChannels.add(validChannel);
                        // remove from missingpkgs
                        innerItr.remove();
                    }
                }
            }

            // finally for each channel needed, subscribe the server
            // to that channel. if there's an error throw an exception
            // TODO: what type of exception
            // once subscribed, throw away any of the remaining missing
            // packages.

            for (Channel needed : neededChannels) {
                if (log.isDebugEnabled()) {
                    log.debug("Subscribing to [{}]", needed.getName());
                }
                SystemManager.subscribeServerToChannel(user, server, needed);
            }

            // if we still have some missing packages, just remove them.
            if (!missingPackages.isEmpty()) {
                for (PackageMetadata pm : missingPackages) {
                    int compare = pm.getComparisonAsInt();
                    if (compare == PackageMetadata.KEY_OTHER_ONLY ||
                            compare == PackageMetadata.KEY_OTHER_NEWER) {
                        if (log.isDebugEnabled()) {
                            log.debug("Removing pm [{}]", pm.getName());
                        }
                        dr.remove(pm);
                    }
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("DataResult size after removals [{}]", dr.size());
            }

            action = ActionManager.schedulePackageRunTransaction(user, server, dr,
                    earliest);
        }
        else {
            if (log.isDebugEnabled()) {
                log.debug("We have [{}] missing packages", missingPackages.size());
            }
            throw new MissingPackagesException("There are [" +
                    missingPackages.size() + "] missing packages");
        }

        return action;
    }

    private static void updatePackageListWithChannels(Channel baseChannel,
            User user, List<PackageMetadata> pkgs) {

        List<Channel> validChannels = ChannelManager.userAccessibleChildChannels(
                user.getOrg().getId(), baseChannel.getId());

        if (log.isDebugEnabled()) {
            log.debug("updatePackageListWithChannels: validchannels [{}]", validChannels.size());
        }

        for (Channel validChannel : validChannels) {
            for (PackageMetadata pm : pkgs) {
                if (PackageManager.isPackageInChannel(
                        validChannel.getId(), pm.getNameId(),
                        pm.getEvrId())) {

                    if (log.isDebugEnabled()) {
                        log.debug("Package [{}] is in Channel [{}]", pm.getName(), validChannel.getId());
                    }

                    pm.addChannel(validChannel);
                }
            }
        }
    }

    /**
     * Syncs the given server id to the given profile id.
     * @param user Current user
     * @param sid Server id to be affected.
     * @param prid Profile id to be used to sync.
     * @param pkgIdCombos Set of packages which will be synced.
     * @param missingoption Defines what do to if packages go missing.  null means
     * ask the user.
     * @param earliest The earliest time to perform this action
     * @return The PackageAction which was scheduled containing the sync information.
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    public static PackageAction syncToProfile(User user, Long sid, Long prid,
            Set<String> pkgIdCombos, String missingoption, Date earliest)
        throws TaskomaticApiException {

        DataResult<PackageMetadata> dr = prepareSyncToProfile(sid, prid, user.getOrg().getId(),
                null, pkgIdCombos);

        // dr should be the list of packages and actions that need to be taken.
        // Now get the channels of the victim (victim being the server).
        Server server = ServerFactory.lookupById(sid);
        Set<Channel> channels = server.getChannels();
        List<PackageMetadata> missingPackages = findMissingPackages(dr, channels);
        PackageAction action = null;

        // this code makes me want spaghetti!

        if (missingPackages.isEmpty()) {
            // schedule sync
            action = ActionManager.schedulePackageRunTransaction(user, server, dr,
                    earliest);
        }
        else if (OPTION_REMOVE.equals(missingoption)) {
            // User chose to have missing packages removed.  So we will remove
            // any package that exists in the missing packages list and is NOT
            // on the server. This means that if the PackageMetadata has a
            // comparison value of KEY_OTHER_ONLY we remove it, otherwise,
            // the server has a version of the package and we don't want to
            // touch it.
            if (log.isDebugEnabled()) {
                log.debug("Missingoption set to remove.  DataResult size [{}]", dr.size());
            }

            for (PackageMetadata pm : missingPackages) {
                int compare = pm.getComparisonAsInt();
                if (compare == PackageMetadata.KEY_OTHER_ONLY ||
                        compare == PackageMetadata.KEY_OTHER_NEWER) {
                    dr.remove(pm);
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("DataResult size after removals [{}]", dr.size());
            }

            action = ActionManager.schedulePackageRunTransaction(user, server, dr,
                    earliest);
        }
        else if (OPTION_SUBSCRIBE.equals(missingoption)) {
            // subscribe to channels and continue
            if (log.isDebugEnabled()) {
                log.debug("Missingoption set to subscribe");
            }

            // get list of accessible channels for the current user
            // for each accessible channel found, see if any of the
            // missing packages are in that channel.  If so,
            // add the channel to the "needed channels list" and
            // remove package from missingpackages list.
            Channel baseChannel = ChannelFactory.getBaseChannel(server.getId());
            List<Channel> validChannels = ChannelManager.userAccessibleChildChannels(
                    user.getOrg().getId(), baseChannel.getId());
            List<Channel> neededChannels = new ArrayList<>();

            for (Channel validChannel : validChannels) {
                for (Iterator<PackageMetadata> innerItr = missingPackages.iterator(); innerItr.hasNext();) {
                    PackageMetadata pm = innerItr.next();
                    if (PackageManager.isPackageInChannel(
                            validChannel.getId(), pm.getNameId(),
                            pm.getEvrId())) {

                        if (log.isDebugEnabled()) {
                            log.debug("Package [{}] is in Channel [{}]", pm.getName(), validChannel.getId());
                        }

                        neededChannels.add(validChannel);
                        // remove from missingpkgs
                        innerItr.remove();
                    }
                }
            }

            // finally for each channel needed, subscribe the server
            // to that channel. if there's an error throw an exception
            // TODO: what type of exception
            // once subscribed, throw away any of the remaining missing
            // packages.

            for (Channel needed : neededChannels) {
                SystemManager.subscribeServerToChannel(user, server, needed);
            }

            // if we still have some missing packages, just remove them.
            if (!missingPackages.isEmpty()) {
                for (PackageMetadata pm : missingPackages) {
                    int compare = pm.getComparisonAsInt();
                    if (compare == PackageMetadata.KEY_OTHER_ONLY ||
                            compare == PackageMetadata.KEY_OTHER_NEWER) {
                        if (log.isDebugEnabled()) {
                            log.debug("Removing pm [{}]", pm.getName());
                        }
                        dr.remove(pm);
                    }
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("DataResult size after removals [{}]", dr.size());
            }

            action = ActionManager.schedulePackageRunTransaction(user, server, dr,
                    earliest);
        }
        else {
            if (log.isDebugEnabled()) {
                log.debug("We have [{}] missing packages", missingPackages.size());
            }

            throw new MissingPackagesException("There are [" +
                    missingPackages.size() + "] missing packages");
        }

        return action;
    }

    /**
     * Returns a list of missing packages.
     * @param user Current user
     * @param sid Server id
     * @param prid Profile id
     * @param pkgIdCombos Set of packages
     * @param pc page control
     * @return a list of missing packages.
     */
    public static DataResult<PackageMetadata> getMissingProfilePackages(User user, Long sid,
            Long prid, Set<String> pkgIdCombos, PageControl pc) {

        DataResult<PackageMetadata> dr = prepareSyncToProfile(sid, prid, user.getOrg().getId(),
                null, pkgIdCombos);
        Server server = ServerFactory.lookupById(sid);
        Set<Channel> channels = server.getChannels();
        List<PackageMetadata> missingpkgs = findMissingPackages(dr, channels);
        DataResult<PackageMetadata> missing = new DataResult<>(missingpkgs);
        missing = prepareList(missing, pc);

        Channel baseChannel = ChannelFactory.getBaseChannel(sid);

        updatePackageListWithChannels(baseChannel, user, missing);

        return missing;
    }

    /**
     * Returns a list of missing packages.
     * @param user Current user
     * @param sid Server id
     * @param sid1 Profile id
     * @param pkgIdCombos Set of packages
     * @param pc page control
     * @return a list of missing packages.
     */
    public static DataResult<PackageMetadata> getMissingSystemPackages(User user, Long sid,
            Long sid1, Set<String> pkgIdCombos, PageControl pc) {

        DataResult<PackageMetadata> dr = prepareSyncToServer(sid, sid1, user.getOrg().getId(),
                null, pkgIdCombos);
        Server server = ServerFactory.lookupById(sid);
        Set<Channel> channels = server.getChannels();
        List<PackageMetadata> missingpkgs = findMissingPackages(dr, channels);
        DataResult<PackageMetadata> missing = new DataResult<>(missingpkgs);

        // should have the subset we plan to work with.
        // NOW, we need to find the channels for each
        // of the missing packages in this list for display purposes.
        // Can this get any worse?  hmm let me think, I bet it could.
        missing = prepareList(missing, pc);

        Channel baseChannel = ChannelFactory.getBaseChannel(sid);
        updatePackageListWithChannels(baseChannel, user, missing);

        return missing;
    }

    /**
     * Get the List of child Channels of the passed in base Channel that contain the
     * packages found in the Profile.  This is useful if you want to compute the child
     * channels required to be subscribed to in order to get a system to sync with a
     * profile.
     *
     * This method iterates over *each* package in the profile and checks for the proper
     * child channel.  Can be expensive.
     *
     * @param user making the call
     * @param baseChannel to look for child channels for
     * @param profileIn to iterate over the set of packages for.
     * @return List of Channel objects.
     */
    public static List<Channel> getChildChannelsNeededForProfile(User user, Channel baseChannel,
            Profile profileIn) {
        List<Channel> retval = new LinkedList<>();

        List<PackageListItem> profilePackages = canonicalProfilePackages(profileIn.getId(),
                user.getOrg().getId(), null);

        log.debug("getChildChannelsNeededForProfile profile has: {} packages in it.", profilePackages.size());

        Set<String> evrNameIds = new HashSet<>();
        // Create the Set of evr_id's
        for (PackageListItem pli : profilePackages) {
            evrNameIds.add(pli.getNevr());
            log.debug("Added nevr: {}", pli.getNevr());
        }

        for (Channel child : ChannelManager.userAccessibleChildChannels(
                user.getOrg().getId(), baseChannel.getId())) {
            log.debug("working with child channel: {}", child.getLabel());
            List<PackageListItem> packages = getPackagesInChannelByIdCombo(child.getId());
            for (PackageListItem row : packages) {

                log.debug("Checking:  {}", row.getNevr());
                if (evrNameIds.contains(row.getNevr())) {
                    retval.add(child);
                    log.debug("found package, breaking out of loop");
                    break;
                }
            }
        }

        return retval;
    }


    private static DataResult<PackageListItem> getPackagesInChannelByIdCombo(Long cid) {
        SelectMode m = ModeFactory.getMode("Package_queries",
                "packages_in_channel_by_id_combo");
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> elabParams = new HashMap<>();
        params.put("cid", cid);
        return makeDataResult(params, elabParams, null, m);
    }

    private static List<PackageMetadata> findMissingPackages(DataResult<PackageMetadata> pkgs, Set<Channel> channels) {

        List<PackageMetadata> missingPkgs = new ArrayList<>();

        DataResult<PackageListItem> pkgsInChannels = new DataResult<>(new ArrayList<>());
        for (Channel c : channels) {
            DataResult<PackageListItem> dr = getPackagesInChannelByIdCombo(c.getId());
            pkgsInChannels.addAll(dr);
        }

        // now determine which packages are in pkgs but not in pkgsInChannels

        // using pkgsInChannels, build a map of name ids (i.e. key) to list
        // of packages (i.e. value) associated w/the id
        Map<String, List<PackageListItem>> pkgsInChannelsByNameId = buildPackagesMap(pkgsInChannels);

        // for each of the pkgs to be synced
        for (PackageMetadata pm : pkgs) {
            // retrieve the packages with the same name that exist w/in channels
            List<PackageListItem> pkgsInChannel = pkgsInChannelsByNameId.get(pm.getMapHash());

            if (pm.getComparisonAsInt() == PackageMetadata.KEY_THIS_ONLY) {
                // makes no sense to check whether missing
                continue;
            }

            // attempt to locate a package from pkgsInChannel that has the same nvre
            // as the pkg to be synced
            boolean foundMatch = false;
            if (pkgsInChannel != null) {
                PackageEvr evrPm = new PackageEvr(
                        pm.getEpoch() == null ? "0" : pm.getEpoch(),
                        pm.getVersion(),
                        pm.getRelease(),
                        pm.getPackageType()
                );
                for (PackageListItem pkgInChannel : pkgsInChannel) {
                    PackageEvr evrInChannel = new PackageEvr(
                            pkgInChannel.getEpoch() == null ? "0" : pkgInChannel.getEpoch(),
                            pkgInChannel.getVersion(),
                            pkgInChannel.getRelease(),
                            pkgInChannel.getPackageType()
                    );
                    if (evrInChannel.compareTo(evrPm) == 0) {
                        foundMatch = true;
                        break;  //stop searching for match
                    }
                }
            }
            if (!foundMatch) {
                missingPkgs.add(pm);
            }
        }
        return missingPkgs;
    }

    private static DataResult<PackageMetadata> prepareList(List<PackageMetadata> result, PageControl pc) {
        DataResult<PackageMetadata> dr = new DataResult<>(result);
        dr.setTotalSize(result.size());

        if (pc != null) {
            dr.setFilter(pc.hasFilter());
            if (pc.hasFilter()) {
                pc.filterData(dr);
            }

            // If we are filtering the content, _don't_ show the alphabar.
            // This matches what the perl code does.  If we want to show a
            // smaller alphabar, just remove the if statement.
            if (pc.getFilterData() == null || pc.getFilterData().equals("")) {
                if (pc.hasIndex()) {
                    dr.setIndex(pc.createIndex(dr));
                }
            }

            // now use the PageControl to limit the list to the
            // selected region.
            dr = dr.subList(pc.getStart() - 1, pc.getEnd());
        }

        return dr;
    }


    /**
     * Creates a packagemetadata
     * @param sys PackageListItem info
     * @param other packageListItem to compare with
     * @param comparison comparison
     * @param param compare string param
     * @return Packagemetadata with the information from the PackageListItem
     */
    private static PackageMetadata createPackageMetadata(PackageListItem sys,
            PackageListItem other, int comparison, String param) {
        PackageMetadata pm = new PackageMetadata(sys, other);
        pm.setComparison(comparison);
        pm.setCompareParam(param);
        return pm;
    }

    /**
     * Returns the list of stored profiles.
     * @param orgId The id of the org the profiles are associated with.
     * @return DataResult of ProfileOverviewDto
     */
    public static DataResult<ProfileOverviewDto> listProfileOverviews(Long orgId) {

        SelectMode m = ModeFactory.getMode("profile_queries", "profile_overview");

        Map<String, Object> params = new HashMap<>();
        params.put("org_id", orgId);
        Map<String, Object> elabParams = new HashMap<>();

        return makeDataResult(params, elabParams, null, m);
    }

    /**
     * Returns the list of packages associated with a stored profile.
     * @param profileId The id of the profile the packages are associated with.
     * @return DataResult of ProfilePackageOverviewDto
     */
    public static DataResult<ProfilePackageOverviewDto> listProfilePackages(
            Long profileId) {

        SelectMode m = ModeFactory.getMode("profile_queries", "profile_package_overview");

        Map<String, Object> params = new HashMap<>();
        params.put("prid", profileId);
        Map<String, Object> elabParams = new HashMap<>();

        return makeDataResult(params, elabParams, null, m);
    }

    /**
     * Returns the Profile whose id is prid.
     * @param prid  Profile id sought.
     * @param org The org in which this profile should be found.
     * @return Profile whose id is prid.
     */
    public static Profile lookupByIdAndOrg(Long prid, Org org) {
        Profile retval = ProfileFactory.lookupByIdAndOrg(prid, org);
        if (retval == null) {
            LocalizationService ls = LocalizationService.getInstance();
            throw new LookupException("The profile " + prid + "could not" + "be found for org " + org.getId(),
                    ls.getMessage("lookup.jsp.title.profile"),
                    ls.getMessage("lookup.jsp.reason1.profile"),
                    ls.getMessage("lookup.jsp.reason2.profile"));
        }
        return retval;
    }

    /**
     * Get the list of Profile's that are compatible with the given Base
     * Channel ID passed in.
     * @param channelIn that you want the list of Profiles against.
     * @param orgIn who owns the Profiles
     * @param pc PageControl to filter the list.
     * @return DataResult containing ProfileDto objects
     */
    public static DataResult<ProfileDto> compatibleWithChannel(Channel channelIn, Org orgIn, PageControl pc) {
        SelectMode m = ModeFactory.getMode("profile_queries",
                "compatible_with_channel");
        Map<String, Object> params = new HashMap<>();
        params.put("org_id", orgIn.getId());
        params.put("cid", channelIn.getId());
        return  makeDataResult(params, new HashMap<>(), pc, m);
    }

}
