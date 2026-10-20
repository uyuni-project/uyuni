/*
 * Copyright (c) 2026 SUSE LLC
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

package com.redhat.rhn.manager.system.proxycontainerconfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.RhnRuntimeException;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactoryTest;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerFactoryTest;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.manager.webui.services.TestSaltApi;
import com.suse.manager.webui.services.iface.SaltApi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link ProxyContainerConfigCreateAcquisitor}, focused on the parent consistency
 * checks applied to already registered systems in {@code getOrCreateProxySystem}.
 */
public class ProxyContainerConfigCreateAcquisitorTest extends BaseTestCaseWithUser {

    private final SaltApi saltApi = new TestSaltApi();
    private final SystemEntitlementManager systemEntitlementManager = new SystemEntitlementManager(
            new SystemUnentitler(saltApi), new SystemEntitler(saltApi)
    );

    private String proxyFqdn;
    private String parentFqdn;
    private String otherParentFqdn;
    private Server parentProxy;
    private Server otherParentProxy;

    @BeforeEach
    public void setUp() throws Exception {
        // createClientCertificate needs an org admin in the server's organization
        UserTestUtils.addUserRole(user, RoleFactory.ORG_ADMIN);

        // unique FQDNs per test: the acquisitor rejects FQDNs already registered on other systems
        String suffix = TestUtils.randomString();
        proxyFqdn = "child-" + suffix + ".example.com";
        parentFqdn = "parent-" + suffix + ".example.com";
        otherParentFqdn = "other-" + suffix + ".example.com";

        parentProxy = createTestProxy(parentFqdn);

        // only a name holder for the current parent in the mismatch test, no proxy needed there
        otherParentProxy = ServerFactoryTest.createTestServer(user, true);
        otherParentProxy.setHostname(otherParentFqdn);
        otherParentProxy.addFqdn(otherParentFqdn);
        ServerFactory.save(otherParentProxy);
    }

    /**
     * An already registered Salt minion connected directly to the server must not be
     * re-parented to a proxy: the configuration would rewrite its server path and proxy
     * settings while its Salt master stays the server.
     */
    @Test
    public void testRejectProxyParentForDirectlyConnectedMinion() {
        MinionServer minion = createDirectlyConnectedMinion(proxyFqdn);

        ProxyContainerConfigCreateContext context = createContext(proxyFqdn, parentFqdn);

        RhnRuntimeException e = assertThrows(RhnRuntimeException.class,
                () -> new ProxyContainerConfigCreateAcquisitor().handle(context));
        assertTrue(e.getMessage().contains("already registered and connected directly to the server"),
                e.getMessage());

        // nothing was written for the rejected configuration
        assertEquals(0, TestUtils.reload(minion).getServerPaths().size());
    }

    /**
     * A directly connected Salt minion stays configurable when the requested parent is the
     * server itself.
     */
    @Test
    public void testAcceptServerParentForDirectlyConnectedMinion() {
        MinionServer minion = createDirectlyConnectedMinion(proxyFqdn);

        ProxyContainerConfigCreateContext context =
                createContext(proxyFqdn, ConfigDefaults.get().getJavaHostname());

        new ProxyContainerConfigCreateAcquisitor().handle(context);

        Server reloaded = TestUtils.reload(minion);
        assertTrue(reloaded.hasEntitlement(EntitlementManager.PROXY));
        assertEquals(0, reloaded.getServerPaths().size());
        assertNotNull(context.getClientCertificate());
    }

    /**
     * A minion already connected via another proxy still hits the parent mismatch check.
     */
    @Test
    public void testRejectDifferentParentForConnectedMinion() {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        minion.addFqdn(proxyFqdn);
        minion.getServerPaths().addAll(
                ServerFactory.createServerPaths(minion, otherParentProxy, otherParentFqdn));
        ServerFactory.save(minion);
        TestUtils.flushAndClearSession();

        ProxyContainerConfigCreateContext context = createContext(proxyFqdn, parentFqdn);

        RhnRuntimeException e = assertThrows(RhnRuntimeException.class,
                () -> new ProxyContainerConfigCreateAcquisitor().handle(context));
        assertTrue(e.getMessage().contains("already registered and connected via"), e.getMessage());
    }

    private Server createTestProxy(String fqdn) throws Exception {
        Server proxy = ServerFactoryTest.createTestProxyServer(user, true);
        proxy.setHostname(fqdn);
        proxy.addFqdn(fqdn);
        ServerFactory.save(proxy);
        return proxy;
    }

    private MinionServer createDirectlyConnectedMinion(String fqdn) {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        minion.addFqdn(fqdn);
        ServerFactory.save(minion);
        return minion;
    }

    /**
     * Build a context for the acquisitor. The {@code serverFqdn} parameter acts as the
     * requested parent. Certificates and SSH keys are preset so that no external
     * services are needed.
     */
    private ProxyContainerConfigCreateContext createContext(String fqdn, String requestedParentFqdn) {
        return new ProxyContainerConfigCreateContext(
                saltApi,
                user,
                systemEntitlementManager,
                requestedParentFqdn,
                fqdn,
                8022,
                2048L,
                "admin@example.org",
                null,               // rootCA - skip certificate processing
                null,               // intermediateCAs
                null,               // proxyCertKey
                null,               // caPair
                null,               // caPassword
                null,               // certData
                null,               // certManager
                "proxy-ssh-pub",    // sshPub - preset, no key generation needed
                "proxy-ssh-key",
                "server-ssh-pub",   // sshParent - preset, no lookup needed
                null                // additionalFqdns
        );
    }
}
