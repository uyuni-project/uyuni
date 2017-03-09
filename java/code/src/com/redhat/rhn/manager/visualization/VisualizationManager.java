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
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.visualization.json.System;
import com.redhat.rhn.manager.visualization.json.VirtualHostManager;

import java.util.Arrays;
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

        Stream<System> hosts = ((Stream<System>) HibernateFactory.getSession()
                .getNamedQuery("Server.listHostSystems")
                .setParameter("org", user.getOrg())
                .list()
                .stream())
                .map(p -> p.setInstalledProducts(installedProducts.get(p.getId())))
                .map(p -> p.setParentId(serverVhmMapping.getOrDefault(
                        p.getId(),
                        unknownVirtualHostManager.getId()
                )));

        Stream<System> guests = ((Stream<System>) HibernateFactory.getSession()
                .getNamedQuery("Server.listGuestSystems")
                .setParameter("org", user.getOrg())
                .list()
                .stream())
                .map(p -> p.setInstalledProducts(installedProducts.get(p.getId())));

        return concatStreams(
                Stream.of(root),
                Stream.of(unknownVirtualHostManager),
                virtualHostManagers,
                hosts,
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

        System root = new System();
        root.setId("root");
        root.setName("SUSE Manager");

        Stream<System> proxies = ((Stream<System>) HibernateFactory.getSession()
                .getNamedQuery("Server.listProxySystems")
                .setParameter("org", user.getOrg())
                .list()
                .stream())
                .map(p -> p.setInstalledProducts(installedProducts.get(p.getId())))
                .map(p -> p.setParentId(root.getId()));

        Stream<System> systemsWithProxies = ((Stream<System>) HibernateFactory.getSession()
                .getNamedQuery("Server.listSystemsBehindProxy")
                .setParameter("org", user.getOrg())
                .list()
                .stream())
                .map(s -> s.setInstalledProducts(installedProducts.get(s.getId())));

        return concatStreams(
                Stream.of(root),
                proxies,
                systemsWithProxies
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
