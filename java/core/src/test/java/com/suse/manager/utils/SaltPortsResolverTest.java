/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ProxyInfo;
import com.redhat.rhn.domain.server.Server;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

public class SaltPortsResolverTest {

    @AfterEach
    public void tearDown() {
        Config.get().remove(ConfigDefaults.SALT_PUBLISH_PORT);
        Config.get().remove(ConfigDefaults.SALT_REQUEST_PORT);
    }

    @Test
    public void testServerDefaults() {
        assertTrue(SaltPortsResolver.forMaster(Optional.empty()).isEmpty());
    }

    @Test
    public void testServerCustomPorts() {
        Config.get().setString(ConfigDefaults.SALT_PUBLISH_PORT, "30005");
        Config.get().setString(ConfigDefaults.SALT_REQUEST_PORT, " 30006 ");

        assertEquals(Map.of(SaltPortsResolver.PILLAR_PUBLISH_PORT, 30005,
                SaltPortsResolver.PILLAR_REQUEST_PORT, 30006), SaltPortsResolver.forMaster(Optional.empty()));
    }

    @Test
    public void testDefaultValuesAreIgnored() {
        Config.get().setString(ConfigDefaults.SALT_PUBLISH_PORT, "4505");
        Config.get().setString(ConfigDefaults.SALT_REQUEST_PORT, "30006");

        assertEquals(Map.of(SaltPortsResolver.PILLAR_REQUEST_PORT, 30006),
                SaltPortsResolver.forMaster(Optional.empty()));
    }

    @Test
    public void testProxyPorts() {
        Config.get().setString(ConfigDefaults.SALT_PUBLISH_PORT, "30005");
        Config.get().setString(ConfigDefaults.SALT_REQUEST_PORT, "30006");

        assertEquals(Map.of(SaltPortsResolver.PILLAR_PUBLISH_PORT, 31005),
                SaltPortsResolver.forMaster(Optional.of(createProxy(31005, null))),
                "Server ports must not leak to the proxy");
    }

    private Server createProxy(Integer publishPort, Integer requestPort) {
        Server proxy = new MinionServer();
        ProxyInfo info = new ProxyInfo();
        info.setSaltPublishPort(publishPort);
        info.setSaltRequestPort(requestPort);
        proxy.setProxyInfo(info);
        return proxy;
    }
}
