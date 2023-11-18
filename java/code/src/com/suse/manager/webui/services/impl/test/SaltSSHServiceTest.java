/*
 * Copyright (c) 2017--2021 SUSE LLC
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
package com.suse.manager.webui.services.impl.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.domain.server.ProxyInfo;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerPath;
import com.redhat.rhn.domain.server.ServerPathId;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.ServerTestUtils;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.webui.services.impl.SaltSSHService;

import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Tests for SaltSSHService.
 */
public class SaltSSHServiceTest extends JMockBaseTestCaseWithUser {

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        Config.get().setString("ssh_push_port_https", "1233");
        Config.get().setString("ssh_push_sudo_user", "mgruser");
    }

    @Test
    public void testProxyCommandNoProxy() {
        Optional<List<String>> res = SaltSSHService.sshProxyCommandOption(
                Collections.emptyList(), "ssh-push", "minion", 22);
        assertFalse(res.isPresent());
    }

    @Test
    public void testProxyCommandSSHPush1Proxy() {
        Optional<List<String>> res = SaltSSHService.sshProxyCommandOption(
                List.of("proxy1"), "ssh-push", "minion", 22);
        assertTrue(res.isPresent());
        assertEquals(List.of(
                "StrictHostKeyChecking=no",
                "ProxyCommand='" +
                        "/usr/bin/ssh -p 22 -i /var/lib/salt/.ssh/mgr_ssh_id -o StrictHostKeyChecking=no " +
                        "-o User=mgrsshtunnel -W minion:22 proxy1 '"),
                res.get());
    }

    @Test
    public void testProxyCommandSSHPushTunnel1Proxy() {
        Optional<List<String>> res = SaltSSHService.sshProxyCommandOption(
                List.of("proxy1:24"), "ssh-push-tunnel", "minion", 22);
        assertTrue(res.isPresent());
        assertEquals(List.of(
                "StrictHostKeyChecking=no",
                "ProxyCommand='" +
                        "/usr/bin/ssh -p 24 -i /var/lib/salt/.ssh/mgr_ssh_id -o StrictHostKeyChecking=no " +
                        "-o User=mgrsshtunnel -W minion:22 proxy1 '"),
                res.get());
    }

    @Test
    public void testProxyCommandSSHPush2Proxies() {
        Optional<List<String>> res = SaltSSHService.sshProxyCommandOption(
                Arrays.asList("proxy1:23", "proxy2"), "ssh-push", "minion", 22);
        assertTrue(res.isPresent());
        assertEquals(List.of(
                "StrictHostKeyChecking=no",
                "ProxyCommand='" +
                        "/usr/bin/ssh -p 23 -i /var/lib/salt/.ssh/mgr_ssh_id -o StrictHostKeyChecking=no " +
                        "-o User=mgrsshtunnel proxy1 " +
                        "/usr/bin/ssh -p 22 -i /var/lib/spacewalk/mgrsshtunnel/.ssh/id_susemanager_ssh_push " +
                        "-o StrictHostKeyChecking=no -o User=mgrsshtunnel -W minion:22 proxy2 '"),
                res.get());
    }

    @Test
    public void testProxyCommandSSHPushTunnel2Proxies() {
        Optional<List<String>> res = SaltSSHService.sshProxyCommandOption(
                Arrays.asList("proxy1", "proxy2"), "ssh-push-tunnel", "minion", 22);
        assertTrue(res.isPresent());
        assertEquals(List.of(
                "StrictHostKeyChecking=no",
                "ProxyCommand='" +
                        "/usr/bin/ssh -p 22 -i /var/lib/salt/.ssh/mgr_ssh_id -o StrictHostKeyChecking=no " +
                        "-o User=mgrsshtunnel proxy1 " +
                        "/usr/bin/ssh -p 22 -i /var/lib/spacewalk/mgrsshtunnel/.ssh/id_susemanager_ssh_push " +
                        "-o StrictHostKeyChecking=no -o User=mgrsshtunnel -W minion:22 proxy2 '"),
                res.get());
    }

    private Server createTestProxyMinimal(String hostname, Integer sshPort) throws Exception {
        Server srv = ServerTestUtils.createTestSystem();
        srv.setHostname(hostname);
        ProxyInfo info = new ProxyInfo();
        info.setServer(srv);
        info.setSshPort(sshPort);
        srv.setProxyInfo(info);
        ServerFactory.save(srv);
        srv = TestUtils.reload(srv);
        return srv;
    }

    @Test
    public void testProxyPathToHostnames() throws Exception {
        Server proxy0 = createTestProxyMinimal("unitTest", 8022);
        Server proxy1 = createTestProxyMinimal("junit", 23);
        Server proxy2 = createTestProxyMinimal("rhn", 24);
        Server proxy3 = createTestProxyMinimal("test", null);

        final Set<ServerPath> serverPaths = Set.of(
                new ServerPath(new ServerPathId(proxy1, proxy2), 1L, "rhn"),
                new ServerPath(new ServerPathId(proxy0, proxy1), 0L, "junit"),
                new ServerPath(new ServerPathId(proxy2, proxy3), 2L, "test")
        );
        proxy0.setServerPaths(serverPaths);

        assertEquals(List.of("test", "rhn:24", "junit:23", "unitTest:8022"),
                SaltSSHService.proxyPathToHostnames(proxy0));

        assertEquals(List.of("unitTest:8022"),
                SaltSSHService.proxyPathToHostnames(Collections.emptySet(), Optional.of("unitTest:8022")));

        assertEquals(List.of("test", "rhn:24", "junit:23"),
                SaltSSHService.proxyPathToHostnames(serverPaths, Optional.empty()));

        assertEquals(Collections.emptyList(),
                SaltSSHService.proxyPathToHostnames(Collections.emptySet(), Optional.empty()));
    }
}
