/*
 * Copyright (c) 2016--2021 SUSE LLC
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

import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;

import com.suse.manager.reactor.messaging.test.SaltTestUtils;
import com.suse.manager.webui.controllers.utils.ContactMethodUtil;
import com.suse.manager.webui.services.impl.MinionPendingRegistrationService;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.services.impl.runner.MgrUtilRunner;
import com.suse.salt.netapi.calls.Client;
import com.suse.salt.netapi.client.SaltClient;
import com.suse.salt.netapi.datatypes.AuthMethod;

import com.google.gson.reflect.TypeToken;

import org.apache.commons.io.FileUtils;
import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Tests for SaltService.
 */
public class SaltServiceTest extends JMockBaseTestCaseWithUser {

    private Path tempDir;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
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
        MinionPendingRegistrationService.addMinion(user, "m1", ContactMethodUtil.SSH_PUSH);
        MinionPendingRegistrationService.addMinion(user, "m2", ContactMethodUtil.DEFAULT);
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
        SaltClient saltClient = mock(SaltClient.class);
        context().checking(new Expectations() {{
            oneOf(saltClient).call(with(SaltTestUtils.functionEquals("mgrutil", "ssh_keygen")),
                    with(any(Client.class)), with(any(Optional.class)), with(any(Map.class)),
                    with(any(TypeToken.class)), with(any(AuthMethod.class)));
            will(returnValue(SaltTestUtils.getCompletionStage(
                    "/com/suse/manager/webui/services/impl/test/service/ssh_keygen.json",
                    new TypeToken<MgrUtilRunner.SshKeygenResult>() { }.getType())));
        }});

        SaltService systemQuery = new SaltService(saltClient);
        Optional<MgrUtilRunner.SshKeygenResult> res = systemQuery
                .generateSSHKey(keyPath.substring(0, keyPath.length() - 4));
        assertTrue(res.isPresent());
        assertEquals(0, res.orElseThrow().getReturnCode());
        systemQuery.close();
    }

    @Override
    public void tearDown() {
        try {
            if (tempDir.toFile().exists()) {
                FileUtils.deleteDirectory(tempDir.toFile());
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
