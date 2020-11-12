package com.suse.manager.webui.services.impl.test;
import org.junit.jupiter.api.BeforeEach;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.suse.manager.webui.services.impl.SaltSSHService;

import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

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
        Optional<String> res = SaltSSHService.sshProxyCommandOption(Collections.emptyList(), "ssh-push", "minion");
        assertFalse(res.isPresent());
    }

    @Test
    public void testProxyCommandSSHPush1Proxy() {
        Optional<String> res = SaltSSHService.sshProxyCommandOption(Arrays.asList("proxy1"), "ssh-push", "minion");
        assertTrue(res.isPresent());
        assertEquals(
                "ProxyCommand='" +
                        "/usr/bin/ssh -i /srv/susemanager/salt/salt_ssh/mgr_ssh_id -o StrictHostKeyChecking=no -o User=mgrsshtunnel -W minion:22 proxy1 '",
                res.get());
    }

    @Test
    public void testProxyCommandSSHPushTunnel1Proxy() {
        Optional<String> res = SaltSSHService.sshProxyCommandOption(Arrays.asList("proxy1"), "ssh-push-tunnel", "minion");
        assertTrue(res.isPresent());
        assertEquals(
                "ProxyCommand='" +
                        "/usr/bin/ssh -i /srv/susemanager/salt/salt_ssh/mgr_ssh_id -o StrictHostKeyChecking=no -o User=mgrsshtunnel  proxy1 " +
                        "/usr/bin/ssh -i /var/lib/spacewalk/mgrsshtunnel/.ssh/id_susemanager_ssh_push -o StrictHostKeyChecking=no -o User=mgruser -R 1233:proxy1:443 minion " +
                        "ssh -i /home/mgruser/.ssh/mgr_own_id -W minion:22 -o StrictHostKeyChecking=no -o User=mgruser minion'",
                res.get());
    }

    @Test
    public void testProxyCommandSSHPush2Proxies() {
        Optional<String> res = SaltSSHService.sshProxyCommandOption(Arrays.asList("proxy1", "proxy2"), "ssh-push", "minion");
        assertTrue(res.isPresent());
        assertEquals(
                "ProxyCommand='" +
                        "/usr/bin/ssh -i /srv/susemanager/salt/salt_ssh/mgr_ssh_id -o StrictHostKeyChecking=no -o User=mgrsshtunnel  proxy1 " +
                        "/usr/bin/ssh -i /var/lib/spacewalk/mgrsshtunnel/.ssh/id_susemanager_ssh_push -o StrictHostKeyChecking=no -o User=mgrsshtunnel -W minion:22 proxy2 '",
                res.get());
    }

    @Test
    public void testProxyCommandSSHPushTunnel2Proxies() {
        Optional<String> res = SaltSSHService.sshProxyCommandOption(Arrays.asList("proxy1", "proxy2"), "ssh-push-tunnel", "minion");
        assertTrue(res.isPresent());
        assertEquals(
                "ProxyCommand='" +
                        "/usr/bin/ssh -i /srv/susemanager/salt/salt_ssh/mgr_ssh_id -o StrictHostKeyChecking=no -o User=mgrsshtunnel  proxy1 " +
                        "/usr/bin/ssh -i /var/lib/spacewalk/mgrsshtunnel/.ssh/id_susemanager_ssh_push -o StrictHostKeyChecking=no -o User=mgrsshtunnel  proxy2 " +
                        "/usr/bin/ssh -i /var/lib/spacewalk/mgrsshtunnel/.ssh/id_susemanager_ssh_push -o StrictHostKeyChecking=no -o User=mgruser -R 1233:proxy2:443 minion " +
                        "ssh -i /home/mgruser/.ssh/mgr_own_id -W minion:22 -o StrictHostKeyChecking=no -o User=mgruser minion'",
                res.get());
    }

}
