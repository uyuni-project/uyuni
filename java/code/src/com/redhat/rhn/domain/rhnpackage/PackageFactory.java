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
package com.redhat.rhn.domain.rhnpackage;

import com.redhat.rhn.common.db.datasource.CachedStatement;
import com.redhat.rhn.common.db.datasource.CallableMode;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.Row;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.server.InstalledPackage;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.BaseSearchAction;
import com.redhat.rhn.frontend.dto.BooleanWrapper;
import com.redhat.rhn.frontend.dto.PackageOverview;
import com.redhat.rhn.manager.user.UserManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

/**
 * PackageFactory
 */
public class PackageFactory extends HibernateFactory {

    private static PackageFactory singleton = new PackageFactory();
    private static Logger log = LogManager.getLogger(PackageFactory.class);

    public static final PackageKeyType PACKAGE_KEY_TYPE_GPG = lookupKeyTypeByLabel("gpg");

    public static final String ARCH_TYPE_RPM = "rpm";
    public static final String ARCH_TYPE_DEB = "deb";
    public static final String ARCH_TYPE_TAR = "tar";

    private static final Map<String, Set<String>> PACKAGE_CAPABILITY_MAP;
    static {
        Map<String, Set<String>> map = new HashMap<>();
        Set<String> rpmCaps = new HashSet<>();
        rpmCaps.add("dependencies");
        rpmCaps.add("change_log");
        rpmCaps.add("file_list");
        rpmCaps.add("errata");
        rpmCaps.add("remove");
        rpmCaps.add("rpm");
        map.put(PackageFactory.ARCH_TYPE_RPM, rpmCaps);
        Set<String> debCaps = new HashSet<>();
        debCaps.add("dependencies");
        debCaps.add("deb");
        map.put(PackageFactory.ARCH_TYPE_DEB, debCaps);
        PACKAGE_CAPABILITY_MAP = Collections.unmodifiableMap(map);
    }

    private PackageFactory() {
        super();
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
     * Lookup a Package by its ID
     * @param id to search for
     * @return the Package found
     */
    private static Package lookupById(Long id) {
        return singleton.lookupObjectByNamedQuery("Package.findById", Map.of("id", id));
    }

    /**
     * Lookup Packages by IDs
     * @param ids list of id to search for
     * @return list of Packages found
     */
    private static List<Package> lookupById(List<Long> ids) {
        return singleton.listObjectsByNamedQuery("Package.findByIds", Map.of(), ids, "pids");
    }

    /**
     * Returns true if the Package with the given name and evr ids exists in the
     * Channel whose id is cid.
     * @param cid Channel id to look in
     * @param nameId Package name id
     * @param evrId Package evr id
     * @return true if the Package with the given name and evr ids exists in the
     * Channel whose id is cid.
     */
    public static boolean isPackageInChannel(Long cid, Long nameId, Long evrId) {
        Map<String, Object> params = new HashMap<>();
        params.put("cid", cid);
        params.put("name_id", nameId);
        params.put("evr_id", evrId);
        SelectMode m = ModeFactory.getMode("Channel_queries", "is_package_in_channel");
        DataResult<BooleanWrapper> dr = m.execute(params);
        if (dr.isEmpty()) {
            return false;
        }

        return dr.get(0).booleanValue();
    }

    /**
     * Lookup a Package by the id, in the context of a given user. Does security
     * check to verify that the user has access to the package.
     * @param id of the Package to search for
     * @param user the user doing the search
     * @return the Package found
     */
    public static Package lookupByIdAndUser(Long id, User user) {
        return lookupByIdAndOrg(id, user.getOrg());
    }

    /**
     * Lookup Packages by the id, in the context of a given user. Does security
     * check to verify that the user has access to the packages.
     * @param ids List of the Package to search for
     * @param user the user doing the search
     * @return List of packages found
     */
    public static List<Package> lookupByIdAndUser(List<Long> ids, User user) {
        return lookupByIdAndOrg(ids, user.getOrg());
    }


    /**
     * Lookup a Package by the id, in the context of a given org. Does security
     * check to verify that the org has access to the package.
     * @param id of the Package to search for
     * @param org the org which much have access to the package
     * @return the Package found
     */
    public static Package lookupByIdAndOrg(Long id, Org org) {
        if (!UserManager.verifyPackageAccess(org, id)) {
            // User doesn't have access to the package... return null as if it
            // doesn't exist.
            return null;
        }
        return lookupById(id);
    }

    /**
     * Lookup Packages by the id, in the context of a given org. Does security
     * check to verify that the org has access to the package.
     * @param ids List of the Packages to search for
     * @param org the org which much have access to the package
     * @return List of Packages found
     */
    public static List<Package> lookupByIdAndOrg(List<Long> ids, Org org) {
        if (!UserManager.verifyPackagesAccess(org, ids)) {
            // User doesn't have access to the package... return null as if it
            // doesn't exist.
            return Collections.emptyList();
        }
        return lookupById(ids);
    }

    /**
     * Store the package provider.
     * @param prov The object we are commiting.
     */
    public static void save(PackageProvider prov) {
        singleton.saveObject(prov);
    }

    /**
     * Store the package delta.
     * @param delta The object we are commiting.
     */
    public static void save(PackageDelta delta) {
        singleton.saveObject(delta);
    }

    /**
     * Store the package.
     * @param pkg The object we are commiting.
     */
    public static void save(Package pkg) {
        singleton.saveObject(pkg);
    }

    /**
     * Lookup a PackageArch by its id.
     * @param id package arch label id sought.
     * @return the PackageArch whose id matches the given id.
     */
    public static PackageArch lookupPackageArchById(Long id) {
        return HibernateFactory.doWithoutAutoFlushing(
          () -> singleton.lookupObjectByNamedQuery("PackageArch.findById", Map.of("id", id), true)
        );
    }

    /**
     * Lookup a PackageArch by its label.
     * @param label package arch label sought.
     * @return the PackageArch whose label matches the given label.
     */
    public static PackageArch lookupPackageArchByLabel(String label) {
        if (label == null) {
            return null;
        }
        return singleton.lookupObjectByNamedQuery("PackageArch.findByLabel", Map.of("label", label), true);
    }

    /**
     * Lookup all PackageArch
     * @return list of PackageArch
     */
    public static List<PackageArch> lookupPackageArch() {
        CriteriaBuilder builder = HibernateFactory.getSession().getCriteriaBuilder();
        CriteriaQuery<PackageArch> q = builder.createQuery(PackageArch.class);
        q.from(PackageArch.class);
        return HibernateFactory.getSession().createQuery(q).getResultList();
    }

    /**
     * List the Package objects by their Package Name
     * @param pn to query by
     * @return List of Package objects if found
     */
    public static List<Package> listPackagesByPackageName(PackageName pn) {
        Session session = HibernateFactory.getSession();

        return session.createNamedQuery("Package.findByPackageName", Package.class)
                .setParameter("packageName", pn)
                .list();
    }

    /**
     * lookup a PackageName object based on it's name, If one does not exist,
     * create a new one and return it.
     * @param pn the package name
     * @return a PackageName object that has a matching name
     */
    public static PackageName lookupOrCreatePackageByName(String pn) {
        long id = lookupOrCreatePackageNameId(pn);
        return lookupPackageName(id);
    }

    /**
     * Lookup the ID of a package name, if it exists, otherwise INSERT one (in a separate transaction)
     * @param name the package name
     * @return a package name id
     */
    public static long lookupOrCreatePackageNameId(String name) {
        CallableMode m = ModeFactory.getCallableMode("Package_queries", "lookup_package_name");

        Map<String, Object> inParams = new HashMap<>();
        inParams.put("name", name);

        Map<String, Integer> outParams = new HashMap<>();
        outParams.put("nameId", Types.NUMERIC);

        Map<String, Object> result = m.execute(inParams, outParams);

        return (Long) result.get("nameId");
    }

    /**
     * lookup a PackageName object based on it's id, returns null if it does
     * not exist
     *
     * @param id the package name id
     * @return a PackageName object that has a matching id or null if that
     * doesn't exist
     */
     public static PackageName lookupPackageName(Long id) {
         return (PackageName) HibernateFactory.getSession().getNamedQuery("PackageName.findById")
                 .setLong("id", id).uniqueResult();
    }

    /**
     * lookup a PackageName object based on it's name, returns null if it does
     * not exist
     *
     * @param pn the package name
     * @return a PackageName object that has a matching name or null if that
     * doesn't exist
     */
    public static PackageName lookupPackageName(String pn) {
        return (PackageName) HibernateFactory.getSession().getNamedQuery("PackageName.findByName")
                .setString("name", pn).uniqueResult();
    }

    /**
     * lookup orphaned packages, i.e. packages that are not contained in any
     * channel
     * @param org the org to check for
     * @return a List of package objects that are not in any channel
     */
    @SuppressWarnings("unchecked")
    public static List<Package> lookupOrphanPackages(Org org) {
        return HibernateFactory.getSession().getNamedQuery("Package.listOrphans")
                .setParameter("org", org).list();
    }

    /**
     * Find a package based off of the NEVRA
     * @param org the org that owns the package
     * @param name the name to search for
     * @param version the version to search for
     * @param release the release to search for
     * @param epoch if epoch is null, the best match for epoch will be used.
     * @param arch the arch to search for
     * @return the requested Package
     */
    public static List<Package> lookupByNevra(Org org, String name, String version,
            String release, String epoch, PackageArch arch) {

        List<Package> packages = HibernateFactory.getSession().getNamedQuery(
                "Package.lookupByNevra").setParameter("org", org).setString("name", name)
                .setString("version", version).setString("release", release).setParameter(
                        "arch", arch).list();

        if (epoch == null || packages.size() < 2) {
            return packages;
        }
        packages.removeIf(pack -> !epoch.equals(pack.getPackageEvr().getEpoch()));
        return packages;
    }

    /**
     * Find a package based off of the NEVRA ids
     * @param org the org that owns the package
     * @param nameId the id of the name to search for
     * @param evrId the id of  the evr to search for
     * @param archId the id of the arch to search for
     * @return the requested Package
     */
    public static List<Package> lookupByNevraIds(Org org, long nameId, long evrId, long archId) {

        return HibernateFactory.getSession().createNamedQuery("Package.lookupByNevraIds", Package.class)
                                            .setParameter("org", org)
                                            .setParameter("nameId", nameId)
                                            .setParameter("evrId", evrId)
                                            .setParameter("archId", archId)
                                            .list();

    }

    /**
     * Find a package based off of the channel, NEVRA and checksum
     * @param channel the channel label
     * @param name the name to search for
     * @param version the version to search for
     * @param release the release to search for
     * @param epoch if epoch is null, the best match for epoch will be used.
     * @param arch the arch to search for
     * @param checksum Optional the checksum to search for
     * @return the requested Package
     */
    public static Package lookupByChannelLabelNevraCs(String channel, String name,
            String version, String release, String epoch, String arch, Optional<String> checksum) {
        @SuppressWarnings("unchecked")
        List<Package> packages = HibernateFactory.getSession()
                .getNamedQuery("Package.lookupByChannelLabelNevraCs")
                .setString("channel", channel)
                .setString("name", name)
                .setString("version", version)
                .setString("release", release)
                .setString("arch", arch)
                .setString("checksum", checksum.orElse(null))
                .list();

        if (packages.isEmpty()) {
            return null;
        }

        if (epoch != null && packages.size() > 1) {
            packages.removeIf(pack -> !epoch.equals(pack.getPackageEvr().getEpoch()));
        }

        return packages.get(0);
    }

    /**
     * Returns an InstalledPackage object, given a server and package name to
     * lookup the latest version of the package. Return null if the package
     * doesn;t exist.
     * @param name name of the package to lookup on
     * @param server server where the give package was installed.
     * @return the InstalledPackage with the given package name for the given
     * server
     */
    public static InstalledPackage lookupByNameAndServer(String name, Server server) {
        PackageName packName = lookupPackageName(name);
        Map<String, Object> params = new HashMap<>();
        params.put("server", server);
        params.put("name", packName);

        List<InstalledPackage> original = singleton.listObjectsByNamedQuery(
                "InstalledPackage.lookupByServerAndName", params);
        if (original.isEmpty()) {
            return null;
        }
        if (original.size() == 1) {
            return original.get(0);
        }
        List<InstalledPackage> packs = new LinkedList<>();
        packs.addAll(original);
        Collections.sort(packs);
        return packs.get(packs.size() - 1);
    }

    /**
     * Returns PackageOverviews from a search.
     * @param pids List of package ids returned from search server.
     * @param archLabels List of channel arch labels.
     * @param relevantUserId user id to filter by if relevant or architecture search
     *   server the user can see is subscribed to
     * @param filterChannelId channel id to filter by if channel search
     * @param searchType type of search to do, one of "relevant", "channel",
     *   "architecture", or "all"
     * @return PackageOverviews from a search.
     */
    public static List<PackageOverview> packageSearch(List<Long> pids,
            List<String> archLabels, Long relevantUserId, Long filterChannelId,
            String searchType) {
        Map<String, Object> params = new HashMap<>();
        SelectMode m;

        if (searchType.equals(BaseSearchAction.ARCHITECTURE)) {
            if (!(archLabels != null && !archLabels.isEmpty())) {
                throw new MissingArchitectureException(
                        "archLabels must not be null for architecture search!");
            }

            // This makes me very sad. PreparedSatement.setObject does not allow
            // you to pass in Lists or Arrays. We can't manually convert archLabels
            // to a string and use the regular infrastructure because it will
            // escape the quotes between architectures. The only thing we can do
            // is to get the SelectMode and manually insert the architecture types
            // before we continue. If we can get PreparedStatement to accept Lists
            // then all this hackishness can go away. NOTE: we know that we have to
            // guard against sql injection in this case. Notice that the archLabels
            // will all be enclosed in single quotes. Valid archLabels will only
            // contain alphanumeric, '-', and "_" characters. We will simply
            // check and enforce that constraint, and then even if someone injected
            // something we would either end up throwing an error or it would be
            // in a string, and therefore not dangerous.
            m = ModeFactory.getMode("Package_queries", "searchByIdAndArches");
            CachedStatement cs = m.getQuery();
            cs.modifyQuery(":channel_arch_labels", archLabels, value -> value.matches("^[a-zA-Z0-9\\-_]*$"));
        }
        else if (searchType.equals(BaseSearchAction.RELEVANT)) {
            if (relevantUserId == null) {
                throw new IllegalArgumentException(
                        "relevantUserId must not be null for relevant search!");
            }
            params.put("uid", relevantUserId);
            m = ModeFactory.getMode("Package_queries", "relevantSearchById");
        }
        else if (searchType.equals(BaseSearchAction.CHANNEL)) {
            if (filterChannelId == null) {
                throw new IllegalArgumentException(
                        "filterChannelId must not be null for channel search!");
            }
            params.put("cid", filterChannelId);
            m = ModeFactory.getMode("Package_queries", "searchByIdInChannel");
        }
        else {
            m = ModeFactory.getMode("Package_queries", "searchById");
        }

        // SelectMode.execute will batch the size properly and CachedStatement.execute
        // will create a comma separated string representation of the list of pids
        DataResult<PackageOverview> result = m.execute(params, pids);
        result.elaborate();
        return result;
    }

    /**
     * Lookup a package key type by label
     * @param label the label of the type
     * @return the key type
     */
    public static PackageKeyType lookupKeyTypeByLabel(String label) {
        return singleton.lookupObjectByNamedQuery("PackageKeyType.findByLabel", Map.of("label", label));
    }

    /**
     * Deletes a particular package object from hibernate. Note, currently This
     * does not delete it from rhnServerNeededCache so you probably want
     * to use SystemManager.deletePackages() to do that instead. This does not
     * also cleanup rhNPackageSource entries
     * @param pack the package to delete
     */
    public static void deletePackage(Package pack) {
        HibernateFactory.getSession().delete(pack);

    }

    /**
     * Deletes a particular package source object
     * @param src the package source object
     */
    public static void deletePackageSource(PackageSource src) {
        HibernateFactory.getSession().delete(src);
    }

    /**
     * Lookup package sources for a particular package
     * @param pack the package associated with the package sources
     * @return the list of package source objects
     */
    public static List<PackageSource> lookupPackageSources(Package pack) {
        if (pack == null) {
            return new ArrayList<>();
        }
        return singleton.listObjectsByNamedQuery("PackageSource.findByPackage", Map.of("pack", pack));
    }

    /**
     * Lookup package source by it's ID
     * @param psid id of the source package
     * @param org the org with access to the source package
     * @return the package source
     */
    public static PackageSource lookupPackageSourceByIdAndOrg(Long psid, Org org) {
        return singleton.lookupObjectByNamedQuery("PackageSource.findByIdAndOrg", Map.of("id", psid, "org", org));
    }

    /**
     * Find other packages with the same NVRE but with different arches
     * @param pack the package
     * @return List of package objects
     */
    public static List<Package> findPackagesWithDifferentArch(Package pack) {
        Map<String, Object> params = new HashMap<>();
        params.put("evr", pack.getPackageEvr());
        params.put("name", pack.getPackageName());
        params.put("arch", pack.getPackageArch());
        params.put("org", pack.getOrg());

        return singleton.listObjectsByNamedQuery("Package.findOtherArches", params);
    }

    /**
     * Provides a mapping of arch type labels to sets of capabilities (ported from the if
     * statement mess in package_type_capable of Package.pm). This should really
     * be in the DB, but it's not :{ and it needs to be ported from perl.
     *
     * @return the map of {@literal arch label -> set of capabilities}
     */
    public static Map<String, Set<String>> getPackageCapabilityMap() {
        return PACKAGE_CAPABILITY_MAP;
    }

    /**
     * list package providers
     * @return list of package providers
     */
    public static List<PackageProvider> listPackageProviders() {
        return singleton.listObjectsByNamedQuery("PackageProvider.listProviders", Map.of());
    }

    /**
     * Looup a package provider by name
     * @param name the name
     * @return the package provider
     */
    public static PackageProvider lookupPackageProvider(String name) {
        return singleton.lookupObjectByNamedQuery("PackageProvider.findByName", Map.of("name", name));
    }

    /**
     * Lookup a package key object
     * @param key the key to lookup
     * @return the package key
     */
    public static PackageKey lookupPackageKey(String key) {
        return singleton.lookupObjectByNamedQuery("PackageKey.findByKey", Map.of("key", key));
    }

    /**
     * Returns information, whether each package in the list is channel compatible
     * and whether the org has accesds to
     * @param orgId organization id
     * @param channelId channel id
     * @param packageIds list of package ids
     * @return dataresult(id, package_arch_id, org_package, org_access, shared_access)
     */
    public static DataResult<Row> getPackagesChannelArchCompatAndOrgAccess(
            Long orgId, Long channelId, List<Long> packageIds) {
        Map<String, Object> params = new HashMap<>();
        params.put("org_id", orgId);
        params.put("channel_id", channelId);
        SelectMode m = ModeFactory.getMode("Package_queries", "channel_arch_and_org_access");
        return m.execute(params, packageIds);
    }

    /**
     * Returns package names that are shared between an erratum and a channel,
     * with string representations of the versions in each.
     * @param cid channel id
     * @param eid errata id
     * @return list of maps, with keys of "name", "channel_version", and "errata_version"
     */
    public static List<Map<String, String>> getErrataChannelIntersection(Long cid, Long eid) {
        Map<String, Object> params = new HashMap<>();
        params.put("cid", cid);
        params.put("eid", eid);
        SelectMode m = ModeFactory.getMode("Package_queries", "channel_errata_intersection");
        return m.execute(params);
    }

    /**
     * Search for packages containing a product which are not installed on a server.
     * Return all missing packages found (in the latest version).
     * @param sid The server id
     * @return Return missing packages which contains a product
     */
    public static List<Package> findMissingProductPackagesOnServer(Long sid) {
        return singleton.listObjectsByNamedQuery("Package.findMissingProductPackagesOnServer", Map.of("sid", sid));
    }

    /**
     * Is the package with nameId available in the provided server's subscribed channels
     * @param server the server
     * @param nameId the name id
     * @return true if available, false otherwise
     */
    public static boolean hasPackageAvailable(Server server, Long nameId) {
        Map<String, Object> params = new HashMap<>();
        params.put("server_id", server.getId());
        params.put("nid", nameId);
        String mode = "has_package_available_with_name";
        SelectMode m =
                ModeFactory.getMode("System_queries", mode);
        DataResult<Row> toReturn = m.execute(params);
        return !toReturn.isEmpty();
    }

}
