/*
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

package com.suse.proxy.test;

import static com.suse.proxy.ProxyConfigUtils.REGISTRY_BASE_TAG;
import static com.suse.proxy.ProxyConfigUtils.REGISTRY_BASE_URL;
import static com.suse.proxy.ProxyConfigUtils.REGISTRY_MODE;
import static com.suse.proxy.ProxyConfigUtils.SOURCE_MODE_FIELD;
import static com.suse.proxy.ProxyConfigUtils.SOURCE_MODE_RPM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerConstants;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import com.suse.proxy.get.formdata.ProxyConfigGetFormDataAcquisitor;
import com.suse.proxy.get.formdata.ProxyConfigGetFormDataContext;
import com.suse.proxy.model.ProxyConfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Tests for the {@link ProxyConfigGetFormDataAcquisitor} class
 */
public class ProxyConfigGetFormDataAcquisitorTest extends BaseTestCaseWithUser {

    private MinionServer testMinionServer;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        testMinionServer = MinionServerFactoryTest.createTestMinionServer(user);
    }

    @Test
    public void testWhenNoProxyConfigAndNoProxies() {
        final String expectedElectableParent = Config.get().getString(ConfigDefaults.SERVER_HOSTNAME);
        ProxyConfigGetFormDataContext proxyConfigGetFormDataContext = new ProxyConfigGetFormDataContext(user,
                testMinionServer, null, GlobalInstanceHolder.SYSTEM_ENTITLEMENT_MANAGER);

        //
        new ProxyConfigGetFormDataAcquisitor().handle(proxyConfigGetFormDataContext);

        assertTrue(proxyConfigGetFormDataContext.getProxyConfigAsMap().isEmpty());
        assertEquals(1, proxyConfigGetFormDataContext.getElectableParentsFqdn().size());
        assertEquals(
                expectedElectableParent,
                proxyConfigGetFormDataContext.getElectableParentsFqdn().iterator().next()
        );
    }

    /**
     * Test a scenario where:
     * a) a proxy configuration exists
     * b) two other proxies exist
     * c) an extra server exists
     * Asserts that:
     * - no defaults override proxy configuration
     * - the electable parents contain all the proxies and the current server (excluding the extra server)
     */
    @Test
    public void testWhenNoProxyConfigAndMultipleProxies() {
        Server proxyA = ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeEnterpriseEntitled(),
                ServerFactoryTest.TYPE_SERVER_PROXY);

        Server proxyB = ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeEnterpriseEntitled(),
                ServerFactoryTest.TYPE_SERVER_PROXY);

        ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeEnterpriseEntitled(),
                ServerFactoryTest.TYPE_SERVER_NORMAL);

        final String[] expectedElectableParentsFqdn = new String[]{
                Config.get().getString(ConfigDefaults.SERVER_HOSTNAME),
                proxyA.getName(),
                proxyB.getName(),
        };

        ProxyConfigGetFormDataContext proxyConfigGetFormDataContext = new ProxyConfigGetFormDataContext(user,
                testMinionServer, new ProxyConfig(), GlobalInstanceHolder.SYSTEM_ENTITLEMENT_MANAGER);

        //
        new ProxyConfigGetFormDataAcquisitor().handle(proxyConfigGetFormDataContext);

        Map<String, Object> actualProxyConfigAsMap = proxyConfigGetFormDataContext.getProxyConfigAsMap();
        assertEquals(SOURCE_MODE_RPM, actualProxyConfigAsMap.get(SOURCE_MODE_FIELD));
        assertFalse(actualProxyConfigAsMap.containsKey(REGISTRY_MODE));
        assertFalse(actualProxyConfigAsMap.containsKey(REGISTRY_BASE_URL));
        assertFalse(actualProxyConfigAsMap.containsKey(REGISTRY_BASE_TAG));

        Set<String> actualElectableParentsFqdn = new HashSet<>(proxyConfigGetFormDataContext.getElectableParentsFqdn());
        assertEquals(expectedElectableParentsFqdn.length, actualElectableParentsFqdn.size());
        assertTrue(actualElectableParentsFqdn.containsAll(Set.of(expectedElectableParentsFqdn)));
    }

}
