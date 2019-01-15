/**
 * Copyright (c) 2017 SUSE LLC
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

package com.redhat.rhn.manager.visualization;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.errata.ErrataFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.visualization.json.System;
import com.redhat.rhn.manager.visualization.json.VirtualHostManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Logic for extracting data for visualization.
 */
public class VisualizationManager {

    private VisualizationManager() { }

    /**
     * Data for virtualization hierarchy
     * @param user the user
     * @return Data for virtualization hierarchy
     */
    public static List<Object> virtualizationHierarchy(User user) {
        Map<String, Set<String>> installedProducts = fetchInstalledProducts(user);
        Map<String, Map<String, Integer>> patchCounts = fetchPatchCounts(user);

        System root = new System();
        root.setId("root");
        root.setParentId(null);
        root.setName("SUSE Manager");

        VirtualHostManager unknownVirtualHostManager = new VirtualHostManager(
                "unknown-vhm",
                root.getId(),
                "Unknown Virtual Host Manager");

        Stream<VirtualHostManager> virtualHostManagers =
                ((Stream<VirtualHostManager>) HibernateFactory.getSession()
                        .getNamedQuery("VirtualHostManager.listByOrg")
                        .setParameter("org", user.getOrg())
                        .list()
                        .stream())
                        .map(vhm -> vhm.setParentId(root.getId()));

        Map<String, String> serverVhmMapping = getServerVhmMapping();

        List<System> hosts = ((Stream<System>) HibernateFactory.getSession()
                .getNamedQuery("Server.listHostSystems")
                .setParameter("org", user.getOrg())
                .list()
                .stream())
                .map(p -> p.setInstalledProducts(installedProducts.get(p.getRawId())))
                .map(s -> s.setPatchCounts(patchCountsToList(patchCounts.get(s.getRawId()))))
                .map(p -> p.setParentId(serverVhmMapping.getOrDefault(
                        p.getId(),
                        unknownVirtualHostManager.getId())))
                .collect(Collectors.toList());

        boolean unknownVHMneeded =
                hosts.stream().anyMatch(h -> unknownVirtualHostManager.getId().equals(h.getParentId()));

        Stream<System> guests = ((Stream<System>) HibernateFactory.getSession()
                .getNamedQuery("Server.listGuestSystems")
                .setParameter("org", user.getOrg())
                .list()
                .stream())
                .map(s -> s.setPatchCounts(patchCountsToList(patchCounts.get(s.getRawId()))))
                .map(p -> p.setInstalledProducts(installedProducts.get(p.getRawId())));

        return concatStreams(
                Stream.of(root),
                Stream.of(unknownVirtualHostManager).filter(m -> unknownVHMneeded),
                virtualHostManagers,
                hosts.stream(),
                guests
        ).collect(Collectors.toList());
    }

    // hack: we need to discover the server to VHM mapping,
    // unfortunately the relation is modelled as unidirectional - VHM -> server
    private static Map<String, String> getServerVhmMapping() {
        return (Map<String, String>) HibernateFactory.getSession()
                .getNamedQuery("Server.serverIdVirtualHostManagerId")
                .list()
                .stream()
                .filter(o -> ((Object [])o)[0] != null)
                .collect(Collectors.toMap(
                        o -> ((Object []) o)[0].toString(),
                        o -> ((Object []) o)[1].toString())
                );
    }

    /**
     * Data for proxy hierarchy
     * @param user the user
     * @return Data for proxy hierarchy
     */
    public static List<Object> proxyHierarchy(User user) {
        Map<String, Set<String>> installedProducts = fetchInstalledProducts(user);
        Map<String, Map<String, Integer>> patchCounts = fetchPatchCounts(user);

        System root = new System();
        root.setId("root");
        root.setName("SUSE Manager");

        Stream<System> systems = ((Stream<System>) HibernateFactory.getSession()
                .getNamedQuery("Server.listProxySystemsHierarchy")
                .setParameter("org", user.getOrg())
                .list()
                .stream())
                .map(p -> p.setInstalledProducts(installedProducts.get(p.getRawId())))
                .map(p -> p.setPatchCounts(patchCountsToList(patchCounts.get(p.getRawId()))));

        return concatStreams(
                Stream.of(root),
                systems
        ).collect(Collectors.toList());
    }

    /**
     * Data for systems with managed groups view
     * @param user the user
     * @return Data for systems with managed groups
     */
    public static List<Object> systemsWithManagedGroups(User user) {
        Map<String, Set<String>> installedProducts = fetchInstalledProducts(user);
        Map<String, Set<String>> groups = fetchSystemManagedGroups(user);
        Map<String, Map<String, Integer>> patchCounts = fetchPatchCounts(user);

        System root = new System();
        root.setId("root");
        root.setName("SUSE Manager");

        Stream<System> systems = ((Stream<System>) HibernateFactory.getSession()
                .getNamedQuery("Server.listSystems")
                .setParameter("org", user.getOrg())
                .list()
                .stream())
                .map(s -> s.setParentId(root.getId()))
                .map(s -> s.setManagedGroups(groups.get(s.getRawId())))
                .map(s -> s.setInstalledProducts(installedProducts.get(s.getRawId())))
                .map(s ->
                        s.setPatchCounts(patchCountsToList(patchCounts.get(s.getRawId()))));

        return concatStreams(
                Stream.of(root),
                systems
        ).collect(Collectors.toList());
    }

    /**
     * Map of system id as string -> set of installed product names
     * @param user user
     * @return map of system id as string -> set of installed product names
     */
    private static Map<String, Set<String>> fetchInstalledProducts(User user) {
        return ((List<Object[]>) HibernateFactory.getSession()
                .getNamedQuery("Server.serverInstalledProductNames")
                .setParameter("org", user.getOrg())
                .list())
                .stream()
                .collect(Collectors.toMap(
                        v -> v[0].toString(),
                        v -> set((String) v[1]),
                        (u, v) -> {
                            u.addAll(v);
                            return u;
                        }
                ));
    }

    private static Map<String, Set<String>> fetchSystemManagedGroups(User user) {
        return ((List<Object[]>) HibernateFactory.getSession()
                .createNamedQuery("Server.systemIdManagedGroupName")
                .setParameter("org", user.getOrg())
                .list())
                .stream()
                .collect(Collectors.toMap(
                        v -> v[0].toString(),
                        v -> set((String) v[1]),
                        (u, v) -> {
                            u.addAll(v);
                            return u;
                        }
                ));
    }

    /**
     * Fetch the patch counts for user-accessible servers from the database as a nested map
     * in this form:
     *
     * system id as string -> advisory type -> count of patches
     *
     * @param user the user
     * @return the patch counts
     */
    private static Map<String, Map<String, Integer>> fetchPatchCounts(User user) {
        return ((List<Object[]>) HibernateFactory.getSession()
                .getNamedQuery("Server.listPatchCountsForAllServers")
                .setParameter("orgId", user.getOrg().getId())
                .list())
                .stream()
                .collect(Collectors.toMap(
                        v -> v[0].toString(),
                        v -> map(v[1].toString(), ((Integer) v[2])),
                        (u, v) -> {
                            u.putAll(v);
                            return u;
                        }
                ));
    }

    /**
     * Converts the patch info from the map form to the list in this form:
     * [X, Y, Z], where
     * X - number of bug fix advisories
     * Y - number of product enhancement advisories
     * Z - number of security advisories
     *
     * @param patchCounts the map containing the patch info
     * @return the list containing the patch info
     */
    private static List<Integer> patchCountsToList(Map<String, Integer> patchCounts) {
        List<Integer> result = new ArrayList<>();
        if (patchCounts != null) {
            result.add(patchCounts.getOrDefault(ErrataFactory.ERRATA_TYPE_BUG, 0));
            result.add(patchCounts.getOrDefault(ErrataFactory.ERRATA_TYPE_ENHANCEMENT, 0));
            result.add(patchCounts.getOrDefault(ErrataFactory.ERRATA_TYPE_SECURITY, 0));
        }
        return result;
   }

    private static <K, V> Map<K, V> map(K k, V v) {
        HashMap<K, V> result = new HashMap<>();
        result.put(k, v);
        return result;
    }

    private static <T> Set<T> set(T elem) {
        HashSet<T> ts = new HashSet<>();
        if (elem != null) {
            ts.add(elem);
        }
        return ts;
    }

    private static Stream<?> concatStreams(Stream<?>... streams) {
        if (streams == null || streams.length == 0) {
            return Stream.empty();
        }

        return Arrays.stream(streams)
                .reduce(Stream.empty(), (v1, v2) -> Stream.concat(v1, v2));
    }
}
