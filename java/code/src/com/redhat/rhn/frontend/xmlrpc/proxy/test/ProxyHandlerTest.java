/**
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
package com.redhat.rhn.frontend.xmlrpc.proxy.test;

import static com.redhat.rhn.domain.server.ServerFactory.createServerPaths;
import static java.lang.Math.toIntExact;

import com.redhat.rhn.common.client.ClientCertificate;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerConstants;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerPath;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.proxy.ProxyHandler;
import com.redhat.rhn.frontend.xmlrpc.system.XmlRpcSystemHelper;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.UserTestUtils;
import com.suse.manager.webui.controllers.utils.RegularMinionBootstrapper;
import com.suse.manager.webui.controllers.utils.SSHMinionBootstrapper;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.manager.webui.services.impl.SaltService;

import java.util.List;
import java.util.Set;

public class ProxyHandlerTest extends RhnBaseTestCase {

    private SystemQuery systemQuery = new SaltService();
    private RegularMinionBootstrapper regularMinionBootstrapper = RegularMinionBootstrapper.getInstance(systemQuery);
    private SSHMinionBootstrapper sshMinionBootstrapper = SSHMinionBootstrapper.getInstance(systemQuery);
    private XmlRpcSystemHelper xmlRpcSystemHelper = new XmlRpcSystemHelper(
            regularMinionBootstrapper,
            sshMinionBootstrapper
    );

    public void testDeactivateProxyWithReload() throws Exception {
        User user = UserTestUtils.findNewUser("testuser", "testorg");
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        Server server = ServerFactoryTest.createTestProxyServer(user, true);
        assertTrue(server.isProxy());
        server = SystemManager.deactivateProxy(server);
        assertFalse(server.isProxy());
    }

    public void testActivateProxy() throws Exception {
        User user = UserTestUtils.findNewUser("testuser", "testorg");
        ProxyHandler ph = new ProxyHandler(xmlRpcSystemHelper);

        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        Server server = ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeEnterpriseEntitled(),
                ServerFactoryTest.TYPE_SERVER_NORMAL);

        ClientCertificate cert = SystemManager.createClientCertificate(server);
        cert.validate(server.getSecret());

        int rc = ph.activateProxy(cert.toString(), "5.0");
        assertEquals(1, rc);
    }

    public void testActivateSaltProxy() throws Exception {
        User user = UserTestUtils.findNewUser("testuser", "testorg");
        ProxyHandler ph = new ProxyHandler(xmlRpcSystemHelper);

        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        Server server = ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeSaltEntitled(),
                ServerFactoryTest.TYPE_SERVER_NORMAL);

        ClientCertificate cert = SystemManager.createClientCertificate(server);
        cert.validate(server.getSecret());

        int rc = ph.activateProxy(cert.toString(), "5.0");
        assertEquals(1, rc);
    }

    public void testDeactivateProxy() throws Exception {
        User user = UserTestUtils.findNewUser("testuser", "testorg");
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        Server server = ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeEnterpriseEntitled(),
                ServerFactoryTest.TYPE_SERVER_PROXY);

        // TODO: need to actually create a valid proxy server, the
        // above doesn't come close to creating a REAL server.

        ClientCertificate cert = SystemManager.createClientCertificate(server);
        cert.validate(server.getSecret());

        ProxyHandler ph = new ProxyHandler(xmlRpcSystemHelper);
        int rc = ph.deactivateProxy(cert.toString());
        assertEquals(1, rc);
    }

    public void aTestWithExistingProxy() throws Exception {
        Server server = ServerFactory.lookupById(1005012107L);
        ClientCertificate cert = SystemManager.createClientCertificate(server);
        cert.validate(server.getSecret());
        ProxyHandler ph = new ProxyHandler(xmlRpcSystemHelper);
        int rc = ph.deactivateProxy(cert.toString());
        assertEquals(1, rc);
    }

    public void testLameTest() {
        assertNotNull(new ProxyHandler(xmlRpcSystemHelper));
    }

    public void testListProxyClients() throws Exception {
        // create user
        User user = UserTestUtils.findNewUser("testuser", "testorg");
        user.addPermanentRole(RoleFactory.ORG_ADMIN);

        // create proxy
        Server proxy = ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeSaltEntitled(),
                ServerFactoryTest.TYPE_SERVER_PROXY);

        // create minion behind proxy
        Server minion = MinionServerFactoryTest.createTestMinionServer(user);
        Set<ServerPath> proxyPaths = createServerPaths(minion, proxy, proxy.getName());
        minion.getServerPaths().addAll(proxyPaths);

        // call method
        List<Long> clientIds = new ProxyHandler(xmlRpcSystemHelper).listProxyClients(user, toIntExact(proxy.getId()));

        // verify client id is in results
        assertContains(clientIds, minion.getId());
    }
}
