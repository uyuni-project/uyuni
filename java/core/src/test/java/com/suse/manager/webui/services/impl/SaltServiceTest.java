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
package com.suse.manager.webui.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactoryTest;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;

import com.suse.manager.reactor.messaging.SaltTestUtils;
import com.suse.manager.ssl.SSLCertPair;
import com.suse.manager.webui.controllers.utils.ContactMethodUtil;
import com.suse.manager.webui.services.impl.runner.MgrUtilRunner;
import com.suse.salt.netapi.calls.Call;
import com.suse.salt.netapi.calls.Client;
import com.suse.salt.netapi.calls.RunnerCall;
import com.suse.salt.netapi.client.SaltClient;
import com.suse.salt.netapi.datatypes.AuthMethod;
import com.suse.salt.netapi.errors.JsonParsingError;

import com.google.gson.reflect.TypeToken;

import org.apache.commons.io.FileUtils;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Tests for SaltService.
 */
public class SaltServiceTest extends JMockBaseTestCaseWithUser {

    private Path tempDir;

    @BeforeEach
    public void setUp() throws Exception {
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        tempDir = Files.createTempDirectory("saltservice");
    }

    @Test
    public void testfilterSSHMinionIdsNoSSHMinions() {
        List<String> minionIds = new ArrayList<>();
        minionIds.add("m1");
        minionIds.add("m2");
        assertEquals(
                Collections.emptyList(),
                SaltService.partitionMinionsByContactMethod(minionIds).get(true));
    }

    @Test
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

    @Test
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

    @Test
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

    @Test
    public void testGenerateSSHKeyExists() throws IOException {
        Path keyFile = Files.createFile(tempDir.resolve("mgr_ssh_id.pub"));
        String keyPath = keyFile.toFile().getCanonicalPath();
        SaltClient saltClient = mock(SaltClient.class);
        context().checking(new Expectations() {{
            oneOf(saltClient).call(with(SaltTestUtils.functionEquals("mgrutil", "ssh_keygen")),
                    with(any(Client.class)), with(any(Optional.class)), with(any(Map.class)),
                    with(any(TypeToken.class)), with(any(AuthMethod.class)));
            will(returnValue(SaltTestUtils.getCompletionStage(
                    "/com/suse/manager/webui/services/impl/service/ssh_keygen.json",
                    new TypeToken<MgrUtilRunner.SshKeygenResult>() { }.getType())));
        }});

        SaltService systemQuery = new SaltService(saltClient);
        Optional<MgrUtilRunner.SshKeygenResult> res = systemQuery
                .generateSSHKey(keyPath.substring(0, keyPath.length() - 4), null);
        assertTrue(res.isPresent());
        assertEquals(0, res.orElseThrow().getReturnCode());
        systemQuery.close();
    }

    @Test
    public void testLogEntryWithSensitiveData() {

        var saltClient = mock(SaltClient.class);
        var saltService = new SaltService(saltClient);
        var listAppender = SaltTestUtils.enableTestLogging(SaltService.class);

        context().checking(new Expectations() {{
            oneOf(saltClient).call(
                    with(SaltTestUtils.functionEquals("mgrutil", "check_ssl_cert")),
                    with(any(Client.class)),
                    with(any(Optional.class)),
                    with(any(Map.class)),
                    with(any(TypeToken.class)),
                    with(any(AuthMethod.class))
            );
            will(returnValue(SaltTestUtils.getCompletionStage(
                    "/com/suse/manager/webui/services/impl/service/ssh_cert_check.json",
                    new TypeToken<JsonParsingError>() {
                    }.getType())));
        }});

        var dummyRootCA = "dummyRootCA";
        var dummyServerCA = "dummyServerCA";
        var dummyServerRSAKey = "dummyServerRSAKey";

        var call = MgrUtilRunner.checkSSLCert(
                "Certificate:\ncert\n-----BEGIN CERTIFICATE-----\n" +
                        dummyRootCA +
                        "\n-----END CERTIFICATE-----",
                new SSLCertPair(
                        "-----BEGIN CERTIFICATE-----\n" +
                                dummyServerCA +
                                "\n-----END CERTIFICATE-----",
                        "-----BEGIN RSA PRIVATE KEY-----\n" +
                                dummyServerRSAKey +
                                "\n-----END RSA PRIVATE KEY-----"
                ),
                null
        );
        var result = saltService.callSync(call);

        assertEquals(result, Optional.empty());
        assertFalse(listAppender.getLog().isEmpty());

        assertTrue(listAppender.matchInLogs("root_ca=HIDDEN"));
        assertTrue(listAppender.matchInLogs("server_crt=HIDDEN"));
        assertTrue(listAppender.matchInLogs("server_key=HIDDEN"));

        assertFalse(listAppender.matchInLogs(dummyRootCA));
        assertFalse(listAppender.matchInLogs(dummyServerCA));
        assertFalse(listAppender.matchInLogs(dummyServerRSAKey));
    }

    /**
     * Tests that the Salt SSH known_hosts entries are updated when the primary FQDN of a
     * system changes, using the given previous FQDN name and the default ssh push port.
     */
    @Test
    public void testUpdateKnownHostsOnPrimaryFqdnChange() {
        MinionServer minion = mock(MinionServer.class);
        context().checking(new Expectations() {{
            allowing(minion).getPrimaryFqdnName();
            will(returnValue("new.example.com"));
            allowing(minion).getMinionId();
            will(returnValue("192.168.100.4"));
            allowing(minion).getSSHPushPort();
            will(returnValue(null));
            allowing(minion).getId();
            will(returnValue(1000010003L));
        }});
        SaltClient saltClient = mock(SaltClient.class);
        context().checking(new Expectations() {{
            oneOf(saltClient).call(with(updateSSHKnownHostCall("salt", "old.example.com",
                            "new.example.com", SaltSSHService.SSH_DEFAULT_PORT)),
                    with(any(Client.class)), with(any(Optional.class)), with(any(Map.class)),
                    with(any(TypeToken.class)), with(any(AuthMethod.class)));
            will(returnValue(SaltTestUtils.getCompletionStage(
                    "/com/suse/manager/webui/services/impl/service/update_known_host.json",
                    new TypeToken<MgrUtilRunner.UpdateKnowHostResult>() { }.getType())));
        }});

        SaltService saltService = new SaltService(saltClient);
        saltService.updateKnownHostsOnPrimaryFqdnChange(minion, "old.example.com");
        saltService.close();
    }

    /**
     * Tests that the minion id is used as old hostname when no previous FQDN name is known,
     * and that the ssh push port of the system is used.
     */
    @Test
    public void testUpdateKnownHostsOnPrimaryFqdnChangeFallbackToMinionId() {
        MinionServer minion = mock(MinionServer.class);
        context().checking(new Expectations() {{
            allowing(minion).getPrimaryFqdnName();
            will(returnValue("new.example.com"));
            allowing(minion).getMinionId();
            will(returnValue("192.168.100.4"));
            allowing(minion).getSSHPushPort();
            will(returnValue(1233));
            allowing(minion).getId();
            will(returnValue(1000010003L));
        }});
        SaltClient saltClient = mock(SaltClient.class);
        context().checking(new Expectations() {{
            oneOf(saltClient).call(with(updateSSHKnownHostCall("salt", "192.168.100.4",
                            "new.example.com", 1233)),
                    with(any(Client.class)), with(any(Optional.class)), with(any(Map.class)),
                    with(any(TypeToken.class)), with(any(AuthMethod.class)));
            will(returnValue(SaltTestUtils.getCompletionStage(
                    "/com/suse/manager/webui/services/impl/service/update_known_host.json",
                    new TypeToken<MgrUtilRunner.UpdateKnowHostResult>() { }.getType())));
        }});

        SaltService saltService = new SaltService(saltClient);
        saltService.updateKnownHostsOnPrimaryFqdnChange(minion, null);
        saltService.close();
    }

    /**
     * Tests that no salt call is performed when the primary FQDN name did not change.
     */
    @Test
    public void testUpdateKnownHostsOnPrimaryFqdnChangeNoOp() {
        MinionServer minion = mock(MinionServer.class);
        context().checking(new Expectations() {{
            allowing(minion).getPrimaryFqdnName();
            will(returnValue("same.example.com"));
            allowing(minion).getMinionId();
            will(returnValue("192.168.100.4"));
            allowing(minion).getSSHPushPort();
            will(returnValue(null));
            allowing(minion).getId();
            will(returnValue(1000010003L));
        }});
        SaltClient saltClient = mock(SaltClient.class);
        context().checking(new Expectations() {{
            never(saltClient).call(with(any(Call.class)), with(any(Client.class)),
                    with(any(Optional.class)), with(any(Map.class)),
                    with(any(TypeToken.class)), with(any(AuthMethod.class)));
        }});

        SaltService saltService = new SaltService(saltClient);
        saltService.updateKnownHostsOnPrimaryFqdnChange(minion, "same.example.com");
        saltService.close();
    }

    /**
     * Tests that a failed known_hosts update (the runner reports an error) is logged and
     * does not propagate.
     */
    @Test
    public void testUpdateKnownHostsOnPrimaryFqdnChangeRunnerFailure() {
        MinionServer minion = mock(MinionServer.class);
        context().checking(new Expectations() {{
            allowing(minion).getPrimaryFqdnName();
            will(returnValue("new.example.com"));
            allowing(minion).getMinionId();
            will(returnValue("192.168.100.4"));
            allowing(minion).getSSHPushPort();
            will(returnValue(null));
            allowing(minion).getId();
            will(returnValue(1000010003L));
        }});
        SaltClient saltClient = mock(SaltClient.class);
        context().checking(new Expectations() {{
            oneOf(saltClient).call(with(updateSSHKnownHostCall("salt", "old.example.com",
                            "new.example.com", SaltSSHService.SSH_DEFAULT_PORT)),
                    with(any(Client.class)), with(any(Optional.class)), with(any(Map.class)),
                    with(any(TypeToken.class)), with(any(AuthMethod.class)));
            will(returnValue(SaltTestUtils.getCompletionStage(
                    "/com/suse/manager/webui/services/impl/service/update_known_host_error.json",
                    new TypeToken<MgrUtilRunner.UpdateKnowHostResult>() { }.getType())));
        }});

        SaltTestUtils.TestLogAppender appender = SaltTestUtils.enableTestLogging(SaltService.class);
        SaltService saltService = new SaltService(saltClient);
        saltService.updateKnownHostsOnPrimaryFqdnChange(minion, "old.example.com");
        saltService.close();
        assertTrue(appender.matchInLogs("Failed to update Salt SSH known_hosts entries"));
    }

    /**
     * Tests that an exception during the known_hosts update is logged and does not
     * propagate.
     */
    @Test
    public void testUpdateKnownHostsOnPrimaryFqdnChangeException() {
        MinionServer minion = mock(MinionServer.class);
        context().checking(new Expectations() {{
            allowing(minion).getPrimaryFqdnName();
            will(returnValue("new.example.com"));
            allowing(minion).getMinionId();
            will(returnValue("192.168.100.4"));
            allowing(minion).getSSHPushPort();
            will(returnValue(null));
            allowing(minion).getId();
            will(returnValue(1000010003L));
        }});
        SaltClient saltClient = mock(SaltClient.class);
        context().checking(new Expectations() {{
            oneOf(saltClient).call(with(updateSSHKnownHostCall("salt", "old.example.com",
                            "new.example.com", SaltSSHService.SSH_DEFAULT_PORT)),
                    with(any(Client.class)), with(any(Optional.class)), with(any(Map.class)),
                    with(any(TypeToken.class)), with(any(AuthMethod.class)));
            will(throwException(new RuntimeException("connection refused")));
        }});

        SaltTestUtils.TestLogAppender appender = SaltTestUtils.enableTestLogging(SaltService.class);
        SaltService saltService = new SaltService(saltClient);
        saltService.updateKnownHostsOnPrimaryFqdnChange(minion, "old.example.com");
        saltService.close();
        assertTrue(appender.matchInLogs("Error updating Salt SSH known_hosts entries"));
    }

    /**
     * Matcher for a <code>mgrutil.update_ssh_known_host</code> runner call with the given
     * parameters.
     * @param user the user owning the known_hosts file
     * @param oldHostname the hostname to rename
     * @param newHostname the new hostname
     * @param port the ssh port
     * @return the matcher for the expectations
     */
    private static Matcher<Call<MgrUtilRunner.UpdateKnowHostResult>> updateSSHKnownHostCall(
            String user, String oldHostname, String newHostname, int port) {
        Map<String, Object> expectedKwargs = new LinkedHashMap<>();
        expectedKwargs.put("user", user);
        expectedKwargs.put("old_hostname", oldHostname);
        expectedKwargs.put("new_hostname", newHostname);
        expectedKwargs.put("port", port);
        return new TypeSafeMatcher<>() {

            @Override
            protected boolean matchesSafely(Call<MgrUtilRunner.UpdateKnowHostResult> item) {
                if (!(item instanceof RunnerCall)) {
                    return false;
                }
                Map<String, Object> payload =
                        ((RunnerCall<MgrUtilRunner.UpdateKnowHostResult>) item).getPayload();
                return "mgrutil.update_ssh_known_host".equals(payload.get("fun")) &&
                        expectedKwargs.equals(payload.get("kwarg"));
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("mgrutil.update_ssh_known_host runner call with kwargs ")
                        .appendValue(expectedKwargs);
            }
        };
    }

        @AfterEach
    public void tearDown() throws Exception {
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
