package com.suse.manager.webui.controllers.utils.test;

import com.google.gson.JsonPrimitive;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.token.test.ActivationKeyTest;
import com.suse.manager.webui.controllers.utils.RegularMinionBootstrapper;
import com.suse.manager.webui.utils.gson.BootstrapParameters;
import com.suse.manager.webui.utils.gson.JSONBootstrapHosts;
import com.suse.salt.netapi.calls.wheel.Key;
import com.suse.salt.netapi.errors.GenericSaltError;
import com.suse.salt.netapi.errors.SaltError;
import com.suse.salt.netapi.results.Result;
import com.suse.salt.netapi.utils.Xor;
import org.jmock.Expectations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tests for bootstrapping regular minions.
 */
public class RegularMinionBootstrapperTest extends AbstractMinionBootstrapperTestBase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        bootstrapper = new RegularMinionBootstrapper(saltServiceMock);
    }

    /**
     * This test verifies that bootstrap fails and keys are deleted when the low-level
     * method that daes bootstrapping fails.
     *
     * @throws Exception if something goes wrong
     */
    public void testKeysDeletedAfterFailure() throws Exception {
        JSONBootstrapHosts input = mockStandardInput();
        setEmptyActivationKeys(input);

        Key.Pair keyPair = mockKeyPair();

        context().checking(new Expectations() {{
            allowing(saltServiceMock).keyExists("myhost");
            will(returnValue(false));

            allowing(saltServiceMock).generateKeysAndAccept("myhost", false);
            will(returnValue(keyPair));

            List<String> bootstrapMods = bootstrapMods();
            Map<String, Object> pillarData = createPillarData();

            // return failure when calling low-level bootstrap
            allowing(saltServiceMock).bootstrapMinion(with(any(BootstrapParameters.class)),
                    with(bootstrapMods), with(pillarData));
            will(returnValue(new Result<>(Xor.left(createGenericSaltError()))));

            // we expect the key to be deleted
            exactly(1).of(saltServiceMock).deleteKey("myhost");
        }});

        Map<String, Object> bootstrap = bootstrapper.bootstrap(input, user);
        assertFalse((Boolean) bootstrap.get("success"));
    }

    public void testIncompatibleActivatioKeys() throws Exception {
        ActivationKey key = ActivationKeyTest.createTestActivationKey(user);
        key.setContactMethod(ServerFactory.findContactMethodByLabel("ssh-push"));
        super.testIncompatibleActivatioKeysBase(key);
    }

    public void testIncompatibleActivatioKeysTunnel() throws Exception {
        ActivationKey key = ActivationKeyTest.createTestActivationKey(user);
        key.setContactMethod(ServerFactory.findContactMethodByLabel("ssh-push-tunnel"));
        super.testIncompatibleActivatioKeysBase(key);
    }

    public void testCompatibleActivatioKeys() throws Exception {
        ActivationKey key = ActivationKeyTest.createTestActivationKey(user);
        key.setContactMethod(ServerFactory.findContactMethodByLabel("default"));
        super.testCompatibleActivatioKeysBase(key);
    }

    @Override
    protected List<String> bootstrapMods() {
        return Arrays.asList("certs", "bootstrap");
    }

    @Override
    protected Map<String, Object> createPillarData() {
        Map<String, Object> pillarData = new HashMap<>();
        pillarData.put("master", ConfigDefaults.get().getCobblerHost());
        pillarData.put("minion_id", "myhost");
        pillarData.put("minion_pub", "pubKey");
        pillarData.put("minion_pem", "privKey");
        return pillarData;
    }

    private SaltError createGenericSaltError() {
        return new GenericSaltError(
                new JsonPrimitive("salt error."),
                new Exception("exception causing the error."));
    }
}
