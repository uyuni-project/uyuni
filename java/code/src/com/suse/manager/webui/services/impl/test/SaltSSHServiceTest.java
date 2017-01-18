package com.suse.manager.webui.services.impl.test;

import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.suse.manager.webui.services.impl.SaltSSHService;
import org.jmock.lib.legacy.ClassImposteriser;

import java.util.Arrays;
import java.util.Optional;

/**
 * Tests for SaltSSHService.
 */
public class SaltSSHServiceTest extends JMockBaseTestCaseWithUser {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setImposteriser(ClassImposteriser.INSTANCE);
    }

    public void testProxySSHOption() {
        Optional<String> res = SaltSSHService.sshProxyCommandOption(Arrays.asList("proxy1", "proxy2"), "ssh-push", "minion.local");
        assertTrue(res.isPresent());
        assertEquals(
                "ssh -i /srv/susemanager/salt/salt_ssh/id_susemanager_ssh_push proxy1 ssh -i /root/.ssh/id_susemanager_ssh_push proxy2  nc minion.local 22",
                res.get());

    }


}
