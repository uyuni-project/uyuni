package com.suse.manager.webui.services.impl.test;

import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.suse.manager.webui.services.impl.SSHMinionsPendingRegistrationService;
import com.suse.manager.webui.services.impl.SaltService;
import org.jmock.lib.legacy.ClassImposteriser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Tests for SaltService.
 */
public class SaltServiceTest extends JMockBaseTestCaseWithUser {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setImposteriser(ClassImposteriser.INSTANCE);
    }

    public void testfilterSSHMinionIdsNoSSHMinions() {
        List<String> minionIds = new ArrayList<>();
        minionIds.add("m1");
        minionIds.add("m2");
        assertEquals(Collections.emptySet(), SaltService.filterSSHMinionIds(minionIds));
    }

    public void testfilterSSHMinionIdsBootstrap() {
        SSHMinionsPendingRegistrationService.addMinion("m1");
        List<String> minionIds = new ArrayList<>();
        minionIds.add("m1");
        minionIds.add("m2");
        assertEquals(Collections.singleton("m1"),
                SaltService.filterSSHMinionIds(minionIds));
        SSHMinionsPendingRegistrationService.removeMinion("m1");
    }

    public void testfilterSSHMinionIds() throws Exception {
        MinionServer sshMinion = MinionServerFactoryTest.createTestMinionServer(user);
        sshMinion.setContactMethod(ServerFactory.findContactMethodByLabel("ssh-push"));

        List<String> minionIds = new ArrayList<>();
        minionIds.add(sshMinion.getMinionId());
        minionIds.add("m2");
        assertEquals(Collections.singleton(sshMinion.getMinionId()),
                SaltService.filterSSHMinionIds(minionIds));
    }

    public void testfilterSSHMinionIdsMixedMinions() throws Exception {
        MinionServer sshMinion = MinionServerFactoryTest.createTestMinionServer(user);
        sshMinion.setContactMethod(ServerFactory.findContactMethodByLabel("ssh-push"));

        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);

        List<String> minionIds = new ArrayList<>();
        minionIds.add(sshMinion.getMinionId());
        minionIds.add(minion.getMinionId());
        assertEquals(Collections.singleton(sshMinion.getMinionId()),
                SaltService.filterSSHMinionIds(minionIds));
    }
}
