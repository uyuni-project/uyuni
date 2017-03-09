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

package com.redhat.rhn.manager.visualization.test;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerPath;
import com.redhat.rhn.domain.server.VirtualInstance;
import com.redhat.rhn.domain.server.test.GuestBuilder;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManagerFactory;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.visualization.VisualizationManager;
import com.redhat.rhn.manager.visualization.json.System;
import com.redhat.rhn.manager.visualization.json.VirtualHostManager;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Test for basic scenarios in VisualizationControllerTest.
 */
public class VisualizationManagerTest extends BaseTestCaseWithUser {

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Basic test for proxy hierarchy
     * @throws Exception if anything goes wrong
     */
    public void testProxyHierarchy() throws Exception {
        Server proxy = createTestProxy();
        SystemManager.storeServer(proxy);
        String proxyHostname = "proxyHostname";

        Server client1 = ServerFactoryTest.createTestServer(user);
        Server client2 = ServerFactoryTest.createTestServer(user);
        Server client3 = ServerFactoryTest.createTestServer(user);

        Set<ServerPath> serverPaths = ServerFactory.createServerPaths(client1, proxy, proxyHostname);
        client1.getServerPaths().addAll(serverPaths);
        serverPaths = ServerFactory.createServerPaths(client2, proxy, proxyHostname);
        client2.getServerPaths().addAll(serverPaths);
        serverPaths = ServerFactory.createServerPaths(client3, proxy, proxyHostname);
        client3.getServerPaths().addAll(serverPaths);

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        List<Object> hierarchy = VisualizationManager.proxyHierarchy(user);

        System proxyProfile = extractSingleSystemByRawId(hierarchy.stream(),
                proxy.getId().toString());
        assertEquals(proxy.getName(), proxyProfile.getName());
        assertEquals("root", proxyProfile.getParentId());

        System client1Profile = extractSingleSystemByRawId(hierarchy.stream(),
                client1.getId().toString());
        assertEquals(client1.getName(), client1Profile.getName());
        assertEquals(proxy.getId().toString(), client1Profile.getParentId());

        System client2Profile = extractSingleSystemByRawId(hierarchy.stream(),
                client2.getId().toString());
        assertEquals(client2.getName(), client2Profile.getName());
        assertEquals(proxy.getId().toString(), client2Profile.getParentId());

        System client3Profile = extractSingleSystemByRawId(hierarchy.stream(),
                client3.getId().toString());
        assertEquals(client3.getName(), client3Profile.getName());
        assertEquals(proxy.getId().toString(), client3Profile.getParentId());

        assertEquals(1, hierarchy.stream()
                .filter(o -> (o instanceof System))
                .map(o -> ((System) o))
                .filter(p -> p.getName().equals(proxy.getName()))
                .count());

        assertEquals(3, hierarchy.stream()
                .filter(o -> (o instanceof System))
                .map(o -> ((System) o))
                .filter(p -> proxy.getId().toString().equals(p.getParentId()))
                .count());
    }

    /**
     * Test for virtualization hierarchy
     * @throws Exception if anything goes wrong
     */
    public void testVirtualizationHierarchy() throws Exception {
        Server host = ServerFactoryTest.createTestServer(user, true);
        VirtualInstance vi = new GuestBuilder(user).createGuest().build();
        vi.setHostSystem(host);
        HibernateFactory.getSession().save(vi);

        com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManager vhm =
                VirtualHostManagerFactory.getInstance().createVirtualHostManager("myVHM",
                        user.getOrg(), "file", Collections.emptyMap());
        vhm.addServer(host);
        HibernateFactory.getSession().save(vhm);

        Server host2 = ServerFactoryTest.createTestServer(user, true);
        VirtualInstance vi2 = new GuestBuilder(user).createGuest().build();
        vi2.setHostSystem(host2);
        HibernateFactory.getSession().save(vi2);

        List<Object> hierarchy = VisualizationManager.virtualizationHierarchy(user);

        System hostProfile = extractSingleSystemByRawId(hierarchy.stream(), host.getId().toString());
        assertEquals(host.getName(), hostProfile.getName());
        assertEquals(vhm.getId().toString(), hostProfile.getParentId());

        System guestProfile = extractSingleSystemByRawId(hierarchy.stream(), vi.getGuestSystem().getId().toString());
        assertEquals(vi.getGuestSystem().getName(), guestProfile.getName());
        assertEquals(host.getId().toString(), guestProfile.getParentId());

        VirtualHostManager vhmProfile = extractSingleVHM(hierarchy.stream(), vhm.getId().toString());
        assertEquals("root", vhmProfile.getParentId());

        System hostProfile2 = extractSingleSystemByRawId(hierarchy.stream(), host2.getId().toString());
        assertEquals(host2.getName(), hostProfile2.getName());
        assertEquals("unknown-vhm", hostProfile2.getParentId());

        System root = extractSingleSystemById(hierarchy.stream(), "root");
        assertNull(root.getParentId());
    }

    private System extractSingleSystemByRawId(Stream<?> stream, String id) {
        List<System> list = stream
                .filter(o -> o instanceof System && ((System) o).getRawId().equals(id))
                .map(o -> ((System) o))
                .collect(Collectors.toList());

        assertEquals(1, list.size());
        return list.get(0);
    }

    private System extractSingleSystemById(Stream<?> stream, String id) {
        List<System> list = stream
                .filter(o -> o instanceof System && ((System) o).getId().equals(id))
                .map(o -> ((System) o))
                .collect(Collectors.toList());

        assertEquals(1, list.size());
        return list.get(0);
    }

    private VirtualHostManager extractSingleVHM(Stream<?> stream, String id) {
        List<VirtualHostManager> list = stream
                .filter(o -> o instanceof VirtualHostManager && ((VirtualHostManager) o).getId().equals(id))
                .map(o -> ((VirtualHostManager) o))
                .collect(Collectors.toList());

        assertEquals(1, list.size());
        return list.get(0);
    }

    /**
     * Creates and saves a proxy system.
     *
     * @return the proxy system
     * @throws Exception if anything goes wrong
     */
    private Server createTestProxy() throws Exception {
        return ServerFactoryTest.createTestProxyServer(user, true);
    }
}
