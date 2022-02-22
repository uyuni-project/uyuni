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

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.domain.server.ServerPath;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;

import com.suse.manager.webui.services.impl.SaltSSHService;

import org.jmock.imposters.ByteBuddyClassImposteriser;

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
    public void setUp() throws Exception {
        super.setUp();
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        Config.get().setString("ssh_push_port_https", "1233");
        Config.get().setString("ssh_push_sudo_user", "mgruser");
    }

    public void testProxyCommandNoProxy() {
        Optional<List<String>> res = SaltSSHService.sshProxyCommandOption(Collections.emptyList(), "ssh-push", "minion", 22);
        assertFalse(res.isPresent());
    }

    public void testProxyCommandSSHPush1Proxy() {
        Optional<List<String>> res = SaltSSHService.sshProxyCommandOption(List.of("proxy1"), "ssh-push", "minion", 22);
        assertTrue(res.isPresent());
        assertEquals(List.of(
                "StrictHostKeyChecking=no",
                "ProxyCommand='" +
                        "/usr/bin/ssh -i /srv/susemanager/salt/salt_ssh/mgr_ssh_id -o StrictHostKeyChecking=no " +
                        "-o User=mgrsshtunnel -W minion:22 proxy1 '"),
                res.get());
    }

    public void testProxyCommandSSHPushTunnel1Proxy() {
        Optional<List<String>> res = SaltSSHService.sshProxyCommandOption(List.of("proxy1"), "ssh-push-tunnel", "minion", 22);
        assertTrue(res.isPresent());
        assertEquals(List.of(
                "StrictHostKeyChecking=no",
                "ProxyCommand='" +
                        "/usr/bin/ssh -i /srv/susemanager/salt/salt_ssh/mgr_ssh_id -o StrictHostKeyChecking=no " +
                        "-o User=mgrsshtunnel -W minion:22 proxy1 '"),
                res.get());
    }

    public void testProxyCommandSSHPush2Proxies() {
        Optional<List<String>> res = SaltSSHService.sshProxyCommandOption(
                Arrays.asList("proxy1", "proxy2"), "ssh-push", "minion", 22);
        assertTrue(res.isPresent());
        assertEquals(List.of(
                "StrictHostKeyChecking=no",
                "ProxyCommand='" +
                        "/usr/bin/ssh -i /srv/susemanager/salt/salt_ssh/mgr_ssh_id -o StrictHostKeyChecking=no " +
                        "-o User=mgrsshtunnel proxy1 " +
                        "/usr/bin/ssh -i /var/lib/spacewalk/mgrsshtunnel/.ssh/id_susemanager_ssh_push " +
                        "-o StrictHostKeyChecking=no -o User=mgrsshtunnel -W minion:22 proxy2 '"),
                res.get());
    }

    public void testProxyCommandSSHPushTunnel2Proxies() {
        Optional<List<String>> res = SaltSSHService.sshProxyCommandOption(
                Arrays.asList("proxy1", "proxy2"), "ssh-push-tunnel", "minion", 22);
        assertTrue(res.isPresent());
        assertEquals(List.of(
                "StrictHostKeyChecking=no",
                "ProxyCommand='" +
                        "/usr/bin/ssh -i /srv/susemanager/salt/salt_ssh/mgr_ssh_id -o StrictHostKeyChecking=no " +
                        "-o User=mgrsshtunnel proxy1 " +
                        "/usr/bin/ssh -i /var/lib/spacewalk/mgrsshtunnel/.ssh/id_susemanager_ssh_push " +
                        "-o StrictHostKeyChecking=no -o User=mgrsshtunnel -W minion:22 proxy2 '"),
                res.get());
    }

    public void testProxyPathToHostnames() {
        final Set<ServerPath> serverPaths = Set.of(
                new ServerPath(1L, "rhn"),
                new ServerPath(0L, "junit"),
                new ServerPath(2L, "test")
        );

        assertEquals(List.of("test", "rhn", "junit", "unitTest"),
                SaltSSHService.proxyPathToHostnames(serverPaths, Optional.of("unitTest")));

        assertEquals(List.of("unitTest"),
                SaltSSHService.proxyPathToHostnames(Collections.emptySet(), Optional.of("unitTest")));

        assertEquals(List.of("test", "rhn", "junit"),
                SaltSSHService.proxyPathToHostnames(serverPaths, Optional.empty()));

        assertEquals(Collections.emptyList(),
                SaltSSHService.proxyPathToHostnames(Collections.emptySet(), Optional.empty()));
    }
}
