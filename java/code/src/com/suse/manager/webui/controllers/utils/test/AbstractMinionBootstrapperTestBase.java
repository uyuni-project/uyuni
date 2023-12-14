/*
 * Copyright (c) 2020 SUSE LLC
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

package com.suse.manager.webui.controllers.utils.test;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;

import com.suse.cloud.CloudPaygManager;
import com.suse.manager.webui.controllers.bootstrap.AbstractMinionBootstrapper;
import com.suse.manager.webui.controllers.bootstrap.BootstrapResult;
import com.suse.manager.webui.services.impl.SaltSSHService;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.services.impl.SaltService.KeyStatus;
import com.suse.manager.webui.services.impl.runner.MgrUtilRunner;
import com.suse.manager.webui.utils.gson.BootstrapHostsJson;
import com.suse.manager.webui.utils.gson.BootstrapParameters;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.salt.netapi.calls.wheel.Key;
import com.suse.salt.netapi.results.Result;
import com.suse.salt.netapi.results.SSHResult;
import com.suse.salt.netapi.utils.Xor;

import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Base for tests for bootstrapping minions.
 */
public abstract class AbstractMinionBootstrapperTestBase extends JMockBaseTestCaseWithUser {

    protected SaltService saltServiceMock;
    protected CloudPaygManager paygManager;

    // tested object, initialized in subclasses
    protected AbstractMinionBootstrapper bootstrapper;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        saltServiceMock = mock(SaltService.class);
        paygManager = new CloudPaygManager();
    }

    /**
     * Tests that the bootstrap fails when the keys for the host already exist.
     */
    @Test
    public void testBootstrapFailsWhenKeysExist() {
        BootstrapHostsJson input = mockStandardInput();
        setEmptyActivationKeys(input);

        context().checking(new Expectations() {{
            allowing(saltServiceMock).keyExists("myhost", KeyStatus.ACCEPTED, KeyStatus.DENIED, KeyStatus.REJECTED);
            will(returnValue(true));
        }});

        BootstrapParameters params = bootstrapper.createBootstrapParams(input);
        BootstrapResult bootstrap = bootstrapper.bootstrap(params, user, "default");
        assertFalse(bootstrap.isSuccess());
    }

    protected BootstrapHostsJson mockStandardInput() {
        BootstrapHostsJson input = mock(BootstrapHostsJson.class);
        context().checking(new Expectations() {{
            allowing(input).getHost();
            will(returnValue("myhost"));
            allowing(input).getPort();
            will(returnValue("6022"));
            allowing(input).getPortInteger();
            will(returnValue(Optional.of(6022)));
            allowing(input).getUser();
            will(returnValue("myuser"));
            allowing(input).maybeGetAuthMethod();
            will(returnValue(of(BootstrapHostsJson.AuthMethod.PASSWORD)));
            allowing(input).getPrivKey();
            will(returnValue(null));
            allowing(input).getPrivKeyPwd();
            will(returnValue(empty()));
            allowing(input).maybeGetPassword();
            will(returnValue(Optional.of("mypassword")));
            allowing(input).getIgnoreHostKeys();
            will(returnValue(true));
            allowing(input).getProxy();
            will(returnValue(null));
        }});
        return input;
    }

    protected void setEmptyActivationKeys(BootstrapHostsJson mock) {
        context().checking(new Expectations() {{
            allowing(mock).getActivationKeys();
            will(returnValue(Collections.emptyList()));
            allowing(mock).getFirstActivationKey();
            will(returnValue(empty()));
            allowing(mock).getProxy();
            will(returnValue(null));
            allowing(mock).maybeGetReactivationKey();
            will(returnValue(empty()));
        }});
    }

    /**
     * Tests that the bootstrap fails when the system for the host already exist.
     * @throws Exception if something goes wrong
     */
    @Test
    public void testBootstrapFailsWhenMinionExists()
            throws Exception {
        MinionServer server = MinionServerFactoryTest.createTestMinionServer(user);
        server.setMinionId("myhost");

        BootstrapHostsJson input = mockStandardInput();
        setEmptyActivationKeys(input);

        context().checking(new Expectations() {{
            allowing(saltServiceMock).keyExists("myhost", KeyStatus.ACCEPTED, KeyStatus.DENIED, KeyStatus.REJECTED);
            will(returnValue(false));
        }});

        BootstrapParameters params = bootstrapper.createBootstrapParams(input);
        BootstrapResult bootstrap = bootstrapper.bootstrap(params, user, getDefaultContactMethod());
        assertFalse(bootstrap.isSuccess());
    }

    /**
     * This test verifies the success-path of the bootstrap.
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testBootstrapSuccess() throws Exception {

        Key.Pair keyPair = mockKeyPair();

        context().checking(new Expectations() {{
            allowing(saltServiceMock).keyExists("myhost", KeyStatus.ACCEPTED, KeyStatus.DENIED, KeyStatus.REJECTED);
            will(returnValue(false));
            allowing(saltServiceMock).keyExists("myhost", KeyStatus.UNACCEPTED);
            will(returnValue(false));

            allowing(saltServiceMock).generateKeysAndAccept("myhost", false);
            will(returnValue(keyPair));

            MgrUtilRunner.ExecResult mockResult = new MgrUtilRunner.SshKeygenResult("key", "pubkey");
            allowing(saltServiceMock).generateSSHKey(SaltSSHService.SSH_KEY_PATH, SaltSSHService.SUMA_SSH_PUB_KEY);
            will(returnValue(of(mockResult)));

            List<String> bootstrapMods = bootstrapMods();
            Map<String, Object> pillarData = createPillarData(Optional.empty(), Optional.empty());
            // return success when calling low-level bootstrap
            allowing(saltServiceMock).bootstrapMinion(with(any(BootstrapParameters.class)),
                    with(bootstrapMods), with(pillarData));
            SSHResult<Map<String, State.ApplyResult>> sshResult = createSuccessResult();
            will(returnValue(new Result<>(Xor.right(sshResult))));

            // we expect the key NOT to be deleted
            atMost(0).of(saltServiceMock).deleteKey("myhost");
        }});

        BootstrapHostsJson input = mockStandardInput();
        setEmptyActivationKeys(input);
        BootstrapParameters params = bootstrapper.createBootstrapParams(input);
        BootstrapResult bootstrap = bootstrapper.bootstrap(params, user, getDefaultContactMethod());
        assertTrue(bootstrap.isSuccess());
    }

    /**
     * Base for tests that check that bootstrap FAILS with on current bootstrapper (set in
     * implementations of this base class) and given activation key.
     * @param key activation key
     */
    protected void testIncompatibleActivationKeysBase(ActivationKey key) {
        BootstrapHostsJson input = mockStandardInput();
        context().checking(new Expectations() {{
            allowing(input).getFirstActivationKey();
            will(returnValue(of(key.getKey())));
            allowing(input).getActivationKeys();
            will(returnValue(List.of(key.getKey())));
            allowing(input).maybeGetReactivationKey();
            will(returnValue(null));
        }});

        BootstrapParameters params = bootstrapper.createBootstrapParams(input);
        assertFalse(bootstrapper.bootstrap(params, user, getDefaultContactMethod()).isSuccess());
    }

    /**
     * Base for tests that check that bootstrap SUCCEEDS with on current bootstrapper (set
     * in implementations of this base class) and given activation key.
     * @param key activation key
     * @throws Exception if anything goes wrong
     */
    protected void testCompatibleActivationKeysBase(ActivationKey key) throws Exception {
        BootstrapHostsJson input = mockStandardInput();
        context().checking(new Expectations() {{
            allowing(input).getActivationKeys();
            will(returnValue(Collections.singletonList(key.getKey())));
            allowing(input).getFirstActivationKey();
            will(returnValue(of(key.getKey())));
            allowing(input).maybeGetReactivationKey();
            will(returnValue(empty()));

            allowing(saltServiceMock).keyExists("myhost", KeyStatus.ACCEPTED, KeyStatus.DENIED, KeyStatus.REJECTED);
            will(returnValue(false));
            allowing(saltServiceMock).keyExists("myhost", KeyStatus.UNACCEPTED);
            will(returnValue(false));

            Key.Pair keyPair = mockKeyPair();
            allowing(saltServiceMock).generateKeysAndAccept("myhost", false);
            will(returnValue(keyPair));

            MgrUtilRunner.ExecResult mockResult = new MgrUtilRunner.SshKeygenResult("key", "pubkey");
            allowing(saltServiceMock).generateSSHKey(SaltSSHService.SSH_KEY_PATH, SaltSSHService.SUMA_SSH_PUB_KEY);
            will(returnValue(of(mockResult)));

            List<String> bootstrapMods = bootstrapMods();
            Map<String, Object> pillarData = createPillarData(Optional.of(key), Optional.empty());
            allowing(saltServiceMock).bootstrapMinion(with(any(BootstrapParameters.class)),
                    with(bootstrapMods), with(pillarData));
            Object sshResult = createSuccessResult();
            will(returnValue(new Result<>(Xor.right(sshResult))));
        }});

        BootstrapParameters params = bootstrapper.createBootstrapParams(input);
        assertTrue(bootstrapper.bootstrap(params, user, getDefaultContactMethod()).isSuccess());
    }

    /**
     * Base for tests that check that bootstrap SUCCEEDS with on current bootstrapper (set
     * in implementations of this base class) and given activation key.
     * @param key activation key
     * @param reactKey an reactivation key
     * @throws Exception if anything goes wrong
     */
    protected void testCompatibleActivationKeysBase(ActivationKey key, ActivationKey reactKey) throws Exception {
        BootstrapHostsJson input = mockStandardInput();
        context().checking(new Expectations() {{
            allowing(input).getActivationKeys();
            will(returnValue(Collections.singletonList(key.getKey())));
            allowing(input).getFirstActivationKey();
            will(returnValue(of(key.getKey())));
            allowing(input).maybeGetReactivationKey();
            will(returnValue(Optional.of(reactKey.getKey())));

            allowing(saltServiceMock).keyExists("myhost", KeyStatus.ACCEPTED, KeyStatus.DENIED, KeyStatus.REJECTED);
            will(returnValue(false));
            allowing(saltServiceMock).keyExists("myhost", KeyStatus.UNACCEPTED);
            will(returnValue(false));

            Key.Pair keyPair = mockKeyPair();
            allowing(saltServiceMock).generateKeysAndAccept("myhost", false);
            will(returnValue(keyPair));

            List<String> bootstrapMods = bootstrapMods();
            Map<String, Object> pillarData = createPillarData(Optional.of(key), Optional.of(reactKey));
            allowing(saltServiceMock).bootstrapMinion(with(any(BootstrapParameters.class)),
                    with(bootstrapMods), with(pillarData));
            Object sshResult = createSuccessResult();
            will(returnValue(new Result<>(Xor.right(sshResult))));
        }});

        BootstrapParameters params = bootstrapper.createBootstrapParams(input);
        assertTrue(params.getReactivationKey().isPresent());
        assertEquals(params.getReactivationKey().get(), reactKey.getKey());
        assertTrue(bootstrapper.bootstrap(params, user, getDefaultContactMethod()).isSuccess());
    }

    protected abstract Map<String, Object> createPillarData(Optional<ActivationKey> key,
            Optional<ActivationKey> reactKey);

    protected abstract List<String> bootstrapMods();

    protected SSHResult<Map<String, State.ApplyResult>> createSuccessResult() {
        SSHResult<Map<String, State.ApplyResult>> sshResult = mock(SSHResult.class);
        context().checking(new Expectations() {{
            State.ApplyResult result = mock(State.ApplyResult.class);
            allowing(result).isResult();
            will(returnValue(true));

            Map<String, State.ApplyResult> innerResult = new HashMap<>();
            innerResult.put("myhost", result);
            allowing(sshResult).getReturn();
            will(returnValue(of(innerResult)));
            allowing(sshResult).getRetcode();
            will(returnValue(0));
        }});
        return sshResult;
    }

    protected Key.Pair mockKeyPair() {
        final Key.Pair keyPair = mock(Key.Pair.class);
        context().checking(new Expectations() {{
            allowing(keyPair).getPub();
            will(returnValue(of("pubKey")));
            allowing(keyPair).getPriv();
            will(returnValue(of("privKey")));
        }});
        return keyPair;
    }

    protected abstract String getDefaultContactMethod();

}
