package com.suse.manager.webui.controllers.utils.test;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.token.test.ActivationKeyTest;
import com.suse.manager.reactor.messaging.RegisterMinionEventMessageAction;
import com.suse.manager.webui.controllers.utils.ContactMethodUtil;
import com.suse.manager.webui.controllers.utils.SSHMinionBootstrapper;
import org.jmock.Expectations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Tests for bootstrapping salt-ssh minions.
 */
public class SSHMinionBootstrapperTest extends AbstractMinionBootstrapperTestBase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        bootstrapper = new SSHMinionBootstrapper(saltServiceMock);
    }

    @Override
    public void testBootstrapSuccess() throws Exception {
        // override the bootstrapper
        bootstrapper = mockRegistrationBootstrapper(Optional.empty());
        super.testBootstrapSuccess();
    }

    private SSHMinionBootstrapper mockRegistrationBootstrapper(
            Optional<String> activationKeyLabel) {
        return new SSHMinionBootstrapper(saltServiceMock) {
            @Override
            protected RegisterMinionEventMessageAction getRegisterAction() {
                RegisterMinionEventMessageAction action =
                        mock(RegisterMinionEventMessageAction.class);
                context().checking(new Expectations() {{
                    allowing(action).registerSSHMinion("myhost", Optional.empty(), activationKeyLabel);
                }});
                return action;
            }
        };
    }

    public void testIncompatibleActivationKeys() throws Exception {
        ActivationKey key = ActivationKeyTest.createTestActivationKey(user);
        key.setContactMethod(ServerFactory.findContactMethodByLabel("default"));
        super.testIncompatibleActivationKeysBase(key);
    }

    public void testCompatibleActivationKeys() throws Exception {
        ActivationKey key = ActivationKeyTest.createTestActivationKey(user);
        key.setContactMethod(ServerFactory.findContactMethodByLabel("ssh-push"));
        bootstrapper = mockRegistrationBootstrapper(Optional.of(key.getKey()));
        super.testCompatibleActivationKeysBase(key);
    }

    public void testCompatibleActivationKeysTunnel() throws Exception {
        ActivationKey key = ActivationKeyTest.createTestActivationKey(user);
        key.setContactMethod(ServerFactory.findContactMethodByLabel("ssh-push-tunnel"));
        bootstrapper = mockRegistrationBootstrapper(Optional.of(key.getKey()));
        super.testCompatibleActivationKeysBase(key);
    }

    @Override
    protected List<String> bootstrapMods() {
        return Arrays.asList("certs", "ssh_bootstrap");
    }

    @Override
    protected String getDefaultContactMethod() {
        return ContactMethodUtil.getSSHMinionDefault();
    }

    @Override
    protected Map<String, Object> createPillarData(Optional<ActivationKey> key) {
        Map<String, Object> pillarData = new HashMap<>();
        pillarData.put("mgr_server", ConfigDefaults.get().getCobblerHost());
        pillarData.put("mgr_origin_server", ConfigDefaults.get().getCobblerHost());
        pillarData.put("minion_id", "myhost");
        pillarData.put("contact_method", key
                .map(k -> k.getContactMethod().getLabel())
                .orElse(getDefaultContactMethod()));
        pillarData.put("mgr_sudo_user", "root");
        return pillarData;
    }
}
