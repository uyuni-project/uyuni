package com.suse.manager.webui.controllers.utils.test;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.suse.manager.reactor.messaging.RegisterMinionEventMessageAction;
import com.suse.manager.webui.controllers.utils.SSHMinionBootstrapper;
import org.jmock.Expectations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        bootstrapper = new SSHMinionBootstrapper(saltServiceMock) {
            @Override
            protected RegisterMinionEventMessageAction getRegisterAction() {
                RegisterMinionEventMessageAction action =
                        mock(RegisterMinionEventMessageAction.class);
                context().checking(new Expectations() {{
                    allowing(action).registerSSHMinion("myhost");
                }});
                return action;
            }
        };
        super.testBootstrapSuccess();
    }

    @Override
    protected List<String> bootstrapMods() {
        return Arrays.asList("certs", "mgr_ssh_identity");
    }

    @Override
    protected Map<String, Object> createPillarData() {
        Map<String, Object> pillarData = new HashMap<>();
        pillarData.put("master", ConfigDefaults.get().getCobblerHost());
        pillarData.put("minion_id", "myhost");
        return pillarData;
    }
}
