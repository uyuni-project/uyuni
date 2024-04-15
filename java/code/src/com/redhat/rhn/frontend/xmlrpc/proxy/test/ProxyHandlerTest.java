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
package com.redhat.rhn.frontend.xmlrpc.proxy.test;

import static com.redhat.rhn.domain.server.ServerFactory.createServerPaths;
import static java.lang.Math.toIntExact;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.client.ClientCertificate;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerConstants;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.server.ServerPath;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.proxy.ProxyHandler;
import com.redhat.rhn.frontend.xmlrpc.system.XmlRpcSystemHelper;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.testing.RhnJmockBaseTestCase;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.cloud.CloudPaygManager;
import com.suse.manager.attestation.AttestationManager;
import com.suse.manager.ssl.SSLCertData;
import com.suse.manager.ssl.SSLCertManager;
import com.suse.manager.ssl.SSLCertPair;
import com.suse.manager.webui.controllers.bootstrap.RegularMinionBootstrapper;
import com.suse.manager.webui.controllers.bootstrap.SSHMinionBootstrapper;
import com.suse.manager.webui.services.SaltStateGeneratorService;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.manager.webui.services.test.TestSaltApi;
import com.suse.manager.webui.services.test.TestSystemQuery;

import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ProxyHandlerTest extends RhnJmockBaseTestCase {

    private static final String TEST_USER = "testuser";
    private static final String TEST_ORG = "testorg";

    private final SaltApi saltApi = new TestSaltApi();
    private final SystemQuery systemQuery = new TestSystemQuery();
    private final CloudPaygManager paygManager = new CloudPaygManager();
    private final AttestationManager attestationManager = new AttestationManager();
    private final RegularMinionBootstrapper regularMinionBootstrapper =
            new RegularMinionBootstrapper(systemQuery, saltApi, paygManager, attestationManager);
    private final SSHMinionBootstrapper sshMinionBootstrapper =
            new SSHMinionBootstrapper(systemQuery, saltApi, paygManager, attestationManager);
    private final XmlRpcSystemHelper xmlRpcSystemHelper = new XmlRpcSystemHelper(
            regularMinionBootstrapper,
            sshMinionBootstrapper
    );
    private final SystemManager systemManager = new SystemManager(ServerFactory.SINGLETON, ServerGroupFactory.SINGLETON,
            saltApi);

    @BeforeEach
    protected void setUp() {
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        SaltStateGeneratorService.INSTANCE.setSkipSetOwner(true);
    }

    @Test
    public void testDeactivateProxyWithReload() throws Exception {
        User user = UserTestUtils.findNewUser(TEST_USER, TEST_ORG);
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        Server server = ServerFactoryTest.createTestProxyServer(user, true);
        assertTrue(server.isProxy());
        Server changedServer = SystemManager.deactivateProxy(server);
        assertFalse(changedServer.isProxy());
    }

    @Test
    public void testActivateProxy() throws Exception {
        User user = UserTestUtils.findNewUser(TEST_USER, TEST_ORG);
        ProxyHandler ph = new ProxyHandler(xmlRpcSystemHelper, systemManager);

        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        Server server = ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeEnterpriseEntitled(),
                ServerFactoryTest.TYPE_SERVER_NORMAL);

        ClientCertificate cert = SystemManager.createClientCertificate(server);
        cert.validate(server.getSecret());

        int rc = ph.activateProxy(cert.toString(), "5.0");
        assertEquals(1, rc);
    }

    @Test
    public void testActivateSaltProxy() throws Exception {
        User user = UserTestUtils.findNewUser(TEST_USER, TEST_ORG);
        ProxyHandler ph = new ProxyHandler(xmlRpcSystemHelper, systemManager);

        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        Server server = ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeSaltEntitled(),
                ServerFactoryTest.TYPE_SERVER_NORMAL);

        ClientCertificate cert = SystemManager.createClientCertificate(server);
        cert.validate(server.getSecret());

        int rc = ph.activateProxy(cert.toString(), "5.0");
        assertEquals(1, rc);
    }

    @Test
    public void testDeactivateProxy() throws Exception {
        User user = UserTestUtils.findNewUser(TEST_USER, TEST_ORG);
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        Server server = ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeEnterpriseEntitled(),
                ServerFactoryTest.TYPE_SERVER_PROXY);

        // TODO: need to actually create a valid proxy server, the
        // above doesn't come close to creating a REAL server.

        ClientCertificate cert = SystemManager.createClientCertificate(server);
        cert.validate(server.getSecret());

        ProxyHandler ph = new ProxyHandler(xmlRpcSystemHelper, systemManager);
        int rc = ph.deactivateProxy(cert.toString());
        assertEquals(1, rc);
    }

    @Test
    public void testListProxyClients() throws Exception {
        // create user
        User user = UserTestUtils.findNewUser(TEST_USER, TEST_ORG);
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
        List<Long> clientIds = new ProxyHandler(xmlRpcSystemHelper, systemManager)
                .listProxyClients(user, toIntExact(proxy.getId()));

        // verify client id is in results

        assertTrue(clientIds.contains(minion.getId()));
    }

    @Test
    public void testContainerConfig() throws Exception {
        User user = UserTestUtils.findNewUser(TEST_USER, TEST_ORG);
        byte[] dummyConfig = "Dummy config".getBytes();
        String server = "srv.acme.lab";
        String proxy = "proxy.acme.lab";
        String email = "admin@acme.lab";

        SystemManager mockSystemManager = mock(SystemManager.class);
        context().checking(new Expectations() {{
            allowing(mockSystemManager).createProxyContainerConfig(
                    with(equal(user)), with(equal(proxy)), with(equal(8022)), with(equal(server)), with(equal(2048L)),
                    with(equal(email)), with(equal("ROOT_CA")), with(equal(List.of("CA1", "CA2"))),
                    with(equal(new SSLCertPair("PROXY_CERT", "PROXY_KEY"))),
                    with(aNull(SSLCertPair.class)), with(aNull(String.class)), with(aNull(SSLCertData.class)),
                    with(any(SSLCertManager.class)));
            will(returnValue(dummyConfig));
        }});

        byte[] actual = new ProxyHandler(xmlRpcSystemHelper, mockSystemManager).containerConfig(user, proxy, 8022,
                server, 2048, email, "ROOT_CA", List.of("CA1", "CA2"), "PROXY_CERT", "PROXY_KEY");
        assertEquals(dummyConfig, actual);
    }

    @Test
    public void testContainerConfigGenerateCert() throws Exception {
        User user = UserTestUtils.findNewUser(TEST_USER, TEST_ORG);
        byte[] dummyConfig = "Dummy config".getBytes();
        String server = "srv.acme.lab";
        String proxy = "proxy.acme.lab";
        String email = "admin@acme.lab";

        SystemManager mockSystemManager = mock(SystemManager.class);
        context().checking(new Expectations() {{
            allowing(mockSystemManager).createProxyContainerConfig(
                    with(equal(user)), with(equal(proxy)), with(equal(22)), with(equal(server)), with(equal(2048L)),
                    with(equal(email)), with(aNull(String.class)), with(equal(Collections.emptyList())),
                    with(aNull(SSLCertPair.class)),
                    with(equal(new SSLCertPair("CACert", "CAKey"))), with(equal("CAPass")),
                    with(equal(new SSLCertData(proxy, List.of("cname1", "cname2"), "DE", "Bayern",
                            "Nurnberg", "ACME", "ACME Tests", "coyote@acme.lab"))),
                    with(any(SSLCertManager.class)));
            will(returnValue(dummyConfig));
        }});

        byte[] actual = new ProxyHandler(xmlRpcSystemHelper, mockSystemManager).containerConfig(user, proxy, 22, server,
                2048, email, "CACert", "CAKey", "CAPass", List.of("cname1", "cname2"),
                "DE", "Bayern", "Nurnberg", "ACME", "ACME Tests", "coyote@acme.lab");
        assertEquals(dummyConfig, actual);
    }
}
