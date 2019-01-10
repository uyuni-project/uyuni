package com.suse.manager.webui.controllers.utils.test;

import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.suse.manager.webui.controllers.utils.AbstractMinionBootstrapper;
import com.suse.manager.webui.controllers.utils.AbstractMinionBootstrapper.BootstrapResult;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.services.impl.SaltService.KeyStatus;
import com.suse.manager.webui.utils.gson.BootstrapParameters;
import com.suse.manager.webui.utils.gson.BootstrapHostsJson;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.salt.netapi.calls.wheel.Key;
import com.suse.salt.netapi.results.Result;
import com.suse.salt.netapi.results.SSHResult;
import com.suse.salt.netapi.utils.Xor;
import org.jmock.Expectations;
import org.jmock.lib.legacy.ClassImposteriser;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;

/**
 * Base for tests for bootstrapping minions.
 */
public abstract class AbstractMinionBootstrapperTestBase extends JMockBaseTestCaseWithUser {

    protected SaltService saltServiceMock;

    // tested object, initialized in subclasses
    protected AbstractMinionBootstrapper bootstrapper;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setImposteriser(ClassImposteriser.INSTANCE);
        saltServiceMock = mock(SaltService.class);
    }

    /**
     * Tests that the bootstrap fails when the keys for the host already exist.
     * @throws Exception if something goes wrong
     */
    public void testBootstrapFailsWhenKeysExist() throws Exception {
        BootstrapHostsJson input = mockStandardInput();
        setEmptyActivationKeys(input);

        context().checking(new Expectations() {{
            allowing(saltServiceMock).keyExists("myhost", KeyStatus.ACCEPTED, KeyStatus.DENIED, KeyStatus.REJECTED);
            will(returnValue(true));
        }});

        BootstrapResult bootstrap = bootstrapper.bootstrap(input, user, "default");
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
            allowing(input).getPassword();
            will(returnValue("mypassword"));
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
        }});
    }

    /**
     * Tests that the bootstrap fails when the system for the host already exist.
     * @throws Exception if something goes wrong
     */
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

        BootstrapResult bootstrap = bootstrapper.bootstrap(input, user,
                getDefaultContactMethod());
        assertFalse(bootstrap.isSuccess());
    }

    /**
     * This test verifies the success-path of the bootstrap.
     *
     * @throws Exception if something goes wrong
     */
    public void testBootstrapSuccess() throws Exception {

        Key.Pair keyPair = mockKeyPair();

        context().checking(new Expectations() {{
            allowing(saltServiceMock).keyExists("myhost", KeyStatus.ACCEPTED, KeyStatus.DENIED, KeyStatus.REJECTED);
            will(returnValue(false));
            allowing(saltServiceMock).keyExists("myhost", KeyStatus.UNACCEPTED);
            will(returnValue(false));

            allowing(saltServiceMock).generateKeysAndAccept("myhost", false);
            will(returnValue(keyPair));

            List<String> bootstrapMods = bootstrapMods();
            Map<String, Object> pillarData = createPillarData(Optional.empty());
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
        BootstrapResult bootstrap = bootstrapper.bootstrap(input, user, getDefaultContactMethod());
        assertTrue(bootstrap.isSuccess());
    }

    /**
     * Base for tests that check that bootstrap FAILS with on current bootstrapper (set in
     * implementations of this base class) and given activation key.
     * @param key activation key
     * @throws Exception if anything goes wrong
     */
    protected void testIncompatibleActivationKeysBase(ActivationKey key) throws Exception {
        BootstrapHostsJson input = mockStandardInput();
        context().checking(new Expectations() {{
            allowing(input).getFirstActivationKey();
            will(returnValue(of(key.getKey())));
        }});

        assertFalse(bootstrapper.bootstrap(input, user, getDefaultContactMethod()).isSuccess());
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

            allowing(saltServiceMock).keyExists("myhost", KeyStatus.ACCEPTED, KeyStatus.DENIED, KeyStatus.REJECTED);
            will(returnValue(false));
            allowing(saltServiceMock).keyExists("myhost", KeyStatus.UNACCEPTED);
            will(returnValue(false));

            Key.Pair keyPair = mockKeyPair();
            allowing(saltServiceMock).generateKeysAndAccept("myhost", false);
            will(returnValue(keyPair));

            List<String> bootstrapMods = bootstrapMods();
            Map<String, Object> pillarData = createPillarData(Optional.of(key));
            allowing(saltServiceMock).bootstrapMinion(with(any(BootstrapParameters.class)),
                    with(bootstrapMods), with(pillarData));
            Object sshResult = createSuccessResult();
            will(returnValue(new Result<>(Xor.right(sshResult))));
        }});

        assertTrue(bootstrapper.bootstrap(input, user, getDefaultContactMethod()).isSuccess());
    }

    protected abstract Map<String, Object> createPillarData(Optional<ActivationKey> key);

    protected abstract List<String> bootstrapMods();

    protected SSHResult<Map<String, State.ApplyResult>> createSuccessResult() {
        SSHResult<Map<String, State.ApplyResult>> sshResult = mock(SSHResult.class);
        context().checking(new Expectations() {{
            State.ApplyResult result = mock(State.ApplyResult.class);
            allowing(result).isResult();
            will(returnValue(true));

            Map<String,State.ApplyResult> innerResult = new HashMap<>();
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
