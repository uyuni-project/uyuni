package com.suse.manager.webui.services.impl.test;

import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;

import com.suse.manager.webui.controllers.utils.ContactMethodUtil;
import com.suse.manager.webui.services.impl.MinionPendingRegistrationService;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.services.impl.runner.MgrUtilRunner;
import org.jmock.lib.legacy.ClassImposteriser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Tests for SaltService.
 */
public class SaltServiceTest extends JMockBaseTestCaseWithUser {

    private Path tempDir;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setImposteriser(ClassImposteriser.INSTANCE);
        tempDir = Files.createTempDirectory("saltservice");
    }

    public void testfilterSSHMinionIdsNoSSHMinions() {
        List<String> minionIds = new ArrayList<>();
        minionIds.add("m1");
        minionIds.add("m2");
        assertEquals(
                Collections.emptyList(),
                SaltService.partitionMinionsByContactMethod(minionIds).get(true));
    }

    public void testfilterSSHMinionIdsBootstrap() {
        MinionPendingRegistrationService.addMinion(user, "m1", ContactMethodUtil.SSH_PUSH, Optional.empty());
        MinionPendingRegistrationService.addMinion(user, "m2", ContactMethodUtil.DEFAULT, Optional.empty());
        List<String> minionIds = new ArrayList<>();
        minionIds.add("m1");
        minionIds.add("m2");
        minionIds.add("m3");
        assertEquals(
                Collections.singletonList("m1"),
                SaltService.partitionMinionsByContactMethod(minionIds).get(true));
        MinionPendingRegistrationService.removeMinion("m1");
        MinionPendingRegistrationService.removeMinion("m2");
    }

    public void testfilterSSHMinionIds() throws Exception {
        MinionServer sshMinion = MinionServerFactoryTest.createTestMinionServer(user);
        sshMinion.setContactMethod(ServerFactory.findContactMethodByLabel(ContactMethodUtil.SSH_PUSH));

        List<String> minionIds = new ArrayList<>();
        minionIds.add(sshMinion.getMinionId());
        minionIds.add("m2");
        assertEquals(
                Collections.singletonList(sshMinion.getMinionId()),
                SaltService.partitionMinionsByContactMethod(minionIds).get(true));
    }

    public void testfilterSSHMinionIdsMixedMinions() throws Exception {
        MinionServer sshMinion = MinionServerFactoryTest.createTestMinionServer(user);
        sshMinion.setContactMethod(ServerFactory.findContactMethodByLabel(ContactMethodUtil.SSH_PUSH));

        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);

        List<String> minionIds = new ArrayList<>();
        minionIds.add(sshMinion.getMinionId());
        minionIds.add(minion.getMinionId());
        assertEquals(
                Collections.singletonList(sshMinion.getMinionId()),
                SaltService.partitionMinionsByContactMethod(minionIds).get(true));
    }

    public void testGenerateSSHKeyExists() throws IOException {
        Path keyFile = Files.createFile(tempDir.resolve("mgr_ssh_id.pub"));
        String keyPath = keyFile.toFile().getCanonicalPath();
        Optional<MgrUtilRunner.ExecResult> res = SaltService.INSTANCE
                .generateSSHKey(keyPath.substring(0, keyPath.length() - 4));
        assertTrue(res.isPresent());
        assertEquals(0, res.get().getReturnCode());
    }

    @Override
    public void tearDown() {
        try {
            Files.deleteIfExists(tempDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
