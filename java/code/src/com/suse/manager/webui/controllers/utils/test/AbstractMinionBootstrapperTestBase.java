package com.suse.manager.webui.controllers.utils.test;

import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.suse.manager.webui.controllers.utils.AbstractMinionBootstrapper;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.utils.gson.BootstrapParameters;
import com.suse.manager.webui.utils.gson.JSONBootstrapHosts;
import com.suse.manager.webui.utils.salt.Key;
import com.suse.salt.netapi.calls.modules.State;
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
        JSONBootstrapHosts input = mockStandardInput();

        context().checking(new Expectations() {{
            allowing(saltServiceMock).keyExists("myhost");
            will(returnValue(true));
        }});

        Map<String, Object> bootstrap = bootstrapper.bootstrap(input, user);
        assertFalse((Boolean) bootstrap.get("success"));
    }

    protected JSONBootstrapHosts mockStandardInput() {
        JSONBootstrapHosts input = mock(JSONBootstrapHosts.class);
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
            allowing(input).getActivationKeys();
            will(returnValue(Collections.emptyList()));
            allowing(input).getIgnoreHostKeys();
            will(returnValue(true));
        }});
        return input;
    }

    /**
     * Tests that the bootstrap fails when the system for the host already exist.
     * @throws Exception if something goes wrong
     */
    public void testBootstrapFailsWhenMinionExists()
            throws Exception {
        MinionServer server = MinionServerFactoryTest.createTestMinionServer(user);
        server.setMinionId("myhost");

        JSONBootstrapHosts input = mockStandardInput();

        context().checking(new Expectations() {{
            allowing(saltServiceMock).keyExists("myhost");
            will(returnValue(false));
        }});

        Map<String, Object> bootstrap = bootstrapper.bootstrap(input, user);
        assertFalse((Boolean) bootstrap.get("success"));
    }

    /**
     * This test verifies the success-path of the bootstrap.
     *
     * @throws Exception if something goes wrong
     */
    public void testBootstrapSuccess() throws Exception {

        Key.Pair keyPair = mockKeyPair();

        context().checking(new Expectations() {{
            allowing(saltServiceMock).keyExists("myhost");
            will(returnValue(false));

            allowing(saltServiceMock).generateKeysAndAccept("myhost", false);
            will(returnValue(keyPair));

            List<String> bootstrapMods = bootstrapMods();
            Map<String, Object> pillarData = createPillarData();

            Map<String,State.ApplyResult> innerResult = new HashMap<>();
            State.ApplyResult result = mock(State.ApplyResult.class);
            allowing(result).isResult();
            will(returnValue(true));

            innerResult.put("myhost", result);
            SSHResult<Map<String,State.ApplyResult>> sshResult = mock(SSHResult.class);
            allowing(sshResult).getReturn();
            will(returnValue(of(innerResult)));
            allowing(sshResult).getRetcode();
            will(returnValue(0));

            // return success when calling low-level bootstrap
            allowing(saltServiceMock).bootstrapMinion(with(any(BootstrapParameters.class)),
                    with(bootstrapMods), with(pillarData));
            will(returnValue(new Result<>(Xor.right(sshResult))));

            // we expect the key NOT to be deleted
            atMost(0).of(saltServiceMock).deleteKey("myhost");
        }});

        JSONBootstrapHosts input = mockStandardInput();
        Map<String, Object> bootstrap = bootstrapper.bootstrap(input, user);
        assertTrue((Boolean) bootstrap.get("success"));
    }

    protected abstract Map<String, Object> createPillarData();

    protected abstract List<String> bootstrapMods();

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
}
