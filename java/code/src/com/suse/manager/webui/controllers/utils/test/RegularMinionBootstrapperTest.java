/**
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

import com.google.gson.JsonPrimitive;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.token.test.ActivationKeyTest;

import com.suse.manager.webui.controllers.utils.AbstractMinionBootstrapper.BootstrapResult;
import com.suse.manager.webui.services.impl.SaltService.KeyStatus;
import com.suse.manager.webui.controllers.utils.ContactMethodUtil;
import com.suse.manager.webui.controllers.utils.RegularMinionBootstrapper;
import com.suse.manager.webui.utils.gson.BootstrapParameters;
import com.suse.manager.webui.utils.gson.BootstrapHostsJson;
import com.suse.salt.netapi.calls.wheel.Key;
import com.suse.salt.netapi.errors.JsonParsingError;
import com.suse.salt.netapi.errors.SaltError;
import com.suse.salt.netapi.results.Result;
import com.suse.salt.netapi.utils.Xor;
import org.jmock.Expectations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        BootstrapHostsJson input = mockStandardInput();
        setEmptyActivationKeys(input);

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

            // return failure when calling low-level bootstrap
            allowing(saltServiceMock).bootstrapMinion(with(any(BootstrapParameters.class)),
                    with(bootstrapMods), with(pillarData));
            will(returnValue(new Result<>(Xor.left(createGenericSaltError()))));

            // we expect the key to be deleted
            exactly(1).of(saltServiceMock).deleteKey("myhost");
        }});

        BootstrapParameters params = bootstrapper.createBootstrapParams(input);
        BootstrapResult bootstrap = bootstrapper.bootstrap(params, user, getDefaultContactMethod());
        assertFalse(bootstrap.isSuccess());
    }

    public void testIncompatibleActivationKeys() throws Exception {
        ActivationKey key = ActivationKeyTest.createTestActivationKey(user);
        key.setContactMethod(ServerFactory.findContactMethodByLabel("ssh-push"));
        super.testIncompatibleActivationKeysBase(key);
    }

    public void testIncompatibleActivationKeysTunnel() throws Exception {
        ActivationKey key = ActivationKeyTest.createTestActivationKey(user);
        key.setContactMethod(ServerFactory.findContactMethodByLabel("ssh-push-tunnel"));
        super.testIncompatibleActivationKeysBase(key);
    }

    public void testCompatibleActivationKeys() throws Exception {
        ActivationKey key = ActivationKeyTest.createTestActivationKey(user);
        key.setContactMethod(ServerFactory.findContactMethodByLabel("default"));
        super.testCompatibleActivationKeysBase(key);
    }

    @Override
    protected List<String> bootstrapMods() {
        return Arrays.asList("certs", "bootstrap");
    }

    @Override
    protected String getDefaultContactMethod() {
        return ContactMethodUtil.getRegularMinionDefault();
    }

    @Override
    protected Map<String, Object> createPillarData(Optional<ActivationKey> key) {
        Map<String, Object> pillarData = new HashMap<>();
        pillarData.put("mgr_server", ConfigDefaults.get().getCobblerHost());
        pillarData.put("mgr_origin_server", ConfigDefaults.get().getCobblerHost());
        pillarData.put("contact_method", key
                .map(k -> k.getContactMethod().getLabel())
                .orElse(getDefaultContactMethod()));
        pillarData.put("minion_id", "myhost");
        pillarData.put("minion_pub", "pubKey");
        pillarData.put("minion_pem", "privKey");
        pillarData.put("mgr_sudo_user", "root");
        return pillarData;
    }

    private SaltError createGenericSaltError() {
        return new JsonParsingError(
                new JsonPrimitive("salt error."),
                new Exception("exception causing the error."));
    }
}
