/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
 * Copyright (c) 2025 SUSE LLC
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
package com.redhat.rhn.frontend.xmlrpc.proxy;

import static com.redhat.rhn.domain.server.ServerFactory.createServerPaths;
import static com.suse.proxy.ProxyConfigUtils.REGISTRY_MODE_ADVANCED;
import static com.suse.proxy.ProxyConfigUtils.REGISTRY_MODE_SIMPLE;
import static com.suse.proxy.ProxyConfigUtils.SOURCE_MODE_REGISTRY;
import static com.suse.proxy.ProxyConfigUtils.SOURCE_MODE_RPM;
import static com.suse.proxy.ProxyConfigUtils.USE_CERTS_MODE_REPLACE;
import static java.lang.Math.toIntExact;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.client.ClientCertificate;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.MinionServerFactoryTest;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerConstants;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerFactoryTest;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.server.ServerPath;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.system.XmlRpcSystemHelper;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.testing.RhnJmockBaseTestCase;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.cloud.CloudPaygManager;
import com.suse.cloud.TestCloudPaygManagerBuilder;
import com.suse.manager.attestation.AttestationManager;
import com.suse.manager.ssl.SSLCertData;
import com.suse.manager.ssl.SSLCertManager;
import com.suse.manager.ssl.SSLCertPair;
import com.suse.manager.webui.controllers.bootstrap.RegularMinionBootstrapper;
import com.suse.manager.webui.controllers.bootstrap.SSHMinionBootstrapper;
import com.suse.manager.webui.services.TestSaltApi;
import com.suse.manager.webui.services.TestSystemQuery;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.manager.webui.utils.gson.ProxyConfigUpdateJson;
import com.suse.proxy.update.ProxyConfigUpdateFacade;
import com.suse.proxy.update.ProxyConfigUpdateFacadeImpl;

import org.hamcrest.Matchers;
import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ProxyHandlerTest extends RhnJmockBaseTestCase {

    private final SaltApi saltApi = new TestSaltApi();
    private final SystemQuery systemQuery = new TestSystemQuery();
    private final CloudPaygManager paygManager = new TestCloudPaygManagerBuilder().build();
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
    private final ProxyConfigUpdateFacade proxyConfigUpdateFacade = new ProxyConfigUpdateFacadeImpl();

    @BeforeEach
    public void setup() throws Exception {
        super.setUp();
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
    }

    @Test
    public void testDeactivateProxyWithReload() throws Exception {
        User user = UserTestUtils.createUser();
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        Server server = ServerFactoryTest.createTestProxyServer(user, true);
        assertTrue(server.isProxy());
        Server changedServer = SystemManager.deactivateProxy(server);
        assertFalse(changedServer.isProxy());
    }

    @Test
    public void testActivateProxy() throws Exception {
        User user = UserTestUtils.createUser();
        ProxyHandler ph = new ProxyHandler(xmlRpcSystemHelper, systemManager, proxyConfigUpdateFacade);

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
        User user = UserTestUtils.createUser();
        ProxyHandler ph = new ProxyHandler(xmlRpcSystemHelper, systemManager, proxyConfigUpdateFacade);

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
        User user = UserTestUtils.createUser();
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        Server server = ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeEnterpriseEntitled(),
                ServerFactoryTest.TYPE_SERVER_PROXY);

        // TODO: need to actually create a valid proxy server, the
        // above doesn't come close to creating a REAL server.

        ClientCertificate cert = SystemManager.createClientCertificate(server);
        cert.validate(server.getSecret());

        ProxyHandler ph = new ProxyHandler(xmlRpcSystemHelper, systemManager, proxyConfigUpdateFacade);
        int rc = ph.deactivateProxy(cert.toString());
        assertEquals(1, rc);
    }

    @Test
    public void testListProxyClients() throws Exception {
        // create user
        User user = UserTestUtils.createUser();
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
        List<Long> clientIds = new ProxyHandler(xmlRpcSystemHelper, systemManager, proxyConfigUpdateFacade)
                .listProxyClients(user, toIntExact(proxy.getId()));

        // verify client id is in results

        assertTrue(clientIds.contains(minion.getId()));
    }

    @Test
    public void testContainerConfig() throws Exception {
        User user = UserTestUtils.createUser();
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

        byte[] actual = new ProxyHandler(xmlRpcSystemHelper, mockSystemManager, proxyConfigUpdateFacade)
                        .containerConfig(user, proxy, 8022, server, 2048, email, "ROOT_CA",
                                         List.of("CA1", "CA2"), "PROXY_CERT", "PROXY_KEY");
        assertEquals(dummyConfig, actual);
    }

    @Test
    public void testContainerConfigGenerateCert() throws Exception {
        User user = UserTestUtils.createUser();
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

        byte[] actual = new ProxyHandler(xmlRpcSystemHelper, mockSystemManager, proxyConfigUpdateFacade)
                .containerConfig(user, proxy, 22, server,
                2048, email, "CACert", "CAKey", "CAPass", List.of("cname1", "cname2"),
                "DE", "Bayern", "Nurnberg", "ACME", "ACME Tests", "coyote@acme.lab");
        assertEquals(dummyConfig, actual);
    }

    @Test
    public void testBootstrapProxyRpm() {
        User user = UserTestUtils.createUser();

        SystemManager mockSystemManager = mock(SystemManager.class);
        ProxyConfigUpdateFacade mockProxyConfigUpdateFacade = mock(ProxyConfigUpdateFacadeImpl.class);

        context().checking(new Expectations() {{
            oneOf(mockProxyConfigUpdateFacade).update(
                with(allOf(
                    Matchers.<ProxyConfigUpdateJson>hasProperty("serverId", equalTo(200L)),
                    Matchers.<ProxyConfigUpdateJson>hasProperty("parentFqdn", equalTo("server.name")),
                    Matchers.<ProxyConfigUpdateJson>hasProperty("proxyPort", equalTo(8022)),
                    Matchers.<ProxyConfigUpdateJson>hasProperty("maxCache", equalTo(100)),
                    Matchers.<ProxyConfigUpdateJson>hasProperty("email", equalTo("email@test.com")),
                    Matchers.<ProxyConfigUpdateJson>hasProperty("useCertsMode", equalTo(USE_CERTS_MODE_REPLACE)),
                    Matchers.<ProxyConfigUpdateJson>hasProperty("rootCA", equalTo("ca-string")),
                    Matchers.<ProxyConfigUpdateJson>hasProperty("intermediateCAs", equalTo(Collections.emptyList())),
                    Matchers.<ProxyConfigUpdateJson>hasProperty("proxyCert", equalTo("cert-string")),
                    Matchers.<ProxyConfigUpdateJson>hasProperty("proxyKey", equalTo("cert-key")),
                    Matchers.<ProxyConfigUpdateJson>hasProperty("sourceMode", equalTo(SOURCE_MODE_RPM))
                )),
                with(equal(mockSystemManager)),
                with(equal(user))
            );
        }});

        ProxyHandler proxyHandler = new ProxyHandler(xmlRpcSystemHelper, mockSystemManager,
                                                     mockProxyConfigUpdateFacade);
        proxyHandler.bootstrapProxy(user, 200, "server.name", 8022, 100, "email@test.com", "ca-string",
                                    Collections.emptyList(), "cert-string", "cert-key");
    }

    @Test
    public void testBootstrapProxyRegistrySimple() {
        User user = UserTestUtils.createUser();

        SystemManager mockSystemManager = mock(SystemManager.class);
        ProxyConfigUpdateFacade mockProxyConfigUpdateFacade = mock(ProxyConfigUpdateFacadeImpl.class);

        context().checking(new Expectations() {{
            oneOf(mockProxyConfigUpdateFacade).update(
                with(allOf(
                    Matchers.<ProxyConfigUpdateJson>hasProperty("serverId", equalTo(200L)),
                    Matchers.<ProxyConfigUpdateJson>hasProperty("parentFqdn", equalTo("server.name")),
                    Matchers.<ProxyConfigUpdateJson>hasProperty("proxyPort", equalTo(8022)),
                    Matchers.<ProxyConfigUpdateJson>hasProperty("maxCache", equalTo(100)),
                    Matchers.<ProxyConfigUpdateJson>hasProperty("email", equalTo("email@test.com")),
                    Matchers.<ProxyConfigUpdateJson>hasProperty("useCertsMode", equalTo(USE_CERTS_MODE_REPLACE)),
                    Matchers.<ProxyConfigUpdateJson>hasProperty("rootCA", equalTo("ca-string")),
                    Matchers.<ProxyConfigUpdateJson>hasProperty("intermediateCAs", equalTo(Collections.emptyList())),
                    Matchers.<ProxyConfigUpdateJson>hasProperty("proxyCert", equalTo("cert-string")),
                    Matchers.<ProxyConfigUpdateJson>hasProperty("proxyKey", equalTo("cert-key")),
                    Matchers.<ProxyConfigUpdateJson>hasProperty("sourceMode", equalTo(SOURCE_MODE_REGISTRY)),
                    Matchers.<ProxyConfigUpdateJson>hasProperty("registryMode", equalTo(REGISTRY_MODE_SIMPLE)),
                    Matchers.<ProxyConfigUpdateJson>hasProperty("registryBaseURL", equalTo("registry-url")),
                    Matchers.<ProxyConfigUpdateJson>hasProperty("registryBaseTag", equalTo("tag"))
                )),
                with(equal(mockSystemManager)),
                with(equal(user))
            );
        }});

        ProxyHandler proxyHandler = new ProxyHandler(xmlRpcSystemHelper, mockSystemManager,
                                                     mockProxyConfigUpdateFacade);
        proxyHandler.bootstrapProxy(user, 200, "server.name", 8022, 100, "email@test.com", "ca-string",
                                    Collections.emptyList(), "cert-string", "cert-key", "registry-url", "tag");
    }


    @Test
    public void testBootstrapProxyRegistryAdvanced() {
        User user = UserTestUtils.createUser();

        SystemManager mockSystemManager = mock(SystemManager.class);
        ProxyConfigUpdateFacade mockProxyConfigUpdateFacade = mock(ProxyConfigUpdateFacadeImpl.class);

        context().checking(new Expectations() {{
            oneOf(mockProxyConfigUpdateFacade).update(
                with(allOf(
                    Matchers.<ProxyConfigUpdateJson>hasProperty("serverId", equalTo(200L)),
                    Matchers.<ProxyConfigUpdateJson>hasProperty("parentFqdn", equalTo("server.name")),
                    Matchers.<ProxyConfigUpdateJson>hasProperty("proxyPort", equalTo(8022)),
                    Matchers.<ProxyConfigUpdateJson>hasProperty("maxCache", equalTo(100)),
                    Matchers.<ProxyConfigUpdateJson>hasProperty("email", equalTo("email@test.com")),
                    Matchers.<ProxyConfigUpdateJson>hasProperty("useCertsMode", equalTo(USE_CERTS_MODE_REPLACE)),
                    Matchers.<ProxyConfigUpdateJson>hasProperty("rootCA", equalTo("ca-string")),
                    Matchers.<ProxyConfigUpdateJson>hasProperty("intermediateCAs", equalTo(Collections.emptyList())),
                    Matchers.<ProxyConfigUpdateJson>hasProperty("proxyCert", equalTo("cert-string")),
                    Matchers.<ProxyConfigUpdateJson>hasProperty("proxyKey", equalTo("cert-key")),
                    Matchers.<ProxyConfigUpdateJson>hasProperty("sourceMode", equalTo(SOURCE_MODE_REGISTRY)),
                    Matchers.<ProxyConfigUpdateJson>hasProperty("registryMode", equalTo(REGISTRY_MODE_ADVANCED)),
                    Matchers.<ProxyConfigUpdateJson>hasProperty("registryBaseURL", equalTo(null)),
                    Matchers.<ProxyConfigUpdateJson>hasProperty("registryBaseTag", equalTo(null)),
                    Matchers.<ProxyConfigUpdateJson>hasProperty("registryHttpdURL", equalTo("http-registry-url")),
                    Matchers.<ProxyConfigUpdateJson>hasProperty("registryHttpdTag", equalTo("http-tag")),
                    Matchers.<ProxyConfigUpdateJson>hasProperty("registrySaltbrokerURL", equalTo("salt-registry-url")),
                    Matchers.<ProxyConfigUpdateJson>hasProperty("registrySaltbrokerTag", equalTo("salt-tag")),
                    Matchers.<ProxyConfigUpdateJson>hasProperty("registrySquidURL", equalTo("squid-registry-url")),
                    Matchers.<ProxyConfigUpdateJson>hasProperty("registrySquidTag", equalTo("squid-tag")),
                    Matchers.<ProxyConfigUpdateJson>hasProperty("registrySshURL", equalTo("ssh-registry-url")),
                    Matchers.<ProxyConfigUpdateJson>hasProperty("registrySshTag", equalTo("ssh-tag")),
                    Matchers.<ProxyConfigUpdateJson>hasProperty("registryTftpdURL", equalTo("tftpd-registry-url")),
                    Matchers.<ProxyConfigUpdateJson>hasProperty("registryTftpdTag", equalTo("tftpd-tag"))
                )),
                with(equal(mockSystemManager)),
                with(equal(user))
            );
        }});

        ProxyHandler proxyHandler = new ProxyHandler(xmlRpcSystemHelper, mockSystemManager,
                                                     mockProxyConfigUpdateFacade);
        proxyHandler.bootstrapProxy(user, 200, "server.name", 8022, 100, "email@test.com", "ca-string",
                                    Collections.emptyList(), "cert-string", "cert-key",
                                    "http-registry-url", "http-tag",
                                    "salt-registry-url", "salt-tag",
                                    "squid-registry-url", "squid-tag",
                                    "ssh-registry-url", "ssh-tag",
                                    "tftpd-registry-url", "tftpd-tag"
                                    );
    }
}
